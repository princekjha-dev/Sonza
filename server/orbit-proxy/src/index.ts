/**
 * Sonza Orbit AI Proxy
 *
 * Secure serverless gateway between Sonza Android clients and AI providers (Groq / OpenAI).
 *
 * Security Guarantees:
 * 1. Zero provider secrets are ever exposed to the client.
 * 2. In-memory sliding rate limiting protects against flooding.
 * 3. Strict payload size checks (max 16KB) guard against resource exhaustion.
 * 4. Optional PROXY_AUTH_TOKEN bearer authentication prevents unauthorized usage.
 * 5. Sanitized error messages ensure no internal keys, URLs, or traces leak.
 * 6. Zero persistence of user voice or conversational data.
 */

export interface Env {
  GROQ_API_KEY?: string;
  OPENAI_API_KEY?: string;
  GROQ_MODEL?: string;
  GROQ_BASE_URL?: string;
  GROQ_TIMEOUT_MS?: string;
  GROQ_MAX_RETRIES?: string;
  DEFAULT_PROVIDER?: string;
  RATE_LIMIT_MAX_PER_MINUTE?: string;
  MAX_BODY_SIZE_BYTES?: string;
  PROXY_AUTH_TOKEN?: string;
}

interface ChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

interface MusicContext {
  title?: string;
  artist?: string;
  album?: string;
  durationMs?: number;
  currentPositionMs?: number;
  isPlaying?: boolean;
  source?: string;
}

interface OrbitChatRequest {
  message: string;
  conversationId?: string;
  musicContext?: MusicContext;
  history?: Array<{ role: 'user' | 'assistant'; content: string }>;
  provider?: string;
}

interface OrbitToolCall {
  tool: string;
  arguments: Record<string, unknown>;
}

interface OrbitChatResponse {
  success: boolean;
  response: string;
  toolCalls?: OrbitToolCall[];
  conversationId?: string;
  error?: string;
}

// In-memory rate limiting tracking: IP -> array of timestamps
const rateLimitMap = new Map<string, number[]>();

function checkRateLimit(clientIp: string, maxRequestsPerMinute: number): boolean {
  const now = Date.now();
  const windowStart = now - 60_000;
  
  const timestamps = (rateLimitMap.get(clientIp) || []).filter((t) => t > windowStart);
  if (timestamps.length >= maxRequestsPerMinute) {
    return false;
  }
  timestamps.push(now);
  rateLimitMap.set(clientIp, timestamps);

  // Periodic cleanup if map grows
  if (rateLimitMap.size > 2000) {
    for (const [key, list] of rateLimitMap.entries()) {
      const valid = list.filter((t) => t > windowStart);
      if (valid.length === 0) {
        rateLimitMap.delete(key);
      } else {
        rateLimitMap.set(key, valid);
      }
    }
  }
  return true;
}

const CORS_HEADERS: Record<string, string> = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Orbit-Client-Token',
  'Content-Type': 'application/json',
};

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    const url = new URL(request.url);

    // Enforce HTTPS in production environments
    if (url.protocol === 'http:' && !url.hostname.includes('localhost') && !url.hostname.includes('127.0.0.1')) {
      return new Response(
        JSON.stringify({ success: false, error: 'HTTPS is required' }),
        { status: 403, headers: CORS_HEADERS }
      );
    }

    // Health check endpoint (safe metadata only)
    if (url.pathname === '/v1/orbit/health' && request.method === 'GET') {
      const configuredProvider = env.DEFAULT_PROVIDER || 'groq';
      const hasGroqKey = Boolean(env.GROQ_API_KEY && env.GROQ_API_KEY.length > 5);
      const hasOpenAiKey = Boolean(env.OPENAI_API_KEY && env.OPENAI_API_KEY.length > 5);
      
      return new Response(
        JSON.stringify({
          status: 'ok',
          service: 'sonza-orbit-proxy',
          provider: configuredProvider,
          ready: hasGroqKey || hasOpenAiKey,
          timestamp: new Date().toISOString()
        }),
        { status: 200, headers: CORS_HEADERS }
      );
    }

    // Main Orbit voice chat endpoint
    if (url.pathname === '/v1/orbit/chat' && request.method === 'POST') {
      return handleOrbitChat(request, env);
    }

    return new Response(
      JSON.stringify({ success: false, error: 'Endpoint not found' }),
      { status: 404, headers: CORS_HEADERS }
    );
  },
};

async function handleOrbitChat(request: Request, env: Env): Promise<Response> {
  const clientIp = request.headers.get('CF-Connecting-IP') || 'anonymous';
  const maxPerMinute = parseInt(env.RATE_LIMIT_MAX_PER_MINUTE || '60', 10);

  // 1. Rate Limiting Check
  if (!checkRateLimit(clientIp, maxPerMinute)) {
    return new Response(
      JSON.stringify({
        success: false,
        error: 'Too many requests. Please slow down.',
      }),
      { status: 429, headers: CORS_HEADERS }
    );
  }

  // 2. Optional Proxy Auth Token Check
  if (env.PROXY_AUTH_TOKEN && env.PROXY_AUTH_TOKEN.trim().length > 0) {
    const authHeader = request.headers.get('Authorization');
    const customHeader = request.headers.get('X-Orbit-Client-Token');
    const token = authHeader?.replace(/^Bearer\s+/i, '') || customHeader;

    if (token !== env.PROXY_AUTH_TOKEN.trim()) {
      return new Response(
        JSON.stringify({
          success: false,
          error: 'Unauthorized access to Orbit AI proxy.',
        }),
        { status: 401, headers: CORS_HEADERS }
      );
    }
  }

  // 3. Payload Size Limitation (16KB max)
  const maxBytes = parseInt(env.MAX_BODY_SIZE_BYTES || '16384', 10);
  const contentLength = parseInt(request.headers.get('content-length') || '0', 10);
  if (contentLength > maxBytes) {
    return new Response(
      JSON.stringify({
        success: false,
        error: 'Request payload exceeds allowable limit.',
      }),
      { status: 413, headers: CORS_HEADERS }
    );
  }

  // 4. Request Body Parsing & Validation
  let body: OrbitChatRequest;
  try {
    const text = await request.text();
    if (text.length > maxBytes) {
      return new Response(
        JSON.stringify({ success: false, error: 'Request too large.' }),
        { status: 413, headers: CORS_HEADERS }
      );
    }
    body = JSON.parse(text);
  } catch {
    return new Response(
      JSON.stringify({ success: false, error: 'Malformed JSON payload.' }),
      { status: 400, headers: CORS_HEADERS }
    );
  }

  const rawMessage = (body.message || '').trim();
  if (!rawMessage || rawMessage.length > 2000) {
    return new Response(
      JSON.stringify({
        success: false,
        error: 'Message must be between 1 and 2000 characters.',
      }),
      { status: 400, headers: CORS_HEADERS }
    );
  }

  // 5. Select Provider and Resolve Secret API Key
  const targetProvider = (body.provider || env.DEFAULT_PROVIDER || 'groq').toLowerCase();
  let apiKey: string | undefined;
  let baseUrl: string;
  let model: string;

  if (targetProvider === 'openai') {
    apiKey = env.OPENAI_API_KEY;
    baseUrl = 'https://api.openai.com/v1';
    model = 'gpt-4o-mini';
  } else {
    // Default: Groq
    apiKey = env.GROQ_API_KEY;
    baseUrl = env.GROQ_BASE_URL || 'https://api.groq.com/openai/v1';
    model = env.GROQ_MODEL || 'qwen/qwen3.8-27b';
  }

  if (!apiKey || apiKey.trim().length === 0) {
    return new Response(
      JSON.stringify({
        success: false,
        error: 'AI service is not configured on proxy. Set provider API key in backend environment.',
      }),
      { status: 503, headers: CORS_HEADERS }
    );
  }

  // 6. Build Context-Aware System Prompt
  const systemPrompt = buildSystemPrompt(body.musicContext);

  // 7. Build Messages Array
  const messages: ChatMessage[] = [{ role: 'system', content: systemPrompt }];

  if (Array.isArray(body.history)) {
    // Take up to last 8 turns for conversational context
    const sanitizedHistory = body.history.slice(-8).map((h) => ({
      role: h.role === 'assistant' ? ('assistant' as const) : ('user' as const),
      content: (h.content || '').substring(0, 1000),
    }));
    messages.push(...sanitizedHistory);
  }

  messages.push({ role: 'user', content: rawMessage });

  // 8. Call AI Provider with Timeout and Retry
  const timeoutMs = parseInt(env.GROQ_TIMEOUT_MS || '15000', 10);
  const maxRetries = parseInt(env.GROQ_MAX_RETRIES || '2', 10);

  const providerPayload = {
    model: model,
    messages: messages,
    temperature: 0.5,
    max_tokens: 300,
  };

  let lastError: string | null = null;
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), timeoutMs);

      const resp = await fetch(`${baseUrl}/chat/completions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${apiKey.trim()}`,
        },
        body: JSON.stringify(providerPayload),
        signal: controller.signal,
      });

      clearTimeout(timer);

      if (resp.ok) {
        const data = (await resp.json()) as {
          choices?: Array<{ message?: { content?: string } }>;
        };
        const content = data?.choices?.[0]?.message?.content?.trim() || '';
        
        // Parse tool calls if any formatted in response
        const { cleanText, toolCalls } = extractToolCalls(content);

        const responsePayload: OrbitChatResponse = {
          success: true,
          response: cleanText,
          toolCalls: toolCalls,
          conversationId: body.conversationId,
        };

        return new Response(JSON.stringify(responsePayload), {
          status: 200,
          headers: CORS_HEADERS,
        });
      }

      // Check if transient error worthy of retry
      if (resp.status >= 500 && attempt < maxRetries) {
        lastError = `Upstream error ${resp.status}`;
        await new Promise((res) => setTimeout(res, 500 * (attempt + 1)));
        continue;
      }

      // Safe non-leaking error message
      return new Response(
        JSON.stringify({
          success: false,
          error: 'AI service temporarily unavailable. Please try again shortly.',
        }),
        { status: 502, headers: CORS_HEADERS }
      );
    } catch (err: unknown) {
      const isAbort = (err as Error)?.name === 'AbortError';
      lastError = isAbort ? 'Request timed out' : 'Network failure';
      if (attempt < maxRetries) {
        await new Promise((res) => setTimeout(res, 500 * (attempt + 1)));
        continue;
      }
    }
  }

  return new Response(
    JSON.stringify({
      success: false,
      error: lastError || 'AI service request could not be completed.',
    }),
    { status: 504, headers: CORS_HEADERS }
  );
}

function buildSystemPrompt(musicContext?: MusicContext): string {
  let prompt = `You are Orbit, the personal voice assistant inside Sonza, an open-source Android music app.
You speak naturally, warmly, and concisely like Siri or Alexa.
Respond in 1-3 crisp, natural spoken sentences. Avoid markdown headers, asterisks, bullet points, or raw URLs because your output is read aloud via Text-to-Speech.`;

  if (musicContext && (musicContext.title || musicContext.artist)) {
    prompt += `\n\nCURRENT MUSIC CONTEXT:`;
    if (musicContext.title) prompt += `\n- Currently playing track: "${musicContext.title}"`;
    if (musicContext.artist) prompt += `\n- Artist: "${musicContext.artist}"`;
    if (musicContext.album) prompt += `\n- Album: "${musicContext.album}"`;
    prompt += `\n- Playback state: ${musicContext.isPlaying ? 'Playing' : 'Paused'}`;
    if (musicContext.currentPositionMs != null && musicContext.durationMs != null) {
      const currentSec = Math.floor(musicContext.currentPositionMs / 1000);
      const totalSec = Math.floor(musicContext.durationMs / 1000);
      prompt += `\n- Progress: ${currentSec}s / ${totalSec}s`;
    }
    prompt += `\nIf the user refers to "this song", "this artist", "who sings this", "what is this", "what does it mean", use this music context immediately.`;
  }

  prompt += `\n\nACTIONS & TOOLS:
When the user explicitly asks to control playback, search the web, call a contact, set a timer, or open an app, you may include an action tag at the very end of your response:
- [TOOL:play_music query="Song Title" artist="Artist Name"]
- [TOOL:playback_control action="pause"|"resume"|"next"|"previous"|"replay"]
- [TOOL:set_volume action="up"|"down"|level="50"]
- [TOOL:web_search query="search terms"]
- [TOOL:open_app app="app name"]
- [TOOL:set_timer minutes=5 label="timer label"]
- [TOOL:call_phone name="contact name"]
Always include a friendly spoken sentence before the tool tag (e.g., "Playing Unholy by Sam Smith. [TOOL:play_music query=\"Unholy\" artist=\"Sam Smith\"]").`;

  return prompt;
}

function extractToolCalls(rawText: string): { cleanText: string; toolCalls: OrbitToolCall[] } {
  const toolRegex = /\[TOOL:([a-z_]+)\s*([^\]]*)\]/gi;
  const toolCalls: OrbitToolCall[] = [];
  let match: RegExpExecArray | null;

  while ((match = toolRegex.exec(rawText)) !== null) {
    const toolName = match[1];
    const rawArgs = match[2];
    const args: Record<string, unknown> = {};

    // Parse key="value" or key=value pairs
    const paramRegex = /([a-z_]+)=(?:"([^"]*)"|'([^']*)'|(\S+))/gi;
    let paramMatch: RegExpExecArray | null;
    while ((paramMatch = paramRegex.exec(rawArgs)) !== null) {
      const key = paramMatch[1];
      const val = paramMatch[2] ?? paramMatch[3] ?? paramMatch[4];
      args[key] = val;
    }

    toolCalls.push({ tool: toolName, arguments: args });
  }

  const cleanText = rawText.replace(toolRegex, '').trim();
  return { cleanText, toolCalls };
}

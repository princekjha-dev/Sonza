# Sonza Orbit Secure AI Proxy

This is the secure serverless backend proxy for **Sonza Orbit**, the open-source Siri/Alexa-style voice assistant for Sonza.

## Architecture

```text
Sonza Android App
        ↓ (HTTPS, zero API keys)
 Secure Orbit Proxy (Cloudflare Worker)
        ↓ (Secure environment secret)
 Groq / OpenAI API
        ↓
 Secure Orbit Proxy
        ↓
Sonza Android App
```

### Security Principles

1. **Zero Client Secrets**: The Android application contains **zero provider API keys**. All upstream API secrets exist solely in the serverless environment.
2. **Abuse Protection**: Includes rate limiting, 16KB payload caps, and optional client bearer token verification.
3. **No Private Data Storage**: Audio streams and user queries are processed in-flight with zero server-side database logging or storage.
4. **Resilience**: Features configurable timeouts and automatic retries for transient failures.

---

## Deployment (Cloudflare Workers)

### Prerequisites

- Node.js 18+
- A Cloudflare account (free tier works great)
- An API key from [Groq Console](https://console.groq.com) or OpenAI

### 1. Install Dependencies

```bash
cd server/orbit-proxy
npm install
```

### 2. Configure Cloudflare Secrets

Run the following commands to securely set your provider API key. **Do not write your API key into files or git!**

```bash
# Set your Groq API Key
npx wrangler secret put GROQ_API_KEY
# Enter your Groq API key when prompted (e.g. gsk_...)

# (Optional) If using OpenAI as default
npx wrangler secret put OPENAI_API_KEY

# (Optional) If you want to protect your proxy with a client auth token
npx wrangler secret put PROXY_AUTH_TOKEN
```

### 3. Deploy to Cloudflare Workers

```bash
npx wrangler deploy
```

Upon completion, Cloudflare will provide your worker URL:
```text
https://sonza-orbit-proxy.<your-subdomain>.workers.dev
```

### 4. Verify Health

```bash
curl https://sonza-orbit-proxy.<your-subdomain>.workers.dev/v1/orbit/health
```

Expected output:
```json
{
  "status": "ok",
  "service": "sonza-orbit-proxy",
  "provider": "groq",
  "ready": true,
  "timestamp": "2026-10-16T05:00:00.000Z"
}
```

---

## Local Development & Testing

You can run the proxy locally using Wrangler:

```bash
# Create local development secrets file (.env.local is gitignored)
echo "GROQ_API_KEY=gsk_..." > .dev.vars

# Start local server
npm run dev
```

Test endpoint locally:
```bash
curl -X POST http://localhost:8787/v1/orbit/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Who sang Unholy?",
    "conversationId": "test-123"
  }'
```

---

## Connecting Sonza Android to your Proxy

1. Open **Sonza** on your device.
2. Go to **Settings** → **AI & Orbit Assistant**.
3. Under **Orbit AI Provider**, select **Groq (Cloud Proxy)**.
4. Enter your deployed Proxy URL:
   `https://sonza-orbit-proxy.<your-subdomain>.workers.dev`
5. (Optional) If you configured `PROXY_AUTH_TOKEN`, enter it in **Proxy Auth Token**.
6. Say *"Hey Orbit, play Unholy"* or *"Hey Orbit, who sang Unholy?"*!

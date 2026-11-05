# Sonza Orbit Voice Assistant Architecture & Deployment Guide

**Orbit** is Sonza's built-in, open-source Siri/Alexa-style voice assistant. It provides natural conversation, multi-turn follow-ups, full music playback awareness, and local tool execution while keeping the Sonza Android codebase 100% safe to publish publicly on GitHub with **zero private API keys**.

---

## 1. High-Level Architecture

```text
Sonza Android App
        ↓ (HTTPS with Zero API secrets)
 Orbit Voice Pipeline
 ┌────────────────────────────────────────────────────────┐
 │ Wake-word -> STT -> Context Memory -> Intent/Tool Router│
 └──────────────────────┬─────────────────────────────────┘
                        │
                        ▼
            OrbitAIProvider Abstraction
    ┌──────────────┬───────────────────┬──────────────┐
    │              │                   │              │
    ▼              ▼                   ▼              ▼
 Local AI     Groq Proxy          OpenAI Proxy    Custom
 (Offline)    (Serverless)        (Serverless)   (Endpoint)
                   │                   │
                   ▼                   ▼
           Cloudflare Worker   Cloudflare Worker
          (Holds GROQ_API_KEY)(Holds OPENAI_KEY)
                   │                   │
                   ▼                   ▼
               Groq API            OpenAI API
```

---

## 2. Core Security & Open-Source Principles

1. **Zero Provider Secrets in Android**:
   The Android application contains **no** `GROQ_API_KEY`, `OPENAI_API_KEY`, or third-party cloud credentials. Everything committed to the Git repository is safe for public open-source distribution.
2. **Serverless Proxy Pattern**:
   When using cloud providers (like Groq or OpenAI), API requests pass through a lightweight serverless proxy (such as Cloudflare Workers). The secret API key is injected server-side from environment secrets.
3. **Abuse Protection**:
   The proxy includes in-memory sliding-window rate limiting (60 requests/minute per client IP), payload caps (16KB), HTTPS enforcement, and optional client bearer tokens.
4. **No Storage of Conversation Transcripts**:
   User voice and text transcripts are never logged or stored server-side.
5. **Clean Local Fallback**:
   If the network is unavailable or the proxy is unconfigured, Orbit automatically falls back to `LocalAIProvider` so device controls, music context queries, and offline playback never break.

---

## 3. Orbit Voice Assistant Capabilities

### Siri/Alexa-Style Conversational Experience
Orbit supports multi-turn natural conversations without repeating "Hey Orbit" after every command:

```text
User:   "Hey Orbit, play Unholy."
Orbit:  "Playing Unholy by Sam Smith and Kim Petras."

User:   "Who sings this?"
Orbit:  "Unholy is performed by Sam Smith and Kim Petras."

User:   "What does it mean?"
Orbit:  "It explores themes of secrecy, betrayal, and a hidden relationship."

User:   "Give me similar songs."
Orbit:  "Playing songs similar to Unholy."
```

### Music Awareness
Orbit continuously maintains structured context of the active track:
- **Title**, **Artist**, **Album**
- **Duration** and **Current Playback Position**
- **Playback State** (Playing vs. Paused)
- **Source** (YouTube, Local, etc.)

Commands like:
- *"What song is this?"*
- *"Who made this?"*
- *"What does this song mean?"*
- *"Play that song again"*
- *"Play something similar"*
work naturally without needing to restate the song name.

### Action & Device Tools
Orbit does not blindly send every command to the cloud LLM. It routes commands intelligently:
- **MusicTool**: Play, pause, resume, skip, previous, replay, volume adjust, similar music mixes.
- **PhoneTool**: Initiates phone calls or opens dialer (`Intent.ACTION_DIAL`).
- **ReminderTool**: Sets sleep timers or alarms (`SleepTimerManager`).
- **AppTool**: Launches installed Android applications (e.g. Spotify, Settings, YouTube).
- **WebSearchTool**: Uses free, public endpoints (DuckDuckGo Instant Answers & Wikipedia API) to retrieve live knowledge without requiring any search API keys.

---

## 4. Provider Configurations

Users and developers can select their preferred provider in **Settings** → **AI & Orbit Assistant**:

```text
Orbit AI Provider
○ Local AI            (100% on-device, fully private, works offline)
● Groq (Cloud Proxy)  (Ultra-fast voice responses via secure proxy)
○ OpenAI (Cloud Proxy)
○ Custom Endpoint     (Ollama, LocalAI, vLLM, or self-hosted gateway)
```

### Setup 1: Local AI (Default Offline Mode)
- **Requirements**: None.
- Fully on-device. No network connection or API keys required.
- Capable of music context questions, device actions, volume controls, and playback.

### Setup 2: Groq via Cloudflare Workers Proxy (Recommended Cloud Mode)
- Ultra-low latency voice responses (sub-second TTS generation).
- Follow the proxy deployment instructions below to host your own free Cloudflare Worker.

### Setup 3: Self-Hosted / Custom Gateway
- Point **Orbit Proxy Base URL** to your self-hosted instance (e.g. `http://192.168.1.100:11434` for Ollama).

---

## 5. Deploying the Cloudflare Workers Proxy

The proxy source code is located in [`server/orbit-proxy/`](file:///home/naruto/Downloads/Sonza/server/orbit-proxy/).

### Step 1: Install Dependencies
```bash
cd server/orbit-proxy
npm install
```

### Step 2: Configure Serverless Secrets
Never commit your API keys to git. Inject them securely via Cloudflare Wrangler CLI:

```bash
# Add your Groq API Key
npx wrangler secret put GROQ_API_KEY
# Paste your key (e.g. gsk_...)

# (Optional) Add your OpenAI API Key
npx wrangler secret put OPENAI_API_KEY

# (Optional) Add a custom client token if you want private proxy authorization
npx wrangler secret put PROXY_AUTH_TOKEN
```

### Step 3: Deploy
```bash
npx wrangler deploy
```

Cloudflare will output your public proxy endpoint:
```text
https://sonza-orbit-proxy.<subdomain>.workers.dev
```

### Step 4: Verify Proxy Health
```bash
curl https://sonza-orbit-proxy.<subdomain>.workers.dev/v1/orbit/health
```

Expected response:
```json
{
  "status": "ok",
  "service": "sonza-orbit-proxy",
  "provider": "groq",
  "ready": true,
  "timestamp": "2026-10-16T05:00:00.000Z"
}
```

### Step 5: Connect Sonza Android App
1. In Sonza, go to **Settings** → **AI & Orbit Assistant**.
2. Select **Groq (Cloud Proxy)**.
3. Enter your deployed URL in **Orbit Proxy Base URL**:
   `https://sonza-orbit-proxy.<subdomain>.workers.dev`
4. If you configured `PROXY_AUTH_TOKEN`, enter it in **Proxy Client Auth Token**.

---

## 6. Building and Running Sonza Android

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17 or JDK 21
- Android SDK 35

### Running Unit Tests
```bash
./gradlew :app:testDebugUnitTest --tests "com.sonza.app.ai.orbit.*"
```

### Building the APK
```bash
./gradlew :app:assembleDebug
```

---

## 7. Security Audit Checklist

- [x] Zero API keys in Kotlin source code or XML resources.
- [x] Zero private keys in BuildConfig or Git history.
- [x] `.gitignore` ignores `.env`, `.env.*`, `*.secret`, `*.key`, `node_modules/`, and `.wrangler/`.
- [x] Android app communicates with AI providers strictly through the user's or self-hosted proxy.
- [x] Errors returned to the user or voice synthesizer are sanitized (no raw 401s, no leaked tokens, no stack traces).

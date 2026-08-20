# Sonza — Design System

> Living reference for UI/UX consistency across the app. Update this file whenever a new
> component, color, or pattern is introduced during the redesign. Screens should be built
> by referencing this doc, not by re-deriving styling ad hoc.

**Status:** Direction locked (Part 0). Component-level hex/spec values below are
implementation-ready; fill in `[ ]` placeholders only where a real asset/file path is
still pending.

---

## Part 0 — Visual Direction

- **Chosen direction:** *Dynamic Album-Driven Dark UI* — a fixed dark shell (nav, chrome,
  typography) with **per-screen accent color extracted from the active album art**,
  inspired by Apple Music's blurred dynamic backgrounds and Spotify's gradient-from-artwork
  "Now Playing" treatment.
- **Rationale:** Sonza's content is genre/mood-driven (Sleep, Relax, Sad, Romance) and
  artwork-heavy — a static single accent color flattens that variety. Deriving accent color
  live from album art gives every screen a distinct, premium feel without needing a large
  custom illustration system, and directly fixes the "looks like a stock template" problem
  since no two sessions look identical.
- **Not doing:** Literal copying of Spotify's green/black or Apple Music's exact red —
  this is an original palette (Part 1) with the *mechanic* (dynamic color, blur, bold type)
  borrowed, not the brand colors themselves.
- **Reference inspiration:** Apple Music dynamic Now Playing background + Spatial
  Audio/Lossless badges; Spotify album-gradient player + persistent mini-player + bottom
  tab bar.

---

## Part 1 — Color Palette

### Fixed (shell) tokens — same on every screen

| Token | Hex | Usage |
|---|---|---|
| `background` | `#0B0B0D` | Screen base — near-black, not pure black (reduces OLED smear, keeps depth) |
| `surface` | `#17171A` | Cards, sheets, elevated containers |
| `surface-variant` | `#232327` | Inactive chips, secondary containers |
| `on-background` | `#F5F5F7` | Primary text on background |
| `on-surface` | `#F5F5F7` | Primary text on surface |
| `on-surface-variant` | `#A1A1A8` | Secondary/muted text |
| `outline` | `#2E2E33` | Borders, dividers |
| `error` | `#FF6B6B` | Error states, destructive actions |
| `on-error` | `#0B0B0D` | Text/icons on `error` |
| `success` | `#4ADE80` | Confirmations (e.g. "added to library") |
| `warning` | `#FBBF24` | Stale/offline banners |
| `scrim` | `#000000` @ 45% | Gradient overlay on album art for text legibility |

### Dynamic (per-screen) tokens — derived at runtime from active album art

| Token | Derivation | Usage |
|---|---|---|
| `accent` | Dominant/vibrant color extracted from current album art (Android Palette API) | Play button, active slider, highlight text, screen-specific gradient top |
| `accent-muted` | `accent` at 20–30% opacity over `background` | Subtle background gradient wash, not full-bleed color |
| `on-accent` | Computed (black or `on-background`) based on `accent` luminance | Text/icons on top of `accent` — always contrast-checked at runtime |

**Fallback:** when no album art is available (e.g. app launch, generic playlist), `accent`
defaults to a fixed brand token: `#5B8DEF` (a cool blue distinct from both Spotify green
and Apple Music red).

**Rules:**
- Minimum contrast ratio **4.5:1** body text, **3:1** large text (WCAG AA) — this must be
  runtime-checked for `on-accent`, since `accent` is dynamic and could be light or dark.
- `accent` is used as a *wash/highlight*, never as a full-screen flat fill — cap gradient
  opacity so `on-background` text stays legible everywhere.
- Dark theme only for v1; light theme is future scope.

---

## Part 2 — Typography

Bold, confident hierarchy — Apple Music's biggest UI strength is type weight contrast, not
just size. Lean into that.

| Style | Size | Weight | Line Height | Usage |
|---|---|---|---|---|
| `display` | 34sp | Bold (700) | 40sp | Now Playing track title (expanded sheet) |
| `headline` | 26sp | Bold (700) | 32sp | Screen titles ("Speed dial", "Listen again") |
| `title-large` | 20sp | SemiBold (600) | 26sp | Section headers |
| `title-medium` | 16sp | SemiBold (600) | 22sp | Card titles, mini-player track name |
| `body-large` | 16sp | Regular (400) | 24sp | Primary readable text |
| `body-medium` | 14sp | Regular (400) | 20sp | Secondary text, artist names |
| `label-large` | 14sp | Medium (500) | 20sp | Buttons, chip labels |
| `label-small` | 12sp | Medium (500) | 16sp | Timestamps, metadata, format badges |

**Font family:** `Manrope` — geometric, confident at bold weights, distinct from both
Spotify's Circular and Apple's SF Pro, and avoids default-Roboto "templated" feel.

**Rules:**
- Never go below `label-small` (12sp).
- Track/song titles: 1 line + ellipsis in grids, 2 lines max in Now Playing.
- Bold weight (700) reserved for titles only — never body text, to keep the hierarchy
  Apple Music achieves.

---

## Part 3 — Spacing Scale

4dp base unit:

| Token | Value | Usage |
|---|---|---|
| `space-xs` | 4dp | Icon-to-label gaps |
| `space-sm` | 8dp | Chip padding, small gaps |
| `space-md` | 12dp | Default gap between related elements |
| `space-lg` | 16dp | Standard screen margin, card padding |
| `space-xl` | 24dp | Section-to-section spacing |
| `space-2xl` | 32dp | Major layout breaks |

**Rules:**
- Screen horizontal margins: `space-lg` (16dp) minimum.
- Grid item spacing: `space-sm` (8dp).
- Always reference the token — never raw dp values in layouts/composables.

---

## Part 4 — Shape & Corner Radius

| Token | Radius | Usage |
|---|---|---|
| `radius-sm` | 8dp | Small chips, format badges |
| `radius-md` | 12dp | Buttons, mini-player |
| `radius-lg` | 16dp | Cards (speed dial tiles, playlist cards) |
| `radius-pill` | 999dp | Genre pills, tags |
| `radius-circle` | 50% | Avatars, play/pause FAB, Now Playing artwork (large) |

---

## Part 5 — Elevation & Blur

| Level | Usage | Implementation notes |
|---|---|---|
| `elevation-0` | Base screen background | No shadow |
| `elevation-1` | Standard cards | Subtle shadow or `surface` tone shift |
| `elevation-2` | Bottom nav, mini-player | Shadow + **background blur** (12dp radial blur of content behind, à la Apple Music) rather than a flat scrim |
| `elevation-3` | Modals, expanded Now Playing sheet | Full-bleed blurred/dynamic-color background derived from album art, strongest shadow |

**Rules:**
- Mini-player and bottom nav use blur-behind, not just opacity — this is the single
  biggest "premium vs template" visual cue from both reference apps.
- If blur is a performance concern on lower-end devices, fall back to `surface` @ 92%
  opacity + shadow — never a fully opaque flat bar.

---

## Part 6 — Component Patterns

### 6.1 Cards (Speed Dial / Grid)
- **Standard tile:** square, `radius-lg`, title overlaid bottom with `scrim` gradient.
- **Featured/hero tile:** 2x width span, top of Home only, same radius, may show
  `accent-muted` wash derived from its own artwork.
- Defined in: `HomeComponents.kt` (`FeaturedHeroCard`, `SquareSongCard`, `PlaylistDisplayCard`), `HomeScreen.kt` (`SpeedDialTile`, `SpeedDialGrid`)

### 6.2 Genre Pills
- Horizontal scroll, `radius-pill`, `space-sm` internal padding.
- **Selected:** filled `accent` background (or fixed brand blue if no dynamic context), `on-accent` text.
- **Unselected:** `surface-variant` background, `on-surface-variant` text, `outline` border.
- Defined in: `MoodChips.kt` (`MoodChipsSection`, `MoodChip`)

### 6.3 Bottom Navigation
- 4 items: Home, Search, Your Library, Settings. Fixed on mobile (per both reference apps).
- **Active:** filled icon variant, `accent` tint (falls back to fixed brand blue outside a playback context), label visible.
- **Inactive:** outlined icon, `on-surface-variant` tint.
- Background: `elevation-2` blur treatment (see Part 5).
- Transition: 150ms scale/color ease-out on tab change.
- Defined in: `ExpressiveBottomNav.kt` (`ExpressiveBottomNav`, `StandardNavBar`, `LiquidGlassNavBar`)

### 6.4 Mini-Player
- Fixed bar above bottom nav, `elevation-2` blur background.
- Shows: album art thumbnail, title/artist (marquee if overflow), play/pause, next.
- Subtle `accent-muted` wash matching current track's artwork.
- **Buffering state:** thin linear progress line under the bar (reflects live stream
  resolution isn't instant) — replaces static play icon briefly with a small spinner.
- Tap to expand → Now Playing sheet (`elevation-3`, full dynamic background).
- Defined in: `StandardMiniPlayer.kt`, `LiquidGlassMiniPlayer.kt`, `ExpandablePlayerSheet.kt`

### 6.5 Now Playing (Expanded Sheet)
- Full-bleed blurred background generated from album art (Apple Music-style), `scrim`
  overlay for text legibility.
- Large artwork (`radius-lg`, not circular — matches both reference apps for main art).
- `display` style track title, `body-large` artist name.
- Progress slider tinted `accent`.
- Playback controls: play/pause, skip, shuffle, repeat — 32dp icon size.
- **Format badges** (see 6.6) shown near track title when applicable.
- Defined in: `[ file/composable name once implemented ]`

### 6.6 Format/Quality Badges
- Small pill (`radius-sm`, `label-small`), e.g. "HQ" / "Downloaded" / "Lyrics" — inspired
  by Apple Music's Dolby Atmos/Lossless badges, adapted to whatever qualities Sonza
  actually supports (do not claim Dolby Atmos/Lossless certification language — use
  neutral labels like "High Quality" unless the app is actually certified).
- Muted `surface-variant` background, `on-surface-variant` text, so it doesn't compete
  with `accent`.
- Defined in: `[ file/composable name once implemented ]`

### 6.7 State Components *(priority — see redesign goals)*

Wraps existing ViewModel data in a UI-state sealed class (`Loading` / `Success` / `Error` /
`Stale`) without touching data-fetching logic.

| State | Component | Behavior |
|---|---|---|
| **Loading** | Skeleton loader (`SearchResultsSkeleton`) | Shimmer placeholder shaped like destination content — not a spinner |
| **Buffering (play tap)** | Inline mini-player progress | Thin progress line + brief spinner on tap, see 6.4 |
| **Stale/Offline** | Banner (`SonzaBanner`) | Non-blocking top banner: `"Showing saved results"` / `"You're offline"` — `warning` token, dismissible |
| **Error** | Full-state error (`SonzaErrorState`) | Icon + message + **Retry** button with `accent` styling — never blank |
| **Empty** | Full-state empty (`SonzaEmptyState`) | Icon + title + description + actionable CTA button |
| **Mid-playback failure** | Snackbar/toast | `"Playback interrupted — tap to retry"` — non-blocking, manual retry |
| **Lyrics unavailable** | Inline message | `"Lyrics not available for this track"` — only after all providers exhausted |

Defined in: `com.sonza.app.ui.components.StateComponents.kt`

---

## Part 7 — Iconography

- **Source:** Material Symbols Rounded (rounded terminals suit the softer, blurred aesthetic better than sharp/outlined).
- **Weight:** 400 default; filled variant for active/selected states.
- **Size:** 24dp standard, 20dp inline/small, 32dp primary playback controls.
- All icons require `contentDescription` (Compose) / `android:contentDescription` (XML) — no exceptions.

---

## Part 8 — Motion

| Interaction | Duration | Easing |
|---|---|---|
| Tab/nav selection | 150ms | Standard ease-out |
| Card tap feedback | 100ms | Ease-out (scale 0.97) |
| Accent color cross-fade (track change) | 400ms | Ease-in-out — color should *melt* between tracks, not snap |
| Skeleton shimmer | 1200ms loop | Linear |
| Banner enter/exit | 200ms | Ease-in-out, slide + fade |
| Now Playing sheet expand | 300ms | Ease-out, background blur fades in with it |

---

## Part 9 — Accessibility Checklist

- [ ] All text meets WCAG AA contrast (4.5:1 body, 3:1 large) — **including runtime-computed `on-accent` pairings**
- [ ] All touch targets ≥ 48dp
- [ ] All icons have content descriptions
- [ ] Loading/error/offline states are announced to screen readers, not just visual
- [ ] Color is never the only indicator of state (pair `accent`/`error`/`warning` with icon or text)
- [ ] Dynamic accent color always has a tested fallback (`#5B8DEF`) when extraction fails or artwork is missing

---

## Change Log

| Date | Change | Component(s) affected |
|---|---|---|
| 2026-08-19 | Initial draft created | — |
| 2026-08-19 | Direction locked: Dynamic Album-Driven Dark UI (Apple Music / Spotify inspired) | Part 0, Part 1 (dynamic tokens), Part 5 (blur), 6.5, 6.6 added |
| 2026-08-19 | Phase 1 & 2 Completed: Foundation (Fixed tokens, Manrope variable font, Spacing, Shapes, Elevation, Motion) & Dynamic Accent Color System (Palette extraction, contrast checking, 400ms crossfade) | `Color.kt`, `Type.kt`, `SpacingTokens.kt`, `Shapes.kt`, `ElevationTokens.kt`, `MotionTokens.kt`, `DominantColorExtractor.kt`, `Theme.kt` |
| 2026-08-19 | Phase 3 Completed: Blur-Behind Components (RenderEffect on API 31+, Surface @ 92% + shadow on API <31 for Bottom Nav and Mini-Player) | `ExpressiveBottomNav.kt`, `StandardMiniPlayer.kt`, `LiquidGlassMiniPlayer.kt`, `LiquidGlassSurface.kt`, `ExpandablePlayerSheet.kt` |
| 2026-08-19 | Phase 4a Completed: Home Screen (Featured hero card, Speed dial grid with dynamic accent & scrim, Genre pills with dynamic accent & outline) | `HomeComponents.kt`, `HomeScreen.kt`, `MoodChips.kt` |
| 2026-08-20 | Phase 4b Completed: Search & Home Design-System Alignment, Emoji elimination with 20dp Material Symbols, Interactive Sonza result cards (scale 0.97 / 100ms), State Components (`SonzaEmptyState`, `SonzaErrorState`, `SonzaBanner`), and Media3 Notification Channel alignment (`media_playback_channel`) | `StateComponents.kt`, `MoodChips.kt`, `SearchScreen.kt`, `HomeComponents.kt`, `HomeScreen.kt`, `MusicPlayerService.kt`, `RemoteAudioRepository.kt` |



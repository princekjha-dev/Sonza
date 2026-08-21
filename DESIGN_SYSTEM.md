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

Unified, clean, confident hierarchy matching the premium music streaming reference:

| Category / Semantic Token | Size | Weight | Line Height | Letter Spacing | Usage |
|---|---|---|---|---|---|
| `SonzaTypography.PageTitle` (`Display`) | 32sp | Bold (700) | 38sp | -0.5sp | Large top screen headers ("New", "Search", "Library") |
| `SonzaTypography.SectionTitle` (`Headline`) | 20sp | Bold (700) | 26sp | -0.3sp | Standardized section headers ("Best New Songs", "New This Week", "Recent Releases") |
| `SonzaTypography.Kicker` | 11sp | Bold (700) | 14sp | 0.8sp | Uppercase super-headers/badges ("UPDATED PLAYLIST", "NEW ALBUM") |
| `SonzaTypography.SongTitle` (`TitleMedium`) | 15sp | SemiBold (600) | 20sp | 0sp | All song rows, list items, mini player track titles (1 line max) |
| `SonzaTypography.ArtistSubtitle` (`BodyMedium`) | 13sp | Regular (400) | 18sp | 0.1sp | Subordinate artist names in song rows, list items, and descriptions |
| `SonzaTypography.CardTitle` (`TitleSmall`) | 14sp | SemiBold (600) | 18sp | 0sp | Album/playlist/mix cards across carousels and discovery grids |
| `SonzaTypography.CardSubtitle` (`BodySmall`) | 12sp | Regular (400) | 16sp | 0.1sp | Subordinate creator/track count/year on cards |
| `SonzaTypography.NavLabel` (`LabelSmall`) | 11sp | Medium (500) / SemiBold (600) | 14sp | 0.2sp | Bottom navigation labels, tab indicators |
| `SonzaTypography.Metadata` | 11sp | Medium (500) | 14sp | 0.2sp | Duration, format badges (LOSSLESS, HI-RES), timestamp indicators |

**Font family:** `Manrope` — geometric variable font with weights Normal (400), Medium (500), SemiBold (600), Bold (700), ExtraBold (800).

**Typography Rules:**
1. **Identical Category Formatting**: All section headers (`Best New Songs`, `New This Week`, `Recent Releases`, `Latest Songs`, `Top Playlists`, etc.) must use `SonzaTypography.SectionTitle` (20sp Bold) with identical margin/padding across every screen.
2. **Song Titles & Artist Subtitles**: Every song title across Home, Search, Library, Playlist, Album, and Artist screens must use `SonzaTypography.SongTitle` (15sp SemiBold) and `SonzaTypography.ArtistSubtitle` (13sp Regular).
3. **Title-to-Subtitle Spacing**: Standardized to exactly `2.dp` (`SpacingTokens.Space2Xs`) for all song rows and card info columns.
4. **Single-line Truncation**: Song titles and artist names in lists truncate with ellipsis at 1 line.
5. **No Ad-Hoc Font Sizing**: Screens must not declare ad-hoc font sizes or random bold weights; all text must bind to `SonzaTypography` tokens.

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

### 6.3 Bottom Navigation (Idle State)
- 4 items: Home, Search, Library, Settings. Fixed at the bottom on mobile.
- **Active:** filled icon variant (`Icons.Filled.*`), dynamic `accent` tint, `NavLabel` (11.5sp Manrope) `SemiBold` (600) label visible.
- **Inactive:** outlined icon variant (`Icons.Outlined.*`), neutral gray (`on-surface-variant` @ 70%) tint, `NavLabel` (11.5sp Manrope) `Medium` (500) label visible.
- **Iconography:** standard 24dp Material Icons (`Home`, `Search`, `LibraryMusic`, `Settings`).
- **Surface:** Clean, minimal, non-floating bar seamlessly integrated with Sonza's dark background (`SonzaBackground`). No background panel surrounding all four tabs, no outer border, no blur/glass effect, no floating container, and no empty player space. Extends edge-to-edge behind system navigation bar with `navigationBarsPadding()`.
- **Accessibility:** Full touch targets (`fillMaxHeight()`, min 48dp), `Role.Tab` semantics with dynamic TalkBack announcements (`"Home, selected"`, `"Search"`, `"Library"`, `"Settings"`).
- **Transition:** 150ms `FastOutSlowInEasing` color transition on tab change (`NavSelectionDuration`). Zero layout shift on state change.
- Defined in: `ExpressiveBottomNav.kt` (`ExpressiveBottomNav`)

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

### 6.8 Search Flow (3-Phase Model)
- **Phase 1: Initial Discovery Screen:** Source-agnostic landing. Displays large "Search" headline, resting search bar ("Search for songs, artists, or albums"), recent searches preview (if history exists), and "Browse all" category tiles with vibrant gradients. Zero technical source labels.
- **Phase 2: Active Search Mode:** Smooth animated transition upon tapping search bar. Displays leading `←` back arrow and trailing `X` clear button. `BackHandler` dismisses keyboard and exits active mode on first press. When query is empty, displays recent search history with individual delete `X` buttons (TalkBack: "Remove X from search history") and "Clear all" button, or `SonzaEmptyState` if history is empty. As user types, shows live query suggestions.
- **Phase 3: Search Results Mode:** Preserves query in search bar. Displays category filter chips ([All], [Songs], [Videos], [Albums], [Artists], [Playlists]). YouTube Music default search catalogue. Rich result items with 0.97 scale feedback, `SearchResultsSkeleton` during load, `SonzaEmptyState` for zero results, and `SonzaErrorState` for failures.
- Defined in: `SearchScreen.kt`, `SearchViewModel.kt`, `SessionManager.kt`

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

## Part M — Search Experience Redesign
- Consolidated search logic into 3-phase flow.
- Added recent history persistence and deletion UI.
- Implemented full TalkBack semantics for search controls.
- Defined source-agnostic search bar requirements.
- Standardized YouTube Music integration for search results.

---

## Part Q — Search Results Screen & Mini Player Redesign
- **Search Results Density & List Rows:** Replaced heavy generic cards with high-density, compact music list rows (56dp height, 48dp artwork, primary bold title, subordinate artist/metadata, 20dp `MoreVert` action icon, 0.97 scale tap feedback). Redundant "Song • " text removed.
- **Horizontal Category Pills:** Replaced generic segmented button with horizontally scrollable pill-shaped category tabs (`All`, `Songs`, `Videos`, `Albums`, `Artists`, `Playlists`) featuring 150ms smooth animated color and border transitions.
- **Curated "All" Results Tab:** Structured music grouping with artists carousel, albums carousel, playlists carousel, and compact song list rows.
- **Dynamic Content Insets:** Inset calculation (`navBarPadding + navBarHeight + miniPlayerHeight + 16.dp`) ensures the last result row scrolls completely above the floating Mini Player and Bottom Navigation.
- **Floating Mini Player:** Elevated floating player container (`RadiusTokens.Md` / 12dp rounded corners, subtle shadow/blur, 8dp margins) sitting above the bottom navigation bar with responsive playback controls, vinyl spin artwork, marquee title, and edge progress line.
- **Zero Technical Terminology:** Absolute elimination of technical backend strings ("HQ Audio", "HQ source busy", "YouTube fallback", "Resolver") in user-facing search and playback components.

---

## Part P — Settings Screen Redesign & Data Cleanup
- **Information Architecture:** Reorganized Settings into clean functional groups: *Account*, *Player & Audio*, *General & Appearance*, *Storage & Data*, *About & Support*, and *Updates*.
- **Reusable `SonzaSettingsRow`:** Unified row component with 40dp tinted squircle icon box, `SonzaTypography.TitleMedium` (15sp SemiBold) title, `SonzaTypography.BodyMedium` (13sp) subtitle, forward indicator/badge/switch, 0.97 tap bounce feedback, and `Role.Button`/`Role.Switch` TalkBack semantics.
- **Dynamic Version Sourcing:** Version dynamically retrieved directly from `BuildConfig.VERSION_NAME` ("2.6.5.1"), eliminating static/stale version strings.
- **Real Destinations & Data Integrity:**
  - *Support Sonza:* Navigates to the native Support & feedback destination (`Destination.Support`).
  - *Privacy Policy:* Opens real URL (`https://princekjha-dev.github.io/Sonza-Website/sonza-privacy.html`).
  - *About Sonza:* Navigates to About destination (`Destination.About`).
  - *Update Channel:* Allows selection between configured channels (Stable, Beta, Nightly).
  - *Check for Updates:* Performs real OTA check via `UpdateViewModel` with user-friendly messages ("You're up to date", "Update available", "Couldn't check for updates") with zero technical HTTP/stack trace exposure.
- **Dynamic Bottom Insets:** Full edge-to-edge calculation ensuring "Check for Updates" is fully accessible above the Bottom Navigation Bar and Mini Player.

---

### 6.9 Image-Driven Music Discovery Cards (Search Redesign)
- **Full-Bleed Visual Background:** Replaces flat solid-color blocks with high-resolution music and artist photography rendered edge-to-edge inside 16dp rounded cards (`RadiusTokens.Lg`).
- **Duotone Color Tint & Scrim:** Curated vibrant theme tints (rose pink, golden amber, royal purple, crimson ruby, electric blue, cyan, etc.) blended directly over the photography with vertical protective dark scrim gradients ensuring 100% WCAG AA text contrast.
- **Direct Typography Overlay:** High-contrast bold Manrope titles (`SonzaTypography.TitleMedium`, 15–17sp Bold `#FFFFFF`) placed directly over the artwork with subtle text shadow.
- **Two-Column Discovery Grid:** Balanced 2-column layout with 12dp spacing (`SpacingTokens.SpaceMd`), smooth 0.97 tap bounce feedback, and dynamic bottom insets scrolling cleanly above the floating Mini Player and Bottom Navigation.
- Defined in: `DiscoveryArtRegistry.kt`, `BrowseCategoryCard.kt`, `CategoryCard.kt`, `SearchScreen.kt`, `SearchViewModel.kt`

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
| 2026-08-20 | Part E Completed: Bottom Navigation Visual Consistency Fix (Standardized 24dp Material Icons with `LibraryMusic`, Manrope 12sp `SonzaTypography.LabelSmall`, fixed shell `SonzaSurface` blur-behind container with edge-to-edge support, 64dp standard height, 150ms `MotionTokens.NavSelectionDuration` transitions without layout shift) | `ExpressiveBottomNav.kt`, `TvNavigationRail.kt`, `MainActivity.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-20 | Part F Completed: YouTube Music Default Search Source (Default search tab set to YouTube Music with immediate data flow, search bar focused on search input, HQ Audio tab preserved for cross-catalogue search) | `SearchViewModel.kt`, `SearchScreen.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-20 | Part L Completed: Bottom Navigation Redesign (Solid non-bleeding elevated surface, 4 balanced columns for Home, Search, Your Library, Settings, 24dp Material Symbols, Manrope LabelSmall typography, Role.Tab TalkBack accessibility, 150ms animated transitions, edge-to-edge system insets) | `ExpressiveBottomNav.kt`, `TvNavigationRail.kt`, `AdaptiveNavigationRail.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-21 | Part M Completed: Search Experience Redesign (3-phase music search flow, source-agnostic search bar, recent search history persistence & deletion, TalkBack accessibility semantics, two-step back handler, animated transitions, YouTube Music default results) | `SearchScreen.kt`, `SearchViewModel.kt`, `SessionManager.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-21 | Part Q Completed: Search Results Screen Redesign & Rebuild (Compact 56dp music result rows, horizontal category pill navigation, structured All tab carousels, dynamic bottom insets for floating mini player, zero technical source strings) | `SearchScreen.kt`, `StandardMiniPlayer.kt`, `YTMusicMiniPlayer.kt`, `ExpandablePlayerSheet.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-21 | Part P Completed: Settings Screen Redesign & Data Cleanup (Reorganized sections into clear hierarchy, unified `SonzaSettingsRow` component with 0.97 bounce feedback, dynamic `BuildConfig.VERSION_NAME` 2.6.5.1, live OTA update check without technical leakage, real Support/Credits/Privacy Policy/About destinations, dynamic bottom insets) | `SettingsScreen.kt`, `SettingsViewModel.kt`, `NavGraph.kt`, `DESIGN_SYSTEM.md` |
| 2026-08-21 | Part R Completed: Search Screen Visual Redesign (Image-driven music discovery cards with full-bleed artist photography, vibrant duotone tints, protective gradient scrims, bold typography overlays, prominent Search header, 2-column responsive layout, and DiscoveryArtRegistry) | `DiscoveryArtRegistry.kt`, `BrowseCategoryCard.kt`, `CategoryCard.kt`, `SearchScreen.kt`, `SearchViewModel.kt`, `DESIGN_SYSTEM.md` |




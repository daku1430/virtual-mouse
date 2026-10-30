Build a complete Android app "Virtual Mouse" in this GitHub repo and make it compile on GitHub Actions. I will NOT compile locally. Commit all files directly to the repo.

## What the app does
A transparent rectangle trackpad overlay that floats above any app. Dragging on it moves an on-screen mouse arrow cursor, and taps on the trackpad perform real clicks in the app underneath. It is turned on/off from a Quick Settings tile.

## Tech
- Kotlin, minSdk 33 (Android 13+ only), targetSdk 34, compileSdk 34
- Package com.virtualmouse.app
- Gradle (Kotlin DSL), AndroidX, no heavy libraries
- Single module "app"
- Do NOT add compatibility code for older Android versions (no Build.VERSION checks, no legacy fallbacks). Use modern APIs directly:
  - TileService.startActivityAndCollapse(PendingIntent) for opening settings from the tile
  - Tile.setSubtitle() to show "On"/"Off" under the tile label

## Components
1. **MouseAccessibilityService** (AccessibilityService)
   - Needed so clicks/scroll/gestures can be injected into other apps via dispatchGesture(). Set canPerformGestures="true" in accessibility_service_config.xml.
   - Draw both overlays with WindowManager using TYPE_ACCESSIBILITY_OVERLAY (so no SYSTEM_ALERT_WINDOW permission is needed).
   - Keep a static/companion reference to the running instance so the tile can toggle it.

2. **Trackpad overlay**
   - Rounded rectangle, semi-transparent (about 25% alpha) with a thin border. Default size about 280dp x 180dp, bottom-right of the screen.
   - Draggable by a small handle at its top edge, resizable via a corner handle, and position/size saved in SharedPreferences.
   - Window flags: FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS, so everything outside the trackpad passes touches to the apps below.

3. **Cursor overlay**
   - Separate full-screen, non-touchable overlay (FLAG_NOT_TOUCHABLE) drawing an arrow cursor at (x, y).
   - Include 3 cursor icons as vector drawables (classic arrow white with black outline, black arrow with white outline, and a small hand/pointer). Selectable in a settings screen, saved in preferences.
   - Cursor starts at screen center and is clamped to screen bounds.

4. **Trackpad behavior**
   - One-finger drag: moves the cursor relatively (delta x pointer speed multiplier). It must feel like a laptop trackpad: no jumping, with a slight acceleration curve.
   - Single tap: left click at the cursor position (dispatchGesture tap, 50ms).
   - Double tap: two quick clicks.
   - Tap, then press-and-drag: click-and-drag (a continuous gesture with a path, so it can drag icons or select text).
   - Long press (about 500ms) without moving: long click.
   - Two-finger tap: right click, implemented as a long press or GLOBAL_ACTION_BACK, with a toggle in settings.
   - Two-finger drag: scroll, via a swipe gesture starting at the cursor position.
   - Ignore accidental touches while the finger is outside the rectangle.

5. **Quick Settings tile** (TileService, "Virtual Mouse")
   - Tapping toggles the overlay (trackpad plus cursor) on/off, with STATE_ACTIVE/STATE_INACTIVE, an icon, and a subtitle "On"/"Off".
   - If the accessibility service is not enabled, tapping opens the Accessibility settings.
   - Keep the tile state in sync in onStartListening().

6. **MainActivity** (simple Material 3 screen)
   - Setup status: Accessibility service on/off, with a button to open Accessibility settings.
   - Short instructions: "Enable the service, then add the Virtual Mouse tile to Quick Settings."
   - Android 13+ sideload note: "If the Accessibility toggle is greyed out: App info → ⋮ menu → Allow restricted settings, then enable the service."
   - Settings: pointer speed slider, trackpad opacity slider, cursor icon picker, right-click mode toggle, and a Reset position button.

## GitHub Actions (important, no local build)
Create `.github/workflows/build.yml`:
- Trigger on push to main and workflow_dispatch
- ubuntu-latest, actions/checkout@v4, actions/setup-java@v4 (temurin 17), android-actions/setup-android@v3
- Use gradle/actions/setup-gradle@v4 with a pinned gradle-version (8.7) and run `gradle assembleDebug --stacktrace`. Do not rely on a gradle-wrapper.jar; if you commit the wrapper, make sure it's valid.
- Upload app/build/outputs/apk/debug/*.apk with actions/upload-artifact@v4 as "VirtualMouse-debug-apk"
- Create a GitHub Release with the APK attached on each push to main (softprops/action-gh-release).

## Rules
- All the files must be complete and ready to build, with no placeholders or TODOs: settings.gradle.kts, build.gradle.kts (root and app), gradle.properties (android.useAndroidX=true), AndroidManifest.xml, resources, vector icons, and the workflow.
- Check the code mentally for compile errors, because I can't test locally. If the Actions build fails, I will paste the error log and you fix it.
- Add a README.md with setup steps (enable accessibility, allow restricted settings, add the tile).
- Privacy: the app uses accessibility only to perform taps/scrolls and reads no screen content (set accessibilityEventTypes to none and don't read any events).

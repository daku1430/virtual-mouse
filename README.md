# Virtual Mouse

A transparent rectangle trackpad overlay that floats above any app. Dragging on it moves an on-screen mouse arrow cursor, and taps on the trackpad perform real clicks in the app underneath.

## Features

- Touchpad overlay with drag, tap, double-tap, long-press, and scroll support
- Visual mouse cursor with 3 icon options
- Quick Settings tile for easy on/off toggle
- Accessibility Service for gesture injection (no root required)

## Setup

1. **Enable the Accessibility Service:**
   - Open Settings → Accessibility → Virtual Mouse
   - Enable the service
   
2. **Allow Restricted Settings (Android 13+):**
   - If the Accessibility toggle is greyed out:
   - Go to App info → ⋮ menu → Allow restricted settings
   - Then enable the service

3. **Add the Quick Settings Tile:**
   - Pull down the Quick Settings panel
   - Edit the tiles
   - Add "Virtual Mouse" tile
   - Tap the tile to start using

## Requirements

- Android 13+ (minSdk 33)
- No root required (uses Accessibility Service for gesture injection)

## Building

The app builds automatically on GitHub Actions on each push to main.

```bash
./gradlew assembleDebug
```

Debug APK will be available in `app/build/outputs/apk/debug/`

## License

MIT License
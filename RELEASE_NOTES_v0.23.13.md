# AADisplay v0.23.13

Android Auto 17.8 — session kill switch, controller improvements, and navigation-app cleanup.

## What's new

### Session kill switch (return to Android Auto home)
- The floating controller's close (**X**) button is now a **kill switch** for the mirror session.
  Pressing it starts the standard teardown — the monitor icon becomes a **countdown**:
  - Tap the countdown to **kill now**.
  - Let it run out to kill automatically.
  - **Press X again during the countdown to cancel** and keep the session alive.
- When the session ends, **Android Auto returns to its native launcher on its own** — no more frozen
  mirror screen, and no need to press the car's Home button. You can re-open AADisplay for a fresh
  session without restarting the car. The return-home is near-instant on a real kill.

### Controller movement and tucking
- **Free placement:** drag the floating controller anywhere and it stays exactly where you drop it
  (it no longer snaps to the nearest side). It's kept on-screen so it can't be lost off an edge.
- **Slide to tuck:** drag the controller off the screen edge to tuck it to a small peek, with a
  smooth slide animation (like a native side panel). Tap the peek handle to slide it back out.

### Removed Waze / Google Maps integration
- The "Disable Waze on AA" toggle and all of its supporting code have been **removed**. Google Maps
  support was already removed in v0.23.11, so the module no longer ties into any navigation app.
  Waze now behaves as a normal app on Android Auto.

## Verified
Confirmed on a connected Desktop Head Unit and on-device: kill returns to the Android Auto launcher
near-instantly, the cancel works, the tuck animation is smooth, the controller free-drags and stays
put, and a session restart after a kill still works.

## Baseline
Builds on **v0.23.12** (facet-bar Home button) and **v0.23.11** (Android Auto 17.8 auto-open fix +
adaptive display DPI).

versionName `0.23#17.8-r14` · versionCode `3029`

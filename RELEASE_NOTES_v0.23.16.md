# AAPwned v0.23.16

A cleanup/accuracy release for the "Disable Waze on Android Auto" feature. **No change to the
behavior you see** — Waze's map still stays on the phone while connected.

## What changed

- **Simplified to the mechanism that actually works.** The feature now relies solely on the
  car-connection intercept: Android Auto's own connection provider is told "not connected" for Waze
  only, so Waze keeps its map on the phone instead of auto-projecting to the car.
- **Removed the dead app-list filter.** The previous build also hooked
  `queryIntentServices/queryIntentActivities` to try to hide Waze from the Android Auto launcher, but
  Coolwalk builds its app grid from a car-app registry owned by Google Play services — a process this
  module doesn't hook — so the filter never hid Waze. It was pure overhead on hot code paths, so it's
  gone.
- **Corrects v0.23.15's description.** v0.23.15 said Waze is "hidden from the Android Auto app list";
  that was never achieved. Waze stays listed and can still be launched explicitly on the car, where
  it projects normally. The real, working behavior is "Waze's map stays on the phone instead of
  auto-projecting."
- **Performance:** the toggle is now cached and refreshed at most once per second instead of
  re-reading the config file from disk on every intercepted call, and the per-call log was dropped.

## About the DHU "frames backlog"

If you see `!!!! we have N frames backlog!` in the Desktop Head Unit console, that is **inherent
decode load**, not this module. High-motion content such as YouTube, mirrored at the head unit's
resolution, produces frame bursts the DHU's software decoder can't keep up with. It persists with the
Waze hook's per-call work removed and is triggered by the content, not the hook. Real head units with
a hardware decoder handle it fine. No change was made.

## Install

Updates the existing AAPwned package in place (`aapwned-0.23-17.8-r20.apk`). Reboot after installing
so the updated Android Auto hook loads. The "Disable Waze on Android Auto" toggle is unchanged.

## Credits

AAPwned is a fork of **[Nitsuya/AADisplay](https://github.com/Nitsuya/AADisplay)** — all credit for
the original module goes to Nitsuya. Credit is kept here in the repo and README, never in the
installed package listing. Bundled template/helper code from duzhaokun123 and QAuxiliary (cinit) is
retained under their respective licenses.

versionName `0.23#17.8-r20` · versionCode `3035`

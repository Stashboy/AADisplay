# AADisplay-Beta

[![AADisplay-Beta](https://img.shields.io/badge/AADisplayBeta-Project-blue?logo=github)](https://github.com/bikekoala/AADisplay-Beta)
[![GitHub Release](https://img.shields.io/github/v/release/Xposed-Modules-Repo/io.github.bikekoala.aa.display.beta)](https://github.com/Xposed-Modules-Repo/io.github.bikekoala.aa.display.beta/releases)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue)
![Android SDK min 31](https://img.shields.io/badge/Android%20SDK-%3E%3D%2031-brightgreen?logo=android)
![Android SDK target 33](https://img.shields.io/badge/Android%20SDK-target%2033-brightgreen?logo=android)

## Overview

This repository is a personal fork of [`Nitsuya/AADisplay`](https://github.com/Nitsuya/AADisplay)

AADisplay is an Xposed / LSPosed module that lets Android Auto display (mirror) almost any app using a VirtualDisplay-based approach.

## Requirements

- Android 12+ (SDK 31+; Android 10–11 are not officially supported)
- Rooted device with **LSPosed** (or compatible Xposed environment)
- Working Android Auto (`com.google.android.projection.gearhead`)
- Some ROMs may be unstable or crash; use at your own risk.

## Basic Usage

1. Enable this module in **LSPosed** and select:
   - **System Framework**
   - **Android Auto**
2. Install your preferred launcher and set its package name in the module settings.
3. Optionally:
   - Set **DPI** values to improve app UI on the car screen.
   - Add Android Auto **properties** to hook and tweak AA configuration.
4. Root is only used for user-configured shell commands; you can deny root if you do not need that feature.

For build instructions and full details, please refer to the original project: [`Nitsuya/AADisplay`](https://github.com/Nitsuya/AADisplay).

## License

Same license as the upstream project. See `LICENSE` for details.
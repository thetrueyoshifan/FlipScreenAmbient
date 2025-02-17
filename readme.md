# FlipScreenAmbient
![FlipScreenAmbient running on an ANS F30](https://raw.githubusercontent.com/thetrueyoshifan/FlipScreenAmbient/refs/heads/master/.github/images/preview.png)

FlipScreenAmbient is a project designed to allow more advanced customization of Android-enabled flip phones than some manufacturers provide, by completely replacing the front screen view with a fully custom overlay.
## Features over original
- Customizable fonts, sizes and colours for the clock, date and battery info
- User-customizable backgrounds

## Planned features
- Integration with stock apps (e.g. alarms, timers and stopwatch)
- Camera support (take pictures with the front screen and volume buttons)
- Volume adjustment UI
- Notification badges for call history, messages, and other notification types

## Supported Devices
FlipScreenAmbient is built for any devices that register their front screen as display ID 1. You can check this via ADB:
```
adb shell dumpsys display | grep DisplayDeviceInfo
```

You should see output similar to this:
```
DisplayDeviceInfo{"Built-in Screen": uniqueId="local:0", 240 x 320, modeId 1, defaultModeId 1, supportedModes [{id=1, width=240, height=320, fps=27.0}], colorMode 0, supportedColorModes [0], HdrCapabilities android.view.Display$HdrCapabilities@1d6308, density 120, 320.842 x 325.12 dpi, appVsyncOff 1000000, presDeadline 37037037, touch INTERNAL, rotation 0, type BUILT_IN, state OFF, FLAG_DEFAULT_DISPLAY, FLAG_ROTATES_WITH_CONTENT, FLAG_SECURE, FLAG_SUPPORTS_PROTECTED_BUFFERS}

DisplayDeviceInfo{"HDMI Screen": uniqueId="local:1", 128 x 160, modeId 2, defaultModeId 2, supportedModes [{id=2, width=128, height=160, fps=27.0}], colorMode 0, supportedColorModes [0], HdrCapabilities android.view.Display$HdrCapabilities@1d6308, density 37, 37.0 x 37.0 dpi, appVsyncOff 1000000, presDeadline 37037037, touch EXTERNAL, rotation 0, type HDMI, state OFF, FLAG_SECURE, FLAG_SUPPORTS_PROTECTED_BUFFERS, FLAG_PRESENTATION, FLAG_OWN_CONTENT_ONLY}
```

**These devices are confirmed to be working:**
- ANS F30

**These devices are based on the same firmware, and should work:**
- Orbic Journey V/L

## Installation
Grab the latest release from [releases](https://github.com/thetrueyoshifan/FlipScreenAmbient/releases), and install it to your device. You will need to grant the application **accessibility permissions** as well as the ability to draw over other apps; this is so that it can replace any current application running on the front screen.

If your device replaces the Accessibility options such that you can't enable this access, you can do it from ADB:
```
adb shell settings put secure enabled_accessibility_services com.gcti.flipscreenambient/com.gcti.flipscreenambient.OverlayAccessibilityService
```

Note that this will replace any currently enabled accessibility services, so you may wish to query those first:
```
adb shell settings get secure enabled_accessibility_services
```

and then append FlipScreenAmbient like this:
```
adb shell settings put secure enabled_accessibility_services EXISTINGSERVICES:com.gcti.flipscreenambient/com.gcti.flipscreenambient.OverlayAccessibilityService
```

## Current Priorities

 - [ ] Intercept volume button actions while screens are off
 - [ ] Add signal bars to display
 - [ ] Convert scaling back to SP
 - [ ] Integrate with stock apps
 - [ ] Add user configuration for display IDs

## Disclaimer
This is my first Kotlin project, and my first Android development project using native Android SDK (as opposed to Xamarin with .NET). As a result, a lot of this code is ugly, somewhat hardcoded, may drain your battery more than usual (but I've tried to minimize that as much as possible) and probably violates coding conventions I'm not even aware of. I am not responsible for bricked devices, dead SD cards, thermonuclear war, or you getting fired because the alarm app failed (because the battery died)!

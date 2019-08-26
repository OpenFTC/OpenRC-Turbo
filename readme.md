
## OpenRC-Turbo

OpenRC is a modified version of the official [FTC SDK](https://github.com/FIRST-Tech-Challenge/SkyStone)
in which all of the source code that is normally tucked away inside the AAR files has been extracted into modules. This makes it easy to see and modify almost the entirety of the Robot Controller app's source code. In addition, the history in Git shows all changes that have been made to the core code since OpenRC's inception. This complements the changelogs that FIRST provides, allowing teams to see exactly what code has been changed.


## Legality for competition use

According to the [2019-2020 Game Manual Part 1](https://www.firstinspires.org/sites/default/files/uploads/resource_library/ftc/game-manual-part-1.pdf), teams are not allowed to replace or modify the portions of the SDK which are distributed as AAR files, per `<RS09>`. This means that in its default configuration, OpenRC is **not** legal for competition.

**HOWEVER**, in order to address this, OpenRC has a `stock` build variant which will compile the `TeamCode` and `FtcRobotController` modules against the official, unmodified AAR files, rather than against the extracted modules.

## Device compatibility

Unfortunately, OpenRC is only compatible with devices that run Android 6.0 or higher. For FTC, this means that it is incompatible with the ZTE Speed. OpenRC will work fine on all other FTC-legal devices (including the new Control Hub).

For the curious: the cause of the incompatibility is the result of a bug in the `dlopen()` function of Android versions prior to 6.0. When loading the `libRobotCore.so` on older Android versions, an `UnsatisfiedLinkError` will be thrown because it cannot find a symbol that is declared in `libVuforia.so` and `dlopen()` is not smart enough to know that `libVuforia.so` has already been loaded into memory. See the "Correct soname/path handling" section of [this](https://android.googlesource.com/platform/bionic/+/master/android-changes-for-ndk-developers.md) page for more details.

## Build variants

### Variant Descriptions

 - **Stock - 27MB APK** *(oof!)*
     - Competition legal

 - **Turbo - 12.5MB APK** *(2.2x smaller!)*
     - OnBotJava removed

 - **Ultra Turbo - 4MB APK** *(6.8x smaller!)*
     - OnBotJava removed
     - Blocks removed
     - Web management removed
     - Sound files removed
     - Old Vuforia targets removed

### Benchmarks

**Note:** While OpenRC is not compatible with the ZTE, and the PadfoneX is not legal for competition, benchmarks are provided for the purposes of comparison across a range of devices and Android versions.


|                            |**ZTE Speed (4.4.4)**|**Nexus 5 (7.1.2)**|**PadfoneX (6.0.1)**|
|:--------------------------:|:-------------------:|:-----------------:|:------------------:|
|**Stock over USB**          |    20 sec           |  13 sec           |   21 sec           |
|**Turbo over USB**          |    15 sec           |  10 sec           |   13 sec           |
|**Extreme Turbo over USB**  |    11 sec           |   9 sec           |   10 sec           |
|                            |                     |                   |                    |
|**Stock over WiFi**         |    40 sec           |  15 sec           |   85 sec           |
|**Turbo over WiFi**         |    18 sec           |  11 sec           |   35 sec           |
|**Extreme Turbo over WiFi** |    13 sec           |   9 sec           |   21 sec           |

### Switching build variants

**IMPORTANT: make sure to test that your project compiles correctly with the stock variant at least a week before your competition!**

Note: you may get a "variant conflict" when switching from `Turbo` to `ExtremeTurbo`. You can fix this by changing `Blocks` to `extremeTurboDebug`

 1. Open the Build Variants tab in the lower left hand corner of Android Studio
 2. In the dropdown for the **TeamCode module**, select your desired variant
 3. Perform a Gradle sync

![image-here](doc/readme_pics/switching_build_variants.png)

## Setup Process

 1. Fork this repository
 2. Clone your fork
 3. Do `git remote add upstream https://github.com/OpenFTC/OpenRC-Turbo.git`
 4. Copy `libVuforia.so` from the `doc` folder of this repo into the `FIRST` folder on the RC's internal storage
 5. Select your desired build variant

## Update Process

Assuming you followed the above setup process, all that you need to do to update your fork when a new OpenRC release is available is:

 1. `git pull upstream master`
 2. Perform a Gradle Sync
 3. If the project fails to build, try *Clean Project*, *Rebuild Project*, and *Invalidate Caches / Restart*

## Versioning Scheme

To prevent confusion, OpenRC does not have its own version number. The version number will directly reflect the SDK version that the release is based on. However, the version number will have a letter appended to the end of it, which will be incremented (A-Z) for each release of OpenRC which is based on the same SDK version. When OpenRC is updated to be based on a new SDK version, the letter will reset to A.

For instance, the 3rd release of OpenRC based on SDK v5.0 would be `5.0C`, whereas the first release of OpenRC based on SDK v5.1 would be `5.1A`.

## Release Notes:

### 5.1A

Released on 26 August 2019

 - Update to SDK v5.1
 - Updated dynamic Vuforia loader to enforce being run on Android 6.0 or higher

### 5.0A

Released on 21 August 2019

 - Initial release.

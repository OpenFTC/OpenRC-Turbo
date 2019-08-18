
## OpenRC-Turbo

OpenRC is a modified version of the official [FTC SDK](https://github.com/FIRST-Tech-Challenge/SkyStone)
in which all of the source code that is normally tucked away inside the AAR files has been extracted into modules. This makes it easy to see and modify almost the entirety of the Robot Controller app's source code. In addition, the history in Git shows all changes that have been made to the core code since OpenRC's inception. This complements the changelogs that FIRST provides, allowing teams to see exactly what code has been changed.


## Legality for competition use

According to the [2019-2020 Game Manual Part 1](https://www.firstinspires.org/sites/default/files/uploads/resource_library/ftc/game-manual-part-1.pdf), teams are not allowed to replace or modify the portions of the SDK which are distributed as AAR files, per `<RS09>`. This means that in its default configuration, OpenRC is **not** legal for competition.

**HOWEVER**, in order to address this, OpenRC has a `stock` build variant which will compile the `TeamCode` and `FtcRobotController` modules against the official, unmodified AAR files, rather than against the extracted modules.

## Build variants

### Variant Descriptions

 - Stock
     - Competition legal
     - 24MB APK

 - Turbo
     - OnBotJava removed
     - 10MB APK

 - Ultra Turbo
     - OnBotJava removed
     - Blocks removed
     - Web management removed
     - Sound files removed
     - Old Vuforia targets removed
     - 4MB APK

### Benchmarks


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

Assuming you followed the above setup process, all that you need to do to update your fork when a new OpenRC release is available is `git pull upstream master`

## Versioning Scheme

To prevent confusion, OpenRC does not have its own version number. The version number will directly reflect the SDK version that the release is based on. However, the version number will have a letter appended to the end of it, which will be incremented (A-Z) for each release of OpenRC which is based on the same SDK version. When OpenRC is updated to be based on a new SDK version, the letter will reset to A.

For instance, the 3rd release of OpenRC based on SDK v5.0 would be `5.0C`, whereas the first release of OpenRC based on SDK v5.1 would be `5.1A`.

## Release Notes:

### 5.0A

 - Initial release.

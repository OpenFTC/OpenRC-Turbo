
## OpenRC-Turbo

OpenRC is a modified version of the official [FTC SDK](https://github.com/FIRST-Tech-Challenge/SkyStone)
in which all of the source code that is normally tucked away inside the AAR files has been extracted into modules. This makes it easy to see and modify almost the entirety of the Robot Controller app's source code. In addition, the history in Git shows all changes that have been made to the core code since OpenRC's inception. This complements the changelogs that FIRST provides, allowing teams to see exactly what code has been changed.


## Legality for competition use

According to the [2019-2020 Game Manual Part 1](https://www.firstinspires.org/sites/default/files/uploads/resource_library/ftc/game-manual-part-1.pdf), teams are not allowed to replace or modify the portions of the SDK which are distributed as AAR files, per `<RS09>`. This means that in its default configuration, OpenRC is **not** legal for competition.

**HOWEVER**, in order to address this, OpenRC has a `stock` build variant which will compile the `TeamCode` and `FtcRobotController` modules against the official, unmodified AAR files, rather than against the extracted modules.

## Versioning Scheme

To prevent confusion, OpenRC does not have its own version number. The version number will directly reflect the SDK version that the release is based on. However, the version number will have a letter appended to the end of it, which will be incremented (A-Z) for each release of OpenRC which is based on the same SDK version. When OpenRC is updated to be based on a new SDK version, the letter will reset to A.

For instance, the 3rd release of OpenRC based on SDK v5.0 would be `5.0C`, whereas the first release of OpenRC based on SDK v5.1 would be `5.1A`.

## Release Notes:

### 5.0A

 - Initial release.

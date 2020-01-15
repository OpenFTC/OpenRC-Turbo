# Blocks Samples

This directory contains the sample projects for blocks.

## Line Breaks

Although the blocks editor produces .blk files with no line breaks, the samples should have line breaks.
To add the appropriate line breaks:
1. Replace `><` with `>\n<`.
2. Replace `<field name="TEXT">\n</field>` with `<field name="TEXT"></field>`.

## Hardware and Webcam Names

If you add a new sample or you modify an existing sample, you may need to modify the method 
replaceHardwareIdentifiers in 
lib/Blocks/src/main/java/com/google/blocks/ftcrobotcontroller/util/ProjectsUtil.java.
Each hardware or webcam name in the sample file must be handled in that method.

__If a hardware (or webcam) name is not handled, it won't be replaced with the correct name of the
hardware (or webcam) from the active configuration when a new op mode is created with that sample.__

1. Look in the .blk file for elements with the following tags:
  * `<field name="IDENTIFIER">`
  * `<field name="IDENTIFIER1">`
  * `<field name="IDENTIFIER2">`
  * `<field name="WEBCAM_NAME">`
2. Make a note of the values of these elements.
   For example, in

   `<field name="IDENTIFIER">right_driveAsDcMotor</field>`

   the value is `right_driveAsDcMotor`.
3. Look in replaceHardwareIdentifiers to see if there is already a call to replaceIdentifierInBlocks for the values.
   For example, the following code is already present to handle `right_driveAsDcMotor`.
```java
        blkContent = replaceIdentifierInBlocks("right_driveAsDcMotor",
            rightDcMotor, blkContent, false, items, "IDENTIFIER", "IDENTIFIER2");
```
4. If there is not a call to replaceIdentifierInBlocks for that value, you must add one.

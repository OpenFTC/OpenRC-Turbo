/*
 * Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.blocks.ftcrobotcontroller.util;

import static com.google.blocks.ftcrobotcontroller.hardware.HardwareUtil.Capability;

import static org.firstinspires.ftc.robotcore.internal.system.AppUtil.BLOCKS_BLK_EXT;
import static org.firstinspires.ftc.robotcore.internal.system.AppUtil.BLOCKS_JS_EXT;
import static org.firstinspires.ftc.robotcore.internal.system.AppUtil.BLOCK_OPMODES_DIR;

import android.content.res.AssetManager;
import androidx.annotation.Nullable;
import android.text.Html;
import android.util.Xml;

import com.google.blocks.ftcrobotcontroller.IOExceptionWithUserVisibleMessage;
import com.google.blocks.ftcrobotcontroller.hardware.HardwareItem;
import com.google.blocks.ftcrobotcontroller.hardware.HardwareItemMap;
import com.google.blocks.ftcrobotcontroller.hardware.HardwareType;
import com.google.blocks.ftcrobotcontroller.hardware.HardwareUtil;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.external.ThrowingCallable;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A class that provides utility methods related to blocks projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SuppressWarnings("WeakerAccess")
public class ProjectsUtil {

  public static final String TAG = "ProjectsUtil";

  public static final String VALID_PROJECT_REGEX =
      "^[a-zA-Z0-9 \\!\\#\\$\\%\\&\\'\\(\\)\\+\\,\\-\\.\\;\\=\\@\\[\\]\\^_\\{\\}\\~]+$";
  private static final String XML_END_TAG = "</xml>";
  private static final String XML_TAG_EXTRA = "Extra";
  private static final String XML_TAG_OP_MODE_META = "OpModeMeta";
  private static final String XML_ATTRIBUTE_FLAVOR = "flavor";
  private static final String XML_ATTRIBUTE_GROUP = "group";
  private static final String XML_TAG_ENABLED = "Enabled";
  private static final String XML_ATTRIBUTE_VALUE = "value";
  private static final String BLOCKS_SAMPLES_PATH = "blocks/samples";
  private static final String DEFAULT_BLOCKS_SAMPLE_NAME = "default";

  private static final OpModeMeta.Flavor DEFAULT_FLAVOR = OpModeMeta.Flavor.TELEOP;

  // Prevent instantiation of utility class.
  private ProjectsUtil() {
  }

  /**
   * Returns the names and last modified time of existing blocks projects that have a blocks file.
   */
  public static String fetchProjectsWithBlocks() {
    return ProjectsLockManager.lockProjectsWhile(new Supplier<String>() {
      @Override public String get() {
        File[] files = BLOCK_OPMODES_DIR.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String filename) {
            if (filename.endsWith(BLOCKS_BLK_EXT)) {
              String projectName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
              return isValidProjectName(projectName);
            }
            return false;
          }
        });
        if (files != null) {
          StringBuilder jsonProjects = new StringBuilder();
          jsonProjects.append("[");
          String delimiter = "";
          for (File file : files) {
            String filename = file.getName();
            String projectName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
            try {
              boolean enabled = isProjectEnabled(projectName);
              jsonProjects.append(delimiter)
                  .append("{")
                  .append("\"name\":\"").append(escapeDoubleQuotes(projectName)).append("\", ")
                  .append("\"escapedName\":\"").append(escapeDoubleQuotes(Html.escapeHtml(projectName))).append("\", ")
                  .append("\"dateModifiedMillis\":").append(file.lastModified()).append(", ")
                  .append("\"enabled\":").append(enabled)
                  .append("}");
              delimiter = ",";
            } catch (IOException e) {
              RobotLog.e("ProjectsUtil.fetchProjectsWithBlocks() - problem with project " + projectName);
              RobotLog.logStackTrace(e);
            }
          }
          jsonProjects.append("]");
          return jsonProjects.toString();
        }
        return "[]";
      }
    });
  }

  public static String escapeSingleQuotes(String s) {
    return s.replace("'", "\\'");
  }

  public static String escapeDoubleQuotes(String s) {
    return s.replace("\"", "\\\"");
  }

  /**
   * Collects information about the existing blocks projects, for the offline blocks editor.
   */
  public static void fetchProjectsForOfflineBlocksEditor(
      final List<OfflineBlocksProject> offlineBlocksProjects) throws IOException {
    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        File[] files = BLOCK_OPMODES_DIR.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String filename) {
            if (filename.endsWith(BLOCKS_BLK_EXT)) {
              String projectName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
              return isValidProjectName(projectName);
            }
            return false;
          }
        });
        if (files != null) {
          for (File file : files) {
            String filename = file.getName();
            String projectName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
            String blkFileContent = fetchBlkFileContent(projectName);
            // The extraXml is after the first </xml>.
            int iXmlEndTag = blkFileContent.indexOf(XML_END_TAG);
            if (iXmlEndTag == -1) {
              // File is empty or corrupt.
              continue;
            }
            String extraXml = blkFileContent.substring(iXmlEndTag + XML_END_TAG.length());
            offlineBlocksProjects.add(new OfflineBlocksProject(filename, blkFileContent,
                projectName, file.lastModified(), isProjectEnabled(projectName, extraXml)));
          }
        }
        return null;
      }
    });
  }

  /**
   * Returns the names of blocks samples
   */
  public static String fetchSampleNames() throws IOException {
    HardwareItemMap hardwareItemMap = HardwareItemMap.newHardwareItemMap();

    StringBuilder jsonSamples = new StringBuilder();
    jsonSamples.append("[");

    AssetManager assetManager = AppUtil.getDefContext().getAssets();
    List<String> sampleFileNames = Arrays.asList(assetManager.list(BLOCKS_SAMPLES_PATH));
    Collections.sort(sampleFileNames);
    if (sampleFileNames != null) {
      String delimiter = "";
      for (String filename : sampleFileNames) {
        if (filename.endsWith(BLOCKS_BLK_EXT)) {
          String sampleName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
          if (!sampleName.equals(DEFAULT_BLOCKS_SAMPLE_NAME)) {
            String blkFileContent = readSample(sampleName, hardwareItemMap);
            Set<Capability> requestedCapabilities = getRequestedCapabilities(blkFileContent);
            // TODO(lizlooney): Consider adding required hardware.
            jsonSamples
                .append(delimiter)
                .append("{")
                .append("\"name\":\"").append(escapeDoubleQuotes(sampleName)).append("\", ")
                .append("\"escapedName\":\"").append(escapeDoubleQuotes(Html.escapeHtml(sampleName))).append("\", ")
                .append("\"requestedCapabilities\":[");
            String delimiter2 = "";
            for (Capability requestedCapability : requestedCapabilities) {
              jsonSamples
                  .append(delimiter2)
                  .append("\"").append(requestedCapability).append("\"");
              delimiter2 = ",";
            }
            jsonSamples
                .append("]")
                .append("}");
            delimiter = ",";
          }
        }
      }
    }
    jsonSamples.append("]");
    return jsonSamples.toString();
  }

  static Map<String, String> getSamples(HardwareItemMap hardwareItemMap) throws IOException {
    Map<String, String> map = new TreeMap<>();
    AssetManager assetManager = AppUtil.getDefContext().getAssets();
    List<String> sampleFileNames = Arrays.asList(assetManager.list(BLOCKS_SAMPLES_PATH));
    for (String filename : sampleFileNames) {
      if (filename.endsWith(BLOCKS_BLK_EXT)) {
        String sampleName = filename.substring(0, filename.length() - BLOCKS_BLK_EXT.length());
        String blkFileContent = readSample(sampleName, hardwareItemMap);
        if (sampleName.equals(DEFAULT_BLOCKS_SAMPLE_NAME)) {
          sampleName = "";
        }
        map.put(sampleName, blkFileContent);
      }
    }
    return map;
  }

  /**
   * Returns the set of capabilities used by the given blocks content.
   */
  private static Set<Capability> getRequestedCapabilities(String blkFileContent) {
    Set<Capability> requestedCapabilities = new LinkedHashSet<>();
    // The order here is important. If any of these capabilities is not supported by the robot
    // configuration, we will show the warning associated with the first missing capability.
    if (blkFileContent.contains("<block type=\"tfod")) {
      requestedCapabilities.add(Capability.TFOD);
    }
    if (blkFileContent.contains("<block type=\"vuforia")) {
      requestedCapabilities.add(Capability.VUFORIA);
    }
    if (blkFileContent.contains("<block type=\"navigation_switchableCamera")) {
      requestedCapabilities.add(Capability.SWITCHABLE_CAMERA);
    }
    if (blkFileContent.contains("_initialize_withWebcam")) {
      requestedCapabilities.add(Capability.WEBCAM);
    }
    if (blkFileContent.contains("_initialize_withCameraDirection")) {
      requestedCapabilities.add(Capability.CAMERA);
    }
    return requestedCapabilities;
  }

  /**
   * Returns the {@link OpModeMeta} for existing blocks projects that have a JavaScript file and
   * are enabled.
   */
  public static List<OpModeMeta> fetchEnabledProjectsWithJavaScript() {
    return ProjectsLockManager.lockProjectsWhile(new Supplier<List<OpModeMeta>>() {
      @Override public List<OpModeMeta> get() {
        String[] filenames = BLOCK_OPMODES_DIR.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String filename) {
            if (filename.endsWith(BLOCKS_JS_EXT)) {
              String projectName = filename.substring(0, filename.length() - BLOCKS_JS_EXT.length());
              return isValidProjectName(projectName);
            }
            return false;
          }
        });
        List<OpModeMeta> projects = new ArrayList<OpModeMeta>();
        if (filenames != null) {
          for (String filename : filenames) {
            String projectName = filename.substring(0, filename.length() - BLOCKS_JS_EXT.length());
            OpModeMeta opModeMeta = fetchOpModeMeta(projectName);
            if (opModeMeta != null) {
              projects.add(opModeMeta);
            }
          }
        }
        return projects;
      }
    });
  }

  @Nullable
  private static OpModeMeta fetchOpModeMeta(String projectName) {
    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }
    try {
      File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
      String blkFileContent = FileUtil.readFile(blkFile);
      // The extraXml is after the first </xml>.
      int iXmlEndTag = blkFileContent.indexOf(XML_END_TAG);
      if (iXmlEndTag == -1) {
        // File is empty or corrupt.
        throw new CorruptFileException("File " + blkFile.getName() + " is empty or corrupt.");
      }
      String extraXml = blkFileContent.substring(iXmlEndTag + XML_END_TAG.length());
      // Return null if the project is not enabled.
      if (!isProjectEnabled(projectName, extraXml)) {
        return null;
      }
      return createOpModeMeta(projectName, extraXml);
    } catch (IOException e) {
      if (!projectName.startsWith("backup_")) {
        RobotLog.e("ProjectsUtil.fetchOpModeMeta(\"" + projectName + "\") - failed.");
        RobotLog.logStackTrace(e);
      }
      return null;
    }
  }

  /**
   * Returns true if the given project name is not null and contains only valid characters.
   * This function does not check whether the project exists.
   */
  public static boolean isValidProjectName(String projectName) {
    if (projectName != null) {
      return projectName.matches(VALID_PROJECT_REGEX);
    }
    return false;
  }

  /**
   * Returns the content of the blocks file with the given project name. The returned content
   * may include extra XML after the blocks XML.
   *
   * @param projectName the name of the project
   */
  public static String fetchBlkFileContent(String projectName) throws IOException {
    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }
    File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
    String blkFileContent = FileUtil.readFile(blkFile);

    // Separate the blocksContent from the extraXml, so we can upgrade the blocksContent.
    // The extraXml is after the first </xml>.
    int iXmlEndTag = blkFileContent.indexOf(XML_END_TAG);
    if (iXmlEndTag == -1) {
      // File is empty or corrupt.
      throw new CorruptFileException("File " + blkFile.getName() + " is empty or corrupt.");
    }
    String blocksContent = blkFileContent.substring(0, iXmlEndTag + XML_END_TAG.length());
    String extraXml = blkFileContent.substring(iXmlEndTag + XML_END_TAG.length());

    String upgradedBlocksContent = upgradeBlocks(blocksContent, HardwareItemMap.newHardwareItemMap());
    if (!upgradedBlocksContent.equals(blocksContent)) {
      blkFileContent = upgradedBlocksContent + extraXml;
    }

    return blkFileContent;
  }

  /**
   * Upgrades the given blocks content based on the given {@link HardwareItemMap}.
   */
  private static String upgradeBlocks(String blkContent, HardwareItemMap hardwareItemMap) {
    // In previous versions, block type prefix bno055imu_ was adafruitBNO055IMU_.
    blkContent = blkContent.replace(
        "<block type=\"adafruitBNO055IMU_",
        "<block type=\"bno055imu_");
    // In previous versions, identifier suffix AsBNO055IMU was AsAdafruitBNO055IMU.
    blkContent = replaceIdentifierSuffixInBlocks(blkContent,
        hardwareItemMap.getHardwareItems(HardwareType.BNO055IMU),
        "AsAdafruitBNO055IMU", "AsBNO055IMU");
    // In previous versions, block type prefix bno055imuParameters_ was adafruitBNO055IMUParameters_.
    blkContent = blkContent.replace(
        "<block type=\"adafruitBNO055IMUParameters_",
        "<block type=\"bno055imuParameters_");
    // In previous versions, shadow type prefix bno055imuParameters_ was adafruitBNO055IMUParameters_.
    blkContent = blkContent.replace(
        "<shadow type=\"adafruitBNO055IMUParameters_",
        "<shadow type=\"bno055imuParameters_");
    // In previous version, value name BNO055IMU_PARAMETERS was ADAFRUIT_BNO055IMU_PARAMETERS.
    blkContent = blkContent.replace(
        "<value name=\"ADAFRUIT_BNO055IMU_PARAMETERS\">",
        "<value name=\"BNO055IMU_PARAMETERS\">");

    // In previous versions, identifier suffix AsREVModule was asLynxModule.
    blkContent = replaceIdentifierSuffixInBlocks(blkContent,
        hardwareItemMap.getHardwareItems(HardwareType.LYNX_MODULE),
        "asLynxModule", "AsREVModule");
    // In previous versions, identifier suffix AsREVColorRangeSensor was asLynxI2cColorRangeSensor.
    blkContent = replaceIdentifierSuffixInBlocks(blkContent,
        hardwareItemMap.getHardwareItems(HardwareType.COLOR_RANGE_SENSOR),
        "asLynxI2cColorRangeSensor", "AsREVColorRangeSensor");

    // In previous versions, some hardware types didn't have suffices.
    HardwareType[] typesThatDidntHaveSuffix = new HardwareType[] {
      HardwareType.ACCELERATION_SENSOR,
      HardwareType.COLOR_SENSOR,
      HardwareType.COMPASS_SENSOR,
      HardwareType.CR_SERVO,
      HardwareType.DC_MOTOR,
      HardwareType.DISTANCE_SENSOR,
      HardwareType.GYRO_SENSOR,
      HardwareType.IR_SEEKER_SENSOR,
      HardwareType.LED,
      HardwareType.LIGHT_SENSOR,
      HardwareType.SERVO,
      HardwareType.TOUCH_SENSOR,
      HardwareType.ULTRASONIC_SENSOR
    };
    for (HardwareType hardwareType : typesThatDidntHaveSuffix) {
      blkContent = replaceIdentifierSuffixInBlocks(blkContent,
          hardwareItemMap.getHardwareItems(hardwareType),
          "", hardwareType.identifierSuffixForJavaScript);
    }

    return blkContent;
  }

  /**
   * Replaces an identifier suffix in blocks.
   */
  private static String replaceIdentifierSuffixInBlocks(
      String blkContent, List<HardwareItem> hardwareItemList,
      String oldIdentifierSuffix, String newIdentifierSuffix) {
    if (hardwareItemList != null) {
      for (HardwareItem hardwareItem : hardwareItemList) {
        String newIdentifier = hardwareItem.identifier;
        if (newIdentifier.endsWith(newIdentifierSuffix)) {
          String oldIdentifier =
              newIdentifier.substring(0, newIdentifier.length() - newIdentifierSuffix.length())
              + oldIdentifierSuffix;
          String[] identifierFieldNames = new String[] {
            "IDENTIFIER",
            "IDENTIFIER1",
            "IDENTIFIER2",
          };
          for (String identifierFieldName : identifierFieldNames) {
            String oldElement = "<field name=\"" + identifierFieldName + "\">" + oldIdentifier + "</field>";
            String newElement = "<field name=\"" + identifierFieldName + "\">" + newIdentifier + "</field>";
            blkContent = blkContent.replace(oldElement, newElement);
          }
        }
      }
    }
    return blkContent;
  }

  /**
   * Returns the content of the JavaScript file with the given project name.
   *
   * @param projectName the name of the project
   */
  public static String fetchJsFileContent(String projectName) throws IOException {
    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }
    return FileUtil.readFile(new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_JS_EXT));
  }

  /**
   * Returns the content of the blocks file for a new project. Note that this method does not save
   * the project. It just creates the content.
   *
   * @param projectName the name of the project
   * @param sampleName the name of the sample to copy.
   * @return the content of the blocks file
   */
  public static String newProject(String projectName, String sampleName) throws IOException {
    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }

    return readSample(sampleName, HardwareItemMap.newHardwareItemMap());
  }

  private static String readSample(String sampleName, HardwareItemMap hardwareItemMap)
      throws IOException {
    if (sampleName == null || sampleName.isEmpty()) {
      sampleName = DEFAULT_BLOCKS_SAMPLE_NAME;
    }

    StringBuilder blkFileContent = new StringBuilder();
    AssetManager assetManager = AppUtil.getDefContext().getAssets();
    String assetName = BLOCKS_SAMPLES_PATH + "/" + sampleName + BLOCKS_BLK_EXT;
    FileUtil.readAsset(blkFileContent, assetManager, assetName);

    return replaceHardwareIdentifiers(blkFileContent.toString(), hardwareItemMap);
  }

  /**
   * Replaces the hardware identifiers in the given blocks content based on the given {@link
   * HardwareItemMap}.
   */
  private static String replaceHardwareIdentifiers(String blkContent, HardwareItemMap hardwareItemMap) {
    // The following handles the identifier that are hardcoded in the sample blocks op modes.
    if (hardwareItemMap.contains(HardwareType.DC_MOTOR)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.DC_MOTOR);
      if (!items.isEmpty()) {
        String firstDcMotor = items.get(0).identifier;
        String secondDcMotor = (items.size() > 1) ? items.get(1).identifier : firstDcMotor;
        String leftDcMotor = null;
        String rightDcMotor = null;
        for (HardwareItem item : items) {
          String lower = item.deviceName.toLowerCase(Locale.ENGLISH);
          if (leftDcMotor == null && lower.contains("left")) {
            leftDcMotor = item.identifier;
          }
          if (rightDcMotor == null && lower.contains("right")) {
            rightDcMotor = item.identifier;
          }
        }
        if (leftDcMotor == null) {
          leftDcMotor = firstDcMotor;
        }
        if (rightDcMotor == null) {
          rightDcMotor = secondDcMotor;
        }
        blkContent = replaceIdentifierInBlocks("motorTestAsDcMotor",
            firstDcMotor, blkContent, false, items, "IDENTIFIER");
        blkContent = replaceIdentifierInBlocks("left_driveAsDcMotor",
            leftDcMotor, blkContent, false, items, "IDENTIFIER", "IDENTIFIER1");
        blkContent = replaceIdentifierInBlocks("right_driveAsDcMotor",
            rightDcMotor, blkContent, false, items, "IDENTIFIER", "IDENTIFIER2");
      }
    }
    if (hardwareItemMap.contains(HardwareType.DIGITAL_CHANNEL)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.DIGITAL_CHANNEL);
      if (!items.isEmpty()) {
        String firstDigitalChannel = items.get(0).identifier;
        blkContent = replaceIdentifierInBlocks("digitalTouchAsDigitalChannel",
            firstDigitalChannel, blkContent, false, items, "IDENTIFIER");
      }
    }
    if (hardwareItemMap.contains(HardwareType.BNO055IMU)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.BNO055IMU);
      if (!items.isEmpty()) {
        String firstBno055imu = items.get(0).identifier;
        blkContent = replaceIdentifierInBlocks("imuAsBNO055IMU",
            firstBno055imu, blkContent, false, items, "IDENTIFIER");
      }
    }
    if (hardwareItemMap.contains(HardwareType.SERVO)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.SERVO);
      if (!items.isEmpty()) {
        String firstServo = items.get(0).identifier;
        blkContent = replaceIdentifierInBlocks("left_handAsServo",
            firstServo, blkContent, false, items, "IDENTIFIER");
        blkContent = replaceIdentifierInBlocks("servoTestAsServo",
            firstServo, blkContent, false, items, "IDENTIFIER");
      }
    }
    if (hardwareItemMap.contains(HardwareType.COLOR_RANGE_SENSOR)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.COLOR_RANGE_SENSOR);
      if (!items.isEmpty()) {
        String firstColorRangeSensor = items.get(0).identifier;
        blkContent = replaceIdentifierInBlocks("sensorColorRangeAsREVColorRangeSensor",
            firstColorRangeSensor, blkContent, false, items, "IDENTIFIER");
      }
    }
    if (hardwareItemMap.contains(HardwareType.REV_BLINKIN_LED_DRIVER)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.REV_BLINKIN_LED_DRIVER);
      if (!items.isEmpty()) {
        String firstRevBlinkinLedDriver = items.get(0).identifier;
        blkContent = replaceIdentifierInBlocks("blinkinAsRevBlinkinLedDriver",
            firstRevBlinkinLedDriver, blkContent, false, items, "IDENTIFIER");
      }
    }
    if (hardwareItemMap.contains(HardwareType.WEBCAM_NAME)) {
      List<HardwareItem> items = hardwareItemMap.getHardwareItems(HardwareType.WEBCAM_NAME);
      if (!items.isEmpty()) {
        // The webcam block uses the deviceName, rather than the identifier.
        String firstWebcamName = items.get(0).deviceName;
        blkContent = replaceIdentifierInBlocks("Webcam 1",
            firstWebcamName, blkContent, true, items, "WEBCAM_NAME");
      }
    }
    return blkContent;
  }

  /**
   * Replaces an identifier in blocks.
   */
  private static String replaceIdentifierInBlocks(String oldIdentifier, String newIdentifier,
      String blkContent, boolean useDeviceName, List<HardwareItem> items, String... fieldNames) {
    for (HardwareItem item : items) {
      String identifier = useDeviceName ? item.deviceName : item.identifier;
      if (identifier.equals(oldIdentifier)) {
        return blkContent;
      }
    }
    for (String fieldName : fieldNames) {
      String oldElement = "<field name=\"" + fieldName + "\">" + oldIdentifier + "</field>";
      String newElement = "<field name=\"" + fieldName + "\">" + newIdentifier + "</field>";
      blkContent = blkContent.replace(oldElement, newElement);
    }
    return blkContent;
  }

  /**
   * Save the blocks file and JavaScript file with the given project name.
   *
   * @param projectName the name of the project
   * @param blkFileContent the content to write to the blocks file.
   * @param jsFileContent the content to write to the JavaScript file.
   */
  public static void saveProject(final String projectName, final String blkFileContent, final String jsFileContent)
      throws IOException {

    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }

    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(BLOCK_OPMODES_DIR, false);

        // Before writing the new content to the files, make temporary copies of the old files,
        // just in case the control hub is unplugged (or the Android's battery dies) while we are
        // writing the file. We don't want the user to be left with the file empty/corrupt and the
        // old and new content both lost.
        File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
        File jsFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_JS_EXT);
        long timestamp = System.currentTimeMillis();
        File blkTempBackup = null;
        File jsTempBackup = null;
        if (blkFile.exists()) {
          blkTempBackup = new File(BLOCK_OPMODES_DIR, "backup_" + timestamp + "_" + projectName + BLOCKS_BLK_EXT);
          FileUtil.copyFile(blkFile, blkTempBackup);
        }
        if (jsFile.exists()) {
          jsTempBackup = new File(BLOCK_OPMODES_DIR, "backup_" + timestamp + "_" + projectName + BLOCKS_JS_EXT);
          FileUtil.copyFile(jsFile, jsTempBackup);
        }
        FileUtil.writeFile(blkFile, blkFileContent);
        FileUtil.writeFile(jsFile, jsFileContent);
        // Once we've written the new content to the files, we can delete the temporary copies of
        // the old files.
        if (blkTempBackup != null) {
          blkTempBackup.delete();
        }
        if (jsTempBackup != null) {
          jsTempBackup.delete();
        }
        return null;
      }
    });
  }

  /**
   * Renames the blocks file and JavaScript file with the given project name.
   *
   * @param oldProjectName the old name of the project
   * @param newProjectName the new name of the project
   */
  public static void renameProject(final String oldProjectName, final String newProjectName)
      throws IOException {
    if (!isValidProjectName(oldProjectName) || !isValidProjectName(newProjectName)) {
      throw new IllegalArgumentException();
    }
    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(BLOCK_OPMODES_DIR, false);

        File oldBlk = new File(BLOCK_OPMODES_DIR, oldProjectName + BLOCKS_BLK_EXT);
        File newBlk = new File(BLOCK_OPMODES_DIR, newProjectName + BLOCKS_BLK_EXT);
        if (oldBlk.renameTo(newBlk)) {
          File oldJs = new File(BLOCK_OPMODES_DIR, oldProjectName + BLOCKS_JS_EXT);
          File newJs = new File(BLOCK_OPMODES_DIR, newProjectName + BLOCKS_JS_EXT);
          oldJs.renameTo(newJs);
        }
        return null;
      }
    });

  }

  /**
   * Copies the blocks file and JavaScript file with the given project name.
   *
   * @param oldProjectName the old name of the project
   * @param newProjectName the new name of the project
   */
  public static void copyProject(final String oldProjectName, final String newProjectName)
      throws IOException {
    if (!isValidProjectName(oldProjectName) || !isValidProjectName(newProjectName)) {
      throw new IllegalArgumentException();
    }
    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(BLOCK_OPMODES_DIR, false);

        File oldBlk = new File(BLOCK_OPMODES_DIR, oldProjectName + BLOCKS_BLK_EXT);
        File newBlk = new File(BLOCK_OPMODES_DIR, newProjectName + BLOCKS_BLK_EXT);
        FileUtil.copyFile(oldBlk, newBlk);
        try {
          File oldJs = new File(BLOCK_OPMODES_DIR, oldProjectName + BLOCKS_JS_EXT);
          File newJs = new File(BLOCK_OPMODES_DIR, newProjectName + BLOCKS_JS_EXT);
          FileUtil.copyFile(oldJs, newJs);
        } catch (IOException e) {
          throw new IOExceptionWithUserVisibleMessage(
              "The blocks project was successfully copied, but the new op mode cannot be run until it " +
              "is saved in the blocks editor.");
        }
        return null;
      }
    });

  }

  /**
   * Enables (or disables) the project with the given name.
   *
   * @param projectName the name of the project
   * @param enable whether to enable (or disable) the project
   */
  public static void enableProject(final String projectName, final boolean enable)
      throws IOException {

    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }

    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
        String blkFileContent = FileUtil.readFile(blkFile);

        // Separate the blocksContent from the extraXml, so we can extract the OpModeMeta from the extraXml.
        // The extraXml is after the first </xml>.
        int iXmlEndTag = blkFileContent.indexOf(XML_END_TAG);
        if (iXmlEndTag == -1) {
          // File is empty or corrupt.
          throw new CorruptFileException("File " + blkFile.getName() + " is empty or corrupt.");
        }
        String blocksContent = blkFileContent.substring(0, iXmlEndTag + XML_END_TAG.length());
        String extraXml = blkFileContent.substring(iXmlEndTag + XML_END_TAG.length());
        OpModeMeta opModeMeta = createOpModeMeta(projectName, extraXml);

        // Regenerate the extra xml with the enable argument.
        final String newBlkFileContent = blocksContent +
            formatExtraXml(opModeMeta.flavor, opModeMeta.group, enable);
        File blkTempBackup = null;
        if (blkFile.exists()) {
          // Before writing the new content to the file, make a temporary copy of the old file,
          // just in case the control hub is unplugged (or the Android's battery dies) while we are
          // writing a file. We don't want the user to be left with the file empty/corrupt and the
          // old and new content both lost.
          long timestamp = System.currentTimeMillis();
          blkTempBackup = new File(BLOCK_OPMODES_DIR, "backup_" + timestamp + "_" + projectName + BLOCKS_BLK_EXT);
          FileUtil.copyFile(blkFile, blkTempBackup);
        }
        FileUtil.writeFile(blkFile, newBlkFileContent);
        // Once we've written the new content to the file, we can delete the temporary copy of
        // the old file.
        if (blkTempBackup != null) {
          blkTempBackup.delete();
        }
        return null;
      }
    });
  }

  /**
   * Delete the blocks and JavaScript files for the given project names.
   *
   * @param projectNames the names of the projects to delete
   */
  public static Boolean deleteProjects(final String[] projectNames) {

    return ProjectsLockManager.lockProjectsWhile(new Supplier<Boolean>() {
      @Override public Boolean get() {
        for (String projectName : projectNames) {
          if (!isValidProjectName(projectName)) {
            throw new IllegalArgumentException();
          }
        }
        boolean success = true;
        for (String projectName : projectNames) {
          File jsFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_JS_EXT);
          if (jsFile.exists()) {
            if (!jsFile.delete()) {
              success = false;
            }
          }
          if (success) {
            File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
            if (blkFile.exists()) {
              if (!blkFile.delete()) {
                success = false;
              }
            }
          }
        }
        return success;
      }
    });
  }

  public static String getBlocksJavaClassName(String projectName) {
    StringBuilder className = new StringBuilder();

    char ch = projectName.charAt(0);
    if (Character.isJavaIdentifierStart(ch)) {
      className.append(ch);
    } else if (Character.isJavaIdentifierPart(ch)) {
      className.append('_').append(ch);
    }
    int length = projectName.length();
    for (int i = 1; i < length; i++) {
      ch = projectName.charAt(i);
      if (Character.isJavaIdentifierPart(ch)) {
        className.append(ch);
      }
    }

    // Make sure the name doesn't match an existing class in
    // /sdcard/FIRST/java/src/org/firstinspires/ftc/teamcode.
    File dir = new File(OnBotJavaHelper.srcDir, "org/firstinspires/ftc/teamcode");
    String base = className.toString();
    File file = new File(dir, base + ".java");
    if (file.exists()) {
      int i = 1; // Note that this is immediately incremented to 2.
      do {
        i++;
        file = new File(dir, base + i + ".java");
      } while (file.exists());
      className.append(i);
    }
    return className.toString();
  }

  /**
   * Save the Java generated from blocks.
   *
   * @param relativeFileName the name of the file
   * @param javaContent the content to write to the Java file.
   */
  public static void saveBlocksJava(final String relativeFileName, final String javaContent)
      throws IOException {

    ProjectsLockManager.lockProjectsWhile(new ThrowingCallable<Void, IOException>() {
      @Override public Void call() throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(BLOCK_OPMODES_DIR, false);

        // Before writing the new content to the files, make a temporary copy of the old file,
        // just in case the control hub is unplugged (or the Android's battery dies) while we are
        // writing the file. We don't want the user to be left with the file empty/corrupt and the
        // old and new content both lost.
        int lastSlash = relativeFileName.lastIndexOf("/");
        String relativeDir = relativeFileName.substring(0, lastSlash + 1);
        String filename = relativeFileName.substring(lastSlash + 1);
        File dir = new File(BLOCK_OPMODES_DIR, "../java/src/" + relativeDir);
        dir.mkdirs();
        File javaFile = new File(dir, filename);
        long timestamp = System.currentTimeMillis();
        File javaTempBackup = null;
        if (javaFile.exists()) {
          javaTempBackup = new File(dir, "backup_" + timestamp + "_" + filename);
          FileUtil.copyFile(javaFile, javaTempBackup);
        }
        FileUtil.writeFile(javaFile, javaContent);
        // Once we've written the new content to the file, we can delete the temporary copy of
        // the old file.
        if (javaTempBackup != null) {
          javaTempBackup.delete();
        }
        return null;
      }
    });
  }

  /**
   * Formats the extra XML.
   */
  private static String formatExtraXml(OpModeMeta.Flavor flavor, String group, boolean enabled)
      throws IOException {
    XmlSerializer serializer = Xml.newSerializer();
    StringWriter writer = new StringWriter();
    serializer.setOutput(writer);
    serializer.startDocument("UTF-8", true);
    serializer.startTag("", XML_TAG_EXTRA);
    serializer.startTag("", XML_TAG_OP_MODE_META);
    serializer.attribute("", XML_ATTRIBUTE_FLAVOR, flavor.toString());
    serializer.attribute("", XML_ATTRIBUTE_GROUP, group);
    serializer.endTag("", XML_TAG_OP_MODE_META);
    serializer.startTag("", XML_TAG_ENABLED);
    serializer.attribute("", XML_ATTRIBUTE_VALUE, Boolean.toString(enabled));
    serializer.endTag("", XML_TAG_ENABLED);
    serializer.endTag("", XML_TAG_EXTRA);
    serializer.endDocument();
    return writer.toString();
  }

  /**
   * Creates an {@link OpModeMeta} instance with the given name, extracting the flavor and group
   * from the given extraXml.
   */
  private static OpModeMeta createOpModeMeta(String projectName, String extraXml) {
    OpModeMeta.Flavor flavor = DEFAULT_FLAVOR;
    String group = "";

    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setInput(new StringReader(removeNewLines(extraXml)));
      int eventType = parser.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
          if (parser.getName().equals(XML_TAG_OP_MODE_META)) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
              String name = parser.getAttributeName(i);
              String value = parser.getAttributeValue(i);
              if (name.equals(XML_ATTRIBUTE_FLAVOR)) {
                flavor = OpModeMeta.Flavor.valueOf(value.toUpperCase(Locale.ENGLISH));
              } else if (name.equals(XML_ATTRIBUTE_GROUP)) {
                if (!value.isEmpty() && !value.equals(OpModeMeta.DefaultGroup)) {
                  group = value;
                }
              }
            }
          }
        }
        eventType = parser.next();
      }
    } catch (IOException | XmlPullParserException e) {
      RobotLog.e("ProjectsUtil.createOpmodeMeta(\"" + projectName + "\", ...) - failed to parse xml.");
      RobotLog.logStackTrace(e);
    }

    return new OpModeMeta(projectName, flavor, group);
  }

  private static boolean isProjectEnabled(String projectName) throws IOException {
    if (!isValidProjectName(projectName)) {
      throw new IllegalArgumentException();
    }
    File blkFile = new File(BLOCK_OPMODES_DIR, projectName + BLOCKS_BLK_EXT);
    String blkFileContent = FileUtil.readFile(blkFile);
    // The extraXml is after the first </xml>.
    int iXmlEndTag = blkFileContent.indexOf(XML_END_TAG);
    if (iXmlEndTag == -1) {
      // File is empty or corrupt.
      throw new CorruptFileException("File " + blkFile.getName() + " is empty or corrupt.");
    }
    String extraXml = blkFileContent.substring(iXmlEndTag + XML_END_TAG.length());
    return isProjectEnabled(projectName, extraXml);
  }

  /**
   * Returns false if the given extraXml contains the tag/attribute for enabling a project and the
   * value of attribute is false. Otherwise it returns true.
   */
  private static boolean isProjectEnabled(String projectName, String extraXml) {
    boolean enabled = true;

    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setInput(new StringReader(removeNewLines(extraXml)));
      int eventType = parser.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
          if (parser.getName().equals(XML_TAG_ENABLED)) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
              String name = parser.getAttributeName(i);
              String value = parser.getAttributeValue(i);
              if (name.equals(XML_ATTRIBUTE_VALUE)) {
                enabled = Boolean.parseBoolean(value);
              }
            }
          }
        }
        eventType = parser.next();
      }
    } catch (IOException | XmlPullParserException e) {
      RobotLog.e("ProjectsUtil.isProjectEnabled(\"" + projectName + "\", ...) - failed to parse xml.");
      RobotLog.logStackTrace(e);
    }

    return enabled;
  }

  private static String removeNewLines(String text) {
    return text.replace("\n", "");
  }
}

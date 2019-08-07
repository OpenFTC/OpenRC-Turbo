// Copyright 2016 Google Inc.

package com.google.blocks.ftcrobotcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.blocks.R;
import com.google.blocks.ftcrobotcontroller.util.ClipboardUtil;
import com.google.blocks.ftcrobotcontroller.hardware.HardwareUtil;
import com.google.blocks.ftcrobotcontroller.util.ProjectsUtil;
import com.google.blocks.ftcrobotcontroller.util.SoundsUtil;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotserver.internal.webserver.MimeTypesUtil;

import java.net.URLEncoder;

/**
 * An {@link Activity} that provides Blockly in a WebView.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class BlocksActivity extends Activity {
  private WebView webView;

  @Override
  @SuppressLint("setJavaScriptEnabled")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_blocks);

    webView = (WebView) findViewById(R.id.webViewBlockly);
    webView.setWebChromeClient(new WebChromeClient());

    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    webView.addJavascriptInterface(new BlocksIO(), "blocksIO");
    webView.loadUrl("file:///android_asset/FtcBlocksProjects.html");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }

  private class BlocksIO {
    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchProjects() {
      try {
        return ProjectsUtil.fetchProjectsWithBlocks();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchSamples() {
      try {
        return ProjectsUtil.fetchSampleNames();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void openProjectBlocks(final String projectName) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          try {
            webView.loadUrl(
                "file:///android_asset/FtcBlocks.html?project="
                + URLEncoder.encode(projectName, "UTF-8"));
          } catch (Exception e) {
            RobotLog.e("BlocksActivity.openProjectBlocks - caught " + e);
          }
        }
      });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchJavaScriptForHardware() {
      try {
        return HardwareUtil.fetchJavaScriptForHardware();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String getConfigurationName() {
      try {
        return HardwareUtil.getConfigurationName();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchBlkFileContent(String projectName) {
      try {
        return ProjectsUtil.fetchBlkFileContent(projectName);
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String newProject(String projectName, String sampleName) {
      try {
        return ProjectsUtil.newProject(projectName, sampleName);
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean saveProject(String projectName, String blkContent, String jsFileContent) {
      try {
        ProjectsUtil.saveProject(projectName, blkContent, jsFileContent);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean renameProject(String oldProjectName, String newProjectName) {
      try {
        ProjectsUtil.renameProject(oldProjectName, newProjectName);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean copyProject(String oldProjectName, String newProjectName) {
      try {
        ProjectsUtil.copyProject(oldProjectName, newProjectName);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean enableProject(String oldProjectName, boolean enable) {
      try {
        ProjectsUtil.enableProject(oldProjectName, enable);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean deleteProjects(String starDelimitedProjectNames) {
      String[] projectNames = starDelimitedProjectNames.split("\\*");
      try {
        ProjectsUtil.deleteProjects(projectNames);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String getBlocksJavaClassName(String projectName) {
      try {
        return ProjectsUtil.getBlocksJavaClassName(projectName);
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean saveBlocksJava(String relativeFileName, String javaContent) {
      try {
        ProjectsUtil.saveBlocksJava(relativeFileName, javaContent);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean saveClipboardContent(String clipboardContent) {
      try {
        ClipboardUtil.saveClipboardContent(clipboardContent);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchClipboardContent() {
      try {
        return ClipboardUtil.fetchClipboardContent();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchSounds() {
      try {
        return SoundsUtil.fetchSounds();
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean saveSound(String soundName, String base64Content) {
      try {
        SoundsUtil.saveSoundFile(soundName, base64Content);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchSoundFileContent(String soundName) {
      try {
        return SoundsUtil.fetchSoundFileContent(soundName);
      } catch (Exception e) {
        return null;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public String fetchSoundFileMimeType(String soundName) {
      String mimeType = MimeTypesUtil.determineMimeType(soundName);
      if (mimeType == null) {
        mimeType = "";
      }
      return mimeType;
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean renameSound(String oldSoundName, String newSoundName) {
      try {
        SoundsUtil.renameSound(oldSoundName, newSoundName);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean copySound(String oldSoundName, String newSoundName) {
      try {
        SoundsUtil.copySound(oldSoundName, newSoundName);
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public boolean deleteSounds(String starDelimitedSoundNames) {
      String[] soundNames = starDelimitedSoundNames.split("\\*");
      try {
        SoundsUtil.deleteSounds(soundNames);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
  }
}

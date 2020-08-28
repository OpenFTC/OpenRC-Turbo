/* Copyright (c) 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftccommon;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.core.content.FileProvider;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;


public class ViewLogsActivity extends ThemedActivity {

  public static final String TAG = "ViewLogsActivity";

  @Override public String getTag() { return this.getClass().getSimpleName(); }
  @Override protected FrameLayout getBackBar() { return findViewById(org.firstinspires.inspection.R.id.backbar); }

  WebView webViewForLogcat;
  int DEFAULT_NUMBER_OF_LINES = 500;
  public static final String FILENAME = LaunchActivityConstantsList.VIEW_LOGS_ACTIVITY_FILENAME;
  private File logFile;
  int errorColor;

  String filepath = " ";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_logs);

    errorColor = getResources().getColor(R.color.text_warning);

    webViewForLogcat = (WebView) findViewById(R.id.webView);
    webViewForLogcat.getSettings().setBuiltInZoomControls(true); //enable pinch to zoom
    webViewForLogcat.getSettings().setDisplayZoomControls(false); //don't show cupcake-era zoom buttons
    webViewForLogcat.setBackgroundColor(getResources().getColor(R.color.logviewer_bgcolor));
  }

  @Override
  protected void onStart() {
    super.onStart();

    Intent intent = getIntent();
    Serializable extra = intent.getSerializableExtra(FILENAME);
    if(extra != null) {
      filepath = (String) extra;
    }

    logFile = new File(filepath);

    try {
      String output = readNLines(DEFAULT_NUMBER_OF_LINES);
      Spannable colorized = colorize(output);

      String html = String.format("<span style='white-space: nowrap;'><font face='monospace' color='white'>%s</font></span>", Html.toHtml(colorized));

      webViewForLogcat.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView webView, String url) {
          /*
           * This is a stupid hack because none of the other ways I could find
           * on the interwebs for scrolling to the bottom worked. Bah hambug.
           */
          webViewForLogcat.scrollTo(0, 900000000);
        }
      });

      webViewForLogcat.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);

          sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "FTC Robot Log - " +
                  java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
          sendIntent.putExtra(android.content.Intent.EXTRA_STREAM,
                  FileProvider.getUriForFile(ViewLogsActivity.this, getPackageName() + ".provider", logFile));

          sendIntent.setType("text/plain");
          startActivity(sendIntent);
          return false;
        }
      });

      webViewForLogcat.loadData(html, "text/html", "UTF-8");

    } catch (IOException e) {
        RobotLog.ee(TAG, e, "Exception loading logcat data");
      webViewForLogcat.loadData("<font color='white'>Error loading logcat data</font>", "text/html", "UTF-8");
    }
  }

  public String readNLines(int n) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
    String[] ringBuffer = new String[n];
    int totalLines = 0;
    String line = null;
    // read into the circular buffer, storing only 'n' lines at a time.
    while ((line = bufferedReader.readLine()) != null) {
      ringBuffer[totalLines % ringBuffer.length] = line;
      totalLines++;
    }

    // this may be in the middle of the ringbuffer,
    // so if we mod by the length of the ringBuffer, we'll get the
    // "start" of the lines. i.e., the "oldest" line.
    int start = totalLines - n;
    if (start < 0) {
      start = 0;
    }

    String output = "";
    for (int i = start; i < totalLines; i++) {
      // this will get you to the "oldest" line in the ringBuffer
      int index = i % ringBuffer.length;
      String currentLine = ringBuffer[index];
      output += currentLine + "\n";
    }

    // Logcat sometimes duplicates logs, so we can also just read from
    // the last "--------- beginning" print out.
    int mostRecentIndex = output.lastIndexOf("--------- beginning");
    if (mostRecentIndex < 0) {
      // that string wasn't found, so just return everything
      return output;
    }
    return output.substring(mostRecentIndex);

  }

  private Spannable colorize(String output) {
    Spannable span = new SpannableString(output);
    String[] lines = output.split("\\n");
    int currentStringIndex = 0;
    for (String line : lines) {
      /*
       * Note: (2020) historically this was "E/RobotCore" || RobotLog.ERROR_PREPEND
       * and had not been touched since 2015.
       *
       * As far as I'm aware, that never actually worked. And in any case,
       * now we generate errors from tags other than RobotCore, so we just highlight
       * and error-flagged messages. Also, RobotLog.ERROR_PREPEND was nuked since
       * this was the only place it was referenced anyway...
       */
      if (line.contains(" E ")) {
        span.setSpan(new ForegroundColorSpan(errorColor),
            currentStringIndex, currentStringIndex + line.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      currentStringIndex += line.length();
      currentStringIndex++; // add for each new line character that we "split" by.
    }

    return span;
  }
}

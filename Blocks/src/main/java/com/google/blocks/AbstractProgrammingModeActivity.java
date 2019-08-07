// Copyright 2016 Google Inc.

package com.google.blocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.webserver.RobotControllerWebInfo;
import org.firstinspires.ftc.robotserver.internal.webserver.PingDetails;
import org.firstinspires.ftc.robotserver.internal.webserver.PingDetailsHolder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for ProgrammingModeActivity, which is used in the robot controller, and
 * RemoteProgrammingModeActivity, which is used in the driver station.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class AbstractProgrammingModeActivity extends ThemedActivity {
  private Context context;
  private TextView textViewLog;

  private TableLayout tableLayoutActiveConnections;
  private final Timer timer = new Timer();
  private volatile TimerTask timerTask;
  private PingDetailsHolder pingDetailsHolder = new PingDetailsHolder();
  private volatile List<PingDetails> currentPingDetails;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_programming_mode);
    setTypeface();

    context = this;
    textViewLog = (TextView) findViewById(R.id.log);
    tableLayoutActiveConnections = (TableLayout) findViewById(R.id.active_connections);

    ImageButton buttonMenu = (ImageButton) findViewById(R.id.menu_buttons);
    buttonMenu.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PopupMenu popupMenu = new PopupMenu(AbstractProgrammingModeActivity.this, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            return onOptionsItemSelected(item); // Delegate to the handler for the hardware menu button
          }
        });
        popupMenu.inflate(R.menu.menu_server);
        popupMenu.show();
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();

    updateActiveConnectionsUI();
    timerTask = new TimerTask() {
      @Override
      public void run() {
        removeOldPings();
      }
    };
    long millis = TimeUnit.SECONDS.toMillis(PingDetailsHolder.REMOVE_OLD_PINGS_INTERVAL_SECONDS);
    timer.schedule(timerTask, millis, millis);
  }

  @Override
  public void onPause() {
    super.onPause();

    timerTask.cancel();
    timerTask = null;
    timer.purge();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_server, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_help) {
      Toast.makeText(this, getString(R.string.help_content), Toast.LENGTH_SHORT).show();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Toggle the visibility of the server log.
   */
  public void onClickDisplayServerLogButton(View view) {
    View instructionsAndStatusContainer = (View) findViewById(R.id.instructions_and_status_container);
    View logContainer = (View) findViewById(R.id.log_container);
    boolean logPreviouslyVisible = (logContainer.getVisibility() == View.VISIBLE);

    // Also toggle the instructions and status so the log has lots of space.
    instructionsAndStatusContainer.setVisibility(logPreviouslyVisible ? View.VISIBLE : View.GONE);
    logContainer.setVisibility(logPreviouslyVisible ? View.GONE : View.VISIBLE);

    Button button = (Button) findViewById(R.id.display_server_log_button);
    button.setText(getString(logPreviouslyVisible
        ? R.string.display_server_log_button : R.string.hide_server_log_button));
  }

  protected void updateDisplay(final RobotControllerWebInfo rcConnectionInfo) {
    // Updating the display should be done on the Android UI thread.
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        String notAvailable = getString(R.string.not_available);
        boolean networkOK = true;

        TextView textView = (TextView) findViewById(R.id.network_name);
        if (rcConnectionInfo.getNetworkName() != null
            && !rcConnectionInfo.getNetworkName().isEmpty()) {
          textView.setText(rcConnectionInfo.getNetworkName());
        } else {
          networkOK = false;
          textView.setText(notAvailable);
        }
        textView.requestLayout();

        textView = (TextView) findViewById(R.id.passphrase);
        if (rcConnectionInfo.getPassphrase() != null
            && !rcConnectionInfo.getPassphrase().isEmpty()) {
          textView.setText(rcConnectionInfo.getPassphrase());
        } else {
          networkOK = false;
          textView.setText(notAvailable);
        }
        textView.requestLayout();

        textView = (TextView) findViewById(R.id.server_url);
        if (rcConnectionInfo.getServerUrl() != null
            && !rcConnectionInfo.getServerUrl().isEmpty()) {
          textView.setText(rcConnectionInfo.getServerUrl());
        } else {
          networkOK = false;
          textView.setText(notAvailable);
        }
        textView.requestLayout();

        textView = (TextView) findViewById(R.id.server_status);
        if (!networkOK) {
          textView.setText(getString(R.string.network_not_ok));
          textView.setTextColor(AppUtil.getInstance().getColor(R.color.text_error));
        } else if (!rcConnectionInfo.isServerAlive()) {
          // TODO(lizlooney): show actual error message so the user knows why the server is not alive.
          textView.setText(getString(R.string.server_not_ok));
          textView.setTextColor(AppUtil.getInstance().getColor(R.color.text_error));
        } else {
          textView.setText(AppUtil.getDefContext().getString(R.string.server_ok, rcConnectionInfo.getTimeServerStarted()));
          textView.setTextColor(AppUtil.getInstance().getColor(R.color.text_okay));
        }
        textView.requestLayout();
      }
    });
  }

  /**
   * Sets the typeface of the text views that show the server connection information to the SERIF
   * typeface.
   */
  @SuppressLint("WrongConstant")
  private void setTypeface() {
    Typeface typeface = Typeface.create(Typeface.SERIF, 0);

    TextView textView;

    textView = (TextView) findViewById(R.id.network_name);
    textView.setTypeface(typeface);
    textView.requestLayout();

    textView = (TextView) findViewById(R.id.passphrase);
    textView.setTypeface(typeface);
    textView.requestLayout();

    textView = (TextView) findViewById(R.id.server_url);
    textView.setTypeface(typeface);
    textView.requestLayout();
  }

  protected void addMessageToTextViewLog(final String msg) {
    Runnable updateTextViewLog = new Runnable() {
      @Override
      public void run() {
        textViewLog.setText(textViewLog.getText() + msg + "\n");
        textViewLog.requestLayout();
      }
    };
    runOnUiThread(updateTextViewLog);
  }

  protected void addPing(PingDetails pingDetails) {
    pingDetailsHolder.addPing(pingDetails);
    updateActiveConnectionsUI();
  }

  private void removeOldPings() {
    if (pingDetailsHolder.removeOldPings()) {
      updateActiveConnectionsUI();
    }
  }

  private void updateActiveConnectionsUI() {
    final List<PingDetails> sortedPingDetails = pingDetailsHolder.sortedPingDetailsList();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (sortedPingDetails.equals(currentPingDetails)) {
          // Don't need to update the table.
          return;
        }

        tableLayoutActiveConnections.removeAllViews();
        if (sortedPingDetails.isEmpty()) {
          TableRow row = new TableRow(context);
          TextView textView = new TextView(context);
          textView.setText("<none>");
          row.addView(textView);
          tableLayoutActiveConnections.addView(row);
        } else {
          for (PingDetails pingDetails : sortedPingDetails) {
            TableRow row = new TableRow(context);

            String name = pingDetails.name;
            TextView textView = new TextView(context);
            textView.setText(pingDetails.machineName);
            row.addView(textView);

            row.addView(new TextView(context), new TableRow.LayoutParams(0, 0, 0.5f));

            textView = new TextView(context);
            textView.setText(name);
            row.addView(textView);

            row.addView(new TextView(context), new TableRow.LayoutParams(0, 0, 0.5f));

            tableLayoutActiveConnections.addView(row);
          }
        }
        tableLayoutActiveConnections.requestLayout();

        currentPingDetails = sortedPingDetails;
      }
    });
  }
}

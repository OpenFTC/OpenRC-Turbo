package com.qualcomm.ftccommon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class StackTraceActivity extends Activity
{
    public static final String KEY_STACK_TRACE = "KEY_STACK_TRACE";

    WebView webView;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        setContentView(R.layout.activity_stacktrace);
        TextView headerErrorMessage = findViewById(R.id.header_error_message);

        String stackTrace = getIntent().getExtras().getString(KEY_STACK_TRACE);

        headerErrorMessage.setText(stackTrace.substring(0, stackTrace.indexOf("\n")));

        webView = findViewById(R.id.webView);
        webView.getSettings().setBuiltInZoomControls(true); // enable pinch to zoom
        webView.getSettings().setDisplayZoomControls(false); // don't show cupcake-era zoom buttons
        webView.setBackgroundColor(getResources().getColor(R.color.logviewer_bgcolor));

        String html = String.format("<span style='white-space: nowrap;'><font face='monospace' color='white'><pre>%s</pre></font></span>", stackTrace.replace("\n", "<br>").replace("\t", "    "));

        webView.loadData(html, "text/html", "UTF-8");
    }

    public void onAccept(View v)
    {
        finish();
    }

    public void onZoomIn(View v) {
        webView.zoomIn();
    }

    public void onZoomOut(View v) {
        webView.zoomOut();
    }
}

package com.example.irtools;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WebDisplayer extends AppCompatActivity {
    TextView positionTextView;
    TextView urlTextView;
    WebView webView;
    TextView tfidfTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_displayer);
        positionTextView = findViewById(R.id.positionTextView);
        urlTextView = findViewById(R.id.urlTextView);
        tfidfTextView = findViewById(R.id.tfidfTextView);
        webView = findViewById(R.id.webView);

        tfidfTextView.setText(String.format("%.2f", getIntent().getDoubleExtra("value", 0.0)));

        //transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        String url = getIntent().getStringExtra("url");
        if (!url.startsWith("http"))
            url = "https://" + url;
        urlTextView.setText(url);
        positionTextView.setText("" + getIntent().getIntExtra("position", 0));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}

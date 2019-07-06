package com.example.cookforyou;

import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RecipeActivity extends AppCompatActivity {

    private WebView webView;
    private ContentLoadingProgressBar mProgressBar;

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        Bundle extras = getIntent().getExtras();
        String recipeUrl = (String) extras.get("recipeLink");

        webView = findViewById(R.id.webViewActivity);
        mProgressBar = findViewById(R.id.website_loading_progress_bar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress < 100) {
                    mProgressBar.setProgress(newProgress);
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        //This is to prevent rejection of DOM storage on phone.
        //which will cause a flood of null exception errors in Logcat.
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
//        {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });
        webView.loadUrl(recipeUrl);

    }
}

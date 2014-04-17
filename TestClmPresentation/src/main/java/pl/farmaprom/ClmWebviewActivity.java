package pl.farmaprom;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ClmWebviewActivity extends ActionBarActivity {

    private HTML5WebView webView;
    private static final String TAG = "ClmWebviewActivity";
    public static final String EXTRA_PRESENTATION_PATH = "presentationPath";
    public static final String EXTRA_PRESENTATION_ID = "presentationId";

    long presentationId;
    boolean presentationStarted = false;
    boolean presentationFinished = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new HTML5WebView(this);
        setContentView(webView.getLayout());

        String presentationPath = getIntent().getExtras().getString("path");
        String indexPageUri = String.format("file://%s", presentationPath);

        presentationId = getIntent().getExtras().getLong(EXTRA_PRESENTATION_ID);

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startSupportActionMode(new MyActionMode());
                Log.d("testXXX", "long click");
                return true;
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        JSInterface jsInterface = new JSInterface();
        webView.addJavascriptInterface(jsInterface, "izi");
        webView.addJavascriptInterface(jsInterface, "android");
        webView.loadUrl(indexPageUri);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                presentationStarted = true;
            }
        });

        getSupportActionBar().hide();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (presentationStarted) {
            webView.loadUrl("javascript:resumePresentation()");
        }
    }

    @Override
    protected void onPause() {
        if (!presentationFinished) {
            webView.loadUrl("javascript:pausePresentation()");
        }
        super.onPause();
    }

    public class JSInterface {
        @JavascriptInterface
        public void finish(String result) throws IOException {
            try {
                presentationFinished = true;
                Log.d(TAG, String.format("result JSON: %s", result));
                JSONArray array = new JSONArray(result);
                JSONObject json = new JSONObject();
                json.put("presentationId", presentationId);
                json.put("resultArray", array);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", json.toString());
                setResult(RESULT_OK, returnIntent);
                (ClmWebviewActivity.this).finish();
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private final class MyActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add("Wyjdź z Webview").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ClmWebviewActivity.this.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.inCustomView()) {
            webView.hideCustomView();
        } else {
            webView.loadUrl("javascript:finishPresentation()");
        }
    }

}

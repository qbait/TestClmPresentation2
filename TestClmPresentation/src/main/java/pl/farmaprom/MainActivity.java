package pl.farmaprom;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_CODE = 6384;
    Preferences preferences;
    Button choosePathButton;
    Button openButton;
    TextView resultJsonTextView;
    String path;
    String result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosePathButton = (Button) findViewById(R.id.choosePathButton);
        openButton = (Button) findViewById(R.id.openButton);
        resultJsonTextView = (TextView) findViewById(R.id.resultJsonTextView);
        preferences = new Preferences(this);

        if (savedInstanceState == null) {
            path = preferences.getPath();
        } else {
            path = savedInstanceState.getString("path");
            result = savedInstanceState.getString("result");
        }
        updatePathButton();
        updateResultTextView();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", path);
        outState.putString("result", result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();

                        try {
                            final File file = FileUtils.getFile(this, uri);
                            path = file.getAbsolutePath();
                            preferences.setPath(path);
                            updatePathButton();
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    result = data.getStringExtra("result");
                    updateResultTextView();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showChooser(View view) {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(
                target, getString(R.string.choose_program));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startPresentation(View view) {
        Intent intent = new Intent(this, ClmWebviewActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("json", path);
        startActivityForResult(intent, 1);
    }

    private void updatePathButton() {
        String text = getString(R.string.path_button);
        text = text.replace("$PATH", path);
        choosePathButton.setText(Html.fromHtml(text));

        if (preferences.isPathSet()) {
            openButton.setEnabled(true);
        }
    }

    private void updateResultTextView() {
        if (result == null) {
            return;
        }
        try {
            JSONObject json = new JSONObject(result);
            resultJsonTextView.setText(json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

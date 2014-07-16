package pl.farmaprom;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.apache.commons.io.FilenameUtils;
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
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosePathButton = (Button) findViewById(R.id.choosePathButton);
        openButton = (Button) findViewById(R.id.openButton);
        resultJsonTextView = (TextView) findViewById(R.id.resultJsonTextView);
        preferences = new Preferences(this);

        path = preferences.getPath();
        updatePathButton();
        updateResultTextView();

    }

    private class DecompressReceiver extends ResultReceiver {
        public DecompressReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            long taskId = (int)resultData.getLong(DecompressService.EXTRA_ID);
            if (resultCode == DecompressService.STARTED) {
                progressDialog = ProgressDialog.show(MainActivity.this, "", "Rozpakowywanie...");
            } else if (resultCode == DecompressService.FINISHED) {
                String notificationText = "gotowa do użycia";
                preferences.setPath(path);
                updatePathButton();
                progressDialog.dismiss();
            } else if (resultCode == DecompressService.ERROR) {
                String notificationText = "problem z przetwarzaniem";
                progressDialog.dismiss();
            }
        }
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
                            String extension = FilenameUtils.getExtension(path);
                            if(extension.equals("zip")) {
                                Intent intent = new Intent(this, DecompressService.class);

                                intent.putExtra(DecompressService.EXTRA_ZIPFILE, path);
                                intent.putExtra(DecompressService.EXTRA_DESTINATION_LOCATION, FilenameUtils.getPath(path));
                                intent.putExtra(DecompressService.EXTRA_ID, 1);
                                intent.putExtra(DecompressService.EXTRA_RECEIVER, new DecompressReceiver(new Handler()));

                                startService(intent);

                                path = "/" + FilenameUtils.getPath(path) + FilenameUtils.getBaseName(path) + "/index.html";
                            } else {
                                preferences.setPath(path);
                                updatePathButton();
                            }


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

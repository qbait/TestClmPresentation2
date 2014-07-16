package pl.farmaprom;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class DecompressService extends IntentService {
    private static final String TAG = "DecompressService";

    public static final int STARTED = 1;
    public static final int FINISHED = 2;
    public static final int ERROR = 3;

    public static final String EXTRA_ZIPFILE = "zipFile";
    public static final String EXTRA_DESTINATION_LOCATION = "destinationLocation";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_RECEIVER = "receiver";

    ResultReceiver receiver;

    public DecompressService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        receiver = (ResultReceiver) intent.getParcelableExtra(EXTRA_RECEIVER);

        long id = intent.getExtras().getLong(EXTRA_ID);
        String zipFile = intent.getExtras().getString(EXTRA_ZIPFILE);
        String location = intent.getExtras().getString(EXTRA_DESTINATION_LOCATION);

        final Thread t = new Thread(new DecompressThread(id, zipFile, location));
        t.start();
    }

    public class DecompressThread implements Runnable {
        long id;
        String zipFile;
        String location;

        public DecompressThread(long id, String zipFile, String location) {
            this.id = id;
            this.zipFile = zipFile;
            this.location = location;
        }

        @Override
        public void run() {
            decompressFile(id, zipFile, location);
        }
    }

    private void decompressFile(long id, String filePath, String destinationPath) {
        sendStarted(id);
        File archive = new File(filePath);
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, destinationPath);
            }
            sendFinished(id);
        } catch (Exception e) {
            Log.e(TAG, "Error while extracting file " + archive, e);
            sendError(id);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                            String outputDir) throws IOException {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Log.v(TAG, "Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }


    private void sendStarted(long presentationId) {
        Bundle resultData = new Bundle();
        resultData.putLong(EXTRA_ID, presentationId);
        receiver.send(STARTED, resultData);
    }

    private void sendFinished(long presentationId) {
        Bundle resultData = new Bundle();
        resultData.putLong(EXTRA_ID, presentationId);
        receiver.send(FINISHED, resultData);
    }

    private void sendError(long presentationId) {
        Bundle resultData = new Bundle();
        resultData.putLong(EXTRA_ID, presentationId);
        receiver.send(ERROR, resultData);
    }
}

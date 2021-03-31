package be.sourcery.ascent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

public class ImportDataActivity extends MyActivity {
    private static final String TAG = "ImportDataActivity";

    private static final int ID_DIALOG_PROGRESS = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_data);
        setTitle(R.string.importData);

        // Capture our button from layout
        TextView text = findViewById(R.id.importTitle);
        Button button = findViewById(R.id.ok);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable(){
                    public void run() {
                        openFilePicker();
                    }
                }).start();
            }
        });
        // Authenticate the user. For most apps, this should be done when the user performs an
        // action that requires Drive access rather than in onCreate.
        requestSignIn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_DOCUMENT:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFileFromFilePicker(uri);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Opens the Storage Access Framework file picker using {@link #REQUEST_CODE_OPEN_DOCUMENT}.
     */
    private void openFilePicker() {

            Log.d(TAG, "Opening file picker.");

            Intent pickerIntent = mDriveServiceHelper.createFilePickerIntent();

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT);

    }

    /**
     * Opens a file from its {@code uri} returned from the Storage Access Framework file picker
     * initiated by {@link #openFilePicker()}.
     */
    private void openFileFromFilePicker(Uri uri) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening " + uri.getPath());

            mDriveServiceHelper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;
                        importData(content);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to open file from picker.", exception));
        }
    }


    protected void importData(String content) {
        InternalDB db = new InternalDB(this);
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            db.clearData();
            BufferedReader r = new BufferedReader(new StringReader(content));
            int count = 0;
            String line = r.readLine();
            while (line != null) {
                Ascent ascent = CodecUtil.decode(line);
                if (ascent != null) {
                    db.addAscent(ascent);
                    count++;
                }
                line = r.readLine();
            }
            Log.d(TAG, "done importing " + count);
            Intent intent = this.getIntent();
            intent.putExtra("count", count);
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
            setResult(RESULT_CANCELED);
        } finally {
            db.close();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

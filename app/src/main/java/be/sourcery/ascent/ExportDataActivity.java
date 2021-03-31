package be.sourcery.ascent;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import com.google.android.gms.tasks.Task;

public class ExportDataActivity extends MyActivity {

    private static final String TAG = "ExportDataActivity";

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    private static final int REQUEST_CODE = 1;

    private final String name = "ascent.csv";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.import_data);
        setTitle(R.string.exportData);
        // Capture our button from layout
        TextView text = findViewById(R.id.importTitle);
        Button button = findViewById(R.id.ok);
        button.setText(R.string.exportButton);
        // Register the onClick listener with the implementation above
        requestSignIn();
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                createFile();
            }
        });
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createFile() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Creating a file.");

            mDriveServiceHelper.createFile(name)
                    .addOnSuccessListener(fileId -> exportData(fileId))
                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create file.", exception));
        }
    }

    protected void exportData(String fileId) {
        if (mDriveServiceHelper != null) {
            InternalDB db = new InternalDB(this);
            int count = 0;
            try {
                // structure:
                // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
                StringBuilder buf = new StringBuilder(8192);
                List<Ascent> ascents = db.getAscents(false);
                for (Iterator<Ascent> iterator = ascents.iterator(); iterator.hasNext(); count++) {
                    Ascent ascent = iterator.next();
                    buf.append(CodecUtil.encode(ascent));
                }

                mDriveServiceHelper.saveFile(fileId, name, buf.toString())
                        .addOnFailureListener(exception ->
                                Log.e(TAG, "Unable to save file to Drive.", exception));
                Intent intent = this.getIntent();
                intent.putExtra("count", count);
                setResult(RESULT_OK, intent);
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.getMessage());
                setResult(RESULT_CANCELED);
            } finally {
                db.close();
            }
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

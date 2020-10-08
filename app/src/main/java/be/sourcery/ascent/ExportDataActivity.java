package be.sourcery.ascent;

import java.io.BufferedWriter;
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

public class ExportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    private static final int REQUEST_CODE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.import_data);
        setTitle(R.string.exportData);
        // Capture our button from layout
        TextView text = findViewById(R.id.importTitle);
        Button button = findViewById(R.id.ok);
        button.setText(R.string.exportButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // We create a new AuthSession so that we can use the Dropbox API.
                File sdcard = Environment.getExternalStorageDirectory();
                File exportFile = new File(sdcard, "ascent-export.csv");
                if (exportFile.exists()) {
                    // show confirm dialog
                    new AlertDialog.Builder(ExportDataActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.overwrite)
                    .setMessage(R.string.overwriteText)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            exportProgress();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
                } else {
                    exportProgress();
                }
            }
        });
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            text.setText(R.string.exportToFile);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            text.setText(R.string.notMounted);
            button.setEnabled(false);
        }
    }
    
    protected void exportData() {
        InternalDB db = new InternalDB(this);
        int count = 0;
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File importFile = new File(sdcard, "ascent-export.csv");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(importFile), StandardCharsets.UTF_8));
            List<Ascent> ascents = db.getAscents(false);
            for (Iterator<Ascent> iterator = ascents.iterator(); iterator.hasNext(); count++) {
                Ascent ascent = iterator.next();
                bw.write(CodecUtil.encode(ascent));
            }
            bw.close();
            
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

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == ID_DIALOG_PROGRESS) {
            ProgressDialog loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage(getString(R.string.exportingData));
            loadingDialog.setIndeterminate(true);
            loadingDialog.setCancelable(false);
            return loadingDialog;
        }
        return super.onCreateDialog(id);
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

    private void exportProgress() {
        showDialog(ID_DIALOG_PROGRESS);
        new Thread(new Runnable(){
            public void run() {
                exportData();
                dismissDialog(ID_DIALOG_PROGRESS);
                finish();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ExportDataActivity.this, "SD access granted", Toast.LENGTH_SHORT).show();
                    Button button = findViewById(R.id.ok);
                    button.setEnabled(true);
                } else {
                    Toast.makeText(ExportDataActivity.this, "SD access granted", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}

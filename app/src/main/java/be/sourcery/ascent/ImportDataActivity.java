package be.sourcery.ascent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import android.app.Dialog;
import android.app.ProgressDialog;
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

public class ImportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    private static final int REQUEST_CODE = 1;

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
                showDialog(ID_DIALOG_PROGRESS);
                new Thread(new Runnable(){
                    public void run() {
                        importData();
                        dismissDialog(ID_DIALOG_PROGRESS);
                        finish();
                    }
                }).start();
            }
        });
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            text.setText(R.string.importFileFound);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    protected void importData() {
        InternalDB db = new InternalDB(this);
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File importFile = new File(sdcard, "ascent.csv");
            
            db.clearData();
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), StandardCharsets.UTF_8));
            int count = 0;
            while (r.ready()) {
                String line = r.readLine();
                Ascent ascent = CodecUtil.decode(line);
                if (ascent != null) {
                    db.addAscent(ascent);
                    count++;
                }
            }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ImportDataActivity.this, "SD access granted", Toast.LENGTH_SHORT).show();
                    Button button = findViewById(R.id.ok);
                    button.setEnabled(true);
                } else {
                    Toast.makeText(ImportDataActivity.this, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == ID_DIALOG_PROGRESS) {
            ProgressDialog loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage(getString(R.string.importingData));
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

}

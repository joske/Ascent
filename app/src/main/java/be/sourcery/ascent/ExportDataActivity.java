package be.sourcery.ascent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;


public class ExportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	private DbxClientV2 client;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.import_data);
        setTitle(R.string.exportData);
        setupActionBar();
        // Capture our button from layout
        TextView text = (TextView)findViewById(R.id.importTitle);
        Button button = (Button)findViewById(R.id.ok);
        button.setText(R.string.exportButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // We create a new AuthSession so that we can use the Dropbox API.
                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
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
            button.setEnabled(true);
        } else {
            text.setText(R.string.notMounted);
            button.setEnabled(false);
        }
    }
    
    protected void exportData() {
        InternalDB db = new InternalDB(this);
        int count = 0;
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File importFile = new File(getFilesDir(), "ascent.csv");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(importFile), "ISO-8859-1"));
            List<Ascent> ascents = db.getAscents();
            for (Iterator<Ascent> iterator = ascents.iterator(); iterator.hasNext(); count++) {
                Ascent ascent = iterator.next();
                bw.write(CodecUtil.encode(ascent));
            }
            bw.close();
            
            DbxUserFilesRequests files = client.files();
            FileInputStream inputStream = new FileInputStream(importFile);
            FileMetadata response = files.uploadBuilder("/ascent.csv").withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            Log.i("DbExampleLog", "The uploaded file's rev is: " + response.getRev());
            Intent intent = this.getIntent();
            intent.putExtra("count", count);
            intent.putExtra("rev", response.getRev());
            setResult(RESULT_OK, intent);
        } catch (Exception e) {
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
                if (client != null) {
                    exportData();
                } else {
                    setResult(RESULT_CANCELED);
                }
                dismissDialog(ID_DIALOG_PROGRESS);
                finish();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAuth();
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken != null) {
	        DbxRequestConfig requestConfig = new DbxRequestConfig("ascent");
	        client = new DbxClientV2(requestConfig, accessToken);
        }
    }

}

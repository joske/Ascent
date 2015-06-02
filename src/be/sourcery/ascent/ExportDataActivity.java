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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ExportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        if (!loggedIn) {
        	mDBApi.getSession().startOAuth2Authentication(ExportDataActivity.this);
        }
        
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
            File sdcard = Environment.getExternalStorageDirectory();
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
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent-export.csv");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(importFile), "ISO-8859-1"));
            List<Ascent> ascents = db.getAscents();
            for (Iterator iterator = ascents.iterator(); iterator.hasNext(); count++) {
                Ascent ascent = (Ascent)iterator.next();
                StringBuffer line = new StringBuffer(200);
                line.append(ascent.getRoute().getName()).append(";");
                line.append(ascent.getRoute().getGrade()).append(";");
                line.append(ascent.getRoute().getCrag().getName()).append(";");
                line.append(ascent.getRoute().getCrag().getCountry()).append(";");
                line.append(ascent.getStyle()).append(";");
                line.append(ascent.getAttempts()).append(";");
                line.append(fmt.format(ascent.getDate())).append(";");
                line.append(ascent.getComment()).append(";");
                line.append(ascent.getStars()).append("\r\n");
                bw.write(line.toString());
            }
            bw.close();
            
            Entry existingEntry = mDBApi.metadata("/ascent.csv", 1, null, false, null);
            
            FileInputStream inputStream = new FileInputStream(importFile);
            String rev = existingEntry != null ? existingEntry.rev : null;
			Entry response = mDBApi.putFile("/ascent.csv", inputStream,
                                            importFile.length(), rev, null);
            Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            Intent intent = this.getIntent();
            intent.putExtra("count", count);
            intent.putExtra("rev", response.rev);
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
                exportData();
                dismissDialog(ID_DIALOG_PROGRESS);
                finish();
            }
        }).start();
    }
    
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();
                // Store it locally in our app for later use
                storeAuth(mDBApi.getSession());
            } catch (IllegalStateException e) {
                Log.i("Ascent", "Error authenticating", e);
            }
        }
    }

}

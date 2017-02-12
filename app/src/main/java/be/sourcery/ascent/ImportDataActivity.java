package be.sourcery.ascent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.dropbox.core.v2.files.FileMetadata;


public class ImportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
	private DbxClientV2 client;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_data);
        setTitle(R.string.importData);

        // Capture our button from layout
        TextView text = (TextView)findViewById(R.id.importTitle);
        Button button = (Button)findViewById(R.id.ok);
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
            button.setEnabled(true);
        }
    }

    protected void importData() {
        InternalDB db = new InternalDB(this);
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File importFile = new File(getFilesDir(), "ascent.csv");
            FileOutputStream outputStream = new FileOutputStream(importFile);
            FileMetadata info = client.files().downloadBuilder("/ascent.csv").download(outputStream);
            Log.i(ImportDataActivity.class.getName(), "The file's rev is: " + info.getRev());
            
            // now we have downoaded from dropbox into the sdcard, we can start parsing this
            db.clearData();
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), "ISO-8859-1"));
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
            setResult(RESULT_CANCELED);
        } finally {
            db.close();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAuth();
        client = getClient();
     }

}

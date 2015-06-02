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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.android.AndroidAuthSession;

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


public class ImportDataActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_data);
        setTitle(R.string.importData);
        setupActionBar();

        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        if (!loggedIn) {
        	mDBApi.getSession().startOAuth2Authentication(ImportDataActivity.this);
        }

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
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent.csv");
            text.setText(R.string.importFileFound);
            button.setEnabled(true);
//            if (importFile.exists()) {
//                // file exists, enable button
//            } else {
//                text.setText(R.string.importFileNotFound);
//                button.setEnabled(false);
//            }
        }
    }

    protected void importData() {
        InternalDB db = new InternalDB(this);
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent.csv");
            FileOutputStream outputStream = new FileOutputStream(importFile);
            DropboxFileInfo info = mDBApi.getFile("/ascent.csv", null, outputStream, null);
            Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);
            
            // now we have downoaded from dropbox into the sdcard, we can start parsing this
            
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), "ISO-8859-1"));
            int count = 0;
            Map<String, Crag> crags = new HashMap<String, Crag>();
            while (r.ready()) {
                String line = r.readLine();
                String[] strings = line.split(";");
                if (strings.length == 9) {
                    try {
                        String routeName = strings[0];
                        String routeGrade = strings[1];
                        String cragName = strings[2];
                        String cragCountry = strings[3];
                        int style = Integer.parseInt(strings[4]);
                        int attempts = Integer.parseInt(strings[5]);
                        Date date = fmt.parse(strings[6]);
                        String comments = strings[7];
                        int stars = Integer.parseInt(strings[8]);

                        Crag crag = null;
                        crag = crags.get(cragName);
                        if (crag == null) {
                            // not found, create it
                            crag = db.addCrag(cragName, cragCountry);
                            crags.put(cragName, crag);
                        }
                        Route route = db.addRoute(routeName, routeGrade, crag);
                        Ascent ascent = db.addAscent(route, date, attempts, style, comments, stars);
                        if (ascent != null) {
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

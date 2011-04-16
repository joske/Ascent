package be.sourcery;

import greendroid.app.GDActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;


public class ImportDataActivity extends GDActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.import_data);
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
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent.csv");
            if (importFile.exists()) {
                // file exists, enable button
                text.setText(R.string.importFileFound);
                button.setEnabled(true);
            } else {
                text.setText(R.string.importFileNotFound);
                button.setEnabled(false);
            }
        }
    }

    protected void importData() {
        InternalDB db = new InternalDB(this);
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent.csv");
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), "ISO-8859-1"));
            int count = 0;
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
                        try {
                            crag = db.getCrag(cragName);
                        } catch (Exception e) {
                            // not found
                            e.printStackTrace();
                        }
                        if (crag == null) {
                            // not found, create it
                            crag = db.addCrag(cragName, cragCountry);
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
            Toast.makeText(this, "Imported " + count + " ascents", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
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


}

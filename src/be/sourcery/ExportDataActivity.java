package be.sourcery;

import greendroid.app.GDActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

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


public class ExportDataActivity extends GDActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.import_data);
        setTitle(R.string.exportData);
        // Capture our button from layout
        TextView text = (TextView)findViewById(R.id.importTitle);
        Button button = (Button)findViewById(R.id.ok);
        button.setText(R.string.exportButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(ID_DIALOG_PROGRESS);
                new Thread(new Runnable(){
                    public void run() {
                        exportData();
                        dismissDialog(ID_DIALOG_PROGRESS);
                        finish();
                    }
                }).start();
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
        try {
            // structure:
            // routeName;routeGrade;cragName;cragCountry;style;attempts;date;comment;stars
            File sdcard = Environment.getExternalStorageDirectory();
            File importFile = new File(sdcard, "ascent-export.csv");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(importFile), "ISO-8859-1"));
            int count = 0;
            List<Ascent> ascents = db.getAscents();
            for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
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
                line.append(ascent.getStars()).append("\n");
                bw.write(line.toString());
            }
            Toast.makeText(this, "Exported " + count + " ascents", Toast.LENGTH_SHORT).show();
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
            loadingDialog.setMessage(getString(R.string.exportingData));
            loadingDialog.setIndeterminate(true);
            loadingDialog.setCancelable(false);
            return loadingDialog;
        }
        return super.onCreateDialog(id);
    }


}

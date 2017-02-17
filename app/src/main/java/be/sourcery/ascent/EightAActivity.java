package be.sourcery.ascent;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import be.sourcery.ascent.eighta.EightAAscent;
import be.sourcery.ascent.eighta.Login;

/**
 * Created by jos on 16/02/17.
 */

public class EightAActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    private ListView listView;
    private InternalDB db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eighta);
        setTitle("Login to 8a.nu");
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        db = new InternalDB(this);
        final TextView emailField = (TextView) findViewById(R.id.email);
        final TextView passwordField = (TextView) findViewById(R.id.password);
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String email = emailField.getText().toString();
                final String password = passwordField.getText().toString();
                showDialog(ID_DIALOG_PROGRESS);
                new Thread(new Runnable(){
                    public void run() {
                        importData(email, password);
                        dismissDialog(ID_DIALOG_PROGRESS);
                        setSuccess();
                    }
                }).start();
            }
        });
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCancelled();
            }
        });
    }

    private void importData(String email, String password) {
        Login login = new Login(email, password);
        String userId = login.login();
        if (userId != null) {
            EightAAscent[] ascents = login.getRouteAscents();
            importAscents(ascents);
        } else {
            setCancelled();
        }
    }

    private void setCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    private void importAscents(EightAAscent[] ascents) {
        for (int i = 0; i < ascents.length; i++) {
            Ascent ascent = importAscent(ascents[i]);
            Log.i(this.getClass().getName(), String.format("imported %d : %s", i, ascent));
        }
    }

    private Ascent importAscent(EightAAscent ascent) {
        Crag crag = db.getCrag(ascent.getCrag(), ascent.getCountryCode());
        if (crag == null) {
            crag = db.addCrag(ascent.getCrag(), ascent.getCountryCode());
        }
        Route route = db.getRoute(new Route(0, ascent.getName(), ascent.getGrade(), crag, 0, null));
        if (route == null) {
            route = db.addRoute(ascent.getName(), translateGrade(ascent.getGrade()), crag, ascent.getSector());
        }
        Ascent newAscent = db.addAscent(route, ascent.getDate(), 0, translateStyle(ascent.getObjectClass(), ascent.isRepeat(), ascent.getStyle()), ascent.getComment(), ascent.getRating(), false, ascent.getId());
        return newAscent;
    }

    private String translateGrade(String grade) {
        if (grade != null && grade.startsWith("4")) {
            return "4";
        }
        if (grade != null && grade.startsWith("3")) {
            return "3";
        }
        return grade;
    }

    private int translateStyle(String objectClass, boolean repeat, int style) {
        if (objectClass != null && objectClass.equals("CLS_UserAscent_Try")) {
            return Ascent.STYLE_TRIED;
        }
        if (repeat) {
            return Ascent.STYLE_REPEAT;
        }
        switch (style) {
            case 1:
                return Ascent.STYLE_REDPOINT;
            case 2:
                return Ascent.STYLE_FLASH;
            case 3:
                return Ascent.STYLE_ONSIGHT;
            case 4:
                return Ascent.STYLE_TOPROPE;
        }
        return Ascent.STYLE_REDPOINT;
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

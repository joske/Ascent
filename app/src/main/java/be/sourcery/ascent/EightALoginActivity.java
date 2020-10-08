package be.sourcery.ascent;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import be.sourcery.ascent.eighta.EightA;

/**
 * Created by jos on 16/02/17.
 */

public class EightALoginActivity extends MyActivity {

    private static final int ID_DIALOG_PROGRESS = 1;
    private ListView listView;
    private InternalDB db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eighta_login);
        setTitle("Login to 8a.nu");
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final TextView emailField = (TextView) findViewById(R.id.email);
        final TextView passwordField = (TextView) findViewById(R.id.password);
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String email = emailField.getText().toString();
                final String password = passwordField.getText().toString();
                showDialog(ID_DIALOG_PROGRESS);
                new LoginTask().execute(email, password);
            }
        });
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCancelled();
            }
        });
    }

    private void setCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setSuccess() {
        setResult(RESULT_OK);
        finish();
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

    class LoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = login(params[0], params[1]);
            return success;
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_LONG);
                dismissDialog(ID_DIALOG_PROGRESS);
                setSuccess();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to login", Toast.LENGTH_LONG);
                dismissDialog(ID_DIALOG_PROGRESS);
                setCancelled();
            }
        }
    }

    private boolean login(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        if (prefs.getString(USER_ID, null) == null) {
            EightA eightA = new EightA();
            String userId = eightA.login(email, password);
            if (userId != null) {
                prefs.edit().putString(USER_ID, eightA.getUserId()).commit();
                prefs.edit().putString(SESSION_ID, eightA.getSessionId()).commit();
                return true;
            }
            return false;
        } else {
            return true;
        }
    }



}

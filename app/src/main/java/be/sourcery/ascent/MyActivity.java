package be.sourcery.ascent;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;


public abstract class MyActivity extends AppCompatActivity {

	protected static final String ACCOUNT_PREFS_NAME = "prefs";
	protected static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	protected boolean loggedIn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

        /**
         * Shows keeping the access keys returned from Trusted Authenticator in a local
         * store, rather than storing user name & password, and re-authenticating each
         * time (which is not to be done, ever).
         */
    protected void loadAuth() {
     	String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            prefs.edit().putString("access-token", accessToken).apply();
            String key = prefs.getString(ACCESS_KEY_NAME, null);
            if (key == null || key.length() == 0) {
            	return;
            }
            this.loggedIn = true;
        }
    }

    protected DbxClientV2 getClient() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken != null) {
            DbxRequestConfig requestConfig = new DbxRequestConfig("ascent");
            return new DbxClientV2(requestConfig, accessToken);
        }
        Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
        return null;
    }

    protected void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

}

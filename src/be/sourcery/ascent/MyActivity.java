package be.sourcery.ascent;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import com.dropbox.core.android.Auth;


public abstract class MyActivity extends Activity {

	final static protected String APP_SECRET = "cluryh2l6iqigim";
    // You don't need to change these, leave them alone.
	protected static final String ACCOUNT_PREFS_NAME = "prefs";
	protected static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	protected static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	protected boolean loggedIn = false;
	

    @TargetApi(11)
    protected void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
            String secret = prefs.getString(ACCESS_SECRET_NAME, null);
            if (key == null || secret == null || key.length() == 0 || secret.length() == 0) {
            	return;
            }
            
            if (key.equals("oauth2:")) {
            	// If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            	this.loggedIn = true;
            }
        }
    }


    protected void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

}

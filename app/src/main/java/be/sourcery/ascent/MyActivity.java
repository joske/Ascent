package be.sourcery.ascent;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class MyActivity extends AppCompatActivity {

	protected static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    public static final String USER_ID = "userId";
    public static final String SESSION_ID = "sessionId";
    protected static final String APP_NAME = "be.sourcery.ascent.Ascent";
	protected boolean loggedIn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

}

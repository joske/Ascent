package be.sourcery;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;


public abstract class MyActivity extends Activity {

    protected void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}

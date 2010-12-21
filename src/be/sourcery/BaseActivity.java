package be.sourcery;

import android.app.Activity;
import android.content.Intent;


public abstract class BaseActivity extends Activity {

    protected void switchToMain() {
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivityForResult(myIntent, 0);
    }

}

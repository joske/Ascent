package be.sourcery;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import be.sourcery.db.InternalDB;


public class AddCragActivity extends BaseActivity {

    InternalDB db = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_crag);
        setTitle("Add Crag");
        db = new InternalDB(this);
        // Capture our button from layout
        Button button = (Button)findViewById(R.id.ok);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText cragView = (EditText)findViewById(R.id.crag);
                String name = cragView.getText().toString();
                EditText countryView = (EditText)findViewById(R.id.country);
                String country = countryView.getText().toString();
                db.addCrag(name, country);
                switchToMain();
            }
        });
    }

}

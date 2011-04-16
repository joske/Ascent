package be.sourcery;

/*
 * This file is part of Ascent.
 *
 *  Ascent is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Ascent is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Ascent.  If not, see <http://www.gnu.org/licenses/>.
 */

import greendroid.app.GDActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import be.sourcery.db.InternalDB;


public class AddCragActivity extends GDActivity {

    InternalDB db = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.add_crag);
        setTitle(R.string.addCrag);
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
                setResult(RESULT_OK);
                finish();
            }
        });
    }

}

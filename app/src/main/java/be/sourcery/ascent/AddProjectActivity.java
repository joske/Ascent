package be.sourcery.ascent;

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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class AddProjectActivity extends MyActivity {

    InternalDB db = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_project);
        setTitle(R.string.addProject);
        setupActionBar();
        Bundle b = this.getIntent().getExtras();
        long cragId = -1;
        if (b != null) {
            cragId = b.getLong("cragId");
        }
        db = new InternalDB(this);
        Spinner s = (Spinner) findViewById(R.id.cragspinner);
        Cursor cursor = db.getCragsCursor();
        startManagingCursor(cursor);
        CursorAdapter ca = new SimpleCursorAdapter(this, R.layout.cragspinner, cursor, new String[] { "name" }, new int[] { R.id.spinnerRow});
        s.setAdapter(ca);
        s.setEnabled(true);
        int selection = getSelection(cragId, cursor);
        if (cragId != -1) {
            s.setSelection(selection);
        }
        Spinner gs = (Spinner) findViewById(R.id.gradespinner);
        ArrayAdapter gadapter = ArrayAdapter.createFromResource(
                this, R.array.grades, android.R.layout.simple_spinner_item);
        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gs.setAdapter(gadapter);
        Button button = (Button)findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String text = null;
                EditText routeNameView = (EditText)findViewById(R.id.routename);
                String name = routeNameView.getText().toString();
                Spinner gradeView = (Spinner)findViewById(R.id.gradespinner);
                String grade = gradeView.getSelectedItem().toString();
                Spinner s = (Spinner) findViewById(R.id.cragspinner);
                long selectedItemId = s.getSelectedItemId();
                Crag crag = db.getCrag(selectedItemId);
                Route r = db.addRoute(name, grade, crag);
                Project project = db.addProject(r, 0);
                if (project != null) {
                    text = "Project added";
                }
                if (text != null) {
                    Toast.makeText(AddProjectActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    private int getSelection(long cragId, Cursor cursor) {
        for (cursor.moveToFirst(); cursor.moveToNext(); ) {
            if (cursor.getLong(0) == cragId) {
                int position = cursor.getPosition();
                cursor.moveToFirst();
                return position;
            }
        }
        return 0;
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;


public class AddRouteActivity extends BaseActivity {

    InternalDB db = null;
    private int year;
    private int month;
    private int day;
    DateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
    private DatePickerDialog.OnDateSetListener dateSetListener =
        new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            AddRouteActivity.this.year = year;
            month = monthOfYear;
            day = dayOfMonth;
            updateDisplay();
        }
    };

    static final int DATE_DIALOG_ID = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_ascent);
        setTitle("Add Route");
        db = new InternalDB(this);
        Spinner s = (Spinner) findViewById(R.id.cragspinner);
        Cursor cursor = db.getCragsCursor();
        startManagingCursor(cursor);
        CursorAdapter ca = new SimpleCursorAdapter(this, R.layout.cragspinner, cursor, new String[] { "name" }, new int[] { R.id.spinnerRow});
        s.setAdapter(ca);
        Spinner ss = (Spinner) findViewById(R.id.stylespinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.styles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ss.setAdapter(adapter);
        Button button = (Button)findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String text = null;
                EditText cragView = (EditText)findViewById(R.id.routename);
                String name = cragView.getText().toString();
                EditText gradeView = (EditText)findViewById(R.id.grade);
                String country = gradeView.getText().toString();
                Spinner s = (Spinner) findViewById(R.id.cragspinner);
                long selectedItemId = s.getSelectedItemId();
                Crag crag = db.getCrag(selectedItemId);
                Route r = db.addRoute(name, country, crag);
                if (r != null) {
                    text = "Route added";
                }
                CheckBox addProject = (CheckBox) findViewById(R.id.addproject);
                if (addProject.isChecked()) {
                    Project project = db.addProject(r, 0);
                    if (project != null) {
                        text = "Project added";
                    }
                }
                CheckBox addAscent = (CheckBox) findViewById(R.id.addascent);
                if (addAscent.isChecked()) {
                    Spinner ss = (Spinner) findViewById(R.id.stylespinner);
                    int pos = ss.getSelectedItemPosition();
                    EditText attemptsView = (EditText)findViewById(R.id.attempts);
                    String string = attemptsView.getText().toString();
                    int attempts = 1;
                    if (string != null) {
                        try {
                            attempts = Integer.parseInt(string);
                        } catch (NumberFormatException e) {
                        }
                    }
                    TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
                    String dateString = dateDisplay.getText().toString();
                    Date date = new Date();
                    try {
                        date = fmt.parse(dateString);
                    } catch (ParseException e) {
                    }
                    RatingBar starsView = (RatingBar)findViewById(R.id.stars);
                    int stars = (int)starsView.getRating();
                    EditText commentView = (EditText)findViewById(R.id.comment);
                    String comment = commentView.getText().toString();
                    Ascent ascent = db.addAscent(r, date, attempts, pos + 1, comment, stars);
                    if (ascent != null) {
                        text = "Ascent added";
                    }
                }
                if (text != null) {
                    Toast.makeText(AddRouteActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                switchToMain();
            }
        });
        Button dateButton = (Button)findViewById(R.id.pickDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        dateSetListener,
                        year, month, day);
        }
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void updateDisplay() {
        TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
        dateDisplay.setText(
                new StringBuilder()
                // Month is 0 based so add 1
                .append(day).append("-")
                .append(month + 1).append("-")
                .append(year).append(" "));
    }

}

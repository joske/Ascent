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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;


public class AddAscentActivity extends GDActivity {

    InternalDB db = null;
    private int year;
    private int month;
    private int day;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    private DatePickerDialog.OnDateSetListener dateSetListener =
        new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            AddAscentActivity.this.year = year;
            month = monthOfYear;
            day = dayOfMonth;
            updateDisplay();
        }
    };

    static final int DATE_DIALOG_ID = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.add_ascent);
        setTitle(R.string.addAscent);
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
        Spinner ss = (Spinner) findViewById(R.id.stylespinner);
        ss.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long rowId) {
                EditText attemptsView = (EditText)findViewById(R.id.attempts);
                if (rowId == 2) {
                    // redpoint
                    attemptsView.setText("");
                    attemptsView.setEnabled(true);
                } else {
                    attemptsView.setText("1");
                    attemptsView.setEnabled(false);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.styles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ss.setAdapter(adapter);
        RadioGroup group = (RadioGroup) findViewById(R.id.group);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton addProject = (RadioButton) findViewById(R.id.addproject);
                EditText attemptsView = (EditText)findViewById(R.id.attempts);
                Spinner ss = (Spinner) findViewById(R.id.stylespinner);
                TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
                RatingBar starsView = (RatingBar)findViewById(R.id.stars);
                EditText commentView = (EditText)findViewById(R.id.comment);
                if (addProject.isChecked()) {
                    ss.setEnabled(false);
                    attemptsView.setEnabled(false);
                    dateDisplay.setEnabled(false);
                    starsView.setEnabled(false);
                    commentView.setEnabled(false);
                    ss.setFocusable(false);
                    attemptsView.setFocusable(false);
                    dateDisplay.setFocusable(false);
                    starsView.setFocusable(false);
                    commentView.setFocusable(false);
                } else {
                    ss.setEnabled(true);
                    attemptsView.setEnabled(true);
                    dateDisplay.setEnabled(true);
                    starsView.setEnabled(true);
                    commentView.setEnabled(true);
                    ss.setFocusable(true);
                    attemptsView.setFocusable(true);
                    dateDisplay.setFocusable(true);
                    starsView.setFocusable(true);
                    commentView.setFocusable(true);
                }
            }
        });
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

                RadioButton addProject = (RadioButton) findViewById(R.id.addproject);
                if (addProject.isChecked()) {
                    Project project = db.addProject(r, 0);
                    if (project != null) {
                        text = "Project added";
                    }
                }
                RadioButton addAscent = (RadioButton) findViewById(R.id.addascent);
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
                    Toast.makeText(AddAscentActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK);
                finish();
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
                .append(year).append("-")
                .append(month + 1).append("-")
                .append(day));
    }

}

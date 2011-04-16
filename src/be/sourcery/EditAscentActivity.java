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
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;


public class EditAscentActivity extends GDActivity {

    private InternalDB db;
    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    static final int DATE_DIALOG_ID = 0;
    private GregorianCalendar cal = new GregorianCalendar();
    private DatePickerDialog.OnDateSetListener dateSetListener =
        new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            cal.set(year, monthOfYear, dayOfMonth);
            updateDisplay();
        }
    };
    private Ascent ascent;
    private Spinner ss;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.edit_ascent);
        setTitle(R.string.editAscent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle b = this.getIntent().getExtras();
        long ascentId = b.getLong("ascentId");
        db = new InternalDB(this);
        ascent = db.getAscent(ascentId);
        TextView routeView = (TextView) this.findViewById(R.id.route);
        routeView.setText(ascent.getRoute().getName() + " " + ascent.getRoute().getGrade() + " (" + ascent.getRoute().getCrag().getName() + ")");

        TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
        Date date = ascent.getDate();
        String dateString = fmt.format(date);
        dateDisplay.setText(dateString);
        cal.setTime(date);
        ss = (Spinner) findViewById(R.id.stylespinner2);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.styles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ss.setAdapter(adapter);
        ss.setSelection(ascent.getStyle() - 1);

        Button dateButton = (Button)findViewById(R.id.pickDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        EditText commentsView = (EditText)findViewById(R.id.comment);
        commentsView.setText(ascent.getComment());
        EditText attemptsView = (EditText)findViewById(R.id.attempts);
        attemptsView.setText("" + ascent.getAttempts());
        RatingBar starsView = (RatingBar)findViewById(R.id.stars);
        starsView.setRating(ascent.getStars());

        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        Button button = (Button)findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String text = null;
                int pos = ss.getSelectedItemPosition();
                ascent.setStyle(pos + 1);
                EditText attemptsView = (EditText)findViewById(R.id.attempts);
                String string = attemptsView.getText().toString();
                int attempts = 1;
                if (string != null) {
                    try {
                        attempts = Integer.parseInt(string);
                        ascent.setAttempts(attempts);
                    } catch (NumberFormatException e) {
                    }
                }
                TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
                String dateString = dateDisplay.getText().toString();
                Date date = new Date();
                try {
                    date = fmt.parse(dateString);
                    ascent.setDate(date);
                } catch (ParseException e) {
                }
                RatingBar starsView = (RatingBar)findViewById(R.id.stars);
                ascent.setStars((int)starsView.getRating());
                EditText commentView = (EditText)findViewById(R.id.comment);
                String comment = commentView.getText().toString();
                ascent.setComment(comment);
                db.updateAscent(ascent);
                if (ascent != null) {
                    text = "Ascent updated";
                }

                if (text != null) {
                    Toast.makeText(EditAscentActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        dateSetListener,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }

    private void updateDisplay() {
        TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
        String dateString = fmt.format(cal.getTime());
        dateDisplay.setText(dateString);
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

}

package be.sourcery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;


public class TickProjectActivity extends MyActivity {

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

    private Project project;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tick_project);
        setTitle(R.string.editAscent);
        setupActionBar();
        Bundle b = this.getIntent().getExtras();
        long projectId = b.getLong("projectId");
        db = new InternalDB(this);
        project = db.getProject(projectId);
        TextView routeView = (TextView) this.findViewById(R.id.route);
        routeView.setText(project.getRoute().getName() + " " + project.getRoute().getGrade() + " (" + project.getRoute().getCrag().getName() + ")");

        TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
        Date date = new Date();
        String dateString = fmt.format(date);
        dateDisplay.setText(dateString);
        cal.setTime(date);
        Button dateButton = (Button)findViewById(R.id.pickDate);
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        EditText commentsView = (EditText)findViewById(R.id.comment);
        EditText attemptsView = (EditText)findViewById(R.id.attempts);
        attemptsView.setText("" + project.getAttempts());
        RatingBar starsView = (RatingBar)findViewById(R.id.stars);

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
                EditText commentView = (EditText)findViewById(R.id.comment);
                String comment = commentView.getText().toString();
                db.addAscent(project, date, attempts, 3, comment, (int)starsView.getRating());
                text = "Ascent added";

                if (text != null) {
                    Toast.makeText(TickProjectActivity.this, text, Toast.LENGTH_SHORT).show();
                }
                setResult(RESULT_OK);
                finish();
            }
        });
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

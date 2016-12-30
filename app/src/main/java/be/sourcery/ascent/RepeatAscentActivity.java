package be.sourcery.ascent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class RepeatAscentActivity extends MyActivity {

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
    private Ascent origAscent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeat_ascent);
        setTitle(R.string.repeatAscent);
        setupActionBar();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle b = this.getIntent().getExtras();
        long ascentId = b.getLong("ascentId");
        db = new InternalDB(this);
        origAscent = db.getAscent(ascentId);
        TextView routeView = (TextView) this.findViewById(R.id.route);
        routeView.setText(origAscent.getRoute().getName() + " " + origAscent.getRoute().getGrade() + " (" + origAscent.getRoute().getCrag().getName() + ")");

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
                TextView dateDisplay = (TextView)findViewById(R.id.dateDisplay);
                String dateString = dateDisplay.getText().toString();
                Date date = new Date();
                try {
                    date = fmt.parse(dateString);
                } catch (ParseException e) {
                }
                EditText commentView = (EditText)findViewById(R.id.comment);
                String comment = commentView.getText().toString();
                db.addAscent(origAscent.getRoute(), date, 1, Ascent.STYLE_REPEAT, comment, origAscent.getStars());

                setResult(RESULT_OK);
                Toast.makeText(RepeatAscentActivity.this, "Ascent added", Toast.LENGTH_SHORT).show();
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

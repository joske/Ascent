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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditAscentActivity extends MyActivity {

	private InternalDB db;
	DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	static final int DATE_DIALOG_ID = 0;
	private GregorianCalendar cal = new GregorianCalendar();
	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			cal.set(year, monthOfYear, dayOfMonth);
			updateDisplay();
		}
	};
	private Ascent ascent;
	private Spinner ss;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_ascent);
		setTitle(R.string.editAscent);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Bundle b = this.getIntent().getExtras();
		long ascentId = b.getLong("ascentId");
		db = new InternalDB(this);
		ascent = db.getAscent(ascentId);
		EditText routeView = (EditText) this.findViewById(R.id.routename);
		routeView.setText(ascent.getRoute().getName());
		Spinner gradeView = (Spinner) findViewById(R.id.gradespinner);
		ArrayAdapter gadapter = ArrayAdapter.createFromResource(this,
				R.array.grades, android.R.layout.simple_spinner_item);
		gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gradeView.setAdapter(gadapter);
		String grade = ascent.getRoute().getGrade();
		int gradeIndex = gadapter.getPosition(grade);
		gradeView.setSelection(gradeIndex);

		Spinner s = (Spinner) findViewById(R.id.cragspinner);
		Cursor cursor = db.getCragsCursor();
		startManagingCursor(cursor);
		CursorAdapter ca = new SimpleCursorAdapter(this, R.layout.cragspinner,
				cursor, new String[] { "name" }, new int[] { R.id.spinnerRow });
		s.setAdapter(ca);
		s.setEnabled(true);
		int selection = getSelection(ascent.getRoute().getCrag().getId(),
				cursor);
		if (selection != -1) {
			s.setSelection(selection);
		}

		TextView dateDisplay = (TextView) findViewById(R.id.dateDisplay);
		Date date = ascent.getDate();
		String dateString = fmt.format(date);
		dateDisplay.setText(dateString);
		cal.setTime(date);
		ss = (Spinner) findViewById(R.id.stylespinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.styles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ss.setAdapter(adapter);
		ss.setSelection(ascent.getStyle() - 1);
		ss.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long rowId) {
				EditText attemptsView = (EditText) findViewById(R.id.attempts);
				if (rowId == 2 || rowId == 6) {
					// redpoint or tried
					attemptsView.setEnabled(true);
					attemptsView.selectAll();
					attemptsView.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
							InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					attemptsView.setText("1");
					attemptsView.setEnabled(false);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		TextView dateButton = (TextView) findViewById(R.id.dateDisplay);
		dateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		EditText commentsView = (EditText) findViewById(R.id.comment);
		commentsView.setText(ascent.getComment());
		EditText attemptsView = (EditText) findViewById(R.id.attempts);
		attemptsView.setText("" + ascent.getAttempts());
		RatingBar starsView = (RatingBar) findViewById(R.id.stars);
		starsView.setRating(ascent.getStars());

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String text = null;
				EditText routeNameView = (EditText) findViewById(R.id.routename);
				String name = routeNameView.getText().toString();
				Spinner gradeView = (Spinner) findViewById(R.id.gradespinner);
				String grade = gradeView.getSelectedItem().toString();
				Spinner s = (Spinner) findViewById(R.id.cragspinner);
				long selectedItemId = s.getSelectedItemId();
				Crag crag = db.getCrag(selectedItemId);
				Route r = ascent.getRoute();
				if (!(r.getName().equals(name))
						|| !(r.getGrade().equals(grade))
						|| !(r.getCrag().equals(crag))) {
					r.setName(name);
					r.setGrade(grade);
					r.setCrag(crag);
					db.updateRoute(r);
				}
				int pos = ss.getSelectedItemPosition();
				ascent.setStyle(pos + 1);
				EditText attemptsView = (EditText) findViewById(R.id.attempts);
				String string = attemptsView.getText().toString();
				int attempts = 1;
				if (string != null) {
					try {
						attempts = Integer.parseInt(string);
						ascent.setAttempts(attempts);
					} catch (NumberFormatException e) {
					}
				}
				TextView dateDisplay = (TextView) findViewById(R.id.dateDisplay);
				String dateString = dateDisplay.getText().toString();
				Date date = new Date();
				try {
					date = fmt.parse(dateString);
					ascent.setDate(date);
				} catch (ParseException e) {
				}
				RatingBar starsView = (RatingBar) findViewById(R.id.stars);
				ascent.setStars((int) starsView.getRating());
				EditText commentView = (EditText) findViewById(R.id.comment);
				String comment = commentView.getText().toString();
				ascent.setComment(comment);
				db.updateAscent(ascent);
				if (ascent != null) {
					text = "Ascent updated";
				}

				if (text != null) {
					Toast.makeText(EditAscentActivity.this, text,
							Toast.LENGTH_SHORT).show();
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
			return new DatePickerDialog(this, dateSetListener,
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	private int getSelection(long cragId, Cursor cursor) {
		for (cursor.moveToFirst(); cursor.moveToNext();) {
			if (cursor.getLong(0) == cragId) {
				int position = cursor.getPosition();
				cursor.moveToFirst();
				return position;
			}
		}
		return 0;
	}

	private void updateDisplay() {
		TextView dateDisplay = (TextView) findViewById(R.id.dateDisplay);
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

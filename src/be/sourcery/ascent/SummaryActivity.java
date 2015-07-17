package be.sourcery.ascent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;



public class SummaryActivity extends MyActivity {

    private InternalDB db;
    private int year;
    private int thisYear;
    private long crag = -1;
    private List<String> grades;
    private List<String> crags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary);
        setTitle(R.string.summary);
        setupActionBar();
        thisYear = 1900 + new Date().getYear();
        year = thisYear;
        db = new InternalDB(this);
        crags = new ArrayList<String>();
        List<Crag> allCrags = db.getCrags();
        crags.add("*");
        for (Iterator iterator = allCrags.iterator(); iterator.hasNext();) {
            Crag crag = (Crag)iterator.next();
            crags.add(crag.getName());
        }
        Spinner cragSpinner = (Spinner) findViewById(R.id.crag_spinner);
        cragSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long rowId) {
                if (rowId == 0) {
                    crag = -1;
                } else {
                    String cragName = crags.get((int)rowId);
                    crag = db.getCrag(cragName).getId();
                }
                updateView();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        ArrayAdapter ca = new ArrayAdapter(this, android.R.layout.simple_spinner_item, crags);
        cragSpinner.setAdapter(ca);
        cragSpinner.setEnabled(true);
        Spinner yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        int firstYear = db.getFirstYear();
        List<String> years = new ArrayList<String>();
        for (int i = year; i >= firstYear; i--) {
            years.add("" + i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, years);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long rowId) {
                year = (int)(thisYear - rowId);
                updateView();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        grades = db.getGrades();
        updateView();
    }

    private void updateView() {
        Map<String, Integer> summaryDoneForYear = db.getSummaryDoneForYear(year, crag);
        Map<String, Integer> summaryTriedForYear = db.getSummaryTriedForYear(year, crag);
        ListView listView = (ListView)this.findViewById(R.id.list);
        String[] from = new String[] {"grade", "done", "tried"};
        int[] to = new int[] { R.id.gradeColumn, R.id.doneColumn, R.id.triedColumn};
        final List<Map<String, String>> data = new ArrayList();
        for (Iterator iterator = grades.iterator(); iterator.hasNext();) {
            String grade = (String)iterator.next();
            if (summaryDoneForYear.containsKey(grade) || summaryTriedForYear.containsKey(grade)) {
                Map<String, String> line = new HashMap();
                line.put("grade", grade);
                if (summaryDoneForYear.containsKey(grade)) {
                    line.put("done", "" + summaryDoneForYear.get(grade));
                } else {
                    line.put("done", "0");
                }
                if (summaryTriedForYear.containsKey(grade)) {
                    line.put("tried", "" + summaryTriedForYear.get(grade));
                } else {
                    line.put("tried", "0");
                }
                data.add(line);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.summary_item, from, to);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                Map<String, String> map = data.get((int)row);
                String grade = map.get("grade");
                showDetails(grade);
            }
        });
    }

    protected void showDetails(String grade) {
        List<Ascent> ascents = db.getAscents(grade, year, crag);
        StringBuilder buf = new StringBuilder();
        for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
            Ascent ascent = (Ascent)iterator.next();
            buf.append(ascent.toString()).append("\n");
        }
        Toast.makeText(this, buf.toString(), Toast.LENGTH_LONG).show();;
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

package be.sourcery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import be.sourcery.db.InternalDB;


public class GradeGraphActivity extends MyActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grades);
        setTitle(R.string.grades);
        setupActionBar();
        getDataset();
//        DefaultRenderer renderer = getRenderer();
//        GraphicalView chartView = ChartFactory.getPieChartView(this, series, renderer);
    }

    private void getDataset() {
        InternalDB db = new InternalDB(this);
        addGrades(db,(ListView) findViewById( R.id.last12), false);
        addGrades(db,(ListView) findViewById(R.id.allTime), true);
        db.close();
    }

    private void addGrades(InternalDB db, ListView view, final boolean allTime) {
        List<Ascent> ascents = allTime ? db.getSortedAscents() : db.getSortedAscentsForLast12Months();
        String currentGrade = null;
        int count = 0;
        ArrayList<String> lines = new ArrayList<String>();
        for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
            Ascent ascent = (Ascent)iterator.next();
            String grade = ascent.getRoute().getGrade();
            if (currentGrade == null) {
                currentGrade = grade;
                count++;
            } else {
                if (!grade.equals(currentGrade)) {
                    lines.add(currentGrade + " - " + count);
                    currentGrade = grade;
                    count = 1;
                } else {
                    count++;
                }
            }
        }
        lines.add(currentGrade + " - " + count);
        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.grade_item, lines);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long rowid) {
                String item = (String)adapter.getItem(position);
                String grade = item.replaceAll("(.*) - .*", "$1");
                showAscents(grade, allTime);
            }

        });
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

    private List<Ascent> getAscents(String grade, List<Ascent> ascents) {
        List<Ascent> gradeAscents = new ArrayList<Ascent>();
        for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
            Ascent ascent = (Ascent)iterator.next();
            if (ascent.getRoute().getGrade().equals(grade)) {
                gradeAscents.add(ascent);
            }
        }
        return gradeAscents;
    }

    private void showAscents(String grade, boolean allTime) {
        Intent myIntent = new Intent(this, GradeAscentsActivity.class);
        Bundle b = new Bundle();
        b.putString("grade", grade);
        b.putBoolean("allTime", allTime);
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

}

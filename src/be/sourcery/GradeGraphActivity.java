package be.sourcery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import be.sourcery.db.InternalDB;


public class GradeGraphActivity extends MyActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grades);
        setTitle(R.string.grades);
        setupActionBar();
        getDataset();
    }

    private void getDataset() {
        InternalDB db = new InternalDB(this);
        addGrades(db,(GradeView) findViewById( R.id.gradeView), false);
        db.close();
    }

    private void addGrades(InternalDB db, GradeView view, final boolean allTime) {
        List<Ascent> ascents = allTime ? db.getSortedAscents() : db.getSortedAscentsForLast12Months();
        String currentGrade = null;
        int count = 0;
        List<GradeInfo> lines = new ArrayList<GradeInfo>();
        for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
            Ascent ascent = (Ascent)iterator.next();
            String grade = ascent.getRoute().getGrade();
            if (currentGrade == null) {
                currentGrade = grade;
                count++;
            } else {
                if (!grade.equals(currentGrade)) {
                    GradeInfo grades = new GradeInfo(currentGrade, count, 0, 0, 0);
                    lines.add(grades);
                    currentGrade = grade;
                    count = 1;
                } else {
                    count++;
                }
            }
        }
        GradeInfo grades = new GradeInfo(currentGrade, count, 0, 0, 0);
        lines.add(grades);
        view.setGradeInfo(lines);
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

    private void showAscents(String grade, boolean allTime) {
        Intent myIntent = new Intent(this, GradeAscentsActivity.class);
        Bundle b = new Bundle();
        b.putString("grade", grade);
        b.putBoolean("allTime", allTime);
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

}

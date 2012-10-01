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
        int osCount = 0;
        int flCount = 0;
        int rpCount = 0;
        int tpCount = 0;
        List<GradeInfo> lines = new ArrayList<GradeInfo>();
        for (Iterator iterator = ascents.iterator(); iterator.hasNext();) {
            Ascent ascent = (Ascent)iterator.next();
            String grade = ascent.getRoute().getGrade();
            if (currentGrade == null || grade.equals(currentGrade)) {
                switch (ascent.getStyle()) {
                    case Ascent.STYLE_ONSIGHT:
                        osCount++;
                        break;
                    case Ascent.STYLE_FLASH:
                        flCount++;
                        break;
                    case Ascent.STYLE_REDPOINT:
                        rpCount++;
                        break;
                    case Ascent.STYLE_TOPROPE:
                        tpCount++;
                        break;
                    default:
                        break;
                }
            }
            if (currentGrade == null) {
                currentGrade = grade;
            } else {
                if (!grade.equals(currentGrade)) {
                    GradeInfo grades = new GradeInfo(currentGrade, osCount, flCount, rpCount, tpCount);
                    lines.add(grades);
                    currentGrade = grade;
                    osCount = ascent.getStyle() == Ascent.STYLE_ONSIGHT ? 1 : 0;
                    flCount = ascent.getStyle() == Ascent.STYLE_FLASH ? 1 : 0;
                    rpCount = ascent.getStyle() == Ascent.STYLE_REDPOINT ? 1 : 0;
                    tpCount = ascent.getStyle() == Ascent.STYLE_TOPROPE ? 1 : 0;
                }
            }
        }
        GradeInfo grades = new GradeInfo(currentGrade, osCount, flCount, rpCount, tpCount);
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

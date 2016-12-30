package be.sourcery.ascent;

import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;



public class Top10Activity extends MyActivity {

    private InternalDB db;
    private Cursor cursor12Months;
    private Cursor cursorYear;
    private Cursor cursorAllTime;
    private CursorAdapter adapterYear;
    private CursorAdapter adapter12Months;
    private CursorAdapter adapterAllTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top10);
        setTitle(R.string.top10);
        setupActionBar();
        db = new InternalDB(this);
        Date now = new Date();
        int year = now.getYear() + 1900;
        TextView thisYearScore = (TextView)this.findViewById(R.id.thisYearText);
        thisYearScore.setText("Score so far this year - "+ db.getScoreForYear(year));
        cursorYear = db.getTop10ForYear(year);
        startManagingCursor(cursorYear);
        adapterYear = new SimpleCursorAdapter(getApplicationContext(), R.layout.top10_ascent_list_item, cursorYear,
                new String[] {"date", "style", "route_grade", "route_name", "score"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.nameCell, R.id.scoreCell});
        ListView listView = (ListView)this.findViewById(R.id.thisYearList);
        listView.setAdapter(adapterYear);

        TextView twelveMonthScore = (TextView)this.findViewById(R.id.twelveMonthText);
        twelveMonthScore.setText("Score last 12 months - " + db.getScoreLast12Months());
        cursor12Months = db.getTop10TwelveMonths();
        startManagingCursor(cursor12Months);
        adapter12Months = new SimpleCursorAdapter(getApplicationContext(), R.layout.top10_ascent_list_item, cursor12Months,
                new String[] {"date", "style", "route_grade", "route_name", "score"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.nameCell, R.id.scoreCell});
        ListView listView2 = (ListView)this.findViewById(R.id.twelveMonthList);
        listView2.setAdapter(adapter12Months);

        TextView allTimeScore = (TextView)this.findViewById(R.id.allTimeText);
        allTimeScore.setText("Score all time - " + db.getScoreAllTime());
        cursorAllTime = db.getTop10AllTime();
        startManagingCursor(cursorAllTime);
        adapterAllTime = new SimpleCursorAdapter(getApplicationContext(), R.layout.top10_ascent_list_item, cursorAllTime,
                new String[] {"date", "style", "route_grade", "route_name", "score"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.nameCell, R.id.scoreCell});
        ListView listView3 = (ListView)this.findViewById(R.id.allTimeList);
        listView3.setAdapter(adapterAllTime);
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

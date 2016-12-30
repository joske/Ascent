package be.sourcery.ascent;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class GradeAscentsActivity extends MyActivity {

    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    private Cursor cursor;
    private Crag crag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupActionBar();
        Bundle b = this.getIntent().getExtras();
        String grade = b.getString("grade");
        boolean allTime = b.getBoolean("allTime");
        db = new InternalDB(this);
        cursor = db.getAscentsCursor(grade, allTime);
        setTitle((allTime ? "All" : "Latest") + " Ascents for " + grade);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " ascents in DB");
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.ascent_list_item, cursor,
                new String[] {"date", "style", "route_grade", "date", "route_name"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.dateCell, R.id.nameCell});
        listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
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

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

}

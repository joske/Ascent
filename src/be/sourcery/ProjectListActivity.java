package be.sourcery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class ProjectListActivity extends MyActivity {

    private CursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);
        setTitle(R.string.projects);
        setupActionBar();
        InternalDB db = new InternalDB(this);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        Cursor cursor = db.getProjectsCursor();
        startManagingCursor(cursor);
        countView.setText(cursor.getCount() + " projects in DB");
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.project_list_item, cursor,
                new String[] {"route_name", "route_grade", "crag_name", "attempts"},
                new int[] {R.id.nameCell, R.id.gradeCell, R.id.cragCell, R.id.attemptsCell});

        ListView listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
            }
        });
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

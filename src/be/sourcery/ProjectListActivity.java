package be.sourcery;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class ProjectListActivity extends Activity {

    private CursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);
        setTitle("Projects");
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

}

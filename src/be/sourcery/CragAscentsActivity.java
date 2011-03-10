package be.sourcery;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class CragAscentsActivity extends Activity {

    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    private Cursor cursor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = this.getIntent().getExtras();
        long cragId = b.getLong("cragId");
        db = new InternalDB(this);
        Crag crag = db.getCrag(cragId);
        setContentView(R.layout.main);
        setTitle("Latest Ascents for " + crag.getName());
        ImageView plus = (ImageView)this.findViewById(R.id.plusButton);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        cursor = db.getAscentsCursor(crag);
        countView.setText(cursor.getCount() + " ascents in DB");
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.ascent_list_item, cursor,
                new String[] {"date", "style", "route_grade", "date", "route_name"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.dateCell, R.id.nameCell});
        listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }


}

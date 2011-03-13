package be.sourcery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                long id = adapter.getItemId(position);
                Ascent ascent = db.getAscent(id);
                editAscent(ascent);
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void editAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, EditAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }



}

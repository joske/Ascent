package be.sourcery;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
    private Crag crag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Bundle b = this.getIntent().getExtras();
        long cragId = b.getLong("cragId");
        db = new InternalDB(this);
        crag = db.getCrag(cragId);
        setTitle("Latest Ascents for " + crag.getName());
        ImageView plus = (ImageView)this.findViewById(R.id.plusButton);
        cursor = db.getAscentsCursor(crag);
        TextView countView = (TextView) this.findViewById(R.id.countView);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crags_actionbar, menu);
        return true;
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
            case R.id.menu_add:
                addAscent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Ascent ascent = db.getAscent(info.id);
        switch (item.getItemId()) {
            case R.id.delete:
                db.deleteAscent(ascent);
                cursor.requery();
                adapter.notifyDataSetChanged();
                return true;
            case R.id.repeat:
                repeatAscent(ascent);
                cursor.requery();
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void onResume() {
        super.onResume();
        cursor.requery();
        TextView countView = (TextView) this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " ascents in DB");
    }

    private void editAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, EditAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    private void repeatAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, RepeatAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    private void addAscent() {
        Intent myIntent = new Intent(this, AddAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("cragId", crag.getId());
        myIntent.putExtras(b);
        startActivityForResult(myIntent, 0);
    }

}

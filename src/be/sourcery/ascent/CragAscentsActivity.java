package be.sourcery.ascent;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class CragAscentsActivity extends MyActivity {

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
        long cragId = b.getLong("cragId");
        db = new InternalDB(this);
        crag = db.getCrag(cragId);
        setTitle("Latest Ascents for " + crag.getName());
        cursor = db.getAscentsCursor(crag);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        int count12M = db.getCountLast12Months(cragId);
        int count = db.getCountAllTime(cragId);
        countView.setText("Ascents: " + count + " - 12M: " + count12M);
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.ascent_list_item, cursor,
                new String[] {"date", "style", "route_grade", "route_name", "comment"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.nameCell, R.id.commentCell});
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
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ComponentName cn = new ComponentName(this, SearchAscentsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appDataBundle = new Bundle();
        appDataBundle.putLong("crag_id", crag.getId());
        startSearch(null, false, appDataBundle, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Search")) {
            return onSearchRequested();
        }
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
        int count12M = db.getCountLast12Months(crag.getId());
        countView.setText("Ascents: " + cursor.getCount() + " - 12M: " + count12M);
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

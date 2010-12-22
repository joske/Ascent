package be.sourcery;

/*
 * This file is part of Ascent.
 *
 *  Ascent is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Ascent is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Ascent.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;

public class MainActivity extends Activity {

    private static final int MENU_ADD_CRAG = 0;
    private static final int MENU_PROJECTS = 1;
    private static final int MENU_CRAGS = 2;
    private DateFormat fmt = new SimpleDateFormat("dd MMM yyyy");
    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    private Cursor cursor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.top);
        TextView title = (TextView) this.findViewById(R.id.titleText);
        title.setText("Latest Ascent");
        ImageView plus = (ImageView)this.findViewById(R.id.plusButton);
        plus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addRoute();
            }
        });
        db = new InternalDB(this);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        List<Ascent> ascents = db.getAscents();
        countView.setText(ascents.size() + " ascents in DB");
        cursor = db.getAscentsCursor();
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

    public void editAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, EditAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_CRAG, 0, "Add Crag");
        menu.add(0, MENU_PROJECTS, 0, "Projects");
        menu.add(0, MENU_CRAGS, 0, "Crags");
        return true;
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
        switch (item.getItemId()) {
            case R.id.delete:
                Ascent ascent = db.getAscent(info.id);
                db.deleteAscent(ascent);
                cursor.requery();
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CRAGS:
                cragsList();
                return true;
            case MENU_ADD_CRAG:
                addCrag();
                return true;
            case MENU_PROJECTS:
                projectsList();
                return true;
        }
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void addAscent() {
        Intent myIntent = new Intent(this, AddAscentActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void addCrag() {
        Intent myIntent = new Intent(this, AddCragActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void addRoute() {
        Intent myIntent = new Intent(this, AddRouteActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void cragsList() {
        Intent myIntent = new Intent(this, CragListActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void projectsList() {
        Intent myIntent = new Intent(this, ProjectListActivity.class);
        startActivityForResult(myIntent, 0);
    }

}
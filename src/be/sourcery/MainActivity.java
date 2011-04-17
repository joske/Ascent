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

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.QuickActionBar;

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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import be.sourcery.db.InternalDB;

public class MainActivity extends GDActivity {

    private static final int MENU_PROJECTS = 0;
    private static final int MENU_CRAGS = 1;
    private static final int MENU_IMPORT = 2;
    private static final int MENU_EXPORT = 3;

    private static final int EXPORT_DATA_REQUEST = 1;
    private static final int IMPORT_DATA_REQUEST = 2;

    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    private Cursor cursor;
    private QuickActionBar mBar;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.main);
        setTitle(R.string.latestAscents);
        addActionBarItem(Type.Add);
        db = new InternalDB(this);
        prepareList();
    }

    private void prepareList() {
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

    private void editAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, EditAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_CRAGS, 0, "Crags");
        menu.add(0, MENU_PROJECTS, 0, "Projects");
        menu.add(0, MENU_IMPORT, 0, "Import Data");
        menu.add(0, MENU_EXPORT, 0, "Export Data");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EXPORT_DATA_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    int count = data.getIntExtra("count", 0);
                    Toast.makeText(this, "Exported " + count + " ascents", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed exporting ascents", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case (IMPORT_DATA_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    int count = data.getIntExtra("count", 0);
                    Toast.makeText(this, "Imported " + count + " ascents", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed importing ascents", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CRAGS:
                cragsList();
                return true;
            case MENU_IMPORT:
                importData();
                return true;
            case MENU_EXPORT:
                exportData();
                return true;
            case MENU_PROJECTS:
                projectsList();
                return true;
        }
        return false;
    }

    private void importData() {
        Intent myIntent = new Intent(this, ImportDataActivity.class);
        startActivityForResult(myIntent, IMPORT_DATA_REQUEST);
    }

    private void exportData() {
        Intent myIntent = new Intent(this, ExportDataActivity.class);
        startActivityForResult(myIntent, EXPORT_DATA_REQUEST);
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void addCrag() {
        Intent myIntent = new Intent(this, AddCragActivity.class);
        startActivityForResult(myIntent, 0);
    }

    private void addAscent() {
        Intent myIntent = new Intent(this, AddAscentActivity.class);
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

    public void onShowBar(View v) {
        mBar.show(v);
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

        switch (position) {
            case 0:
                addAscent();
                break;

            default:
                return super.onHandleActionBarItemClick(item, position);
        }

        return true;
    }

}
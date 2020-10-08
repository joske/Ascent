package be.sourcery.ascent;

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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class CragListActivity extends MyActivity {

    private CursorAdapter adapter;
    private InternalDB db;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crag_list_activity);
        setTitle(R.string.crags);
        FloatingActionButton FAB = findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCrag();
            }
        });

        db = new InternalDB(this);
        cursor = db.getCragsCrusor();
        startManagingCursor(cursor);
        TextView countView = this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " crags in DB");
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.crag_item, cursor,
                new String[] {"name", "country"},
                new int[] {R.id.nameCell, R.id.countryCell});
        ListView listView = this.findViewById(R.id.list);
        registerForContextMenu(listView);
        listView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        cursor.requery();
        TextView countView = this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " crags in DB");
    }

    private void addCrag() {
        Intent myIntent = new Intent(this, AddCragActivity.class);
        startActivityForResult(myIntent, 0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crags_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Crag crag = db.getCrag(info.id);
        switch (item.getItemId()) {
            case R.id.delete:
                db.deleteCrag(crag);
                update();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void update() {
        cursor.requery();
        adapter.notifyDataSetChanged();
    }

}
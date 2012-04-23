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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class CragListActivity extends MyActivity {

    private CursorAdapter adapter;
    private InternalDB db;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crag_list);
        setTitle(R.string.crags);
        setupActionBar();
        db = new InternalDB(this);
        cursor = db.getCragsCrusor();
        startManagingCursor(cursor);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " crags in DB");
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.crag_item, cursor,
                new String[] {"name", "country"},
                new int[] {R.id.nameCell, R.id.countryCell});
        ListView listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                long id = adapter.getItemId(position);
                Crag crag = db.getCrag(id);
                showAscents(crag);
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void onResume() {
        super.onResume();
        cursor.requery();
        TextView countView = (TextView) this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " crags in DB");
    }

    private void showAscents(Crag crag) {
        Intent myIntent = new Intent(this, CragAscentsActivity.class);
        Bundle b = new Bundle();
        b.putLong("cragId", crag.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    private void addCrag() {
        Intent myIntent = new Intent(this, AddCragActivity.class);
        startActivityForResult(myIntent, 0);
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
                addCrag();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
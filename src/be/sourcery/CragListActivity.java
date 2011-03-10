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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class CragListActivity extends Activity {

    private CursorAdapter adapter;
    private InternalDB db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.crag_list);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.top);
        TextView title = (TextView) this.findViewById(R.id.titleText);
        title.setText("Crags");
        ImageView plus = (ImageView)this.findViewById(R.id.plusButton);
        plus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                addCrag();
            }
        });
        setTitle("Crags");
        db = new InternalDB(this);
        Cursor cursor = db.getCragsCrusor();
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
}

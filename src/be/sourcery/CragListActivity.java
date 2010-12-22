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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.sourcery.db.InternalDB;


public class CragListActivity extends Activity {

    private CragsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crag_list);
        setTitle("Crags");
        InternalDB db = new InternalDB(this);
        List<Crag> crags = db.getCrags();
        adapter = new CragsAdapter(getApplicationContext(), R.layout.crag_item, (ArrayList)crags);
        ListView listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                Crag crag = adapter.getItem(position);
                editCrag(crag);
            }
        });
        db.close();
    }

    private void editCrag(Crag crag) {
    }

    private class CragsAdapter extends ArrayAdapter<Crag> {

        private final ArrayList<Crag> crags;

        public CragsAdapter(Context context, int textViewResourceId, ArrayList<Crag> crags) {
            super(context, textViewResourceId, crags);
            this.crags = crags;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.crag_item, null);
            }

            Crag crag = crags.get(position);
            TextView nameCell = (TextView) v.findViewById(R.id.nameCell);
            nameCell.setText(crag.getName());
            TextView countryCell = (TextView) v.findViewById(R.id.countryCell);
            countryCell.setText(crag.getCountry());
            return v;
        }

    }



}

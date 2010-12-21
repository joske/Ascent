package be.sourcery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import be.sourcery.db.InternalDB;

public class MainActivity extends Activity {

    private static final int MENU_ADD_CRAG = 0;
    private static final int MENU_ADD_ROUTE = 1;
    private static final int MENU_CRAGS = 2;
    private DateFormat fmt = new SimpleDateFormat("dd MMM yyyy");
    private AscentAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        InternalDB db = new InternalDB(this);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        List<Ascent> ascents = db.getAscents();
        countView.setText(ascents.size() + " ascents in DB");
        adapter = new AscentAdapter(getApplicationContext(), R.layout.list_item, (ArrayList)ascents);
        ListView listView = (ListView)this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                Ascent ascent = adapter.getItem(position);
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
        menu.add(0, MENU_ADD_ROUTE, 0, "Add Route");
        menu.add(0, MENU_CRAGS, 0, "Crags");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CRAGS:
                cragsList();
                return true;
            case MENU_ADD_CRAG:
                addCrag();
                return true;
            case MENU_ADD_ROUTE:
                addRoute();
                return true;
        }
        return false;
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

    private class AscentAdapter extends ArrayAdapter<Ascent> {

        private final ArrayList<Ascent> ascents;

        public AscentAdapter(Context context, int textViewResourceId, ArrayList<Ascent> ascents) {
            super(context, textViewResourceId, ascents);
            this.ascents = ascents;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }

            Ascent ascent = ascents.get(position);
            int style = ascent.getStyle();
            String s = "RP";
            if (style == Ascent.STYLE_FLASH) {
                s = "FL";
            } else if (style == Ascent.STYLE_ONSIGHT) {
                s = "OS";
            } else if (style == Ascent.STYLE_TOPROPE) {
                s = "TP";
            }
            TextView styleCell = (TextView) v.findViewById(R.id.styleCell);
            styleCell.setText(s);
            TextView gradeCell = (TextView) v.findViewById(R.id.gradeCell);
            gradeCell.setText(ascent.getRoute().getGrade());
            TextView nameCell = (TextView) v.findViewById(R.id.nameCell);
            nameCell.setText(ascent.getRoute().getName());
            TextView dateCell = (TextView) v.findViewById(R.id.dateCell);
            dateCell.setText(fmt.format(ascent.getDate()));
            return v;
        }

    }

}
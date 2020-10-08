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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import be.sourcery.ascent.eighta.EightA;

public class MainActivity extends MyActivity {

    private static final int EXPORT_DATA_REQUEST = 1;
    private static final int IMPORT_DATA_REQUEST = 2;
    private static final int REPEAT_REQUEST = 3;
    public static final int EIGHTA_LOGIN = 4;

    protected static final String FIRST_TIME = "FIRST_TIME";

    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    ActionBarDrawerToggle mDrawerToggle;
    private Cursor cursor;
    private long crag = -1;
    private List<String> crags;
    private SharedPreferences prefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setTitle(R.string.latestAscents);
        prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.inflateMenu(R.menu.main_actionbar);

        db = new InternalDB(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAscent();
            }
        });
        String[] activities = getResources().getStringArray(R.array.activities);
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, activities));
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch ((int)id) {
                    case 0:
                        cragsList();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 1:
                        showScore();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 2:
                        showGrades();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 3:
                        showTop10();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 4:
                        showSummary();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 5:
                        importData();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 6:
                        exportData();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
//                    case 7:
//                        eighASync();
//                        mDrawerLayout.closeDrawer(mDrawerList);
//                        break;
                }
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerToggle.syncState();

        populateList();
        update();
    }

    private void populateList() {
        crags = new ArrayList<>();
        List<Crag> allCrags = db.getCrags();
        crags.add("*");
        for (Crag crag : allCrags) {
            crags.add(crag.getName());
        }
        Spinner cragSpinner = (Spinner) findViewById(R.id.crag_spinner);
        cragSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long rowId) {
                if (rowId == 0) {
                    crag = -1;
                } else {
                    String cragName = crags.get((int)rowId);
                    crag = db.getCrag(cragName).getId();
                }
                update();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        ArrayAdapter ca = new ArrayAdapter(this, android.R.layout.simple_spinner_item, crags);
        cragSpinner.setAdapter(ca);
        cragSpinner.setEnabled(true);
        listView = (ListView)this.findViewById(R.id.list);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long row) {
                long id = adapter.getItemId(position);
                Ascent ascent = db.getAscent(id);
                editAscent(ascent);
            }
        });
    }

    private void update() {
        cursor = db.getAscentsCursor(crag);
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.ascent_list_item, cursor,
                new String[] {"date", "style", "route_grade", "route_name", "crag_name", "sector", "comment"},
                new int[] {R.id.dateCell, R.id.styleCell, R.id.gradeCell, R.id.nameCell, R.id.cragCell, R.id.sectorCell, R.id.commentCell},
                0);
        listView.setAdapter(adapter);
        TextView countView = (TextView) this.findViewById(R.id.countView);
        int count12 = db.getCountLast12Months(crag);
        int count = db.getCountAllTime(crag);
        countView.setText("Ascents: " + count + " - 12M: " + count12);
        TextView scoreView = (TextView) this.findViewById(R.id.scoreView);
        Date now = new Date();
        int year = now.getYear() + 1900;
        scoreView.setText("Score: " + db.getScoreLast12Months() + " - All Time: " + db.getScoreAllTime() + " - Year: " + db.getScoreForYear(year));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchView != null) {
            ComponentName cn = new ComponentName(this, SearchAscentsActivity.class);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        }

        return true;
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appDataBundle = new Bundle();
        appDataBundle.putLong("crag_id", crag);
        startSearch(null, false, appDataBundle, false);
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
        Ascent ascent = db.getAscent(info.id);
        switch (item.getItemId()) {
            case R.id.delete:
                db.deleteAscent(ascent);
                update();
                return true;
            case R.id.repeat:
                repeatAscent(ascent);
                update();
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
                    String rev = data.getStringExtra("rev");
                    Toast.makeText(this, "Exported " + count + " ascents and added to Dropbox rev " + rev, Toast.LENGTH_SHORT).show();
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
        update();
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void onResume() {
        super.onResume();
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (prefs.getString(USER_ID, null) == null) {
            if (isConnected) {
                eightALogin();
            }
        }
        update();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle() != null && "Search".equals(item.getTitle())) {
            return onSearchRequested();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


   private void showSummary() {
        Intent myIntent = new Intent(this, SummaryActivity.class);
        startActivity(myIntent);
   }

    private void eighASync() {
//        SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
//        if (prefs.getString(USER_ID, null) == null) {
//            eightALogin();
//        }
//        if (prefs.getString(USER_ID, null) != null) {
//            new SyncTask(new AsyncResponse() {
//                public void onDone(Long added) {
//                    Toast.makeText(getApplicationContext(), String.format("Successfully imported %d", added), Toast.LENGTH_LONG);
//                    update();
//                }
//            }).execute();
//        }
    }

    interface AsyncResponse {
        public void onDone(Long added);
    }

    class SyncTask extends AsyncTask<Void, Void, Long> {

        private AsyncResponse delegate = null;

        public SyncTask(AsyncResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Long doInBackground(Void... params) {
            EightA eightA = new EightA(prefs.getString(USER_ID, null), prefs.getString(SESSION_ID, null));
            //            List<Ascent> ascents = db.getAscents(true);
//            eightA.pushAscents(ascents);
            return eightA.importData(getBaseContext());
        }

        protected void onPostExecute(Long added) {
            setResult(RESULT_OK);
            delegate.onDone(added);
        }
    }

    private void eightALogin() {
//        Intent myIntent = new Intent(this, EightALoginActivity.class);
//        startActivityForResult(myIntent, EIGHTA_LOGIN);
    }

    private void showScore() {
        Intent myIntent = new Intent(this, ScoreGraphActivity.class);
        startActivity(myIntent);
    }

    private void showGrades() {
        Intent myIntent = new Intent(this, GradeGraphActivity.class);
        startActivity(myIntent);
    }

    private void showTop10() {
        Intent myIntent = new Intent(this, Top10Activity.class);
        startActivity(myIntent);
    }

    private void editAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, EditAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

    private void importData() {
        Intent myIntent = new Intent(this, ImportDataActivity.class);
        startActivityForResult(myIntent, IMPORT_DATA_REQUEST);
    }

    private void exportData() {
        Intent myIntent = new Intent(this, ExportDataActivity.class);
        startActivityForResult(myIntent, EXPORT_DATA_REQUEST);
    }

    private void repeatAscent(Ascent ascent) {
        Intent myIntent = new Intent(this, RepeatAscentActivity.class);
        Bundle b = new Bundle();
        b.putLong("ascentId", ascent.getId());
        myIntent.putExtras(b);
        startActivityForResult(myIntent, REPEAT_REQUEST);
    }

    private void addAscent() {
        Intent myIntent = new Intent(this, AddAscentActivity.class);
        if (crag != -1) {
            Bundle b = new Bundle();
            b.putLong("cragId", crag);
            myIntent.putExtras(b);
        }
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
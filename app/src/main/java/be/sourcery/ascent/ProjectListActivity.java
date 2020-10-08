package be.sourcery.ascent;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ProjectListActivity extends MyActivity {

    private CursorAdapter adapter;
    private InternalDB db;
    private ListView listView;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);
        setTitle(R.string.projects);
        db = new InternalDB(this);
        cursor = db.getProjectsCursor();
        startManagingCursor(cursor);
        adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.project_list_item, cursor,
                new String[] {"route_name", "route_grade", "crag_name", "attempts"},
                new int[] {R.id.nameCell, R.id.gradeCell, R.id.cragCell, R.id.attemptsCell});

        listView = this.findViewById(R.id.list);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
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
                addProject();
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_actionbar, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_list_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Project project = db.getProject(info.id);
        switch (item.getItemId()) {
            case R.id.tickProjectMenu:
                tickProject(project);
                return true;
            case R.id.deleteProjectMenu:
                db.deleteProject(project);
                update();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        cursor.requery();
        adapter.notifyDataSetChanged();
        TextView countView = this.findViewById(R.id.countView);
        countView.setText(cursor.getCount() + " projects in DB");
    }

    private void addProject() {
        Intent myIntent = new Intent(this, AddProjectActivity.class);
        startActivity(myIntent);
    }

    private void tickProject(Project project) {
        Intent myIntent = new Intent(this, TickProjectActivity.class);
        Bundle b = new Bundle();
        b.putLong("projectId", project.getId());
        myIntent.putExtras(b);
        startActivity(myIntent);
    }

}

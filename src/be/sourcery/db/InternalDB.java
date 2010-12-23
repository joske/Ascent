package be.sourcery.db;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import be.sourcery.Ascent;
import be.sourcery.Crag;
import be.sourcery.Project;
import be.sourcery.Route;


public class InternalDB {

    private SQLiteDatabase database;
    private final Context ctx;
    private OpenHelper openHelper;
    private DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public InternalDB(Context ctx) {
        this.ctx = ctx;
        openHelper = new OpenHelper(ctx);
        this.database = openHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
    }

    public Crag addCrag(String name, String country) {
        String stmt = "insert into crag (name, country) values (?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindString(1, name);
        insert.bindString(2, country);
        long id = insert.executeInsert();
        Crag crag = new Crag(id, name, country);
        return crag;
    }

    public Route addRoute(String name, String grade, Crag crag) {
        String stmt = "insert into routes (name, grade, crag_id) values (?, ?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindString(1, name);
        insert.bindString(2, grade);
        insert.bindLong(3, crag.getId());
        long id = insert.executeInsert();
        Route r = new Route(id, name, grade, crag);
        return r;
    }

    public Ascent addAscent(Route route, Date date, int attempts, int style, String comment, int stars) {
        String stmt = "insert into ascents (route_id, attempts, style_id, date, comment, stars) values (?, ?, ?, ?, ?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindLong(1, route.getId());
        insert.bindLong(2, attempts);
        insert.bindLong(3, style);
        insert.bindString(4, fmt.format(date));
        insert.bindString(5, comment);
        insert.bindLong(6, stars);
        long id =  insert.executeInsert();
        return new Ascent(id, route, style, attempts, new Date());
    }

    public void updateAscent(Ascent ascent) {
        String stmt = "update ascents set attempts = ?, style_id = ?, date = ?, comment = ?, stars = ? where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindLong(1, ascent.getAttempts());
        update.bindLong(2, ascent.getStyle());
        update.bindString(3, fmt.format(ascent.getDate()));
        update.bindString(4, ascent.getComment());
        update.bindLong(5, ascent.getStars());
        update.bindLong(6, ascent.getId());
        update.execute();
    }

    public Project addProject(Route route, int attempts) {
        String stmt = "insert into projects (route_id, attempts) values (?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindLong(1, route.getId());
        insert.bindLong(2, attempts);
        long id = insert.executeInsert();
        return new Project(id, route, attempts);
    }

    public Cursor getCragsCrusor() {
        List<Crag> list = new ArrayList<Crag>();
        Cursor cursor = database.query("crag", new String[] { "_id", "name", "country" },
                null, null, null, null, "name desc");
        return cursor;
    }

    public List<Crag> getCrags() {
        List<Crag> list = new ArrayList<Crag>();
        Cursor cursor = database.query("crag", new String[] { "_id", "name", "country" },
                null, null, null, null, "name desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String country = cursor.getString(2);
                Crag c = new Crag(id, name, country);
                list.add(c);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public Cursor getCragsCursor() {
        Cursor cursor = database.query("crag", new String[] { "_id", "name" },
                null, null, null, null, "name desc");
        return cursor;
    }

    public Crag getCrag(long id) {
        Crag c = null;
        Cursor cursor = database.query("crag", new String[] { "name", "country" },
                "_id = " + id, null, null, null, "name desc");
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            String country = cursor.getString(1);
            c = new Crag(id, name, country);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return c;
    }

    public List<Route> getRoutes() {
        List<Route> list = new ArrayList<Route>();
        Cursor cursor = database.query("routes", new String[] { "_id", "name", "grade", "crag_id" },
                null, null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String grade = cursor.getString(2);
                long cragId = cursor.getLong(3);
                Route r = new Route(id, name, grade, getCrag(cragId));
                list.add(r);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public List<Route> getRoutes(Crag crag) {
        List<Route> list = new ArrayList<Route>();
        Cursor cursor = database.query("routes", new String[] { "_id", "name", "grade" },
                "crag_id = " + crag.getId(), null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String grade = cursor.getString(2);
                Route r = new Route(id, name, grade, crag);
                list.add(r);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public Route getRoute(long id) {
        Route c = null;
        Cursor cursor = database.query("routes", new String[] { "_id", "name", "grade", "crag_id" },
                "_id = " + id, null, null, null, "name desc");
        if (cursor.moveToFirst()) {
            String name = cursor.getString(1);
            String grade = cursor.getString(2);
            long crag_id = cursor.getLong(3);
            c = new Route(id, name, grade, getCrag(crag_id));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return c;
    }

    public Cursor getProjectsCursor() {
        List<Project> list = new ArrayList<Project>();
        Cursor cursor = database.query("project_routes", new String[] { "_id", "route_name", "route_grade", "crag_name", "attempts"},
                null, null, null, null, "_id desc");
        return cursor;
    }

    public List<Project> getProjects() {
        List<Project> list = new ArrayList<Project>();
        Cursor cursor = database.query("projects", new String[] { "_id", "route_id", "attempts", },
                null, null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                int attempts = cursor.getInt(2);
                Project p = new Project();
                Route r = getRoute(route_id);
                p.setId(id);
                p.setRoute(r);
                p.setAttempts(attempts);
                list.add(p);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public Cursor getAscentsCursor() {
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date" },
                null, null, null, null, "date desc");
        return cursor;
    }


    public List<Ascent> getAscents() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date" },
                null, null, null, null, "date desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                int attempts = cursor.getInt(2);
                int style = cursor.getInt(3);
                String date = cursor.getString(4);
                Ascent a = new Ascent();
                Route r = getRoute(route_id);
                a.setId(id);
                a.setRoute(r);
                a.setStyle(style);
                a.setAttempts(attempts);
                try {
                    if (date != null) {
                        a.setDate(fmt.parse(date));
                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                list.add(a);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public List<Ascent> getAscents(Crag crag) {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date" },
                "crag_id = " + crag.getId(), null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                int attempts = cursor.getInt(2);
                int style = cursor.getInt(3);
                String date = cursor.getString(4);
                Ascent a = new Ascent();
                Route r = getRoute(route_id);
                a.setId(id);
                a.setRoute(r);
                a.setStyle(style);
                a.setAttempts(attempts);
                try {
                    if (date != null) {
                        a.setDate(fmt.parse(date));
                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                list.add(a);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public Ascent getAscent(long ascentId) {
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars" },
                "_id = " + ascentId, null, null, null, null);
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            long route_id = cursor.getLong(1);
            int attempts = cursor.getInt(2);
            int style = cursor.getInt(3);
            String date = cursor.getString(4);
            Ascent a = new Ascent();
            Route r = getRoute(route_id);
            a.setId(id);
            a.setRoute(r);
            a.setStyle(style);
            a.setAttempts(attempts);
            a.setComment(cursor.getString(5));
            a.setStars(cursor.getInt(6));
            try {
                if (date != null) {
                    a.setDate(fmt.parse(date));
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return a;
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }

    public void deleteAscent(Ascent ascent) {
        String stmt = "delete from ascents where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindLong(1, ascent.getId());
        update.execute();
    }

    class OpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 2;
        private static final String DATABASE_NAME = "ascent";


        public OpenHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table crag (_id integer primary key autoincrement, name text, country text);");
            db.execSQL("create table styles (_id integer primary key, name text, short_name text);");
            db.execSQL("insert into styles values (1, 'Onsight', 'OS');");
            db.execSQL("insert into styles values (2, 'Flash', 'FL');");
            db.execSQL("insert into styles values (3, 'Redpoint', 'RP');");
            db.execSQL("insert into styles values (4, 'Toprope', 'TP');");
            db.execSQL("create table routes (_id integer primary key autoincrement, name text, grade text, crag_id text);");
            db.execSQL("create table ascents (_id integer primary key autoincrement, route_id int, date text, attempts int, style_id int, comment string, stars int);");
            db.execSQL("create table projects (_id integer primary key autoincrement, route_id int, attempts int);");
            db.execSQL("create view ascent_routes as select a._id as _id, r._id as route_id, r.name as route_name, r.grade as route_grade, a.attempts as attempts, s.short_name as style, a.date as date from ascents a inner join routes r on a.route_id = r._id inner join styles s on a.style_id = s._id;");
            db.execSQL("create view project_routes as select p._id as _id, r.name as route_name, r.grade as route_grade, c.name as crag_name, p.attempts as attempts from projects p inner join routes r on p.route_id = r._id inner join crag c on r.crag_id = c._id;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }
    }

}

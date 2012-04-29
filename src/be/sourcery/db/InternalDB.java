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
import android.util.Log;
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
        Route r = new Route(id, name, grade, crag, getGradeScore(grade));
        return r;
    }

    public Ascent addAscent(Route route, Date date, int attempts, int style, String comment, int stars) {
        String stmt = "insert into ascents (route_id, attempts, style_id, date, comment, stars, score) values (?, ?, ?, ?, ?, ?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindLong(1, route.getId());
        insert.bindLong(2, attempts);
        insert.bindLong(3, style);
        insert.bindString(4, fmt.format(date));
        insert.bindString(5, comment);
        insert.bindLong(6, stars);
        int gradeScore = route.getGradeScore();
        int styleScore = getStyleScore(style);
        insert.bindLong(7, gradeScore + styleScore);
        long id =  insert.executeInsert();
        return new Ascent(id, route, style, attempts, new Date(), comment, stars, gradeScore + styleScore);
    }

    public void updateAscent(Ascent ascent) {
        String stmt = "update ascents set attempts = ?, style_id = ?, date = ?, comment = ?, stars = ?, score = ? where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindLong(1, ascent.getAttempts());
        update.bindLong(2, ascent.getStyle());
        update.bindString(3, fmt.format(ascent.getDate()));
        update.bindString(4, ascent.getComment());
        update.bindLong(5, ascent.getStars());
        update.bindLong(6, ascent.getId());
        int gradeScore = ascent.getRoute().getGradeScore();
        int styleScore = getStyleScore(ascent.getStyle());
        update.bindLong(7, gradeScore + styleScore);
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
                null, null, null, null, "name asc");
        return cursor;
    }

    public List<Crag> getCrags() {
        List<Crag> list = new ArrayList<Crag>();
        Cursor cursor = database.query("crag", new String[] { "_id", "name", "country" },
                null, null, null, null, "name asc");
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
                null, null, null, null, "name asc");
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

    public Crag getCrag(String searchName) {
        Crag c = null;
        Cursor cursor = database.query("crag", new String[] { "_id", "name", "country" },
                "name like '" + searchName + "%'", null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            String country = cursor.getString(2);
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
                int gradeScore = getGradeScore(grade);
                Route r = new Route(id, name, grade, getCrag(cragId), gradeScore);
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
                int gradeScore = getGradeScore(grade);
                Route r = new Route(id, name, grade, crag, gradeScore);
                list.add(r);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public int getGradeScore(String grade) {
        int gradeScore = 0;
        Cursor cursor = database.query("grades", new String[] { "score" },
                "grade = '" + grade + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            gradeScore = cursor.getInt(0);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return gradeScore;
    }

    public int getStyleScore(int style) {
        int styleScore = 0;
        Cursor cursor = database.query("styles", new String[] { "score" },
                "_id = " + style, null, null, null, null);
        if (cursor.moveToFirst()) {
            styleScore = cursor.getInt(0);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return styleScore;
    }

    public Route getRoute(long id) {
        Route c = null;
        Cursor cursor = database.query("routes", new String[] { "_id", "name", "grade", "crag_id" },
                "_id = " + id, null, null, null, "name desc");
        if (cursor.moveToFirst()) {
            String name = cursor.getString(1);
            String grade = cursor.getString(2);
            long crag_id = cursor.getLong(3);
            int gradeScore = getGradeScore(grade);
            c = new Route(id, name, grade, getCrag(crag_id), gradeScore);
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
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars", "score" },
                null, null, null, null, "date desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                int attempts = cursor.getInt(2);
                int style = cursor.getInt(3);
                String date = cursor.getString(4);
                String comment = cursor.getString(5);
                int stars = cursor.getInt(6);
                Ascent a = new Ascent();
                Route r = getRoute(route_id);
                a.setId(id);
                a.setRoute(r);
                a.setStyle(style);
                a.setAttempts(attempts);
                a.setComment(comment);
                a.setStars(stars);
                a.setScore(cursor.getInt(7));
                try {
                    if (date != null) {
                        a.setDate(fmt.parse(date));
                    }
                } catch (ParseException e) {
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

    public Cursor getAscentsCursor(Crag crag) {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date" },
                "crag_id = " + crag.getId(), null, null, null, "date desc");
        return cursor;
    }

    public Cursor getAscentsCursorForHighestScoredLast12Months() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date", "score" },
                "julianday(date('now'))- julianday(date) < 365", null, null, null, "date desc, score desc",  "10");
        return cursor;
    }

    public int getScoreLast12Months() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "score", "date", "route_name", "route_grade" },
                "julianday(date('now'))- julianday(date) < 365",
                null,
                null,
                null,
                "score desc, date desc",
        "10");
        int total = 0;
        if (cursor.moveToFirst()) {
            do {
                int score = cursor.getInt(0);
                String name = cursor.getString(2);
                String grade = cursor.getString(3);
                Log.w("score", "name=" + name + ", grade=" + grade + ", score=" + score);
                total += score;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total;
    }

    public Ascent getAscent(long ascentId) {
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars", "score" },
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
            a.setScore(cursor.getInt(7));
            try {
                if (date != null) {
                    a.setDate(fmt.parse(date));
                }
            } catch (ParseException e) {
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

        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "ascent";


        public OpenHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table crag (_id integer primary key autoincrement, name text, country text);");
            db.execSQL("create table styles (_id integer primary key, name text, short_name text, score int);");
            db.execSQL("insert into styles values (1, 'Onsight', 'OS', 145);");
            db.execSQL("insert into styles values (2, 'Flash', 'FL', 53);");
            db.execSQL("insert into styles values (3, 'Redpoint', 'RP', 0);");
            db.execSQL("insert into styles values (4, 'Toprope', 'TP', -52);");
            db.execSQL("insert into styles values (5, 'Repeat', 'Rep', 0);");
            db.execSQL("insert into styles values (6, 'Multipitch', 'MP', 0);");
            db.execSQL("create table routes (_id integer primary key autoincrement, name text, grade text, crag_id integer);");
            db.execSQL("create table ascents (_id integer primary key autoincrement, route_id int, date text, attempts int, style_id int, comment string, stars int, score int);");
            db.execSQL("create table projects (_id integer primary key autoincrement, route_id int, attempts int);");
            db.execSQL("create table grades (grade text primary key, score number);");
            db.execSQL("insert into grades values ('3', 150);");
            db.execSQL("insert into grades values ('4', 200);");
            db.execSQL("insert into grades values ('5a', 250);");
            db.execSQL("insert into grades values ('5b', 300);");
            db.execSQL("insert into grades values ('5c', 350);");
            db.execSQL("insert into grades values ('6a', 400);");
            db.execSQL("insert into grades values ('6a+', 450);");
            db.execSQL("insert into grades values ('6b', 500);");
            db.execSQL("insert into grades values ('6b+', 550);");
            db.execSQL("insert into grades values ('6c', 600);");
            db.execSQL("insert into grades values ('6c+', 650);");
            db.execSQL("insert into grades values ('7a', 700);");
            db.execSQL("insert into grades values ('7a+', 750);");
            db.execSQL("insert into grades values ('7b', 800);");
            db.execSQL("insert into grades values ('7b+', 850);");
            db.execSQL("insert into grades values ('7c', 900);");
            db.execSQL("insert into grades values ('7c+', 950);");
            db.execSQL("insert into grades values ('8a', 1000);");
            db.execSQL("insert into grades values ('8a+', 1050);");
            db.execSQL("insert into grades values ('8b', 1100);");
            db.execSQL("insert into grades values ('8b+', 1150);");
            db.execSQL("insert into grades values ('8c', 1200);");
            db.execSQL("insert into grades values ('8c+', 1250);");
            db.execSQL("insert into grades values ('9a', 1300);");
            db.execSQL("insert into grades values ('9a+', 1350);");
            db.execSQL("insert into grades values ('9b', 1400);");
            db.execSQL("insert into grades values ('9b+', 1450);");
            db.execSQL("insert into grades values ('9c', 1500);");
            db.execSQL("insert into grades values ('9c+', 1550);");
            db.execSQL("insert into grades values ('10a', 1600);");
            db.execSQL("insert into grades values ('10a+', 1650);");
            db.execSQL("insert into grades values ('10b', 1700);");
            db.execSQL("insert into grades values ('10b+', 1750);");
            db.execSQL("insert into grades values ('10c', 1800);");
            db.execSQL("insert into grades values ('10c+', 1850);");
            db.execSQL("create view ascent_routes as select a._id as _id, r._id as route_id, r.name as route_name, r.grade as route_grade, a.attempts as attempts, s.short_name as style, s.score as style_score, a.date as date, r.crag_id as crag_id, a.score as score, g.score as grade_score from ascents a inner join routes r on a.route_id = r._id inner join styles s on a.style_id = s._id inner join grades g on g.grade = r.grade;");
            db.execSQL("create view project_routes as select p._id as _id, r.name as route_name, r.grade as route_grade, c.name as crag_name, p.attempts as attempts from projects p inner join routes r on p.route_id = r._id inner join crag c on r.crag_id = c._id;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}

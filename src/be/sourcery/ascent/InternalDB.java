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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;


public class InternalDB {

    public static final String KEY_ROUTE = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_GRADE = SearchManager.SUGGEST_COLUMN_TEXT_2;
    private static final String FTSASCENTS = "FTascents";
    private static final String DATABASE_NAME = "ascent";

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

    public void updateRoute(Route r) {
        String stmt = "update routes set name = ?, grade = ?, crag_id = ? where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindString(1, r.getName());
        update.bindString(2, r.getGrade());
        update.bindLong(3, r.getCrag().getId());
        update.bindLong(4, r.getId());
        r.setGradeScore(getGradeScore(r.getGrade()));
        update.execute();
    }

    public Ascent addAscent(Project project, Date date, int attempts, int style, String comment, int stars) {
        database.beginTransaction();
        Route route;
        int score;
        long id;
        try {
            String stmt = "insert into ascents (route_id, attempts, style_id, date, comment, stars, score) values (?, ?, ?, ?, ?, ?, ?);";
            SQLiteStatement insert = database.compileStatement(stmt);
            route = project.getRoute();
            insert.bindLong(1, route.getId());
            insert.bindLong(2, attempts);
            insert.bindLong(3, style);
            insert.bindString(4, fmt.format(date));
            insert.bindString(5, comment);
            insert.bindLong(6, stars);
            score = 0;
            if (style != 5) {
                int gradeScore = route.getGradeScore();
                int styleScore = getStyleScore(style);
                score = gradeScore + styleScore;
            }
            insert.bindLong(7, score);
            id = insert.executeInsert();
            deleteProject(project);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return new Ascent(id, route, style, attempts, new Date(), comment, stars, score);
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
        int totalScore = 0;
        if (style != 5 && style != 6 && style != 7) {
            int gradeScore = route.getGradeScore();
            int styleScore = getStyleScore(style);
            totalScore = calculateScore(attempts, style, gradeScore, styleScore);
        }
        insert.bindLong(7, totalScore);
        long id =  insert.executeInsert();
        return new Ascent(id, route, style, attempts, new Date(), comment, stars, totalScore);
    }

    public void updateAscent(Ascent ascent) {
        String stmt = "update ascents set attempts = ?, style_id = ?, date = ?, comment = ?, stars = ?, score = ? where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        int attempts = ascent.getAttempts();
        update.bindLong(1, attempts);
        int style = ascent.getStyle();
        update.bindLong(2, style);
        update.bindString(3, fmt.format(ascent.getDate()));
        update.bindString(4, ascent.getComment());
        update.bindLong(5, ascent.getStars());
        int gradeScore = ascent.getRoute().getGradeScore();
        int styleScore = getStyleScore(style);
        int totalScore = calculateScore(attempts, style, gradeScore, styleScore);
        update.bindLong(6, totalScore);
        update.bindLong(7, ascent.getId());
        update.execute();
    }

    protected int calculateScore(int attempts, int style, int gradeScore, int styleScore) {
        int totalScore = gradeScore + styleScore;
        if (style == Ascent.STYLE_REDPOINT && attempts == 2) {
            totalScore += 2;
        }
        return totalScore;
    }

    public List<String> getGrades() {
        List<String> list = new ArrayList();
        Cursor cursor = database.query("grades", new String[] { "grade" },
                null, null, null, null, "score desc");
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                list.add(name);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public Project addProject(Route route, int attempts) {
        String stmt = "insert into projects (route_id, attempts) values (?, ?);";
        SQLiteStatement insert = database.compileStatement(stmt);
        insert.bindLong(1, route.getId());
        insert.bindLong(2, attempts);
        long id = insert.executeInsert();
        return new Project(id, route, attempts);
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
                "_id = ?", new String[] { "" + id}, null, null, "name desc");
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
                "name like ? ", new String[] { searchName + "%"}, null, null, "_id desc");
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
                "crag_id = ?", new String[] { "" + crag.getId()}, null, null, "_id desc");
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
                "grade = ?", new String[] {grade }, null, null, null, null);
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
                "_id = ? ", new String[] { "" + style }, null, null, null, null);
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
                "_id = ?", new String[] { "" + id}, null, null, "name desc");
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

    public Project getProject(long id) {
        Project project = null;
        Cursor cursor = database.query("projects", new String[] { "route_id", "attempts", },
                null, null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                long route_id = cursor.getLong(0);
                int attempts = cursor.getInt(1);
                project = new Project();
                Route r = getRoute(route_id);
                project.setId(id);
                project.setRoute(r);
                project.setAttempts(attempts);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return project;
    }

    public List<Ascent> getAscents(String grade, int year, long crag) {
        List<Ascent> list = new ArrayList<Ascent>();
        String whereClause = "(style_id = 1 or style_id = 2 or style_id = 3 or style_id = 7) and route_grade = ?";
        if (year != -1) {
            whereClause += " and strftime('%Y', date) = ?";
        }
        if (crag != -1) {
            whereClause += " and crag_id = ?";
        }
        List<String> selectionList = new ArrayList();
        selectionList.add("" + grade);
        if (year != -1) {
            selectionList.add("" + year);
        }
        if (crag != -1) {
            selectionList.add("" + crag);
        }
        String[] selectionArgs = selectionList.toArray(new String[selectionList.size()]);
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars", "score" },
                whereClause, selectionArgs, null, null, "date desc, _id asc");
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

    public List<Ascent> getAscents() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars", "score" },
                null, null, null, null, "date desc, _id asc");
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

    public List<Ascent> getSortedAscentsForLast12Months() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes", new String[] { "_id", "route_id", "route_grade", "attempts", "style_id", "date", "comment", "stars", "score" },
                "julianday(date('now'))- julianday(date) < 365", null, null, null, "route_grade desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                String route_grade = cursor.getString(2);
                int attempts = cursor.getInt(3);
                int style = cursor.getInt(4);
                String date = cursor.getString(5);
                String comment = cursor.getString(6);
                int stars = cursor.getInt(7);
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

    public List<Ascent> getSortedAscents() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes", new String[] { "_id", "route_id", "route_grade", "attempts", "style_id", "date", "comment", "stars", "score" },
                null, null, null, null, "route_grade desc");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long route_id = cursor.getLong(1);
                String route_grade = cursor.getString(2);
                int attempts = cursor.getInt(3);
                int style = cursor.getInt(4);
                String date = cursor.getString(5);
                String comment = cursor.getString(6);
                int stars = cursor.getInt(7);
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

    public Cursor getAscentsCursor(long cragId) {
        String whereClause = null;
        String[] selectionArgs = null;
        if (cragId != -1) {
            whereClause = "crag_id = ?";
            selectionArgs = new String[] { "" + cragId};
        }
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date", "stars", "comment" },
                whereClause, selectionArgs, null, null, "date desc, _id asc");
        return cursor;
    }

    public Cursor getAscentsCursor(String grade, boolean allTime) {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date" },
                "route_grade = ?" + (allTime ? "" : " and julianday(date('now'))- julianday(date) < 365"),
                new String[] {grade}, null, null, "date desc, _id asc");
        return cursor;
    }

    public Cursor getAscentsCursorForHighestScoredLast12Months() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date", "score" },
                "julianday(date('now'))- julianday(date) < 365", null, null, null, "date desc, score desc",  "10");
        return cursor;
    }

    public Cursor getAscentsCursorForLast12Months() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date", "score" },
                "julianday(date('now'))- julianday(date) < 365", null, null, null, "route_grade desc");
        return cursor;
    }

    protected int getCount(long cragId, boolean last12Months) {
        String whereClause = "style_id <> 7";
        if (last12Months) {
            whereClause += " and (julianday(date('now'))- julianday(date) < 365)";
        }
        String[] selectionArgs = null;
        if (cragId != -1) {
            whereClause += " and crag_id = ?";
            selectionArgs = new String[] { "" + cragId};
        }
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "date" },
                whereClause,
                selectionArgs,
                null,
                null,
                "date desc");
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getCountAllTime(long cragId) {
        return getCount(cragId, false);
    }

    public int getCountLast12Months(long cragId) {
        return getCount(cragId, true);
    }

    public int getScoreLast12Months() {
        return getScore(true);
    }

    public int getScoreAllTime() {
        return getScore(false);
    }

    protected int getScore(boolean last12Months) {
        String whereClause = "style_id <> 7";
        if (last12Months) {
            whereClause += " and (julianday(date('now'))- julianday(date) < 365)";
        }
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "score", "date", "route_name", "route_grade" },
                whereClause,
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
                total += score;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total;
    }

    protected Cursor getTop10TwelveMonths() {
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "date", "style", "route_grade", "route_name", "score" },
                "julianday(date('now'))- julianday(date) < 365 and style_id <> 7",
                null,
                null,
                null,
                "score desc, date desc",
                "10");
        return cursor;
    }

    protected Cursor getTop10AllTime() {
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "date", "style", "route_grade", "route_name", "score" },
                "style_id <> 7",
                null,
                null,
                null,
                "score desc, date desc",
                "10");
        return cursor;
    }

    public Cursor getTop10ForYear(int year) {
        Cursor cursor = database.query("ascent_routes",
                new String[] { "_id", "date", "style", "route_grade", "route_name", "score" },
                "strftime('%Y', date) = ? and style_id <> 7",
                new String[] { "" + year },
                null,
                null,
                "score desc, date desc",
                "10");
        return cursor;
    }

    public Map<String, Integer> getSummaryDoneForYear(int year, long crag) {
        String whereClause = "(style_id = 1 or style_id = 2 or style_id = 3)";
        return getSummary(year, crag, whereClause);
    }

    public Map<String, Integer> getSummaryTriedForYear(int year, long crag) {
        String whereClause = "style_id = 7";
        return getSummary(year, crag, whereClause);
    }

    protected Map<String, Integer> getSummary(int year, long crag, String whereClause) {
        if (year != -1) {
            whereClause += " and strftime('%Y', date) = ?";
        }
        if (crag != -1) {
            whereClause += " and crag_id = ?";
        }
        List<String> selectionList = new ArrayList();
        if (year != -1) {
            selectionList.add("" + year);
        }
        if (crag != -1) {
            selectionList.add("" + crag);
        }
        String[] selectionArgs = selectionList.toArray(new String[selectionList.size()]);
        Cursor cursor = database.query("ascent_routes",
                new String[] { "route_grade", "count(*)" },
                whereClause,
                selectionArgs,
                "route_grade",
                null,
                "route_grade desc",
                null);
        Map<String, Integer> list = new HashMap();
        if (cursor.moveToFirst()) {
            do {
                String grade = cursor.getString(0);
                int count = cursor.getInt(1);
                list.put(grade, Integer.valueOf(count));
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    public int getScoreForYear(int year) {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "score", "date", "route_name", "route_grade" },
                "strftime('%Y', date) = ? and style_id <> 7",
                new String[] { "" + year },
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
                total += score;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return total;
    }

    public int getFirstYear() {
        List<Ascent> list = new ArrayList<Ascent>();
        Cursor cursor = database.query("ascent_routes",
                new String[] { "date, route_name" },
                null,
                null,
                null,
                null,
                "date asc",
                "1");
        int year = 0;
        if (cursor.moveToFirst()) {
            try {
                Date date = fmt.parse(cursor.getString(0));
                year = date.getYear() + 1900;
            } catch (ParseException e) {
            }
        }
        cursor.close();
        return year;
    }

    public Cursor searchAscents(String query, long crag_id) {
        String[] columns = new String[] {  "_id", "route_id", "route_name", "route_grade", "attempts", "style", "date", "stars", "comment"  };
        String selection = "route_name like ?";
        String[] selectionArgs = new String[] { "%" + query + "%"};
        if (crag_id != -1) {
            selection += " and crag_id = ?";
            selectionArgs = new String[] { "%" + query + "%", "" + crag_id};
        }
        Cursor cursor = database.query(
                "ascent_routes",
                columns,
                selection,
                selectionArgs,
                null,
                null,
                "date desc",
                null);
        return cursor;
    }

    public Cursor searchAscents(String query, String[] columns) {
        String selection = KEY_ROUTE + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTSASCENTS);
        HashMap<String,String> columnMap = new HashMap<String,String>();
        builder.setProjectionMap(columnMap);
        columnMap.put(KEY_ROUTE, KEY_ROUTE);
        columnMap.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        columnMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        columnMap.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);

        Cursor cursor = builder.query(openHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }


    public Ascent getAscent(long ascentId) {
        Cursor cursor = database.query("ascents", new String[] { "_id", "route_id", "attempts", "style_id", "date", "comment", "stars", "score" },
                "_id = ?", new String[] { "" + ascentId }, null, null, null, null);
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

    public void deleteProject(Project project) {
        String stmt = "delete from projects where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindLong(1, project.getId());
        update.execute();
    }

    public void deleteCrag(Crag crag) {
        String stmt = "delete from crag where _id = ?;";
        SQLiteStatement update = database.compileStatement(stmt);
        update.bindLong(1, crag.getId());
        update.execute();
    }

    class OpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 10;
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
            db.execSQL("insert into styles values (7, 'Tried', 'AT', 0);");
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
            db.execSQL("create view ascent_routes as select a._id as _id, r._id as route_id, r.name as route_name, r.grade as route_grade, a.attempts as attempts, a.comment as comment, s._id as style_id, s.short_name as style, s.score as style_score, a.stars as stars, a.date as date, r.crag_id as crag_id, a.score as score, g.score as grade_score, c.name as crag_name, c._id as crag_id from ascents a inner join routes r on a.route_id = r._id inner join styles s on a.style_id = s._id inner join grades g on g.grade = r.grade inner join crag c on r.crag_id = c._id;");
            db.execSQL("create view project_routes as select p._id as _id, r.name as route_name, r.grade as route_grade, c.name as crag_name, p.attempts as attempts from projects p inner join routes r on p.route_id = r._id inner join crag c on r.crag_id = c._id;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 8) {
                db.execSQL("drop view ascent_routes;");
                db.execSQL("create view ascent_routes as select a._id as _id, r._id as route_id, r.name as route_name, r.grade as route_grade, a.attempts as attempts, a.comment as comment, s._id as style_id, s.short_name as style, s.score as style_score, a.stars as stars, a.date as date, r.crag_id as crag_id, a.score as score, g.score as grade_score, c.name as crag_name from ascents a inner join routes r on a.route_id = r._id inner join styles s on a.style_id = s._id inner join grades g on g.grade = r.grade inner join crag c on r.crag_id = c._id;");
            }
            if (oldVersion == 9) {
                db.execSQL("drop view ascent_routes;");
                db.execSQL("create view ascent_routes as select a._id as _id, r._id as route_id, r.name as route_name, r.grade as route_grade, a.attempts as attempts, a.comment as comment, s._id as style_id, s.short_name as style, s.score as style_score, a.stars as stars, a.date as date, r.crag_id as crag_id, a.score as score, g.score as grade_score, c.name as crag_name, c._id as crag_id from ascents a inner join routes r on a.route_id = r._id inner join styles s on a.style_id = s._id inner join grades g on g.grade = r.grade inner join crag c on r.crag_id = c._id;");
            }
        }
    }


}

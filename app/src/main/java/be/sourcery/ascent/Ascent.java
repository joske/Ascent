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

import java.util.Date;


public class Ascent {

    public static final int STYLE_ONSIGHT = 1;
    public static final int STYLE_FLASH = 2;
    public static final int STYLE_REDPOINT = 3;
    public static final int STYLE_TOPROPE = 4;
    public static final int STYLE_REPEAT = 5;
    public static final int STYLE_MULTIPITCH = 6;
    public static final int STYLE_TRIED = 7;

    private long id;
    private Route route;
    private int style;
    private int attempts;
    private Date date;
    private String comment;
    private int stars;
    private int score;
    private boolean modified;
    private String eightaId;

    public Ascent() {
    }

    public Ascent(long id, Route route, int style, int attempts, Date date, int score) {
        super();
        this.id = id;
        this.setScore(score);
        this.setRoute(route);
        this.style = style;
        this.attempts = attempts;
        this.date = date;
    }

    public Ascent(long id, Route route, int style, int attempts, Date date, String comment, int stars, int score) {
        this(id, route, style, attempts, date, score);
        this.comment = comment;
        this.stars = stars;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStyle() {
        return style;
    }

    public String getStyleString() {
        switch (style) {
            case 1:
                return "OS";
            case 2:
                return "FL";
            case 3:
                return "RP";
            case 4:
                return "TP";
            case 5:
                return "Rep";
            case 6:
                return "MP";
            default:
                return "AT";
        }
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String toString() {
        return route + " on " + date;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getEightaId() {
        return eightaId;
    }

    public void setEightaId(String eightaId) {
        this.eightaId = eightaId;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}

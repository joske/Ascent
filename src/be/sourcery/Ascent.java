package be.sourcery;

import java.util.Date;

import android.os.Parcel;


public class Ascent {

    public static final int STYLE_ONSIGHT = 1;
    public static final int STYLE_FLASH = 2;
    public static final int STYLE_REDPOINT = 3;
    public static final int STYLE_TOPROPE = 4;

    private long id;
    private Route route;
    private int style;
    private int attempts;
    private Date date;
    private String comment;
    private int stars;

    public Ascent() {
    }

    public Ascent(long id, Route route, int style, int attempts, Date date) {
        super();
        this.id = id;
        this.setRoute(route);
        this.style = style;
        this.attempts = attempts;
        this.date = date;
    }

    public Ascent(long id, Route route, int style, int attempts, Date date, String comment, int stars) {
        this(id, route, style, attempts, date);
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

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }

}

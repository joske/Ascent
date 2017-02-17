package be.sourcery.ascent.eighta;

import java.util.Date;

/**
 * Created by jos on 16/02/17.
 */

public class EightAAscent {

    private int rating;
    private String grade;
    private String name;
    private String crag;
    private int style;
    private int score;
    private boolean repeat;
    private Date date;
    private String type;
    private String sector;
    private String objectClass;
    private String countryCode;
    private String comment;
    private String id;

    public String getSector() {
        return this.sector;
    }

    public void setRating(int i) {
        this.rating = i;
    }

    public void setSector(String str) {
        this.sector = str;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGrade() {
        return this.grade;
    }

    public void setStyle(int i) {
        this.style = i;
    }

    public void setGrade(String str) {
        this.grade = str;
    }

    public String getName() {
        return this.name;
    }

    public void setScore(int i) {
        this.score = i;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getCrag() {
        return this.crag;
    }

    public void setCrag(String str) {
        this.crag = str;
    }

    public int getStyle() {
        return this.style;
    }

    public void setType(String str) {
        this.type = str;
    }

    public int setObjectClass() {
        return ("CLS_UserAscent".equals(this.objectClass) && repeat) ? this.score : 0;
    }

    public void setObjectClass(String str) {

        this.objectClass = str;
    }

    public Date getDate() {
        return this.date;
    }

    public String toString() {
        return String.format("%s - %s", getName(), getCrag());
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

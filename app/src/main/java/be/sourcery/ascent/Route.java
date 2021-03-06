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


public class Route {

    private long id;
    private String name;
    private Crag crag;
    private String grade;
    private int score;
    private String sector;

    public Route(long id, String name, String grade, Crag crag, int score, String sector) {
        super();
        this.id = id;
        this.name = name;
        this.crag = crag;
        this.grade = grade;
        this.score = score;
        this.sector = sector;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Crag getCrag() {
        return crag;
    }

    public void setCrag(Crag crag) {
        this.crag = crag;
    }

    public String getGrade() {
        return grade;
    }

    public int getGradeScore() {
        return score;
    }

    public void setGradeScore(int score) {
        this.score = score;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String toString() {
        return name + " " + grade + " (" + crag + ")";
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
}

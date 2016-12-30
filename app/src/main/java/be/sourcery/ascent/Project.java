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


public class Project {

    private long id;
    private Route route;
    private int attempts;

    public Project() {
    }

    public Project(long id, Route route, int attempts) {
        super();
        this.id = id;
        this.setRoute(route);
        this.attempts = attempts;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public int getAttempts() {
        return attempts;
    }


    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }


    public void setRoute(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

}

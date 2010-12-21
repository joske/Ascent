package be.sourcery;



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

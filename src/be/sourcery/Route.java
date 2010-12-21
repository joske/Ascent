package be.sourcery;



public class Route {

    private long id;
    private String name;
    private Crag crag;
    private String grade;

    public Route() {
    }

    public Route(long id, String name, String grade, Crag crag) {
        super();
        this.id = id;
        this.name = name;
        this.crag = crag;
        this.grade = grade;
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


    public void setGrade(String grade) {
        this.grade = grade;
    }

}

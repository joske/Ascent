package be.sourcery;


public class Crag {

    private long id;
    private String name;
    private String country;

    public Crag(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public Crag(long id, String name, String country) {
        this.setId(id);
        this.name = name;
        this.country = country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}

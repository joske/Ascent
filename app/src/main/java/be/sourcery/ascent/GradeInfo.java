package be.sourcery.ascent;


public class GradeInfo {

    private final String grade;
    private final int osCount;
    private final int flashCount;
    private final int rpCount;
    private final int tpCount;

    public GradeInfo(String grade, int osCount, int flashCount, int rpCount, int tpCount) {
        super();
        this.grade = grade;
        this.rpCount = rpCount;
        this.osCount = osCount;
        this.flashCount = flashCount;
        this.tpCount = tpCount;
    }


    public String getGrade() {
        return grade;
    }


    public int getRpCount() {
        return rpCount;
    }


    public int getOsCount() {
        return osCount;
    }


    public int getFlashCount() {
        return flashCount;
    }


    public int getTpCount() {
        return tpCount;
    }

    public int getTotal() {
        return osCount + flashCount + rpCount + tpCount;
    }

}

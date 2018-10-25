package cs115.ucsc.polidev.politrack;

public class Report {
    public String type;
    public String time;
    public double longit;
    public double latit;
    public String reportedUser;
    public int count;

    public Report(String type, String time, double longit, double latit, String reportedUser, int count){
        this.type = type;
        this.time = time;
        this.longit = longit;
        this.latit = latit;
        this.reportedUser = reportedUser;
        this.count = count;
    }


}

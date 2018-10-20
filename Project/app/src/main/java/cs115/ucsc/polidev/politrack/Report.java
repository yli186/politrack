package cs115.ucsc.polidev.politrack;

public class Report {
    public String type;
    public String time;
    public String longit;
    public String latit;
    public String reportedUser;
    public int count;

    public Report(String type, String time, String longit, String latit, String reportedUser, int count){
        this.type = type;
        this.time = time;
        this.longit = longit;
        this.latit = latit;
        this.reportedUser = reportedUser;
        this.count = count;
    }


}

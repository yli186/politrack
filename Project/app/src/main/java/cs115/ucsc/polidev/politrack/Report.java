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
    public Report(){

    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public double getLongit() {
        return longit;
    }

    public double getLatit(){
        return latit;
    }
    public String getReportedUser(){
        return reportedUser;
    }
    public int getCount(){
        return count;
    }
}

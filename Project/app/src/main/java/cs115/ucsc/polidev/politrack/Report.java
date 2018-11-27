package cs115.ucsc.polidev.politrack;
import java.io.Serializable;

public class Report implements Serializable{
    public String type;
    public String time;
    public double latit;
    public double longit;
    public String reportedUser;
    public int count;

    public Report(String type, String time, double latit, double longit, String reportedUser, int count){
        this.type = type;
        this.time = time;
        this.latit = latit;
        this.longit = longit;
        this.reportedUser = reportedUser;
        this.count = count;
    }
    public Report(){

    }

    public void setCount(){ this.count = this.getCount() + 1; }

    public void setTime(String new_time){ this.time = new_time; }

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

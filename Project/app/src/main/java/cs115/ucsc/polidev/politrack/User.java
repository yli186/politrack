package cs115.ucsc.polidev.politrack;

public class User {
    public String userEmail;
    public String password;
    public String name;
    public int prefRadius;
    public String category;
    public int prefTime;
    public boolean prefNotification;
    public boolean prefShake;
    public User(String name, String userEmail, String password, int prefRadius, String category, int prefTime, boolean prefNotification, boolean prefShake){
        this.userEmail = userEmail;
        this.name = name;
        this.password = password;
        this.category = category;
        this.prefRadius = prefRadius;
        this.prefTime = prefTime;
        this.prefNotification = prefNotification;
        this.prefShake = prefShake;
    }
    public User(){

    }
}

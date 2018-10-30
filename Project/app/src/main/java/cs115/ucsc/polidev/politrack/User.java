package cs115.ucsc.polidev.politrack;

public class User {
    public String userEmail;
    public String password;
    public String name;
    public int prefRadius;
    public User(String name, String userEmail, String password, int prefRadius){
        this.userEmail = userEmail;
        this.name = name;
        this.password = password;
        this.prefRadius = prefRadius;
    }
    public User(){

    }
}

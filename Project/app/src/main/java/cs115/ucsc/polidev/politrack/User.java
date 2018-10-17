package cs115.ucsc.polidev.politrack;

public class User {
    public String userEmail;
    public String password;
    public String name;

    public User(String name, String userEmail, String password){
        this.userEmail = userEmail;
        this.name = name;
        this.password = password;
    }
}

package it.bx.fallmerayer.tfo.mainserver;

//User Object
public class User{
    private String username;
    private String password;
    private boolean isLoggedin;
    private int udpport;

    public User(String username, String password, boolean isLoggedin, int udpport) {
        this.username = username;
        this.password = password;
        this.isLoggedin = isLoggedin;
        this.udpport = udpport;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedin() {
        return isLoggedin;
    }

    public void setLoggedin(boolean loggedin) {
        isLoggedin = loggedin;
    }

    //Compares two users
    public boolean compareUsers(User user) {
        return this.username.equals(user.getUsername()) && this.username.equals(getPassword()) && this.isLoggedin == user.isLoggedin;
    }

    public void setUdpport(int udpport) {
        this.udpport = udpport;
    }

    @Override
    public String toString() {
        return "User{" + username + ", isLoggedin=" + isLoggedin + ", port=" + udpport + "}";
    }
}
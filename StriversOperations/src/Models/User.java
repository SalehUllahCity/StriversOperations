package Models;

public class User {
    private int userID;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String password;

    public User(int userID, String name, String email, String phone, String role, String password) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.password = password;
    }

    // Getters
    public int getUserID() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getPassword() { return password; }

    // Setters
    public void setUserID(int userID) { this.userID = userID; }
    public void setFirstName(String Name) { this.name = Name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setPassword(String password) { this.password = password; }
}

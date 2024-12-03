package server;

public class UsernameValidator {

    public static boolean isUsernameValid(String username){
        return username != "" && username.matches("^[A-Za-z0-9_]{3,14}$");
    }

    public static boolean isUsernameAvailable(String username) {
        return !Server.isLoggedIn(username);
    }

    public static int checkUsername(String username) {
        if (!isUsernameValid(username)) {
            return 5001; // Invalid format or length
        }
        else if (!isUsernameAvailable(username)) {
            return 5000; // User is already logged in
        }
        else {
            return 5002; // User cannot login 2 times
        }
    }
}

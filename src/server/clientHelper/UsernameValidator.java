package server.clientHelper;

public class UsernameValidator {

    /**
     * Check if the username is valid
     * @param username the username to be checked
     * @return true if the username is valid, false otherwise
     */
    public static boolean isUsernameValid(String username){
        return !username.equals("") && username.matches("^[A-Za-z0-9_]{3,14}$");
    }

    /**
     * Check if the username is available
     * @param username the username to be checked
     * @return true if the username is available, false otherwise
     */
    public static boolean isUsernameAvailable(String username) {
        return !ClientLogger.getInstance().isUserLoggedIn(username);
    }

    /**
     * Check if the username is valid and available
     * @param username the username to be checked
     * @return 5000 if the username is already logged in,
     * 5001 if the username is invalid,
     * 5002 if the username is valid but already logged in
     */
    public static int checkUsername(String username) {
        if (!isUsernameValid(username)) {
            return 5001;
        }
        else if (!isUsernameAvailable(username)) {
            return 5000;
        }
        else {
            return 5002;
        }
    }
}
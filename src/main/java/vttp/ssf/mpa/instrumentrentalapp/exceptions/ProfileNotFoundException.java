package vttp.ssf.mpa.instrumentrentalapp.exceptions;

public class ProfileNotFoundException extends RuntimeException{

    public ProfileNotFoundException() {
        super("User has not created a profile.");
    }

}

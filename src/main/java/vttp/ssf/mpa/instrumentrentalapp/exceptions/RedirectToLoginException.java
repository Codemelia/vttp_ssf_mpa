package vttp.ssf.mpa.instrumentrentalapp.exceptions;

public class RedirectToLoginException extends RuntimeException {

    public RedirectToLoginException() {
        super("Redirecting to login page.");
    }
    
}


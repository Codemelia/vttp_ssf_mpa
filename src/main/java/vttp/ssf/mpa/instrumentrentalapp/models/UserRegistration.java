package vttp.ssf.mpa.instrumentrentalapp.models;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vttp.ssf.mpa.instrumentrentalapp.validations.ValidAge;

// model for user registration

public class UserRegistration {

    // For validation

    @NotBlank(message="Please enter your mobile number")
    @Pattern(regexp="^(?:\\+65)?[689]\\d{7}$", message="Mobile number must be a valid Singapore mobile number")
    private String number;

    @NotBlank(message="Please enter your address")
    @Pattern(regexp="^(\\d{1,5})\\s([A-Za-z0-9\\s\\.\\-]+)\\s*(?:#(\\d{1,2})\\s*-?\\d{1,4})?\\s*,?\\s*(?:Singapore\\s*)?S?\\(?\\d{6}\\)?$",
        message="Address must be a valid Singapore address with your block number, street name, and postal code")
    private String address; // to be stored as latitude/longitude

    @NotNull(message="Please enter your date of birth")
    @Past(message="Date of birth must be in the past")
    @ValidAge(message="You must be at least 16 years old to register") // max set to 100
    private LocalDate birthDate;

    @NotBlank(message="Please enter your username")
    @Size(min=5, max=12, message="Username must be between 5 and 12 characters")
    private String username; // Username

    @NotBlank(message="Please enter your password")
    @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$", 
        message="Password must contain at least 8 characters with one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    // for storing lat/long/neighborhood
    private double latitude;
    private double longitude;
    private String neighborhood;
    
    // Getters and Setters

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    // Constructors

    public UserRegistration() {
    }

    public UserRegistration(String number, double latitude, double longitude, String neighborhood, LocalDate birthDate, String username, String password) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighborhood = neighborhood;
        this.birthDate = birthDate;
        this.username = username;
        this.password = password;
    }

}

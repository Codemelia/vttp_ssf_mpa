package vttp.ssf.mpa.instrumentrentalapp.models;

import jakarta.validation.constraints.Size;
import vttp.ssf.mpa.instrumentrentalapp.validations.ValidUrl;

public class UserProfile {

    // profile fields will be optional
    
    @ValidUrl(message="Image URLs must be in valid JPG or PNG format")
    private String profilePicture;
    private String username; // from userobj

    @Size(max=30, message="Name must not exceed 30 characters")
    private String nickname;

    private String gender;

    @Size(max=150, message="Bio must not exceed 150 characters")
    private String bio; // restrict for display

    @Size(max=50, message="Preferences must not exceed 50 characters")
    private String dealPreferences; // for preferred meetup location/shipping methods

    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getDealPreferences() {
        return dealPreferences;
    }
    public void setDealPreferences(String dealPreferences) {
        this.dealPreferences = dealPreferences;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public UserProfile() {
        
    }
    
    public UserProfile(String profilePicture, String username, String nickname, String gender, String bio, String dealPreferences) {
        this.profilePicture = profilePicture;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.bio = bio;
        this.dealPreferences = dealPreferences;
    }
    
    @Override
    public String toString() {
        return "UserProfile [profilePicture=" + profilePicture + ", username=" + username + ", nickname=" + nickname
                + ", gender=" + gender + ", bio=" + bio + ", dealPreferences=" + dealPreferences + "]";
    }

}

package vttp.ssf.mpa.instrumentrentalapp.models.helpers;

public class Location {

    private double latitude;
    private double longitude;
    private String neighborhood;

    // Getters and Setters

    public Location(double latitude, double longitude, String neighborhood) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighborhood = neighborhood;
    }
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

    @Override
    public String toString() {
        return "Location [latitude=" + latitude + ", longitude=" + longitude + ", neighborhood=" + neighborhood + "]";
    }

}


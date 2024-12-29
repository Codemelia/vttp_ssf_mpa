package vttp.ssf.mpa.instrumentrentalapp.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vttp.ssf.mpa.instrumentrentalapp.validations.ValidUrls;

public class RentalListing implements Serializable {
    
    private static final long serialVersionUID = 1L;  // Add serialVersionUID

    private LocalDate dateCreated; // set by server
    private ZonedDateTime dateUpdated; // set by server

    private String listingId; // set by server
    private String ownerName; // taken from regis
    private String ownerNumber; // taken from regis

    @NotBlank(message="Title is required")
    @Size(min=12, max=30, message="Title must contain between 12 and 25 characters") // display purposes
    private String title;

    private String instrumentType; // defaults to Others

    @Size(max=25, message="Instrument type cannot exceed 25 characters") // display purposes
    private String instrumentBrand; // defaults to Others

    @Size(max=25, message="Instrument type cannot exceed 25 characters") // display purposes
    private String instrumentModel; // defaults to Others

    @NotBlank(message="Description is required")
    @Size(min=50, max=300, message="Description must contain between 50 and 300 characters")
    private String description;

    @NotNull(message="Rental fees must be specified")
    private double fees;

    private String priceModel; // hourly or daily or monthly - defaults to hourly

    @NotNull(message="Availability of instrument must be declared")
    private Boolean isAvailable;

    @NotEmpty(message="Select your preferred contact methods")
    private List<String> contactMethods; // if "others" selected, owner should include contact method in desc

    private double latitude; // taken from regis
    private double longitude; // taken from regis
    private String neighborhood; // taken from regis

    @ValidUrls(message="Image URLs must be in valid JPG or PNG format")
    private List<String> instrumentPics; // urls for pic of instrument (max 3) - optional

    public String getNeighborhood() {
        return neighborhood;
    }
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerNumber() {
        return ownerNumber;
    }
    public void setOwnerNumber(String ownerNumber) {
        this.ownerNumber = ownerNumber;
    }

    public String getListingId() {
        return listingId;
    }
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstrumentType() {
        return instrumentType;
    }
    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getInstrumentBrand() {
        return instrumentBrand;
    }
    public void setInstrumentBrand(String instrumentBrand) {
        this.instrumentBrand = instrumentBrand;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }
    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public double getFees() {
        return fees;
    }
    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getPriceModel() {
        return priceModel;
    }
    public void setPriceModel(String priceModel) {
        this.priceModel = priceModel;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public List<String> getContactMethods() {
        return contactMethods;
    }
    public void setContactMethods(List<String> contactMethods) {
        this.contactMethods = contactMethods;
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

    public LocalDate getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ZonedDateTime getDateUpdated() {
        return dateUpdated;
    }
    public void setDateUpdated(ZonedDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public List<String> getInstrumentPics() {
        return instrumentPics;
    }
    public void setInstrumentPics(List<String> instrumentPics) {
        this.instrumentPics = instrumentPics;
    }

    public RentalListing() {

    }

    public RentalListing(LocalDate dateCreated, ZonedDateTime dateUpdated, String listingId, String ownerName, String ownerNumber, String title, String instrumentType, 
        String instrumentBrand, String instrumentModel, List<String> instrumentPics, String description, double fees, String priceModel, Boolean isAvailable, 
        List<String> contactMethods, double latitude, double longitude, String neighborhood) {
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.ownerName = ownerName;
        this.ownerNumber = ownerNumber;
        this.listingId = listingId;
        this.title = title;
        this.instrumentType = instrumentType;
        this.instrumentBrand = instrumentBrand;
        this.instrumentModel = instrumentModel;
        this.instrumentPics = instrumentPics;
        this.description = description;
        this.fees = fees;
        this.priceModel = priceModel;
        this.isAvailable = isAvailable;
        this.contactMethods = contactMethods;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighborhood = neighborhood;
    }

    @Override
    public String toString() {
        return "RentalListing [dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + ", listingId=" + listingId
                + ", ownerName=" + ownerName + ", ownerNumber=" + ownerNumber + ", title=" + title + ", instrumentType="
                + instrumentType + ", instrumentBrand=" + instrumentBrand + ", instrumentModel=" + instrumentModel
                + ", description=" + description + ", fees=" + fees + ", priceModel=" + priceModel + ", isAvailable="
                + isAvailable + ", contactMethods=" + contactMethods + ", latitude=" + latitude + ", longitude="
                + longitude + ", neighborhood=" + neighborhood + "]";
    }
    
}

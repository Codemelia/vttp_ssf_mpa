# OVERVIEW
This is an instrument rental app that has two core functions: <br>
1. For instrument owners to put their instruments up for rental. <br>
2. For musicians to browse through instruments to rent in their area. <br>

APIs used: <br>
1. Google Geocoding API: Obtains latitude, longitude, and neighborhood from user address - these details are used for sorting in the app as well as displaying location. <br>
2. Google Static Maps API: Obtains map image src from latitude and longitude - this is displayed in listing page. <br>

# Pages and Features
For any request to pages other than "/", "/login", and "/register" without authentication (login), user will be redirected to login page. <br>

## Login (index page)
1. Validation only requires field to be filled. <br>

## Registration
1. Validation for all variables. <br>
IMPT: Mobile number is validated to SG format (can contain +65, must start with digits 8 or 9), this is to ensure that the listing pages generate the correct links. <br>
IMPT: Address is validated to SG format (must contain block number, street, postal code - unit number optional), this is to ensure that the geocoding API obtains the best results when called. <br>

## Home (renter-centric)
1. Everyone can access renter features once they log in. <br>
2. Renters will have access to: Profile Management, Browse and Search Listings, Liked Listings. <br>
3. Core feature here is the search/sort/filter functions, where users can apply a search radius (max 50km to match SG radius) before searching to shortlist their results based on distance. Users can also apply all the other functions which will submit form on change for smooth user experience. <br>
4. Only search results are cached to ensure listings remain fresh and to avoid data redundancy/latency <br>
5. Users can select either Like or View Details, former adds the listing to the user's liked listings and latter brings user to the specific listing page. <br>

## My Listings (owner-centric)
1. Everyone can access this page. <br>
2. Owners will have access to: Profile Management, Create New Listing, Manage Listings. <br>
3. Core function here is listing management, where the owner's listings are displayed on the table in which they can choose to edit or delete their listings. Owners can create new listings, where only the required fields are validated for display and search optimisation purposes. <br>
4. If owners do not set instrument images on their listings, the cover image (on homepage) will default to a default icon. <br>

## Shared features
1. Both renters and owners can create and edit their own profiles by accessing Manage Your Profile. None of the fields are validated for requirement, but rather length and format for display purposes. If they do not have a profile pic set, a default prof pic will be set to their profile. <br>
2. For owners, few profile fields are shown on their listings, with the option to view their profile via a link. When a potential renter clicks on this link, they will go to a view-only profile page displaying the owner's profile. <br>
3. For renters, their profile is used to generate contact links which they can click on in the listing page when they want to contact the owner. A pre-filled message is generated, where their profile link will be embedded. Owners can access that link to view renter profile. <br>
4. Both renters and owners can access My Calendar, which is a personal tracking calendar for their rentals and listings; they can add or edit the booking form on the same page, and immediate visual feedback is shown on the calendar. They have the options to edit or delete the bookings. <br>

## Logout
1. Session is invalidated when user logs out.
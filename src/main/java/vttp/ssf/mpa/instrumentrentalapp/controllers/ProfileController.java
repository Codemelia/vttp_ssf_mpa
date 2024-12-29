package vttp.ssf.mpa.instrumentrentalapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vttp.ssf.mpa.instrumentrentalapp.exceptions.ProfileNotFoundException;
import vttp.ssf.mpa.instrumentrentalapp.models.UserProfile;
import vttp.ssf.mpa.instrumentrentalapp.services.UserService;

// for profile functions

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userSvc;
    
    // to create new prof - to be displayed on listings for credibility
    @GetMapping
    public ModelAndView getProfile(HttpSession session) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // mav
        ModelAndView mav = new ModelAndView("createprofile")
            .addObject("username", username);

        // get userprof
        UserProfile profile = userSvc.getProfile(username);

        // if prof null, send new prof
        if (profile != null) {
            mav.addObject("userProfile", profile);
        } else {
            mav.addObject("userProfile", new UserProfile());
        }

        return mav;

    }

    // for owner profile
    @PostMapping("/submit")
    public ModelAndView postProfile(@Valid @ModelAttribute("userProfile") UserProfile userProfile, BindingResult binding, HttpSession session, RedirectAttributes redirectAttributes) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // if there are validation errors (only for optional restrictions), return to page with errors
        if (binding.hasErrors()) {
            return new ModelAndView("createprofile");
        }

        try {

            // save userprofile non-null values
            userSvc.saveProfile(username, userProfile);

            // return to profile page with filled data
            return new ModelAndView("createprofile")
                .addObject("userProfile", userProfile)
                .addObject("username", username)
                .addObject("successMessage", "Profile save was successful!");

        } catch (Exception e) {

            // return to profile page with global error
            ModelAndView mav = new ModelAndView("createprofile");
            mav.addObject("userProfile", userProfile);
            mav.addObject("username", username);
            mav.addObject("errorMessage", "Profile save was unsuccessful: %s".formatted(e.getMessage()));
            mav.setStatus(HttpStatus.BAD_REQUEST); // set to 400
            return mav;

        }
    }

    // to view user profile
    @GetMapping("/{username}")
    public ModelAndView viewProfile(@PathVariable String username) {

        // if userprofile not set yet/cannot be found, throw exception (handled in controlleradvice)

        try {

            UserProfile userProfile = userSvc.getProfile(username);

            return new ModelAndView("viewprofile")
            .addObject("userProfile", userProfile)
            .addObject("username", username);

        } catch (Exception e) {
            throw new ProfileNotFoundException(); // set own error
        }

    }

}

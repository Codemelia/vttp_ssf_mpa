package vttp.ssf.mpa.instrumentrentalapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vttp.ssf.mpa.instrumentrentalapp.models.UserLogin;
import vttp.ssf.mpa.instrumentrentalapp.models.UserRegistration;

import vttp.ssf.mpa.instrumentrentalapp.services.UserService;

// controller for user login/regis

@Controller
@RequestMapping
public class UserController {

    @Autowired
    private UserService userSvc;
    
    // get login page
    @GetMapping(path={"/", "/login"})
    public ModelAndView getLogin() {

        return new ModelAndView("login")
            .addObject("userLogin", new UserLogin());

    }

    // post login page - validate user here
    @PostMapping("/login")
    public ModelAndView postHomepage(@Valid @ModelAttribute("userLogin") UserLogin user, BindingResult binding, HttpSession session) {

        // if there are validation errors
        if (binding.hasErrors()) {
            // return to login page with errors
            ModelAndView mav = new ModelAndView("login");
            mav.setStatus(HttpStatus.BAD_REQUEST); // set to 400
            return mav;
        }

        // if user not in redis db/pw does not match
        if (!userSvc.validate(user)) {

            // add global error to binding
            binding.reject("errorMessage", "Username or password is invalid. Please try again.");

            // return to login page with global error
            ModelAndView mav = new ModelAndView("login");
            mav.setStatus(HttpStatus.UNAUTHORIZED); // set to 401
            return mav;

        } else {

            // mark user authenticated in session
            session.setAttribute("authenticatedUser", user.getUsername()); // dont save pw

            // if user was trying to access resource before login, redirect back to that uri after login
            String requestUri = (String) session.getAttribute("requestUri");

            // redirect according to request URI or to home if null
            if (requestUri != null && !requestUri.isEmpty() && !(requestUri.startsWith("/login") || requestUri.equals("/"))) {
                session.removeAttribute("requestUri"); // remove before redirect to prevent loop
                return new ModelAndView("redirect:%s".formatted(requestUri));
            } else {
                return new ModelAndView("redirect:/home");
            }

        }

    }

    // register page handler
    @GetMapping("/register")
    public ModelAndView getRegister() {

        return new ModelAndView("register")
            .addObject("userRegistration", new UserRegistration());

    }

    // process registration
    @PostMapping("/register")
    public ModelAndView postRegister(@Valid @ModelAttribute("userRegistration") UserRegistration user, BindingResult binding, RedirectAttributes redirectAttributes) {

        // if there are validation errors
        if (binding.hasErrors()) {

            // return to regis page with errors
            ModelAndView mav = new ModelAndView("register");
            mav.setStatus(HttpStatus.BAD_REQUEST); // set to 400
            return mav;

        }

        // if no validation errors
        // check if user alr exists in redis
        if (userSvc.userExists(user.getUsername())) {

            // add global error if user alr exists (means username alr in use)
            binding.reject("regisError", "Username already exists. Please login if it is your account, or use a different username.");

            // return to regis page with global error
            ModelAndView mav = new ModelAndView("register");
            mav.setStatus(HttpStatus.CONFLICT); // set to 409
            return mav;

        }
        
        // if user doesnt exist in redis
        try {

            // save user to redis
            userSvc.saveUser(user);

            // add success message to redirect
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");

            // redirect to login page
            return new ModelAndView("redirect:/login");

        // catch all exceptions (if geocoding api call fails/saving fails)
        } catch (Exception e) {
            
            // if error saving to redis, add global error
            binding.reject("saveError", "Error saving registration: %s".formatted(e.getMessage()));

            // return to regis page with global error
            ModelAndView mav = new ModelAndView("register");
            mav.setStatus(HttpStatus.BAD_REQUEST); // set to 400
            return mav;

        }

    }

    // log out button
    @GetMapping("/logout")
    public ModelAndView logOut(HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {

        // invalidate session
        session.invalidate();

        // Clear the session cookie from the client
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0); // Expire immediately
        cookie.setPath("/"); // Ensure the cookie is cleared for the correct path
        response.addCookie(cookie);

        // redirect to login page with redir attr
        redirectAttributes.addFlashAttribute("flashMessage", "Successfully logged out! See you again.");
        return new ModelAndView("redirect:/login");

    }

}

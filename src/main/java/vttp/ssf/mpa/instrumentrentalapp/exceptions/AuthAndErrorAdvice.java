package vttp.ssf.mpa.instrumentrentalapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class AuthAndErrorAdvice {

    // authentication method to be called first thing for controller mappings
    @ModelAttribute
    public void checkAuthentication(HttpSession session, HttpServletRequest request) {
        
        // if the session or username is null, it means the user is unauthenticated
        if (session == null || session.getAttribute("authenticatedUser") == null) {
            
            // get uri
            String uri = request.getRequestURI();

            System.out.println("Request URI: " + uri);
            System.out.println("Authenticated User: " + session.getAttribute("authenticatedUser"));

            // throw redirect exception if request is anything other than /, /login or /register
            if (!(uri.equals("/") || uri.startsWith("/login") || uri.startsWith("/register"))) {

                // save uin session before throwing redirect exception
                session.setAttribute("requestUri", uri);
                throw new RedirectToLoginException();

            }
    
        }

    }

    // custom exception for redirection
    @ResponseStatus(HttpStatus.FOUND) // set status to 302
    @ExceptionHandler(RedirectToLoginException.class)
    public ModelAndView handleRedirectToLogin(RedirectAttributes redirectAttributes) {

        // add a flash message
        redirectAttributes.addFlashAttribute("errorMessage", "Unauthorised access. Please login.");

        // redirect to login with flash message
        return new ModelAndView("redirect:/login");

    }

    // handle generic exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception ex) {
        return new ModelAndView("error")
            .addObject("error", "Exception occurred: %s".formatted(ex.getMessage()));  // add error message
    }

    // handle NullPointerException
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleNullPointerException(NullPointerException ex) {
        return new ModelAndView("error")
            .addObject("error", "Null pointer exception occurred: %s".formatted(ex.getMessage())); // add error message
    }

    // handle NoResourceFoundException
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex) {
        return new ModelAndView("error")
            .addObject("error", "No resource found exception occurred: %s".formatted(ex.getMessage())); // add error message
    }

    // handle ProfileNotFoundException
    @ExceptionHandler(ProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleProfileNotFoundException(ProfileNotFoundException ex) {
        return new ModelAndView("error")
            .addObject("error", "Profile not found exception occurred: %s".formatted(ex.getMessage())); // add error message
    } 

}

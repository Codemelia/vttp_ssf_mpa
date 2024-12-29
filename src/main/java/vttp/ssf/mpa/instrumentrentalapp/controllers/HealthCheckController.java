package vttp.ssf.mpa.instrumentrentalapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vttp.ssf.mpa.instrumentrentalapp.services.SharedService;

// for app-redis connection check

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @Autowired
    private SharedService sharedSvc;

    // perform healthcheck by checking if redis is active/contains data
    @GetMapping
    public ResponseEntity<String> getHealth() {

        try {

            // try to get randomkey
            sharedSvc.randomKey();

            // if redis has randomkey, return app.json with body {} status 200
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{}");

        } catch (Exception e) {
            
            // if redis does not have randomkey, return app.json with body {} status 503
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body("{}");

        }
        
    }
}

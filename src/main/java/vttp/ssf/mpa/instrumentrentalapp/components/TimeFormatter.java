package vttp.ssf.mpa.instrumentrentalapp.components;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

@Component("timeFormatter")
public class TimeFormatter {
    
    // to get time ago for daateUpdated
    public String getTimeAgo(ZonedDateTime dateUpdated) {

        // get duration btw dateUpdated and now
        Duration duration = Duration.between(dateUpdated, ZonedDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return seconds + "s ago";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h ago";
        } else if (seconds < 604800) {
            return (seconds / 86400) + "d ago";
        } else {
            return (seconds / 604800) + "w ago";
        }
    }

}

package ch.so.agi.oereb;

import elemental2.dom.Location;

public class UrlComponents {
    private Location location;
    
    private String pathname;
    
    public UrlComponents(Location location, String pathname) {
        this.location = location;
        this.pathname = pathname;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }
}

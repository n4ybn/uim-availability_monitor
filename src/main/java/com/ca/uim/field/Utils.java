package com.ca.uim.field;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimLog;
import com.nimsoft.nimbus.NimRequest;
import com.nimsoft.nimbus.PDS;

import java.util.ArrayList;

public class Utils {
    static private NimLog logger = NimLog.getLogger(Utils.class);

    /**
     * Run the getrobots callback on the hub and collect the base robot information0
     * @return ArrayList of Robot Objects
     * @throws NimException
     */
    public static ArrayList<Robot> getRobots() throws NimException {
        ArrayList<Robot> robots = new ArrayList<Robot>();
        NimRequest getRobotsRequest = null;
        PDS getRobotsResponse = null;
        PDS[] pdss = null;
        try {
            getRobotsRequest = new NimRequest("hub", "getrobots");
            getRobotsResponse = getRobotsRequest.send();
        } catch (NimException e) {
            logger.error("Failed to retrieve the list of robots from hub. We will skip discovering this hub for now. Cause: " + e.getMessage());
        } finally {
            if (getRobotsRequest != null) {
                getRobotsRequest.disconnect();
                getRobotsRequest.close();
            }
        }
        if (getRobotsResponse != null) { // ok, we received a list of robots from the hub
            pdss = getRobotsResponse.getTablePDSs("robotlist");

            for (int entry = 0; entry < pdss.length; entry++) { // robot iteration
                boolean isActive;
                String robotAddress = pdss[entry].getStringIfExists("addr"); // get robot address
                String robotName = pdss[entry].getStringIfExists("name"); // get robot name
                String origin = pdss[entry].getStringIfExists("origin"); // get origin
                String ipAddress = pdss[entry].getStringIfExists("ip");
                String version = pdss[entry].getStringIfExists("version");
                int status = pdss[entry].getIntIfExists("status");
                int offline = pdss[entry].getIntIfExists("offline");
                if (status != 0 || offline != 0) {
                    isActive = false;
                } else {
                    isActive = true;
                }
                Robot r = new Robot(origin, robotName, robotAddress, isActive, ipAddress, version);
                robots.add(r);
            }
        }
        return robots;
    }
}

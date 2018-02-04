package com.ca.uim.field;

import com.nimsoft.nimbus.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

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
            logger.error("Failed to retrieve the list of robots from hub. This probe will not work. Cause: " + e.getMessage());
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

    /**
     * Get a list of probes deployed on a given robot and add the following information
     * active, state and name and port
     * @param robotAddress
     * @return HashMap<String, Probe>
     * @throws NimException
     */
    public static HashMap<String, Probe> getProbeList(String robotAddress) throws NimException {
        HashMap<String, Probe> probeMap = new HashMap<>();
        logger.debug("Getting probe list from: " + robotAddress);
        NimRequest getProbesRequest = null;
        PDS getProbesResponse = null;
        try {
            getProbesRequest = new NimRequest(robotAddress + "/controller", "probe_list");
            getProbesResponse = getProbesRequest.send();
        } catch (NimException e) {
            logger.error("Failed to retrieve the list of probes from robot " + robotAddress + ". Cause: " + e.getMessage());
        } finally {
            if (getProbesRequest != null) {
                getProbesRequest.disconnect();
                getProbesRequest.close();
            }
        }
        try {
            if (getProbesResponse != null) { // ok, we received a list of probes from the robot
                Enumeration<String> p = getProbesResponse.keys();
                while (p.hasMoreElements()) {
                    String probeKey = p.nextElement();
                    PDS probeList = getProbesResponse.getPDS(probeKey);
                    String probeName = probeList.getString("name");
                    String state = probeList.getString("process_state");
                    int active = probeList.getInt("active");
                    int port = probeList.getInt("port");
                    Probe probe = new Probe();
                    probe.setProbeName(probeName);
                    probe.setState(state);
                    probe.setPort(port);
                    if (active == 1) {
                        probe.setActive(true);
                    } else {
                        probe.setActive(false);
                    }
                    probeMap.put(probeName, probe);
                }
            }
        } catch (Exception e) {
            logger.error("Error while extracting PDS elements from probe_list");
        }
        return probeMap;
    }

    /**
     * Check if a probe is active
     * @param probe
     * @return Boolean
     */
    public static Boolean checkProbeHealth(Probe probe) {
        Boolean isHealthy = true;
        if (!probe.getActive()) {
            isHealthy = false;
            return isHealthy;
        }

        return isHealthy;
    }

    /**
     * Get the probe configuration
     * @return NimConfig
     */
    public static NimConfig getProbeConfiguration() {
        NimConfig config = null;
        try {
            config = NimConfig.getInstance();
        } catch (NimException e) {
            logger.error("Failed while retrieving NimConfig.getInstance()");
        }
        return config;
    }

    public static boolean isRobotReachable(String address) {
        boolean isReachable = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            isReachable = inetAddress.isReachable(5000);
            logger.debug(address+" is reachable? "+isReachable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isReachable;
    }
}

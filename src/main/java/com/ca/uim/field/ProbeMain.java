package com.ca.uim.field;

import com.nimsoft.nimbus.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProbeMain {
    private MyNimProbe nimProbe;
    static private NimLog logger = NimLog.getLogger(ProbeMain.class);

    public static final  String PROBE_NAME = "availability_monitor";
    public static final  String PROBE_VERSION = "1.00";
    public static final  String PROBE_VENDOR = "CA Technologies";

    public static void main(final String[] args) {
        try {
           ProbeMain pm = new ProbeMain(args);
           pm.executeUntilHalt();
        } catch (final Exception e) {
            logger.fatal(e.getMessage());
        }
    }

    public ProbeMain(final String[] args)throws NimException{
        nimProbe = new MyNimProbe(PROBE_NAME, PROBE_VERSION, PROBE_VENDOR, args);
        nimProbe.login();
    }

    private void executeUntilHalt() throws NimException{
        do {
            NimConfig config =  Utils.getProbeConfiguration();
            Long interval = config.getValueAsLong("setup", "interval", 60);
            nimProbe.registerCallbackOnTimer(this, "checkAvailability", interval*1000, false);
        } while (nimProbe.doForever());
    }

    /**
     * Checks the robot availability on the configured interval in seconds
     * @throws NimException
     */
    public void checkAvailability () throws NimException {
        logger.info("Checking hub for robot availability");
        // Get list of robots and their probes for the local hub
        ArrayList<Robot> robotList = Utils.getRobots();
        for (Robot r : robotList) {
            if (r.isActive()) {
                HashMap<String, Probe> probeMap = new HashMap<>();
                try {
                    probeMap =  Utils.getProbeList(r.getRobotAddress());
                    // Check the spooler and hdb
                    Probe spooler = probeMap.get("spooler");
                    Probe hdb = probeMap.get("hdb");
                    if (Utils.checkProbeHealth(spooler) || Utils.checkProbeHealth(hdb)) {
                        logger.info("Robot: "+r.getRobotName()+" is active and healthy, moving on");
                        logger.debug("Setting availability and reachability to 1 for robot: "+r.getRobotName());
                    } else {
                        logger.info("Spooler or HDB not active on robot: "+r.getRobotName()+". Please validate probe security");
                    }
                } catch (Exception e) {
                    logger.error("Error while retrieving the probe list from "+r.getRobotName());
                }
            } else {
                logger.info("Robot: "+r.getRobotName()+" is offline, setting availability to 0");
                // Now check if the system is online.
                boolean isReachable = Utils.isRobotReachable(r.getIpAddress());
                if (isReachable) {
                    logger.debug(r.getRobotName()+" is pingable, setting reachability to 1");
                } else {
                    logger.debug(r.getRobotName()+" is not pingable, setting reachability to 0");
                }
            }

        }
    }

}

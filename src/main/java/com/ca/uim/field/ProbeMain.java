package com.ca.uim.field;

import com.nimsoft.nimbus.*;
import com.nimsoft.nimbus.ci.ConfigurationItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProbeMain {
    private MyNimProbe nimProbe;
    static private NimLog logger = NimLog.getLogger(ProbeMain.class);

    public static final String PROBE_NAME = "availability_monitor";
    public static final String PROBE_VERSION = "1.00";
    public static final String PROBE_VENDOR = "CA Technologies";
    public static int INTERVAL = 60;
    public static boolean runOnce = true;

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
            INTERVAL = config.getValueAsInt("setup", "interval", 60);
            nimProbe.registerCallbackOnTimer(this, "checkAvailability", INTERVAL*1000, false);
            try {
                checkAvailability();
            } catch (Exception e) {
                logger.error("Error while running checkAvailability on probe startup.");
            }
        } while (nimProbe.doForever());
    }

    /**
     * Checks the robot availability on the configured interval in seconds
     * @throws NimException
     */
    public void checkAvailability () throws NimException, IOException {
        logger.info("Checking hub for robot availability");
        // Get list of robots and their probes for the local hub
        ArrayList<Robot> robotList = Utils.getRobots();
        for (Robot r : robotList) {
            ConfigurationItem ci = new ConfigurationItem("10.2", r.getRobotName(), r.getRobotName());
            // Check ip address for localhost, this can prevent proper communication
            if (r.getIpAddress().equalsIgnoreCase("127.0.0.1")) {
                String downMessage = "Robot ip address detected as 127.0.0.1, please configure the robot_ip parameter";
                NimAlarm downAlarm = new NimAlarm(NimAlarm.NIML_MAJOR, downMessage, "1.1.1", r.getRobotName()+"/127.0.0.1", r.getRobotName(), ci, "1:17");
                String nimid = downAlarm.send();
                logger.info("Alarm created with NIMID: " + nimid);
                downAlarm.close();
            }

            // insert into CM_CONFIGURATION_ITEM_METRIC_DEFINITION VALUES ('10.2:98', 'Robot Availability', 'state', '10.2', NULL);
            // insert into CM_CONFIGURATION_ITEM_METRIC_DEFINITION VALUES ('10.2:99', 'Robot Reachability', 'state', '10.2', NULL);
            if (r.isActive()) {
                HashMap<String, Probe> probeMap = new HashMap<>();
                try {
                    probeMap =  Utils.getProbeList(r.getRobotAddress());
                    // Check the spooler and hdb
                    Probe spooler = probeMap.get("spooler");
                    Probe hdb = probeMap.get("hdb");
                    if (Utils.checkProbeHealth(spooler) || Utils.checkProbeHealth(hdb)) {
                        //logger.info("Robot: "+r.getRobotName()+" is active and healthy, moving on");
                        logger.debug("Setting availability and reachability to 1 for robot: "+r.getRobotName());
                        NimQoS aQos = new NimQoS(ci, "98", "QOS_ROBOT_AVAILABILITY", false);
                        if (runOnce) {
                            aQos.setDefinition("QOS_NETWORK", "Status of robot availability", "Status", "status");
                        }

                        aQos.setSource(r.getRobotName());
                        aQos.setSampleRate(INTERVAL);
                        aQos.setTarget(r.getRobotName());
                        aQos.setValue(1);
                        aQos.send();
                        aQos.close();
                        NimQoS rQos = new NimQoS(ci, "99", "QOS_ROBOT_REACHABILITY", false);
                        if (runOnce) {
                            rQos.setDefinition("QOS_NETWORK", "Status of robot reachability", "Status", "status");
                        }
                        rQos.setSource(r.getRobotName());
                        rQos.setSampleRate(INTERVAL);
                        rQos.setTarget(r.getRobotName());
                        rQos.setValue(1);
                        rQos.send();
                        rQos.close();
                    } else {
                        logger.info("Spooler or HDB not active on robot: "+r.getRobotName()+". Please validate probe security");
                        String downMessage = "HDB or Spooler is not active, please validate security on these probes";
                        NimAlarm downAlarm = new NimAlarm(NimAlarm.NIML_MAJOR, downMessage, "1.1.1", r.getRobotName()+"/validate_security", r.getRobotName(), ci, "1:17");
                        String nimid = downAlarm.send();
                        logger.info("Alarm created with NIMID: " + nimid);
                        downAlarm.close();
                        NimQoS aQos = new NimQoS(ci, "98", "QOS_ROBOT_AVAILABILITY", false);
                        if (runOnce) {
                            aQos.setDefinition("QOS_NETWORK", "Status of robot availability", "Status", "status");
                        }

                        aQos.setSource(r.getRobotName());
                        aQos.setSampleRate(INTERVAL);
                        aQos.setTarget(r.getRobotName());
                        aQos.setValue(0);
                        aQos.send();
                        aQos.close();
                        NimQoS rQos = new NimQoS(ci, "99", "QOS_ROBOT_REACHABILITY", false);
                        if (runOnce) {
                            rQos.setDefinition("QOS_NETWORK", "Status of robot reachability", "Status", "status");
                        }
                        rQos.setSource(r.getRobotName());
                        rQos.setSampleRate(INTERVAL);
                        rQos.setTarget(r.getRobotName());
                        rQos.setValue(1);
                        rQos.send();
                        rQos.close();
                    }
                } catch (Exception e) {
                    logger.error("Error while retrieving the probe list from "+r.getRobotName());
                }
            } else {
                logger.info("Robot: "+r.getRobotName()+" is offline, setting availability to 0");
                NimQoS aQos = new NimQoS(ci, "98", "QOS_ROBOT_AVAILABILITY", false);
                if (runOnce) {
                    aQos.setDefinition("QOS_NETWORK", "Status of robot availability", "Status", "status");
                }

                aQos.setSource(r.getRobotName());
                aQos.setSampleRate(INTERVAL);
                aQos.setTarget(r.getRobotName());
                aQos.setValue(0);
                aQos.send();
                aQos.close();
                boolean isReachable = Utils.isRobotReachable(r.getIpAddress());
                if (isReachable) {
                    logger.debug(r.getRobotName()+" is pingable, setting reachability to 1");
                    NimQoS rQos = new NimQoS(ci, "99", "QOS_ROBOT_REACHABILITY", false);
                    if (runOnce) {
                        rQos.setDefinition("QOS_NETWORK", "Status of robot availability", "Status", "status");
                    }
                    rQos.setSource(r.getRobotName());
                    rQos.setSampleRate(INTERVAL);
                    rQos.setTarget(r.getRobotName());
                    rQos.setValue(1);
                    rQos.send();
                    rQos.close();
                } else {
                    logger.debug(r.getRobotName()+" is not pingable, setting reachability to 0");
                    NimQoS rQos = new NimQoS(ci, "99", "QOS_ROBOT_REACHABILITY", false);
                    if (runOnce) {
                        rQos.setDefinition("QOS_NETWORK", "Status of robot availability", "Status", "status");
                    }
                    rQos.setSource(r.getRobotName());
                    rQos.setSampleRate(INTERVAL);
                    rQos.setTarget(r.getRobotName());
                    rQos.setValue(0);
                    rQos.send();
                    rQos.close();
                }
            }
        }
        runOnce = false;
    }

}

package com.ca.uim.field;

import com.nimsoft.nimbus.*;

import java.util.ArrayList;

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
            // TODO - Determime robot availability from the local hub
            // TODO - Determine robot reachability from the local hub

            // Get list of robots for the local hub
            ArrayList<Robot> robotList = Utils.getRobots();
            // Verify the hdb, spooler and controller are in a good state

        } while (nimProbe.doForever());
    }

}

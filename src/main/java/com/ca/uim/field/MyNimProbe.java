package com.ca.uim.field;

import com.nimsoft.nimbus.*;

public class MyNimProbe extends NimProbe {
    private static final NimLog logger = NimLog.getLogger(MyNimProbe.class);

    public MyNimProbe(String name, String version, String company, String[] args) throws NimException {
        super(name, version, company, args);
    }

    // used to override protected method and allow for creation of NimSession before the doForever loop is invoked
    public void login() throws NimException {
        super.login();
    }

    public void stop(String reason) {
        logger.error("Exiting probe: " + reason);
        try {
            cbStop(new NullNimSession());
        } catch (NimException e) {
            logger.error("Failed to stop probe: " + e.toString());
        }
    }

    private static class NullNimSession extends NimSession {

        private NullNimSession() {
        }
        @Override
        public void sendReply(int status, PDS pdsdata) throws NimException {
            //Do nothing
        }
    }



}

package com.ca.uim.field;

import java.util.ArrayList;
import java.util.List;

public class Robot {
    private String robotName;
    private String robotAddress;
    private String hubAddress;
    private String hubName;
    private String origin;
    private String ipAddress;
    private String version;
    private boolean isActive;
    private List<String> probeList = new ArrayList<String>();

    public Robot(String origin, String robotName, String robotAddress, boolean isActive, String ipAddress, String version) {
        this.robotName = robotName;
        this.robotAddress = robotAddress;
        this.origin = origin;
        this.isActive = isActive;
        this.ipAddress = ipAddress;
        this.version = version;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getRobotAddress() {
        return robotAddress;
    }

    public void setRobotAddress(String robotAddress) {
        this.robotAddress = robotAddress;
    }

    public String getHubAddress() {
        return hubAddress;
    }

    public void setHubAddress(String hubAddress) {
        this.hubAddress = hubAddress;
    }

    public String getHubName() {
        return hubName;
    }

    public void setHubName(String hubName) {
        this.hubName = hubName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getProbeList() {
        return probeList;
    }

    public void setProbeList(List<String> probeList) {
        this.probeList = probeList;
    }
}

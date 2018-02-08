# UIM AVAILABILITY MONITOR
Monitor the availability of different CIs in CA UIM

Download the latest version here: [availability_monitor](https://github.com/BryanKMorrow/uim-availability_monitor/blob/master/releases/availability_monitor.zip)

### DESCRIPTION
The goal of this probe will be to hopefully improve the collection of availability and reachability for different CIs in the UIM environment. It currently only runs for the local hub, so it only detects and reports on its directly connected robots.

### PREREQUISITES
* You will need to insert the following two Metric Definitions into the database before using this probe. This can be done from the SLM portlet -> Tools -> SQL Query:
`insert into CM_CONFIGURATION_ITEM_METRIC_DEFINITION VALUES ('10.2:98', 'Robot Availability', 'state', '10.2', NULL);`
`insert into CM_CONFIGURATION_ITEM_METRIC_DEFINITION VALUES ('10.2:99', 'Robot Reachability', 'state', '10.2', NULL);`
* Needs to be deployed onto a hub


### CURRENT FEATURES
* Creates two QOS metrics to track robot availability -> QOS\_AVAILABILITY\_AVAILABILITY (QAA) and QOS\_REACHABILITY\_REACHABILITY
* The metric vales are either a 1 (online) or 0 (offline)
* Sends alarm if robot ip address is equal to 127.0.0.1
* Sends alarm if hdb or spooler is in an inactive state
* Current alarms are **NOT** automatically cleared

### TODO
* CABI Report
* Alarms automatically cleared
* Automated SLA/SLO Creation
* Robot List Management by Callback(s)
* HTML 5 Dashboard

### TECHNICAL DETAILS
1. Runs on every interval in seconds (setup->interval)
2. Runs **getrobots** on its controller probe via callback
3. Loops through each robot and does the following:
   1. Sends an alarm if the robot's configured IP address is 127.0.0.1
   2. Runs **probe_list** to return the list of probes running on the machine
   3. Sends an alarm if hdb and/or spooler are in a red state
      1. Sends QAA as 0 and QRR as 1
   4. If robot is active it sends QAA and QRR as 1
   5. If not active it will attempt to ping the ip address of the robot
      1. If pingable QAA is set to 0 and QRR is set to 1
      2. If not pingable, QAA is set to 0 and QRR is set to 0  
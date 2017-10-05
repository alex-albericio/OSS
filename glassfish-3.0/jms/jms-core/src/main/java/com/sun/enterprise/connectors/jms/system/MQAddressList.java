/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.connectors.jms.system;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.admin.util.AdminConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.api.Globals;

/**
 * Defines an MQ addressList.
 *
 * @author Binod P.G
 */
public class MQAddressList {

    static Logger logger = LogDomains.getLogger(MQAddressList.class,  LogDomains.RSR_LOGGER);
    private static String myName =
               System.getProperty(SystemPropertyConstants.SERVER_NAME);

    private List<MQUrl> urlList = new ArrayList<MQUrl>();

    private JmsService jmsService = null;
    //private AppserverClusterViewFromCacheRepository rep = null;
    private static String nodeAgentHost = null;
    private String targetName = null;

    /**
     * Create an empty address list
     */
    public MQAddressList() {
        this(null);
    }

    /**
     * Use the provided <code>JmsService</code> to create an addresslist
     */
    public MQAddressList(JmsService service) {
        //use the server instance this is being run in as the target
        this(service, getServerName());
    }

    /**
     * Creates an instance from jmsService and resolves
     * values using the provided target name
     * @param targetName Represents the target for which the addresslist
     * needs to be created
     * @param service <code>JmsService</code> instance.
     */
    public MQAddressList(JmsService service, String targetName) {
        logFine(" init" + service + "target " + targetName);
        this.jmsService = service;
        this.targetName = targetName;
    }

    /**
     * Sets up the addresslist.
     */
    public void setup() throws Exception {
        try {
            if (isClustered() && (!this.jmsService.getType().equals(ActiveJmsResourceAdapter.REMOTE)) ) {
                //setup for LOCAL/EMBEDDED clusters.
                logFine("setting up for cluster " +  this.targetName);
                setupClusterViewFromRepository();
                setupForCluster();
            } else {
                logFine("setting up for SI/DAS " + this.targetName);
                if (isAConfig(targetName) || isDAS(targetName)) {
                    logFine("performing default setup for DAS/remote clusters/PE instance" + targetName);
                    defaultSetup();
                } else {
                    logFine("configuring for Standalone EE server instance");
                    //resolve and add.
                    setupClusterViewFromRepository();
                    setupForStandaloneServerInstance();
                }
            }
        } catch (ConnectorRuntimeException ce) {
            throw new Exception(ce);
        }
    }

    private void setupClusterViewFromRepository() throws Exception {
        ServerContext context = Globals.get(ServerContext.class);//todo: ApplicationServer.getServerContext();
        Server server = context.getConfigBean();
        String domainurl = context.getServerConfigURL();
        //rep = new AppserverClusterViewFromCacheRepository(domainurl);
        try {
            nodeAgentHost = getNodeAgentHostName(server);
            logFine("na host" + nodeAgentHost);
        } catch (Exception e) {
            logger.log(Level.FINE,"Exception while attempting to get nodeagentHost", e.getMessage());
            logger.log(Level.FINER, e.getMessage(), e);
        }
    }
    public String getNodeAgentHostName(final Server as) throws Exception{
        Domain domain = Globals.get(Domain.class);
        NodeAgents nodeAgents = domain.getNodeAgents();
        List nodeAgentsList = nodeAgents.getNodeAgent();
        NodeAgent agent = null;
        for (int i =0; i < nodeAgentsList.size(); i++) {
            agent = (NodeAgent) nodeAgentsList.get(i);
            if (!(agent.getName().equals(as.getNodeAgentRef())))
            {
                 agent = null;
                continue;
         }
        }
        if (agent != null){
                JmxConnector connector = getNodeAgentSystemConnector(agent);
        //final NodeAgent na = NodeAgentHelper.getNodeAgentForServer(domainCC, as.getName());
        final boolean dasShookHandsWithNodeAgent = Boolean.parseBoolean(agent.getPropertyValue(AdminConstants.RENDEZVOUS_PROPERTY_NAME));//NodeAgentHelper.hasNodeAgentRendezvousd(domainCC, na);
        if (! dasShookHandsWithNodeAgent)
            throw new RuntimeException("Error: NA: " + agent.getName() + " has not rendezvous'ed with DAS");
         final String naHost = connector.getPropertyValue(AdminConstants.HOST_PROPERTY_NAME);//NodeAgentHelper.getNodeAgentSystemConnector(domainCC, na.getName()).getElementPropertyByName(IAdminConstants.HOST_PROPERTY_NAME).getValue();
         return ( naHost );
        }
        return null;
    }

     public JmxConnector getNodeAgentSystemConnector(NodeAgent controller)throws Exception
     {
        final String systemConnectorName = controller.getSystemJmxConnectorName();
        final JmxConnector connector = controller.getJmxConnector();
        if (connector == null) {
            throw new Exception("Node Agent controller.getName() does not have a system connector named systemConnectorName");
        }
        return connector;
    }

    public String getMasterBroker(String clustername) {
    String masterbrk = null;
    //if (rep != null) {
        try {
        JmsHost mb = getMasterJmsHostInCluster(clustername);
        JmsService js = getJmsServiceForMasterBroker
                                 (clustername);
        MQUrl url = createUrl(mb, js);
        masterbrk = url.toString();
        logger.log(Level.FINE, "Master broker obtained is "
               + masterbrk);
        }
        catch (Exception e) {
        logger.log(Level.SEVERE, "Cannot obtain master broker");
        logger.log(Level.SEVERE, e.getMessage(), e);
        }
    //}
    return masterbrk;
    }

      private JmsService getJmsServiceForMasterBroker(String clusterName)  {
         Domain domain = Globals.get(Domain.class);
         Clusters clusters = domain.getClusters();
         List clusterList = clusters.getCluster();
         Cluster cluster = null;
         for (int i =0; i < clusterList.size(); i++){
             if (clusterName.equals(((Cluster)clusterList.get(i)).getName()))
                    cluster =    (Cluster)clusterList.get(i);
         }

        //final String myCluster      = ClusterHelper.getClusterByName(domainCC, clusterName).getName();
	    final Server[] buddies      = this.getServersInCluster(cluster);//ServerHelper.getServersInCluster(domainCC, myCluster);
        final Config cfg =  getConfigForServer(buddies[0]);
        //final Config cfg             =  ServerHelper.getConfigForServer(domainCC, buddies[0].getName());
        return cfg.getJmsService();
	}

    private Config getConfigForServer(Server server){

         String cfgRef = server.getConfigRef();
         return getConfigByName(cfgRef);
    }
    private Config getConfigByName(String cfgRef){
         Domain domain = Globals.get(Domain.class);
         Configs configs = domain.getConfigs();
                List configList = configs.getConfig();
                for(int i=0; i < configList.size(); i++){
                    Config config = (Config)configList.get(i);
                    if (config.getName().equals(cfgRef))
                        return config;
        }
        return null;
    }
     private JmsHost getMasterJmsHostInCluster(String clusterName) throws Exception {
        //final String myCluster      = ClusterHelper.getClusterByName(domainCC, clusterName).getName();
        Domain domain = Globals.get(Domain.class);
        Clusters clusters = domain.getClusters();
        List clustersList = clusters.getCluster();
        Server[] buddies = null;
        for (int i =0; i < clustersList.size(); i++)
        {
            if(clusterName.equals(((Cluster)clustersList.get(i)).getName()))
                buddies = getServersInCluster((Cluster)clustersList.get(i));
        }
        //final Server[] buddies      = ServerHelper.getServersInCluster(domainCC, myCluster);
	    for (final Server as : buddies) {
	 	try {
             		final JmsHost copy	  = getResolvedJmsHost(as);
			// return the first valid host
			// there may be hosts attached to an NA that is down
             		return copy;
	 	} catch (Exception e) {
		// we dont add the host if we cannot get it
			;
	 	}
	}
	throw new RuntimeException("No JMS hosts available to select as Master");

        // final JmsHost copy   = getResolvedJmsHost(buddies[0]);
    }

    private Server[] getServersInCluster(Cluster cluster)
    {
        //first ensure that the cluster exists
        //Cluster cluster = ClusterHelper.getClusterByName(configContext, clusterName);

        //Now fetch the server instances in the cluster.
        List serverRefs = cluster.getServerRef();

        Server[] result = new Server[serverRefs.size()];
        Domain domain = Globals.get(Domain.class);

        for (int i = 0; i <  serverRefs.size(); i++) {
            result[i] = domain.getServerNamed(((ServerRef)serverRefs.get(i)).getRef());

          //  try {
            //} catch (ConfigException ex) {
              //  throw new ConfigException(_strMgr.getString("noSuchClusterInstance",
                //    clusterName, serverRefs[i].getRef()));
            //}
        }
        return result;
    }

    private boolean isDAS(String targetName)  {
        if (isAConfig(targetName)) {
            return false;
        }//todo: V3 need to fix this
        return  true;//ServerHelper.isDAS(getAdminConfigContext(), targetName);
    }

    private boolean isAConfig(String targetName)  {
        //return ServerHelper.isAConfig(getAdminConfigContext(), targetName);
        final Config config = getConfigByName(targetName);
        return (config != null ? true : false);
    }


    /**
     * Gets the admin config context associated with this server instance
     * Usage Notice: Use this only for operations that are performed in DAS
     * and requires the admin config context
     */
   /* private ConfigContext getAdminConfigContext() {
        return com.sun.enterprise.admin.server.core.AdminService.
                   getAdminService().getAdminContext().getAdminConfigContext();
    } */

    /**
     * Setup addresslist for Standalone server instance in EE
     */
    private void setupForStandaloneServerInstance() throws Exception {
        if (jmsService.getType().equals(ActiveJmsResourceAdapter.REMOTE)) {
            logFine("REMOTE Standalone server instance and hence use default setup");
            defaultSetup();
        } else {
            //For LOCAL or EMBEDDED standalone server instances, we need to resolve
            //the JMSHost
            logFine("LOCAL/EMBEDDED Standalone server instance");
            JmsHost host = getResolvedJmsHostForStandaloneServerInstance(this.targetName);
            MQUrl url = createUrl(host);
            urlList.add(url);
        }
    }

    /**
     * Default setup concatanates all JMSHosts in a JMSService to create the address list
     */
    private void defaultSetup() throws Exception {
        logFine("performing defaultsetup");
        JmsService jmsService = Globals.get(JmsService.class);
        List hosts = jmsService.getJmsHost();
        for (int i=0; i < hosts.size(); i++) {
            MQUrl url = createUrl((JmsHost)hosts.get(i));
            urlList.add(url);
        }
    }

    /**
     * Setup the address list after calculating the JMS hosts
     * belonging to the local appserver cluster members.
     * For LOCAL/EMBEDDED clusters the MQ broker corresponding
     * to "this" server instance needs to be placed ahead
     * of the other brokers of the other siblings in the AS
     * cluter to enable sticky connection balancing by MQ.
     */
    private void setupForCluster() throws Exception {
        java.util.Map<String,JmsHost> hostMap =
            getResolvedLocalJmsHostsInMyCluster(true);
        //First add my jms host.
        JmsHost jmsHost = hostMap.get(myName);
        MQUrl myUrl = createUrl(jmsHost, nodeAgentHost);
        urlList.add(myUrl);
        hostMap.remove(myName);

        // Add all buddies to URL.
        for (JmsHost host : hostMap.values() ) {
            MQUrl url = createUrl(host);
            urlList.add(url);
        }
    }

    public Map<String, JmsHost> getResolvedLocalJmsHostsInMyCluster(final boolean includeMe) {
        final Map<String, JmsHost> map = new HashMap<String, JmsHost> ();
         Cluster cluster = getClusterForServer( myName);
        if (cluster != null){
            final Server[] buddies = getServersInCluster(cluster);
            for (final Server as : buddies) {
                if (!includeMe && myName.equals(as.getName()))
                    continue;
		try {
                	final JmsHost copy      = getResolvedJmsHost(as);
                	map.put(as.getName(), copy);
		} catch (Exception e) {
			;
		}
            }
        }
        return ( map );
    }

    private Cluster getClusterForServer(String instanceName){
         Domain domain = Globals.get(Domain.class);
         Clusters clusters = domain.getClusters();
         List clusterList = clusters.getCluster();

         for (int i =0; i < clusterList.size(); i++){
             Cluster cluster = (Cluster)clusterList.get(i);
             if (isServerInCluster(cluster, instanceName))
                         return cluster;
         }
        return null;
    }

    private boolean isServerInCluster (Cluster cluster, String instanceName){
        List serverRef = cluster.getServerRef();
        for (int i=0; i < serverRef.size(); i++){
            if(instanceName.equals(serverRef.get(i)))
                return true;
        }
        return false;
    }
    /**
     * Creates a String representation of address list from
     * array list. In short, it is a comma separated list.
     * Actual syntax of an MQ url is inside MQUrl class.
     *
     * @return AddressList String
     * @see MQUrl
     */
    public String toString() {
        String s = "";

        Iterator it = urlList.iterator();
    if (it.hasNext()) {
            s = it.next().toString();
    }

        while (it.hasNext()) {
            s = s + "," +  it.next().toString();
        }

        logFine("toString returns :: " + s);
        return s;
    }

    /**
     * Creates an instance of MQUrl from JmsHost element in
     * the dtd and add it to the addresslist.
     *
     * @param host An instance of <code>JmsHost</code> object.
     */
    public void addMQUrl(JmsHost host) {
        MQUrl url = createUrl(host);
        urlList.add(url);
    }

    /**
     * Deletes the url represented by the JmsHost from the AddressList.
     *
     * @param host An instance of <code>JmsHost</code> object.
     */
    public void removeMQUrl(JmsHost host) {
        MQUrl url = createUrl(host);
        urlList.remove(url);
    }

    /**
     * Updates the information about the <code>JmsHost</code>
     * in the address list.
     *
     * @param host An instance of <code>JmsHost</code> object.
     */
    public void updateMQUrl(JmsHost host) {
        MQUrl url = createUrl(host);
        urlList.remove(url);
        urlList.add(url);
    }

    private MQUrl createUrl(JmsHost host) {
        return createUrl(host, this.jmsService);
    }

    private MQUrl createUrl(JmsHost host, String overridedHostName) {
        return createUrl(host, this.jmsService, overridedHostName);
    }

    public static MQUrl createUrl(JmsHost host, JmsService js) {
        return createUrl(host, js, null);
    }

    public static MQUrl createUrl(JmsHost host, JmsService js, String overridedHostName) {
        try {
        String name = host.getName();
        String hostName = host.getHost();
        // For LOCAL/EMBEDDED Clustered instances and
        // standalone server instances, use
        // their nodeagent's hostname as the jms host name.
        ServerContext serverContext = Globals.get(ServerContext.class);
        Server server = serverContext.getConfigBean();
        if (overridedHostName != null && !overridedHostName.trim().equals("")) {
           hostName = overridedHostName;
        }

        String port = host.getPort();
        MQUrl url = new MQUrl(name);
        url.setHost(hostName);
        url.setPort(port);
        if (js != null) {
            String scheme = js.getMqScheme();
            if (scheme != null && !scheme.trim().equals("")) {
                url.setScheme(scheme);
            }

            String service = js.getMqService();
            if (service != null && !service.trim().equals("")) {
                url.setService(service);
            }
        }
        return url;
        } catch (Exception ce) {
            ce.printStackTrace();
        }
        return null;
    }

     //Used to get resolved local JmsHost for a standalone server instance
    private JmsHost getResolvedJmsHostForStandaloneServerInstance(
                                         String serverName) throws Exception {
        logFine(" getresolved " + serverName);
       //ConfigContext con =  getAdminConfigContext();
       Server serverInstance = getServerByName(serverName);
       logFine("serverinstace " + serverInstance);
       JmsHost jmsHost = getResolvedJmsHost(serverInstance);
       return jmsHost;
    }

    private Server getServerByName(String serverName){
        Domain domain = Globals.get(Domain.class);
        Servers servers = domain.getServers();
        List serverList = servers.getServer();

        for (int i=0; i < serverList.size(); i++){
            Server server = (Server) serverList.get(i);
            if(serverName.equals(server.getName()))
                return server;
        }
           return null;
    }


    private JmsHost getResolvedJmsHost(Server as) throws Exception{
        logFine("getResolvedJmsHost " + as);
        //todo: Only required for cluster support. Commenting this out for now. 
        return null; //getResolvedJmsHost(as);
    }

    private boolean isClustered() throws ConnectorRuntimeException {
        Domain domain = Globals.get(Domain.class);
        Clusters clusters = domain.getClusters();
        if (clusters == null) return false;

        List clusterList = clusters.getCluster();

        return JmsRaUtil.isClustered(clusterList, myName);
    }

    private static String getServerName() {
        String serverName=System.getProperty(SystemPropertyConstants.SERVER_NAME);
        return serverName;
    }

    private void logFine(String s) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "MQAddressList :: " + s);
        }
    }

    public int getSize() {
        if (this.urlList != null) {
            return this.urlList.size();
        } else {
            return 0;
        }
    }
}

package org.alfresco.integrations.rumors.service;


import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


public interface RumorsService
{

    public boolean enableXMPPNode(NodeRef nodeRef);


    public boolean disableXMPPNode(NodeRef nodeRef);


    public boolean updateXMPPNode(NodeRef nodeRef, Map<QName, Serializable> properties);


    public boolean addUserToXMPPNodeRoster(NodeRef nodeRef);


    public boolean addUserToXMPPNodeRoster(NodeRef nodeRef, boolean recipricate);


    public boolean updateXMPPUserRosterName(NodeRef nodeRef);


    public boolean userExists(NodeRef nodeRef);


    public boolean deleteUser(NodeRef nodeRef);


    public void sendNotification(NodeRef nodeRef, String message);


    public void sendNotification(NodeRef nodeRef, String jid, String message);


    public void broadcast(NodeRef nodeRef, String message);


}

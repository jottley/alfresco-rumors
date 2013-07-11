
package org.alfresco.integrations.rumors.service;


import org.alfresco.service.cmr.repository.NodeRef;


public interface RumorsService
{

    public boolean enableXMPPNode(NodeRef nodeRef);


    public boolean disableXMPPNode(NodeRef nodeRef);


    public boolean userExists(NodeRef nodeRef);


    public boolean deleteUser(NodeRef nodeRef);


    public void sendNotification();


}

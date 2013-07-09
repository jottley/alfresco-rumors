
package org.alfresco.integrations.rumors.service;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.HashMap;

import org.alfresco.integrations.openfire.user.OpenFireUserService;
import org.alfresco.integrations.openfire.user.excpetions.UserAlreadyExistsException;
import org.alfresco.integrations.rumors.RumorsModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;


public class RumorsServiceImpl
    implements RumorsService
{

    OpenFireUserService openFireUserService;
    FileFolderService   fileFolderService;
    NodeService         nodeService;
    MetadataEncryptor   encryptor;


    public void setOpenFireUserService(OpenFireUserService openFireUserService)
    {
        this.openFireUserService = openFireUserService;
    }


    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }


    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    public void setMetadataEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }


    @Override
    public boolean makeXMPPNode(NodeRef nodeRef)
    {
        boolean added = false;
        String password = generatePassword();

        try
        {
            added = openFireUserService.addUser(nodeRef.getId(), password, fileFolderService.getFileInfo(nodeRef).getName(), null);

            if (added)
            {
                addRumorsAspect(nodeRef, password);
            }
        }
        catch (UserAlreadyExistsException e)
        {

        }

        return added;
    }


    public void addUserToXMPPNodeRoster()
    {

    }


    @Override
    public boolean userExists(NodeRef nodeRef)
    {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public boolean deleteUser(NodeRef nodeRef)
    {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void sendNotification()
    {
        // TODO Auto-generated method stub

    }


    private String generatePassword()
    {
        SecureRandom secureRandom = new SecureRandom();
        byte bytes[] = new byte[256];
        secureRandom.nextBytes(bytes);

        return String.valueOf(secureRandom.nextLong());
    }


    private void addRumorsAspect(NodeRef nodeRef, String password)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(RumorsModel.PROP_XMPP_NODE_PASSWORD, encryptor.encrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, password));

        nodeService.addAspect(nodeRef, RumorsModel.ASPECT_XMPP_NODE, properties);
    }

}


package org.alfresco.integrations.rumors.service;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.HashMap;

import org.alfresco.integrations.openfire.user.OpenFireUserService;
import org.alfresco.integrations.openfire.user.excpetions.UserAlreadyExistsException;
import org.alfresco.integrations.rumors.RumorsModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;


public class RumorsServiceImpl
    implements RumorsService
{

    OpenFireUserService openFireUserService;
    FileFolderService   fileFolderService;
    NodeService         nodeService;
    PersonService       personService;
    MetadataEncryptor   encryptor;

    private String      xmppServer;


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


    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }


    public void setMetadataEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }
    
    public void setXmppServer(String xmppServer)
    {
        this.xmppServer = xmppServer;
    }


    @Override
    public boolean enableXMPPNode(NodeRef nodeRef)
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


    public boolean disableXMPPNode(NodeRef nodeRef)
    {
        boolean disabled;

        disabled = openFireUserService.deleteUser(nodeRef.getId());

        if (disabled)
        {
            nodeService.removeAspect(nodeRef, RumorsModel.ASPECT_XMPP_NODE);
        }

        return disabled;
    }


    public boolean addUserToXMPPNodeRoster(NodeRef nodeRef)
    {
        return addUserToXMPPNodeRoster(nodeRef, false);
    }
    
    
    public boolean addUserToXMPPNodeRoster(NodeRef nodeRef, boolean recipricate)
    {
        boolean added;
        added = openFireUserService.addRoster(nodeRef.getId(), getUserJID(), AuthenticationUtil.getFullyAuthenticatedUser());
        
        if (recipricate)
        {
            String[] working = getUserJID().split("@");
            
            openFireUserService.addRoster(working[0], nodeRef.getId() + "@" + working[1], fileFolderService.getFileInfo(nodeRef).getName());
        }

        if (added)
        {
            nodeService.setProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_ROSTER, AuthenticationUtil.getFullyAuthenticatedUser());
        }

        return true;
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
    public void sendNotification(NodeRef nodeRef, String string)
    {
        Connection connection = new XMPPConnection(xmppServer);
        Chat chat;
        try
        {
            connection.connect();
            connection.login(nodeRef.getId(), (String)encryptor.decrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, nodeService.getProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_PASSWORD)));
            chat = connection.getChatManager().createChat(getUserJID(), new MessageListener()
            {

                public void processMessage(Chat chat, Message message)
                {

                }
            });
            chat.sendMessage(string);
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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


    private String getUserJID()
    {
        String jid = "";

        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

        if (nodeService.hasAspect(person, RumorsModel.ASPECT_XMPP_USER))
        {
            jid = (String)nodeService.getProperty(person, RumorsModel.PROP_XMPP_USER_USERNAME);
        }

        return jid;
    }

}

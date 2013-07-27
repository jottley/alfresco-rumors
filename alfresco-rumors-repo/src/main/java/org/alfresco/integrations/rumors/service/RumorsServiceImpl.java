
package org.alfresco.integrations.rumors.service;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.integrations.openfire.user.OpenFireUserService;
import org.alfresco.integrations.openfire.user.excpetions.UserAlreadyExistsException;
import org.alfresco.integrations.rumors.RumorsModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;


public class RumorsServiceImpl
    implements RumorsService
{

    private final static Log log = LogFactory.getLog(RumorsServiceImpl.class);

    OpenFireUserService      openFireUserService;
    FileFolderService        fileFolderService;
    NodeService              nodeService;
    PersonService            personService;
    MetadataEncryptor        encryptor;

    private String           xmppServer;


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


    public boolean updateXMPPNode(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        boolean update;

        update = openFireUserService.updateUser(nodeRef.getId(), (String)encryptor.decrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, properties.get(RumorsModel.PROP_XMPP_NODE_PASSWORD)), (String)properties.get(ContentModel.PROP_NAME), null);

        return update;
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
            nodeService.setProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_ROSTER, AuthenticationUtil.getFullyAuthenticatedUser()
                                                                                + ":" + getUserJID());
        }

        return true;
    }


    public boolean updateXMPPUserRosterName(NodeRef nodeRef)
    {
        boolean updated;

        String[] working = getUserJID().split("@");

        updated = openFireUserService.updateRoster(working[0], nodeRef.getId() + "@" + working[1], fileFolderService.getFileInfo(nodeRef).getName());

        return updated;
    }


    @Override
    public boolean userExists(NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean deleteUser(NodeRef nodeRef)
    {
        return openFireUserService.deleteUser(nodeRef.getId());
    }


    @Override
    public void sendNotification(NodeRef nodeRef, String message)
    {
        sendNotification(nodeRef, getUserJID(), message);
    }


    @Override
    public void sendNotification(NodeRef nodeRef, String jid, String message)
    {
        Connection connection = new XMPPConnection(xmppServer);
        Chat chat;
        try
        {
            connection.connect();
            connection.login(nodeRef.getId(), (String)encryptor.decrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, nodeService.getProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_PASSWORD)));
            chat = connection.getChatManager().createChat(jid, new MessageListener()
            {

                public void processMessage(Chat chat, Message message)
                {
                    // direct connections back to the thread will be ignored
                }
            });
            chat.sendMessage(message);
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            connection.disconnect();
        }
    }


    public void broadcast(NodeRef nodeRef, String message)
    {
        if (nodeService.hasAspect(nodeRef, RumorsModel.ASPECT_XMPP_NODE))
        {
            Map<String, String> roster = getRosterEntries(nodeRef);

            if (roster != null)
            {
                for (String user : roster.keySet())
                {
                    sendNotification(nodeRef, roster.get(user), message);
                }
            }
        }
    }


    public boolean isXMPPUser()
    {
        return StringUtils.isNotBlank(getUserJID()) ? true : false;
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
        properties.put(RumorsModel.PROP_XMPP_NODE_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());

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


    private Map<String, String> getRosterEntries(NodeRef nodeRef)
    {
        Map<String, String> rosterEntries = null;
        if (nodeService.hasAspect(nodeRef, RumorsModel.ASPECT_XMPP_NODE))
        {
            @SuppressWarnings("unchecked")
            Collection<String> roster = (Collection<String>)nodeService.getProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_ROSTER);

            if (!roster.isEmpty())
            {
                rosterEntries = new HashMap<String, String>();

                for (String entry : roster)
                {
                    String[] e = entry.split(":");
                    rosterEntries.put(e[0], e[1]);
                }
            }
        }

        return rosterEntries;
    }

}


package org.alfresco.integrations.rumors.service;


import java.io.File;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.integrations.openfire.user.OpenFireUserService;
import org.alfresco.integrations.openfire.user.excpetions.UserAlreadyExistsException;
import org.alfresco.integrations.rumors.RumorsModel;
import org.alfresco.integrations.rumors.client.XMPPClient;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.node.GetNodesWithAspectCannedQueryFactory;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;


public class RumorsServiceImpl
    implements RumorsService
{

    private final static Log             log     = LogFactory.getLog(RumorsServiceImpl.class);

    OpenFireUserService                  openFireUserService;
    FileFolderService                    fileFolderService;
    NodeService                          nodeService;
    PersonService                        personService;
    SearchService                        searchService;
    CommentService                       commentService;
    ContentService                       contentService;
    TransactionService                   transactionService;
    VersionService                       versionService;
    MetadataEncryptor                    encryptor;
    GetNodesWithAspectCannedQueryFactory nodesWithAspectCannedQueryFactory;

    private String                       xmppServer;

    private Map<String, XMPPClient>      clients = new HashMap<String, XMPPClient>(50);


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


    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }


    public void setCommentService(CommentService commentService)
    {
        this.commentService = commentService;
    }


    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }


    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }


    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }


    public void setMetadataEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }


    public void setNodesWithAspectCannedQueryFactory(GetNodesWithAspectCannedQueryFactory nodesWithAspectCannedQueryFactory)
    {
        this.nodesWithAspectCannedQueryFactory = nodesWithAspectCannedQueryFactory;
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
            if (clients.containsKey(nodeRef.toString()))
            {
                clients.get(nodeRef.toString()).disconnect();
                clients.remove(nodeRef.toString());
            }
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
    public void sendNotification(final NodeRef nodeRef, String jid, String message)
    {
        Chat chat = null;
        if (clients.containsKey(nodeRef.toString()))
        {
            ChatManager chatManager = clients.get(nodeRef.toString()).getChatManager();

            chat = chatManager.createChat(jid, new MessageListener()
            {
                public void processMessage(Chat chat, Message message)
                {
                    messageHandler(nodeRef, chat, message);
                }
            });
        }
        else
        {
            Connection connection = new XMPPConnection(xmppServer);
            try
            {
                connection.connect();
                connection.login(nodeRef.getId(), (String)encryptor.decrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, nodeService.getProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_PASSWORD)));
                connection.getChatManager().addChatListener(new RumorsServiceImpl.ChatListner(nodeRef));

                chat = connection.getChatManager().createChat(jid, nodeRef.toString(), new MessageListener()
                {
                    public void processMessage(Chat chat, Message message)
                    {
                        messageHandler(nodeRef, chat, message);
                    }
                });

                clients.put(nodeRef.toString(), new XMPPClient(connection, createFileTransferManager(connection, nodeRef)));
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            chat.sendMessage(message);
        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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


    public void startClients()
    {
        CannedQueryResults<NodeRef> cannedQueryResults = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<CannedQueryResults<NodeRef>>()
        {
            @Override
            public CannedQueryResults<NodeRef> doWork()
                throws Exception
            {
                return nodesWithAspectCannedQueryFactory.getCannedQuery(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, Collections.singleton(RumorsModel.ASPECT_XMPP_NODE), new PagingRequest(10)).execute();
            }
        });

        try
        {
            if (cannedQueryResults.getPageCount() > 0)
            {
                Iterator<List<NodeRef>> pageIterator = cannedQueryResults.getPages().iterator();
                while (pageIterator.hasNext())
                {
                    List<NodeRef> page = pageIterator.next();
                    for (NodeRef nodeRef : page)
                    {
                        clients.put(nodeRef.toString(), startClient(nodeRef));
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private XMPPClient startClient(final NodeRef nodeRef)
    {
        Connection connection = new XMPPConnection(xmppServer);
        FileTransferManager fileTransferManager = null;

        try
        {
            log.info("Starting XMPP Client for " + nodeRef.getId());
            connection.connect();
            connection.login(nodeRef.getId(), (String)encryptor.decrypt(RumorsModel.PROP_XMPP_NODE_PASSWORD, nodeService.getProperty(nodeRef, RumorsModel.PROP_XMPP_NODE_PASSWORD)));

            connection.getChatManager().addChatListener(new RumorsServiceImpl.ChatListner(nodeRef));

            fileTransferManager = createFileTransferManager(connection, nodeRef);

        }
        catch (XMPPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new XMPPClient(connection, fileTransferManager);
    }


    public void shutdownClients()
    {
        if (!clients.isEmpty())
        {
            for (XMPPClient xmppClient : clients.values())
            {
                xmppClient.disconnect();
            }
        }
    }


    private String getAlfrescoUser(final String jid)
    {
        String username = null;

        ResultSet resultSet = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<ResultSet>()
        {
            @Override
            public ResultSet doWork()
                throws Exception
            {
                ResultSet resultSet = null;

                try
                {
                    resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, "+@xmpp\\:username:\""
                                                                                                                             + jid.substring(0, jid.indexOf("/"))
                                                                                                                             + "\" AND +ASPECT:\"xmpp:user\"");
                }
                catch (LuceneQueryParserException e)
                {
                    log.info("Unable to execute getAlfrescoUser xmpp:user query: " + e.getMessage());
                }

                return resultSet;
            }
        });

        try
        {
            if (resultSet.length() > 0)
            {
                for (ResultSetRow resultSetRow : resultSet)
                {
                    NodeRef nodeRef = resultSetRow.getNodeRef();
                    PersonInfo personInfo = personService.getPerson(nodeRef);

                    username = personInfo.getUserName();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return username;
    }


    public class ChatListner
        implements ChatManagerListener
    {
        private NodeRef nodeRef;


        public ChatListner(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }


        @Override
        public void chatCreated(Chat chat, boolean createdLocally)
        {
            chat.addMessageListener(new MessageListener()
            {

                @Override
                public void processMessage(Chat chat, final Message message)
                {
                    messageHandler(nodeRef, chat, message);
                }
            });
        }
    }


    protected void messageHandler(final NodeRef nodeRef, final Chat chat, final Message message)
    {
        String username = getAlfrescoUser(message.getFrom());

        if (message.getBody() != null && username != null)
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                @Override
                public Object doWork()
                    throws Exception
                {
                    commentService.createComment(nodeRef, "XMPP Comment", message.getBody(), false);

                    return null;
                }
            }, username);

        }
    }


    private FileTransferManager createFileTransferManager(Connection connection, final NodeRef nodeRef)
    {
        FileTransferManager fileTransferManager = new FileTransferManager(connection);

        fileTransferManager.addFileTransferListener(new FileTransferListener()
        {

            @Override
            public void fileTransferRequest(final FileTransferRequest fileTransferRequest)
            {
                try
                {
                    String username = getAlfrescoUser(fileTransferRequest.getRequestor());

                    log.info(fileTransferRequest.getFileName());
                    final IncomingFileTransfer incomingFiletransfer = fileTransferRequest.accept();

                    final File incomingFile = new File(TempFileProvider.getTempDir().getPath() + File.separatorChar
                                                       + nodeRef.getId());
                    log.info(incomingFile.getAbsoluteFile());

                    incomingFiletransfer.recieveFile(incomingFile);

                    do
                    {
                        log.info("uploading file");
                        Thread.sleep(1000);
                    }
                    while (!incomingFiletransfer.isDone());

                    log.info(incomingFiletransfer.getStatus().toString());

                    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                    {

                        @Override
                        public Object doWork()
                            throws Exception
                        {
                            RetryingTransactionCallback<Object> txnWork = new RetryingTransactionCallback<Object>()
                            {
                                public Object execute()
                                    throws Exception
                                {
                                    log.info("About to add file");
                                    try
                                    {
                                        addVersionableAspect(nodeRef);
                                        
                                        ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                                        contentWriter.guessMimetype(fileTransferRequest.getFileName());
                                        contentWriter.guessEncoding();
                                        contentWriter.putContent(incomingFile);
                                        log.info("File updated");
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            };

                            transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false);
                            return null;
                        }
                    }, username);

                }
                catch (ContentIOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (XMPPException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        return fileTransferManager;
    }


    private void addVersionableAspect(NodeRef nodeRef)
    {
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            versionProperties.put(Version2Model.PROP_VERSION_TYPE, VersionType.MAJOR);

            nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION, true);
            nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);
        }

        log.debug("Version Node:" + nodeRef + "; Version Properties: " + versionProperties);
        versionService.createVersion(nodeRef, versionProperties);
    }


}

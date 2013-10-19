
package org.alfresco.integrations.xmpp.client;


import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;


public class XMPPClient
    implements Client
{
    private Connection          connection;
    private FileTransferManager fileTransferManager;


    public XMPPClient(Connection connection, FileTransferManager fileTransferManager)
    {
        this.connection = connection;
        this.fileTransferManager = fileTransferManager;
    }


    public void disconnect()
    {
        connection.disconnect();
    }


    public ChatManager getChatManager()
    {
        return connection.getChatManager();
    }


    public FileTransferManager getFileTransferManager()
    {
        return fileTransferManager;
    }


    private boolean activeClient()
    {
        return connection.isConnected();
    }

}

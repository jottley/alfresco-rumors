
package org.alfresco.integrations.xmpp;


import org.alfresco.service.namespace.QName;


public interface XMPPModel
{
    public static final String ORG_XMPP_MODEL_1_0_URI = "http://www.alfresco.org/model/xmpp/1.0";

    public static final QName  ASPECT_XMPP_NODE         = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "node");
    public static final QName  PROP_XMPP_NODE_PASSWORD  = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "password");
    public static final QName  PROP_XMPP_NODE_ROSTER    = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "roster");
    public static final QName  PROP_XMPP_NODE_OWNER     = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "owner");

    public static final QName  ASPECT_XMPP_USER         = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "user");
    public static final QName  PROP_XMPP_USER_USERNAME  = QName.createQName(ORG_XMPP_MODEL_1_0_URI, "username");
}


package org.alfresco.integrations.xmpp.jscript.app;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.integrations.xmpp.service.XMPPService;
import org.alfresco.repo.jscript.app.CustomResponse;


public class IsXMPPUser
    implements CustomResponse
{
    XMPPService xmppService;


    public void setXmppService(XMPPService xmppService)
    {
        this.xmppService = xmppService;
    }


    @Override
    public Serializable populate()
    {
        Map<String, Serializable> jsonObj = new LinkedHashMap<String, Serializable>();

        jsonObj.put("isXMPPUser", xmppService.isXMPPUser());

        return (Serializable)jsonObj;
    }

}

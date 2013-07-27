
package org.alfresco.integrations.rumors.jscript.app;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.integrations.rumors.service.RumorsService;
import org.alfresco.repo.jscript.app.CustomResponse;


public class IsXMPPUser
    implements CustomResponse
{
    RumorsService rumorsService;


    public void setRumorsService(RumorsService rumorsService)
    {
        this.rumorsService = rumorsService;
    }


    @Override
    public Serializable populate()
    {
        Map<String, Serializable> jsonObj = new LinkedHashMap<String, Serializable>();

        jsonObj.put("isXMPPUser", rumorsService.isXMPPUser());

        return (Serializable)jsonObj;
    }

}

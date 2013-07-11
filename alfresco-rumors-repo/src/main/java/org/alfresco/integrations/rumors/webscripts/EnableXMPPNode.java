
package org.alfresco.integrations.rumors.webscripts;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.integrations.rumors.service.RumorsService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class EnableXMPPNode
    extends DeclarativeWebScript
{
    private RumorsService rumorsService;

    private final String  JSON_KEY_NODEREF = "nodeRef";
    private final String  MODEL_SUCCESS    = "success";


    public void setRumorsService(RumorsService rumorsService)
    {
        this.rumorsService = rumorsService;
    }


    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, Serializable> content = parseContent(req);

        boolean created = rumorsService.makeXMPPNode((NodeRef)content.get(JSON_KEY_NODEREF));

        model.put(MODEL_SUCCESS, created);

        return model;
    }


    private Map<String, Serializable> parseContent(final WebScriptRequest req)
    {
        final Map<String, Serializable> result = new HashMap<String, Serializable>();

        Content content = req.getContent();
        String jsonString = null;
        JSONObject json = null;

        try
        {
            if (content != null)
            {
                jsonString = content.getContent();
                if (jsonString != null && jsonString.length() > 0)
                {
                    json = new JSONObject(jsonString);
                    if (json.has(JSON_KEY_NODEREF))
                    {
                        NodeRef nodeRef = new NodeRef(json.getString(JSON_KEY_NODEREF));
                        result.put(JSON_KEY_NODEREF, nodeRef);
                    }
                    else
                    {
                        throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Key " + JSON_KEY_NODEREF
                                                                                + " is missing from JSON: " + jsonString);
                    }
                }
                else
                {
                    throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "No content sent with request.");
                }
            }
            else
            {
                throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "No content sent with request.");
            }
        }
        catch (IOException e)
        {
            throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        catch (JSONException e)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Unable to parse JSON: " + jsonString);
        }


        return result;
    }
}

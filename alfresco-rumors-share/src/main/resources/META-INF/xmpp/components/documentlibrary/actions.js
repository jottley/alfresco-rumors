/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco
 * 
 * Alfresco is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Alfresco is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * XMPP Document Library actions. Defines XMPP JS actions for documents.
 * 
 * @author jottley
 */
(function()
{

    /*
     * YUI aliases
     */
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, KeyListener = YAHOO.util.KeyListener;

    /**
     * Enable a node as a XMPP Client Node
     * 
     * @method enableXMPPNodeAction
     * @param record
     *            {object} Object literal representing the file or folder on
     *            which the work should be performed
     */
    YAHOO.Bubbling
                    .fire("registerAction", {
                        actionName : "enableXMPPNodeAction",
                        fn : function dlA_enableXMPPNodeAction(record)
                        {

                            var me = this;

                            Alfresco.XMPP.showMessage({
                                text : "XMPP Enabling",
                                displayTime : 0,
                                showSpinner : true
                            });

                            var success = {
                                fn : function(response)
                                {
                                    var _success = {
                                        fn : function(response)
                                        {
                                            var __success = {
                                                fn : function(response)
                                                {
                                                    Alfresco.util.PopupManager
                                                                    .displayMessage({
                                                                        text : "Enabled"
                                                                    });
                                                    _pageRefresh(record.nodeRef);
                                                },
                                                scope : this
                                            }

                                            var __failure = {
                                                fn : function(response)
                                                {
                                                    Alfresco.util.PopupManager
                                                                    .displayMessage({
                                                                        text : "failure"
                                                                    });
                                                },
                                                scope : this
                                            }

                                            Alfresco.util.Ajax
                                                            .jsonRequest({
                                                                url : Alfresco.constants.PROXY_URI
                                                                                + 'xmpp/node/message',
                                                                method : 'POST',
                                                                dataObj : {
                                                                    nodeRef : record.nodeRef,
                                                                    message : 'You have XMPP enabled '
                                                                                    + record.jsNode.properties.title
                                                                },
                                                                requestContentType : Alfresco.util.Ajax.JSON,
                                                                successCallback : __success,
                                                                failureCallback : __failure
                                                            });
                                        },
                                        scope : this
                                    }

                                    var _failure = {
                                        fn : function(response)
                                        {
                                            Alfresco.util.PopupManager
                                                            .displayMessage({
                                                                text : "failure"
                                                            });
                                        },
                                        scope : this
                                    }

                                    Alfresco.util.Ajax
                                                    .jsonRequest({
                                                        url : Alfresco.constants.PROXY_URI
                                                                        + 'xmpp/node/roster/add',
                                                        method : 'POST',
                                                        dataObj : {
                                                            nodeRef : record.nodeRef,
                                                            recipricate : true
                                                        },
                                                        requestContentType : Alfresco.util.Ajax.JSON,
                                                        successCallback : _success,
                                                        failureCallback : _failure
                                                    });
                                },
                                scope : this
                            }

                            var failure = {
                                fn : function(response)
                                {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text : "failure"
                                    });
                                },
                                scope : this
                            }

                            Alfresco.util.Ajax.jsonRequest({
                                url : Alfresco.constants.PROXY_URI
                                                + 'xmpp/node/enable',
                                method : 'POST',
                                dataObj : {
                                    nodeRef : record.nodeRef
                                },
                                requestContentType : Alfresco.util.Ajax.JSON,
                                successCallback : success,
                                failureCallback : failure
                            });
                        }
                    })

    /**
     * Disable a node as a XMPP Client Node
     * 
     * @method diableXMPPNodeAction
     * @param record
     *            {object} Object literal representing the file or folder on
     *            which the work should be performed
     */
    YAHOO.Bubbling.fire("registerAction", {
        actionName : "disableXMPPNodeAction",
        fn : function dlA_disableXMPPNodeAction(record)
        {

            var me = this;

            Alfresco.XMPP.showMessage({
                text : "Disabling XMPP",
                displayTime : 0,
                showSpinner : true
            });

            var success = {
                fn : function(response)
                {
                    Alfresco.util.PopupManager.displayMessage({
                        text : "Disabled"
                    });
                    _pageRefresh(record.nodeRef);
                },
                scope : this
            }

            var failure = {
                fn : function(response)
                {
                    Alfresco.util.PopupManager.displayMessage({
                        text : "failure"
                    });
                },
                scope : this
            }

            Alfresco.util.Ajax.jsonRequest({
                url : Alfresco.constants.PROXY_URI + 'xmpp/node/disable',
                method : 'POST',
                dataObj : {
                    nodeRef : record.nodeRef
                },
                requestContentType : Alfresco.util.Ajax.JSON,
                successCallback : success,
                failureCallback : failure
            });
        }
    })

    /**
     * Displays the corresponding details page for the current node
     * 
     * @method _navigateForward
     * @private
     */
    function _pageRefresh(nodeRef)
    {
        /* Was the return page specified */
        var returnPath = Alfresco.util
                        .getQueryStringParameter("return", location.hash
                                        .replace("#", ""));
        if (returnPath) {
            // remove the 'file' querystring param, which causes a file to be
            // highlighted
            returnPath = returnPath.replace(/\?file=[^&#]*/, "");

            window.location.href = location.protocol + "//" + location.host
                            + Alfresco.constants.URL_PAGECONTEXT + returnPath;
        }
        /*
         * Did we come from the document library? If so, then direct the user
         * back there
         */
        else if (document.location.toString().match(/documentlibrary([#?]|$)/)
                        || document.location.toString()
                                        .match(/repository([#?]|$)/)) {
            /*
             * Send the user back to the last page - this could be either the
             * document list or document details page
             * 
             * We could use window.history.back(), but that does not trigger the
             * document actions and metadata to be reloaded
             */
            window.location.reload();
        }
        else {
            // go forward to the appropriate details page for the node
            window.location.href = Alfresco.util
                            .siteURL("document-details?nodeRef=" + nodeRef);
        }
    }
})();
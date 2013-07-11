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
 * Rumors Document Library actions. Defines Rumors JS actions for documents.
 * 
 * @author jottley
 */
(function() {
   
   /*
    * YUI aliases
    */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       KeyListener = YAHOO.util.KeyListener;

   /**
    * Make a node an XMPP Client Node
    *
    * @method onMakeXMPPNodeAction
    * @param record {object} Object literal representing the file or folder on which the work should be performed
    */
   YAHOO.Bubbling.fire("registerAction", {
      actionName : "enableXMPPNodeAction",
      fn : function dlA_enableXMPPNodeAction(record) {
         
         var me = this;
         
         Alfresco.Rumors.showMessage({
             text: "Enabling Rumors", 
             displayTime: 0,
             showSpinner: true
          });
         
         var success = {
        		 fn : function(response)
        		 {
        			 Alfresco.util.PopupManager
						.displayMessage( {
							text : "Enabled"
						});
        		 },
        		 scope: this
         }
         
         var failure = {
        		 fn : function(response)
        		 {
        			 Alfresco.util.PopupManager
						.displayMessage( {
							text : "failure"
						});
        		 },
        		 scope: this
         }
         
         Alfresco.util.Ajax.jsonRequest({
        	 url: Alfresco.constants.PROXY_URI + 'xmpp/node/enable',
        	 method: 'POST',
        	 dataObj: {
        		 nodeRef: record.nodeRef
        	 },
        	 requestContentType: Alfresco.util.Ajax.JSON,
        	 successCallback: success,
        	 failureCallback: failure
         });
      }
   })
})();
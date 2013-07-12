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
    * Enable a node as a XMPP Client Node
    *
    * @method enableXMPPNodeAction
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
        	         var _success = {
        	        		 fn : function(response)
        	        		 {
        	        			   var __success = {
        	          	        		 fn : function(response)
        	          	        		 {
        	          	        			 Alfresco.util.PopupManager
        	          							.displayMessage( {
        	          								text : "Enabled"
        	          							});
        	          	        		 },
        	          	        		 scope: this
        	          	         }
        	          	         
        	          	         var __failure = {
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
        	          	        	 url: Alfresco.constants.PROXY_URI + 'xmpp/node/message',
        	          	        	 method: 'POST',
        	          	        	 dataObj: {
        	          	        		 nodeRef: record.nodeRef,
        	          	        		 message: 'You have XMPP enabled ' + record.jsNode.properties.title
        	          	        	 },
        	          	        	 requestContentType: Alfresco.util.Ajax.JSON,
        	          	        	 successCallback: __success,
        	          	        	 failureCallback: __failure
        	          	         });
        	        		 },
        	        		 scope: this
        	         }
        	         
        	         var _failure = {
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
        	        	 url: Alfresco.constants.PROXY_URI + 'xmpp/node/roster/add',
        	        	 method: 'POST',
        	        	 dataObj: {
        	        		 nodeRef: record.nodeRef,
        	        		 recipricate: true
        	        	 },
        	        	 requestContentType: Alfresco.util.Ajax.JSON,
        	        	 successCallback: _success,
        	        	 failureCallback: _failure
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
   
   
   /**
    * Disable a node as a XMPP Client Node
    *
    * @method diableXMPPNodeAction
    * @param record {object} Object literal representing the file or folder on which the work should be performed
    */
   YAHOO.Bubbling.fire("registerAction", {
      actionName : "disableXMPPNodeAction",
      fn : function dlA_disableXMPPNodeAction(record) {
         
         var me = this;
         
         Alfresco.Rumors.showMessage({
             text: "Disabling Rumors", 
             displayTime: 0,
             showSpinner: true
          });
         
         var success = {
        		 fn : function(response)
        		 {
        			 Alfresco.util.PopupManager
						.displayMessage( {
							text : "Disabled"
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
        	 url: Alfresco.constants.PROXY_URI + 'xmpp/node/disable',
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
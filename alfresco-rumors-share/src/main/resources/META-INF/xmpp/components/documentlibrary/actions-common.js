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
 * XMPP utility functions
 * 
 * @author jottley
 * @author wabson
 */
(function() {
   
   /**
    * Salesforce namespace
    */
   Alfresco.XMPP = Alfresco.XMPP || {};
   
   /*
    * YUI aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /*
    * Static user-displayed message, timer and status
    */
   var userMessage = null, userMessageText = "";
   
   /**
    * Destroy the message displayed to the user
    * 
    * @method hideMessage
    * @static
    */
   Alfresco.XMPP.hideMessage = function RA_hideMessage()
   {
      if (userMessage)
      {
         if (userMessage.element)
         {
            userMessage.destroy();
         }
         userMessage = null;
         userMessageText = "";
      }
   };
   
   /**
    * Remove any existing popup message and show a new message
    * 
    * @method showMessage
    * @static
    * @param config {object} object literal containing success callback
    *          - text {String} The message text to display
    *          - displayTime {int} Display time in seconds. Defaults to zero, i.e. show forever
    *          - showSpinner {boolean} Whether to display the spinner image or not, default is true
    */
   Alfresco.XMPP.showMessage = function RA_showMessage(config)
   {
      if (userMessageText != config.text) // only update if text has changed
      {
         Alfresco.XMPP.hideMessage();
         var displayTime = (config.displayTime === null || typeof config.displayTime == "undefined") ? 0 : config.displayTime,
               showSpinner = (config.showSpinner === null || typeof config.showSpinner == "undefined") ? true : config.showSpinner;
         userMessage = Alfresco.util.PopupManager.displayMessage({
            displayTime: displayTime,
            text: showSpinner ? '<span class="wait">' + config.text + '</span>' : config.text,
            noEscape: true,
            modal: true
         });
         userMessageText = config.text;
      }
   };
})();
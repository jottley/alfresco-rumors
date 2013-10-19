
package org.alfresco.integrations.xmpp.webscripts;


import org.alfresco.integrations.xmpp.service.XMPPService;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.extensions.webscripts.DeclarativeWebScript;


public abstract class XMPPWebScript
    extends DeclarativeWebScript
    implements ApplicationContextAware
{
    protected final static String XMPP_SUBSYSTEM = "xmpp";
    protected final static String XMPPSERVICE    = "xmppService";

    protected ApplicationContext  applicationContext;


    abstract void setXmppService(XMPPService xmppService);


    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }


    protected void getXmppServiceSubsystem()
    {
        ApplicationContextFactory subsystem = (ApplicationContextFactory)applicationContext.getBean(XMPP_SUBSYSTEM);
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext)subsystem.getApplicationContext();
        setXmppService((XMPPService)childContext.getBean(XMPPSERVICE));
    }
}

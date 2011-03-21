package eu.wisebed.shibboauth.gwt.server;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uniluebeck.itm.tr.util.Logging;
import eu.wisebed.shibboauth.ShibbolethAuthenticator;
import eu.wisebed.shibboauth.gwt.client.GreetingService;
import eu.wisebed.shibboauth.gwt.shared.FieldVerifier;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
    private static String url = "https://gridlab23.unibe.ch/portal/SNA/secretUserKey";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GreetingServiceImpl.class);

    static {
        Logging.setLoggingDefaults();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    public String authenticate(String username, String password) throws IllegalArgumentException {
        // Verify that the input is valid.
        if (!FieldVerifier.isValidName(username)) {
            // If the input is not valid, throw an IllegalArgumentException back
            // to
            // the client.
            throw new IllegalArgumentException("Name must be at least 4 characters long");
        }

        boolean authenticated = false;
        String resultString = "";

        ShibbolethAuthenticator sa = null;
        try {
            sa = new ShibbolethAuthenticator();
            sa.setUrl(url);
            sa.setUsernameAtIdpDomain(username);
            sa.setPassword(password);
            sa.authenticate();
            authenticated = sa.isAuthenticated();
            if (authenticated)
                resultString = sa.getAuthenticationPageContent();
        } catch (Exception e) {
            log.error("error while authenticating: " + e, e);
        }

        // String serverInfo = getServletContext().getServerInfo();
        // String userAgent = getThreadLocalRequest().getHeader("User-Agent");
        return "Hello " + username + ", authentication " + (authenticated ? "was successfull" : "failed")
                + "<br/><br/>" + resultString;
    }

    @Override
    public Collection<String> getIdpList() {
        ShibbolethAuthenticator sa = null;
        Collection<String> l = new LinkedList<String>();
        log.debug("Trying to obtain a list of IDPs");
        try {
            sa = new ShibbolethAuthenticator();
            sa.setUrl(url);
            for (URL u : sa.getIDPs())
                l.add(u.getHost());
        } catch (Exception e) {
            log.debug("Unable to fetch list of IDPs: " + e, e);
        }
        log.debug("Returning " + l.size() + " IDPs");
        return l;
    }
}

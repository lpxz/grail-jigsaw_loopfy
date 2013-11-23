package org.w3c.www.protocol.http.auth;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.w3c.tools.codec.Base64Encoder;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpChallenge;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

class UserField extends TextField {

    PasswordPrompter prompter = null;

    public boolean keyDown(Event evt, int key) {
        if (key == '\t') {
            prompter.focusPassword();
            return true;
        }
        return super.keyDown(evt, key);
    }

    UserField(PasswordPrompter prompter, String txt, int len) {
        super(txt, len);
        this.prompter = prompter;
    }
}

class PasswordField extends TextField {

    PasswordPrompter prompter = null;

    public boolean keyDown(Event evt, int key) {
        if ((key == '\n') || (key == '\r')) {
            prompter.done(PasswordPrompter.EVT_OK);
            return true;
        }
        return super.keyDown(evt, key);
    }

    PasswordField(PasswordPrompter prompter, String txt, int len) {
        super(txt, len);
        setEchoCharacter('*');
        this.prompter = prompter;
    }
}

class PasswordPrompter extends Panel {

    TextField txtUser = null;

    TextField txtPassword = null;

    Button butOk = null;

    Button butCancel = null;

    String user = null;

    String password = null;

    static final int EVT_OK = 1;

    static final int EVT_CANCEL = 2;

    int evt = -1;

    protected synchronized boolean waitForCompletion() {
while (true) { 
{
            edu.hkust.clap.monitor.Monitor.loopBegin(653);
while (evt < 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(653);
{
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(653);

            switch(evt) {
                case EVT_OK:
                    return true;
                case EVT_CANCEL:
                    return false;
            }
        }} 
    }

    protected synchronized void done(int evt) {
        this.evt = evt;
        notifyAll();
    }

    protected void focusPassword() {
        txtPassword.requestFocus();
    }

    public boolean action(Event evt, Object what) {
        int e = -1;
        if (evt.target == butOk) {
            e = EVT_OK;
        } else if (evt.target == butCancel) {
            e = EVT_CANCEL;
        } else {
            return super.action(evt, what);
        }
        done(e);
        return true;
    }

    /**
     * Get the entered user name.
     * @return The user name as a String.
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the entered password.
     * @return The password as a String.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Run the dialog, as if modal.
     * @return A boolean <strong>false</strong> if interaction was canceled,
     * <strong>true</strong> otherwise.
     */
    public boolean prompt() {
        Frame toplevel = new Frame("Authentication Required");
        toplevel.add("Center", this);
        toplevel.pack();
        toplevel.show();
        txtUser.requestFocus();
        boolean result = waitForCompletion();
        this.user = txtUser.getText();
        this.password = txtPassword.getText();
        toplevel.hide();
        toplevel.dispose();
        return result;
    }

    PasswordPrompter(Request request, Reply reply) {
        super();
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        HttpChallenge challenge = (request.hasProxy() ? reply.getProxyAuthenticate() : reply.getWWWAuthenticate());
        Label label = new Label(challenge.getScheme() + " authentication for " + challenge.getAuthParameter("realm"));
        GridBagConstraints row = new GridBagConstraints();
        row.gridwidth = GridBagConstraints.REMAINDER;
        row.anchor = GridBagConstraints.WEST;
        gb.setConstraints(label, row);
        add(label);
        GridBagConstraints ct = new GridBagConstraints();
        ct.gridx = GridBagConstraints.RELATIVE;
        ct.anchor = GridBagConstraints.EAST;
        ct.weighty = 1.0;
        GridBagConstraints cv = new GridBagConstraints();
        cv.gridx = GridBagConstraints.RELATIVE;
        cv.gridwidth = GridBagConstraints.REMAINDER;
        cv.fill = GridBagConstraints.HORIZONTAL;
        cv.anchor = GridBagConstraints.WEST;
        cv.weightx = 1.0;
        cv.weighty = 1.0;
        label = new Label("User:", Label.LEFT);
        gb.setConstraints(label, ct);
        add(label);
        txtUser = new UserField(this, "", 32);
        gb.setConstraints(txtUser, cv);
        add(txtUser);
        label = new Label("Password:", Label.LEFT);
        gb.setConstraints(label, ct);
        add(label);
        txtPassword = new PasswordField(this, "", 32);
        gb.setConstraints(txtPassword, cv);
        add(txtPassword);
        butOk = new Button("Ok");
        row.anchor = GridBagConstraints.EAST;
        row.weightx = 1.0;
        row.gridwidth = GridBagConstraints.RELATIVE;
        gb.setConstraints(butOk, row);
        add(butOk);
        butCancel = new Button("Cancel");
        row.anchor = GridBagConstraints.WEST;
        row.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(butCancel, row);
        add(butCancel);
    }
}

class CachedRealm {

    public String realm = null;

    public HttpCredential credentials = null;

    CachedRealm(String realm, HttpCredential credentials) {
        this.realm = realm;
        this.credentials = credentials;
    }
}

public class AuthFilter implements PropRequestFilter {

    /**
     * The per-server realms we know about.
     */
    protected static Hashtable realms = new Hashtable(13);

    /**
     * the HttpManager that installed us.
     */
    protected HttpManager manager = null;

    protected static void registerRealm(Request request, Reply reply, HttpCredential credentials) {
        if (lookupRealm(request, reply) != null) return;
        String srvkey = request.getManager().getServerKey(request);
        String realm = ((request.hasProxy() ? reply.getProxyAuthenticate() : reply.getWWWAuthenticate()).getAuthParameter("realm"));
        CachedRealm cache[] = (CachedRealm[]) realms.get(srvkey);
        if (cache == null) {
            cache = new CachedRealm[1];
            cache[0] = new CachedRealm(realm, credentials);
        } else {
            CachedRealm nc[] = new CachedRealm[cache.length + 1];
            System.arraycopy(cache, 0, nc, 0, cache.length);
            nc[cache.length] = new CachedRealm(realm, credentials);
            cache = nc;
        }
        realms.put(srvkey, cache);
    }

    protected static HttpCredential lookupRealm(Request request, Reply reply) {
        String srvkey = request.getManager().getServerKey(request);
        CachedRealm cache[] = (CachedRealm[]) realms.get(srvkey);
        if (cache == null) return null;
        HttpChallenge challenge = (request.hasProxy() ? reply.getProxyAuthenticate() : reply.getWWWAuthenticate());
        String realm = challenge.getAuthParameter("realm");
        edu.hkust.clap.monitor.Monitor.loopBegin(654);
for (int i = 0; i < cache.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(654);
{
            if (cache[i].realm.equalsIgnoreCase(realm)) return cache[i].credentials;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(654);

        return null;
    }

    /**
     * PropRequestFilter implementation - Initialize the filter.
     * Time to register ourself to the HttpManager.
     * @param manager The HTTP manager that is initializing ourself.
     */
    public void initialize(HttpManager manager) {
        this.manager = manager;
        manager.setFilter(this);
        manager.setAllowUserInteraction(true);
    }

    /**
     * This filter doesn't handle exceptions.
     * @param request The request that triggered the exception.
     * @param ex The triggered exception.
     * @return Always <strong>false</strong>.
     */
    public boolean exceptionFilter(Request request, HttpException ex) {
        return false;
    }

    /**
     * On the way out, we let the request fly through.
     * @param request The request about to be emitted.
     */
    public Reply ingoingFilter(Request request) {
        return null;
    }

    /**
     * Catch any authentication requirement, and fullfill it with user's help.
     * This method trap all request for authentication, and pops up a
     * dialog prompting for the user's name and password.
     * <p>It then retries the request with the provided authentication
     * informations.
     * @param request The request that requires authentication.
     * @param reply The original reply.
     * @exception HttpException If some HTTP error occurs.
     */
    public Reply outgoingFilter(Request request, Reply reply) throws HttpException {
        if ((reply.getStatus() != HTTP.UNAUTHORIZED) && (reply.getStatus() != HTTP.PROXY_AUTH_REQUIRED)) return null;
        HttpCredential credentials = null;
        if ((credentials = lookupRealm(request, reply)) == null) {
            if (!request.getAllowUserInteraction()) return null;
            PasswordPrompter prompter = new PasswordPrompter(request, reply);
            if (!prompter.prompt()) return null;
            String user = prompter.getUser();
            String password = prompter.getPassword();
            credentials = HttpFactory.makeCredential("Basic");
            Base64Encoder encoder = new Base64Encoder(user + ":" + password);
            credentials.setAuthParameter("cookie", encoder.processString());
        }
        if (request.hasProxy()) request.setProxyAuthorization(credentials); else request.setAuthorization(credentials);
        Reply retry = request.getManager().runRequest(request);
        if (retry.getStatus() / 100 != 4) {
            registerRealm(request, reply, credentials);
            if (request.hasProxy()) {
                LocalAuthFilter.installProxyAuth(manager, credentials);
            } else {
                LocalAuthFilter.installLocalAuth(manager, request.getURL(), credentials);
            }
            try {
                InputStream in = reply.getInputStream();
                if (in != null) in.close();
            } catch (IOException ex) {
            }
            return retry;
        } else {
            return null;
        }
    }

    /**
     * We don't maintain cached informations.
     */
    public void sync() {
    }
}

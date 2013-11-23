package org.w3c.jigsaw.servlet;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;
import org.w3c.jigsaw.http.httpd;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 * @deprecated since jsdk2.1
 */
public class JigsawHttpSessionContext implements HttpSessionContext, PropertyMonitoring {

    class SessionSweeper extends Thread {

        int delay;

        JigsawHttpSessionContext ctxt = null;

        public void run() {
            edu.hkust.clap.monitor.Monitor.loopBegin(587);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(587);
{
                try {
                    sleep(delay);
                } catch (InterruptedException ex) {
                }
                ctxt.sweepSession();
            }} 



        }

        void setDelay(int delay) {
            this.delay = delay;
        }

        SessionSweeper(JigsawHttpSessionContext ctxt, int delay) {
            super("SessionSweeper");
            this.delay = delay;
            this.ctxt = ctxt;
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);
            this.start();
        }
    }

    private Hashtable sessions;

    private int sessionNb = 0;

    private int sessionCount = 0;

    private int maxsession;

    private long maxidle;

    private JigsawHttpSession oldestIdleSession = null;

    private SessionSweeper sweeper = null;

    private ServletProps props = null;

    /**
     * PropertyMonitoring implementation.
     */
    public boolean propertyChanged(String name) {
        if (name.equals(ServletProps.SERVLET_SESSION_IDLE)) {
            this.maxidle = props.getSessionsMaxIdleTime();
        } else if (name.equals(ServletProps.SERVLET_MAX_SESSION)) {
            this.maxsession = props.getMaxSessionsNumber();
        } else if (name.equals(ServletProps.SERVLET_SESSION_SWEEP)) {
            sweeper.setDelay(props.getSessionsSweepDelay());
        }
        return true;
    }

    /**
     * Remove sessions with idle time > max idle time
     */
    protected synchronized void sweepSession() {
        long now = System.currentTimeMillis();
        Enumeration e = sessions.keys();
        oldestIdleSession = null;
        edu.hkust.clap.monitor.Monitor.loopBegin(588);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(588);
{
            JigsawHttpSession session = (JigsawHttpSession) sessions.get(e.nextElement());
            long interval = now - session.getLastAccessedTime();
            int maxinactiveinterval = session.getMaxInactiveInterval() * 1000;
            long maxinterval = (maxinactiveinterval > 0) ? maxinactiveinterval : maxidle;
            if (interval > maxinterval) session.invalidate(); else {
                if (oldestIdleSession != null) {
                    if (oldestIdleSession.getLastAccessedTime() > session.getLastAccessedTime()) oldestIdleSession = session;
                } else {
                    oldestIdleSession = session;
                }
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(588);

    }

    protected synchronized void removeOldestIdleSession() {
        if (oldestIdleSession != null) {
            oldestIdleSession.invalidate();
            oldestIdleSession = null;
        }
    }

    /**
     * Returns an enumeration of all of the session IDs in this context. 
     * @return an enumeration of all session IDs in this context.
     * @deprecated since jsdk2.1
     */
    public Enumeration getIds() {
        return sessions.keys();
    }

    /**
     * Returns the session bound to the specified session ID.
     * @param sessionID - the ID of a particular session object 
     * @return the session. Returns null if the session ID does 
     * not refer to a valid session. 
     * @deprecated since jsdk2.1
     */
    public HttpSession getSession(String sessionId) {
        return (HttpSession) sessions.get(sessionId);
    }

    /**
     * Add a session in this context.
     * @param session - The JigsawHttpSession to add.
     * @return The session ID.
     */
    protected synchronized String addSession(JigsawHttpSession session) {
        if (sessionCount >= maxsession) removeOldestIdleSession();
        String id = getNewSessionId();
        sessions.put(id, session);
        sessionCount++;
        return id;
    }

    /**
     * Remove a session of this session context.
     * @param id - The session ID.
     */
    protected void removeSession(String id) {
        sessions.remove(id);
        sessionCount--;
    }

    private String getNewSessionId() {
        return "J" + String.valueOf(new Date().hashCode()) + "-" + (sessionNb++);
    }

    public JigsawHttpSessionContext(httpd server, ServletProps props) {
        this.props = props;
        this.sessions = new Hashtable(3);
        this.maxidle = props.getSessionsMaxIdleTime();
        this.maxsession = props.getMaxSessionsNumber();
        this.sweeper = new SessionSweeper(this, props.getSessionsSweepDelay());
        server.getProperties().registerObserver(this);
    }
}

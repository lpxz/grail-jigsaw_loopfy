package org.w3c.tools.jdbc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This subclass of java.beans.PropertyChangeSupport is identical
 * in functionality except that it fire a PropertyChangeEvent even if the
 * old value and the new value are identicals.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class JdbcPropertyChangeSupport extends PropertyChangeSupport {

    /**
     * Our listeners
     */
    private Vector listeners = null;

    /**
     * Our listeners for specific properties
     */
    private Hashtable children = null;

    /**
     * Our Bean
     */
    private Object source = null;

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * @param listener  The PropertyChangeListener to be added
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.addElement(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered
     * for all properties.
     * @param listener  The PropertyChangeListener to be removed
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.  The listener
     * will be invoked only when a call on firePropertyChange names that
     * specific property.
     * @param propertyName  The name of the property to listen on.
     * @param listener  The PropertyChangeListener to be added
     */
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (children == null) {
            children = new Hashtable();
        }
        JdbcPropertyChangeSupport child = (JdbcPropertyChangeSupport) children.get(propertyName);
        if (child == null) {
            child = new JdbcPropertyChangeSupport(source);
            children.put(propertyName, child);
        }
        child.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     * @param propertyName  The name of the property that was listened on.
     * @param listener  The PropertyChangeListener to be removed
     */
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (children == null) {
            return;
        }
        JdbcPropertyChangeSupport child = (JdbcPropertyChangeSupport) children.get(propertyName);
        if (child == null) {
            return;
        }
        child.removePropertyChangeListener(listener);
    }

    /**
     * Report a bound property update to any registered listeners.
     * No event is fired if old and new are equal and non-null.
     * @param propertyName  The programmatic name of the property
     *		that was changed.
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        Vector targets = null;
        JdbcPropertyChangeSupport child = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (Vector) listeners.clone();
            }
            if (children != null && propertyName != null) {
                child = (JdbcPropertyChangeSupport) children.get(propertyName);
            }
        }
        PropertyChangeEvent evt = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        if (targets != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(260);
for (int i = 0; i < targets.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(260);
{
                PropertyChangeListener target = (PropertyChangeListener) targets.elementAt(i);
                target.propertyChange(evt);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(260);

        }
        if (child != null) {
            child.firePropertyChange(evt);
        }
    }

    /**
     * Report an int bound property update to any registered listeners.
     * No event is fired if old and new are equal and non-null.
     * <p>
     * This is merely a convenience wrapper around the more general
     * firePropertyChange method that takes Object values.
     * @param propertyName  The programmatic name of the property
     *		that was changed.
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        firePropertyChange(propertyName, new Integer(oldValue), new Integer(newValue));
    }

    /**
     * Report a boolean bound property update to any registered listeners.
     * No event is fired if old and new are equal and non-null.
     * <p>
     * This is merely a convenience wrapper around the more general
     * firePropertyChange method that takes Object values.
     * @param propertyName  The programmatic name of the property
     *		that was changed.
     * @param oldValue  The old value of the property.
     * @param newValue  The new value of the property.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        firePropertyChange(propertyName, new Boolean(oldValue), new Boolean(newValue));
    }

    /**
     * Fire an existing PropertyChangeEvent to any registered listeners.
     * No event is fired if the given event's old and new values are
     * equal and non-null.
     * @param evt  The PropertyChangeEvent object.
     */
    public void firePropertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Vector targets = null;
        JdbcPropertyChangeSupport child = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (Vector) listeners.clone();
            }
            if (children != null && propertyName != null) {
                child = (JdbcPropertyChangeSupport) children.get(propertyName);
            }
        }
        if (targets != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(260);
for (int i = 0; i < targets.size(); i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(260);
{
                PropertyChangeListener target = (PropertyChangeListener) targets.elementAt(i);
                target.propertyChange(evt);
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(260);

        }
        if (child != null) {
            child.firePropertyChange(evt);
        }
    }

    /**
     * Check if there are any listeners for a specific property.
     * @param propertyName  the property name.
     * @return true if there are ore or more listeners for the given property
     */
    public synchronized boolean hasListeners(String propertyName) {
        if (listeners != null && !listeners.isEmpty()) {
            return true;
        }
        if (children != null) {
            JdbcPropertyChangeSupport child = (JdbcPropertyChangeSupport) children.get(propertyName);
            if (child != null && child.listeners != null) {
                return !child.listeners.isEmpty();
            }
        }
        return false;
    }

    /**
     * Constructor
     * @param sourceBean our bean
     */
    public JdbcPropertyChangeSupport(Object sourceBean) {
        super(sourceBean);
        this.source = sourceBean;
    }
}

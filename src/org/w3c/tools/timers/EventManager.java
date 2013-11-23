package org.w3c.tools.timers;

import java.util.Vector;

class Event {

    /** Absolute time, in ms, to deliver this event. */
    long time;

    /** Piece of data to pass to the event handler. */
    Object data;

    /** handler for this event */
    EventHandler handler;

    Event(long time, EventHandler handler, Object data) {
        this.time = time;
        this.handler = handler;
        this.data = data;
    }
}

/** This implements an event manager for timer events.  Timer events
      are a way to have events occur some time in the future.  They are an
      alternative to using separate threads which issue sleep requests
      themselves. */
public class EventManager extends Thread implements EventHandler {

    static final boolean debug = false;

    /** Vector of events, sorted by time. */
    Vector queue = new Vector();

    public EventManager() {
        setName("Event Manager");
    }

    /** registerTimer inserts a new timer event into the queue.  The
	  queue is always sorted by time, in increasing order.  That is,
	  things farther into the future are further down in the queue.

	  ms is milliseconds in the future, handler is the object that
	  will handle the event, and data is a "rock" that is passed to
	  the handler to do with what it will.

	  This returns an opaque object which can be used to recall the
	  timer before it is delivered. */
    public Object registerTimer(long ms, EventHandler handler, Object data) {
        long time = ms + System.currentTimeMillis();
        Event event = new Event(time, handler, data);
        return registerTimer(event);
    }

    boolean done = false;

    public synchronized void stopEventManager() {
        done = true;
        notify();
    }

    /** This is like the above registerTimer, except it takes an event
	  object with the deliver time filled in.  If deliver time is
	  before the current time, the event is "immediately" delivered.
	  Do a binary search to figure out where the event goes. */
    public synchronized Object registerTimer(Event newEvent) {
        int lo = 0;
        int hi = queue.size();
        long newTime = newEvent.time;
        Event e;
        long midTime;
        if (done) {
            return null;
        }
        if (hi == 0) {
            queue.addElement(newEvent);
        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(377);
while (hi - lo > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(377);
{
                int mid = (hi + lo) >> 1;
                e = (Event) queue.elementAt(mid);
                midTime = e.time;
                if (midTime < newTime) {
                    lo = mid + 1;
                } else if (midTime > newTime) {
                    hi = mid;
                } else {
                    lo = mid;
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(377);

            if (lo < hi && ((Event) queue.elementAt(lo)).time > newTime) {
                lo += 1;
            }
            queue.insertElementAt(newEvent, lo);
        }
        if (debug) {
            checkQueue();
        }
        notify();
        return newEvent;
    }

    /** This recalls a previously registered timer event. */
    public synchronized Object recallTimer(Object timer) {
        int lo = 0;
        int hi = queue.size();
        int limit = hi;
        long destTime = ((Event) timer).time;
        Event e;
        long midTime;
        if (hi == 0) {
            return null;
        }
        edu.hkust.clap.monitor.Monitor.loopBegin(378);
while (hi - lo > 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(378);
{
            int mid = (hi + lo) >> 1;
            e = (Event) queue.elementAt(mid);
            midTime = e.time;
            if (midTime < destTime) {
                lo = mid + 1;
            } else if (midTime > destTime) {
                hi = mid;
            } else {
                lo = mid;
                edu.hkust.clap.monitor.Monitor.loopBegin(379);
for (int i = mid - 1; i >= 0; i--) { 
edu.hkust.clap.monitor.Monitor.loopInc(379);
{
                    e = (Event) queue.elementAt(i);
                    if (e.time == midTime) {
                        lo = i;
                    } else {
                        break;
                    }
                }} 
edu.hkust.clap.monitor.Monitor.loopEnd(379);

                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(378);

        edu.hkust.clap.monitor.Monitor.loopBegin(380);
while (lo < limit) { 
edu.hkust.clap.monitor.Monitor.loopInc(380);
{
            e = (Event) queue.elementAt(lo);
            if (e.time == destTime) {
                if (e == timer) {
                    queue.removeElementAt(lo);
                    break;
                } else {
                    lo += 1;
                }
            } else {
                return null;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(380);

        if (debug) {
            checkQueue();
        }
        if (lo == 0) notify();
        return timer;
    }

    /** This is a separate method so that it can be
	   synchronized while the actual execution of the event
	   does not lock the EventManager.  In other words, while
	   one event is being processed by its handler, others can
	   register or recall other events. */
    synchronized Event getNextEvent() {
while (true) { 
{
            edu.hkust.clap.monitor.Monitor.loopBegin(382);
while (queue.size() == 0) { 
edu.hkust.clap.monitor.Monitor.loopInc(382);
{
                if (debug) {
                    System.out.println("Queue waiting for event.");
                }
                if (done) {
                    return null;
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(382);

            Event e = (Event) queue.elementAt(0);
            long now = System.currentTimeMillis();
            long dt;
            dt = e.time - now;
            if (dt <= 0) {
                queue.removeElementAt(0);
                return e;
            }
            if (debug) {
                System.out.println("Queue sleeping for " + dt + "ms");
            }
            try {
                wait(dt);
            } catch (InterruptedException ex) {
            }
        }} 

    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);
        edu.hkust.clap.monitor.Monitor.loopBegin(383);
while (true) { 
edu.hkust.clap.monitor.Monitor.loopInc(383);
{
            Event e = getNextEvent();
            if (done) {
                break;
            }
            try {
                e.handler.handleTimerEvent(e.data, e.time);
            } catch (Exception ex) {
                System.out.println("Exception " + ex + " in EventManager");
                ex.printStackTrace();
            } catch (Error er) {
                System.out.println("Error " + er + " in EventManager");
                er.printStackTrace();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(383);

    }

    public void handleTimerEvent(Object rock, long time) {
        long dt = (System.currentTimeMillis() - time);
        System.out.println("Handling event with dt=" + dt);
    }

    public static void main(String args[]) {
        EventManager mgr = new EventManager();
        int i;
        edu.hkust.clap.monitor.Monitor.loopBegin(384);
for (i = 0; i < 30; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(384);
{
            mgr.registerTimer(1000 + (int) (1000 * Math.random()), mgr, null);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(384);

        mgr.checkQueue();
    }

    synchronized void checkQueue() {
        Vector q = queue;
        if (q.size() == 0) return;
        Event e = (Event) q.elementAt(0);
        int i, size = q.size();
        edu.hkust.clap.monitor.Monitor.loopBegin(385);
for (i = 1; i < size; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(385);
{
            Event next = (Event) q.elementAt(i);
            if (next.time < e.time) {
                System.out.println("Events out of order!\n");
                System.out.println(q);
            }
            e = next;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(385);

    }
}

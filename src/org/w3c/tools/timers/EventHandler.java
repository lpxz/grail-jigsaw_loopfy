package org.w3c.tools.timers;

public interface EventHandler {

    /** Handle an timer event.  Data is the rock the register wanted
	  passed to the handleEvent routine, and time is the time the
	  event was scheduled to be delivered.  The handler can compare
	  that time to the current time, if it wants, to see whether or
	  not it's falling behind. */
    void handleTimerEvent(Object data, long time);
}

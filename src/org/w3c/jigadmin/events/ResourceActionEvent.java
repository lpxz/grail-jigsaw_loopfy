package org.w3c.jigadmin.events;

import java.util.EventObject;

/**
 * ActionEvent dedicated to Resource.
 * @version $Revision: 1.1 $
 * @author Beno�t Mah� (bmahe@w3.org)
 */
public class ResourceActionEvent extends EventObject {

    public static final int DELETE_EVENT = 10;

    public static final int ADD_EVENT = 20;

    public static final int REINDEX_EVENT = 30;

    public static final int SAVE_EVENT = 40;

    public static final int STOP_EVENT = 50;

    public static final int REFERENCE_EVENT = 60;

    public static final int EDIT_EVENT = 70;

    private int command = -1;

    public ResourceActionEvent(Object source, int command) {
        super(source);
        this.command = command;
    }

    public int getResourceActionCommand() {
        return command;
    }

    public String toString() {
        switch(command) {
            case DELETE_EVENT:
                return "DELETE RESOURCES";
            case REFERENCE_EVENT:
                return "SHOW REFERENCE DOCUMENTATION";
            case REINDEX_EVENT:
                return "REINDEX RESOURCES";
            case SAVE_EVENT:
                return "SAVE RESOURCES";
            case STOP_EVENT:
                return "STOP SERVER";
            case ADD_EVENT:
                return "ADD RESOURCE";
            case EDIT_EVENT:
                return "EDIT RESOURCE";
            default:
                return "UKNOWN";
        }
    }
}

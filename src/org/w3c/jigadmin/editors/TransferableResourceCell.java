package org.w3c.jigadmin.editors;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * The transferable ResourceCell
 * @see org.w3c.jigadmin.editors.ResourceCell
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class TransferableResourceCell implements Transferable {

    static final int CELL = 0;

    static final int STRING = 1;

    static final int PLAIN_TEXT = 2;

    public static final DataFlavor RESOURCE_CELL_FLAVOR = new DataFlavor(ResourceCell.class, "Resource Cell");

    static DataFlavor flavors[] = { RESOURCE_CELL_FLAVOR, DataFlavor.stringFlavor, DataFlavor.plainTextFlavor };

    private ResourceCell data = null;

    /**
     * Constructor
     * @param data The ResourceCell
     */
    public TransferableResourceCell(ResourceCell data) {
        this.data = data;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the
     * data can be provided in. The array should be ordered according to 
     * preference for providing the data (from most richly descriptive to 
     * least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported for 
     * this object.
     * @param flavor the requested flavor for the data 
     * @return boolean indicating wether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        boolean returnValue = false;
        edu.hkust.clap.monitor.Monitor.loopBegin(197);
for (int i = 0, n = flavors.length; i < n; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(197);
{
            if (flavor.equals(flavors[i])) {
                returnValue = true;
                break;
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(197);

        return returnValue;
    }

    /**
     * Returns an object which represents the data to be transferred. 
     * The class of the object returned is defined by the representation 
     * class of the flavor.
     * @param flavor the requested flavor for the data
     * @return an object which represents the data to be transferred
     * @exception IOException  if the data is no longer available in the 
     * requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor 
     * is not supported.
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Object returnObject;
        if (flavor.equals(flavors[CELL])) {
            returnObject = data;
        } else if (flavor.equals(flavors[STRING])) {
            returnObject = data.toString();
        } else if (flavor.equals(flavors[PLAIN_TEXT])) {
            String string = data.toString();
            returnObject = new ByteArrayInputStream(string.getBytes());
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
        return returnObject;
    }
}

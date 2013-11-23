package org.w3c.jigadmin.widgets;

import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.datatransfer.StringSelection;
import javax.swing.JList;
import javax.swing.ListModel;
import org.w3c.jigadmin.editors.TransferableResourceCell;
import org.w3c.jigadmin.editors.ResourceCell;
import java.util.Vector;

/**
 * A JList that accepts drag event.
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class DraggableList extends JList implements DragGestureListener {

    DragSource dragSource = DragSource.getDefaultDragSource();

    static final DragSourceListener dsl = new DragSourceListener() {

        public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent) {
        }

        public void dragEnter(DragSourceDragEvent DragSourceDragEvent) {
        }

        public void dragExit(DragSourceEvent DragSourceEvent) {
        }

        public void dragOver(DragSourceDragEvent DragSourceDragEvent) {
        }

        public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent) {
        }
    };

    public DraggableList() {
        super();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    public DraggableList(Object[] listData) {
        super(listData);
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    public DraggableList(ListModel dataModel) {
        super(dataModel);
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    public DraggableList(Vector listData) {
        super(listData);
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    private void startDrag(DragGestureEvent dragGestureEvent) {
        Object selected = getSelectedValue();
        if (selected instanceof String) {
            String sel = (String) selected;
            StringSelection selection = new StringSelection(sel);
            dragSource.startDrag(dragGestureEvent, DragSource.DefaultCopyDrop, selection, dsl);
        } else if (selected instanceof ResourceCell) {
            TransferableResourceCell trans = new TransferableResourceCell((ResourceCell) selected);
            dragSource.startDrag(dragGestureEvent, DragSource.DefaultCopyDrop, trans, dsl);
        } else {
            String sel = selected.toString();
            StringSelection selection = new StringSelection(sel);
            dragSource.startDrag(dragGestureEvent, DragSource.DefaultCopyDrop, selection, dsl);
        }
    }

    public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
        try {
            startDrag(dragGestureEvent);
        } catch (InvalidDnDOperationException ex) {
            ex.printStackTrace();
        }
    }
}

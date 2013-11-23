package org.w3c.tools.widgets;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface AnswerListener {

    public int YES = 1;

    public int NO = 2;

    public void questionAnswered(Object source, int response);
}

package org.w3c.tools.widgets;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class Utilities {

    public static final Insets insets0;

    public static final Insets insets2;

    public static final Insets insets4;

    public static final Insets insets5;

    public static final Insets insets10;

    public static final Insets insets20;

    public static final Font smallFont;

    public static final Font defaultFont;

    public static final Font boldFont;

    public static final Font mediumFont;

    public static final Font mediumBoldFont;

    public static final Font bigFont;

    public static final Font bigBoldFont;

    public static final Font reallyBigFont;

    public static final Font reallyBigBoldFont;

    public static final Dimension dim0_0;

    public static final Dimension dim1_1;

    public static final Dimension dim2_2;

    public static final Dimension dim3_3;

    public static final Dimension dim4_4;

    public static final Dimension dim5_5;

    public static final Dimension dim10_10;

    public static final Dimension dim20_20;

    static {
        insets0 = new Insets(0, 0, 0, 0);
        insets2 = new Insets(2, 2, 2, 2);
        insets4 = new Insets(4, 4, 4, 4);
        insets5 = new Insets(5, 5, 5, 5);
        insets10 = new Insets(10, 10, 10, 10);
        insets20 = new Insets(20, 20, 20, 20);
        smallFont = new Font("Dialog", Font.PLAIN, 10);
        defaultFont = new Font("Dialog", Font.PLAIN, 12);
        boldFont = new Font("Dialog", Font.BOLD, 12);
        mediumFont = new Font("Dialog", Font.PLAIN, 15);
        mediumBoldFont = new Font("Dialog", Font.BOLD, 15);
        bigFont = new Font("Dialog", Font.PLAIN, 18);
        bigBoldFont = new Font("Dialog", Font.BOLD, 18);
        reallyBigFont = new Font("Dialog", Font.PLAIN, 24);
        reallyBigBoldFont = new Font("Dialog", Font.BOLD, 24);
        dim0_0 = new Dimension(0, 0);
        dim1_1 = new Dimension(1, 1);
        dim2_2 = new Dimension(2, 2);
        dim3_3 = new Dimension(3, 3);
        dim4_4 = new Dimension(4, 4);
        dim5_5 = new Dimension(5, 5);
        dim10_10 = new Dimension(10, 10);
        dim20_20 = new Dimension(20, 20);
    }
}

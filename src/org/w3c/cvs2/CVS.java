package org.w3c.cvs2;

/**
 * Well known CVS constants.
 */
public interface CVS {

    public static final int FILE_OK = 1;

    public static final int FILE_A = 2;

    public static final int FILE_R = 3;

    public static final int FILE_M = 4;

    public static final int FILE_C = 5;

    public static final int FILE_U = 6;

    public static final int FILE_Q = 7;

    public static final int FILE_NCO = 8;

    public static final int DIR_CO = 9;

    public static final int DIR_NCO = 10;

    public static final int DIR_Q = 11;

    public static final String status[] = { "** RESERVED **", "Up to date", "Locally Added", "Locally Removed", "Locally Modified", "Conflict !", "Needs Patch", "Unknown", "Not Checked Out", "Checked Out", "Not Checked Out", "Unknown" };
}

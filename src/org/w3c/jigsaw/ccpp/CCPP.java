package org.w3c.jigsaw.ccpp;

import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface CCPP {

    public static final String HTTP_EXT_ID = "http://www.w3.org/1999/06/24-CCPPexchange";

    public static final String PROFILE_HEADER = "Profile";

    public static final String PROFILE_DIFF_HEADER = "Profile-Diff";

    public static final String PROFILE_WARNING_HEADER = "Profile-Warning";

    public static final String UNKNOWN_WARNING_MESSAGE = "Unknonwn Warning";

    public static final String CCPP_REQUEST_STATE = "org.w3c.jigsaw.ccpp.ccpprequest";

    public static final String msg_100[] = { "Ok", "Used stale profile", "Not used profile" };

    public static final String msg_200[] = { "Not applied", "Content selection applied", "Content generation applied", "Transformation applied" };

    public static final int OK = 100;

    public static final int USED_STALE_PROFILE = 101;

    public static final int NOT_USED_STALE_PROFILE = 102;

    public static final int NOT_APPLIED = 200;

    public static final int CONTENT_SELECTION_APPLIED = 201;

    public static final int CONTENT_GENERATION_APPLIED = 202;

    public static final int TRANSFORMATION_APPLIED = 203;
}

package org.w3c.tools.jpeg;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public interface Jpeg {

    public int M_SOF0 = 0xC0;

    public int M_SOF1 = 0xC1;

    public int M_SOF2 = 0xC2;

    public int M_SOF3 = 0xC3;

    public int M_SOF5 = 0xC5;

    public int M_SOF6 = 0xC6;

    public int M_SOF7 = 0xC7;

    public int M_SOF9 = 0xC9;

    public int M_SOF10 = 0xCA;

    public int M_SOF11 = 0xCB;

    public int M_SOF13 = 0xCD;

    public int M_SOF14 = 0xCE;

    public int M_SOF15 = 0xCF;

    public int M_SOI = 0xD8;

    public int M_EOI = 0xD9;

    public int M_SOS = 0xDA;

    public int M_APP0 = 0xE0;

    public int M_APP1 = 0xE1;

    public int M_APP2 = 0xE2;

    public int M_APP3 = 0xE3;

    public int M_APP4 = 0xE4;

    public int M_APP5 = 0xE5;

    public int M_APP6 = 0xE6;

    public int M_APP7 = 0xE7;

    public int M_APP8 = 0xE8;

    public int M_APP9 = 0xE9;

    public int M_APP10 = 0xEA;

    public int M_APP11 = 0xEB;

    public int M_APP12 = 0xEC;

    public int M_APP13 = 0xED;

    public int M_APP14 = 0xEE;

    public int M_APP15 = 0xEF;

    public int M_COM = 0xFE;

    public int M_MAX_COM_LENGTH = 65500;
}

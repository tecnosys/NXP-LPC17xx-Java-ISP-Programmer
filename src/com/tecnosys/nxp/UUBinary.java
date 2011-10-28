/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnosys.nxp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author admin
 */
public class UUBinary {

    int segments;
    int segment_size; //512
    int segment_arrays;
    int segment_crc[] = new int[255];
    int size;
    byte[][][] bytes = new byte[255][255][];

    public UUBinary(InputStream in, int segment_size) {
        this.segment_size = segment_size;
        try {

            int orig_size = in.available();

            int chunk = segment_size;
            int lpc17xx_checksum = 0;
            int headercrc=0;
            int uusize = 45; //fixed max uuencoding
            int chunk_remain = (chunk - uusize) + (chunk % uusize);
            int step = 0;

            int new_size = (orig_size) + (orig_size % 3);

            segments = 0;
            for (int n = 0; n < new_size;) { // 512new_size
                int crc = 0;
                int pos = 0;
                for (int p = 0; p < chunk;) {

                    if (p < chunk_remain) {
                        step = uusize;
                    } else {
                        int temp = (chunk % uusize);
                        step = temp;//-(temp%3);
                    }

                    byte[] uubytes = new byte[step];

                    for (int i = 0; i < step; i++) {
                        //(((b) == 0)? 0x60: ((b) + 0x20))
                        if (p < orig_size) {


                            uubytes[i] = (byte) in.read();
                            if ((n == 3)||(n == 7)||(n == 11)||(n == 15)||(n == 19)||(n == 23)||(n == 27)) {
                 //               int firstByte = uubytes[i-3];
                   //             int secondByte = uubytes[i-2];
                     //           int thirdByte = uubytes[i-1];
                       //         int fourthByte = uubytes[i];

                 lpc17xx_checksum += (uubytes[i-3]& 0xFF);
                lpc17xx_checksum += (uubytes[i-2]& 0xFF) << 8;
                lpc17xx_checksum += (uubytes[i-1]& 0xFF) << 16;
                lpc17xx_checksum += (uubytes[i]& 0xFF) << 24;

                         //       int anUnsignedInt = ((int) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte));// & 0xFFFFFFFF;
                             System.out.print(" lpc crc  "+lpc17xx_checksum);
                           //     lpc17xx_checksum+=anUnsignedInt;
                            }

                            if (n == 28) {
                                lpc17xx_checksum=0-lpc17xx_checksum;
                                uubytes[i] = (byte) ( (lpc17xx_checksum )  );
                                 System.out.println(" --> "+Integer.toHexString( uubytes[i]));
                            }
                            if (n == 29) {
                                uubytes[i] = (byte) ( (lpc17xx_checksum ) >>8 );
                                System.out.println(" --> "+Integer.toHexString( uubytes[i]));
                            }
                            if (n == 30) {
                                uubytes[i] = (byte) ( (lpc17xx_checksum ) >>16  );
                            System.out.println(" --> "+Integer.toHexString( uubytes[i]));
                            }
                            if (n == 31) {

                                uubytes[i] = (byte)  ( (lpc17xx_checksum ) >>24    );
                            System.out.println(" --> "+Integer.toHexString( uubytes[i]));
                            }
                        } else {
                            uubytes[i] = 0;//0x60;
                        }

                        crc = crc + unsignedByteToInt(uubytes[i]); //System.out.println(" "+ unsignedByteToInt(uubytes[i]));
                        p++;
                        n++;
                    }

                       /*
                       for (int i = 0; i < step; i++) {
                        //(((b) == 0)? 0x60: ((b) + 0x20))
                        if (p < orig_size) {

                            uubytes[i] = (byte) in.read();

                        } else {
                            System.out.println(" "+n);
                            uubytes[i] = 0;//0x60;
                        }
// crc = crc + unsignedByteToInt(uubytes[i]); //System.out.println(" "+ unsignedByteToInt(uubytes[i]));

                        p++;
                        n++;
                    }
                  //  System.out.println(" "+p+" "+n);


                    if (headercrc == 0) {
                       int base =0x14;
                        int basecrc =0x14;
                        // Clear the vector at 0x1C so zxt doesn't affect the checksum:
                        for (int zx = 0; zx < 4; zx++) {
                            uubytes[zx + base] = 0;
                        }

                      //     for (int zx = 0; zx < 4; zx++) {
                        //    uubytes[zx + 0x1c] = 0;
                       // }

                       System.out.println("xx");
                       for (int zx = 0; zx < (4 * 8);) {
                            System.out.print("["+zx+"] "+Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;

                            //lpc17xx_checksum += uubytes[zx++] << 8;
                            //lpc17xx_checksum += uubytes[zx++] << 16;
                            //lpc17xx_checksum += uubytes[zx++] << 24;
                        }
                       System.out.println("");
                        // Calculate a natzxve checksum of the lzxttle endzxan vector table:
                        for (int zx = 0; zx < (4 * 8);) {
                            lpc17xx_checksum += uubytes[zx++];
                            lpc17xx_checksum += uubytes[zx++] << 8;
                            lpc17xx_checksum += uubytes[zx++] << 16;
                            lpc17xx_checksum += uubytes[zx++] << 24;
                        }

         
                       lpc17xx_checksum = 0 - lpc17xx_checksum;

                        uubytes[basecrc + 0] = (byte) (lpc17xx_checksum >> (8 * 0));
                        System.out.println(" --> " + Integer.toHexString(uubytes[basecrc + 0]));
                        uubytes[basecrc + 1] = (byte) (lpc17xx_checksum >> (8 * 1));
                        System.out.println(" --> " + Integer.toHexString(uubytes[basecrc + 1]));
                        uubytes[basecrc + 2] = (byte) (lpc17xx_checksum >> (8 * 2)^ 0xff);
                        System.out.println(" --> " + Integer.toHexString(uubytes[basecrc + 2]));
                        uubytes[basecrc + 3] = (byte) (lpc17xx_checksum >> (8 * 3)^ 0xe);
                        System.out.println(" --> " + Integer.toHexString(uubytes[basecrc + 3]));

                        headercrc++;

                                               System.out.println("zz");
                       for (int zx = 0; zx < (4 * 8);) {
                            System.out.print("["+zx+"] "+Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;
                           System.out.print(Integer.toHexString(uubytes[zx++])+" ");//zx++;

                            //lpc17xx_checksum += uubytes[zx++] << 8;
                            //lpc17xx_checksum += uubytes[zx++] << 16;
                            //lpc17xx_checksum += uubytes[zx++] << 24;
                        }
                    }
*/
     //               System.out.println("\n\r\n\r ");
                    //Do checksum here
                    //for (int i = 0; i < step; i++) {
                      // System.out.print("["+i+"]"+Integer.toHexString(uubytes[i])+"");
                    //    crc = crc + unsignedByteToInt(uubytes[i]); //System.out.println(" "+ unsignedByteToInt(uubytes[i]));

                  //  }
  // System.out.println("\n\r\n\r ");
                    ByteArrayOutputStream uuarray = new ByteArrayOutputStream();

                    encodeLine(uubytes, 0, uubytes.length, uuarray);

                    ByteArrayOutputStream uu = new ByteArrayOutputStream();

                    for (int i = 0; i < uuarray.size(); i++) { // for (int i = 9; i < uuarray.size() - trim; i++) {

                        byte c = uuarray.toByteArray()[i];
                         
                        if (c == 0x20) { //0x20
                            c = '`';
                        }
                        uu.write(c);

                    }

                   // System.out.print("Building Segment:" + segments + " CRC:" + crc);
                    segment_arrays = pos; //set array dimensions

                    ByteArrayOutputStream buf = new ByteArrayOutputStream();

                    buf.write(uu.toString().trim().getBytes());
                    //buf.write(0x0D);
                    //0,1,34
                    bytes[segments][pos] = buf.toByteArray();

                    //System.out.println(" " + buf.toString());
                    pos++;


                }
                segment_crc[segments] = crc;

                segments++;

            }

            

/*
              int ivt_CRC = lpc17xx_checksum;
        for(int i = 0; i < 4; i++)
        {
            System.out.println("-> "+(ivt_CRC >> (8 * i)));
        }
*/
/*
        System.out.println(" "+ Integer.toHexString( (byte)((lpc17xx_checksum ) >>24   )));
	System.out.println(" "+ Integer.toHexString( (byte)((lpc17xx_checksum ) >>16   )));
	System.out.println(" "+ Integer.toHexString( (byte)((lpc17xx_checksum ) >>8   )));
	System.out.println(" "+ Integer.toHexString( (byte)((lpc17xx_checksum )    )));
            //System.out.println(" " + buf[0] + " " + Integer.toHexString(buf[1]) + " " + Integer.toHexString(buf[2]) + " " + Integer.toHexString(buf[3]) + " " );
 */
 in.close();

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    public void encodeLine(
            byte[] data, int offset, int length, OutputStream out)
            throws IOException {
        // write out the number of characters encoded in this line.


        out.write((byte) ((length & 0x3F) + ' '));
        byte a;
        byte b;
        byte c;

        for (int i = 0; i < length;) {
            // set the padding defaults
            b = 1;
            c = 1;
            // get the next 3 bytes (if we have them)
            a = data[offset + i++];
            if (i < length) {
                b = data[offset + i++];
                if (i < length) {
                    c = data[offset + i++];
                }
            }

            byte d1 = (byte) (((a >>> 2) & 0x3F) + ' ');
            byte d2 = (byte) ((((a << 4) & 0x30) | ((b >>> 4) & 0x0F)) + ' ');
            byte d3 = (byte) ((((b << 2) & 0x3C) | ((c >>> 6) & 0x3)) + ' ');
            byte d4 = (byte) ((c & 0x3F) + ' ');

            out.write(d1);
            out.write(d2);
            out.write(d3);
            out.write(d4);
        }

        // terminate with a linefeed alone
        out.write('\n');
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    public static void main(String[] args) {
        try {

          //  System.out.println("/Console_LPC1768");
          //  new UUBinary((InputStream) new FileInputStream("/Console_LPC1768.bin"), 512);

            System.out.println("/CANopener_v09_LPC1768.bin");
            new UUBinary((InputStream) new FileInputStream("/CANopener_v09_LPC1768.bin"), 512);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

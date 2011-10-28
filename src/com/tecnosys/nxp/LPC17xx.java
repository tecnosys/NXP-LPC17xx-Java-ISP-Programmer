/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnosys.nxp;

/**
 *
 * @author admin
 */
import gnu.io.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
//import sun.tools.jconsole.ConnectDialog;

public class LPC17xx {

        static boolean go = false;
    static boolean dtr = false;
    static boolean rts = false;
    static boolean mcu = false;
 static boolean show = false;
    public static void main(String[] args) {
        if (args.length == 0) {
            showUsage();
        }

        if (args.length == 1) {
            if (args[0].trim().equals("--listports")) {
                listPorts();
                System.exit(0);
            } else {
                showUsage();
            }
        }




        String fname = "";
        String fcom = "";
        if (args.length > 1) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].trim().equals("--listports")) {
                    listPorts();
                }
                if (args[i].trim().equals("--rts")) {
                    rts = true;
                }
                if (args[i].trim().equals("--dtr")) {
                    dtr = true;
                }
                  if (args[i].trim().equals("--go")) {
                    go = true;
                }
                if (args[i].trim().startsWith("--mcu:reset")) {
                    mcu = true;
                }
                if (args[i].trim().startsWith("--binary=")) {
                    fname = args[i].replaceFirst("--binary=", "");
                }
                if (args[i].trim().startsWith("--port=")) {
                    fcom = args[i].replaceFirst("--port=", "");
                }
            }
            new LPC17xx(fcom, fname);
            System.exit(0);
        }


    }

    public static void showUsage() {
        System.out.println("Usage:    LPC17xx --rts --dtr --port=/dev/tty.SLAB_USBtoUART --binary=Blinky_LPC1768.bin");
        System.out.println("");
        System.out.println("            --listports          List all available COM ports");
        System.out.println("            --rts                pull RTS high");
        System.out.println("            --dtr                pull DTR high");
        System.out.println("            --mcu:reset          (this is used by myself for automated software reset into ISP mode)");
        System.out.println("            --go                 this runs the binary after flash has completed");
        System.out.println("            --port=              COM3 or /dev/ttl.example");
        System.out.println("            --binary=            file.bin");
  System.out.println("");
  System.out.println("");
        System.exit(0);
    }

    public LPC17xx(String commport, String binfile) {
        try {
            //  connect("/dev/tty.usbserial-A700e6Ac");

            System.out.println("" + commport + " " + binfile);
            connect(commport, binfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void connect(String portName, String binfile) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT|SerialPort.FLOWCONTROL_XONXOFF_OUT);
                // Never tried this before, it was just one or the other
                //     serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                //                       SerialPort.FLOWCONTROL_RTSCTS_OUT);
//115200
//serialPort.setUARTType(portName, arg1)
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();

                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

                if (dtr) {
                    serialPort.setDTR(true);
                    System.out.println("DTR:HIGH");
                    Thread.sleep(100);
                }
                if (rts) {
                    serialPort.setRTS(true);
                    System.out.println("RTS:HIGH");
                    Thread.sleep(100);
                    serialPort.setRTS(false);
                    System.out.println("RTS:LOW");
                    Thread.sleep(100);
                }

                if (mcu) {
                    serialPort.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
System.out.println("MCU:RESET");
                    out.write("mcu:reset\r".getBytes());
                    Thread.sleep(100);
                }
         //       System.exit(0);
                // System.out.println("RTS: " + serialPort.isRTS() + " DTR:" + serialPort.isDTR());
int waitime = 4;
                String ostring = "?\n\r";
                out.write("?\n\r".getBytes());
                Thread.sleep(100);
                out.write("Synchronized\n\r".getBytes());
                Thread.sleep(waitime);
                out.write("12000\n\r".getBytes());
                Thread.sleep(waitime);
                out.write("J\n\r".getBytes());
                Thread.sleep(waitime);
                out.write("U 23130\n\r".getBytes());

                Thread.sleep(waitime);
                out.write("P 0 29\n\r".getBytes());
                Thread.sleep(waitime);
                out.write("E 0 29\n\r".getBytes());
                Thread.sleep(waitime);
                out.write("U 23130\n\r".getBytes());
// serialPort.setRTS(false);
                // serialPort.setDTR(false);

                Thread.sleep(waitime);

//new Encoder((InputStream)new FileInputStream("/blink2.bin"),out);
                UUBinary uu = new UUBinary((InputStream) new FileInputStream(binfile), 512); // /Blinky_LPC1768.bin

                System.out.println("Flashing:" + uu.segment_size + " / " + uu.segment_arrays + " x "+uu.segments);
                int pos = uu.segments;
show=true;
                int flash_address = (uu.segments - 1) * (uu.segment_size);
                for (int i = uu.segments - 1; i >= 0; i--) {

                    out.write(("W 268435968 " + uu.segment_size + "\n\r").getBytes());
                    Thread.sleep(waitime);

                    for (int x = 0; x <= uu.segment_arrays; x++) {
                        out.write(uu.bytes[i][x]);
                        //System.out.println(uu.bytes[i][x]);
                        out.write(0x0D);
                        Thread.sleep(waitime);

                    }
                    out.write((uu.segment_crc[i] + "\n\r").getBytes());
                    Thread.sleep(waitime);
                    out.write("P 0 29\n\r".getBytes());
                    Thread.sleep(waitime);

                    out.write(("C " + flash_address + " 268435968 " + uu.segment_size + "\n\r").getBytes());

                    Thread.sleep(waitime);
                    flash_address = flash_address - uu.segment_size;
               //     System.out.println("seg:" + i + " crc:" + uu.segment_crc[i]);
                 //System.out.print(".");


                }
                System.out.println("\nComplete");
                serialPort.setRTS(true);
                serialPort.setDTR(false);
                //  System.out.println("2 " + serialPort.isRTS() + " " + serialPort.isDTR());
                Thread.sleep(100);
                serialPort.setRTS(false);
                serialPort.setDTR(false);
                // System.out.println("2 " + serialPort.isRTS() + " " + serialPort.isDTR());

                Thread.sleep(100);
                 if (go) {
                out.write(("G 0 T\n\r").getBytes());
                 }
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    static void listPorts() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()));
        }
    }

    static String getPortTypeName(int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    /** */
    public static class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    System.out.println(new String(buffer, 0, len));
                    //String buf= new String(buffer, 0, len);
                      //     if(buf.contains("OK")&&show) System.out.print(".");
                     // if(buf.contains("RESEND")&&show) System.out.print("x");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** */
    public static class SerialWriter implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                int c = 0;
                while ((c = System.in.read()) > -1) {
                    this.out.write(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


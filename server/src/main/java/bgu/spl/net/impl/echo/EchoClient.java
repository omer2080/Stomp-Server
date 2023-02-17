// package bgu.spl.net.impl.echo;

// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.OutputStreamWriter;
// import java.net.Socket;

// public class EchoClient {

//     public static void main(String[] args) throws IOException {

//         if (args.length == 0) {
//             args = new String[]{"localhost", "hello"};
//         }

//         if (args.length < 2) {
//             System.out.println("you must supply two arguments: host, message");
//             System.exit(1);
//         }

//         //BufferedReader and BufferedWriter automatically using UTF-8 encoding
//         try (Socket sock = new Socket(args[0], 7777);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {

//             System.out.println("sending message to server");
//             out.write(args[1]);
//             out.newLine();
//             out.flush();

//             System.out.println("awaiting response");
//             String line = in.readLine();
//             System.out.println("message from server: " + line);
//         }
//     }
// }


//=======================================================================================
//=======================================================================================

//========================= FROM HERE IS OUR IMPLEMENTATION =============================

package bgu.spl.net.impl.echo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class EchoClient {


    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            args = new String[]{"127.0.0.1",
                    "CONNECT" +"\n"+
                            "accept-version:1.2" +"\n"+
                            "host:stomp.cs.bgu.ac.il" +"\n"+
                            "login:meni" +"\n"+
                            "passcode:film" +"\n"+"\n"+
                            "\u0000"+

                    "SUBSCRIBE"+"\n"+
                    "destination:germany_spain"+"\n"+
                    "id:17"+"\n"+
                    "receipt:73"+"\n"+"\n"+
                            "\u0000"+

//                    "CONNECT" +"\n"+
//                            "accept-version:1.2" +"\n"+
//                            "host:stomp.cs.bgu.ac.il" +"\n"+
//                            "login:meni" +"\n"+
//                            "passcode:film" +"\n"+"\n"+
//                            "\u0000"


//


                        "DISCONNECT"+"\n"+
                      "receipt:77"+"\n"+"\n"+
                            "\u0000"+
                            "CONNECT" +"\n"+
                            "accept-version:1.2" +"\n"+
                            "host:stomp.cs.bgu.ac.il" +"\n"+
                            "login:meni" +"\n"+
                            "passcode:film" +"\n"+"\n"+
                            "\u0000"+
                            "SUBSCRIBE"+"\n"+
                            "destination:germany_spain"+"\n"+
                            "id:17"+"\n"+
                            "receipt:73"+"\n"+"\n"+
                            "\u0000"

//                            "SUBSCRIBE"+"\n"+
//                            "destination:israel_spain"+"\n"+
//                            "id:17"+"\n"+
//                            "receipt:69"+"\n"+"\n"+
//                            "\u0000"
//                            "CONNECT" +"\n"+
//                            "accept-version:1.2" +"\n"+
//                            "host:stomp.cs.bgu.ac.il" +"\n"+
//                            "login:meni" +"\n"+
//                            "passcode:fil" +"\n"+"\n"+
//                            "\u0000"+

//                            "UNSUBSCRIBE\n"+
//                            "id:17\n"+
//                            "receipt:74"+"\n"+"\n"+
//                            "\u0000"


//                            "SEND\n" +
//                            "receipt:75"+"\n"+
//                        "destination:germany_spain\n\n"+
//                        "Hello topic a \n"+"\n"+
//                            "\u0000"



//                            "SEND\n" +
//                            "receipt:76"+"\n"+
//                            "destination:germany_spain\n\n"+
//                            "Hello topic b \n"+"\n"+
//                            "\u0000"




            };
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        try (Socket sock = new Socket(args[0], 7777);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
            final LineMessageEncoderDecoder encdec = new LineMessageEncoderDecoder();
            System.out.println("sending message to server");
            out.write(args[1]);
//            out.newLine();
            out.flush();

            System.out.println("awaiting response");
//            String line = in.readLine();
            String line;
            int read;
            while (true) {
                read = in.read();
                line = encdec.decodeNextByte((byte) read);
                if (line != null) {
                    System.out.println("message from server: \n" + line);
                }
            }
        }
    }
}
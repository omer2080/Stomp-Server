package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // check args and make sure they are valid or add defaults
        if(args.length < 2){
            System.out.println("Usage: StompServer <port> <tpc/reactor>\nRunning with default values: 7777, tpc");
            args = new String[]{"7777", "tpc"};
        }
        int port = Integer.parseInt(args[0]);

        if(args[1].equals("tpc")){
            System.out.println("Starting tpc server");
            Server.threadPerClient(
                    port, //port
                    () -> new StompMsgProtocolImpl(), //protocol factory
                    StompEncDecImpl::new //message encoder decoder factory
            ).serve();
        }
        else if(args[1].equals("reactor")){
            System.out.println("Starting reactor server");
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port, //port
                    () -> new StompMsgProtocolImpl(), //protocol factory
                    StompEncDecImpl::new //message encoder decoder factory
            ).serve();
        }
        else{
            System.out.println("Usage: StompServer <port> <tpc/reactor>");
        }
    }
}


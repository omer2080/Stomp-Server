package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMsgProtocolImpl implements StompMessagingProtocol<String>{

    private boolean shouldTerminate = false;
    private int connectionId;
    private ConnectionsImpl<String> connections=null;

    //Initiate the protocol with the active connections structure of the server and saves the owner client’s connection id
    @Override
    public void start(int _connectionId, Connections<String> _connections) {
        connectionId = _connectionId;
        connections = (ConnectionsImpl<String>) _connections;

    }

    //processes a given message & responses are sent via the connections object send functions (if needed)
    @Override
    public void process(String msg) {
        // System.out.println("StompMsgProtocolImpl, inside process(), msg is:\n\""+msg+"\"");  // for debuging
        Frame frame = new Frame(msg);
        String command = frame.getCommand();
        Map<String,String> headers = frame.getHeaders();
        String body = frame.getBody();
        String receipt = headers.get("receipt"); // null if there is no receipt.
        if("CONNECT".equals(command)){
            String myUserName = headers.get("login");
            String myPassword = headers.get("passcode");
            String myVersion = headers.get("accept-version");
            String myHost = headers.get("host");
            if (myUserName == null || myPassword == null || myVersion == null || myHost == null){
               sendError(command, receipt, "malformed frame received"); 
               return;
            }
            boolean isUsernameExist = connections.isUsernameExist(myUserName);
            //correct user:
            if (isUsernameExist && connections.isUsernameMatchPass(myUserName, myPassword)){
                User user = connections.getUserByName(myUserName);
                //user is already logged in:
                if(user.isConnected()){
                    sendError(command, receipt, "User already logged in");
                    return;
                }
                //user is logging in now: 
                else{
                    user.setConnectionId(connectionId);
                    user.connectUser();
                    connections.connectUser(user);
                    sendConnected(myVersion,receipt);
                }
            } 
            //incorrect password:
            else if(isUsernameExist && !(connections.isUsernameMatchPass(myUserName, myPassword))){
                sendError(command, receipt, "Wrong password"); 
                return;
            }
            //new user - need to be registered:
            else{
                User newUser = new User(myUserName, myPassword, connectionId); 
                newUser.connectUser();
                connections.addNewUser(newUser); 
                sendConnected(myVersion,receipt);
            }
        }
        
        //in any other case, the user must be connected, or an errorFrame will be returned:
        else if(connections.getUserByConnectionId(connectionId) == null ||
        connections.getUserByConnectionId(connectionId).isConnected() == false){
            sendError(command, receipt, "a user is not logged in - unvalid command");
            return;
        }

        else if("SEND".equals(command)){
            String channel = headers.get("destination");
            if (channel == null || body == null || body.equals("")){ //here body must contain something
                sendError(command, receipt, "malformed frame received");
                return;
            }
            ConcurrentLinkedQueue<Integer> subscribedUsers = connections.getSubscribedUsers(channel);
            if(subscribedUsers == null){
                sendError(command, receipt, "channel \"" + channel +"\" does not exist");
                return;
            }
            if(subscribedUsers.contains(connectionId) == false){
                sendError(command, receipt, "client must be subscribed to the channel");
                return;
            }
            else{
                String messageId = Integer.toString(connections.getAndIncMsgCounter());
                // send msg to all the subscribed users of topic "channel"
                for(Integer clientId : subscribedUsers){
                    String subsId = connections.getUserByConnectionId(clientId).getSubscriptionIdByChannel(channel);
                    sendMessage(clientId,subsId,messageId,channel, body);
                }
            }
        }

        else if("SUBSCRIBE".equals(command)){
            String destination = headers.get("destination");
            String subscriptionId = headers.get("id");
            if (destination == null || subscriptionId == null){
                sendError(command, receipt, "malformed frame received");
                return;
            }
            if(connections.getSubscribedUsers(destination) == null){
                connections.createNewChannel(destination);}
            if(connections.getSubscribedUsers(destination).contains(connectionId) == false){
                connections.subscribeUserToChannel(connectionId, destination);
                User user = connections.getUserByConnectionId(connectionId);
                user.addSubscription(destination, subscriptionId);
            }
        }

        else if("UNSUBSCRIBE".equals(command)){
            String subscriptionId = headers.get("id");
            if(subscriptionId == null){
                sendError(command, receipt, "malformed frame received");
                return;
            }
            User user = connections.getUserByConnectionId(connectionId);
            String channelToRemove = user.getChannelBySubscriptionId(subscriptionId);
            user.removeSubByChannelName(channelToRemove);
            connections.unSubscribeUserToChannel(connectionId, channelToRemove);
        }

        else if("DISCONNECT".equals(command)){
            if(receipt == null){
                sendError(command, receipt, "malformed frame received");
                return;
            }
            /// ...disconnectUser() is done inside connections.disconnect()
            sendReceipt(receipt);
            shouldTerminate = true;
            connections.disconnect(connectionId);
        }

        else{
            // System.out.println("StompMsgProtocolImpl, illegal command");
            sendError(command, receipt, "illegal command");
            return;
        }

        if(!(receipt == null || command.equals("CONNECT") || command.equals("DISCONNECT"))){
            // System.out.println("StompMsgProtocolImpl, sending receipt frame #"+receipt+".");
            sendReceipt(receipt); 
        }
        // System.out.println("StompMsgProtocolImpl, end of process()");

    }

    // Server's responses:
    private void sendConnected(String version, String receipt) {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("version", version);
        if (receipt != null){
            headers.put("receipt-id", receipt);
        }
        Frame connectedFrame = new Frame("CONNECTED", headers, "");
        connections.send(connectionId, connectedFrame.toString());
    }

    private void sendMessage(int clientId,String subscriptionId,String messageId,String destination,String body) {
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("destination", destination);
        headers.put("subscription", subscriptionId);
        headers.put("message-id", messageId);
        String command = "MESSAGE";
        Frame messageFrame = new Frame(command,headers,body);
        connections.send(clientId, messageFrame.toString());
    }

    private void sendReceipt(String receipt) {
        if (receipt != null){
            HashMap<String,String> headers = new HashMap<>();
            headers.put("receipt-id", receipt);
            Frame receiptFrame = new Frame("RECEIPT", headers, "");
            connections.send(connectionId, receiptFrame.toString());
        }
    }

    private void sendError(String commandCausedError, String receipt, String mistakeMsg) {
        Frame errorFrame = new Frame(commandCausedError,receipt,mistakeMsg);        
        connections.send(connectionId, errorFrame.toString());
        shouldTerminate = true;
        connections.disconnect(connectionId);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

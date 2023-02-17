package bgu.spl.net.impl.stomp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.srv.ConnectionHandler;

import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {

    //mapping client's connectionId to his ConnectionHandler
    private final ConcurrentHashMap<Integer,ConnectionHandler<T>> connectionHandlersMap;

    //mapping between a connectionId to its user
    private final ConcurrentHashMap<Integer,User> connectionIdToUserMap;
    
    //mapping between a topic and the users connectionIds subscribed to it
    private final ConcurrentHashMap<String,ConcurrentLinkedQueue<Integer>> topicToUsersIdMap;
    
    //mapping between a username and password for all active Users
    private final ConcurrentHashMap<String,String> usernameToPasswordMap;

    //mapping between a username and User
    private final ConcurrentHashMap<String,User> usernameToUserMap;

    //the connectionId that will be given to the next client who loggs in.
    private AtomicInteger nextConnectionId;

    //the messaggeId that will be given to the next message the server will send.
    private AtomicInteger messageIdCounter;

    //constructor:
    public ConnectionsImpl(){
        connectionHandlersMap = new ConcurrentHashMap<Integer,ConnectionHandler<T>>();
        topicToUsersIdMap = new ConcurrentHashMap<String,ConcurrentLinkedQueue<Integer>>();
        connectionIdToUserMap = new ConcurrentHashMap<Integer,User>();
        usernameToPasswordMap = new ConcurrentHashMap<String,String>();
        usernameToUserMap = new ConcurrentHashMap<String,User>();
        nextConnectionId = new AtomicInteger(); //initial value is 0
        messageIdCounter = new AtomicInteger(); //initial value is 0
    }

    /**
     * sends a message T to client represented by the given connectionId.
     */
    @Override
    public boolean send(int connectionId, T msg) {
        // System.out.println("CHImpl sending message:\n"+msg+".");
        connectionHandlersMap.get(connectionId).send(msg);
        return false;
    }

    /**
     * Sends a message T to clients subscribed to {@code channel}.
     */
    @Override
    public void send(String channel, T msg) {
        ConcurrentLinkedQueue<Integer> usersToSend = topicToUsersIdMap.get(channel);
        if (usersToSend != null){
            for (Integer connectionId : usersToSend){
                send(connectionId, msg);
            }
        }
    }

    /**
     * Removes an active client from connections database and disconnecting it's user iff connected
    */
    @Override
    public void disconnect(int connectionId) {
        // client is connected to a user:
        if(connectionIdToUserMap.get(connectionId) != null){ 
            User user = connectionIdToUserMap.get(connectionId);
            // remove registration from subscribed channels (only in connectins):
            Map<String,String> subscribedChannelsToRemove = user.getSubscribedChannelsMap();
            for (String channel : subscribedChannelsToRemove.keySet()){
                topicToUsersIdMap.get(channel).remove((Integer)connectionId); //the issue with remove and index
            }
            // remove user from UserMap:
            connectionIdToUserMap.remove(connectionId);
            //disconnect() of the user object itself:
            user.disconnectUser();
        }
    }

    /**
     * given a userName, the function returns if the userName is already logged in
    */
    public boolean isUsernameExist(String userNameTocheckString){
        return usernameToPasswordMap.containsKey(userNameTocheckString);
    }

    /**
     * returns true iff the username exists && matches the password
    */ 
    public boolean isUsernameMatchPass(String username, String password){
        if (! usernameToPasswordMap.containsKey(username)){
            return false;
        }
        return (password.equals(usernameToPasswordMap.get(username)));
    }

    /**
     * add new client to the ConnectionHandlers map (before protocol process any message)
    */
    public int addNewClient(ConnectionHandler<T> connectionHandler){
        int newConnectionId = nextConnectionId.getAndIncrement();
        connectionHandlersMap.put(newConnectionId, connectionHandler);
        return newConnectionId;
    }

    public void addNewUser(User user){
        connectionIdToUserMap.put(user.getconnectionId(), user);
        usernameToPasswordMap.put(user.getUserName(),user.getPasscode());
        usernameToUserMap.put(user.getUserName(), user);
    }

    public void connectUser(User user){
        connectionIdToUserMap.put(user.getconnectionId(), user);
    }

    public void subscribeUserToChannel(int connectionId, String channel){
        topicToUsersIdMap.get(channel).add(connectionId);
    }

    public void unSubscribeUserToChannel(int connectionId, String channel){
        topicToUsersIdMap.get(channel).remove((Integer)connectionId); //did it because of the remove definition
    }

    public ConcurrentLinkedQueue<Integer> getSubscribedUsers(String channel){
        return topicToUsersIdMap.get(channel);
    }

    public void createNewChannel(String channel){
        topicToUsersIdMap.put(channel, new ConcurrentLinkedQueue<Integer>());
    }

    public User getUserByName(String username){
        return usernameToUserMap.get(username);
    }

    public User getUserByConnectionId(int connectionId){
        return connectionIdToUserMap.get(connectionId);
    }

    public int getAndIncMsgCounter(){
        return messageIdCounter.getAndIncrement();
    }
}

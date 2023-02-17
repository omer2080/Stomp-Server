package bgu.spl.net.impl.stomp;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class User {
        //fields:
        private String userName;
        private String passcode;
        private int connectionId;
        private ConcurrentHashMap<String,String> subscribedChannels; //<channel,SubscriptionId>
        private boolean isConnected = false;

        //constructor:
        User(String _userName, String _passcode, int _connectionId){
            userName = _userName;
            passcode = _passcode;
            connectionId = _connectionId;
            subscribedChannels = new ConcurrentHashMap<String,String>();

         }

        public String getUserName(){
            return userName;
        }

        public String getPasscode(){
            return passcode;
        }
        
        public Integer getconnectionId(){
            return connectionId;
        }
        
        public boolean isConnected(){
            return isConnected;
        }

        /**
         * @return channel,subscriptionId map
         */
        public ConcurrentHashMap<String,String> getSubscribedChannelsMap(){
            return subscribedChannels;
        }

        public void addSubscription(String channelName, String SubscriptionId){
            if (channelName != null && SubscriptionId != null){
                subscribedChannels.put(channelName, SubscriptionId);
            }
        }

        public void removeSubByChannelName(String channelName){
                subscribedChannels.remove(channelName);
        }

        public void setConnectionId(int _connectionId){
            connectionId = _connectionId;
        }

        //might return null
        public String getChannelBySubscriptionId(String subscriptionId){
            Set<String> keySet = subscribedChannels.keySet();
            for (String key : keySet){
                if (subscribedChannels.get(key).equals(subscriptionId)){
                    return key;
                }
            }
            return null; //not supposed to get here
        }

        public void connectUser(){
            isConnected = true;
        }

        public void disconnectUser(){
            isConnected = false;
            connectionId = -1;
            subscribedChannels.clear();
        }

        public String getSubscriptionIdByChannel(String channel) {
            return subscribedChannels.get(channel);
        }      

}
        





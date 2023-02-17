#pragma once
#include <string>
#include <unordered_map>
#include <Frame.h>
#include <stdlib.h>
#include <string>
#include <iostream>


using namespace std;


class User {
private:
    string name;
    string password;
    bool connected;
    int nextSubscriptionId;
    int nextReceiptId;
    unordered_map<string,string> receiptIdToPrintMsg;
    unordered_map<string,string> subscribedChannels; //channel name to subscription id
    
public:
    User(const string &name, const string &password);
  
    void connect();
    void disconnect();
    bool isConnected();
    
    void addReceiptToFrame(Frame &frame, string receiptMsg); 
    int addSubscription(string channel);
    void removeSubscriptionBychannel(string channel);

    string getSubscriptionIdByChannel(string channel);
    string getChannelBySubscription(string subId);
    string getPrintMsgByReceiptId(string receiptId);
    unordered_map<string,string> getSubscribeChannels();

};

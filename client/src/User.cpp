#include "User.h"

using namespace std;


User::User(const string &name, const string &password) : 
    name(name), password(password),
    connected(false),
    nextSubscriptionId(0),
    nextReceiptId(0),
    receiptIdToPrintMsg({}), 
    subscribedChannels({})
    {}

void User::connect() {connected = true;}
void User::disconnect() {
    connected = false;
}
bool User::isConnected() {return connected;}
unordered_map<string,string> User::getSubscribeChannels(){return subscribedChannels;}

int User::addSubscription(string channel){
    int currentSubId = nextSubscriptionId;
    subscribedChannels[channel] = to_string(currentSubId);
    nextSubscriptionId++;
    return currentSubId;
}

void User::removeSubscriptionBychannel(string channel){ subscribedChannels.erase(channel); }

string User::getSubscriptionIdByChannel(string channel){
    if(subscribedChannels.find(channel) == subscribedChannels.end())
        return "";
    else
        return subscribedChannels[channel];
}
string User::getPrintMsgByReceiptId(string receiptId){
    if(receiptIdToPrintMsg.find(receiptId) == receiptIdToPrintMsg.end())
        return "";
    else
        return receiptIdToPrintMsg[receiptId];
}

string User::getChannelBySubscription(string subId){
    for (unordered_map<string,string>::const_iterator it = subscribedChannels.begin(); it != subscribedChannels.end(); ++it)
        if (it->second == subId)
            return it->first;
    return "";
}

// gets a frame and msg, and adds a receipt header to the frame + prepare msg to be printed when receipt is received from server
void User::addReceiptToFrame(Frame &frame, string receiptMsg){
    string receiptId = to_string(nextReceiptId);
    frame.addHeader("receipt", receiptId);
    receiptIdToPrintMsg[receiptId] = receiptMsg; //map
    nextReceiptId++;
}

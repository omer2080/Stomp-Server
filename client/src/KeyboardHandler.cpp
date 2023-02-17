#include <KeyboardHandler.h>

using namespace std;

KeyboardHandler::KeyboardHandler(User &user, StompProtocol &protocol):user(user), protocol(protocol){}

vector<string> KeyboardHandler::divideStr(string &str, char delimiter){
    string part;
	vector<string> partlist;
	istringstream strStream(str);
	while(getline(strStream , part, delimiter)){
		partlist.push_back(part);
	}
	return partlist;
}

void KeyboardHandler::run(){
    while(user.isConnected()){ // if error received, user will be disconnected.
        // get line input from the user and split it by delimiter:
        // cout << "listening to keyboard input..." << endl; // TODO: delete, debuging
        string line;
        getline(cin,line);
        if(!user.isConnected()){
            break;
        }
        if(line.empty()){
            cout << "input is empty string, continue" << endl;
            continue;
        }
        vector<string> partlist = divideStr(line, ' ');
        string command = partlist.front();

        if(command.compare("join") == 0){
            joinRecieved(partlist);
        }
        else if(command.compare("exit") == 0){
            exitRecieved(partlist);
        }
        else if(command.compare("report") == 0){
            reportRecieved(partlist);
        }
        else if(command.compare("summary") == 0){
            summaryRecieved(partlist);
        }
        else if(command.compare("logout") == 0){
            logoutRecieved(partlist);
            break; // to end keyboardHandler thread.
        }
        else if(command.compare("login") == 0){
            cout << "The client is already logged in, log out before trying again" << endl;
        }
        else{
            cout << "invalid command" << endl;
        }
    }
}

void KeyboardHandler::joinRecieved(vector<string> &commandElements){
    // generate "SUBSCRIBE" frame:
    string c = "SUBSCRIBE";
    string b = "";
    string game_name = commandElements.at(1);
    pair<string,string> destinationPair = make_pair("destination", game_name);
    unordered_map<string, string> h = {destinationPair};
    Frame subFrame = Frame(c, h, b);
    // send it to protocol to add subId and receiptId, and to add subscription to User, and send it:
    protocol.process(subFrame);
}

void KeyboardHandler::exitRecieved(vector<string> &commandElements){
    string game_name = commandElements.at(1);
    string subId = user.getSubscriptionIdByChannel(game_name);
    if(subId == "" || subId.empty()){
        cout << "you are not subscribed to this channel" << endl;
        return;
    } 
    // generate "UNSUBSCRIBE" frame (Protocol)
    string c = "UNSUBSCRIBE";
    string b = "";
    pair<string,string> subIdPair = make_pair("id", subId);
    unordered_map<string, string> h = {subIdPair};
    Frame unSubFrame = Frame(c, h, b);
    // send it to protocol to add receiptId and to unsubscribe inside the User:
    protocol.process(unSubFrame);
}

void KeyboardHandler::reportRecieved(vector<string> &commandElements){
    // Usage: "report <destination> <body message>"
    
    string c = "SEND";
    // string file = commandElements.at(1); // not supported.
    
    string destination = commandElements.at(1);
    string b = "";
    size_t i;
    for(i = 2; i < commandElements.size()-1; i++){
        b += commandElements.at(i) + " ";
    }
    if(commandElements.size() > 2){
        b += commandElements.at(i);
    }

    // insert destination header:
    pair<string,string> destPair = make_pair("destination", destination); //TODO: after debug, change destination.
    unordered_map<string, string> h = {destPair};
    Frame sendFrame = Frame(c, h, b);
    // send it(them?) to server:
    protocol.process(sendFrame);
}

void KeyboardHandler::logoutRecieved(vector<string> &commandElements){
    // generate "DISCONNECT" frame
    string c = "DISCONNECT";
    string b = "";
    unordered_map<string, string> h = {};
    Frame disConnectFrame = Frame(c, h, b);
    // send it to the protocol to add receiptId and to send it to server:
    protocol.process(disConnectFrame);
}

void KeyboardHandler::summaryRecieved(vector<string> &commandElements){ // not supported.
    string c = commandElements.at(0);
    string gameName = commandElements.at(1);
    string userName = commandElements.at(2);
    string file = commandElements.at(3);
}

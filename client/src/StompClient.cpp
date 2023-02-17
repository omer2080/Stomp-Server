#include <stdlib.h>
#include <thread>
#include "../include/ConnectionHandler.h"
#include <sstream>
#include <boost/lexical_cast.hpp>
#include <KeyboardHandler.h>
#include <User.h>
#include <StompProtocol.h>

using namespace std;

int main (int argc, char *argv[]) {
   
    while(true){
        string line;
        vector<string> commandElements;
        string command="";

        // get inputs until "login" command received:
        while(command.compare("login") != 0){
            cout << "Please login:       (to exit press ctrl+c)" << endl;
            // get user input and split it by spaces:
            getline(cin,line);
            if(line.empty()){
                cout << "input is an empty string, enter login command!" << endl;
                continue;
            }
            commandElements = KeyboardHandler::divideStr(line, ' ');
            command = commandElements.front();
        }
        // cout << "login command received. get the login details" <<endl; // for debuging

        // login command received. get the login details:
        string ip = commandElements.at(1);
        string username = commandElements.at(2);
        string password = commandElements.at(3);
        vector<string> ipParts = KeyboardHandler::divideStr(ip, ':');
        string host = ipParts.at(0);
        string portstr = ipParts.at(1);
        short port = boost::lexical_cast<short>(portstr);
        
        // create user, connection handler, protocol instances:
        User user(username, password);
        ConnectionHandler connectionHandler(host, port);
        StompProtocol protocol(user, connectionHandler);

        // socket connection:
        if (!connectionHandler.connect()){
            cerr << "Cannot connect to " << host << ":" << port << endl;
            continue; // big while loop
        }
        // cout << "socket connected" << endl; // for debuging

        //generate CONNECT frame:
        string c = "CONNECT";
        string b = "";
        pair<string,string> versionPair = make_pair("accept-version", "1.2");
        pair<string,string> hostPair = make_pair("host", "stomp.cs.bgu.ac.il");
        pair<string,string> loginPair = make_pair("login", username);
        pair<string,string> passcodePair = make_pair("passcode", password);
        unordered_map<string, string> h = {versionPair, hostPair, loginPair, passcodePair};
        Frame connectFrame(c, h, b);
        
        // cout << "sending connect frame" << endl; // for debuging
        
        // send frame to server through protocol:
        protocol.process(connectFrame);

        // cout << "waiting for response" << endl; // for debuging
        
        // recieve msg from server:
        string responseFrame;
        if (!connectionHandler.getFrameAscii(responseFrame, '\0')) {
            cout << "error while recieving msg back from server. Exiting...\n" << std::endl;
            connectionHandler.close();
            continue; // big while loop
        }

        // convert msg to frame and verify it is a "CONNECTED"/"ERROR" frame:
        Frame connectedFrame(responseFrame);
        if(connectedFrame.getCommand().compare("CONNECTED") == 0){
            protocol.process(connectedFrame);
        }
        else if(connectedFrame.getCommand().compare("ERROR") == 0){
            protocol.process(connectedFrame);
            connectionHandler.close();
            continue; // big while loop
        }
        else{
            cout << "did not recieve CONNECTED/ERROR frame back from protocol. Exiting...\n" << std::endl;
            connectionHandler.close();
            continue; // big while loop
        }
        
        // cout << "done connecting, starting keyboard thread" << endl;  // for debuging

        // Succesfuly connected, create and run input thread:
        KeyboardHandler keyboardHandler(user, protocol);
        thread keyboardThread(&KeyboardHandler::run, keyboardHandler);
        // cout << "keyboard thread started" << endl; // for debuging

        // current thread will listen to socket and recieve frames from server:
        string responseStr;
        while(user.isConnected()){
            // cout << "listening to server responses..." << endl; // for debuging
            // recieve msg from server:
            responseStr = "";
            if (!connectionHandler.getFrameAscii(responseStr, '\0')) {
                cout << "could not get msg from server. Exiting...\n" << std::endl;
                user.disconnect();
                break;
            }
            if(responseStr.empty()){
                // cout << "recieved empty string from server, skip it" << endl; // for debuging
                continue;
            }
            // cout << "recieved response:" << endl << "\"" << responseStr << "\"" << endl << endl; // for debuging

            // convert msg to frame and process it:
            Frame responseFrame(responseStr);
            // cout << "calling protocol.process(responseFrame)" << endl; // for debuging
            protocol.process(responseFrame);
        }
        
        cout << "Closing the connection with the server..." << endl << "If connection is not closing automaticly, press Enter." << endl;
        // close keyboard thread and CH:
        keyboardThread.join();
        // cout << "keyboard thread joined" << endl;  // for debuging
        connectionHandler.close();
        cout << "Connection with the server is closed.\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" << endl; 
    }
    return 0;
}

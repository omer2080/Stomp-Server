#pragma once
#include <User.h>
#include <StompProtocol.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <sstream>
#include <boost/lexical_cast.hpp>
#include <Frame.h>


class KeyboardHandler {
public:
    KeyboardHandler(User &user, StompProtocol &protocol);
    static vector<string> divideStr(string &str, char delimiter);
    void run();

private:
    User &user;
    StompProtocol &protocol;
    
    void joinRecieved(vector<string> &commandElements);
    void exitRecieved(vector<string> &commandElements);
    void reportRecieved(vector<string> &commandElements);
    void summaryRecieved(vector<string> &commandElements);
    void logoutRecieved(vector<string> &commandElements);

};
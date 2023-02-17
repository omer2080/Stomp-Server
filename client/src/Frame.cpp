#include "Frame.h"
#include <string>
#include <map>
#include <unordered_map>


// import java.util.*;

using namespace std;
Frame::Frame():mCommand(""), mHeaders(unordered_map<string,string>()), mBody("") {}
Frame::Frame(const string &command, const unordered_map<string,string> &headers, string &body) : mCommand(command), mHeaders(headers), mBody(body){}
Frame::Frame(const string &msg):mCommand(""), mHeaders(unordered_map<string,string>()), mBody("") {
    //command:
    string c;
    int ind = 0;
    while(msg[ind] != '\n'){
        c += msg[ind];
        ind++;
    }
    ind++;
    //headers:
    unordered_map<string,string> h;
    string currKey;
    string currVal;
    while(msg[ind] !='\n'){
        while(msg[ind] != ':'){
            currKey += msg[ind];
            ind++;
        }
        ind++;
        while(msg[ind]!='\n'){
            currVal += msg[ind];
            ind++;
        }
        h[currKey] = currVal;
        currKey = "";
        currVal = "";
        ind++;
    }
    //body:
    string b;
    while(msg[ind]!='\0'){
        b += msg[ind];
        ind++;
    }
    mCommand = c;
    mHeaders = h;
    mBody = b;
}


string Frame::getCommand() const { return mCommand; }
unordered_map<string, string> Frame::getHeaders() const { return mHeaders; }
string Frame::getBody() const { return mBody; }

void Frame::setCommand(const string &command) { mCommand = command; }
void Frame::setHeaders(const unordered_map<string, string> &headers) { mHeaders = headers; }
void Frame::setBody(const string &body) { mBody = body; }

void Frame::addHeader(const string &key, const string &value) { mHeaders[key] = value; }
string Frame::getHeaderByKey(const string &key) const { return mHeaders.at(key); }
bool Frame::hasHeader(const string &key) const { return mHeaders.count(key) > 0; }

string Frame::toString(){
    string output;
    output += mCommand;
    output += '\n';
    for (const auto &header : mHeaders) {
        output += header.first + ':' + header.second;
        output += '\n';
    }
    output += '\n';
    output += mBody;
    // output += '\0'; // already done in other location
    return output;
}







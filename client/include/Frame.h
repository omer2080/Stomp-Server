#pragma once
// import java.util.*;
#include <map>
#include <string>
#include <unordered_map>

using namespace std;

class Frame{
private:
    string mCommand;
    unordered_map<string, string> mHeaders;
    string mBody;

public:
    // Frame(const string &command);
    Frame(const string &command , const unordered_map<string,string> &headers , string &body);
    Frame(const string &msg);
    Frame();

    string getCommand() const;
    string getHeaderByKey(const string &key) const;
    string getBody() const;
    unordered_map<string, string> getHeaders() const;

    void setCommand(const string &command);
    void setHeaders(const unordered_map<string, string> &headers);
    void setBody(const string &body);
    void addHeader(const string &key, const string &value);
    bool hasHeader(const string &key) const;
    string toString();
    
};

#pragma once

#include "../include/ConnectionHandler.h"
#include <Frame.h>
#include <User.h>

class StompProtocol
{
private:
    User &user;
    ConnectionHandler &connectionHandler;

public:
    StompProtocol(User &user, ConnectionHandler &connectionHandler);
    void process(Frame &frame);
    void sendFrameToServer(Frame &frame);

    // Frames from the server:
    void connectedRecieved(Frame &frame);
    void messageRecieved(Frame &frame);
    void receiptRecieved(Frame &frame);
    void errorRecieved(Frame &frame);
    
    // Frames from the user:
    void connectReceived(Frame &frame);
    void subReceived(Frame &frame);
    void unSubReceived(Frame &frame);
    void sendReceived(Frame &frame);
    void disconnectReceived(Frame &frame);
};

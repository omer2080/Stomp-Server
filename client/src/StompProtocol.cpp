#include <StompProtocol.h>

StompProtocol::StompProtocol(User &user, ConnectionHandler &ch): user(user), connectionHandler(ch){};

void StompProtocol::process(Frame &frame){
	string command = frame.getCommand();
    
	// server response frames:
	if(command.compare("CONNECTED") == 0){
		connectedRecieved(frame);
	}
	else if(command.compare("MESSAGE") == 0){
		messageRecieved(frame);
	}
	else if(command.compare("RECEIPT") == 0){
		receiptRecieved(frame);
	}
	else if(command.compare("ERROR") == 0){
		errorRecieved(frame);
	}

    // user input frames:
    else if(command.compare("CONNECT") == 0){
        connectReceived(frame);
    }
    else if(command.compare("SUBSCRIBE") == 0){
        subReceived(frame);
    }
    else if(command.compare("UNSUBSCRIBE") == 0){
        unSubReceived(frame);
    }
    else if(command.compare("SEND") == 0){
        sendReceived(frame);
    }
    else if(command.compare("DISCONNECT") == 0){
        disconnectReceived(frame);
    }
	else{
		cout << "invalid command" << endl;
	}
}


// ~~~~~~~~~~~~~~~ Frames from the server: ~~~~~~~~~~~~~~~

void StompProtocol::connectedRecieved(Frame &frame){
	user.connect();
	cout << "Login successful" << endl;
}

void StompProtocol::messageRecieved(Frame &frame){
	string channelName = frame.getHeaderByKey("destination");
	string subId = frame.getHeaderByKey("subscription");
	string body = frame.getBody();
	cout << "~~~~~~~~~~~~~~" << endl; 
	cout << "Message recieved" << endl; 
	cout << "From channel: " << channelName << endl;
	cout << "subscriptionId: " << subId << endl;
	cout << "Message: " << body << endl;
	cout << "~~~~~~~~~~~~~~" << endl;
}

void StompProtocol::receiptRecieved(Frame &frame){
	string receiptId = frame.getHeaderByKey("receipt-id");
	string toPrint = user.getPrintMsgByReceiptId(receiptId);
	// if reciept of DISCONNECT frame:
	if (toPrint == "Disconnected succesfully"){
		user.disconnect();
	}
	if (toPrint != ""){
		// cout << "~~~~~~~~~~~~~~" << endl; // for debug
		// cout << "receipt message: 		" << endl; // for debug
		cout << toPrint << endl;
		// cout << "~~~~~~~~~~~~~~" << endl; // for debug
	}
}

void StompProtocol::errorRecieved(Frame &frame){
	string commandCaused = frame.getHeaderByKey("error caused by command");
	string msg = frame.getHeaderByKey("message");
	string errorMsg ="~~~~~~~~~~~~~~\nError Frame:\nCaused by command: " + commandCaused + "\nMessage: " + msg+"\n~~~~~~~~~~~~~~";
	cout << errorMsg << endl;
	// close connection:
	user.disconnect();
}

// ~~~~~~~~~~~~~~ Frames from the user (keyboard): ~~~~~~~~~~~~~~~

void StompProtocol::connectReceived(Frame &frame){
	user.addReceiptToFrame(frame,string("Login successful"));
	sendFrameToServer(frame);
}

void StompProtocol::subReceived(Frame &frame){
	// add receipt stuff:
	string channelName = frame.getHeaderByKey("destination");
	user.addReceiptToFrame(frame,string("Joined channel " + channelName));
	// add subscription ID stuff:
	int subId = user.addSubscription(channelName);
	frame.addHeader("id",to_string(subId));
	// send frame:
	sendFrameToServer(frame);
}

void StompProtocol::unSubReceived(Frame &frame){
	string channelName = user.getChannelBySubscription(frame.getHeaderByKey("id"));
	user.addReceiptToFrame(frame,string("Exited channel " + channelName));
	sendFrameToServer(frame);
	user.removeSubscriptionBychannel(channelName);
}

void StompProtocol::sendReceived(Frame &frame){
	user.addReceiptToFrame(frame,string(""));
	sendFrameToServer(frame);
}

void StompProtocol::disconnectReceived(Frame &frame){
	user.addReceiptToFrame(frame,string("Disconnected succesfully"));
	sendFrameToServer(frame);
	// will disconnect user when receipt will arrive - done.
}

// ~~~~~~~~~~ end of frames methods ~~~~~~~~~~~~~~~

void StompProtocol::sendFrameToServer(Frame &frame){
	string frameAsString = frame.toString();
	// send through CH:
	if(connectionHandler.sendFrameAscii(frameAsString, '\0')==false){
		cerr << "could not send frame to server" << endl;
	}
}
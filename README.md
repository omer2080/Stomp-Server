# Stomp Server Project

The community-led World Cup update subscription service will provide a platform for users to engage with each other and share real-time updates and analysis about the game. Users can subscribe to channels dedicated to their favorite teams and receive notifications about the game's progress. They can also report on important moments and share their thoughts and analysis with other subscribers.

This repository contains the implementation of a STOMP (Streaming Text Oriented Messaging Protocol) server and client. The aim of this project is to provide a simple messaging system that allows clients to communicate with each other through the server.
The server is fully implemented in Java and uses the TCP and Reactor design patterns.

## Features
- Support for sending and receiving messages between clients through the server.
- Support for multiple clients connecting to the server at the same time.
- Implementation of basic STOMP protocol commands: CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, and DISCONNECT.

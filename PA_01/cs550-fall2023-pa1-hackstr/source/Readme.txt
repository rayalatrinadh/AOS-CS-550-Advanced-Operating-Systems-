# P2P File Sharing System

This is a simple P2P file sharing system implemented in Java. It consists of:

- Server_1.java - The server that handles peer registration and searching
- ClientS.java - The client that interacts with the server
- FileInfo.java - Class to hold file metadata 

## Usage

To compile:
javac Server_1.java
javac ClientS.java

or use the makefile

make all

To run server:
java Server_1.java or make run_server

To run client:
java ClientS.java or make run_peer

The client can perform the following operations:

- Register with server to share files
- Search for files shared by other clients
- Download files from other peers

The server coordinates peers and handles searching.

## Implementation Details

The system uses Java socket programming for communication between peers and server. ObjectInputStream and ObjectOutputStream are used to send serialized Java objects between client and server.

File metadata is shared with the server on registration. The server indexes this metadata to enable searching.

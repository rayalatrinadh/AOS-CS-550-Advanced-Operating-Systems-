# Specify the Java compiler and flags
JAVAC=javac
FLAGS=

# Source and class files
SOURCES=MainPeer.java Mclient.java Mserver.java
CLASS_FILES=$(SOURCES:.java=.class)
DAT_FILES=$(wildcard *.dat)
PEER_DIRS=$(addprefix PeerFiles/Peer, $(shell seq 1 17))
DOWNLOAD_DIR=PeerFiles/downloads

# Default target
all: $(CLASS_FILES)

# Rule to compile .java files to .class
%.class: %.java
	$(JAVAC) $(FLAGS) $<

# Rule to initialize directory structure
init:
	@mkdir -p $(PEER_DIRS) $(DOWNLOAD_DIR)

# Rule to clean .class and .dat files
clean:
	rm *.class
	rm *.dat
	rm *.log.*
		

# Rule to remove the PeerFiles directory
clean-dirs:
	rm -rf PeerFiles

# Rule to run the MainPeer class (assumes you have a 'main' method in MainPeer)
run: all
	java MainPeer

.PHONY: all clean run init clean-dirs

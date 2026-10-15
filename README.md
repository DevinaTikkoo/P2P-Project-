# P2P File Sharing Project

**CNT4007 – Computer Communication Networks | Fall 2026**

**Project Group 74**

**Group Members:** Devina Tikkoo, Priyanka Jain

## Project Overview

This project is a peer-to-peer file-sharing application written in Java, following a simplified BitTorrent protocol. The goal is to allow multiple peers to connect over TCP and exchange file pieces until every peer has the complete file.

## Midpoint Progress

The following components have been implemented:

- Parsing `Common.cfg` and `PeerInfo.cfg`.
- Initializing peers using their assigned peer IDs.
- Establishing and accepting TCP connections.
- Sending, receiving, and validating 32-byte handshake messages.
- Managing established peer connections.
- Logging peer activity to individual log files.

The program compiles successfully, and TCP connections and handshakes have been tested between Peers 1001 and 1002 on localhost. The configuration includes nine peers, but connections between all nine have not yet been tested.

There are several features that have not been implemented: File transfers, bitfield messages, choking/unchoking, and completion detection.

## Compiling and Running

**Requirements:** Java JDK installed.

From the project root in a terminal, compile the source files with the following command:

```bash
javac -d out src/*.java
```

In the same terminal, start Peer 1001:

```bash
java -cp out peerProcess 1001
```

Open a separate terminal and start Peer 1002:

```bash
java -cp out peerProcess 1002
```

Start peers in the order listed in `PeerInfo.cfg`. The current configuration uses `localhost` with separate ports for each peer.

*Note: Althought not done now, for the final submittion each peer will write its activity to `log_peer_[peerID].log` in the project directory. For now, the log is created in the root...

## Repository Structure

```text
P2P-Project/
├── src/
│   ├── CommonConfig.java
│   ├── ConfigurationLoader.java
│   ├── ConnectionManager.java
│   ├── Listener.java
│   ├── PeerConnection.java
│   ├── PeerInfo.java
│   ├── PeerLogger.java
│   └── peerProcess.java
├── Common.cfg
├── PeerInfo.cfg
└── README.md
```

- `src/` – Java source files for configuration, networking, handshakes, and logging.
- `Common.cfg` – File properties and shared protocol settings.
- `PeerInfo.cfg` – Peer IDs, hostnames, ports, and initial file ownership.

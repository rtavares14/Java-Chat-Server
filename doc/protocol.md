# Protocol description

This client-server tests describes the following scenarios:

- Setting up a connection between client and server.
- Broadcasting a message to all connected clients.
- Periodically sending heartbeat to connected clients.
- Disconnection from the server.
- Handling invalid messages.

In the description below, `C -> S` represents a message from the client `C` is send to server `S`. When applicable, `C`
is extended with a number to indicate a specific client, e.g., `C1`, `C2`, etc. The keyword `others` is used to indicate
all other clients except for the client who made the request. Messages can contain a JSON body. Text shown between `<`
and `>` are placeholders.

The tests follows the formal JSON specification, RFC 8259, available on https://www.rfc-editor.org/rfc/rfc8259.html

# 1. Establishing a connection

The client first sets up a socket connection to which the server responds with a welcome message. The client supplies a
username on which the server responds with an OK if the username is accepted or an ERROR with a number in case of an
error.
_Note:_ A username may only consist of characters, numbers, and underscores ('_') and has a length between 3 and 14
characters.

## 1.1 Happy flow

Client sets up the connection with server.

```
S -> C: READY {"version": "<server version number>"}
```

- `<server version number>`: the semantic version number of the server.

After a while when the client logs the user in:

```
C -> S: ENTER {"username":"<username>"}
S -> C: ENTER_RESP {"status":"OK"}
```

- `<username>`: the username of the user that needs to be logged in.
  To other clients (Only applicable when working on Level 2):

```
S -> others: JOINED {"username":"<username>"}
```

## 1.2 Unhappy flow

```
S -> C: ENTER_RESP {"status":"ERROR", "code":<error code>}
```      

Possible `<error code>`:

| Error code | Description                              |
|------------|------------------------------------------|
| 5000       | User with this name already exists       |
| 5001       | Username has an invalid format or length |      
| 5002       | Already logged in                        |

# 2. Broadcast message

Sends a message from a client to all other clients. The sending client does not receive the message itself but gets a
confirmation that the message has been sent.

## 2.1 Happy flow

```
C -> S: BROADCAST_REQ {"message":"<message>"}
S -> C: BROADCAST_RESP {"status":"OK"}
```

- `<message>`: the message that must be sent.

Other clients receive the message as follows:

```
S -> others: BROADCAST {"username":"<username>","message":"<message>"}   
```   

- `<username>`: the username of the user that is sending the message.

## 2.2 Unhappy flow

```
S -> C: BROADCAST_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description           |
|------------|-----------------------|
| 6000       | User is not logged in |

# 3. Private message

Sends a private message from a client to specific client. The sending client does not receive the message itself but
gets a confirmation that the message has been sent.

## 3.1 Happy flow

```
C -> S: SENDTO_REQ {"username":"<reciever>","message":"<message>"}
S -> C: SENDTO_RESP {"status":"OK"}
```

- `<username>`: the username of the user that must receive the message.
- `<message>`: the message that must be sent.

Chosen client receive the message as follows:

```
S -> chosen client: SENDTO {"username":"<sender>","message":"<message>"}   
```   

- `<sender>`: the username of the user that is sending the message.
- `<reciever>`: the username of the user that must receive the message.
- `<message>`: the message that must be sent.

## 3.2 Unhappy flow

```
S -> C: SENDTO_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description                   |
|------------|-------------------------------|
| 6000       | User(sender) is not logged in |
| 6006       | User(receiver) not in system  |

# 4. List of connected clients

The server sends a list of connected clients to the client that requested it.

## 4.1 Happy flow

```
C -> S: LIST_REQ
S -> C: LIST {"clients":["<username1>","<username2>",...]}
```

- `<username1>`, `<username2>`, ...: the usernames of the connected clients.

## 4.2 Unhappy flow

```
S -> C: LIST_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description           |
|------------|-----------------------|
| 6000       | User is not logged in |

# 5. Rock Paper Scissors game

The user will request to play a game of rock-paper-scissors with the chosen client. When the first client request to
play a message will be sent to the chosen client. When the second client receives the message to play a game, they both
will enter the *game room*. The game will be played in one round. If a client wants to play and the **game room** is
full, the server will send an error message. Also when a client ask to play a game in a **game room**, the server will
send an error message.

## 5.1 Enter the game

### 5.1.1 Happy flow

```
C1 -> S: RPS_START_REQ {"username":"<player2>"}
S -> C1: RPS_START_RESP {"status":"OK"}
```

When the game room will start:

- `<player1>`: the username of the first player.
- `<player2>`: the username of the second player.
- `<username>`: the username of the user that is sending the message.

```
s -> C2: RPS_MSG {"username":"<player1>"}
S -> All: A RPS game is starting between <player1> and <player2>
```

when the second player receives the invite for the game:
both players will send to the game room and the game will start.

### 5.1.2 Unhappy flow

```
S -> C1: RPS_START_RESP {"status":"ERROR","code":<error code>}
```

Possible `<error code>`:

| Error code | Description                 |
|------------|:----------------------------|
| 6000       | User is not logged in       |
| 6006       | User not found              |
| 9001       | Game room is full           |
| 9009       | You cant play with yourself |

## 5.2 Play the game

The server will send a message to both clients when the *game room* is full and the game will start.
When the game starts, the users can type in their choice (max 10 seconds). Then the server will send a message to both
clients with the result of the round.

### 5.2.1 Happy flow

```
C1 -> s: RPS_CHOICE_REQ {"choice":"<choice>"}
C2 -> s: RPS_CHOICE_REQ {"choice":"<choice>"}
s -> C1: RPS_CHOICE_RESP {"status":"OK"}
s -> C2: RPS_CHOICE_RESP {"status":"OK"}
```

- `<choice>`: the choice of the user (rock, paper or scissors).

### 5.2.2 Unhappy flow

```
S -> C1: RPS_CHOICE_RESP {"status":"ERROR","code":<error code>}
```

Possible `<error code>`:

| Error code | Description                |
|------------|----------------------------|
| 6000       | User is not logged in      |
| 9003       | Not playing the game       |
| 9004       | Not rock paper or scissors |
| 9005       | Cant chose twice           |
| 9006       | Answer not in time         |

## 5.3 End the game

When the game ends, the server will send a message to both clients with the result of the game and make them leave the
*game room*.

### 5.3.1 Happy flow

```
S -> C1: RPS_END {"winner":"<username>" , "resultC1":"<choice>" , "resultC2":"<choice>"}
S -> C2: RPS_END {"winner":"<username>" , "resultC1":"<choice>" , "resultC2":"<choice>"}
```

- `<username>`: the winner of the game. Can be C1 , C2 or null if it is a draw.

### 5.3.2 Unhappy flow

- None

# 6 File transfer

The user will request to send a file to the chosen client. When the first client request to send a file, a message will
be sent to the chosen client. When the second client accepts to receive the file, they both will enter the *file
transfer room* that is just a term for the new thread that will open for the transfer of the file. The file will be sent
in chunks of 1024 bytes. The file will be saved in the folder `files` in the root of the project.

My sender will send a request to another client, as he will send the request to another client an uuid will be
generated. The receiver will accept the request and the file transfer will start. If the receiver does not accept the
request the file transfer will not start and the uuid will be deleted.

## 6.1 Starting the file transfer

### 6.1.1 Happy flow

```
C1 -> S: FILET_REQ {"username":"<reciever>","filepath":"<filepath>","filesize":"<filesize>","checksum":"<checksum>"}
S -> C1: FILET_RESP {"status":"OK" , "error":<error code>}
S -> C2: FILET {"username":"<sender>","filepath":"<filepath>","filesize":"<filesize>","checksum":"<checksum>"}
```

- `<reciever>`: the username of the chosen client to send the file to.
- `<sender>`: the username of the chosen client to receive the file.
- `<filepath>`: the name of the file that must be sent.
- `<filesize>`: the size of the file that must be sent.
- `<checksum>`: the checksum of the file that must be sent.
- `<uuid>`: the uuid of the file transfer.

### 6.1.2 Unhappy flow

```
S -> C1: FILET_RESP {"status":"ERROR","code":<error code>}
```

Possible `<error code>`:

| Error code | Description                  |
|------------|------------------------------|
| 6000       | User is not logged in        |
| 6006       | User not found               |
| 10000      | Cant send a file to yourself |
| 10001      | File not found               |

## 6.2 Accepting the file transfer

### 6.2.1 Happy flow

```
C2 -> S: FILET_ACP_REQ {"sender":"username"}
S -> C2: FILET_CHOICE_RESP {"status":"OK"}
```

The downloader will open a new socket connection to start the file transfer. The downloader will get file in the folder
`files` in the root of the project.

```
C2_Downloader -> S (Opens the fileT socket)
S -> C1 : FILET_START {"status":"OK","username":"<reciever>","uuid":"<uuid>"}
C1_Uploader -> S (Opens the fileT socket)
```

The file transfer will start and the file will be sent in chunks of bytes.

```
C1_Uploader -> S <<uuid> + <bytes>>
S -> C2_Downloader <<uuid> + <bytes>>
```

After the file transfer is done, the server will send a message to both clients that the file transfer is done and the
socket will be closed.

```
S -> C1: FILET_END {"status":"OK","uuid":"<uuid>"}
S -> C2: FILET_END {"status":"OK","uuid":"<uuid>"}
```

- `<reciever>`: the username of the chosen client to send the file to.
- `<uuid>`: the uuid of the file transfer.
- `<bytes>`: the bytes of the file that must be sent.

### 6.2.2 Unhappy flow

Example when the receiver does not accept the file transfer:

````
C2 -> S: FILET_REJ_REQ {"sender":"username"}
S -> C2: FILET_CHOICE_RESP {"status":"OK"}
S -> C1: FILET_REJ {"status":"error","code":<error code>}
````

```
S -> C2 or C1: FILET_RESP {"status":"ERROR","code":<error code>}
```

Possible `<error code>`:

| Error code | Description                           |
|------------|---------------------------------------|
| 10002      | Sender refused file (only for sender) |
| 10003      | Sender left                           |
| 10004      | Receiver left                         |
| 10005      | Checksum bad                          |
| 10006      | No file transfer found                |
| 10007      | This user did not send a file to you  |
| 10008      | File transfer already started         |
| 10009      | Receiver did not respond in time      |

# 7.Heartbeat message

Sends a ping message to the client to check whether the client is still active. The receiving client should respond with
a pong message to confirm it is still active. If after 3 seconds no pong message has been received by the server, the
connection to the client is closed. Before closing, the client is notified with a HANGUP message, with reason code 7000.

The server sends a ping message to a client every 10 seconds. The first ping message is send to the client 10 seconds
after the client is logged in.

When the server receives a PONG message while it is not expecting one, a PONG_ERROR message will be returned.

## 7.1 Happy flow

```
S -> C: PING
C -> S: PONG
```     

## 7.2 Unhappy flow

```
S -> C: HANGUP {"reason": <reason code>}
[Server disconnects the client]
```      

Possible `<reason code>`:

| Reason code | Description      |
|-------------|------------------|
| 7000        | No pong received |    

```
S -> C: PONG_ERROR {"code": <error code>}
```

Possible `<error code>`:

| Error code | Description       |
|------------|-------------------|
| 8000       | Pong without ping |    

# 8. Termination of the connection

When the connection needs to be terminated, the client sends a bye message. This will be answered (with a BYE_RESP
message) after which the server will close the socket connection.

## 8.1 Happy flow

```
C -> S: BYE
S -> C: BYE_RESP {"status":"OK"}
[Server closes the socket connection]
```

Other, still connected clients, clients receive:

```
S -> others: LEFT {"username":"<username>"}
```

## 8.2 Unhappy flow

- None

# 9. Invalid message header

If the client sends an invalid message header (not defined above), the server replies with an unknown command message.
The client remains connected.

Example:

```
C -> S: MSG This is an invalid message
S -> C: UNKNOWN_COMMAND
```

# 10. Invalid message body

If the client sends a valid message, but the body is not valid JSON, the server replies with a pars error message. The
client remains connected.

Example:

```
C -> S: BROADCAST_REQ {"aaaa}
S -> C: PARSE_ERROR
```
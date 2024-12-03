# Protocol description

This client-server tests describes the following scenarios:
- Setting up a connection between client and server.
- Broadcasting a message to all connected clients.
- Periodically sending heartbeat to connected clients.
- Disconnection from the server.
- Handling invalid messages.

In the description below, `C -> S` represents a message from the client `C` is send to server `S`. When applicable, `C` is extended with a number to indicate a specific client, e.g., `C1`, `C2`, etc. The keyword `others` is used to indicate all other clients except for the client who made the request. Messages can contain a JSON body. Text shown between `<` and `>` are placeholders.

The tests follows the formal JSON specification, RFC 8259, available on https://www.rfc-editor.org/rfc/rfc8259.html

# 1. Establishing a connection

The client first sets up a socket connection to which the server responds with a welcome message. The client supplies a username on which the server responds with an OK if the username is accepted or an ERROR with a number in case of an error.
_Note:_ A username may only consist of characters, numbers, and underscores ('_') and has a length between 3 and 14 characters.

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

Sends a message from a client to all other clients. The sending client does not receive the message itself but gets a confirmation that the message has been sent.

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

| Error code | Description            |
|------------|------------------------|
| 6000       | User is not logged in  |

# 3. Private message

Sends a private message from a client to specific client. The sending client does not receive the message itself but gets a confirmation that the message has been sent.

## 3.1 Happy flow

```
C -> S: SENDTO_REQ {"username":"<username>","message":"<message>"}
S -> C: SENDTO_RESP {"status":"OK"}
```
- `<username>`: the username of the user that must receive the message.
- `<message>`: the message that must be sent.

Chosen client receive the message as follows:
```
S -> chosen client: SENDTO {"username":"<username>","message":"<message>"}   
```   
- `<username>`: the username of the user that is sending the message.
- `<message>`: the message that must be sent.

## 3.2 Unhappy flow

```
S -> C: SENDTO_RESP {"status": "ERROR", "code": <error code>}
```
Possible `<error code>`:

| Error code | Description            |
|------------|------------------------|
| 6000       | User is not logged in  |
| 6006       | Username not in system |

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

| Error code | Description            |
|------------|------------------------|
| 6000       | User is not logged in  |

# 5. Rock Paper Scissors game

The user will request to play a game of rock-paper-scissors with the chosen client. When the first client request to play a message will be sent to the chosen client. When the second client accepts to play a game, they both will enter the *game room*. The game will be played in one round. If a client wants to play and the **game room** is full, the server will send an error message. Also when a client ask to play a game in a **game room**, the server will send an error message.

## 5.1 Enter the game 
### 5.1.1 Happy flow

```
C1 -> S: RPS_START_REQ {"username":"<username>"}
S -> C1: RPS_START_RESP {"status":"OK"}
s -> C2: RPS_START {"username":"<username>"}
```
When the game room will start:
- `<username>`: the username of the chosen client to play with.

### 5.1.2 Unhappy flow

```
S -> C1: RPS_RESP {"status":"ERROR","code":<error code>}
```

Possible `<error code>`:

| Error code | Description               |
|------------|---------------------------|
| 6000       | User is not logged in     |
| 6006       | User not found            |
| 9000       | Already playing in a game |
| 9001       | Game room is full         |

## 5.2 Play the game

The server will send a message to both clients when the *game room* is full and the game will start.
When the game starts, the users can type in their choice (max 10 seconds). Then the server will send a message to both clients with the result of the round. The game ends when one of the clients wins 3 rounds.

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
| 9005       | Answer not in time         |
| 9006       | Cant chose twice           |


## 5.3 End the game

When the game ends, the server will send a message to both clients with the result of the game and make them leave the *game room*.

### 5.3.1 Happy flow

```
S -> C1: RPS_END {"winner":"<username>" , "resultC1":"<choice>" , "resultC2":"<choice>"}
S -> C2: RPS_END {"winner":"<username>" , "resultC1":"<choice>" , "resultC2":"<choice>"}
```
- `<username>`: the winner of the game. Can be C1 , C2 or null if it is a draw.

### 5.3.2 Unhappy flow
- None

# 6. Heartbeat message

Sends a ping message to the client to check whether the client is still active. The receiving client should respond with a pong message to confirm it is still active. If after 3 seconds no pong message has been received by the server, the connection to the client is closed. Before closing, the client is notified with a HANGUP message, with reason code 7000.

The server sends a ping message to a client every 10 seconds. The first ping message is send to the client 10 seconds after the client is logged in.

When the server receives a PONG message while it is not expecting one, a PONG_ERROR message will be returned.

## 6.1 Happy flow

```
S -> C: PING
C -> S: PONG
```     

## 6.2 Unhappy flow

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

| Error code | Description         |
|------------|---------------------|
| 8000       | Pong without ping   |    

# 7. Termination of the connection

When the connection needs to be terminated, the client sends a bye message. This will be answered (with a BYE_RESP message) after which the server will close the socket connection.

## 7.1 Happy flow
```
C -> S: BYE
S -> C: BYE_RESP {"status":"OK"}
[Server closes the socket connection]
```

Other, still connected clients, clients receive:
```
S -> others: LEFT {"username":"<username>"}
```

## 7.2 Unhappy flow

- None

# 8. Invalid message header

If the client sends an invalid message header (not defined above), the server replies with an unknown command message. The client remains connected.

Example:
```
C -> S: MSG This is an invalid message
S -> C: UNKNOWN_COMMAND
```

# 9. Invalid message body

If the client sends a valid message, but the body is not valid JSON, the server replies with a pars error message. The client remains connected.

Example:
```
C -> S: BROADCAST_REQ {"aaaa}
S -> C: PARSE_ERROR
```
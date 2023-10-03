# UDP Server-Client Implementation

Program that sends UDP packages from Server to Client, implementing a "gremlin" function to randomly damage certain packages and checksum to see if a package needs to be re-sent.

## How to run:

First thing you want to do is compile all of the files. Run:

```
javac *.java
```

Next we want to start up our UDP Server. Run:

```
java UDPServer
```

Lastly, we want to open up another terminal window and run the UDP Client:

```
java UDPClient
```
## Output

The results should look like this for the client:

![UDP Server Gif](https://github.com/jacobomantilla10/UDP_Server-Client/UDPClient.gif)

And like this for the server:

![UDP Client Screen Grab](https://github.com/jacobomantilla10/UDP_Server-Client/UDPServerScreenGrab.png)

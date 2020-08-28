# WebSocket support

## Choosing a library

The WebSocket server implementation was designed to be pluggable, so if the stability or performance
of Java-WebSocket is found to be lacking, we can switch it out. Make sure that whatever library is used
meets all of the following requirements:

#### WebSocket Server Requirements (not an exhaustive list)

* During shutdown of the server, all of the individual WebSocket connections should be closed cleanly if possible.
    * `FtcWebSocket.onClose()` must be called for every WebSocket connection, whether it closes cleanly or not.
    * If all of the connections fail to close in a timely manner, it should time out and force-close
        them (making sure that `onClose()` is called).
* The server must handle sending pings and listening for pongs.
* The library must be thread-safe.
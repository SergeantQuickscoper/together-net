# Together Net

Together Net is an BLE peer-to-peer chat app, that can be used to send chat messages without internet or cell tower connection. Below are a list of implemented classes and their functionality to give an overview of the app architecture, as well as a guide to running the app on your own Android devices.

## Fully implemented classes:
- **Discovery Manager** - Gets immediate adjacent neighbors to the current node using BLE Scanning and advertising, and also responsible for fetching required data for the Mesh Manager.

- **Message Manager** - Starts a BLE GATT server instance for the recieving of messages. The 517 byte message packet contains a source and destination node ID alongside the message and metadata to know if the message is to be forwarded or stored, where forwarded messages and determining the next node is decided by the Mesh Manager.

- **Node DAO** - Responsible for storing and fetching node data from the local SQLite database, classes and their functions. Used by the Mesh Manager and Discovery Manager, to store and fetch node data.

- **Message DAO** - Responsible for storing and fetching message data from the local SQLite database. Used by the Message Manager to store and fetch message data. Messages are stored and sent with their source and destination unique node IDs, to ensure proper routing.

- **UI Activities** - Provides the user interface screens for composing, viewing, and managing chats.

- **GATT Server Manager** - Manages the BLE GATT server lifecycle used to receive incoming messages and characteristics handling. Executed by the Message Manager to accept and persist incoming message packets.

- **GATT Client Manager** - Manages BLE GATT client operations for connecting to nearby peers, discovering services, and writing message characteristics. Executed by the Message Manager to send messages and forward packets to the next hop.

## Future expansion and partially implemented classes
- **Mesh Manager** - Responsible for deciding the next hop node for a message to be sent to, based on the data provided by the Discovery Manager and the Node DAO. Implements a basic routing algorithm (current choice is DSDV) that chooses the next hop based on the least number of hops to the destination node.

- **ACK system** - Some way to tell the sender that their message has been sent, this could be done by just sending a confirmation token back through the mesh to the sender and then displaying a delivered sign.

- **End to End encryption system** - Encrypt user messages such that only the sender and reciever have access to message content.

## Guide to running it yourself
- Development environment - Use Android Studio (latest stable release).
- Devices - At least 2 physical Android phones that support BLE are required to test peer-to-peer messaging. **Ensure location and Bluetooth are enabled on the devices.**
- Clone the repository:
   `git clone https://github.com/SergeantQuickscoper/together-net`
   
- Ways to run the app on devices:
  - **Option 1** - Pair to Android Studio: Connect each phone to your development machine (USB + enable USB debugging / ADB) and run the app from Android Studio to install and launch on the devices.
  - **Option 2** — Build APK and install: Build a release or debug APK in Android Studio, transfer the APK to each phone and install it, then launch the app.
- **Important Notes**: Ensure Bluetooth and location permissions are granted on each device and precise location permission is granted.

Authored by: Don Roy Chacko <donisepic30@gmail.com>
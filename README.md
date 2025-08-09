# Together Net

Together Net is a BLE peer-to-peer chat app, that can be used to send chat messages without internet or cell tower connection. 

## Planned out design:
- BLE Manager - Gets immediate adjacent neighbors to the current node, and also responsible for fetching required data for the Mesh Manager (phone)
- Mesh Manager - Responsible for creating the routing table, and updating it based on reachable nodes.
- Message Manager - Implements the store and forward logic, for sent messages and recieved messages that are supposed to be forwarded to the next node decided by the Mesh Manager
- ACK system - Some way to tell the sender that their message has been sent, this could be done by just sending a confirmation token back through the mesh to the sender and then displaying a delivered sign.
- End to End encryption system - Encrypt user messages such that only the sender and reciever have access to message content.

Authored by: Don Roy Chacko <donisepic30@gmail.com>

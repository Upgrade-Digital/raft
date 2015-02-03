Raft Distributed Consensus Protocol.
====================================

The goal of this project is to provide a reference implementation of the Raft 
distributed election protocol described at https://raftconsensus.github.io/

The original goal of this project was to provide a reliable distributed election
implementation to allow for fine grained election based resource management 
where resources are contended. This use case is very similar to the partition
ring design used by the Cockroach DB team https://github.com/cockroachdb/cockroach
with the fundamental difference being that this implementation was explicitly 
intended to be used outside the data layer. That being said, thanks to the guys
from Cockroach DB for the inspiration at Fosdem 2015 to try and finish this 
implementation. 

The goal is to only implement the 'Raft' parts to allow for multiple concurrent
mutators to 'elect' themselves. The goal being that in the most general case
that with N resources the protocol would guarantee that only one 'actor' was 
mutating a resource at a given time.

The process is that when an actor wishes to mutate a resource it would attempt
to be elected, at this point the Raft protocol takes over. Once elected the 
actor would perform the work. The election would, as with the general protocol
support sustained 'ownership' and reservation of that resource while the elected
actor continues to work. The resource may though not at a given time have an 
elected worker.

The goal is minimal configuration and minimal assumptions about the transport
for communication between raft nodes. We will here provide an implementation 
that supports running in a servlet container and will as far as possible
abstract the different functional dependencies to allow use in different 
contexts.


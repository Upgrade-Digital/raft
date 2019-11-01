Distributed Commit Replication with Raft
========================================

The goal of this project is to create a library which implements distributed
commit log replication which can be used by a variety of processing,
replication and election systems. To achieve this the library manages the
distribution of consensus based commits to a number of consumers. Multiple
clients use the library to coordinate commits via writer election using the
Raft algorithm.

While a number of alternative reference implementation of the Raft protocol
exist today they are typically implemented as part of a storage system or
distributed election protocol, more details at
https://raftconsensus.github.io/.

# Commit Replication

To create a clear layered model for commit replication this implementation
defines a number of concepts related to those defined in the Raft protocol

* Level 1: Client which emits commits to be replicated
* Level 2: Library (referred to as the server in the Raft docs) which handles
  the election of a master, intermittent log state and shipping of updates and
  state management
* Level 3: Consumer which ingests commits from the library.

The concept here is that the library could support multiple usage scenarios
for commit replication for example:

1. Distributed file writes where the library can be used as a proxy for writing
   a file across data centers for replication purposes.
2. Replicating writes to a CRUD bucket (Creates, Updates, Deletes).
3. Shipping block based write replication e.g. for database applications where
   binary log replication could be of use.

## Values / Approach

This library is primarily concerned with the replication and as such tries to
avoid implying any specific implementation for other parts of the system. To
achieve this the goals of the project include:

* Separation of the replication via election from client and consumer spaces.
* Generic implementation with low level access to principles.
* Efficiency of implementation and protocol to ensure viability of the library
  for high performance applications.
* Maximising SOLID / DRYness / KISS principles
  * Singular responsibility of the library for dealing with commit replication
    between clients and consumers of commits.
  * Open / closed: at a micro level within the implementation but also through
    elegant interfaces that make extending functionality trivial.
  * Liskov substitution: provide clear interfaces to allow numerous
    implementation to be supported including a strong acceptance testing suite
    including simulation based test suites.
  * Interface segregation: Lots of small interfaces with strong encapsulation
    of specific roles.
  * Dependency inversion: Rely heavily on interface definitions.

# Contributing / Building

Please refer to the BUILDING.md file for instructions on building the library.

Contributions are welcome to the project. Please consider:

* Submitting issues.
* Requesting features.
* Documenting use case scenarios.
* Send a pull request!

# Future Work
* Alternative client and consumer samples.
* Master / server mode with weighted election.
* Multi partition election to spread load across server set.
* Locality weighting.
* Rack awareness.

## Historical Goals
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


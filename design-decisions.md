# Paradigm
While evaluating different architectural paradigms for the implementation a
number of options were considered:

* Synchronous implementation in line with the general design of Raft.
* Async transport between peers using callback response.

For the initial version it was decided to use a synchronous approach as this
is more in keeping with the original design principles for RAFT.

# Orchestration

## Thread management

In keeping with current approaches in Java for thread management initially
using an exector service to handle thread management (scaling). The primary
reason is that this allows the parameterisation of the execution limits by
users of the library. It also has the benefit of increasing dependency
inversion of control and injectability.

## Scheduling

To avoid in wait / sleep semantics the current plan is to make use of the
scheduled executor service for activities like leader timeout. The goal is to
minimise waiting semantics and avoid excessive locking for events like
append leader refresh.

# Testing

In order to facilitate unit and system testing the Raft class supports
parameterisation of fakeable / mockable dependencies. The goal of this is to
allow algorithmic testing of the implementation without needing to rely on
concurrent simulation or exhaustive testing.

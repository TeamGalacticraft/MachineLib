# Wire Network Design

## Overview 

### Goals

* Fast energy transfer
  * Energy transfer performance is prioritized over network graph modification speed
    * Network changes should be rare compared to energy transfer
* Works across unloaded chunk borders
  * If a player has a power generation station far from their base, as long as the station and the base are loaded, energy should still be transferred.
    * Intermediate chunks should NOT have to be loaded
    * Setups must persist across world loads
* Do not break when `/setblock`, `/fill`, and company are used
* Support limiting flow (NYI)
* Support mixing of wires of different flow rates at the block level (NYI)
* Support regeneration of entire network graph via a command (NYI)
  * Verify and/or fix persistent state

### Outline

* No `BlockEntity`s.
  * Keep track of networks within the `Level`
    * Easier to keep track of global state
  * Keep track of connections using `BlockState`s
    * Ensures that visuals always line up to functionality
  * Rely on block placement

## Implementation Details

### `WireBlock`

### `WireSegment`

### `WireNetworkManager`


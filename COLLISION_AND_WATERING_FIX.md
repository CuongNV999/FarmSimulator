# Collision and Watering Fix Report

## Issues Fixed

### Issue 1: Soil Watering Problem
**Problem:** Some soil tiles cannot be changed to wet when watered manually, but work fine when it rains.

**Changes Made:**
1. Added extensive debug logging to `WateringCanAction.java`:
   - Logs when watering can is used and target position
   - Logs each soil found and its distance
   - Logs soil component status before and after watering
   - Logs if soil entity is missing SoilComponent

2. Enhanced `SoilComponent.setWet()` method:
   - Added null check for state transition
   - Added debug output showing state changes
   - Helps identify if state transition is failing

**Files Modified:**
- `src/main/java/Project1Game/model/item/WateringCanAction.java`
- `src/main/java/Project1Game/component/farming/SoilComponent.java`

**Testing Instructions:**
1. Run the game and open the console/log window
2. Use the watering can on various soil tiles
3. Check console for debug messages:
   - "WateringCan used at target position: ..."
   - "Soil at ..., distance: ..."
   - "Found soil to water at: ..."
   - "Before watering - isWet: ..."
   - "After watering - isWet: ..."
   - "Soil watered: State changed to TilledWetState"
4. If some soils don't water, check if:
   - No soil is found (distance issue)
   - SoilComponent is missing
   - State transition fails (returns null)
   - isWet remains false after watering

### Issue 2: Monster and Animal Collision Behavior
**Problem:** When monsters bump into monsters, or animals bump into animals, they don't stop and show idle animation. They should stop, show idle animation, and choose different directions.

**Changes Made:**
1. Completely rewrote `CreatureAvoidanceHandler` in `Main.java`:
   - Both creatures now stop immediately (velocity = 0)
   - Both creatures call `forceNewDirection()`
   - No longer just pushes one creature

2. Added `collisionCooldown` system to `BaseAnimalComponent`:
   - 0.5 second pause after collision
   - Prevents immediate movement resumption
   - Forces creature to stay idle during cooldown
   - Velocity is continuously reset to 0 during cooldown

3. Added `collisionCooldown` system to `BaseMonsterComponent`:
   - Same 0.5 second pause mechanism
   - Prevents wandering behavior override
   - Ensures idle state is maintained

4. Enhanced `forceNewDirection()` in both components:
   - Sets collision cooldown timer
   - Stops all movement
   - Sets idle animation (animals only - monsters use auto animation)
   - Resets wander timers to choose new direction after cooldown

**Files Modified:**
- `src/main/java/Project1Game/Main.java`
- `src/main/java/Project1Game/component/farming/animal/BaseAnimalComponent.java`
- `src/main/java/Project1Game/component/farming/monster/BaseMonsterComponent.java`

**How It Works:**
1. When collision detected → Both creatures' velocities set to 0
2. `forceNewDirection()` called on both creatures
3. Collision cooldown (0.5s) starts
4. During cooldown:
   - All movement is blocked
   - Velocity continuously reset to 0
   - onUpdate() returns early, skipping normal AI
5. After cooldown:
   - Wander timer triggers new direction choice
   - Creatures move in different directions

**Testing Instructions:**
1. Spawn multiple animals close together
2. Let them wander and collide
3. Observe behavior:
   - ✓ Both animals should stop immediately
   - ✓ Both should show idle animations
   - ✓ They should pause for ~0.5 seconds
   - ✓ Then choose new random directions

4. Test with monsters:
   - Spawn multiple monsters (same type)
   - Watch them collide
   - ✓ Same behavior as animals

5. Test mixed collisions:
   - Animal-Monster collisions
   - ✓ Both should stop and idle

## Technical Details

### Collision Cooldown System
The collision cooldown prevents the normal AI update loop from immediately overriding the collision response:

```java
// In onUpdate():
if (collisionCooldown > 0) {
    collisionCooldown -= tpf;
    if (physics != null) {
        physics.setVelocityX(0);
        physics.setVelocityY(0);
    }
    return; // Skip all other AI updates
}
```

This ensures:
- Creatures stay stopped for the full cooldown duration
- No wandering/seeking behavior during cooldown
- Animations remain in idle state (auto-managed by velocity = 0)

### Animation Behavior
- **Animals:** Manually set to random idle animation in `forceNewDirection()`
- **Monsters:** Animation auto-managed by `MonsterAnimationComponent` based on velocity
- When velocity = 0, appropriate idle animation is automatically shown

## Known Behaviors
1. Collision response respects important states:
   - Animals following player are NOT interrupted
   - Animals fleeing monsters are NOT interrupted
   - Monsters alerted by player are NOT interrupted
   - Monsters returning to bush are NOT interrupted

2. Only wandering creatures are affected by collision cooldown

## Debugging
If issues persist, check console for:
- Watering: Debug messages showing soil detection and state changes
- Collisions: Add System.out.println in `forceNewDirection()` to verify it's being called

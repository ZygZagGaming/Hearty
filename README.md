# Hearty
 
A Forge mod that provides several features that change how health is displayed in Minecraft.

## Features

### Half-container hearts
If the player has an odd max health, the final heart will display a half-container as opposed to a full one.

### Half-heart merging
If the player has an odd max health, the final half heart will be completed with the first absorption half-heart.

### Double hearts (configurable)
Makes each heart contain 1hp instead of 2hp.

### Heart display priority (configurable)
Makes withered hearts display when the player has both wither and poison.

### Custom hearts
Using IMCs, other mods can register their own custom hearts.
  
Each heart is serialized with the type `oshi.util.tuples.Triplet<Double, org.apache.common.lang3.function.TriFunction<Player, Level, Gui, ResourceLocation>, org.apache.common.lang3.function.TriFunction<Player, Level, Gui, Integer>>`. It's a bit of a mouthful.
  
The `Double` is the heart's priority, e.g. what order hearts are in. For reference, full hearts are 1.0, empty is 2.0, and absorption is 3.0.  
The first `TriFunction` is a provider for the heart's texture as a ResourceLocation. For example, `new ResourceLocation("namespace:textures/foo/bar/heart.png")` would correspond to the texture at `assets/namespace/textures/foo/bar/heart.png`.  
The second `TriFunction` is a provider for the number of hearts to render at any given time.  
  
You can send an IMC during the `InterModEnqueueEvent` using a line like this:
```java
InterModComms.sendTo(
        "hearty",
        "register_heart",
        () -> triplet
);
```
where `triplet` is of the aforementioned type.

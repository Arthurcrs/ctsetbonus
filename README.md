## About the mod

This mod provides some integration between the mod [Set Bonus](https://www.curseforge.com/minecraft/mc-mods/set-bonus) and [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker) (it requires both of them to work).

1.  Allows defining Sets and Bonuses from a zs script instead of the Set Bonus config. This allows the definition of the sets and its effects in a single file for each set, also, avoids needing to create ids for equipment. It might be more convenient to define sets in that way.
2.  Provides a method to verify if the player has a current set bonus in a zs script, allowing for more customizable effects, mainly because it can be used in CraftTweaker Events to check if the player has the set bonus then do something.
3.  Proves a method to get how many set pieces the player has of a given set. This can be used to add "gradual bonuses" such as dealing more damage (for example) the more pieces of a given set the player has.

<br/>

<span style="color:#e67e23">About the Beta:</span> I made some basic testing and it seems to be fine. If you have any suggestion or bugs please point it out. I will be further testing in the following weeks.

<span style="color:#f1c40f">Warnings:</span>

*   Avoid using "/setbonus resetconfig" when sets are defined using scripts. This command clears all sets and reloads only the config file, using it will remove the sets added with scripts.
*   Either define all of then in the config file or with scrips. It will probrably work using both of the config and scripts, but some conflicts might happen in edge cases.

### IPlayer extensions:

<span style="color:#169179;">hasSetBonus(String bonusId)</span>
<div class="spoiler">
Returns true if the player has a given set bonus.

```
events.onPlayerAttackEntity(function(event as PlayerAttackEntityEvent) {
    val attacker = event.player; // IPlayer
    val count = attacker.getSetPieceCount("Diamond");
    if (attacker.hasSetBonus("diamondFull")){
        attacker.sendChat("Has the set bonus!");
    }
});
```
</div>
<span style="color:#169179;">getSetPieceCount(String setName)</span>
<div class="spoiler">
Returns the number of pieces the player has of a given set.

```
events.onPlayerAttackEntity(function(event as PlayerAttackEntityEvent) {
    val attacker = event.player; // IPlayer
    val count = attacker.getSetPieceCount("Diamond");
    attacker.sendChat("Diamond pieces: " + count);
});
```
</div>

### Import

To use the methods to create sets, bonuses and link elements to bonuses (same as the config of Set Bonus) remember to import the class of this addon in the .zs script:

```
import ctsetbonus.SetTweaks as SB;
```

### Methods to link equipments to sets:

<span style="color:#169179;">addEquipToSet(String setName, String slotString, String equipRL)</span>
<div class="spoiler">
Adds a single item to a slot in a set. Creates the set if missing. Adding multiple items to the same slot works as an "or", so the set will accept any of the items added in that slot.

```
SB.addEquipToSet("Leather", "head", "minecraft:leather_helmet");
```
</div>
<span style="color:#169179;">addEquipToSet(String setName, int slotInt, String equipRL)</span>
<div class="spoiler">
Same as above, but with slot index.

```
SB.addEquipToSet("Leather", 36, "minecraft:leather_boots");
```
</div>
<span style="color:#169179;">addEquipToSet(String setName, String slotString, String\[\] equipsRL) </span>
<div class="spoiler">
Adds items to a single slot in a set.

```
val swords = [
    "minecraft:wooden_sword",
    "minecraft:stone_sword",
    "minecraft:iron_sword"
    ];
SB.addEquipToSet("BladeDancer", "mainhand", swords)
```
</div>
<span style="color:#169179;">addEquipToSet(String setName, int slotInt, String\[\] equipsRL)</span>
<div class="spoiler">
Same, but using the slot index.

```
val boots = [
    "minecraft:leather_boots", 
    "minecraft:chainmail_boots"
    ];
SB.addEquipToSet("MixedBoots", 36, boots);
```
</div>

### Methods to link bonus to sets:

<span style="color:#169179;">addBonusToSet(String bonusID, String bonusDescription, String setName)</span>
<div class="spoiler">
Links a bonus to a full set (with discovery method set to "always visible"). Creates the bonus if missing.

```
SB.addBonusToSet("diamondFull", "Unbreakable poise", "Diamond");
```
</div>
<span style="color:#169179;">addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts)</span>
<div class="spoiler">
Links a bonus that activates at a specific piece count (also with discovery method "always visible").

```
SB.addBonusToSet("iron2pc", "Steadfast", "Iron", 2);
```
</div>
<span style="color:#169179;">addBonusToSet(String bonusID, String bonusDescription, String setName, int numberOfParts, int discoveryMode)
</span>
<div class="spoiler">
Adds with explicit discovery mode: <br />
0 = hidden until the player has activated the bonus at least once<br />
1 = always visible<br />
2 = always hidden<br />

To require the entire set, use "-1" for "numberOfParts"

```
SB.addBonusToSet("goldFull", "Gilded vigor", "Gold", -1, 1); // full set, visible
```
</div>

### Methods to link bonus elements to bonuses:

<span style="color:#169179;">addPotionEffectToBonus(String bonusID, String effectRL, int level)</span>
<div class="spoiler">
Permanent effect (no interval) at the given amplifier.

```
SB.addPotionEffectToBonus("leatherFull", "minecraft:speed", 0);
```
</div>
<span style="color:#169179;">addPotionEffectToBonus(String bonusID, String effectRL, int level, int duration, int interval)</span>
<div class="spoiler">
Timed effect with refresh interval (ticks).

```
SB.addPotionEffectToBonus("chain3pc", "minecraft:strength", 0, 200, 40); // 10s, tick every 2s
```
</div>
<span style="color:#169179;">addAttributeModToBonus(String bonusID, String attribute, double amount, String operation)</span>
<div class="spoiler">
Adds an attribute modifier. operation: "add", "mult_base", "mult_total". operation: "add", "mult_base", "mult_total".

```
SB.addAttributeModToBonus("diamondFull", "generic.armor", 0.20, "mult_base");
```
</div>
<span style="color:#169179;">addEnchantmentToBonus(String bonusID, String slotString, String enchantRL, int level)</span>
<div class="spoiler">
Applies an enchantment (vanilla behavior, mode 0) to whatever item is in the slot.

```
SB.addEnchantmentToBonus("iron2pc", "head", "minecraft:respiration", 1);
```
</div>

<span style="color:#169179;">addEnchantmentToBonus(String bonusID, String slotSpec, String itemRL, String enchantRL,int level, int mode)</span>
<div class="spoiler">
Applies an enchantment to a specific item in the slot, with a given mode, modes are:

0 = Vanilla enchantment combination behavior<br />1 = Vanilla behavior, but without limits (can go above max level)<br />2 = Set the level directly, overriding whatever level it might've had before<br />3 = Add to the existing level (can be used to subtract from existing level if you put in a negative level number)<br />4 = Add to existing level, without limits<br />

```
SB.addEnchantmentToBonus("goldFull", "mainhand", "minecraft:golden_sword", "minecraft:fire_aspect", 1, 4);
```
</div>
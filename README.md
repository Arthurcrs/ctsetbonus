
## About the mod

This mod provides some integration between the mod [Set Bonus](https://www.curseforge.com/minecraft/mc-mods/set-bonus) and [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker) (it requires both of them to work).

1.  Allows defining Sets and Bonuses from a zs script instead of the Set Bonus config. This allows the definition of the sets and its effects in a single file for each set, also, avoids needing to create ids for equipment. It might be more convenient to define sets in that way.
2.  Provides a method to verify if the player has a current set bonus in a zs script, allowing for more customizable effects, mainly because it can be used in CraftTweaker Events to check if the player has the set bonus then do something.
3.  Proves a method to get how many set pieces the player has of a given set. This can be used to add "gradual bonuses" such as dealing more damage (for example) the more pieces of a given set the player has.

<br/>

### <span style="color:#E67E23;">About the Beta</span>
I made some basic testing and it seems to be fine. If you have any suggestion or bugs please point it out. I will be further testing in the following weeks.

### <span style="color:#F1C40F;">Warnings</span>
* Avoid using `/setbonus resetconfig` when sets are defined using scripts. This command clears all sets and reloads only the config file, using it will remove the sets added with scripts.
* Either define all of them in the config file or with scripts. It will probably work using both config and scripts, but some conflicts might happen in edge cases.

---

## IPlayer Extensions

### hasSetBonus(String bonusName)
Returns true if the player has a given set bonus.
```zenscript
events.onPlayerAttackEntity(function(event as PlayerAttackEntityEvent) {
    val attacker = event.player; // IPlayer
    val count = attacker.getSetPieceCount("Diamond");
    if (attacker.hasSetBonus("diamondFull")){
        attacker.sendChat("Has the set bonus!");
    }
});
```

### getSetPieceCount(String setName) 
Returns the number of pieces the player has of a given set.
```zenscript
events.onPlayerAttackEntity(function(event as PlayerAttackEntityEvent) {
    val attacker = event.player; // IPlayer
    val count = attacker.getSetPieceCount("Diamond");
    attacker.sendChat("Diamond pieces: " + count);
});
```

---

## Import

To use the methods to create sets, bonuses and link elements to bonuses (same as the config of Set Bonus) remember to import the class of this addon in the `.zs` script:

```zenscript
import ctsetbonus.SetTweaks as SB;
```

---

## Methods to Link Equipment to Sets

### addEquipToSet(String setName, String slot, String equipRL)
Adds a single item to a slot in a set. Creates the set if missing. Adding multiple items to the same slot works as an "or", so the set will accept any of the items added in that slot.
```zenscript
SB.addEquipToSet("Leather", "head", "minecraft:leather_helmet");
```

### addEquipToSet(String setName, int slot, String equipRL)
Same as above, but with slot index.
```zenscript
SB.addEquipToSet("Leather", 36, "minecraft:leather_boots");
```

### addEquipToSet(String setName, String slot, String[] equipsRL)
Adds items to a single slot in a set.
```zenscript
val swords = [
    "minecraft:wooden_sword",
    "minecraft:stone_sword",
    "minecraft:iron_sword"
];
SB.addEquipToSet("BladeDancer", "mainhand", swords);
```

### addEquipToSet(String setName, int slot, String[] equipsRL)
Same, but using the slot index.
```zenscript
val boots = [
    "minecraft:leather_boots", 
    "minecraft:chainmail_boots"
];
SB.addEquipToSet("MixedBoots", 36, boots);
```

---

## Methods add Set requirements to Bonus

### addSetReqToBonus(String bonusName, String bonusDescription, String setName)
Adds a Full Set as a requirement to a Bonus (with discovery method set to "always visible"). Creates the bonus if missing.
```zenscript
SB.addSetReqToBonus("diamondFull", "Unbreakable poise", "Diamond");
```

### addSetReqToBonus(String bonusName, String bonusDescription, String setName, int numberOfParts)
Adds a specific number of items of a Set as a requirement for a Bonus (also with discovery method "always visible").
```zenscript
SB.addSetReqToBonus("iron2pc", "Steadfast", "Iron", 2);
```

### addSetReqToBonus(String bonusName, String bonusDescription, String setName, int numberOfParts, int discoveryMode)
Adds with explicit discovery mode:  
0 = hidden until the player has activated the bonus at least once  
1 = always visible  
2 = always hidden  

To require the entire set, use `-1` for `numberOfParts`.
```zenscript
SB.addSetReqToBonus("goldFull", "Gilded vigor", "Gold", -1, 1); // full set, visible
```

---

## Methods to Link Bonus Elements to Bonuses

### addPotionEffectToBonus(String bonusName, String effectRL, int level)
Permanent effect (no interval) at the given amplifier.
```zenscript
SB.addPotionEffectToBonus("leatherFull", "minecraft:speed", 0);
```

### addPotionEffectToBonus(String bonusName, String effectRL, int level, int duration, int interval)
Timed effect with refresh interval (ticks).
```zenscript
SB.addPotionEffectToBonus("chain3pc", "minecraft:strength", 0, 200, 40); // 10s, tick every 2s
```

### addAttributeModToBonus(String bonusName, String attribute, double amount, String operation)
Adds an attribute modifier. Operation: "add", "mult_base", "mult_total".
```zenscript
SB.addAttributeModToBonus("diamondFull", "generic.armor", 0.20, "mult_base");
```

### addEnchantmentToBonus(String bonusName, String slot, String equipRL, String enchantRL, int level)
Applies an enchantment to a specific item in the slot using the vanilla enchantment combination behavior.

```zenscript
SB.addEnchantmentToBonus("goldFull", "mainhand", "minecraft:golden_sword", "minecraft:fire_aspect", 1);
```

### addEnchantmentToBonus(String bonusName, String slot, String equipRL, String enchantRL, int level, int mode)
Applies an enchantment to a specific item in the slot, with a given mode:  
0 = Vanilla enchantment combination behavior  
1 = Vanilla behavior, but without limits (can go above max level)  
2 = Set the level directly, overriding whatever level it might've had before  
3 = Add to the existing level (can be used to subtract from existing level if you put in a negative level number)  
4 = Add to existing level, without limits  

```zenscript
SB.addEnchantmentToBonus("goldFull", "mainhand", "minecraft:golden_sword", "minecraft:fire_aspect", 1, 4);
```

### addEnchantmentToBonus(String bonusName, String slot, String equipRL, String enchantRL, int level, String mode)
Same thing as the previous, but using a String instead of a code.
"vanilla" = Vanilla enchantment combination behavior  
"vanilla_unlimited" = Vanilla behavior, but without limits (can go above max level)  
"override" = Set the level directly, overriding whatever level it might've had before  
"additive" = Add to the existing level (can be used to subtract from existing level if you put in a negative level number)  
"additive_unlimited" = Add to existing level, without limits  

```zenscript
SB.addEnchantmentToBonus("goldFull", "mainhand", "minecraft:golden_sword", "minecraft:fire_aspect", 1, "vanilla_unlimited");
```
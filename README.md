# WackyUpdate-V1

A Fabric mod for **Minecraft 1.21.1** (Java 21, Gradle, Fabric API).

```
./gradlew build          # -> build/libs/WackyUpdate-V1-1.0.0.jar
./gradlew runClient      # dev client
```

Drop the jar plus [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.1 into your `mods` folder.

---

## Speculum (the crystal ore)

- **Speculum Pockets** are small hollow crystal geodes found deep underground (y -56 to 6, about one per 12 chunks). They have a tuff shell lined with randomly coloured prism glass and **Budding Speculum**.
- Budding Speculum grows **Small → Medium → Large Speculum Bud → Speculum Cluster**, just like amethyst.
- Once a cluster is fully grown, it pushes out a **Prism Glass** block in a random colour in front of itself.
- Mine a cluster with a pickaxe to get **1 Speculum Shard**. Fortune works. Silk Touch gives you the cluster itself. Unfinished buds drop nothing without Silk Touch.

### Prism Glass
- Clear glass in 16 colours that **connects seamlessly**: faces between neighbouring prism glass are hidden.
- It **glows inwards but not outwards**: it renders full-bright, but its light level is 0, so it never lights up its surroundings.
- Mine it without Silk Touch and it keeps its colour.
- Right-click it with a **dye** to recolour it, or with a **Speculum Shard** to roll a random colour.
- Recipe: 8 glass around 1 shard gives 8 prism glass.

### Things made from speculum
| Item | Notes |
|---|---|
| **Speculum Armor** (4 pieces) | Full crystallised-glass armour. **22 armour points + 2.5 toughness** (diamond is 20 + 2.0), but **durability multiplier 22** (diamond is 33). Standard armour recipes using shards. |
| **Speculum Lantern** | Light level 15. **Fireflies drift around it in a 9×9 area.** Iron nuggets + shard + glowstone dust. |
| **Spectre Lens** | One of the two non-armour speculum items (the lantern is the other). See below. |

No speculum tools exist, on purpose.

### Spectre Lens
- **Lens in one hand + any shovel in the other** reveals hidden **gravel mites** within 12 blocks. Their gravel gets a pulsing outline you can see through walls, and rainbow sparks fizz out of it.
- **Use the lens on its own** for *Spectre Sight*: every creature within 24 blocks glows through walls for 6 seconds. 15-second cooldown, 128 uses.

## Gravel Mites → Feet → Rollerskates
- **Mite Gravel** looks exactly like gravel and generates rarely *inside* gravel deposits. Dig it up and 1–2 **Gravel Mites** scurry out. With Silk Touch you get the infested block instead.
- Mites are skittish. They run from you and, given a moment, burrow back into any gravel they touch.
- Each mite drops **1–2 Mite Feet** (Looting helps).
- **10 feet = 1 Compacted Feet Block.** A crafting grid only has 9 slots, so you squish them by hand: hold 10+ feet, **sneak + use**. The block is bouncy (half-slime). Crafting the block back gives 10 feet.
- **Rollerskates**: leather boots + 2 iron ingots + Compacted Feet Block.
  - **+90% speed** on skate surfaces: slickwood, ramps, skate floors, grind rails, boost pads, polished speculum, prism tiles, smooth stone, polished stones, smooth quartz, packed/blue ice. The list lives in the `#wackyupdate:skateable` block tag.
  - Taller step height, so curbs and ramps feel smooth.
  - **Tricks:** launch off a skate surface and tap **sneak** mid-air. Every tap is a trick. Land to cash in the combo for XP (the reward grows with combo²).

## Slickwood (skate-park tree)
A creamy, glossy tree with mint leaves. You can find it in plains, meadows, forests, flower forests, savannas and cherry groves.
- Log, Stripped Log, Planks, Leaves, Sapling, Slab, Stairs, Fence
- **Slickwood Ramp**: a true 45° ramp with a smooth, step-free hitbox. 6 planks in a staircase shape gives 6 ramps. It rises in the direction you face when you place it.
- **Slickwood Skate Floor**: a thin, waxed, slippery floor tile. 2 slickwood slabs + honeycomb gives 6.

## Ragdolls
- Zombies, husks, drowned, skeletons, strays, wither skeletons, bogged and Skate Ghouls **turn into ragdolls when they die**.
  - The body gets flung away from whatever killed it, harder for explosions. Its limbs flop with spring physics, and it lies around for ~10 seconds.
  - You can punch it around while it lasts. Loot and XP drop as normal.
  - Edit `#wackyupdate:ragdolls` to change which mobs do this.
- **Ragdoll Dummy** (wool + hay bale + sticks): a permanent training dummy.
  - It wobbles when hit and shows the damage dealt. Knock it hard and it flops over, then gets back up.
  - It flies off boost pads and explosions.
  - **Sneak-punch** it to pick it back up.

## Lightshows & the Prism Light Network

### Prism Projector
Iron + speculum shard + redstone block.
1. Place it facing any direction. Its front is the side you clicked.
2. Put a **crystal lens** directly in front of it:
   - **prism glass** or **stained glass** gives a beam in that colour;
   - **any speculum crystal, speculum block, prism tiles or amethyst** gives a **rainbow** beam;
   - plain glass gives white.
3. **Right-click** cycles the mode. **Sneak + right-click** shifts its beat offset in 1/4 beat steps, so you can chain projectors into chases.

| Mode | Activated by | What it does |
|---|---|---|
| Sweep, Spin, Strobe, Fan, Disco | redstone | Decorative lightshows. A stronger signal gives longer beams. |
| **Focus** | redstone | A straight **laser that really travels through the world**. Its **strength = the redstone power going in** (1–15). |
| **Music** | **note blocks & jukeboxes** within 16 blocks (no redstone needed) | Every note fires a burst of beams in fresh directions, **coloured by the note's pitch** (with a rainbow lens). A playing jukebox makes it dance on the beat. |

Every projector runs on the same world clock (120 BPM), so they all stay in sync.

### Cause → effect
**What turns lights on:** redstone (on/off plus beam strength), note blocks, jukeboxes, and other Light Receptors (light-powered redstone can switch the next projector).

**What lights do in return:**
- **Light Receptor** (glass + redstone + shard + iron). It outputs **redstone equal to the strength** of the strongest Focus beam hitting it. It glows when lit.
  - Right-click with a **dye** to make it react **only to that colour**. Right-click empty-handed to react to anything again. The ring around its eye shows the filter colour.
  - White and rainbow light contain every colour, so they pass every filter.
- **Laser tripwires.** Any mob or player that steps into a Focus beam blocks it, and downstream receptors switch off. Solid blocks block it too. Glass, leaves, water and other see-through blocks don't.
- **Comparator output** from a projector:
  - Show modes: a **pulse on every beat**, to sync pistons, lamps and note blocks.
  - Focus mode: **how many receptors** the beam is powering.
  - Music mode: a short pulse on every note, **1–15 by pitch**.
- **Grow light.** Aim a Focus beam at **Budding Speculum** and its crystals grow many times faster.

### Steering the beam
- **Speculum Mirror** (2 per craft: iron + shards + glass pane). A Focus beam that enters it leaves through its mirror face, with no loss. **Right-click any face to point the mirror there**, and sneak-click to point it the opposite way.
- **Prism Splitter** (prism glass + shard). Lets the beam through **and** sends a copy out of its orange marked face. Each output is 1 strength weaker. Right-click a face to move the side output.
- **Colour filters.** A beam passing through prism glass or stained glass **takes on that colour** (1 strength weaker). This is how you send different colours to different filtered receptors.
- Range is 16 + 2 × power blocks per beam, and mirror loops are detected and stopped.

**Example:** red lens → projector (Focus) → mirror → splitter.
- One branch goes into a red-filtered receptor that drives a door.
- The other goes through blue glass into a blue-filtered receptor that runs an alarm when someone walks through the beam.

## The 5 extra features
1. **Spectre Sight**: use the lens alone to make nearby creatures glow through walls.
2. **Skate Tricks**: airborne sneak-tap combos for XP.
3. **Prism Tonic & the Prismatic effect**: drink it (bottle + shard + glow berries + sugar) to get a rainbow particle trail and +15% speed for 3 minutes.
4. **Glimmer Nights**: fireflies hover over flowers and tall grass at night. About one night in three is a *Glimmer Night* with far more of them, and it's announced on screen.
5. **Recolourable Prism Glass**: dye or shard right-click, as described above.

## 10 extra blocks
Block of Speculum, Polished Speculum, Speculum Bricks (these chime like amethyst), Prism Tiles, Firefly Jar, **Grind Rail**, **Boost Pad**, Slickwood Slab, Slickwood Stairs, Slickwood Fence.
- **Grind Rail**: hop on it with skates to grind along it and keep your speed.
- **Boost Pad**: flings anything that steps on it in the arrow's direction.

## 3 animals
| Mob | Where | Breed with | Notes |
|---|---|---|---|
| **Prism Snail** | swamps, forests, lush caves | moss | Its crystal shell comes in any of 16 colours, and babies mix their parents' colours. Armoured. |
| **Rollypin** | savannas, sparse jungle | spider eye | Scaly pangolin-thing. When hit it **curls into a ball** and rolls away fast, taking half damage. |
| **Glowtoad** | swamps, mangroves, rivers, meadows | glow berries | Hops about with a **glowing, puffing throat sac**. Fireflies gather around it at night. |

## 2 hostile mobs
| Mob | Where | Notes |
|---|---|---|
| **Shardling** | dark caves below y 16 | A walking chunk of crystal. Arrows deal half damage. Sometimes drops a speculum shard and prism glass. |
| **Skate Ghoul** | plains, forests, savanna, meadow, desert at night | An undead skater that suddenly **dashes** at you on its rollerskates. Burns in daylight. 6% chance to drop its **Rollerskates**. |

---

### Project layout
- `src/main/java`: common code (blocks, items, entities, world gen, skate and ragdoll handlers)
- `src/client/java`: rendering (entity models, projector beams, lens x-ray, fireflies, Glimmer Nights)
- `tools/generate_assets.py`: generates **every** texture, model, blockstate, loot table, recipe, tag, worldgen file and the lang file. Edit it and re-run `python3 tools/generate_assets.py` (needs Pillow). All textures are procedural placeholder art, so swap in hand-drawn PNGs any time.

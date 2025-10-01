# Pokémon Ga-Olé Arcade Game (Java Console Program)

This project is a Java-based console application designed to simulate the Pokémon Ga-Olé battle-and-catch arcade game experience. Developed as a group project for PRG1203: Object-Oriented Programming Fundamentals at the Faculty of Engineering and Technology, Sunway University, the system demonstrates object-oriented programming design principles such as encapsulation, inheritance, polymorphism, and highlights Unified Modelling Language (UML)-driven planning, modular class structure, file processing, and interactive user display.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Disclaimer](#disclaimer)
3. [Features](#features)
4. [Installation and Running Instructions](#installation-and-running-instructions)
5. [Usage](#usage)
6. [File Format](#file-format)
7. [Example](#example)
8. [Notes](#notes)
9. [Resources](#resources)
10. [Credits](#credits)
11. [License](#license)

---

## Project Overview :blue_book:

This project provides a simple and effective Java console application to simulate the Pokémon Ga-Olé "Battle and Catch" arcade experience. Users can generate and catch wild Pokémons, manage their roster, send pairs of Pokémons into turn-based battles (including special mechanics like Double Rush and Rush Combo), attempt captures with Poké Balls, as well as view and save top battle scores. Game data such as high scores and optional Pokémon data are stored in plain text files (e.g., scores.txt, pokemons.txt), which act as a basic database. The application leverages Java object-oriented programming fundamentals such as classes and objects, encapsulation, inheritance, polymorphism, collections, file input/output (i/o), user-input handling, and modular design.

---

## Disclaimer :warning:

Some members of the project team have prior experience playing Pokémon in general, but **not** Pokémon Ga-Olé specifically. Pokémon Ga-Olé machines are relatively uncommon in Malaysia, so direct hands-on experience with the original arcade hardware and localised machine behaviours was limited. The simulation implemented in this console project aims to approximate key Ga-Olé mechanics for teaching and demonstration purposes only — it is **not** an exact replica of the arcade experience.

---

## Features :sparkles:
1. Per-player persistent saves as one file per player under `pokemon/saves/`.
2. Player profile management, which includes create, list, load, and delete profile options.
3. Currency and vending flow modelled on the arcade: User-facing amounts display as `¥`, internal code and save files use the key/identifier `yen`.
4. Start-cost flow: Player deposits to meet the START_COST (¥100) to begin a session at the Pokémon Center.
5. Game modes implemented (console simulation):
    - **Get by Battle** — Choose a course, play up to three rounds, capture attempts, Evolution Chance and Grade Up prompts.
    - **Get Now** — Quick Ball mini-game (Two throws; optional dispense for ¥100).
    - **Trainer and Battle** — Simplified single-round trainer encounters.
6. Ga-Olé mechanics approximated and supported:
    - **Evolution Chance** — Offer to evolve/dispense after qualifying rounds and captures (requires ¥ payment to accept).
    - **Grade Up Chance** — Upgrade Pokémon grade (requires payment to accept).
    - **Mega Evolution Chance** — Simulated timed prompt before first attack (console-friendly).
    - **Z-Moves** — Z-charge accumulation and roulette-style activation simulation.
7. Manual JavaScript Object Notation (JSON) save/load implemented (legacy coins → yen migration supported).
8. `fmtYen()` helper (in `Main.java`) centralises display formatting for `¥` output.

---

## Installation and Running Instructions :hammer_and_wrench:
1. **Requirements**
- Java JDK 8 or later installed and on your PATH (JDK download/installation is platform-specific).
- Terminal (Command Prompt / PowerShell / bash) or an IDE (IntelliJ / Eclipse / VS Code with Java extension).

2. **Compile (from project root)**
```
javac pokemon/*.java
```

3. **Run the main program**
```
# Normal run
java -cp pokemon Main

# Run and give a starting balance (useful for testing)
java -cp pokemon Main 500
```
Passing an integer argument (e.g. 500) will set the starting yen for the new/loaded player during that run for convenience while testing.

---

## Usage :joystick:
1. The program is launched by executing `java -cp pokemon Main` from the command line, or by running it through an Integrated Development Environment (IDE).
2. The user is prompted to either create a new trainer profile or load an existing one. When creating a profile, a unique trainer name must be entered. Previously saved profiles are listed for selection.
3. Once a profile is created or loaded, the program requests deposits until the user's balance reaches at least ¥100, which is required to begin a session. Deposits can be made in any whole number amount, and once the minimum is met, the start cost is deducted and the session begins.
4. At the Pokémon Center menu, the user selects one of three game modes: Get by Battle, Get Now, or Trainer and Battle. Each mode is adapted for console-based interaction and reflects simplified Ga-Olé mechanics.
5. During or after gameplay rounds, the user may receive opportunities for Evolution Chance or Grade Up Chance. Accepting these options requires a ¥100 payment, which is automatically deducted from the user's balance.
6. Mega Evolution and Z-Move mechanics are simulated during battles using console prompts and randomised systems that approximate charging and activation.
7. Upon exiting the program, the user's progress is automatically saved to a JSON file located in `pokemon/saves/`, named using a sanitised version of the trainer's profile. This allows the user to resume their session later by loading the same profile.

---

## File Format :page_facing_up:
- Player saves are stored as JSON files in the `pokemon/saves/` directory with filenames generated by sanitizing the trainer name: lowercase and non-alphanumeric characters replaced with underscores (e.g., `lee_isaac.json`).

- Top-level keys in each save include:

    - `playerName` (string)
    - `battlesWon` (int)
    - `battlesLost` (int)
    - `yen` (int) — Player's balance (the JSON key is `"yen"`; user-facing user interface (UI) prints amounts with the `¥` symbol)
    - `pokemon` (object or `null`) with fields:
        - `id`, `name`, `types` (array), `maxHp`, `hp`, `attack`, `defense`
        - Ga-Olé specific fields: `grade` (int), `evolvesToId` (int or `null`), `megaCapable` (boolean), `zMoveCapable` (boolean)
        - `moves` (array of strings)
    - Legacy compatibility: The loader accepts the older `"coins"` key as a fallback and will migrate the save to use `"yen"` on the next save cycle. New saves always use `"yen"`.
    - Implementation notes:
        - The project currently builds/parses JSON manually in `JsonSaveSystem.java`. For production-grade robustness consider switching to a JSON library (Gson/Jackson) and using atomic write patterns (temp file + atomic move) when saving.

---

## Example :mag_right:
Example content of a player save pokemon/saves/isaac.json:
```
{
  "playerName": "Isaac",
  "battlesWon": 0,
  "battlesLost": 0,
  "yen": 1000,
  "pokemon": [
    {
      "id": 25,
      "name": "Pikachu",
      "types": ["Electric"],
      "maxHp": 35,
      "hp": 35,
      "attack": 55,
      "defense": 40,
      "grade": 1,
      "evolvesToId": null,
      "megaCapable": false,
      "zMoveCapable": true,
      "move": ["Tackle"]
    }
  ]
}
```

---

## Notes :memo:

- The existing report in the "Documents" folder is no longer viable, as this Java console program has been further improved, where additions have been inserted.
- Game mechanics, features, and historical details for Pokémon Ga-Olé were referenced from [Bulbapedia’s Pokémon Ga-Olé article](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Ga-Ol%C3%A9) to ensure accuracy in replicating arcade gameplay elements.
- Only **Set One Pokémons** are included in this Java console program to keep file size manageable, and the list was sourced from [this Pokémon Database](https://bulbapedia.bulbagarden.net/wiki/Set_1_(Ga-Ol%C3%A9)).
- All user-facing displays use the symbol `¥` while internal code and save files use the key/identifier `yen`.
- Save format is JSON (see `JsonSaveSystem.java`) and persists only the simplified player state and Pokémon collection used by this console simulator.
- The battle system has been intentionally simplified to keep the codebase compact and maintainable:
  - Battles are turn-based and console-driven (text input/output (I/O)). There are no graphical animations, sound, or arcade-style visual effects.
  - A basic two-versus-two team flow and a console-friendly attack-gauge prototype (reaction-time mini-game) were implemented in `Battle.java` as a simplified approximation of the arcade’s real-time gauge mechanics.
  - Move implementations are simplified (see `Move.java`, `TypeMove.java`) and do not fully reproduce every move effect, priority rule, or in-depth mechanics from the official games or the arcade.
- Important Ga-Olé features not implemented (intentional / out-of-scope):
  - **Physical disk/card scan and hardware emulation**: There is no simulation of scanning Ga-Olé disks/cards, no unique disk serials, and no disk printing/dispensing emulation.
  - **Ga-Olé Passes, Support Pokémon, Item Chance!, and Ga-Olé Tickets**: These economy/auxiliary systems were excluded because of complexity.
  - **Full arcade timing mechanics**: The arcade’s real-time gauges, multi-button timing, and rapid-sequence inputs are **not** modeled beyond the simple console reaction test.
  - **Accurate capture mini-games and item roulette**: Capture logic uses a simplified probabilistic formula; special item interactions and event-based capture modifiers are not implemented.
  - **Event/trainer roster scheduling and difficulty scaling**: There is no rotating event system or detailed trainer rosters with scripted strategies; opponent selection is simplified.
  - **Complete move list & advanced move effects**: Status conditions, multi-hit moves, priorities, recoil mechanics beyond a simple `Struggle`, and many move-specific mechanics are not fully supported.
  - **Rarity/set/collector economy**: Disk/set rarity tiers, set releases, and associated collection mechanics are not present.
  - **Multiplayer/networked arcade play**: No local or online multiplayer modes; the project is a single-player console simulator.
- **Schema choice for types and move**: `types` is an array (e.g. `"types": ["Psychic","Fairy"]`) because a Pokémon may have one or more types; using an array keeps the schema uniform so loaders can always iterate without special-casing single vs dual types. `move` is a singular field (e.g. `"move": ["Dazzling Gleam"]`) because set one disks provide exactly one primary move; it is stored as a compact single-element array for consistency with `types` and to make future expansion (multiple moves or move metadata) straightforward. 

---

## Resources :books:
- **Pokémon Ga-Olé official website**: [https://world.pokemongaole.com/](https://world.pokemongaole.com/)
- **Pokémon Ga-Olé "How to Play" page** [https://world.pokemongaole.com/sg/howtoplay/](https://world.pokemongaole.com/sg/howtoplay/)
- **Bulbapedia’s Pokémon Ga-Olé article**: [https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Ga-Ol%C3%A9](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Ga-Ol%C3%A9)
 
---

## Credits :busts_in_silhouette:
This project was made successful by the contributions of the following team members:
1. **[Lee Ming Hui Isaac (Group Leader)](https://github.com/LeeIsaac1201)**
2. **[Cyril Clement](https://github.com/reivering)**
3. **[Gavindra Ramadhansyah Fadyl](https://github.com/GavinFadyl)**
4. **[Kim Hyunwoo](https://github.com/hyunwod)**
5. **[Lee Cheng Yuen](https://github.com/sorrl)**

---

## License :scroll:

© 2023-2025 Lee Ming Hui Isaac, Cyril Clement, Gavindra Ramadhansyah Fadyl, Kim Hyunwoo, and Lee Cheng Yuen. All rights reserved.

This code and its documentation are proprietary. You may not copy, modify, distribute, or otherwise use this software without express written permission from the copyright holder.

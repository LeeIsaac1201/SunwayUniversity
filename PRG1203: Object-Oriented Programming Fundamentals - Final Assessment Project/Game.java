// Imports for file input/output (I/O), working with file paths, and using collections, randomisation, and user input.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Console-based hub and mode controller for Pokémon Ga-Olé that handles the main menu, Pokémon Center, and different game modes.
public class Game {
    // Core game state: Player profile, input scanner, random generator, Pokédex data, and running flag.
    private Player player;
    private Scanner scanner;
    private Random random;
    private List<PokedexEntry> pokedex;
    private boolean gameRunning;

    // Session state for Pokémon Center (arcade-style session credit).
    private boolean sessionActive;
    private int getByBattleRemaining;
    private int getNowRemaining;
    private static final int GET_BY_BATTLE_MAX = 3;
    private static final int GET_NOW_MAX = 9;
    private static final int SESSION_COST = 100;

    // Constructor: Initialises the game with a player, sets up utilities, and loads Pokédex data.
    public Game(Player player) {
        this.player = player;
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.pokedex = new ArrayList<>();
        this.gameRunning = true;

        // Session defaults: no active session until player pays in the Pokémon Center.
        this.sessionActive = false;
        this.getByBattleRemaining = 0;
        this.getNowRemaining = 0;

        loadPokedex();
        ensureDefaultPokedex();
    }

    // Starts the console-based game loop, showing the main menu until the game ends.
    public void startGame() {
        System.out.println("Welcome to Pokémon Ga-Olé (console demo)!");
        if (player == null) {
            System.out.println("No player profile found. Please create a profile first.");
            return;
        }

        while (gameRunning) {
            showMainMenu();
        }
    }

    // Displays the main menu and handles player choices.
    private void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("Trainer: " + player.getName());
        System.out.println("Balance: " + Main.fmtYen(player.getYen()));
        System.out.println("Battles Won: " + player.getBattlesWon() + "  Lost: " + player.getBattlesLost());
        System.out.println("1) Go to Pokémon Center (Insert \u00A5" + SESSION_COST + " to start a session)");
        System.out.println("2) Show my Pokémon");
        System.out.println("3) Add funds");
        System.out.println("4) Save");
        System.out.println("5) Exit");
        System.out.print("Enter your option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                pokemonCenter();
                break;
            case "2":
                showPlayerPokemon();
                break;
            case "3":
                depositYen();
                break;
            case "4":
                JsonSaveSystem.saveGame(player);
                System.out.println("Saved.");
                break;
            case "5":
                // Auto-save and exit.
                JsonSaveSystem.saveGame(player);
                System.out.println("Saved. Exiting.");
                gameRunning = false;
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    // Deposit helper invoked from main menu.
    private void depositYen() {
        System.out.println("\n--- Add Funds ---");
        System.out.println("Current balance: " + Main.fmtYen(player.getYen()));
        System.out.print("Insert amount now (enter integer amount, or 0 to cancel): ");
        String line = scanner.nextLine().trim();
        int add;
        try {
            add = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Deposit cancelled.");
            return;
        }
        if (add <= 0) {
            System.out.println("No yen inserted. Cancelled.");
            return;
        }
        player.addYen(add);
        System.out.println(Main.fmtYen(add) + " added. New balance: " + Main.fmtYen(player.getYen()));
        JsonSaveSystem.saveGame(player);
    }

    // Displays the player's Pokémon collection with statistics and moves.
    private void showPlayerPokemon() {
        List<Pokemon> list = player.getPokemons();
        if (list == null || list.isEmpty()) {
            System.out.println("You don't have any Pokémon yet.");
            return;
        }
        System.out.println("\n=== Your Pokémon Collection ===");
        for (int i = 0; i < list.size(); i++) {
            Pokemon p = list.get(i);
            String activeMark = (i == 0) ? " (active)" : "";
            System.out.printf("%d) %s (ID: %d)%s\n   HP: %d/%d  ATK: %d  DEF: %d  Types: %s\n",
                    i + 1, p.getName(), p.getId(), activeMark, p.getHp(), p.getMaxHp(), p.getAttack(), p.getDefense(),
                    p.getTypes() == null ? "None" : p.getTypes());
            System.out.print("   Moves: ");
            if (p.getMoves() == null || p.getMoves().isEmpty()) System.out.println("None");
            else {
                List<String> moveNames = new ArrayList<>();
                for (Move m : p.getMoves()) moveNames.add(m.name());
                System.out.println(moveNames);
            }
        }
    }

    // Pokémon Center menu: Lets the player choose a game mode after paying ¥100.
    private void pokemonCenter() {
        System.out.println("\n=== Pokémon Center ===");
        System.out.println("Select a mode (each session costs \u00A5" + SESSION_COST + " — charged once per arcade session):");
        System.out.println("Note: \u00A5" + SESSION_COST + " starts a session that gives up to " + GET_BY_BATTLE_MAX + " Get by Battle rounds and up to " + GET_NOW_MAX + " Get Now rounds. Additional purchases (e.g., keeping a caught Pokémon) still cost yen.");
        boolean inCenter = true;

        while (inCenter) {
            // Show session remaining information if an arcade session is active.
            if (sessionActive) {
                System.out.println("\nSession active — remaining: Get by Battle = " + getByBattleRemaining + ", Get Now = " + getNowRemaining);
            }

            System.out.println("\nSelect a mode (You have " + Main.fmtYen(player.getYen()) + "):");
            System.out.println("1) Get by Battle - Up to " + GET_BY_BATTLE_MAX + " rounds (per session)");
            System.out.println("2) Get Now - Quick mini-game (up to " + GET_NOW_MAX + " rounds per session)");
            System.out.println("3) Trainer and Battle - Single trainer challenge");
            System.out.println("4) Back to main menu");
            System.out.print("Enter your option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    // If no session active, ensure at least SESSION_COST inserted (this method will deduct).
                    if (!sessionActive) {
                        boolean ok = Main.ensureInsertedStartCost(player, scanner);
                        if (!ok) {
                            break;
                        }
                        sessionActive = true;
                        getByBattleRemaining = GET_BY_BATTLE_MAX;
                        getNowRemaining = GET_NOW_MAX;
                        System.out.println(Main.fmtYen(SESSION_COST) + " accepted. Session started.");
                    }

                    // If session active but no remaining rounds for this mode, prompt user.
                    if (getByBattleRemaining <= 0) {
                        System.out.println("No Get by Battle rounds remaining in this session. Start a new session to play more.");
                        break;
                    }

                    getByBattleMode();
                    // If session consumed (e.g., from loss) or counters exhausted, sessionActive may be false now.
                    break;
                case "2":
                    if (!sessionActive) {
                        boolean ok = Main.ensureInsertedStartCost(player, scanner);
                        if (!ok) {
                            break;
                        }
                        sessionActive = true;
                        getByBattleRemaining = GET_BY_BATTLE_MAX;
                        getNowRemaining = GET_NOW_MAX;
                        System.out.println(Main.fmtYen(SESSION_COST) + " accepted. Session started.");
                    }

                    if (getNowRemaining <= 0) {
                        System.out.println("No Get Now rounds remaining in this session. Start a new session to play more.");
                        break;
                    }

                    getNowMode();
                    break;
                case "3":
                    if (!sessionActive) {
                        boolean ok = Main.ensureInsertedStartCost(player, scanner);
                        if (!ok) {
                            break;
                        }
                        sessionActive = true;
                        getByBattleRemaining = GET_BY_BATTLE_MAX;
                        getNowRemaining = GET_NOW_MAX;
                        System.out.println(Main.fmtYen(SESSION_COST) + " accepted. Session started.");
                    }

                    trainerAndBattleMode();
                    break;
                case "4":
                    // Exiting the Pokémon Center ends the current session (no refund).
                    sessionActive = false;
                    inCenter = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

            // If both counters are zero, automatically end session.
            if (sessionActive && getByBattleRemaining <= 0 && getNowRemaining <= 0) {
                System.out.println("Session rounds exhausted. Session ended.");
                sessionActive = false;
            }
        }

        // Auto-save progress after leaving the Pokémon Center.
        JsonSaveSystem.saveGame(player);
    }

    // "Get by Battle" mode: Up to three wild Pokémon battles in a single paid session.
    private void getByBattleMode() {
        System.out.println("\n--- Get by Battle Mode (max " + GET_BY_BATTLE_MAX + " rounds per session) ---");
        if (player.getPokemon() == null) {
            System.out.println("You need a Pokémon to play Get by Battle. Acquire one first.");
            return;
        }

        // Determine how many rounds are permitted by the session (bounded by GET_BY_BATTLE_MAX and remaining session rounds for this mode).
        final int MAX_ROUNDS = GET_BY_BATTLE_MAX;
        int allowedThisCall = Math.min(MAX_ROUNDS, getByBattleRemaining);
        if (allowedThisCall <= 0) {
            System.out.println("No Get by Battle rounds remaining in this session.");
            return;
        }

        int roundsCompleted = 0;

        for (int round = 1; round <= allowedThisCall; round++) {
            System.out.println("\nRound " + round + " of " + allowedThisCall);
            Pokemon wild = randomWildPokemon();
            if (wild == null) {
                System.out.println("No wild Pokémon available. Ending mode.");
                break;
            }
            System.out.println("A wild " + wild.getName() + " appeared!");

            // Prepare fresh Pokémon copies for battle.
            Pokemon playerMon = copyPokemon(player.getPokemon());
            ensureSomeMoves(playerMon);
            ensureSomeMoves(wild);

            // Start the battle.
            Battle b = new Battle(playerMon, wild, player);
            b.start();

            roundsCompleted++;

            // End session if player loses.
            if (playerMon.isFainted()) {
                System.out.println("You lost the round. Get by Battle session ends.");
                // Consumes all remaining rounds (session ends immediately).
                getByBattleRemaining = 0;
                getNowRemaining = 0;
                sessionActive = false;
                break;
            } else {
                System.out.println("Round complete. You may continue to the next round.");
            }

            // Ask if player wants to continue.
            if (round < allowedThisCall) {
                boolean cont = readYesNo("Continue to next round? (press Enter to continue, 'n' to stop)", true);
                if (!cont) {
                    break;
                }
            }
        }

        // Deduct rounds completed from this session's Get by Battle counter.
        getByBattleRemaining -= roundsCompleted;
        if (getByBattleRemaining < 0) getByBattleRemaining = 0;

        // Report if this mode is now exhausted but do not forcibly end session if other mode still has rounds.
        if (getByBattleRemaining <= 0 && sessionActive) {
            System.out.println("Get by Battle rounds for this session exhausted.");
        }

        // Save after session ends or partially used.
        JsonSaveSystem.saveGame(player);
    }

    // "Get Now" mode: A quick mini-game with two Quick Balls per round, up to nine rounds.
    private void getNowMode() {
        System.out.println("\n--- Get Now Mode (2 Quick Balls) ---");

        final int MAX_CONSECUTIVE = GET_NOW_MAX;
        if (getNowRemaining <= 0) {
            System.out.println("No Get Now rounds remaining in this session.");
            return;
        }

        int roundsPlayed = 0;
        boolean keepPlaying = true;

        // Guard the loop by the remaining Get Now counter.
        while (keepPlaying && roundsPlayed < Math.min(MAX_CONSECUTIVE, getNowRemaining)) {
            roundsPlayed++;

            // Generate two wild Pokémon encounters.
            Pokemon first = randomWildPokemon();
            Pokemon second = randomWildPokemon();
            List<Pokemon> encounters = new ArrayList<>();
            if (first != null) encounters.add(first);
            if (second != null) encounters.add(second);

            int ballCount = 2;
            List<Pokemon> caught = new ArrayList<>();

            // Attempt to catch each encountered Pokémon.
            for (int i = 0; i < ballCount && i < encounters.size(); i++) {
                Pokemon wild = copyPokemon(encounters.get(i));
                System.out.println("Quick Ball throw at " + wild.getName() + "!");
                double baseChance = 0.5;
                double hpFactor = (double)(wild.getMaxHp() - Math.max(1, wild.getHp())) / wild.getMaxHp();
                double finalChance = Math.min(0.95, Math.max(0.05, baseChance + hpFactor * 0.2 + random.nextGaussian()*0.05));
                boolean caughtFlag = random.nextDouble() < finalChance;
                if (caughtFlag) {
                    System.out.println("You caught " + wild.getName() + "!");
                    caught.add(wild);
                } else {
                    System.out.println(wild.getName() + " broke free.");
                }
            }

            // Handle caught Pokémon.
            if (caught.isEmpty()) {
                System.out.println("No Pokémon caught this round.");
            } else {
                System.out.println("You caught " + caught.size() + " Pokémon this round.");
                for (int i = 0; i < caught.size(); i++) {
                    System.out.printf("%d) %s (ID %d)\n", i + 1, caught.get(i).getName(), caught.get(i).getId());
                }

                // Dispense (free) section (allows multiple indices or "all").
                System.out.println();
                System.out.println("Dispense (free) — machine takes the Pokémon; it will NOT be added to your collection.");
                System.out.println("Enter numbers to dispense separated by commas (e.g. 1,2), 'all' to dispense all, or press Enter to skip:");
                System.out.print("Enter your option: ");
                String dispenseInput = scanner.nextLine().trim();
                if (!dispenseInput.isEmpty()) {
                    if (dispenseInput.equalsIgnoreCase("all") || dispenseInput.equalsIgnoreCase("a")) {
                        caught.clear();
                        System.out.println("All caught Pokémon dispensed.");
                    } else {
                        List<Integer> dispenseIndices = parseIndicesFromInput(dispenseInput, caught.size());
                        if (!dispenseIndices.isEmpty()) {
                            // Builds list of objects to remove (avoid index shift issues).
                            List<Pokemon> toRemove = new ArrayList<>();
                            for (int idx : dispenseIndices) {
                                if (idx >= 0 && idx < caught.size()) {
                                    toRemove.add(caught.get(idx));
                                }
                            }
                            for (Pokemon rem : toRemove) {
                                caught.remove(rem);
                                System.out.println("Dispensed " + rem.getName() + ".");
                            }
                        } else {
                            System.out.println("No valid dispense indices provided; skipping dispense step.");
                        }
                    }
                }

                // If anything remains in caught, allow the player to buy-and-keep multiple Pokémon.
                if (!caught.isEmpty()) {
                    final int KEEP_COST = 100;
                    System.out.println();
                    System.out.println("Keep permanently for " + Main.fmtYen(KEEP_COST) + " each — pay to add one or more of the caught Pokémon to your collection.");
                    System.out.println("Enter numbers to buy-and-keep separated by commas (e.g. 1,2), 'all' to attempt to buy all, or press Enter to skip:");
                    System.out.print("Enter your option: ");
                    String keepInput = scanner.nextLine().trim();
                    List<Pokemon> newlyAdded = new ArrayList<>();
                    if (!keepInput.isEmpty()) {
                        List<Integer> buyIndices;
                        if (keepInput.equalsIgnoreCase("all") || keepInput.equalsIgnoreCase("a")) {
                            buyIndices = new ArrayList<>();
                            for (int i = 0; i < caught.size(); i++) buyIndices.add(i);
                        } else {
                            buyIndices = parseIndicesFromInput(keepInput, caught.size());
                        }

                        if (buyIndices.isEmpty()) {
                            System.out.println("No valid indices entered — no Pokémon added.");
                        } else {
                            // Deduplicate indices while preserving order.
                            LinkedHashSet<Integer> uniq = new LinkedHashSet<>(buyIndices);
                            for (int idx : uniq) {
                                if (idx < 0 || idx >= caught.size()) {
                                    System.out.println("Ignoring invalid index: " + (idx + 1));
                                    continue;
                                }
                                Pokemon chosen = caught.get(idx);

                                // Prevent adding the exact same species by identification number if already in collection.
                                boolean alreadyOwned = false;
                                for (Pokemon owned : player.getPokemons()) {
                                    if (owned != null && owned.getId() == chosen.getId()) {
                                        alreadyOwned = true;
                                        break;
                                    }
                                }
                                if (alreadyOwned) {
                                    System.out.println("You already own a " + chosen.getName() + " (ID " + chosen.getId() + "). Skipping this one.");
                                    continue;
                                }

                                if (player.getYen() < KEEP_COST) {
                                    System.out.println("Insufficient funds to buy " + chosen.getName() + ". Stopping purchases.");
                                    break;
                                }

                                if (!player.spendYen(KEEP_COST)) {
                                    System.out.println("Unexpected failure deducting funds for " + chosen.getName() + ". Stopping purchases.");
                                    break;
                                }

                                // Finalise purchase.
                                chosen.setHp(chosen.getMaxHp());
                                ensureSomeMoves(chosen);
                                player.addPokemon(chosen);
                                newlyAdded.add(chosen);
                                System.out.println(Main.fmtYen(KEEP_COST) + " deducted. " + chosen.getName() + " has been added to your collection.");
                            }

                            // Remove purchased Pokémon from caught list (remove by identity).
                            for (Pokemon added : newlyAdded) {
                                while (caught.remove(added)) {}
                            }

                            // Summary of purchases.
                            if (!newlyAdded.isEmpty()) {
                                System.out.println();
                                System.out.println("Purchase summary:");
                                for (Pokemon np : newlyAdded) {
                                    System.out.printf(" - %s (ID %d)\n", np.getName(), np.getId());
                                }
                                System.out.println("New balance: " + Main.fmtYen(player.getYen()));
                            } else {
                                System.out.println("No Pokémon were added to your collection.");
                            }

                            // Offer to set active Pokémon from the entire collection.
                            if (!newlyAdded.isEmpty()) {
                                boolean setActive = readYesNo("Would you like to make one of your Pokémon active now? (y to choose)", false);
                                if (setActive) {
                                    promptSetActiveFromCollection();
                                }
                            }
                        }
                    } else {
                        System.out.println("No Pokémon added to your collection.");
                    }
                }
            }

            JsonSaveSystem.saveGame(player);

            // Ask to play another round only if there are Get Now rounds remaining.
            if (roundsPlayed >= getNowRemaining) {
                // All pre-paid rounds consumed for Get Now
                System.out.println("No Get Now rounds remaining in this session.");
                break;
            } else {
                // There are still pre-paid rounds left in the session
                boolean playAnother = readYesNo("Play another Get Now round? (press Enter to continue, 'n' to stop)", true);
                if (!playAnother) {
                    keepPlaying = false;
                }
            }
        }

        // Deduct roundsPlayed from the session Get Now allowance.
        getNowRemaining -= roundsPlayed;
        if (getNowRemaining < 0) getNowRemaining = 0;

        // If Get Now rounds exhausted, report it; do NOT forcibly end the entire session here
        // so the player can still use remaining Get by Battle allowance if any.
        if (getNowRemaining <= 0 && sessionActive) {
            System.out.println("Get Now rounds for this session exhausted.");
        }

        System.out.println("Get Now session ended.");
        JsonSaveSystem.saveGame(player);
    }

    // Trainer and Battle mode (simplified): pits player against a stronger trainer (single tougher Pokémon); if the player wins and a reward is offered, adding it to the collection costs ¥100.
    private void trainerAndBattleMode() {
        System.out.println("\n--- Trainer and Battle Mode ---");
        if (player.getPokemon() == null) {
            System.out.println("You need a Pokémon to battle trainers.");
            return;
        }

        Pokemon opp = randomWildPokemon();
        if (opp == null) {
            System.out.println("No trainers available now.");
            return;
        }
        // Boost opponent to make it more trainer-like.
        opp.setMaxHp((int)(opp.getMaxHp() * 1.4));
        opp.setHp(opp.getMaxHp());
        opp.setAttack((int)(opp.getAttack() * 1.4));
        opp.setDefense((int)(opp.getDefense() * 1.3));
        ensureSomeMoves(opp);

        System.out.println("Trainer's lead Pokémon is " + opp.getName() + "! Prepare to battle.");

        Pokemon playerMon = copyPokemon(player.getPokemon());
        ensureSomeMoves(playerMon);

        Battle battle = new Battle(playerMon, opp, player);
        battle.start();

        // Determine the outcome: If the opponenent fainted and playerMon not fainted, victory is declared.
        if (!playerMon.isFainted() && opp.isFainted()) {
            System.out.println("You defeated the trainer's lead Pokémon! You earn a reward.");
            // Offer a reward Pokémon.
            Pokemon reward = randomWildPokemon();
            if (reward != null) {
                System.out.println("You earned a reward: " + reward.getName() + " (ID " + reward.getId() + ").");
                boolean pay = readYesNo("Pay " + Main.fmtYen(100) + " to add this Pokémon to your collection? (y to purchase)", false);
                if (pay) {
                    if (!player.spendYen(100)) {
                        System.out.println("You don't have " + Main.fmtYen(100) + ". Cannot add reward to collection.");
                    } else {
                        reward.setHp(reward.getMaxHp());
                        ensureSomeMoves(reward);
                        player.addPokemon(reward);
                        System.out.println(Main.fmtYen(100) + " deducted. " + reward.getName() + " added to your collection.");
                        boolean makeActive = readYesNo("Make this your active Pokémon? (y to set active)", false);
                        if (makeActive) {
                            player.setPokemon(reward);
                            System.out.println(reward.getName() + " is now your active Pokémon.");
                        } else {
                            // Offer to choose active from entire collection if desired.
                            boolean chooseAnother = readYesNo("Would you like to choose a different active Pokémon from your collection? (y to choose)", false);
                            if (chooseAnother) {
                                promptSetActiveFromCollection();
                            }
                        }
                    }
                } else {
                    System.out.println("Reward declined.");
                }
            } else {
                System.out.println("No reward available.");
            }
        } else {
            System.out.println("Trainer battle ended.");
        }

        JsonSaveSystem.saveGame(player);
    }

    // Represents a single Pokédex entry loaded from the JavaScript Object Notation (JSON) file.
    @SuppressWarnings("unused")
    private static class PokedexEntry {
        int id;
        String name;
        List<String> types = new ArrayList<>();
        int maxHp;
        int attack;
        int defense;
        List<String> moveNames = new ArrayList<>();
        int grade = 0;
        int energy = 0;
        int specialAttack = 0;
        int specialDefense = 0;
        int speed = 0;
    }

    // Loads Pokédex entries from pokemon/pokemon.json if available, using a simple parser.
    private void loadPokedex() {
        String path = "pokemon/pokemon.json";
        try {
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No Pokédex file found at " + path + " - continuing without a Pokédex.");
                return;
            }
            String raw = String.join("\n", Files.readAllLines(Paths.get(path)));
            String[] objects = raw.split("\\{");
            int autoId = 1;
            for (String obj : objects) {
                // Require canonical "disk_number" in the set one schema.
                if (!obj.contains("\"disk_number\"")) continue;
                PokedexEntry e = new PokedexEntry();

                // Always derive numeric id from disk_number.
                String disk = extractStringFromJsonFragment(obj, "\"disk_number\"");
                if (disk == null || disk.isEmpty()) {
                    e.id = autoId++;
                } else {
                    String digits = disk.replaceAll("[^0-9]", "");
                    try {
                        e.id = Integer.parseInt(digits);
                    } catch (Exception ex) {
                        e.id = autoId++;
                    }
                }

                // Name: Canonical key "name:".
                e.name = extractStringFromJsonFragment(obj, "\"name\"");

                // Health: Canonical key "health_points:".
                e.maxHp = extractIntFromJsonFragment(obj, "\"health_points\"");

                // Attack and special attack (canonical keys).
                e.attack = extractIntFromJsonFragment(obj, "\"attack\"");
                e.specialAttack = extractIntFromJsonFragment(obj, "\"special_attack\"");

                // Defense and special defense.
                e.defense = extractIntFromJsonFragment(obj, "\"defense\"");
                e.specialDefense = extractIntFromJsonFragment(obj, "\"special_defense\"");

                // Types: canonical "types:".
                List<String> types = extractStringArrayFromJsonFragment(obj, "\"types\"");
                e.types = types;

                // Move(s): canonical "move" (single-element array).
                List<String> mnames = extractStringArrayFromJsonFragment(obj, "\"move\"");
                e.moveNames = mnames;

                // Other metadata (canonical keys).
                e.grade = extractIntFromJsonFragment(obj, "\"grade\"");
                e.energy = extractIntFromJsonFragment(obj, "\"energy\"");
                e.speed = extractIntFromJsonFragment(obj, "\"speed\"");

                pokedex.add(e);
            }
            System.out.println("Loaded Pokédex with " + pokedex.size() + " entries.");
        } catch (IOException ex) {
            System.out.println("Failed to read Pokédex: " + ex.getMessage());
        }
    }

    // Ensures a fallback Pokédex is available if no external file is loaded.
    private void ensureDefaultPokedex() {
        if (!pokedex.isEmpty()) return;

        // Add a small set of fallback species so wild encounters always work.
        PokedexEntry pikachu = new PokedexEntry();
        pikachu.id = 25; pikachu.name = "Pikachu"; pikachu.types = Arrays.asList("Electric"); pikachu.maxHp = 35; pikachu.attack = 55; pikachu.defense = 40;
        PokedexEntry pidgey = new PokedexEntry();
        pidgey.id = 16; pidgey.name = "Pidgey"; pidgey.types = Arrays.asList("Normal", "Flying"); pidgey.maxHp = 30; pidgey.attack = 30; pidgey.defense = 25;
        PokedexEntry rattata = new PokedexEntry();
        rattata.id = 19; rattata.name = "Rattata"; rattata.types = Arrays.asList("Normal"); rattata.maxHp = 28; rattata.attack = 34; rattata.defense = 20;
        PokedexEntry bulbasaur = new PokedexEntry();
        bulbasaur.id = 1; bulbasaur.name = "Bulbasaur"; bulbasaur.types = Arrays.asList("Grass", "Poison"); bulbasaur.maxHp = 45; bulbasaur.attack = 49; bulbasaur.defense = 49;
        PokedexEntry charmander = new PokedexEntry();
        charmander.id = 4; charmander.name = "Charmander"; charmander.types = Arrays.asList("Fire"); charmander.maxHp = 39; charmander.attack = 52; charmander.defense = 43;
        PokedexEntry squirtle = new PokedexEntry();
        squirtle.id = 7; squirtle.name = "Squirtle"; squirtle.types = Arrays.asList("Water"); squirtle.maxHp = 44; squirtle.attack = 48; squirtle.defense = 65;

        pokedex.add(pikachu);
        pokedex.add(pidgey);
        pokedex.add(rattata);
        pokedex.add(bulbasaur);
        pokedex.add(charmander);
        pokedex.add(squirtle);

        System.out.println("No external Pokédex found - using built-in fallback Pokédex with " + pokedex.size() + " species.");
    }

    // Extracts an integer value from a JSON fragment given a key.
    private static int extractIntFromJsonFragment(String frag, String key) {
        try {
            String search = key + ":";
            int idx = frag.indexOf(search);
            if (idx == -1) return 0;
            int colon = frag.indexOf(":", idx);
            if (colon == -1) return 0;
            int comma = frag.indexOf(",", colon);
            int end = comma == -1 ? frag.indexOf("}", colon) : comma;
            if (end == -1) end = frag.length();
            String num = frag.substring(colon + 1, end).trim().replaceAll("[^0-9\\-]", "");
            if (num.isEmpty()) return 0;
            return Integer.parseInt(num);
        } catch (Exception ex) {
            return 0;
        }
    }

    // Extracts a string value from a JSON fragment given a key.
    private static String extractStringFromJsonFragment(String frag, String key) {
        String search = key + ":";
        int idx = frag.indexOf(search);
        if (idx == -1) return "";
        idx = frag.indexOf("\"", idx);
        if (idx == -1) return "";
        int end = frag.indexOf("\"", idx + 1);
        if (end == -1) return "";
        return frag.substring(idx + 1, end);
    }

    // Extracts an array of strings from a JSON fragment given a key.
    private static List<String> extractStringArrayFromJsonFragment(String frag, String key) {
        List<String> out = new ArrayList<>();
        String search = key + ":";
        int idx = frag.indexOf(search);
        if (idx == -1) return out;
        idx = frag.indexOf("[", idx);
        if (idx == -1) return out;
        int end = frag.indexOf("]", idx);
        if (end == -1) return out;
        String inside = frag.substring(idx + 1, end);
        String[] parts = inside.split(",");
        for (String p : parts) {
            p = p.trim();
            if (p.startsWith("\"") && p.endsWith("\"")) {
                out.add(p.substring(1, p.length() - 1));
            } else if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    // Returns a random Pokémon from the Pokédex, or null if none are available.
    private Pokemon randomWildPokemon() {
        if (pokedex.isEmpty()) return null;
        PokedexEntry e = pokedex.get(random.nextInt(pokedex.size()));
        Pokemon p = new Pokemon();
        p.setId(e.id);
        p.setName(e.name);
        p.setTypes(new ArrayList<>(e.types));
        p.setMaxHp(e.maxHp);
        p.setHp(e.maxHp);
        // Prefer attack; if attack is zero, fall back to specialAttack to preserve canonical JSON intent.
        int chosenAttack = e.attack != 0 ? e.attack : e.specialAttack;
        int chosenDefense = e.defense != 0 ? e.defense : e.specialDefense;
        p.setAttack(chosenAttack);
        p.setDefense(chosenDefense);
        // Apply default moves from the Pokédex entry if present.
        if (e.moveNames != null && !e.moveNames.isEmpty()) {
            for (String mn : e.moveNames) {
                Move mv = JsonSaveSystem.createMoveByName(mn);
                if (mv != null) p.addMove(mv);
            }
        }
        return p;
    }

    // Ensures a Pokémon has at least one move, adding a BasicAttack and a type-based move if needed.
    private void ensureSomeMoves(Pokemon p) {
        if (p.getMoves() == null) p.setMoves(new ArrayList<>());
        if (p.getMoves().isEmpty()) {
            p.addMove(new BasicAttack());
            if (p.getTypes() != null && !p.getTypes().isEmpty()) {
                String tstr = p.getTypes().get(0);
                try {
                    Type t = Type.fromString(tstr);
                    p.addMove(new TypeMove(t));
                } catch (Exception ignored) {}
            }
        }
    }

    // Creates a shallow copy of a Pokémon to avoid mutating the original instance.
    private Pokemon copyPokemon(Pokemon src) {
        Pokemon p = new Pokemon();
        p.setId(src.getId());
        p.setName(src.getName());
        p.setTypes(new ArrayList<>(src.getTypes() == null ? Collections.emptyList() : src.getTypes()));
        p.setMaxHp(src.getMaxHp());
        p.setHp(src.getMaxHp()); // Start with full health points (HP).
        p.setAttack(src.getAttack());
        p.setDefense(src.getDefense());
        // Copy moves if present.
        if (src.getMoves() != null) {
            for (Move m : src.getMoves()) {
                p.addMove(m);
            }
        }
        return p;
    }

    // Helper to parse comma-separated 1-based indices into 0-based integer list (ignores invalid tokens).
    private List<Integer> parseIndicesFromInput(String input, int upperExclusive) {
        List<Integer> out = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return out;
        String[] parts = input.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            try {
                int n = Integer.parseInt(part) - 1;
                if (n >= 0 && n < upperExclusive && !out.contains(n)) out.add(n);
            } catch (NumberFormatException e) {
                // Ignores invalid token.
            }
        }
        return out;
    }

    // Reads a boolean yes or no response; pressing Enter returns default Yes, and 'y', 'yes' (case-insensitive), or a single space are treated as yes, anything else as no.
    private boolean readYesNo(String prompt, boolean defaultYes) {
        System.out.print(prompt + " ");
        String s = scanner.nextLine();
        if (s == null) s = "";
        s = s.trim();
        if (s.isEmpty()) return defaultYes;
        if (s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equals(" ")) return true;
        return false;
    }

    // Prompts the user to choose an active Pokémon from their entire collection, and shows a numbered list and sets index 0 as active when chosen.
    private void promptSetActiveFromCollection() {
        List<Pokemon> all = player.getPokemons();
        if (all == null || all.isEmpty()) {
            System.out.println("You have no Pokémon to choose from.");
            return;
        }
        System.out.println("\nChoose an active Pokémon from your collection:");
        for (int i = 0; i < all.size(); i++) {
            Pokemon p = all.get(i);
            String activeMark = (i == 0) ? " (currently active)" : "";
            System.out.printf("%d) %s (ID %d)%s\n", i + 1, p.getName(), p.getId(), activeMark);
        }
        System.out.print("Enter number to set active, or press Enter to cancel: ");
        String sel = scanner.nextLine().trim();
        if (sel.isEmpty()) {
            System.out.println("Active Pokémon unchanged.");
            return;
        }
        try {
            int idx = Integer.parseInt(sel) - 1;
            if (idx >= 0 && idx < all.size()) {
                player.setPokemon(all.get(idx));
                System.out.println(all.get(idx).getName() + " is now your active Pokémon.");
            } else {
                System.out.println("Invalid selection. Active Pokémon unchanged.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input. Active Pokémon unchanged.");
        }
    }
}

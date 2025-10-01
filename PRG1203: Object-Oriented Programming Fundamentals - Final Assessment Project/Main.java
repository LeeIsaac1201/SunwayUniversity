// Imports for user input handling and utility classes.
import java.util.*;

public class Main {

    // Formats yen for display using the yen symbol (\u00A5).
    public static String fmtYen(int amount) {
        return "\u00A5" + amount;
    }

    // Formats yen with grouping separators (e.g., ¥1,234); falls back to basic format if formatting fails.
    public static String fmtYenNice(int amount) {
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.JAPAN);
            String s = nf.format(amount);
            return s.replace("￥", "\u00A5");
        } catch (Exception e) {
            return fmtYen(amount);
        }
    }

    // Minimum cost required to start a session at the Pokémon Center.
    private static final int START_COST = 100;

    // Prompts the player to insert at least START_COST yen before starting a Pokémon Center session.
    // This method deducts START_COST from player's balance when successful, or prompts deposits until enough is available.
    public static boolean ensureInsertedStartCost(Player player, Scanner scanner) {
        if (player == null || scanner == null) return false;

        // If player already has enough yen, deduct immediately.
        if (player.getYen() >= START_COST) {
            boolean deducted = player.spendYen(START_COST);
            if (deducted) {
                System.out.println("Inserted " + fmtYen(START_COST) + ". Welcome to the Pokémon Center!");
                return true;
            } else {
                System.out.println("Unexpected error deducting " + fmtYen(START_COST) + ". Please try again later.");
                return false;
            }
        }

        // Otherwise, prompt for deposits until START_COST is reached or cancelled.
        System.out.println("You need to insert " + fmtYen(START_COST) + " to start a session at the Pokémon Center.");
        while (player.getYen() < START_COST) {
            System.out.println("Current balance: " + fmtYen(player.getYen()));
            System.out.print("Insert amount now (enter integer amount, or 0 to cancel): ");
            String line = scanner.nextLine().trim();
            int add = 0;
            try {
                add = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount entered. Please enter a whole number (e.g. 100) or 0 to cancel.");
                continue;
            }

            if (add == 0) {
                System.out.println("No yen inserted. Cancelled.");
                return false;
            }
            if (add < 0) {
                System.out.println("Amount must be positive. Try again or enter 0 to cancel.");
                continue;
            }

            // Add deposit to player's balance.
            player.addYen(add);
            System.out.println(fmtYen(add) + " added. New balance: " + fmtYen(player.getYen()));

            // If enough funds, deduct and proceed.
            if (player.getYen() >= START_COST) {
                boolean ok = player.spendYen(START_COST);
                if (ok) {
                    System.out.println("Inserted " + fmtYen(START_COST) + ". Welcome to the Pokémon Center!");
                    return true;
                } else {
                    System.out.println("Unexpected error deducting " + fmtYen(START_COST) + ". Please try again.");
                    return false;
                }
            } else {
                // Still not enough, prompt again.
                int remaining = START_COST - player.getYen();
                System.out.println("Still need " + fmtYen(remaining) + " to start a session. You can add more or enter 0 to cancel.");
            }
        }

        // Final fallback check.
        if (player.getYen() >= START_COST) {
            boolean ok = player.spendYen(START_COST);
            if (ok) {
                System.out.println("Inserted " + fmtYen(START_COST) + ". Welcome to the Pokémon Center!");
                return true;
            }
        }
        return false;
    }
    
    // Main entry point for the Pokémon Ga-Olé console game.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player = null;

        // Introduction screen
        System.out.println();
        System.out.println("Welcome to the Pokémon Ga-Olé arcade game coded in Java!");
        System.out.println();
        System.out.println("To create a new player profile, select option one (1).");
        System.out.println("To log in an existing player profile, select option two (2).");
        System.out.println();
        System.out.print("Select an option: ");

        // Retrieve list of existing saved player profiles.
        List<String> existingPlayers = JsonSaveSystem.getExistingPlayers();

        String mainChoice = scanner.nextLine().trim();

        // Handle main menu choice
        if (mainChoice.equals("2")) {
            if (existingPlayers.isEmpty()) {
                System.out.println("No existing player profiles found. Creating new player instead...");
            } else {
                boolean playerSelected = false;

                // Loop until a player is selected or user chooses to create a new one.
                while (!playerSelected) {
                    // Display list of existing players.
                    System.out.println("\nExisting player profiles:");
                    for (int i = 0; i < existingPlayers.size(); i++) {
                        System.out.println((i + 1) + ". " + existingPlayers.get(i));
                    }

                    // Display additional options.
                    System.out.println("\nOther options:");
                    System.out.println((existingPlayers.size() + 1) + ". Delete a player profile");
                    System.out.println((existingPlayers.size() + 2) + ". Go back to main menu");

                    System.out.print("\nSelect an option (1-" + (existingPlayers.size() + 2) + "): ");

                    try {
                        int playerChoice = Integer.parseInt(scanner.nextLine().trim());

                        // Load selected player profile
                        if (playerChoice >= 1 && playerChoice <= existingPlayers.size()) {
                            String selectedPlayerName = existingPlayers.get(playerChoice - 1);
                            player = JsonSaveSystem.loadGame(selectedPlayerName);

                            if (player != null) {
                                System.out.println("\nWelcome back, Trainer " + player.getName() + "!");
                                playerSelected = true;
                            } else {
                                System.out.println("Failed to load " + selectedPlayerName + ". Please try again.");
                            }

                        // Delete a player's profile.
                        } else if (playerChoice == existingPlayers.size() + 1) {
                            System.out.println("\nSelect a player profile to delete:");
                            for (int i = 0; i < existingPlayers.size(); i++) {
                                System.out.println((i + 1) + ". " + existingPlayers.get(i));
                            }
                            System.out.println((existingPlayers.size() + 1) + ". Cancel deletion");

                            System.out.print("\nSelect a player to delete (1-" + (existingPlayers.size() + 1) + "): ");

                            try {
                                int deleteChoice = Integer.parseInt(scanner.nextLine().trim());

                                if (deleteChoice >= 1 && deleteChoice <= existingPlayers.size()) {
                                    String playerToDelete = existingPlayers.get(deleteChoice - 1);

                                    // Confirm deletion.
                                    System.out.print("Are you sure you want to delete '" + playerToDelete + "'? This cannot be undone. (Yes (y)/No (n)): ");
                                    String confirmation = scanner.nextLine().trim().toLowerCase();

                                    if (confirmation.equals("y") || confirmation.equals("yes")) {
                                        if (JsonSaveSystem.deletePlayerSave(playerToDelete)) {
                                            System.out.println("Player profile '" + playerToDelete + "' has been deleted successfully.");

                                            // Refresh player list after deletion.
                                            existingPlayers = JsonSaveSystem.getExistingPlayers();

                                            // If no profiles remain, proceed to create a new player.
                                            if (existingPlayers.isEmpty()) {
                                                System.out.println("No player profiles remaining. Creating new player...");
                                                playerSelected = true;
                                            }
                                        } else {
                                            System.out.println("Failed to delete player profile '" + playerToDelete + "'.");
                                        }
                                    } else {
                                        System.out.println("Deletion cancelled.");
                                    }
                                } else if (deleteChoice == existingPlayers.size() + 1) {
                                    System.out.println("Deletion cancelled.");
                                } else {
                                    System.out.println("Invalid selection. Deletion cancelled.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Deletion cancelled.");
                            }
                        // Return to the main menu to create a new player profile.
                        } else if (playerChoice == existingPlayers.size() + 2) {
                            System.out.println("Going back to create new player...");
                            playerSelected = true;

                        // Invalid selection handling.
                        } else {
                            System.out.println("Invalid selection. Please try again.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please try again.");
                    }
                }
            }
        } else if (!mainChoice.equals("1")) {
            System.out.println("Invalid choice. Creating new player...");
        }

        // Create a new player profile if none was loaded.
        if (player == null) {
            System.out.println("\n--- Create New Player ---");

            // Refresh player list in case profiles were deleted earlier.
            existingPlayers = JsonSaveSystem.getExistingPlayers();

            String name;
            do {
                System.out.print("Enter your trainer name: ");
                name = scanner.nextLine().trim();

                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty. Please try again.");
                    continue;
                }

                // Prevent duplicate trainer names.
                if (existingPlayers.contains(name)) {
                    System.out.println("Trainer name '" + name + "' already exists. Please choose a different name.");
                    continue;
                }

                break;
            } while (true);

            // Create player profile and assign a starter Pokémon.
            player = new Player(name);
            System.out.println("Welcome, Trainer " + name + "!");
            Pokemon starter = createStarterPokemon();
            player.setPokemon(starter);
            System.out.println("You've been given a starter Pokémon: " + starter.getName() + " (ID " + starter.getId() + ").");
            JsonSaveSystem.saveGame(player);
        }

        // Game start message and tips.
        System.out.println("\nStarting your Pokémon adventure...");
        System.out.println();
        System.out.println("Tip: To play, choose option 1 (Go to Pokémon Center) from the in-game main menu, then insert \u00A5" + START_COST + " when prompted.");
        System.out.println("You can also add funds any time from the main menu (option 5).");

        // NOTE: Removed the mandatory startup deposit prompt — the player can deposit from main menu.
        // The check to ensure at least START_COST is inserted occurs inside Game.pokemonCenter() via ensureInsertedStartCost().

        // Assign starter to loaded players without Pokémon (edge case).
        if (player.getPokemon() == null) {
            Pokemon starter = createStarterPokemon();
            player.setPokemon(starter);
            System.out.println("No Pokémon found for this trainer. A starter Pokémon (" + starter.getName() + ") has been assigned.");
            JsonSaveSystem.saveGame(player);
        }

        // Launch game
        Game game = new Game(player);
        game.startGame();

        // Auto-save on exit
        JsonSaveSystem.saveGame(player);
        System.out.println("Your progress has been automatically saved!");

        scanner.close();
        System.out.println("Goodbye, Trainer " + player.getName() + "!");
    }

    // Creates a default starter Pokémon (Pikachu) with sensible statistics and moves.
    private static Pokemon createStarterPokemon() {
        Pokemon p = new Pokemon();
        p.setId(25);
        p.setName("Pikachu");
        p.setTypes(new ArrayList<>(Arrays.asList("Electric")));
        p.setMaxHp(35);
        p.setHp(35);
        p.setAttack(55);
        p.setDefense(40);
        p.setGrade(1);
        p.setEvolvesToId(null);
        p.setMegaCapable(false);
        p.setZMoveCapable(true);

        // Add default moves.
        p.setMoves(new ArrayList<>());
        p.addMove(new BasicAttack());

        // Attempt to add a type-based move for Electric type.
        try {
            Type t = null;
            try { t = Type.fromString("Electric"); } catch (Exception ignored) {}
            if (t == null) {
                try { t = Type.fromString("ELECTRIC"); } catch (Exception ignored) {}
            }
            if (t == null) {
                try { t = Type.fromString(p.getTypes().get(0)); } catch (Exception ignored) {}
            }
            if (t != null) {
                p.addMove(new TypeMove(t));
            }
        } catch (Throwable ignored) {
            // Ignore errors and proceed with BasicAttack only.
        }

        return p;
    }
}

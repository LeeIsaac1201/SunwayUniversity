// Imports for null-checking and object utilities, generating random numbers, as well as reading player input.
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

// Represents a Pokémon battle between the player and an opponent.
public class Battle {

    // Fields for storing the player's Pokémon, opponent's Pokémon, input scanner, random generator, and player stats.
    private final Pokemon playerPokemon;
    private final Pokemon opponentPokemon;
    private final Scanner scanner;
    private final Random random;
    private final Player player;

    // Constructor for initialising the battle with the player's Pokémon, opponent's Pokémon, and player statistics.
    public Battle(Pokemon playerPokemon, Pokemon opponentPokemon, Player player) {
        this.playerPokemon = Objects.requireNonNull(playerPokemon, "playerPokemon must not be null");
        this.opponentPokemon = Objects.requireNonNull(opponentPokemon, "opponentPokemon must not be null");
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.scanner = new Scanner(System.in);
        this.random = new Random();
    }

    // Main battle loop: Runs until one Pokémon faints, then declares the winner and updates statistics.
    public void start() {
        System.out.println("\n=== BATTLE START ===");
        System.out.printf("%s vs %s!\n", playerPokemon.getName(), opponentPokemon.getName());
        System.out.println("=" + "=".repeat(20) + "=");

        while (!playerPokemon.isFainted() && !opponentPokemon.isFainted()) {
            if (!playerPokemon.isFainted()) {
                playerTurn();
            }

            if (opponentPokemon.isFainted()) break;

            if (!opponentPokemon.isFainted()) {
                opponentTurn();
            }

            showBattleStatus();

            try {
                Thread.sleep(1000); // Small delay for pacing.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n=== BATTLE END ===");
        if (playerPokemon.isFainted()) {
            System.out.println(playerPokemon.getName() + " fainted! You lost the battle...");
            player.incrementBattlesLost();
        } else if (opponentPokemon.isFainted()) {
            System.out.println(opponentPokemon.getName() + " fainted! You won the battle!");
            player.incrementBattlesWon();
        }
    }

    // Handles the player's turn, including move selection and execution.
    private void playerTurn() {
        if (playerPokemon.getMoves() == null || playerPokemon.getMoves().isEmpty()) {
            System.out.println(playerPokemon.getName() + " has no moves! Using struggle...");
            struggleAttack(playerPokemon, opponentPokemon);
            return;
        }

        System.out.println("\n" + playerPokemon.getName() + "'s turn!");
        System.out.println("Choose a move:");

        for (int i = 0; i < playerPokemon.getMoves().size(); i++) {
            System.out.println((i + 1) + ". " + playerPokemon.getMoves().get(i).name());
        }

        System.out.print("Enter move number: ");

        try {
            String line = scanner.nextLine();
            int choice = -1;
            try {
                if (line != null) choice = Integer.parseInt(line.trim()) - 1;
            } catch (NumberFormatException nfe) {
                choice = -1;
            }

            if (choice >= 0 && choice < playerPokemon.getMoves().size()) {
                Move selectedMove = playerPokemon.getMoves().get(choice);
                selectedMove.execute(playerPokemon, opponentPokemon);
            } else {
                System.out.println("Invalid move selection! Using first available move...");
                playerPokemon.getMoves().get(0).execute(playerPokemon, opponentPokemon);
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Using first available move...");
            try { scanner.nextLine(); } catch (Exception ignored) {}
            playerPokemon.getMoves().get(0).execute(playerPokemon, opponentPokemon);
        }
    }

    // Handles the opponent's turn by randomly selecting and executing a move.
    private void opponentTurn() {
        if (opponentPokemon.getMoves() == null || opponentPokemon.getMoves().isEmpty()) {
            System.out.println(opponentPokemon.getName() + " has no moves! Using struggle...");
            struggleAttack(opponentPokemon, playerPokemon);
            return;
        }

        System.out.println("\n" + opponentPokemon.getName() + "'s turn!");

        Move selectedMove = opponentPokemon.getMoves().get(random.nextInt(opponentPokemon.getMoves().size()));
        selectedMove.execute(opponentPokemon, playerPokemon);
    }

    // Executes the Struggle move, dealing damage to the target and recoil to the attacker.
    private void struggleAttack(Pokemon attacker, Pokemon target) {
        int damage = Math.max(1, attacker.getAttack() / 4);
        System.out.printf("%s used Struggle!\n", attacker.getName());
        target.takeDamage(damage);

        int recoilDamage = Math.max(1, damage / 4);
        attacker.takeDamage(recoilDamage);
        System.out.printf("%s is hurt by recoil!\n", attacker.getName());
    }

    // Displays the current health points of both Pokémon.
    private void showBattleStatus() {
        System.out.println("\n--- Battle Status ---");
        System.out.printf("%s: %d/%d health points\n",
            playerPokemon.getName(), playerPokemon.getHp(), playerPokemon.getMaxHp());
        System.out.printf("%s: %d/%d health points\n",
            opponentPokemon.getName(), opponentPokemon.getHp(), opponentPokemon.getMaxHp());
        System.out.println("--------------------");
    }
}

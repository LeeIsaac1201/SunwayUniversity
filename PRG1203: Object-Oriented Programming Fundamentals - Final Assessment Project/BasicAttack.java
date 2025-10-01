import java.util.concurrent.ThreadLocalRandom; // Import for generating random numbers.

// Represents a basic attack move in the game.
public class BasicAttack implements Move {

    @Override
    public String name() {
        // Returns the name of the move.
        return "Tackle";
    }

    @Override
    public int execute(Pokemon user, Pokemon target) {
        // Get attack and defense stats.
        int atk = user.getAttack();
        int def = target.getDefense();

        // Generate a random multiplier between 0.85 and 1.0.
        double rand = 0.85 + ThreadLocalRandom.current().nextDouble() * 0.15;

        // Calculate damage with a minimum of 1.
        int dmg = (int) Math.max(1, Math.round((atk - def * 0.5) * rand));

        // Display the move being used.
        System.out.printf("%s used %s!\n", user.getName(), name());

        // Apply damage to the target.
        target.takeDamage(dmg);

        // Return the damage dealt.
        return dmg;
    }
}

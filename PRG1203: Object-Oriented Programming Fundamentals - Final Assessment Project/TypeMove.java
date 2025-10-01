import java.util.concurrent.ThreadLocalRandom; // Import for generating random values used in damage variation.

// Represents a type based move that uses the Pokemon type effectiveness chart.
public class TypeMove implements Move {
    private final Type moveType; // The elemental type of this move.

    // Constructs a TypeMove with the specified elemental type.
    public TypeMove(Type moveType) {
        this.moveType = moveType == null ? Type.NORMAL : moveType;
    }

    // Returns the display name of the move in the form "Electric Strike."
    @Override
    public String name() {
        return formatDisplay(moveType.name()) + " Strike";
    }

    // Executes the move, calculates damage, applies it to the target, and returns the damage dealt.
    @Override
    public int execute(Pokemon user, Pokemon target) {
        if (user == null || target == null) return 0;

        int atk = Math.max(1, user.getAttack());
        int def = Math.max(0, target.getDefense());

        // Determine the target primary type and default to NORMAL when no types are present.
        Type targetType = Type.NORMAL;
        if (target.getTypes() != null && !target.getTypes().isEmpty()) {
            targetType = Type.fromString(target.getTypes().get(0));
            if (targetType == null) targetType = Type.NORMAL;
        }

        // Calculate type effectiveness and a random variation factor for damage.
        double eff = TypeChart.getEffectiveness(moveType, targetType);
        double randFactor = 0.85 + ThreadLocalRandom.current().nextDouble() * 0.15;
        int damage = (int) Math.max(1, (atk * 1.5 - def * 0.5) * eff * randFactor);

        System.out.printf("%s used %s!\n", user.getName(), name());

        if (eff <= 0.0) {
            System.out.println("It has no effect.");
            damage = 0;
        } else if (eff > 1.0) {
            System.out.println("It is super effective.");
        } else if (eff < 1.0) {
            System.out.println("It is not very effective.");
        }

        if (damage > 0) {
            target.takeDamage(damage);
            System.out.printf("%s dealt %d damage to %s.\n", user.getName(), damage, target.getName());
        } else {
            System.out.printf("%s dealt no damage to %s.\n", user.getName(), target.getName());
        }

        return damage;
    }

    // Helper method to convert an enum name such as "ELECTRIC" to title case "Electric."
    private String formatDisplay(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replace('_', ' ').toLowerCase();
        if (s.isEmpty()) return "";
        String[] parts = s.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() == 0) continue;
            parts[i] = Character.toUpperCase(parts[i].charAt(0)) + (parts[i].length() > 1 ? parts[i].substring(1) : "");
        }
        return String.join(" ", parts);
    }
}

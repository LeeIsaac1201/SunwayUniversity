// Imports for using EnumMap and Map collections.
import java.util.EnumMap;
import java.util.Map;

// Represents the type effectiveness chart for Pokémon battles and stores how effective one type is against another.
public class TypeChart {
    // Nested map structure: Attacker type → (defender type → effectiveness multiplier).
    private static final Map<Type, Map<Type, Double>> chart = new EnumMap<>(Type.class);

    static {
        // Initialise all rows with default 1.0
        for (Type atk : Type.values()) {
            Map<Type, Double> row = new EnumMap<>(Type.class);
            for (Type def : Type.values()) {
                row.put(def, 1.0);
            }
            chart.put(atk, row);
        }

        // Helper lambdas (methods) to set common multipliers.
        // Super effective = 2.0
        java.util.function.BiConsumer<Type, Type> superEff = (a, d) -> chart.get(a).put(d, 2.0);
        // Not very effective = 0.5
        java.util.function.BiConsumer<Type, Type> notEff = (a, d) -> chart.get(a).put(d, 0.5);
        // No effect / immune = 0.0
        java.util.function.BiConsumer<Type, Type> noEff = (a, d) -> chart.get(a).put(d, 0.0);

        // Normal
        notEff.accept(Type.NORMAL, Type.ROCK);
        notEff.accept(Type.NORMAL, Type.STEEL);
        noEff.accept(Type.NORMAL, Type.GHOST);

        // Fire
        superEff.accept(Type.FIRE, Type.GRASS);
        superEff.accept(Type.FIRE, Type.ICE);
        superEff.accept(Type.FIRE, Type.BUG);
        superEff.accept(Type.FIRE, Type.STEEL);
        notEff.accept(Type.FIRE, Type.FIRE);
        notEff.accept(Type.FIRE, Type.WATER);
        notEff.accept(Type.FIRE, Type.ROCK);
        notEff.accept(Type.FIRE, Type.DRAGON);

        // Water
        superEff.accept(Type.WATER, Type.FIRE);
        superEff.accept(Type.WATER, Type.GROUND);
        superEff.accept(Type.WATER, Type.ROCK);
        notEff.accept(Type.WATER, Type.WATER);
        notEff.accept(Type.WATER, Type.GRASS);
        notEff.accept(Type.WATER, Type.DRAGON);

        // Electric
        superEff.accept(Type.ELECTRIC, Type.WATER);
        superEff.accept(Type.ELECTRIC, Type.FLYING);
        notEff.accept(Type.ELECTRIC, Type.ELECTRIC);
        notEff.accept(Type.ELECTRIC, Type.GRASS);
        notEff.accept(Type.ELECTRIC, Type.DRAGON);
        noEff.accept(Type.ELECTRIC, Type.GROUND);

        // Grass
        superEff.accept(Type.GRASS, Type.WATER);
        superEff.accept(Type.GRASS, Type.GROUND);
        superEff.accept(Type.GRASS, Type.ROCK);
        notEff.accept(Type.GRASS, Type.FIRE);
        notEff.accept(Type.GRASS, Type.GRASS);
        notEff.accept(Type.GRASS, Type.POISON);
        notEff.accept(Type.GRASS, Type.FLYING);
        notEff.accept(Type.GRASS, Type.BUG);
        notEff.accept(Type.GRASS, Type.DRAGON);
        notEff.accept(Type.GRASS, Type.STEEL);

        // Ice
        superEff.accept(Type.ICE, Type.GRASS);
        superEff.accept(Type.ICE, Type.GROUND);
        superEff.accept(Type.ICE, Type.FLYING);
        superEff.accept(Type.ICE, Type.DRAGON);
        notEff.accept(Type.ICE, Type.FIRE);
        notEff.accept(Type.ICE, Type.WATER);
        notEff.accept(Type.ICE, Type.ICE);
        notEff.accept(Type.ICE, Type.STEEL);

        // Fighting
        superEff.accept(Type.FIGHTING, Type.NORMAL);
        superEff.accept(Type.FIGHTING, Type.ICE);
        superEff.accept(Type.FIGHTING, Type.ROCK);
        superEff.accept(Type.FIGHTING, Type.DARK);
        superEff.accept(Type.FIGHTING, Type.STEEL);
        notEff.accept(Type.FIGHTING, Type.POISON);
        notEff.accept(Type.FIGHTING, Type.FLYING);
        notEff.accept(Type.FIGHTING, Type.PSYCHIC);
        notEff.accept(Type.FIGHTING, Type.BUG);
        notEff.accept(Type.FIGHTING, Type.FAIRY);
        noEff.accept(Type.FIGHTING, Type.GHOST);

        // Poison
        superEff.accept(Type.POISON, Type.GRASS);
        superEff.accept(Type.POISON, Type.FAIRY);
        notEff.accept(Type.POISON, Type.POISON);
        notEff.accept(Type.POISON, Type.GROUND);
        notEff.accept(Type.POISON, Type.ROCK);
        notEff.accept(Type.POISON, Type.GHOST);
        noEff.accept(Type.POISON, Type.STEEL);

        // Ground
        superEff.accept(Type.GROUND, Type.FIRE);
        superEff.accept(Type.GROUND, Type.ELECTRIC);
        superEff.accept(Type.GROUND, Type.POISON);
        superEff.accept(Type.GROUND, Type.ROCK);
        superEff.accept(Type.GROUND, Type.STEEL);
        notEff.accept(Type.GROUND, Type.GRASS);
        notEff.accept(Type.GROUND, Type.BUG);
        noEff.accept(Type.GROUND, Type.FLYING);

        // Flying
        superEff.accept(Type.FLYING, Type.FIGHTING);
        superEff.accept(Type.FLYING, Type.BUG);
        superEff.accept(Type.FLYING, Type.GRASS);
        notEff.accept(Type.FLYING, Type.ELECTRIC);
        notEff.accept(Type.FLYING, Type.ROCK);
        notEff.accept(Type.FLYING, Type.STEEL);

        // Psychic
        superEff.accept(Type.PSYCHIC, Type.FIGHTING);
        superEff.accept(Type.PSYCHIC, Type.POISON);
        notEff.accept(Type.PSYCHIC, Type.PSYCHIC);
        notEff.accept(Type.PSYCHIC, Type.STEEL);
        noEff.accept(Type.PSYCHIC, Type.DARK);

        // Bug
        superEff.accept(Type.BUG, Type.GRASS);
        superEff.accept(Type.BUG, Type.PSYCHIC);
        superEff.accept(Type.BUG, Type.DARK);
        notEff.accept(Type.BUG, Type.FIRE);
        notEff.accept(Type.BUG, Type.FIGHTING);
        notEff.accept(Type.BUG, Type.POISON);
        notEff.accept(Type.BUG, Type.FLYING);
        notEff.accept(Type.BUG, Type.GHOST);
        notEff.accept(Type.BUG, Type.STEEL);
        notEff.accept(Type.BUG, Type.FAIRY);

        // Rock
        superEff.accept(Type.ROCK, Type.FIRE);
        superEff.accept(Type.ROCK, Type.ICE);
        superEff.accept(Type.ROCK, Type.FLYING);
        superEff.accept(Type.ROCK, Type.BUG);
        notEff.accept(Type.ROCK, Type.FIGHTING);
        notEff.accept(Type.ROCK, Type.GROUND);
        notEff.accept(Type.ROCK, Type.STEEL);

        // Ghost
        superEff.accept(Type.GHOST, Type.GHOST);
        superEff.accept(Type.GHOST, Type.PSYCHIC);
        notEff.accept(Type.GHOST, Type.DARK);
        noEff.accept(Type.GHOST, Type.NORMAL);
        noEff.accept(Type.GHOST, Type.FIGHTING);

        // Dragon
        superEff.accept(Type.DRAGON, Type.DRAGON);
        notEff.accept(Type.DRAGON, Type.STEEL);
        noEff.accept(Type.DRAGON, Type.FAIRY);

        // Dark
        superEff.accept(Type.DARK, Type.PSYCHIC);
        superEff.accept(Type.DARK, Type.GHOST);
        notEff.accept(Type.DARK, Type.FIGHTING);
        notEff.accept(Type.DARK, Type.DARK);
        notEff.accept(Type.DARK, Type.FAIRY);

        // Steel
        superEff.accept(Type.STEEL, Type.ICE);
        superEff.accept(Type.STEEL, Type.ROCK);
        superEff.accept(Type.STEEL, Type.FAIRY);
        notEff.accept(Type.STEEL, Type.FIRE);
        notEff.accept(Type.STEEL, Type.WATER);
        notEff.accept(Type.STEEL, Type.ELECTRIC);
        notEff.accept(Type.STEEL, Type.STEEL);

        // Fairy
        superEff.accept(Type.FAIRY, Type.FIGHTING);
        superEff.accept(Type.FAIRY, Type.DRAGON);
        superEff.accept(Type.FAIRY, Type.DARK);
        notEff.accept(Type.FAIRY, Type.FIRE);
        notEff.accept(Type.FAIRY, Type.POISON);
        notEff.accept(Type.FAIRY, Type.STEEL);
    }

    // Retrieves the effectiveness multiplier for a given attacker → defender type matchup; returns 1.0 if no specific matchup is defined.
    public static double getEffectiveness(Type attacker, Type defender) {
        if (attacker == null || defender == null) return 1.0;
        return chart.getOrDefault(attacker, Map.of()).getOrDefault(defender, 1.0);
    }
}

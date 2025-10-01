// Enumeration representing the basic Pokémon types supported in the game.
public enum Type {
NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON, GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY;
    // Convert a string to a Type in a forgiving, case-insensitive way.
public static Type fromString(String s) {
    if (s == null) return NORMAL;
    String t = s.trim().replace('_', ' ').replaceAll("\\s+", " ").toUpperCase();
    // Handle a few common short forms and synonyms.
    switch (t) {
        case "ELEC":
        case "ELECTRICAL":
            return ELECTRIC;
        case "PSY":
            return PSYCHIC;
        case "GRASS":
            return GRASS;
        case "POISON":
            return POISON;
        case "FLY":
        case "FLYING":
            return FLYING;
        case "STEEL":
            return STEEL;
        case "FAIRY":
            return FAIRY;
        default:
            try {
                return Type.valueOf(t);
            } catch (IllegalArgumentException e) {
                return NORMAL;
            }
    }
}

// Return a user-friendly Title Case name for display.
@Override
public String toString() {
    String lower = name().toLowerCase();
    if (lower.isEmpty()) return "";
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
}
}

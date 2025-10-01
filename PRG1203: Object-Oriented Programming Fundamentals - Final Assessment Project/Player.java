// Imports for serialisation support and list collection utilities.
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Represents a player profile with a name, Pokémon collection, battle stats, and yen balance.
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Pokemon> pokemons; // Player's Pokémon collection; index 0 is the active Pokémon.
    private int battlesWon;
    private int battlesLost;
    private int yen;

    // Creates a new player with an empty collection, zero stats, and no yen.
    public Player(String name) {
        this.name = name;
        this.pokemons = new ArrayList<>();
        this.battlesWon = 0;
        this.battlesLost = 0;
        this.yen = 0;
    }

    // Name accessor.
    public String getName() {
        return name;
    }

    // Active Pokémon accessors: Returns the active Pokémon (index 0), or null if none exist. 
    public Pokemon getPokemon() {
        if (pokemons == null || pokemons.isEmpty()) return null;
        return pokemons.get(0);
    }

    // Sets or replaces the active Pokémon (index 0); adds it if the collection is empty.
    public void setPokemon(Pokemon pokemon) {
        if (pokemon == null) return;
        if (pokemons == null) pokemons = new ArrayList<>();
        if (pokemons.isEmpty()) pokemons.add(pokemon);
        else pokemons.set(0, pokemon);
    }

    // Collection accessors.
    public List<Pokemon> getPokemons() {
        if (pokemons == null) pokemons = new ArrayList<>();
        return pokemons;
    }

    public void setPokemons(List<Pokemon> pokemons) {
        this.pokemons = pokemons == null ? new ArrayList<>() : pokemons;
    }

    // Adds a Pokémon to the collection (appended to the end).
    public void addPokemon(Pokemon p) {
        if (p == null) return;
        if (pokemons == null) pokemons = new ArrayList<>();
        pokemons.add(p);
    }

    // Removes a Pokémon by index; returns the removed Pokémon or null if invalid.
    public Pokemon removePokemon(int index) {
        if (pokemons == null || index < 0 || index >= pokemons.size()) return null;
        return pokemons.remove(index);
    }

    // Duplicate detection helpers: Returns true if the collection contains an equivalent Pokémon. 
    public boolean containsEquivalent(Pokemon p) {
        return indexOfEquivalent(p) != -1;
    }

    // Returns the index of the first equivalent Pokémon, or -1 if none found.
    public int indexOfEquivalent(Pokemon p) {
        if (p == null || pokemons == null || pokemons.isEmpty()) return -1;
        for (int i = 0; i < pokemons.size(); i++) {
            Pokemon existing = pokemons.get(i);
            if (existing != null && existing.isEquivalent(p)) {
                return i;
            }
        }
        return -1;
    }

    // Battle statistics.
    public int getBattlesWon() {
        return battlesWon;
    }

    public void setBattlesWon(int battlesWon) {
        this.battlesWon = battlesWon;
    }

    public int getBattlesLost() {
        return battlesLost;
    }

    public void setBattlesLost(int battlesLost) {
        this.battlesLost = battlesLost;
    }

    public void incrementBattlesWon() {
        this.battlesWon++;
    }

    public void incrementBattlesLost() {
        this.battlesLost++;
    }

    // Yen management.
    public int getYen() {
        return this.yen;
    }

    public void setYen(int yen) {
        this.yen = yen;
    }

    public synchronized void addYen(int amount) {
        if (amount > 0) this.yen += amount;
    }

    // Attempts to spend yen; returns true if successful.
    public synchronized boolean spendYen(int amount) {
        if (amount <= 0) return false;
        if (this.yen >= amount) {
            this.yen -= amount;
            return true;
        }
        return false;
    }

    // Console Display: Prints the player's stats and balance to the console.
    public void displayStats() {
        System.out.println("=== Trainer Stats ===");
        System.out.println("Name: " + name);
        System.out.println("Battles Won: " + battlesWon);
        System.out.println("Battles Lost: " + battlesLost);
        if (battlesWon + battlesLost > 0) {
            double winRate = (double) battlesWon / (battlesWon + battlesLost) * 100;
            System.out.printf("Win Rate: %.1f%%\n", winRate);
        }
        System.out.println("Balance: ¥" + this.yen);
    }
}

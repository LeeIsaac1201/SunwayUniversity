// Imports for list handling, array utilities, and sorting.
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

// Represents a lightweight Pokémon model for the Ga-Olé console demonstration, including statistics, types, moves, and meta fields for evolutions and special capabilities.
public class Pokemon {
    private int id;
    private String name;
    private List<String> types;
    private int maxHp;
    private int hp;
    private int attack;
    private int defense;
    private int specialAttack = 0;
    private int specialDefense = 0;
    private int speed = 0;
    private int energy = 0;
    private List<Move> moves;
    private int grade = 1;
    private Integer evolvesToId = null;
    private boolean megaCapable = false;
    private boolean zMoveCapable = false;

    // Default constructor for JavaScript Object Notation (JSON) deserialisation.
    public Pokemon() {
        this.moves = new ArrayList<>();
        this.types = new ArrayList<>();
    }

    // Constructor for manual creation.
    public Pokemon(String name, List<String> types, int maxHp, int attack, int defense) {
        this.name = name;
        this.types = types;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.moves = new ArrayList<>();
    }

    // Getters and setters.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpecialAttack() {
        return specialAttack;
    }

    public void setSpecialAttack(int specialAttack) {
        this.specialAttack = specialAttack;
    }

    public int getSpecialDefense() {
        return specialDefense;
    }

    public void setSpecialDefense(int specialDefense) {
        this.specialDefense = specialDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Integer getEvolvesToId() {
        return evolvesToId;
    }

    public void setEvolvesToId(Integer evolvesToId) {
        this.evolvesToId = evolvesToId;
    }

    public boolean isMegaCapable() {
        return megaCapable;
    }

    public void setMegaCapable(boolean megaCapable) {
        this.megaCapable = megaCapable;
    }

    public boolean isZMoveCapable() {
        return zMoveCapable;
    }

    public void setZMoveCapable(boolean zMoveCapable) {
        this.zMoveCapable = zMoveCapable;
    }

    // Move-related methods.
    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public void addMove(Move move) {
        if (moves == null) moves = new ArrayList<>();
        if (moves.size() < 4) { // Pokemon typically have a maximum of four moves.
            moves.add(move);
        }
    }

    public void removeMove(Move move) {
        if (moves == null) return;
        moves.remove(move);
    }

    // Gameplay methods.
    public boolean isFainted() {
        return hp <= 0;
    }

    public void takeDamage(int damage) {
        hp = Math.max(hp - damage, 0);
        System.out.printf("%s took %d damage and now has %d health points.\n", name, damage, hp);
    }

    public void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
        System.out.printf("%s healed %d HP and now has %d health points.\n", name, amount, hp);
    }

    public void fullHeal() {
        hp = maxHp;
        System.out.printf("%s was fully healed to %d health points.\n", name, hp);
    }

    @Override
    public String toString() {
        return String.format("%s (Health Points (HP): %d/%d, Attack (ATK): %d, Special Attack (SP ATK): %d, Defense (DEF): %d, Special Defense (SP DEF): %d, Speed: %d, Energy: %d, Types: %s)",
                           name, hp, maxHp, attack, specialAttack, defense, specialDefense, speed, energy, types);
    }

    // Determines if another Pokémon is equivalent (same identification number, statistics, types, and moves).
    public boolean isEquivalent(Pokemon other) {
        if (other == null) return false;
        if (this.id != other.id) return false;
        if (this.maxHp != other.maxHp) return false;
        if (this.attack != other.attack) return false;
        if (this.specialAttack != other.specialAttack) return false;
        if (this.defense != other.defense) return false;
        if (this.specialDefense != other.specialDefense) return false;
        if (this.speed != other.speed) return false;

        // Compare types (case/whitespace-insensitive, order-insensitive).
        List<String> myTypes = new ArrayList<>();
        List<String> otherTypes = new ArrayList<>();
        if (this.types != null) myTypes.addAll(this.types);
        if (other.types != null) otherTypes.addAll(other.types);

        normalizeStringList(myTypes);
        normalizeStringList(otherTypes);
        Collections.sort(myTypes);
        Collections.sort(otherTypes);
        if (!myTypes.equals(otherTypes)) return false;

        // Compare move names (case/whitespace-insensitive, order-insensitive).
        List<String> myMoves = getMoveNamesList();
        List<String> otherMoves = other.getMoveNamesList();

        normalizeStringList(myMoves);
        normalizeStringList(otherMoves);
        Collections.sort(myMoves);
        Collections.sort(otherMoves);
        return myMoves.equals(otherMoves);
    }

    // Helper: Produce a list of move names (null-safe).
    private List<String> getMoveNamesList() {
        List<String> out = new ArrayList<>();
        if (this.moves == null) return out;
        for (Move m : this.moves) {
            if (m == null) out.add("");
            else {
                try {
                    String n = m.name();
                    out.add(n == null ? "" : n);
                } catch (Exception e) {
                    // In case Move implementation behaves oddly.
                    out.add("");
                }
            }
        }
        return out;
    }

    // Helper: Normalise strings in-place (trim + lowercase + null->empty).
    private void normalizeStringList(List<String> list) {
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s == null) s = "";
            s = s.trim().toLowerCase();
            list.set(i, s);
        }
    }
}

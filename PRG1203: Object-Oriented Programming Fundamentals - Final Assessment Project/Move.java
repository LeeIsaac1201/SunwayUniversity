// Represents a Pokémon move that can be executed in battle.
public interface Move {
    String name(); // Returns the name of the move.
    int execute(Pokemon user, Pokemon target); // Executes the move using the given user and target Pokémon, returning the damage dealt.
}

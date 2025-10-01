// Imports for file input/output (I/O), working with file paths, and using collections.
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JsonSaveSystem {
    // Directory and file extension used for saving player data.
    private static final String SAVE_DIRECTORY = "saves/";
    private static final String SAVE_FILE_EXTENSION = ".json";

    // Ensures the save directory exists, creating it if necessary.
    private static void ensureSaveDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Failed to create save directory: " + e.getMessage());
        }
    }

    // Generates a sanitised file name for the player's save file.
    private static String getPlayerFileName(String playerName) {
        String sanitized = playerName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        return SAVE_DIRECTORY + sanitized + SAVE_FILE_EXTENSION;
    }

    // Saves the player's game data to a JavaScript Object Notation (JSON) file. This write format uses the canonical keys: "pokemon" entries include "move" (single-element array).
    public static boolean saveGame(Player player) {
        ensureSaveDirectoryExists();
        String fileName = getPlayerFileName(player.getName());

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"playerName\": \"").append(escapeJson(player.getName())).append("\",\n");
            json.append("  \"battlesWon\": ").append(player.getBattlesWon()).append(",\n");
            json.append("  \"battlesLost\": ").append(player.getBattlesLost()).append(",\n");
            json.append("  \"yen\": ").append(player.getYen()).append(",\n");

            // Serialise player's Pokémon collection.
            List<Pokemon> pokes = player.getPokemons();
            if (pokes == null) pokes = new ArrayList<>();

            json.append("  \"pokemon\": [\n");
            for (int i = 0; i < pokes.size(); i++) {
                Pokemon p = pokes.get(i);
                json.append("    {\n");
                json.append("      \"id\": ").append(p.getId()).append(",\n");
                json.append("      \"name\": \"").append(escapeJson(p.getName())).append("\",\n");

                // Pokémon types.
                json.append("      \"types\": [");
                if (p.getTypes() != null) {
                    for (int t = 0; t < p.getTypes().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"").append(escapeJson(p.getTypes().get(t))).append("\"");
                    }
                }
                json.append("],\n");

                // Pokémon statistics.
                json.append("      \"maxHp\": ").append(p.getMaxHp()).append(",\n");
                json.append("      \"hp\": ").append(p.getHp()).append(",\n");
                json.append("      \"attack\": ").append(p.getAttack()).append(",\n");
                json.append("      \"defense\": ").append(p.getDefense()).append(",\n");

                // Optional meta fields (safe fallbacks if missing).
                try {
                    json.append("      \"grade\": ").append(p.getGrade()).append(",\n");
                } catch (Throwable ignore) { json.append("      \"grade\": 0,\n"); }
                try {
                    Integer evo = p.getEvolvesToId();
                    json.append("      \"evolvesToId\": ").append(evo == null ? "null" : evo).append(",\n");
                } catch (Throwable ignore) { json.append("      \"evolvesToId\": null,\n"); }
                try {
                    json.append("      \"megaCapable\": ").append(p.isMegaCapable()).append(",\n");
                } catch (Throwable ignore) { json.append("      \"megaCapable\": false,\n"); }
                try {
                    json.append("      \"zMoveCapable\": ").append(p.isZMoveCapable()).append(",\n");
                } catch (Throwable ignore) { json.append("      \"zMoveCapable\": false,\n"); }

                // "move" - Canonical single-element array: write the first move if present.
                String firstMove = null;
                if (p.getMoves() != null && !p.getMoves().isEmpty()) {
                    firstMove = p.getMoves().get(0).name();
                }
                json.append("      \"move\": [");
                if (firstMove != null) {
                    json.append("\"").append(escapeJson(firstMove)).append("\"");
                }
                json.append("]\n");

                json.append("    }");
                if (i < pokes.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");
            json.append("}");

            // Write JSON to file.
            writer.write(json.toString());
            System.out.println("Game saved to " + fileName);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
            return false;
        }
    }

    // Loads a saved game for the specified player name from disk.
    public static Player loadGame(String playerName) {
        String fileName = getPlayerFileName(playerName);
        File saveFile = new File(fileName);
        if (!saveFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            String json = content.toString();

            // Extract player data from JSON.
            String loadedPlayerName = extractJsonString(json, "playerName");
            int battlesWon = extractJsonInt(json, "battlesWon");
            int battlesLost = extractJsonInt(json, "battlesLost");
            int yen = extractJsonInt(json, "yen");

            // Create and populate Player object.
            Player player = new Player(loadedPlayerName);
            player.setBattlesWon(battlesWon);
            player.setBattlesLost(battlesLost);
            player.setYen(yen);

            // Loads Pokémon collection.
            List<Pokemon> pokes = parsePokemonArrayFromJson(json);
            player.setPokemons(pokes);

            System.out.println("Game loaded successfully! Welcome back, " + loadedPlayerName + "!");
            return player;
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return null;
        }
    }

    // Loads the first available saved game (if any).
    public static Player loadGame() {
        List<String> players = getExistingPlayers();
        if (players.isEmpty()) return null;
        return loadGame(players.get(0));
    }

    // Retrieves a list of all saved player names from the save directory.
    public static List<String> getExistingPlayers() {
        List<String> playerNames = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(SAVE_DIRECTORY))) return playerNames;
            Files.walk(Paths.get(SAVE_DIRECTORY))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(SAVE_FILE_EXTENSION))
                .forEach(path -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) content.append(line).append("\n");
                        String json = content.toString();
                        String playerName = extractJsonString(json, "playerName");
                        if (playerName != null && !playerName.isEmpty() && !playerNames.contains(playerName)) {
                            playerNames.add(playerName);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to read player from " + path + ": " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("Failed to scan save directory: " + e.getMessage());
        }
        Collections.sort(playerNames);
        return playerNames;
    }

    // Checks if at least one save file exists.
    public static boolean saveFileExists() {
        return !getExistingPlayers().isEmpty();
    }

    // Deletes the save file for the specified player.
    public static boolean deletePlayerSave(String playerName) {
        try {
            String fileName = getPlayerFileName(playerName);
            return Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            System.err.println("Failed to delete save for " + playerName + ": " + e.getMessage());
            return false;
        }
    }

    // Extracts a string value from a JSON string for the given key.
    private static String extractJsonString(String json, String key) {
        String searchPattern = "\"" + key + "\": \"";
        int start = json.indexOf(searchPattern);
        if (start == -1) return "";
        start += searchPattern.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    // Extracts an integer value from a JSON string for the given key.
    private static int extractJsonInt(String json, String key) {
        String searchPattern = "\"" + key + "\": ";
        int start = json.indexOf(searchPattern);
        if (start == -1) return 0;
        start += searchPattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) return 0;
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Extracts a boolean value from a JSON string for the given key.
    private static boolean extractJsonBoolean(String json, String key) {
        String searchPattern = "\"" + key + "\": ";
        int start = json.indexOf(searchPattern);
        if (start == -1) return false;
        start += searchPattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) return false;
        String token = json.substring(start, end).trim();
        return token.equalsIgnoreCase("true");
    }

    // Parses the Pokémon array from a JSON string into a list of Pokémon objects.
    private static List<Pokemon> parsePokemonArrayFromJson(String json) {
        List<Pokemon> result = new ArrayList<>();
        String search = "\"pokemon\":";
        int idx = json.indexOf(search);
        if (idx == -1) return result;
        idx = json.indexOf("[", idx);
        if (idx == -1) return result;
        int end = json.indexOf("]", idx);
        if (end == -1) return result;

        // Scan for object blocks between idx and end.
        int i = idx + 1;
        while (i < end) {
            int objStart = json.indexOf("{", i);
            if (objStart == -1 || objStart > end) break;

            // Find matching closing brace.
            int depth = 0;
            int j = objStart;
            for (; j <= end; j++) {
                char c = json.charAt(j);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) break;
                }
            }
            if (j > end) break;

            String obj = json.substring(objStart, j + 1);
            Pokemon p = parsePokemonFromFragment(obj);
            if (p != null) result.add(p);
            i = j + 1;
        }
        return result;
    }

    // Parses a single Pokémon object from a JSON fragment. This parse expects the canonical save format. It reads "move" (single-element array) only.
    private static Pokemon parsePokemonFromFragment(String fragment) {
        try {
            Pokemon pokemon = new Pokemon();
            pokemon.setId(extractJsonInt(fragment, "id"));
            pokemon.setName(extractJsonString(fragment, "name"));
            pokemon.setMaxHp(extractJsonInt(fragment, "maxHp"));
            pokemon.setHp(extractJsonInt(fragment, "hp"));
            pokemon.setAttack(extractJsonInt(fragment, "attack"));
            pokemon.setDefense(extractJsonInt(fragment, "defense"));

            // Optional meta fields.
            Integer grade = extractJsonNullableInt(fragment, "grade");
            if (grade != null) pokemon.setGrade(grade);
            Integer evolves = extractJsonNullableInt(fragment, "evolvesToId");
            pokemon.setEvolvesToId(evolves);
            pokemon.setMegaCapable(extractJsonBoolean(fragment, "megaCapable"));
            pokemon.setZMoveCapable(extractJsonBoolean(fragment, "zMoveCapable"));

            // Types and moves.
            List<String> types = parseJsonStringArray(fragment, "types");
            pokemon.setTypes(types);

            // Load moves: canonical "move" single-element array preferred, fall back to legacy "moves."
            List<String> moveNames = parseJsonStringArray(fragment, "move");
            if (moveNames.isEmpty()) moveNames = parseJsonStringArray(fragment, "moves");
            for (String mn : moveNames) {
                Move mv = createMoveByName(mn);
                if (mv != null) pokemon.addMove(mv);
            }

            return pokemon;
        } catch (Exception e) {
            System.out.println("Error parsing Pokemon fragment: " + e.getMessage());
            return null;
        }
    }

    // Extracts an integer value from JSON that may be null.
    private static Integer extractJsonNullableInt(String json, String key) {
        String searchPattern = "\"" + key + "\": ";
        int start = json.indexOf(searchPattern);
        if (start == -1) return null;
        start += searchPattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) return null;
        String token = json.substring(start, end).trim();
        if ("null".equals(token)) return null;
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Parses an array of strings from a JSON string for the given key.
    private static List<String> parseJsonStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String searchPattern = "\"" + key + "\": [";
        int start = json.indexOf(searchPattern);
        if (start == -1) return result;
        start += searchPattern.length();
        int end = json.indexOf("]", start);
        if (end == -1) return result;
        String arrayContent = json.substring(start, end);
        if (arrayContent.trim().isEmpty()) return result;
        String[] items = arrayContent.split(",");
        for (String item : items) {
            String cleaned = item.trim();
            if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
                result.add(cleaned);
            }
        }
        return result;
    }

    // Creates a Move object based on its name string.
    public static Move createMoveByName(String moveName) {
        if (moveName == null) return null;
        String trimmed = moveName.trim();
        if (trimmed.isEmpty()) return null;
        String lower = trimmed.toLowerCase();
        if (lower.equals("tackle")) return new BasicAttack();
        if (lower.endsWith(" strike")) {
            String typeStr = trimmed.substring(0, trimmed.length() - " Strike".length()).trim();
            try {
                try { return new TypeMove(Type.fromString(typeStr)); } catch (Exception ignored) {}
                try { return new TypeMove(Type.fromString(typeStr.toUpperCase())); } catch (Exception ignored) {}
                try { return new TypeMove(Type.fromString(formatDisplayName(typeStr))); } catch (Exception ignored) {}
            } catch (Exception e) {}
            return new BasicAttack();
        }
        return new BasicAttack();
    }

    // Formats a raw string into a display-friendly name (capitalised words).
    private static String formatDisplayName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replace("_", " ");
        if (s.isEmpty()) return "";
        String[] parts = s.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() == 0) continue;
            String lower = parts[i].toLowerCase();
            parts[i] = Character.toUpperCase(lower.charAt(0)) + (lower.length() > 1 ? lower.substring(1) : "");
        }
        return String.join(" ", parts);
    }

    // Escapes special characters in a string for safe JSON output.
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

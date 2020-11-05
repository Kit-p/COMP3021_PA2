package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Deserializer {
    @NotNull
    private Path path;

    private Configuration configuration;

    private Integer[] storedScores;

    Place centralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();



    public Deserializer(@NotNull final Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException("Cannot find file to load!");
        }

        this.path = path;
    }

    /**
     * Returns the first non-empty and non-comment (starts with '#') line from the reader.
     *
     * @param br {@link BufferedReader} to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line
     * @throws InvalidGameException if unexpected end of file
     */
    @Nullable
    private String getFirstNonEmptyLine(@NotNull final BufferedReader br) throws IOException {
        // TODO
        String line = br.readLine();
        while (line != null) {
            if (!line.isBlank() && !line.startsWith("#")) {
                return line;
            }
            line = br.readLine();
        }
        return null;
    }

    public void parseGame() {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;

            int size;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get size here
                try {
                    size = Integer.parseInt(line.split(":")[1].strip());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationError("Invalid board size");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get numMovesProtection here
                try {
                    numMovesProtection = Integer.parseInt(line.split(":")[1].strip());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationError("Invalid number of protection moves");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of columns");
            }

            //TODO
            /**
             *  read central place here
             *  If success, assign to {@link Deserializer#centralPlace}
             *  Hint: You may use {@link Deserializer#parsePlace(String)}
             */
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                this.centralPlace = parsePlace(line.split(":")[1].strip());
            }

            int numPlayers;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                //TODO: get number of players here
                try {
                    numPlayers = Integer.parseInt(line.split(":")[1].strip());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationError("Invalid number of players");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }


            // TODO:
            /**
             * create an array of players {@link Player} with length of numPlayers, and name it by the read-in name
             * Also create an array representing scores {@link Deserializer#storedScores} of players with length of numPlayers
             */
            Player[] players = new Player[numPlayers];
            this.storedScores = new Integer[numPlayers];
            for (int i = 0; i < numPlayers; i++) {
                line = getFirstNonEmptyLine(reader);
                if (line == null) {
                    throw new InvalidGameException("Unexpected EOF when parsing player information");
                }
                String playerName = line.split(";")[0].split(":")[1].strip();
                players[i] = new ConsolePlayer(playerName);
                int playerScore;
                try {
                    playerScore = Integer.parseInt(line.split("; ")[1].split(":")[1].strip());
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationError("Invalid player score");
                }
                this.storedScores[i] = playerScore;
            }

            // TODO
            /**
             * try to initialize a configuration object  with the above read-in variables
             * if fail, throw InvalidConfigurationError exception
             * if success, assign to {@link Deserializer#configuration}
             */
            this.configuration = new Configuration(size, players, numMovesProtection);

            // TODO
            /**
             * Parse the string of move records into an array of {@link MoveRecord}
             * Assign to {@link Deserializer#moveRecords}
             * You should first implement the following methods:
             * - {@link Deserializer#parseMoveRecord(String)}}
             * - {@link Deserializer#parseMove(String)} ()}
             * - {@link Deserializer#parsePlace(String)} ()}
             */
            line = getFirstNonEmptyLine(reader);
            while (line != null && !line.startsWith("END")) {
                this.moveRecords.add(parseMoveRecord(line.strip()));
            }

        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }
    }

    public Configuration getLoadedConfiguration(){
        return configuration;
    }

    public Integer[] getStoredScores(){
        return storedScores;
    }

    public ArrayList<MoveRecord> getMoveRecords(){
        return moveRecords;
    }

    /**
     * Parse the string into a {@link MoveRecord}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveRecordString a string of a move record
     * @return a {@link MoveRecord}
     */
    private MoveRecord parseMoveRecord(String moveRecordString){
        // TODO
        Player player = new ConsolePlayer(moveRecordString.split("; ")[0].split(":")[1].strip());
        Move move = parseMove(moveRecordString.split("; ")[1]);
        return new MoveRecord(player, move);
    }

    /**
     * Parse a string of move to a {@link Move}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveString given string
     * @return {@link Move}
     */
    private Move parseMove(String moveString) {
        // TODO
        String[] places = moveString.split("->");
        if (places.length != 2) {
            throw new InvalidConfigurationError("Move should have a source and a destination");
        }
        Place source = parsePlace(places[0].strip());
        Place destination = parsePlace(places[1].strip());
        if (source == null || destination == null) {
            throw new InvalidConfigurationError("Place is empty");
        }
        return new Move(source, destination);
    }

    /**
     * Parse a string of move to a {@link Place}
     * Handle InvalidConfigurationError if the parse fails.
     * @param placeString given string
     * @return {@link Place}
     */
    private Place parsePlace(String placeString) {
        //TODO
        placeString = placeString.replace("(", "").replace(")", "");
        String[] coordinates = placeString.split(",");
        if (coordinates.length != 2) {
            return null;
        }
        int x, y;
        try {
            x = Integer.parseInt(coordinates[0].strip());
            y = Integer.parseInt(coordinates[1].strip());
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationError("Invalid coordinates");
        }
        return new Place(x, y);
    }


}

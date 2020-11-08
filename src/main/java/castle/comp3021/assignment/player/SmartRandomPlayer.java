package castle.comp3021.assignment.player;

import castle.comp3021.assignment.piece.Archer;
import castle.comp3021.assignment.piece.Knight;
import castle.comp3021.assignment.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A computer player that makes a move using smart strategy.
 */
public class SmartRandomPlayer extends Player {
    public SmartRandomPlayer(String name, Color color) {
        super(name, color);
    }

    public SmartRandomPlayer(String name) {
        this(name, Color.BLUE);
    }

    /**
     * Bonus:
     * You can implement a smarter way to play the random role
     * Hint:
     *  - E.g., weighted random selection is an option
     *  - You are encouraged to come up with other strategies as long as they involve random (i.e., not fixed rules)
     * @param game           the current game object
     * @param availableMoves available moves for this player to choose from.
     * @return next Move
     */
    @Override
    public @NotNull Move nextMove(Game game, Move[] availableMoves) {
        //TODO: bonus only
        final int N = game.getConfiguration().getSize() / 3;
        Move[] bestNMoves = filterBestNMoves(game, availableMoves, N);
        int index = new Random().nextInt(bestNMoves.length);
        return bestNMoves[index];
    }

    /**
     * Generate the best N moves
     * @param game           the current game object
     * @param availableMoves available moves for this player to choose from
     * @param n              number of moves to be generated
     * @return best N moves generated
     */
    private Move[] filterBestNMoves(Game game, Move[] availableMoves, int n) {
        if (availableMoves.length <= n) {
            return availableMoves;
        }
        HashMap<Move, Integer> moveRatingMap = new HashMap<>();
        for (Move move : availableMoves) {
            moveRatingMap.put(move, rateMove(game, move));
        }
        LinkedHashMap<Move, Integer> sortedMoveRatingMap =
                moveRatingMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue
                                , (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMoveRatingMap.keySet().stream().limit(n).toArray(Move[]::new);
    }

    /**
     * Rate each move
     * @param game the current game object
     * @param move the move to be rated
     * @return rating of the move
     */
    private int rateMove(Game game, Move move) {
        if (checkVictoryMove(game, move)) {
            return Integer.MAX_VALUE;
        }
        return rateMoveByCapture(game, move) + rateMoveByPotential(game, move) + getMoveScore(game, move);
    }

    /**
     * Check if the move leads to victory
     * @param game the current game object
     * @param move the move to be rated
     * @return leads to victory or not
     */
    private boolean checkVictoryMove(Game game, Move move) {
        Configuration configuration = game.getConfiguration();
        if (game.getNumMoves() < configuration.getNumMovesProtection()) {
            return false;
        }
        Place source = move.getSource();
        Place destination = move.getDestination();
        Place centralPlace = configuration.getCentralPlace();
        Piece sourcePiece = game.getPiece(source);
        if ((sourcePiece instanceof Knight)
                && source.equals(centralPlace) && !destination.equals(centralPlace)) {
            return true;
        }

        if (this.rateMoveByCapture(game, move) <= 0) {
            return false;
        }

        int count = 0;
        int size = configuration.getSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Piece piece = game.getPiece(i, j);
                if (piece != null && !(piece.getPlayer().equals(this))) {
                    count++;
                }
            }
        }
        return (count <= 1);
    }

    /**
     * Check what piece does the move capture, rate with specified weighing
     * @param game the current game object
     * @param move the move to be rated
     * @return calculated rating
     */
    private int rateMoveByCapture(Game game, Move move) {
        final int WEIGHING_FACTOR = game.getConfiguration().getSize() * 50;
        final int KNIGHT_SCORE = WEIGHING_FACTOR * 5;
        final int ARCHER_SCORE = WEIGHING_FACTOR * 2;
        Piece capturingPiece = game.getPiece(move.getDestination());
        if (capturingPiece instanceof Knight) {
            return KNIGHT_SCORE;
        } else if (capturingPiece instanceof Archer) {
            return ARCHER_SCORE;
        }
        return 0;
    }

    /**
     * Check the potential advantage brought by the move, rate with specified weighing
     * @param game the current game object
     * @param move the move to be rated
     * @return calculated rating
     */
    private int rateMoveByPotential(Game game, Move move) {
        Configuration configuration = game.getConfiguration();
        boolean protectionExpired = (game.getNumMoves() >= configuration.getNumMovesProtection());
        int size = configuration.getSize();
        final int WEIGHING_FACTOR = size * 20;
        final int KNIGHT_BONUS = (hasCapturedCentralPlace(game)) ? 0 : 5;
        final int KNIGHT_ASSIST_BONUS = (!protectionExpired) ? 0 : 1;
        final int ARCHER_ATTACK_BONUS = (!protectionExpired) ? 0 : 10;
        Place source = move.getSource();
        Place destination = move.getDestination();
        Place centralPlace = configuration.getCentralPlace();
        Piece piece = game.getPiece(source);
        int totalScore = 0;
        if (piece instanceof Knight) {
            int currentDistance = getManhattanDistance(source, centralPlace);
            int newDistance = getManhattanDistance(destination, centralPlace);
            int currentBonus = (currentDistance % 3 != 0) ? 0 : KNIGHT_BONUS;
            int newBonus = (newDistance % 3 != 0) ? 0 : KNIGHT_BONUS;
            int score = currentDistance * newBonus - newDistance * currentBonus;
            score *= (size - newDistance);
            totalScore += score;
            if (canAnyArcherFire(game, destination)) {
                totalScore += KNIGHT_ASSIST_BONUS;
            }
        } else if (piece instanceof Archer) {
            if (canAnyArcherFire(game, destination)) {
                totalScore += ARCHER_ATTACK_BONUS;
            }
            if (source.equals(centralPlace)) {
                return Integer.MAX_VALUE / WEIGHING_FACTOR;
            }
        }
        return WEIGHING_FACTOR * totalScore;
    }

    /**
     * Rate the move according to the Manhattan Distance
     * @param game the current game object
     * @param move the move to be rated
     * @return calculated rating
     */
    private int getMoveScore(Game game, Move move) {
        int size = game.getConfiguration().getSize();
        Place source = move.getSource();
        Place destination = move.getDestination();
        int manhattanDistance = getManhattanDistance(source, destination);
        return size - manhattanDistance;
    }

    /**
     * Calculate the Manhattan Distance between 2 places
     * @param source      the source place
     * @param destination the destination place
     * @return calculated Manhattan Distance
     */
    private int getManhattanDistance(Place source, Place destination) {
        int sourceX = source.x();
        int sourceY = source.y();
        int destinationX = destination.x();
        int destinationY = destination.y();
        return Math.abs(destinationX - sourceX) + Math.abs(destinationY - sourceY);
    }

    /**
     * Check if any archer can fire after a piece moves to the destination
     * @param game        the current game object
     * @param destination the destination place
     * @return can any archer fire or all cannot
     */
    private boolean canAnyArcherFire(Game game, Place destination) {
        Configuration configuration = game.getConfiguration();
        if (game.getNumMoves() < configuration.getNumMovesProtection()) {
            return false;
        }
        int size = configuration.getSize();
        int x = destination.x();
        int y = destination.y();
        ArrayList<Place> archers = new ArrayList<>();
        ArrayList<Place> enemies = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Place place = new Place(i, y);
            Piece piece = game.getPiece(place);
            if (piece instanceof Archer && piece.getPlayer().equals(this)) {
                archers.add(place);
            } else if (piece != null && !(piece.getPlayer().equals(this))) {
                enemies.add(place);
            }
        }
        for (Place archer : archers) {
            for (Place enemy : enemies) {
                int startX = Math.min(archer.x(), enemy.x());
                int endX = Math.max(archer.x(), enemy.x());
                if (startX >= x || endX <= x) {
                    continue;
                }
                int inBetweenPieceCount = (game.getPiece(destination) == null) ? 1 : 0;
                for (int i = startX + 1; i < endX; i++) {
                    if (game.getPiece(i, y) != null) {
                        inBetweenPieceCount++;
                    }
                }
                if (inBetweenPieceCount == 1) {
                    return true;
                }
            }
        }
        archers = new ArrayList<>();
        enemies = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            Place place = new Place(x, j);
            Piece piece = game.getPiece(place);
            if (piece instanceof Archer && piece.getPlayer().equals(this)) {
                archers.add(place);
            } else if (piece != null && !(piece.getPlayer().equals(this))) {
                enemies.add(place);
            }
        }
        for (Place archer : archers) {
            for (Place enemy : enemies) {
                int startY = Math.min(archer.y(), enemy.y());
                int endY = Math.max(archer.y(), enemy.y());
                if (startY >= y || endY <= y) {
                    continue;
                }
                int inBetweenPieceCount = (game.getPiece(destination) == null) ? 1 : 0;
                for (int j = startY + 1; j < endY; j++) {
                    if (game.getPiece(x, j) != null) {
                        inBetweenPieceCount++;
                    }
                }
                if (inBetweenPieceCount == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCapturedCentralPlace(Game game) {
        Piece piece = game.getPiece(game.getConfiguration().getCentralPlace());
        return (piece instanceof Knight && piece.getPlayer().equals(this));
    }
}

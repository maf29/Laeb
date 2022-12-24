/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.players;

import edu.upc.epsevg.prop.othello.CellType;
import static edu.upc.epsevg.prop.othello.CellType.to_01;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author UX431F
 */
public class PlayerMiniMax implements IPlayer, IAuto{
    String name = "La'eb";
    private int MAX = Integer.MAX_VALUE;
    private int MIN = Integer.MIN_VALUE;
    private static final int MAX_DEPTH = 8;
    
     private boolean timeout = false;
    private int TAULERS_EXAMINATS_TOTAL; //prof
    private long TAULERS_EXAMINATS;
    private java.awt.Point to = new Point(2,4);
    private static final int[][] VALUES_TABLE = {
    { 120, -20,  20,   5,   5,  20, -20, 120 },
    { -20, -40,  -5,  -5,  -5,  -5, -40, -20 },
    {  20,  -5,  15,   3,   3,  15,  -5,  20 },
    {   5,  -5,   3,   3,   3,   3,  -5,   5 },
    {   5,  -5,   3,   3,   3,   3,  -5,   5 },
    {  20,  -5,  15,   3,   3,  15,  -5,  20 },
    { -20, -40,  -5,  -5,  -5,  -5, -40, -20 },
    { 120, -20,  20,   5,   5,  20, -20, 120 }
    };
    
    private static final int WEIGHT_NUM_MOVES = 5;
    private static final int WEIGHT_MOBILITY = 1;
    private static final int WEIGHT_STABLE_DISCS = 10;
    private static final int WEIGHT_CORNER_DISCS = 20;
    private static final int WEIGHT_DISC_PARITY = 10;
    private static final int WEIGHT_DISCS_CONTROLLED = 20;
    // The board is represented as a 2D array of integers.
    // 0: empty cell
    // 1: player 1's disc
    // -1: player 2's disc
    
    private int[][] board;
    private int turn;  // 1 for player 1, -1 for player 2
    HashMap<GameStatus, Integer> transpositionTable = new HashMap<>();
    
    @Override
    public Move move(GameStatus gs) {
        TAULERS_EXAMINATS=0;
        TAULERS_EXAMINATS_TOTAL=1;
        ArrayList<Point> moves = gs.getMoves();

        if (moves.isEmpty()) {
            return null;
        }

        Point bestMove = moves.get(0);
        int bestScore = MIN;

        HashMap<GameStatus, Integer> transpTable = new HashMap<>();

        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus(gs);
            nextBoard.movePiece(move);

            CellType opp = CellType.opposite(nextBoard.getCurrentPlayer());
            System.out.println("piece (oposite es el siguiente a jugar) = -> "+opp);

            int score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false, transpTable);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            TAULERS_EXAMINATS_TOTAL++;
        }
        Move move = new Move(bestMove, TAULERS_EXAMINATS, TAULERS_EXAMINATS_TOTAL, SearchType.MINIMAX);

        return move;
    }

    @Override
    public void timeout() {
        timeout = true;
        System.out.println("Bah! You are so slow...");
    }

    @Override
    public String getName() {
        return name;
    }
    
    private int minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizingPlayer, HashMap<GameStatus, Integer> transpTable) {
        // check if the current position has been evaluated before
        Integer score = transpTable.get(gs);
        if (score != null) {
            // if the position has been evaluated before, return the stored score
            return score;
        }

        // base case: check if the game is over or the maximum depth has been reached
        if (gs.checkGameOver() || depth == 0) {
            // return the score of the current position
            return heuristica(gs);
        }

        // list of possible moves
        ArrayList<Point> moves = gs.getMoves();
        if (maximizingPlayer) {
            int bestScore = MIN;
            for (Point move : moves) {
                // simulate the move on a copy of the current board
                GameStatus nextBoard = new GameStatus(gs);
                nextBoard.movePiece(move);

                // recursive call to minimax with the updated board and reduced depth
                int valor = minimax(nextBoard, depth - 1, alpha, beta, false, transpTable);

                // update the best score for the maximizing player
                bestScore = Math.max(bestScore, valor);

                // update alpha
                alpha = Math.max(alpha, bestScore);

                // prune the tree if alpha is greater than or equal to beta
                if (alpha >= beta) {
                    break;
                }
            }
            // store the score in the transposition table
            transpositionTable.put(gs, bestScore);
            return bestScore;
        } else {
            int worstScore = MAX;
            for (Point move : moves) {
                // simulate the move on a copy of the current board
                GameStatus nextBoard = new GameStatus(gs);
                nextBoard.movePiece(move);

                // recursive call to minimax with the updated board and reduced depth
                int valor = minimax(nextBoard, depth - 1, alpha, beta, true, transpTable);

                // update the worst score for the minimizing player
                worstScore = Math.min(worstScore, valor);

                // update beta
                beta = Math.min(beta, worstScore);

                // prune the tree if alpha is greater than or equal to beta
                if (alpha >= beta) {
                    break;
                }
            }
            // store the score in the transposition table
            transpositionTable.put(gs, worstScore);
            return worstScore;
        }
    }

    
    private boolean isStable(Point p, GameStatus gs) {
        // Check if the disc at the given point is stable
        CellType disc = gs.getPos(p.x, p.y);
        if (disc == CellType.EMPTY) {
            return false;
        }

        int size = gs.getSize();

        // Check the rows and columns adjacent to the disc
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int x = p.x + i;
                int y = p.y + j;
                if (x >= 0 && x < size && y >= 0 && y < size && gs.getPos(x, p.y) == disc && gs.getPos(p.x, y) == disc) {
                    return true;
                }
            }
        }

        // Check the diagonals adjacent to the disc
        if (p.x - 1 >= 0 && p.y - 1 >= 0 && p.x + 1 < size && p.y + 1 < size && gs.getPos(p.x - 1, p.y - 1) == disc && gs.getPos(p.x + 1, p.y + 1) == disc) {
            return true;
        }
        if (p.x - 1 >= 0 && p.y + 1 < size && p.x + 1 < size && p.y - 1 >= 0 && gs.getPos(p.x - 1, p.y + 1) == disc && gs.getPos(p.x + 1, p.y - 1) == disc) {
            return true;
        }

        return false;
    }

   /**
    Calcula una puntuación heurística para el jugador actual 
    * en el estado de juego dado.
    La puntuación se basa en el número de discos, la movilidad, la estabilidad 
    * y posición estratégica de los discos del jugador,
    * así como los valores correspondientes de los discos del oponente.
    @param gs Estado actual de la partida.
    @return Puntuación heurística del jugador actual.
   */
    public int heuristica(GameStatus gs) {
        TAULERS_EXAMINATS++;

        // Initialize the score to the number of discs for the player
        int score = gs.getScore(gs.getCurrentPlayer());

        // Add a penalty for each possible move the opponent has
        int numMoves = gs.getMoves().size();
        score -= numMoves * WEIGHT_NUM_MOVES;

        // Add a bonus for each disc that has mobility
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == gs.getCurrentPlayer()) {
                    score += gs.getMoves().size() * WEIGHT_MOBILITY;
                }
            }
        }

        // Add a bonus for each stable disc
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (isStable(new Point(i, j), gs)) {
                    score += WEIGHT_STABLE_DISCS;
                }
            }
        }

        // Add a bonus for each disc in a corner
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == gs.getCurrentPlayer() &&
                    ((i == 0 && j == 0) || (i == 0 && j == gs.getSize() - 1) ||
                     (i == gs.getSize() - 1 && j == 0) || (i == gs.getSize() - 1 && j == gs.getSize() - 1))) {
                    score += WEIGHT_CORNER_DISCS;
                }
            }
        }

        // Add a penalty for each disc in a corner occupied by the opponent
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == CellType.opposite(gs.getCurrentPlayer()) &&
                    ((i == 0 && j == 0) || (i == 0 && j == gs.getSize() - 1) ||
                     (i == gs.getSize() - 1 && j == 0) || (i == gs.getSize() - 1 && j == gs.getSize() - 1))) {
                    score -= WEIGHT_CORNER_DISCS;
                }
            }
        }

        // Add the values from the values table for each disc
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
            CellType piece = gs.getPos(j, i);
            if (piece == gs.getCurrentPlayer()) {
                        score += VALUES_TABLE[i][j];
                    } else if (piece == CellType.opposite(gs.getCurrentPlayer())) {
                        score -= VALUES_TABLE[i][j];
                    }
                }
            }// Add a bonus for each disc controlled by the player
            int numDiscs = gs.getScore(gs.getCurrentPlayer());
            score += numDiscs * WEIGHT_DISCS_CONTROLLED;

            // Add a penalty for each disc controlled by the opponent
            int numOpponentDiscs = gs.getScore(CellType.opposite(gs.getCurrentPlayer()));
            score -= numOpponentDiscs * WEIGHT_DISCS_CONTROLLED;

            // Add a bonus for each disc with mobility
            for (int i = 0; i < gs.getSize(); i++) {
                for (int j = 0; j < gs.getSize(); j++) {
                    if (gs.getPos(j, i) == gs.getCurrentPlayer()) {
                        int mobility = getMobility(gs, new Point(i, j), gs.getCurrentPlayer());
                        score += mobility * WEIGHT_MOBILITY;
                    }
                }
            }

            // Add a penalty for each disc with mobility for the opponent
            for (int i = 0; i < gs.getSize(); i++) {
                for (int j = 0; j < gs.getSize(); j++) {
                    if (gs.getPos(j, i) == CellType.opposite(gs.getCurrentPlayer())) {
                        int mobility = getMobility(gs, new Point(i, j), CellType.opposite(gs.getCurrentPlayer()));
                        score -= mobility * WEIGHT_MOBILITY;
                    }
                }
            }

            // Add a bonus for a parity advantage (more discs on the board)
            int discParity = gs.getScore(gs.getCurrentPlayer()) - gs.getScore(CellType.opposite(gs.getCurrentPlayer()));
            if (discParity > 0) {
                score += discParity * WEIGHT_DISC_PARITY;
            } else if (discParity < 0) {
                score -= -discParity * WEIGHT_DISC_PARITY;
            }

            // Add a bonus for moves that increase the player's mobility
            for (Point move : gs.getMoves()) {
                int mobilityBefore = getMobility(gs, move, gs.getCurrentPlayer());
                int mobilityAfter = getMobility(gs, move, CellType.opposite(gs.getCurrentPlayer()));
                score += (mobilityAfter - mobilityBefore) * WEIGHT_MOBILITY;
            }

        return score;
    }
    public int getMobility(GameStatus gs, Point p, CellType player) {
        int mobility = 0;

        int size = gs.getSize();

        // Iterate through the eight directions around the given cell
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                // Check if the current direction has a valid move
                int x = p.x + dx;
                int y = p.y + dy;
                if (x >= 0 && x < size && y >= 0 && y < size && gs.canMove(new Point(x, y), player)) {
                    mobility++;
                }
            }
        }

        return mobility;
    }
}
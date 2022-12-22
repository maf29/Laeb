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
    //private HashMap<GameStatus, Integer> transpositionTable;
    private java.awt.Point to = new Point(2,4);
    // Declare the hashmap or hashtable
    Map<String, PositionScore> transpositionTable;

  
    private int[][] board;
    private int turn;  // 1 for player 1, -1 for player 2

   
    @Override
    public Move move(GameStatus gs) {
        TAULERS_EXAMINATS=0;
        TAULERS_EXAMINATS_TOTAL=1;
        ArrayList<Point> moves = gs.getMoves();
        if (moves.isEmpty()) {
            return null;
        }

        Point bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;

        transpositionTable = new HashMap<>();  // for a hashmap
        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus(gs);
            nextBoard.movePiece(move);
            CellType opp = CellType.opposite(nextBoard.getCurrentPlayer());
            System.out.println("piece (oposite es el siguiente a jugar) = -> "+opp);
            int score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false);
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
    
    
    
    class PositionScore {
        //The PositionScore class is a simple class that stores a GameStatus object and an integer representing the best score for that position. Here is an example of how you can define the PositionScore class:
        GameStatus gs;
        int bestScore;

        public PositionScore(GameStatus gs, int bestScore) {
            this.gs = gs;
            this.bestScore = bestScore;
        }
    }
    
    private int minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizingPlayer) {
        // Check if the position is in the transposition table
        //HashMap<GameStatus, Integer> boardHash = hashBoard(gs);
        //transpositionTable = new HashMap<>();
        
         // Check if the position is in the hashmap
        String hash = gs.toString();
        if (transpositionTable.containsKey(hash)) {
            return transpositionTable.get(hash).bestScore;
        }

        // Check if the search depth has been reached or the game is over
        if (depth == 0 || gs.isGameOver()) {
            return evaluate(gs);
        }

        int bestScore;
        if (maximizingPlayer) {
            bestScore = MIN;
            for (Point move : gs.getMoves()) {
                if (!gs.canMove(move, gs.getCurrentPlayer())) {
                    continue;
                }
            
                GameStatus nextBoard = new GameStatus​(gs);
                nextBoard.movePiece​(move);
                
                int score = minimax(nextBoard, depth-1, alpha, beta, false);
                bestScore = Math.max(score, bestScore);
                
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {    //PODA alfa-beta
                  break;
                }
            }
        } else{ //
            bestScore = MAX;
            for (Point move : gs.getMoves()) {
                if (!gs.canMove(move, gs.getCurrentPlayer())) {
                    continue;
                }
            
                GameStatus nextBoard = new GameStatus​(gs);
                nextBoard.movePiece​(move);
                
                int score = minimax(nextBoard, depth - 1, alpha, beta, true);
                bestScore = Math.min(score, bestScore);
                
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {    //PODA alfa-beta
                  break;
                }
            }
        }

        // Add the position and score to the transposition table
        transpositionTable.put(hash, new PositionScore(gs, bestScore));
    
        return bestScore;
    }
    
    
    private int evaluate(GameStatus gs) {
        // Get the scores for each player
        int player1Score = gs.getScore(CellType.PLAYER1);
        int player2Score = gs.getScore(CellType.PLAYER2);

        // Get the list of all possible moves
        ArrayList<Point> moves = gs.getMoves();

        // Filter the list of moves to get the list of moves for each player
        List<Point> player1Moves = new ArrayList<>();
        List<Point> player2Moves = new ArrayList<>();
        for (Point move : moves) {
            if (gs.getCurrentPlayer() == CellType.PLAYER1) {
                player1Moves.add(move);
            } else if (gs.getCurrentPlayer() == CellType.PLAYER2) {
                player2Moves.add(move);
            }
        }

        // Calculate the mobility for each player
        int player1Mobility = player1Moves.size();
        int player2Mobility = player2Moves.size();

        // Calculate the stability for each player
        int player1Stability = 0;
        int player2Stability = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gs.getPos(i, j) == CellType.PLAYER1) {
                    player1Stability += calculateStability(gs, i, j, CellType.PLAYER1);
                } else if (gs.getPos(i, j) == CellType.PLAYER2) {
                    player2Stability += calculateStability(gs, i, j, CellType.PLAYER2);
                }
            }
        }

        // Calculate the potential for future flips for each player
        int player1Potential = 0;
        int player2Potential = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gs.getPos(i, j) == CellType.EMPTY) {
                    // Check the surrounding spaces
                    if (gs.canMove(new Point(i - 1, j), CellType.PLAYER1) && gs.getPos(i - 1, j) == CellType.PLAYER1) {
                        player1Potential++;
                    }
                    if (gs.canMove(new Point(i + 1, j), CellType.PLAYER1) && gs.getPos(i + 1, j) == CellType.PLAYER1) {
                        player1Potential++;
                    }
                    if (gs.canMove(new Point(i, j - 1), CellType.PLAYER1) && gs.getPos(i, j - 1) == CellType.PLAYER1) {
                        player1Potential++;
                    }
                    if (gs.canMove(new Point(i, j + 1), CellType.PLAYER1) && gs.getPos(i, j + 1) == CellType.PLAYER1) {
                        player1Potential++;
                    }
                    if (gs.canMove(new Point(i - 1, j), CellType.PLAYER2) && gs.getPos(i - 1, j) == CellType.PLAYER2) {
                        player2Potential++;
                    }
                    if (gs.canMove(new Point(i + 1, j), CellType.PLAYER2) && gs.getPos(i + 1, j) == CellType.PLAYER2) {
                        player2Potential++;
                    }
                    if (gs.canMove(new Point(i, j - 1), CellType.PLAYER2) && gs.getPos(i, j - 1) == CellType.PLAYER2) {
                        player2Potential++;
                    }
                    if (gs.canMove(new Point(i, j + 1), CellType.PLAYER2) && gs.getPos(i, j + 1) == CellType.PLAYER2) {
                        player2Potential++;
                    }
                }
            }
        }

        // Use a weighted evaluation function to combine the different factors
        int score = (10 * (player1Score - player2Score)) +
                    (5 * (player1Mobility - player2Mobility)) +
                    (3 * (player1Stability - player2Stability)) +
                    (1 * (player1Potential - player2Potential));

        return score;
    }
    
    private int calculateStability(GameStatus gs, int row, int col, CellType player) {
        // Check if the piece is surrounded on all sides by the same player
        if ((row > 0 && gs.getPos(row - 1, col) == player) &&
            (row < 7 && gs.getPos(row + 1, col) == player) &&
            (col > 0 && gs.getPos(row, col - 1) == player) &&
            (col < 7 && gs.getPos(row, col + 1) == player)) {
            return 1;
        }
        return 0;
    }

}

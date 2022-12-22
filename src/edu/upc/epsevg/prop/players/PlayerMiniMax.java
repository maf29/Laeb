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
            return score(gs);
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

    
    
    
    
    private int score(GameStatus gs) {
        int score = 0;

        // count the number of pieces on the board
        int numPieces = 0;
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(i, j) != CellType.EMPTY) {
                    numPieces++;
                }
            }
        }

        // assign a higher weight to the number of consecutive pieces in a row
        int numConsecutive = 0;
        for (int i = 0; i < gs.getSize(); i++) {
            int consecutive = 0;
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(i, j) == gs.getCurrentPlayer()) {
                    consecutive++;
                } else {
                    numConsecutive = Math.max(numConsecutive, consecutive);
                    consecutive = 0;
                }
            }
            numConsecutive = Math.max(numConsecutive, consecutive);
        }

        // add the score for the number of pieces and the number of consecutive pieces
        score += numPieces * 10;
        score += numConsecutive * 100;
        
        return score;
    }
    /*public int heuristica (GameStatus t) {
        TAULERS_EXAMINATS++;
        int [][] values = {
            { 120, -20,  20,   5,   5,  20, -20, 120 },
            { -20, -40,  -5,  -5,  -5,  -5, -40, -20 },
            {  20,  -5,  15,   3,   3,  15,  -5,  20 },
            {   5,  -5,   3,   3,   3,   3,  -5,   5 },
            {   5,  -5,   3,   3,   3,   3,  -5,   5 },
            {  20,  -5,  15,   3,   3,  15,  -5,  20 },
            { -20, -40,  -5,  -5,  -5,  -5, -40, -20 },
            { 120, -20,  20,   5,   5,  20, -20, 120 }
        };
        
        int score = 0;
        for (int i = 0; i < t.getSize(); i++) {
            for (int j = 0; j < t.getSize(); j++) {
                
                CellType piece = t.getPos​(j,i);
                
                if(piece == t.getCurrentPlayer()){
                    score += values[i][j];
                } else if(piece == CellType.opposite​(t.getCurrentPlayer())){    
                    //conseguir jugador oponente
                    score -= values[i][j];
                }
            }
        }  
        return score;
    }*/
}

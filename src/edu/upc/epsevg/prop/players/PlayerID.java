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
import java.util.Map;
/**
 *
 * @author UX431F
 */
public class PlayerID implements IPlayer, IAuto{
    String name = "La'eb";
    private int MAX = Integer.MAX_VALUE;
    private int MIN = Integer.MIN_VALUE;
    private static final int MAX_DEPTH = 8;
    
     private boolean timeout = false;
    private int TAULERS_EXAMINATS_TOTAL; //prof
    private long TAULERS_EXAMINATS; //nodes explorats
    //private HashMap<GameStatus, Integer> transpositionTable; //string: gamestatus; integer: heuristic
    private java.awt.Point to = new Point(2,4);
    
    // The board is represented as a 2D array of integers.
    // 0: empty cell
    // 1: player 1's disc
    // -1: player 2's disc
    
    private int[][] board;
    private int turn;  // 1 for player 1, -1 for player 2
    
    /*Esta función devuelve una HashTable que mapea cada posible GameStatus
    después de hacer un movimiento a una 
    puntuación entera correspondiente obtenida por el algoritmo minimax. La 
    función itera a través de todos los movimientos posibles, crea un nuevo 
    objeto GameStatus para cada movimiento, y calcula la puntuación utilizando 
    la función minimax. La puntuación se añade entonces a la HashTable con el 
    objeto GameStatus correspondiente como clave.*/
    private HashMap<GameStatus, Integer> transpositionTable;
    public HashMap<GameStatus, Integer> hashBoard(GameStatus gs) {
    
        HashMap<GameStatus, Integer> heuristics = new HashMap<>();
        ArrayList<Point> moves = gs.getMoves();

        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus​(gs);
            nextBoard.movePiece​(move);

            int score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false);

            heuristics.put(nextBoard, score);
        }
        return heuristics;
    }

    @Override
    public Move move(GameStatus gs) {
        
        TAULERS_EXAMINATS=0;
        TAULERS_EXAMINATS_TOTAL=1;
        ArrayList<Point> moves = gs.getMoves();
        transpositionTable = new HashMap<>();
              
        if (moves.isEmpty()) {
            return null;
        }
        
        Point bestMove = moves.get(0);
        int bestScore = MIN;
        
        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus​(gs);
            
            nextBoard.movePiece​(move);  //movimiento de una posicion posible
            
            CellType opp = CellType.opposite​(nextBoard.getCurrentPlayer());
                       
            //HASH TABLE VA AQUI
            HashMap<GameStatus, Integer> boardHash = hashBoard(nextBoard);
            int score;
            if (transpositionTable.containsKey(boardHash)) {
                // Si ya hemos evaluado este tablero, devolvemos la heurística almacenada en la tabla
                score = transpositionTable.get(boardHash);
            } else {
                // Si no hemos evaluado este tablero, lo evaluamos con minimax y almacenamos la heurística en la tabla
                score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false);
                transpositionTable.putAll(boardHash); 
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }  
            TAULERS_EXAMINATS_TOTAL++;  
        }
        Move move = new Move(bestMove, TAULERS_EXAMINATS, TAULERS_EXAMINATS_TOTAL, SearchType.MINIMAX);
        
        return move;//new Move(bestMove.x, bestMove.y) 
    }

    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        timeout = true;
        System.out.println("Bah! You are so slow...");
    }

    @Override
    public String getName() {
        return name;
    }
    
    private int minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizingPlayer) {

        //TIMEOUT
         if (timeout) {    //--> en vez de poner if de la funcion, crear una variable global
            return 0;
        }
         
        //CASOS BASE
        if(gs.checkGameOver()){                      //  someone wins
            if(gs.getCurrentPlayer() == gs.GetWinner())  //  wins la'eb
                return 1000000;
            else{                                    //  wins the other one
                return -1000000;
            }
        }
        else if(gs.isGameOver() || depth==0){    //depth 0 or not possible moves
            return heuristica(gs);
        }
        if(!gs.currentPlayerCanMove()){  //contrincant no pot moure
            System.out.println("!gs.currentPlayerCanMove()");
            if(maximizingPlayer){
                gs.skipTurn();
                //return minimax(gs, depth-1, alpha, beta, false);
            }
            else{ 
                return minimax(gs, depth - 1, alpha, beta, true);
            }
            gs.skipTurn();
        }
        
        if (maximizingPlayer){  //minimizar
            int bestScore = MIN;
            for (Point move : gs.getMoves()) {
                GameStatus nextBoard = new GameStatus​(gs);
                nextBoard.movePiece​(move);
                
                int score = minimax(nextBoard, depth-1, alpha, beta, false);
                bestScore = Math.max(score, bestScore);
                
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {    //PODA alfa-beta
                  break;
                }
            }
            return bestScore;
        } else{ //maximizar
            int bestScore = MAX;
            for (Point move : gs.getMoves()) {
                GameStatus nextBoard = new GameStatus​(gs);
                nextBoard.movePiece​(move);
                
                int score = minimax(nextBoard, depth - 1, alpha, beta, true);
                bestScore = Math.min(score, bestScore);
                
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {    //PODA alfa-beta
                  break;
                }
            }
            return bestScore;
        }
        //return 0;
    }
     
    public int heuristica (GameStatus t) {
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
    }


    
}

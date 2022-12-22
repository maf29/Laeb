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



/**
 *
 * @author UX431F
 */
public class PlayerMiniMax implements IPlayer, IAuto{
    String name = "La'eb";
    private int MAX = Integer.MAX_VALUE;
    private int MIN = Integer.MIN_VALUE;
    private static final int MAX_DEPTH = 8;
    
    private int TAULERS_EXAMINATS_TOTAL=0;
    private long TAULERS_EXAMINATS=0;
    private java.awt.Point to = new Point(2,4);
    
    // The board is represented as a 2D array of integers.
    // 0: empty cell
    // 1: player 1's disc
    // -1: player 2's disc
    
    private int[][] board;
    private int turn;  // 1 for player 1, -1 for player 2
    
    @Override
    public Move move(GameStatus gs) {
        
        //System.out.println("4x3 ==> "+gs.getPos​(4,3)); 
        //desde la fila cuentas 4 columnas a la derecha, desde la columna cuentas 3 filas abajo 
        
        TAULERS_EXAMINATS=0;
        ArrayList<Point> moves = gs.getMoves();
        System.out.println("moves:" + moves);
        
        if (moves.isEmpty()) {
            return null;
        }
        
        Point bestMove = moves.get(0);
        int bestScore = MIN;
        
        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus​(gs);
            
            System.out.println("CURRENT PLAYER (before): "+ nextBoard.getCurrentPlayer());  //PRUEBA
            
            nextBoard.movePiece​(move);  //movimiento de una posicion posible
            
            System.out.println("nextBoard:" + nextBoard);   //PRUEBA
            System.out.println("CURRENT PLAYER: "+ nextBoard.getCurrentPlayer());   //PRUEBA
            
            /*
            realmente para sacar el oppuesto hace falta simplemente poner
            getCurrentPlayer porque como se hizo una jugada ahora le current es el oponente
            */
            CellType opp = CellType.opposite​(nextBoard.getCurrentPlayer());
            System.out.println("piece (oposite es el siguiente a jugar) = -> "+opp);    //PRUEBA
            
            //HASH TABLE VA AQUI
            int score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            
        }
        System.out.println("TAULERS EXAMINATS ULTIMA FITXA: " + TAULERS_EXAMINATS);
        System.out.println("TAULERS EXAMINATS TOTALS: " + TAULERS_EXAMINATS_TOTAL);
        //no se si estan bien TAULERS_EXAMINATS y TAULERS_EXAMINATS_TOTAL
        Move move = new Move(bestMove, TAULERS_EXAMINATS, TAULERS_EXAMINATS_TOTAL, SearchType.MINIMAX);
        
        
        return move;//new Move(bestMove.x, bestMove.y) 
    }

    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        System.out.println("Bah! You are so slow...");
    }

    @Override
    public String getName() {
        return name;
    }
    
    private int minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizingPlayer) {
        /*
        if (timeout()) {    //--> en vez de poner if de la funcion, crear una variable global
            return 0;
          }
        */
        TAULERS_EXAMINATS_TOTAL += 1;
        TAULERS_EXAMINATS += 1;
        /*
        mirar si has ganado y hay que parar */
        if(gs.checkGameOver()){
            System.out.println("gs.checkGameOver()");
            return -1;  //no se si es -1?
        }
        
        //HAY ALGO MAL EN EL SKIP TURN
        //skip turn hacer un if y else solo para hacer que se pase el turno
        if(!gs.currentPlayerCanMove()){  //no estoy segura de este?
            System.out.println("!gs.currentPlayerCanMove()");
            /*if(maximizingPlayer){
                gs.skipTurn();
                //return minimax(gs, depth-1, alpha, beta, false);
            }
            else{
                
                return minimax(gs, depth - 1, alpha, beta, true);
            }*/
            gs.skipTurn();
        }
        
        
        if (depth == 0) {   //|| gs.getMoves().isEmpty()
            return heuristica(gs);
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
        } else{ //
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

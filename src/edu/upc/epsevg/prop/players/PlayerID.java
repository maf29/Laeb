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
    private final int MAX = Integer.MAX_VALUE;
    private final int MIN = Integer.MIN_VALUE;
    private static final int MAX_DEPTH = 8;
   
    private boolean timeout = false;
    private int TAULERS_EXAMINATS_TOTAL; //depth
    private long TAULERS_EXAMINATS; //nodes explorats
    
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
    private static final int WEIGHT_DISC_PARITY = 20;
    private static final int WEIGHT_DISCS_CONTROLLED = 10;
  
    // We consider:
    //      0: empty cell
    //      1: player 1 (Black)
    //      -1: player 2 (White)
   
   private static long[][][] hashTable;  // Hash table for storing game statuses
    private HashMap<Long, Integer> transpositionTable;  // Transposition table for storing heuristic values
   
    /**
    Calcula la mejor jugada para el jugador actual en el 
    estado de la partida usando el algoritmo minimax.
    @param gs Estado actual de la partida.
    @return La mejor jugada para el jugador actual.
    */
    @Override
    public Move move(GameStatus gs) {
        
        TAULERS_EXAMINATS=0;
        TAULERS_EXAMINATS_TOTAL=1;
        ArrayList<Point> moves = gs.getMoves();
      
        // Compruebe si el jugador actual tiene alg??n movimiento posible
        if (moves.isEmpty()) {
            // Si el jugador actual no tiene movimientos posibles, llame al m??todo "skipTurn"
            gs.skipTurn();
            return null;
        }
        
        Point bestMove = moves.get(0);
        int bestScore = MIN;
        
        for (Point move : moves) {
            GameStatus nextBoard = new GameStatus???(gs);
            
            // Realice el movimiento actual en el nuevo Board
            nextBoard.movePiece???(move);  
            
            int score = minimax(nextBoard, MAX_DEPTH, MIN, MAX, false);
       
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }  
            TAULERS_EXAMINATS_TOTAL++;     
        }
        Move move = new Move(bestMove, TAULERS_EXAMINATS, TAULERS_EXAMINATS_TOTAL, SearchType.MINIMAX_IDS);
        
        return move;
    }

    /**
    Establece el tiempo de espera en true e imprime un mensaje.
    */
    @Override
    public void timeout() {
        timeout = true;
        System.out.println("Bah! You are so slow...");
    }

    /**
    Devuelve el nombre de este jugador.
    @return El nombre de este jugador.
    */
    @Override
    public String getName() {
        return name;
    }
    
    /**
    Calcula la mejor jugada para el jugador actual en el estado de juego
    * dado usando el algoritmo minimax con poda alfa-beta 
    * y tabla de transposici??n de Zobrish.
    @param gs Estado actual de la partida.
    @param depth La profundidad de b??squeda restante.
    @param alpha El valor alfa para la poda alfa-beta.
    @param beta El valor beta para la poda alfa-beta.
    @param maximizingPlayer Bandera que indica si el jugador actual es el 
    * jugador maximizador (true) o el jugador minimizador (false).
    @return Valor heur??stico de la mejor jugada para el jugador actual.
    */
    private int minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizingPlayer) {
        //TIMEOUT----------
        if (timeout) {    
            return 0;
        }
        
        //Calcula el valor hash del estado actual del juego utilizando el m??todo de hashing Zobrist 
        transpositionTable = new HashMap<>();
        hashTable = new long[8][8][3];
        
        long hash = 0;
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                int value = 0;
                if(gs.getPos(i,j)== CellType.PLAYER1) value = 1;
                else if (gs.getPos(i,j)== CellType.PLAYER2) value = -1;
                hash ^= hashTable[i][j][value + 1];
            }
        }
       
        // CASOS BASE----------
        // Fin del juego: Cuando el juego ha terminado (es decir, no hay m??s
        // movimientos v??lidos disponibles), se puede devolver la puntuaci??n
        // heur??stica para el estado actual del juego.
        if (gs.isGameOver()) {
            return heuristica(gs);
        }
        // L??mites alpha-beta: Si el valor alpha actual es mayor o igual que el
        // valor beta, se puede devolver la puntuaci??n heur??stica para el
        // estado actual del juego, ya que cualquier b??squeda posterior no ser?? ??til.
        if (alpha >= beta) {
            return heuristica(gs);
        }
        // Cuando un jugador no puede realizar una jugada, hacemos un skipTurn() 
        // para que el siguiente jugador pueda realizar una jugada
        if (gs.getMoves().isEmpty()) {
            gs.skipTurn();
            return minimax(gs, depth, alpha, beta, maximizingPlayer);
        }

        // Si la profundidad es 1, la funci??n minimax evaluar?? las puntuaciones
        // heur??sticas para los estados del juego que se pueden alcanzar 
        // realizando un movimiento desde el estado actual del juego
        if (depth == 1) {
            return heuristica(gs);
        }

        // Si la profundidad de la funci??n minimax es 0, significa que la b??squeda
        // ha alcanzado la profundidad m??xima que se ha establecido para el 
        // ??rbol de b??squeda. En este caso, se puede devolver la puntuaci??n 
        // heur??stica del estado actual del juego como resultado de la funci??n minimax.
        if (depth == 0) {
            return heuristica(gs);
        }
        
        // Check if the current game status is in the transposition table
        if (transpositionTable.containsKey(hash)) {
            // If it is, return the stored heuristic value
            return transpositionTable.get(hash);
        }
        
        if (maximizingPlayer){  //minimize
            int bestScore = MIN;
            for (Point move : gs.getMoves()) {
                GameStatus nextBoard = new GameStatus???(gs);
                nextBoard.movePiece???(move);
                
                int score = minimax(nextBoard, depth-1, alpha, beta, false);
                bestScore = Math.max(score, bestScore);
                
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {    //alpha-beta pruing
                  break;
                }
            }
            transpositionTable.put(hash, bestScore);
            return bestScore;
            
        } else{ //maximize
            int bestScore = MAX;
            for (Point move : gs.getMoves()) {
                GameStatus nextBoard = new GameStatus???(gs);
                nextBoard.movePiece???(move);
                
                int score = minimax(nextBoard, depth - 1, alpha, beta, true);
                bestScore = Math.min(score, bestScore);
                
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {    //alpha-beta pruing
                  break;
                }
            }
            transpositionTable.put(hash, bestScore);
            return bestScore;
        }
    }
    
    /**
    Determina si el disco en el punto dado del 
    * estado del juego es estable.
    Un disco se considera estable si est?? rodeado de discos del mismo 
    * del mismo color en cualquier direcci??n.
    @param p El punto del disco a comprobar.
    @param gs El estado actual del juego.
    @return true si el disco en el punto dado es estable, false en caso contrario.
    */
    private boolean isStable(Point p, GameStatus gs) {
        // Comprueba si la ficha en el punto dado es estable
        CellType disc = gs.getPos(p.x, p.y);
        if (disc == CellType.EMPTY) {
            return false;
        }

        int size = gs.getSize();

        // Comprueba las filas y columnas adyacentes a la ficha
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

        // Comprueba las diagonales adyacentes a la ficha
        if (p.x - 1 >= 0 && p.y - 1 >= 0 && p.x + 1 < size && p.y + 1 < size && gs.getPos(p.x - 1, p.y - 1) == disc && gs.getPos(p.x + 1, p.y + 1) == disc) {
            return true;
        }
        if (p.x - 1 >= 0 && p.y + 1 < size && p.x + 1 < size && p.y - 1 >= 0 && gs.getPos(p.x - 1, p.y + 1) == disc && gs.getPos(p.x + 1, p.y - 1) == disc) {
            return true;
        }

        return false;
    }
    
    /**Calcula una puntuaci??n heur??stica para el jugador actual 
    * en el estado de juego dado.
    La puntuaci??n se basa en el n??mero de discos, la movilidad, la estabilidad 
    * y posici??n estrat??gica de los discos del jugador,
    * as?? como los valores correspondientes de los discos del oponente.
    @param gs Estado actual de la partida.
    @return Puntuaci??n heur??stica del jugador actual.
   */
    public int heuristica(GameStatus gs) {
        TAULERS_EXAMINATS++;

        //Inicializa la puntuaci??n con el n??mero de fichas para el jugador
        int score = gs.getScore(gs.getCurrentPlayer());

        //A??ade una penalizaci??n por cada movimiento posible que tiene el oponente
        int numMoves = gs.getMoves().size();
        score -= numMoves * WEIGHT_NUM_MOVES;

        // A??ade un bono por cada ficha que tiene movilidad
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == gs.getCurrentPlayer()) {
                    score += gs.getMoves().size() * WEIGHT_MOBILITY;
                }
            }
        }

        // A??ade un bono por cada ficha estable
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (isStable(new Point(i, j), gs)) {
                    score += WEIGHT_STABLE_DISCS;
                }
            }
        }

        // A??ade un bono por cada ficha en una esquina
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == gs.getCurrentPlayer() &&
                    ((i == 0 && j == 0) || (i == 0 && j == gs.getSize() - 1) ||
                     (i == gs.getSize() - 1 && j == 0) || (i == gs.getSize() - 1 && j == gs.getSize() - 1))) {
                    score += WEIGHT_CORNER_DISCS;
                }
            }
        }

        // A??ade una penalizaci??n por cada ficha en una esquina ocupada por el oponente
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == CellType.opposite(gs.getCurrentPlayer()) &&
                    ((i == 0 && j == 0) || (i == 0 && j == gs.getSize() - 1) ||
                     (i == gs.getSize() - 1 && j == 0) || (i == gs.getSize() - 1 && j == gs.getSize() - 1))) {
                    score -= WEIGHT_CORNER_DISCS;
                }
            }
        }

        // A??ade los valores de la tabla de valores para cada ficha
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
            CellType piece = gs.getPos(j, i);
                if (piece == gs.getCurrentPlayer()) {
                    score += VALUES_TABLE[i][j];
                } else if (piece == CellType.opposite(gs.getCurrentPlayer())) {
                    score -= VALUES_TABLE[i][j];
                }
            }
        }
        
        // A??ade un bono por cada ficha controlado por el jugador
        int numDiscs = gs.getScore(gs.getCurrentPlayer());
        score += numDiscs * WEIGHT_DISCS_CONTROLLED;

        // A??ade una penalizaci??n por cada ficha controlado por el oponente
        int numOpponentDiscs = gs.getScore(CellType.opposite(gs.getCurrentPlayer()));
        score -= numOpponentDiscs * WEIGHT_DISCS_CONTROLLED;

        // A??ade un bono por cada ficha con movilidad
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == gs.getCurrentPlayer()) {
                    int mobility = getMobility(gs, new Point(i, j), gs.getCurrentPlayer());
                    score += mobility * WEIGHT_MOBILITY;
                }
            }
        }

        // A??ade una penalizaci??n por cada fichas con movilidad para el oponente
        for (int i = 0; i < gs.getSize(); i++) {
            for (int j = 0; j < gs.getSize(); j++) {
                if (gs.getPos(j, i) == CellType.opposite(gs.getCurrentPlayer())) {
                    int mobility = getMobility(gs, new Point(i, j), CellType.opposite(gs.getCurrentPlayer()));
                    score -= mobility * WEIGHT_MOBILITY;
                }
            }
        }

        // A??ade un bono por una ventaja en la paridad (m??s fichas en el tablero)
        int discParity = gs.getScore(gs.getCurrentPlayer()) - gs.getScore(CellType.opposite(gs.getCurrentPlayer()));
        if (discParity > 0) {
            score += discParity * WEIGHT_DISC_PARITY;
        } else if (discParity < 0) {
            score -= -discParity * WEIGHT_DISC_PARITY;
        }

        // A??ade un bono por movimientos que aumentan la movilidad del jugador
        for (Point move : gs.getMoves()) {
            int mobilityBefore = getMobility(gs, move, gs.getCurrentPlayer());
            int mobilityAfter = getMobility(gs, move, CellType.opposite(gs.getCurrentPlayer()));
            score += (mobilityAfter - mobilityBefore) * WEIGHT_MOBILITY;
        }

        return score;
    }
    
    /**
        Calcula el n??mero de movimientos posibles para el jugador dado 
        * en el estado actual de la partida.
        @param gs Estado actual de la partida.
        @param p La casilla actual.
        @param player El jugador para el que se calcula la movilidad.
        @return N??mero de movimientos posibles para el jugador dado en el 
        * estado actual de la partida.
    */
    public int getMobility(GameStatus gs, Point p, CellType player) {
        int mobility = 0;

        int size = gs.getSize();

        // Iterar a trav??s de las ocho direcciones alrededor de la celda dada
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                // Comprueba si la direcci??n actual tiene un movimiento v??lido
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

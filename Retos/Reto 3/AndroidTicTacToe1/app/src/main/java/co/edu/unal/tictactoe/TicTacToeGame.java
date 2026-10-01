package co.edu.unal.tictactoe;

import java.util.Random;

public class TicTacToeGame {

    public static final int BOARD_SIZE = 9;
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    private char mBoard[] = new char[BOARD_SIZE];
    private Random mRand;

    public TicTacToeGame() {
        mRand = new Random();
    }

    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    public void setMove(char player, int location) {
        if (location >= 0 && location < BOARD_SIZE) {
            mBoard[location] = player;
        }
    }

    public int getComputerMove() {
        int move;
        // 1. Intentar ganar
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    mBoard[i] = OPEN_SPOT;
                    return i;
                }
                mBoard[i] = OPEN_SPOT;
            }
        }
        // 2. Bloquear al humano
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = OPEN_SPOT;
                    return i;
                }
                mBoard[i] = OPEN_SPOT;
            }
        }
        // 3. Casilla aleatoria
        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }

    /**
     * @return 0 si continúa, 1 si es empate, 2 si gana X (Humano), 3 si gana O (Android)
     */
    public int checkForWinner() {
        // Horizontales
        for (int i = 0; i <= 6; i += 3) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i+1] == HUMAN_PLAYER && mBoard[i+2] == HUMAN_PLAYER) return 2;
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i+1] == COMPUTER_PLAYER && mBoard[i+2] == COMPUTER_PLAYER) return 3;
        }
        // Verticales
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i+3] == HUMAN_PLAYER && mBoard[i+6] == HUMAN_PLAYER) return 2;
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i+3] == COMPUTER_PLAYER && mBoard[i+6] == COMPUTER_PLAYER) return 3;
        }
        // Diagonales
        if ((mBoard[0] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[6] == HUMAN_PLAYER)) return 2;
        if ((mBoard[0] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[6] == COMPUTER_PLAYER)) return 3;

        // Comprobar si hay casillas libres
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) return 0;
        }
        return 1; // Empate
    }
}
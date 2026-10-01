package co.edu.unal.tictactoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BoardView extends View {

    public static final int GRID_WIDTH = 6;
    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;
    private Paint mPaint;
    private TicTacToeGame mGame;

    public BoardView(Context context) {
        super(context);
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.x_img);
        mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o_img);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setGame(TicTacToeGame game) {
        mGame = game;
    }

    public int getBoardCellWidth() {
        return getWidth() / 3;
    }

    public int getBoardCellHeight() {
        return getHeight() / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int boardWidth = getWidth();
        int boardHeight = getHeight();

        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(GRID_WIDTH);

        int cellWidth = boardWidth / 3;
        int cellHeight = boardHeight / 3;

        // Dibujar las dos líneas verticales
        canvas.drawLine(cellWidth, 0, cellWidth, boardHeight, mPaint);
        canvas.drawLine(cellWidth * 2, 0, cellWidth * 2, boardHeight, mPaint);

        // Dibujar las dos líneas horizontales
        canvas.drawLine(0, cellHeight, boardWidth, cellHeight, mPaint);
        canvas.drawLine(0, cellHeight * 2, boardWidth, cellHeight * 2, mPaint);

        // Dibujar las imágenes de X y O
        for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {
            int col = i % 3;
            int row = i / 3;

            int left = col * cellWidth;
            int top = row * cellHeight;
            int right = left + cellWidth;
            int bottom = top + cellHeight;

            // Margen para que las imágenes no toquen los bordes de la cuadrícula
            int padding = 20;
            Rect dest = new Rect(left + padding, top + padding, right - padding, bottom - padding);

            if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER) {
                canvas.drawBitmap(mHumanBitmap, null, dest, null);
            } else if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER) {
                canvas.drawBitmap(mComputerBitmap, null, dest, null);
            }
        }
    }
}
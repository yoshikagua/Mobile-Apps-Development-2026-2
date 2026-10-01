package co.edu.unal.tictactoe;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.SoundPool;

public class MainActivity extends AppCompatActivity {

    private TicTacToeGame mGame;
    private BoardView mBoardView;
    private TextView mInfoTextView;

    private boolean mGameOver;
    private boolean mHumanTurn; // Para evitar que el humano toque mientras Android piensa

    private int mHumanWins = 0;
    private int mAndroidWins = 0;
    private int mTies = 0;

    private TextView mHumanScoreTextView;
    private TextView mAndroidScoreTextView;
    private TextView mTiesScoreTextView;

    private SoundPool mSoundPool;
    private int mHumanSoundId;
    private int mComputerSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = findViewById(R.id.information);
        mHumanScoreTextView = findViewById(R.id.human_score);
        mTiesScoreTextView = findViewById(R.id.ties_score);
        mAndroidScoreTextView = findViewById(R.id.android_score);

        mGame = new TicTacToeGame();

        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        Button btnNewGame = findViewById(R.id.btn_new_game);
        Button btnDifficulty = findViewById(R.id.btn_difficulty);
        Button btnQuit = findViewById(R.id.btn_quit);

        btnNewGame.setOnClickListener(v -> startNewGame());
        btnDifficulty.setOnClickListener(v -> showDifficultyDialog());
        btnQuit.setOnClickListener(v -> showQuitDialog());

        updateScoreBoard();
        startNewGame();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Configurar atributos de audio para juegos
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // Crear el SoundPool (permite hasta 2 sonidos simultáneos)
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        // Cargar los sonidos en memoria
        mHumanSoundId = mSoundPool.load(this, R.raw.sword, 1);
        mComputerSoundId = mSoundPool.load(this, R.raw.swish, 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    private void updateScoreBoard() {
        mHumanScoreTextView.setText(getString(R.string.score_human, mHumanWins));
        mTiesScoreTextView.setText(getString(R.string.score_ties, mTies));
        mAndroidScoreTextView.setText(getString(R.string.score_android, mAndroidWins));
    }

    private void startNewGame() {
        mGame.clearBoard();
        mBoardView.invalidate(); // Redibujar el tablero vacío
        mGameOver = false;
        mHumanTurn = true;
        mInfoTextView.setText(R.string.first_human);
    }

    private boolean setMove(char player, int location) {
        if (mGame.getBoardOccupant(location) == TicTacToeGame.OPEN_SPOT) {
            mGame.setMove(player, location);
            mBoardView.invalidate(); // Redibujar el tablero con la nueva pieza

            // Parámetros: soundId, volumenIzquierdo, volumenDerecho, prioridad, loop, velocidad
            if (player == TicTacToeGame.HUMAN_PLAYER) {
                if (mSoundPool != null) {
                    mSoundPool.play(mHumanSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            } else {
                if (mSoundPool != null) {
                    mSoundPool.play(mComputerSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
            return true;
        }
        return false;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Actuar solo cuando se levanta el dedo para evitar toques duplicados
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int col = (int) event.getX() / mBoardView.getBoardCellWidth();
                int row = (int) event.getY() / mBoardView.getBoardCellHeight();
                int pos = row * 3 + col;

                if (!mGameOver && mHumanTurn && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                    mHumanTurn = false; // Bloquear toques adicionales
                    checkGameStatus();
                }
            }
            return true;
        }
    };

    private void checkGameStatus() {
        int winner = mGame.checkForWinner();

        if (winner == 0) {
            if (!mHumanTurn) {
                mInfoTextView.setText(R.string.turn_computer);

                // Retraso de 1 segundo para el movimiento de la IA
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    mHumanTurn = true;
                    checkGameStatus();
                }, 1000);
            } else {
                mInfoTextView.setText(R.string.turn_human);
            }
        } else {
            if (winner == 1) {
                mInfoTextView.setText(R.string.result_tie);
                mTies++;
            } else if (winner == 2) {
                mInfoTextView.setText(R.string.result_human_wins);
                mHumanWins++;
            } else {
                mInfoTextView.setText(R.string.result_computer_wins);
                mAndroidWins++;
            }
            mGameOver = true;
            updateScoreBoard();
        }
    }

    // --- DIÁLOGOS OMITIDOS PARA BREVEDAD (Mantén los mismos métodos de showDifficultyDialog y showQuitDialog) ---
    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.difficulty_choose);
        final CharSequence[] levels = { getString(R.string.difficulty_easy), getString(R.string.difficulty_harder), getString(R.string.difficulty_expert) };
        int selected = mGame.getDifficultyLevel().ordinal();
        builder.setSingleChoiceItems(levels, selected, (dialog, item) -> {
            dialog.dismiss();
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[item]);
            Toast.makeText(getApplicationContext(), levels[item], Toast.LENGTH_SHORT).show();
        });
        builder.create().show();
    }

    private void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.quit_question).setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> MainActivity.this.finish())
                .setNegativeButton(R.string.no, null);
        builder.create().show();
    }
}
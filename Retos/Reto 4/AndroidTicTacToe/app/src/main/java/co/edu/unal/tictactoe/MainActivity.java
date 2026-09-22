package co.edu.unal.tictactoe;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TicTacToeGame mGame;
    private Button mBoardButtons[];
    private TextView mInfoTextView;
    private boolean mGameOver;

    // Variables de Marcador
    private int mHumanWins = 0;
    private int mAndroidWins = 0;
    private int mTies = 0;

    private TextView mHumanScoreTextView;
    private TextView mAndroidScoreTextView;
    private TextView mTiesScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = findViewById(R.id.one);
        mBoardButtons[1] = findViewById(R.id.two);
        mBoardButtons[2] = findViewById(R.id.three);
        mBoardButtons[3] = findViewById(R.id.four);
        mBoardButtons[4] = findViewById(R.id.five);
        mBoardButtons[5] = findViewById(R.id.six);
        mBoardButtons[6] = findViewById(R.id.seven);
        mBoardButtons[7] = findViewById(R.id.eight);
        mBoardButtons[8] = findViewById(R.id.nine);

        mInfoTextView = findViewById(R.id.information);

        // Referencias del Marcador
        mHumanScoreTextView = findViewById(R.id.human_score);
        mTiesScoreTextView = findViewById(R.id.ties_score);
        mAndroidScoreTextView = findViewById(R.id.android_score);

        // Botones de la barra inferior
        Button btnNewGame = findViewById(R.id.btn_new_game);
        Button btnDifficulty = findViewById(R.id.btn_difficulty);
        Button btnQuit = findViewById(R.id.btn_quit);

        btnNewGame.setOnClickListener(v -> startNewGame());
        btnDifficulty.setOnClickListener(v -> showDifficultyDialog());
        btnQuit.setOnClickListener(v -> showQuitDialog());

        mGame = new TicTacToeGame();

        updateScoreBoard();
        startNewGame();
    }

    private void updateScoreBoard() {
        mHumanScoreTextView.setText(getString(R.string.score_human, mHumanWins));
        mTiesScoreTextView.setText(getString(R.string.score_ties, mTies));
        mAndroidScoreTextView.setText(getString(R.string.score_android, mAndroidWins));
    }

    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;

        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        mInfoTextView.setText(R.string.first_human);
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));

        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        } else {
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        @Override
        public void onClick(View view) {
            if (!mGameOver && mBoardButtons[location].isEnabled()) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);

                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                } else if (winner == 1) {
                    mInfoTextView.setText(R.string.result_tie);
                    mTies++;
                    mGameOver = true;
                } else if (winner == 2) {
                    mInfoTextView.setText(R.string.result_human_wins);
                    mHumanWins++;
                    mGameOver = true;
                } else {
                    mInfoTextView.setText(R.string.result_computer_wins);
                    mAndroidWins++;
                    mGameOver = true;
                }

                updateScoreBoard();
            }
        }
    }

    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.difficulty_choose);

        final CharSequence[] levels = {
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_harder),
                getString(R.string.difficulty_expert)
        };

        int selected = mGame.getDifficultyLevel().ordinal();

        builder.setSingleChoiceItems(levels, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();

                switch (item) {
                    case 0:
                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                        break;
                    case 1:
                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                        break;
                    case 2:
                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                        break;
                }

                Toast.makeText(getApplicationContext(), levels[item], Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();
    }

    private void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.quit_question)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> MainActivity.this.finish())
                .setNegativeButton(R.string.no, null);

        builder.create().show();
    }
}
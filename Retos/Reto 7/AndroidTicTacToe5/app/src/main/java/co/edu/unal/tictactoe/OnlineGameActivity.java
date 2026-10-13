package co.edu.unal.tictactoe;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ValueEventListener;

public class OnlineGameActivity extends AppCompatActivity {

    private String mGameId;
    private String mRole; // "HOST" or "GUEST"
    private String mPlayerId;
    private String mPlayerName;

    private String mMySymbol; // "X" for HOST, "O" for GUEST
    private String mOpponentSymbol; // "O" for HOST, "X" for GUEST

    private TicTacToeGame mGame;
    private BoardView mBoardView;
    private TextView mOnlineGameTitle;
    private TextView mOnlineInfoText;
    private TextView mTvPlayer1Score;
    private TextView mTvTieScore;
    private TextView mTvPlayer2Score;

    private Button mBtnNewGame;
    private Button mBtnQuit;

    private OnlineGameManager mGameManager;
    private ValueEventListener mGameListener;
    private OnlineGame mCurrentGame;

    private SoundPool mSoundPool;
    private int mXSoundId;
    private int mOSoundId;

    private String mPreviousBoard = "         ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_online_game);
        }

        mGameId = getIntent().getStringExtra("EXTRA_GAME_ID");
        mRole = getIntent().getStringExtra("EXTRA_ROLE");
        mPlayerId = getIntent().getStringExtra("EXTRA_PLAYER_ID");
        mPlayerName = getIntent().getStringExtra("EXTRA_PLAYER_NAME");

        if ("HOST".equals(mRole)) {
            mMySymbol = "X";
            mOpponentSymbol = "O";
        } else {
            mMySymbol = "O";
            mOpponentSymbol = "X";
        }

        mOnlineGameTitle = findViewById(R.id.online_game_title);
        mOnlineInfoText = findViewById(R.id.online_info);
        mTvPlayer1Score = findViewById(R.id.tv_player1_score);
        mTvTieScore = findViewById(R.id.tv_ties_score);
        mTvPlayer2Score = findViewById(R.id.tv_player2_score);

        mBtnNewGame = findViewById(R.id.btn_online_new_game);
        mBtnQuit = findViewById(R.id.btn_online_quit);

        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.online_board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mGameManager = new OnlineGameManager(this);

        mBtnNewGame.setOnClickListener(v -> {
            if (mCurrentGame != null && mGameId != null) {
                mGameManager.resetGame(mGameId);
            }
        });

        mBtnQuit.setOnClickListener(v -> showQuitDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        mXSoundId = mSoundPool.load(this, R.raw.sword, 1);
        mOSoundId = mSoundPool.load(this, R.raw.swish, 1);

        if (mGameManager != null) {
            mGameManager.goOnline();
        }

        if (mGameId != null) {
            mGameListener = mGameManager.listenToGame(mGameId, new OnlineGameManager.GameUpdateListener() {
                @Override
                public void onGameUpdated(OnlineGame game) {
                    mCurrentGame = game;
                    updateUI(game);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(OnlineGameActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }

        if (mGameListener != null && mGameId != null) {
            mGameManager.stopListeningToGame(mGameId, mGameListener);
            mGameListener = null;
        }
    }

    private void updateUI(OnlineGame game) {
        if (game == null) return;

        mOnlineGameTitle.setText(game.getName());

        // Actualizar tablero local
        String boardStr = game.getBoard();
        if (boardStr != null && boardStr.length() == 9) {
            char[] boardChars = new char[9];
            for (int i = 0; i < 9; i++) {
                char c = boardStr.charAt(i);
                if (c == 'X') {
                    boardChars[i] = TicTacToeGame.HUMAN_PLAYER;
                } else if (c == 'O') {
                    boardChars[i] = TicTacToeGame.COMPUTER_PLAYER;
                } else {
                    boardChars[i] = TicTacToeGame.OPEN_SPOT;
                }
            }
            mGame.setBoardState(boardChars);
            mBoardView.invalidate();

            // Reproducir sonido si hubo un cambio en el tablero
            if (!boardStr.equals(mPreviousBoard)) {
                playBoardChangeSound(mPreviousBoard, boardStr);
                mPreviousBoard = boardStr;
            }
        }

        // Actualizar puntuación
        String hostLabel = game.getHostName().isEmpty() ? "Jugador 1" : game.getHostName();
        String guestLabel = game.getGuestName().isEmpty() ? "Jugador 2" : game.getGuestName();

        mTvPlayer1Score.setText(hostLabel + " (X): " + game.getHostScore());
        mTvTieScore.setText("Empates: " + game.getTies());
        mTvPlayer2Score.setText(guestLabel + " (O): " + game.getGuestScore());

        // Actualizar estado del juego
        String status = game.getStatus();
        if (OnlineGame.STATUS_WAITING.equals(status)) {
            mOnlineInfoText.setText(R.string.status_waiting_player2);
            mBtnNewGame.setVisibility(View.GONE);
        } else if (OnlineGame.STATUS_IN_PROGRESS.equals(status)) {
            mBtnNewGame.setVisibility(View.GONE);
            if (mMySymbol.equals(game.getTurn())) {
                mOnlineInfoText.setText(getString(R.string.status_your_turn, mMySymbol));
            } else {
                String oppName = "HOST".equals(mRole) ? guestLabel : hostLabel;
                mOnlineInfoText.setText(getString(R.string.status_opponent_turn, oppName));
            }
        } else if (OnlineGame.STATUS_FINISHED.equals(status)) {
            mBtnNewGame.setVisibility(View.VISIBLE);
            String winner = game.getWinner();
            if (OnlineGame.WINNER_TIE.equals(winner)) {
                mOnlineInfoText.setText(R.string.status_game_tied);
            } else if (mMySymbol.equals(winner)) {
                mOnlineInfoText.setText(R.string.status_you_won);
            } else {
                String oppName = "HOST".equals(mRole) ? guestLabel : hostLabel;
                mOnlineInfoText.setText(getString(R.string.status_opponent_won, oppName));
            }
        } else if (OnlineGame.STATUS_ABANDONED.equals(status)) {
            mOnlineInfoText.setText(R.string.player_left);
            mBtnNewGame.setVisibility(View.VISIBLE);
        }
    }

    private void playBoardChangeSound(String oldBoard, String newBoard) {
        if (mSoundPool == null) return;
        for (int i = 0; i < 9; i++) {
            if (oldBoard.charAt(i) == ' ' && newBoard.charAt(i) != ' ') {
                if (newBoard.charAt(i) == 'X') {
                    mSoundPool.play(mXSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                } else if (newBoard.charAt(i) == 'O') {
                    mSoundPool.play(mOSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
                break;
            }
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mCurrentGame == null) return true;

                if (!OnlineGame.STATUS_IN_PROGRESS.equals(mCurrentGame.getStatus())) {
                    return true;
                }

                if (!mMySymbol.equals(mCurrentGame.getTurn())) {
                    Toast.makeText(OnlineGameActivity.this, "Espera tu turno", Toast.LENGTH_SHORT).show();
                    return true;
                }

                int col = (int) event.getX() / mBoardView.getBoardCellWidth();
                int row = (int) event.getY() / mBoardView.getBoardCellHeight();
                int pos = row * 3 + col;

                if (pos >= 0 && pos < 9 && mGame.getBoardOccupant(pos) == TicTacToeGame.OPEN_SPOT) {
                    char charToSet = "X".equals(mMySymbol) ? TicTacToeGame.HUMAN_PLAYER : TicTacToeGame.COMPUTER_PLAYER;
                    mGame.setMove(charToSet, pos);

                    // Convertir el tablero de mGame a String
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 9; i++) {
                        char c = mGame.getBoardOccupant(i);
                        if (c == TicTacToeGame.HUMAN_PLAYER) sb.append('X');
                        else if (c == TicTacToeGame.COMPUTER_PLAYER) sb.append('O');
                        else sb.append(' ');
                    }
                    String updatedBoard = sb.toString();

                    // Verificar si hay un ganador
                    int winnerCode = mGame.checkForWinner();
                    String winnerStatus = OnlineGame.WINNER_NONE;

                    int hostScore = mCurrentGame.getHostScore();
                    int guestScore = mCurrentGame.getGuestScore();
                    int ties = mCurrentGame.getTies();

                    if (winnerCode == 1) { // Tie
                        winnerStatus = OnlineGame.WINNER_TIE;
                        ties++;
                    } else if (winnerCode == 2) { // Human ('X' = Host) wins
                        winnerStatus = OnlineGame.WINNER_X;
                        hostScore++;
                    } else if (winnerCode == 3) { // Computer ('O' = Guest) wins
                        winnerStatus = OnlineGame.WINNER_O;
                        guestScore++;
                    }

                    String nextTurn = mOpponentSymbol;

                    mGameManager.makeMove(mGameId, updatedBoard, nextTurn, winnerStatus,
                            hostScore, guestScore, ties);
                }
            }
            return true;
        }
    };

    private void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.quit_question)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    if (mGameManager != null && mGameId != null && mPlayerId != null) {
                        mGameManager.leaveGame(mGameId, mPlayerId);
                    }
                    finish();
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showQuitDialog();
            }
        });
    }
}
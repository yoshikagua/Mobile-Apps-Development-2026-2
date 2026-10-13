package co.edu.unal.tictactoe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnlineLobbyActivity extends AppCompatActivity {

    private EditText mEtPlayerName;
    private Button mBtnCreateGame;
    private ListView mLvGames;
    private TextView mTvEmptyGames;
    private TextView mTvConnectionStatus;

    private OnlineGameManager mGameManager;
    private ValueEventListener mGamesListener;
    private ValueEventListener mConnectionListener;
    private GamesAdapter mAdapter;
    private List<OnlineGame> mGamesList;

    private String mPlayerId;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_lobby);

        // Ajustar márgenes seguros para evitar desbordamiento en la parte superior
        View rootContainer = findViewById(android.R.id.content);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootContainer, (v, insets) -> {
            androidx.core.graphics.Insets statusBarInsets = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.statusBars()
            );
            v.setPadding(v.getPaddingLeft(), statusBarInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_online_lobby);
        }

        mEtPlayerName = findViewById(R.id.et_player_name);
        mBtnCreateGame = findViewById(R.id.btn_create_game);
        mLvGames = findViewById(R.id.lv_available_games);
        mTvEmptyGames = findViewById(R.id.tv_empty_games);
        mTvConnectionStatus = findViewById(R.id.tv_connection_status);

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mPlayerId = mPrefs.getString("mPlayerId", null);
        if (mPlayerId == null) {
            mPlayerId = UUID.randomUUID().toString();
            mPrefs.edit().putString("mPlayerId", mPlayerId).apply();
        }

        String savedPlayerName = mPrefs.getString("mPlayerName", "Jugador");
        mEtPlayerName.setText(savedPlayerName);

        String savedDbUrl = mPrefs.getString("mDatabaseUrl", null);
        if (savedDbUrl != null && !savedDbUrl.trim().isEmpty()) {
            OnlineGameManager.sCustomDatabaseUrl = savedDbUrl.trim();
        }

        mGamesList = new ArrayList<>();
        mAdapter = new GamesAdapter(this, mGamesList);
        mLvGames.setAdapter(mAdapter);
        mLvGames.setEmptyView(mTvEmptyGames);

        mGameManager = new OnlineGameManager(this);

        mBtnCreateGame.setOnClickListener(v -> showCreateGameDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Configurar Firebase URL");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showConfigDbDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfigDbDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurar URL de Firebase");

        final EditText etUrl = new EditText(this);
        etUrl.setHint("https://tu-proyecto-rtdb.firebaseio.com/");
        String currentUrl = mPrefs.getString("mDatabaseUrl", "https://triquionline-51313-default-rtdb.firebaseio.com/");
        etUrl.setText(currentUrl);
        builder.setView(etUrl);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                mPrefs.edit().putString("mDatabaseUrl", url).apply();
                OnlineGameManager.sCustomDatabaseUrl = url;
                mGameManager = new OnlineGameManager(this);
                Toast.makeText(this, "URL actualizada", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameManager != null) {
            mGameManager.goOnline();
            mConnectionListener = mGameManager.listenToConnectionState(isConnected -> {
                if (mTvConnectionStatus != null) {
                    if (isConnected) {
                        mTvConnectionStatus.setText("🟢 Conectado a la nube");
                        mTvConnectionStatus.setTextColor(0xFF388E3C);
                    } else {
                        mTvConnectionStatus.setText("🔴 Sin conexión (reconectando...)");
                        mTvConnectionStatus.setTextColor(0xFFD32F2F);
                    }
                }
            });
        }

        mGamesListener = mGameManager.listenToAvailableGames(new OnlineGameManager.GamesListCallback() {
            @Override
            public void onGamesUpdated(List<OnlineGame> games) {
                mGamesList.clear();
                mGamesList.addAll(games);
                mAdapter.notifyDataSetChanged();

                // ELIMINA LAS LÍNEAS DE mTvEmptyGames.setVisibility(...)
                // ListView.setEmptyView() ya se encarga automáticamente de eso.
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OnlineLobbyActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGamesListener != null) {
            mGameManager.stopListeningToAvailableGames(mGamesListener);
            mGamesListener = null;
        }

        if (mConnectionListener != null) {
            mGameManager.stopListeningToConnectionState(mConnectionListener);
            mConnectionListener = null;
        }

        String playerName = mEtPlayerName.getText().toString().trim();
        if (!playerName.isEmpty()) {
            mPrefs.edit().putString("mPlayerName", playerName).apply();
        }
    }

    private String getPlayerName() {
        String name = mEtPlayerName.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Jugador";
        }
        return name;
    }

    private void showCreateGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.btn_create_game);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_game, null);
        EditText etGameName = dialogView.findViewById(R.id.et_dialog_game_name);
        etGameName.setText("Partida de " + getPlayerName());

        builder.setView(dialogView);
        builder.setPositiveButton("Crear", (dialog, which) -> {
            String gameName = etGameName.getText().toString().trim();
            if (gameName.isEmpty()) {
                gameName = "Partida de " + getPlayerName();
            }

            String playerName = getPlayerName();
            mGameManager.createGame(gameName, playerName, mPlayerId, new OnlineGameManager.GameCallback() {
                @Override
                public void onSuccess(OnlineGame game) {
                    Intent intent = new Intent(OnlineLobbyActivity.this, OnlineGameActivity.class);
                    intent.putExtra("EXTRA_GAME_ID", game.getGameId());
                    intent.putExtra("EXTRA_ROLE", "HOST");
                    intent.putExtra("EXTRA_PLAYER_ID", mPlayerId);
                    intent.putExtra("EXTRA_PLAYER_NAME", playerName);
                    startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(OnlineLobbyActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    private void joinGame(OnlineGame game) {
        String playerName = getPlayerName();
        mGameManager.joinGame(game.getGameId(), playerName, mPlayerId, new OnlineGameManager.GameCallback() {
            @Override
            public void onSuccess(OnlineGame updatedGame) {
                Intent intent = new Intent(OnlineLobbyActivity.this, OnlineGameActivity.class);
                intent.putExtra("EXTRA_GAME_ID", updatedGame.getGameId());
                intent.putExtra("EXTRA_ROLE", "GUEST");
                intent.putExtra("EXTRA_PLAYER_ID", mPlayerId);
                intent.putExtra("EXTRA_PLAYER_NAME", playerName);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OnlineLobbyActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class GamesAdapter extends ArrayAdapter<OnlineGame> {

        public GamesAdapter(Context context, List<OnlineGame> games) {
            super(context, 0, games);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_game, parent, false);
            }

            OnlineGame game = getItem(position);
            if (game != null) {
                TextView tvGameName = convertView.findViewById(R.id.tv_game_name);
                TextView tvHostName = convertView.findViewById(R.id.tv_host_name);
                Button btnJoin = convertView.findViewById(R.id.btn_join);

                tvGameName.setText(game.getName());
                tvHostName.setText("Creado por: " + game.getHostName());

                btnJoin.setOnClickListener(v -> joinGame(game));
            }

            return convertView;
        }
    }
}
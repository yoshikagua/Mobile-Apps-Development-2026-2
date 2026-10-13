package co.edu.unal.tictactoe;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineGameManager {

    private static final String DEFAULT_DATABASE_URL = "https://triquionline-51313-default-rtdb.firebaseio.com/";
    private static final String GAMES_NODE = "games";

    public static String sCustomDatabaseUrl = null;

    private DatabaseReference mDatabase;

    public interface GamesListCallback {
        void onGamesUpdated(List<OnlineGame> games);
        void onError(String message);
    }

    public interface GameCallback {
        void onSuccess(OnlineGame game);
        void onError(String message);
    }

    public interface GameUpdateListener {
        void onGameUpdated(OnlineGame game);
        void onError(String message);
    }

    public interface ConnectionCallback {
        void onConnectionStateChanged(boolean isConnected);
    }

    public static String sanitizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return DEFAULT_DATABASE_URL;
        }
        url = url.trim();
        if (url.endsWith(":null")) {
            url = url.substring(0, url.length() - 5);
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    public OnlineGameManager() {
        this(null);
    }

    public OnlineGameManager(Context context) {
        ensureFirebaseInitialized(context);
        String dbUrl = sanitizeUrl(sCustomDatabaseUrl);
        try {
            mDatabase = FirebaseDatabase.getInstance(dbUrl).getReference(GAMES_NODE);
        } catch (Exception e) {
            try {
                if (context != null && !FirebaseApp.getApps(context).isEmpty()) {
                    mDatabase = FirebaseDatabase.getInstance(FirebaseApp.getApps(context).get(0), dbUrl).getReference(GAMES_NODE);
                } else {
                    mDatabase = FirebaseDatabase.getInstance().getReference(GAMES_NODE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void ensureFirebaseInitialized(Context context) {
        if (context != null && FirebaseApp.getApps(context).isEmpty()) {
            try {
                String dbUrl = sanitizeUrl(sCustomDatabaseUrl);
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:100000000000:android:1000000000000000")
                        .setApiKey("AIzaSyB1234567890abcdefghijklmnopqrstuv")
                        .setDatabaseUrl(dbUrl)
                        .setProjectId("tictactoe-online-default")
                        .build();
                FirebaseApp.initializeApp(context.getApplicationContext(), options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void goOnline() {
        if (mDatabase != null) {
            try {
                mDatabase.getDatabase().goOnline();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ValueEventListener listenToConnectionState(final ConnectionCallback callback) {
        String dbUrl = sanitizeUrl(sCustomDatabaseUrl);
        DatabaseReference connectedRef;
        try {
            connectedRef = FirebaseDatabase.getInstance(dbUrl).getReference(".info/connected");
        } catch (Exception e) {
            connectedRef = mDatabase.getDatabase().getReference(".info/connected");
        }

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                callback.onConnectionStateChanged(Boolean.TRUE.equals(connected));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onConnectionStateChanged(false);
            }
        };

        connectedRef.addValueEventListener(listener);
        return listener;
    }

    public void stopListeningToConnectionState(ValueEventListener listener) {
        if (listener != null) {
            try {
                String dbUrl = sanitizeUrl(sCustomDatabaseUrl);
                FirebaseDatabase.getInstance(dbUrl).getReference(".info/connected").removeEventListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Escucha las partidas disponibles en Firebase (status == WAITING)
     */
    public ValueEventListener listenToAvailableGames(final GamesListCallback callback) {
        goOnline();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OnlineGame> availableGames = new ArrayList<>();
                for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                    OnlineGame game = gameSnapshot.getValue(OnlineGame.class);
                    if (game != null && OnlineGame.STATUS_WAITING.equals(game.getStatus())) {
                        availableGames.add(game);
                    }
                }
                callback.onGamesUpdated(availableGames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };

        mDatabase.addValueEventListener(listener);
        return listener;
    }

    public void stopListeningToAvailableGames(ValueEventListener listener) {
        if (listener != null) {
            mDatabase.removeEventListener(listener);
        }
    }

    /**
     * Crea un nuevo juego en Firebase
     */
    public void createGame(String gameName, String hostName, String hostId, final GameCallback callback) {
        DatabaseReference newGameRef = mDatabase.push();
        String gameId = newGameRef.getKey();

        if (gameId == null) {
            callback.onError("No se pudo generar un ID para la partida.");
            return;
        }

        OnlineGame game = new OnlineGame(gameId, gameName, hostId, hostName);
        newGameRef.setValue(game).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(game);
            } else {
                Exception e = task.getException();
                callback.onError(e != null ? e.getMessage() : "Error creando la partida.");
            }
        });
    }

    /**
     * Unirse a un juego existente como invitado (Jugador 2)
     */
    public void joinGame(String gameId, String guestName, String guestId, final GameCallback callback) {
        DatabaseReference gameRef = mDatabase.child(gameId);

        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OnlineGame game = snapshot.getValue(OnlineGame.class);
                if (game == null) {
                    callback.onError("La partida ya no existe.");
                    return;
                }

                if (!OnlineGame.STATUS_WAITING.equals(game.getStatus())) {
                    callback.onError("La partida ya no está disponible.");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("guestId", guestId);
                updates.put("guestName", guestName);
                updates.put("status", OnlineGame.STATUS_IN_PROGRESS);

                gameRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        game.setGuestId(guestId);
                        game.setGuestName(guestName);
                        game.setStatus(OnlineGame.STATUS_IN_PROGRESS);
                        callback.onSuccess(game);
                    } else {
                        Exception e = task.getException();
                        callback.onError(e != null ? e.getMessage() : "Error al unirse a la partida.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Escucha en tiempo real las actualizaciones de una partida específica
     */
    public ValueEventListener listenToGame(String gameId, final GameUpdateListener listener) {
        DatabaseReference gameRef = mDatabase.child(gameId);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OnlineGame game = snapshot.getValue(OnlineGame.class);
                if (game != null) {
                    listener.onGameUpdated(game);
                } else {
                    listener.onError("La partida ha sido eliminada.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };

        gameRef.addValueEventListener(eventListener);
        return eventListener;
    }

    public void stopListeningToGame(String gameId, ValueEventListener listener) {
        if (gameId != null && listener != null) {
            mDatabase.child(gameId).removeEventListener(listener);
        }
    }

    /**
     * Realizar un movimiento en el tablero de la partida
     */
    public void makeMove(String gameId, String newBoard, String nextTurn, String winner,
                         int hostScore, int guestScore, int ties) {
        DatabaseReference gameRef = mDatabase.child(gameId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("board", newBoard);
        updates.put("turn", nextTurn);
        updates.put("winner", winner);
        updates.put("hostScore", hostScore);
        updates.put("guestScore", guestScore);
        updates.put("ties", ties);

        if (!OnlineGame.WINNER_NONE.equals(winner)) {
            updates.put("status", OnlineGame.STATUS_FINISHED);
        }

        gameRef.updateChildren(updates);
    }

    /**
     * Reiniciar la partida para una nueva ronda
     */
    public void resetGame(String gameId) {
        DatabaseReference gameRef = mDatabase.child(gameId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("board", "         ");
        updates.put("turn", "X");
        updates.put("winner", OnlineGame.WINNER_NONE);
        updates.put("status", OnlineGame.STATUS_IN_PROGRESS);

        gameRef.updateChildren(updates);
    }

    /**
     * Salir o cancelar la partida
     */
    public void leaveGame(String gameId, String playerId) {
        if (gameId == null) return;
        DatabaseReference gameRef = mDatabase.child(gameId);

        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OnlineGame game = snapshot.getValue(OnlineGame.class);
                if (game == null) return;

                if (OnlineGame.STATUS_WAITING.equals(game.getStatus()) && playerId.equals(game.getHostId())) {
                    // Si el anfitrión sale mientras espera, elimina el juego
                    gameRef.removeValue();
                } else {
                    // Marca la partida como abandonada
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", OnlineGame.STATUS_ABANDONED);
                    gameRef.updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
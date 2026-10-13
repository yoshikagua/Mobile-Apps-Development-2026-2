package co.edu.unal.tictactoe;

public class OnlineGame {

    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_ABANDONED = "ABANDONED";

    public static final String WINNER_NONE = "NONE";
    public static final String WINNER_X = "X";
    public static final String WINNER_O = "O";
    public static final String WINNER_TIE = "TIE";

    private String gameId;
    private String name;
    private String hostId;
    private String hostName;
    private String guestId;
    private String guestName;
    private String status;
    private String board;
    private String turn;
    private String winner;
    private int hostScore;
    private int guestScore;
    private int ties;
    private long createdAt;

    // Constructor vacío requerido por Firebase Realtime Database
    public OnlineGame() {
    }

    public OnlineGame(String gameId, String name, String hostId, String hostName) {
        this.gameId = gameId;
        this.name = name;
        this.hostId = hostId;
        this.hostName = hostName;
        this.guestId = "";
        this.guestName = "";
        this.status = STATUS_WAITING;
        this.board = "         "; // 9 espacios vacíos
        this.turn = "X";
        this.winner = WINNER_NONE;
        this.hostScore = 0;
        this.guestScore = 0;
        this.ties = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getHostScore() {
        return hostScore;
    }

    public void setHostScore(int hostScore) {
        this.hostScore = hostScore;
    }

    public int getGuestScore() {
        return guestScore;
    }

    public void setGuestScore(int guestScore) {
        this.guestScore = guestScore;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
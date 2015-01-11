package games.ride.car.dartscorekeeper;

/**
 * Created by olsontl on 11/15/14.
 */
public class Player {
    private String name;
    private int highestScore;
    private int lowestScore;
    private int matchesWon;
    private int matchesLost;
    private int scoresHit[][];
    private int totalThrows;
    private int bullsHit[];
    private int turnsPlayed;
    private int gamesPlayed;

    public Player(String name) {
        this.name = name;
        highestScore = 0;
        lowestScore = 0;
        matchesWon = 0;
        matchesLost = 0;
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 3; j++) {
                scoresHit[i][j] = 0;
            }
        }
        bullsHit[0] = bullsHit[1] = 0;
        turnsPlayed = 0;
        gamesPlayed = 0;
    }

    public Player(String name, int highestScore, int lowestScore, int matchesWon, int matchesLost,
                  int[][] scoresHit, int totalThrows, int[] bullsHit, int turnsPlayed, int gamesPlayed) {
        this.name = name;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.matchesWon = matchesWon;
        this.matchesLost = matchesLost;
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 3; j++) {
                this.scoresHit[i][j] = scoresHit[i][j];
            }
        }
        this.totalThrows = totalThrows;
        this.bullsHit[0] = bullsHit[0];
        this.bullsHit[1] = bullsHit[1];
        this.turnsPlayed = turnsPlayed;
        this.gamesPlayed = gamesPlayed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(int lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int matchesLost) {
        this.matchesLost = matchesLost;
    }

    public int[][] getScoresHit() {
        return scoresHit;
    }

    public void setScoresHit(int score, int multiplier, int value) {
        this.scoresHit[score][multiplier] = value;
    }

    public int getTotalThrows() {
        return totalThrows;
    }

    public void setTotalThrows(int totalThrows) {
        this.totalThrows = totalThrows;
    }

    public int[] getBullsHit() {
        return bullsHit;
    }

    public void setBullsHit(int multiplier, int value) {
        this.bullsHit[multiplier] = value;
    }

    public int getTurnsPlayed() {
        return turnsPlayed;
    }

    public void setTurnsPlayed(int turnsPlayed) {
        this.turnsPlayed = turnsPlayed;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void incrementMatchesWon() {
        matchesWon += 1;
    }

    public void incrementMatchesLost() {
        matchesLost += 1;
    }

    public void incrementScoresHit(int score, int multiplier) {
        scoresHit[score][multiplier] += 1;
    }

    public void incrementBullsHit(int multiplier) {
        bullsHit[multiplier] += 1;
    }

    public void incrementTurnsPlayed() {
        turnsPlayed += 1;
    }

    public void incrementGamesPlayed() {
        gamesPlayed += 1;
    }

}

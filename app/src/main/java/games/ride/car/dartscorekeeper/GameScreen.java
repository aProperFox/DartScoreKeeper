package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class GameScreen extends Activity {

    protected int teamTurn;
    protected int playerTurn;
    protected int buttonsPressed;
    protected boolean isGameOver;

    protected final String[] scoreNames = {"20", "19", "18", "17", "16", "15", "bull"};
    protected List<HashMap<String, String>> teamScores;

    protected List<HashMap<String, String>> tempState;

    protected TextToSpeech ttobj;
    protected final String[] taunts = {"Is that really the best you can dew", "Wow... Great throw",
            "Smooth move, ecks lacks", "Pathetic", "How disappointing", "Lame", "Not even close",
            "Close, but no cigar", "That was terrible", "Well this is boring", "I'm certainly not impressed",
            "Don't expect to go pro any time soon", "I'm leaving", "Are you sure you know what target you're aiming for?",
            "Maybe it's the darts... Maybe it's just you", "Is there a draft in here, Or are you just that bad?",
            "That's okay... I didn't expect much from you"};
    protected int tauntsIterator;

    private Vibrator myVib;

    protected String closed = "(X)";

    protected Dialog endDialog;
    protected View exitView;
    protected View endView;

    protected Dialog overlayDialog;
    protected View overlayView;

    protected int disabledPlayer;

    protected static enum Score {
        TWENTY, NINETEEN, EIGHTEEN, SEVENTEEN, SIXTEEN, FIFTEEN, BULL, NONE
    }

    public int getPoint(Score score) {
        if(score == Score.TWENTY)
            return 20;
        else if(score == Score.NINETEEN)
            return 19;
        else if(score == Score.EIGHTEEN)
            return 18;
        else if(score == Score.SEVENTEEN)
            return 17;
        else if(score == Score.SIXTEEN)
            return 16;
        else if(score == Score.FIFTEEN)
            return 15;
        else if(score == Score.BULL)
            return 25;
        else
            return 0;
    }
    public Score getScore(String score) {
        if(score.equals("20"))
            return Score.TWENTY;
        else if(score.equals("19"))
            return Score.NINETEEN;
        else if(score.equals("18"))
            return Score.EIGHTEEN;
        else if(score.equals("17"))
            return Score.SEVENTEEN;
        else if(score.equals("16"))
            return Score.SIXTEEN;
        else if(score.equals("15"))
            return Score.FIFTEEN;
        else if(score.equals("bull"))
            return Score.BULL;
        else
            return null;
    }

    public String getScoreName(Score score) {
        if(score == Score.TWENTY)
            return "20";
        else if(score == Score.NINETEEN)
            return "19";
        else if(score == Score.EIGHTEEN)
            return "18";
        else if(score == Score.SEVENTEEN)
            return "17";
        else if(score == Score.SIXTEEN)
            return "16";
        else if(score == Score.FIFTEEN)
            return "15";
        else if(score == Score.BULL)
            return "bull";
        else
            return null;
    }

    protected static final class Turn {
        public Score score[] = new Score[3];
        public int multiplier[] = new int[3];
        public Turn() {
            score[0] = score[1] = score[2] = Score.NONE;
            multiplier[0] = multiplier[1] = multiplier[2] = 0;
        }
    }
    protected ArrayList<Turn> turns = new ArrayList<Turn>();
    protected Turn currentTurn;

    protected String[] scores = new String[3];
    protected int dartsLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGame();

    }

    public void initializeGame() {

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.game_screen_landscape);
        } else {
            setContentView(R.layout.game_screen_portrait);

        }

        if(MainMenu.PLAYERS == 3) {
            TextView extraName = (TextView) findViewById(R.id.team2_player2_name);
            extraName.setVisibility(View.GONE);
            extraName = (TextView) findViewById(R.id.team1_player2_name);
            extraName.setVisibility(View.GONE);

        }
        else {
            LinearLayout column_3 = (LinearLayout) findViewById(R.id.team_3_column);
            column_3.setVisibility(View.GONE);

            if(MainMenu.PLAYERS == 2) {
                TextView extraName = (TextView) findViewById(R.id.team2_player2_name);
                extraName.setVisibility(View.GONE);
                extraName = (TextView) findViewById(R.id.team1_player2_name);
                extraName.setVisibility(View.GONE);
            }
        }

        disabledPlayer = 0;

        setPlayerNames();
        teamTurn = 1;
        playerTurn = 1;

        TextView activePlayer = (TextView) findViewById(getResources().getIdentifier("team" + teamTurn + "_player" + playerTurn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));

        buttonsPressed = 0;

        teamScores = (MainMenu.PLAYERS == 3)? new ArrayList<HashMap<String, String>>(3): new ArrayList<HashMap<String, String>>(2);

        teamScores.add(new HashMap<String, String>());
        teamScores.add(new HashMap<String, String>());
        teamScores.get(0).put("score", "0");
        teamScores.get(1).put("score", "0");
        if(MainMenu.PLAYERS == 3) {
            teamScores.add(new HashMap<String, String>());
            teamScores.get(2).put("score", "0");
        }

        isGameOver = false;

        tempState = (MainMenu.PLAYERS == 3)? new ArrayList<HashMap<String, String>>(3): new ArrayList<HashMap<String, String>>(2);

        tempState.add(new HashMap<String, String>());
        tempState.add(new HashMap<String, String>());
        tempState.get(0).put("score", "0");
        tempState.get(1).put("score", "0");
        if(MainMenu.PLAYERS == 3) {
            tempState.add(new HashMap<String, String>());
            tempState.get(2).put("score", "0");
        }

        connectToTTS();

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        randomizeArray(taunts);
        tauntsIterator = 0;

        scores[0] = "";
        scores[1] = "";
        scores[2] = "";
        dartsLeft = 3;

        currentTurn = new Turn();

        initializeDialog();
    }

    public void onTeamScore(Score point, int multiplier) {
        if(isGameOver) {
            return;
        }

        TextView textView = (TextView) findViewById(getResources().getIdentifier("team_" + teamTurn + "_" + getScoreName(point), "id", getPackageName()));

        String team = getResources().getResourceName(textView.getId());
        team = team.substring(team.lastIndexOf('/') + 1, team.lastIndexOf('_'));

        int teamId = Integer.parseInt(team.substring(team.lastIndexOf('_') + 1));
        int turn = this.teamTurn;

        String scoreName = getScoreName(point);

        String scoreHit = teamScores.get(turn-1).get(scoreName);

        int timesHit = 0;
        if(scoreHit != null && !scoreHit.isEmpty()) {
            if(scoreHit.equals("/")) {
                timesHit = 1;
            } else if(scoreHit.equals("X")) {

                timesHit = 2;
            } else if(scoreHit.equals(closed)) {
                timesHit = 3;
            } else {
                    Log.e("ERROR", "Option " + scoreHit + " is not a valid score!");
            }
        }

        int newScore = 0;
        int leftOver = 0;

        String newText = closed;

        if(timesHit == 0) {
            switch(multiplier) {
                case 1:
                    newText = "/";
                    break;
                case 2:
                    newText = "X";
                    break;
                case 3:
                    break;
                default:
                    break;
            }


        } else if(timesHit == 1) {
            switch(multiplier) {
                case 1:
                    newText = "X";
                    break;
                case 2:
                    newText = closed;
                    break;
                case 3:
                    leftOver = 1;
                    break;
                default:
                    break;
            }
        } else if(timesHit == 2) {
            switch(multiplier) {
                case 1:
                    newText = closed;
                    break;
                case 2:
                    leftOver = 1;
                    break;
                case 3:
                    leftOver = 2;
                    break;
                default:
                    break;
            }

        } else {
            switch(multiplier) {
                case 1:
                    leftOver = 1;
                    break;
                case 2:
                    leftOver = 2;
                    break;
                case 3:
                    leftOver = 3;
                    break;
                default:
                    break;
            }
        }

        teamScores.get(turn - 1).put(scoreName, newText);
        textView.setText(newText);

        if(MainMenu.PLAYERS == 3) {

            TextView score = new TextView(this);

            for(int i = 1; i < 4; i++) {
                if(i != turn && i != disabledPlayer) {

                    String scoreClosed = teamScores.get(i-1).get(scoreName);

                    if(scoreClosed == null || scoreClosed != closed) {

                        score = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));

                        newScore = (getPoint(point) * leftOver) + Integer.parseInt(score.getText().toString());

                        score.setText("" + newScore);
                        teamScores.get(i-1).put("score", "" + newScore);
                    }

                }
            }


        } else {
            int otherTeam = (turn == 1)? 2: 1;
            String scoreClosed = teamScores.get(otherTeam-1).get(scoreName);

            if(scoreClosed == null || scoreClosed != closed) {

                String teamScoreId = team + "_score";

                TextView score = (TextView) findViewById(getResources().getIdentifier(teamScoreId, "id", getPackageName()));

                newScore = (getPoint(point) * leftOver) + Integer.parseInt(score.getText().toString());

                score.setText("" + newScore);
                teamScores.get(turn-1).put("score", "" + newScore);
            }
        }



    }


    public void endTurn(View view) {

        overlayDialog.cancel();

        myVib.vibrate(50);

        for(int i = 0; i < 3-dartsLeft; i++) {
            onTeamScore(currentTurn.score[i], currentTurn.multiplier[i]);
        }

        checkForWin();

        if(isGameOver) {
            return;
        }

        if(MainMenu.PLAYERS == 4) {
            if(teamTurn == 1 && playerTurn == 1) {
                teamTurn = 2;
            } else if(teamTurn == 2 && playerTurn == 1) {
                teamTurn = 1;
                playerTurn = 2;
            } else if(teamTurn == 1 && playerTurn == 2){
                teamTurn = 2;
            } else {
                teamTurn = 1;
                playerTurn = 1;
            }
        } else {
            teamTurn++;
            if(teamTurn > MainMenu.PLAYERS) {
                teamTurn = 1;
            }
            if(teamTurn == disabledPlayer) {
                teamTurn++;
                if(teamTurn > MainMenu.PLAYERS) {
                    teamTurn = 1;
                }
            }
        }

        TextView activePlayer;
        for(int i = 1; i <= MainMenu.PLAYERS; i++) {
            if(i != teamTurn) {
                if(MainMenu.PLAYERS == 4) {
                    if(i == 3) {
                        activePlayer = (TextView) findViewById(getResources().getIdentifier("team2_player2_name", "id", getPackageName()));
                        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.light));
                    } else if(i == 4) {
                        activePlayer = (TextView) findViewById(getResources().getIdentifier("team1_player2_name", "id", getPackageName()));
                        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.light));
                    } else {
                        activePlayer = (TextView) findViewById(getResources().getIdentifier("team" + i + "_player1_name", "id", getPackageName()));
                        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.light));
                    }
                } else {
                    activePlayer = (TextView) findViewById(getResources().getIdentifier("team" + i + "_player1_name", "id", getPackageName()));
                    activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.light));
                }
            }
        }

        activePlayer = (TextView)findViewById(getResources().getIdentifier("team" + teamTurn + "_player" + playerTurn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));

        if(dartsLeft == 3) {
            if(getSharedPreferences(MainMenu.PREFERENCES, 0).getBoolean("allowTaunts", true)) {
                if(tauntsIterator >= taunts.length)
                    tauntsIterator = 0;
                ttobj.speak(taunts[tauntsIterator], TextToSpeech.QUEUE_FLUSH, null);
            }
            tauntsIterator++;
        } else {
            Button button = new Button(this);
            Resources resources = getResources();
            ImageView imageView = new ImageView(this);
            for(int i = 0; i < 3; i++) {
                if(!scores[i].equals("")) {
                    button = (Button) overlayView.findViewById(getResources().getIdentifier(scores[i], "id", getPackageName()));
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_0));
                }
                imageView = (ImageView) overlayView.findViewById(getResources().getIdentifier("dart_" + (i + 1), "id", getPackageName()));
                imageView.setVisibility(View.VISIBLE);
            }
            scores[0] = "";
            scores[1] = "";
            scores[2] = "";
            dartsLeft = 3;
        }

        turns.add(currentTurn);
        currentTurn = new Turn();

        buttonsPressed = 0;


        tempState = new ArrayList<HashMap<String, String>>();
        for(HashMap<String, String> hm: teamScores) {
            tempState.add((HashMap<String, String>)hm.clone());
        }

    }

    public void checkForWin() {
        int turn = this.teamTurn;

        int score = Integer.parseInt(teamScores.get(turn-1).get("score"));

        HashMap<String, String> playerMap = teamScores.get(turn-1);

        boolean hasWon = true;

        for(int i = 0; i < scoreNames.length; i++) {
            if (playerMap.get(scoreNames[i]) == null || playerMap.get(scoreNames[i]) != closed) {
                hasWon = false;
                continue;
            } else if(playerMap.get(scoreNames[i]) == closed) {
                TextView textView = (TextView) findViewById(getResources().getIdentifier("team_" + turn + "_" + scoreNames[i], "id", getPackageName()));
                textView.setTextColor(getResources().getColor(R.color.closed));
            }
        }

        if(!hasWon) {
            return;
        }

        int otherTeam;

        String playerName;
        String text;

        TextView endText;

        SharedPreferences preferences = getSharedPreferences(MainMenu.PREFERENCES, 0);

        String players[] = {"", "", "", ""};
        players[0] = preferences.getString("Player 1", "Player 1");
        players[1] = preferences.getString("Player 2", "Player 2");
        players[2] = preferences.getString("Player 3", "Player 3");
        players[3] = preferences.getString("Player 4", "Player 4");

        switch (MainMenu.PLAYERS) {
            case 2:
                otherTeam = (turn == 1)? 2 : 1;

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + turn, "Player " + turn);
                    text = playerName + " Wins!";

                    if(turn == 2) {
                        preferences.edit().putString("Player 1", players[1]).putString("Player 2", players[0]).apply();
                    }

                    endText = (TextView) endView.findViewById(R.id.game_over_text);
                    endText.setText(text);
                    ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    isGameOver = true;
                    endDialog.setContentView(endView);
                    endDialog.show();
                }
                break;

            case 3:
                for(int i = 1; i < 4; i++) {
                    System.out.println("Score of " + i + " = " + teamScores.get(i-1).get("score") + ", Current score: " + score);
                    if(Integer.parseInt(teamScores.get(i-1).get("score")) < score) {
                        hasWon = false;
                    } else if(Integer.parseInt(teamScores.get(i-1).get("score")) > score && disabledPlayer == 0) {
                        disabledPlayer = i;
                            LinearLayout disabled = (LinearLayout)findViewById(getResources().getIdentifier("team_" + i + "_column", "id", getPackageName()));
                            disabled.setAlpha(0.25f);
                    }
                }
                if(!hasWon)
                    return;
                playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + turn, "Player " + turn);
                text = playerName + " Wins!";

                endText = (TextView) endView.findViewById(R.id.game_over_text);
                endText.setText(text);
                ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                isGameOver = true;
                endDialog.setContentView(endView);
                endDialog.show();

                switch (turn) {
                    case 1:
                        break;
                    case 2:
                        preferences.edit().putString("Player 1", players[1]).putString("Player 2", players[0]).apply();
                        break;
                    case 3:
                        preferences.edit().putString("Player 1", players[2]).putString("Player 3", players[0]).apply();
                        break;
                }

                break;
            case 4:
                otherTeam = (turn == 1)? 2 : 1;
                System.out.println("Other team = " + otherTeam);
                int p1 = 0, p2 = 0;

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    if(turn == 1) {
                        p1 = 1;
                        p2 = 4;
                    } else {
                        p1 = 2;
                        p2 = 3;

                    }
                        playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + p1, "Player " + p1);
                        text = playerName + " and ";
                        playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + p2, "Player " + p2);
                        text += playerName + " win!";


                    endText = (TextView) endView.findViewById(R.id.game_over_text);
                    endText.setText(text);
                    ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    isGameOver = true;
                    endDialog.setContentView(endView);
                    endDialog.show();

                    if(p1 == 2)
                        preferences.edit().putString("Player 1", players[1]).putString("Player 2", players[0])
                            .putString("Player 3", players[3]).putString("Player 4", players[2]).apply();

                }

                break;
        }
    }

    private void initializeDialog() {
        // Set display variables
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Layout inflater to help inflate a view for the dialog when the game is over
        LayoutInflater inflater = LayoutInflater.from(GameScreen.this);
        exitView = inflater.inflate(R.layout.dialog_exit, null);
        endView = inflater.inflate(R.layout.dialog_end_game, null);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            exitView.setMinimumHeight((int) (screenHeight * 0.9f));
            exitView.setMinimumWidth((int) (screenWidth * 0.6f));
            endView.setMinimumHeight((int) (screenHeight * 0.9f));
            endView.setMinimumWidth((int) (screenWidth * 0.6f));
            overlayView = inflater.inflate(R.layout.dialog_scores_landscape, null);
        } else {
            exitView.setMinimumWidth((int) (screenWidth * 0.9f));
            exitView.setMinimumHeight((int) (screenHeight * 0.6f));
            endView.setMinimumWidth((int) (screenWidth * 0.9f));
            endView.setMinimumHeight((int) (screenHeight * 0.6f));
            overlayView = inflater.inflate(R.layout.dialog_scores_portrait, null);
        }

        // Instantiate the end dialog for when the game is over
        endDialog = new Dialog( GameScreen.this, android.R.style.Theme_Translucent );
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            endDialog.getWindow().setLayout( screenWidth /2, (int) (screenHeight * 0.9f));
        } else {
            endDialog.getWindow().setLayout((int) (screenWidth * 0.9f), screenHeight / 2);
        }
        endDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        endDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        endDialog.setContentView(exitView);
        endDialog.getWindow().getAttributes().dimAmount = 0.5f;
        endDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationCancel;

        endDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                }
                return true;
            }
        });
        overlayView.setMinimumHeight(screenHeight);
        overlayView.setMinimumWidth(screenWidth);

        overlayDialog = new Dialog(GameScreen.this, android.R.style.Theme_Translucent);
        overlayDialog.getWindow().setLayout(screenWidth, screenHeight);
        overlayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        overlayDialog.setContentView(overlayView);
        overlayDialog.getWindow().getAttributes().windowAnimations = R.style.Fade;
    }

    @Override
    public void onBackPressed(){
        endDialog.show();
    }

    public void onConfirm(View view) {
        myVib.vibrate(100);
        endDialog.cancel();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(600); // As I am using LENGTH_LONG in Toast
                    GameScreen.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        thread.run();
    }

    public void onDecline(View view) {
        myVib.vibrate(100);
        endDialog.cancel();
    }

    public void onQuit(View view) {
        myVib.vibrate(100);
        endDialog.cancel();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(600); // As I am using LENGTH_LONG in Toast
                    GameScreen.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        thread.run();
    }

    public void onRematch(View view) {
        myVib.vibrate(100);
        endDialog.cancel();

        initializeGame();
    }

    public void undoMoves(View view) {
        int turn = this.teamTurn;
        if(MainMenu.PLAYERS == 4) {
            if(turn == 3 || turn == 2) {
                turn = 2;
            } else {
                turn = 1;
            }
        }

        TextView textView = new TextView(this);
        String tempText = "";

        for(int i = 0; i < scoreNames.length; i++) {
            textView = (TextView)findViewById(getResources().getIdentifier("team_" + turn + "_" + scoreNames[i], "id", getPackageName()));
            tempText = tempState.get(turn-1).get(scoreNames[i]);
            if(tempText == null)
                tempText = "";
            System.out.println("Temp text: " + tempText);
            textView.setText(tempText);
            teamScores.get(turn-1).put(scoreNames[i], tempText);
        }

        if(MainMenu.PLAYERS == 3) {
            for(int i = 1; i < 4; i++) {
                if(i != turn) {
                    textView = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));
                    tempText = tempState.get(i-1).get("score");
                    if(tempText == null)
                        tempText = "";
                    System.out.println("Temp text: " + tempText);
                    textView.setText(tempText);
                    teamScores.get(i-1).put("score", tempText);
                }

            }
        } else {
            textView = (TextView) findViewById(getResources().getIdentifier("team_" + turn + "_score", "id", getPackageName()));
            tempText = tempState.get(turn-1).get("score");
            if(tempText == null)
                tempText = "";
            System.out.println("Temp text: " + tempText);
            textView.setText(tempText);
            teamScores.get(turn-1).put("score", tempText);
        }

        buttonsPressed = 0;

    }

    public void openSettings(View view) {

    }

    public void onPressScore(View view) {
        if(dartsLeft > 0) {
            Button button = (Button) view;
            Resources resources = getResources();
            String scoreName = getResources().getResourceName(button.getId());
            scores[3-dartsLeft] = scoreName;
            currentTurn.score[3-dartsLeft] = getScore(scoreName.substring(scoreName.lastIndexOf("t") + 1, scoreName.lastIndexOf("x")));
            currentTurn.multiplier[3-dartsLeft] = Integer.parseInt(scoreName.substring(scoreName.length()-1));

            int hits = 0;
            for(int i = 0; i < 3; i++) {
                if(scoreName.equals(scores[i]))
                    hits++;
            }
            switch (hits) {
                case 1:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_1));
                    break;
                case 2:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_2));
                    break;
                case 3:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_3));
                    break;
                default:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_0));
                    break;
            }
            ImageView imageView = (ImageView) overlayView.findViewById(getResources().getIdentifier("dart_" + dartsLeft, "id", getPackageName()));
            imageView.setVisibility(View.INVISIBLE);
            dartsLeft--;
        }
    }

    public void undoScore(View view) {
        if(dartsLeft < 3) {
            Resources resources = getResources();
            Button button = (Button) overlayView.findViewById(resources.getIdentifier(scores[2 - dartsLeft], "id", getPackageName()));
            if(dartsLeft == 0) {

            } else if (dartsLeft == 1) {

            } else {

            }
            CharSequence sequence = scores[0] + "," + scores[1] + "," + scores[2];
            int count = StringUtils.countMatches(sequence, scores[2-dartsLeft]);
            switch (count) {
                case 1:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_0));
                    break;
                case 2:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_1));
                    break;
                case 3:
                    button.setBackgroundDrawable(resources.getDrawable(R.drawable.mult_2));
                    break;
                default:
                    break;
            }
            scores[2 - dartsLeft] = "";
            ImageView imageView = (ImageView) overlayView.findViewById(getResources().getIdentifier("dart_" + (dartsLeft+1), "id", getPackageName()));
            imageView.setVisibility(View.VISIBLE);
            dartsLeft++;
        } else {
            overlayDialog.cancel();
        }
    }

    public void enterScore(View view) {
        overlayDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LayoutInflater inflater = LayoutInflater.from(GameScreen.this);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.game_screen_landscape);
            overlayView = inflater.inflate(R.layout.dialog_scores_landscape, null);
        } else {
            setContentView(R.layout.game_screen_portrait);
            overlayView = inflater.inflate(R.layout.dialog_scores_portrait, null);
        }

        Resources resources = getResources();
        String packageName = getPackageName();
        String[] tempScores = new String[3];
        for(int i = 0; i < 3; i++) {
            tempScores[i] = scores[i];
        }

        scores[0] = scores[1] = scores[2] = "";
        dartsLeft = 3;
        for(int i = 0; i < 3; i++) {
            if(tempScores[i].contains(packageName)) {
                onPressScore(overlayView.findViewById(resources.getIdentifier(tempScores[i], "id", packageName)));
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        overlayDialog.getWindow().setLayout(screenWidth, screenHeight);
        overlayDialog.setContentView(overlayView);

        if(MainMenu.PLAYERS != 3) {
            LinearLayout column_3 = (LinearLayout) findViewById(R.id.team_3_column);
            column_3.setVisibility(View.GONE);
        }

        if(MainMenu.PLAYERS == 2 || MainMenu.PLAYERS == 3) {
            TextView extraName = (TextView) findViewById(R.id.team2_player2_name);
            extraName.setVisibility(View.GONE);
            extraName = (TextView) findViewById(R.id.team1_player2_name);
            extraName.setVisibility(View.GONE);
        }

        TextView activePlayer = (TextView)findViewById(getResources().getIdentifier("team" + teamTurn + "_player" + playerTurn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));

        if(disabledPlayer != 0) {
            LinearLayout disabled = (LinearLayout) findViewById(getResources().getIdentifier("team_" + disabledPlayer + "_column", "id", getPackageName()));
            disabled.setAlpha(0.25f);
        }

        // Re-initialze view
        TextView textView = new TextView(this);
        String tempText = "";

        int teamCount = (MainMenu.PLAYERS == 3)? MainMenu.PLAYERS: 2;

        for(int j = 1; j <= teamCount; j++) {
            for (int i = 0; i < scoreNames.length; i++) {
                textView = (TextView) findViewById(getResources().getIdentifier("team_" + j + "_" + scoreNames[i], "id", getPackageName()));
                tempText = teamScores.get(j - 1).get(scoreNames[i]);
                if (tempText == null)
                    tempText = "";
                textView.setText(tempText);
                if(tempText == closed) {
                    if(tempState.get(j-1).get(scoreNames[i]) == closed)
                    textView.setTextColor(getResources().getColor(R.color.closed));
                }
            }
        }

        for(int i = 1; i <= teamCount; i++) {
            textView = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));
            tempText = teamScores.get(i-1).get("score");
            if(tempText == null)
                tempText = "";
            textView.setText(tempText);
            if(tempText == closed) {
                textView.setTextColor(getResources().getColor(R.color.closed));
            }
        }

        setPlayerNames();


    }

    public void setPlayerNames() {

        SharedPreferences preferences = getSharedPreferences(MainMenu.PREFERENCES, 0);

        TextView player1 = (TextView)findViewById(R.id.team1_player1_name), player2 = (TextView)findViewById(R.id.team2_player1_name);

        player1.setText(preferences.getString("Player 1", "Player 1"));
        player2.setText(preferences.getString("Player 2", "Player 2"));

        if(MainMenu.PLAYERS == 3) {
            TextView player3 = (TextView)findViewById(R.id.team3_player1_name);
            player3.setText(preferences.getString("Player 3", "Player 3"));
        }
        else {

            if(MainMenu.PLAYERS == 4) {
                TextView player3 = (TextView)findViewById(R.id.team2_player2_name);
                player3.setText(preferences.getString("Player 3", "Player 3"));
                TextView player4 = (TextView)findViewById(R.id.team1_player2_name);
                player4.setText(preferences.getString("Player 4", "Player 4"));
            }
        }


    }

    public void randomizeArray(String[] array){
        Random rgen = new Random();  // Random number generator

        for (int i=0; i<array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            String temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }

    }

    private void connectToTTS() {
        ttobj=new TextToSpeech(getApplicationContext(),
            new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS){
                        ttobj.setLanguage(Locale.UK);
                    }
                }
            });
    }

    @Override
    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        connectToTTS();
        super.onResume();
    }

}

package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class GameScreen extends Activity {

    protected int turn;
    protected int buttonsPressed;
    protected boolean isGameOver;

    protected final String[] scoreNames = {"20", "19", "18", "17", "16", "15", "bull"};
    protected List<HashMap<String, String>> teamScores;

    protected List<HashMap<String, String>> tempState;

    protected TextToSpeech ttobj;
    protected final String[] taunts = {"Is that really the best you can dew", "Wow... Great throw",
            "Smooth move, ecks lacks", "Pathetic", "How disappointing", "Lame", "Not even close",
            "Close, but no cigar", "That was terrible", "Well this is boring", "I'm certainly not impressed",
            "Don't expect to go pro any time soon", "Not quite ready for the premiere league",
            "Are you sure you know what target you're aiming for?", "Maybe it's the darts... Maybe it's just you",
            "Is there a draft in here, Or are you just that bad?", "That's okay... I didn't expect much from you"};
    protected boolean hitSomething;

    private Vibrator myVib;

    protected String closed = "(X)";

    protected Dialog endDialog;
    protected View exitView;
    protected View endView;

    protected int disabledPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGame();


    }

    public void initializeGame() {

        if(MainMenu.PLAYERS == 3) {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_game_screen_3_columns_landscape);
            } else {
                setContentView(R.layout.activity_game_screen_3_columns_portrait);
            }
        }
        else {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_game_screen_2_columns_landscape);
            } else {
                setContentView(R.layout.activity_game_screen_2_columns_portrait);
            }

            if(MainMenu.PLAYERS == 2) {
                TextView extraName = (TextView) findViewById(R.id.player3_name);
                extraName.setVisibility(View.GONE);
                extraName = (TextView) findViewById(R.id.player4_name);
                extraName.setVisibility(View.GONE);
            }
        }

        disabledPlayer = 0;

        setPlayerNames();
        turn = 1;

        TextView activePlayer = (TextView) findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));
        activePlayer.setTextColor(getResources().getColor(R.color.dark));

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

        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.UK);
                        }
                    }
                });

        hitSomething = false;

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        initializeDialog();
    }

    public void onTeamScore(View view) {
        if(isGameOver) {
            return;
        }

        TextView textView = (TextView) view;

        String team = getResources().getResourceName(textView.getId());
        team = team.substring(team.lastIndexOf('/') + 1, team.lastIndexOf('_'));

        int teamId = Integer.parseInt(team.substring(team.lastIndexOf('_') + 1));
        int turn = this.turn;

        if(MainMenu.PLAYERS == 4) {
            if(turn == 2 || turn == 3)
                turn = 2;
            else
                turn = 1;
        }


        if(teamId != turn) {
            Context context = getApplicationContext();
            CharSequence text = "It's not your turn!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        buttonsPressed ++;
        if(buttonsPressed > 9) {
            Context context = getApplicationContext();
            CharSequence text = "You couldn't have scored that many!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        hitSomething = true;

        String scoreName = getResources().getResourceName(textView.getId());
        scoreName = scoreName.substring(scoreName.lastIndexOf('_') + 1);

        int hitPoints = 0;

        if(scoreName.equals("bull")) {
            hitPoints = 25;

        } else {
            hitPoints = Integer.parseInt(scoreName);
        }

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

        if(timesHit == 0) {

            teamScores.get(turn - 1).put(scoreName, "/");
            textView.setText("/");

        } else if(timesHit == 1) {

            teamScores.get(turn - 1).put(scoreName, "X");
            textView.setText("X");

        } else if(timesHit == 2) {

            teamScores.get(turn - 1).put(scoreName, closed);
            textView.setText(closed);

        } else {

            if(MainMenu.PLAYERS == 3) {

                TextView score = new TextView(this);

                for(int i = 1; i < 4; i++) {
                    if(i != turn && i != disabledPlayer) {

                        String scoreClosed = teamScores.get(i-1).get(scoreName);

                        if(scoreClosed == null || scoreClosed != closed) {

                            score = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));

                            newScore = Integer.parseInt(score.getText().toString());
                            newScore += hitPoints;

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

                    newScore = Integer.parseInt(score.getText().toString());
                    newScore += hitPoints;

                    score.setText("" + newScore);
                    teamScores.get(turn-1).put("score", "" + newScore);
                }
            }



        }

    }

    public void endTurn(View view) {

        myVib.vibrate(50);

        checkForWin();

        if(isGameOver) {
            return;
        }

        if(MainMenu.PLAYERS == 4) {
            if(turn == 1) {
                turn = 2;
            } else if(turn == 2) {
                turn = 4;
            } else if(turn == 3){
                turn = 1;
            } else {
                turn = 3;
            }
        } else {
            turn++;
            if(turn > MainMenu.PLAYERS) {
                turn = 1;
            }
            if(turn == disabledPlayer) {
                turn++;
                if(turn > MainMenu.PLAYERS) {
                    turn = 1;
                }
            }
        }

        TextView activePlayer = (TextView)findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));
        activePlayer.setTextColor(getResources().getColor(R.color.dark));

        for(int i = 1; i <= MainMenu.PLAYERS; i++) {
            if(i != turn) {
                activePlayer = (TextView) findViewById(getResources().getIdentifier("player" + i + "_name", "id", getPackageName()));
                activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.dark));
                activePlayer.setTextColor(getResources().getColor(R.color.light));
            }
        }


        if(!hitSomething) {
            Random rand = new Random();
            if(getSharedPreferences(MainMenu.PREFERENCES, 0).getBoolean("allowTaunts", true))
                ttobj.speak(taunts[rand.nextInt(taunts.length)], TextToSpeech.QUEUE_FLUSH, null);
        }

        buttonsPressed = 0;
        hitSomething = false;

        tempState = new ArrayList<HashMap<String, String>>();
        for(HashMap<String, String> hm: teamScores) {
            tempState.add((HashMap<String, String>)hm.clone());
        }

    }

    public void checkForWin() {
        int turn = this.turn;
        if(MainMenu.PLAYERS == 4) {
            if(turn == 2 || turn == 3) {
                turn = 2;
            } else {
                turn = 1;
            }
        }
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
                        TextView disabled = new TextView(this);
                        for(int j = 0; j < scoreNames.length; j++) {
                            disabled = (TextView)findViewById(getResources().getIdentifier("team_" + i + "_" + scoreNames[j], "id", getPackageName()));
                            disabled.setAlpha(0.25f);
                        }
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
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            exitView.setMinimumHeight((int) (screenHeight * 0.9f));
            exitView.setMinimumWidth((int) (screenWidth * 0.6f));
        } else {
            exitView.setMinimumWidth((int) (screenWidth * 0.9f));
            exitView.setMinimumHeight((int) (screenHeight * 0.6f));
        }

        endView = inflater.inflate(R.layout.dialog_end_game, null);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            endView.setMinimumHeight((int) (screenHeight * 0.9f));
            endView.setMinimumWidth((int) (screenWidth * 0.6f));
        } else {
            endView.setMinimumWidth((int) (screenWidth * 0.9f));
            endView.setMinimumHeight((int) (screenHeight * 0.6f));
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(MainMenu.PLAYERS == 3) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_game_screen_3_columns_landscape);

            } else {
                setContentView(R.layout.activity_game_screen_3_columns_portrait);
            }

        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_game_screen_2_columns_landscape);

            } else {
                setContentView(R.layout.activity_game_screen_2_columns_portrait);
            }

            if(MainMenu.PLAYERS == 2) {
                TextView extraName = (TextView) findViewById(R.id.player3_name);
                extraName.setVisibility(View.GONE);
                extraName = (TextView) findViewById(R.id.player4_name);
                extraName.setVisibility(View.GONE);
            }
        }

        TextView activePlayer = (TextView)findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
        activePlayer.setBackgroundDrawable(getResources().getDrawable(R.color.active));
        activePlayer.setTextColor(getResources().getColor(R.color.dark));

        if(disabledPlayer != 0) {
            TextView disabled = new TextView(this);
            for(int j = 0; j < scoreNames.length; j++) {
                disabled = (TextView)findViewById(getResources().getIdentifier("team_" + disabledPlayer + "_" + scoreNames[j], "id", getPackageName()));
                disabled.setAlpha(0.25f);
            }
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

        TextView player1 = (TextView)findViewById(R.id.player1_name), player2 = (TextView)findViewById(R.id.player2_name);

        player1.setText(preferences.getString("Player 1", "Player 1"));
        player2.setText(preferences.getString("Player 2", "Player 2"));

        if(MainMenu.PLAYERS == 3) {
            TextView player3 = (TextView)findViewById(R.id.player3_name);
            player3.setText(preferences.getString("Player 3", "Player 3"));
        }
        else {

            if(MainMenu.PLAYERS == 4) {
                TextView player3 = (TextView)findViewById(R.id.player3_name);
                player3.setText(preferences.getString("Player 3", "Player 3"));
                TextView player4 = (TextView)findViewById(R.id.player4_name);
                player4.setText(preferences.getString("Player 4", "Player 4"));
            }
        }


    }

    @Override
    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

}

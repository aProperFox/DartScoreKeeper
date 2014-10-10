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
import android.widget.LinearLayout;
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
    protected final String[] taunts = {"Is that really the best you can dew", "Wowwwwwwwwwwww... Great throw",
            "Smooth move, ex-lax", "I've seen better throws at a quadriplegic baseball game", "Pathetic",
            "How disappointing", "Michael J Fox called, he wants his aim back", "Lame", "Not even close",
            "Close, but no cigar"};
    protected boolean hitSomething;

    private Vibrator myVib;

    protected String closed = "(X)";

    protected Dialog endDialog;
    protected View endView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
        }

        setPlayerNames();


        turn = 1;

        if(MainMenu.PLAYERS == 3) {
            TextView activeTeam = (TextView) findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.active));
        } else {
            LinearLayout activeTeam = (LinearLayout) findViewById(getResources().getIdentifier("team_" + turn, "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.active));
        }

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
                    if(i != turn) {

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

        int players = (MainMenu.PLAYERS != 4)? MainMenu.PLAYERS : 2;
        if(players == 3) {

            TextView activeTeam = (TextView) findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.light));
            turn += 1;


            if(turn > players)
                turn = 1;

            activeTeam = (TextView) findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.active));
        } else {

            LinearLayout activeTeam = (LinearLayout) findViewById(getResources().getIdentifier("team_" + turn, "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.light));
            turn += 1;


            if(turn > players)
                turn = 1;

            activeTeam = (LinearLayout) findViewById(getResources().getIdentifier("team_" + turn, "id", getPackageName()));
            activeTeam.setBackground(getResources().getDrawable(R.color.active));
        }

        if(!hitSomething) {
            Random rand = new Random();
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
        int score = Integer.parseInt(teamScores.get(turn-1).get("score"));


        HashMap<String, String> playerMap = teamScores.get(turn-1);

        for(int i = 0; i < scoreNames.length; i++) {
            if (playerMap.get(scoreNames[i]) == null || playerMap.get(scoreNames[i]) != closed) {
                return;
            }
        }


        int otherTeam = 0;

        Context context;
        String playerName = "";
        CharSequence text;
        int duration = Toast.LENGTH_LONG;

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // As I am using LENGTH_LONG in Toast
                    GameScreen.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        switch (MainMenu.PLAYERS) {
            case 2:
                otherTeam = (turn == 1)? 2 : 1;

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    context = getApplicationContext();
                    playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + turn, "Player " + turn);
                    text = playerName + " Wins!";

                    Toast toast2 = Toast.makeText(context, text, duration);
                    toast2.show();
                    ttobj.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    isGameOver = true;
                    thread.start();
                }
                break;

            case 3:
                for(int i = 1; i < 4; i++) {
                    System.out.println("Score of " + i + " = " + teamScores.get(i-1).get("score") + ", Current score: " + score);
                    if(Integer.parseInt(teamScores.get(i-1).get("score")) < score)
                        return;
                }
                context = getApplicationContext();
                playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + turn, "Player " + turn);
                text = playerName + " Wins!";

                Toast toast3 = Toast.makeText(context, text, duration);
                toast3.show();
                ttobj.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null);
                isGameOver = true;
                thread.start();

                break;
            case 4:
                otherTeam = (turn == 1)? 2 : 1;
                System.out.println("Other team = " + otherTeam);

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    context = getApplicationContext();
                    text = "Team " + turn + " Wins!";

                    Toast toast4 = Toast.makeText(context, text, duration);
                    toast4.show();
                    ttobj.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    isGameOver = true;
                    thread.start();
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
        endView = inflater.inflate(R.layout.dialog_exit, null);
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
        endDialog.setContentView(endView);
        endDialog.getWindow().getAttributes().dimAmount = 0.5f;

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
        myVib.vibrate(50);
        endDialog.cancel();
        finish();
    }

    public void onDecline(View view) {
        myVib.vibrate(50);
        endDialog.cancel();
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

            TextView activePlayer = (TextView)findViewById(getResources().getIdentifier("player" + turn + "_name", "id", getPackageName()));
            activePlayer.setBackground(getResources().getDrawable(R.color.active));
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_game_screen_2_columns_landscape);

            } else {
                setContentView(R.layout.activity_game_screen_2_columns_portrait);
            }
            LinearLayout activePlayer = (LinearLayout)findViewById(getResources().getIdentifier("team_" + turn, "id", getPackageName()));
            activePlayer.setBackground(getResources().getDrawable(R.color.active));
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
            }
        }

        for(int i = 1; i <= teamCount; i++) {
            textView = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));
            tempText = teamScores.get(i-1).get("score");
            if(tempText == null)
                tempText = "";
            textView.setText(tempText);

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

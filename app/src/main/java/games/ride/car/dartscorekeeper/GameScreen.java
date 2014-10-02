package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GameScreen extends Activity {

    protected int turn;
    protected int buttonsPressed;
    protected boolean isGameOver;

    protected final String[] scoreNames = {"20", "19", "18", "17", "16", "15", "bull"};
    protected List<HashMap<String, String>> teamScores;

    protected List<HashMap<String, String>> tempState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(MainMenu.PREFERENCES, 0);


        if(MainMenu.PLAYERS == 3) {
            setContentView(R.layout.activity_game_screen_3_columns);
            TextView player3 = (TextView)findViewById(R.id.player3_name);
            player3.setText(preferences.getString("Player 3", "Player 3"));
        }
        else {
            setContentView(R.layout.activity_game_screen_2_columns);

            if(MainMenu.PLAYERS == 4) {
                TextView player3 = (TextView)findViewById(R.id.player3_name);
                player3.setText(preferences.getString("Player 3", "Player 3"));
                TextView player4 = (TextView)findViewById(R.id.player4_name);
                player4.setText(preferences.getString("Player 4", "Player 4"));
            }
        }
        TextView player1 = (TextView)findViewById(R.id.player1_name), player2 = (TextView)findViewById(R.id.player2_name);

        player1.setText(preferences.getString("Player 1", "Player 1"));
        player2.setText(preferences.getString("Player 2", "Player 2"));

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

        //Collections.copy(tempState, teamScores);

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

        String scoreName = getResources().getResourceName(textView.getId());
        scoreName = scoreName.substring(scoreName.lastIndexOf('_') + 1);

        int hitPoints = 0;

        if(scoreName.equals("bull")) {
            hitPoints = 25;

        } else {
            hitPoints = Integer.parseInt(scoreName);
        }

        String scoreHit = teamScores.get(turn-1).get(scoreName);

        int timesHit = (scoreHit != null && !scoreHit.isEmpty())? teamScores.get(turn-1).get(scoreName).length(): 0;

        String hitString = "";

        int newScore = 0;

        if(timesHit < 3) {
            timesHit ++;
            for(int i = 0; i < timesHit; i++) {
                hitString += "X";
            }
            teamScores.get(turn - 1).put(scoreName, hitString);
            textView.setText(hitString);
            if(timesHit == 3) {
                //textView.setTextColor(Color.rgb(176,53,53));
            }
        } else {

            if(MainMenu.PLAYERS == 3) {

                TextView score = new TextView(this);

                for(int i = 1; i < 4; i++) {
                    if(i != turn) {

                        String scoreClosed = teamScores.get(i-1).get(scoreName);

                        if(scoreClosed == null || scoreClosed.length() < 3) {

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

                if(scoreClosed == null || scoreClosed.length() < 3) {

                    String teamScoreId = team + "_score";

                    TextView score = (TextView) findViewById(getResources().getIdentifier(teamScoreId, "id", getPackageName()));

                    newScore = Integer.parseInt(score.getText().toString());
                    newScore += hitPoints;

                    score.setText("" + newScore);
                    teamScores.get(turn-1).put("score", "" + newScore);
                }
            }



        }

        checkForWin();

    }

    public void endTurn(View view) {
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


        buttonsPressed = 0;

        tempState = new ArrayList<HashMap<String, String>>();
        for(HashMap<String, String> hm: teamScores) {
            tempState.add((HashMap<String, String>)hm.clone());
        }

    }

    public void checkForWin() {
        int score = Integer.parseInt(teamScores.get(turn-1).get("score"));


        HashMap<String, String> playerMap = teamScores.get(turn-1);

        for(int i = 0; i < scoreNames.length; i++) {
            if (playerMap.get(scoreNames[i]) == null || playerMap.get(scoreNames[i]).length() < 3 ) {
                return;
            }
        }


        int otherTeam = 0;

        Context context;
        String playerName = "";
        CharSequence text;
        int duration = Toast.LENGTH_LONG;

        switch (MainMenu.PLAYERS) {
            case 2:
                otherTeam = (turn == 1)? 2 : 1;

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    context = getApplicationContext();
                    playerName = getSharedPreferences(MainMenu.PREFERENCES, 0).getString("Player " + turn, "Player " + turn);
                    text = playerName + " Wins!";

                    Toast toast2 = Toast.makeText(context, text, duration);
                    toast2.show();
                    isGameOver = true;
                    finish();
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
                isGameOver = true;
                finish();

                break;
            case 4:
                otherTeam = ((turn-1)>>1) + 1;
                System.out.println("Other team = " + otherTeam);

                if(score >= Integer.parseInt(teamScores.get(otherTeam - 1).get("score"))) {
                    context = getApplicationContext();
                    text = "Team " + turn + " Wins!";

                    Toast toast4 = Toast.makeText(context, text, duration);
                    toast4.show();
                    isGameOver = true;
                    finish();
                }

                break;
        }
    }

    @Override
    public void onBackPressed(){

        finish();
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

}

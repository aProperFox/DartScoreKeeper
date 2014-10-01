package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class GameScreen extends Activity {

    protected int turn;
    protected int buttonsPressed;
    protected boolean isGameOver;

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

        isGameOver = false;

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

        int timesHit = textView.getText().length();

        String hitString = "";

        int newScore = 0;

        if(timesHit < 3) {
            timesHit ++;
            for(int i = 0; i < timesHit; i++) {
                hitString += "X";
            }
            textView.setText(hitString);
        } else {

            if(MainMenu.PLAYERS == 3) {

                TextView score = new TextView(this);

                for(int i = 1; i < 4; i++) {
                    if(i != turn) {
                        score = (TextView) findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));

                        newScore = Integer.parseInt(score.getText().toString());
                        newScore += hitPoints;

                        score.setText("" + newScore);

                    }
                }
                score = (TextView) findViewById(getResources().getIdentifier("team_" + turn + "_score", "id", getPackageName()));
                newScore = Integer.parseInt(score.getText().toString());

            } else {

                String teamScoreId = team + "_score";

                TextView score = (TextView) findViewById(getResources().getIdentifier(teamScoreId, "id", getPackageName()));

                newScore = Integer.parseInt(score.getText().toString());
                newScore += hitPoints;

                score.setText("" + newScore);
            }



        }

        checkForWin(newScore);

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

    }

    public void checkForWin(int score) {

        for(int i = 20; i > 13; i--) {
            TextView textView;
            if(i == 14) {
                textView = (TextView)findViewById(getResources().getIdentifier("team_" + turn + "_bull", "id", getPackageName()));
                if(textView.getText().toString().length() < 3) {
                    return;
                }
            } else {
                textView = (TextView)findViewById(getResources().getIdentifier("team_" + turn + "_" + i, "id", getPackageName()));
                if(textView.getText().toString().length() < 3) {
                    return;
                }
            }
        }

        TextView scoreView = new TextView(this);
        int otherTeam = 0;

        Context context;
        String playerName = "";
        CharSequence text;
        int duration = Toast.LENGTH_LONG;

        switch (MainMenu.PLAYERS) {
            case 2:
                otherTeam = (turn == 1)? 2 : 1;
                System.out.println("Other team = " + otherTeam);
                scoreView = (TextView)findViewById(getResources().getIdentifier("team_" + otherTeam + "_score", "id", getPackageName()));

                if(score >= Integer.parseInt(scoreView.getText().toString())) {
                    context = getApplicationContext();
                    playerName = getPreferences(0).getString("Player " + turn, "Player " + turn);
                    text = playerName + " Wins!";

                    Toast toast2 = Toast.makeText(context, text, duration);
                    toast2.show();
                    isGameOver = true;
                }
                break;

            case 3:
                for(int i = 1; i < 4; i++) {
                    scoreView = (TextView)findViewById(getResources().getIdentifier("team_" + i + "_score", "id", getPackageName()));
                    if(Integer.parseInt(scoreView.getText().toString()) < score)
                        return;
                }
                context = getApplicationContext();
                playerName = getPreferences(0).getString("Player " + turn, "Player " + turn);
                text = playerName + " Wins!";

                Toast toast3 = Toast.makeText(context, text, duration);
                toast3.show();
                isGameOver = true;

                break;
            case 4:
                otherTeam = ((turn-1)>>1) + 1;
                System.out.println("Other team = " + otherTeam);
                scoreView = (TextView)findViewById(getResources().getIdentifier("team_" + otherTeam + "_score", "id", getPackageName()));

                if(score >= Integer.parseInt(scoreView.getText().toString())) {
                    context = getApplicationContext();
                    text = "Team " + turn + " Wins!";

                    Toast toast4 = Toast.makeText(context, text, duration);
                    toast4.show();
                    isGameOver = true;
                }

                break;
        }
    }

    @Override
    public void onBackPressed(){

        finish();
    }

}
package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Tyler on 10/1/2014.
 */
public class CustomizeScreen extends Activity {

    private EditText player1, player2, player3, player4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customzier_screen);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(MainMenu.PREFERENCES, 0);


        player1 = (EditText) findViewById(R.id.player_1);
        player2 = (EditText) findViewById(R.id.player_2);
        player3 = (EditText) findViewById(R.id.player_3);
        player4 = (EditText) findViewById(R.id.player_4);

        player1.setText(preferences.getString("Player 1", "Player 1"));
        player2.setText(preferences.getString("Player 2", "Player 2"));
        player3.setText(preferences.getString("Player 3", "Player 3"));
        player4.setText(preferences.getString("Player 4", "Player 4"));

        if(MainMenu.PLAYERS == 2) {

            player3.setVisibility(View.INVISIBLE);

            player4.setVisibility(View.INVISIBLE);


        } else if(MainMenu.PLAYERS == 3) {

            player3.setVisibility(View.VISIBLE);

            player4.setVisibility(View.INVISIBLE);

        } else {

            player3.setVisibility(View.VISIBLE);

            player4.setVisibility(View.VISIBLE);

        }

    }

   public void onPlayGame(View view) {

       SharedPreferences preferences = getSharedPreferences(MainMenu.PREFERENCES, 0);
       preferences.edit().putString("Player 1", player1.getText().toString()).commit();
       preferences.edit().putString("Player 2", player2.getText().toString()).commit();
       preferences.edit().putString("Player 3", player3.getText().toString()).commit();
       preferences.edit().putString("Player 4", player4.getText().toString()).commit();

        Intent i = new Intent(CustomizeScreen.this, GameScreen.class);
       startActivity(i);
   }

}

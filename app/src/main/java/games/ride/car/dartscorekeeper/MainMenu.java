package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class MainMenu extends Activity {

    public static int PLAYERS;

    public static String PREFERENCES = "Prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);



        Spinner spinner = (Spinner) findViewById(R.id.player_selector);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.players_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new SpinnerActivity());

        PLAYERS = 2;


    }

    public void clickBoard(View view) {
        Intent i = new Intent(MainMenu.this, CustomizeScreen.class);
        startActivity(i);
    }

}

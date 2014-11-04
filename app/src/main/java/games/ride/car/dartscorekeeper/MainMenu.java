package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainMenu extends Activity {

    public static int PLAYERS = 2;

    public static String PREFERENCES = "Prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    public void clickBoard(View view) {
        Intent i = new Intent(MainMenu.this, CustomizeScreen.class);
        startActivity(i);
    }

}

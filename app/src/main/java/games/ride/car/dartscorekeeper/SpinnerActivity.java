package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by Tyler on 10/1/2014.
 */

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public SpinnerActivity() {
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == 0) {
            MainMenu.PLAYERS = 2;
        } else if(i == 1) {
            MainMenu.PLAYERS = 3;
        } else if(i == 2) {
            MainMenu.PLAYERS = 4;
        } else {
            System.out.println("ERROR! Item selected doesn't exist?");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}


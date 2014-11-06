package games.ride.car.dartscorekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tyler on 10/1/2014.
 */
public class CustomizeScreen extends Activity {

    private AutoCompleteTextView player1, player2, player3, player4;

    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customzier_screen);

        final SharedPreferences preferences = getApplicationContext().getSharedPreferences(MainMenu.PREFERENCES, 0);


        player1 = (AutoCompleteTextView) findViewById(R.id.player_1);
        player2 = (AutoCompleteTextView) findViewById(R.id.player_2);
        player3 = (AutoCompleteTextView) findViewById(R.id.player_3);
        player4 = (AutoCompleteTextView) findViewById(R.id.player_4);

        player1.setText(preferences.getString("Player 1", ""));
        player2.setText(preferences.getString("Player 2", ""));
        player3.setText(preferences.getString("Player 3", ""));
        player4.setText(preferences.getString("Player 4", ""));

        if(MainMenu.PLAYERS == 2) {

            setRowVisibilityByPlayerCount(2);

        } else if(MainMenu.PLAYERS == 3) {

            setRowVisibilityByPlayerCount(3);

        } else {

            setRowVisibilityByPlayerCount(4);

        }

        Set<String> names = new HashSet<String>();
        names = preferences.getStringSet("names", names);

        ArrayList<String> players = new ArrayList<String>();
        players.addAll(names);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, players);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.player_1);
        textView.setAdapter(adapter);
        textView = (AutoCompleteTextView)
                findViewById(R.id.player_2);
        textView.setAdapter(adapter);
        textView = (AutoCompleteTextView)
                findViewById(R.id.player_3);
        textView.setAdapter(adapter);
        textView = (AutoCompleteTextView)
                findViewById(R.id.player_4);
        textView.setAdapter(adapter);

        checkBox = (CheckBox) findViewById(R.id.checkbox);

        checkBox.setChecked(preferences.getBoolean("allowTaunts", true));

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    preferences.edit().putBoolean("allowTaunts", true).apply();
                } else {
                    preferences.edit().putBoolean("allowTaunts", false).apply();
                }
            }
        });

    }

    private void setRowVisibilityByPlayerCount(int players) {
        TableRow row = new TableRow(this);
        Button removeButton = (Button) findViewById(R.id.remove_player_1);
        ImageButton button = new ImageButton(this);
        switch (players) {
            case 2:
                row = (TableRow) player3.getParent();
                row.setVisibility(View.GONE);

                row = (TableRow) player4.getParent();
                row.setVisibility(View.GONE);

                button = (ImageButton) findViewById(R.id.move_down_2);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_down_3);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_down_4);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_3);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_4);
                button.setVisibility(View.INVISIBLE);

                removeButton = (Button) findViewById(R.id.remove_player_2);
                removeButton.setText(" + ");
                removeButton.setTextColor(Color.rgb(60,60,60));
                removeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_button));
                break;
            case 3:
                row = (TableRow) player3.getParent();
                row.setVisibility(View.VISIBLE);

                row = (TableRow) player4.getParent();
                row.setVisibility(View.GONE);

                button = (ImageButton) findViewById(R.id.move_down_2);
                button.setVisibility(View.VISIBLE);
                button = (ImageButton) findViewById(R.id.move_down_3);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_down_4);
                button.setVisibility(View.INVISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_3);
                button.setVisibility(View.VISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_4);
                button.setVisibility(View.INVISIBLE);

                removeButton = (Button) findViewById(R.id.remove_player_2);
                removeButton.setText(" - ");
                removeButton.setTextColor(Color.rgb(255,0,0));
                removeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_button));

                removeButton = (Button) findViewById(R.id.remove_player_3);
                removeButton.setText(" + ");
                removeButton.setTextColor(Color.rgb(60, 60, 60));
                removeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_button));
                break;
            case 4:
                row = (TableRow) player3.getParent();
                row.setVisibility(View.VISIBLE);

                row = (TableRow) player4.getParent();
                row.setVisibility(View.VISIBLE);

                button = (ImageButton) findViewById(R.id.move_down_3);
                button.setVisibility(View.VISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_3);
                button.setVisibility(View.VISIBLE);
                button = (ImageButton) findViewById(R.id.move_up_4);
                button.setVisibility(View.VISIBLE);

                removeButton = (Button) findViewById(R.id.remove_player_2);
                removeButton.setText("   ");
                removeButton.setBackgroundDrawable(new ColorDrawable(0x00000000));

                removeButton = (Button) findViewById(R.id.remove_player_3);
                removeButton.setText(" - ");
                removeButton.setTextColor(Color.rgb(255, 0, 0));

                removeButton = (Button) findViewById(R.id.remove_player_4);
                removeButton.setText("   ");
                removeButton.setBackgroundDrawable(new ColorDrawable(0x00000000));
                break;
            default:
                break;
        }
    }

    public void onPlayGame(View view) {

        SharedPreferences preferences = getSharedPreferences(MainMenu.PREFERENCES, 0);

        player1.setText(player1.getText().toString().replaceAll("\\s",""));
        player2.setText(player2.getText().toString().replaceAll("\\s",""));
        player3.setText(player3.getText().toString().replaceAll("\\s",""));
        player4.setText(player4.getText().toString().replaceAll("\\s",""));

        // Return if some name isn't set
        if(player1.getText().toString().isEmpty()) {
            showNameErrorToast(1);
            return;
        } else {
            preferences.edit().putString("Player 1", player1.getText().toString()).commit();
        }

        if(player2.getText().toString().isEmpty()) {
            showNameErrorToast(2);
            return;
        } else {
            preferences.edit().putString("Player 2", player2.getText().toString()).commit();
        }

        if(MainMenu.PLAYERS > 2 && player3.getText().toString().isEmpty()) {
            showNameErrorToast(3);
            return;
        } else {
            preferences.edit().putString("Player 3", player3.getText().toString()).commit();
        }

        if(MainMenu.PLAYERS > 3 && player4.getText().toString().isEmpty()) {
            showNameErrorToast(4);
            return;
        } else {
            preferences.edit().putString("Player 4", player4.getText().toString()).commit();
        }

        Set<String> names = new HashSet<String>();
        names = preferences.getStringSet("names", names);

        String players[] = {player1.getText().toString(), player2.getText().toString(), player3.getText().toString(), player4.getText().toString()};

        for(int i = 0; i < MainMenu.PLAYERS; i++) {
            if(!names.contains(players[i])) {
                names.add(players[i]);
            }
        }

        preferences.edit().putStringSet("names", names).commit();

        if(checkBox.isChecked()) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-3, 0);
        }

        Intent i = new Intent(CustomizeScreen.this, GameScreen.class);
        startActivity(i);
    }

    protected void showNameErrorToast(int playerId) {
        Context context = getApplicationContext();
        String text = "Player " + playerId + "'s name is empty!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    public void moveRow(View view) {
        ImageButton button = (ImageButton) view;
        String rowName = getResources().getResourceEntryName(button.getId());
        int rowMod = (rowName.contains("up"))? -1: 1;

        int rowId = Integer.parseInt(rowName.substring(rowName.lastIndexOf('_') + 1));
        String currentRow, newRow;
        AutoCompleteTextView currentName = (AutoCompleteTextView) findViewById(getResources().getIdentifier("player_" + rowId, "id", getPackageName()));
        currentRow = currentName.getText().toString();

        AutoCompleteTextView newName = (AutoCompleteTextView) findViewById(getResources().getIdentifier("player_" + (rowId+rowMod), "id", getPackageName()));
        newRow = newName.getText().toString();

        newName.setText(currentRow);
        currentName.setText(newRow);
        newName.dismissDropDown();
        currentName.dismissDropDown();

    }

    public void updatePlayers(View view) {
        Button button = (Button) view;

        if(button.getText().toString().contains("-")) {
            if(MainMenu.PLAYERS == 3) {
                setRowVisibilityByPlayerCount(2);
            } else if(MainMenu.PLAYERS == 4) {
                setRowVisibilityByPlayerCount(3);
            }
            MainMenu.PLAYERS -= 1;
        } else if(button.getText().toString().contains("+")) {
            if(MainMenu.PLAYERS == 2) {
                setRowVisibilityByPlayerCount(3);
            } else if(MainMenu.PLAYERS == 3) {
                setRowVisibilityByPlayerCount(4);
            }
            MainMenu.PLAYERS += 1;
        }

    }

}

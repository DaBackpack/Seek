package com.doggydigits.seek;

import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.Parse;
import com.parse.ParseObject;


/* Previous Activity MUST SEND PLAYER NUM AND GAME NAME AS "playerNum" and "gameName"
 * 
 */
public class GameRound extends ActionBarActivity {

	// gameName is the String the host entered for the game.
	// HP is remaining hit points. When <= 0, he's out.
	// playerNum is a number assigned by the game before this activity is reached. (In the lobby.)
	// charge represents this player's charge value: if -1, then the charge is not held. Otherwise, 0-3.
	public String gameName;
	public int HP, charge, playerNum;
	
	public int teamNum;
	
	// This is the intent that called this activity. (Usually the lobby.)
	public Intent intent;
	
	// This field constantly is updated with new GPS coordinates.
	public Location loc;
	
	// TODO: Set max size of one game.
	
	public static int MAX_SIZE = 20;
	
	// This is only stored locally! 
	// Records playerNums that may deal damage.
	public HashSet<Integer> attackingMe;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_round);

		Parse.initialize(getApplicationContext(), "tpMJgJuw0gFHtXqVO4YaRfvXVXsJmfiWJTNI8Ib6", "9nZ8rLxVbmm6KC94rbzeupQRzTemahxMTuenNxW8");

		attackingMe = new HashSet<Integer>();
		intent = getIntent();
		gameName = intent.getStringExtra("gameName");
		playerNum = intent.getIntExtra("playerNum", - 1);
		teamNum = intent.getIntExtra("teamNum", -1);
		
		if(gameName.length() == 0 || playerNum == -1 || teamNum == -1)
			; //throw exception
		
		HP = 100;
		charge = -1;
		
		// If this player is the host, begin the countdown from his clock.
		// He also initializes the "charges" array.
		// Otherwise, the player awaits the countdown to begin.
		ParseObject p = new ParseObject(gameName);
		
		if (playerNum == 0){
			p.put("started", 0);
			int[] array = new int[MAX_SIZE];
			int i = 0;
			while (i < MAX_SIZE){
				array[i] = -1;
				i++;
			}
			
			//p.put("charge", array);
			//p.saveInBackground();
			beginCountdown();
		} else // Players may begin in the countdown.
			if (p.getInt("started") == 0) {
				awaitCountdown();
		}
			else { // Game has already started...
				//TODO: What do we do with people just entering midway?
				//
			}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_round, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* Once the game starts, the host sends countdown data to the server.
	 * 
	 */
	public void beginCountdown(){
		ParseObject p = new ParseObject(gameName);
		int timer = 30;
		while (timer > 0){
			p.put("timer", timer);
			p.saveInBackground();
			displayTimer();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
			timer--;
		}
		
		// The timer is up, time to play!!
		beginGame();
	}
	
	public void awaitCountdown(){
		ParseObject p = new ParseObject(gameName);
		int timer = p.getInt("timer");
		while (timer > 0){
			displayTimer();
			timer = p.getInt("timer");
			
			// This is so the server isn't spammed with requests for the timer
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
			
		}
		
		// The timer is up, time to play!!
		beginGame();
	}
	
	// Display timer animation.
	public void displayTimer(){
		//TODO: display timer animation
	}
	
	public void beginGame(){
		// Every second, retrieve your GPS location and store it locally.
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	loc = location;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		
		// Register the listener with the Location Manager to receive location updates once per second
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
	
		// Next, we retrieve current data from the server.
			retrieveData();
	}
	
// The LOCAL integer charge represents the current player's charge value.
// The SERVER'S charge is an array of ALL charges by all players.
	
public void sendData(int[] chargeOld){
		ParseObject p = new ParseObject(gameName);
		p.put(playerNum + "HP", HP);
		p.put(playerNum + "lat", String.valueOf(loc.getLatitude()));
		p.put(playerNum + "long", String.valueOf(loc.getLongitude()));
		p.put(playerNum + "alt", String.valueOf(loc.getAltitude()));
		
		chargeOld[playerNum] = charge;
		p.put("charge", chargeOld);
		p.saveInBackground();
	}
	
	public void retrieveData(){
		
		ParseObject p = new ParseObject(gameName);
		int[] chargeOld = (int[]) p.get("charge");
		
		// If something fucks up, then there's likely a key missing so we send it anyway
		try {
		String locOldLat = (String) p.get(playerNum + "lat");
		String locOldLong = (String) p.get(playerNum + "long");
		String locOldAlt = (String) p.get(playerNum + "alt");
		int HPOld = p.getInt(playerNum + "HP");
		
		Location locOld = new Location(loc);
		locOld.setLatitude(Double.valueOf(locOldLat));
		locOld.setLongitude(Double.valueOf(locOldLong));
		locOld.setAltitude(Double.valueOf(locOldAlt));
		
		// If the player's status has been updated, reflect it in the server.
		// Otherwise, we don't care about updating.
		if (!loc.equals(locOld) || HPOld != HP || charge != chargeOld[playerNum]){
			sendData(chargeOld);
		}
		
		
		}
		catch (Exception e){
			sendData(chargeOld);
		}
		
		// Now we need to check for all player charges in the charges array and see who's firing.
		// 
		
		
		
	}
	
	public void damageCalculation()
	{
	}

}

package com.doggydigits.seek;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;



/* Previous Activity MUST SEND PLAYER NUM AND GAME NAME AS "playerNum" and "gameName"
 * 
 */
public class GameRound extends Activity {

	// gameName is the String the host entered for the game.
	// HP is remaining hit points. When <= 0, he's out.
	// playerNum is a number assigned by the game before this activity is reached. (In the lobby.)
	// charge represents this player's charge value: if -1, then the charge is not held. Otherwise, 0-3.
	public String gameName;
	public int HP, charge, playerNum;

	public TextView HPDisplay;
	
    public int timeOffset;
    public long startTime;
    public long lastUpdate;
    public int gameTime;
    public int lastStunTime;

	public int teamNum;
	public Player[] players;

	// This is the intent that called this activity. (Usually the lobby.)
	public Intent intent;
	
	// This field constantly is updated with new GPS coordinates.
	public Location loc;
	
	// TODO: Set max size of one game.
	
	public static final int MAX_SIZE = 20;
    public static final double BLAST_RADIUS = 20;
    public static final double SAFETY_RADIUS = 30;
	
	
    public void updateText(){
    		TextView latview = (TextView) findViewById(R.id.lat_view);
    		latview.setText("" + loc.getLatitude());
    		TextView longview = (TextView) findViewById(R.id.long_view);
    		longview.setText("" + loc.getLongitude());
    		
    			
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_round);
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		Parse.initialize(getApplicationContext(), "tpMJgJuw0gFHtXqVO4YaRfvXVXsJmfiWJTNI8Ib6", "9nZ8rLxVbmm6KC94rbzeupQRzTemahxMTuenNxW8");

		intent = getIntent();
		gameName = intent.getStringExtra("gameName");
		playerNum = intent.getIntExtra("playerNum", - 1);
		
		//TODO FIX THIS
		playerNum = intent.getStringExtra("isHost").equals("host") ? 0 : 1;
		
		// "red" is 0, "blue" is 1
		teamNum = intent.getStringExtra("team").equals("red") ? 0 : 1;
		
		if(gameName.length() == 0 || playerNum == -1)
			; //throw exception
		
		HP = 100;
		charge = -1;
		
		// If this player is the host, begin the countdown from his clock.
		// He also initializes the "charges" array.
		// Otherwise, the player awaits the countdown to begin.
		ParseObject p = new ParseObject(gameName);

		players = new Player[p.getInt("playerCount")];
        for (int i = 0 ; i < players.length; i++)
        {
            players[i] = new Player(i, i % 2, 0, 0, 0, 0);
        }

        HPDisplay = (TextView)findViewById(R.id.hp_view);
        updateHPDisplay();
        
		if (playerNum == 0)
        {
            p.put("started", 0);
			beginCountdown();
		}
        else // Players may begin in the countdown.
        {
            if (p.getInt("started") == 0)
                awaitCountdown();
            else { // Game has already started...
                //TODO: What do we do with people just entering midway?
                //
            }
        }

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_round, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
    {
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
	public void beginCountdown()
    {
		ParseObject p = new ParseObject(gameName);
		int timer = 0;
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

	public void awaitCountdown()
    {
		ParseObject p = new ParseObject(gameName);
		int timer = 0;//p.getInt("timer");
		while (timer > 0){
			displayTimer();
			
			// This is so the server isn't spammed with requests for the timer
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
            timer = p.getInt("timer");
		}
		
		// The timer is up, time to play!!
		beginGame();
	}
	
	// Display timer animation.
	public void displayTimer()
    {
		//TODO: display timer animation
	}
	
	public void updateHPDisplay()
	{
		HPDisplay.setText("" + HP);
	}
	
	public void beginGame(){
		// Every second, retrieve your GPS location and store it locally.
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
                double accsqrt = Math.sqrt(location.getAccuracy());
		    	loc.setLatitude((1 - accsqrt) * loc.getLatitude() + accsqrt * location.getLatitude());
                loc.setLongitude((1 - accsqrt) * loc.getLongitude() + accsqrt * location.getLongitude());
                loc.setAltitude((1 - accsqrt) * loc.getAltitude() + accsqrt * location.getAltitude());
                updateText();
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		
		// Register the listener with the Location Manager to receive location updates twice per second
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 250, 0, locationListener);

        // Host loop
        if (playerNum == 0)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Games are 6 minutes long
                    while (gameTime < 3600) {
                        try
                        {
                        	
                            Thread.sleep(500);
                            new Thread(new Runnable(){

                                @Override
                                public void run()
                                {

                                    gameTime = (int)((System.currentTimeMillis() - startTime - 30000) * 100);

                                    ParseObject p = new ParseObject(gameName);
                                    p.put("time", gameTime);
                                }
                            });
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                }
            }).start();
        }

        // Game loop thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (gameTime < 3600) {
                    try {
                        Thread.sleep(500);
                        new Runnable() {

                            @Override
                            public void run()
                            {

                                final ParseObject p = new ParseObject(gameName);
                                gameTime = p.getInt("time");
                                lastUpdate = System.currentTimeMillis();

                                final int[] stuff = new int[1];
                                
                                // Get locations and status of each player
                                for(stuff[0] = 0; stuff[0] < players.length; stuff[0]++)
                                    players[stuff[0]].setStatus(p.getString(stuff[0] + "::status"));

                                // Check for charge detonations
                                for(stuff[0] = 0; stuff[0] < players.length; stuff[0]++)
                                {
                                    // If another player is within a reasonable range
                                    // and will be detonating soon, spawn a new thread to handle
                                    // damage detection, and push notifications.
                                	
                                    final double dist = getDist(players[stuff[0]]);
                                    final int countdown = gameTime - players[stuff[0]].charge;
                                    if(dist < SAFETY_RADIUS && teamNum != players[stuff[0]].team && countdown < 8 && countdown > 0)
                                    {
                                    	
                                    	// Get instance of Vibrator from current Context
                                    	final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    	
                                    	final boolean[] done = {false};
                                    	
                                    
                                    	
                                        new Thread(new Runnable() {
                                            public void run() {
                                                try {
													Thread.sleep(countdown * 95);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
                                                done[0] = true;
                                                players[stuff[0]].setStatus(p.getString(stuff[0] + "::status"));

                                                // Added that you can't be damaged by own team.
                                                if(getDist(players[stuff[0]]) < BLAST_RADIUS && (teamNum != players[stuff[0]].team))
                                                    damageCalculation(players[stuff[0]], dist, gameTime + countdown);
                                            }
                                        }).start();
                                        
                                        new Thread(new Runnable() {
                                            public void run() {
                                                // Send push notifications during this period.
                                            	
                                            	while (!done[0]){
                                            		double dist = getDist(players[stuff[0]]);
                                            		v.vibrate(new long[] {(long) (1000 / dist), (long) (100 * dist)}, 1);
                                            		try {
														Thread.sleep(300);
													} catch (InterruptedException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
                                            	}
                                        }}).start();
                                    }
                                }

                                // Set own location
                                p.put(playerNum + "::", serializeStatus());
                            }
                        };
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();

	}
	

    public void chargeButtonPressed(View view)
    {
        if(HP > 0 && gameTime - lastStunTime > 30)
        {
            lastStunTime = (int)((System.currentTimeMillis() - lastUpdate) * 100) + gameTime;
            charge = lastStunTime + 35;
        }
    }

    public double getDist(Player p)
    {
        Location l = new Location(loc);
        l.setLatitude(p.lat);
        l.setLongitude(p.lon);
        l.setAltitude(p.alt);

        return loc.distanceTo(l);
    }

	public void damageCalculation(Player p, double dist, int gameTime)
	{
        int damage = (int) (80 * (1 - (BLAST_RADIUS - dist) / BLAST_RADIUS));
        HP = Math.max(HP - damage, 0);

        lastStunTime = gameTime;
        
        updateHPDisplay();
	}

    public String serializeStatus()
    {
        String toRet = String.valueOf(charge) + ";;" + HP + ";;" + loc.getLatitude() +
                ";;" + loc.getLongitude() + ";;" + loc.getAltitude();

        return toRet;
    }

}

final class Player
{
    public int pnum;
    public int team;
    public int charge;
    public int HP;
    public double lat;
    public double lon;
    public double alt;

    public Player(int player, int teamnum, int c, double la, double lo, double al)
    {
        pnum = player;
        team = teamnum;
        charge = c;
        lat = la;
        lon = lo;
        alt = al;

        HP = 0;
    }

    public void setStatus(int c, int h, Location loc)
    {
        charge = c;
        HP = h;
        lat = loc.getLatitude();
        lon = loc.getLongitude();
        alt = loc.getAltitude();
    }

    // Turn a string into a usable object
    public void setStatus(String serializedBlast)
    {
        String[] res = serializedBlast.split(";;");
        charge = Integer.parseInt(res[0]);
        lat = Double.parseDouble(res[1]);
        lon = Double.parseDouble(res[2]);
        alt = Double.parseDouble(res[3]);
    }

    // Serialize object
    public String serializeStatus()
    {
        String toRet = String.valueOf(charge) + ";;" + HP + ";;" + lat + ";;" + lon + ";;" + alt;

        return toRet;
    }
}

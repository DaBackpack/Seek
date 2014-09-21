package com.doggydigits.seek;

import java.util.ArrayList;

import com.parse.Parse;
import com.parse.ParseObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.os.Build;

public class LobbyActivity extends ActionBarActivity {

	public ListView playerListView1;
	public ListView playerListView2;
	public Intent intent;
	public int playerNum;
	public boolean started;
	public long startTime;
	public String gameName;
	public String playerName;
	public String isHost;
	public String team;
	
	public void updatePlayerList(String[] names)
	{
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		for(int i = 0 ; i < names.length ; i++)
		{
			String[] data = names[i].split(";;");
			if(data[1] == "red")
				list1.add(data[0] + ((playerNum == i) ? "*" : ""));
			else
				list2.add(data[0] + ((playerNum == i) ? "*" : ""));
		}
		
	    playerListView1.setAdapter(new ArrayAdapter<String>(this, 0, list1));
	    playerListView2.setAdapter(new ArrayAdapter<String>(this, 0, list2));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    Parse.initialize(getApplicationContext(), "tpMJgJuw0gFHtXqVO4YaRfvXVXsJmfiWJTNI8Ib6", "9nZ8rLxVbmm6KC94rbzeupQRzTemahxMTuenNxW8");
	    ParseObject p = new ParseObject("gameName");
		p.saveInBackground();
		
		intent = getIntent();

		gameName = intent.getStringExtra("gameName");
		playerNum = intent.getIntExtra("playerNum", -1);
		playerName = intent.getStringExtra("playerName");
		isHost = intent.getStringExtra("isHost");
		team = intent.getStringExtra("team");
		
	    playerListView1 = (ListView)findViewById(R.id.player_list_view1);
	    playerListView2 = (ListView)findViewById(R.id.player_list_view2);
	    
	    playerListView1.setAdapter(new ArrayAdapter<String>(this, 0));
	    playerListView2.setAdapter(new ArrayAdapter<String>(this, 0));
	    
	    if(playerNum == 0)
	    {
			Button button = (Button) findViewById(R.id.start);
			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					
					ParseObject p = new ParseObject(gameName);
					p.put("started", 1);
					p.put("startTime", (int)(System.currentTimeMillis() << 32 >> 32));
					startTime = System.currentTimeMillis();
				}
		   });
	    }
	    else
	    {
		    // Run waiting for players to join thread
		    new Thread(new Runnable() {
	            @Override
	            public void run() {
	            	started = false;
	                while (!started) {
	                    try
	                    {
	                        Thread.sleep(500);
	                        new Thread(new Runnable(){
	
	                            @Override
	                            public void run()
	                            {
	
	                                ParseObject p = new ParseObject(gameName);
	                                
	                                if(started = (p.getInt("started") == 1))
	                                {
	                                	long time = p.getInt("startTime");
	                                    startTime = (System.currentTimeMillis() >> 32) << 32 + time;
	                                }
	                                else
	                                {
	                                	String[] names = new String[p.getInt("playerCount")];
	                                	
	                                	for(int i = 0 ; i < names.length ; i++)
	                                	{
	                                		names[i] = p.getString(i + "::name") + ";;" + p.getString(i + "::team");
	                                	}
	                                	
	                                	updatePlayerList(names);
	                                }
	                            }
	                        });
	                    } catch (Exception e) {
	                        // TODO: handle exception
	                    }
	                }
	            }
	        }).start();
	    }
	    try {
			Thread.sleep(startTime - System.currentTimeMillis());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    Intent intent2 = new Intent(this, GameRound.class);
	    intent2.putExtra("gameName", gameName);
		intent2.putExtra("playerName", playerName);
		intent2.putExtra("isHost", isHost);
		intent2.putExtra("playerNum", playerNum);
		intent2.putExtra("isHost", isHost);
		intent2.putExtra("team", team);
	    startActivity(intent2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
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

}

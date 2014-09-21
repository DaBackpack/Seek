package com.doggydigits.seek;


import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ParanormalActivity extends ActionBarActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_paranormal);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.paranormal, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_paranormal,
					container, false);
			
			final String[] info = new String[4];
			final boolean []error = {false, false}; //1st check is for host toggle, 2nd is for team
			// player name [0]
			// game name [1] 
			// team name [2]
			// hostStatus [3]  if user is host "host" , joining game "join"
			
			final Activity act = getActivity();
			Button button = (Button) rootView.findViewById(R.id.start);
			   button.setOnClickListener(new OnClickListener()
			   {
				   @Override
			       public void onClick(View v)
			       {
					   info[0] = ((EditText) getView().findViewById(R.id.playername)).getText().toString();
					   info[1] = ((EditText) getView().findViewById(R.id.gamename)).getText().toString();
					   //check host status
					   boolean join = ((ToggleButton) getView().findViewById(R.id.join)).isChecked();
					   boolean host = ((ToggleButton) getView().findViewById(R.id.host)).isChecked();

					   Parse.initialize(getActivity(), "tpMJgJuw0gFHtXqVO4YaRfvXVXsJmfiWJTNI8Ib6", "9nZ8rLxVbmm6KC94rbzeupQRzTemahxMTuenNxW8");
						ParseObject p = new ParseObject(info[1]);
						p.saveInBackground();
						
					   if(join && !host) {
						   info[3] = "join";
						   int currentCount =  p.getInt("playerCount") + 1;
						   p.put(currentCount+"::name", info[0]);
						   p.put("playerCount", currentCount);
					   }
					   else if(host && !join) {
						   info[3] = "host";
						   p.put("0::name", info[0]);
						   p.put("playerCount", 1);
					   }
					   else{
						   error[0] = true;
					   }
				
					   //check team status
					   boolean red = ((ToggleButton) getView().findViewById(R.id.red)).isChecked();
					   boolean blue = ((ToggleButton) getView().findViewById(R.id.blue)).isChecked();
					   boolean random = ((ToggleButton) getView().findViewById(R.id.random)).isChecked();
					   if((red && blue && random) || (red && blue) || (red && random) || 
							   (blue && random) || (!red && !blue && !random)){
						   error[1] = true;
					   }
					   else if(red){
						   info[2] = "red";
					   }
					   else if(blue){
						   info[2] = "blue";
					   }
					   else{
						   int rando = (int) (Math.random()%2+1);
						   if(rando == 1){
							   info[2] = "red";
						   }
						   else{
							   info[2] = "blue";
						   }
							
					   }

					   // if (no errors)
					   Intent intent = new Intent(act, GameRound.class);
					   intent.putExtra("gameName", info[1]);
					   intent.putExtra("playerName", info[0]);
					   intent.putExtra("isHost", info[3]);
					   intent.putExtra("team", info[2]);
					   
					  
					   startActivity(intent);
					   
			       } 
			   });
			   
			   
			return rootView;
		}

		 
	}

}

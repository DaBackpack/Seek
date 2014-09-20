package com.doggydigits.seek;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;

public class GPSTest extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpstest);

		Parse.initialize(this, "tpMJgJuw0gFHtXqVO4YaRfvXVXsJmfiWJTNI8Ib6", "9nZ8rLxVbmm6KC94rbzeupQRzTemahxMTuenNxW8");
		
		
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      makeUseOfNewLocation(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		
	}
	
	private void makeUseOfNewLocation(Location loc) {
        TextView v = (TextView)this.findViewById(R.id.textView1);
        String text = "lat: " + loc.getLatitude() + ", long: " + loc.getLongitude() + ", height: " + loc.getAltitude();
        v.setText(text);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpstest, menu);
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

package ch.fenceposts.appquest.schrittzaehler;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import ch.fenceposts.appquest.schrittzaehler.stepcounter.StepCounter;
import ch.fenceposts.appquest.schrittzaehler.stepcounter.listener.StepListener;

public class WalkActivity extends Activity {

	private static final String DEBUG_TAG = "mydebug";
	private int steps;
	private SensorManager sensorManager;
	private StepCounter stepCounter;
	private StepListener stepListener;
	private TextView textViewWalk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor accelerometerSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		
		stepListener = new StepListener() {

			@Override
			public void onStep() {
				steps -= 1;
				writeSteps();
				Log.d(DEBUG_TAG, "step detected. " + String.valueOf(steps) + " to go");
				if (steps == 0) {
					setResult(RESULT_OK);
					finish();
				}
			}
		};
		
		stepCounter = new StepCounter(stepListener);
		
		sensorManager.registerListener(stepCounter, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);

		textViewWalk = (TextView) findViewById(R.id.textViewWalk);
		steps = getIntent().getIntExtra("ch.fenceposts.schrittzaehler.walk.steps", 42);
		writeSteps();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.walk, menu);
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

	private void writeSteps() {
		textViewWalk.setText("walk " + String.valueOf(steps) + " steps!");
	}
}

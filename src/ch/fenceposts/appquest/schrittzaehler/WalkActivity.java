package ch.fenceposts.appquest.schrittzaehler;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import ch.fenceposts.appquest.schrittzaehler.stepcounter.StepCounter;
import ch.fenceposts.appquest.schrittzaehler.stepcounter.listener.StepListener;

public class WalkActivity extends Activity {

	private static final String	DEBUG_TAG		= "mydebug";
	private static final String	DEBUG_TAG_TTS	= "mytts";
	private int					steps;
	private Sensor				accelerometerSensor;
	private SensorManager		sensorManager;
	private StepCounter			stepCounter;
	private StepListener		stepListener;
	private TextToSpeech		textToSpeech;
	private TextView			textViewWalk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_walk);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometerSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

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

		textViewWalk = (TextView) findViewById(R.id.textViewWalk);
		steps = getIntent().getIntExtra("ch.fenceposts.schrittzaehler.walk.steps", 42);
		writeSteps();
	}

	@Override
	public void onResume() {
		super.onResume();

		stepCounter = new StepCounter(stepListener);

		sensorManager.registerListener(stepCounter, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);

		textToSpeech = new TextToSpeech(this, new OnInitListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					textToSpeech.setLanguage(Locale.US);
					Log.d(DEBUG_TAG_TTS, "textToSpeech language set to US @WalkActivity");
					textToSpeech.speak(textViewWalk.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
				} else {
					Log.d(DEBUG_TAG_TTS, "Failure in onInit of TextToSpeech @WalkActivity! Status code:" + String.valueOf(status));
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();

		sensorManager.unregisterListener(stepCounter);

		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			Log.d(DEBUG_TAG_TTS, "textToSpeech stopped and shut down @WalkActivity");
		}
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

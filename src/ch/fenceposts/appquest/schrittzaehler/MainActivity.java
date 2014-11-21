// http://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
package ch.fenceposts.appquest.schrittzaehler;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ch.fenceposts.appquest.schrittzaehler.direction.Direction;

public class MainActivity extends Activity {

	private static final int SCAN_QR_CODE_START = 0;
	private static final int SCAN_QR_CODE_END = 1;
	private static final int DETECT_WALK = 2; // test
	private static final int DETECT_TURN = 3; // test
	private static final String DEBUG_TAG = "mydebug";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Log.d(DEBUG_TAG, "onActivityResult called");
		switch (requestCode) {
		case SCAN_QR_CODE_START:
			Log.d(DEBUG_TAG, "SCAN_QR_CODE_START");
			if (resultCode == RESULT_OK) {
				String jsonQrCodeStart = intent.getStringExtra("SCAN_RESULT");
				Log.d(DEBUG_TAG, "qrCodeStart:" + jsonQrCodeStart);

				startWalking(jsonQrCodeStart);
			}
			break;
		case SCAN_QR_CODE_END:
			Log.d(DEBUG_TAG, "SCAN_QR_CODE_END");
			if (resultCode == RESULT_OK) {
				String jsonQrCodeEnd = intent.getStringExtra("SCAN_RESULT");
				Log.d(DEBUG_TAG, "qrCodeEnd:" + jsonQrCodeEnd);
			}
			break;
		// walk test
		case DETECT_WALK:
			Log.d(DEBUG_TAG, "DETECT_WALK");
			if (resultCode == RESULT_OK) {
				Log.d(DEBUG_TAG, "walk detected");
				// nächster Abschnitt
			}
			break;
		// turn test
		case DETECT_TURN:
			Log.d(DEBUG_TAG, "DETECT_TURN");
			if (resultCode == RESULT_OK) {
				Log.d(DEBUG_TAG, "turn detected");
				// nächster Abschnitt
			}
			break;
		default:
			Log.d(DEBUG_TAG, "requestCode didn't match any checked codes");
			break;
		}
	}

	public void readQrCodeStart(View view) {
		Intent intentQrCodeReader = new Intent("com.google.zxing.client.android.SCAN");
		intentQrCodeReader.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intentQrCodeReader, SCAN_QR_CODE_START);
	}

	public void readQrCodeEnd(View view) {
		Intent intentQrCodeReader = new Intent("com.google.zxing.client.android.SCAN");
		intentQrCodeReader.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intentQrCodeReader, SCAN_QR_CODE_END);
	}

	@SuppressLint("NewApi")
	private void startWalking(String jsonQrCodeStart) {

		Integer steps = null;
		Direction direction = null;

		List<String> walkingInstructionsList = new LinkedList<>();
		int startStation;

		try {
			JSONObject jsonStart = new JSONObject(jsonQrCodeStart);
			JSONArray walkingInstructionsJson = jsonStart.getJSONArray("input");

			for (int i = 0; i < walkingInstructionsJson.length(); i++) {
				walkingInstructionsList.add(walkingInstructionsJson.get(i).toString());
			}
			Log.d(DEBUG_TAG, "walkingInstructionsList:" + walkingInstructionsList);

			startStation = jsonStart.getInt("startStation");
			Log.d(DEBUG_TAG, "startStation:" + String.valueOf(startStation));

		} catch (JSONException jsone) {
			Log.d(DEBUG_TAG, "JSONException occured!");
			jsone.printStackTrace();
		}

		for (String instruction : walkingInstructionsList) {
			if ((direction = Direction.fromString(instruction)) != null) {

			} else {
				try {
					steps = Integer.parseInt(instruction);
					Log.d(DEBUG_TAG, "steps to walk:" + steps);
				} catch (NumberFormatException nfe) {
					Log.d(DEBUG_TAG, "could not parse instruction to direction nor to integer!");
					nfe.printStackTrace();
				}
			}
		}

		if (steps != null) {
			// walkSteps(steps);
		} else if (direction != null) {
			// turnDirection(direction);
		}
	}

	public void startActivityTurn(View view) {
		Direction direction = null; // CHANGE!!!!
		Intent intentTurn = new Intent(this, TurnActivity.class);
		intentTurn.putExtra("ch.fenceposts.schrittzaehler.turn.direction", direction);
		startActivityForResult(intentTurn, DETECT_TURN);
	}

	public void startActivityWalk(View view) {
		Integer steps = null; // CHANGE!!!!
		Intent intentWalk = new Intent(this, WalkActivity.class);
		intentWalk.putExtra("ch.fenceposts.schrittzaehler.walk.steps", steps);
		startActivityForResult(intentWalk, DETECT_TURN);
	}
}

// http://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
// http://www.programcreek.com/java-api-examples/index.php?api=android.speech.tts.TextToSpeech
package ch.fenceposts.appquest.schrittzaehler;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import ch.fenceposts.appquest.schrittzaehler.direction.Direction;

public class MainActivity extends Activity {

	private static final int	SCAN_QR_CODE_START	= 0;
	private static final int	SCAN_QR_CODE_END	= 1;
	private static final int	DETECT_WALK			= 2;
	private static final int	DETECT_TURN			= 3;
	private static final String	DEBUG_TAG			= "mydebug";
	private static final String	DEBUG_TAG_TTS		= "mytts";
	private List<String>		walkingInstructionsList;
	private List<String>		walkingInstructionsListBack;
	private TextToSpeech		textToSpeech;
	private TextView			textViewStationEndValue;
	private TextView			textViewStationStartValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textViewStationEndValue = (TextView) findViewById(R.id.textViewStationEndValue);
		textViewStationStartValue = (TextView) findViewById(R.id.textViewStationStartValue);

	}

	@Override
	public void onResume() {
		super.onResume();

		textToSpeech = new TextToSpeech(this, new OnInitListener() {

			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					textToSpeech.setLanguage(Locale.US);
					Log.d(DEBUG_TAG_TTS, "textToSpeech language set to US @MainActivity");
				} else {
					Log.d(DEBUG_TAG_TTS, "Failure in onInit of TextToSpeech @MainActivity! Status code:" + String.valueOf(status));
				}
			}
		});
	}

	@Override
	// Snippet von HSR zum einlesen vom QR-Code
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add("Log");
		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				log(null);
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
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
		if (resultCode == RESULT_OK) {
			if (requestCode == DETECT_TURN || requestCode == DETECT_WALK) {
				walkingInstructionsList.remove(0);
				Log.d(DEBUG_TAG, "walkingInstructionsList in onActivityResult:" + walkingInstructionsList);
			}
			switch (requestCode) {
			// station start
				case SCAN_QR_CODE_START:
					Log.d(DEBUG_TAG, "SCAN_QR_CODE_START");
					String jsonQrCodeStart = intent.getStringExtra("SCAN_RESULT");
					Log.d(DEBUG_TAG, "qrCodeStart:" + jsonQrCodeStart);
					startWalking(jsonQrCodeStart);

					break;
				// station end
				case SCAN_QR_CODE_END:
					Log.d(DEBUG_TAG, "SCAN_QR_CODE_END");
					String jsonQrCodeEnd = intent.getStringExtra("SCAN_RESULT");
					Log.d(DEBUG_TAG, "qrCodeEnd:" + jsonQrCodeEnd);
					endWalking(jsonQrCodeEnd);

					break;
				// walk
				case DETECT_WALK:
					Log.d(DEBUG_TAG, "DETECT_WALK");
					Log.d(DEBUG_TAG, "walk detected");
					gotoNextInstruction();

					break;
				// turn
				case DETECT_TURN:
					Log.d(DEBUG_TAG, "DETECT_TURN");
					Log.d(DEBUG_TAG, "turn detected");
					gotoNextInstruction();

					break;
				default:
					Log.d(DEBUG_TAG, "requestCode didn't match any checked codes");
					break;
			}
		}
	}

	public void start(View view) {

		 readQrCodeStart(null);
//		String jsonQrCodeStart = "{\"input\":[\"links\",\"2\"], \"startStation\" :1}";
		// String jsonQrCodeStart =
		// "{\"input\":[\"10\",\"links\",\"15\",\"rechts\",\"20\",\"links\",\"25\"], \"startStation\" :1}";
//		startWalking(jsonQrCodeStart);
	}

	public void resume(View view) {
		if (walkingInstructionsListBack != null) {
			gotoNextInstruction();
		} else {
			alertNoInstructions();
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

	private void startWalking(String jsonQrCodeStart) {

		walkingInstructionsList = new LinkedList<>();
		int stationStart;

		try {
			JSONObject jsonStart = new JSONObject(jsonQrCodeStart);
			JSONArray walkingInstructionsJson = jsonStart.getJSONArray("input");

			for (int i = 0; i < walkingInstructionsJson.length(); i++) {
				walkingInstructionsList.add(walkingInstructionsJson.get(i).toString());
			}
			Log.d(DEBUG_TAG, "walkingInstructionsList:" + walkingInstructionsList);

			stationStart = jsonStart.getInt("startStation");
			Log.d(DEBUG_TAG, "startStation:" + String.valueOf(stationStart));
			textViewStationStartValue.setText(String.valueOf(stationStart));

			walkingInstructionsListBack = new LinkedList<>(walkingInstructionsList);

			// start reading instructions
			gotoNextInstruction();
		} catch (JSONException jsone) {
			Log.d(DEBUG_TAG, "JSONException occured!");
			jsone.printStackTrace();
		}
	}

	private void endWalking(String jsonQrCodeEnd) {

		int stationEnd;
		int stationStart;

		try {
			JSONObject jsonEnd = new JSONObject(jsonQrCodeEnd);
			stationEnd = jsonEnd.getInt("endStation");
			Log.d(DEBUG_TAG, "stationEnd:" + String.valueOf(stationEnd));
			textViewStationEndValue.setText(String.valueOf(stationEnd));

			stationStart = Integer.parseInt(textViewStationStartValue.getText().toString());
			jsonEnd.put("startStation", stationStart);

			log(jsonEnd.toString());
		} catch (JSONException jsone) {
			Log.d(DEBUG_TAG, "JSONException occured!");
			jsone.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	private boolean gotoNextInstruction() {
		updateInstructionTable();
		if (walkingInstructionsList.size() > 0) {

			String instruction = walkingInstructionsList.get(0);
			Integer steps = null;
			Direction direction = null;

			try {
				direction = Direction.fromString(instruction);
				Log.d(DEBUG_TAG, "turn direction:" + direction);
				startActivityTurn(null, direction);
			} catch (IllegalArgumentException iae) {
				Log.d(DEBUG_TAG, "could not parse instruction to direction!");
				try {
					steps = Integer.parseInt(instruction);
					Log.d(DEBUG_TAG, "steps to walk:" + steps);
					startActivityWalk(null, steps);
				} catch (NumberFormatException nfe) {
					Log.d(DEBUG_TAG, "could not parse instruction to integer!");
					return false;
				}
			}
			return true;
		} else {
			textToSpeech.speak("You have reached your destination.", TextToSpeech.QUEUE_FLUSH, null);
			readQrCodeEnd(null);
			return true;
		}
	}

	public void updateInstructionTable() {
		TableLayout tableLayoutInstructions = (TableLayout) findViewById(R.id.tableLayoutInstructions);

		// clear table
		tableLayoutInstructions.removeAllViews();

		LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

		for (int i = 0; i < walkingInstructionsListBack.size(); i++) {
			String instruction = walkingInstructionsListBack.get(i);
			// table row
			TableRow tableRow = new TableRow(this);
			if ((walkingInstructionsListBack.size() - walkingInstructionsList.size()) > i) {
				tableRow.setBackgroundResource(R.drawable.row_border_solid);
			} else {
				tableRow.setBackgroundResource(R.drawable.row_border);
			}

			// text view
			TextView textView = new TextView(this);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
			textView.setText(instruction);

			// add text view to table row
			tableRow.addView(textView);
			tableRow.setGravity(Gravity.CENTER);

			// add table row to table layout
			tableLayoutInstructions.addView(tableRow, layoutParams);
		}
	}

	public void startActivityTurn(View view, Direction direction) {
		Log.d(DEBUG_TAG, "startActivityTurn called");
		Intent intentTurn = new Intent(this, TurnActivity.class);
		intentTurn.putExtra("ch.fenceposts.schrittzaehler.turn.direction", direction.getDirection());
		startActivityForResult(intentTurn, DETECT_TURN);
	}

	public void startActivityWalk(View view, int steps) {
		Log.d(DEBUG_TAG, "startActivityWalk called");
		Intent intentWalk = new Intent(this, WalkActivity.class);
		intentWalk.putExtra("ch.fenceposts.schrittzaehler.walk.steps", steps);
		startActivityForResult(intentWalk, DETECT_WALK);
	}

	private void alertNoInstructions() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = getResources().getString(R.string.alert_no_instructions);
		builder.setMessage(text);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void alertNoStations() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = getResources().getString(R.string.alert_no_stations);
		builder.setMessage(text);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void log(String jsonString) {
		Intent intent = new Intent("ch.appquest.intent.LOG");

		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}

		intent.putExtra("ch.appquest.taskname", "Schrittzaehler");
		if (jsonString == null) {
			JSONObject json = new JSONObject();
			try {
				json.put("endStation", Integer.valueOf(textViewStationEndValue.getText().toString()));
				json.put("startStation", Integer.valueOf(textViewStationStartValue.getText().toString()));

				jsonString = json.toString();
			} catch (JSONException jsone) {
				Log.d(DEBUG_TAG, "JSONException occured while logging wihtout given json string!");
			} catch (NumberFormatException nfe) {
				alertNoStations();
				return;
			}
		}
		Log.d(DEBUG_TAG, "log json string:" + jsonString);

		// Achtung, je nach App wird etwas anderes eingetragen (siehe Tabelle ganz unten):
		intent.putExtra("ch.appquest.logmessage", jsonString);
		startActivity(intent);
	}
}

package at.lnu.ass3.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.lnu.ass3.R;

public class CityForm extends Activity {
	private AlertDialog alertDate;
	private AlertDialog alertName;
	public static final String INTENT_RESULT_NAME = "result_name";
	public static final String INTENT_RESULT_STATE = "result_state";
	public static final String INTENT_RESULT_COUNTRY = "result_country";
	
	private static final String TAG = CityForm.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_city_form);

		/* Assign listener to button */
		Button button = (Button) findViewById(R.id.mycountry_done_button);
		button.setOnClickListener(new ButtonClick());

		createAlerts();
	}

	private void createAlerts() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.mycountries_date_alert);
		builder.setCancelable(false);
		builder.setPositiveButton("Done", new DialogDone());
		alertDate = builder.create();

		builder.setMessage(R.string.mycountries_name_alert);
		alertName = builder.create();
	}

	private class DialogDone implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			dialog.dismiss();
		}
	}

	private class ButtonClick implements View.OnClickListener {
		public void onClick(View v) {

			EditText nameReader = (EditText) findViewById(R.id.weather_city_name_reader);
			String name = nameReader.getText().toString();

			if (!name.matches("[a-zA-Z]+")) {
				Log.d(TAG, "city name has wrong format");
				alertName.show();
				return;
			}
			Log.d(TAG,"name ok!");

			EditText stateReader = (EditText) findViewById(R.id.weather_city_state_reader);
			String state = stateReader.getText().toString();

			if (!state.matches("[a-zA-Z]+")) {
				Log.d(TAG, "state name has wrong format");
				alertName.show();
				return;
			}
			Log.d(TAG,"state name ok!");
			
			
			EditText countryReader = (EditText) findViewById(R.id.weather_city_country_reader);
			String country = countryReader.getText().toString();

			if (!country.matches("[a-zA-Z]+")) {
				Log.d(TAG, "country name has wrong format");
				alertName.show();
				return;
			}
			Log.d(TAG,"country name ok!");

			/* Create new result intent */
			Intent reply = new Intent();
			reply.putExtra(INTENT_RESULT_NAME, name);
			reply.putExtra(INTENT_RESULT_STATE, state);
			reply.putExtra(INTENT_RESULT_COUNTRY, country);
			setResult(RESULT_OK, reply);
			finish();
		}
	}
}

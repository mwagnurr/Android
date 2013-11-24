package at.lnu.ass2.mycountries;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.lnu.ass2.R;

public class MyCountriesForm extends Activity {
	private static final String TAG = MyCountriesForm.class.getSimpleName();

	private AlertDialog alertDate;
	private AlertDialog alertName;
	public static final String INTENT_START_CV = "start_cv";
	public static final String INTENT_RESULT_NAME = "result_name";
	public static final String INTENT_RESULT_YEAR = "result_year";
	public static final String INTENT_RESULT_EDITCV = "result_cv";

	private CountryVisit editingCV;
	private boolean editing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycountries_form);

		/* Assign listener to button */
		Button button = (Button) findViewById(R.id.mycountry_done_button);
		button.setOnClickListener(new ButtonClick());

		createAlerts();
		editingCV = (CountryVisit) getIntent().getSerializableExtra(INTENT_START_CV);
		if (editingCV != null) {
			Log.d(TAG, "received CountryVisit with starting intent -> process it as editing");
			editing = true;

			// update EditText fields with the already existing Strings
			EditText name = (EditText) findViewById(R.id.mycountry_name_reader);
			name.setText(editingCV.getName());

			EditText year = (EditText) findViewById(R.id.mycountry_date_reader);
			year.setText(Integer.toString(editingCV.getYear()));

		} else {
			Log.d(TAG, "starting intent had no CountryVisit -> process it as new form");
			editing = false;

		}

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

			EditText nameReader = (EditText) findViewById(R.id.mycountry_name_reader);
			String name = nameReader.getText().toString();

			if (!name.matches("[a-zA-Z]+") || name.length() >= 20) {
				Log.d(TAG, "country name has wrong format");
				alertName.show();
				return;
			}
			Log.d(TAG, "name ok!");

			EditText dateReader = (EditText) findViewById(R.id.mycountry_date_reader);
			String yearString = dateReader.getText().toString();
			Integer year = null;
			try {
				year = Integer.parseInt(yearString);
			} catch (NumberFormatException ne) {
				Log.d(TAG, "date has wrong format");
				alertDate.show();
				ne.printStackTrace();
				return;
			}
			Log.d(TAG, "year ok!");

			/* Create new result intent */
			Intent reply = new Intent();

			if (editingCV != null && editing == true) { // if editing
				editingCV.setName(name);
				editingCV.setYear(year);

				reply.putExtra(INTENT_RESULT_EDITCV, editingCV);
				Log.d(TAG, "putting result extra editingCV: " + editingCV.getName());

			} else { // if new CountryVisit
				reply.putExtra(INTENT_RESULT_NAME, name);
				reply.putExtra(INTENT_RESULT_YEAR, year);
				Log.d(TAG, "putting result extra Strings for new CV: " + name + "/" + year);
			}

			setResult(RESULT_OK, reply);
			finish();
		}
	}
}

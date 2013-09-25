package at.lnu.ass2.mycountries;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.lnu.ass2.R;

public class MyCountriesForm extends Activity {
	private AlertDialog alertDate;
	private AlertDialog alertName;
	public static final String INTENT_RESULT_NAME = "result_name";
	public static final String INTENT_RESULT_YEAR = "result_year";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycountries_form);

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

			EditText nameReader = (EditText) findViewById(R.id.mycountry_name_reader);
			String name = nameReader.getText().toString();

			if (!name.matches("[a-zA-Z]+")) {
				System.out.println("country name has wrong format");
				alertName.show();
				return;
			}
			System.out.println("name ok!");

			EditText dateReader = (EditText) findViewById(R.id.mycountry_date_reader);
			String yearString = dateReader.getText().toString();
			Integer year = null;
			try {
				year = Integer.parseInt(yearString);
			} catch (NumberFormatException ne) {
				System.out.println("date has wrong format");
				alertDate.show();
				ne.printStackTrace();
				return;
			}
			System.out.println("year ok!");

			/* Create new result intent */
			Intent reply = new Intent();
			reply.putExtra(INTENT_RESULT_NAME, name);
			reply.putExtra(INTENT_RESULT_YEAR, year);
			setResult(RESULT_OK, reply);
			System.out.println("form - send input: " + year + " " + name);
			finish();
		}
	}
}

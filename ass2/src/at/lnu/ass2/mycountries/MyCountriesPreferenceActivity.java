package at.lnu.ass2.mycountries;

import java.util.List;
import android.preference.PreferenceActivity;
import at.lnu.ass2.R;

public class MyCountriesPreferenceActivity extends PreferenceActivity {
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}
}

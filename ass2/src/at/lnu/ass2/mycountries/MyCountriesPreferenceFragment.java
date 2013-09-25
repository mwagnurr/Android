package at.lnu.ass2.mycountries;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import at.lnu.ass2.R;

public class MyCountriesPreferenceFragment extends PreferenceFragment{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mycountries_prefs);
	}
}

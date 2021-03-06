package at.lnu.ass3.weather;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class CityEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String country;
	private String state;
	private URL url;

	public CityEntity(String country, String state, String name) {
		super();
		this.name = name;
		this.country = country;
		this.state = state;
		generateURL(country, state, name);
	}

	/**
	 * constructs an city entity from a String in the format: country name/state name/city name
	 * 
	 * @param fullCityName
	 */
	public CityEntity(String fullCityName) {

		String[] strings = fullCityName.split("/");

		if (fullCityName == null || strings == null || strings.length != 3) {
			System.err.println("error, fullCityName has wrong format");
			return;
		}
		this.country = strings[0];
		this.state = strings[1];
		this.name = strings[2];
		generateURL(country, state, name);
	}

	/**
	 * @param country
	 * @param state
	 * @param name
	 */
	private void generateURL(String country, String state, String name) {
		try {

			String nameEncoded = URLEncoder.encode(name, "utf-8");
			String stateEncoded = URLEncoder.encode(state, "utf-8");
			String countryEncoded = URLEncoder.encode(country, "utf-8");
			// String url = "http://stackoverflow.com/search?q=" + query;

			String url = "http://www.yr.no/sted/" + countryEncoded + "/" + stateEncoded + "/"
					+ nameEncoded + "/forecast.xml";

			this.url = new URL(url);
			// this.url = new URL(
			// "http://www.yr.no/sted/Sverige/Kronoberg/V%E4xj%F6/forecast.xml");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns all the String fields defining a city entity in the format: country name/state name
	 * /city name
	 * 
	 * @return
	 */
	public String getFullCityName() {
		return country + "/" + state + "/" + name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return name;
	}

}

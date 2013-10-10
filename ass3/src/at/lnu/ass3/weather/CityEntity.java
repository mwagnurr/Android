package at.lnu.ass3.weather;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class CityEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String country;
	private String state;
	private URL url;

	public CityEntity(String name, String country, String state) {
		super();
		this.name = name;
		this.country = country;
		this.state = state;
		try {
			
			String query = URLEncoder.encode("apples oranges", "utf-8");
			String url = "http://stackoverflow.com/search?q=" + query;
			
			this.url = new URL(
					"http://www.yr.no/sted/Sverige/Kronoberg/V%E4xj%F6/forecast.xml");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		return name + "/" + state + "/" + country;
	}

}

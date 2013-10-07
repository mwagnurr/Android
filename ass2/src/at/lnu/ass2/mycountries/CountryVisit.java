package at.lnu.ass2.mycountries;


public class CountryVisit{

	private long id;
	private int year;
	private String name;

	public CountryVisit(long id, String name, int year) {
		this.id = id;
		this.year = year;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toPresentableString(){
		return name + ", " + year;
	}
	
	public String toString(){
		return "[" + id +", " + name + ", " + year + "]";
	}

}

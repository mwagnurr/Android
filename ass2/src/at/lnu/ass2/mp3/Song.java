package at.lnu.ass2.mp3;

public class Song {
	private String artist;
	private String album;
	private String title;
	private String path;
	private long duration;
	private int trackNr;
	private int year;

	public Song() {

	}

	public Song(String artist, String album, String title, String path,
			long duration, int trackNr, int year) {
		super();
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.path = path;
		this.duration = duration;
		this.trackNr = trackNr;
		this.year = year;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getTrackNr() {
		return trackNr;
	}

	public void setTrackNr(int trackNr) {
		this.trackNr = trackNr;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	@Override
	public String toString() {
		return "Song [artist=" + artist + ", album=" + album + ", title="
				+ title + ", path=" + path + ", duration=" + duration
				+ ", trackNr=" + trackNr + ", year=" + year + "]";
	}

}

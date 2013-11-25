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

	public Song(String artist, String album, String title, String path, long duration, int trackNr,
			int year) {
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
		return "Song [artist=" + artist + ", album=" + album + ", title=" + title + ", path="
				+ path + ", duration=" + duration + ", trackNr=" + trackNr + ", year=" + year + "]";
	}


	/**
	 * auto generated equals method to compare entities
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Song other = (Song) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (artist == null) {
			if (other.artist != null)
				return false;
		} else if (!artist.equals(other.artist))
			return false;
		if (duration != other.duration)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (trackNr != other.trackNr)
			return false;
		if (year != other.year)
			return false;
		return true;
	}

}

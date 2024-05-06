import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Recording {
	private final String artist;
	private final String title;
	private final int year;
	private final String type;
	private final SortedSet<String> genre = new TreeSet<>();

	public Recording(String title, String artist, int year, String type, Set<String> genre) {
		this.artist = artist;
		this.title = title;
		this.year = year;
		this.type = type;
		this.genre.addAll(genre);
	}

	@Override
	public String toString() {
		return "Recording{" +
				"artist='" + artist + '\'' +
				", title='" + title + '\'' +
				", year=" + year +
				", type='" + type + '\'' +
				'}';
	}

	public String getArtist() {
		return artist;
	}

	public String getGenre() {
		return String.valueOf(genre);
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public int getYear() {
		return year;
	}
}

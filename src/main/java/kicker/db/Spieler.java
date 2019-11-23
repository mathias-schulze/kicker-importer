package kicker.db;

public class Spieler implements Comparable<Spieler> {
	
	private String id;
	private String name;
	
	public Spieler(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Spieler spieler) {
		return this.name.compareTo(spieler.name);
	}
}

package kicker.exporter.json;

import java.util.Map;
import java.util.TreeMap;

public class Team {
	
	private Long id;
	private Map<String, Spieler> spieler = new TreeMap<>();
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Map<String, Spieler> getSpieler() {
		return spieler;
	}
	
	public void setSpieler(Map<String, Spieler> spieler) {
		this.spieler = spieler;
	}
	
	public void addSpieler(Spieler spieler) {
		this.spieler.put(spieler.getName(), spieler);
	}
}
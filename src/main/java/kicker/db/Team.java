package kicker.db;

import java.util.ArrayList;
import java.util.List;

public class Team {
	
	private String id;
	private Long kickerCoolId;
	private List<Spieler> spieler = new ArrayList<>();
	
	public Team(String id, Long kickerCoolId) {
		this.id = id;
		this.kickerCoolId = kickerCoolId;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Long getKickerCoolId() {
		return kickerCoolId;
	}
	
	public void setKickerCoolId(Long kickerCoolId) {
		this.kickerCoolId = kickerCoolId;
	}
	
	public List<Spieler> getSpieler() {
		return spieler;
	}
	
	public void setSpieler(List<Spieler> spieler) {
		this.spieler = spieler;
	}
	
	public void addSpieler(Spieler spieler) {
		this.spieler.add(spieler);
	}
}

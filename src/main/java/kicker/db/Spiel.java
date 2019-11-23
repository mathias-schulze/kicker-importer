package kicker.db;

import java.util.Date;

public class Spiel implements Comparable<Spiel> {
	
	private String id;
	private Long kickerCoolId;
	private Date datum;
	
	private Team sieger;
	private Team verlierer;
	
	private Long toreSieger;
	private Long toreVerlierer;
	
	private Boolean krabbeln;
	
	public Spiel(String id, Long kickerCoolId, Date datum) {
		this.id = id;
		this.kickerCoolId = kickerCoolId;
		this.datum = datum;
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
	
	public Date getDatum() {
		return datum;
	}
	
	public void setDatum(Date datum) {
		this.datum = datum;
	}
	
	public void setErgebnis(Team sieger, Team verlierer, Long toreSieger, Long toreVerlierer, Boolean krabbeln) {
		this.sieger = sieger;
		this.verlierer = verlierer;
		this.toreSieger = toreSieger;
		this.toreVerlierer = toreVerlierer;
		this.krabbeln = krabbeln;
	}
	
	public Team getSieger() {
		return sieger;
	}
	
	public void setSieger(Team sieger) {
		this.sieger = sieger;
	}
	
	public Team getVerlierer() {
		return verlierer;
	}
	
	public void setVerlierer(Team verlierer) {
		this.verlierer = verlierer;
	}
	
	public Long getToreSieger() {
		return toreSieger;
	}
	
	public void setToreSieger(Long toreSieger) {
		this.toreSieger = toreSieger;
	}
	
	public Long getToreVerlierer() {
		return toreVerlierer;
	}
	
	public void setToreVerlierer(Long toreVerlierer) {
		this.toreVerlierer = toreVerlierer;
	}
	
	public Boolean getKrabbeln() {
		return krabbeln;
	}
	
	public void setKrabbeln(Boolean krabbeln) {
		this.krabbeln = krabbeln;
	}
	
	@Override
	public int compareTo(Spiel spiel) {
		return this.kickerCoolId.compareTo(spiel.kickerCoolId);
	}
}

package kicker.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import kicker.db.Spiel;
import kicker.db.Spieler;
import kicker.db.Team;
import kicker.exporter.json.Daten;

public class KickerImporter {
	
	private static final DateFormat DF_DB = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat DF_VIEW = new SimpleDateFormat("dd.MM.yyyy");
	
	private void importData(String firestorConf) throws IOException {
		
		Daten daten = readJson("src\\main\\resources\\kicker.json");
		Firestore db = getFirestore(firestorConf);
		
		Map<String, Spieler> spielerById = getSpieler(db);
		Map<String, Spieler> spielerByName = spielerById.values().stream()
				.collect(Collectors.toMap(Spieler::getName, Function.identity()));
		importSpieler(db, daten, spielerByName);
		spielerById = getSpieler(db);
		spielerByName = spielerById.values().stream()
				.collect(Collectors.toMap(Spieler::getName, Function.identity()));
		
		Map<String, Team> teamsById = getTeams(db, spielerById);
		Map<Long, Team> teamsByKickerCoolId = teamsById.values().stream()
				.collect(Collectors.toMap(Team::getKickerCoolId, Function.identity()));
		importTeams(db, daten, spielerByName, teamsByKickerCoolId);
		teamsById = getTeams(db, spielerById);
		teamsByKickerCoolId = teamsById.values().stream()
				.collect(Collectors.toMap(Team::getKickerCoolId, Function.identity()));
		
		Map<String, Spiel> spieleById = getSpiele(db, teamsById);
		Map<Long, Spiel> spieleByKickerCoolId = spieleById.values().stream()
				.collect(Collectors.toMap(Spiel::getKickerCoolId, Function.identity()));
		importSpiele(db, daten, spieleByKickerCoolId, teamsByKickerCoolId);
		spieleById = getSpiele(db, teamsById);
		spieleByKickerCoolId = spieleById.values().stream()
				.collect(Collectors.toMap(Spiel::getKickerCoolId, Function.identity()));
		
		printSpieler(spielerById);
		printTeams(teamsById);
		printSpiele(spieleById);
		
		System.out.println("Anzahl Spieler: " + spielerById.size());
		System.out.println("Anzahl Teams  : " + teamsById.size());
		System.out.println("Anzahl Spiele : " + spieleById.size());
	}
	
	private Daten readJson(String filename) throws IOException {
		
		File file = new File(filename);
		ObjectMapper om = new ObjectMapper();
		return om.readValue(file, Daten.class);
	}
	
	private Firestore getFirestore(String firestorConf) throws IOException {
		InputStream serviceAccount = new FileInputStream("src\\\\main\\\\resources\\\\" + firestorConf);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		FirebaseOptions options = new FirebaseOptions.Builder()
		    .setCredentials(credentials)
		    .build();
		FirebaseApp.initializeApp(options);

		return FirestoreClient.getFirestore();
	}
	
	private void importSpieler(Firestore db, Daten daten, Map<String, Spieler> spielerByName) {
		
		CollectionReference spielerCollection = db.collection("spieler");
		
		daten.getSpieler().values().forEach(s -> {
			if (!spielerByName.containsKey(s.getName())) {
				DocumentReference spielerRef = spielerCollection.document();
				Map<String, Object> data = new HashMap<>();
				data.put("name", s.getName());
				ApiFuture<WriteResult> result = spielerRef.set(data);
				try {
					result.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void importTeams(Firestore db, Daten daten, Map<String, Spieler> spielerByName, 
			Map<Long, Team> teamsByKickerCoolId) {
		
		CollectionReference teamCollection = db.collection("teams");
		
		daten.getTeams().values().forEach(t -> {
			if (!teamsByKickerCoolId.containsKey(t.getId())) {
				DocumentReference teamRef = teamCollection.document();
				Map<String, Object> dataTeam = new HashMap<>();
				dataTeam.put("id", teamRef.getId());
				dataTeam.put("kickerCoolId", t.getId());
				ApiFuture<WriteResult> resultTeam = teamRef.set(dataTeam);
				try {
					resultTeam.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				
				CollectionReference spielerCollection = teamRef.collection("spieler");
				t.getSpieler().values().forEach(s -> {
					Spieler dbSpieler = spielerByName.get(s.getName());
					DocumentReference spielerRef = spielerCollection.document(dbSpieler.getId());
					Map<String, Object> dataSpieler = new HashMap<>();
					dataSpieler.put("id", spielerRef.getId());
					ApiFuture<WriteResult> resultSpieler = spielerRef.set(dataSpieler);
					try {
						resultSpieler.get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				});
			}
		});
	}
	
	private void importSpiele(Firestore db, Daten daten, Map<Long, Spiel> spieleByKickerCoolId, 
			Map<Long, Team> teamsByKickerCoolId) {
		
		CollectionReference spielCollection = db.collection("spiele");
		
		daten.getSpiele().forEach(s -> {
			if (!spieleByKickerCoolId.containsKey(s.getId())) {
				DocumentReference spielRef = spielCollection.document();
				Map<String, Object> dataSpiel = new HashMap<>();
				dataSpiel.put("id", spielRef.getId());
				dataSpiel.put("kickerCoolId", s.getId());
				try {
					dataSpiel.put("datum", DF_DB.parseObject(s.getDatum()));
				} catch (ParseException pe) {
					pe.printStackTrace();
				}
				dataSpiel.put("sieger", teamsByKickerCoolId.get(s.getSieger().getId()).getId());
				dataSpiel.put("verlierer", teamsByKickerCoolId.get(s.getVerlierer().getId()).getId());
				dataSpiel.put("toreSieger", s.getToreSieger());
				dataSpiel.put("toreVerlierer", s.getToreVerlierer());
				dataSpiel.put("krabbeln", s.getToreVerlierer() == 0);
				ApiFuture<WriteResult> resultSpiel = spielRef.set(dataSpiel);
				try {
					resultSpiel.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private Map<String, Spieler> getSpieler(Firestore db) {
		
		Map<String, Spieler> spieler = new HashMap<>();
		
		ApiFuture<QuerySnapshot> query = db.collection("spieler").get();
		try {
			QuerySnapshot querySnapshot = query.get();
			List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
			for (QueryDocumentSnapshot document : documents) {
				Spieler s = new Spieler(document.getId(), document.getString("name"));
				spieler.put(s.getId(), s);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return spieler;
	}
	
	private Map<String, Team> getTeams(Firestore db, Map<String, Spieler> spielerById) {
		
		Map<String, Team> teams = new HashMap<>();
		
		CollectionReference teamCollection = db.collection("teams");
		ApiFuture<QuerySnapshot> query = teamCollection.get();
		try {
			QuerySnapshot querySnapshot = query.get();
			List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
			for (QueryDocumentSnapshot document : documents) {
				Team team = new Team(document.getId(), document.getLong("kickerCoolId"));
				teams.put(team.getId(), team);
				DocumentReference teamRef = teamCollection.document(team.getId());
				teamRef.listCollections().forEach(c -> {
					String id = c.getId();
					if (id.equals("spieler")) {
						ApiFuture<QuerySnapshot> querySpieler = c.get();
						try {
							QuerySnapshot querySnapshotSpieler = querySpieler.get();
							List<QueryDocumentSnapshot> documentsSpieler = querySnapshotSpieler.getDocuments();
							for (QueryDocumentSnapshot documentSpieler : documentsSpieler) {
								team.addSpieler(spielerById.get(documentSpieler.getId()));
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return teams;
	}
	
	private Map<String, Spiel> getSpiele(Firestore db, Map<String, Team> teamsById) {
		
		Map<String, Spiel> spiele = new HashMap<>();
		
		ApiFuture<QuerySnapshot> query = db.collection("spiele").get();
		try {
			QuerySnapshot querySnapshot = query.get();
			List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
			for (QueryDocumentSnapshot document : documents) {
				Spiel spiel = new Spiel(document.getId(), document.getLong("kickerCoolId"), document.getDate("datum"));
				spiel.setErgebnis(teamsById.get(document.getString("sieger")), 
						teamsById.get(document.getString("verlierer")), 
						document.getLong("toreSieger"), 
						document.getLong("toreVerlierer"), 
						document.getBoolean("krabbeln"));
				spiele.put(spiel.getId(), spiel);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return spiele;
	}
	
	private void printSpieler(Map<String, Spieler> spieler) {
		
		List<Spieler> spielerListe = new ArrayList<Spieler>(spieler.values());
		Collections.sort(spielerListe);
		
		spielerListe.forEach(s -> {
			System.out.println(s.getName());
		});
	}
	
	private void printTeams(Map<String, Team> teams) {
		
		teams.values().forEach(t -> {
			List<Spieler> spieler = t.getSpieler();
			System.out.println(spieler.get(0).getName() + " - " + spieler.get(1).getName());
		});
	}
	
	private void printSpiele(Map<String, Spiel> spiele) {
		
		List<Spiel> spieleListe = new ArrayList<Spiel>(spiele.values());
		Collections.sort(spieleListe);
		
		spieleListe.forEach(s -> {
			List<Spieler> sieger = s.getSieger().getSpieler();
			List<Spieler> verlierer = s.getVerlierer().getSpieler();
			System.out.println(DF_VIEW.format(s.getDatum()) + ": "
					+ sieger.get(0).getName() + " " + sieger.get(1).getName() + " "
					+ s.getToreSieger() + " : " + s.getToreVerlierer() + " "
					+ verlierer.get(0).getName() + " " + verlierer.get(1).getName());
		});
	}
	
	public static void main(String[] args) throws IOException  {
		(new KickerImporter()).importData(args[0]);
	}
}

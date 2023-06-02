package org.example;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {
    public static Firestore db;
    public static Storage storage;
    public static void initialize() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream(new File("").getAbsolutePath() + "/src/main/resources/Utilities/melozone-7db34-firebase-adminsdk-kuyso-b2b06fbe51.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
        db = FirestoreClient.getFirestore();
        storage = StorageOptions.newBuilder().setProjectId("melozone-7db34").build().getService();
    }
    public static <DocumentReference> void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Scanner sc= new Scanner(System.in);
        initialize();
        System.out.println("Provide artist id");
        String artistId=sc.nextLine();
        System.out.println("Provide album name");
        String albumName=sc.nextLine();
        Map<String, Object> albumData = new HashMap<>();
        albumData.put("ArtistId", artistId);
        albumData.put("Name", albumName);
        String albumId = db.collection("Albums").add(albumData).get().getId();
        File coverDirectory = Files.createDirectories(Paths.get("src/main/resources/Covers")).toFile();
        File[] covers = coverDirectory.listFiles();
        if (covers != null) {
            for (File file : covers) {
                BlobId coverBlobID = BlobId.of("melozone-7db34.appspot.com", albumId+".jpg");
                BlobInfo coverBlobInfo = BlobInfo.newBuilder(coverBlobID).build();
                storage.createFrom(coverBlobInfo, file.toPath());
            }
        }
        File songsDirectory = Files.createDirectories(Paths.get("src/main/resources/Songs")).toFile();
        File[] songs = songsDirectory.listFiles();
        Arrays.sort(songs);
        ArrayList<String> songNames = new ArrayList<>();
        for(File file: songs){
            System.out.println("Provide the name of song: " + file.getName());
            String songName=sc.nextLine();
            songNames.add(songName);
        }
        int pos=0;
        if (songs != null) {
            for (File file : songs) {
                String songName= songNames.get(pos);
                System.out.println("Uploading song "+pos+"/"+songNames.size()+" "+songName);
                Map<String, Object> songData = new HashMap<>();
                songData.put("ArtistId", artistId);
                songData.put("AlbumId", albumId);
                songData.put("Name", songName);
                songData.put("Position", pos);
                String songId = db.collection("Songs").add(songData).get().getId();
                BlobId songBlobId = BlobId.of("melozone-7db34.appspot.com", songId+".mp3");
                BlobInfo songBlobInfo = BlobInfo.newBuilder(songBlobId).build();
                storage.createFrom(songBlobInfo, file.toPath());
                pos++;
            }
        }
    }
}
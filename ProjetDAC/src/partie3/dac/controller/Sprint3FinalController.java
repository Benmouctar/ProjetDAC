package partie3.dac.controller;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import partie3.dac.model.Sprint3GestionnaireAnalyseur;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Sprint3FinalController {

    @FXML private TableView<FichierImporte> tableFichiers;
    @FXML private TableColumn<FichierImporte, String> colNomFichier;
    @FXML private TableColumn<FichierImporte, Long> colTailleFichier;
    @FXML private TableColumn<FichierImporte, String> colCheminFichier;

    @FXML private TextArea zoneContenu;
    @FXML private TextArea zoneResultat;
    @FXML private Label labelStatut;
    @FXML private Label labelTemps; 
    @FXML private ProgressBar barreProgression;

    @FXML private Button btnImporter;
    @FXML private Button btnAfficher;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnalyser;
    @FXML private Button btnAnalyserTous;
    @FXML private Button btnVider;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnQuitter;

    private final ObservableList<FichierImporte> fichiersImportes = FXCollections.observableArrayList();
    private final Sprint3GestionnaireAnalyseur gestionnaire = new Sprint3GestionnaireAnalyseur();

    @FXML
    private void initialize() {
        // Sécurité : on vérifie que les colonnes existent avant de les configurer
        if (colNomFichier != null) colNomFichier.setCellValueFactory(cell -> cell.getValue().nomProperty());
        if (colTailleFichier != null) colTailleFichier.setCellValueFactory(cell -> cell.getValue().tailleProperty().asObject());
        if (colCheminFichier != null) colCheminFichier.setCellValueFactory(cell -> cell.getValue().cheminProperty());

        tableFichiers.setItems(fichiersImportes);

        btnAfficher.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAnalyser.setDisable(true);

        tableFichiers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean selected = newSel != null;
            btnAfficher.setDisable(!selected);
            btnSupprimer.setDisable(!selected);
            btnAnalyser.setDisable(!selected);
            if (!selected) zoneContenu.clear();
        });

        labelStatut.setText("Prêt.");
    }

    @FXML
    private void importerFichiers() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importer des fichiers texte");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        List<File> selection = chooser.showOpenMultipleDialog(getStage());
        if (selection != null && !selection.isEmpty()) {
            int added = 0;
            for (File f : selection) {
                boolean exists = fichiersImportes.stream().anyMatch(fi -> fi.getChemin().equals(f.getAbsolutePath()));
                if (!exists) {
                    fichiersImportes.add(new FichierImporte(f.getName(), f.length(), f.getAbsolutePath()));
                    added++;
                }
            }
            labelStatut.setText(added + " fichier(s) importé(s).");
        }
    }

    @FXML
    private void afficherContenuFichier() {
        FichierImporte sel = tableFichiers.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            zoneContenu.setText(Files.readString(new File(sel.getChemin()).toPath()));
            labelStatut.setText("Contenu affiché : " + sel.getNom());
        } catch (IOException e) {
            labelStatut.setText("Erreur lecture.");
        }
    }

    @FXML
    private void analyserFichierSelectionne() {
        FichierImporte sel = tableFichiers.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        long startTime = System.currentTimeMillis();
        analyzeAndShow(sel);
        long endTime = System.currentTimeMillis();
        labelTemps.setText("Temps d'analyse : " + (endTime - startTime) + " ms");
    }

    @FXML
    private void analyserTous() {
        if (fichiersImportes.isEmpty()) return;
        long startTime = System.currentTimeMillis();
        StringBuilder all = new StringBuilder();
        List<File> filesForProgress = new ArrayList<>();
        for (FichierImporte fi : fichiersImportes) {
            all.append(runAnalysisText(fi)).append("\n\n");
            filesForProgress.add(new File(fi.getChemin()));
        }
        zoneResultat.setText(all.toString());
        gestionnaire.analyserFichiers(filesForProgress, null, barreProgression, labelStatut, () -> {
            long endTime = System.currentTimeMillis();
            labelTemps.setText("Temps total : " + (endTime - startTime) + " ms");
            labelStatut.setText("Analyse de tous les fichiers terminée.");
        });
    }

    @FXML
    private void viderZone() {
        zoneContenu.clear();
        zoneResultat.clear();
        barreProgression.setProgress(0);
        labelTemps.setText("Temps : 0 ms");
        labelStatut.setText("Zones vidées.");
    }

    @FXML
    private void sauvegarderResultat() {
        if (zoneResultat.getText() == null || zoneResultat.getText().isBlank()) return;
        FileChooser chooser = new FileChooser();
        File target = chooser.showSaveDialog(getStage());
        if (target != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
                writer.write(zoneResultat.getText());
                labelStatut.setText("Sauvegardé : " + target.getName());
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimerFichier() {
        FichierImporte sel = tableFichiers.getSelectionModel().getSelectedItem();
        if (sel != null) {
            fichiersImportes.remove(sel);
            labelStatut.setText("Fichier supprimé.");
        }
    }

    @FXML
    private void quitterApplication() {
        gestionnaire.shutdown();
        Stage s = getStage();
        if (s != null) s.close();
    }

    private void analyzeAndShow(FichierImporte fi) {
        zoneResultat.setText(runAnalysisText(fi));
        labelStatut.setText("Analyse terminée : " + fi.getNom());
    }

    private String runAnalysisText(FichierImporte fi) {
        try {
            String texte = Files.readString(new File(fi.getChemin()).toPath());
            int nbLignes = texte.isEmpty() ? 0 : texte.split("\r\n|\r|\n").length;
            String cleaned = texte.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}'’-]", " ");
            String[] tokens = cleaned.trim().isEmpty() ? new String[0] : cleaned.trim().split("\\s+");
            
            Map<String, Integer> freq = new HashMap<>();
            for (String t : tokens) {
                String w = t.replaceAll("^'+|'+$", "");
                if (!w.isBlank()) freq.put(w, freq.getOrDefault(w, 0) + 1);
            }

            List<Map.Entry<String,Integer>> top10frequent = freq.entrySet().stream()
                    .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());

            List<Map.Entry<String,Integer>> top5rare = freq.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .limit(5)
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            sb.append("==\n");
            sb.append("   ANALYSE DU FICHIER : ").append(fi.getNom().toUpperCase()).append("\n");
            sb.append("==\n\n");
            sb.append(">>> NOMBRE DE LIGNES     : ").append(nbLignes).append("\n");
            sb.append(">>> NOMBRE DE MOTS       : ").append(tokens.length).append("\n\n");
            sb.append("###\n");
            sb.append("   TOP 10 DES MOTS LES PLUS RÉPÉTÉS\n");
            sb.append("###\n");
            for (Map.Entry<String,Integer> e : top10frequent) {
                sb.append("   [X] ").append(String.format("%-15s", e.getKey().toUpperCase())).append(" : ").append(e.getValue()).append(" fois\n");
            }
            sb.append("\n***\n");
            sb.append("   TOP 5 DES MOTS LES MOINS RÉPÉTÉS\n");
            sb.append("***\n");
            for (Map.Entry<String,Integer> e : top5rare) {
                sb.append("   [-] ").append(String.format("%-15s", e.getKey().toUpperCase())).append(" : ").append(e.getValue()).append(" fois\n");
            }
            sb.append("\n==");
            return sb.toString();
        } catch (IOException e) { return "Erreur d'analyse."; }
    }

    private Stage getStage() { return (Stage) tableFichiers.getScene().getWindow(); }

    public static class FichierImporte {
        private final SimpleStringProperty nom;
        private final SimpleLongProperty taille;
        private final SimpleStringProperty chemin;

        public FichierImporte(String nom, long taille, String chemin) {
            this.nom = new SimpleStringProperty(nom);
            this.taille = new SimpleLongProperty(taille);
            this.chemin = new SimpleStringProperty(chemin);
        }
        public String getNom() { return nom.get(); }
        public String getChemin() { return chemin.get(); }
        public SimpleStringProperty nomProperty() { return nom; }
        public LongProperty tailleProperty() { return taille; }
        public SimpleStringProperty cheminProperty() { return chemin; }
    }
}
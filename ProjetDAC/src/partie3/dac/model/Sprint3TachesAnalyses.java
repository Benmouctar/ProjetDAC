package partie3.dac.model;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Sprint3TachesAnalyses extends Task<Void> {
    
    private final File fichier;
    private final TableView<String> tableResultats;
    private final ProgressBar barre;
    private final Label labelInfo;

    public Sprint3TachesAnalyses(File fichier, TableView<String> tableResultats, ProgressBar barre, Label labelInfo) {
        this.fichier = fichier;
        this.tableResultats = tableResultats;
        this.barre = barre;
        this.labelInfo = labelInfo;
    }

    @Override
    protected Void call() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            int count = 0;
            while ((ligne = br.readLine()) != null) {
                count++;
            }
            final String msg = "Analyse terminée : " + fichier.getName();
            Platform.runLater(() -> labelInfo.setText(msg));
        } catch (Exception e) {
            Platform.runLater(() -> labelInfo.setText("Erreur sur : " + fichier.getName()));
            e.printStackTrace();
        }
        return null;
    }
}
package partie3.dac.model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Sprint3GestionnaireAnalyseur {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void analyserFichier(File fichier, TableView<String> tableResultats, ProgressBar barre, Label labelInfo, Runnable callback) {
        Sprint3TachesAnalyses task = new Sprint3TachesAnalyses(fichier, tableResultats, barre, labelInfo);
        task.setOnSucceeded(e -> { if (callback != null) Platform.runLater(callback); });
        task.setOnFailed(e -> { if (callback != null) Platform.runLater(callback); });
        executor.submit(task);
    }

    public void analyserFichiers(List<File> fichiers, TableView<String> tableResultats, ProgressBar barre, Label labelInfo, Runnable callback) {
        if (fichiers.isEmpty()) return;

        AtomicInteger fichiersTermines = new AtomicInteger(0);
        int total = fichiers.size();
        Platform.runLater(() -> barre.setProgress(0));

        for (File f : fichiers) {
            Sprint3TachesAnalyses task = new Sprint3TachesAnalyses(f, tableResultats, barre, labelInfo) {
                @Override
                protected void succeeded() {
                    super.succeeded();
                    int finis = fichiersTermines.incrementAndGet();
                    Platform.runLater(() -> {
                        barre.setProgress((double) finis / total);
                        if (finis == total && callback != null) callback.run();
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    int finis = fichiersTermines.incrementAndGet();
                    Platform.runLater(() -> {
                        labelInfo.setText("⚠️ Erreur : " + f.getName());
                        barre.setProgress((double) finis / total);
                        if (finis == total && callback != null) callback.run();
                    });
                }
            };
            executor.submit(task);
        }
    }

    public boolean sauvegarderResultats(File fichier, String contenu) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier))) {
            writer.println(contenu);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
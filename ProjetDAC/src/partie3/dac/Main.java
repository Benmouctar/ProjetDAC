package partie3.dac;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.util.Optional;

public class Main extends Application {
    
    // Mon mot de passe 
    private static final String MOT_DE_PASSE_VALIDE = "Mouctar@26MI";

    @Override
    public void start(Stage stage) throws Exception {
        
        //MON DEBUT LA PARTIE SÉCURITÉ
        if (!demanderMotDePasse()) {
            // Si le mot de passe est faux ou annulé, on ferme tout
            System.exit(0);
        }
        //MA FIN DE LA PARTIE SÉCURITÉ

        // Si on arrive ici, c'est que le mot de passe est bon
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/sprint-view.fxml"));
        
        Scene scene = new Scene(fxmlLoader.load());
        
       
        stage.setTitle("Analyseur de Texte - Sprint 3 (Sécurisé)");
        stage.setScene(scene);
        stage.show();
    }

    
    private boolean demanderMotDePasse() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Accès Sécurisé");
        dialog.setHeaderText("Authentification requise");
        dialog.setContentText("Veuillez entrer le mot de passe pour ouvrir l'Analyseur :");

        // Affichons  la boîte et attendons la réponse
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            if (result.get().equals(MOT_DE_PASSE_VALIDE)) {
                return true; // Accès autorisé
            } else {
                // Message d'erreur si mauvais mot de passe
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Mot de passe incorrect");
                alert.setContentText("L'application va se fermer.");
                alert.showAndWait();
                return false;
            }
        }
        return false; // Annulé par l'utilisateur
    }

    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
}
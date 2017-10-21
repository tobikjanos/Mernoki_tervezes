/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticdatabaseupdater;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author TobikJanos
 */
public class AutomaticDatabaseUpdater extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        
        Scene scene = new Scene(root);
        //scene.getStylesheets().add("AutomaticDatabaseUpdate/StyleSheet.css");
        
        stage.setWidth(800);
        stage.setHeight(525);
        stage.setResizable(false);
        stage.sizeToScene();
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

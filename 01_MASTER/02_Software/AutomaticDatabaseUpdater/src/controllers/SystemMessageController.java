/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author TobikJanos
 */
public class SystemMessageController {
   
   /**
    * brief: creates alert box to display information
    * @param title   - title of alert box
    * @param msg     - message of alert box
    */
   public static void DisplayInformationMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(new Image("file:src/Images/logo.png"));
      
      alert.showAndWait();
   }   
      
   /**
    * brief: creates alert box to display warning
    * @param title   - title of alert box
    * @param msg     - message of alert box
    * @return        - user response on buttons
    */
   public static boolean DisplayWarningMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(new Image("file:src/Images/logo.png"));
      
      ButtonType buttonTypeOK = new ButtonType("Folytatás");
      ButtonType buttonTypeCancel = new ButtonType("Vissza");
      
      alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);
      
      Optional<ButtonType> result = alert.showAndWait();
      
      if(result.get() == buttonTypeOK)
         return true;
      else
         return false;
   }
   
   /**
    * brief: creates alert box to display error
    * @param title   - title of alert box
    * @param msg     - message of alert box
    */
   public static void DisplayErrorMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(new Image("file:src/Images/logo.png"));
      
      alert.showAndWait();
   }
   
}

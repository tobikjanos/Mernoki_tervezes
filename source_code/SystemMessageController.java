/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticdatabaseupdate;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

/**
 * FXML Controller class
 *
 * @author Dzsákom
 */
public class SystemMessageController {
  
   
   public static void DisplayInformationMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      alert.showAndWait();
   }
   
   public static void DisplayWarningMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      alert.showAndWait();
   }
   
   
   public static void DisplayErrorMessage(String title, String msg)
   {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(null);
      alert.setHeaderText(title);
      alert.setContentText(msg);
      
      alert.showAndWait();
   }
   
}

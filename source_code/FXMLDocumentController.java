/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdate;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 *
 * @author Dzsákom
 */
public class FXMLDocumentController implements Initializable {
   
   
   /****************************
    *           TAB 1          *
    ****************************/
   
   @FXML
   private TextField t1TextIPaddress, t1TextPort, t1TextDBname, t1TextUsername, t1TextSchema;
   @FXML
   private PasswordField t1Password;
   
   @FXML
   private void MouseClickSaveDatabaseConfig()
   {
//      DatabaseController.ClearDatabaseSetup();
      
      DatabaseController.setIP_ADDRESS(      t1TextIPaddress.getText()  );
      DatabaseController.setPORT(            t1TextPort.getText()       );
      DatabaseController.setDATABASE_NAME(   t1TextDBname.getText()     );
      DatabaseController.setUSERNAME(        t1TextUsername.getText()   );
      DatabaseController.setPASSWORD(        t1Password.getText()       );
      DatabaseController.setSCHEMA(          t1TextSchema.getText()     );
      
      DatabaseController db = new DatabaseController();
      System.out.println(db.toString());
      
   }
   
   @FXML
   private void MouseClickTestDatabaseConnection()
   {
      
      //      DatabaseController.ClearDatabaseSetup();
      
      DatabaseController.setIP_ADDRESS(      t1TextIPaddress.getText()  );
      DatabaseController.setPORT(            t1TextPort.getText()       );
      DatabaseController.setDATABASE_NAME(   t1TextDBname.getText()     );
      DatabaseController.setUSERNAME(        t1TextUsername.getText()   );
      DatabaseController.setPASSWORD(        t1Password.getText()       );
      DatabaseController.setSCHEMA(          t1TextSchema.getText()     );
      
      
      try {
         Connection conn = DatabaseController.Connect();
         if(conn.isValid(7))
         {
            SystemMessageController.DisplayInformationMessage("Adatbázis kapcsolat tesztelése","Teszt sikeres!");
            System.out.println("Connected");
         }
         else
         {
            SystemMessageController.DisplayErrorMessage("Adatbázis kapcsolat tesztelése","Teszt sikertelen!");
            System.out.println("Not connected");
         }
      } catch (Exception ex) {
         SystemMessageController.DisplayErrorMessage("Adatbázis kapcsolat tesztelése","Teszt sikertelen!");
         Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
      }
      
   }
   
   /****************************
    *           TAB 2          *
    ****************************/
   
   @FXML
   private TextField t2TextAddFood, t2TextAddNutr, t2TextAddWgt,
           t2TextChgFood, t2TextChgNutr, t2TextChgWgt,
           t2TextDelFood, t2TextDelNutr, t2TextDelWgt,
           t2TextRevisionNumber;
   @FXML
   private Button t2ButtonAddFood, t2ButtonAddNutr, t2ButtonAddWgt,
           t2ButtonChgFood, t2ButtonChgNutr, t2ButtonChgWgt,
           t2ButtonDelFood, t2ButtonDelNutr, t2ButtonDelWgt;
   @FXML
   private ProgressIndicator t2PiAddFood, t2PiAddNutr, t2PiAddWgt,
           t2PiChgFood, t2PiChgNutr, t2PiChgWgt,
           t2PiDelFood, t2PiDelNutr, t2PiDelWgt;
   
   
   @FXML
   private void DragOverAcceptFile(DragEvent event)
   {
      if(event.getDragboard().hasFiles())
      {
         event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
   }
   
   @FXML
   private void DragDroppedGetFilePath(DragEvent event)
   {
      Dragboard board = event.getDragboard();
      if (board.hasFiles())
      {
         List<File> files = board.getFiles();
         String path = files
                 .get(0)
                 .toPath()
                 .toString();
         
         List<TextField> listTextFields = new ArrayList<>();
         listTextFields.addAll( Arrays.asList( t2TextAddFood, t2TextAddNutr, t2TextAddWgt,t2TextChgFood, t2TextChgNutr, t2TextChgWgt,t2TextDelFood, t2TextDelNutr, t2TextDelWgt ) );
         for(TextField tf : listTextFields)
         {
            if(event.getSource().equals( tf ) || event.getTarget().equals( tf )){ tf.setText(path); }
         }
         /*
         if(event.getSource().equals(t2TextAddFood) || event.getTarget().equals(t2TextAddFood)){  t2TextAddFood.setText(path); }
         if(event.getSource().equals(t2TextAddNutr) || event.getTarget().equals(t2TextAddNutr)){  t2TextAddNutr.setText(path); }
         if(event.getSource().equals(t2TextAddWgt) || event.getTarget().equals(t2TextAddWgt)){    t2TextAddWgt.setText(path);  }
         if(event.getSource().equals(t2TextChgFood) || event.getTarget().equals(t2TextChgFood)){  t2TextChgFood.setText(path); }
         if(event.getSource().equals(t2TextChgNutr) || event.getTarget().equals(t2TextChgNutr)){  t2TextChgNutr.setText(path); }
         if(event.getSource().equals(t2TextChgWgt) || event.getTarget().equals(t2TextChgWgt)){    t2TextChgWgt.setText(path);  }
         if(event.getSource().equals(t2TextDelFood) || event.getTarget().equals(t2TextDelFood)){  t2TextDelFood.setText(path); }
         if(event.getSource().equals(t2TextDelNutr) || event.getTarget().equals(t2TextDelNutr)){  t2TextDelNutr.setText(path); }
         if(event.getSource().equals(t2TextDelWgt) || event.getTarget().equals(t2TextDelWgt)){    t2TextDelWgt.setText(path);  }
         */
         if (path.endsWith(".txt"))
         {
            event.acceptTransferModes(TransferMode.ANY);
         }
         
      }
      
      event.setDropCompleted(true);
      event.consume();
   }
   
   @FXML
   private void MouseClickChooseFile(MouseEvent event)
   {
      FileChooser fileChooser = new FileChooser();
      File selectedFile = fileChooser.showOpenDialog(null);
      
      if(selectedFile != null)
      {
         if(event.getSource().equals(t2ButtonAddFood) || event.getTarget().equals(t2ButtonAddFood)){  t2TextAddFood.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonAddNutr) || event.getTarget().equals(t2ButtonAddNutr)){  t2TextAddNutr.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonAddWgt)  || event.getTarget().equals(t2ButtonAddWgt)) {  t2TextAddWgt.setText(selectedFile.getPath());   }
         if(event.getSource().equals(t2ButtonChgFood) || event.getTarget().equals(t2ButtonChgFood)){  t2TextChgFood.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonChgNutr) || event.getTarget().equals(t2ButtonChgNutr)){  t2TextChgNutr.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonChgWgt)  || event.getTarget().equals(t2ButtonChgWgt)) {  t2TextChgWgt.setText(selectedFile.getPath());   }
         if(event.getSource().equals(t2ButtonDelFood) || event.getTarget().equals(t2ButtonDelFood)){  t2TextDelFood.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonDelNutr) || event.getTarget().equals(t2ButtonDelNutr)){  t2TextDelNutr.setText(selectedFile.getPath());  }
         if(event.getSource().equals(t2ButtonDelWgt)  || event.getTarget().equals(t2ButtonDelWgt)) {  t2TextDelWgt.setText(selectedFile.getPath());   }
      }
      else
      {
         System.err.println("File has not been choosen!");
      }
   }
   
   @FXML
   private void MouseClickStartRead()
   {      
      DatabaseController.setVERSION(t2TextRevisionNumber.getText());
      System.out.println("________________________________________________________________________");
      System.out.println("REVISION: " + DatabaseController.getVERSION());
            
      Task task = new Task<Void>() {
         @Override
         protected Void call() throws Exception {
            System.out.println("________________________________________________________________________");
            System.out.println("ADD_FOOD");
            t2PiAddFood.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextAddFood.getText(), "ADD_FOOD"));
            t2PiAddFood.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("ADD_NUTR");
            t2PiAddNutr.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextAddNutr.getText(), "ADD_NUTR"));
            t2PiAddNutr.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("ADD_WGT");
            t2PiAddWgt.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextAddWgt.getText(), "ADD_WGT"));
            t2PiAddWgt.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("CHG_FOOD");
            t2PiChgFood.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextChgFood.getText(), "CHG_FOOD"));
            t2PiChgFood.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("CHG_NUTR");
            t2PiChgNutr.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextChgNutr.getText(), "CHG_NUTR"));
            t2PiChgNutr.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("CHG_WGT");
            t2PiChgWgt.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextChgWgt.getText(), "CHG_WGT"));
            t2PiChgWgt.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("DEL_FOOD");
            t2PiDelFood.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextDelFood.getText(), "DEL_FOOD"));
            t2PiDelFood.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("DEL_NUTR");
            t2PiDelNutr.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextDelNutr.getText(), "DEL_NUTR"));
            t2PiDelNutr.setVisible(false);
            System.out.println("________________________________________________________________________");
            System.out.println("DEL_WGT");
            t2PiDelWgt.setVisible(true);
            System.out.println(ETLController.ReadFiles(t2TextDelWgt.getText(), "DEL_WGT"));
            t2PiDelWgt.setVisible(false);
            
            return null;
         }
      };
      
      new Thread(task).start();
   }
   
   /****************************
    *           TAB 3          *
    ****************************/
   @FXML
   private ListView t3ListView;
   
   @FXML
   private Button t3ButtonStart, t3ButtonStop;
   
   @FXML
   private ProgressBar t3ProgressBar;
      
   @FXML
   private Text t3TextSuccessCounter, t3TextTotal;
      
   
   @FXML
   private void StartProcess()
   {
      Task task = new Task<Void>() {
         @Override
         protected Void call() throws Exception {
            
            Connection conn = DatabaseController.Connect();
            ETLController.CreateFunctions(conn);
            ETLController.LoadDatabase(conn);
            
            return null;
         }
      };
      
      new Thread(task).start();
   }
   
   
   public void AddTraceMessage(String status, String data)
   {
      Platform.runLater(() -> {
         String msg = status + "\t\t" + data;
         t3ListView.getItems().add(msg);
      });
   }
   
   public void SetProgressStatus(Integer currValue, Integer maxValue)
   {
      Platform.runLater(() -> {
         t3TextSuccessCounter.setText(String.valueOf(currValue));
         t3TextTotal.setText("/" + String.valueOf(maxValue));
         t3ProgressBar.setProgress(currValue.doubleValue() / maxValue.doubleValue());
      });
   }
   
   public void SetProgressBar(Integer value, Integer maxValue)
   {
      t3ProgressBar.setProgress(value.doubleValue() / maxValue.doubleValue());
   }
   /****************************
    *           TAB 4          *
    ****************************/
   
   @FXML
   Button t4ButtonChooseFile;
   
   @FXML
   ListView t4ListView;
   
   @FXML
   private void MouseClickChooseLogFile(MouseEvent event)
   {
      t4ListView.setPlaceholder(new Label(""));
      
      FileChooser fileChooser = new FileChooser();
      File defaultDir = new File("LOG\\");
      fileChooser.setInitialDirectory(defaultDir);
      
      File selectedFile = fileChooser.showOpenDialog(null);
      
      List<TraceMessage> listTraceMessages = new ArrayList<>();
      
      if(selectedFile != null)
      {
         t4ListView.getItems().clear();
         FileHandler.readFile(selectedFile.getPath(), listTraceMessages, "LOG_FILE");
         
         Platform.runLater(() -> {
            for(TraceMessage msg : listTraceMessages)
            {
               t4ListView.getItems().add(msg.getStatus() + "   " + msg.getData());
            }
         });
      }
      else
      {
         System.err.println("File has not been choosen!");
      }
      
   }
   
   @Override
   public void initialize(URL url, ResourceBundle rb) {
      // TODO
      
      t3ListView.setPlaceholder(new Label(""));
      
      t3TextSuccessCounter.setFill(Color.GREEN);
      
      ETLController.setController(this);
      DatabaseController.setController(this);
      
      
      t1TextIPaddress.setText("localhost");
      t1TextPort.setText("5432");
      t1TextDBname.setText("lavinia");
      t1TextUsername.setText("postgres");
      t1Password.setText("qaswed123");
      t1TextSchema.setText("minta");
      
      t2TextAddFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_FOOD.txt");
      t2TextAddNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_NUTR.txt"); 
      t2TextAddWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_WGT.txt");
      t2TextChgFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_FOOD.txt");
      t2TextChgNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_NUTR.txt");
      t2TextChgWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_WGT.txt");
      t2TextDelFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_FOOD.txt");
      t2TextDelNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_NUTR.txt");
      t2TextDelWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_WGT.txt");
      t2TextRevisionNumber.setText("60");
      
      
      
   }
   
}

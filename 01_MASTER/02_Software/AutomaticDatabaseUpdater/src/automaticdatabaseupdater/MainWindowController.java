/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdater;
import controllers.*;
import descriptors.TraceMessage;

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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author TobikJanos
 */
public class MainWindowController implements Initializable {
    
    @FXML
    private TabPane TabPaneMain;
    @FXML
    private Tab Tab1, Tab2, Tab3, Tab4;
    
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
        DatabaseHandler.ClearDatabaseSetup();
        
        boolean isEmpty = false;
        StringBuilder stringBuilder = new StringBuilder("Üres mezők:\n");
        if(t1TextIPaddress.getText().equals(""))  { isEmpty = true; stringBuilder.append("IP cím\n"); }
        if(t1TextPort.getText().equals(""))       { isEmpty = true; stringBuilder.append("Port\n"); }
        if(t1TextDBname.getText().equals(""))     { isEmpty = true; stringBuilder.append("Adatbázis név\n"); }
        if(t1TextUsername.getText().equals(""))   { isEmpty = true; stringBuilder.append("Felhasználó név\n"); }
        if(t1TextSchema.getText().equals(""))     { isEmpty = true; stringBuilder.append("Jelszó\n"); }
        if(t1Password.getText().equals(""))       { isEmpty = true; stringBuilder.append("Adatbázis séma\n"); }
        
        if(isEmpty)
        {
            SystemMessageController.DisplayWarningMessage("Kitöltetlen mező!", stringBuilder.toString());
            return;
        }
        
        DatabaseHandler.setIP_ADDRESS(      t1TextIPaddress.getText()  );
        DatabaseHandler.setPORT(            t1TextPort.getText()       );
        DatabaseHandler.setDATABASE_NAME(   t1TextDBname.getText()     );
        DatabaseHandler.setUSERNAME(        t1TextUsername.getText()   );
        DatabaseHandler.setPASSWORD(        t1Password.getText()       );
        DatabaseHandler.setSCHEMA(          t1TextSchema.getText()     );
        
        DatabaseHandler db = new DatabaseHandler();
        System.out.println(db.toString());
        
        Tab2.setDisable(false);
    }
    
    @FXML
    private void MouseClickTestDatabaseConnection()
    {
        
        DatabaseHandler.ClearDatabaseSetup();
        
        DatabaseHandler.setIP_ADDRESS(      t1TextIPaddress.getText()  );
        DatabaseHandler.setPORT(            t1TextPort.getText()       );
        DatabaseHandler.setDATABASE_NAME(   t1TextDBname.getText()     );
        DatabaseHandler.setUSERNAME(        t1TextUsername.getText()   );
        DatabaseHandler.setPASSWORD(        t1Password.getText()       );
        DatabaseHandler.setSCHEMA(          t1TextSchema.getText()     );
        
        
        try {
            Connection conn = DatabaseHandler.Connect();
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
            conn.close();
        } catch (Exception ex) {
            SystemMessageController.DisplayErrorMessage("Adatbázis kapcsolat tesztelése","Teszt sikertelen!");
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
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
            t2ButtonDelFood, t2ButtonDelNutr, t2ButtonDelWgt,
            t2ButtonStart;
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
        
        /**
         * Check empty TextFields
         */
        boolean isEmpty = false;
        StringBuilder stringBuilder = new StringBuilder("Üres mezők:\n");
        if(t2TextAddFood.getText().equals("")){    isEmpty = true; stringBuilder.append("ADD_FOOD\n"); }
        if(t2TextAddNutr.getText().equals("")){    isEmpty = true; stringBuilder.append("ADD_NUTR\n"); }
        if(t2TextAddWgt.getText().equals("")){     isEmpty = true; stringBuilder.append("ADD_WGT\n"); }
        if(t2TextChgFood.getText().equals("")){    isEmpty = true; stringBuilder.append("CHG_FOOD\n"); }
        if(t2TextChgNutr.getText().equals("")){    isEmpty = true; stringBuilder.append("CHG_NUTR\n"); }
        if(t2TextChgWgt.getText().equals("")){     isEmpty = true; stringBuilder.append("CHG_WGT\n"); }
        if(t2TextDelFood.getText().equals("")){    isEmpty = true; stringBuilder.append("DEL_FOOD\n"); }
        if(t2TextDelNutr.getText().equals("")){    isEmpty = true; stringBuilder.append("DEL_NUTR\n"); }
        if(t2TextDelWgt.getText().equals("")){     isEmpty = true; stringBuilder.append("DEL_WGT\n"); }
        
        
        DatabaseHandler.setVERSION(t2TextRevisionNumber.getText());
        System.out.println("________________________________________________________________________");
        System.out.println("REVISION: " + DatabaseHandler.getVERSION());
        
        if(t2TextRevisionNumber.getText().equals(""))
        {
            SystemMessageController.DisplayErrorMessage("Üres mező!", "Adja meg a revíziószámot");
            return;
        }
        
        
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                
                Integer dataCounter = 0;
                
                Tab1.setDisable(true);
                Tab3.setDisable(true);
                t2ButtonStart.setDisable(true);
                
                
                /**
                 * ________________________________________________________________________
                 * ADD_FOOD
                 */
                if(!t2TextAddFood.getText().equals(""))
                {
                    t2PiAddFood.setVisible(true);
                    ETLController.ReadFiles(t2TextAddFood.getText(), "ADD_FOOD");
                    t2PiAddFood.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * ADD_NUTR
                 */
                if(!t2TextAddNutr.getText().equals(""))
                {
                    t2PiAddNutr.setVisible(true);
                    ETLController.ReadFiles(t2TextAddNutr.getText(), "ADD_NUTR");
                    t2PiAddNutr.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * ADD_WGT
                 */
                if(!t2TextAddWgt.getText().equals(""))
                {
                    t2PiAddWgt.setVisible(true);
                    ETLController.ReadFiles(t2TextAddWgt.getText(), "ADD_WGT");
                    t2PiAddWgt.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * CHG_FOOD
                 */
                if(!t2TextChgFood.getText().equals(""))
                {
                    t2PiChgFood.setVisible(true);
                    ETLController.ReadFiles(t2TextChgFood.getText(), "CHG_FOOD");
                    t2PiChgFood.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * CHG_NUTR
                 */
                if(!t2TextChgNutr.getText().equals(""))
                {
                    t2PiChgNutr.setVisible(true);
                    ETLController.ReadFiles(t2TextChgNutr.getText(), "CHG_NUTR");
                    t2PiChgNutr.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * CHG_WGT
                 */
                if(!t2TextChgWgt.getText().equals(""))
                {
                    t2PiChgWgt.setVisible(true);
                    ETLController.ReadFiles(t2TextChgWgt.getText(), "CHG_WGT");
                    t2PiChgWgt.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * DEL_FOOD
                 */
                if(!t2TextDelFood.getText().equals(""))
                {
                    t2PiDelFood.setVisible(true);
                    ETLController.ReadFiles(t2TextDelFood.getText(), "DEL_FOOD");
                    t2PiDelFood.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * DEL_NUTR
                 */
                if(!t2TextDelNutr.getText().equals(""))
                {
                    t2PiDelNutr.setVisible(true);
                    ETLController.ReadFiles(t2TextDelNutr.getText(), "DEL_NUTR");
                    t2PiDelNutr.setVisible(false);
                }
                
                /**
                 * ________________________________________________________________________
                 * DEL_WGT
                 */
                if(!t2TextDelWgt.getText().equals(""))
                {
                    t2PiDelWgt.setVisible(true);
                    ETLController.ReadFiles(t2TextDelWgt.getText(), "DEL_WGT");
                    t2PiDelWgt.setVisible(false);
                }
                
                Tab3.setDisable(false);
                t2ButtonStart.setDisable(false);
                
                return null;
            }
        };
        
        /**
         * in case of empty TextField wait for WarningMessage
         */
        if(isEmpty)
        {
            if(SystemMessageController.DisplayWarningMessage("Kitöltetlen mező!", stringBuilder.toString()))
                new Thread(task).start();
        }
        else
        {
            new Thread(task).start();
        }
        
    }
    
    /****************************
     *           TAB 3          *
     ****************************/
    @FXML
    private ListView t3ListView;
    
    @FXML
    private Button t3ButtonStart;
    
    @FXML
    private ProgressBar t3ProgressBar;
    
    @FXML
    private Text t3TextSuccessCounter, t3TextTotal;
    
    
    @FXML
    private void StartProcess()
    {
       Tab1.setDisable(true);
       Tab2.setDisable(true);
       t3ButtonStart.setDisable(true);
       
       Platform.runLater(() -> {
          t3ListView.getItems().clear();
       });
       
       Task task = new Task<Void>() {
          @Override
          protected Void call() throws Exception {
             
             
             Connection conn = DatabaseHandler.Connect();
             ETLController.CreateFunctions(conn);
             ETLController.LoadDatabase(conn);
             
             return null;
          }
       };
       
       new Thread(task).start();
       
       t3ButtonStart.setDisable(false);
    }
    
    
    public void AddTraceMessage(String status, String data)
    {
        Platform.runLater(() -> {
            String msg = status + "\t\t" + data;
            t3ListView.getItems().add(msg);
        });
    }
    
    public void SetProgressStatus(Integer successValue, Integer errorValue, Integer maxValue)
    {
        Platform.runLater(() -> {
            t3TextSuccessCounter.setText(String.valueOf(successValue));
            t3TextTotal.setText("/" + String.valueOf(maxValue));
            
            Double progressValue = successValue.doubleValue() + errorValue.doubleValue();
            t3ProgressBar.setProgress(progressValue / maxValue.doubleValue());
        });
    }
    
    /****************************
     *           TAB 4          *
     ****************************/
        
    @FXML
    private ListView t4ListView;
    
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
                    t4ListView.getItems().add(msg.getStatus() + "   -   " + msg.getData());
                }
            });
        }
        else
        {
            System.err.println("File has not been choosen!");
        }
        
    }
    
    @FXML
    private void InitNewUpdateProcess()
    {
        TabPaneMain.getSelectionModel().select(Tab1);
        
        Tab1.setDisable(false);
        Tab2.setDisable(true);
        Tab3.setDisable(true);
        
        /**
         * TAB_1
         */
        
//        t1TextIPaddress.setText("");
//        t1TextPort.setText("");
//        t1TextDBname.setText("");
//        t1TextUsername.setText("");
//        t1TextSchema.setText("");
//        t1Password.setText("");
        
        DatabaseHandler.ClearDatabaseSetup();
        
        /**
         * TAB_2
         */
        
        t2TextAddFood.setText("");
        t2TextAddNutr.setText("");
        t2TextAddWgt.setText("");
        t2TextChgFood.setText("");
        t2TextChgNutr.setText("");
        t2TextChgWgt.setText("");
        t2TextDelFood.setText("");
        t2TextDelNutr.setText("");
        t2TextDelWgt.setText("");
        t2TextRevisionNumber.setText("");
        
        /**
         * TAB_3
         */
        
        t3ListView.getItems().clear();
        t3ProgressBar.setProgress(0);
        t3TextSuccessCounter.setText("");
        t3TextTotal.setText("");
        
        /**
         * TAB_4
         */
        
        t4ListView.getItems().clear();
        
    }
    
    @FXML
    private void ExitProgram()
    {
        Platform.exit();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Tab1.setDisable(false);
        Tab2.setDisable(true);
        Tab3.setDisable(true);
        Tab4.setDisable(false);
        
        t3ListView.setPlaceholder(new Label(""));
        
        t3TextSuccessCounter.setFill(Color.GREEN);
        
        ETLController.setController(this);
        DatabaseHandler.setController(this);
        
        
//        t1TextIPaddress.setText("localhost");
//        t1TextPort.setText("5432");
//        t1TextDBname.setText("lavinia");
//        t1TextUsername.setText("postgres");
//        t1Password.setText("qaswed123");
//        t1TextSchema.setText("minta2");
//        
//        t2TextAddFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_FOOD.txt");
//        t2TextAddNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_NUTR.txt");
//        t2TextAddWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\ADD_WGT.txt");
//        t2TextChgFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_FOOD.txt");
//        t2TextChgNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_NUTR.txt");
//        t2TextChgWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\CHG_WGT.txt");
//        t2TextDelFood.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_FOOD.txt");
//        t2TextDelNutr.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_NUTR.txt");
//        t2TextDelWgt.setText("D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\testUpdateFiles\\DEL_WGT.txt");
//        t2TextRevisionNumber.setText("60");       
        
    }
    
}

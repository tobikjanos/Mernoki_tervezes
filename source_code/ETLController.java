/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dzsákom
 */
public class ETLController {
   
   public static enum FILETYPES{ADD_FOOD, ADD_NUTR, ADD_WGT, CHG_FOOD, CHG_NUTR, CHG_WGT, DEL_FOOD, DEL_NUTR, DEL_WGT} /* TODO: add more file types*/
   /**
    * IDEAS:
    * - enum tags might should be AddFood, AddNutr, ...
    * - lists' name should be AddFood, AddNutr, ...
    */
   
   public static List<FileFoodStruct>       listAddFood     = new ArrayList<>();
   public static List<FileNutrientStruct>   listAddNutr     = new ArrayList<>();
   public static List<FileWeightStruct>     listAddWgt      = new ArrayList<>();
   public static List<FileFoodStruct>       listChgFood     = new ArrayList<>();
   public static List<FileNutrientStruct>   listChgNutr     = new ArrayList<>();
   public static List<FileWeightStruct>     listChgWgt      = new ArrayList<>();
   public static List<FileFoodStruct>       listDelFood     = new ArrayList<>();
   public static List<FileNutrientStruct>   listDelNutr     = new ArrayList<>();
   public static List<FileWeightStruct>     listDelWgt      = new ArrayList<>();
   
   private static FXMLDocumentController controller;
   
   
   public static int ReadFiles( String strFilePath , String strFileType )
   {
      FILETYPES fileTypes = FILETYPES.valueOf(strFileType);
      int intRetVal = 0;
      
      switch(fileTypes)
      {
         case ADD_FOOD:
         {
            intRetVal = FileHandler.readFile(strFilePath, listAddFood, strFileType);
            break;
         }
         case ADD_NUTR:
         {
            intRetVal = FileHandler.readFile(strFilePath, listAddNutr, strFileType);
            break;
         }
         case ADD_WGT:
         {
            intRetVal = FileHandler.readFile(strFilePath, listAddWgt, strFileType);
            break;
         }
         case CHG_FOOD:
         {
            intRetVal = FileHandler.readFile(strFilePath, listChgFood, strFileType);
            break;
         }
         case CHG_NUTR:
         {
            intRetVal = FileHandler.readFile(strFilePath, listChgNutr, strFileType);
            break;
         }
         case CHG_WGT:
         {
            intRetVal = FileHandler.readFile(strFilePath, listChgWgt, strFileType);
            break;
         }
         case DEL_FOOD:
         {
            intRetVal = FileHandler.readFile(strFilePath, listDelFood, strFileType);
            break;
         }
         case DEL_NUTR:
         {
            intRetVal = FileHandler.readFile(strFilePath, listDelNutr, strFileType);
            break;
         }
         case DEL_WGT:
         {
            intRetVal = FileHandler.readFile(strFilePath, listDelWgt, strFileType);
            break;
         }
         default:
         {
            System.err.println("Type of file is not known!\tGiven type: " + strFileType);
         }
      }
      return intRetVal;
   }
   
   /**
    * creates stored procedures
    * @param conn    -  database connection
    */
   public static void CreateFunctions( Connection conn )
   {
      DatabaseController.createAddFoodFunction(conn);
      DatabaseController.createAddNutrientFunction(conn);
      DatabaseController.createAddWeightFunction(conn);
      
      DatabaseController.createChgFoodFunction(conn);
      DatabaseController.createChgNutrientFunction(conn);
      DatabaseController.createChgWeightFunction(conn);
      
      DatabaseController.createDelFoodFunction(conn);
      DatabaseController.createDelNutrientFunction(conn);
      DatabaseController.createDelWeightFunction(conn);
   }
   
   /**
    *
    * @param conn -  database connection
    */
   public static void LoadDatabase(Connection conn)
   {
      int totalData = listAddFood.size() +
              listAddNutr.size() +
              listAddWgt.size() +
              listChgFood.size() +
              listChgNutr.size() +
              listChgWgt.size() +
              listDelFood.size() +
              listDelNutr.size() +
              listDelWgt.size();
      
      int progressCounter = 0;
      int moduleCounter = 0;
      
      LogHandler.ClearLogList();
      LogHandler.CreateLogFile();
      
      try
      {
         conn.setAutoCommit(false);
         Savepoint savePoint1 = conn.setSavepoint("savePoint1");
         
         try
         {
            /**__________________________________________________________________________
             * ADD_FOOD 
             */
            SendTraceMessage("[  INFO  ]", "ADD_FOOD elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "ADD_FOOD elkezdődött");
            for(FileFoodStruct ffs : listAddFood)
            {
               DatabaseController.executeAddFoodFunction(conn, ffs);
               //if execute done cnt+1
               //if not cnt not increment
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "ADD_FOOD befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * ADD_NUTR 
             */
            SendTraceMessage("[  INFO  ]", "ADD_NUTR elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "ADD_NUTR elkezdődött");
            moduleCounter = 0;
            for(FileNutrientStruct fns : listAddNutr)
            {
               DatabaseController.executeAddNutrientFunction(conn, fns);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "ADD_NUTR befejeződött: " + moduleCounter + " adat");
            
            /**__________________________________________________________________________
             * ADD_WGT 
             */
            SendTraceMessage("[  INFO  ]", "ADD_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "ADD_WGT elkezdődött");
            moduleCounter = 0;
            for(FileWeightStruct fws : listAddWgt)
            {
               DatabaseController.executeAddWeightFunction(conn, fws);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "ADD_WGT befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * CHG_FOOD 
             */
            SendTraceMessage("[  INFO  ]", "CHG_FOOD elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_FOOD elkezdődött");
            moduleCounter = 0;
            for(FileFoodStruct ffs : listChgFood)
            {
               DatabaseController.executeChgFoodFunction(conn, ffs);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "CHG_FOOD befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * CHG_NUTR 
             */
            SendTraceMessage("[  INFO  ]", "CHG_NUTR elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_NUTR elkezdődött");
            moduleCounter = 0;
            for(FileNutrientStruct fns : listChgNutr)
            {
               DatabaseController.executeChgNutrientFunction(conn, fns);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "CHG_NUTR befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * CHG_WGT 
             */
            SendTraceMessage("[  INFO  ]", "CHG_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_WGT elkezdődött");
            moduleCounter = 0;
            for(FileWeightStruct fws : listChgWgt)
            {
               DatabaseController.executeChgWeightFunction(conn, fws);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "CHG_WGT befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * DEL_WGT 
             */
            SendTraceMessage("[  INFO  ]", "DEL_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_WGT elkezdődött");
            moduleCounter = 0;
            for(FileWeightStruct fws : listDelWgt)
            {
               DatabaseController.executeDelWeightFunction(conn, fws);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "DEL_WGT befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * DEL_NUTR 
             */
            SendTraceMessage("[  INFO  ]", "DEL_NUTR elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_NUTR elkezdődött");
            moduleCounter = 0;
            for(FileNutrientStruct fns : listDelNutr)
            {
               DatabaseController.executeDelNutrientFunction(conn, fns);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "DEL_NUTR befejeződött: " + moduleCounter + " adat");
            /**__________________________________________________________________________
             * DEL_FOOD 
             */
            SendTraceMessage("[  INFO  ]", "DEL_FOOD elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_FOOD elkezdődött");
            moduleCounter = 0;
            for(FileFoodStruct ffs : listDelFood)
            {
               DatabaseController.executeDelFoodFunction(conn, ffs);
               moduleCounter++;
               progressCounter++;
               SendCounterValue(progressCounter, totalData);
            }
            SendTraceMessage("[  INFO  ]", "DEL_FOOD befejeződött: " + moduleCounter + " adat");
            
            conn.commit();
            conn.close();
            
         } catch (SQLException ex) {
            Logger.getLogger(ETLController.class.getName()).log(Level.SEVERE, null, ex);
            conn.rollback(savePoint1);
            SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
         }
         
      } catch (SQLException ex) {
         Logger.getLogger(ETLController.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      LogHandler.WriteLogFile();
         
   }
   
   private static void SendTraceMessage(String status, String data)
   {
      controller.AddTraceMessage(status, data);
   }
   
   private static void SendCounterValue(Integer currValue, Integer maxValue)
   {
      controller.SetProgressStatus(currValue, maxValue);
   }
   
   public static void setController(FXMLDocumentController cntrlr) 
   {
      controller = cntrlr;
   }
   
   /*
   main function for testing the class and functions
   */
   public static void main(String[] args)
   {
      String filePathFOOD = "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_FOOD.txt";
      String filePathNUTR = "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_NUTR.txt";
      
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      System.out.println("Reading Foods started!");
      ReadFiles( filePathFOOD ,"ADD_FOOD" );
      System.out.println("Reading Foods ended!");
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      System.out.println("Reading Nutrients started!");
      ReadFiles( filePathNUTR , "ADD_NUTR" );
      System.out.println("Reading Nutrients ended!");
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      
      Connection conn = DatabaseController.Connect();
      LoadDatabase(conn);
      
      /*
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      for( int idx=0 ; idx < listFoodStruct.size() ; idx++ )
      {
      System.out.println(listFoodStruct.get(idx).toString());
      }
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      for( int idx=0 ; idx < listNutrientStruct.size() ; idx++ )
      {
      System.out.println(listNutrientStruct.get(idx).toString());
      }
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      */
   }
   
}

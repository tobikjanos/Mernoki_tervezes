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
   
   private static final List<FileFoodStruct>       listAddFood     = new ArrayList<>();
   private static final List<FileNutrientStruct>   listAddNutr     = new ArrayList<>();
   private static final List<FileWeightStruct>     listAddWgt      = new ArrayList<>();
   private static final List<FileFoodStruct>       listChgFood     = new ArrayList<>();
   private static final List<FileNutrientStruct>   listChgNutr     = new ArrayList<>();
   private static final List<FileWeightStruct>     listChgWgt      = new ArrayList<>();
   private static final List<FileFoodStruct>       listDelFood     = new ArrayList<>();
   private static final List<FileNutrientStruct>   listDelNutr     = new ArrayList<>();
   private static final List<FileWeightStruct>     listDelWgt      = new ArrayList<>();
   
   private static FXMLDocumentController controller;
   
   
   public static void ReadFiles( String strFilePath , String strFileType )
   {
      
      FILETYPES fileTypes = FILETYPES.valueOf(strFileType);
      
      switch(fileTypes)
      {
         case ADD_FOOD:
         {
            FileHandler.readFile(strFilePath, listAddFood, strFileType);
            break;
         }
         case ADD_NUTR:
         {
            FileHandler.readFile(strFilePath, listAddNutr, strFileType);
            break;
         }
         case ADD_WGT:
         {
            FileHandler.readFile(strFilePath, listAddWgt, strFileType);
            break;
         }
         case CHG_FOOD:
         {
            FileHandler.readFile(strFilePath, listChgFood, strFileType);
            break;
         }
         case CHG_NUTR:
         {
            FileHandler.readFile(strFilePath, listChgNutr, strFileType);
            break;
         }
         case CHG_WGT:
         {
            FileHandler.readFile(strFilePath, listChgWgt, strFileType);
            break;
         }
         case DEL_FOOD:
         {
            FileHandler.readFile(strFilePath, listDelFood, strFileType);
            break;
         }
         case DEL_NUTR:
         {
            FileHandler.readFile(strFilePath, listDelNutr, strFileType);
            break;
         }
         case DEL_WGT:
         {
            FileHandler.readFile(strFilePath, listDelWgt, strFileType);
            break;
         }
         default:
         {
            System.err.println("Type of file is not known!\tGiven type: " + strFileType);
         }
      }
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
   public static void LoadDatabase( Connection conn )
   {
      /**
       * Initialize variables
       */
      Integer totalData = listAddFood.size() +
              listAddNutr.size() +
              listAddWgt.size() +
              listChgFood.size() +
              listChgNutr.size() +
              listChgWgt.size() +
              listDelFood.size() +
              listDelNutr.size() +
              listDelWgt.size();
      
      Integer successCounter = 0;
      Integer errorCounter = 0;
      Integer moduleCounter = 0;
      
      System.out.println(listAddFood.size() + " " + listAddNutr.size());
      
      /**
       * Clear log list
       * Create log file
       */
      LogHandler.ClearLogList();
      LogHandler.CreateLogFile();
      
      try
      {
         conn.setAutoCommit(false);
         Savepoint savePoint1 = conn.setSavepoint("savePoint1");
         
         if(!listAddFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * ADD_FOOD
             */
            SendTraceMessage(       "[  INFO  ]", "ADD_FOOD elkezdődött");
            LogHandler.AddElement(  "[  INFO  ]", "ADD_FOOD elkezdődött");
            
            for(FileFoodStruct ffs : listAddFood)
            {
               try
               {
                  DatabaseController.executeAddFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "ADD_FOOD befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listAddNutr.isEmpty())
         {
            
         /**__________________________________________________________________________
          * ADD_NUTR
          */
            SendTraceMessage(       "[  INFO  ]", "ADD_NUTR elkezdődött");
            LogHandler.AddElement(  "[  INFO  ]", "ADD_NUTR elkezdődött");
            moduleCounter = 0;
            
            for(FileNutrientStruct fns : listAddNutr)
            {
               try
               {
                  DatabaseController.executeAddNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "ADD_NUTR befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listAddWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * ADD_WGT
             */
            SendTraceMessage("[  INFO  ]", "ADD_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "ADD_WGT elkezdődött");
            moduleCounter = 0;
            
            for(FileWeightStruct fws : listAddWgt)
            {
               try
               {
                  DatabaseController.executeAddWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "ADD_WGT befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listChgFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_FOOD
             */
            SendTraceMessage("[  INFO  ]", "CHG_FOOD elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_FOOD elkezdődött");
            moduleCounter = 0;
            
            for(FileFoodStruct ffs : listChgFood)
            {
               try
               {
                  DatabaseController.executeChgFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "CHG_FOOD befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listChgNutr.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_NUTR
             */
            SendTraceMessage("[  INFO  ]", "CHG_NUTR elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_NUTR elkezdődött");
            moduleCounter = 0;
            
            for(FileNutrientStruct fns : listChgNutr)
            {
               try
               {
                  DatabaseController.executeChgNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "CHG_NUTR befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listChgWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_WGT
             */
            SendTraceMessage("[  INFO  ]", "CHG_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "CHG_WGT elkezdődött");
            moduleCounter = 0;
            
            for(FileWeightStruct fws : listChgWgt)
            {
               try
               {
                  DatabaseController.executeChgWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "CHG_WGT befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listDelWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_WGT
             */
            SendTraceMessage("[  INFO  ]", "DEL_WGT elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_WGT elkezdődött");
            moduleCounter = 0;
            
            for(FileWeightStruct fws : listDelWgt)
            {
               try
               {
                  DatabaseController.executeDelWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "DEL_WGT befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listDelNutr.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_NUTR
             */
            SendTraceMessage("[  INFO  ]", "DEL_NUTR elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_NUTR elkezdődött");
            moduleCounter = 0;
            
            for(FileNutrientStruct fns : listDelNutr)
            {
               try
               {
                  DatabaseController.executeDelNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "DEL_NUTR befejeződött: " + moduleCounter + " adat");
            
         }
         
         if(!listDelFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_FOOD
             */
            SendTraceMessage("[  INFO  ]", "DEL_FOOD elkezdődött");
            LogHandler.AddElement("[  INFO  ]", "DEL_FOOD elkezdődött");
            moduleCounter = 0;
            
            for(FileFoodStruct ffs : listDelFood)
            {
               try
               {
                  DatabaseController.executeDelFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
               } catch (SQLException ex) {
                  errorCounter++;
                  SendCounterValue(successCounter, errorCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  savePoint1 = conn.setSavepoint("savePoint1");
               }
            }
            SendTraceMessage("[  INFO  ]", "DEL_FOOD befejeződött: " + moduleCounter + " adat");
            
         }
         
         /**
          * If error(s) occured do the rollback in the whole transaction
          */
         if( !errorCounter.equals(0) )
         {
            conn.rollback(savePoint1);
            conn.commit();
            SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
         }
         
         conn.commit();
         conn.close();
         
      } catch (SQLException ex) {
         System.err.println(ex.getMessage());
      }
      
      LogHandler.WriteLogFile();
         
   }
   
   private static void SendTraceMessage(String status, String data)
   {
      controller.AddTraceMessage(status, data);
   }
   
   private static void SendCounterValue(Integer successValue, Integer errorValue, Integer maxValue)
   {
      controller.SetProgressStatus(successValue, errorValue, maxValue);
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
      Connection conn = DatabaseController.Connect();
      CreateFunctions(conn);
      
//      String filePathFOOD = "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_FOOD.txt";
//      String filePathNUTR = "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_NUTR.txt";
//      
//      System.out.println("|--------------------------------------------------------------------------------------------------------|");
//      System.out.println("Reading Foods started!");
//      ReadFiles( filePathFOOD ,"ADD_FOOD" );
//      System.out.println("Reading Foods ended!");
//      System.out.println("|--------------------------------------------------------------------------------------------------------|");
//      
//      System.out.println("|--------------------------------------------------------------------------------------------------------|");
//      System.out.println("Reading Nutrients started!");
//      ReadFiles( filePathNUTR , "ADD_NUTR" );
//      System.out.println("Reading Nutrients ended!");
//      System.out.println("|--------------------------------------------------------------------------------------------------------|");
//      
//      Connection conn = DatabaseController.Connect();
//      LoadDatabase(conn);
      
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

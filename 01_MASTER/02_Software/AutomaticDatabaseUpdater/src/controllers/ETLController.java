/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package controllers;
import automaticdatabaseupdater.MainWindowController;
import descriptors.*;

import java.sql.Connection;
import static java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author TobikJanos
 */
public class ETLController {
   
   public static enum FILETYPES{ADD_FOOD, ADD_NUTR, ADD_WGT, CHG_FOOD, CHG_NUTR, CHG_WGT, DEL_FOOD, DEL_NUTR, DEL_WGT, ADD_NDEF, CHG_NDEF}
   
   private static final List<FileStructFood>       listAddFood     = new ArrayList<>();
   private static final List<FileStructNutrient>   listAddNutr     = new ArrayList<>();
   private static final List<FileStructWeight>     listAddWgt      = new ArrayList<>();
   private static final List<FileStructFood>       listChgFood     = new ArrayList<>();
   private static final List<FileStructNutrient>   listChgNutr     = new ArrayList<>();
   private static final List<FileStructWeight>     listChgWgt      = new ArrayList<>();
   private static final List<FileStructFood>       listDelFood     = new ArrayList<>();
   private static final List<FileStructNutrient>   listDelNutr     = new ArrayList<>();
   private static final List<FileStructWeight>     listDelWgt      = new ArrayList<>();
   
   private static final List<FileStructNutrient>   listAddNdef     = new ArrayList<>();
   private static final List<FileStructNutrient>   listChgNdef     = new ArrayList<>();
   
   private static MainWindowController controller;
   
   /**
    * brief: read content of a file
    * @param strFilePath - path of given file to read
    * @param strFileType - type of given file
    */
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
         case ADD_NDEF:
         {
            FileHandler.readFile(strFilePath, listAddNdef, strFileType);
            break;
         }
         case CHG_NDEF:
         {
            FileHandler.readFile(strFilePath, listChgNdef, strFileType);
            break;
         }
         default:
         {
            System.err.println("Type of file is not known!\tGiven type: " + strFileType);
         }
      }
      return;
   }
   
   /**
    * brief: call functions to create stored procedure on the database server
    * @param conn    -  database connection
    */
   public static void CreateFunctions( Connection conn )
   {
      
      SendTraceMessage("[  INFO  ]", "Tárolt eljárások inicializálása");
      LogHandler.AddElement("[  INFO  ]", "Tárolt eljárások inicializálása");
      
      DatabaseHandler.createAddFoodFunction(conn);
      DatabaseHandler.createAddNutrientFunction(conn);
      DatabaseHandler.createAddWeightFunction(conn);
      
      DatabaseHandler.createChgFoodFunction(conn);
      DatabaseHandler.createChgNutrientFunction(conn);
      DatabaseHandler.createChgWeightFunction(conn);
      
      DatabaseHandler.createDelFoodFunction(conn);
      DatabaseHandler.createDelNutrientFunction(conn);
      DatabaseHandler.createDelWeightFunction(conn);
      
      DatabaseHandler.createAddNutrientDefiniton(conn);
      DatabaseHandler.createChgNutrientDefiniton(conn);

      return;
   }
   
   /**
    * brief: call database functions on elements of lists
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
              listDelWgt.size() +
              listAddNdef.size() +
              listChgNdef.size();
      
      Integer successCounter = 0;
      Integer moduleCounter = 0;
            
      
      try
      {
         /**
          * set database properties including autocommit, transaction isolation, savepoint
          */
         conn.setAutoCommit(false);
         conn.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED);
         Savepoint savePoint1 = conn.setSavepoint("savePoint1");
         
         SendTraceMessage(       "[  INFO  ]", "Adatbázis-frissítési folyamat elkezdése");
         LogHandler.AddElement(  "[  INFO  ]", "Adatbázis-frissítési folyamat elkezdése");
         
         if(!conn.isClosed() && !listAddNdef.isEmpty())
         {
            /**__________________________________________________________________________
             * ADD_NDEF
             */
            SendTraceMessage(       "[  INFO  ]", "ADD_NDEF frissítés elkezdése");
            LogHandler.AddElement(  "[  INFO  ]", "ADD_NDEF frissítés elkezdése");
            
            for(FileStructNutrient fsn : listAddNdef)
            {
               try
               {
                  DatabaseHandler.executeAddNutrDefFunction(conn, fsn);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listChgNdef.isEmpty())
         {
            /**__________________________________________________________________________
             * CHG_NDEF
             */
            SendTraceMessage(       "[  INFO  ]", "CHG_NDEF frissítés elkezdése");
            LogHandler.AddElement(  "[  INFO  ]", "CHG_NDEF frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructNutrient fsn : listChgNdef)
            {
               try
               {
                  DatabaseHandler.executeChgNutrDefFunction(conn, fsn);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
                  
         if(!conn.isClosed() && !listAddFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * ADD_FOOD
             */
            SendTraceMessage(       "[  INFO  ]", "ADD_FOOD frissítés elkezdése");
            LogHandler.AddElement(  "[  INFO  ]", "ADD_FOOD frissítés frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructFood ffs : listAddFood)
            {
               try
               {
                  DatabaseHandler.executeAddFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
                  
         if(!conn.isClosed() && !listAddNutr.isEmpty())
         {
            
            /**__________________________________________________________________________
             * ADD_NUTR
             */
            SendTraceMessage(       "[  INFO  ]", "ADD_NUTR frissítés elkezdése");
            LogHandler.AddElement(  "[  INFO  ]", "ADD_NUTR frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructNutrient fns : listAddNutr)
            {
               try
               {
                  DatabaseHandler.executeAddNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listAddWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * ADD_WGT
             */
            SendTraceMessage("[  INFO  ]", "ADD_WGT frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "ADD_WGT frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructWeight fws : listAddWgt)
            {
               try
               {
                  DatabaseHandler.executeAddWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listChgFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_FOOD
             */
            SendTraceMessage("[  INFO  ]", "CHG_FOOD frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "CHG_FOOD frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructFood ffs : listChgFood)
            {
               try
               {
                  DatabaseHandler.executeChgFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listChgNutr.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_NUTR
             */
            SendTraceMessage("[  INFO  ]", "CHG_NUTR frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "CHG_NUTR frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructNutrient fns : listChgNutr)
            {
               try
               {
                  DatabaseHandler.executeChgNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listChgWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * CHG_WGT
             */
            SendTraceMessage("[  INFO  ]", "CHG_WGT frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "CHG_WGT frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructWeight fws : listChgWgt)
            {
               try
               {
                  DatabaseHandler.executeChgWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listDelWgt.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_WGT
             */
            SendTraceMessage("[  INFO  ]", "DEL_WGT frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "DEL_WGT frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructWeight fws : listDelWgt)
            {
               try
               {
                  DatabaseHandler.executeDelWeightFunction(conn, fws);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listDelNutr.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_NUTR
             */
            SendTraceMessage("[  INFO  ]", "DEL_NUTR frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "DEL_NUTR frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructNutrient fns : listDelNutr)
            {
               try
               {
                  DatabaseHandler.executeDelNutrientFunction(conn, fns);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
         
         if(!conn.isClosed() && !listDelFood.isEmpty())
         {
            
            /**__________________________________________________________________________
             * DEL_FOOD
             */
            SendTraceMessage("[  INFO  ]", "DEL_FOOD frissítés elkezdése");
            LogHandler.AddElement("[  INFO  ]", "DEL_FOOD frissítés elkezdése");
            moduleCounter = 0;
            
            for(FileStructFood ffs : listDelFood)
            {
               try
               {
                  DatabaseHandler.executeDelFoodFunction(conn, ffs);
                  
                  moduleCounter++;
                  successCounter++;
                  SendCounterValue(successCounter, totalData);
                  
               } catch (SQLException ex) {
                  SendCounterValue(successCounter, totalData);
                  
                  conn.rollback(savePoint1);
                  conn.commit();
                  SendTraceMessage("[  INFO  ]", "Rollback alkalmazása");
                  SendTraceMessage("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  LogHandler.AddElement("[  INFO  ]", "Rollback alkalmazása");
                  LogHandler.AddElement("[  INFO  ]", "Az adatbázis visszaállt az előző állapotra");
                  conn.close();
                  break;
               }
            }
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + moduleCounter);
         }
                  
         if( !conn.isClosed() )
         {
            conn.commit();
            SendTraceMessage("[  INFO  ]", "Adatbázis-frissítés vége");
            SendTraceMessage("[  INFO  ]", "Feldolgozott adatok száma: " + successCounter);
            LogHandler.AddElement("[  INFO  ]", "Adatbázis-frissítés vége");
            LogHandler.AddElement("[  INFO  ]", "Feldolgozott adatok száma: " + successCounter);
            conn.close();
         }
         
      } catch (SQLException ex) {
         System.err.println(ex.getMessage());
      }
      
      LogHandler.WriteLogFile();
      
      return;
   }
   
   public static void ClearArrayLists()
   {
      listAddFood.clear();
      listAddNutr.clear();
      listAddWgt.clear();
      listChgFood.clear();
      listChgNutr.clear();
      listChgWgt.clear();
      listDelFood.clear();
      listDelNutr.clear();
      listDelWgt.clear();
      
      listAddNdef.clear();
      listChgNdef.clear();
   }
   
   private static void SendTraceMessage(String status, String data)
   {
      controller.AddTraceMessage(status, data);
      return;
   }
   
   private static void SendCounterValue(Integer successValue, Integer maxValue)
   {
      controller.SetProgressStatus(successValue, maxValue);
      return;
   }
   
   public static void setController(MainWindowController cntrlr)
   {
      controller = cntrlr;
      return;
   }
   
   /*
   main function for testing the class and functions
   */
   public static void main(String[] args)
   {
      
   }
}

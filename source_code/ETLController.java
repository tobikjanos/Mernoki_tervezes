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
   
   public static enum FILETYPES{FileFoodStruct, FileNutrientStruct} /* TODO: add more file types*/
   
   public static List<FileFoodStruct>       listFoodStruct     = new ArrayList<>();
   public static List<FileNutrientStruct>   listNutrientStruct = new ArrayList<>();
   
   public static void readFiles( String strFilePath , String strFileType )
   {
      FILETYPES fileTypes = FILETYPES.valueOf(strFileType);
      
      switch(fileTypes)
      {
         case FileFoodStruct:
         {
            FileHandler.readFile(strFilePath, listFoodStruct, strFileType);
            break;
         }
         case FileNutrientStruct:
         {
            FileHandler.readFile(strFilePath, listNutrientStruct, strFileType);
            break;
         }
         default:
         {
            System.err.println("Type of file is not known!");
         }
      }
      
   }
   
   /**
    * creates stored procedures
    * @param conn    -  database connection
    */
   public static void createFunctions( Connection conn )
   {
      DatabaseController.createAddFoodFunction(conn);
      DatabaseController.createAddNutrientFunction(conn);
      DatabaseController.createAddWeightFunction(conn);
      
      DatabaseController.createChgFoodFunction(conn);
      DatabaseController.createChgNutrientFunction(conn);
      DatabaseController.createChgWeightFunction(conn);
      
      /* TODO:
      DatabaseController.createDelFoodFunction(conn);
      DatabaseController.createDelNutrientFunction(conn);
      DatabaseController.createDelWeightFunction(conn);
      */
   }
   
   /**
    * 
    * @param conn -  database connection
    */
   public static void loadDatabase(Connection conn)
   {
      try
      {
         conn.setAutoCommit(false);
         Savepoint savePoint1 = conn.setSavepoint("savePoint1");
         
         try
         {
            /* ADD_FOOD */
            for(FileFoodStruct ffs : listFoodStruct)
            {
               DatabaseController.executeAddFoodFunction(conn, ffs);
            
            }
            /* ADD_NUTR */
            for(FileNutrientStruct fns : listNutrientStruct)
            {
               DatabaseController.executeAddNutrientFunction(conn, fns);
            }
            
            /* 
               TODO
               implement other execute function methods
            */
            
         } catch (SQLException ex) {
            Logger.getLogger(ETLController.class.getName()).log(Level.SEVERE, null, ex);
            conn.rollback(savePoint1);
         }
         
         
      } catch (SQLException ex) {
         Logger.getLogger(ETLController.class.getName()).log(Level.SEVERE, null, ex);
      }
      
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
      readFiles( filePathFOOD ,"FileFoodStruct" );      
      System.out.println("Reading Foods ended!");
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
      System.out.println("Reading Nutrients started!");      
      readFiles( filePathNUTR , "FileNutrientStruct" );      
      System.out.println("Reading Nutrients ended!");
      System.out.println("|--------------------------------------------------------------------------------------------------------|");
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

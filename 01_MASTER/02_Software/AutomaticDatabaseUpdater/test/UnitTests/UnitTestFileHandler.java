/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitTests;

import controllers.FileHandler;
import descriptors.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author TobikJanos
 */
public class UnitTestFileHandler extends TestCase {
   
   private List<FileStructFood>       listAddFood;
   private List<FileStructNutrient>   listAddNutr;
   private List<FileStructWeight>     listAddWgt;
   private List<FileStructFood>       listChgFood;
   private List<FileStructNutrient>   listChgNutr;
   private List<FileStructWeight>     listChgWgt;
   private List<FileStructFood>       listDelFood;
   private List<FileStructNutrient>   listDelNutr;
   private List<FileStructWeight>     listDelWgt;
   
   @Before
   @Override
   public void setUp()
   {
      listAddFood = new ArrayList<>();
      listAddNutr = new ArrayList<>();
      listAddWgt = new ArrayList<>();
      listChgFood = new ArrayList<>();
      listChgNutr = new ArrayList<>();
      listChgWgt = new ArrayList<>();
      listDelFood = new ArrayList<>();
      listDelNutr = new ArrayList<>();
      listDelWgt = new ArrayList<>();
   }
   
   @Test
   public void testParseUpdateFile()
   {
      String strToParse = "~60000~^~0100~^~test_name00~^~TEST_NAME00~^~~^~~^~Y~^~~^0^~~^^^^";
      
      List<String> list = ParseUpdateFile(strToParse);
      
      assertEquals( new ArrayList<>( Arrays.asList("60000", "0100", "test_name00", "TEST_NAME00", "null", "null", "Y", "null", "0", "null")),
                    list );
   }
   
   @Test
   public void testParseLogFile()
   {
      String strToParse = "[   OK   ]   -   60000   |   test_name00   |   TEST_NAME00   |   0";
      
      List<String> list = ParseLogFile(strToParse);
      
      assertEquals( new ArrayList<>( Arrays.asList("[   OK   ]", "60000   |   test_name00   |   TEST_NAME00   |   0")),
                    list );
   }
   
   @Test
   public void testReadAddFood()
   {
      readFile( "~60000~^~0100~^~test_name00~^~TEST_NAME00~^~~^~~^~Y~^~~^0^~~^^^^", listAddFood, "ADD_FOOD" );
      
      assertEquals( "60000", listAddFood.get(0).getNDB_No() );
      assertEquals( "test_name00", listAddFood.get(0).getLong_desc() );
      assertEquals( "TEST_NAME00", listAddFood.get(0).getShrt_desc());
      assertEquals( 0, listAddFood.get(0).getRefuse_percent());
      
   }
   
   @Test
   public void testReadAddNutrient()
   {
      readFile( "~60000~^~208~^0.0^0^^~7~^~Z~^~~^~~^^^^^^^~~^09/2015^", listAddNutr, "ADD_NUTR" );
      
      assertEquals( "60000", listAddNutr.get(0).getNDB_No() );
      assertEquals( "208", listAddNutr.get(0).getNutr_No() );
      assertEquals( 0.0, listAddNutr.get(0).getNutr_Val() );
      
   }
   
   @Test
   public void testReadAddWeight()
   {
      readFile( "~60000~^~1~^1.00^~container (5.3 oz)~^150.00^^^^", listAddWgt, "ADD_WGT" );
      
      assertEquals( "60000", listAddWgt.get(0).getNDB_No());
      assertEquals( 1.0, listAddWgt.get(0).getAmount());
      assertEquals( "container (5.3 oz)", listAddWgt.get(0).getMsre_Desc());
      assertEquals( 150.00, listAddWgt.get(0).getGm_Wgt());
      
   }
   
   @Test
   public void testReadChgFood()
   {
      readFile( "~60000~^~0100~^~test_name00~^~TEST_NAME00~^~~^~~^~Y~^~~^1^~~^^^^", listChgFood, "CHG_FOOD" );
      
      assertEquals( "60000", listChgFood.get(0).getNDB_No() );
      assertEquals( "test_name00", listChgFood.get(0).getLong_desc() );
      assertEquals( "TEST_NAME00", listChgFood.get(0).getShrt_desc());
      assertEquals( 1, listChgFood.get(0).getRefuse_percent());
      
   }
   
   @Test
   public void testReadChgNutrient()
   {
      readFile( "~60000~^~208~^1.0^0^^~7~^~Z~^~~^~~^^^^^^^~~^09/2015^", listChgNutr, "CHG_NUTR" );
      
      assertEquals( "60000", listChgNutr.get(0).getNDB_No() );
      assertEquals( "208", listChgNutr.get(0).getNutr_No() );
      assertEquals( 1.0, listChgNutr.get(0).getNutr_Val() );
      
   }
   
   @Test
   public void testReadChgWeight()
   {
      readFile( "~60000~^~1~^1.00^~container (5.3 oz)~^200.00^^^^", listChgWgt, "CHG_WGT" );
      
      assertEquals( "60000", listChgWgt.get(0).getNDB_No());
      assertEquals( 1.00, listChgWgt.get(0).getAmount());
      assertEquals( "container (5.3 oz)", listChgWgt.get(0).getMsre_Desc());
      assertEquals( 200.00, listChgWgt.get(0).getGm_Wgt());
      
   }
   
   @Test
   public void testReadDelFood()
   {
      readFile( "~60000~^~TEST_NAME00~", listDelFood, "DEL_FOOD" );
      
      assertEquals( "60000", listDelFood.get(0).getNDB_No() );
      assertEquals( "TEST_NAME00", listDelFood.get(0).getShrt_desc());
      
   }
   
   @Test
   public void testReadDelNutrient()
   {
      readFile( "~60000~^~208~", listDelNutr, "DEL_NUTR" );
      
      assertEquals( "60000", listDelNutr.get(0).getNDB_No() );
      assertEquals( "208", listDelNutr.get(0).getNutr_No() );
      
   }
   
   @Test
   public void testReadDelWeight()
   {
      readFile( "~60000~^~1~^1.00^~container (5.3 oz)~^200.00^^^^", listDelWgt, "DEL_WGT" );
      
      assertEquals( "60000", listDelWgt.get(0).getNDB_No());
      assertEquals( 1.00, listDelWgt.get(0).getAmount());
      assertEquals( "container (5.3 oz)", listDelWgt.get(0).getMsre_Desc());
      assertEquals( 200.00, listDelWgt.get(0).getGm_Wgt());
      
   }
   
/*****************
 * METHODS TO TEST
 *****************/
   
   public static <T> void readFile( String strToAdd , List<T> TList , String strFileType )
   {
      TList.clear();
      
      try {
            String strLine = strToAdd;
            
            switch( strFileType )
            {
               case "ADD_FOOD":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructFood Object = new FileStructFood(
                          strList.get(0),
                          strList.get(2),
                          strList.get(3),
                          Integer.valueOf(strList.get(8)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "ADD_NUTR":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructNutrient Object = new FileStructNutrient(
                          strList.get(0),
                          strList.get(1),
                          Double.valueOf(strList.get(2)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "ADD_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructWeight Object = new FileStructWeight(
                          strList.get(0),
                          Double.valueOf(strList.get(2)),
                          strList.get(3),
                          Double.valueOf(strList.get(4)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "CHG_FOOD":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructFood Object = new FileStructFood(
                          strList.get(0),
                          strList.get(2),
                          strList.get(3),
                          Integer.valueOf(strList.get(8)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "CHG_NUTR":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructNutrient Object = new FileStructNutrient(
                          strList.get(0),
                          strList.get(1),
                          Double.valueOf(strList.get(2)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "CHG_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructWeight Object = new FileStructWeight(
                          strList.get(0),
                          Double.valueOf(strList.get(2)),
                          strList.get(3),
                          Double.valueOf(strList.get(4)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "DEL_FOOD":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructFood Object = new FileStructFood(
                          strList.get(0),
                          strList.get(1));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "DEL_NUTR":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructNutrient Object = new FileStructNutrient(
                          strList.get(0),
                          strList.get(1));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "DEL_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileStructWeight Object = new FileStructWeight(
                          strList.get(0),
                          Double.valueOf(strList.get(2)),
                          strList.get(3),
                          Double.valueOf(strList.get(4)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "LOG_FILE":
               {
                  ArrayList<String> strList = ParseLogFile( strLine );
                  TraceMessage Object = new TraceMessage(
                          strList.get(0),
                          strList.get(1));
                  TList.add( (T)Object );  
                  break;
               }
            }
         
      } catch ( Exception ex ) {
         Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   private static ArrayList<String> ParseUpdateFile(String strRawString)
   {
      ArrayList<String> strList = new ArrayList<>();
      strList.clear();
      
      String[] tokens = strRawString.split("[\\^]");   /* old regex: "[\\^.\\^.]" */
      for(int i=0; i<tokens.length; i++)
      {
         tokens[i] = tokens[i].replaceAll("~", "");
         if(tokens[i].equals(""))
            strList.add("null");
         else
            strList.add(tokens[i]);
      }
      
      return strList;
   }
   
   private static ArrayList<String> ParseLogFile(String strRawString)
   {
      ArrayList<String> strList = new ArrayList<>();
      strList.clear();
      
      if(strRawString.matches(".*\\[.*\\].*"))
      {
         String[] tokens = strRawString.split("\\s*-\\s*");
         strList.add(tokens[0]);
         strList.add(tokens[1]);
      }
      else
      {
         strList.add("");
         strList.add(strRawString);
      }
      
      return strList;
   }
   
}

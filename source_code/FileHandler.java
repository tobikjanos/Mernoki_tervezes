/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.*;

/**
 *
 * @author Dzsákom
 */
public class FileHandler {
   
   /**
    *
    * @param strFilePath    - path of given file          e.g. "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_NUTR.txt"
    * @param strFileType    - type of the given file      e.g. AddFood
    * @param TList          - list to fill with data      e.g. List<FileFoodStruct> ListFFS
    */
   public static <T> void readFile( String strFilePath , List<T> TList , String strFileType )
   {
      TList.clear();
      
      try {
         
         File file = FileUtils.getFile(strFilePath);
         LineIterator iter = FileUtils.lineIterator(file);
         
         while( iter.hasNext() )
         {
            String strLine = iter.next();
            
            
            switch( strFileType )
            {
               case "ADD_FOOD":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileFoodStruct Object = new FileFoodStruct(
                          Integer.valueOf(strList.get(0)),
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
                  FileNutrientStruct Object = new FileNutrientStruct(
                          Integer.valueOf(strList.get(0)),
                          Integer.valueOf(strList.get(1)),
                          Double.valueOf(strList.get(2)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "ADD_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileWeightStruct Object = new FileWeightStruct(
                          Integer.valueOf(strList.get(0)),
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
                  FileFoodStruct Object = new FileFoodStruct(
                          Integer.valueOf(strList.get(0)),
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
                  FileNutrientStruct Object = new FileNutrientStruct(
                          Integer.valueOf(strList.get(0)),
                          Integer.valueOf(strList.get(1)),
                          Double.valueOf(strList.get(2)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "CHG_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileWeightStruct Object = new FileWeightStruct(
                          Integer.valueOf(strList.get(0)),
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
                  FileFoodStruct Object = new FileFoodStruct(
                          Integer.valueOf(strList.get(0)),
                          strList.get(1));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "DEL_NUTR":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileNutrientStruct Object = new FileNutrientStruct(
                          Integer.valueOf(strList.get(0)),
                          Integer.valueOf(strList.get(1)));
                  TList.add( (T)Object );
                  System.out.println(Object.toString());
                  break;
               }
               case "DEL_WGT":
               {
                  ArrayList<String> strList = ParseUpdateFile( strLine );
                  FileWeightStruct Object = new FileWeightStruct(
                          Integer.valueOf(strList.get(0)),
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
         }
         //System.out.println( "Total line: " + moduleCounter );
         iter.close();
         
      } catch (IOException ex) {
         Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   /*
   public static void readFile(String FilePath) throws IOException
   {
   // "D:\\EGYETEM\\Szakdolgozat\\sr28upd\\ADD_FOOD.txt"
   BufferedReader br = new BufferedReader(new FileReader(FilePath));
   
   String line = null;
   int cnt = 0;
   while((line=br.readLine())!=null)
   {
   cnt++;
   System.out.println(line);
   ArrayList<String> list = (ArrayList<String>) parseString(line);
   for(int i_List=0; i_List<list.size(); i_List++)
   {
   System.out.print(list.get(i_List) + " | ");
   }
   System.out.print("\n");
   }
   System.out.println(cnt);
   }
   */
   
   /**
    *
    * @param strRawString  - param to be tokenized
    * @return              - return string list of tokens
    */
   private static ArrayList<String> ParseUpdateFile(String strRawString)
   {
      ArrayList<String> strList = new ArrayList<>();
      strList.clear();
      
      String[] tokens = strRawString.split("[\\^]");   /* old regex: "[\\^.\\^.]" */
      for(int i=0; i<tokens.length; i++)
      {
         tokens[i] = tokens[i].replaceAll("~", "");
         if(tokens[i].equals(""))
            strList.add("0");
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
   
   public static void WriteTraceMessagesToFile(File file, ArrayList<TraceMessage> listMessages)
   {
      try {
         FileUtils.writeLines(file, listMessages);
      } catch (IOException ex) {
         Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   public static void main(String[] args) throws IOException
   {
      
      /*
      TODO
      . karaktert NULL-nak veszi, a parser külön elemként kezeli -> parser átírása -- DONE
      többi fájl olvasása, hogy minden működik-e
      */
   }
}

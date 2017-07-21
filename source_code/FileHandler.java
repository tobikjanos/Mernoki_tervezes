/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    * @param <T>            - type of the given struct    e.g. FileFoodStruct
    * @param strFilePath    - path of given file          e.g. "D:\\EGYETEM\\Szakdolgozat\\Mernoki_tervezes\\update files\\sr28upd\\ADD_NUTR.txt"
    * @param strFileType    - type of the given file      e.g. FileFoodStruct
    * @param TList          - list to fill with data      e.g. List<FileFoodStruct> ListFFS
    */
   public static <T> void readFile( String strFilePath , List<T> TList , String strFileType )
   {
      try {
         
         File File = FileUtils.getFile(strFilePath);
         LineIterator iter = FileUtils.lineIterator(File);
         
         int lineCounter = 0;
         
         while( iter.hasNext() )
         {
            String strLine = iter.next();
            //System.out.println(strLine);
            ArrayList<String> strList = parseString( strLine );
            //System.out.println(strList.toString());
            
            /**
             * TEST START - seems OK
             */
            
            switch( strFileType )
            {
               case "FileFoodStruct":
               {
                  FileFoodStruct Object = new FileFoodStruct(
                       Integer.valueOf(strList.get(0)),
                       strList.get(3),
                       strList.get(2),
                       Integer.valueOf(strList.get(8)));
                  TList.add( (T)Object );
                  break;
               }
               case "FileNutrientStruct":
               {
                  FileNutrientStruct Object = new FileNutrientStruct(
                       Integer.valueOf(strList.get(0)),
                       Integer.valueOf(strList.get(1)),
                       Double.valueOf(strList.get(2)));
                  TList.add( (T)Object );
                  break;
               }
            }
            
            /**
             * TEST END
             */
            
            
            lineCounter++;
         }
         System.out.println( "Total line: " + lineCounter );
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
   public static ArrayList<String> parseString(String strRawString)
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
   
   public static void main(String[] args) throws IOException
   {
      FileHandler fileHandler = new FileHandler();

      /*
      TODO
      . karaktert NULL-nak veszi, a parser külön elemként kezeli -> parser átírása -- DONE
      többi fájl olvasása, hogy minden működik-e
      */
   }
}

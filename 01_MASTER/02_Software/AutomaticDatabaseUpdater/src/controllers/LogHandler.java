/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import descriptors.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TobikJanos
 */
public class LogHandler {
   
   private static List<TraceMessage> listMessages;
   private static File logFile;
   
   /**
    * brief: create file with the current timestamp that will consists of log data
    */
   public static void CreateLogFile()
   {
      String timeStamp = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(Calendar.getInstance().getTime());

      try {
         logFile = new File("LOG\\logfile_" + timeStamp.replace("-", "T") + ".txt");
         logFile.getParentFile().mkdirs();
         logFile.createNewFile();
         System.out.println(logFile.getPath());
      } catch (IOException ex) {
         Logger.getLogger(LogHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      return;
   }
   
   /**
    * brief: add element to listMessages
    * @param status     - status of the message
    * @param data       - data of the message
    */
   public static void AddElement(String status, String data)
   {
      TraceMessage msg = new TraceMessage(status, data);
      listMessages.add(msg);
   }
   
   /**
    * brief: clear listMessages
    */
   public static void ClearLogList()
   {
      listMessages = new ArrayList<>();
      listMessages.clear();
   }
   
   /**
    * brief: write listMessages to the log file
    */
   public static void WriteLogFile()
   {
      FileHandler.WriteTraceMessagesToFile(logFile, (ArrayList<TraceMessage>) listMessages);
   }
   
   public static void main(String[] args) throws IOException
   {
      
   }    
}

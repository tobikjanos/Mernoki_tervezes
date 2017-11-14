/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitTests;

import descriptors.TraceMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author TobikJanos
 */
public class UnitTestLogHandler extends TestCase {
   
   private static List<TraceMessage> listMessages;
   private static File logFile;
   
   @Before
   @Override
   public void setUp()
   {
      listMessages = new ArrayList<>();
   }
   
   public static void AddElement(String status, String data)
   {
      TraceMessage msg = new TraceMessage(status, data);
      listMessages.add(msg);
   }
   
   public static void ClearLogList()
   {
      listMessages = new ArrayList<>();
      listMessages.clear();
   }
   
   @Test
   public void testAddElement()
   {
      assertEquals( new Boolean(true).booleanValue(), listMessages.isEmpty() );
      
      AddElement("Status", "Data");
      
      assertEquals( new Boolean(false).booleanValue(), listMessages.isEmpty() );
      assertEquals( "Status", listMessages.get(0).getStatus() );
      assertEquals( "Data", listMessages.get(0).getData() );
   }
   
   @Test
   public void testClearLogList()
   {
      assertEquals( new Boolean(true).booleanValue(), listMessages.isEmpty() );
      
      listMessages.add( new TraceMessage( "Status", "Data" ) );
      assertEquals( new Boolean(false).booleanValue(), listMessages.isEmpty() );
      
      ClearLogList();
      assertEquals( new Boolean(true).booleanValue(), listMessages.isEmpty() );
   }
   
}

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package controllers;
import automaticdatabaseupdater.MainWindowController;
import descriptors.*;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TobikJanos
 */
public class DatabaseHandler {
   
   private static String IP_ADDRESS       = "localhost";      /* localhost */
   private static String PORT             = "5432";           /* 5432 */
   private static String DATABASE_NAME    = "lavinia";        /* lavinia */
   private static String USERNAME         = "postgres";       /* postgres */
   private static String PASSWORD         = "qaswed123";      /* qaswed123 */
   private static String SCHEMA           = "minta2";         /* minta */
   private static String VERSION          = "30";             /* 28 */
   
   private static MainWindowController controller;
   
   
   
   public static void createAddFoodFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addFood(" + "\n" +
              "   ndb_no text," + "\n" +
              "   shrt_desc text," + "\n" +
              "   long_desc text," + "\n" +
              "   refuse_percent integer" + "\n" +
              ")" + "\n" +
              "RETURNS void" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "   var_food_id integer;" + "\n" +
              "   var_label_id integer;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   perform _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';" + "\n" +
              "   if found then RAISE EXCEPTION 'food_id már létezik (NDB_No = %)', $1; end if;" + "\n\n" +
              
              "   var_food_id := nextval('"+SCHEMA+".food_food_id_seq');" + "\n" +
              "   var_label_id := nextval('"+SCHEMA+".label_label_id_seq');" + "\n\n" +
              
              "   insert into "+SCHEMA+".label (label_id, label_type_code) values (var_label_id, 3);	-- 3 is food name" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( label )'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".label_text (label_id, lang_id, label_text, label_long_text) values (var_label_id, 2, $2, $3); -- 2 is english" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( label_text )'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".food (food_id, foodname_label_id, sd_id) values (var_food_id, var_label_id, 36); -- sd_id -> security desc" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( food )'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".food_source(food_id, source_id, content_unit_id, content_unit_q, refuse_percent, source_link_no)" + "\n" +
              "   values(var_food_id, 1, 15, 10, $4, 'USDA:SR"+VERSION+":' || $1 || ' SOTE:null'); -- VERSION is from java code" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( food_source )'; end if;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addFood");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> addFood");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addFood");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addFood");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createAddNutrientFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addNutrient(" + "\n" +
              "     ndb_no text," + "\n" +
              "     nutr_no text," + "\n" +
              "     nutr_val double precision" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "     var_retval text;" + "\n" +
              "     var_food_id integer;" + "\n" +
              "     var_nutr_id integer;" + "\n" +
              "     cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "     var_retval := '';" + "\n\n" +
              
              "     var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "     if var_nutr_id is null then RAISE EXCEPTION 'nutr_id nem létezik ( Nutr_No = % )', $2; end if;" + "\n\n" +
              
              "     open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';" + "\n" +
              "     loop" + "\n" +
              "        fetch cursor_food_id into var_food_id;" + "\n" +
              "        exit when not found;" + "\n\n" +
              
              "        insert into "+SCHEMA+".food_content(food_id, source_id, nutr_id, fc_quantity)" + "\n" +
              "        values(var_food_id, 1, var_nutr_id, $3);" + "\n" +
              "        if not found then RAISE EXCEPTION 'Insert nem sikerült ( food_content )'; end if;" + "\n\n" +
              
              "        var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "     end loop;" + "\n" +
              "     close cursor_food_id;" + "\n" +
              
              "     if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "     return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addNutrient");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> addNutrient");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addNutrient");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addNutrient");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createAddWeightFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addWeight(" + "\n" +
              "     NDB_No text," + "\n" +
              "     Unit_label text," + "\n" +
              "     Weight double precision" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "     var_retval text;" + "\n" +
              "     var_food_id integer;" + "\n" +
              "     var_unit_id integer;" + "\n" +
              "     var_label_id integer;" + "\n" +
              "     cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';" + "\n" +
              "   loop" + "\n" +
              "      fetch cursor_food_id into var_food_id;" + "\n" +
              "      exit when not found;" + "\n\n" +
              
              "      perform _lt.label_text from "+SCHEMA+".label_text _lt where label_text = $2;" + "\n" +
              "      if found then" + "\n\n" +
              
              "         var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u " + "\n" +
              "                        left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id " + "\n" +
              "                        left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "                        where _lt.label_text = $2);" + "\n" +
              "         if var_unit_id is null then RAISE EXCEPTION 'unit_id nem létezik ( label_text = % )', $2; end if;" + "\n\n" +
              
              "         insert into "+SCHEMA+".food_units (food_id,source_id, unit_id, lang_id, scale) values (var_food_id, 1, var_unit_id, 2, $3);" + "\n" +
              "         if not found then RAISE EXCEPTION 'Insert nem sikerült ( food_units )'; end if;" + "\n" +
              
              "      else" + "\n\n" +
              
              "         var_unit_id := nextval('"+SCHEMA+".unit_unit_id_seq');" + "\n" +
              "         var_label_id := nextval('"+SCHEMA+".label_label_id_seq');" + "\n\n" +
              
              "         insert into "+SCHEMA+".label (label_id, label_type_code) values (var_label_id, 1);	-- 1 is unit name" + "\n" +
              "         if not found then RAISE EXCEPTION 'Insert nem sikerült ( label )'; end if;" + "\n\n" +
              
              "         insert into "+SCHEMA+".label_text (label_id, lang_id, label_text) values (var_label_id, 2, $2); -- 2 is english" + "\n" +
              "         if not found then RAISE EXCEPTION 'Insert nem sikerült ( label_text )'; end if;" + "\n\n" +
              
              "         insert into "+SCHEMA+".unit (unit_id, unit_label_id) values (var_unit_id, var_label_id);" + "\n" +
              "         if not found then RAISE EXCEPTION 'Insert nem sikerült ( unit )'; end if;" + "\n\n" +
              
              "         insert into "+SCHEMA+".food_units (food_id,source_id, unit_id, lang_id, scale) values (var_food_id, 1, var_unit_id, 2, $3);" + "\n" +
              "         if not found then RAISE EXCEPTION 'Insert nem sikerült ( food_units )'; end if;" + "\n\n" +
              
              "      end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;" + "\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addWeight");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> addWeight");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addWeight");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addWeight");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createChgFoodFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgFood(" + "\n" +
              "   ndb_no text," + "\n" +
              "   shrt_desc text," + "\n" +
              "   long_desc text," + "\n" +
              "   refuse_percent integer" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF " + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "   var_retval text;" + "\n" +
              "   var_food_id integer;" + "\n" +
              "   var_label_id integer;" + "\n" +
              "   cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';" + "\n" +
              "   loop" + "\n" +
              "      fetch cursor_food_id into var_food_id;" + "\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_label_id := (select _f.foodname_label_id from "+SCHEMA+".food _f where food_id = var_food_id);" + "\n" +
              "      if var_label_id is null then RAISE EXCEPTION 'label_id nem létezik ( food_id = % )', var_food_id; end if;" + "\n\n" +
              
              "      update "+SCHEMA+".label_text set label_text = $2, label_long_text = $3" + "\n" +
              "      where label_id = var_label_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Update nem sikerült ( label_text )'; end if;" + "\n\n" +
              
              "      update "+SCHEMA+".food_source set refuse_percent = $4" + "\n" +
              "      where food_id = var_food_id;	-- VERSION is from java code" + "\n" +
              "      if not found then RAISE EXCEPTION 'Update nem sikerült ( food_source )'; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgFood");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> chgFood");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgFood");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgFood");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createChgNutrientFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgNutrient(" + "\n" +
              "	ndb_no text," + "\n" +
              "	nutr_no text," + "\n" +
              "	nutr_val double precision" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "   var_retval text;" + "\n" +
              "   var_food_id integer;" + "\n" +
              "   var_nutr_id integer;" + "\n" +
              "   cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';" + "\n" +
              "   loop" + "\n" +
              "      fetch cursor_food_id into var_food_id;"+ "\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "      if var_nutr_id is null then RAISE EXCEPTION 'nutr_id nem létezik ( Nutr_No = % )', $2; end if;" + "\n\n" +
              
              "      update "+SCHEMA+".food_content set fc_quantity = $3" + "\n" +
              "      where food_id = var_food_id and nutr_id = var_nutr_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Update nem sikerült ( food_content ) % %', var_food_id, var_nutr_id; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgNutrient");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> chgNutrient");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgNutrient");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgNutrient");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createChgWeightFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgWeight(" + "\n" +
              "	NDB_No text," + "\n" +
              "	Unit_label text," + "\n" +
              "	Weight double precision" + "\n" +
              "	)    " + "\n" +
              "	RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_retval text;" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';\n" +
              "   loop\n" +
              "      fetch cursor_food_id into var_food_id;\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u" + "\n" +
              "		   			left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id" + "\n" +
              "		   			left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "   	   			where _lt.label_text = $2);" + "\n\n" +
              "      if var_unit_id is null then RAISE EXCEPTION 'unit_id nem létezik ( label = % )', $2; end if;" + "\n\n" +
              
              "      update "+SCHEMA+".food_units set scale = $3" + "\n" +
              "      where food_id = var_food_id and unit_id = var_unit_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Update nem sikerült ( food_units )'; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgWeight");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> chgWeight");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgWeight");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgWeight");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createDelFoodFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delFood(" + "\n" +
              "   NDB_No text," + "\n" +
              "   Shrt_Desc text" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_retval text;" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_label_id integer;" + "\n" +
              "	cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';\n" +
              "   loop\n" +
              "      fetch cursor_food_id into var_food_id;\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_label_id := (select _f.foodname_label_id from "+SCHEMA+".food _f where food_id = var_food_id);" + "\n" +
              "      if var_label_id is null then RAISE EXCEPTION 'label_id nem létezik ( food_id = % )', var_food_id; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".food_source where food_id = var_food_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( food_source )'; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".food where food_id = var_food_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( food )'; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".label_text where label_id = var_label_id and label_text = $2;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( label_text )'; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".label where label_id = var_label_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( label )'; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delFood");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> delFood");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delFood");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delFood");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createDelNutrientFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delNutrient(" + "\n" +
              "	NDB_No text," + "\n" +
              "	nutr_no text" + "\n" +
              "	)" + "\n" +
              "	RETURNS text" + "\n" +
              "	LANGUAGE 'plpgsql'" + "\n" +
              "	COST 100.0" + "\n" +
              "	VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_retval text;" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_nutr_id integer;" + "\n" +
              "	cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';\n" +
              "   loop\n" +
              "      fetch cursor_food_id into var_food_id;\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%');" + "\n" +
              "      if var_food_id is null then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "      var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "      if var_nutr_id is null then RAISE EXCEPTION 'nutr_id nem létezik ( Nutr_No = % )', $2; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".food_content where food_id = var_food_id and nutr_id = var_nutr_id;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( food_content )'; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delNutrient");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> delNutrient");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delNutrient");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delNutrient");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createDelWeightFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delWeight(" + "\n" +
              "	NDB_No text," + "\n" +
              "	Unit_label text," + "\n" +
              "	Weight double precision" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_retval text;" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	cursor_food_id refcursor;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   var_retval := '';" + "\n\n" +
              
              "   open cursor_food_id for select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%';\n" +
              "   loop\n" +
              "      fetch cursor_food_id into var_food_id;\n" +
              "      exit when not found;" + "\n\n" +
              
              "      var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like 'USDA%' || $1 || ' SOTE%');" + "\n" +
              "      if var_food_id is null then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "      var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u" + "\n" +
              "					left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id" + "\n" +
              "					left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "					where _lt.label_text = $2);" + "\n" +
              "      if var_unit_id is null then RAISE EXCEPTION 'unit_id nem létezik ( label_text = % )', $2; end if;" + "\n\n" +
              
              "      delete from "+SCHEMA+".food_units where food_id = var_food_id and unit_id = var_unit_id and scale = $3;" + "\n" +
              "      if not found then RAISE EXCEPTION 'Delete nem sikerült ( food_units )'; end if;" + "\n\n" +
              
              "      var_retval := var_retval || var_food_id || ' ';" + "\n\n" +
              
              "   end loop;\n" +
              "   close cursor_food_id;" + "\n\n" +
              
              "   if var_retval = '' then RAISE EXCEPTION 'food_id nem létezik ( NDB_No = % )', $1; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delWeight");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> delWeight");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delWeight");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: delWeight");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void createAddNutrientDefiniton( Connection conn )
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addNutrDef(" + "\n" +
              "	Nutr_No text," + "\n" +
              "	Units text," + "\n" +
              "	Tagname text," + "\n" +
              "	Nutr_Desc text," + "\n" +
              "	Num_Dec integer" + "\n" +
              "	)" + "\n" +
              "    RETURNS void" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_label_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	var_nutr_id integer;" + "\n" +
              "BEGIN" + "\n\n" +
              
              "   perform _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $1 || '%';" + "\n" +
              "   if found then RAISE EXCEPTION 'nutr_id már létezik (Nutr_No = %)', $1; end if;" + "\n\n" +
              
              "   var_label_id := nextval('"+SCHEMA+".label_label_id_seq');\n" +
              "   var_nutr_id := nextval('"+SCHEMA+".nutrient_nutr_id_seq');" + "\n\n" +
              
              "   insert into "+SCHEMA+".label ( label_id, label_type_code )" + "\n" +
              "   values ( var_label_id, 4 );" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( label )'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".label_text ( label_id, lang_id, label_text, label_long_text )" + "\n" +
              "   values ( var_label_id, 2, $3, $4 );" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( label_text )'; end if;" + "\n\n" +
              
              "   var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u" + "\n" +
              "					left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id" + "\n" +
              "					left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "					where _lt.label_text ilike $2 and _lt.lang_id = 2);" + "\n" +
              "   if var_unit_id is null then RAISE EXCEPTION 'unit_id nem létezik ( label_text = % )', $2; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".nutrient ( nutr_id, nutrname_label_id, usda_nutr_no, eurofir_nutr_no, unit_id, decimals, menugene_nutr_no )" + "\n" +
              "   values ( var_nutr_id, var_label_id, 'SR" + VERSION + ":' || $1, null, var_unit_id, $5, null );" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert nem sikerült( nutrient )'; end if;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addNutrDef");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> addNutrDef");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addNutrDef");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: addNutrDef");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   
   }
   
   public static void createChgNutrientDefiniton(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgNutrDef(" + "\n" +
              "	Nutr_No text," + "\n" +
              "	Units text," + "\n" +
              "	Tagname text," + "\n" +
              "	Nutr_Desc text," + "\n" +
              "	Num_Dec integer" + "\n" +
              "	)" + "\n" +
              "    RETURNS void" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_label_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	var_nutr_id integer;" + "\n" +
              "BEGIN" + "\n\n" +
              
              
              "   var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $1 || '%');"+"\n" +
              "   if var_nutr_id is null then RAISE EXCEPTION 'nutr_id nem létezik ( Nutr_No = % )', $1; end if;" + "\n\n" +
              
              "   var_label_id := (select _n.nutrname_label_id from "+SCHEMA+".nutrient _n where nutr_id = var_nutr_id);" + "\n" +
              "   if var_label_id is null then RAISE EXCEPTION 'nutrname_label_id nem létezik ( Nutr_No = % )', $1; end if;" + "\n\n" +
              
              "   var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u"+"\n" +
              "					left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id"+"\n" +
              "					left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id"+"\n" +
              "					where _lt.label_text ilike $2 and _lt.lang_id = 2);" + "\n" +
              "   if var_unit_id is null then RAISE EXCEPTION 'unit_id nem létezik ( label_text = % )', $2; end if;" + "\n\n" +
              
              "   update "+SCHEMA+".nutrient" + "\n" +
              "	set unit_id = var_unit_id, decimals = $5" + "\n" +
              "	where nutr_id = var_nutr_id;" + "\n" +
              "   if not found then RAISE EXCEPTION 'Update nem sikerült( nutrient )'; end if;" + "\n\n" +
                            
              "   update "+SCHEMA+".label_text" + "\n" +
              "	set label_text = $3, label_long_text = $4" + "\n" +
              "	where label_id = var_label_id and lang_id = 2;" + "\n" +
              "   if not found then RAISE EXCEPTION 'Update nem sikerült( label_text )'; end if;" + "\n\n" +
              
              "END;" + "\n" +
              "$$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgNutrDef");
         
         ps.close();
         
      } catch (SQLException ex) {
         System.err.println("***LOG: function creation error -> chgNutrDef");
         String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
         SendTraceMessage("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgNutrDef");
         SendTraceMessage("[ERRORMSG]", ErrorMessage);
         LogHandler.AddElement("[ FAILED ]", "Tárolt eljárás létrehozás nem sikerült: chgNutrDef");
         LogHandler.AddElement("[ FAILED ]", ErrorMessage);
      } finally {
         return;
      }
   }
   
   public static void executeAddFoodFunction(Connection conn, FileStructFood food) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".addFood( ?, ?, ?, ? )}");
         cstmt.registerOutParameter(1, Types.OTHER);
         cstmt.setString(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         cstmt.setString(4, food.getLong_desc());
         cstmt.setInt(5, food.getRefuse_percent());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
      } catch (SQLException ex) {
         
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute addFood function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            System.err.println(ErrorMessage);
            SendTraceMessage("[ FAILED ]", food.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", food.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   public static void executeAddNutrientFunction(Connection conn, FileStructNutrient nutrient) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".addNutrient(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, nutrient.getNDB_No());
         cstmt.setString(3, nutrient.getNutr_No());
         cstmt.setDouble(4, nutrient.getNutr_Val());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
         
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute addNutrient function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            System.err.println(ErrorMessage);
            SendTraceMessage("[ FAILED ]", nutrient.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", nutrient.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   public static void executeAddWeightFunction(Connection conn, FileStructWeight weight) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".addWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, weight.getNDB_No());
         if( (weight.getAmount() % 1.0f) == 0)
         {
            cstmt.setString(3, String.valueOf(weight.getAmount().intValue()) + " " + weight.getMsre_Desc());
         }
         else
         {
            cstmt.setString(3, String.valueOf(weight.getAmount()) + " " + weight.getMsre_Desc());
         }
         cstmt.setDouble(4, weight.getGm_Wgt()/10.0);
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute addWeight function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", weight.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", weight.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   public static void executeChgFoodFunction(Connection conn, FileStructFood food) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgFood(?, ?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         cstmt.setString(4, food.getLong_desc());
         cstmt.setInt(5, food.getRefuse_percent());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute chgFood function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", food.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", food.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   public static void executeChgNutrientFunction(Connection conn, FileStructNutrient nutrient) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgNutrient(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, nutrient.getNDB_No());
         cstmt.setString(3, nutrient.getNutr_No());
         cstmt.setDouble(4, nutrient.getNutr_Val());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute chgNutrient function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", nutrient.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", nutrient.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   public static void executeChgWeightFunction(Connection conn, FileStructWeight weight) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, weight.getNDB_No());
         if( (weight.getAmount() % 1.0f) == 0)
         {
            cstmt.setString(3, String.valueOf(weight.getAmount().intValue()) + " " + weight.getMsre_Desc());
         }
         else
         {
            cstmt.setString(3, String.valueOf(weight.getAmount()) + " " + weight.getMsre_Desc());
         }
         cstmt.setDouble(4, weight.getGm_Wgt()/10.0);
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute chgWeight function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", weight.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", weight.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   public static void executeDelFoodFunction(Connection conn, FileStructFood food) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".delFood(?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute delFood function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", food.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", food.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   public static void executeDelNutrientFunction(Connection conn, FileStructNutrient nutrient) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".delNutrient(?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, nutrient.getNDB_No());
         cstmt.setString(3, nutrient.getNutr_No());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute delNutrient function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", nutrient.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", nutrient.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   public static void executeDelWeightFunction(Connection conn, FileStructWeight weight) throws SQLException
   {
      CallableStatement cstmt = null;
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".delWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.LONGVARCHAR);
         cstmt.setString(2, weight.getNDB_No());
         if( (weight.getAmount() % 1.0f) == 0)
         {
            cstmt.setString(3, String.valueOf(weight.getAmount().intValue()) + " " + weight.getMsre_Desc());
         }
         else
         {
            cstmt.setString(3, String.valueOf(weight.getAmount()) + " " + weight.getMsre_Desc());
         }
         cstmt.setDouble(4, weight.getGm_Wgt()/10.0);
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
         String res = cstmt.getString(1);
         String [] array = res.split(" ");
         String foodItem;
         for(String idx : array)
         {
            foodItem = "Food ID  " + idx + "   |HU   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 1) + "   |EN   " + getFoodNameByFoodID(conn, Integer.valueOf(idx), 2);
            LogHandler.AddElement("[  INFO  ]", "Frissített élelmiszer   |" + foodItem);
         }
         
      } catch (SQLException ex) {
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute delWeight function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            SendTraceMessage("[ FAILED ]", weight.toString());
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", weight.toString());
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   public static void executeAddNutrDefFunction(Connection conn, FileStructNutrient nutrient) throws SQLException
   {
      CallableStatement cstmt = null;
      String nutr = nutrient.getNutr_No() + "   |   " + nutrient.getUnits() + 
                 "   |   " + nutrient.getTagname() + "   |   " + nutrient.getNutr_Desc() + "   |   " + nutrient.getNum_Dec();
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".addNutrDef( ?, ?, ?, ?, ? )}");
         cstmt.registerOutParameter(1, Types.OTHER);
         cstmt.setString(2, nutrient.getNutr_No());
         cstmt.setString(3, nutrient.getUnits());
         cstmt.setString(4, nutrient.getTagname());
         cstmt.setString(5, nutrient.getNutr_Desc());
         cstmt.setInt(6, nutrient.getNum_Dec());
                  
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", nutr );
         
      } catch (SQLException ex) {
         
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute addNutrDef function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            System.err.println(ErrorMessage);
            SendTraceMessage("[ FAILED ]", nutr);
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", nutr);
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   public static void executeChgNutrDefFunction(Connection conn, FileStructNutrient nutrient) throws SQLException
   {
      CallableStatement cstmt = null;
      String nutr = nutrient.getNutr_No() + "   |   " + nutrient.getUnits() + 
                 "   |   " + nutrient.getTagname() + "   |   " + nutrient.getNutr_Desc() + "   |   " + nutrient.getNum_Dec();
      try
      {
         cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgNutrDef( ?, ?, ?, ?, ? )}");
         cstmt.registerOutParameter(1, Types.OTHER);
         cstmt.setString(2, nutrient.getNutr_No());
         cstmt.setString(3, nutrient.getUnits());
         cstmt.setString(4, nutrient.getTagname());
         cstmt.setString(5, nutrient.getNutr_Desc());
         cstmt.setInt(6, nutrient.getNum_Dec());
         
         cstmt.execute();
         
         LogHandler.AddElement("[   OK   ]", nutr );
         
      } catch (SQLException ex) {
         
         if( !ex.getSQLState().equals("25P02") )
         {
            System.err.println("***LOG: execute chgNutrDef function caused error");
            String ErrorMessage = ex.getMessage().substring(0, ex.getMessage().indexOf("\n"));
            System.err.println(ErrorMessage);
            SendTraceMessage("[ FAILED ]", nutr);
            SendTraceMessage("[ERRORMSG]", ErrorMessage);
            LogHandler.AddElement("[ FAILED ]", nutr);
            LogHandler.AddElement("[ERRORMSG]", ErrorMessage);
            throw new SQLException(ex.getMessage());
         }
      } finally {
         cstmt.close();
      }
   }
   
   private static ArrayList<String> getFoodIDsByNDB_No( Connection conn, String NDB_No )
   {
      ArrayList<String> retList = new ArrayList<>();
      
      PreparedStatement stmt = null;
      ResultSet rs = null;
      
      try
      {
         String query_getFoodIds = "select food_id from "+SCHEMA+".food_source where source_link_no like 'USDA%' || ? || ' SOTE%';";
         stmt = conn.prepareStatement(query_getFoodIds);
         stmt.setString(1, NDB_No);
         
         rs = stmt.executeQuery();
         
         while( rs.next() )
         {
            Integer foodID = rs.getInt(1);
            String foodItem = getFoodNameByFoodID(conn, foodID, 1) + "\t\t" + getFoodNameByFoodID(conn, foodID, 2);
            retList.add(foodItem);
         }
         rs.close();
         stmt.close();
         
      }catch(SQLException ex){
         ex.printStackTrace();
      }
      
      return retList;
   }
   
   private static String getFoodNameByFoodID(Connection conn, Integer foodID, Integer language)
   {
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String nRet = "";
      
      try
      {
         String query_getFood = "select _f.food_id, _lt.label_text \n" +
                 "   from "+SCHEMA+".label_text _lt inner join "+SCHEMA+".label _l on _lt.label_id = _l.label_id \n" +
                 "   inner join "+SCHEMA+".food _f on _l.label_id = _f.foodname_label_id \n" +
                 "   where _f.food_id = ? and _lt.lang_id = ?;";
         stmt = conn.prepareStatement(query_getFood);
         stmt.setInt(1, foodID);
         stmt.setInt(2, language);
         
         rs = stmt.executeQuery();
         
         while(rs.next())
         {
            nRet = rs.getString(2);
         }
      } catch(SQLException ex) {
         ex.printStackTrace();
      }
      
      return nRet;
   }
   
   
   public static Connection Connect()
   {
      Connection conn = null;
      
      System.out.println("------------ PostgreSQL JDBC Connection Testing ------------");
      try {
         
         Class.forName("org.postgresql.Driver");
         
      } catch (ClassNotFoundException e) {
         
         System.out.println("Where is your PostgreSQL JDBC Driver? Include in your library path!");
         e.printStackTrace();
      }
      
      System.out.println("PostgreSQL JDBC Driver Registered!");
      
      try {
         
         conn = DriverManager.getConnection(
                 "jdbc:postgresql://" + IP_ADDRESS + ":" + PORT + "/" + DATABASE_NAME,
                 USERNAME,
                 PASSWORD);
         
      } catch (SQLException e){
         System.out.println("Connection Failed! Check output console");
         e.printStackTrace();
      }
      
      if (conn != null)
      {
         System.out.println("Connection has established to " + DATABASE_NAME);
      }
      else
      {
         System.out.println("Failed to make connection!");
      }
      
      return conn;
   }
   
   public static boolean TestConnection() throws SQLException
   {
      boolean retVal;
      
      Connection conn = null;
      try
      {
         conn = Connect();
         
      } catch (Exception ex) {
         Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      if(conn != null)
      {
         DatabaseMetaData dbmd = conn.getMetaData();
         ResultSet rs = dbmd.getSchemas();
         boolean isScheamFound = false;
         while(rs.next())
         {
            if(SCHEMA.equals(rs.getString(1)))
            {
               isScheamFound = true;
            }
         }
         
         if(isScheamFound)
         {
            retVal = true;
            System.out.println("Connected");
         }
         else
         {
            retVal = false;
            System.out.println("Not connected");
         }
         
         conn.close();
      }
      else
      {
         retVal = true;
         System.out.println("Not connected");
      }
      
      return retVal;
   }
   
   private static void SendTraceMessage(String status, String data)
   {
      controller.AddTraceMessage(status, data);
      return;
   }
   
   public static void setController(MainWindowController cntrlr) {
      controller = cntrlr;
   }
   
   public static void ClearDatabaseSetup()
   {
      IP_ADDRESS  = "";
      PORT = "";
      DATABASE_NAME = "";
      USERNAME = "";
      PASSWORD = "";
      SCHEMA = "";
      VERSION = "";
      return;
   }
   
   public static void main(String[] args) throws IOException
   {
      Connection conn = null;
      
      
      try
      {
         conn = Connect();
               
         createAddNutrientDefiniton(conn);
         createChgNutrientDefiniton(conn);
         
//         System.out.println("________________________________________________");
//         createAddFoodFunction(conn);
//         createAddNutrientFunction(conn);
//         createAddWeightFunction(conn);
//         
//         System.out.println("________________________________________________");
//         createChgFoodFunction(conn);
//         createChgNutrientFunction(conn);
//         createChgWeightFunction(conn);
//         
//         System.out.println("________________________________________________");
//         createDelFoodFunction(conn);
//         createDelNutrientFunction(conn);
//         createDelWeightFunction(conn);
         
         conn.close();
         
         
//         System.out.println("________________________________________________");
//         executeAddFoodFunction(conn, new FileFoodStruct(50016, "test_food10", "TEST_FOOD10", 0));
//         executeAddFoodFunction(conn, new FileFoodStruct(50017, "test_food11", "TEST_FOOD11", 0));

//         System.out.println("________________________________________________");
//         executeChgFoodFunction(conn, new FileFoodStruct(50016, "test_food10_1", "TEST_FOOD10_1", 100));
//         executeChgFoodFunction(conn, new FileFoodStruct(50017, "test_food11_1", "TEST_FOOD11_1", 100));

//         System.out.println("________________________________________________");
//         executeDelFoodFunction(conn, new FileFoodStruct(50016, "TEST_FOOD10_1"));
//         executeDelFoodFunction(conn, new FileFoodStruct(50017, "TEST_FOOD11_1"));

//         System.out.println("________________________________________________");
//         executeAddNutrientFunction(conn, new FileNutrientStruct(50016, 208, 999.99));
//         executeAddNutrientFunction(conn, new FileNutrientStruct(50017, 209, 999.99));

//         System.out.println("________________________________________________");
//         executeChgNutrientFunction(conn, new FileNutrientStruct(50020, 208, 10));
//         executeChgNutrientFunction(conn, new FileNutrientStruct(50021, 209, 10));

//         System.out.println("________________________________________________");
//         executeDelNutrientFunction(conn, new FileNutrientStruct(50016, 208));
//         executeDelNutrientFunction(conn, new FileNutrientStruct(50017, 209));

//         System.out.println("________________________________________________");
//         executeAddWeightFunction(conn, new FileWeightStruct(50016, 100.0, "testing desc10", 1000.0));
//         executeAddWeightFunction(conn, new FileWeightStruct(50017, 100.0, "testing desc11", 1200.0));

//         System.out.println("________________________________________________");
//         executeChgWeightFunction(conn, new FileWeightStruct(50016, 100.0, "testing desc10", 700.0));
//         executeChgWeightFunction(conn, new FileWeightStruct(50017, 100.0, "testing desc11", 600.0));

//         System.out.println("________________________________________________");
//         executeDelWeightFunction(conn, new FileWeightStruct(50016, 100.0, "testing desc10", 700.0));
//         executeDelWeightFunction(conn, new FileWeightStruct(50017, 100.0, "testing desc11", 600.0));

      }catch(SQLException e){
         System.out.println("SQLException has been caught");
      }
      
   }
   
   /*
   * Setter methods
   */
   public static void setIP_ADDRESS(String IP_ADDRESS) {
      DatabaseHandler.IP_ADDRESS = IP_ADDRESS;
   }
   
   public static void setPORT(String PORT) {
      DatabaseHandler.PORT = PORT;
   }
   
   public static void setDATABASE_NAME(String DATABASE_NAME) {
      DatabaseHandler.DATABASE_NAME = DATABASE_NAME;
   }
   
   public static void setUSERNAME(String USERNAME) {
      DatabaseHandler.USERNAME = USERNAME;
   }
   
   public static void setPASSWORD(String PASSWORD) {
      DatabaseHandler.PASSWORD = PASSWORD;
   }
   
   public static void setSCHEMA(String SCHEMA) {
      DatabaseHandler.SCHEMA = SCHEMA;
   }
   
   public static void setVERSION(String VERSION) {
      DatabaseHandler.VERSION = VERSION;
   }
   
   
   /*
   * Getter methods
   */
   public static String getIP_ADDRESS() {
      return IP_ADDRESS;
   }
   
   public static String getPORT() {
      return PORT;
   }
   
   public static String getDATABASE_NAME() {
      return DATABASE_NAME;
   }
   
   public static String getUSERNAME() {
      return USERNAME;
   }
   
   public static String getPASSWORD() {
      return PASSWORD;
   }
   
   public static String getSCHEMA() {
      return SCHEMA;
   }
   
   public static String getVERSION() {
      return VERSION;
   }
   
   @Override
   public String toString()
   {
      return IP_ADDRESS       + "\t" +
              PORT            + "\t" +
              DATABASE_NAME   + "\t" +
              USERNAME        + "\t" +
              PASSWORD        + "\t" +
              SCHEMA          + "\t" +
              VERSION;
   }
   
}

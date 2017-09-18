/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package automaticdatabaseupdate;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.codegen.types.Type;



/**
 *
 * @author Dzsákom
 */
public class DatabaseController {
   
   private static String IP_ADDRESS  = "localhost";            /* localhost */
   private static String PORT = "5432";                   /* 5432 */
   private static String DATABASE_NAME = "lavinia";          /* lavinia */
   private static String USERNAME = "postgres";               /* postgres */
   private static String PASSWORD = "qaswed123";               /* qaswed123 */
   private static String SCHEMA = "minta";                 /* minta */
   private static String VERSION = "30";                /* 28 */
   
   private static FXMLDocumentController controller;

   
   
   public static void createAddFoodFunction(Connection conn)
   {
      String createFunction = 
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addFood(" + "\n" +
              "   ndb_no integer," + "\n" +
              "   shrt_desc text," + "\n" +
              "   long_desc text," + "\n" +
              "   refuse_percent integer" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "   var_food_id integer;" + "\n" +
              "   var_label_id integer;" + "\n" +
              "   var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "   var_retval := '';" + "\n\n" +
              
              "   perform _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%';" + "\n" +
              "   if found then RAISE EXCEPTION 'Food already exists with NDB_No: %', $1; end if;" + "\n\n" +
              
              "   var_food_id := nextval('"+SCHEMA+".food_food_id_seq');" + "\n" +
              "   var_label_id := nextval('"+SCHEMA+".label_label_id_seq');" + "\n\n" +
              
              "   insert into "+SCHEMA+".label (label_id, label_type_code) values (var_label_id, 3);	-- 3 is food name" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert into label was not succeeded'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".label_text (label_id, lang_id, label_text, label_long_text) values (var_label_id, 2, $2, $3); -- 2 is english" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert into label_text was not succeeded'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".food (food_id, foodname_label_id, sd_id) values (var_food_id, var_label_id, 36); -- sd_id -> security desc" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert into food was not succeeded'; end if;" + "\n\n" +
              
              "   insert into "+SCHEMA+".food_source(food_id, source_id, content_unit_id, content_unit_q, refuse_percent, source_link_no)" + "\n" +
              "   values(var_food_id, 1, 15, 10, $4, 'USDA:SR"+VERSION+":' || $1 || ' SOTE:null'); -- VERSION is from java code" + "\n" +
              "   if not found then RAISE EXCEPTION 'Insert into food_source was not succeeded'; end if;" + "\n\n" +
              
              "   return var_retval; -- return number of foods inserted" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addFood");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> addFood");
      }
   }
   
   public static void createAddNutrientFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addNutrient(" + "\n" +
              "     ndb_no integer," + "\n" +
              "     nutr_no integer," + "\n" +
              "     nutr_val double precision" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "     var_food_id integer;" + "\n" +
              "     var_nutr_id integer;" + "\n" +
              "     var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "     var_retval := '';" + "\n\n" +
              
              "     var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "     if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "     var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "     if var_nutr_id = null then RAISE EXCEPTION 'nutr_id does not exist with Nutr_No: %', $2; end if;" + "\n\n" +
              
              "     insert into "+SCHEMA+".food_content(food_id, source_id, nutr_id, fc_quantity)" + "\n" +
              "     values(var_food_id, 1, var_nutr_id, $3);" + "\n" +
              "     if not found then RAISE EXCEPTION 'Insert into food_content was not succeeded'; end if;" + "\n\n" +
              
              "     return var_retval; -- return number of nutrients inserted" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addNutrient");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> addNutrient");
         System.out.println("ErrorCode:\t" + ex.getErrorCode());
         System.out.println("Message:\t" + ex.getMessage());
         System.out.println("SQLState:\t" + ex.getSQLState());
         System.out.println("Cause:\t" + ex.getCause());
         System.out.println("StackTrace:\t"); ex.printStackTrace();
      }
   }
   
   public static void createAddWeightFunction(Connection conn)
   {
      String createFunction = 
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".addWeight(" + "\n" +
              "     NDB_No integer," + "\n" +
              "     Unit_label text," + "\n" +
              "     Weight double precision" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "     var_food_id integer;" + "\n" +
              "     var_unit_id integer;" + "\n" +
              "     var_label_id integer;" + "\n" +
              "     var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "   var_retval := '';" + "\n\n" +
              
              "   var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "   if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "   perform _lt.label_text from "+SCHEMA+".label_text _lt where label_text = $2;" + "\n" +
              "   if found then" + "\n\n" +
              
              "      var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u " + "\n" +
              "                     left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id " + "\n" +
              "                     left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "                     where _lt.label_text = $2);" + "\n" +
              "      if var_unit_id = null then RAISE EXCEPTION 'unit_id does not exist with label: %', $2; end if;" + "\n\n" +
              
              "      insert into "+SCHEMA+".food_units (food_id,source_id, unit_id, lang_id, scale) values (var_food_id, 1, var_unit_id, 2, $3);" + "\n" +
              "      if not found then RAISE EXCEPTION 'Insert into food_units was not succeeded'; end if;" + "\n" +
              
              "   else" + "\n\n" +
              
              "      var_unit_id := nextval('"+SCHEMA+".unit_unit_id_seq');" + "\n" +              
              "      var_label_id := nextval('"+SCHEMA+".label_label_id_seq');" + "\n\n" +
              
              "      insert into "+SCHEMA+".label (label_id, label_type_code) values (var_label_id, 1);	-- 1 is unit name" + "\n" +
              "      if not found then RAISE EXCEPTION 'Insert into label was not succeeded'; end if;" + "\n\n" +
              
              "      insert into "+SCHEMA+".label_text (label_id, lang_id, label_text) values (var_label_id, 2, $2); -- 2 is english" + "\n" +
              "      if not found then RAISE EXCEPTION 'Insert into label_text was not succeeded'; end if;" + "\n\n" +
              
              "      insert into "+SCHEMA+".unit (unit_id, unit_label_id) values (var_unit_id, var_label_id);" + "\n" +
              "      if not found then RAISE EXCEPTION 'Insert into unit was not succeeded'; end if;" + "\n\n" +
              
              "      insert into "+SCHEMA+".food_units (food_id,source_id, unit_id, lang_id, scale) values (var_food_id, 1, var_unit_id, 2, $3);" + "\n" +
              "      if not found then RAISE EXCEPTION 'Insert into food_units was not succeeded'; end if;" + "\n\n" +
              
              "   end if;" + "\n\n" +
              
              "   return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addWeight");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> addWeight");
      }
   }
   
   public static void createChgFoodFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgFood(" + "\n" +
              "   ndb_no integer," + "\n" +
              "   shrt_desc text," + "\n" +
              "   long_desc text," + "\n" +
              "   refuse_percent integer" + "\n" +
              ")" + "\n" +
              "RETURNS text" + "\n" +
              "LANGUAGE 'plpgsql'" + "\n" +
              "COST 100.0" + "\n" +
              "VOLATILE NOT LEAKPROOF " + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "   var_food_id integer;" + "\n" +
              "   var_label_id integer;" + "\n" +
              "   var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "   var_retval := '';" + "\n\n" +
              
              "   var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "   if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "   var_label_id := (select _f.foodname_label_id from "+SCHEMA+".food _f where food_id = var_food_id);" + "\n" +
              "   if var_label_id = null then RAISE EXCEPTION 'label_id does not exist with food_id: %', var_food_id; end if;" + "\n\n" +
              
              "   update "+SCHEMA+".label_text set label_text = $2, label_long_text = $3" + "\n" +
              "   where label_id = var_label_id;" + "\n" +
              "   if not found then var_retval := var_retval || ' 0303'; end if;" + "\n\n" +
              
              "   update "+SCHEMA+".food_source set refuse_percent = $4, source_link_no = 'USDA:SR"+VERSION+":' || $1 || ' SOTE:null'" + "\n" +
              "   where food_source.food_id = var_food_id;	-- VERSION is from java code" + "\n" +
              "   if not found then var_retval := var_retval || ' 0304'; end if;" + "\n\n" +
              
              "   return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgFood");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> chgFood");
      }
      
   }
   
   public static void createChgNutrientFunction(Connection conn)
   {
      String createFunction = 
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgNutrient(" + "\n" +
              "	ndb_no integer," + "\n" +
              "	nutr_no integer," + "\n" +
              "	nutr_val double precision" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_food_id integer;" + "\n" +
              "    var_nutr_id integer;" + "\n" +
              "	var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "	var_retval := '';" + "\n\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "	if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "	var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "	if var_nutr_id = null then RAISE EXCEPTION 'nutr_id does not exist with Nutr_No: %', $2; end if;" + "\n\n" +
              
              "	update "+SCHEMA+".food_content set fc_quantity = $3" + "\n" +
              "	where food_id = var_food_id and nutr_id = var_nutr_id;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Update food_content was not succeeded'; end if;" + "\n\n" +
              
              "	return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgNutrient");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> chgNutrient");
      }
   }
   
   public static void createChgWeightFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgWeight(" + "\n" +
              "	NDB_No integer," + "\n" +
              "	Unit_label text," + "\n" +
              "	Weight double precision" + "\n" +
              "	)    " + "\n" +
              "	RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "	var_retval := '';" + "\n\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "	if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "	var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u" + "\n" +
              "					left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id" + "\n" +
              "					left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "					where _lt.label_text = $2);" + "\n\n" +
              "	if var_unit_id = null then RAISE EXCEPTION 'unit_id does not exist with label: %', $2; end if;" + "\n\n" +
              
              "	update "+SCHEMA+".food_units set scale = $3" + "\n" +
              "	where food_id = var_food_id and unit_id = var_unit_id;" + "\n" +              
              "	if not found then RAISE EXCEPTION 'Update food_units was not succeeded'; end if;" + "\n\n" +
              
              "	return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgWeight");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> chgWeight");
      }
   }
   
   public static void createDelFoodFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delFood(" + "\n" +
              "	NDB_No integer," + "\n" +
              "   Shrt_Desc text" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_label_id integer;" + "\n" +
              "	var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "	var_retval := '';" + "\n\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "	if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "	var_label_id := (select _f.foodname_label_id from "+SCHEMA+".food _f where food_id = var_food_id);" + "\n" +
              "	if var_label_id = null then RAISE EXCEPTION 'label_id does not exist with food_id: %', var_food_id; end if;" + "\n\n" +
              
              
              "	delete from "+SCHEMA+".food_source where food_id = var_food_id;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from food_source was not succeded'; end if;" + "\n\n" +
              
              "	delete from "+SCHEMA+".food where food_id = var_food_id;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from food was not succeded'; end if;" + "\n\n" +
              
              "	delete from "+SCHEMA+".label_text where label_id = var_label_id and label_text = $2;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from label_text was not succeded'; end if;" + "\n\n" +
              
              "	delete from "+SCHEMA+".label where label_id = var_label_id;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from label was not succeded'; end if;" + "\n\n" +
              
              "	return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delFood");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> delFood");
      }
   }
   
   public static void createDelNutrientFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delNutrient(" + "\n" +
              "	NDB_No integer," + "\n" +
              "	nutr_no integer" + "\n" +
              "	)" + "\n" +
              "	RETURNS text" + "\n" +
              "	LANGUAGE 'plpgsql'" + "\n" +
              "	COST 100.0" + "\n" +
              "	VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_nutr_id integer;" + "\n" +
              "	var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "	var_retval := '';" + "\n\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "	if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "	var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || $2 || '%');" + "\n" +
              "	if var_nutr_id = null then RAISE EXCEPTION 'nutr_id does not exist with Nutr_No: %', $2; end if;" + "\n\n" +
              
              "	delete from "+SCHEMA+".food_content where food_id = var_food_id and nutr_id = var_nutr_id;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from food_content was not succeeded'; end if;" + "\n\n" +
              
              "	return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delNutrient");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> delNutrient");
      }
   }
   
   public static void createDelWeightFunction(Connection conn)
   {
      String createFunction =
              "CREATE OR REPLACE FUNCTION "+SCHEMA+".delWeight(" + "\n" +
              "	NDB_No integer," + "\n" +
              "	Unit_label text," + "\n" +
              "	Weight double precision" + "\n" +
              "	)" + "\n" +
              "    RETURNS text" + "\n" +
              "    LANGUAGE 'plpgsql'" + "\n" +
              "    COST 100.0" + "\n" +
              "    VOLATILE NOT LEAKPROOF" + "\n" +
              "AS $var_retval$" + "\n" +
              
              "DECLARE" + "\n" +
              "	var_food_id integer;" + "\n" +
              "	var_unit_id integer;" + "\n" +
              "	var_retval text;" + "\n" +
              "BEGIN" + "\n" +
              "	var_retval := '';" + "\n\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || $1 || '%');" + "\n" +
              "	if var_food_id = null then RAISE EXCEPTION 'food_id does not exist with NDB_No: %', $1; end if;" + "\n\n" +
              
              "	var_unit_id := (select _u.unit_id from "+SCHEMA+".unit _u" + "\n" +
              "					left outer join "+SCHEMA+".label _l on _u.unit_label_id = _l.label_id" + "\n" +
              "					left outer join "+SCHEMA+".label_text _lt on _l.label_id = _lt.label_id" + "\n" +
              "					where _lt.label_text = $2);" + "\n" +
              "	if var_unit_id = null then RAISE EXCEPTION 'unit_id does not exist with label: %', $2; end if;" + "\n\n" +
              
              "	delete from "+SCHEMA+".food_units where food_id = var_food_id and unit_id = var_unit_id and scale = $3;" + "\n" +
              "	if not found then RAISE EXCEPTION 'Delete from food_units was not succeeded'; end if;" + "\n\n" +
              
              "	return var_retval;" + "\n" +
              "END;" + "\n" +
              "$var_retval$;";
      try
      {
         if(conn.isClosed()) conn = Connect();
         
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> delWeight");
         
         ps.close();
         // conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> delWeight");
      }
   }
   
   public static void executeAddFoodFunction(Connection conn, FileFoodStruct food) throws SQLException
   {      
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".addFood( ?, ?, ?, ? )}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         cstmt.setString(4, food.getLong_desc());
         cstmt.setInt(5, food.getRefuse_percent());
         
         cstmt.execute();
         System.out.println("***LOG: execute addFood has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute addFood function caused error");
         SendTraceMessage("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   
   public static void executeAddNutrientFunction(Connection conn, FileNutrientStruct nutrient) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".addNutrient(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, nutrient.getNDB_No());
         cstmt.setInt(3, nutrient.getNutr_No());
         cstmt.setDouble(4, nutrient.getNutr_Val());
         
         cstmt.execute();
         System.out.println("***LOG: execute addNutrient has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute addNutrient function caused error");
         SendTraceMessage("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   
   public static void executeAddWeightFunction(Connection conn, FileWeightStruct weight) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".addWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, weight.getNDB_No());
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
         System.out.println("***LOG: execute addWeight has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute addWeight function caused error");
         SendTraceMessage("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeChgFoodFunction(Connection conn, FileFoodStruct food) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgFood(?, ?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         cstmt.setString(4, food.getLong_desc());
         cstmt.setInt(5, food.getRefuse_percent());
         
         cstmt.execute();
         System.out.println("***LOG: execute chgFood has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute chgFood function caused error");
         SendTraceMessage("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeChgNutrientFunction(Connection conn, FileNutrientStruct nutrient) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgNutrient(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, nutrient.getNDB_No());
         cstmt.setInt(3, nutrient.getNutr_No());
         cstmt.setDouble(4, nutrient.getNutr_Val());
         
         cstmt.execute();
         System.out.println("***LOG: execute chgNutrient has been succeeded and returned: " + cstmt.getString(1));
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute chgNutrient function caused error");
         System.out.println("Message: " + ex.getMessage());
         SendTraceMessage("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeChgWeightFunction(Connection conn, FileWeightStruct weight) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".chgWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, weight.getNDB_No());
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
         System.out.println("***LOG: execute chgWeight has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute chgWeight function caused error");
         SendTraceMessage("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeDelFoodFunction(Connection conn, FileFoodStruct food) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".delFood(?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         
         cstmt.execute();
         System.out.println("***LOG: execute delFood has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", food.toString());
         
      } catch (SQLException ex) {
//         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute delFood function caused error");
         SendTraceMessage("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ FAILED ]", food.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeDelNutrientFunction(Connection conn, FileNutrientStruct nutrient) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".delNutrient(?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, nutrient.getNDB_No());
         cstmt.setInt(3, nutrient.getNutr_No());
         
         cstmt.execute();
         System.out.println("***LOG: execute delNutrient has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
		 
         LogHandler.AddElement("[   OK   ]", nutrient.toString());
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute delNutrient function caused error");
         SendTraceMessage("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ FAILED ]", nutrient.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException(ex.getMessage());
      }
   }
   public static void executeDelWeightFunction(Connection conn, FileWeightStruct weight) throws SQLException
   {
      try
      {
         if(conn.isClosed()){
            SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat megszakadt");
            conn = Connect();
         }
         
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".delWeight(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.VARCHAR);
         cstmt.setInt(2, weight.getNDB_No());
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
         System.out.println("***LOG: execute delWeight has been succeeded and returned: " + cstmt.getString(1));
         
         cstmt.close();
         // conn.close();
         
         LogHandler.AddElement("[   OK   ]", weight.toString());
         
      } catch (SQLException ex) {
//         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute delWeight function caused error");
         SendTraceMessage("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ FAILED ]", weight.toString());
         LogHandler.AddElement("[ERRORMSG]", ex.getMessage());
         throw new SQLException("REASON");
      }
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
         
      } catch (SQLException e)
      {
         System.out.println("Connection Failed! Check output console");
         e.printStackTrace();
      }
      
      if (conn != null)
      {
         System.out.println("Connection has established to " + DATABASE_NAME);
         SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat létrehozása sikeres");
      }
      else
      {
         System.out.println("Failed to make connection!");
         SendTraceMessage("[  INFO  ]", "Adatbázis-kapcsolat létrehozása sikertelen");
      }
      
      return conn;
   }
   
   private static void SendTraceMessage(String status, String data)
   {
      controller.AddTraceMessage(status, data);
   }
   
   public static void setController(FXMLDocumentController cntrlr) {
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
   }
   
   public static void main(String[] args) throws IOException
   {
      Connection conn = null;
      
      
      //test foods: 50001, 50002, 50003, 50004
      try
      {
         conn = Connect();
         
         System.out.println("________________________________________________");
         createAddFoodFunction(conn);
         createAddNutrientFunction(conn);
         createAddWeightFunction(conn);
         
         System.out.println("________________________________________________");
         createChgFoodFunction(conn);
         createChgNutrientFunction(conn);
         createChgWeightFunction(conn);
         
         System.out.println("________________________________________________");
         createDelFoodFunction(conn);
         createDelNutrientFunction(conn);
         createDelWeightFunction(conn);
         
         
         
         
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
         
         System.out.println("________________________________________________");
         executeChgNutrientFunction(conn, new FileNutrientStruct(50020, 208, 10));
         executeChgNutrientFunction(conn, new FileNutrientStruct(50021, 209, 10));
         
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
         
         if(!conn.isClosed())
         {
            conn.close();
         }
      }catch(SQLException e){
         System.out.println("SQLException has been caught");
      }

   }
   
   /*
    * Setter methods 
    */
   public static void setIP_ADDRESS(String IP_ADDRESS) {
      DatabaseController.IP_ADDRESS = IP_ADDRESS;
   }
   
   public static void setPORT(String PORT) {
      DatabaseController.PORT = PORT;
   }
   
   public static void setDATABASE_NAME(String DATABASE_NAME) {
      DatabaseController.DATABASE_NAME = DATABASE_NAME;
   }
   
   public static void setUSERNAME(String USERNAME) {
      DatabaseController.USERNAME = USERNAME;
   }
   
   public static void setPASSWORD(String PASSWORD) {
      DatabaseController.PASSWORD = PASSWORD;
   }
   
   public static void setSCHEMA(String SCHEMA) {
      DatabaseController.SCHEMA = SCHEMA;
   }
   
   public static void setVERSION(String VERSION) {
      DatabaseController.VERSION = VERSION;
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

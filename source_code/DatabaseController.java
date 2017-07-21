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
   
   private static String IP_ADDRESS  = "localhost";
   private static String PORT = "5432";
   private static String DATABASE_NAME = "lavinia";
   private static String USERNAME = "postgres";
   private static String PASSWORD = "qaswed123";
   private static String SCHEMA = "minta";
   private static String VERSION = "28";
   
   
   public static void executeAddFoodFunction(Connection conn, FileFoodStruct food) throws SQLException
   {
      //other: use PgArray
      
      try
      {
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".addFood( ?, ?, ?, ? )}");
         cstmt.registerOutParameter(1, Types.INTEGER);
         cstmt.setInt(2, food.getNDB_No());
         cstmt.setString(3, food.getShrt_desc());
         cstmt.setString(4, food.getLong_desc());
         cstmt.setInt(5, food.getRefuse_percent());
         
         
         cstmt.execute();
         System.out.println("***LOG: execute addFood has been succeeded and returned: " + cstmt.getInt(1));
         
         cstmt.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute addFood function caused error");
      }
   }
   
   public static void executeAddNutrientFunction(Connection conn, FileNutrientStruct nutrient)
   {
      try
      {
         CallableStatement cstmt = conn.prepareCall("{? = call "+SCHEMA+".addNutrient(?, ?, ?)}");
         cstmt.registerOutParameter(1, Types.INTEGER);
         cstmt.setInt(2, nutrient.getNDB_No());
         cstmt.setInt(3, nutrient.getNutr_No());
         cstmt.setDouble(4, nutrient.getNutr_Val());
         
         cstmt.execute();
         System.out.println("***LOG: execute addNutrient has been succeeded and returned: " + cstmt.getInt(1));
         
         cstmt.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: execute addNutrient function caused error");
      }
   }
   
   public static void createAddFoodFunction(Connection conn)
   {
      String createFunction = "CREATE OR REPLACE FUNCTION "+SCHEMA+".addFood(NDB_No integer, Long_Desc text, Shrt_Desc text, Refuse_percent integer) " +
              "RETURNS integer AS $printStrings$\n" +
              "DECLARE\n" +
              "	var_retval integer;\n" +
              "	var_food_id integer;\n" +
              "	var_label_id integer;\n" +
              "BEGIN\n" +
              "       var_retval := 0;\n" +
              "       perform _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || NDB_No || '%'; \n" +
              "       if found then \n" +
              "           return var_retval; \n" +
              "       end if; \n" +
              
              "	var_food_id := nextval('"+SCHEMA+".food_food_id_seq');\n" +
              "	var_label_id := nextval('"+SCHEMA+".label_label_id_seq');\n" +
              
              "	insert into "+SCHEMA+".label (label_id, label_type_code) values (var_label_id, 3);	-- 3 is food name\n" +
              "	insert into "+SCHEMA+".label_text (label_id, lang_id, label_text, label_long_text) values (var_label_id, 2, Shrt_Desc, Long_Desc); -- 2 is english\n" +
              "	insert into "+SCHEMA+".food (food_id, foodname_label_id, sd_id) values (var_food_id, var_label_id, 36); -- sd_id -> security desc\n" +
              
              "	insert into "+SCHEMA+".food_source(food_id, source_id, content_unit_id, content_unit_q, refuse_percent, source_link_no)\n" +
              "	values(var_food_id, 1, 15, 10, Refuse_Percent, 'USDA:SR"+VERSION+":' || NDB_No || ' SOTE:null'); -- VERSION is from java code\n" +
              
              "       var_retval := 1;\n" +
              
              "	return var_retval; -- return number of foods inserted\n" +
              
              "END;\n" +
              "$printStrings$ LANGUAGE plpgsql;";
      try
      {
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addFood");
         
         ps.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> addFood");
      }
   }
   
   public static void createAddNutrientFunction(Connection conn)
   {
      String createFunction = "CREATE OR REPLACE FUNCTION "+SCHEMA+".addNutrient(NDB_No integer, Nutr_No integer, Nutr_Val double precision) " +
              "RETURNS integer AS $printStrings$\n" +
              "DECLARE\n" +
              "	var_retval integer;\n" +
              "	var_food_id integer;\n" +
              "       var_nutr_id integer;\n" +
              "BEGIN\n" +
              "       var_retval := 0;\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || NDB_No || '%');\n" +
              "       var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || Nutr_No || '%');\n" +
              
              "	insert into "+SCHEMA+".food_content(food_id, source_id, nutr_id, fc_quantity)\n" +
              "	values(var_food_id, 1, var_nutr_id, Nutr_Val);\n" +
              
              "       var_retval := 1;\n" +
              
              "	return var_retval; -- return number of nutrients inserted\n" +
              "END;\n" +
              "$printStrings$ LANGUAGE plpgsql;";
      try
      {
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> addNutrient");
         
         ps.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> addNutrient");
      }
   }
   
   public static void createAddWeightFunction(Connection conn)
   {
      
   }
   
   public static void createChgFoodFunction(Connection conn)
   {
      String createFunction = "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgfood(ndb_no integer, long_desc text, shrt_desc text, refuse_percent integer)\n" +
              "RETURNS integer AS $function$\n" +
              "DECLARE\n" +
              "	var_retval integer;\n" +
              "	var_food_id integer;\n" +
              "	var_label_id integer;\n" +
              "BEGIN\n" +
              "	var_retval := 0;\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || NDB_No || '%');\n" +
              "	var_label_id := (select _f.foodname_label_id from "+SCHEMA+".food _f where food_id = var_food_id);\n" +
              
              "	update "+SCHEMA+".label_text set label_text = shrt_desc, label_long_text = long_desc\n" +
              "	where label_id = var_label_id;\n" +
              
              "	update "+SCHEMA+".food_source set refuse_percent = refuse_percent, source_link_no = 'USDA:"+VERSION+":' || NDB_No || ' SOTE:null'\n" +
              "	where food_id = var_food_id;	-- VERSION is from java code\n" +
              
              "       var_retval := 1;\n" +
              "	return var_retval; -- return number of foods inserted\n" +
              "END;\n" +
              
              "$function$ LANGUAGE plpgsql;";
      try
      {
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgFood");
         
         ps.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> chgFood");
      }
      
   }
   
   public static void createChgNutrientFunction(Connection conn)
   {
      String createFunction = "CREATE OR REPLACE FUNCTION "+SCHEMA+".chgnutrient(ndb_no integer, nutr_no integer, nutr_val double precision)\n" +
              "RETURNS integer AS $function$\n" +
              "DECLARE\n" +
              "	var_retval integer;\n" +
              "	var_food_id integer;\n" +
              "       var_nutr_id integer;\n" +
              "BEGIN\n" +
              "	var_retval := 0;\n" +
              
              "	var_food_id := (select _fs.food_id from "+SCHEMA+".food_source _fs where source_link_no like '%' || NDB_No || '%');\n" +
              "	var_nutr_id := (select _n.nutr_id from "+SCHEMA+".nutrient _n where _n.usda_nutr_no like '%' || Nutr_No || '%');\n" +
              
              "	update "+SCHEMA+".food_content set fc_quantity = nutr_val\n" +
              "	where food_id = var_food_id and nutr_id = var_nutr_id;\n" +
              
              "	var_retval := 1;\n" +
              "	return var_retval; -- return number of nutrients inserted\n" +
              "END;\n" +
              
              "$function$ LANGUAGE plpgsql;";
      try
      {
         PreparedStatement ps = conn.prepareStatement(createFunction);
         ps.execute();
         System.out.println("***LOG: function has been created -> chgNutrient");
         
         ps.close();
         conn.close();
         
      } catch (SQLException ex) {
         Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex);
         System.out.println("***LOG: function creation error -> chgNutrient");
      }
   }
   
   public static void createChgWeightFunction(Connection conn)
   {
      
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
      }
      else
      {
         System.out.println("Failed to make connection!");
      }
      
      return conn;
   }
   
   public static void main(String[] args) throws IOException
   {
      Connection conn = Connect();
      System.out.println("SCHEMA: " + SCHEMA);
      
      createAddFoodFunction(conn);
      createAddNutrientFunction(conn);
      
      createChgFoodFunction(conn);
      createChgNutrientFunction(conn);
      
      //test foods: 50001, 50002, 50003, 50004
      try{
         //executeAddFoodFunction(conn, new FileFoodStruct(50001, "test_food1", "TEST_FOOD1", 0));
         executeAddFoodFunction(conn, new FileFoodStruct(50008, "test_food2", "TEST_FOOD2", 0));
         
         conn.close();
         
      }catch(SQLException e){
         System.out.println("SQLException has been caught");
      }
      
      //executeAddNutrientFunction(conn, new FileNutrientStruct(50003, 208, 999.99));
      //executeAddNutrientFunction(conn, new FileNutrientStruct(50004, 209, 999.99));
      
      
   }
   
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
}

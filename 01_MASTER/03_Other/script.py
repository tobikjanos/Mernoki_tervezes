import psycopg2
import sys, re

conn = psycopg2.connect( database="lavinia", user="postgres", password="qaswed123", host="127.0.0.1", port="5432" )

cur = conn.cursor()

#	READ AND INSERT DATA FROM TEXT FILE
#
#with open('SQLFile.txt', 'r', encoding="utf-8") as content_file:
#	content = content_file.read()
#
#cur.execute( content )
#


query = "select substring(source_link_no from 11 for 5) as ndb_no, count( substring(source_link_no from 11 for 5) ) \
from minta2.food_source                                                                                                                                                  		\
where source_link_no != '' and source_link_no not like 'USDA:0 %'                                                                                   		\
group by substring(source_link_no from 11 for 5)                                                                                                              		\
having count( substring(source_link_no from 11 for 5) ) > 1                                                                                              		\
order by substring(source_link_no from 11 for 5)"

with open("result.txt", "w+") as file:
	

	try:
		#execute query
		cur.execute(query)
		#fetch rows into list
		list = cur.fetchall()
		#display results
		for row in list:
			ndb_no = row[0]
			counter = row[1]
			file.write("" + "\n")
			file.write("NDB_No: " + ndb_no + "   |   Food_ID   |" + "   darabszám: "  + str(counter) + "\n")
			file.write("----------------+-------------+---------------" + "\n")
			query = "select food_id from minta2.food_source where source_link_no like 'USDA%' || '" + ndb_no + "' || ' SOTE%'"
			#execute query
			cur.execute(query)
			#fetch rows into list
			listFoodID = cur.fetchall()
			#display results
			for idx in listFoodID:
				
				food_id = idx[0]
				file.write("                |   " + str(food_id) + "\n")
				
				
			
		
	except:
		print("Error\n")



conn.commit()
conn.close()
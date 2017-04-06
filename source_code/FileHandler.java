/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticdatabaseupdate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dzsákom
 */
public class FileHandler {
    
    public static void main(String[] args) throws IOException
    {
        FileHandler program = new FileHandler();
        
        String filePath = "D:\\EGYETEM\\Szakdolgozat\\sr28upd\\ADD_FOOD.txt";
        
        program.readFile(filePath);
                
    }
    
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
    
    // put string tokens into arraylist
    public static List<String> parseString(String rawString)
    {
        List<String> strList = new ArrayList<>();
        strList.clear();
        
        String[] tokens = rawString.split("[\\^.\\^.]");
        for(int i=0; i<tokens.length; i++)
        {
            tokens[i] = tokens[i].replaceAll("~", "");
            if(tokens[i].equals(""))
                strList.add("NULL");
            else
                strList.add(tokens[i]);
        }
        
        return strList;
    }
    
}

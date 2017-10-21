/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package descriptors;

/**
 *
 * @author TobikJanos
 */
public class FileStructFood {
    
    private String NDB_No;
    private String Long_desc;
    private String Shrt_desc;
    private int refuse_percent;

    public FileStructFood(String NDB_No, String Long_desc, String Shrt_desc, int refuse_percent) {
        this.NDB_No = NDB_No;
        this.Shrt_desc = Shrt_desc;
        this.Long_desc = Long_desc;
        this.refuse_percent = refuse_percent;
    }
    
    public FileStructFood(String NDB_No, String Shrt_desc) {
        this.NDB_No = NDB_No;
        this.Shrt_desc = Shrt_desc;
        this.Long_desc = "null";
        this.refuse_percent = 0;
    }

    public String getNDB_No() {
        return NDB_No;
    }

    public void setNDB_No(String NDB_No) {
        this.NDB_No = NDB_No;
    }

    public String getShrt_desc() {
        return Shrt_desc;
    }

    public void setShrt_desc(String Shrt_desc) {
        this.Shrt_desc = Shrt_desc;
    }

    public String getLong_desc() {
        return Long_desc;
    }

    public void setLong_desc(String Long_desc) {
        this.Long_desc = Long_desc;
    }

    public int getRefuse_percent() {
        return refuse_percent;
    }

    public void setRefuse_percent(int refuse_percent) {
        this.refuse_percent = refuse_percent;
    }
    
    @Override
    public String toString()
    {
       return  this.NDB_No + "   |   " +
               this.Long_desc + "   |   " +
               this.Shrt_desc + "   |   " + 
               this.refuse_percent;
    }
}

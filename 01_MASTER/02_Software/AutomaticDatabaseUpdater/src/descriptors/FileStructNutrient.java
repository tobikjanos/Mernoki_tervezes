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
public class FileStructNutrient {
    
    private String NDB_No;
    private String Nutr_No;
    private double Nutr_Val;
    
    private String Units;
    private String Tagname;
    private String Nutr_Desc;
    private Integer Num_Dec;

    public FileStructNutrient(String NDB_No, String Nutr_No, double Nutr_Val) {
        this.NDB_No = NDB_No;
        this.Nutr_No = Nutr_No;
        this.Nutr_Val = Nutr_Val;
    }
    
    public FileStructNutrient(String NDB_No, String Nutr_No) {
        this.NDB_No = NDB_No;
        this.Nutr_No = Nutr_No;
        this.Nutr_Val = 0.0;
    }

   public String getUnits() {
      return Units;
   }

   public void setUnits(String Units) {
      this.Units = Units;
   }

   public String getTagname() {
      return Tagname;
   }

   public void setTagname(String Tagname) {
      this.Tagname = Tagname;
   }

   public String getNutr_Desc() {
      return Nutr_Desc;
   }

   public void setNutr_Desc(String Nutr_Desc) {
      this.Nutr_Desc = Nutr_Desc;
   }

   public Integer getNum_Dec() {
      return Num_Dec;
   }

   public void setNum_Dec(Integer Num_Dec) {
      this.Num_Dec = Num_Dec;
   }

   public FileStructNutrient(String Nutr_No, String Units, String Tagname, String Nutr_Desc, Integer Num_Dec) {
      this.Nutr_No = Nutr_No;
      this.Units = Units;
      this.Tagname = Tagname;
      this.Nutr_Desc = Nutr_Desc;
      this.Num_Dec = Num_Dec;
   }

    public String getNDB_No() {
        return NDB_No;
    }

    public void setNDB_No(String NDB_No) {
        this.NDB_No = NDB_No;
    }

    public String getNutr_No() {
        return Nutr_No;
    }

    public void setNutr_No(String Nutr_No) {
        this.Nutr_No = Nutr_No;
    }

    public double getNutr_Val() {
        return Nutr_Val;
    }

    public void setNutr_Val(double Nutr_Val) {
        this.Nutr_Val = Nutr_Val;
    }
    
    @Override
    public String toString()
    {
       return  this.NDB_No + "   |   " +
               this.Nutr_No + "   |   " + 
               this.Nutr_Val;
    }
    
}

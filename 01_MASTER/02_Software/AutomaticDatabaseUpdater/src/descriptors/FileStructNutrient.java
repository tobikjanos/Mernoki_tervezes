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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticdatabaseupdate;

/**
 *
 * @author Dzsákom
 */
public class FileNutrientStruct {
    private int NDB_No;
    private int Nutr_No;
    private double Nutr_Val;

    public FileNutrientStruct(int NDB_No, int Nutr_No, double Nutr_Val) {
        this.NDB_No = NDB_No;
        this.Nutr_No = Nutr_No;
        this.Nutr_Val = Nutr_Val;
    }

    public int getNDB_No() {
        return NDB_No;
    }

    public void setNDB_No(int NDB_No) {
        this.NDB_No = NDB_No;
    }

    public int getNutr_No() {
        return Nutr_No;
    }

    public void setNutr_No(int Nutr_No) {
        this.Nutr_No = Nutr_No;
    }

    public double getNutr_Val() {
        return Nutr_Val;
    }

    public void setNutr_Val(double Nutr_Val) {
        this.Nutr_Val = Nutr_Val;
    }
}

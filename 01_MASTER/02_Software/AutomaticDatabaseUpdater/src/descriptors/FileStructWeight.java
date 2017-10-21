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
public class FileStructWeight {
   
   private String NDB_No;
   private Double Amount;
   private String Msre_Desc;
   private double Gm_Wgt;

   public FileStructWeight(String NDB_No, double Amount, String Msre_Desc, double Gm_Wgt) {
      this.NDB_No = NDB_No;
      this.Amount = Amount;
      this.Msre_Desc = Msre_Desc;
      this.Gm_Wgt = Gm_Wgt;
   }

   public String getNDB_No() {
      return NDB_No;
   }

   public void setNDB_No(String NDB_No) {
      this.NDB_No = NDB_No;
   }

   public Double getAmount() {
      return Amount;
   }

   public void setAmount(Double Amount) {
      this.Amount = Amount;
   }

   public String getMsre_Desc() {
      return Msre_Desc;
   }

   public void setMsre_Desc(String Msre_Desc) {
      this.Msre_Desc = Msre_Desc;
   }

   public double getGm_Wgt() {
      return Gm_Wgt;
   }

   public void setGm_Wgt(double Gm_Wgt) {
      this.Gm_Wgt = Gm_Wgt;
   }
   
   @Override
    public String toString()
    {
       return  this.NDB_No + "   |   " +
               this.Amount + "   |   " + 
               this.Msre_Desc + "   |   " +
               this.Gm_Wgt;
    }
    
}

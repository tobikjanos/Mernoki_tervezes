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
public class TraceMessage {
   
   String status;
   String data;

   public TraceMessage(String status, String data) {
      this.status = status;
      this.data = data;
   }
   
   public TraceMessage() {}

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getData() {
      return data;
   }

   public void setData(String data) {
      this.data = data;
   }
   
   @Override
   public String toString(){
      return this.status + "   -   " + 
              this.data;
   }
   
}

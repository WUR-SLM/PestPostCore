/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestpostcore;

/**
 *
 * @author wesse016
 */
public class Node {
  public int id;
  public double z;
  public double c;
  public double c1;
  
  public Node(){
    id = -1;
    z = -1.0;
    c = -1.0;
    c1 = -1.0;
  }
  
  public void setNewValue(double aValue){
    c1 = c;
    c = aValue;
  }

}

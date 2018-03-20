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
public class Element {
  public int id;
  public int topNode;
  public int bottomNode;
  public double size;
  
  public Element(){
    id = -1;
    topNode = -1;
    bottomNode = -1;
    size = -1.0;
  }
  
  public void setNodes(Node aTop, Node aBottom){
    topNode = aTop.id;
    bottomNode = aBottom.id;
    size = aTop.z - aBottom.z;
  }
}

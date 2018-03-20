/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestpostcore;

import java.util.ArrayList;

/**
 *
 * @author wesse016
 */
public class FEM {
  private ArrayList<Node> node;
  private ArrayList<Element> element;
  private double[][] matrix;
  private double[] vector;
  private double[] c;
  private double dispersionCoefficient; // D
  private double waterVelocity; // v
  private double retardationFactor; // R
  private double degradationCoefficient; // mu
  private double dT;
  private double infiltrationDepth;
  private double stepSize;
   
  public FEM(){
    node = new ArrayList<Node>();
    element = new ArrayList<Element>();
  }
  
  public double getCafterStep(int aDepth){
      double myC = c[aDepth];
      return myC;
  }
  
  public String getCValues(){
    String result = "";
    for (Node myNode : node){
      result = result.concat(",").concat(((Double)myNode.c).toString());
    }
    return result;
  }
  
  private void createGrid(int aN){
    node.clear();
    element.clear();
    double zBottom = 0.0;
    double zTop = 0.0;
    double dz = 1.0;
    for (int i=0; i<aN; i++){
      zTop = zBottom;
      zBottom = zBottom - dz;
      Node myNode = new Node();
      myNode.id = i;
      myNode.z = zTop;
      node.add(myNode);
      if (i > 0){
        Element myElement = new Element();
        myElement.topNode = i-1;
        myElement.bottomNode = i;
        myElement.size = zTop - zBottom;
        element.add(myElement);
      }
    }
    int n = node.size();
    matrix = new double[n][3];
    vector = new double[n];
  }
  
  public double[] solve(){
    int n = vector.length;
    
//    double[] diff = new double[n];
//    double[] old_vec = new double[n];
//    double[][] old_mat = new double[n][3];
//    for(int i=0; i<n; i++){
//      old_vec[i] = vector[i];
//      old_mat[i][0] = matrix[i][0];
//      old_mat[i][1] = matrix[i][1];
//      old_mat[i][2] = matrix[i][2];
//    }
    
    double[] x = new double[n];
    // top row
    matrix[0][2] = matrix[0][2] / matrix[0][1];
    vector[0] = vector[0] / matrix[0][1];
    
    // center rows
    for (int i=1; i<n-1; i++){
      matrix[i][1] = matrix[i][1] - matrix[i][0] * matrix[i-1][2];
      vector[i] = vector[i] - matrix[i][0] * vector[i-1];  
      matrix[i][2] = matrix[i][2] / matrix[i][1];
      vector[i] = vector[i] / matrix[i][1];
    }
    
    // last row
    matrix[n-1][1] = matrix[n-1][1] - matrix[n-1][0] * matrix[n-2][2];
    vector[n-1] = vector[n-1] - vector[n-2] * matrix[n-1][0];
    
    // back substitution
    x[n-1] = vector[n-1] / matrix[n-1][1];
    for (int i=n-2; i>=0; i--){
      x[i] = vector[i] - matrix[i][2] * x[i+1];
    }
    
//    //check
//    diff[0] = old_vec[0] - old_mat[0][1] * x[0] - old_mat[0][2] * x[1];
//    for (int i=1; i<n-1; i++){
//      diff[i] = old_vec[i] - old_mat[i][0] * x[i-1] - old_mat[i][1] * x[i] - old_mat[i][2] * x[i+1];
//    }
//    diff[n-1] = old_vec[n-1] - old_mat[n-1][0] * x[n-2] - old_mat[n-1][1] * x[n-1];
    
    return x;
  }
  
  private void setInitialValue(double aInitialConcentration){
    for (Node myNode : node){
      myNode.c = aInitialConcentration;
    }
  }
  
  private void clearMatrixAndVector(){
    for (int i=0; i< vector.length; i++){
      matrix[i][0] = 0.0;
      matrix[i][1] = 0.0;
      matrix[i][2] = 0.0;
      vector[i] = 0.0;
    }
  }
  
  private void fillMatrixAndVector(){
    int i;
    int j;
    double x;
    for (Element myElement : element){
      i = myElement.topNode;
      j = myElement.bottomNode;
      matrix[i][1] = matrix[i][1] + dispersionCoefficient / myElement.size;
      matrix[i][2] = matrix[i][2] - dispersionCoefficient / myElement.size;
      matrix[j][1] = matrix[j][1] + dispersionCoefficient / myElement.size;
      matrix[j][0] = matrix[j][0] - dispersionCoefficient / myElement.size;
      
      x = (retardationFactor * myElement.size / 3.0) /dT;
      matrix[i][1] = matrix[i][1] + x;
      matrix[j][1] = matrix[j][1] + x;
      vector[i] = vector[i] + x * node.get(i).c;
      vector[j] = vector[j] + x * node.get(j).c;
      x = (retardationFactor * myElement.size / 6.0)/dT;
      matrix[i][2] = matrix[i][2] + x;
      matrix[j][0] = matrix[j][0] + x;
      vector[i] = vector[i] + x * node.get(j).c;
      vector[j] = vector[j] + x * node.get(i).c;
      
      x = degradationCoefficient * retardationFactor * myElement.size / 3.0;
      matrix[i][1] = matrix[i][1] + x;
      matrix[j][1] = matrix[j][1] + x;
      vector[i] = vector[i] + x * node.get(i).c;
      vector[j] = vector[j] + x * node.get(j).c;
      x = degradationCoefficient * retardationFactor * myElement.size / 6.0;
      matrix[i][2] = matrix[i][2] + x;
      matrix[j][0] = matrix[j][0] + x;
     
      if(node.get(i).z < infiltrationDepth){
        matrix[i][1] = matrix[i][1] + 0.5 * waterVelocity;
        matrix[i][2] = matrix[i][2] + 0.5 * waterVelocity;
      }
      if (node.get(j).z < infiltrationDepth){
        matrix[j][1] = matrix[j][1] + 0.5 * waterVelocity;
        matrix[j][0] = matrix[j][0] + 0.5 * waterVelocity;
      }
    }
  }
  
  private void setTopValue(double aValue){
    matrix[0][1] = 1.0;
    matrix[0][2] = 0.0;
    vector[0] = aValue;
  }
  
  private void setNewValues(double[] aValue){
    for (Node myNode: node){
      if (aValue[myNode.id] < 0.0){
        myNode.setNewValue(0.0);
      }
      else
      {
        myNode.setNewValue(aValue[myNode.id]);
      }
    }
  }
  
  public void initialize(int aN, double aRetardation, double aDegradation, double aDispersion, 
          double aInitialConcentration, double aStepSize){
    dispersionCoefficient = aDispersion;
    retardationFactor = aRetardation;
    waterVelocity = 0.00000;
    degradationCoefficient = aDegradation;
    stepSize = aStepSize;

    createGrid(aN);
    double[] c = new double[vector.length];
    setInitialValue(aInitialConcentration);
    
    
  }
  public void runFEMForOneStep(double aDepth, double aVelocity, double aSurfaceConcentration){
    waterVelocity = aVelocity;
 //   waterVelocity = 0.00001;
    infiltrationDepth = -1.0 * aDepth;
    dT = 0.05;
    double tEnd = stepSize;
    double t = 0;
    while (t < tEnd) {
      t = t + dT;
      clearMatrixAndVector();
      fillMatrixAndVector();
      if(waterVelocity > 0.000001){
        setTopValue(aSurfaceConcentration);
      }
      c = solve();
      setNewValues(c);
//      System.out.print(t);
//      System.out.print(" ");
//      System.out.print(c[0]);
//      System.out.print(" ");
//      System.out.print(c[9]);
//      System.out.print(" ");
//      System.out.print(c[19]);
//      System.out.print(" ");
//      System.out.print(c[29]);
//      System.out.print(" ");
//      System.out.print(c[39]);
//      System.out.print(" ");
//      System.out.print(c[49]);
//      System.out.print(" ");
//      System.out.print(c[59]);
//      System.out.print(" ");
//      System.out.print(c[69]);
//      System.out.print(" ");
//      System.out.print(c[79]);
//      System.out.println();
    }
    
  }

}

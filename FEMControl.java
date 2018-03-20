/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestpostcore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;



/**
 *
 * @author wesse016
 */
public class FEMControl {
  private FEM myFEM;
  public double[][] valueToStore;
  private int[] requiredDepth;
  private double[] surfaceConcentration;
  private StringBuilder outputString;
  private String lineSeparator;
  
  public FEMControl(){
    
  }
  public FEMControl(int[] aDepth, int aT, int aN, double aRetardation, double aDegradation,
          double aDispersion, double aInitialConcentration, double[] aTop, double aTopConcentration,
          double aStepSize){
    lineSeparator = System.getProperty("line.separator");
    outputString = new StringBuilder();
    myFEM = new FEM();
    myFEM.initialize(aN, aRetardation, aDegradation, aDispersion, aInitialConcentration, aStepSize);
    valueToStore = new double[aDepth.length][aT];
    requiredDepth = new int[aDepth.length];
    for (int i=0; i<aDepth.length; i++){
      requiredDepth[i] = aDepth[i];
    }
    surfaceConcentration = new double[aT];
    for (int i=0; i<aT; i++){
      if (aTopConcentration < -10.0){
        surfaceConcentration[i] = aTop[i];
      }
      else
      {
        surfaceConcentration[i] = aTopConcentration;
      }
    }
  }
  
  
  public void runFEM(double[] aSurfaceContent, double[] aInfiltration, double[] aDepth, double aThetaFC, double aVelocity){
    double myInfiltration = 0;
    double velocity = 0;
    try{
      if (aVelocity < -1.0){
        for (int i=0; i<aInfiltration.length; i++){
          if(i==0){
            myInfiltration = aInfiltration[0];
          }
          else
          {
            myInfiltration = aInfiltration[i] - aInfiltration[i-1];
          }
          velocity = myInfiltration / aThetaFC;
          myFEM.runFEMForOneStep(aDepth[i], velocity, aSurfaceContent[i]);
          for (int j=0; j<requiredDepth.length; j++){
            valueToStore[j][i] = myFEM.getCafterStep(requiredDepth[j]);
          }
        }
      }
      else
      {
        velocity = aVelocity;
        for (int i=1; i< surfaceConcentration.length; i++){
          myFEM.runFEMForOneStep(0.0, velocity, surfaceConcentration[i]);
          outputString = outputString.append(Integer.toString(i).concat(myFEM.getCValues().concat(lineSeparator)));
        }
      }
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }   
  }
  
  public void storeOutput(String aFile){
    try{
      try{
        File myFile = new File(aFile);
        myFile.createNewFile();

        BufferedWriter myWriter = new BufferedWriter(new FileWriter(myFile));
        myWriter.write(outputString.toString());
        myWriter.close();
      }
      catch (Exception e){
        System.out.println(e.getMessage());
      }
    }
    finally
    {
    }
  }
}

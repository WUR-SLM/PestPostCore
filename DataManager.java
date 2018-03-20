/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pestpostcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 *
 * @author wesseling
 */
public class DataManager {
  private String lineSeparator;
  public String logLine;
  private StringBuilder log;
  private NetcdfFileWriteable fileFC;
  private NetcdfFileWriteable fileInfil;
  private NetcdfFileWriteable fileSurfaceWater;
  private NetcdfFileWriteable fileVelocity;
  private NetcdfFileWriteable fileRunoff;
  private NetcdfFileWriteable fileSedimentTransport;
  private NetcdfFileWriteable fileSurfaceContent;
  
  private NetcdfFile fileFCread;
  private NetcdfFile fileInfilRead;
  private NetcdfFile fileSurfaceWaterRead;
  private NetcdfFile fileVelocityRead;
  private NetcdfFile fileRunoffRead;
  private NetcdfFile fileSedimentTransportRead;
  private NetcdfFile fileContentRead;
  private NetcdfFileWriteable[] fileConcentration;
  
  public DataManager(){
    log = new StringBuilder();
    lineSeparator = System.getProperty("line.separator");
    fileFC = null;
    fileInfil = null;
    fileSurfaceWater = null;
    fileInfilRead = null;
    fileSurfaceWaterRead = null;
    fileRunoffRead = null;
    fileSedimentTransportRead = null;
    fileConcentration = new NetcdfFileWriteable[10];
  }
  
   
  private List _listeners = new ArrayList();
  public synchronized void addEventListener(AddToLogEventClassListener listener)	{
        boolean //<editor-fold defaultstate="collapsed" desc="comment">
                add
                //</editor-fold>
 = _listeners.add(listener);
  }
  public synchronized void removeEventListener(AddToLogEventClassListener listener)	{
      _listeners.remove(listener);
  }

    // call this method whenever you want to notify
    //the event listeners of the particular event
  private synchronized void fireEvent()	{
    AddToLogEventClass event = new AddToLogEventClass(this);
    Iterator i = _listeners.iterator();
    while(i.hasNext())	{
      ((AddToLogEventClassListener)i.next()).handleAddToLogEventClassEvent(event);
    }
  }

  private void showMessage(String aString){
    logLine = aString.concat(lineSeparator);
    fireEvent();
  }
  
  public StringBuilder readFile(String aFile){
    StringBuilder result = new StringBuilder();
    try {
      FileReader fr = new FileReader(aFile);
      BufferedReader input = new BufferedReader(fr);
      String line;
      try {
        while ((line = input.readLine()) != null) {
          result.append(line + lineSeparator);
        }
      }
      finally {
        input.close();
      }
    }
    catch(Exception ex) {
      showMessage(ex.getMessage());
    }
    return result;
  }

       
  public ArrayList<Integer> readOrder(String aFile){
    ArrayList<Integer> myOrder = new ArrayList<Integer>();
    BufferedReader input = null;
    Integer nr;
    try{ 
      try {
        FileReader fr = new FileReader(aFile);
        input = new BufferedReader(fr);
        String line = input.readLine();
   
        while (line != null) {
          String[] parts = line.split(",");
          try{
            nr = Integer.parseInt(parts[1].trim());
          }
          catch(Exception e)
          {
            nr = -1;
          }
          myOrder.add(nr);
          line = input.readLine();
        }
      }
      catch(Exception ex) {
        showMessage(ex.getMessage());
      }
    }
      finally
      {
      try {
        input.close();
      } catch (IOException ex) {
      }
      }
      
    return myOrder;
  }
       
    public void storeFile(String aFile, String aContent, Boolean aAppend){
      try{
//        char buffer[] = new char[aContent.length()]; 
//        aContent.getChars(0, aContent.length(), buffer, 0); 
//        FileWriter f0 = new FileWriter(aFile, true);
//        for (int i=0; i < buffer.length; i++) { 
//          f0.write(buffer[i]); 
//        } 
//        f0.close(); 
        FileWriter fw  = new FileWriter(aFile, aAppend);
        fw.write(aContent);
        fw.close();        
      }
      catch (Exception ex) {
        showMessage(ex.getMessage());
      }
    }

  public void createThetaFCFile(String aFile, int aX, int aY){
    try {
      try{
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension yDim = ncfFile.addDimension("y", aY);
       
        ArrayList dims = new ArrayList();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "units", "m");
  
        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        ncfFile.addVariable("ThetaFC", DataType.DOUBLE, dims);
        
        ncfFile.create();
        
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
             
        ArrayDouble.D2 newData = new ArrayDouble.D2(yValues.size(), xValues.size());
        double s = -99999;
        for (int j=0; j<yDim.getLength(); j++) {
          for (int i=0; i<xDim.getLength(); i++) {
            newData.set(j,i,s);
          }
        }
        int[] origin = new int[] {0,0};
        ncfFile.write("ThetaFC", origin, newData);
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File for thetaFC created");
      } 
    }


  public void createInfiltrationFile(String aFile, int aX, int aY, int aT){
    try {
      try{
       ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();
        dims.clear();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        
        ncfFile.addVariable("Infiltration", DataType.DOUBLE, dims);
        ncfFile.addVariable("InfilDepth", DataType.DOUBLE, dims);
        
        ncfFile.create();
                    
        ArrayDouble.D1 yVar = new ArrayDouble.D1(yValues.size());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayDouble.D1 xVar = new ArrayDouble.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
       ArrayDouble.D1 tVar = new ArrayDouble.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
 
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("Infiltration", origin, newData);
            ncfFile.write("InfilDepth", origin, newData);            
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for infiltration data created");
      } 
    }

  public void createSurfaceWaterFile(String aFile, int aX, int aY, int aT){
    try {
      try{
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();
        dims.clear();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        ncfFile.addVariable("SurfaceWater", DataType.DOUBLE, dims);
        
        ncfFile.create();
                    
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
       ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i< tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
 
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("SurfaceWater", origin, newData);
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for surface water created");
      } 
    }

  public void createVelocityFile(String aFile, int aX, int aY, int aT){
    try {
      try{
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();
        dims.clear();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        ncfFile.addVariable("Velocity", DataType.DOUBLE, dims);
        
        ncfFile.create();
                    
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
       ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
 
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("Velocity", origin, newData);
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for velocities created");
      } 
    }

  public void createRunoffFile(String aFile, int aX, int aY, int aT){
    try {
      try{
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();
        dims.clear();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        ncfFile.addVariable("Runoff", DataType.DOUBLE, dims);
        
        ncfFile.create();
                    
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
       ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
 
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("Runoff", origin, newData);
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for runoff created");
      } 
    }


  public void createSedimentTransportFile(String aFile, int aX, int aY, int aT){
    try {
      try{
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();
        dims.clear();
        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        for (Integer i=0; i<10; i++){
          String q = "Q".concat(i.toString());
          ncfFile.addVariable(q, DataType.DOUBLE, dims);
        }
        
        ncfFile.create();
                    
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
       ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
 
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            for (Integer i=0; i<10; i++){
              String q = "Q".concat(i.toString());
              ncfFile.write(q, origin, newData);
            }
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for detachment created");
      } 
    }


  public void createConcentrationFile(String aFile, int aX, int aY, int aZ, int aT){
    try {
      try{
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();

        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        ncfFile.addVariable("Concentration", DataType.DOUBLE, dims);
        
        ncfFile.create();
        
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
        ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
             
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("Concentration", origin, newData);
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for concentration data created");
      } 
    }


 public void createSurfaceContentFile(String aFile, int aX, int aY, int aT){
    try {
      try{
        ArrayList<Integer> yValues = new ArrayList<Integer>();
        ArrayList<Integer> xValues = new ArrayList<Integer>();
        ArrayList<Integer> tValues = new ArrayList<Integer>();
        
        for (int i=0; i<aY; i++){
          yValues.add(i);
        }
        for (int i=0; i<aX; i++){
          xValues.add(i);
        }
        for (int i=0; i<aT; i++){
          tValues.add(i);
        }
        
        // check existing file
        File myFile = new File(aFile);
        if (myFile.exists()){
          myFile.delete();
        }
        //Create new netcdf-3 file with the given filename
        NetcdfFileWriteable ncfFile =  NetcdfFileWriteable.createNew(aFile, true);
             
        Dimension yDim = ncfFile.addDimension("y", aY);
        Dimension xDim = ncfFile.addDimension("x", aX);
        Dimension tDim = ncfFile.addDimension("t", aT);
       
        ArrayList dims = new ArrayList();

        dims.add(yDim);
        ncfFile.addVariable("y", DataType.INT, dims);
        ncfFile.addVariableAttribute("y", "Units", "m");
      
        dims.clear();
        dims.add(xDim);
        ncfFile.addVariable("x", DataType.INT, dims);
        ncfFile.addVariableAttribute("x", "Units", "m");

        dims.clear();
        dims.add(tDim);
        ncfFile.addVariable("t", DataType.INT, dims);
        ncfFile.addVariableAttribute("t","Units", "step");

        dims.clear();
        dims.add(yDim);
        dims.add(xDim);
        dims.add(tDim);
        ncfFile.addVariable("Content", DataType.DOUBLE, dims);
        
        ncfFile.create();
        
        ArrayLong.D1 yVar = new ArrayLong.D1(yDim.getLength());
        for (int i=0; i<yValues.size(); i++){
          yVar.set(i, yValues.get(i));
        }
        ncfFile.write("y", yVar);
                    
        ArrayLong.D1 xVar = new ArrayLong.D1(xDim.getLength());
        for (int i=0; i<xValues.size(); i++){
          xVar.set(i, xValues.get(i));
        }
        ncfFile.write("x", xVar);
        
        ArrayLong.D1 tVar = new ArrayLong.D1(tDim.getLength());
        for (int i=0; i<tValues.size(); i++){
          tVar.set(i, tValues.get(i));
        }
        ncfFile.write("t", tVar);
             
        int[] origin = new int[3];
        origin[2] = 0;
        ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, tValues.size());
        double s = -99999;
        for (int i=0; i<tDim.getLength(); i++) {
          newData.set(0,0,i,s);
        }
        for (int k=0; k<yDim.getLength(); k++) {
          origin[0] = k;
          for (int j=0; j<xDim.getLength(); j++) {
            origin[1] = j;
            ncfFile.write("Content", origin, newData);
          }
        }
        if (ncfFile != null){
         try {
            ncfFile.close();
         } 
         catch (Exception e) {
            log = log.append(e.getMessage() + lineSeparator);
          }
        }
      }
      catch (Exception e) {
        log = log.append(e.getMessage() + lineSeparator);
      }
      } 
      finally {
//        showMessage("File " + aFile + " for surface content data created");
      } 
    }


  public void openFCFileForWriting(String aFile){
    try{
      fileFC = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeFCFile(){
    if (fileFC != null){
      try {
        fileFC.close();
        fileFC = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openConcentrationFileForWriting(int aNumber, String aFile){
    try{
      fileConcentration[aNumber] = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeConcentrationFile(int aNumber){
    if (fileConcentration[aNumber] != null){
      try {
        fileConcentration[aNumber].close();
        fileConcentration[aNumber] = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }
 public void openSurfaceContentFileForWriting(String aFile){
    try{
      fileSurfaceContent = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeSurfaceContentFile(){
    if (fileSurfaceContent != null){
      try {
        fileSurfaceContent.close();
        fileSurfaceContent = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openVelocityFileForWriting(String aFile){
    try{
      fileVelocity = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeVelocityFile(){
    if (fileVelocity != null){
      try {
        fileVelocity.close();
        fileVelocity = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openRunoffFileForWriting(String aFile){
    try{
      fileRunoff = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeRunoffFile(){
    if (fileRunoff != null){
      try {
        fileRunoff.close();
        fileRunoff = null; 
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }
  
  public void openSedimentTransportFileForWriting(String aFile){
    try{
      fileSedimentTransport = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeSedimentTransportFile(){
    if (fileSedimentTransport != null){
      try {
        fileSedimentTransport.close();
        fileSedimentTransport = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void storeFCValues(double[] aValue, int aY){
    try{
      ArrayDouble.D2 newData = new ArrayDouble.D2(1, aValue.length);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,aValue[i]);
      }
      int[] origin = new int[2];
      origin[0] = aY;
      origin[1] = 0;
      fileFC.write("ThetaFC", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void openFCFileForReading(String aFile){
    try{
      fileFCread = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void closeFCFileAfterReading(){
    if (fileFCread != null){
      try {
        fileFCread.close();
        fileFCread = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openVelocityFileForReading(String aFile){
    try{
      fileVelocityRead = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void closeVelocityFileAfterReading(){
    if (fileVelocityRead != null){
      try {
        fileVelocityRead.close();
        fileVelocityRead = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openRunoffFileForReading(String aFile){
    try{
      fileRunoffRead = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void closeRunoffFileAfterReading(){
    if (fileRunoffRead != null){
      try {
        fileRunoffRead.close();
        fileRunoffRead = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public void openSedimentTransportFileForReading(String aFile){
    try{
      fileSedimentTransportRead = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void closeSedimentTransportFileAfterReading(){
    if (fileSedimentTransportRead != null){
      try {
        fileSedimentTransportRead.close();
        fileSedimentTransportRead = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

  public double[] getFCValues(int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileFCread.findVariable("ThetaFC");
        if(myVar == null){
          showMessage("ThetaFC not found!");
        }
        else
        {
          int[] dims = new int[2];
          dims[0] = 1;
          dims[1] = aN;
          int[] origin = new int[2];
          origin[0] = aY;
          origin[1] = 0;
          
          ArrayDouble.D2 dataArray = (ArrayDouble.D2)myVar.read(origin, dims);
          for(Integer j=0; j<dims[1]; j++){
            data[j] = dataArray.get(0,j);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }
 
  public void openInfilFileForWriting(String aFile){
    try{
      fileInfil = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }

  public void openSurfaceWaterFileForWriting(String aFile){
    try{
      fileSurfaceWater = NetcdfFileWriteable.openExisting(aFile); 
    }
    catch (Exception e){
      log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void storeConcentration(int aNumber, int aY, int aX, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, 1, aValue.length);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,0,i,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = aX;
      origin[2] = 0;
      fileConcentration[aNumber].write("Concentration", origin, newData);      
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }

  public void storeSurfaceContent(int aT, int aY, int aX, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileSurfaceContent.write("Content", origin, newData);
      fileSurfaceContent.flush();
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }

  public void storeInfiltration(int aT, int aY, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileInfil.write("Infiltration", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void storeInfiltrationDepth(int aT, int aY, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileInfil.write("InfilDepth", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void storeSurfaceWater(int aT, int aY, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileSurfaceWater.write("SurfaceWater", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
public void storeVelocity(int aT, int aY, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileVelocity.write("Velocity", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void storeRunoff(int aT, int aY, double[] aValue){
    try{
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileRunoff.write("Runoff", origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void storeSedimentTransport(int aDirection, int aT, int aY, double[] aValue){
    try{
      Integer dir = aDirection; 
      ArrayDouble.D3 newData = new ArrayDouble.D3(1, aValue.length, 1);
      for (int i=0; i<aValue.length; i++) {
         newData.set(0,i,0,aValue[i]);
      }
      int[] origin = new int[3];
      origin[0] = aY;
      origin[1] = 0;
      origin[2] = aT;
      fileSedimentTransport.write("Q".concat(dir.toString()), origin, newData);
    } 
    catch (Exception e) {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }
  
  public void closeInfilFile(){
    if (fileInfil != null){
      try {
        fileInfil.close();
        fileInfil = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }
  
  public void closeSurfaceWaterFile(){
    if (fileSurfaceWater != null){
      try {
        fileSurfaceWater.close();
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }
  
    public void openInfilFileForReading(String aFile){
    try{
      fileInfilRead = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeInfilFileAfterReading(){
    if (fileInfilRead != null){
      try {
        fileInfilRead.close();
        fileInfilRead = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }

    public void openSurfaceWaterFileForReading(String aFile){
    try{
      fileSurfaceWaterRead = NetcdfFile.open(aFile);
    }
    catch(Exception e)
    {
      log = log.append(e.getMessage() + lineSeparator);
    }
  }

  public void closeSurfaceWaterFileAfterReading(){
    if (fileSurfaceWaterRead != null){
      try {
        fileSurfaceWaterRead.close();
        fileSurfaceWaterRead = null;
      } 
      catch (Exception e) {
         log = log.append(e.getMessage() + lineSeparator);
       }
    }
  }



  public double[] getInfiltrationValues(int aT, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileInfilRead.findVariable("Infiltration");
        if(myVar == null){
          showMessage("Infiltration not found!");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = aN;
          dims[2] = 1;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = 0;
          origin[2] = aT;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[1]; j++){
            data[j] = dataArray.get(0,j,0);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }

  public double[] getInfiltrationValuesAsTimeSeries(int aX, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileInfilRead.findVariable("Infiltration");
        if(myVar == null){
          showMessage("Infiltration not found!");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = 1;
          dims[2] = aN;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = aX;
          origin[2] = 0;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[2]; j++){
            data[j] = dataArray.get(0,0,j);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }
 
  public double[] getInfilDepthValuesAsTimeSeries(int aX, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileInfilRead.findVariable("InfilDepth");
        if(myVar == null){
          showMessage("InfilDepth not found");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = 1;
          dims[2] = aN;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = aX;
          origin[2] = 0;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[2]; j++){
            data[j] = dataArray.get(0,0,j);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }
 

  public double[] getSurfaceWaterValues(int aT, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileSurfaceWaterRead.findVariable("SurfaceWater");
        if(myVar == null){
          showMessage("SurfaceWater not found!");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = aN;
          dims[2] = 1;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = 0;
          origin[2] = aT;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[1]; j++){
            data[j] = dataArray.get(0, j, 0);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }
 
  public double[] getVelocityValues(int aT, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileVelocityRead.findVariable("Velocity");
        if(myVar == null){
          showMessage("Velocity not found!");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = aN;
          dims[2] = 1;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = 0;
          origin[2] = aT;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[1]; j++){
            data[j] = dataArray.get(0,j,0);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }
 
  public double[] getRunoffValues(int aT, int aY, int aN){
    double[] data = new double[aN]; 
    try{
      try{
        Variable myVar = fileRunoffRead.findVariable("Runoff");
        if(myVar == null){
          showMessage("Runoff not found!");
        }
        else
        {
          int[] dims = new int[3];
          dims[0] = 1;
          dims[1] = aN;
          dims[2] = 1;
          int[] origin = new int[3];
          origin[0] = aY;
          origin[1] = 0;
          origin[2] = aT;
          
          ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
          for(Integer j=0; j<dims[1]; j++){
            data[j] = dataArray.get(0,j,0);
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }

  public double[][] getSedimentTransportValues(int aT, int aY, int aN){
    double[][] data = new double[10][aN]; 
    try{
      try{
        for (int k=0; k<10; k++){
          Integer dir = k;
          String varName = "Q".concat(dir.toString());
          Variable myVar = fileSedimentTransportRead.findVariable(varName);
          if(myVar == null){
            showMessage(varName.concat(" not found!"));
          }
          else
          {
            int[] dims = new int[3];
            dims[0] = 1;
            dims[1] = aN;
            dims[2] = 1;
            int[] origin = new int[3];
            origin[0] = aY;
            origin[1] = 0;
            origin[2] = aT;
          
            ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
            for(Integer j=0; j<dims[1]; j++){
              data[k][j] = dataArray.get(0,j,0);
            }
          }
        }
      }
      catch(Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally{
      return data;
    }
  }

    public double[] getSurfaceContentValues(int aX, int aY, int aN){
      double[] data = new double[aN]; 
      try{
        try{
          Variable myVar = fileRunoffRead.findVariable("Content");
          if(myVar == null){
            showMessage("Content not found!");
          }
          else
          {
            int[] dims = new int[3];
            dims[0] = 1;
            dims[1] = 1;
            dims[2] = aN;
            int[] origin = new int[3];
            origin[0] = aY;
            origin[1] = aX;
            origin[2] = 0;
            
            ArrayDouble.D3 dataArray = (ArrayDouble.D3)myVar.read(origin, dims);
            for(Integer j=0; j<dims[2]; j++){
              data[j] = dataArray.get(0,0,j);
            }
          }
        }
        catch(Exception e)
        {
          showMessage(e.getMessage());
        }
      }
      finally{
        return data;
      }
  }
 

    public void openContentFileForReading(String aFile){
      try{
        fileContentRead = NetcdfFile.open(aFile);
      }
      catch(Exception e)
      {
        log = log.append(e.getMessage() + lineSeparator);
      }
    }
    
    public void closeContentFileAfterReading(){
      if (fileContentRead != null){
        try {
          fileContentRead.close();
          fileContentRead = null;
        } 
        catch (Exception e) {
           log = log.append(e.getMessage() + lineSeparator);
         }
      }
    }


}

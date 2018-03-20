/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pestpostcore;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.ini4j.Ini;

/**
 *
 * @author wesse016
 */
public class Control extends Thread{
  
  private Thread controlThread;
  private String threadName;
  
  public String logLine;
  private String lineSeparator;
  
  private Dataset datasetKSat;
  private Dataset datasetThetaSat;
  private Dataset datasetThetaIni;
  private Dataset datasetLDD;
  private Dataset datasetDEM;
  private String outputDir;
  private String fileDEM;
  private String fileKSat;
  private String fileThetaSat;
  private String fileThetaIni;
  private String fileThetaFC;
  private String fileInfiltration; 
  private String fileConcentration;
  private String fileInfilBase;
  private String fileLDD;
  private String fileOrder;
  private String fileLosses;
  private String fileSurfaceWaterBase;
  private String fileSurfaceWater;
  private String fileVelocityBase;
  private String fileRunoffBase;
  private String fileVelocity;
  private String fileRunoff;
  private String fileSedimentTransportBase;
  private String fileSedimentTransport;
  private String filePesticide;
  private String filePrecipitation;
  private String filePrecipitationData;
  private String fileSurfaceContent;
  private String fileSegments;
  private String segmentContent;
  private Boolean newFilesRequired;
  private Boolean[] depthRequired;
  private int[] requiredDepth;
  
  private DataManager dataManager;
  
  private int nX;;
  private int nY;
  private int nT;
  private int nZ;
  private int skipRowsAtEnd;
  private int numberOfSegments;
  
  private double dX;
  private double dY;
  private double dXabs;
  private double dYabs;
  private double dDiag;
  private double cellArea;
  private double[][] precipitationValue;
  private File iniFile;
  
  private double[] kSatValue;
  private double[] thetaSatValue;
  private double[] thetaIniValue;
  private double[] thetaFCValue;
  private double[] zValue;
  private double[] lowSegment;
  private double[] highSegment;
  private double timeStep;
  
  private double maxCw;
  private double maxCs;
  private double initialConcentration;
  private double appliedConcentration;
  private double retardationFactor;
  private double dispersionCoefficient;
  private double degradationCoefficient;
  private double halfTime;
  private double topConcentrationFactor;
  private double initialCellContent;
  
  public Boolean singleRun;
  private double topConcentration;
  private double waterVelocity;
  private String outputFile;
   
  private ArrayList<Integer> computationOrder;
  private ArrayList<Cell> cell;

  private Double lossOfWater;
  private Double lossOfSolid;
  private Double lossByWater;
  private Double lossBySolid;
  
  private Boolean soilContentRequired;
  private Double fInfiltration;
  
  public Control(String[] args){
    try{
      String iniFileName = args[0];
      singleRun = false;
      threadName = "ControlThread";
      lineSeparator = System.getProperty("line.separator");
      segmentContent = "Step, Time, S1, S2, S3, S4, S5, Total " + lineSeparator;
      depthRequired = new Boolean[10];
      requiredDepth = new int[10];
      newFilesRequired = false;
      dataManager = new DataManager();  
//    dataManager.addEventListener(new AddToLogEventListener());
//    String iniFileName = System.getProperty("user.dir");
//    iniFileName = iniFileName.concat("/PestPost.ini");           
      iniFile = new File(iniFileName);
      if (!iniFile.exists()){
        showMessage("??? Error: file " + iniFileName + " does not exist!");
      }
      cell = new  ArrayList<>();
      computationOrder = new  ArrayList<>();
    
      readIniFile();
    
      String myString = "T,Vw,Ws,Pw,Ps" + lineSeparator;
      dataManager.storeFile(fileLosses, myString, Boolean.FALSE);
      lossOfWater = 0.0;
      lossOfSolid = 0.0;
      lossByWater = 0.0;
      lossBySolid = 0.0;
      
      gdal.AllRegister();
      Vector fileProperties = new Vector();
      fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
      Driver myDriver = gdal.GetDriverByName("PCRaster");
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
    }
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
    System.out.println(aString);
//    logLine = aString.concat(lineSeparator);
//    fireEvent();
  }
  

  public void readIniFile(){
    Ini ini = new Ini();
    
    try {
      ini.load(new FileReader(iniFile));
      fileDEM = ini.fetch("Lisem", "DEM");
      fileKSat = ini.fetch("Lisem", "KSat");
      fileThetaSat = ini.fetch("Lisem", "ThetaSat");
      fileThetaIni = ini.fetch("Lisem", "ThetaIni");
      fileInfilBase = ini.fetch("Lisem", "Infil");
      fileInfilBase = fileInfilBase.substring(0, fileInfilBase.lastIndexOf('.')+1);
      fileLDD = ini.fetch("Lisem", "Directions");
      fileSurfaceWaterBase = ini.fetch("Lisem", "SurfaceWaterHeight");
      fileSurfaceWaterBase = fileSurfaceWaterBase.substring(0, fileSurfaceWaterBase.lastIndexOf('.')+1);
      fileVelocityBase = ini.fetch("Lisem", "Velocity");
      fileVelocityBase = fileVelocityBase.substring(0, fileVelocityBase.lastIndexOf('.')+1);
      fileRunoffBase = ini.fetch("Lisem", "Runoff");
      fileRunoffBase = fileRunoffBase.substring(0, fileRunoffBase.lastIndexOf('.')+1);
      fileSedimentTransportBase = ini.fetch("Lisem", "SedimentTransport");
      fileSedimentTransportBase = fileSedimentTransportBase.substring(0, fileSedimentTransportBase.lastIndexOf('.')+1);
      filePesticide = ini.fetch("Lisem", "Pesticide");
      filePrecipitation = ini.fetch("Lisem", "Precipitation");
      filePrecipitationData = ini.fetch("Lisem", "PrecipitationData");
      timeStep = Double.parseDouble(ini.fetch("Lisem", "Timestep"));

      topConcentration = Double.parseDouble(ini.fetch("SingleRun", "cTop"));
      waterVelocity = Double.parseDouble(ini.fetch("SingleRun", "Velocity"));
      outputFile = ini.fetch("SingleRun", "OutputFile");

      nT = Integer.parseInt(ini.fetch("Pesticide", "TimeSteps"));
      nZ = Integer.parseInt(ini.fetch("Pesticide", "Depth")) + 1;
      initialConcentration = Double.parseDouble(ini.fetch("Pesticide", "InitialC"));
      appliedConcentration = Double.parseDouble(ini.fetch("Pesticide", "AppliedC"));
      retardationFactor = Double.parseDouble(ini.fetch("Pesticide", "Retardation"));
      dispersionCoefficient = Double.parseDouble(ini.fetch("Pesticide", "Dispersion"));
      degradationCoefficient = Double.parseDouble(ini.fetch("Pesticide", "Degradation"));
      halfTime = Double.parseDouble(ini.fetch("Pesticide", "HalfTime"));
      maxCw = Double.parseDouble(ini.fetch("Pesticide", "MaxCw"));
      maxCs = Double.parseDouble(ini.fetch("Pesticide", "MaxCs"));
      fInfiltration = Double.parseDouble(ini.fetch("Pesticide", "fInfiltration"));
      initialCellContent = Double.parseDouble(ini.fetch("Pesticide",  "InitialContent"));

      outputDir = ini.fetch("Output","Dir");
      fileThetaFC = ini.fetch("Output", "ThetaFC");
      fileOrder = ini.fetch("Output", "Order");
      fileLosses = ini.fetch("Output", "Losses");
      fileSegments = ini.fetch("Output", "Segments");
      newFilesRequired = ini.fetch("Output", "NewFiles").equals("1");
      
      requiredDepth[0] = Integer.parseInt(ini.fetch("Output","Depth1"));
      requiredDepth[1] = Integer.parseInt(ini.fetch("Output","Depth2"));
      requiredDepth[2] = Integer.parseInt(ini.fetch("Output","Depth3"));
      requiredDepth[3] = Integer.parseInt(ini.fetch("Output","Depth4"));
      requiredDepth[4] = Integer.parseInt(ini.fetch("Output","Depth5"));
      requiredDepth[5] = Integer.parseInt(ini.fetch("Output","Depth6"));
      requiredDepth[6] = Integer.parseInt(ini.fetch("Output","Depth7"));
      requiredDepth[7] = Integer.parseInt(ini.fetch("Output","Depth8"));
      requiredDepth[8] = Integer.parseInt(ini.fetch("Output","Depth9"));
      requiredDepth[9] = Integer.parseInt(ini.fetch("Output","Depth10"));
      
      depthRequired[0] = ini.fetch("Output", "Depth1Required").trim().equals("1");
      depthRequired[1] = ini.fetch("Output", "Depth2Required").trim().equals("1");
      depthRequired[2] = ini.fetch("Output", "Depth3Required").trim().equals("1");
      depthRequired[3] = ini.fetch("Output", "Depth4Required").trim().equals("1");
      depthRequired[4] = ini.fetch("Output", "Depth5Required").trim().equals("1");
      depthRequired[5] = ini.fetch("Output", "Depth6Required").trim().equals("1");
      depthRequired[6] = ini.fetch("Output", "Depth7Required").trim().equals("1");
      depthRequired[7] = ini.fetch("Output", "Depth8Required").trim().equals("1");
      depthRequired[8] = ini.fetch("Output", "Depth9Required").trim().equals("1");
      depthRequired[9] = ini.fetch("Output", "Depth10Required").trim().equals("1");
      
      soilContentRequired = !ini.fetch("Run",  "SurfaceOnly").trim().equals("1");
      
      skipRowsAtEnd = Integer.parseInt(ini.fetch("Skip", "Rows"));
      
      numberOfSegments = Integer.parseInt(ini.fetch("Segments", "Number"));
      lowSegment = new double[numberOfSegments];
      highSegment = new double[numberOfSegments];
      
      for (int i=0; i<numberOfSegments; i++){
        String name = "minSegment".concat(String.valueOf(i));
        highSegment[i] = -1.0 * Math.abs(Double.parseDouble(ini.fetch("Segments", name)));
        name = "maxSegment".concat(String.valueOf(i));
        lowSegment[i] = -1.0 * Math.abs(Double.parseDouble(ini.fetch("Segments", name)));
      }
      

     } catch (Exception e) {
      showMessage(e.getMessage());
    }
  }

 
  private Boolean openFiles(){
    
    Boolean ok = true;
    Integer nXKSat = -1;
    Integer nYKSat = -1;
    Integer nXThetaSat = -1;
    Integer nYThetaSat = -1;
    Integer nXThetaIni = -1;
    Integer nYThetaIni = -1;
    Integer nXLdd = -1;
    Integer nYLdd = -1;
    try{
      try{
//        showMessage("Registering GDAL");
//        gdal.AllRegister();
//        showMessage("Registered GDAL");
        datasetDEM = gdal.Open(fileDEM);
         if (datasetDEM == null){
          ok = false;
          showMessage("??? Dataset for DEM not read!");
        }
        else
        {
          double[] p = datasetDEM.GetGeoTransform();
          dX = p[1];
          dY = p[5];
          dXabs = Math.abs(dX);
          dYabs = Math.abs(dY);
          dDiag = Math.sqrt(dXabs * dXabs + dYabs * dYabs);
          nX = datasetDEM.getRasterXSize();
          nY = datasetDEM.getRasterYSize();
          cellArea = Math.abs(dX * dY);
        }
                 
        datasetKSat = gdal.Open(fileKSat);
        
         if (datasetKSat == null){
          ok = false;
          showMessage("??? Dataset for KSat not read!");
        }
        else
        {
          nXKSat = datasetKSat.getRasterXSize();
          nYKSat = datasetKSat.getRasterYSize();
        }
        datasetThetaSat = gdal.Open(fileThetaSat);
        if (datasetThetaSat == null){
          ok = false;
          showMessage("??? Dataset for ThetaSat not read!");
        }
        else
        {
          nXThetaSat = datasetThetaSat.getRasterXSize();
          nYThetaSat = datasetThetaSat.getRasterYSize();
        }
        datasetThetaIni = gdal.Open(fileThetaIni);
        if (datasetThetaIni == null){
          ok = false;
          showMessage("??? Dataset for ThetaIni not read!");
        }
        else
        {
          nXThetaIni = datasetThetaIni.getRasterXSize();
          nYThetaIni = datasetThetaIni.getRasterYSize();
        }
        datasetLDD = gdal.Open(fileLDD);
        if(datasetLDD == null){
          ok = false;
          showMessage("??? Dataset for ldd not read!");
        }
        else
        {
          nXLdd = datasetLDD.getRasterXSize();
          nYLdd = datasetLDD.GetRasterYSize();
        }
        if (ok){
          if ((!nXKSat.equals(nX)) ||
              (!nYKSat.equals(nY)) ||
              (!nXKSat.equals(nXThetaSat)) ||
              (!nYKSat.equals(nYThetaSat)) ||
              (!nXKSat.equals(nXThetaIni)) ||
              (!nYKSat.equals(nYThetaIni)) ||
              (!nXLdd.equals(nXThetaSat)) ||
              (!nYLdd.equals(nYThetaIni))){
            showMessage("??? Unequal grid sizes");
            ok = false;
          }
        }
        
        if (ok){
          createOutputFiles();
        }
        else
        {
          showMessage("Could not read data");
        }
      } 
      catch (Exception e) {
        ok = false;
        showMessage("Error reading input data");
      }
    }
    finally{
      return ok;
    }
  }

  private void createOutputFiles(){
    fileThetaFC = outputDir +"thetaFC.nc";
    fileInfiltration = outputDir + "infil.nc";
    fileOrder = outputDir + "order.txt";
    fileConcentration = outputDir + "concentration";
    fileSurfaceWater = outputDir + "surfaceWater.nc";
    fileVelocity = outputDir + "velocity.nc";
    fileRunoff = outputDir + "runoff.nc";
    fileSedimentTransport = outputDir + "sedimentConcentration.nc";
    fileSurfaceContent = outputDir + "surfaceContent.nc";
    if (newFilesRequired){
   //   createOrderFile();
      dataManager.createThetaFCFile(fileThetaFC, nX, nY);
      dataManager.createInfiltrationFile(fileInfiltration, nX, nY, nT);
      dataManager.createSurfaceWaterFile(fileSurfaceWater, nX, nY, nT);
      dataManager.createVelocityFile(fileVelocity, nX, nY, nT);
      dataManager.createRunoffFile(fileRunoff, nX, nY, nT);
      dataManager.createSedimentTransportFile(fileSedimentTransport, nX, nY, nT);
      dataManager.createSurfaceContentFile(fileSurfaceContent, nX, nY, nT);
      for (int i=0; i<10; i++){
        String fileName = fileConcentration + i + ".nc";
        dataManager.createConcentrationFile(fileName, nX, nY, nZ, nT);
      }
    } 
  }
    
  public Boolean defineArrays(){
    Boolean ok = true;
    try{
      try{
        kSatValue = new double[nX];
        thetaSatValue = new double[nX];
        thetaIniValue = new double[nX];
        thetaFCValue = new double[nX];
        zValue = new double[nX];
      }
      catch (Exception e) {
        ok = false;
        showMessage(e.getMessage());
      }
    }
    finally{
      return ok;
    }
  }
  
  public void getDataFromFile(int aRow){
    try{
      try{
        Band myBand = datasetKSat.GetRasterBand(1);
        myBand.ReadRaster(0, aRow, nX, 1, kSatValue);
        myBand = datasetThetaSat.GetRasterBand(1);
        myBand.ReadRaster(0, aRow, nX, 1, thetaSatValue);
        myBand = datasetThetaIni.GetRasterBand(1);
        myBand.ReadRaster(0, aRow, nX, 1, thetaIniValue);
        
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      
    }
  }
  
  private void computeThetaFC(){
    for (int i=0; i<nX; i++){
      if (thetaSatValue[i] < 0.0){
        thetaFCValue[i] = thetaSatValue[i];
      }
      else
      {
        thetaFCValue[i] = (double)(0.48017 * Math.log(thetaSatValue[i])  + 0.73434);
      }
    }
  }
  
  private void storeThetaFC(int aRow){
      dataManager.storeFCValues(thetaFCValue, aRow);
  }
  
  private void fillInfiltrationFile(){
    String inputBaseName = fileInfilBase;
    Dataset inputData= null;
    Band inputBand = null;
    int nSteps = nT;
    
    try{
      try{
        dataManager.openFCFileForReading(fileThetaFC);
        dataManager.openInfilFileForWriting(fileInfiltration);
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        Integer mod = 0;
        Integer ext = 0;
        for (int k=0; k<nSteps; k++){
//          System.out.println(k);
            ext = k+1;
            mod = ext / 1000;
            ext = ext % 1000;
            String suffix = ext.toString();
            while (suffix.length() < 3){
              suffix = "0".concat(suffix);
            }
            
            String modStr = mod.toString();
            String inputFile = inputBaseName.substring(0, inputBaseName.length() - modStr.length() - 1);
            inputFile = inputFile.concat(modStr).concat(".").concat(suffix);
//          showMessage("reading file " + inputFile);
          File fileInput = new File(inputFile);
          if (fileInput.exists()){
            inputData = gdal.Open(inputFile);
            inputBand = inputData.GetRasterBand(1);         
            nXValues = inputData.GetRasterXSize();
            nYValues = inputData.getRasterYSize();
//            showMessage("nX="+Integer.toString(nXValues));
//            showMessage("nY="+Integer.toString(nYValues));

            
            for (int j=0; j<nYValues; j++){
              double[] value = new double[nXValues];
              double[] infiltration = new double[nXValues];
              inputBand.ReadRaster(0, j, nXValues, 1, infiltration);
              double[] fcValue = dataManager.getFCValues(j, nXValues);
              for (int i=0; i<nXValues; i++){
               if (infiltration[i] < -99999){
                 infiltration[i] = -99999;
               }
               else
               {
                 infiltration[i] = 0.001 * infiltration[i];
               }
                if ((fcValue[i] > 0.0) & (infiltration[i] > -0.0001)){
                  value[i] = infiltration[i] / fcValue[i];
                }
                else
                {
                  value[i] = -99999;
                }
              }
              dataManager.storeInfiltration(k,j, infiltration);
              dataManager.storeInfiltrationDepth(k,j, value);
            }
          }
          else
          {
            showMessage("???ERROR: file " + inputFile + " does not exist." + lineSeparator);
          }  
          inputData = null;
          inputBand = null;
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      dataManager.closeFCFileAfterReading();
      dataManager.closeInfilFile();
//      showMessage("Infiltration depths computed");
    }
  }
  
  private void fillSurfaceWaterFile(){
    String inputBaseName = fileSurfaceWaterBase;
    Dataset inputData = null;
    Band inputBand = null;
    int nSteps = nT;
    
    try{
      try{
        dataManager.openSurfaceWaterFileForWriting(fileSurfaceWater);
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        Integer mod = 0;
        Integer ext = 0;
        for (int k=0; k<nSteps; k++){
            ext = k+1;
            mod = ext / 1000;
            ext = ext % 1000;
            String suffix = ext.toString();
            while (suffix.length() < 3){
              suffix = "0".concat(suffix);
            }
            
            String modStr = mod.toString();
            String inputFile = inputBaseName.substring(0, inputBaseName.length() - modStr.length() - 1);
            inputFile = inputFile.concat(modStr).concat(".").concat(suffix);
//          showMessage("reading file " + inputFile);
          File fileInput = new File(inputFile);
          if (fileInput.exists()){
            inputData = gdal.Open(inputFile);
            inputBand = inputData.GetRasterBand(1);         
            nXValues = inputData.GetRasterXSize();
            nYValues = inputData.getRasterYSize();
//            showMessage("nX="+Integer.toString(nXValues));
//            showMessage("nY="+Integer.toString(nYValues));

            
            for (int j=0; j<nYValues; j++){
              double[] surfaceWater = new double[nXValues];
              inputBand.ReadRaster(0, j, nXValues, 1, surfaceWater);
              for (int i=0; i<nXValues; i++){
                surfaceWater[i] = 0.001 * surfaceWater[i];                
              }
              dataManager.storeSurfaceWater(k, j, surfaceWater);
            }
          }
          else
          {
            showMessage("???ERROR: file " + inputFile + " does not exist." + lineSeparator);
          }
          inputData = null;
          inputBand = null;
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      dataManager.closeSurfaceWaterFile();
//      showMessage("Surface water heights stored");
    }
  }
  
  private void fillVelocityFile(){
    String inputBaseName = fileVelocityBase;
    Dataset inputData = null;
    Band inputBand = null;
    int nSteps = nT;
    
    try{
      try{
        dataManager.openVelocityFileForWriting(fileVelocity);
        gdal.AllRegister();
        Vector fileProperties = new Vector();
        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        Integer mod = 0;
        Integer ext = 0;
        for (int k=0; k<nSteps; k++){
            ext = k+1;
            mod = ext / 1000;
            ext = ext % 1000;
            String suffix = ext.toString();
            while (suffix.length() < 3){
              suffix = "0".concat(suffix);
            }
            
            String modStr = mod.toString();
            String inputFile = inputBaseName.substring(0, inputBaseName.length() - modStr.length() - 1);
            inputFile = inputFile.concat(modStr).concat(".").concat(suffix);
//          showMessage("reading file " + inputFile);
          File fileInput = new File(inputFile);
          if (fileInput.exists()){
            inputData = gdal.Open(inputFile);
            inputBand = inputData.GetRasterBand(1);         
            nXValues = inputData.GetRasterXSize();
            nYValues = inputData.getRasterYSize();
//            showMessage("nX="+Integer.toString(nXValues));
//            showMessage("nY="+Integer.toString(nYValues));

            
            for (int j=0; j<nYValues; j++){
              double[] velocity = new double[nXValues];
              inputBand.ReadRaster(0, j, nXValues, 1, velocity);
              dataManager.storeVelocity(k,j, velocity);
            }
          }
          else
          {
            showMessage("???ERROR: file " + inputFile + " does not exist." + lineSeparator);
          }
          inputData = null;
          inputBand = null;
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      dataManager.closeVelocityFile();
  //    showMessage("Velocities stored");
    }
  }
  
  private void fillRunoffFile(){
    String inputBaseName = fileRunoffBase;
    Dataset inputData = null;
    Band inputBand = null;
    int nSteps = nT;
    
    try{
      try{
        dataManager.openRunoffFileForWriting(fileRunoff);
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        Integer mod = 0;
        Integer ext = 0;
        for (int k=0; k<nSteps; k++){
//          System.out.println(k);
            ext = k+1;
            mod = ext / 1000;
            ext = ext % 1000;
            String suffix = ext.toString();
            while (suffix.length() < 3){
              suffix = "0".concat(suffix);
            }
            
            String modStr = mod.toString();
            String inputFile = inputBaseName.substring(0, inputBaseName.length() - modStr.length() - 1);
            inputFile = inputFile.concat(modStr).concat(".").concat(suffix);
//          showMessage("reading file " + inputFile);
          File fileInput = new File(inputFile);
          if (fileInput.exists()){
            inputData = gdal.Open(inputFile);
            inputBand = inputData.GetRasterBand(1);         
            nXValues = inputData.GetRasterXSize();
            nYValues = inputData.getRasterYSize();
//            showMessage("nX="+Integer.toString(nXValues));
//            showMessage("nY="+Integer.toString(nYValues));

            
            for (int j=0; j<nYValues; j++){
              double[] runoff = new double[nXValues];
              inputBand.ReadRaster(0, j, nXValues, 1, runoff);
              for (int i=0; i < nXValues; i++){
//                if((runoff[i] < 1.0001e-4) & (runoff[i] > -1.0001e-4)){
//                  runoff[i] =  0.0;
//                }
//                else
//                {
//                  if (runoff[i] > 1.0e-4){
                    runoff[i] = 1.0e-3 * runoff[i];
//                  }
//                }
              }
              dataManager.storeRunoff(k, j, runoff);
            }
          }
          else
          {
            showMessage("???ERROR: file " + inputFile + " does not exist." + lineSeparator);
          }
          inputData = null;
          inputBand = null;
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      dataManager.closeRunoffFile();
 //     showMessage("Runoff stored");
    }
  }
  
  private void fillSedimentTransportFile(){
    Dataset inputData = null;
    Band inputBand = null;
    int nSteps = nT;
    
    try{
      try{
        for (Integer i=0; i<=9; i++){
           String inputBaseName = fileSedimentTransportBase.replace("x", i.toString());
          dataManager.openSedimentTransportFileForWriting(fileSedimentTransport);
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

          Integer nXValues = -1;
          Integer nYValues = -1;
          Integer mod = 0;
          Integer ext = 0;
          for (int k=0; k<nSteps; k++){
  //          System.out.println(k);
              ext = k+1;
              mod = ext / 1000;
              ext = ext % 1000;
              String suffix = ext.toString();
              while (suffix.length() < 3){
                suffix = "0".concat(suffix);
              }

              String modStr = mod.toString();
              String inputFile = inputBaseName.substring(0, inputBaseName.length() - modStr.length() - 1);
              inputFile = inputFile.concat(modStr).concat(".").concat(suffix);
  //          showMessage("reading file " + inputFile);
            File fileInput = new File(inputFile);
            if (fileInput.exists()){
              inputData = gdal.Open(inputFile);
              inputBand = inputData.GetRasterBand(1);         
              nXValues = inputData.GetRasterXSize();
              nYValues = inputData.getRasterYSize();
              for (int j=0; j<nYValues; j++){
                double[] transport = new double[nXValues];
                inputBand.ReadRaster(0, j, nXValues, 1, transport);
                dataManager.storeSedimentTransport(i, k, j, transport);
              }
            }
            else
            {
              showMessage("???ERROR: file " + inputFile + " does not exist." + lineSeparator);
            }
            inputData = null;
            inputBand = null;
          }
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      dataManager.closeSedimentTransportFile();
 //     showMessage("SedimentTransport stored");
    }
  }
  
  private void closeOutputFiles(){
 //   dataManager.closeHDF5File();
  }
  
  private void readInitialContent(){
    Dataset inputData = null;
    Band inputBand = null;
    
    for (Cell myCell: cell){
      myCell.content = 0.001;
    }
    try{
      try{
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        File fileInput = new File(filePesticide);
        if (fileInput.exists()){
          inputData = gdal.Open(filePesticide);
          inputBand = inputData.GetRasterBand(1);         
          nXValues = inputData.GetRasterXSize();
          nYValues = inputData.getRasterYSize();
          
          int k = -1;
          for (int j=0; j<nYValues; j++){
            double[] pesticide = new double[nXValues];
            inputBand.ReadRaster(0, j, nXValues, 1, pesticide);
            for (int i=0; i<nXValues; i++){
              k = k + 1;
              if (pesticide[i] > 1.0e-4){
                cell.get(k).content = initialCellContent;
              }
              else
              {
                cell.get(k).content = 0.0;
              }
 //            cell.get(k).content = 0.000001 * pesticide[i];
//              cell.get(k).content = 0.0001;
            }
          }
        }
      }
      catch (Exception e)
      {
        showMessage("??? Error reading contents: " + e.getMessage());
      }
    }
    finally
    {
  //   showMessage("Initial contents read......");
    }
  }
  

  private void readRainMap(){
    Dataset inputData = null;
    Band inputBand = null;
    
    try{
      try{
//        gdal.AllRegister();
//        Vector fileProperties = new Vector();
//        fileProperties.add("PCRASTER_VALUESCALE=VS_SCALAR");
//        Driver myDriver = gdal.GetDriverByName("PCRaster");

        Integer nXValues = -1;
        Integer nYValues = -1;
        File fileInput = new File(filePrecipitation);
        if (fileInput.exists()){
          inputData = gdal.Open(filePrecipitation);
          inputBand = inputData.GetRasterBand(1);         
          nXValues = inputData.GetRasterXSize();
          nYValues = inputData.getRasterYSize();
          
          int k = -1;
          for (int j=0; j<nYValues; j++){
            int[] p = new int[nXValues];
            inputBand.ReadRaster(0, j, nXValues, 1, p);
            for (int i=0; i<nXValues; i++){
              k = k + 1;
              cell.get(k).rainGauge = p[i] - 1;
            }
          }
        }
        else
        {
          showMessage("???Error: file " + filePrecipitation + " does not exist!");
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
//     showMessage("Rain gauges read......");
    }
  }

  private void readRainData(){
    File myFile = new File(filePrecipitationData);
    if (myFile.exists()){
      try{
        try{
          StringBuilder myStringBuilder = dataManager.readFile(filePrecipitationData);
          String[] line = myStringBuilder.toString().split("\n");
          int lastLine = line.length-1;
          while (line[lastLine].trim().length() == 0){
            lastLine = lastLine-1;
          }
          int stations = line[lastLine].split("\t").length - 1;

          precipitationValue = new double[lastLine-stations+1][stations];
          int k = -1;
          for (int i=stations+1; i<=lastLine; i++){
            k++;
            String[] part = line[i].split("\t");
            for (int j=0; j<stations; j++){
              precipitationValue[k][j] = 0.001 * Double.valueOf(part[j+1]) / 3600.0;
            }
          }
        }
        catch(Exception e){
          showMessage("??? Error reading precipitation data: " + e.getMessage());
        }    
      }
      finally{
//        showMessage("Precipitation data read");
      }
    }
    else
    {
      showMessage("???ERROR: file " + filePrecipitationData + " does not exist!");
    }
  }
  

  private Boolean processData(){
    Boolean ok = true;
    
    dataManager.openFCFileForWriting(fileThetaFC);
//    showMessage("nY=" + Integer.toString(nY));
    for (int j=0; j<nY; j++){
//      showMessage(Integer.toString(j));
      getDataFromFile(j);
      computeThetaFC();
      storeThetaFC(j);
    }
    dataManager.closeFCFile();
    return ok;
  }
    
  private void storeConcentrations(int aY, int aX, double[][] aValue){
    double[] c = new double[nT];
    for (int i=0; i<10; i++){
     if (depthRequired[i]){
       for (int j=0; j<nT; j++){
         c[j] = aValue[i][j];
       }
       dataManager.storeConcentration(i, aY, aX, c);
     }
    }
  }
  
  private void openConcentrationFiles(){
    for (int i=0; i<depthRequired.length; i++){
      if (depthRequired[i]){
        String fileName = outputDir + "\\concentration" + i + ".nc";
        dataManager.openConcentrationFileForWriting(i, fileName);
      }
    }
  }

  private void closeConcentrationFiles(){
    for (int i=0; i<depthRequired.length; i++){
      if (depthRequired[i]){
        dataManager.closeConcentrationFile(i);
      }
    }
  }
  
  private void computeTopConcentrationFactor(){
    // 0.5 = exp (f.T)
    // f.T = ln (0.5)
    // f = ln(0.5) / T
    topConcentrationFactor = Math.log(0.5) / halfTime;
  }
  
  private double[] computeTopConcentration(int aT){
    double[] c = new double[aT];
    double t = 0.0;
    double x = 0.0;
    c[0] = 0.0;
    for (int i=1; i<aT; i++){
      t = i - 0.5;
      x = topConcentrationFactor * t;
      if (x > -10.0){
        c[i] = appliedConcentration * Math.exp(x);
      }
      else
      {
        c[i] = 0.0;
      }   
    }
    return c;
  }
  
  private void computeSurfacePotential(int aT){
    for (Cell myCell : cell){
      if(myCell.active){
        myCell.computeZ();;
      }
    }
  }
  
  private void computeSlope(){
    double hx1;
    double hx2;
    double hy1;
    double hy2;
    
    for (int i=0; i<cell.size(); i++){
      Cell myCell = cell.get(i);
      
      if (myCell.active){
        if ((myCell.xIndex == 0) || (myCell.xIndex > 0) & (!cell.get(i-1).active)){
          hx1 = -9999.0;
        }
        else
        {
          hx1 = cell.get(i-1).z;
        }

        if ((myCell.xIndex == nX-1) || ((myCell.xIndex < nX-1) & (!cell.get(i+1).active))){
          hx2 = -9999.0;
        }
        else
        {
          hx2 = cell.get(i+1).z;
        }

        if ((myCell.yIndex == 0) || ((myCell.yIndex > 0) & (!cell.get(i-nX).active))){
          hy2 = -9999.0;
        }
        else
        {
          hy2 = cell.get(i-nX).z;
        }

        if ((myCell.yIndex == nY-1) || ((myCell.yIndex < nY-1) & (!cell.get(i+nX).active))){
          hy1 = -9999.0;
        }
        else
        {
          hy1 = cell.get(i+nX).z;
        }

        myCell.xSlope = 0.0;
        myCell.xSlopeDir = 0;
        if (Math.abs(hx1 -hx2) > 0.0001) {
          if (hx1 < -1000.0){
            myCell.xSlope = (hx2 - myCell.z) / Math.abs(dX);
            myCell.xSlopeDir = 1;
          }
          else
          {
            if (hx2 < -1000.0){
              myCell.xSlope = (hx1 - myCell.z) / Math.abs(dX);
              myCell.xSlopeDir = -1;
            }
            else
            {
              if (hx1 < hx2){
                myCell.xSlope = (hx1 - myCell.z) / Math.abs(dX);
                myCell.xSlopeDir = -1;
              }
              else
              {
                myCell.xSlope = (hx2 - myCell.z) / Math.abs(dX);
                myCell.xSlopeDir = 1;
              }
            }
          }
        }
        if (myCell.xSlope > 0.0){
          myCell.xSlope = 0.0;
          myCell.xSlopeDir = 0;
        }

        myCell.ySlope = 0.0;
        myCell.ySlopeDir = 0;
        if (Math.abs(hy1 -hy2) > 0.0001) {
          if (hy1 < -1000.0){
            myCell.ySlope = (hy2 - myCell.z) / Math.abs(dY);
            myCell.ySlopeDir = 1;
          }
          else
          {
            if (hy2 < -1000.0){
              myCell.ySlope = (hy1 - myCell.z) / Math.abs(dY);
              myCell.ySlopeDir = -1;
            }
            else
            {
              if (hy1 < hy2){
                myCell.ySlope = (hy1 - myCell.z) / Math.abs(dY);
                myCell.ySlopeDir = -1;
              }
              else
              {
                myCell.ySlope = (hy2 - myCell.z) / Math.abs(dY);
                myCell.ySlopeDir = 1;
              }
            }
          }
        }
        if (myCell.ySlope > 0.0){
          myCell.ySlope = 0.0;
          myCell.ySlopeDir = 0;
        }
      }
      myCell.xSlope = Math.abs(myCell.xSlope);
      myCell.ySlope = Math.abs(myCell.ySlope);
    }
  }
  
  
  private void readDataForTimestep(int aT){
    double[] sw = new double[nX];
    double[] ro = new double[nX];
    double[] inf = new double[nX];
    double[] v = new double[nX];
    double[][] sc = new double[10][nX];
    
    int pos = -1;
    for (int i=0; i<nY; i++){
      inf = dataManager.getInfiltrationValues(aT, i, nX);
      sw = dataManager.getSurfaceWaterValues(aT, i, nX);
      ro = dataManager.getRunoffValues(aT, i, nX);
      v = dataManager.getVelocityValues(aT, i, nX);
      sc = dataManager.getSedimentTransportValues(aT, i, nX);
      
      for(int j=0; j<nX; j++){
        pos++;
        cell.get(pos).infiltration = inf[j];
        cell.get(pos).surfaceVelocity = v[j];
        cell.get(pos).runoff = timeStep * ro[j];
        cell.get(pos).waterHeight = sw[j];
        // read all directions
        for(int k=0; k<10; k++){
          cell.get(pos).sedimentTransport[k] = sc[k][j];
        }
      }      
    }
  }
  
  private void findTargets(){
    for (Cell myCell : cell){
      myCell.clearTargets();
      for (int i=0; i<myCell.slope.length; i++){
        if (myCell.slope[i] > 1.0e-15 ){
          myCell.target[i] = myCell.neighbour[i];
        }
      }
    }
  }
  
  private void findSources(){
    try{
      clearSources();
      for (int i=0; i<cell.size(); i++){
        Cell sourceCell = cell.get(i);
        if (sourceCell.active){
          for (int j=0; j<sourceCell.target.length; j++){
            if (sourceCell.target[j] > -1){
              cell.get(sourceCell.target[j]).addSource(i);
            }
          }
        }
      }
    }
    catch(Exception e){
      showMessage("???Error finding sources: " + e.getMessage());
    }
  }
  
  private void clearSources(){
    for (Cell myCell: cell){
      myCell.clearSources();;
    }
  }
  
  private void distributeRunoffAndSediment(Cell aCell){
    double t = aCell.totalSlope();
    for (int i=0; i<aCell.slope.length; i++){
      if (aCell.slope[i] > 1.0e-18){
        double f = aCell.slope[i] /t; 
        double q = f * aCell.runoff;
        cell.get(aCell.neighbour[i]).setWaterInflow(aCell.id, q);
  //      double d = f * aCell.sedimentTransport;
    //    cell.get(aCell.neighbour[i]).setDeposition(aCell.id, d);
      }
    }
  }
  
  private void computeRunoffAndSediment(){
    try{
      try{
        for (int i : computationOrder){
          Cell myCell = cell.get(i);
          distributeRunoffAndSediment(myCell);
        }
      }
      catch(Exception e){
        showMessage("???Error computing runoff: " + e.getMessage());
      }
    }
    finally
    {
      
    }    
  }

//  private void computeDelta(double aTimestep){
//    for (Cell myCell : cell){
//      if (myCell.active){
//        myCell.computeDelta(aTimestep);
//        if (myCell.deltaX > dXabs){
//          showMessage("!!!Warning: In cell " + Integer.toString(myCell.id) + 
//                    " deltaX (" + Double.toString((Double)myCell.deltaX) + ") > dX (" + ((Double)dXabs).toString() + ")");
//        }
//        if (myCell.deltaY > dYabs){
//          showMessage("!!!Warning: In cell " + Integer.toString(myCell.id) + 
//                    " deltaY (" + Double.toString((Double)myCell.deltaY) + ") > dY (" + ((Double)dYabs).toString() + ")");
//        }
//      }
//    }
//  }
  
  private void computeSlopes(){
    for (Cell myCell : cell){
      for (int i=0; i<8; i++){
        myCell.slope[i] = -1.0;
      }
      
      // top left
      if ((myCell.xIndex > 0) & (myCell.yIndex > 0)){
        if (cell.get(myCell.neighbour[0]).z < myCell.z){
          myCell.slope[0] = (myCell.z - cell.get(myCell.neighbour[0]).z) / dDiag;
        }
      }

      // top
      if (myCell.yIndex > 0){
        if (cell.get(myCell.neighbour[1]).z < myCell.z){
          myCell.slope[1] = (myCell.z - cell.get(myCell.neighbour[1]).z) / dYabs;
        }
      }
      
      // top right
      if ((myCell.xIndex < nX-1) & (myCell.yIndex > 0)){
        if (cell.get(myCell.neighbour[2]).z < myCell.z){
          myCell.slope[2] = (myCell.z - cell.get(myCell.neighbour[2]).z) / dDiag;
        }
      }

      // left
      if (myCell.xIndex > 0){
        if (cell.get(myCell.neighbour[3]).z < myCell.z){
          myCell.slope[3] = (myCell.z - cell.get(myCell.neighbour[3]).z) / dXabs;
        }
      }
      
      // right
      if (myCell.xIndex < nX-1){
        if (cell.get(myCell.neighbour[4]).z < myCell.z){
          myCell.slope[4] = (myCell.z - cell.get(myCell.neighbour[4]).z) / dXabs;
        }
      }

      // bottom left
      if ((myCell.xIndex > 0) & (myCell.yIndex < nY - 1)){
        if (cell.get(myCell.neighbour[5]).z < myCell.z){
          myCell.slope[5] = (myCell.z - cell.get(myCell.neighbour[5]).z) / dDiag;
        }
      }
      
      // bottom
      if (myCell.yIndex < nY-1){
        if (cell.get(myCell.neighbour[6]).z < myCell.z){
          myCell.slope[6] = (myCell.z - cell.get(myCell.neighbour[6]).z) / dYabs;
        }
      }
      
      // bottom right
      if ((myCell.xIndex < nX-1) & (myCell.yIndex < nY-1)){
        if (cell.get(myCell.neighbour[7]).z < myCell.z){
          myCell.slope[7] = (myCell.z - cell.get(myCell.neighbour[7]).z) / dDiag;
        }
      }

//      myCell.setMaxSlope();

    }
  }
  
  private void resetCellProcessed(){
    for (Cell myCell : cell){
      myCell.processed = !myCell.active;
    }
  }
  
  public void findInflow(){
    for (Cell myCell : cell){
      myCell.checkInflow();
    }
  } 
  
  private void findOrder(){
    try{
      try{
        computationOrder.clear();
        // non-active cells
        for (Cell myCell : cell){
          myCell.processed = !myCell.active;
        }
        
        // find compartments without inflow
        for (Cell myCell : cell){
          if (myCell.active){
            if (!myCell.hasInflow){
              computationOrder.add(myCell.id);
              myCell.processed = true;
              break;
            }
          }
        }
        
        Boolean searching = true;
        Boolean allProcessed = false;
        while (searching){
          searching = false;
          for (Cell myCell: cell){
           if (!myCell.processed){
              allProcessed = true;
              for (int i=0; i < myCell.source.length; i++){
                if (myCell.source[i] < 0){
                  break;
                }
                else
                {
                  if (!cell.get(myCell.source[i]).processed){
                    allProcessed = false;
                    break;
                  }
                }            
              }
              if (allProcessed){
                myCell.processed = true;
                computationOrder.add(myCell.id);
                searching = true;
              }
            }
          }
        }        
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      for (Cell myCell : cell){
        if (!myCell.processed){
          System.out.println(myCell.id);
          showChain(myCell);
        }
      }
    }
  }

  private void showChain(Cell aCell){
    for (int i=0; i<aCell.source.length; i++){
      Cell myCell = cell.get(aCell.source[i]);
      if (!myCell.processed){
        System.out.println(myCell.id);
        showChain(myCell);
      }
    }
  }
//  private void computeWaterFlow(){
//    double totalSlope = 0.0;
//    try{
//      try{
//        for (int i : computationOrder){
//          Cell myCell = cell.get(i);
//          if (myCell.active){
//            totalSlope = 0.0;
//            for (int j = 0; j<myCell.slope.length; j++){
//              if (myCell.slope[j] > 1.0e-15){
//                totalSlope = totalSlope + myCell.slope[j];
//              }
//            }
//          }       
//        }
//      }
//      catch (Exception e){
//        showMessage("??? Error in computeWaterFlow: " + e.getMessage());
//      }
//    }
//    finally{
//      
//    }
//  }
  
  private void clearWaterInflowAndDeposition(){
    for (Cell myCell : cell){
      if(myCell.active){
        myCell.clearWaterInFlow();
        myCell.cleaDeposition();
      }
    }
  }
  
  private void computeConcentrations(){
    for (int i: computationOrder){
      double dCrunon = 0.0;
      double dCdep = 0.0;
      Cell myCell = cell.get(i);
      for (int j=0; j<myCell.source.length; j++){
        if (myCell.source[j] > -1){
          if (cell.get(myCell.source[j]).active){ 
            dCrunon = dCrunon + myCell.waterInflow[j] * cell.get(myCell.source[j]).concentrationInWater;
            dCdep = dCdep + myCell.deposition[j] * cell.get(myCell.source[j]).concentrationInSolid;
          }
        }
      }

      double dCin =  dCrunon + dCdep;
      myCell.content = myCell.content + dCin;
      
      // infiltration      
      double dVinf = cellArea * (myCell.infiltration - myCell.previousInfiltration);    
//      double dVinf = 0.0;
            
      // runoff
      double dVrunoff = myCell.runoff;
   //   dVrunoff=0.0;

      // sufficient content for max concentration in water?
      double dCout = (fInfiltration * dVinf + dVrunoff) * maxCw;
      if (dCout < myCell.content){
        myCell.content = myCell.content - dCout;
        myCell.concentrationInWater = maxCw;
      }
      else
      {
        if ((fInfiltration * dVinf + dVrunoff) > 1.0e-18){
          myCell.concentrationInWater = myCell.content / (fInfiltration * dVinf + dVrunoff);
        }
        else
        {
          myCell.concentrationInWater = 0.0;
        }
        myCell.content = 0.0;
      }

      myCell.infiltratedContent = myCell.infiltratedContent + fInfiltration * dVinf;
//      // sufficient content for max concentration in sediment?
//      double dVout = myCell.sedimentTransport;
//      dCout = dVout * maxCs;
//      if (dCout < myCell.content){
//        myCell.content = myCell.content - dCout;
//        myCell.concentrationInSolid = maxCs;
//      }
//      else
//      {
//        if (dVout > 1.0e-15){
//          myCell.concentrationInSolid = myCell.content / dVout;
//        }
//        myCell.content = 0.0;
//      }
    }
  }
  
  private void assignPrecipitation(int aStep){
    try{
      int pos = (int) (timeStep * aStep / 60);
      for (Cell myCell: cell){
        if(myCell.active){
            myCell.precipitation = precipitationValue[pos][myCell.rainGauge];
        }
      }
    }
    catch (Exception e)          {
      showMessage("???Error assigning precipitation: " + e.getMessage());
    }
  }
  
  private void storeSurfaceContents(int aT){
    double[] values = new double[nX];
    for (int i=0; i<nY; i++){
      int base = i * nX;
      for (int  j=0; j<nX; j++){
        values[j] = cell.get(base+j).content;
      }
      dataManager.storeSurfaceContent(aT, i, nX, values);
    }
  }
  
  private void showSlopes(){
    for (int i=1265; i<1320; i++){
      System.out.print(i);
      System.out.print(" ");
      System.out.print(cell.get(i).xSlopeDir);
      System.out.print(" ");
      System.out.print(cell.get(i).ySlopeDir);
      System.out.print(" ");
      System.out.print(cell.get(i-nX).z);
      System.out.print(" ");
      System.out.print(cell.get(i).z);
      System.out.print(" ");
      System.out.print(cell.get(i+nX).z);
      System.out.print(" ");
      System.out.println();
    }
  }
  
  private void copyActualToPrevious(){
    for (Cell myCell : cell){
      myCell.copyActualToPrevious();
    }
  }
  
  private void showTotalContent(){
    Double t = 0.0;
    for (Cell myCell : cell){
      t = t + myCell.content;
    }
    System.out.println(t.toString());
  }
  
  private void computeLosses(int aStep){
    Double t = aStep * timeStep;
   
    for (Cell myCell: cell){
      if (myCell.freeOutflow){
        lossOfWater = lossOfWater + myCell.runoff;
   //     lossOfSolid = lossOfSolid + myCell.sedimentTransport;
        lossByWater = lossByWater + myCell.runoff * myCell.concentrationInWater;
   //     lossBySolid = lossBySolid + myCell.sedimentTransport * myCell.concentrationInSolid;
      }
        lossOfSolid = lossOfSolid + myCell.sedimentTransport[0];
        lossBySolid = lossBySolid + myCell.sedimentTransport[0] * myCell.concentrationInSolid;
    }
    
    String myString = t.toString() + "," + lossOfWater.toString() + "," + lossOfSolid.toString() + "," +
                      lossByWater.toString() + "," + lossBySolid.toString() + lineSeparator;
    dataManager.storeFile(fileLosses, myString, Boolean.TRUE);
  }
  
  private void computeTotals(int aStep){
    double[] contentOfSegment = new double[numberOfSegments];
    for (int i=0; i<numberOfSegments;i++){
      contentOfSegment[i]=0.0;
    }
    for (Cell myCell : cell){
      for (int i=0; i<numberOfSegments; i++){
        if ((myCell.y > lowSegment[i]) & (myCell.y <= highSegment[i])){
          contentOfSegment[i] = contentOfSegment[i] + myCell.content ;
        }
      }
    }
    double sum = 0.0;
    double t = aStep * timeStep;
    String myString = String.valueOf(aStep).concat(",").concat(String.valueOf(t)).concat(",");
    for(int i=0; i<numberOfSegments; i++){
      myString = myString.concat(String.valueOf(contentOfSegment[i])).concat(",");
      sum = sum + contentOfSegment[i];
    } 
    myString = myString.concat(String.valueOf(sum));
//    System.out.println(myString);
    segmentContent = segmentContent.concat(myString.concat(lineSeparator));
  }
  
  private void processSurfaceWater(){
  //  maxCs = 0.0;
//    maxCw = 0.0;
    
    double dt = 0.01;
    double t = 0.0;
    
    try{
      try{
        dataManager.openSurfaceContentFileForWriting(fileSurfaceContent);
        for (int k=0; k<nT; k++){
//          if (k % 10 == 0){
//            showMessage("Step " + Integer.toString(k));
//          }
          copyActualToPrevious();
          assignPrecipitation(k);
          clearWaterInflowAndDeposition();
          resetCellProcessed();
          readDataForTimestep(k);
          computeSurfacePotential(k);
          computeSlopes();
          findTargets();
          findSources();
          findOrder();
          findInflow();
          computeRunoffAndSediment();
//          t = 0.0;
//          while ( t <= timeStep){
//            t = t + dt;
//            computeDelta(timeStep);
//            computeWaterFlow();
            computeConcentrations();
//          }
 //         showTotalContent();
          storeSurfaceContents(k);
//          showTotalContent();
           computeLosses(k+1);
           computeTotals(k+1);
        }
        dataManager.storeFile(fileSegments, segmentContent, Boolean.FALSE);
      }
      catch (Exception e){
         showMessage("??? Error in processSurfaceWater: " + e.getMessage());
      }
    }
    finally
    {
      dataManager.closeSurfaceContentFile();
    }
  }
  
  private void openFilesForReading(){
    dataManager.openFCFileForReading(fileThetaFC);
    dataManager.openInfilFileForReading(fileInfiltration);
    dataManager.openRunoffFileForReading(fileRunoff);
    dataManager.openSedimentTransportFileForReading(fileSedimentTransport);
    dataManager.openVelocityFileForReading(fileVelocity);
    dataManager.openSurfaceWaterFileForReading(fileSurfaceWater);
    dataManager.openContentFileForReading(fileSurfaceContent);
  }  
  
  private void closeFilesAfterReading(){
    dataManager.closeInfilFileAfterReading();
    dataManager.closeFCFileAfterReading();
    dataManager.closeSurfaceWaterFileAfterReading();
    dataManager.closeVelocityFileAfterReading();
    dataManager.closeRunoffFileAfterReading();
    dataManager.closeSedimentTransportFileAfterReading();
    dataManager.closeContentFileAfterReading();
  }
  
  private void simulate(){
    try{
      try{
        computeTopConcentrationFactor();
        openConcentrationFiles();

        double[] infilDepth = new double[nT];
        double[] infiltration = new double[nT];
        double[] thetaFC = new double[nX];
        double[] content = new double[nT];

        for (Cell myCell : cell){
          int i = myCell.xIndex;
          int j = myCell.yIndex;
          if (i==0){
            showMessage("Row " + j + "/" + nY);
            thetaFC = dataManager.getFCValues(j, nX);
          }
          if (thetaFC[i] > 0.0001){
            infiltration = dataManager.getInfiltrationValuesAsTimeSeries(i,j,nT);
            infilDepth = dataManager.getInfilDepthValuesAsTimeSeries(i,j,nT);            
            content = dataManager.getSurfaceContentValues(i,j,nT);
            myCell.createFEMControl(requiredDepth, nT, nZ, retardationFactor, dispersionCoefficient, 
                degradationCoefficient, initialConcentration, content, -9999.0, timeStep);
            myCell.runFEM(content, infiltration, infilDepth, thetaFC[i], -9999.0);
            storeConcentrations(j,i,myCell.getFEM().valueToStore);
          }
        }
      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
      closeConcentrationFiles();
    }
      
  }
  
  private Boolean createGrid(){
    Boolean ok = true;
    double x;
    double y = -0.5 * dY;
    int id = -1;
    try{
      try{
        for (int j=0; j<nY; j++){
          Band myBand = datasetDEM.GetRasterBand(1);
          myBand.ReadRaster(0, j, nX, 1, zValue);
          y = y + dY;
          x = -0.5 * dX;
          for (int i=0; i<nX; i++){
            id++;
            x = x + dX;
            Cell myCell = new Cell();
            myCell.id = id;
            myCell.x = x;
            myCell.y = y;
            myCell.xIndex = i;
            myCell.yIndex = j;
            myCell.zSoil = zValue[i];
            if ((myCell.zSoil > -0.001) & (myCell.zSoil < 9999.0)){
              myCell.active = true;
            }
            else
            {
               myCell.active = false;
            }
            myCell.freeOutflow = (myCell.yIndex == nY-1-skipRowsAtEnd);
            cell.add(myCell);
          }
        }
        
      }
      catch (Exception e)
      {
        ok = false;
        showMessage("ERROR in createGrid: "+ e.getMessage());
      }
    }
    finally
    {
      
    }
    return ok;
  }
 
  private void defineNeighbours(){
    for (Cell myCell: cell){
      myCell.defineNeighbours(nX, nY);
    }
  }
  
  private void assignInitialConcentration(){
    for (Cell myCell : cell){
      if (myCell.content > 0.00000001){
        myCell.concentrationInWater = maxCw;
        myCell.concentrationInSolid = maxCs;
      }
      else
      {
        myCell.concentrationInWater = 0.0;
        myCell.concentrationInSolid = 0.0;
      }
  //    myCell.previousConcentration  = myCell.concentration;
    }
  }
  
  public Boolean process(){
    Boolean ok = false;
 //   showData(fileThetaFC);
    try{
      try{
        
        ok = openFiles();
        if (ok){
//          showMessage("Datafiles opened.");
        }
        else
        {
          showMessage("???ERROR: Can not open datafiles!");
        }
        if (ok){
          ok = defineArrays();
        }
        if (ok){
//          showMessage("Arrays defined");
        }
        else
        {
          showMessage("???ERROR: Can not define arrays!");
        }
        if (ok){
          ok =  createGrid();
//          showMessage("Grid created");
        }
        if (ok){
          defineNeighbours();
//          showMessage("Neighbours defined");
          if (newFilesRequired){
//            showMessage("Preparing output files and computing infiltration....");
            processData();
            fillInfiltrationFile();
//            showMessage("Infiltration computed....");
            fillSurfaceWaterFile();
//            showMessage("Surface water heights stored....");
//            fillVelocityFile();
//            showMessage("Velocities stored....");
            fillRunoffFile();
            fillSedimentTransportFile();
//            showMessage("Runoff stored....");
          }
          openFilesForReading();
          readRainMap();
          readRainData();
          readInitialContent();
          assignInitialConcentration();
          processSurfaceWater();
          if (soilContentRequired){
            simulate();
          }
          closeFilesAfterReading();
        }
      }
      catch (Exception e){
        showMessage ("??? Error in process: " + e.getMessage());
      }
    }
    finally
    {
      closeOutputFiles();
//      showMessage("Finished");
    }
    return ok;
  }

  private void simulateSingleRun(){
    double[] topConcentrations = new double[1];
    try{
      try{
        timeStep = 1.0;
        FEMControl myFEM = new FEMControl(requiredDepth, nT, nZ, retardationFactor, dispersionCoefficient, 
                degradationCoefficient, initialConcentration, topConcentrations, topConcentration, timeStep);
        double[] infilDepth = new double[nT];
        double[] infiltration = new double[nT];
        double[] content = new double[nT];
        
        myFEM.runFEM(content, infiltration, infilDepth, 0, waterVelocity);
        myFEM.storeOutput(outputFile);
//        showMessage("Finished");

      }
      catch (Exception e)
      {
        showMessage(e.getMessage());
      }
    }
    finally
    {
    }
      
  }
  
  
  public void run(){
    try{
      if (singleRun){
        simulateSingleRun();
      }
      else
      {
        Boolean ok = process();
      }
    }
    catch (Exception e)
    {
      
      showMessage("????ERROR: " + e.getMessage());
    }
  }
  
  
  public void start ()
   {
//      showMessage("Starting " +  threadName );
      if (controlThread == null)
      {
         controlThread = new Thread (this, threadName);
         controlThread.start();
      }
   }

}


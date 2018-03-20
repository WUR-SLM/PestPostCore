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
public class Cell {
  public int id;
  public int xIndex;
  public int yIndex;
  public int xSlopeDir;
  public int ySlopeDir;
  public int rainGauge;
  public double x;
  public double y;
  public double deltaX;
  public double deltaY;
  public double zSoil;
  public double z;
  public double xSlope;
  public double ySlope;
  public double infiltration;
  public double previousInfiltration;
  public double waterHeight;
  public double previousWaterHeight;
  public double surfaceVelocity;
  public double runoff;
  public double precipitation;
  public double content;
//  public double previousContent;
  public double concentrationInWater;
  public double concentrationInSolid;
//  public double previousConcentration;
  public double[] sedimentTransport;
  public double infiltratedContent;
  public boolean active;
  public boolean hasInflow;
  public boolean processed;
  public int[] target;
  public int[] source;
  public double[] waterInflow;
  public double[] slope;
  public double[] deposition;
  public int[] neighbour;
  public boolean freeOutflow;
  private FEMControl myFEM;
  
  public Cell(){
    hasInflow = false;
    id = -1;
    xIndex = -1;
    yIndex = -1;
    x = -9999.0;
    y = -9999.0;
    z = -9999.0;
    deltaX = 0.0;
    deltaY = 0.0;
    zSoil = -9999.0;
    xSlope = 0.0;
    ySlope = 0.0;
    content = -9999.0;
    concentrationInWater = -9999.0;
    concentrationInSolid = -9999.0;
    precipitation = 0.0;
    rainGauge = -1;
    infiltratedContent = 0.0;
 //   myFEM = new FEMControl();
    active = false;
    target = new int[8];
    source = new int[8];
    waterInflow = new double[8];
    deposition = new double[8];
    neighbour = new int[8];
    processed = false;
    waterHeight = 0.0;
    infiltration = 0.0;
    slope = new double[8];
    runoff = 0.0;
    sedimentTransport = new double[10];
    for (int i=0; i<10; i++){
      sedimentTransport[i] = 0.0;
    }
    freeOutflow = false;
  }
  
  public void createFEMControl(int[] aDepth, int aT, int aN, double aRetardation, double aDegradation,
          double aDispersion, double aInitialConcentration, double[] aTop, double aTopConcentration,
          double aStepSize){
    myFEM = new FEMControl(aDepth, aT, aN, aRetardation, aDegradation, aDispersion, aInitialConcentration, aTop, aTopConcentration, aStepSize);
  }
  
  public void runFEM(double[] aSurfaceContent, double[] aInfiltration, double[] aDepth, double aThetaFC, double aVelocity){
    myFEM.runFEM(aSurfaceContent, aInfiltration, aDepth, aThetaFC, aVelocity); 
  }
  
  public double totalSlope(){
    double t = 0.0;
    for(int i=0; i<slope.length; i++){
      if (slope[i] > 1.0e-15){
        t = t + slope[i];
      }
    }
    return t;
  }
  
  public void setMaxSlope(){
    int n = 0;
    double s = slope[0];
    for (int i=1; i<slope.length; i++){
      if (slope[i] > s){
        s = slope[i];
        n = i;
      }
    }
    
    for (int i=0; i<slope.length; i++){
      if(i!=n){
        slope[i] = -1.0;
      }
    }     
  }
  
  public void copyActualToPrevious(){
    previousWaterHeight = waterHeight;
    previousInfiltration = infiltration;
  }
  
  public void computeZ(){
    z = zSoil + waterHeight;
  }
  
  public void clearTargets(){
    for (int i=0; i<8; i++){
      target[i] = -1;
    }
  }
  
  public void clearSources(){
    for (int i=0; i<8; i++){
      source[i] = -1;
    }
  }
  
  public void addSource(int aSource){
    for (int i=0; i<8; i++){
      if (source[i] < 0){
        source[i] = aSource;
        break;
      }
    }
  }

  public void defineNeighbours(int aNx, int aNy){
    // top left
    if ((xIndex > 0) & (yIndex > 0)){
      neighbour[0] = id - aNx - 1;
    }
    else
    {
      neighbour[0] = -1;
    }

    // top
    if (yIndex > 0){
      neighbour[1] = id - aNx;
    }
    else
    {
      neighbour[1] = -1;
    }
    
    // top right
    if ((xIndex < aNx-1) & (yIndex > 0)){
      neighbour[2] = id - aNx + 1;
    }
    else
    {
      neighbour[2] = -1;
    }

    // left
    if (xIndex > 0){
      neighbour[3] = id - 1;
    }
    else
    {
      neighbour[3] = -1;
    }

    // right
    if (xIndex < aNx-1){
      neighbour[4] = id + 1;
    }
    else
    {
      neighbour[4] = -1;
    }

    // bottom left
    if ((xIndex > 0) & (yIndex < aNy - 1)){
      neighbour[5] = id + aNx - 1;
    }
    else
    {
      neighbour[5] = -1;
    }

    // bottom
    if (yIndex < aNy-1){
      neighbour[6] = id + aNx;
    }
    else
    {
      neighbour[6] = -1;
    }

    // bottom right
    if ((xIndex < aNx-1) & (yIndex < aNy-1)){
        neighbour[7] = id + aNx + 1;
    }
    else
    {
      neighbour[7] = -1;
    }

  }
  
  public void computeDelta(double aTimestep){
    double delta = runoff;
    if ((delta < 1.0e-15) || ((xSlope < 1.0e-15) & (ySlope < 1.0e-15))) {
      deltaX = 0.0;
      deltaY = 0.0;
    }
    else
    {
      if (xSlope < 1.0e-12){
        deltaX = 0.0;
        deltaY = ySlopeDir * delta; 
      }
      else
      {
        if (ySlope < 1.0e-12){
          deltaY = 0.0;
          deltaX = xSlopeDir * delta;
        }
        else
        {
          double f = xSlope / ySlope;
          deltaY = Math.sqrt(delta * delta / (1.0 + f * f));
          deltaX = f * deltaY;
        }
      }
    }
  }
  
  public void checkInflow(){
    hasInflow = false;
    for (int i=0; i<source.length; i++){
      if (source[i] > -1){
        hasInflow = true;
        break;
      }
    }
  }
  
  public void clearWaterInFlow(){
    for (int i=0; i<waterInflow.length; i++){
      waterInflow[i] = 0.0;
    }
  }
 
  public void cleaDeposition(){
    for (int i=0; i<deposition.length; i++){
      deposition[i] = 0.0;
    }
  }
  
  public void setWaterInflow(int aCell, double aValue){
    for (int i=0; i<waterInflow.length; i++){
      if (source[i] == aCell){
        waterInflow[i] = aValue;
        break;
      }
    }
  }

  public void setDeposition(int aCell, double aValue){
    for (int i=0; i<deposition.length; i++){
      if (source[i] == aCell){
        deposition[i] = aValue;
        break;
      }
    }
  }
  
  public double totalInflow(){
    double v = 0.0;
    for (int i=0; i<waterInflow.length; i++){
      v = v + waterInflow[i];
    }
    return v;
  }
  
  public double totalDeposition(){
    double d = 0.0;
    for (int i=0; i<deposition.length; i++){
      d = d + deposition[i];
    }
    return d;
  }
  
  public FEMControl getFEM(){
    return myFEM;
  }
}

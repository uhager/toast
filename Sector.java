// part of Toast
// author: Ulrike Hager

import java.util.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Sector {
	private static final boolean DEBUG = false;
	private ArrayList<double[]> dataPoints;
	private double slope, angle, origin, lastRadius, firstRadius, length, firstZ, lastZ, totalEnergy, secondRadius, secondZ, penultimateRadius, penultimateZ,deltaE;
	private int number;
	private double radiusOffset;
	private ArrayList<double[]> coordList;
	private Color sectorColour;
	private boolean downwardsTrack;
    public Sector(int i, Color aColour){
		dataPoints = new ArrayList<double[]>();
		coordList = new ArrayList<double[]>();
		lastRadius = length = 0;
		firstZ=secondZ=1000000;
		lastZ=penultimateZ=-1000000;
		totalEnergy=0;
		number = i;
		sectorColour = aColour;
		switch(number){
		case 0:
		case 1:
		case 6:
		case 7:
			downwardsTrack = false;
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			downwardsTrack = true;
			break;
		}
	}

    public Sector(int i, Color aColour, ArrayList<double[]> dataPoints){
		this.dataPoints = dataPoints;
		coordList = new ArrayList<double[]>();
		lastRadius = length = 0;
		firstZ=secondZ=1000000;
		lastZ=penultimateZ=-1000000;
		totalEnergy=0;
		number = i;
		sectorColour = aColour;
		switch(number){
		case 0:
		case 1:
		case 6:
		case 7:
			downwardsTrack = false;
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			downwardsTrack = true;
			break;
		}
		updateSector();
	}

	public ArrayList<double[]> getData(){
		return dataPoints;
	}

	public void setData(ArrayList<double[]> someData){
		dataPoints = someData;
	}
		
	public void addData(ArrayList<double[]> someData){
		int initialPads = dataPoints.size();
		for (int j=0 ; j<someData.size() ; j++){
			boolean added =false;
			for (int i=0 ; i<initialPads ; i++){
				if (dataPoints.get(i)[0]-someData.get(j)[0]<2){
					if (someData.get(j)[2]>dataPoints.get(i)[2]) dataPoints.set(i,someData.get(j));
					added = true;
					break;
				}
			}
			if (!added) dataPoints.add(someData.get(j));
		}
	}
	
	public void addPoint(ArrayList<Double> inputData){ //zPos, double radius, double energy){
//		double[] temp = {zPos, radius, energy};
//		double[] temp = (double [])inputData.toArray(new Double[0]);
		double [] temp = new double[inputData.size()];
		for (int i = 0; i<inputData.size();i++){
			temp[i] = inputData.get(i);
		}
		dataPoints.add(temp);
//		if (DEBUG) System.out.println("Sector: added " + temp[0]);
	}

	public Color getColour(){
		return sectorColour;
	}
	
	public double getTotalEnergy(){
		return totalEnergy;
	}
	public int getNumber(){
		return number;
	}
	
	public double getAngle(){                        // use after getOrigin to set the slope
		return angle;
	}

	public void zeroData(){
		lastRadius = length = 0;
		firstZ=1000000;
		lastZ=-1000000;
		totalEnergy=0;
	}

	public void zeroTrack(){
		dataPoints.clear();
		dataPoints = new ArrayList<double[]>();
	}		
	public double getLastZ(){
		return lastZ;
	}
	public double getFirstZ(){
		return firstZ;
	}
		
	public double getLastRadius(){
		return lastRadius;
	}

	public void updateSector(){
//		if (DEBUG) System.out.println("Sector: updating");
		totalEnergy=0;
		deltaE = Double.NaN;
		radiusOffset = 0;
		double driftOrigin = 12.0;
		double firstE,secondE,penultimateE,lastE,thirdE,thirdLastE;
		firstE = secondE = thirdE = thirdLastE = penultimateE = lastE = 0;
		if (dataPoints.size() < 2){
			origin = Double.NaN; length=Double.NaN; slope=Double.NaN; angle = Double.NaN; lastZ = Double.NaN; lastRadius = Double.NaN;
			return;
		}
		
		if (dataPoints.size() == 2){
			if ((dataPoints.get(0)[2]<0.000001) || (dataPoints.get(1)[2]<0.000001)){
				origin = Double.NaN; length=Double.NaN; slope=Double.NaN; angle = Double.NaN; lastZ = Double.NaN; lastRadius = Double.NaN;
				return;
			}				
			if (dataPoints.get(0)[1]<dataPoints.get(1)[1]){
				firstZ=dataPoints.get(0)[0];
				secondZ=dataPoints.get(1)[0];
				firstRadius=dataPoints.get(0)[1];
				secondRadius=dataPoints.get(1)[1];
			}
			else{
				firstZ=dataPoints.get(1)[0];
				secondZ=dataPoints.get(0)[0];
				firstRadius=dataPoints.get(1)[1];
				secondRadius=dataPoints.get(0)[1];
			}
			lastZ=secondZ;
			lastRadius = secondRadius;
 			totalEnergy = dataPoints.get(0)[2] + dataPoints.get(1)[2];
			slope = (2*(firstZ*firstRadius+secondZ*secondRadius)-((firstZ+secondZ)*(firstRadius+secondRadius)))/(2*(firstZ*firstZ+secondZ*secondZ)-(firstZ+secondZ)*(firstZ+secondZ));
			angle = (Math.atan(slope))*(180.0/Math.PI);
			origin = firstZ - firstRadius/slope;
			length = (lastZ-origin) / (Math.cos(angle*Math.PI/180));  // 4mm per pad
			if (angle < 0) angle += 180;
			if ((origin<-121) || (origin>122)){ origin = Double.NaN; length=Double.NaN; slope=Double.NaN; angle=Double.NaN;}
			return;
		}
		
		if (dataPoints.size() > 2){   //dataPoints.size() > 2
			//				if (DEBUG) System.out.println("Sector: dataPoints.size() " + dataPoints.size());

			double sumZ, sumZZ, sumR, sumRR, sumZR, nPad;
			sumZ = sumZZ = sumR = sumZR = nPad = 0;
			double thirdZ=firstZ;
			double thirdRadius=firstRadius;
			double thirdLastZ=lastZ;
			double thirdLastRadius=lastRadius;
			for (int i=0 ; i<dataPoints.size() ; i++){    //sort through dataPoints
// 					if (DEBUG) System.out.println("Sector: dataPoints.get(i)[0]" + dataPoints.get(i)[0]);				
// 					if (DEBUG) System.out.println("Sector: dataPoints.get(i)[1]" + dataPoints.get(i)[1]);				
// 					if (DEBUG) System.out.println("Sector: dataPoints.get(i)[2]" + dataPoints.get(i)[2]);				
				if ((dataPoints.get(i)[1]>60) || (dataPoints.get(i)[2]<0.000001)) continue; 
				totalEnergy += dataPoints.get(i)[2];
				if (dataPoints.get(i)[0]<firstZ) {
					thirdZ = secondZ;
					thirdRadius = secondRadius;
					thirdE=secondE;
					secondZ = firstZ;
					secondE = firstE;
					secondRadius = firstRadius;
					firstZ=dataPoints.get(i)[0];
					firstRadius=dataPoints.get(i)[1];
					firstE=dataPoints.get(i)[2];
				}
				else if (dataPoints.get(i)[0]<secondZ){
					thirdZ = secondZ;
					thirdE=secondE;
					thirdRadius = secondRadius;
					secondZ=dataPoints.get(i)[0];
					secondRadius=dataPoints.get(i)[1];
					secondE=dataPoints.get(i)[2];
				}
				else if (dataPoints.get(i)[0] < thirdZ){
					thirdZ=dataPoints.get(i)[0];
					thirdE=dataPoints.get(i)[2];
					thirdRadius=dataPoints.get(i)[1];
				}
				if (dataPoints.get(i)[0]>lastZ) {
					thirdLastZ = penultimateZ;
					thirdLastRadius = penultimateRadius;
					thirdLastE = penultimateE;
					penultimateZ = lastZ;
					penultimateRadius = lastRadius;
					penultimateE = lastE;
					lastZ=dataPoints.get(i)[0];
					lastRadius=dataPoints.get(i)[1];
					lastE=dataPoints.get(i)[2];
				}
				else if (dataPoints.get(i)[0] > penultimateZ) {
					thirdLastZ = penultimateZ;
					thirdLastRadius = penultimateRadius;
					thirdLastE = penultimateE;
					penultimateZ=dataPoints.get(i)[0];
					penultimateRadius=dataPoints.get(i)[1];
					penultimateE=dataPoints.get(i)[2];
				}
				else if (dataPoints.get(i)[0] > thirdLastZ){
					thirdLastZ=dataPoints.get(i)[0];
					thirdLastE=dataPoints.get(i)[2];
					thirdLastRadius=dataPoints.get(i)[1];
				}
				nPad++;
				sumZ += dataPoints.get(i)[0];
				sumZZ += dataPoints.get(i)[0]*dataPoints.get(i)[0];
				sumR += dataPoints.get(i)[1];
				sumZR += dataPoints.get(i)[0]*dataPoints.get(i)[1];
			}

			double totSlope = (nPad*sumZR - sumZ*sumR)/(nPad*sumZZ - sumZ*sumZ);
		
			if ((lastRadius<firstRadius) && ((lastRadius <= secondRadius) || (firstE > secondE/2.5))){
				double temp = firstZ; firstZ=lastZ; lastZ=temp;
				temp = firstRadius; firstRadius=lastRadius; lastRadius=temp;
				temp = firstE; firstE=lastE; lastE=temp;
				secondZ = penultimateZ; secondRadius = penultimateRadius; secondE = penultimateE;
				thirdZ = thirdLastZ; thirdRadius = thirdLastRadius; thirdE=thirdLastE;
			}
			if (dataPoints.size()>3) deltaE = secondE + thirdE;
			double firstSlope = (2*(firstZ*firstRadius+secondZ*secondRadius)-((firstZ+secondZ)*(firstRadius+secondRadius)))/(2*(firstZ*firstZ+secondZ*secondZ)-(firstZ+secondZ)*(firstZ+secondZ));
			double secondSlope = (2*(thirdZ*thirdRadius+secondZ*secondRadius)-((thirdZ+secondZ)*(thirdRadius+secondRadius)))/(2*(thirdZ*thirdZ+secondZ*secondZ)-(thirdZ+secondZ)*(thirdZ+secondZ));
			double totAngle = (Math.atan(totSlope))*(180.0/Math.PI);
			double secondAngle = (Math.atan(secondSlope))*(180.0/Math.PI);
			slope = firstSlope;
			angle = (Math.atan(slope))*(180.0/Math.PI);
			origin = firstZ - firstRadius/slope;
// 		if (DEBUG) System.out.println("points 1&2 slope: " + slope + " angle: " + angle + " origin: " + origin);
// 		System.out.println("points 2&3 slope: " + secondSlope + " angle: " + secondAngle + " origin: " + (secondZ - secondRadius/secondSlope));
// 		System.out.println("total slope: " + totSlope + " angle: " + totAngle + " origin: " + (firstZ - firstRadius/totSlope) + "\n");
			if ((origin<-120) || (origin>120)) {radiusOffset = 1.5; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
			if ((origin<-120) || (origin>120)) {radiusOffset = 3; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
 	
//		if ((Math.signum(totSlope) != Math.signum(slope)) || (Math.abs(totAngle-angle) - Math.abs(secondAngle-totAngle) > 10 ) || (origin<-121) || (origin>122)){
//		if ((Math.abs(totAngle-angle) - Math.abs(secondAngle-totAngle) > 10 ) || (origin<-121) || (origin>122)){
			if (((Math.abs(secondAngle-angle) > 30) && (Math.abs(secondAngle-totAngle) < 20)  && (Math.abs(secondSlope) > 0.00000001)) || (origin<-120) || (origin>120) || (firstE < secondE/2.5) || ((Math.signum(slope) != Math.signum(totSlope)) && (Math.abs(secondSlope) > 0.00000001) )){
				firstZ = secondZ; firstRadius = secondRadius;
				slope = secondSlope; angle = secondAngle;
				radiusOffset = 0;
				origin = firstZ - firstRadius/slope;
				if ((origin<-120) || (origin>120)) {radiusOffset = 1.5; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
				if ((origin<-120) || (origin>120)) {radiusOffset = 3; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
			}
			if (nPad == 2) {
				angle = totAngle;
				slope = totSlope;
				radiusOffset = 0;
				origin = firstZ - firstRadius/slope;
				if ((origin<-120) || (origin>120)) {radiusOffset = 1.5; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
				if ((origin<-120) || (origin>120)) {radiusOffset = 3; origin = firstZ - 1/slope *(firstRadius - radiusOffset);}
 	
			}
			length = (lastZ-origin-radiusOffset/slope) / (Math.cos(angle*Math.PI/180));  // 4mm per pad
			if (angle < 0) angle += 180;
			if ((origin<-120) || (origin>120)){ origin = Double.NaN; length=Double.NaN; slope=Double.NaN; angle=Double.NaN;}
		
		}
//				if (DEBUG) System.out.println("Sector: updated");

	}
	
	public double getOrigin(){
		return origin;
	}
	

	public double getTotalLength(){                             
		return length;
	}
	
	public double getDeltaE(){
		return deltaE;
	}

	public void calculateCoordinates(int Scale, boolean logScale){
		if (DEBUG) System.out.println("Sector: calculateCoordinates, dataPoints.size() " + dataPoints.size());
		coordList.clear();
		for (int j=0 ; j<dataPoints.size() ; j++){
			double[] coords = new double[3];
			if (logScale) coords[2] = Math.pow(10,dataPoints.get(j)[2]) * (double)Scale;
			else coords[2] = dataPoints.get(j)[2] * (double)Scale;
			coords[0] = (dataPoints.get(j)[0]+120.0)*10.0/4.0+20.0- coords[2]/2.0;
			if (!downwardsTrack) coords[1] = (450.0 - dataPoints.get(j)[1] *3.0)-(coords[2]/2.0);
			else coords[1] =((dataPoints.get(j)[1]) *3.0)+450.0-(coords[2]/2.0);
			if (DEBUG) System.out.println("Sector: j " + j + " xc " + coords[0] + " yc " + coords[1] + " rc " + coords[2]);
				coordList.add(coords);
		}
	}
	
    public void paintTrack(Graphics g){
//		if (DEBUG) System.out.println("Sector: paintTrack");
		Graphics2D g2D = (Graphics2D) g;
		double xc,yc,rc;
		for (int j=0 ; j<dataPoints.size() ; j++){
			g2D.setColor(sectorColour);
			g2D.fill(new Ellipse2D.Double(coordList.get(j)[0],coordList.get(j)[1],coordList.get(j)[2],coordList.get(j)[2]));
		}
	}

    public void paintLabel(Graphics g, ArrayList<Particle> particleList){
		if (DEBUG) System.out.println("Sector: paintLabel");
		Graphics2D g2D = (Graphics2D) g;
		int particleType;
		String particleLabel;
		for (int j=0 ; j<dataPoints.size() ; j++){
			try {
				if (DEBUG) System.out.println("Sector: try label, type 0: " + particleList.get(0).getName());
				particleType = (int)dataPoints.get(j)[3];
				if (DEBUG) System.out.println("Sector: particleType " + particleType);
			}catch (Exception e){continue;}
			if (particleList.size()>particleType){
				particleList.get(particleType).addCoordinate(coordList.get(j));
				particleLabel = particleList.get(particleType).getName();
			}
			else particleLabel = particleType +"";
			if (DEBUG) System.out.println("Sector: particleLabel " + particleLabel);
			g2D.setColor(Color.WHITE);
			g2D.setFont(new Font("sansserif", Font.PLAIN, 9));
			FontMetrics fm = g.getFontMetrics();
			int wordWidth = fm.stringWidth(particleLabel);
			g2D.fillRect((int)(coordList.get(j)[0]+coordList.get(j)[2]/2.0)-1,(int)(coordList.get(j)[1]+coordList.get(j)[2]/2.0)-fm.getHeight(),fm.stringWidth(particleLabel)+2,fm.getHeight()+1);
			if (DEBUG) System.out.println("Sector: j " + j + " xc " + coordList.get(j)[0] + " yc " + coordList.get(j)[1] + " rc " + coordList.get(j)[2]);
			g2D.setColor(Color.BLACK);
				g2D.drawString(particleLabel,(int)(coordList.get(j)[0]+coordList.get(j)[2]/2.0),(int)(coordList.get(j)[1]+coordList.get(j)[2]/2.0));
		}
	}

    public void paintLine(Graphics g, int Scale){
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(sectorColour);
		if ((!Double.isNaN(origin)) && (!Double.isNaN(slope))){
			switch(number){
			case 0:
			case 1:
			case 6:
			case 7:
				g2D.draw(new Line2D.Double(((origin+120.0)*10.0/4.0)+20.0,450.0-(radiusOffset*3),((lastZ+120.0)*10.0/4.0)+20.0,(((origin-lastZ)*slope-radiusOffset)*3.0)+450.0));
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				g2D.draw(new Line2D.Double(((origin+120.0)*10.0/4.0)+20.0,450.0+(radiusOffset*3),((lastZ+120.0)*10.0/4.0)+20.0,(((lastZ-origin)*slope+radiusOffset)*3.0)+450.0));
				break;
			}
		}
	}


}


// part of Toast
// author: Ulrike Hager

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class OutputPanel extends JPanel implements MouseListener { //class OutputPanel, draws histograms and tracks
    private static final boolean DEBUG = false;
    boolean energyCut, angleCut, originCut, totalEnergyCut, deltaECut;
    boolean eventInfo;
    int CurrentSector=8;
    double currentEnergy, totalEnergy;
    String currentEvent;
    String Input, unknownString;
    int unknownStrings;
    int[] totalHits, currentHits, sliceOffset;
    int sumEvents, sumTracks;
    String voltage, pressure, runNumber, presentedEvents, acceptedEvents, flow;
    HistBar[] histBar;
    HistCircle histCircle;
    HistCircle[] sliceCircle;
    OneDHistogramFixed zHistAll, angleHistAll, radiusHistAll, energyHistAll, totalEnergyHist;
    OneDHistogramFixed[] energyHist;
    OneDHistogramFixed[] zHist;
    OneDHistogramFixed[] angleHist;
    OneDHistogramFixed[] radiusHist;
    OneDHistogramFixed[] coincidenceHist;
    //	OneDHistogram energyHistAll;
    //	TwoDHistogram  energyVsTotalLengthAll;
    TwoDHistogramFixed[] energyVsZ,energyVsZSmall;
    TwoDHistogramFixed[] deltaEvsE, deltaEvsESmall;
    TwoDHistogramFixed[] energyVsOrigin,energyVsOriginSmall;
    TwoDHistogramFixed[] energyVsAngle,energyVsAngleSmall;
    TwoDHistogramFixed[] dEVsAngle,dEVsAngleSmall;
    TwoDHistogramFixed[] energyVsTotalLength, energyVsTotalLengthSmall;
    TwoDHistogramFixed energyVsZAll, energyVsAngleAll, originVsAngleAll, radiusVsZAll, energyVsOriginAll,energyVsTotalLengthAll,deltaEvsEAll,dEVsAngleAll;
    TwoDHistogramFixed[] radiusVsZ, radiusVsZSmall;
    TwoDHistogramFixed[] originVsAngle, originVsAngleSmall;
    double energyLow, energyHigh,totalEnergyLow, totalEnergyHigh, zLow, zHigh, angleLow, angleHigh, zEndLow, zEndHigh, radiusLow, radiusHigh, lengthLow, lengthHigh, deltaELow, deltaEHigh;
    public double energyLowCut, energyHighCut,totalEnergyLowCut, totalEnergyHighCut, zLowCut, zHighCut, angleLowCut, angleHighCut, zEndLowCut, zEndHighCut, radiusLowCut, radiusHighCut, lengthLowCut, lengthHighCut, deltaELowCut,deltaEHighCut;
    Color[] tacColour;
    ArrayList<Sector> collectSectors, workSectors, paintSectors; 
    ArrayList<Integer> coincidenceList;
    private ArrayList<Particle> particleList, particleList2;
    //	Particle[] particleArray;
    Dimension offDimension;
    Image offImage;
    Graphics offGraphics;
    GlobalVars globals;
    boolean wait;
    int firstEvent, lastEvent;
    DecimalFormat twoDecimal;
    JFrame popupFrame;
    JTextArea popupText;
	
    public OutputPanel(GlobalVars globals){ //constructor
	this.globals=globals;
	totalHits = new int[8];
	voltage=pressure=acceptedEvents=presentedEvents=runNumber=flow="0";
	energyLow = 0; energyHigh = 20; globals.energyLow = energyLow; globals.energyHigh = energyHigh; energyLowCut = energyLow; energyHighCut = energyHigh;
	totalEnergyLow = 0; totalEnergyHigh = 2* energyHigh; globals.totalEnergyLow = totalEnergyLow; globals.totalEnergyHigh = totalEnergyHigh; totalEnergyLowCut = totalEnergyLow; totalEnergyHighCut = totalEnergyHigh;
	deltaELow = 0; deltaEHigh = 5; globals.deltaELow = deltaELow; globals.deltaEHigh = deltaEHigh; deltaELowCut = energyLow; deltaEHighCut = energyHigh;
	radiusLow = 0; radiusHigh = 60; globals.radiusLow = radiusLow; globals.radiusHigh = radiusHigh;
	angleLow = 0; angleHigh = 180.5; globals.angleLow = angleLow; globals.angleHigh = angleHigh; angleLowCut = angleLow; angleHighCut = angleHigh;
	zLow = -120; zHigh = 122; globals.zLow = zLow; globals.zHigh = zHigh; globals.zEndLow = zLow; globals.zEndHigh = zHigh; zLowCut = zLow; zHighCut = zHigh; zEndLowCut = zLow; zEndHighCut = zHigh;
	//		zEndLow = -1; zEndHigh = 62; 
	lengthLow= 0; lengthHigh = 300; globals.lengthLow= lengthLow; globals.lengthHigh = lengthHigh; lengthLowCut = lengthLow; lengthHighCut = lengthHigh;
	int[] anOffset = {150,430,150,430,150,150,400,400};
	sliceOffset = anOffset;

	// 		coincidenceSet = new HashSet<Integer>();
	// 		for(int i=0; i<paintSectors.length; i++){
	// 			paintSectors[i] = new Sector(i);
	// 		}
	tacColour = new Color[8];
	//		for(int i=0; i<tacColour.length; i++){
	tacColour[0]= new Color(160,16,23);
	tacColour[1]= new Color(5,94,154);
	tacColour[2]= new Color(0,153,77);
	tacColour[3]= new Color(192,204,59);
	tacColour[4]= new Color(86,33,122);
	tacColour[5]= new Color(43,162,148);
	tacColour[6]= new Color(222,141,42);
	tacColour[7]= new Color(10,34,69);
	//		}

	coincidenceList= new ArrayList<Integer>();
	collectSectors = new ArrayList<Sector>();
	workSectors = new ArrayList<Sector>();
	paintSectors = new ArrayList<Sector>();
	//		particleList = new ArrayList<Particle>(); 
	//		coincidenceList = new int[8];
	reInitAll();
	wait = false;
	firstEvent= Integer.MIN_VALUE;
	lastEvent = Integer.MAX_VALUE;
	addMouseListener(this);
	energyCut = false;
	angleCut = false;
	originCut = false;
	eventInfo = false;
	twoDecimal = new DecimalFormat("0.00");
	popupFrame = new JFrame("Event Info");
    } //end constructor


    public void mouseClicked(MouseEvent event) {
	if (globals.page==0){
	    int xPos=event.getX();
	    int yPos=event.getY();
	    boolean matchesParticle = false;
	    int particleID =0;
	    for (int i = 0; i<particleList2.size();i++){
		if (particleList2.get(i).matchCoordinates(xPos,yPos)){
		    particleID = i;
		    matchesParticle = true;
		    break;
		}
	    }
	    if (matchesParticle){
		popupText = new JTextArea(5, 20);
		popupText.setEditable(false);
		popupText.append(particleList2.get(particleID).popupInfo());
		if (globals.singlePopup) popupFrame.dispose();
		popupFrame = new JFrame("particle");
		popupFrame.add(popupText);
		popupFrame.pack();
		popupFrame.setVisible(true);
	    }
	    else if ((xPos>360) && (yPos>630)){
		popupText = new JTextArea(5, 20);
		popupText.setEditable(false);
		popupText.append(unknownString);
		if (globals.singlePopup) popupFrame.dispose();
		popupFrame = new JFrame("Unknown string");
		popupFrame.add(popupText);
		popupFrame.pack();
		popupFrame.setVisible(true);

	    }
			
	    else if ((paintSectors.size()>0) && (!eventInfo)){
		DecimalFormat oneDecimal = new DecimalFormat("0.0");
		popupText = new JTextArea(5, 20);
		//			JScrollPane scrollPane = new JScrollPane(textArea); 
		popupText.setEditable(false);
		popupText.append("Event: " + currentEvent + "\n");
		for (int i =0; i < paintSectors.size(); i++){
		    int s = paintSectors.get(i).getNumber();
		    popupText.append("sector: " + s + " \n Energy: " + twoDecimal.format(paintSectors.get(i).getTotalEnergy()) + " \n Endpoint Z: " + paintSectors.get(i).getLastZ() +"\t R: " + oneDecimal.format(paintSectors.get(i).getLastRadius()) +" \n Origin: " + oneDecimal.format(paintSectors.get(i).getOrigin()) + "\t Angle: " +  oneDecimal.format(paintSectors.get(i).getAngle()) + "\n ") ;
		}
			
		if (globals.singlePopup) popupFrame.dispose();
		popupFrame = new JFrame("Event " + currentEvent);
		popupFrame.add(popupText);
		popupFrame.pack();
		popupFrame.setVisible(true);
			
		eventInfo = true;
	    }
	}
    }
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event){}
    public void mouseEntered(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}


	
    public void sectorIni(){
	currentHits = new int[8];
	histBar =new HistBar[8];
	histCircle =new HistCircle(400,150,globals.stepSize, tacColour);
	sliceCircle = new HistCircle[4];
	for (int i = 0; i<sliceCircle.length; i++){
	    sliceCircle[i] = new HistCircle(sliceOffset[i],sliceOffset[i+4],globals.stepSize*2,tacColour);
	}
	for(int i=0; i<histBar.length; i++){
	    histBar[i] = new HistBar(i,totalHits[i],globals.stepSize);
	    if (globals.correctForShielding){
		if (i == 1 || i == 2) histBar[i].setStepSize(1.2*globals.stepSize);
		if (i == 4 || i == 7) histBar[i].setStepSize(1.5*globals.stepSize);
	    }
	}
	if (globals.correctForShielding){
	    for(int i=0; i<8; i++){
		if (i == 1 || i == 2){
		    histCircle.setStepSize(i,1.2*globals.stepSize);
		    for (int j = 0; j<4; j++){
			sliceCircle[j].setStepSize(i,1.2*globals.stepSize);
		    }
		}
		if (i == 4 || i == 7){
		    histCircle.setStepSize(i,1.5*globals.stepSize);
		    for (int j = 0; j<4; j++){
			sliceCircle[j].setStepSize(i,1.5*globals.stepSize);
		    }
		}
	    }
	}

    }

    public void setStepSize(){
	for(int i=0; i<histBar.length; i++){
	    histBar[i] = new HistBar(i,totalHits[i],globals.stepSize);
	    if (i == 1 || i == 2) histBar[i].setStepSize(1.2*globals.stepSize);
	    else if (i == 4 || i == 7) histBar[i].setStepSize(1.5*globals.stepSize);
	    //			else histBar[i].setStepSize(globals.stepSize);
	}
	histCircle =new HistCircle(400,150,globals.stepSize, tacColour);
	sliceCircle = new HistCircle[4];
	for (int i = 0; i<sliceCircle.length; i++){
	    sliceCircle[i] = new HistCircle(sliceOffset[i],sliceOffset[i+4],globals.stepSize*2,tacColour);
	}
	if (globals.correctForShielding){
	    for(int i=0; i<8; i++){
		if (i == 1 || i == 2){
		    histCircle.setStepSize(i,1.2*globals.stepSize);
		    for (int j = 0; j<4; j++){
			sliceCircle[j].setStepSize(i,1.2*globals.stepSize);
		    }
		}
		if (i == 4 || i == 7){
		    histCircle.setStepSize(i,1.5*globals.stepSize);
		    for (int j = 0; j<4; j++){
			sliceCircle[j].setStepSize(i,1.5*globals.stepSize);
		    }
		}
	    }
	}

    }

	
    public void initHistos(){
	radiusVsZAll = new TwoDHistogramFixed(500, 500, 30, 40,63,zLow,zHigh,240,radiusLow,radiusHigh);
	radiusVsZ = new TwoDHistogramFixed[8];
	for(int i=0; i<radiusVsZ.length; i++){
	    int[] offset = offsetHalf(i);
	    radiusVsZ[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,240,radiusLow,radiusHigh);
	}
	radiusVsZSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<radiusVsZSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    radiusVsZSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],121,zLow,zHigh,140,radiusLow,radiusHigh);
	}
	energyVsZAll = new TwoDHistogramFixed(500, 500, 30, 40,242,zLow,zHigh,800,energyLow,energyHigh);
	energyVsZ = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsZ.length; i++){
	    int[] offset = offsetHalf(i);
	    energyVsZ[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,800,energyLow,energyHigh);
	}
	energyVsZSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsZSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    energyVsZSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],121,zLow,zHigh,200,energyLow,energyHigh);
	}
	deltaEvsEAll = new TwoDHistogramFixed(500, 500, 30, 40,800,energyLow,energyHigh,200,deltaELow,deltaEHigh);
	deltaEvsE = new TwoDHistogramFixed[8];
	for(int i=0; i<deltaEvsE.length; i++){
	    int[] offset = offsetHalf(i);
	    deltaEvsE[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],800,energyLow,energyHigh,200,deltaELow,deltaEHigh);
	}
	deltaEvsESmall = new TwoDHistogramFixed[8];
	for(int i=0; i<deltaEvsESmall.length; i++){
	    int[] offset = offsetOverview(i);
	    deltaEvsESmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],200,energyLow,energyHigh,100,deltaELow,deltaEHigh);
	}
	energyVsAngleAll = new TwoDHistogramFixed(500, 500, 30, 40,361,angleLow,angleHigh,800,energyLow,energyHigh);
	energyVsAngle = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsAngle.length; i++){
	    int[] offset = offsetHalf(i);
	    energyVsAngle[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],361,angleLow,angleHigh,800,energyLow,energyHigh);
	}
	energyVsAngleSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsAngleSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    energyVsAngleSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],90,0,180,100,energyLow,energyHigh);
	}
	dEVsAngleAll = new TwoDHistogramFixed(500, 500, 30, 40,361,angleLow,angleHigh,800,deltaELow,deltaEHigh);
	dEVsAngle = new TwoDHistogramFixed[8];
	for(int i=0; i<dEVsAngle.length; i++){
	    int[] offset = offsetHalf(i);
	    dEVsAngle[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],361,angleLow,angleHigh,800,deltaELow,deltaEHigh);
	}
	dEVsAngleSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<dEVsAngleSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    dEVsAngleSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],90,0,180,200,deltaELow,deltaEHigh);
	}
	originVsAngleAll = new TwoDHistogramFixed(500, 500, 30, 40,361,angleLow,angleHigh,242,zLow,zHigh);
	originVsAngle = new TwoDHistogramFixed[8];
	for(int i=0; i<originVsAngle.length; i++){
	    int[] offset = offsetHalf(i);
	    originVsAngle[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],361,angleLow,angleHigh,242,zLow,zHigh);
	}
	originVsAngleSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<originVsAngleSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    originVsAngleSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],90,angleLow,angleHigh,121,zLow,zHigh);
	}
	energyVsOriginAll = new TwoDHistogramFixed(500, 500, 30, 40,242,zLow,zHigh,800,energyLow,energyHigh);
	energyVsOrigin = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsOrigin.length; i++){
	    int[] offset = offsetHalf(i);
	    energyVsOrigin[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],242,zLow,zHigh,600,energyLow,energyHigh);
	}
	energyVsOriginSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsOriginSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    energyVsOriginSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],242,zLow,zHigh,100,energyLow,energyHigh);
	}
	totalEnergyHist = new OneDHistogramFixed(Color.BLUE,800,totalEnergyLow,totalEnergyHigh);
	energyHist = new OneDHistogramFixed[8];
	energyHistAll = new OneDHistogramFixed(Color.BLUE,800,energyLow,energyHigh);
	for(int i=0; i<energyHist.length; i++){
	    energyHist[i] = new OneDHistogramFixed(tacColour[i],800,energyLow,energyHigh);
	}
	angleHistAll = new OneDHistogramFixed(Color.BLUE, 361,angleLow,angleHigh);
	angleHist = new OneDHistogramFixed[8];
	for(int i=0; i<angleHist.length; i++){
	    angleHist[i] = new OneDHistogramFixed(tacColour[i], 361,angleLow,angleHigh);
	}
	energyVsTotalLengthAll = new TwoDHistogramFixed(500, 500, 30, 40,300,lengthLow,lengthHigh,800,energyLow,energyHigh);
	energyVsTotalLength = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsTotalLength.length; i++){
	    int[] offset = offsetHalf(i);
	    energyVsTotalLength[i] = new TwoDHistogramFixed(300,265,offset[0],offset[1],150,lengthLow,lengthHigh,800,energyLow,energyHigh);
	}
	energyVsTotalLengthSmall = new TwoDHistogramFixed[8];
	for(int i=0; i<energyVsTotalLengthSmall.length; i++){
	    int[] offset = offsetOverview(i);
	    energyVsTotalLengthSmall[i] = new TwoDHistogramFixed(180,160,offset[0],offset[1],100,lengthLow,lengthHigh,200,energyLow,energyHigh);
	}
	radiusHist = new OneDHistogramFixed[8];
	radiusHistAll = new OneDHistogramFixed(Color.BLUE,240,radiusLow,radiusHigh);
	for(int i=0; i<radiusHist.length; i++){
	    radiusHist[i] = new OneDHistogramFixed(tacColour[i],240,radiusLow,radiusHigh);
	}
	coincidenceHist = new OneDHistogramFixed[8];
	for(int i=0; i<coincidenceHist.length; i++){
	    coincidenceHist[i] = new OneDHistogramFixed(tacColour[i], 8,0,8);
	}
	zHist = new OneDHistogramFixed[8];
	zHistAll = new OneDHistogramFixed(Color.BLUE,242,zLow,zHigh);
	for(int i=0; i<zHist.length; i++){
	    zHist[i] = new OneDHistogramFixed(tacColour[i],242,zLow,zHigh);
	}

    }

    public void reInitAll(){
	sectorIni();
	initHistos();
	sumTracks = sumEvents = 0;
	totalEnergy = 0;
	unknownStrings=0;
	particleList = new ArrayList<Particle>(); 
    }
    public void drawLines(Graphics g){
	g.setColor(Color.BLACK);
	g.drawLine(20,300,650,300);
	g.drawLine(20,450,650,450);
	g.drawLine(20,600,650,600);
    }

    public Dimension getPreferredSize() {
	return new Dimension(700,655);
    }

    public void changeSliceStep(int i){
	if (i > 3) i = i-4;
	sliceCircle[i] = new HistCircle(sliceOffset[i],sliceOffset[i+4],globals.stepSize*2,tacColour);
    }
	
    public int[] offsetOverview(int i){
	int[] offset = new int[2];
	offset[0] = 30;
	offset[1] = 0;
	if (i == 0) offset[1] = 15;
	if (i==1 || i ==2 || i==4 || i == 5 || i == 7) offset[0] = 220;
	if (i==3 || i == 6) {
	    offset[0]=-440;
	    offset[1]=195;
	}
	return offset;
    }

    public int[] offsetHalf(int i){
	int[] offset = new int[2];
	offset[0] = 30;
	offset[1] = 0;
	if (i == 0) offset[1] = 15;
	if (i==1 || i ==3 || i==5 || i ==7) offset[0] = 320;
	if (i==2 || i==6) {
	    offset[0]=-320;
	    offset[1]=285;
	}
	return offset;
    }


    public void paintOneDFixedOverview(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
	for(int i=0; i<histo.length; i++){
	    int[] offset = offsetOverview(i);
	    histo[i].paint(g, 180, 160, offset[0], offset[1], lowLimit, highLimit);
	}
    }
	
    public void paintOneDCircleOverview(OneDHistogramFixed[] histo, Graphics g){
	for(int i=0; i<histo.length; i++){
	    int[] offset = offsetOverview(i);
	    histo[i].paintCircle(g, 180, 160, offset[0], offset[1], i);
	}
    }
	
    public void paintOneDFixedFirstHalf(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
	for(int i=0; i<histo.length/2; i++){
	    int[] offset = offsetHalf(i);
	    histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
	}
    }

    public void paintOneDFixedSecondHalf(OneDHistogramFixed[] histo, Graphics g, double lowLimit, double highLimit){
	for(int i=histo.length/2; i<histo.length; i++){
	    int[] offset = offsetHalf(i);
	    histo[i].paint(g, 300, 265, offset[0], offset[1], lowLimit, highLimit);
	}
    }

    public void paintOneDCircleFirstHalf(OneDHistogramFixed[] histo, Graphics g){
	for(int i=0; i<histo.length/2; i++){
	    int[] offset = offsetHalf(i);
	    histo[i].paintCircle(g, 300, 265, offset[0], offset[1], i);
	}
    }

    public void paintOneDCircleSecondHalf(OneDHistogramFixed[] histo, Graphics g){
	for(int i=histo.length/2; i<histo.length; i++){
	    int[] offset = offsetHalf(i);
	    histo[i].paintCircle(g, 300, 265, offset[0], offset[1], i);
	}
    }


    public void drawPlots(Graphics g){
	if (DEBUG) System.out.println("OutputPanel: drawPlots");
	Graphics2D g2D = (Graphics2D) g;
	if ((globals.page) == 0){
	    g.setColor(Color.BLACK);
	    g.drawString("events: "+ String.valueOf(sumEvents),20,615);
	    if ((globals.speed > 1) || (globals.online)){
		if ((globals.gotContact) && (!globals.online))	g.drawString("File: "+ globals.inFile.getName(),450,615);
		if ((globals.gotContact) && (globals.online))	g.drawString("online: " + globals.hostName,450,615);
		if (sumEvents>0) g.drawString("tracks/event: "+ String.valueOf(twoDecimal.format((double)sumTracks/(double)sumEvents)),200,615);
		g.drawString("Presented: "+ presentedEvents,20,645);
		g.drawString("Accepted: "+ acceptedEvents,200,645);
		g.drawString("HV [V]: "+ voltage,20,630);
		g.drawString("P [mbar]: "+ pressure,200,630);			
		g.drawString("flow [ccm]: "+ flow,380,630);
		g.drawString("unknown strings: "+ unknownStrings,380,645);
		for (int i =0 ; i<paintSectors.size(); i++){
		    if (DEBUG) System.out.println("OutputPanel: draw sectors");

		    try{
			int sector = paintSectors.get(i).getNumber();
			if (globals.displaySector[sector]){
			    //					g.setColor(tacColour[sector]);
			    paintSectors.get(i).calculateCoordinates(globals.energyFactor,globals.logScale);
			    if (globals.drawTrack) paintSectors.get(i).paintTrack(g);
			    if (globals.drawLine) paintSectors.get(i).paintLine(g,globals.energyFactor);
			    if (globals.drawLabel) paintSectors.get(i).paintLabel(g, particleList2);
			}
		    }catch(IndexOutOfBoundsException e){
		    }catch(NullPointerException e){}
		}
		try{
		    histCircle.paint(g);
		}catch(NullPointerException e){}
		for(int i=0; i<histBar.length; i++){
		    g.setColor(tacColour[i]);
		    try{
			histBar[i].paint(g);
		    }catch(NullPointerException e){}
		}
		drawLines(g);
		//				particleList2.clear();
	    }

	}
	if (globals.page == 1) {
	    //g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    //g2D.drawString("Sectors", 300,600);
	    for (int i = 0; i < 4; i++){
		sliceCircle[i].paint(g);
	    }
	}
	if ((globals.page == 2) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Energy / MeV", 300,600);
	    totalEnergyHist.paint(g, 500, 500, 30, 40,globals.totalEnergyLow,globals.totalEnergyHigh);
	}
		
	if ((globals.page == 3) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Energy / MeV", 300,600);
	    switch(globals.display){
	    case 0: energyHistAll.paint(g, 500, 500, 30, 40,globals.energyLow,globals.energyHigh); break;
	    case 1: paintOneDFixedOverview(energyHist,g,globals.energyLow,globals.energyHigh); break;
	    case 2: paintOneDFixedFirstHalf(energyHist,g,globals.energyLow,globals.energyHigh); break;
	    case 3: paintOneDFixedSecondHalf(energyHist,g,globals.energyLow,globals.energyHigh); break;
	    }
	}
			
	if ((globals.page == 4) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("z Origin / mm", 300,600);
	    switch(globals.display){
	    case 0: zHistAll.paint(g, 500, 500, 30, 40,globals.zLow,globals.zHigh);break;
	    case 1: paintOneDFixedOverview(zHist,g,globals.zLow,globals.zHigh); break;
	    case 2: paintOneDFixedFirstHalf(zHist,g,globals.zLow,globals.zHigh); break;
	    case 3: paintOneDFixedSecondHalf(zHist,g,globals.zLow,globals.zHigh); break;
	    }
	}
	if ((globals.page == 5) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Energy / MeV", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("deltaE / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: deltaEvsEAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) deltaEvsESmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) deltaEvsE[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) deltaEvsE[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 6) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Z endpoint / pads", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("Energy / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: energyVsZAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) energyVsZSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) energyVsZ[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) energyVsZ[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 7) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Z endpoint / pads", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("Radius endpoint / mm", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: radiusVsZAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) radiusVsZSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) radiusVsZ[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) radiusVsZ[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 8) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("z Origin / pads", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("Energy / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: energyVsOriginAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) energyVsOriginSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) energyVsOrigin[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) energyVsOrigin[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 9) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Angle / degrees", 300,600);
	    switch(globals.display){
	    case 0: angleHistAll.paint(g, 500, 500, 30, 40,globals.angleLow,globals.angleHigh);break;
	    case 1: paintOneDFixedOverview(angleHist,g,globals.angleLow,globals.angleHigh); break;
	    case 2: paintOneDFixedFirstHalf(angleHist,g,globals.angleLow,globals.angleHigh); break;
	    case 3: paintOneDFixedSecondHalf(angleHist,g,globals.angleLow,globals.angleHigh); break;
	    }
	}
	if ((globals.page == 10) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Angle / degrees", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("Energy / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: energyVsAngleAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) energyVsAngleSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) energyVsAngle[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) energyVsAngle[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 11) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Angle / degrees", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("z Origin / pads", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: originVsAngleAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) originVsAngleSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) originVsAngle[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) originVsAngle[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 12) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Extrapolated track length along calculated slope / mm", 250,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("Energy / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: energyVsTotalLengthAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) energyVsTotalLengthSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) energyVsTotalLength[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) energyVsTotalLength[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 13) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Radius endpoint / mm", 300,600);
	    switch(globals.display){
	    case 0: radiusHistAll.paint(g, 500, 500, 30, 40,globals.radiusLow,globals.radiusHigh);break;
	    case 1: paintOneDFixedOverview(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
	    case 2: paintOneDFixedFirstHalf(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
	    case 3: paintOneDFixedSecondHalf(radiusHist,g,globals.radiusLow,globals.radiusHigh); break;
	    }
	}
	if ((globals.page == 14) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Angle / degrees", 300,600);
	    g2D.rotate(-Math.PI/2);
	    g2D.drawString("deltaE / MeV", -300,8);
	    g2D.rotate(Math.PI/2);
	    switch(globals.display){
	    case 0: dEVsAngleAll.drawHistogram(g); break;
	    case 1: for (int i = 0;i<8;i++) dEVsAngleSmall[i].drawHistogram(g); break;
	    case 2: for (int i = 0;i<4;i++) dEVsAngle[i].drawHistogram(g); break;
	    case 3: for (int i = 4;i<8;i++) dEVsAngle[i].drawHistogram(g); break;
	    }
	}
	if ((globals.page == 15) && ((globals.speed > 1) || (globals.online))) {
	    g2D.setFont(new Font("sansserif", Font.PLAIN, 10));
	    g2D.drawString("Sectors", 300,600);
	    switch(globals.coinc){
	    case 0: paintOneDCircleOverview(coincidenceHist,g); break;
	    case 1: paintOneDFixedOverview(coincidenceHist,g,0,7); break;
	    }
	}

    }

	
    public void paintComponent(Graphics g) {
	super.paintComponent(g);       
	//		if (DEBUG) System.out.println("Paint");
	setBackground(Color.WHITE);
	drawPlots(g);
    }
	
    public void update(Graphics g) { //update using 'offscreen' image
	Dimension d = new Dimension(700,555);

	if ((offGraphics == null)
	    || (d.width != offDimension.width)
	    || (d.height != offDimension.height)) {
	    offDimension = d;
	    offImage = createImage(d.width, d.height);
	    offGraphics = offImage.getGraphics();
	}

	setBackground(Color.WHITE);
	drawPlots(offGraphics);
	g.drawImage(offImage, 0, 0, null);
    }

    public void readSector(){
	//		if (DEBUG) System.out.println("reading sector num: " + Input);
	Input=Input.substring(1,2);
	CurrentSector = Integer.parseInt(Input);
	collectSectors.add(new Sector(CurrentSector, tacColour[CurrentSector]));
	//		if (DEBUG) System.out.println(CurrentSector);
					
    }		

    public void fillHistograms(int i, int sector){
	//		if (DEBUG) System.out.println("fillHistograms, sector: " + sector);
	double currentOrigin =workSectors.get(i).getOrigin();
	double currentAngle = workSectors.get(i).getAngle();
	double currentRadius = workSectors.get(i).getLastRadius();
	if (DEBUG) System.out.println("fill histos, energy: " + currentEnergy + " Origin: " + currentOrigin + " angle: " + currentAngle + " radius: " + currentRadius);
	boolean eCheck = ((currentEnergy >= energyLowCut) && (currentEnergy <= energyHighCut)) || (!energyCut);
	boolean tECheck = ((totalEnergy >= totalEnergyLowCut) && (totalEnergy <= totalEnergyHighCut)) || (!totalEnergyCut);
	boolean zCheck = ((currentOrigin >= zLowCut) && (currentOrigin <= zHighCut)) || (!originCut);
	boolean aCheck = ((currentAngle >= angleLowCut) && (currentAngle <= angleHighCut)) || (!angleCut);
	boolean dECheck = ((workSectors.get(i).getDeltaE() >= deltaELowCut) && (workSectors.get(i).getDeltaE() <= deltaEHighCut)) || (!deltaECut);
	if (eCheck && zCheck && aCheck && tECheck && dECheck){
	    if (DEBUG) System.out.println("passed cuts");
	    totalEnergyHist.updateHistogram(totalEnergy);
	    energyHist[sector].updateHistogram(currentEnergy);
	    zHist[sector].updateHistogram(currentOrigin);
	    energyHistAll.updateHistogram(currentEnergy);
	    angleHistAll.updateHistogram(currentAngle);
	    angleHist[sector].updateHistogram(currentAngle);
	    zHistAll.updateHistogram(currentOrigin);
	    radiusHistAll.updateHistogram(currentRadius);
	    radiusHist[sector].updateHistogram(currentRadius);
	    energyVsAngleAll.updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);
	    energyVsAngle[sector].updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);
	    energyVsAngleSmall[sector].updateHistogram(currentAngle,currentEnergy,globals.angleLow,globals.angleHigh, globals.energyLow,globals.energyHigh);

	    dEVsAngleAll.updateHistogram(currentAngle,workSectors.get(i).getDeltaE(),globals.angleLow,globals.angleHigh, globals.deltaELow,globals.deltaEHigh);
	    dEVsAngle[sector].updateHistogram(currentAngle,workSectors.get(i).getDeltaE(),globals.angleLow,globals.angleHigh, globals.deltaELow,globals.deltaEHigh);
	    dEVsAngleSmall[sector].updateHistogram(currentAngle,workSectors.get(i).getDeltaE(),globals.angleLow,globals.angleHigh, globals.deltaELow,globals.deltaEHigh);

	    originVsAngleAll.updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
	    originVsAngle[sector].updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
	    originVsAngleSmall[sector].updateHistogram(currentAngle,currentOrigin, globals.angleLow,globals.angleHigh,globals.zLow,globals.zHigh);
	    energyVsZAll.updateHistogram((workSectors.get(i).getLastZ()),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);
	    energyVsZ[sector].updateHistogram((workSectors.get(i).getLastZ()),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);
	    energyVsZSmall[sector].updateHistogram(workSectors.get(i).getLastZ(),currentEnergy,globals.zEndLow,globals.zEndHigh,globals.energyLow,globals.energyHigh);

	    deltaEvsEAll.updateHistogram(currentEnergy,workSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.deltaELow,globals.deltaEHigh);
	    deltaEvsE[sector].updateHistogram(currentEnergy,workSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.deltaELow,globals.deltaEHigh);
	    deltaEvsESmall[sector].updateHistogram(currentEnergy,workSectors.get(i).getDeltaE(),globals.energyLow,globals.energyHigh,globals.deltaELow,globals.deltaEHigh);

	    radiusVsZAll.updateHistogram(workSectors.get(i).getLastZ(),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
	    radiusVsZ[sector].updateHistogram(workSectors.get(i).getLastZ(),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
	    radiusVsZSmall[sector].updateHistogram((workSectors.get(i).getLastZ()),currentRadius,globals.zEndLow,globals.zEndHigh, globals.radiusLow,globals.radiusHigh);
	    energyVsOriginAll.updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
	    energyVsOrigin[sector].updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
	    energyVsOriginSmall[sector].updateHistogram(currentOrigin,currentEnergy,globals.zLow,globals.zHigh, globals.energyLow,globals.energyHigh);
	    energyVsTotalLengthAll.updateHistogram(workSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
	    energyVsTotalLength[sector].updateHistogram(workSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
	    energyVsTotalLengthSmall[sector].updateHistogram(workSectors.get(i).getTotalLength(),currentEnergy, globals.lengthLow,globals.lengthHigh, globals.energyLow,globals.energyHigh);
	}
	if (DEBUG) System.out.println("fillHistograms: done");

    }

    public void writeHistos(String outDir){
	if (DEBUG) System.out.println("writeHistos");
	HashMap<OneDHistogramFixed, String> histoOneMap = new HashMap<OneDHistogramFixed, String>();
	Set histoSet;
	histoOneMap.put(radiusHistAll,"radius_all");
	for (int i=0;i<radiusHist.length;i++){
	    histoOneMap.put(radiusHist[i],"radius_" + i);
	}
	histoOneMap.put(totalEnergyHist,"energy_total");
	histoOneMap.put(energyHistAll,"energy_all");
	for (int i=0;i<energyHist.length;i++){
	    histoOneMap.put(energyHist[i],"energy_" + i);
	}
	histoOneMap.put(angleHistAll,"angle_all");
	for (int i=0;i<angleHist.length;i++){
	    histoOneMap.put(angleHist[i],"angle_" + i);
	}
	for (int i=0;i<coincidenceHist.length;i++){
	    histoOneMap.put(coincidenceHist[i],"coincidence_" + i);
	}
	histoSet = histoOneMap.entrySet();
	Iterator iter = histoSet.iterator();
	while (iter.hasNext()){
	    Map.Entry me = (Map.Entry)iter.next();
	    OneDHistogramFixed tempHist = (OneDHistogramFixed)me.getKey();
	    //			System.out.println("name: " + me.getValue());
	    tempHist.writeData(outDir + File.separator + me.getValue() + ".dat");
	}
	HashMap<TwoDHistogramFixed, String> histoTwoMap = new HashMap<TwoDHistogramFixed, String>();
	histoTwoMap.put(energyVsZAll,"EnergyVsZEnd_all");
	for (int i = 0; i<energyVsZ.length; i++){
	    histoTwoMap.put(energyVsZ[i],"EnergyVsZEnd_" + i);
	}
	histoTwoMap.put(energyVsAngleAll,"EnergyVsAngle_all");
	for (int i = 0; i<energyVsAngle.length; i++) {
	    histoTwoMap.put(energyVsAngle[i],"EnergyVsAngle_" + i);
	}
	histoTwoMap.put(originVsAngleAll,"OriginVsAngle_all");
	for (int i = 0; i<originVsAngle.length; i++) {
	    histoTwoMap.put(originVsAngle[i],"OriginVsAngle_" + i);
	}
	histoTwoMap.put(radiusVsZAll,"RadiusVsZEnd_all");
	for (int i = 0; i<radiusVsZ.length; i++) {
	    histoTwoMap.put(radiusVsZ[i],"RadiusVsZEnd_" + i);
	}
	histoTwoMap.put(energyVsOriginAll,"EnergyVsOrigin_all");
	for (int i = 0; i<energyVsOrigin.length; i++) {
	    histoTwoMap.put(energyVsOrigin[i],"EnergyVsOrigin_" + i);
	}
	histoTwoMap.put(energyVsTotalLengthAll,"EnergyVsTotalLength_all");
	for (int i = 0; i<energyVsTotalLength.length; i++) {
	    histoTwoMap.put(energyVsTotalLength[i],"EnergyVsTotalLength_" + i);
	}
	histoTwoMap.put(deltaEvsEAll,"deltaEvsE_all");
	for (int i = 0; i<deltaEvsE.length; i++) {
	    histoTwoMap.put(deltaEvsE[i],"deltaEvsE_" + i);
	}
		
	histoSet = histoTwoMap.entrySet();
	iter = histoSet.iterator();
	while (iter.hasNext()){
	    Map.Entry me = (Map.Entry)iter.next();
	    TwoDHistogramFixed tempHist = (TwoDHistogramFixed)me.getKey();
	    tempHist.writeData(outDir + File.separator + me.getValue() + ".dat");
	}
	try{
	    BufferedWriter outFile = new BufferedWriter(new FileWriter(outDir + File.separator +"toasted.txt"));
	    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm z");
	    Date date = new Date();
	    if (globals.online) outFile.write(globals.hostName + "\t");
	    else if (!globals.online) outFile.write(globals.inFile + "\t");
	    outFile.write(dateFormat.format(date) + "\n");
	    outFile.write("latest voltage: " + voltage +"\n");
	    outFile.write("latest pressure: " + pressure +"\n");
	    outFile.write("latest flow: " + flow +"\n");
	    outFile.write("presented events: " + presentedEvents +"\n");
	    outFile.write("accepted events: " + acceptedEvents +"\n");
	    outFile.write("toasted events: " + sumEvents +"\n");
	    outFile.write("tracks: " + sumTracks +"\n");
	    if (globals.oneTrackPerSector) outFile.write("multiple tracks in one sector per event are added\n\n");
	    else if (!globals.oneTrackPerSector) outFile.write("multiple tracks in one sector per event are treated seperately\n\n");
	    outFile.write("Cuts: " +"\n");
	    if (globals.selectCoincidences) outFile.write("coincidences only");			
	    if (totalEnergyCut) outFile.write("total energy: [" + totalEnergyLowCut + ":" + totalEnergyHighCut + "]\n");
	    if (energyCut) outFile.write("energy: [" + energyLowCut + ":" + energyHighCut + "]\n");
	    if (angleCut) outFile.write("energy: [" + angleLowCut + ":" + angleHighCut + "]\n");
	    if (originCut) outFile.write("energy: [" + zLowCut + ":" + zHighCut + "]\n");
			
	    outFile.close();
	}catch (IOException e){
	    System.out.println("could not open file: " + outDir + File.separator + "toasted.txt" + " to write");
	}

    }
	

    public void readData()  { //read data
	if (DEBUG) System.out.println("start reading data");
	Input = null;
	while ((globals.gotContact) && (!globals.stop) ) {             //while incoming stream ok and no resets from main thread

	    try {  //try
		Input = globals.incoming.readLine();

		if (DEBUG) System.out.println("Input: " + Input );
		if (Input == null) {
		    if (DEBUG) System.out.println("Couldn't get I/O for reading.");
		    globals.gotContact = false;
		    break;
		}									
	    } catch (IOException e) {
		if (DEBUG) System.out.println("Couldn't get I/O for reading.");
		globals.gotContact = false;
		break;
	    }
			
	    if (Input.indexOf(":")>=0){                                 //get voltage, pressure et al
		//					if (DEBUG) System.out.println("read line: " + Input);
		Scanner inputScanner = new Scanner(Input);
		String[] temp=Input.split(":");
		if (DEBUG) System.out.println("name: " + temp[0] + "value: " + temp[1]);
		if (temp[0].matches("HV")) voltage=temp[1].trim();
		else if (temp[0].matches("P")) pressure=temp[1].trim();
		else if (temp[0].matches("R")) runNumber=temp[1].trim();
		else if (temp[0].matches("F")) flow=temp[1].trim();
		else if (temp[0].matches("PTE")) presentedEvents=temp[1].trim();
		else if (temp[0].matches("ATE")) acceptedEvents=temp[1].trim();
		else if (temp[0].matches("Particle")) {
		    Scanner particleScan = new Scanner(temp[1].trim());
		    particleList.add(new Particle(particleScan.next(),particleScan.nextDouble()));
		}
		//				if (!DEBUG && !globals.freezeTrack) repaint();
	    }

	    else if ((Input.startsWith("#")) && (Input.substring(1).matches("[^a-zA-Z]"))){ //input starts with #
		//			else if ((Input.startsWith("#")) ){ //input starts with #
		if (collectSectors.size()>0){            // general input with # and sector<8, check coincidences in sector
		    if (globals.oneTrackPerSector && coincidenceList.contains(CurrentSector)){
			collectSectors.get(coincidenceList.indexOf(CurrentSector)).addData(collectSectors.get(collectSectors.size()-1).getData());
			collectSectors.remove(collectSectors.size()-1);
		    }
		    else {
			totalHits[CurrentSector]++;
			currentHits[CurrentSector]++;
			if (!coincidenceList.contains(CurrentSector)) coincidenceList.add(CurrentSector);
			int maxDisplay = (int)(200/globals.stepSize);
			if (globals.correctForShielding) maxDisplay = (int)(200/(1.4*globals.stepSize));
					
			if (currentHits[CurrentSector] > maxDisplay){   //histogram full, zero
			    sectorIni();
			    currentHits[CurrentSector]++;
			    //			if (!DEBUG && !globals.freezeTrack) repaint();
			}

		    }
		}
		if ((Input.startsWith("#")) && (!Input.trim().endsWith("#"))){                                //input with # new sector
		    //					if (DEBUG) System.out.println("read sector");
		    readSector();
		    if (globals.stepEvents) wait = true;
		}
		else if (Input.startsWith("##") && (Input.trim().endsWith("#")) &&  (CurrentSector != 8)){   //end of event, fill histograms
		    globals.bgoEvent = false;
		    if ((!globals.selectCoincidences) || ((globals.selectCoincidences) && (coincidenceList.size() > 1))){
			//						if (DEBUG) System.out.println("end of coincident event");
			workSectors.clear();
			workSectors = new ArrayList<Sector>();
			if ((globals.paintEvent) && (!globals.freezeTrack)){
			    paintSectors.clear();
			    paintSectors = new ArrayList<Sector>();
			}

			sumEvents++;
			currentEvent = presentedEvents;
			eventInfo = false;
			for (int i =0 ; i<collectSectors.size(); i++){
							
			    workSectors.add(new Sector(collectSectors.get(i).getNumber(),collectSectors.get(i).getColour(),collectSectors.get(i).getData()));
			    totalEnergy += workSectors.get(workSectors.size()-1).getTotalEnergy();

			    histBar[collectSectors.get(i).getNumber()].incHeight();
			    histBar[collectSectors.get(i).getNumber()].setLabel(currentHits[collectSectors.get(i).getNumber()]);
			    histBar[collectSectors.get(i).getNumber()].setTotalHits(totalHits[collectSectors.get(i).getNumber()]); 
			    histCircle.incHeight(collectSectors.get(i).getNumber());
			    sumTracks++;
			}
			collectSectors.clear();
			collectSectors = new ArrayList<Sector>();
						
			for (int i =0 ; i<workSectors.size(); i++){
						
			    currentEnergy =workSectors.get(i).getTotalEnergy();
			    if (currentEnergy>0){                
				fillHistograms(i,workSectors.get(i).getNumber());
			    }
			    for (int j = 0; j < 4; j++){
				if ((workSectors.get(i).getFirstZ() >= globals.sliceLimit[j]) && (workSectors.get(i).getFirstZ() < globals.sliceLimit[j+4])) {
				    sliceCircle[j].incHeight(workSectors.get(i).getNumber());
				    if (sliceCircle[j].getHeight(workSectors.get(i).getNumber()) > 120) sliceCircle[i].reset(workSectors.get(i).getNumber());
				}
			    }
			    if ((globals.paintEvent) && (!globals.freezeTrack)) if (globals.displaySector[workSectors.get(i).getNumber()]) paintSectors.add(workSectors.get(i));
			}
			if (DEBUG) System.out.println("OutputPanel: sliceCircles done");

			Integer[] coincidences = (Integer[])coincidenceList.toArray(new Integer[coincidenceList.size()]);
			for (int i =0 ; i<coincidences.length-1; i++){
			    int sector = coincidences[i];
			    for (int j =i+1 ; j<coincidences.length; j++){
				int sector2 = coincidences[j];
				coincidenceHist[sector].updateHistogram((double)sector2);
				coincidenceHist[sector2].updateHistogram((double)sector);
			    }
			}
			//   								try {
			if (DEBUG) System.out.println("OutputPanel: coincidenceHists done");
			if ((!globals.freezeTrack) && (globals.paintEvent)) particleList2 = new ArrayList<Particle>(particleList);
			//						particleArray = particleList.toArray(new Particle[particleList.size()])
			//   						      SwingUtilities.invokeAndWait(new Runnable() {
			//   									  public void run() {
										  
			if ((!DEBUG) && (!globals.freezeTrack)) repaint();
			globals.paintEvent = false;
			//   									  }
			//   								  });
			//   							  	}catch (Exception exc) {
			//   									System.out.println("Can't create because of " + exc);
			//   								}
			if (DEBUG) System.out.println("OutputPanel: after repaint (not done in debug mode)");


			totalEnergy=0;
			coincidenceList.clear();
			particleList.clear();
			if (DEBUG) System.out.println("OutputPanel: cleared lists");

		    }
		    if (!globals.bgoEvent){
		    try{
			if (globals.speed>0) Thread.sleep(globals.speed);
		    } catch (InterruptedException e){
			if (DEBUG) System.out.println("can't wait");
		    }
		    }
		    while (wait){
			try{
			    Thread.sleep(100);
			} catch (InterruptedException e){
			    if (DEBUG) System.out.println("can't wait");
			}
						
		    }
		    if (DEBUG) System.out.println("OutputPanel: end of ## stuff");

		}
	    }
					
	    else if (Input.startsWith("#")){ //input starts with #, but no sector number
		unknownStrings++;
		unknownString=Input;
		globals.bgoEvent = true;
	    }
	    else {
		if ((CurrentSector<8) && (!globals.bgoEvent) ) {                // get track only if wanted
		    try {
			if (DEBUG) System.out.println("read sub-sub-line: " + Input);
			Scanner inputScanner = new Scanner(Input);
			ArrayList<Double> inputData = new ArrayList<Double>();
			inputData.add(Double.parseDouble(inputScanner.next())-120);
			while (inputScanner.hasNext()){
			    double temp = Double.parseDouble(inputScanner.next());
			    inputData.add(temp);
			}
			//							collectSectors.get(collectSectors.size()-1).addPoint(zPos, radTemp, enerTemp, particleNumber);
			//					}
			collectSectors.get(collectSectors.size()-1).addPoint(inputData);
			if (globals.displaySector[CurrentSector]) globals.paintEvent = true;
		    } catch (Exception e) {
			if (DEBUG) System.out.println("could not split input");
			unknownStrings++;
			unknownString=Input;
			if (!DEBUG && !globals.freezeTrack && globals.paintEvent) {repaint();globals.paintEvent=false;}
		    }
		}

	    }
	}    //while incoming stream and no stop sign

    }  // end of run()


    // public int[] parseEvents(){
    // 	Input = null;
    // 	firstEvent= Integer.MIN_VALUE;
    // 	lastEvent = Integer.MAX_VALUE;
    // 	boolean eventEnd = false;
    // 	try {                                             //try
    // 		inFile = new FileReader(globals.inFile);
    // 		BufferedReader in = new BufferedReader(inFile);
    // 		while (((Input = in.readLine()) != null) && (!globals.stop) ) {  //while lines in file
    // 			if (Input.indexOf(":")>=0){                                 //get voltage, pressure et al
    // 				String[] temp=Input.split(":");
    // 				if (temp[0].matches("PTE")) {
    // 					if (firstEvent == Integer.MIN_VALUE) firstEvent=Integer.parseInt(temp[1].trim());
    // 					lastEvent=Integer.parseInt(temp[1].trim());
    // 					eventEnd = false;
    // 				}
    // 			}
    // 			else if (Input.startsWith("##") && (Input.trim().endsWith("#"))) eventEnd= true;
    // 		}
    // 		if (!eventEnd) lastEvent -= 1;
    // 	} catch (Exception e) {
    // 		System.err.println("couldn't read file"); 
    // 	}
    // 	int[] eventRange = new int[2];
    // 	eventRange[0] = firstEvent;
    // 	eventRange[1] = lastEvent;
		
    // 	return eventRange;
    // }
	
}

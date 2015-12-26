// part of Toast
// author: Ulrike Hager

import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class GlobalVars{
    public static boolean gotContact;
    public static boolean freezeTrack;
    public  boolean stop;
    public  boolean correctForShielding;
    public  boolean selectCoincidences;
    boolean bgoEvent;
    boolean paintEvent;
    boolean drawLine;
    boolean drawTrack, drawLabel;
    boolean stepEvents;
    boolean online;
    boolean loopFile;
    boolean singlePopup;
    boolean oneTrackPerSector;
    String hostName;
    boolean logScale;
    int page, display, coinc;
    int energyFactor, speed;
    double stepSize;
    public double energyLow, energyHigh, zLow, zHigh, angleLow, angleHigh, zEndLow, zEndHigh, radiusLow, radiusHigh, lengthLow, lengthHigh, totalEnergyLow, totalEnergyHigh, deltaELow, deltaEHigh;
    BufferedReader incoming;
    File inFile;
    boolean[] displaySector;
    public double[] sliceLimit;

    public GlobalVars(){
	gotContact = false;
	stepSize=1;
	energyFactor = 100;
	page = display = coinc = 0;
	stop = false;
	freezeTrack = false;
	paintEvent = false;
	bgoEvent = false;
	correctForShielding = false;
	selectCoincidences = false;
	stepEvents = false;
	online = false;
	drawLine=false;
	drawLabel=false;
	drawTrack = true;
	loopFile = false;
	singlePopup = true;
	oneTrackPerSector = false;
	logScale = false;
	hostName = "localhost";
	energyLow = energyHigh = zLow = zHigh = angleLow = angleHigh = zEndLow = zEndHigh = radiusLow = radiusHigh = lengthLow = lengthHigh = totalEnergyLow = totalEnergyHigh = deltaELow = deltaEHigh = Double.NaN;
	sliceLimit = new double[8];
	for (int i = 0; i<4; i++){
	    sliceLimit[i] = i*60-120;
	    sliceLimit[i+4] = (i+1)*60-120;
	}
	displaySector = new boolean[8];
	for (int i = 0; i<8; i++){
	    displaySector[i] = true;
	}

    }
}

// part of Toast
// author: Ulrike Hager

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.lang.*;

class TacticViewer extends JFrame implements ActionListener, ListSelectionListener, ChangeListener{        //class TacticUI
	private static final boolean DEBUG = false;

	boolean allStop = false;
//	static boolean loopOn = false;
	boolean socketOpen = false;
	static Socket tacticSocket;
//	String hostName;
	int hostPort;
	Thread daqThread, connectThread;
	GlobalVars globals;
	OutputPanel outputPanel;
	JTextArea cutText;
	JLabel stepLabel, tmLabel, energyLabel;
	JSpinner stepSpinner, energySpinner, energyLowLimit, energyHighLimit, zLowLimit, zHighLimit, angleLowLimit, angleHighLimit, radiusLowLimit, radiusHighLimit, zEndLowLimit, zEndHighLimit, lengthLowLimit, lengthHighLimit, totalEnergyLowLimit, totalEnergyHighLimit, deltaELowLimit, deltaEHighLimit;
	JSpinner[] sliceLimit;
	JPanel buttonPanel;
	JButton setButton, energyResetButton, scaleButton, resetTotalHitsButton, nextButton;
	JCheckBox setEnergyLimit, setZLimit, setAngleLimit, setRadiusLimit, setZEndLimit, setLengthLimit, setTotalEnergyLimit, setDeltaELimit;
	JList pageChoice, displayChoice, coincChoice;
	JToggleButton freezeButton;
	JMenuBar menuBar;
	JMenu settingsMenu, fileMenu, speedMenu, viewMenu, cutMenu, displaySectorMenu;
	JMenuItem loadFile, saveFile, rerunFile, openSocket, localConnect;
	JCheckBoxMenuItem  correctForShielding, selectCoincidences, drawLine, drawTrack, drawLabel, loopFile, singlePopup, oneTrackPerSector, logScale, zCut, angleCut, energyCut, totalEnergyCut, deltaECut;
	JCheckBoxMenuItem[] displaySector; 
	JRadioButtonMenuItem maxSpeed, fastSpeed, mediumSpeed, slowSpeed, crawlSpeed, eventSpeed;
	ButtonGroup speedGroup;
	JFileChooser fileChooser;
	RetrieveData retrieveData;

	class RetrieveData extends SwingWorker<Integer,Void>{
	public void waitForSocket(){ //if server didn't answer, wait and try again
//			synchronized(this) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) { }
			}
//		}

		public void connectSocket() { //connect Socket
			while (!globals.gotContact){
				if (DEBUG) System.out.println("running connection thread, hostname: " + globals.hostName + " port: " + hostPort);
				if (allStop) return;
				waitForSocket();
				try {
					tacticSocket = new Socket(globals.hostName, hostPort);
					globals.incoming = new BufferedReader(new InputStreamReader(tacticSocket.getInputStream()));
					globals.gotContact = true;
					socketOpen = true;
					if (DEBUG) System.out.println("connected");
				} catch (UnknownHostException e) {
					System.out.println("Unknown host.");
			JFrame popupFrame = new JFrame("Error!");
			JTextArea textArea = new JTextArea(5, 20);
			textArea.setEditable(false);
				textArea.append("Unknown host " + globals.hostName);
				globals.online= false;
				saveFile.setEnabled(false);
				} catch (IOException e) {
					if (DEBUG) System.out.println("Couldn't get I/O for the connection.");
					waitForSocket();
				}
			}
		}
				public void openFile() { //openFile
			if (DEBUG) System.out.println("running readFile thread");
			if (allStop) return;
			try {
				FileReader inFile = new FileReader(globals.inFile);
				globals.incoming = new BufferedReader(inFile);
				globals.gotContact = true;
				if (DEBUG) System.out.println("reading file");
			} catch (IOException e) {
				if (DEBUG) System.out.println("Couldn't open file.");
			}
		}

			public void stopConnection() { //stop
		if (DEBUG) System.out.println("stopping connection");
		try {
			globals.gotContact = false;
			if (socketOpen){
				tacticSocket.close();
			if (DEBUG) System.out.println("socket closed");
				socketOpen = false;
			}
			globals.incoming.close();
			if (DEBUG) System.out.println("incoming closed");
//			globals.gotContact = false;
		} catch (IOException e) {
			if (DEBUG) System.out.println("stopConnection" + e);
		}
		if (DEBUG) System.out.println("connection stopped");
	}


		@Override
		protected Integer doInBackground(){
			boolean loopOn = true;
			while ((loopOn) && (!allStop)){
			if ((globals.online) && (!globals.gotContact)) connectSocket();
			if ((!globals.online) && (!globals.gotContact)) openFile();
		outputPanel.readData();
		if ((globals.online) || (globals.loopFile)) loopOn = true;
		else loopOn = false;
		if (globals.loopFile) outputPanel.reInitAll();
		if (globals.online){
// 				try{
// 					Thread.sleep(100);
// 				} catch (InterruptedException e) {
// 					System.out.println("end of loop: no sleep");
// 				}
			stopConnection();
		}
			}
		return 0;
		}
		@Override
		protected void done(){
			stopConnection();
		}
		}
		


	public void layoutButtonPanel(){
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setBackground(Color.WHITE);
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5,0,10,0);
		c.gridheight = 13;
		c.weightx = 0.5;
		c.weighty = 13;
		c.gridx = 0;
		c.gridy = 0;
		buttonPanel.add(pageChoice,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,10,0,10);
		c.gridheight = 3;
		c.gridwidth = 2;
		c.weightx = 1;
		c.weighty = 3;
		c.gridx = 0;
		c.gridy = 0;
		buttonPanel.add(cutText,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.insets = new Insets(0,0,0,0);
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 18;
		buttonPanel.add(freezeButton,c);
		
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,0,0,0);
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 19;
		buttonPanel.add(energyResetButton,c);

 		c.anchor = GridBagConstraints.LAST_LINE_END;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.weightx = 0.5;
 		c.weighty = 1;
 		c.gridx = 0;
 		c.gridy = 22;
 		buttonPanel.add(nextButton,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 22;
		buttonPanel.add(resetTotalHitsButton,c);

		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridwidth = 1;
		c.weightx = 0.6;
		c.weighty = 0.6;
		c.gridx = 1;
		c.gridy = 23;
		buttonPanel.add(tmLabel,c);

		switch(globals.page){  //switch
		case 0:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.weightx = 0.5;
			c.weighty = 1;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(stepSpinner,c);
		
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(setButton,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.insets = new Insets(0,0,5,0);
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(energySpinner,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 16;
			buttonPanel.add(scaleButton,c);
			break;
			
		case 1:
			for (int i = 0; i<4; i++){
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14+i;
			buttonPanel.add(sliceLimit[i],c);

			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 14+i;
			buttonPanel.add(sliceLimit[i+4],c);
			}
			break;

		case 2:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setTotalEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(totalEnergyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(totalEnergyHighLimit,c);

			break;

		case 3:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,10,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);

			break;
		
		case 4:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 5:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setDeltaELimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(deltaELowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(deltaEHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,10,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);

			break;
		
		case 6:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZEndLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zEndLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zEndHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 7:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZEndLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zEndLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zEndHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setRadiusLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(radiusLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(radiusHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;
		

		case 8:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,80,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 9:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(angleHighLimit,c);		

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 10:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(angleHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,80,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 11:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setZLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(zLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(zHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(angleHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,80,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 12:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setLengthLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(lengthLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(lengthHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setEnergyLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(energyLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(energyHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

			
		case 13:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setRadiusLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(radiusLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(radiusHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;
		
		case 14:
			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 14;
			buttonPanel.add(setDeltaELimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 15;
			buttonPanel.add(deltaELowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 15;
			buttonPanel.add(deltaEHighLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 16;
			buttonPanel.add(setAngleLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 0;
			c.gridy = 17;
			buttonPanel.add(angleLowLimit,c);

			c.anchor = GridBagConstraints.LAST_LINE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(0,0,0,0);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.weightx = 0.5;
			c.weighty = 1;
			c.gridx = 1;
			c.gridy = 17;
			buttonPanel.add(angleHighLimit,c);

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,80,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(displayChoice,c);
			break;

		case 15:

			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.insets = new Insets(5,0,50,0);
			c.gridheight = 10;
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = 0;
			buttonPanel.add(coincChoice,c);

			break;
		}
	}
	
	private TacticViewer(){
		
		globals = new GlobalVars();
//		hostName = "localhost";
		fileChooser = new JFileChooser();
		outputPanel = new OutputPanel(globals);
		buttonPanel = new JPanel();
		retrieveData = new RetrieveData();
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.getAccessibleContext().setAccessibleDescription("load input file, save histograms (perhaps one day)");
		settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_A);
		settingsMenu.getAccessibleContext().setAccessibleDescription("Various settings");
		speedMenu = new JMenu("Speed");
		viewMenu = new JMenu("View");
		cutMenu = new JMenu("Cuts");
		displaySectorMenu = new JMenu("Sectors");
		displaySectorMenu.setToolTipText("Display the tracks of the chosen sectors, does not affect the histograms");
		menuBar.add(fileMenu);
		menuBar.add(settingsMenu);
		menuBar.add(viewMenu);
		menuBar.add(speedMenu);
		menuBar.add(cutMenu);
		menuBar.add(displaySectorMenu);
		
		String[] masterPages = {"Hits&Tracks", "Beachball/z", "Total energy/event","Energy","Z origin","dE vs E","E vs Z end","R end vs Z end", "E vs Z origin", "Angle", "E vs Angle", "Z origin vs Angle", "E vs length", "R end", "dE vs Angle","Coincidences"};
		pageChoice = new JList(masterPages);
		pageChoice.setSelectedIndex(0);
		pageChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pageChoice.addListSelectionListener(this);
        pageChoice.setVisibleRowCount(16);

		String[] subPages ={"Sum", "Overview", "0-3","4-7"};

		displayChoice = new JList(subPages);
		displayChoice.setSelectedIndex(globals.display);
		displayChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		displayChoice.addListSelectionListener(this);
        displayChoice.setVisibleRowCount(4);

		String[] coincPages ={"Windmills", "Bars"};

		coincChoice = new JList(coincPages);
		coincChoice.setSelectedIndex(globals.coinc);
		coincChoice.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coincChoice.addListSelectionListener(this);
        coincChoice.setVisibleRowCount(2);

		writeCutText();

		setButton = new JButton("Scale hits");
		setButton.addActionListener(this);
		setButton.setToolTipText("Resets the hits and applies the step size, does not affect the histograms on the other pages.");
		
		nextButton = new JButton("next event");
		nextButton.addActionListener(this);
		nextButton.setToolTipText("If speed is set to 'event-by-event', process next event");
		nextButton.setEnabled(false);
		
		SpinnerModel stepModel = new SpinnerNumberModel(1,0.1,20,0.5);
		stepSpinner = new JSpinner(stepModel);
		stepSpinner.setEditor(new JSpinner.NumberEditor(stepSpinner, "#.#"));
		stepSpinner.setValue(new Double(1.0));
		stepSpinner.setToolTipText("Sets the increment in the hits plots, choose a larger value for lower count rates.");
		
		resetTotalHitsButton = new JButton("Reset all hits");
		resetTotalHitsButton.addActionListener(this);
		resetTotalHitsButton.setToolTipText("Resets all hits and applies the step size, does not affect the histograms on the other pages.");

		freezeButton = new JToggleButton("Freeze",globals.freezeTrack);
		freezeButton.addActionListener(this);
		freezeButton.setToolTipText("Freezes display of one track, does not affect the histograms.");
		
		scaleButton = new JButton("Scale energy");
		scaleButton.addActionListener(this);
		scaleButton.setToolTipText("Scale the energy to adjust marker size, does not affect the histograms.");

		SpinnerModel energyModel = new SpinnerNumberModel(50,1,Integer.MAX_VALUE,5);
		energySpinner = new JSpinner(energyModel);
		energySpinner.setEditor(new JSpinner.NumberEditor(energySpinner, "#"));
		energySpinner.setValue(new Integer(50));
		globals.energyFactor = 50;
		energySpinner.setToolTipText("Sets the scaling of the energy [MeV] for drawing tracks, does not affect histograms.");
		
		energyResetButton = new JButton("Zero histograms");
		energyResetButton.addActionListener(this);
		energyResetButton.setToolTipText("Resets the energy and z histograms, does not affect the hits.");

		tmLabel = new JLabel("TOAST created by Ulrike");
		tmLabel.setForeground(new Color(30,30,150));
		Font usedFont = tmLabel.getFont();
		Font smallFont = usedFont.deriveFont(8.5f);
		tmLabel.setFont(smallFont);
		tmLabel.setVerticalTextPosition(JLabel.BOTTOM);
		tmLabel.setHorizontalTextPosition(JLabel.RIGHT);

		SpinnerModel energyLowLimitModel = new SpinnerNumberModel(0,-1,100,0.1);
		energyLowLimit = new JSpinner(energyLowLimitModel);
		energyLowLimit.setEditor(new JSpinner.NumberEditor(energyLowLimit, "#.#"));
		energyLowLimit.setValue(new Double(0.0));
		energyLowLimit.setToolTipText("Sets the lower limit in energy histograms.");

		SpinnerModel energyHighLimitModel = new SpinnerNumberModel(0,0,100,0.1);
		energyHighLimit = new JSpinner(energyHighLimitModel);
		energyHighLimit.setEditor(new JSpinner.NumberEditor(energyHighLimit, "#.#"));
		energyHighLimit.setValue(new Double(5.0));
		energyHighLimit.setToolTipText("Sets the upper limit in energy histograms.");

		setEnergyLimit = new JCheckBox("set energy limits");
		setEnergyLimit.setSelected(false);
		setEnergyLimit.addActionListener(this);
		setEnergyLimit.setToolTipText("Use the chosen limits for the energy histograms. Uncheck and recheck to update limits.");

		SpinnerModel totalEnergyLowLimitModel = new SpinnerNumberModel(0,-1,100,0.1);
		totalEnergyLowLimit = new JSpinner(totalEnergyLowLimitModel);
		totalEnergyLowLimit.setEditor(new JSpinner.NumberEditor(totalEnergyLowLimit, "#.#"));
		totalEnergyLowLimit.setValue(new Double(0.0));
		totalEnergyLowLimit.setToolTipText("Sets the lower limit in the total energy histogram.");

		SpinnerModel totalEnergyHighLimitModel = new SpinnerNumberModel(0,0,100,0.1);
		totalEnergyHighLimit = new JSpinner(totalEnergyHighLimitModel);
		totalEnergyHighLimit.setEditor(new JSpinner.NumberEditor(totalEnergyHighLimit, "#.#"));
		totalEnergyHighLimit.setValue(new Double(5.0));
		totalEnergyHighLimit.setToolTipText("Sets the upper limit in the total energy histogram.");

		setTotalEnergyLimit = new JCheckBox("total energy limits");
		setTotalEnergyLimit.setSelected(false);
		setTotalEnergyLimit.addActionListener(this);
		setTotalEnergyLimit.setToolTipText("Use the chosen limits for the total energy histogram. Uncheck and recheck to update limits.");

		SpinnerModel[] sliceModel = new SpinnerNumberModel[8];
		for (int i = 0; i<8; i++){
			sliceModel[i] = new SpinnerNumberModel(-120,-120,120,1.0);
		}
		sliceLimit = new JSpinner[8];
		for (int i = 0; i<8; i++){
			sliceLimit[i] = new JSpinner(sliceModel[i]);
			sliceLimit[i].setEditor(new JSpinner.NumberEditor(sliceLimit[i],"#"));
			sliceLimit[i].setValue(globals.sliceLimit[i]);
			sliceLimit[i].addChangeListener(this);
		}

		SpinnerModel zLowLimitModel = new SpinnerNumberModel(-120,-121,120,1.0);
		zLowLimit = new JSpinner(zLowLimitModel);
		zLowLimit.setEditor(new JSpinner.NumberEditor(zLowLimit, "#"));
		zLowLimit.setValue(new Double(-120.0));
		zLowLimit.setToolTipText("Sets the lower limit in z histograms.");

		SpinnerModel zHighLimitModel = new SpinnerNumberModel(120,-120,122.0,1.0);
		zHighLimit = new JSpinner(zHighLimitModel);
		zHighLimit.setEditor(new JSpinner.NumberEditor(zHighLimit, "#"));
		zHighLimit.setValue(new Double(120.0));
		zHighLimit.setToolTipText("Sets the upper limit in z histograms.");

		setZLimit = new JCheckBox("set z origin limits");
		setZLimit.setSelected(false);
		setZLimit.addActionListener(this);
		setZLimit.setToolTipText("Use the chosen limits for the z origin histograms. Uncheck and recheck to update limits.");

		SpinnerModel angleLowLimitModel = new SpinnerNumberModel(0,-180.0,180.0,1.0);
		angleLowLimit = new JSpinner(angleLowLimitModel);
		angleLowLimit.setEditor(new JSpinner.NumberEditor(angleLowLimit, "#"));
		angleLowLimit.setValue(new Double(0.0));
		angleLowLimit.setToolTipText("Sets the lower limit in angle histograms.");

		SpinnerModel angleHighLimitModel = new SpinnerNumberModel(0,-180.0,180.0,1.0);
		angleHighLimit = new JSpinner(angleHighLimitModel);
		angleHighLimit.setEditor(new JSpinner.NumberEditor(angleHighLimit, "#"));
		angleHighLimit.setValue(new Double(90.0));
		angleHighLimit.setToolTipText("Sets the upper limit in angle histograms.");

		setAngleLimit = new JCheckBox("set angle limits");
		setAngleLimit.setSelected(false);
		setAngleLimit.addActionListener(this);
		setAngleLimit.setToolTipText("Use the chosen limits for the angle histograms. Uncheck and recheck to update limits.");

		SpinnerModel radiusLowLimitModel = new SpinnerNumberModel(0,0,50.0,1.0);
		radiusLowLimit = new JSpinner(radiusLowLimitModel);
		radiusLowLimit.setEditor(new JSpinner.NumberEditor(radiusLowLimit, "#"));
		radiusLowLimit.setValue(new Double(12.0));
		radiusLowLimit.setToolTipText("Sets the lower limit in radius histograms.");

		SpinnerModel radiusHighLimitModel = new SpinnerNumberModel(0,0,70.0,1.0);
		radiusHighLimit = new JSpinner(radiusHighLimitModel);
		radiusHighLimit.setEditor(new JSpinner.NumberEditor(radiusHighLimit, "#"));
		radiusHighLimit.setValue(new Double(50.0));
		radiusHighLimit.setToolTipText("Sets the upper limit in radius histograms.");

		setRadiusLimit = new JCheckBox("set radius limits");
		setRadiusLimit.setSelected(false);
		setRadiusLimit.addActionListener(this);
		setRadiusLimit.setToolTipText("Use the chosen limits for the radius histograms. Uncheck and recheck to update limits.");

		SpinnerModel lengthLowLimitModel = new SpinnerNumberModel(0,0,200.0,1.0);
		lengthLowLimit = new JSpinner(lengthLowLimitModel);
		lengthLowLimit.setEditor(new JSpinner.NumberEditor(lengthLowLimit, "#"));
		lengthLowLimit.setValue(new Double(0.0));
		lengthLowLimit.setToolTipText("Sets the lower limit in length histograms.");

		SpinnerModel lengthHighLimitModel = new SpinnerNumberModel(0,0,200.0,1.0);
		lengthHighLimit = new JSpinner(lengthHighLimitModel);
		lengthHighLimit.setEditor(new JSpinner.NumberEditor(lengthHighLimit, "#"));
		lengthHighLimit.setValue(new Double(120.0));
		lengthHighLimit.setToolTipText("Sets the upper limit in length histograms.");

		setLengthLimit = new JCheckBox("set length limits");
		setLengthLimit.setSelected(false);
		setLengthLimit.addActionListener(this);
		setLengthLimit.setToolTipText("Use the chosen limits for the length histograms. Uncheck and recheck to update limits.");

		SpinnerModel zEndLowLimitModel = new SpinnerNumberModel(-121,-121,120.0,1.0);
		zEndLowLimit = new JSpinner(zEndLowLimitModel);
		zEndLowLimit.setEditor(new JSpinner.NumberEditor(zEndLowLimit, "#"));
		zEndLowLimit.setValue(new Double(-120.0));
		zEndLowLimit.setToolTipText("Sets the lower limit in z endpoint histograms.");

		SpinnerModel zEndHighLimitModel = new SpinnerNumberModel(120,-121,122.0,1.0);
		zEndHighLimit = new JSpinner(zEndHighLimitModel);
		zEndHighLimit.setEditor(new JSpinner.NumberEditor(zEndHighLimit, "#"));
		zEndHighLimit.setValue(new Double(120.0));
		zEndHighLimit.setToolTipText("Sets the upper limit in z endpoint histograms.");

		setZEndLimit = new JCheckBox("set z end limits");
		setZEndLimit.setSelected(false);
		setZEndLimit.addActionListener(this);
		setZEndLimit.setToolTipText("Use the chosen limits for the z endpoint histograms. Uncheck and recheck to update limits.");

		SpinnerModel deltaELowLimitModel = new SpinnerNumberModel(0,0,20.0,0.1);
		deltaELowLimit = new JSpinner(deltaELowLimitModel);
		deltaELowLimit.setEditor(new JSpinner.NumberEditor(deltaELowLimit, "#.#"));
		deltaELowLimit.setValue(new Double(0.0));
		deltaELowLimit.setToolTipText("Sets the lower limit in dE histograms.");

		SpinnerModel deltaEHighLimitModel = new SpinnerNumberModel(0,0,20.0,0.1);
		deltaEHighLimit = new JSpinner(deltaEHighLimitModel);
		deltaEHighLimit.setEditor(new JSpinner.NumberEditor(deltaEHighLimit, "#.#"));
		deltaEHighLimit.setValue(new Double(5.0));
		deltaEHighLimit.setToolTipText("Sets the upper limit in dE histograms.");

		setDeltaELimit = new JCheckBox("set deltaE limits");
		setDeltaELimit.setSelected(false);
		setDeltaELimit.addActionListener(this);
		setDeltaELimit.setToolTipText("Use the chosen limits for the dE histograms. Uncheck and recheck to update limits.");

		zCut = new JCheckBoxMenuItem("Origin");
		zCut.setSelected(false);
		zCut.addActionListener(this);
		zCut.setToolTipText("only fill histograms if the event origin lies within the specified limits, use reset button to clear histograms");
		cutMenu.add(zCut);

		angleCut = new JCheckBoxMenuItem("Angle");
		angleCut.setSelected(false);
		angleCut.addActionListener(this);
		angleCut.setToolTipText("only fill histograms if the angle lies within the specified limits, use reset button to clear histograms");
		cutMenu.add(angleCut);

		energyCut = new JCheckBoxMenuItem("Energy");
		energyCut.setSelected(false);
		energyCut.addActionListener(this);
		energyCut.setToolTipText("only fill histograms if the energy lies within the specified limits, use reset button to clear histograms");
		cutMenu.add(energyCut);

		totalEnergyCut = new JCheckBoxMenuItem("Total energy");
		totalEnergyCut.setSelected(false);
		totalEnergyCut.addActionListener(this);
		totalEnergyCut.setToolTipText("only fill histograms if the total energy lies within the specified limits, use reset button to clear histograms");
		cutMenu.add(totalEnergyCut);

		deltaECut = new JCheckBoxMenuItem("deltaE");
		deltaECut.setSelected(false);
		deltaECut.addActionListener(this);
		deltaECut.setToolTipText("only fill histograms if the energy of the second and third point of the track lies within the specified limits, use reset button to clear histograms");
		cutMenu.add(deltaECut);

		correctForShielding = new JCheckBoxMenuItem("Correct for shielding");
		correctForShielding.setSelected(globals.correctForShielding);
		correctForShielding.addActionListener(this);
		correctForShielding.setToolTipText("Scale the hits in the histograms to correct for shielding by the flutes. 1.2 for sectors 1 & 2, 1.5 for sectors 4 & 7");
		settingsMenu.add(correctForShielding);

		selectCoincidences = new JCheckBoxMenuItem("Coincidences only");
		selectCoincidences.setSelected(globals.selectCoincidences);
		selectCoincidences.addActionListener(this);
		selectCoincidences.setToolTipText("Show only coincidences in the histograms and tracks");
		settingsMenu.add(selectCoincidences);

		oneTrackPerSector = new JCheckBoxMenuItem("Single track per sector");
		oneTrackPerSector.setSelected(globals.oneTrackPerSector);
		oneTrackPerSector.addActionListener(this);
		oneTrackPerSector.setToolTipText("If the simulation gives several tracks in one sector for one event, the traacks will be added, using the largest energy if one pad has several hits");
		settingsMenu.add(oneTrackPerSector);

		drawTrack = new JCheckBoxMenuItem("Draw tracks");
		drawTrack.setSelected(globals.drawTrack);
		drawTrack.addActionListener(this);
		drawTrack.setToolTipText("draw colourful balls to represent the track");
		viewMenu.add(drawTrack);
		
		drawLine = new JCheckBoxMenuItem("Draw lines");
		drawLine.setSelected(globals.drawLine);
		drawLine.addActionListener(this);
		drawLine.setToolTipText("draw a line representing the calculated path");
		viewMenu.add(drawLine);

		drawLabel = new JCheckBoxMenuItem("Draw labels");
		drawLabel.setSelected(globals.drawLabel);
		drawLabel.addActionListener(this);
		drawLabel.setToolTipText("draw a line representing the calculated path");
		viewMenu.add(drawLabel);

		logScale = new JCheckBoxMenuItem("Log scale");
		logScale.setSelected(globals.logScale);
		logScale.addActionListener(this);
		logScale.setToolTipText("use a logarithmic scale for drawing tracks");
		viewMenu.add(logScale);

//		settingsMenu.addSeparator();
		
		singlePopup = new JCheckBoxMenuItem("Reuse event window");
		singlePopup.setSelected(globals.singlePopup);
		singlePopup.addActionListener(this);
		singlePopup.setToolTipText("Event information is displayed in the same popup rather than opening a new window for each event");
		viewMenu.add(singlePopup);

		
		speedGroup = new ButtonGroup();

		maxSpeed = new JRadioButtonMenuItem("Max");
		maxSpeed.setActionCommand("0");
		maxSpeed.addActionListener(this);
		speedGroup.add(maxSpeed);
		speedMenu.add(maxSpeed);
		
		fastSpeed = new JRadioButtonMenuItem("Fast");
		fastSpeed.setActionCommand("5");
		fastSpeed.addActionListener(this);
		speedGroup.add(fastSpeed);
		speedMenu.add(fastSpeed);
		
		mediumSpeed = new JRadioButtonMenuItem("Medium");
		mediumSpeed.setActionCommand("100");
		mediumSpeed.addActionListener(this);
		mediumSpeed.setSelected(true);
		speedGroup.add(mediumSpeed);
		speedMenu.add(mediumSpeed);		
		
		slowSpeed = new JRadioButtonMenuItem("Slow");
		slowSpeed.setActionCommand("500");
		slowSpeed.addActionListener(this);
		speedGroup.add(slowSpeed);
		speedMenu.add(slowSpeed);
		
		crawlSpeed = new JRadioButtonMenuItem("Crawl");
		crawlSpeed.setActionCommand("1500");
		crawlSpeed.addActionListener(this);
		speedGroup.add(crawlSpeed);
		speedMenu.add(crawlSpeed);
		
		eventSpeed = new JRadioButtonMenuItem("Event-by-event");
		eventSpeed.setActionCommand("2");
		eventSpeed.addActionListener(this);
		speedGroup.add(eventSpeed);
		speedMenu.add(eventSpeed);
		
		localConnect = new JMenuItem("localhost");
		localConnect.addActionListener(this);
		fileMenu.add(localConnect);
		
		loadFile = new JMenuItem("Load");
		loadFile.addActionListener(this);
		fileMenu.add(loadFile);

		openSocket = new JMenuItem("Online");
		openSocket.addActionListener(this);
		fileMenu.add(openSocket);

		rerunFile = new JMenuItem("Rerun");
		rerunFile.addActionListener(this);
		rerunFile.setEnabled(false);
		fileMenu.add(rerunFile);

		saveFile = new JMenuItem("Save");
		saveFile.addActionListener(this);
		saveFile.setEnabled(false);
		fileMenu.add(saveFile);
		
		loopFile = new JCheckBoxMenuItem("Loop file");
		loopFile.setSelected(globals.loopFile);
		loopFile.addActionListener(this);
		loopFile.setToolTipText("keep replaying the same file");
		fileMenu.add(loopFile);

		displaySector = new JCheckBoxMenuItem[8];
		for (int i=0; i<8; i++){
			displaySector[i] = new JCheckBoxMenuItem(""+i+"");
			displaySector[i].setSelected(globals.displaySector[i]);
			displaySector[i].addActionListener(this);
			displaySectorMenu.add(displaySector[i]);
		}
		
		layoutButtonPanel();
		setJMenuBar(menuBar);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		getContentPane().add(outputPanel);
		getContentPane().add(buttonPanel);
		
		setTitle("TOAST - TACTIC Offline Analysis and Sorting Tool");
//		setVisible(true);
		WindowListener l = new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			};
		addWindowListener(l);
		pack();
		globals.speed = Integer.parseInt(mediumSpeed.getActionCommand());

	}

	public void writeCutText(){
//		cutText = new JTextArea("Cuts:\n");
		cutText = new JTextArea();
		cutText.setEditable(false);
		if (outputPanel.energyCut) cutText.append("Energy:\t [" + outputPanel.energyLowCut + ":" + outputPanel.energyHighCut + "]\n");
		else cutText.append("Energy:\t no cut\n");
		if (outputPanel.totalEnergyCut) cutText.append("Total energy:\t [" + outputPanel.totalEnergyLowCut + ":" + outputPanel.totalEnergyHighCut + "]\n");
		else cutText.append("Total energy:\t no cut\n");
		if (outputPanel.originCut) cutText.append("Origin:\t [" + outputPanel.zLowCut + ":" + outputPanel.zHighCut + "]\n");
		else cutText.append("Origin:\t no cut\n");
		if (outputPanel.angleCut) cutText.append("Angle:\t [" + outputPanel.angleLowCut + ":" + outputPanel.angleHighCut + "]\n");
		else cutText.append("Angle:\t no cut\n");
		if (outputPanel.deltaECut) cutText.append("dE:\t [" + outputPanel.deltaELowCut + ":" + outputPanel.deltaEHighCut + "]\n");
		else cutText.append("dE:\t no cut\n");
		buttonPanel.revalidate();

	}

	public void actionPerformed(ActionEvent event){
		Object source = event.getSource();
		if(source == setButton){
			globals.stepSize=(Double)(stepSpinner.getValue());
			outputPanel.setStepSize();
		}
		if(source == resetTotalHitsButton){
//			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
//			outputPanel.wait =false;
//			globals.stop = true;
//			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
			outputPanel.wait =true;
			globals.stepSize=(Double)(stepSpinner.getValue());
			outputPanel.sectorIni();
			outputPanel.totalHits = new int[8];
			outputPanel.sumTracks = outputPanel.sumEvents = 0;
			outputPanel.repaint();
			outputPanel.wait =false;
//			globals.stop = false;
		}
		if(source == scaleButton){
			globals.energyFactor=(Integer)(energySpinner.getValue());
			outputPanel.repaint();
		}
		if(source == nextButton){
			outputPanel.wait = false;
		}
		if (source == setEnergyLimit){
			if (!setEnergyLimit.isSelected())
			{
				globals.energyLow = Double.NaN;
				globals.energyHigh = Double.NaN;
			}
			else if (setEnergyLimit.isSelected())
			{
				globals.energyLow = (Double)(energyLowLimit.getValue());
				globals.energyHigh = (Double)(energyHighLimit.getValue());
			}
			if (globals.energyLow>=globals.energyHigh) globals.energyHigh = globals.energyLow + 1;
			outputPanel.repaint();
		}
		if (source == setTotalEnergyLimit){
			if (!setTotalEnergyLimit.isSelected())
			{
				globals.totalEnergyLow = Double.NaN;
				globals.totalEnergyHigh = Double.NaN;
			}
			else if (setTotalEnergyLimit.isSelected())
			{
				globals.totalEnergyLow = (Double)(totalEnergyLowLimit.getValue());
				globals.totalEnergyHigh = (Double)(totalEnergyHighLimit.getValue());
			}
			if (globals.totalEnergyLow>=globals.totalEnergyHigh) globals.totalEnergyHigh = globals.totalEnergyLow + 1;
			outputPanel.repaint();
		}

		if (source == setZLimit){
			if (!setZLimit.isSelected())
			{
				globals.zLow = Double.NaN;
				globals.zHigh = Double.NaN;
			}
			else if (setZLimit.isSelected())
			{
				globals.zLow = (Double)(zLowLimit.getValue());
				globals.zHigh = (Double)(zHighLimit.getValue());
			}
			if (globals.zLow>=globals.zHigh) globals.zHigh = globals.zLow + 1;
			outputPanel.repaint();
		}
		if (source == setAngleLimit){
			if (!setAngleLimit.isSelected())
			{
				globals.angleLow = Double.NaN;
				globals.angleHigh = Double.NaN;
			}
			else if (setAngleLimit.isSelected())
			{
				globals.angleLow = (Double)(angleLowLimit.getValue());
				globals.angleHigh = (Double)(angleHighLimit.getValue());
			}
			if (globals.angleLow>=globals.angleHigh) globals.angleHigh = globals.angleLow + 1;
			outputPanel.repaint();
		}
		if (source == setRadiusLimit){
			if (!setRadiusLimit.isSelected())
			{
				globals.radiusLow = Double.NaN;
				globals.radiusHigh = Double.NaN;
			}
			else if (setRadiusLimit.isSelected())
			{
				globals.radiusLow = (Double)(radiusLowLimit.getValue());
				globals.radiusHigh = (Double)(radiusHighLimit.getValue());
			}
			if (globals.radiusLow>=globals.radiusHigh) globals.radiusHigh = globals.radiusLow + 1;
			outputPanel.repaint();

		}
		if (source == setLengthLimit){
			if (!setLengthLimit.isSelected())
			{
				globals.lengthLow = Double.NaN;
				globals.lengthHigh = Double.NaN;
			}
			else if (setLengthLimit.isSelected())
			{
				globals.lengthLow = (Double)(lengthLowLimit.getValue());
				globals.lengthHigh = (Double)(lengthHighLimit.getValue());
			}
			if (globals.lengthLow>=globals.lengthHigh) globals.lengthHigh = globals.lengthLow + 1;
			outputPanel.repaint();
		}
		if (source == setZEndLimit){
			if (!setZEndLimit.isSelected())
			{
				globals.zEndLow = Double.NaN;
				globals.zEndHigh = Double.NaN;
			}
			else if (setZEndLimit.isSelected())
			{
				globals.zEndLow = (Double)(zEndLowLimit.getValue());
				globals.zEndHigh = (Double)(zEndHighLimit.getValue());
			}
			if (globals.zEndLow>=globals.zEndHigh) globals.zEndHigh = globals.zEndLow + 1;
			outputPanel.repaint();
		}
		if (source == setDeltaELimit){
			if (!setDeltaELimit.isSelected())
			{
				globals.deltaELow = Double.NaN;
				globals.deltaEHigh = Double.NaN;
			}
			else if (setDeltaELimit.isSelected())
			{
				globals.deltaELow = (Double)(deltaELowLimit.getValue());
				globals.deltaEHigh = (Double)(deltaEHighLimit.getValue());
			}
			if (globals.deltaELow>=globals.deltaEHigh) globals.deltaEHigh = globals.deltaELow + 1;
			outputPanel.repaint();

		}
		if (source == correctForShielding){
				globals.correctForShielding = correctForShielding.isSelected();
		}

		if (source == oneTrackPerSector){
				globals.oneTrackPerSector = oneTrackPerSector.isSelected();
		}

		if (source == singlePopup){
				globals.singlePopup = singlePopup.isSelected();
		}

		if (source == selectCoincidences){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
				globals.selectCoincidences = selectCoincidences.isSelected();

		}

		if (source == drawTrack){
				globals.drawTrack = drawTrack.isSelected();
			outputPanel.repaint();
		}

		if (source == logScale){
				globals.logScale = logScale.isSelected();
			outputPanel.repaint();
		}

		if (source == drawLine){
				globals.drawLine = drawLine.isSelected();
			outputPanel.repaint();
		}

		if (source == drawLabel){
				globals.drawLabel = drawLabel.isSelected();
			outputPanel.repaint();
		}
		for (int i=0;i<8;i++){
		if (source == displaySector[i]){
				globals.displaySector[i] = displaySector[i].isSelected();
				break;
		}
		}
		if (source == zCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!zCut.isSelected())
			{
				outputPanel.zLowCut = outputPanel.zLow;
				outputPanel.zHighCut = outputPanel.zHigh;
				outputPanel.originCut = false;
 				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				
			}
			else if (zCut.isSelected())
			{
				double low, high;
				try{
				low = Double.parseDouble((String)JOptionPane.showInputDialog(this,"minimum ("+ outputPanel.zLow +"): ","Origin cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.zLowCut));
				high = Double.parseDouble((String)JOptionPane.showInputDialog(this,"maximum ("+ outputPanel.zHigh +"): ","Origin cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.zHighCut));
				}catch(NumberFormatException e){
					low = high = Double.MIN_VALUE;
					zCut.setSelected(false);
				}
				if ((low>=outputPanel.zLow) && (high<=outputPanel.zHigh) && (high>low)){
				outputPanel.zLowCut = low;
				outputPanel.zHighCut = high;
				outputPanel.originCut = true;
				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				}
				else zCut.setSelected(false); 
			}
		}
		if (source == deltaECut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!deltaECut.isSelected())
			{
				outputPanel.deltaELowCut = outputPanel.energyLow;
				outputPanel.deltaEHighCut = outputPanel.energyHigh;
				outputPanel.deltaECut = false;
 				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				
			}
			if (deltaECut.isSelected())
			{
				double low, high;
				try{
				low = Double.parseDouble((String)JOptionPane.showInputDialog(this,"minimum ("+ outputPanel.energyLow +"): ","deltaE cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.deltaELowCut));
				high = Double.parseDouble((String)JOptionPane.showInputDialog(this,"maximum ("+ outputPanel.energyHigh +"): ","deltaE cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.deltaEHighCut));
				}catch(NumberFormatException e){
					low = high = Double.MIN_VALUE;
					deltaECut.setSelected(false);
				}
				if ((low>=outputPanel.energyLow) && (high<=outputPanel.energyHigh) && (high>low)){
				outputPanel.deltaELowCut = low;
				outputPanel.deltaEHighCut = high;
				outputPanel.deltaECut = true;
				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				}
				else deltaECut.setSelected(false); 
			}
		}

		if (source == angleCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!angleCut.isSelected())
			{
				outputPanel.angleLowCut = outputPanel.angleLow;
				outputPanel.angleHighCut = outputPanel.angleHigh;
				outputPanel.angleCut = false;
 				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				
			}
			if (angleCut.isSelected())
			{
				double low, high;
				try{
				low = Double.parseDouble((String)JOptionPane.showInputDialog(this,"minimum ("+ outputPanel.angleLow +"): ","Angle cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.angleLowCut));
				high = Double.parseDouble((String)JOptionPane.showInputDialog(this,"maximum ("+ outputPanel.angleHigh +"): ","Angle cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.angleHighCut));
				}catch(NumberFormatException e){
					low = high = Double.MIN_VALUE;
					angleCut.setSelected(false);
				}
				if ((low>=outputPanel.angleLow) && (high<=outputPanel.angleHigh) && (high>low)){
				outputPanel.angleLowCut = low;
				outputPanel.angleHighCut = high;
				outputPanel.angleCut = true;
				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				}
				else angleCut.setSelected(false); 
			}

		}
		if (source == energyCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!energyCut.isSelected())
			{
				outputPanel.energyLowCut = outputPanel.energyLow;
				outputPanel.energyHighCut = outputPanel.energyHigh;
				outputPanel.energyCut = false;
 				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();

			}
			else if (energyCut.isSelected())
			{
				double low, high;
				try{
				low = Double.parseDouble((String)JOptionPane.showInputDialog(this,"minimum ("+ outputPanel.energyLow +"): ","Energy cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.energyLowCut));
				high = Double.parseDouble((String)JOptionPane.showInputDialog(this,"maximum ("+ outputPanel.energyHigh +"): ","Energy cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.energyHighCut));
				}catch(NumberFormatException e){
					low = high = Double.MIN_VALUE;
					energyCut.setSelected(false);
				}
				if ((low>=outputPanel.energyLow) && (high<=outputPanel.energyHigh) && (high>low)){
				outputPanel.energyLowCut = low;
				outputPanel.energyHighCut = high;
				outputPanel.energyCut = true;
				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				}
				else energyCut.setSelected(false); 
			}
		}

		if (source == totalEnergyCut){
			if (DEBUG) System.out.println("threads when clicked: " + Thread.currentThread().activeCount());
			if (!totalEnergyCut.isSelected())
			{
				outputPanel.totalEnergyLowCut = outputPanel.totalEnergyLow;
				outputPanel.totalEnergyHighCut = outputPanel.totalEnergyHigh;
				outputPanel.totalEnergyCut = false;
 				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();

			}
			else if (totalEnergyCut.isSelected())
			{
				double low, high;
				try{
				low = Double.parseDouble((String)JOptionPane.showInputDialog(this,"minimum ("+ outputPanel.totalEnergyLow +"): ","total energy cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.totalEnergyLowCut));
				high = Double.parseDouble((String)JOptionPane.showInputDialog(this,"maximum ("+ outputPanel.totalEnergyHigh +"): ","total energy cut",JOptionPane.PLAIN_MESSAGE,null,null,""+outputPanel.totalEnergyHighCut));
				}catch(NumberFormatException e){
					low = high = Double.MIN_VALUE;
					totalEnergyCut.setSelected(false);
				}
				if ((low>=outputPanel.totalEnergyLow) && (high<=outputPanel.totalEnergyHigh) && (high>low)){
				outputPanel.totalEnergyLowCut = low;
				outputPanel.totalEnergyHighCut = high;
				outputPanel.totalEnergyCut = true;
				buttonPanel.removeAll();
				writeCutText();
 				layoutButtonPanel();
				}
				else totalEnergyCut.setSelected(false);
			}
		}

		if(source == energyResetButton){
			if (DEBUG) System.out.println("energyResetButton, threads when clicked: " + Thread.currentThread().activeCount());
			outputPanel.wait =true;
//			globals.stop = true;
				try{
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("energyResetButton: no sleep");
				}
			outputPanel.initHistos();
//			globals.stop = false;
			outputPanel.repaint();
			outputPanel.wait = false;
		}
		if (source == freezeButton){
			globals.freezeTrack = freezeButton.isSelected();
		}
		if (source == loopFile){
				globals.loopFile = loopFile.isSelected();
		}


		if (source == loadFile){
				if (DEBUG) System.out.println("threads when clicked load: " + Thread.currentThread().activeCount());
			int returnVal = fileChooser.showOpenDialog(this);
			if (DEBUG) System.out.println("fileChooser return value: " + returnVal + "need for approved: " + JFileChooser.APPROVE_OPTION );

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				outputPanel.wait =false;
				allStop = true;
				globals.stop = true;
				if (retrieveData.getState() == SwingWorker.StateValue.valueOf("STARTED")) retrieveData.cancel(true);
				try{
					Thread.sleep(100);
				} catch (InterruptedException e) { }
				outputPanel.reInitAll();
				globals.inFile = fileChooser.getSelectedFile();
				saveFile.setEnabled(true);
				rerunFile.setEnabled(true);
				mediumSpeed.setSelected(true);
				globals.speed = Integer.parseInt(speedGroup.getSelection().getActionCommand());
				speedMenu.setEnabled(true);
				globals.online = false;
				globals.stop = false;
				allStop = false;
				retrieveData = new RetrieveData();
				retrieveData.execute();
			}
		}

		if (source == rerunFile){
			outputPanel.wait =false;
			globals.stop = true;
			if (DEBUG) System.out.println("threads just after stop: " + Thread.currentThread().activeCount());
				if (retrieveData.getState() == SwingWorker.StateValue.valueOf("STARTED")) retrieveData.cancel(true);
			outputPanel.reInitAll();
			globals.gotContact = false;
			globals.stop = false;
				retrieveData = new RetrieveData();
				retrieveData.execute();
			
		}
		
		if (source == openSocket){
				if (DEBUG) System.out.println("openSocket");
				allStop = true;
				outputPanel.wait =false;
				globals.stop = true;
//				if ((globals.gotContact) && (globals.online)) {retrieveData.stopConnection();}
				globals.gotContact = false;
				if (DEBUG) System.out.println("before cancelling SwingWorker");
//				if (retrieveData.getState() == SwingWorker.StateValue.valueOf("STARTED")) retrieveData.cancel(true);
				try{
					Thread.sleep(100);
				} catch (InterruptedException e) { }
				if (DEBUG) System.out.println("SwingWorker cancelled");
				globals.hostName = (String)JOptionPane.showInputDialog(this,"hostname:","Open socket",JOptionPane.PLAIN_MESSAGE,null,null,globals.hostName);
				hostPort = Integer.parseInt((String)JOptionPane.showInputDialog(this,"port:","Open socket",JOptionPane.PLAIN_MESSAGE,null,null,"4711"));
				if ((globals.hostName != null) && (globals.hostName.length() > 0)) {

				outputPanel.reInitAll();
				maxSpeed.setSelected(true);
//				globals.speed = Integer.parseInt(speedGroup.getSelection().getActionCommand());
				globals.speed = 0;
				saveFile.setEnabled(true);
//				speedMenu.setEnabled(false);
				globals.stop = false;
				allStop = false;
				globals.online = true;
				retrieveData = new RetrieveData();
				retrieveData.execute();
				}
			}
		
		if (source == localConnect){
				if (DEBUG) System.out.println("localConnect");
				allStop = true;
				outputPanel.wait =false;
				globals.stop = true;
//				if ((globals.gotContact) && (globals.online)) {retrieveData.stopConnection();}
				globals.gotContact = false;
				if (DEBUG) System.out.println("before cancelling SwingWorker");
//				if (retrieveData.getState() == SwingWorker.StateValue.valueOf("STARTED")) retrieveData.cancel(true);
				try{
					Thread.sleep(100);
				} catch (InterruptedException e) { }
				if (DEBUG) System.out.println("SwingWorker calcelled");
				globals.hostName = "localhost";
				hostPort = 4711;
				outputPanel.reInitAll();
				maxSpeed.setSelected(true);
//				globals.speed = Integer.parseInt(speedGroup.getSelection().getActionCommand());
				globals.speed = 0;
				saveFile.setEnabled(true);
//				speedMenu.setEnabled(false);
				globals.stop = false;
				allStop = false;
				globals.online = true;
				retrieveData = new RetrieveData();
				retrieveData.execute();
			}
		

		if (source == saveFile){
			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try{
					boolean createFile = new File(fileChooser.getSelectedFile().getCanonicalPath()).mkdir();
					if (createFile) outputPanel.writeHistos(fileChooser.getSelectedFile().getCanonicalPath());
				} catch (IOException e) {
					System.out.println("could not create output directory");
				}
			}
		}
		if (source == maxSpeed || source == fastSpeed ||source == mediumSpeed ||source == slowSpeed ||source == crawlSpeed) {
			globals.speed = Integer.parseInt(event.getActionCommand());
			globals.stepEvents = false;
			outputPanel.wait = false;
			nextButton.setEnabled(false);
		}
		if (source == eventSpeed){
			globals.speed = Integer.parseInt(event.getActionCommand());
			globals.stepEvents = true;
			nextButton.setEnabled(true);
		}
			
	}

	
	public void valueChanged(ListSelectionEvent event) {
		Object source = event.getSource();
		if (source == pageChoice){
			globals.page = pageChoice.getSelectedIndex();
			if (!DEBUG) {
				buttonPanel.removeAll();
				layoutButtonPanel();
				buttonPanel.revalidate();
				outputPanel.repaint();
			}
 		}
		if (source == displayChoice){
			globals.display = displayChoice.getSelectedIndex();
			outputPanel.repaint();
		}		
		if (source == coincChoice){
			globals.coinc = coincChoice.getSelectedIndex();
			outputPanel.repaint();
		}		
	}

	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		for (int i = 0; i<8; i++){
			if (source == sliceLimit[i]) {
				globals.sliceLimit[i] =  (Double)(sliceLimit[i].getValue());
				outputPanel.changeSliceStep(i);
				return;
			}
        }
    }

	public static void main(String[] args)  { //main
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}
		try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {

		new TacticViewer().setVisible(true);
		     }
        });

	}catch (Exception exc) {
      System.out.println("Can't create because of " + exc);
		}
	}
		
}








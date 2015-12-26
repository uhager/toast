// part of Toast
// author: Ulrike Hager

import java.util.ArrayList;

public class Particle{
	private static final boolean DEBUG = false;
	private String name;
	private double origin;
	private ArrayList<double[]> coordinates;
	
	public Particle(String name, double origin){
		this.name=name;
		this.origin=origin;
		coordinates=new ArrayList<double[]>();
//		System.out.println("Particle: Name " +name + " origin " + origin );
	}
	public String getName(){
		return name;
	}
	public double getOrigin(){
		return origin;
	}

	public void addCoordinate(double[] point){
		if (point[2]<5) point[2]=5;
		coordinates.add(point); 
		if (DEBUG) System.out.println("Particle: addCoordinate " + coordinates.get(coordinates.size()-1)[0]);
	}

	public boolean matchCoordinates(double xPos, double yPos){
		boolean matches = false;
		if (DEBUG) System.out.println("Particle: matchCoordinate, coordinates: " + coordinates.size());
		for (int i=0; i<coordinates.size(); i++){
			if ((xPos>coordinates.get(i)[0]) && (xPos<coordinates.get(i)[0]+coordinates.get(i)[2])){
				if ((yPos>coordinates.get(i)[1]) && (yPos<coordinates.get(i)[1]+coordinates.get(i)[2])){
					if (DEBUG) System.out.println("Particle: matchCoordinate " + coordinates.get(i)[0]);
					matches = true;
					if (DEBUG) System.out.println("Particle: matchCoordinate matches");
					break;
				}
			}
		}
		return matches;
	}

	public String popupInfo(){
		String infoText = name + ", z0 = " + origin;
		return infoText;
	}
}

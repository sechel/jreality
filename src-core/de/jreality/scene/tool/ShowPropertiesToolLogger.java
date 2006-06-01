package de.jreality.scene.tool;

import de.jreality.math.Pn;

/**
 * @author bleicher
 *
 */

public class ShowPropertiesToolLogger{
	private String log;
	private boolean homogeneous=true;
	
	
	public ShowPropertiesToolLogger(){
	}
	
	public ShowPropertiesToolLogger(String str){
		this();
		setLine(str);
	}
	
	public void addLine(String newLine){
		if(log==null) log=new String(newLine);
		else log=log+"\n"+newLine;
	}
	
	public void addLine(String description, double[] point){
		addLine(description+": ");
		addPoint(point);
	}
	
	public void addLine(String description,int[] ints){
		addLine(description+": ");
		addPoint(ints);
	}
	
	public void addLine(double[] point){
		addLine("point",point);
	}
	
	public void addPoint(double[] point){
		if(point==null){
			log=log+"null";
			return;
		}
		if(point.length<1) return;
		
		String newStr="";		
		int length=point.length;
		double[] writePoint=point;
		if(length==4){
			if(!homogeneous){		
				length-=1;
				writePoint=new double[length];			
				Pn.dehomogenize(writePoint,point);
				if(point[3]==0) newStr=newStr+" (inf) ";
			}
		}
		
		newStr=newStr+point[0];
		for(int i=1;i<length;i++){
			newStr=newStr+", "+writePoint[i];
		}
		log=log+newStr;
	}
	
	public void addPoint(int[] ints){
		if(ints==null){
			log=log+"null";
			return;
		}
		String newStr=""+ints[0];		
		for(int i=1;i<ints.length;i++){
			newStr=newStr+", "+ints[i];
		}
		log=log+newStr;
	}	
	
	public void clear(){
		log=null;
	}
	
	public void setLine(String line){
		clear();
		addLine(line);
	}
	
	public String getLog(){
		if(log==null) log="";
		return log;
	}
	
	public void setHomogeneousLogging(boolean hom){
		homogeneous=hom;
	}
}

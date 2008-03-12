package de.jreality.writer;

import java.util.HashMap;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

public class VRMLWriterHelper {

	private HashMap<Integer, GeoParts> geometryParts;
	
	private class GeoParts{
		int geoCount=0;
		int faceCount=0;
		int lineCount=0;
		int pointCount=0;
		boolean facesDefined=false; 
		boolean linesDefined=false; 
		boolean pointsDefined=false;
	} 
	
	/** counts how often a Geometry is used.
	 *  divide into Faces, Edges, Points 
	 * @author gonska
	 */
	private class MyVisitor extends SceneGraphVisitor{
		private EffectiveAppearance effApp= EffectiveAppearance.create();
		private DefaultGeometryShader dgs;
		private boolean faces=false;
		private boolean lines=false;
		private boolean points=false;
		
		public MyVisitor() {}
		public MyVisitor(MyVisitor mv) {
			effApp=mv.effApp;
		}		
		public void visit(SceneGraphComponent c) {
			c.childrenAccept(new MyVisitor(this));			
			super.visit(c);
		}
		public void visit(Appearance a) {
			effApp=effApp.create(a);
			dgs = ShaderUtility.createDefaultGeometryShader(effApp);
			super.visit(a);
		}
		public void visit(IndexedFaceSet i) {
			if (dgs.getShowFaces())faces=true;
			super.visit(i);
		}
		public void visit(IndexedLineSet g) {
			if (dgs.getShowLines())lines=true;
			super.visit(g);
		}
		public void visit(PointSet p) {
			if (dgs.getShowPoints())points=true;
			 // remember all kinds of PointSet:
			if(geometryParts.containsKey(p.hashCode())){
				GeoParts gp=geometryParts.get(p.hashCode());
				if (faces) gp.faceCount++;
				if (lines) gp.lineCount++;
				if (points) gp.pointCount++;
			}
			else {
				GeoParts gp= new GeoParts();
				if (faces) gp.faceCount++;
				if (lines) gp.lineCount++;
				if (points) gp.pointCount++;
				geometryParts.put(p.hashCode(), gp);
			}
			super.visit(p);
		}
	};
		
	public void inspect(SceneGraphNode c){
		geometryParts= new HashMap<Integer, GeoParts>();
		c.accept(new MyVisitor());	
	}
	public boolean isMultipleUsedFaceSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return (gp.faceCount>1);
		}
		return false;			
	} 	
	public boolean isMultipleUsedLineSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return (gp.lineCount>1);
		}
		return false;			
	} 	
	public boolean isMultipleUsedPointSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return (gp.pointCount>1);
		}
		return false;			
	} 	
	public boolean isDefinedFaceSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return gp.facesDefined;
		}
		return false;			
	} 	
	public boolean isDefinedLineSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return gp.linesDefined;
		}
		return false;			
	} 	
	public boolean isDefinedPointSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			return gp.pointsDefined;
		}
		return false;			
	} 	
	public void setDefinedFaceSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			gp.facesDefined=true;
		}
	}
	public void setDefinedLineSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			gp.linesDefined=true;
		}
	}
	public void setDefinedPointSet(Geometry g){
		if (geometryParts.containsKey(g.hashCode())){
			GeoParts gp= geometryParts.get(g.hashCode());
			gp.pointsDefined=true;
		}
	}
	
}

package de.jreality.jogl3.optimization;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlReflectionMap;
import de.jreality.jogl3.glsl.GLShader;

public class RenderableUnitCollection{
	
	public void resetRestNonTranspObjects(){
		restNonTranspObjects = new LinkedList<RenderableObject>();
	}
	
	public List<RenderableObject> restNonTranspObjects = new LinkedList<RenderableObject>();
	
	public final int MAX_NUM_VERTICES = 1000;
	
	WeakHashMap<GLShader, WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap, RenderableUnit>>> units = new WeakHashMap<GLShader,WeakHashMap<GlTexture,WeakHashMap<GlReflectionMap,RenderableUnit>>>();
	
	public void add(RenderableObject o){
		if(o.geom instanceof JOGLFaceSetInstance){
			JOGLFaceSetInstance f = (JOGLFaceSetInstance)o.geom;
			JOGLFaceSetEntity fse = (JOGLFaceSetEntity) f.getEntity();
			
			if(fse.getAllVBOs()[0].getLength() <= MAX_NUM_VERTICES){
				
				GlTexture tex = f.faceTexture;
				GLShader shader = f.getPolygonShader();
				GlReflectionMap reflMap = f.reflMap;
				
				WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap, RenderableUnit>> hm1 = units.get(shader);
				if(hm1 == null){
					hm1 = new WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap,RenderableUnit>>();
					units.put(shader, hm1);
				}
				//hm1 now usable
				WeakHashMap<GlReflectionMap, RenderableUnit> hm2 = hm1.get(tex);
				if(hm2 == null){
					hm2 = new WeakHashMap<GlReflectionMap,RenderableUnit>();
					hm1.put(tex, hm2);
				}
				//hm2 now usable
				RenderableUnit ru = hm2.get(reflMap);
				if(ru == null){
					ru = new RenderableUnit(tex, shader, reflMap);
					hm2.put(reflMap, ru);
				}
				ru.register(o);
			}else{
				restNonTranspObjects.add(o);
			}
		}else{
			restNonTranspObjects.add(o);
		}
	}
	
	public void render(GL3 gl){
		for(GLShader t : units.keySet()){
			for(GlTexture tex : units.get(t).keySet()){
				for(GlReflectionMap r : units.get(t).get(tex).keySet()){
					units.get(t).get(tex).get(r).render(gl);
				}
			}
		}
	}
	
}

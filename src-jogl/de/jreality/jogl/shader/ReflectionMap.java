/*
 * Created on Mar 2, 2005
 *
 */
package de.jreality.jogl.shader;

import java.io.IOException;

import net.java.games.jogl.GL;
import de.jreality.scene.Texture2D;

/**
 * @author Charles Gunn
 *
 */
public class ReflectionMap {
	Texture2D[] faceTextures;
	Texture2D globalSettings;			// apply to all 6 faces of the cube
	private GL gl;
	
	private ReflectionMap(Texture2D[] ft, Texture2D gs) {
		super();
		faceTextures = ft;
		globalSettings = gs;
	}
	
	public Texture2D[] getFaceTextures() {
		return faceTextures;
	}
	public Texture2D getGlobalSettings() {
		return globalSettings;
	}
	
	public static ReflectionMap reflectionMapFactory(String stem, String[] suffixes, String fileExtension)	{
		//String[] texNameSuffixes = {"bk","ft","dn","up","lf","rt"};
		Texture2D[] faceTex = new Texture2D[6];
		Texture2D globalTex = new Texture2D();
		globalTex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
		globalTex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
		globalTex.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		globalTex.setApplyMode(Texture2D.GL_REPLACE);
		for (int i = 0; i<6; ++i)	{
			try {
				//BufferedImage image = Texture2D.loadImage(resourceDir+ "desertstorm/desertstorm_"+texNameSuffixes[i]+".JPG");
				//faceTex[i] = new Texture2D(image);
				faceTex[i] = new Texture2D(stem+suffixes[i]+"."+fileExtension);
				faceTex[i].setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				faceTex[i].setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
				faceTex[i].setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				faceTex[i].setApplyMode(Texture2D.GL_REPLACE);
				//faceTex[i].setMagFilter(GL.GL_NEAREST);
				//faceTex[i].setMinFilter(GL.GL_NEAREST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new ReflectionMap(faceTex, globalTex);		
	}
}

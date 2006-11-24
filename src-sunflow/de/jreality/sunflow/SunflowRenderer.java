/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.sunflow;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.camera.PinholeLens;
import org.sunflow.core.primitive.Mesh;
import org.sunflow.core.primitive.SkyBox;
import org.sunflow.core.shader.UberShader;
import org.sunflow.image.Bitmap;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

import de.jreality.math.Matrix;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;


public class SunflowRenderer extends SunflowAPI {

	IdentityHashMap<Object, String> geom2name = new IdentityHashMap<Object, String>();
	HashMap<String, Object> name2geom = new HashMap<String, Object>();
	ArrayList<File> tmpFiles = new ArrayList<File>();
	
	private class Visitor extends SceneGraphVisitor {
		
		SceneGraphPath path=new SceneGraphPath();
		EffectiveAppearance eapp;
		DefaultGeometryShader dgs;
		DefaultPolygonShader dps;
		
		int appCount=0;
		
		@Override
		public void visit(SceneGraphComponent c) {
			path.push(c);
			Geometry g = c.getGeometry();
			if (c.getAppearance() != null) c.getAppearance().accept(this);
			if (g != null) {
			  g.accept(this);
			  parameter("transform", new Matrix(path.getMatrix(null)));
			  parameter("shaders", "default-shader"+appCount);
			  String geomName = getName(g);
			  instance(geomName+".instance", geomName);
			}
			  for (int i=0; i < c.getChildComponentCount(); i++) {
				  c.getChildComponent(i).accept(this);
			  }
			path.pop();
		}
		
		@Override
		public void visit(Sphere s) {
			geometry(getName(s), new org.sunflow.core.primitive.Sphere());
		}
		
		@Override
		public void visit(Cylinder c) {
			geometry(getName(c), new org.sunflow.core.primitive.Cylinder());
		}

		@Override
		public void visit(Appearance a) {
			appCount++;
			System.out.println("Visitor.visit(Appearance)");
			eapp = EffectiveAppearance.create(path);
			dgs = ShaderUtility.createDefaultGeometryShader(eapp);
			dps = (DefaultPolygonShader) dgs.getPolygonShader();
			java.awt.Color c = dps.getDiffuseColor();
			double diffuseCoefficient = dps.getDiffuseCoefficient();
			Color diffuseColor = new Color(
					(float)(c.getRed()*diffuseCoefficient/255),
					(float)(c.getGreen()*diffuseCoefficient/255),
					(float)(c.getBlue()*diffuseCoefficient/255)
			);
			parameter("diffuse", diffuseColor);
			c = dps.getSpecularColor();
			double specularCoefficient = dps.getSpecularCoefficient();
			Color specularColor = new Color(
					(float)(c.getRed()*specularCoefficient/255),
					(float)(c.getGreen()*specularCoefficient/255),
					(float)(c.getBlue()*specularCoefficient/255)
			);
			parameter("reflection", specularColor);
			if  (dps.getTexture2d() != null) {
				parameter("texture", getName(dps.getTexture2d().getImage()));
			}
			parameter("power", dps.getSpecularExponent());
			//parameter("samples", 4);
			shader("default-shader"+appCount, new UberShader());//dps.getTexture2d() != null ? new TexturedWardShader() : new AnisotropicWardShader());
		}
		
		@Override
		public void visit(IndexedFaceSet ifs) {
			float[] points = convert(ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(), 3);
			float[] normals = null;
			if (ifs.getVertexAttributes(Attribute.NORMALS) != null) {
				normals = convert(ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(), 3);
				parameter("normals", "vector", "vertex", normals);
			} else if (ifs.getFaceAttributes(Attribute.NORMALS) != null) {
				normals = convert(ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(), 3);
				//parameter("normals", "vector", "vertex", normals);
			}
			DataList tex = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
			float[] texCoords = tex != null ? convert(tex.toDoubleArrayArray(), 2) : null;
			int[] faces = convert(ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray());
			parameter("triangles", faces);
			parameter("points", "point", "vertex", points);
			if (texCoords != null) {
				parameter("uvs", "texcoord", "vertex", texCoords);				
			}
			geometry(getName(ifs), new Mesh());
		}
	}
	
	public SunflowRenderer() {
		String dataDir = System.getProperty("jreality.data","/net/MathVis/data/testData3D");
		addTextureSearchPath(dataDir+"/textures");
	}
	
	public int[] convert(IntArrayArray faces) {
		int triCnt=0;
		for (int i=0; i<faces.getLength(); i++) {
			triCnt+=(faces.getLengthAt(i)-2);
		}
		int[] tris = new int[triCnt*3];
		int ind=0;
		for (int i=0; i<faces.getLength(); i++) {
			IntArray face = faces.getValueAt(i);
			for (int k=0; k<face.getLength()-2; k++) {
				tris[ind++]=face.getValueAt(0);
				tris[ind++]=face.getValueAt(k+1);
				tris[ind++]=face.getValueAt(k+2);
			}
		}
		return tris;
	}

	public float[] convert(DoubleArrayArray array, int slotLen) {
		float[] ret = new float[array.getLength()*slotLen];
		int ind=0;
		for (int i=0; i<array.getLength(); i++) {
			for (int j=0; j<slotLen; j++) {
				ret[ind++]=(float) array.getValueAt(i, j);
			}
		}
		return ret;
	}

	public void render(SceneGraphComponent sceneRoot, SceneGraphPath cameraPath, Display display, int width, int height) {
		
		// light
//		parameter("texture", "sky_small.hdr");
//		parameter("center", new Vector3(1, 0, -1));
//		parameter("up", new Vector3(0, 1, 0));
//		parameter("samples", 200);
//		ImageBasedLight light = new ImageBasedLight();
//		light.init("skylight", this);
		
//		parameter("dir",new Vector3(0,1,1));
//		DirectionalLight sun = new DirectionalLight();
//		light("sun", sun);

		Appearance rootApp = sceneRoot.getAppearance();
		if(rootApp != null) {
			if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,
					CommonAttributes.SKY_BOX, rootApp)) {
				CubeMap cm = (CubeMap) AttributeEntityUtility
				.createAttributeEntity(CubeMap.class,
						CommonAttributes.SKY_BOX, rootApp, true);
				SkyBox skyBox = new SkyBox(cm);
				parameter("center", new Vector3(1, 0, 0));
				parameter("up", new Vector3(0, -1, 0));
				skyBox.init("skyBox", this);
			}
		}

		// add texture path
        try {
			File tmpF = File.createTempFile("foo", ".png");
			addTextureSearchPath(tmpF.getParentFile().getAbsolutePath());
			if (!tmpF.delete()) tmpF.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
        // visit
        new Visitor().visit(sceneRoot);
        
        // camera
		float aspect = width/(float)height;
		parameter("aspect",aspect);
		Camera c = (Camera) cameraPath.getLastElement();
		Matrix m = new Matrix(cameraPath.getMatrix(null));
		parameter("transform",m);
		double fov = c.getFieldOfView();
		System.out.println("cam fov="+fov);
		if (width>height) {
			fov = Math.atan(((double)width)/((double)height)*Math.tan(fov/360*Math.PI))/Math.PI*360;
			System.out.println("adjusted fov="+fov);
		}
		parameter("fov", fov);
		String name = getUniqueName("camera");
		camera(name, new PinholeLens());
		parameter("camera", name);
		
		// sunflow rendering
		parameter("sampler", "bucket");
		parameter("resolutionX", width);
        parameter("resolutionY", height);
        options(SunflowAPI.DEFAULT_OPTIONS);
        render(SunflowAPI.DEFAULT_OPTIONS, display);
	}
	
	public String getName(Geometry geom) {
		String prefix=geom.getName();
		return getName(prefix, geom);
	}

	int imgCnt;
	public String getName(ImageData img) {
		if (geom2name.containsKey(img)) return geom2name.get(img);
		File tmp;
		try {
			tmp = File.createTempFile("texture", ".png");
			//ImageIO.write((RenderedImage) img.getImage(), "PNG", tmp);
			Bitmap.save((BufferedImage) img.getImage(), tmp.getAbsolutePath());
			tmpFiles.add(tmp);
		} catch (IOException e) {
			throw new Error();
		}
		return getName(tmp.getName(), img);
	}

	private String getName(String prefix, Object geom) {
		String ret;
		if (geom2name.containsKey(geom)) ret = geom2name.get(geom);
		else {
			if (!name2geom.containsKey(prefix)) {
				geom2name.put(geom, prefix);
				ret = prefix;
			} else {
		        int counter = 1;
		        String name;
		        do {
		            name = String.format("%s_%d", prefix, counter);
		            counter++;
		        } while (name2geom.containsKey(name));
		        name2geom.put(name, geom);
		        geom2name.put(geom, name);
		        ret = name;
			}
		}
		System.out.println("name="+ret);
		return ret;

	}

	public void parameter(String string, java.awt.Color c) {
		System.out.println(string+"="+c);
		parameter(string, new Color(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f));
	}

	public void parameter(String name, Matrix m) {
		parameter(name, new Matrix4(
				(float) m.getEntry(0, 0),
				(float) m.getEntry(0, 1),
				(float) m.getEntry(0, 2),
				(float) m.getEntry(0, 3),
				(float) m.getEntry(1, 0),
				(float) m.getEntry(1, 1),
				(float) m.getEntry(1, 2),
				(float) m.getEntry(1, 3),
				(float) m.getEntry(2, 0),
				(float) m.getEntry(2, 1),
				(float) m.getEntry(2, 2),
				(float) m.getEntry(2, 3),
				(float) m.getEntry(3, 0),
				(float) m.getEntry(3, 1),
				(float) m.getEntry(3, 2),
				(float) m.getEntry(3, 3)				
		));
	}

	public void parameterPoint(String name, double[] column) {
		System.out.println(name+"="+Arrays.toString(column));
		parameter(name, new Point3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameterVector(String name, double[] column) {
		System.out.println(name+"="+Arrays.toString(column));
		parameter(name, new Vector3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameter(String name, double val) {
		parameter(name, (float) val);
	}
}

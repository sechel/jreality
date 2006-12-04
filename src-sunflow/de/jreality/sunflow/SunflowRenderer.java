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
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;

import javax.imageio.ImageIO;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.camera.PinholeLens;
import org.sunflow.core.gi.AmbientOcclusionGIEngine;
import org.sunflow.core.light.DirectionalLight;
import org.sunflow.core.light.GlPointLight;
import org.sunflow.core.primitive.Mesh;
import org.sunflow.core.primitive.SkyBox;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;


public class SunflowRenderer extends SunflowAPI {

	IdentityHashMap<Object, String> geom2name = new IdentityHashMap<Object, String>();
	HashMap<String, Object> name2geom = new HashMap<String, Object>();
	ArrayList<File> tmpFiles = new ArrayList<File>();

	private String POINT_SPHERE="point";
	private String LINE_CYLINDER="line";
	
	private RenderOptions options = new RenderOptions();

	private class Visitor extends SceneGraphVisitor {
		
		SceneGraphPath path=new SceneGraphPath();
		EffectiveAppearance eapp;
		DefaultGeometryShader dgs;
		DefaultPolygonShader dps;
		
		int appCount=0;
		private Matrix currentMatrix;
		
		int instanceCnt=0;
		
		@Override
		public void visit(SceneGraphComponent c) {
			if (!c.isVisible()) return;
			path.push(c);
			currentMatrix=new Matrix(path.getMatrix(null));
			Geometry g = c.getGeometry();
			eapp = EffectiveAppearance.create(path);
			dgs = ShaderUtility.createDefaultGeometryShader(eapp);
			rhs = ShaderUtility.createRenderingHintsShader(eapp);
			if (c.getLight() != null) c.getLight().accept(this);
			if (g != null) {
				if (g instanceof PointSet) {
					renderPoints((PointSet)g);
					if (g instanceof IndexedLineSet && dgs.getShowLines()  && ((IndexedLineSet) g).getNumEdges() > 0) {
						renderLines((IndexedLineSet) g);
					}
					if (g instanceof IndexedFaceSet && ((IndexedFaceSet) g).getNumFaces() > 0 && dgs.getShowFaces()) {
						dps = (DefaultPolygonShader) dgs.getPolygonShader();
						applyShader(dps);
						renderFaces((IndexedFaceSet) g);
					}
				} else {
					dps = (DefaultPolygonShader) dgs.getPolygonShader();
					applyShader(dps);
					g.accept(this);
					parameter("transform", currentMatrix);
					parameter("shaders", "default-shader" + appCount);
					String geomName = getName(g);
					instance(geomName + ".instance"+instanceCnt++, geomName);
				}
			}
			  for (int i=0; i < c.getChildComponentCount(); i++) {
				  c.getChildComponent(i).accept(this);
			  }
			path.pop();
		}

		public void renderFaces(IndexedFaceSet ifs) {
			float[] points = convert(ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(), 3, null);
			float[] normals = null;
			if (dps.getSmoothShading() && ifs.getVertexAttributes(Attribute.NORMALS) != null) {
				normals = convert(ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(), 3, null);
				parameter("normals", "vector", "vertex", normals);
			} else {
				// sunflow calculates the face normal from the triangle points...
			}
			DataList tex = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
			Texture2D tex2d = dps.getTexture2d();
			float[] texCoords = null;
			if (tex != null && tex2d != null) {
				Matrix texMat = null;
				// this is needed for sunflow build-in shaders:
				//MatrixBuilder.euclidean().scale(1,-1,1).getMatrix();
				//texMat.multiplyOnRight(tex2d.getTextureMatrix());
				texCoords = convert(tex.toDoubleArrayArray(), 2, texMat);
			}
			int[] faces = convert(ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray());
			parameter("triangles", faces);
			parameter("points", "point", "vertex", points);
			if (texCoords != null) {
				parameter("uvs", "texcoord", "vertex", texCoords);				
			}
			geometry(getName(ifs), new Mesh());
			parameter("transform", currentMatrix);
			parameter("shaders", "default-shader" + appCount);
			String geomName = getName(ifs);
			instance(geomName + ".instance"+instanceCnt++, geomName);
		}
		
		private void renderLines(IndexedLineSet indexedLineSet) {
			DefaultLineShader ls = (DefaultLineShader) dgs.getLineShader();
			if (ls.getTubeDraw()) {
				dps = (DefaultPolygonShader) ls.getPolygonShader();
				double r = ls.getTubeRadius();
				DoubleArrayArray pts = indexedLineSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
				IntArrayArray lines = indexedLineSet.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
				DataList radiiAttributes = indexedLineSet.getEdgeAttributes(Attribute.RADII);
				DoubleArray radii = radiiAttributes != null ? radiiAttributes.toDoubleArray() : null;
				double[] zAxis = pts.getLengthAt(0) == 3 ? new double[]{0,0,-1} : new double[]{0,0,-1,1};
				DataList colorAttributes = indexedLineSet.getEdgeAttributes(Attribute.COLORS);
				boolean lineColors = colorAttributes != null;
				DoubleArrayArray colors = lineColors ? colorAttributes.toDoubleArrayArray() : null;
				if (!lineColors) {
					applyShader(dps);
				}
				for (int i=0; i<lines.getLength(); i++) {
					double radius = radii != null ? radii.getValueAt(i) : r;
					if (lineColors) {
						Appearance app = new Appearance("fake app");
						EffectiveAppearance ea = eapp.create(app);
						dgs = ShaderUtility.createDefaultGeometryShader(ea);
						dps = (DefaultPolygonShader) ((DefaultLineShader) dgs.getLineShader()).getPolygonShader();
						double[] vc = colors.getValueAt(i).toDoubleArray(null);
						java.awt.Color vcc = new java.awt.Color((float) vc[0], (float) vc[1], (float) vc[2]);
						app.setAttribute("lineShader.polygonShader.diffuseColor", vcc);
						if (vc.length == 4) {
							app.setAttribute("lineShader.polygonShader.transparency", dps.getTransparency()*vc[3]);
						}
						applyShader(dps);
					}
					for (int j=0; j<lines.getLengthAt(i)-1; j++) {
						double[] p1 = pts.getValueAt(lines.getValueAt(i, j)).toDoubleArray(null);
						double[] p2 = pts.getValueAt(lines.getValueAt(i, j+1)).toDoubleArray(null);
						double[] seg = Rn.subtract(null, p2, p1);
						double[] center = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
						double len=Rn.euclideanNorm(seg);
						Matrix m = Matrix.times(currentMatrix, MatrixBuilder.euclidean()
								.translate(
										center[0],
										center[1],
										center[2]
								).rotateFromTo(zAxis, seg)
								.scale(radius, radius, len/2).getMatrix());
						parameter("transform", m);
						parameter("shaders", "default-shader" + appCount);
						instance(LINE_CYLINDER + ".instance"+instanceCnt++, LINE_CYLINDER);
					}
				}
			}
		}

		private void renderPoints(PointSet pointSet) {
			DefaultPointShader ps = (DefaultPointShader) dgs.getPointShader();
			if (dgs.getShowPoints() && ps.getSpheresDraw() && pointSet.getNumPoints() > 0) {
				dps = (DefaultPolygonShader) ps.getPolygonShader();
				double r = ps.getPointRadius();
				DoubleArrayArray pts = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
				DataList radiiAttributes = pointSet.getVertexAttributes(Attribute.RADII);
				DoubleArray radii = radiiAttributes != null ? radiiAttributes.toDoubleArray() : null;
				DataList colorAttributes = pointSet.getVertexAttributes(Attribute.COLORS);
				boolean vertexColors = colorAttributes != null;
				DoubleArrayArray colors = vertexColors ? colorAttributes.toDoubleArrayArray() : null;
				if (!vertexColors) {
					applyShader(dps);
				}
				for (int i=0; i<pts.getLength(); i++) {
					if (vertexColors) {
						Appearance app = new Appearance("fake app");
						EffectiveAppearance ea = eapp.create(app);
						dgs = ShaderUtility.createDefaultGeometryShader(ea);
						dps = (DefaultPolygonShader) ((DefaultPointShader) dgs.getPointShader()).getPolygonShader();
						double[] vc = colors.getValueAt(i).toDoubleArray(null);
						java.awt.Color vcc = new java.awt.Color((float) vc[0], (float) vc[1], (float) vc[2]);
						app.setAttribute("pointShader.polygonShader.diffuseColor", vcc);
						if (vc.length == 4) {
							app.setAttribute("pointShader.polygonShader.transparency", dps.getTransparency()*vc[3]);
						}
						applyShader(dps);
					}
					double w = pts.getLengthAt(i) == 3 ? 1 : pts.getValueAt(i, 3);
					if (w != 0) {
						Matrix m = Matrix.times(currentMatrix, MatrixBuilder.euclidean().translate(
								pts.getValueAt(i, 0)/w,
								pts.getValueAt(i, 1)/w,
								pts.getValueAt(i, 2)/w
								).scale(radii != null ? radii.getValueAt(i) : r).getMatrix());
						parameter("transform", m);
						parameter("shaders", "default-shader" + appCount);
						instance(POINT_SPHERE + ".instance"+instanceCnt++, POINT_SPHERE);
					}
				}
			}
		}
		
		int lightID;
		private RenderingHintsShader rhs;
		@Override
		public void visit(de.jreality.scene.DirectionalLight l) {
			if (options.isUseOriginalLights() || !l.isAmbientFake()) {
				double[] dir = currentMatrix.multiplyVector(new double[]{0,0,1,0});
				parameterVector("dir", dir);
				DirectionalLight sun = new DirectionalLight();
				java.awt.Color c = l.getColor();
				float i = (float)l.getIntensity() *(float)Math.PI;
				Color col = new Color(c.getRed()/255f*i, c.getGreen()/255f*i, c.getBlue()/255f*i);
				parameter("power", col);
				light("directionalLight"+lightID++, sun);
			}
		}
		
		@Override
		public void visit(de.jreality.scene.PointLight l) {
			if (options.isUseOriginalLights() || !l.isAmbientFake()) {
				double[] point = currentMatrix.multiplyVector(new double[]{0,0,0,1});
				parameterPoint("center", point);
				GlPointLight light = new GlPointLight();
				java.awt.Color c = l.getColor();
				float i = (float)l.getIntensity() *(float)Math.PI;
				Color col = new Color(c.getRed()/255f*i, c.getGreen()/255f*i, c.getBlue()/255f*i);
				parameter("power", col);
				parameter("fallOffA0", l.getFalloffA0());
				parameter("fallOffA1", l.getFalloffA1());
				parameter("fallOffA2", l.getFalloffA2());
				light("pointLight"+lightID++, light);
			}
		}

		@Override
		public void visit(Sphere s) {
			geometry(getName(s), new org.sunflow.core.primitive.Sphere());
		}
		
		@Override
		public void visit(Cylinder c) {
			geometry(getName(c), new org.sunflow.core.primitive.Cylinder());
		}

		private void applyShader(DefaultPolygonShader ps) {
			appCount++;
			shader("default-shader"+appCount, new org.sunflow.core.shader.DefaultPolygonShader(ps, rhs));
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

	public float[] convert(DoubleArrayArray array, int slotLen, Matrix matrix) {
		float[] ret = new float[array.getLength()*slotLen];
		double[] tmp = new double[4];
		tmp[3]=1;
		int ind=0;
		for (int i=0; i<array.getLength(); i++) {
			for (int j=0; j<slotLen; j++) {
				tmp[j]=array.getValueAt(i, j);
			}
			if (matrix != null) tmp = matrix.multiplyVector(tmp);
			for (int j=0; j<slotLen; j++) {
				ret[ind++]=(float) tmp[j];
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

		// init default primitives
		geometry(POINT_SPHERE, new org.sunflow.core.primitive.Sphere());
		geometry(LINE_CYLINDER, new org.sunflow.core.primitive.Cylinder());

		
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
		parameter("sampler", options.isProgessiveRender() ? "ipr" : "bucket");
		parameter("resolutionX", width);
        parameter("resolutionY", height);
        parameter("aa.min", options.getAaMin());
        parameter("aa.max", options.getAaMax());
        parameter("depths.diffuse", options.getDepthsDiffuse());
        parameter("depths.reflection", options.getDepthsReflection());
        parameter("depths.refraction", options.getDepthsRefraction());
        float ambient = (float)options.getAmbientOcclusionBright();
        int ambientOcclusionSamples = options.getAmbientOcclusionSamples();
        if (!options.isUseOriginalLights() && ambient >0) giEngine(new AmbientOcclusionGIEngine(new Color(ambient, ambient, ambient), Color.BLACK, ambientOcclusionSamples, 100));
        //giEngine(new FakeGIEngine(new Vector3(0,1,0), Color.WHITE, Color.BLACK));
        //giEngine(new InstantGI(128, 1, .01f, 0));
        //giEngine(new PathTracingGIEngine(200));
        options(SunflowAPI.DEFAULT_OPTIONS);
        render(SunflowAPI.DEFAULT_OPTIONS, display);
        
        // delete tmp texture files
        for (File f : tmpFiles) {
        	if (!f.delete()) f.deleteOnExit();
        }
	}
	
	public String getName(Geometry geom) {
		String prefix=geom.getName();
		return getName(prefix, geom);
	}

	int imgCnt;
	public String getName(ImageData data) {
		if (geom2name.containsKey(data)) return geom2name.get(data);
		File tmp;
		try {
			BufferedImage img;
			tmp = File.createTempFile("texture", ".png");
			   byte[] byteArray = data.getByteArray();
			   int dataHeight = data.getHeight();
			   int dataWidth = data.getWidth();
			   img = new BufferedImage(dataWidth, dataHeight,
			   BufferedImage.TYPE_INT_ARGB);
			   WritableRaster raster = img.getRaster();
			   int[] pix = new int[4];
		         for (int y = 0, ptr = 0; y < dataHeight; y++) {
		           for (int x = 0; x < dataWidth; x++, ptr += 4) {             
//		             if (transparencyEnabled)
//		               pix[3]=byteArray[ptr + 3]; 
//		             else{
//		               if (byteArray[ptr + 3]==0) pix[3]=(byte) 0;                 
//		               else pix[3]=(byte) 255;                              
//		             }    
		             pix[0] = byteArray[ptr];
		             pix[1] = byteArray[ptr + 1];
		             pix[2] = byteArray[ptr + 2];
		             pix[3] = byteArray[ptr + 3]; 
		             raster.setPixel(x, y, pix);
		           }
		         }                      
			ImageIO.write((BufferedImage) img, "PNG", tmp);
			tmpFiles.add(tmp);
		} catch (IOException e) {
			throw new Error();
		}
		return getName(tmp.getName(), data);
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
		return ret;

	}

	public void parameter(String string, java.awt.Color c) {
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
		parameter(name, new Point3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameterVector(String name, double[] column) {
		parameter(name, new Vector3((float) column[0], (float) column[1], (float) column[2]));
	}

	public void parameter(String name, double val) {
		parameter(name, (float) val);
	}

	public RenderOptions getOptions() {
		return options;
	}

	public void setOptions(RenderOptions options) {
		this.options = options;
	}
}
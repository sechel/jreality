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

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Stack;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.camera.PinholeLens;
import org.sunflow.core.display.FrameDisplay;
import org.sunflow.core.light.MeshLight;
import org.sunflow.core.primitive.CornellBox;
import org.sunflow.core.primitive.Mesh;
import org.sunflow.core.shader.DiffuseShader;
import org.sunflow.core.shader.GlassShader;
import org.sunflow.core.shader.MirrorShader;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;
import org.sunflow.system.ImagePanel;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;


public class SunflowRenderer extends SunflowAPI {

	IdentityHashMap<Geometry, String> geom2name = new IdentityHashMap<Geometry, String>();
	HashMap<String, Geometry> name2geom = new HashMap<String, Geometry>();
	
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
			  parameter("transform", new Matrix(path.getMatrix(null)));
			  g.accept(this);
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
		public void visit(Appearance a) {
			appCount++;
			System.out.println("Visitor.visit(Appearance)");
			eapp = EffectiveAppearance.create(path);
			dgs = ShaderUtility.createDefaultGeometryShader(eapp);
			dps = (DefaultPolygonShader) dgs.getPolygonShader();
			parameter("diffuse", dps.getDiffuseColor());
			shader("default-shader"+appCount, new DiffuseShader());
		}
	}
	
	public void render(SceneGraphComponent sceneRoot, SceneGraphPath cameraPath, Display display) {
		Camera c = (Camera) cameraPath.getLastElement();
		Matrix m = new Matrix(cameraPath.getMatrix(null));
		parameterPoint("eye", m.getColumn(3));
		double[] target = Rn.subtract(null, m.getColumn(3), m.getColumn(2));
		parameterPoint("target", target);
		parameterVector("up", m.getColumn(1));
		parameter("fov", c.getFieldOfView());
		String name = getUniqueName("camera");
		camera(name, new PinholeLens());
		parameter("camera", name);
		options(SunflowAPI.DEFAULT_OPTIONS);
		CornellBox box = new CornellBox();
        box.init(getUniqueName("cornellbox"), this);
		new Visitor().visit(sceneRoot);
		parameter("sampler", "bucket");
        options(SunflowAPI.DEFAULT_OPTIONS);
        render(SunflowAPI.DEFAULT_OPTIONS, display);
	}
	
	public String getName(Geometry geom) {
		String ret;
		if (geom2name.containsKey(geom)) ret = geom2name.get(geom);
		else {
			if (!name2geom.containsKey(geom.getName())) {
				geom2name.put(geom, geom.getName());
				ret = geom.getName();
			} else {
		        int counter = 1;
		        String name, prefix=geom.getName();
		        do {
		            name = String.format("%s_%d", prefix, counter);
		            counter++;
		        } while (name2geom.containsKey(name));
		        name2geom.put(name, geom);
		        geom2name.put(geom, name);
		        ret = name;
			}
		}
		System.out.println("geomName="+ret);
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

	public void test() {
        // camera
        parameter("eye", new Point3(0, 0, -16));
        parameter("target", new Point3(0, 0, 0));
        parameter("up", new Vector3(0, 1, 0));
        parameter("fov", 45.0f);
        String name = getUniqueName("camera");
        camera(name, new PinholeLens());
        parameter("camera", name);
        options(SunflowAPI.DEFAULT_OPTIONS);
        // cornell box
        Color grey = new Color(0.70f, 0.70f, 0.70f);
        Color blue = new Color(0.25f, 0.25f, 0.80f);
        Color red = new Color(0.80f, 0.25f, 0.25f);
        Color emit = new Color(15, 15, 15);

        float minX = -200;
        float maxX = 200;
        float minY = -160;
        float maxY = minY + 400;
        float minZ = -250;
        float maxZ = 200;

        float[] verts = new float[] { minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, };
        int[] indices = new int[] { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4, 1, 2, 5, 5, 6, 2, 2, 3, 6, 6, 7, 3, 0, 3, 4, 4, 7, 3 };

//        parameter("diffuse", grey);
//        shader("grey_shader", new DiffuseShader());
        parameter("diffuse", red);
        shader("red_shader", new DiffuseShader());
//        parameter("diffuse", blue);
//        shader("blue_shader", new DiffuseShader());
//
//        // build walls
//        parameter("triangles", indices);
//        parameter("points", "point", "vertex", verts);
//        parameter("faceshaders", new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 2, 2 });
//        geometry("walls", new Mesh());
//
//        // instance walls
//        parameter("shaders", new String[] { "grey_shader", "red_shader", "blue_shader" });
//        instance("walls.instance", "walls");

//        // create mesh light
//        parameter("points", "point", "vertex", new float[] { -50, maxY - 1, -50, 50, maxY - 1, -50, 50, maxY - 1, 50, -50, maxY - 1, 50 });
//        parameter("triangles", new int[] { 0, 1, 2, 2, 3, 0 });
//        parameter("radiance", emit);
//        parameter("samples", 16);
//        MeshLight light = new MeshLight();
//        light.init("light", this);

        new CornellBox().init(getUniqueName("cornellBox"), this);
        
        geometry("foo", new org.sunflow.core.primitive.Sphere());
        parameter("shaders", "red_shader");
		instance("foo.instance", "foo");
		
        // spheres
        parameter("eta", 1.6f);
        shader("Glass", new GlassShader());
        sphere("glass_sphere", "Glass", -60, minY + 100, -100, 50);
        parameter("color", new Color(0.70f, 0.70f, 0.70f));
        shader("Mirror", new MirrorShader());
        sphere("mirror_sphere", "Mirror", 100, minY + 60, -50, 50);
	}

    private void sphere(String name, String shaderName, float x, float y, float z, float radius) {
        geometry(name, new org.sunflow.core.primitive.Sphere());
        parameter("transform", Matrix4.translation(x, y, z).multiply(Matrix4.scale(radius)));
        parameter("shaders", shaderName);
        instance(name + ".instance", name);
    }
    
    public static void main(String[] args) {
		SunflowRenderer sr = new SunflowRenderer();
		Display d = new FrameDisplay();
		sr.test();
		sr.render(SunflowAPI.DEFAULT_OPTIONS, d);
	}

}

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


package de.jreality.ui.viewerapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.ReaderJRS;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.tools.EncompassTool;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;


public class GFZTool  extends AbstractTool {
	
	private static InputSlot actSlot = InputSlot.getDevice("SystemTime");
	private static InputSlot pause = InputSlot.getDevice("RotationToggle");
	private static InputSlot minus = InputSlot.getDevice("pageDown");
	private static InputSlot plus = InputSlot.getDevice("pageUp");
	
	
	public GFZTool() {
		addCurrentSlot(actSlot, "Need notification to perform once.");
		addCurrentSlot(pause);
		addCurrentSlot(minus);
		addCurrentSlot(plus);
	}
	
	
	private double angle = 0.001;  //angle of rotation
	private double[] axis = new double[]{0, 0, 1};  //axis of rotation
	private double layerTimer = 1500.0;  //time in millis between layer change
	
	private AnimatorTask task = null;
	private SceneGraphComponent cmp = null;
	private int layerCount, topLayer, direction;
	
	
	public void perform(ToolContext tc) {
		
		if (task == null) {  //first performance
			cmp = tc.getRootToToolComponent().getLastComponent();
			layerCount = 24;
			topLayer = 1;  //skip first child of gfz
			direction = -1;  //start with hiding labels
			
			task = new AnimatorTask() {
				double sum = 0;
				
				public boolean run(double time, double dt) {
					//rotate cmp
					MatrixBuilder m = MatrixBuilder.euclidean(cmp.getTransformation());
					m.rotate(0.05*dt*angle, axis);
					m.assignTo(cmp);
					//set visibility of layers
					if (sum > layerTimer) {
						if ( direction<0 && topLayer < layerCount ) {
							cmp.getChildComponent(topLayer++).setVisible(false);
							if (topLayer == layerCount) direction = 1;
						}
						else if ( direction>0 && topLayer > 1 ) {
							cmp.getChildComponent(--topLayer).setVisible(true);
							if (topLayer == 1) direction = -1;
						}
						sum = 0;
					}
					else sum += dt; 
					
					return true;
				}
			};
			removeCurrentSlot(actSlot);
			//task is scheduled in the following
		}
		
		//don't perform if minus or plus are released
		if (tc.getSource().equals(minus) && tc.getAxisState(minus).isReleased()) 
			return;
		if (tc.getSource().equals(plus) && tc.getAxisState(plus).isReleased()) 
			return;
		
		if (tc.getAxisState(minus).isPressed()) {
			if (topLayer < layerCount) {
				cmp.getChildComponent(topLayer++).setVisible(false);
				if (topLayer == layerCount) direction = 1;
			}
			return;
		}
		if (tc.getAxisState(plus).isPressed()) {
			if (topLayer > 1) {
				cmp.getChildComponent(--topLayer).setVisible(true);
				if (topLayer == 1) direction = -1;
			}
			return;
		}
		
		//pause
		if (tc.getAxisState(pause).isReleased())
			AnimatorTool.getInstance().schedule(cmp, task);
		if (tc.getAxisState(pause).isPressed())
			AnimatorTool.getInstance().deschedule(cmp);
	}
	
	
	
	
//	PROPERTIES
	static final String gfzDir = "/net/MathVis/gfz";
	static final int slideInterval = 5000;  //time after which the slide changes in millis
	static final double scenePanelWidth = 1.0;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		
//		LOAD GFZ DATA
		File file = new File(gfzDir + "/gfz.jrs");
		ReaderJRS r = new ReaderJRS();
		r.setInput(new Input(file));
		JrScene scene = r.getScene();
		
		final SceneGraphComponent root = scene.getSceneRoot();
		final SceneGraphComponent sceneCmp = scene.getPath("emptyPickPath").getLastComponent();
		final SceneGraphComponent gfz = sceneCmp.getChildComponent(0);
		
		PickUtility.assignFaceAABBTrees(gfz);  //allows fast picking
		gfz.addTool(new GFZTool());
		//gfz transformation
		MatrixBuilder.euclidean().rotateX(-Math.PI/2.3).assignTo(sceneCmp);
		
		
//		BOTTOM RIGHT PANEL
		final SceneGraphComponent panCmp = new SceneGraphComponent();
		panCmp.setName("scenePanel");
		addSlide(gfzDir + "/sheet1.jpg", panCmp);
		addSlide(gfzDir + "/sheet2.jpg", panCmp);
		addSlide(gfzDir + "/sheet3.jpg", panCmp);
		addSlide(gfzDir + "/sheet4.jpg", panCmp);
		//panel transformation
		MatrixBuilder.euclidean().translate(4500, -2800, 1000).scale(2500).rotateY(-Math.PI/5).assignTo(panCmp);
		root.addChild(panCmp);
		new Thread(new Runnable(){
			public void run() {
				final int children = panCmp.getChildComponentCount();
				int index = 0;
				panCmp.getChildComponent(index).setVisible(true);
				while (true) {
					try { Thread.sleep(slideInterval); } 
					catch (InterruptedException e) { e.printStackTrace(); }
					int nextIndex = (index+1) % children;
					panCmp.getChildComponent(nextIndex).setVisible(true);
					panCmp.getChildComponent(index).setVisible(false);
					index = nextIndex;
				}
			}
		}).start();
//		panCmp.addTool(new DraggingTool());
		
		
//		LEGEND
		SceneGraphComponent legend = new SceneGraphComponent();
		legend.setName("legend");
		root.addChild(legend);
		legend.setGeometry(Primitives.texturedQuadrilateral(new double[]{0,1,0,1,1,0,1,0,0,0,0,0}));
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.LINE_WIDTH, 2.0);
		app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.GRAY);
		app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		app.setAttribute(CommonAttributes.FACE_DRAW, true);
		app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		app.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
		legend.setAppearance(app);
		ImageData img = ImageData.load(Input.getInput(gfzDir + "/img/Legende.png"));
		//TODO: add texture to component
		Texture2D tex = TextureUtility.createTexture(app, CommonAttributes.POLYGON_SHADER, img);
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(1).getMatrix());
		//legend transformation
		final double ratio = 1.0;  //size of billboard
		MatrixBuilder.euclidean().translate(-6500, -2800, 0).scale(5).scale(ratio*img.getWidth(), ratio*img.getHeight(), 0).assignTo(legend);
		legend.addTool(new EncompassTool());
		
		
//		START VIEWERAPP
		ViewerApp viewerApp = new ViewerApp(scene);
//		viewerApp.setShowMenu(true);
//		viewerApp.setAttachNavigator(true);
//		viewerApp.setAttachBeanShell(true);
		viewerApp.update();
		viewerApp.display();
		
//		root.addTool(new SelectionTool(viewerApp));
	}
	
	
	
	private static ScenePanel createImagePanel(String fileName) {
		
		ScenePanel pan = new ScenePanel();
		pan.setShowFeet(false);
		pan.setAngle(Math.PI/2);
		final Image img = new ImageIcon(fileName).getImage();
		final int w = img.getWidth(null);
		final int h = img.getHeight(null);
		final int border=20;
		JPanel imgPanel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				g.clearRect(0, 0, w+2*border, h+2*border);
				g.drawImage(img, border, border, w, h, null);
			}
		};
		Dimension d = new Dimension(w+2*border, h+2*border);
		imgPanel.setSize(w+2*border, h+2*border);
		imgPanel.setPreferredSize(d);
		imgPanel.setMinimumSize(d);
		imgPanel.setMaximumSize(d);
		
		pan.getFrame().getContentPane().add(imgPanel);
		pan.getFrame().pack();
		
		pan.getFrame().setVisible(true);
		pan.setPanelWidth(scenePanelWidth);
		
		return pan;
	}
	
	
	private static void addSlide(String filename, SceneGraphComponent parent) {
		final ScenePanel pan = createImagePanel(filename);
		final SceneGraphComponent child = pan.getComponent();
		child.setVisible(false);
		parent.addChild(child);
	}
}


//------ OLD STUFF ---------------------------------------------------------------------
//COORDINATE SYSTEM (bounding box)
//final CoordinateSystemFactory coords = new CoordinateSystemFactory(gfz, 500.0);
//coords.showLabels(false);
//coords.showBox(true);
//coords.showGrid(false);
//coords.beautify(true);
//final Color boxColor = Color.GRAY;
//coords.setGridColor(boxColor);
//coords.setBoxColor(boxColor);


//PIPE LABELS
//final double offset = 200.0;
//final double sqrt = Math.sqrt(0.5*Math.pow(offset, 2.0));
//double[][] vertices = new double[][]{
//{-1222-sqrt, -400-sqrt, 800},  //red 
//{-1160, -347+offset, 1000},     //blue
//{-1139+sqrt, -429-sqrt, 1200}}; //green
//PointSetFactory fac = new PointSetFactory();
//fac.setVertexCount(3);
//fac.setVertexCoordinates(vertices);
//fac.setVertexLabels(new String[]{"red", "blue", "green"});
//fac.setVertexColors(new Color[]{Color.RED, Color.BLUE, Color.GREEN});
//fac.update();
//final SceneGraphComponent pipeLabels = new SceneGraphComponent();
//pipeLabels.setName("pipe labels");
//pipeLabels.setGeometry(fac.getPointSet());
//Appearance app = new Appearance();
//final double labelScale = 4.0;
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"scale", labelScale);  //label scale)
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"offset", new double[]{0, 100.0, 0});
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"alignment", SwingConstants.NORTH);
//app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//app.setAttribute(CommonAttributes.POINT_RADIUS, 50.0);
//pipeLabels.setAppearance(app);
//gfz.addChild(pipeLabels);


//ALTERNATIVELY PIPE LABELS
////pipe labels
//final double offset = 200.0;
//final double sqrt = Math.sqrt(0.5*Math.pow(offset, 2.0));
//final double labelScale = 5.0;
//double[][] vertices = new double[][]{
//{-1222-sqrt, -400-sqrt, 800},  //red 
//{-1160, -347+offset, 1000},     //blue
//{-1139+sqrt, -429-sqrt, 1200}}; //green
//String[] labels = new String[]{"red", "blue", "green"};
//Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.GREEN};
//final SceneGraphComponent pipeLabels = new SceneGraphComponent();
//pipeLabels.setName("pipe labels");
//PointSetFactory fac;
//for (int i = 0; i < 3; i++) {
//fac = new PointSetFactory();
//fac.setVertexCount(1);
//fac.setVertexCoordinates(vertices[i]);
//fac.setVertexLabels(new String[]{labels[i]});
//fac.update();
//final SceneGraphComponent cmp = new SceneGraphComponent();
//cmp.setName(labels[i]);
//cmp.setGeometry(fac.getPointSet());
//Appearance app = new Appearance();
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"scale", labelScale);  //label scale)
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"alignment", SwingConstants.CENTER);
//app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
////app.setAttribute(CommonAttributes.POINT_RADIUS, 50.0);
//cmp.setAppearance(app);
//pipeLabels.addChild(cmp);
//}
//gfz.addChild(pipeLabels);
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


package de.jreality.scene.tool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;


public class ToolRotateTool extends Tool {

  LinkedList usedSlots = new LinkedList();
  LinkedList toolsRotate = new LinkedList();
  
  InputSlot rotateSlot = InputSlot.getDevice("ToolRotateActivation");

  Tool selectedTool;
  SceneGraphComponent component;
  
  double[] offset=new double[]{0, 1.7, -1};
  
  final SceneGraphComponent displayComponent;
  final IndexedFaceSet quad;
  Texture2D tex;
  BufferedImage img;
  
  boolean displaying;
  
  int selectedIndex=-1;
  
  static final long displayTime=3000;
  
  public ToolRotateTool() {
    usedSlots.add(rotateSlot);
    displayComponent=new SceneGraphComponent();
    displayComponent.setAppearance(new Appearance());
    img = new BufferedImage(160, 50, BufferedImage.TYPE_INT_ARGB);
    quad = quad(0.4, 0.125);
    displayComponent.setGeometry(quad);
    tex = TextureUtility.createTexture(displayComponent.getAppearance(), "polygonShader", new ImageData(img), false);
    displayComponent.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
    displayComponent.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.gray);
    displayComponent.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.gray);
    displayComponent.getAppearance().setAttribute("lineShader.tubeRadius", 0.01);
    displayComponent.getAppearance().setAttribute("pointShader.pointRadius", 0.01);
    displayComponent.getAppearance().setAttribute("showPoints", true);
    displayComponent.getAppearance().setAttribute("lighting", false);
    setOffset(offset);
  }
  
  public List getActivationSlots() {
    return Collections.EMPTY_LIST; // always active
  }

  public List getCurrentSlots() {
    return usedSlots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  public void activate(ToolContext tc) {
  }

  public void perform(ToolContext tc) {
    if (component == null) component = tc.getRootToToolComponent().getLastComponent();
    if (toolsRotate.isEmpty()) return;
    if (!tc.getAxisState(rotateSlot).isPressed()) return;
    if (selectedTool != null)
      component.removeTool(selectedTool);
    selectedIndex++;
    if (selectedIndex >= toolsRotate.size()) {
      selectedIndex=-1;
    }
    selectedTool=(selectedIndex != -1) ? (Tool) toolsRotate.get(selectedIndex) : null;
    display(tc);
    if (selectedIndex == -1) return;
    
    component.addTool(selectedTool);
    
    if (!displaying) {
      //tc.getViewer().getSceneRoot().addChild(displayComponent);
      tc.getToolSystem().getAvatarPath().getLastComponent().addChild(displayComponent);
      displaying=true;
      System.out.println("added display comp");
    }
    
  }
  
  private void display(ToolContext tc) {
    String text = "no Tool selected";
    if (selectedTool != null) {
      String cn = selectedTool.getClass().getName();
      text = cn.substring(cn.lastIndexOf('.')+1);
    }
    System.out.println("i="+selectedIndex+" text="+text);
    tex.setImage(write(text));
    
    tc.deschedule(ToolRotateTool.this);
    displayComponent.setGeometry(quad);
    
    tc.schedule(ToolRotateTool.this, new AnimatorTask() {
      long st = System.currentTimeMillis();
      public boolean run(AnimatorContext context) {
        long t = System.currentTimeMillis()-st;
        if (t >= displayTime) {
          displayComponent.setGeometry(null);
          return false;
        }
        return true;
      }
    });
  }

  private ImageData write(String string) {
    Graphics2D g = img.createGraphics();
    g.setBackground(Color.white);
    g.setColor(Color.blue);
    g.clearRect(0, 0, img.getWidth(), img.getHeight());
    g.drawString(string, 20, 30);
    return new ImageData(img);
  }

  public void deactivate(ToolContext tc) {
  }
  public void addTool(Tool tool) {
    if (toolsRotate.contains(tool))
      throw new IllegalArgumentException("duplicate Tool");
    toolsRotate.addLast(tool);
  }
  public void removeTool(Tool tool) {
    int i = toolsRotate.indexOf(tool);
    toolsRotate.remove(tool);
    if (i == selectedIndex) {
      selectedIndex = -1;
      return;
    }
    if (i < selectedIndex) {
      selectedIndex--;
    } else {
      selectedIndex++;
    }
  }
  public void setComponent(SceneGraphComponent comp) {
    if (component != null && selectedTool != null)
      component.removeTool(selectedTool);
    component=comp;
    if (selectedTool != null)
      component.addTool(selectedTool);
  }
  
  /**
   * returns a quad in the z=0 plane with x from -width/2 to width/2
   * and y from -height/2 to height/2 <br>
   * this is useful for drawing java.awt.Images onto...
   * @param width
   * @param height
   * @return
   */
  static IndexedFaceSet quad(double width, double height) {
    IndexedFaceSet ifs = new IndexedFaceSet(4, 1);
    double w = width/2;
    double h = height/2;
    double[] verts = new double[]{ -w, -h, 0, -w, h, 0, w, h, 0, w, -h, 0};
    double[] texCoords = new double[]{0, 1, 0, 0, 1, 0, 1, 1};
    int[][] indices = new int[][]{{0,1,2,3}};
    ifs.setVertexAttributes(Attribute.COORDINATES,
        new DoubleArrayArray.Inlined(verts, 3)
    );
    ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES,
        new DoubleArrayArray.Inlined(texCoords, 2)
    );
    ifs.setFaceAttributes(Attribute.INDICES,
        new IntArrayArray.Array(indices)
    );
    IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
    GeometryUtility.calculateAndSetNormals(ifs);
    return ifs;
  }

  public double[] getOffset() {
    return offset;
  }

  public void setOffset(double[] offset) {
    if (offset.length != 3 && offset.length != 4) throw new IllegalArgumentException("not a translation vector!");
    this.offset = offset;
    MatrixBuilder.euclidean()
    .translate(offset)
    .assignTo(displayComponent);
  }

}

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


package de.jreality.tools;

import java.awt.Color;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;

public class PointerDisplayTool extends AbstractTool {

  InputSlot pointer = InputSlot.getDevice("PointerShipTransformation");
  SceneGraphComponent c = new SceneGraphComponent();
  
  public PointerDisplayTool(double radius) {
    addCurrentSlot(pointer);
    SceneGraphComponent stick=new SceneGraphComponent();
    MatrixBuilder.euclidean().translate(0,0,-1).scale(radius, radius, 1).assignTo(stick);
    IndexedFaceSet cube = Primitives.cube();
    GeometryUtility.calculateAndSetFaceNormals(cube);
	stick.setGeometry(cube);
    c.setAppearance(new Appearance());
    c.getAppearance().setAttribute("diffuseColor", Color.yellow);
    c.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
    c.getAppearance().setAttribute("showPoints", false);
    c.getAppearance().setAttribute("showFaces", true);
    c.getAppearance().setAttribute("showLines", false);
    c.getAppearance().setAttribute(CommonAttributes.PICKABLE, false);
    c.setTransformation(new Transformation());
    c.addChild(stick);
  }
  public PointerDisplayTool() {
    this(0.01);
  }
  
  boolean isAssigned;
  Matrix m = new Matrix();
  
  public void perform(ToolContext tc) {
	if (!isAssigned) {
		tc.getAvatarPath().getLastComponent().addChild(c);
		isAssigned=true;
	}
    m.assignFrom(tc.getTransformationMatrix(pointer));
    m.assignTo(c.getTransformation());
  }
}

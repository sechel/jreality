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


package de.jreality.tutorial.tool;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;

public class TextureExample2 {
  
  // java de.jreality.tutorial.TextureExample grid.jpeg
  
  private static double[] texMatrix;
private static Texture2D texture2d;

public static void main(String[] args) throws IOException {
	  IndexedFaceSet ico = new CatenoidHelicoid(40);
	  SceneGraphComponent sgc = new SceneGraphComponent();
	  sgc.setGeometry(ico);
      sgc.setAppearance(new Appearance());
      sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, Color.YELLOW);
      sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
      sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
      ImageData id = null;
      sgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.GREEN);
      double scale = 3;
      if (args.length > 0) id =  ImageData.load(Input.getInput(args[0]));
      else  {
    	  SimpleTextureFactory stf = new SimpleTextureFactory();
    	  stf.update();
    	  id = stf.getImageData();
    	  scale = 10;
      }
      texture2d = TextureUtility.createTexture(sgc.getAppearance(), CommonAttributes.POLYGON_SHADER,id);
      texture2d.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
      texture2d.setApplyMode(Texture2D.GL_MODULATE);
    
    Tool t = new AbstractTool(InputSlot.getDevice("AllDragActivation")) {

    	private double[] origTexCoords;
    	Matrix origTexMatrix;

		{
    		addCurrentSlot(InputSlot.getDevice("PointerTransformation"), "drags the texture");
    	}
    	
		public void activate(ToolContext tc) {
			origTexCoords = tc.getCurrentPick().getTextureCoordinates();
			origTexMatrix = texture2d.getTextureMatrix();
			System.err.println("Activating texture tool");
		}

		public void perform(ToolContext tc) {
			if (tc.getCurrentPick() == null) return;
			double[] texCoords = tc.getCurrentPick().getTextureCoordinates();
			double[] diff = Rn.subtract(null, origTexCoords, texCoords);
			double[] diff4 = {diff[0], diff[1], 0, 1.0};
			double[] trans = P3.makeTranslationMatrix(null, diff4, Pn.EUCLIDEAN);
			texture2d.setTextureMatrix(new Matrix(Rn.times(null, origTexMatrix.getArray(), trans)));
		}

		public void deactivate(ToolContext tc) {
		}

		public String getDescription(InputSlot slot) {
			return null;
		}

		public String getDescription() {
			return null;
		}
    	
    };
	sgc.addTool(t);		

    ViewerApp.display(sgc);
  }
}
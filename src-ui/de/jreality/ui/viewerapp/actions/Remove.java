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


package de.jreality.ui.viewerapp.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jreality.ui.treeview.SelectionEvent;
import de.jreality.ui.viewerapp.Navigator;


public class Remove extends AbstractAction {

  private static final long serialVersionUID = 1L;
  
  private Object obj = null;
  
  
  public Remove(String name, Navigator navigator) {
    super(name, navigator);
  }
  
  
  public Remove(String name, Navigator n, Component frame) {
    super(name, n, frame);
  }

  
  public void actionPerformed(ActionEvent e) {
  
    final SceneGraphComponent parent = (SceneGraphComponent) actee;
    
    if (obj instanceof SceneGraphNode) {
      ((SceneGraphNode) obj).accept(new SceneGraphVisitor() {
        
        public void visit(SceneGraphComponent sc) {
          //TODO: remove tools of sc?
          parent.removeChild(sc);
        }
        public void visit(Geometry g) {
          parent.setGeometry(null);
        }
        public void visit(Transformation t) {
          parent.setTransformation(null);
        }
        public void visit(Appearance a) {
          parent.setAppearance(null);
        }
        public void visit(Camera c) {
          parent.setCamera(null);
        }
        public void visit(Light l) {
         parent.setLight(null); 
        }
      });
    }
    else if (obj instanceof Tool) {
//      Scene.executeWriter(parent, new Runnable() {
//        public void run() {
          parent.removeTool((Tool) obj);
//        }
//      });
    }
  }


  void selectionChanged(SelectionEvent e) {
    
    SceneGraphComponent parent = e.getParentOfSelection();
    
    if (parent != null) {
      setEnabled(true);
      actee = parent;
      
      if (e.selectionIsSGNode())
        obj = e.selectionAsSGNode();
      else if (e.selectionIsTool())
        obj = e.selectionAsTool();
      //else parent == null
    }
    else setEnabled(false);
  }


  Object getDefaultActee() {
    return navigator.getRoot();
  }
  
}
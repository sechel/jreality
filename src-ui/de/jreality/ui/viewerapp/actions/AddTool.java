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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.FlyTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.LookAtTool;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ScaleTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.tools.ShipRotateTool;
import de.jreality.tools.ShipScaleTool;
import de.jreality.tools.ShowPropertiesTool;
import de.jreality.tools.TranslateTool;
import de.jreality.ui.treeview.SelectionEvent;
import de.jreality.ui.viewerapp.Navigator;


public class AddTool extends AbstractAction {

  private static final long serialVersionUID = 1L;
  
  private JList toolList = null;
  private JScrollPane scrollPane = null;
  
  
  public AddTool(String name, Navigator navigator, Component frame) {
    super(name, navigator, frame);
    setupToolList();
  }
  
  
  public AddTool(String name, SceneGraphComponent node, Component frame) {
    super(name, node, frame);
    setupToolList();
  }


  void selectionChanged(SelectionEvent e) {
    
    if (e.selectionIsSGComp()) {
      setEnabled(true);
      actee = e.selectionAsSGComp();
    }
    else setEnabled(false);
  }


  public void actionPerformed(ActionEvent e) {
   
    int ret = JOptionPane.showConfirmDialog(frame, scrollPane, "Add Tool", JOptionPane.OK_CANCEL_OPTION);
    if (ret == JOptionPane.OK_OPTION) {
      Object[] selectedTools = toolList.getSelectedValues();
      for (int i=0; i<selectedTools.length; i++) {
        try {
          final Tool t = (Tool) ((Class)selectedTools[i]).newInstance();
          ((SceneGraphComponent) actee).addTool(t);
        } catch (Exception exc) {
          //System.out.println("Could not add tool!");
        }
      }
    }
    toolList.clearSelection();
  }
  
 
  private void setupToolList() {
    
    List<Class> tools = new LinkedList<Class>();
    
    tools.add(DraggingTool.class);
    tools.add(EncompassTool.class);
    tools.add(FlyTool.class);
    tools.add(HeadTransformationTool.class);
    tools.add(LookAtTool.class);
    tools.add(PointerDisplayTool.class);
    tools.add(RotateTool.class);
    tools.add(ScaleTool.class);
    tools.add(ShipNavigationTool.class);
    tools.add(ShipRotateTool.class);
    tools.add(ShipScaleTool.class);
    tools.add(ShowPropertiesTool.class);
    tools.add(TranslateTool.class);
    
//    try {
//      tools.add(Class.forName("de.jreality.portal.tools.PortalHeadMoveTool"));
//    } catch (ClassNotFoundException exc) {}

    toolList = new JList(tools.toArray());
    toolList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    scrollPane = new JScrollPane(toolList);
  }
  
}
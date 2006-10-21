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

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.tool.Tool;
import de.jtem.beans.BooleanEditor;
import de.jtem.beans.ColorEditor;
import de.jtem.beans.EditorSpawner;
import de.jtem.beans.FontEditor;
import de.jtem.beans.InspectorPanel;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;


public class Navigator {

  private InspectorPanel inspector;
  private JTree sceneTree;
  private TreeSelectionModel tsm;
  private BeanShell beanShell;
  
  private SceneGraphComponent sceneRoot;  //the scene root
  
  
  public Navigator(SceneGraphComponent sceneRoot) {
  
    inspector = new InspectorPanel(false);
    BooleanEditor.setNameOfNull("inherit");
    EditorSpawner.setNameOfNull("inherit");
    ColorEditor.setAllowNullByDefault(true);
    FontEditor.setAllowNullByDefault(true);

    sceneTree = new JTree();
    SceneTreeModel model = new SceneTreeModel(sceneRoot);
    sceneTree.setModel(model);
    sceneTree.setAnchorSelectionPath(new TreePath(model.getRoot()));
    sceneTree.setCellRenderer(new JTreeRenderer());
    sceneTree.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "toggle");  //collaps/expand nodes with ENTER

    tsm = sceneTree.getSelectionModel();
    tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    tsm.addTreeSelectionListener(new SelectionListener(){

      public void selectionChanged(SelectionEvent e) {

        Object selection = null;
        
        if (e.selectionIsSGNode()) selection = e.selectionAsSGNode();
        else if (e.selectionIsTool()) selection = e.selectionAsTool();
        else selection = e.getSelection();  //e.g. shader
        
        inspector.setObject(selection);
        if (beanShell != null) beanShell.setSelf(selection);
      }
    });
    
    tsm.setSelectionPath(sceneTree.getAnchorSelectionPath());  //select sceneRoot in tree by default
    
    this.sceneRoot = sceneRoot;
  }
  
  
  public InspectorPanel getInspector() {
    return inspector;
  }
  
  
  public JTree getSceneTree() {
    return sceneTree;
  }
  
  
  public TreeSelectionModel getTreeSelectionModel() {
    return tsm;
  }
  

  public void setBeanShell(BeanShell beanShell) {
    this.beanShell = beanShell;
  }
  
  
  public SceneGraphComponent getRoot() {
    return sceneRoot;
  }
  
  
  
  public static abstract class SelectionListener implements TreeSelectionListener {
    
    public abstract void selectionChanged(SelectionEvent e);
    
    public void valueChanged(TreeSelectionEvent e) {
      
      boolean[] areNew = new boolean[e.getPaths().length];
      for (int i = 0; i < areNew.length; i++)
        areNew[i] = e.isAddedPath(i);
      
      SelectionEvent se = new SelectionEvent(e.getSource(), e.getPaths(), 
          areNew, e.getOldLeadSelectionPath(), e.getNewLeadSelectionPath()); 
      
      selectionChanged(se);
    }
    
    
  }
  
  
  public static class SelectionEvent extends TreeSelectionEvent{

    private static final long serialVersionUID = 1L;

    private Object selection;
    

    /** calls TreeSelectionEvent(...) */
    public SelectionEvent(Object source, TreePath[] paths, boolean[] areNew, TreePath oldLeadSelectionPath, TreePath newLeadSelectionPath) {
      super(source, paths, areNew, oldLeadSelectionPath, newLeadSelectionPath);
      
      TreePath path = getNewLeadSelectionPath();
      if (path != null) selection = path.getLastPathComponent();
    }
    
    
    public Object getSelection() {
      return selection;
    }
    

    public boolean selectionIsSGNode() {
      return (selection instanceof SceneTreeNode);
    }
    
    
    public SceneGraphNode selectionAsSGNode() {
      if (selectionIsSGNode())
        return ((SceneTreeNode) selection).getNode();
      else return null;
    }
    
    
    public boolean selectionIsSGComp() {
      return (selectionAsSGNode() instanceof SceneGraphComponent);
    }
    
    
    public SceneGraphComponent selectionAsSGComp() {
      if (selectionIsSGComp())
        return (SceneGraphComponent) selectionAsSGNode();
      else return null;
    }
    
    
    public boolean selectionIsTool() {
      return (selection instanceof TreeTool);
    }
    
    
    public Tool selectionAsTool() {
      if (selectionIsTool())
        return ((TreeTool) selection).getTool();
      else return null;
    }
    
    
    public boolean selectionIsAttributeEntity() {
      return (!selectionIsSGNode() && !selectionIsTool());
    }
    
    
    public AttributeEntity selectionAsAttributeEntity() {
      if (selectionIsAttributeEntity())
        return (AttributeEntity) selection;
      else return null;
    }

    
    /**
     * Returns null if the selection has no parent (i.e. selection is the root node) 
     * or if the selection is not a SGNode or a tool (e.g. shader).
     * @return the parent component
     */
    public SceneGraphComponent getParentOfSelection() {
      
      SceneGraphComponent parent = null;
      
      if (selectionIsSGNode()) {
        SceneTreeNode p = ((SceneTreeNode) selection).getParent();
        if (p != null)
          parent = (SceneGraphComponent) p.getNode();
          //could parent be something different than SGComponent?
      }
      else if (selectionIsTool())
        parent = (SceneGraphComponent) ((TreeTool) selection).getTreeNode().getNode();
        
      return parent;  //returns null if no parent or selection other than SGNode or tool (e.g. shader)
    }


    /**
     * Converts the TreePath of the current selection into a SceneGraphPath.  
     * @return the path of the current selection
     */
    public SceneGraphPath getSGPath() {
      SceneGraphPath sgPath = new SceneGraphPath();
      Object[] treePath = getPath().getPath();
      for (int i = 0; i < treePath.length; i++) {
        selection = treePath[i];
        if (selectionIsSGNode())
          sgPath.push(selectionAsSGNode());
        else break;
      }
      return sgPath;
    }
  }
}
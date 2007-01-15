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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
import de.jreality.ui.viewerapp.actions.edit.AddTool;
import de.jreality.ui.viewerapp.actions.edit.AssignFaceAABBTree;
import de.jreality.ui.viewerapp.actions.edit.ExportOBJ;
import de.jreality.ui.viewerapp.actions.edit.Remove;
import de.jreality.ui.viewerapp.actions.edit.Rename;
import de.jreality.ui.viewerapp.actions.edit.SaveSelected;
import de.jreality.ui.viewerapp.actions.edit.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.edit.TogglePickable;
import de.jtem.beans.BooleanEditor;
import de.jtem.beans.EditorSpawner;
import de.jtem.beans.InspectorPanel;
import de.jtem.beans.NumberSpinnerEditor;


/**
 * Scene tree and inspector panel for a given scene graph.
 * 
 * @author msommer
 */
public class Navigator implements SelectionListener {

	private InspectorPanel inspector;
	private JTree sceneTree;
	private SceneTreeModel treeModel;
	private TreeSelectionModel tsm;
	
	private SelectionManager sm;
	private SceneGraphComponent sceneRoot;  //the scene root
	private Object currentSelection;
	
	private Component navigator;
	

	public Navigator(SceneGraphComponent sceneRoot, SelectionManager selectionManager) {

		sm = selectionManager;
		sm.addSelectionListener(this);
		
		inspector = new InspectorPanel(false);
		BooleanEditor.setNameOfNull("inherit");
		EditorSpawner.setNameOfNull("inherit");
		EditorSpawner.setNameOfCreation("inherited");
		NumberSpinnerEditor.setNameOfNull("inherit");
		NumberSpinnerEditor.setNameOfCreation("inherited");
		//EditorManager.registerEditor(Texture2D.class, ObjectEditor.class);

		sceneTree = new JTree();
		treeModel = new SceneTreeModel(sceneRoot);
		
		sceneTree.setModel(treeModel);
		//set default selection (use the selection manager's default)
		sceneTree.setAnchorSelectionPath(new TreePath(treeModel.convertSceneGraphPath(sm.getDefaultSelection())));
		sceneTree.setCellRenderer(new JTreeRenderer());
		sceneTree.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "toggle");  //collaps/expand nodes with ENTER

		tsm = sceneTree.getSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tsm.addTreeSelectionListener(new SelectionListener(){

			public void selectionChanged(SelectionEvent e) {

				//update inspector
				if (e.selectionIsSGNode()) currentSelection = e.selectionAsSGNode();
				else if (e.selectionIsTool()) currentSelection = e.selectionAsTool();
				else currentSelection = e.getSelection();  //attribute entity or null
				inspector.setObject(currentSelection);

				//update selection manager
				Tool tool = e.selectionAsTool();  //null if no tool
				AttributeEntity entity = e.selectionAsAttributeEntity();  //null if no attribute entity
				sm.setSelection(e.getSGPath(), tool, entity);  //does nothing if already selected
			}
		});
		
		this.sceneRoot = sceneRoot;
		
		tsm.setSelectionPath(new TreePath(treeModel.convertSceneGraphPath(sm.getSelection())));  //select current selection
		setupContextMenu();
	}

	
	public void selectionChanged(de.jreality.ui.viewerapp.SelectionEvent e) {
		//convert selection of manager into TreePath
        Object[] selection = treeModel.convertSceneGraphPath(e.getSelection());
        TreePath path = new TreePath(selection);
        
        if (e.nodeSelected() && !path.equals(tsm.getSelectionPath()))  //compare paths only if a node is selected  
        	tsm.setSelectionPath(path);
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
	

	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}


	public Object getCurrentSelection() {
		return currentSelection;
	}

	
	private void setupContextMenu() {

		final JPopupMenu cm = new JPopupMenu();
		cm.setLightWeightPopupEnabled(false);
		
		//create content of context menu
		Component parentComp = sceneTree;
		cm.add(new JMenuItem(new SaveSelected(ViewerAppMenu.SAVE_SELECTED, sm, parentComp)));
	    cm.add(new JMenuItem(new ExportOBJ(ViewerAppMenu.EXPORT_OBJ, sm, parentComp)));
	    cm.addSeparator();
	    cm.add(new JMenuItem(new Remove(ViewerAppMenu.REMOVE, sm)));
	    cm.add(new JMenuItem(new Rename(ViewerAppMenu.RENAME, sm, parentComp)));
	    cm.addSeparator();
	    cm.add(new JMenuItem(new AddTool(ViewerAppMenu.ADD_TOOL, sm, parentComp)));
	    cm.addSeparator();
	    cm.add(new JMenuItem(new ToggleAppearance(ViewerAppMenu.TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
	    cm.add(new JMenuItem(new ToggleAppearance(ViewerAppMenu.TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
	    cm.add(new JMenuItem(new ToggleAppearance(ViewerAppMenu.TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
	    cm.addSeparator();
	    cm.add(new JMenuItem(new TogglePickable(ViewerAppMenu.TOGGLE_PICKABLE, sm)));
	    cm.add(new JMenuItem(new AssignFaceAABBTree(ViewerAppMenu.ASSIGN_FACE_AABBTREE, sm)));

		    
		//add listener to the navigator's tree
		sceneTree.addMouseListener(new MouseAdapter() {

			public void mousePressed( MouseEvent e ) {
				handlePopup( e );
			}

			public void mouseReleased( MouseEvent e ) {
				handlePopup( e );
			}

			private void handlePopup( MouseEvent e ) {
				if ( e.isPopupTrigger() ) {
					TreePath path = sceneTree.getPathForLocation( e.getX(), e.getY() );
					if ( path != null ) {
						tsm.clearSelection();  //ensures that SelectionListeners are notified even if path did not change
						tsm.setSelectionPath( path );
						cm.show( e.getComponent(), e.getX(), e.getY()+10 );
					}
				}
			}
		});
	}

	
	/**
	 * Get the navigator as a Component.
	 * @return the navigator
	 */
	public Component getComponent() {
		
		if (navigator == null) {
			sceneTree.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
			JScrollPane top = new JScrollPane(sceneTree);
			top.setBorder(BorderFactory.createEmptyBorder());

			inspector.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
			JScrollPane bottom = new JScrollPane(inspector);
			bottom.setBorder(BorderFactory.createEmptyBorder());

			JSplitPane navigator = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, top, bottom);
			navigator.setContinuousLayout(true);
			navigator.setOneTouchExpandable(true);
			navigator.setResizeWeight(1.0);  //use extra space for sceneTree
			navigator.setBorder(BorderFactory.createEmptyBorder());

			navigator.setPreferredSize(new Dimension(0,0));  //let user set the size
			this.navigator = navigator;
		}
		
		return navigator;
	}

	
//	-- INNER CLASSES -----------------------------------

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

	}  //end of class SelectionListener


	public static class SelectionEvent extends TreeSelectionEvent {

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
    
	}  //end of class SelectionEvent

}
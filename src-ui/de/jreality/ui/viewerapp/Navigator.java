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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
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
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
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

	private Component navigator;
	private Component parentComp;


	/**
	 * @param sceneRoot the scene root
	 * @param selectionManager the underlying selection manager
	 */
	public Navigator(SceneGraphComponent sceneRoot, SelectionManager selectionManager) {
		this(sceneRoot, selectionManager, null);
	}


	/**
	 * @param sceneRoot the scene root
	 * @param selectionManager the underlying selection manager
	 * @param parentComp used by dialogs from the context menu (<code>null</code> allowed)
	 */
	public Navigator(SceneGraphComponent sceneRoot, SelectionManager selectionManager, Component parentComp) {

		sm = selectionManager;
		sm.addSelectionListener(this);

		this.parentComp = parentComp;

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
		sceneTree.setAnchorSelectionPath(new TreePath(treeModel.convertSelection(sm.getDefaultSelection())));
		sceneTree.setCellRenderer(new JTreeRenderer());
		sceneTree.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "toggle");  //collaps/expand nodes with ENTER

		tsm = sceneTree.getSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tsm.addTreeSelectionListener(new SelectionListener(){

			public void selectionChanged(SelectionEvent e) {

				Selection currentSelection = e.getSelection();
				//update inspector
				inspector.setObject(currentSelection.getLastElement());
				//update selection manager
				sm.setSelection(currentSelection);  //does nothing if already selected
			}
		});

		this.sceneRoot = sceneRoot;

		tsm.setSelectionPath(new TreePath(treeModel.convertSelection(sm.getSelection())));  //select current selection
		setupContextMenu();
	}


	public void selectionChanged(de.jreality.ui.viewerapp.SelectionEvent e) {
		//convert selection of manager into TreePath
		Object[] selection = treeModel.convertSelection(e.getSelection());
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


	public Selection getSelection() {
		return sm.getSelection();
	}


	private void setupContextMenu() {

		final JPopupMenu cm = new JPopupMenu();
		cm.setLightWeightPopupEnabled(false);

		//create content of context menu
		JMenu editMenu = ViewerAppMenu.createEditMenu(parentComp, sm);
		for (Component c : editMenu.getMenuComponents()) cm.add(c);

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

//			navigator.setPreferredSize(new Dimension(0,0));  //let user set the size
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

		/** calls TreeSelectionEvent(...) */
		public SelectionEvent(Object source, TreePath[] paths, boolean[] areNew, TreePath oldLeadSelectionPath, TreePath newLeadSelectionPath) {
			super(source, paths, areNew, oldLeadSelectionPath, newLeadSelectionPath);
		}

		private Object convert(Object o) {
			if (o instanceof SceneTreeNode)
				return ((SceneTreeNode) o).getNode();
			else if (o instanceof TreeTool)
				return ((TreeTool) o).getTool();

			return o;
		}

		/**
		 * Converts the TreePath of the current selection into a Selection object.  
		 * @return the current selection
		 */
		public Selection getSelection() {
			Selection selection = new Selection();
			Object[] treePath = getPath().getPath();
			for (int i = 0; i < treePath.length; i++)
				selection.push( convert(treePath[i]) );
			return selection;
		}

	}  //end of class SelectionEvent

}
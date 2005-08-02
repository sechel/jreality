/*
 * Created on 12.01.2004
 */
package de.jreality.scene.proxy.treeview;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.jreality.scene.*;
import de.jreality.scene.proxy.treeview.SceneTreeModel.Node;

/**
 * @author Holger
 */
public class FlashSelection implements TreeSelectionListener, ActionListener
{
  private final static byte FLASH_INVISIBLE = 1;
  private final static byte FLASH_VISIBLE1 =  0;
  private final static byte FLASH_VISIBLE  =  2;
  private final Timer timer=new Timer(350, this);
  private byte flashState;
  private int pendingTicks;
  private SceneGraphNode         selected;
  private SceneGraphComponent selectedParent;
  private SceneGraphComponent selectedComponent;
  private Geometry            selectedGeometry;
  private Viewer       viewer;

  public FlashSelection(Viewer v) {
    timer.setRepeats(true);
    timer.setCoalesce(false);
    viewer=v;
  }

  public void actionPerformed(ActionEvent evt) {
    if(flashState==FLASH_INVISIBLE) {
      if(selectedComponent!=null) {
        selectedParent.addChild(selectedComponent);
      } else {
        selectedParent.setGeometry(selectedGeometry);
      }
      flashState=FLASH_VISIBLE;
      if(pendingTicks--==0) timer.stop();
    } else if(flashState==FLASH_VISIBLE1){
      if(selectedComponent!=null) {
        selectedParent.removeChild(selectedComponent);
      } else {
        selectedParent.setGeometry(null);
      }
      flashState=FLASH_INVISIBLE;
    } else flashState=FLASH_VISIBLE1;
    viewer.render();
  }

  public void valueChanged(TreeSelectionEvent evt) {
    assert EventQueue.isDispatchThread();
    TreePath p=evt.getNewLeadSelectionPath();
    if(p==null) return;
    SceneTreeModel.Node n=(Node)p.getLastPathComponent();
    if(n.parent==null) return;//not the whole scene
    Object sel=n.target;
    if(sel==selected&&n.parent.target==selectedParent) return;
    if(sel instanceof SceneGraphComponent) {
      checkTerminateLastTick();
      selected=selectedComponent=(SceneGraphComponent)sel;
      selectedGeometry=null;
    } else if(sel instanceof Geometry) {
      checkTerminateLastTick();
      selected=selectedGeometry=(Geometry)sel;
      selectedComponent=null;
    } else return;
    selectedParent=(SceneGraphComponent)n.parent.target;
    flashState=FLASH_VISIBLE1;
    pendingTicks=3;
    timer.start();
  }

  /**
   * 
   */
  private void checkTerminateLastTick() {
    if(flashState==FLASH_INVISIBLE) {
      if(selectedComponent!=null) {
        selectedParent.addChild(selectedComponent);
      } else {
        selectedParent.setGeometry(selectedGeometry);
        flashState=FLASH_VISIBLE1;
        timer.stop();
      }
    }
  }
}
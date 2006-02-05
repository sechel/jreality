/*
 * Created on Mar 1, 2004
 *
 */
package de.jreality.util;

import java.util.LinkedList;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;

/**
 * @author gunn
 *
 * This visitor traverses a scene graph searching for {@link Path the first path}from the given 
 * {@link SceneGraphComponent} and the given {@link SceneGraphNode}.
 * 
 * TODO: make this a collector, put methods in SceneGraphUtility (get all paths as well)
 * 
 * TODO: return list of ALL paths
 * 
 * make singleton 
 */
public class PathCollector extends SceneGraphVisitor {

    SceneGraphComponent root;
    SceneGraphPath currentPath = new SceneGraphPath();
    LinkedList collectedPaths = new LinkedList();
    Matcher matcher;
    
	  public PathCollector(Matcher matcher, SceneGraphComponent root)	{
	  	this.matcher=matcher;
      this.root=root;
	  }

	  /*
	   * Changed this method to reflect new policy on SceneGraphPath to allow
	   * SceneGraphNode to be final element of the path rather than segregated
	   * into separate field.  -gunn 4.6.4
	   */
	   public Object visit()	{
		  visit(root);
      return collectedPaths;
	  }
	  
	  public void visit(SceneGraphNode m) {
      currentPath.push(m);
			if (currentPath.getLength() > 0 && matcher.matches(currentPath)) {
			  collectedPaths.add(currentPath.clone());
      }
      currentPath.pop();
	  }
	
	  public void visit(SceneGraphComponent c) {
      super.visit(c);
  	  currentPath.push(c);
		  c.childrenAccept(this);
		  currentPath.pop();
	}
    
  public interface Matcher {
     boolean matches(SceneGraphPath p);
  };

}

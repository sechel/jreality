/*
 * Created on Nov 19, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Pn;
import de.jreality.scene.*;


/**
 * This class holds static methods that make the parsing/traversal etc of a scene graph more comfortable.
 * This class is out of order at the moment!
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class SceneGraphUtility {

	public static SceneGraphComponent createFullSceneGraphComponent()	{
		return createFullSceneGraphComponent("unnamed");
	}
	public static SceneGraphComponent createFullSceneGraphComponent(String name)	{
		SceneGraphComponent c = new SceneGraphComponent();
		c.setTransformation(new Transformation());
		c.setAppearance(new Appearance());
		c.setName(name);
		return c;
	}
  /**
   * Replace the first child with the given component
  */
    public static void replaceChild(SceneGraphComponent c, SceneGraphComponent ch)  {
      int n = c.getChildComponentCount();
      if (n == 0) { c.addChild(ch); return; } 
      SceneGraphComponent och = c.getChildComponent(0);
      if (och == ch) return;
      c.removeChild(och);
      c.addChild(ch);
  }
	
  /**
   * @param component
   * 
   * TODO: this should be called removeChildComponents!
   */
  	public static void removeChildren(SceneGraphComponent c) {
	  	while (c.getChildComponentCount() > 0) c.removeChild(c.getChildComponent(0));
  	}
	
  	public static void resetMatrix(SceneGraphComponent r)	{
	  	final class ResetMatrixVisitor extends SceneGraphVisitor	{
		  	public void visit(SceneGraphComponent c)	{
			  	Transformation t = c.getTransformation();
			  	if (t != null) t.resetMatrix();
			  	c.childrenAccept(this);
		  	}
	  	}
	  	ResetMatrixVisitor rmv = new ResetMatrixVisitor();
	  	rmv.visit(r);
  		}
  
 	public static void setDefaultMatrix(SceneGraphComponent r)	{
	  	final class SetDefaultMatrixVisitor extends SceneGraphVisitor	{
		  	public void visit(SceneGraphComponent c)	{
			  	Transformation t = c.getTransformation();
			  	if (t != null) t.setDefaultMatrix();
			  	c.childrenAccept(this);
		  	}
	  	}
	  	SetDefaultMatrixVisitor rmv = new SetDefaultMatrixVisitor();
	  rmv.visit(r);
  	}
  
 	public static void setSignature(SceneGraphComponent r, int signature)	{
 		final int sig = signature;
         final HashMap map =new HashMap();
	  	final class SetSignatureVisitor extends SceneGraphVisitor	{
		  	public void visit(SceneGraphComponent c)	{
			  	Transformation t = c.getTransformation();
			  	if (t != null) t.setSignature(sig);
			  	c.childrenAccept(this);
		  	}
//		  	public void visit(Camera c)	{
//		  		c.setSignature(sig);
//		  	}
		  	public void visit(Geometry g)	{
		  		if (sig == Pn.EUCLIDEAN) return;
		  		Integer s = new Integer(sig);
                 map.put(g,s);
		  	}
	  	}
	  	SetSignatureVisitor rmv = new SetSignatureVisitor();
	  rmv.visit(r);
      Set keys = map.keySet();
      for (Iterator iter = keys.iterator(); iter.hasNext();) {
          Geometry g = (Geometry) iter.next();
          int s = ((Integer) map.get(g)).intValue();
          GeometryUtility.setSignature(g,s);
      }
  	}
  
  	public static List collectLights(SceneGraphComponent rootNode) {
  	    return (List) new LightCollector(rootNode).visit();
  	}
  	public static List collectClippingPlanes(SceneGraphComponent rootNode) {
  	    return (List) new ClippingPlaneCollector(rootNode).visit();
  	}
    public static List getPathsBetween(final SceneGraphComponent begin, final SceneGraphNode end) {
      final PathCollector.Matcher matcher = new PathCollector.Matcher() {
        public boolean matches(SceneGraphPath p) {
          return p.getLastElement() == end;
        }
      };
      return (List) new PathCollector(matcher, begin).visit();
    }
    public static List getPathsToNamedNodes(final SceneGraphComponent root, final String name) {
      final PathCollector.Matcher matcher = new PathCollector.Matcher() {
        public boolean matches(SceneGraphPath p) {
          System.out.println("compare="+p);
          return p.getLastElement().getName().equals(name);
        }
      };
      return (List) new PathCollector(matcher, root).visit();
    }
    
    /**
     * method to remove a child of arbitrary type
     * 
     * @param node the child to remove
     * @throws IllegalArgumentException if node is no child
     */
    public static void removeChildNode(final SceneGraphComponent parent, SceneGraphNode node) {
      node.accept(new SceneGraphVisitor() {
        public void visit(Appearance a) {
          if (parent.getAppearance() == a) parent.setAppearance(null);
          else throw new IllegalArgumentException("no such child!");
        }

        public void visit(Camera c) {
          if (parent.getCamera() == c) parent.setCamera(null);
          else throw new IllegalArgumentException("no such child!");
        }

        public void visit(Geometry g) {
          if (parent.getGeometry() == g) parent.setGeometry(null);
          else throw new IllegalArgumentException("no such child!");
        }

        public void visit(Light l) {
          if (parent.getLight() == l) parent.setLight(null);
          else throw new IllegalArgumentException("no such child!");
        }

        public void visit(Transformation t) {
          if (parent.getTransformation() == t) parent.setTransformation(null);
          else throw new IllegalArgumentException("no such child!");
        }

        public void visit(SceneGraphComponent c) {
          if (parent.getChildNodes().contains(c)) parent.removeChild(c);
          else throw new IllegalArgumentException("no such child!");
        }
      });
    }
    
    /**
     * method to add a child of arbitrary type
     * 
     * @param node the child to add
     */
    public static void addChildNode(final SceneGraphComponent parent, SceneGraphNode node) {
      node.accept(new SceneGraphVisitor() {
        public void visit(Appearance a) {
          parent.setAppearance(a);
        }

        public void visit(Camera c) {
          parent.setCamera(c);
        }

        public void visit(Geometry g) {
          parent.setGeometry(g);
        }

        public void visit(Light l) {
          parent.setLight(l);
        }

        public void visit(Transformation t) {
          parent.setTransformation(t);
        }

        public void visit(SceneGraphComponent c) {
          parent.addChild(c);
        } 
      });
    }

}

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


package de.jreality.util;

import java.util.List;

import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;


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
	
  	public static void setSignature(SceneGraphComponent r, int signature)	{
 		final int sig = signature;
 		 if (r.getAppearance() == null) r.setAppearance(new Appearance());
 		 r.getAppearance().setAttribute(CommonAttributes.SIGNATURE,sig);
    	}
  
  	public static List collectLights(SceneGraphComponent rootNode) {
  	    return (List) new LightCollector(rootNode).visit();
  	}
  	public static List collectClippingPlanes(SceneGraphComponent rootNode) {
  	    return (List) new ClippingPlaneCollector(rootNode).visit();
  	}
    public static List<SceneGraphPath> getPathsBetween(final SceneGraphComponent begin, final SceneGraphNode end) {
      final PathCollector.Matcher matcher = new PathCollector.Matcher() {
        public boolean matches(SceneGraphPath p) {
          return p.getLastElement() == end;
        }
      };
      return new PathCollector(matcher, begin).visit();
    }
    public static List getPathsToNamedNodes(final SceneGraphComponent root, final String name) {
      final PathCollector.Matcher matcher = new PathCollector.Matcher() {
        public boolean matches(SceneGraphPath p) {
 //         System.out.println("compare="+p);
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

    
    /**
     * Linear search for the child. Can be overridden
     * if there is a more efficient way of determining the index.
     */
    public static int getIndexOfChild(SceneGraphComponent parent, SceneGraphComponent child)
    {
      final int l = parent.getChildComponentCount();
      for(int i=0; i<l; i++)
        if(parent.getChildComponent(i) == child) return i;
      return -1;
    }
    
    
    public static SceneGraphNode copy(SceneGraphNode template) {
      CopyVisitor cv = new CopyVisitor();
      template.accept(cv);
      return cv.getCopy();
    }
    
	public static int getSignature(SceneGraphPath sgp) {
		EffectiveAppearance eap = EffectiveAppearance.create(sgp);
		int sig = eap.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
		return sig;
	}
}

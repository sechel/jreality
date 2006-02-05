/*
 * Created on 30.06.2004
 *
 * This file is part of the de.jreality.util package.
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

import java.awt.EventQueue;

import de.jreality.scene.*;
import de.jreality.scene.event.*;

/**
 * RenderTrigger is a class for managing render requests for a scene ()or parts of it)
 * One can add subtrees of a scene (SceneGraphComponents ad their children) to be watched
 * for changes. On the other side one can add Viewers on which render() will be called
 * for any chage that is reported.
 * 
 * If one adds only Viewers and no scene it can be used to simply dispatch render requests
 * to several viewers (via the forceRender() method).
 * @version 1.0
 * @author timh
 *
 * TODO: fix problems maybe use Proxy (remove/add doesn't work how it should)
 */
public class RenderTrigger implements SceneGraphComponentListener,
  TransformationListener, AppearanceListener, GeometryListener {

    private RenderTriggerCaster viewer;
    //private HashMap map = new HashMap();
    public RenderTrigger() {
        super();
    }

    public void forceRender() {
        fireRender();
    }
    public void addSceneGraphComponent(SceneGraphComponent c) {
        registerNode(c);
    }
    public void removeSceneGraphComponent(SceneGraphComponent c) {
        unregisterNode(c);
    }
    private void registerNode(SceneGraphNode n) {
        SceneGraphVisitor v =new SceneGraphVisitor() {
            

           
            public void visit(SceneGraphComponent c) {
                    c.childrenAccept(this);
                    c.addSceneGraphComponentListener(RenderTrigger.this);
              }
            
            public void visit(Appearance a) {
                a.addAppearanceListener(RenderTrigger.this);
                super.visit(a);
            }
            public void visit(Geometry g) {
                g.addGeometryListener(RenderTrigger.this);
                super.visit(g);
            }
            public void visit(Transformation t) {
                t.addTransformationListener(RenderTrigger.this);
                super.visit(t);
            }
        };
        n.accept(v);
    }
    private void unregisterNode(SceneGraphNode n) {
        SceneGraphVisitor v =new SceneGraphVisitor() {
            public void visit(SceneGraphComponent c) {
                c.removeSceneGraphComponentListener(RenderTrigger.this);
                c.childrenAccept(this);
          }
            public void visit(Appearance a) {
                a.removeAppearanceListener(RenderTrigger.this);
                super.visit(a);
            }
            public void visit(Geometry g) {
                g.removeGeometryListener(RenderTrigger.this);
                super.visit(g);
            }
            public void visit(Transformation t) {
                t.removeTransformationListener(RenderTrigger.this);
                super.visit(t);
            }
        };
        n.accept(v);
    }
    
    Runnable renderRunnable = new Runnable() {
      public void run() {
        if(viewer!= null)
          viewer.render();
      }
    };
    
    private void fireRender() {
      if (EventQueue.isDispatchThread()) renderRunnable.run();
      else EventQueue.invokeLater(renderRunnable);
    }

    public void addViewer(Viewer v) {
        viewer = RenderTriggerCaster.add(viewer,v);
    }
    
    public void removeViewer(Viewer v) {
        viewer =RenderTriggerCaster.remove(viewer,v);
    }
    
    public void childAdded(SceneGraphComponentEvent ev) {
        registerNode(ev.getNewChildElement());
        fireRender();
    }

    public void childRemoved(SceneGraphComponentEvent ev) {
        unregisterNode(ev.getOldChildElement());
        fireRender();
    }

    public void childReplaced(SceneGraphComponentEvent ev) {
        unregisterNode(ev.getOldChildElement());
        registerNode(ev.getNewChildElement());
        fireRender();
    }

    public void visibilityChanged(SceneGraphComponentEvent ev) {
      fireRender();
    }

    public void transformationMatrixChanged(TransformationEvent ev) {
        fireRender();
    }


    public void appearanceChanged(AppearanceEvent ev) {
        fireRender();
    }

    public void geometryChanged(GeometryEvent ev) {
        fireRender();
    }

    static abstract class RenderTriggerCaster
    {
        abstract RenderTriggerCaster remove(Viewer oldl);
        abstract void render();
        static RenderTriggerCaster add(RenderTriggerCaster a, Viewer b)
        {
          return add(a, new RenderTriggerSingleCaster(b));
        }
        static RenderTriggerCaster add(RenderTriggerCaster a, RenderTriggerCaster b)
        {
            final RenderTriggerCaster result;
            if(a==null) result=b; else if(b==null) result=a;
            else result=new RenderTriggerMulticaster(a, b);
            return result;
        }
        static RenderTriggerCaster remove(RenderTriggerCaster l, Viewer oldl)
        {
            return (l==null)? null: l.remove(oldl);
        }
    }
    static final class RenderTriggerSingleCaster extends RenderTriggerCaster
    {
        final Viewer v;
        RenderTriggerSingleCaster(Viewer viewer)
        {
          v=viewer;
        }
        RenderTriggerCaster remove(Viewer oldl)
        {
          return oldl!=v? this: null;
        }
        void render()
        {
            v.render();
        }
    }
    static final class RenderTriggerMulticaster extends RenderTriggerCaster
    {
        private final RenderTriggerCaster a, b;
        private RenderTriggerMulticaster(RenderTriggerCaster a, RenderTriggerCaster b) {
            this.a = a; this.b = b;
        }
        RenderTriggerCaster remove(Viewer oldl) {
            RenderTriggerCaster a2 = a.remove(oldl);
            RenderTriggerCaster b2 = b.remove(oldl);
            if(a2 == a && b2 == b) return this;
            return add(a2, b2);
        }
        void render()
        {
            a.render(); b.render();
        }
    }
}

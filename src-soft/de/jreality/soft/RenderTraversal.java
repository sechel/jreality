/*
 * Created on Dec 5, 2003
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
package de.jreality.soft;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.plaf.LabelUI;

import de.jreality.backends.label.LabelUtility;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.scene.*;
import de.jreality.scene.data.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TextShader;
import de.jreality.shader.Texture2D;

/**
 * This class traverses a scene graph starting from the given "root" scene
 * graph component. The children of each sgc are visited in the following
 * order: First the appearance is visited then the current transformation
 * is popped to the transformationstack and a copy of it gets multiplied by
 * the transformation of the sgc. This copy is then visited. Then all
 * geometries are visited. 
 * Finally the transformation gets poped from the stack.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class RenderTraversal extends SceneGraphVisitor {
    private static final String FACE_SHADER = /*"faceShader."+*/CommonAttributes.POLYGON_SHADER;
  private boolean shaderUptodate;

  protected Environment     environment = new Environment();

  protected EffectiveAppearance eAppearance;

  double[]  initialTrafo,   currentTrafo;
  private   Transformation  initialTransformation;
  protected LineShader   lineShader;
  protected PolygonPipeline pipeline;
  //protected PolygonShader   pointOutlineShader;
  protected PointShader   pointShader;
  protected PolygonShader   polygonShader;
  protected RenderTraversal reclaimableSubcontext;

  /**
   * 
   */
  public RenderTraversal() {
    super();
    eAppearance=EffectiveAppearance.create();
  }

  protected RenderTraversal(RenderTraversal parentContext) {
    eAppearance=parentContext.eAppearance;
    initializeFromParentContext(parentContext);
  }

  /**
  	 * @return PolygonPipeline
  	 */
  public PolygonPipeline getPipeline() {
    return pipeline;
  }

  /**
     * Sets the pipeline.
     * @param pipeline The pipeline to set
     */
  public void setPipeline(PolygonPipeline pipeline) {
    if (this.pipeline != null)
      this.pipeline.setEnvironment(null);
    this.pipeline=pipeline;
    pipeline.setEnvironment(environment);
  }

  protected void initializeFromParentContext(RenderTraversal parentContext) {
    RenderTraversal p=parentContext;
    environment=p.environment;
    eAppearance=parentContext.eAppearance;
    pipeline=p.pipeline;
    pipeline.setFaceShader(polygonShader=p.polygonShader);
    pipeline.setLineShader(lineShader=p.lineShader);
    pipeline.setPointShader(pointShader=p.pointShader);
	shaderUptodate= p.shaderUptodate;
    //pipeline.setPointOutlineShader(pointOutlineShader=p.pointOutlineShader);
    pipeline.setMatrix(currentTrafo=initialTrafo=parentContext.currentTrafo);
  }

  /**
   * Sets the initialTransformation.
   * @param initialTransformation The initialTransformation to set
   */
  public void setInitialTransformation(Transformation initialTransformation) {
    this.initialTransformation= initialTransformation;
    environment.setInitialTransformation(initialTransformation);
  }

  RenderTraversal subContext() {
    if (reclaimableSubcontext != null) {
      reclaimableSubcontext.initializeFromParentContext(this);
      //TODO is this o.k. ?
	  //reclaimableSubcontext.shaderUptodate = false;
//	  reclaimableSubcontext.shaderUptodate = this.shaderUptodate;
//	  reclaimableSubcontext.pointShader   = this.pointShader;
//	  reclaimableSubcontext.lineShader    = this.lineShader;
//	  reclaimableSubcontext.polygonShader = this.polygonShader;
      return reclaimableSubcontext;
    } else
      return reclaimableSubcontext= new RenderTraversal(this);
  }
  /**
   * This starts the traversal of a SceneGraph starting form root.
   * @param root
   */
  public void traverse(SceneGraphComponent root) {
    environment.removeAll();
    if (initialTrafo == null)
      initialTrafo= new double[16];
    if (initialTransformation != null)
      initialTransformation.getMatrix(initialTrafo);
    else
      VecMat.assignIdentity(initialTrafo);
    currentTrafo= initialTrafo;
    environment.traverse(root);
    visit(root);
    pipeline.setMatrix(initialTrafo);
  }

  public void visit(SceneGraphComponent c) {
    if(c.isVisible())
        c.childrenAccept(subContext());
  }

  public void visit(Transformation t) {
    if (initialTrafo == currentTrafo)
      currentTrafo= new double[16];
    VecMat.copyMatrix(initialTrafo, currentTrafo);
    VecMat.multiplyFromRight(currentTrafo, t.getMatrix());
    pipeline.setMatrix(currentTrafo);
  }

  public void visit(Appearance app) {
    eAppearance = eAppearance.create(app);
    shaderUptodate = false;
  }
  private void setupShader()
  {
    if(!eAppearance.getAttribute("geometryShader", "default").equals("default"))
      Logger.getLogger("de.jreality").warning("unsupported geometry shader");
    String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
    
    if((boolean)eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT)) {
        PolygonShader fs=ShaderLookup
            .getPolygonShaderAttr(eAppearance, geomShaderName, FACE_SHADER);
        pipeline.setFaceShader(this.polygonShader=fs);
    } else {
        pipeline.setFaceShader(this.polygonShader=null);
    }
    if((boolean)eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW), CommonAttributes.EDGE_DRAW_DEFAULT)) {
        LineShader ls=ShaderLookup
       .getLineShaderAttr(eAppearance, geomShaderName, CommonAttributes.LINE_SHADER);;
       pipeline.setLineShader(this.lineShader=ls);
    } else {
        pipeline.setLineShader(this.lineShader=null);
    }
       
    if((boolean)eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.VERTEX_DRAW), CommonAttributes.VERTEX_DRAW_DEFAULT)) {
        PointShader ps=ShaderLookup
            .getPointShaderAttr(eAppearance, geomShaderName, CommonAttributes.POINT_SHADER);;
        pipeline.setPointShader(this.pointShader=ps);
    } else {
        pipeline.setPointShader(this.pointShader=null);
    }
    //pipeline.setPointRadius((double)eAppearance.getAttribute(geomShaderName, .5));
//    pipeline.setPointOutlineShader(this.pointOutlineShader=pos);

//      pipeline.setPointOutlineShader(pointOutlineShader);
//      pipeline.setPointRadius(psa.getPointSize());
//      pipeline.setOutlineFraction(psa.getOutlineFraction());
//      pipeline.setLineWidth(lsa.getLineWidth());
    shaderUptodate = true;
  }

//  public void visit(Geometry g) {
//    System.err.println("Warning: unknown geometry type " + g);
//  }

  public void visit(IndexedLineSet g) {
    if(!shaderUptodate) setupShader();
    DataList dl  = g.getEdgeAttributes(Attribute.INDICES);
    if(lineShader != null&& dl!= null) {
        IntArrayArray edgeIndices=dl.toIntArrayArray();
        DoubleArrayArray vertices=g.getVertexAttributes(Attribute.COORDINATES)
          .toDoubleArrayArray();
        pipeline.startGeometry(g);
        for (int i= 0, n=edgeIndices.size(); i < n; i++)
        {
          IntArray edge=edgeIndices.item(i).toIntArray();
          for(int j = 0; j<edge.getLength()-1;j++) {
              DoubleArray p1=vertices.item(edge.getValueAt(j)).toDoubleArray();
              DoubleArray p2=vertices.item(edge.getValueAt(j+1)).toDoubleArray();
              //pipeline.processLine(p1, p2);
              pipeline.processPseudoTube(p1, p2);
          }
          //int ix1=edge.getValueAt(0), ix2=edge.getValueAt(1);
          //DoubleArray p1=vertices.item(ix1).toDoubleArray();
          //DoubleArray p2=vertices.item(ix2).toDoubleArray();
          //pipeline.processLine(p1, p2);
        }
    }
    visit((PointSet)g);
  }
  
  private int[] fni =new int[Polygon.VERTEX_LENGTH];
  private IntArray fnia =new IntArray(fni);
  
  public void visit(IndexedFaceSet ifs) {
    if(!shaderUptodate) setupShader();

    if(polygonShader != null) {
        DataList indices=ifs.getFaceAttributes(Attribute.INDICES);
        if(indices != null){
        DoubleArrayArray points
          =ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
    	DoubleArrayArray normals =null;
    	DataList vndl =ifs.getVertexAttributes(Attribute.NORMALS);
        if(vndl !=null)
            normals =vndl.toDoubleArrayArray();
      	DataList texCoords = (ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES));
        pipeline.startGeometry(ifs);

        if(normals== null || polygonShader instanceof FlatPolygonShader) {
            DoubleArrayArray faceNormals = null;
            DataList fndl =ifs.getFaceAttributes(Attribute.NORMALS);
            if(fndl !=null) faceNormals = fndl.toDoubleArrayArray();
            for (int i= 0; i < ifs.getNumFaces(); i++) {
                IntArray faceIndices=indices.item(i).toIntArray();
                Arrays.fill(fni,i);
                pipeline.processPolygon(points, faceIndices,
                        faceNormals, fnia,
                        texCoords);
            }
        } else {
            for (int i= 0; i < ifs.getNumFaces(); i++) {
                IntArray faceIndices=indices.item(i).toIntArray();
                pipeline.processPolygon(points, faceIndices,
                        normals, faceIndices,
                        texCoords);
            }
        }
    }
    }
    visit((IndexedLineSet)ifs);
  }

  public void visit(PointSet p) {
    if(!shaderUptodate) setupShader();
    DoubleArrayArray a = null;
    int n= p.getNumPoints();
    if(pointShader!= null) {
        a = p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
        if(a == null) return;
        pipeline.startGeometry(p);
        for (int i= 0; i < n; i++) 
            pipeline.processPoint(a, i);
        
		// Labels
		
		DataList dl = p.getVertexAttributes(Attribute.LABELS);
		if(dl != null) {
			StringArray labels = dl.toStringArray();
			Class shaderType =  (Class) eAppearance.getAttribute(
					ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,"textShader"),DefaultTextShader.class);
			
			DefaultTextShader ts = (DefaultTextShader) AttributeEntityUtility.createAttributeEntity(shaderType, ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,"textShader"), eAppearance);
			Font font = ts.getFont();
			Color c = ts.getDiffuseColor();
			double scale = ts.getScale().doubleValue();
			
			PolygonShader storePS = this.polygonShader;
			PointShader storePtS = this.pointShader;
			LineShader storeLS = this.lineShader;
			DefaultPolygonShader labelShader = new DefaultPolygonShader();
			pipeline.setFaceShader(this.polygonShader=labelShader);
			pipeline.setPointShader(this.pointShader = null);
			pipeline.setLineShader(this.lineShader = null);
			//pipeline.setMatrix(new Matrix().getArray());
				EffectiveAppearance storeEA = eAppearance;
				eAppearance = EffectiveAppearance.create();
			double[] m = new double[16];
			VecMat.invert(currentTrafo,m);

//      BufferedImage[] imgs = LabelUtility.createPointImages(p, font, c);
//      for(int i = 0, max=imgs.length; i<max;i++) {
//        BufferedImage img = imgs[i];

			for(int i = 0; i<labels.getLength();i++) {
				String li = labels.getValueAt(i);
				BufferedImage img = LabelUtility.createImageFromString(li,font,c);
				
				SceneGraphComponent sgc = LabelUtility.sceneGraphForLabel(null,img.getWidth()*scale, img.getHeight()*scale,new double[]{0,0,0},
						m,a.getValueAt(i).toDoubleArray(null));
				labelShader = new DefaultPolygonShader();
				labelShader.texture = new SimpleTexture(new ImageData(img));
				pipeline.setFaceShader(this.polygonShader=labelShader);
				sgc.accept(this);
			}
			pipeline.setFaceShader(this.polygonShader=storePS);
			pipeline.setPointShader(this.pointShader=storePtS);
			pipeline.setLineShader(this.lineShader=storeLS);
			eAppearance = storeEA;
		}
    }
    
//    if(false) {
//        if(a == null) 
//            a = p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
//        DoubleArrayArray normals
//            =p.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray();
//        if(a == null || normals == null) 
//            return;
//        
//        double scale = .2;
//        LineShader l =pipeline.getLineShader();
//        DefaultLineShader ns =new DefaultLineShader();
//        ns.setup(eAppearance,"");
//        pipeline.setLineShader(ns);
//        for (int i= 0; i < n; i++) {
//            DoubleArray pi=a.item(i).toDoubleArray();
//            DoubleArray ni= normals.item(i).toDoubleArray();
//            double[] q = new double[3];
//            q[0] =pi.getValueAt(0) + scale * ni.getValueAt(0);
//            q[1] =pi.getValueAt(1) + scale * ni.getValueAt(1);
//            q[2] =pi.getValueAt(2) + scale * ni.getValueAt(2);
//            DoubleArray qi = new DoubleArray(q);
//            pipeline.processLine(pi,qi);
//        }
//        pipeline.setLineShader(l);
//    }
  }

  
  public void visit(Sphere s) {
    if(!shaderUptodate) setupShader();
    pipeline.startGeometry(s);
    Geometries.unitSphere().apply(pipeline);
  }
  
  public void visit(Cylinder c) {
      if(!shaderUptodate) setupShader();
      pipeline.startGeometry(c);
      Geometries.cylinder().apply(pipeline);
  }

  //
  //
  //TODO: ensure somehow, that the local lights are removed 
  //      after traversing the subtree!
  //
  
  public void visit(DirectionalLight l) {
    super.visit(l);
    if(l.isGlobal()) return; //global lights are already in the environment
    float[] color= l.getColor().getRGBColorComponents(null);
    double[] direction= new double[3];
    //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,1,direction);
    VecMat.transformNormal(currentTrafo, 0, 0, 1, direction);
    VecMat.normalize(direction);
    environment.addDirectionalLight(new de.jreality.soft.DirectionalLightSoft(
      color[0], color[1], color[2], l.getIntensity(), direction));
   
  }

  public void visit(PointLight l) {
      super.visit(l);
      if(l.isGlobal()) return; //global lights are already in the environment
      float[] color= l.getColor().getRGBColorComponents(null);
      double[] direction= new double[3];
      //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,-1,direction);
      VecMat.transformNormal(currentTrafo, 0, 0, -1, direction);
      VecMat.normalize(direction);
      double[] src= new double[3];
      //VecMat.transform(currentTrafo.getMatrix(),0,0,0,src);
      VecMat.transform(currentTrafo, 0, 0, 0, src);
      environment.addSpotLight(new de.jreality.soft.SpotLightSoft(
          color[0], color[1], color[2], l.getIntensity(), direction,
          src, Math.PI, 0,l.getFalloffA0(), l.getFalloffA1(), l.getFalloffA2()));
    }

  
  public void visit(SpotLight l) {
    //super.visit(l);
    if(l.isGlobal()) return; //global lights are already in the environment
    float[] color= l.getColor().getRGBColorComponents(null);
    double[] direction= new double[3];
    //VecMat.transformNormal(currentTrafo.getMatrix(),0,0,-1,direction);
    VecMat.transformNormal(currentTrafo, 0, 0, -1, direction);
    VecMat.normalize(direction);
    double[] src= new double[3];
    //VecMat.transform(currentTrafo.getMatrix(),0,0,0,src);
    VecMat.transform(currentTrafo, 0, 0, 0, src);
    environment.addSpotLight(new de.jreality.soft.SpotLightSoft(
        color[0], color[1], color[2], l.getIntensity(), direction,
        src, l.getConeAngle(), l.getConeDeltaAngle(),l.getFalloffA0(), l.getFalloffA1(), l.getFalloffA2()));
  }
  
}
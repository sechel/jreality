/*
 * Created on 12.01.2004
 */
package de.jreality.examples;

import java.awt.Color;
import java.util.Random;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/**
 * @author Holger
 */
public class TestScenarioBuilder extends SceneBuilder {

  /**
   * 
   */
  public TestScenarioBuilder() {
    super();
  }

  public SceneGraphComponent createFourSpheres() {
      addInCircle(createSphereGeometry(), 4, createBasicAppearance());
    return current();
  }

  public SceneGraphComponent createFourCatenoids() {
    //addWithDistance(createCatenoid(), 6, createSampleAppearance2());
    addInCircle(createCatenoid(), 6, createSampleAppearance());
    return current();
  }
  
  public SceneGraphComponent createVertexColorCatenoid() {
      IndexedFaceSet c = (IndexedFaceSet) createCatenoid();
      addInCircle(c, 0, new Appearance[]{createSampleAppearance()[2]});
      int n = c.getVertexAttributes(Attribute.COORDINATES).size();
      final int colorComp=4, colorArrLen= colorComp*n;
      double[] data = new double[colorArrLen];
      Random r =new Random(1);
      for(int i = 0; i<colorArrLen; i+=colorComp)
          for(int j=0; j<colorComp; j++)
              data[i+j]  =r.nextDouble();
      c.setVertexAttributes(Attribute.COLORS,
        StorageModel.DOUBLE_ARRAY.inlined(colorComp).createReadOnly(data));
      return current();
  }

  public Geometry createCatenoid() {
    CatenoidHelicoid c =new CatenoidHelicoid(20);
    c.setAlpha(Math.PI/2);
    GeometryUtility.calculateAndSetFaceNormals(c); 
    return c;
  }

  private void addWithDistance(Geometry geom, double d, Appearance[] app) {
    Transformation trans=new Transformation();
    trans.setTranslation(d, 0, 0);
    addM(geom, trans, app);
  }

  private void addInCircle(Geometry geom, double r, Appearance[] app) {
    Transformation trans=new Transformation();
	trans.setRotation(Math.PI*2/app.length,0d,1d,0d);
	trans.setTranslation(r, 0, 0);
    addM(geom, trans, app);
    up().translate(-r, 0, 0);
  }

  private void addM(Geometry geom, Transformation t, Appearance[] app) {
    addM(createGeometryNode(createSceneGraphComponent(), geom), t, app);
  }

  private void addM(SceneGraphComponent node, Transformation t, Appearance[] app) {
    final int max=app.length-1;
    if(max==-1) return;
    Transformation local=new Transformation();
    for(int i=0; i<max; i++) {
      add(local).add(app[i]).addChild(node);
      local=new Transformation(local.getMatrix());
      local.multiplyOnRight(t);
    }
    add(local).add(app[max]).addChild(node);
  }

  public Appearance[] createBasicAppearance() {
      Appearance[] app= new Appearance[4];
      Appearance a =new Appearance();
      app[0] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "flat");
      a.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "constant");
      a =new Appearance();
      app[1] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "flat");
      a =new Appearance();
      app[2] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "default");
      a =new Appearance();
      app[3] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
      
      return app;
  }
  public Appearance[] createSampleAppearance() {
      Appearance[] app= new Appearance[4];
      
      // A light blue transparent app with green lines. No vertices.
      Appearance a =new Appearance();
      //silver smooth app .
      a =new Appearance();
      app[0] = a;
      a.setAttribute(CommonAttributes.EDGE_DRAW, false);
      a.setAttribute(CommonAttributes.VERTEX_DRAW, false);
      a.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(.4f,.4f,.45f));
      a.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 1.);
      a.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, .2);
      //a.setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
      a.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(.6f,.1f,.2f));
      a.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.08);
      a.setAttribute("pointShader.coreShader.vertexShader", "constant");
      a.setAttribute("pointShader.outlineShader.diffuseColor", new Color(0.0f, 0.0f, 0.0f));
      a.setAttribute("pointShader.outlineShader.vertexShader", "constant");
      a.setAttribute("pointShader.outlineFraction", 0.3);
      
       // green flat shaded faces.
      a =new Appearance();
      app[1] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "flat");
      a.setAttribute(CommonAttributes.EDGE_DRAW, false);
      a.setAttribute(CommonAttributes.VERTEX_DRAW, false);
      /*
      a.setAttribute(CommonAttributes.EDGE_DRAW, true);
      //a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "flat");
      //a.setAttribute("lineShader.polygonShader.vertexShader", "constant");
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0.2f,.6f,.4f));
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.0);
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "default");
      a.setAttribute(CommonAttributes.LINE_WIDTH, 0.05);
      */
      
      // plain gold  and blue colored twosided smooth shaded app.
      a =new Appearance();
      app[2] = a;
      
      
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "twoSide");
      a.setAttribute(CommonAttributes.EDGE_DRAW, false);
      a.setAttribute(CommonAttributes.VERTEX_DRAW, false);
      
      a.setAttribute(CommonAttributes.POLYGON_SHADER+".front", "implode");
      a.setAttribute(CommonAttributes.POLYGON_SHADER+".front.implodeFactor", -.3);
      a.setAttribute(CommonAttributes.POLYGON_SHADER+".front"+CommonAttributes.DIFFUSE_COLOR, new Color(.8f,.5f,.0f));
      //a.setAttribute(fs+CommonAttributes.TRANSPARENCY, .3);
      
      a.setAttribute(CommonAttributes.POLYGON_SHADER+".back", "default");
    String bs = CommonAttributes.POLYGON_SHADER+".back.";
      a.setAttribute(bs+CommonAttributes.DIFFUSE_COLOR, new Color(.0f,.5f,.8f));
      
      a =new Appearance();
     app[3] = a;
      a.setAttribute(CommonAttributes.POLYGON_SHADER, "default");
      a.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
      a.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.5);
      a.setAttribute(CommonAttributes.EDGE_DRAW, true);
      //a.setAttribute(CommonAttributes.FACE_DRAW, false);
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "default");
      //a.setAttribute("lineShader.polygonShader.vertexShader", "constant");
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0.2f,.6f,.4f));
      a.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.0);
     a.setAttribute(CommonAttributes.VERTEX_DRAW, false);
      a.setAttribute(CommonAttributes.POINT_RADIUS, 0.05);
      a.setAttribute(CommonAttributes.TUBE_RADIUS, 0.05);
      a.setAttribute(CommonAttributes.LINE_WIDTH, 2.0);
      
   
      
      return app;
  }

  protected SceneBuilder createSceneBuilder(
    SceneBuilder parent,
    SceneGraphComponent forChild) {
    return new TestScenarioBuilder(parent, forChild);
  }

  public TestScenarioBuilder(SceneBuilder parent, SceneGraphComponent forChild) {
    super(parent, forChild);
  }

}

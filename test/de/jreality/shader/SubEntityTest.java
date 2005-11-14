package de.jreality.shader;

import java.awt.Color;
import java.util.Arrays;

import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import junit.framework.TestCase;

public class SubEntityTest extends TestCase {

  public void testDefaultGeometryShader() {
    Appearance a = new Appearance();
    DefaultGeometryShader gs = (DefaultGeometryShader) AttributeEntityUtility.createAttributeEntity(DefaultGeometryShader.class, "", a);
    System.out.println(gs);
    gs.setEdgeDraw(false);
    gs.setFaceDraw(true);
    DefaultPolygonShader ps = (DefaultPolygonShader) gs.getPolygonShader();
    System.out.println(ps);
    ps.setAmbientCoefficient(1.2);
    ps.setSmoothShading(false);
    System.out.println(a);
    
    DefaultPointShader dps = (DefaultPointShader)gs.getPointShader();
    TwoSidePolygonShader tps = (TwoSidePolygonShader) dps.createPolygonShader("TwoSidedPolygonShader");
    
    DefaultPolygonShader back = (DefaultPolygonShader) tps.createBack("DefaultPolygonShader");
    DefaultPolygonShader front = (DefaultPolygonShader) tps.createFront("DefaultPolygonShader");
    
    back.setDiffuseColor(Color.BLACK);
    front.setDiffuseColor(Color.WHITE);
    
//    EffectiveAppearance ea = EffectiveAppearance.create().create(a);
//    DefaultGeometryShader gs2 = (DefaultGeometryShader) AttributeEntityUtility.createAttributeEntity(DefaultGeometryShader.class, "", ea);
//    System.out.println(gs2);

    System.out.println(a);
  }
  
}

package de.jreality.shader;

import java.awt.Color;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import junit.framework.TestCase;

public class SubEntityTest extends TestCase {

  public void testDefaultEntities() {
    
    Appearance a = new Appearance();
    EffectiveAppearance ea = EffectiveAppearance.create().create(a);

    assertFalse(AttributeEntityUtility.hasAttributeEntity(DefaultGeometryShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(DefaultGeometryShader.class, "", ea));

    assertFalse(AttributeEntityUtility.hasAttributeEntity(PointShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(PointShader.class, "", ea));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(DefaultPointShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(DefaultPointShader.class, "", ea));

    assertFalse(AttributeEntityUtility.hasAttributeEntity(LineShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(LineShader.class, "", ea));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(DefaultLineShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(DefaultLineShader.class, "", ea));

    assertFalse(AttributeEntityUtility.hasAttributeEntity(PolygonShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(PolygonShader.class, "", ea));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(DefaultPolygonShader.class, "", a));
    assertTrue(AttributeEntityUtility.hasAttributeEntity(DefaultPolygonShader.class, "", ea));
    
    assertFalse(AttributeEntityUtility.hasAttributeEntity(Texture2D.class, "", a));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(Texture2D.class, "", ea));

    assertFalse(AttributeEntityUtility.hasAttributeEntity(CubeMap.class, "", a));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(CubeMap.class, "", ea));

    assertFalse(AttributeEntityUtility.hasAttributeEntity(TwoSidePolygonShader.class, "", a));
    assertFalse(AttributeEntityUtility.hasAttributeEntity(TwoSidePolygonShader.class, "", ea));
  
  }

  public void testDefaultGeometryShader() {
    Appearance a = new Appearance();
    
    DefaultGeometryShader gs = ShaderUtility.createDefaultGeometryShader(a);
    
    gs.setEdgeDraw(false);
    gs.setFaceDraw(true);
    
    DefaultPolygonShader ps = (DefaultPolygonShader) gs.getPolygonShader();
    
    ps.setAmbientCoefficient(1.2);
    ps.setSmoothShading(false);  

    DefaultPointShader dps = (DefaultPointShader)gs.getPointShader();
    
    TwoSidePolygonShader tps = (TwoSidePolygonShader) dps.createPolygonShader("twoSide");
    
    DefaultPolygonShader back = (DefaultPolygonShader) tps.getBack();
    DefaultPolygonShader front = (DefaultPolygonShader) tps.getFront();
    
    back.setDiffuseColor(Color.BLACK);
    front.setDiffuseColor(Color.WHITE);
    
    EffectiveAppearance ea = EffectiveAppearance.create().create(a);
    DefaultGeometryShader gs2 = ShaderUtility.createDefaultGeometryShader(ea);
    System.out.println(gs2);

    System.out.println(a);
  }
  
  public void testDefaultGeometryShaderRead() {
    EffectiveAppearance ea = EffectiveAppearance.create();
    
    DefaultGeometryShader gs = (DefaultGeometryShader) AttributeEntityUtility.createAttributeEntity(DefaultGeometryShader.class, "", ea);
    
    DefaultPolygonShader ps = (DefaultPolygonShader) gs.getPolygonShader();
    DefaultLineShader ls = (DefaultLineShader) gs.getLineShader();
    DefaultPointShader pos = (DefaultPointShader) gs.getPointShader();
    
    System.out.println("\n***********");
    
    System.out.println(ps.getTexture2d());

    System.out.println("\n***********");

    
    System.out.println(gs);
  }
}

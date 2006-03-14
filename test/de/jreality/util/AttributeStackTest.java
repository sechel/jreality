
package de.jreality.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 */
public class AttributeStackTest extends TestCase {

  EffectiveAppearance stack;
  Appearance     app1, app2, app3;

  public AttributeStackTest() {
    super("Test Attribute Stack");
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    stack = EffectiveAppearance.create();
    stack = stack.create(app1 = new Appearance());
    stack = stack.create(app2 = new Appearance());
    stack = stack.create(app3 = new Appearance());
  }

  /*
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
  }

  private void clear(Appearance app) {
    Set l=app.getStoredAttributes();
    for(Iterator i = l.iterator(); i.hasNext(); )
      app.setAttribute((String) i.next(), Appearance.INHERITED);
  }

  /*
   * 
   */
  public void testInheritance()
  {
    app1.setAttribute("hello", "world");
    assertEquals("world", stack.getAttribute("hello", "failed"));
    app1.setAttribute("hello", Appearance.INHERITED);
    assertEquals("others", stack.getAttribute("hello", "others"));
    app1.setAttribute("hello", "overridden");
    app2.setAttribute("hello", Appearance.DEFAULT);
    assertEquals("world", stack.getAttribute("hello", "world"));
    app3.setAttribute("hello", "others");
    assertEquals("others", stack.getAttribute("hello", "others"));
    app3.setAttribute("hello", Appearance.INHERITED);
    assertEquals("universe", stack.getAttribute("hello", "universe"));
  }

  /*
   * 
   */
  public void testNameSpace()
  {
    app3.setAttribute("bla", "test");
    assertEquals("test", stack.getAttribute("bla", "failed"));
    assertEquals("test", stack.getAttribute("foo.bla", "failed"));
    assertEquals("test", stack.getAttribute("foo.bar.bla", "failed"));
    app3.setAttribute("foo.bla", "test2");
    assertEquals("test", stack.getAttribute("bla", "failed"));
    assertEquals("test2", stack.getAttribute("foo.bla", "failed"));
    assertEquals("test2", stack.getAttribute("foo.bar.bla", "failed"));
    app3.setAttribute("foo.bar.bla", "test3");
    assertEquals("test", stack.getAttribute("bla", "failed"));
    assertEquals("test2", stack.getAttribute("foo.bla", "failed"));
    assertEquals("test3", stack.getAttribute("foo.bar.bla", "failed"));
    app3.setAttribute("foo.bla", Appearance.INHERITED);
    assertEquals("test", stack.getAttribute("bla", "failed"));
    assertEquals("test", stack.getAttribute("foo.bla", "failed"));
    assertEquals("test3", stack.getAttribute("foo.bar.bla", "failed"));
    app3.setAttribute("foo.bla", Appearance.DEFAULT);
    assertEquals("test", stack.getAttribute("bla", "failed"));
    assertEquals("test2", stack.getAttribute("foo.bla", "test2"));
    assertEquals("test3", stack.getAttribute("foo.bar.bla", "failed"));
    app3.setAttribute("bla", Appearance.INHERITED);
    assertEquals("test4", stack.getAttribute("bla", "test4"));
    assertEquals("test2", stack.getAttribute("foo.bla", "test2"));
    assertEquals("test3", stack.getAttribute("foo.bar.bla", "failed"));
  }

  /*
   * 
   */
  public void testAttributeTypes()
  {
    app1.setAttribute("test", new Integer(42));
    app2.setAttribute("test", "world");
    app3.setAttribute("test", "");
    assertEquals(new Integer(42), stack.getAttribute("test", new Integer(0)));
    assertEquals(new Double(0.815), stack.getAttribute("test", new Double(0.815)));
    assertEquals("", stack.getAttribute("test", "failed"));
    assertEquals(new Integer(42),
      stack.getAttribute("test", new Double(0.815), Number.class));
  }

  public void testPathEffectiveAttributes() {
    SceneGraphPath path = new SceneGraphPath();

    SceneGraphComponent sgc1 = new SceneGraphComponent();
    sgc1.setAppearance(app1);
    path.push(sgc1);
    
    SceneGraphComponent sgc2 = new SceneGraphComponent();
    sgc1.addChild(sgc2);
    sgc2.setAppearance(app2);
    path.push(sgc2);
    
    SceneGraphComponent sgc3 = new SceneGraphComponent();
    sgc2.addChild(sgc3);
    sgc3.setAppearance(app3);
    path.push(sgc3);
    
    EffectiveAppearance eap = EffectiveAppearance.create(path);
    
    app1.setAttribute("hello", "world");
    assertEquals(eap.getAttribute("hello", "failed"), stack.getAttribute("hello", "failed"));
    app1.setAttribute("hello", Appearance.INHERITED);
    assertEquals(eap.getAttribute("hello", "others"), stack.getAttribute("hello", "others"));
    app1.setAttribute("hello", "overridden");
    app2.setAttribute("hello", Appearance.DEFAULT);
    assertEquals(eap.getAttribute("hello", "world"), stack.getAttribute("hello", "world"));
    app3.setAttribute("hello", "others");
    assertEquals(eap.getAttribute("hello", "others"), stack.getAttribute("hello", "others"));
    app3.setAttribute("hello", Appearance.INHERITED);
    assertEquals(eap.getAttribute("hello", "universe"), stack.getAttribute("hello", "universe"));
    
    assertTrue(EffectiveAppearance.matches(eap, path));
    
    
    SceneGraphPath p2 = path.popNew();
    assertFalse(EffectiveAppearance.matches(eap, p2));
    
    SceneGraphPath longerPath = new SceneGraphPath();
    SceneGraphComponent r = new SceneGraphComponent();
    r.setAppearance(new Appearance());
    longerPath.push(r);
    for (Iterator i = path.iterator(); i.hasNext(); )
      longerPath.push((SceneGraphNode) i.next());
    
    assertFalse(EffectiveAppearance.matches(eap, longerPath));
    
    SceneGraphPath shorterPath = new SceneGraphPath();

    Iterator i = path.iterator();
    for (i.next(); i.hasNext(); )
      shorterPath.push((SceneGraphNode) i.next());

    assertFalse(EffectiveAppearance.matches(eap, shorterPath));
    
    SceneGraphPath emptyPath = new SceneGraphPath();
    assertFalse(EffectiveAppearance.matches(eap, emptyPath));
    
    SceneGraphPath differentPath = new SceneGraphPath();
    differentPath.push(sgc2);
    differentPath.push(sgc1);
    differentPath.push(sgc3);
    
    assertFalse(EffectiveAppearance.matches(eap, differentPath));

    differentPath.push(sgc2);
    differentPath.push(sgc1);
    differentPath.push(sgc3);
   
    assertFalse(EffectiveAppearance.matches(eap, differentPath));
    
  }
  
}

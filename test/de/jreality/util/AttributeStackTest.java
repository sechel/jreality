
package de.jreality.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
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

    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setAppearance(app1);
    path.push(sgc);
    
    sgc.addChild(sgc = new SceneGraphComponent());
    sgc.setAppearance(app2);
    path.push(sgc);
    
    sgc.addChild(sgc = new SceneGraphComponent());
    sgc.setAppearance(app3);
    path.push(sgc);
    
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
  }
  
}

/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.logging.Level;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Secure;

public class ConstructPeerGraphVisitor extends SceneGraphVisitor	{
	SceneGraphComponent myRoot;
	JOGLPeerComponent thePeerRoot, myParent;
	SceneGraphPath sgp;
	boolean topLevel = true;
	JOGLRenderer jr;
	static Class<? extends JOGLPeerComponent> peerClass = JOGLPeerComponent.class;
//	static {
//		try {
//			String foo = Secure.getProperty("jreality.jogl.peerClass");
//			if (foo != null)
//				try {
//					peerClass = (Class<? extends JOGLPeerComponent>) Class.forName(foo);
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		} catch (AccessControlException e) {
//			e.printStackTrace();
//		} catch(SecurityException se)	{
//			LoggingSystem.getLogger(ConstructPeerGraphVisitor.class).warning("Security exception in setting configuration options");
//		}
//	}
	public static void setPeerClass(Class<? extends JOGLPeerComponent> c)	{
		peerClass = c; 
	}
	
	public static void setPeerClass(String name) throws ClassNotFoundException	{
		peerClass = (Class<? extends JOGLPeerComponent>) Class.forName(name); 
	}

	public ConstructPeerGraphVisitor(SceneGraphComponent r, JOGLPeerComponent p, JOGLRenderer jr)	{
		super();
		myRoot = r;
		sgp = new SceneGraphPath();
		myParent = p;
		this.jr = jr;
	}

	private ConstructPeerGraphVisitor(ConstructPeerGraphVisitor pv, JOGLPeerComponent p)	{
		super();
		sgp = (SceneGraphPath) pv.sgp.clone();
		myParent = p;
		topLevel = false;
		this.jr = pv.jr;
	}

	public void visit(SceneGraphComponent c) {
		sgp.push(c);
//		JOGLPeerComponent peer = new JOGLPeerComponent(sgp, myParent, jr);
		JOGLPeerComponent peer = null;
		try {
			try {
				//peer = JOGLConfiguration.getPeerClass().getConstructor(null).newInstance(null);
				peer = peerClass.getConstructor(null).newInstance(null);
//				System.err.println("Got instance of class "+peer.getClass());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch(SecurityException se)	{
			LoggingSystem.getLogger(this).warning("Security exception in setting configuration options");
		}
		peer.init(sgp, myParent, jr);
//		System.err.println("Got sgc of class "+peer.getClass().getName());
//		System.err.println("peerClass is "+JOGLConfiguration.peerClass.getName());
		if (topLevel) thePeerRoot = peer;
		else if (myParent != null) {
			int n = myParent.children.size();
			//String space = (new char[2*sgp.getLength()]).toString();
			myParent.children.add(peer);
			peer.childIndex = n;
		}
		c.childrenAccept(new ConstructPeerGraphVisitor(this, peer));
		sgp.pop();
	}

	public Object visit()	{
		visit(myRoot);
		return thePeerRoot;
	}

}

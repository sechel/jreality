/*
 * Created on Jan 14, 2007
 *
 */
package de.jreality.jogl;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;

public class ConstructPeerGraphVisitor extends SceneGraphVisitor	{
	SceneGraphComponent myRoot;
	JOGLPeerComponent thePeerRoot, myParent;
	SceneGraphPath sgp;
	boolean topLevel = true;
	JOGLRenderer jr;
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
		JOGLPeerComponent peer = new JOGLPeerComponent(sgp, myParent, jr);
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

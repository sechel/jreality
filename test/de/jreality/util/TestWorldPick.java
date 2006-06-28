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

import junit.framework.TestCase;
import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;

/**
 * @author gunn
 *
 */
public class TestWorldPick extends TestCase {

	
	
	public void testWorldPick()	{
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("root");
		SceneGraphComponent world = new SceneGraphComponent();
		world.setName("world1");
		world.setGeometry(new Sphere()); //Primitives.cube()); //
		world.getGeometry().setName("sphere");
		world.setTransformation(new Transformation());
		world.getTransformation().setTranslation(0.0, 0.0, 1.0);
		root.addChild(world);
		world = new SceneGraphComponent();
		world.setName("world2");
		world.setGeometry(Primitives.cube()); //
		world.getGeometry().setName("cube");
		world.setTransformation(new Transformation());
		world.getTransformation().setTranslation(0.0, 0.0, -1.0);
		root.addChild(world);
/*
		charlesgunn.jreality.InteractiveViewer iv = new charlesgunn.jreality.InteractiveViewer(null, root);
		PickAction pa = new PickAction(iv);
		double[] p0 = {2,2,2,1};
		double[] p1 = {-2,-2,-2,1};
		pa.setPickSegment(p0, p1);
		Vector hits = (Vector) pa.visit();
		if (hits != null )	{
			for (int i = 0; i< hits.size(); ++i)	{
				PickPoint thisone = (PickPoint) hits.get(i);
				System.out.println(thisone.toString());				
			}
		}
*/
	}
}

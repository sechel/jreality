package de.jreality.worlds;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.BoundingBoxTraversal;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtilities;

/*
 * wie am Freitag besprochen, sende ich Ihnen anbei Mars-Daten f???r die
3D-Visualisierung, im Einzelnen:

- Digitales Gel???ndemodell (DTM.raw): 100 m Lage- und 1 m H???henaufl???sung
(1315 Spalten x 4500 Zeilen, signed short)
- Bilddaten (Mosaik.jpg): 40 m Lageaufl???sung

Die beiden Dateien beziehen sich auf ein- und dieselbe Kartenprojektion
und zeigen identische Ausschnitte der Mars-Oberfl???che. Das dargestellte
Tal-System hei???t "Magala Valles". Bitte denken sie daran, dass die Daten
unver???ffentlicht sind und ausschlie???lich f???r diesen Test verwendet
werden d???rfen!

F???r evtl. R???ckfragen stehe ich Ihnen noch bis morgen Vormittag bzw. ab
dem 26. Juli wieder zur Verf???gung.

Created on Jul 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MarsFlyby extends AbstractLoadableScene{

	public int getSignature() {
		return Pn.EUCLIDEAN;
	}
	
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("world");
		File f = new File("data/resources/DTM.raw");
		int filesize = (int) f.length();
		System.out.println("size is "+filesize);
		byte[] data = new byte[filesize];
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(f));
			in.readFully(data);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteBuffer bb = ByteBuffer.wrap(data);
		System.out.println("Order: "+bb.order());
		bb.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer sb = bb.asShortBuffer();
		double[][] hfield = new double[4500][1315];
		for (int i = 0; i<4500; ++i)	{
			for (int j = 0; j<1315; ++j)	{
				//hfield[i][j] = .001 * sb.get();
				hfield[i][j] = .0001 * sb.get();
			}
		}
		int nu, nv;
		int step = 3;
		nu = 1315/step - 1;
		nv = 4500/step - 1;
		double[][] subfield = new double[nu*nv][3];
		double[][] texcoords = new double[nu*nv][2];
		double factor = 1.0/(step*step);
		double ufactor = 1.0/(nu-1.0);
		double vfactor = 1.0/(nv-1.0);
		for (int i = 0; i<nv; i++)	{
			int row = step*i;
			for (int j = 0; j<nu; j++)	{
				int column = step * j;
				texcoords[i*nu+j][0] = j * ufactor;
				texcoords[i*nu+j][1] = i * vfactor;
				// Make the coordinate system smaller
				//subfield[i*nu+j][0] = .1 * i;
				//subfield[i*nu+j][1] = .1 * j;
				subfield[i*nu+j][0] = .01 * i;
				subfield[i*nu+j][1] = .01 * j;
				double acc= 0;
				for (int k = 0; k<step; ++k)	
					for (int m = 0; m<step; ++m)	{
						acc += hfield[row+k][column+m];
					}
				subfield[i*nu+j][2] = acc * factor;
			}
		}
		System.out.println("Read completed");
		QuadMeshShape qm = new QuadMeshShape(nu, nv,/* 1125,*/ false, false);
		// TODO fix this when the StorageModel constructor is fixed.
		qm.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(subfield[0].length).createReadOnly(subfield));
		qm.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(texcoords[0].length).createReadOnly(texcoords));

		GeometryUtility.calculateAndSetNormals(qm);
		world.setGeometry(qm);
		BoundingBoxTraversal bbv = new BoundingBoxTraversal();
		bbv.traverse(world);
		Rectangle3D worldBox = bbv.getBoundingBox();
		world.getTransformation().setStretch(10.);
		world.getTransformation().setCenter(worldBox.getCenter());
		Appearance ap1 = world.getAppearance();
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureFile","data/resources/Mosaik-medium.jpg");
		return world;
		
	}

	ConfigurationAttributes config = null;

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}


}

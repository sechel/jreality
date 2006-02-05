/*
 * Author	gunn
 * Created on Nov 8, 2005
 *
 */
package de.jreality.geometry;

import java.awt.geom.Rectangle2D;

// I know the official line is that this should inherit from AbstractQuadMeshFactory but 
// there are just too many methods that I'd have to make public ... 
public class HeightFieldFactory extends QuadMeshFactory {
	Rectangle2D theDomain = new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0);
	boolean domainHasChanged = true;
	public HeightFieldFactory() {
		super();
	}

	public HeightFieldFactory(int signature, int mMaxU, int mMaxV,
			boolean closeU, boolean closeV) {
		super();
		setSignature(signature);
		setULineCount(mMaxU);
		setVLineCount(mMaxV);
		setClosedInUDirection(closeU);
		setClosedInVDirection(closeV);
	}

	public void setRegularDomain(Rectangle2D r)	{
		theDomain=r;
	}
	
	public Rectangle2D getRegularDomain()	{
		return theDomain;
	}
	
	protected void updateImpl() {
		super.updateImpl();

		if( domainHasChanged)
			ifs.setGeometryAttributes(GeometryUtility.REGULAR_DOMAIN_QUAD_MESH_SHAPE, theDomain);
		
		domainHasChanged= false;
	}

	public static double[] getCoordinatesForUV(double[] store, Rectangle2D d, int u, int v, int uc, int vc)	{
		if (store == null) store = new double[2];
		store[0] = d.getMinX()+d.getWidth()*(u/(uc-1.0));
		store[1] = d.getMinY()+d.getHeight()*(v/(vc-1.0));
		return store;
	}
}

/*
 * Author	gunn
 * Created on Nov 8, 2005
 *
 */
package de.jreality.geometry;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

public class RegularDomainQuadMeshFactory extends AbstractQuadMeshFactory {
	Rectangle2D theDomain = new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0);
	boolean domainHasChanged = true;
	public RegularDomainQuadMeshFactory() {
		super();
	}

	public RegularDomainQuadMeshFactory(int signature, int mMaxU, int mMaxV,
			boolean closeU, boolean closeV) {
		super(signature, mMaxU, mMaxV, closeU, closeV);
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

package de.jreality.scene.proxy.scene;

import java.awt.geom.Rectangle2D;

public class Camera extends de.jreality.scene.Camera implements RemoteCamera {

    public void setViewPort(double x, double y, double w, double h) {
        Rectangle2D rec = new Rectangle2D.Double();
        rec.setRect(x, y, w, h);
        setViewPort(rec);
    }

}

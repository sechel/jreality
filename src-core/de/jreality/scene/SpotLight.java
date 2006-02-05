/*
 * Created on Dec 1, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene;

/**
 * This is a spot light.The light direction is the z-axis.
 * Other directions may be obtained by changing the transformation. The falloff is computed by 
 * the inverse of the polynomial with coefficients A0, A1 and A2.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class SpotLight extends PointLight {

    private double coneAngle = Math.PI / 6;
    private double coneDeltaAngle = coneAngle / 3.;
    private double distribution = 2;

    public double getConeAngle() {
      startReader();
      try {
        return coneAngle;
      } finally {
        finishReader();
      }
    }

    /**
     * Sets the coneAngle. This is the maximal illuminated cone.
     * @param coneAngle The coneAngle to set
     */
    public void setConeAngle(double coneAngle) {
      startWriter();
      if (this.coneAngle != coneAngle) fireLightChanged();
      this.coneAngle = coneAngle;
      finishWriter();
    }

    public double getConeDeltaAngle() {
      startReader();
      try {
        return coneDeltaAngle;
      } finally {
        finishReader();
      }
    }

    public double getDistribution() {
      startReader();
      try {
        return distribution;
      } finally {
        finishReader();
      }
    }

    /**
     * Sets the coneDeltaAngle. This angle gives the width of the smooth falloff of the light's intensity towards the 
     * edge of the coneAngle.
     * @param coneDeltaAngle The coneDeltaAngle to set
     */
    public void setConeDeltaAngle(double coneDeltaAngle) {
      startWriter();
      if (this.coneDeltaAngle != coneDeltaAngle) fireLightChanged();
      this.coneDeltaAngle = coneDeltaAngle;
      finishWriter();
    }

    /**
     * Sets the distribution. This is the regular falloff of the lights intensity towards the edge of the cone
     * it is an exponent. 
     * @param distribution The distribution to set
     */
    public void setDistribution(double distribution) {
      startWriter();
      if (this.distribution != distribution) fireLightChanged();
      this.distribution = distribution;
      finishWriter();
    }

    public void accept(SceneGraphVisitor v) {
      v.visit(this);
    }
    static void superAccept(SpotLight l, SceneGraphVisitor v) {
      l.superAccept(v);
    }
    private void superAccept(SceneGraphVisitor v) {
      super.accept(v);
    }

}

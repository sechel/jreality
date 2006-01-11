/*
 * Created on 12.05.2004
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.soft;

import de.jreality.shader.Texture2D;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class SimpleTexture implements Texture {
    byte[] bytes;
    int width;
    int height;

//    double vscale = 1.;
//    double uscale=1.;
    double matrix[];
    protected boolean clampU =false;
    protected boolean clampV =false;
    protected int incr =4;
    private final boolean interpolate;
    
    public SimpleTexture(de.jreality.scene.Texture2D texture) {
        this.bytes = texture.getByteArray();
        this.width=texture.getWidth();
        this.height = texture.getHeight();
        //this.uscale = texture.getSScale();
        //this.vscale = texture.getTScale();
        this.matrix = texture.getTextureMatrix();
        this.clampU = texture.getRepeatS()==de.jreality.scene.Texture2D.CLAMP;
        this.clampV = texture.getRepeatT()==de.jreality.scene.Texture2D.CLAMP;
        incr =3*width*height== bytes.length?3:4;
        interpolate =(texture.getMinFilter()==de.jreality.scene.Texture2D.GL_LINEAR);
    }

    public SimpleTexture(Texture2D texture) {
      this.bytes = (byte[]) texture.getImage().getByteArray().clone();
      this.width=texture.getImage().getWidth();
      this.height = texture.getImage().getHeight();
      //this.uscale = texture.getSScale();
      //this.vscale = texture.getTScale();
      this.matrix = texture.getTextureMatrix().getArray();
      this.clampU = texture.getRepeatS()==Texture2D.CLAMP;
      this.clampV = texture.getRepeatT()==Texture2D.CLAMP;
      incr =3*width*height== bytes.length?3:4;
      interpolate =(texture.getMinFilter()==Texture2D.GL_LINEAR);
  }

    public void  getColor(double u, double v,int x, int y, int color[]) {
        //if(u!= 0)System.out.println(((int)(u*width*uscale+.5))%width);
        //int c = pixels[((int)(u*width*uscale+.5))%width +width*(((int)(v*height*vscale+.5))%height)];
        if(interpolate)
            getPixelInterpolate(u,v,color);
        else
            getPixelNearest(u,v,color);
        
    }

    protected final void getPixelNearest(final double uu, final double vv,  final int[] color) {
        int a,b;
        double u = width*(uu*matrix[0] + vv*matrix[1] + matrix[3]);
        double v = height*(uu*matrix[4+0] + vv*matrix[4+1] + matrix[4+3]);
        u = u<0?u -Math.floor(u/width):u;
        v = v<0?v -Math.floor(v/height):v;

        
        if(clampU) {
            a = (int)(u);
            a =a<0?0:a>=width?width-1:a;
        } else {
            a = ((int)(u))%width;
        }
        if(clampV) {
            b = (int)(v);
            b =b<0?b:b>=height?height-1:b;
        } else {
            b = (((int)(v))%height);
        }
        //c = pixels[a +width*b];
        //c = pixels[((int)(u*width*uscale))%width +width*(((int)(v*height*vscale))%height)];
        int pos =incr *(a +width*b);
        color[0]   = ((255&bytes[pos+0])  *color[0]*NewPolygonRasterizer.COLOR_CH_SCALE)>>NewPolygonRasterizer.FIXP;
        color[1]   = ((255&bytes[pos+1])  *color[1]*NewPolygonRasterizer.COLOR_CH_SCALE)>>NewPolygonRasterizer.FIXP;
        color[2]   = ((255&bytes[pos+2])  *color[2]*NewPolygonRasterizer.COLOR_CH_SCALE)>>NewPolygonRasterizer.FIXP;
//        color[0]  = 255&bytes[pos+0];
//        color[1]  = 255&bytes[pos+1];
//        color[2]  = 255&bytes[pos+2];
        
        if(incr == 4)
            color[3] = (255&bytes[pos+3]*color[3]*NewPolygonRasterizer.COLOR_CH_SCALE)>>NewPolygonRasterizer.FIXP;
//            color[3]  = 255&bytes[pos+3];
        else
            color[3] = (255*color[3]*NewPolygonRasterizer.COLOR_CH_SCALE)>>NewPolygonRasterizer.FIXP;
//            color[3] = 255;
    
        
        //color[0]  =255;
        //color[1]  =255;
        //color[2]  =255;
        //color[3]  =255;
    }
    
    protected final void getPixelInterpolate(final double uu, final double vv,  final int[] color) {
        int ap,am, bp, bm;
        double[] tmpColor =new double[4];
        double dam;
        double dap;
        double dbm;
        double dbp;
        double u = width*(uu*matrix[0] + vv*matrix[1] + matrix[3]);
        double v = height*(uu*matrix[4+0] + vv*matrix[4+1] + matrix[4+3]);
        u = u<0?u -Math.floor(u/width):u;
        v = v<0?v -Math.floor(v/height):v;
        dam = (u);
        am  = (int)dam;
        dam = 1 - (dam-am);

        dap = (u + 1);
        ap  = (int)dap;
        dap = 1 - dam;
        
        if(clampU) {
            am =am<0?am:am>=width?width-1:am;
            
            ap =ap<0?ap:ap>=width?width-1:ap;
        } else {
            am = (am)%width;
            ap = (ap)%width;
        }
        
        dbm = (v);
        bm  = (int)dbm;
        dbm = 1 - (dbm - bm);

        dbp = (v + 1);
        bp  = (int)dbp;
        dbp = 1- dbm;
        
        if(clampV) {
            bm =bm<0?bm:bm>=height?height-1:bm;
            
            bp =bp<0?bp:bp>=height?height-1:bp;
        } else {
            bm = (bm)%height;
            bp = (bp)%height;
        }
        
        
        int pos =incr *(am +width*bm);
        double fac=dam*dbm;
        tmpColor[0]  += fac*(255&bytes[pos+0]);
        tmpColor[1]  += fac*(255&bytes[pos+1]);
        tmpColor[2]  += fac*(255&bytes[pos+2]);
        if(incr == 4)
            tmpColor[3]  += fac*(255&bytes[pos+3]);
        
        pos =incr *(ap +width*bm);
        fac=dap*dbm;
        tmpColor[0]  += fac*(255&bytes[pos+0]);
        tmpColor[1]  += fac*(255&bytes[pos+1]);
        tmpColor[2]  += fac*(255&bytes[pos+2]);
        if(incr == 4)
            tmpColor[3]  += fac*(255&bytes[pos+3]);

        pos =incr *(ap +width*bp);
        fac=dap*dbp;
        tmpColor[0]  += fac*(255&bytes[pos+0]);
        tmpColor[1]  += fac*(255&bytes[pos+1]);
        tmpColor[2]  += fac*(255&bytes[pos+2]);
        if(incr == 4)
            tmpColor[3]  += fac*(255&bytes[pos+3]);
        
        pos =incr *(am +width*bp);
        fac=dam*dbp;
        tmpColor[0]  += fac*(255&bytes[pos+0]);
        tmpColor[1]  += fac*(255&bytes[pos+1]);
        tmpColor[2]  += fac*(255&bytes[pos+2]);
        if(incr == 4)
            tmpColor[3]  += fac*(255&bytes[pos+3]);
        
        
        
        color[0] =255&((int)tmpColor[0]);
        color[1] =255&((int)tmpColor[1]);
        color[2] =255&((int)tmpColor[2]);
        if(incr == 4)
            color[3] =255&((int)tmpColor[3]);
        
        else
            color[3] = 255;
        
    }

    /*
     * We might need this for MIP MAPPING...
     *  
        final static  int LogTable256[] = 
        {
            0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };

    public static final int findLog(int v) { // 32-bit word to find the log of
        int c = 0; // c will be lg(v)
        int t, tt; // temporaries

        if ((tt = v >> 16) !=0) {
            c = ((t = v >> 24)!=0) ? 24 + LogTable256[t] : 16 + LogTable256[tt & 0xFF];
        }
        else {
            c = ((t = v & 0xFF00)!=0) ? 8 + LogTable256[t >> 8] : LogTable256[v & 0xFF];
        }
        return c;
    }
    
    static final int b[] = {0x2, 0xC, 0xF0, 0xFF00, 0xFFFF0000};
    static final int S[] = {1, 2, 4, 8, 16};
    public static final int findLog2(int v){

        int c = 0; // result of log2(v) will go here
        for (int i = 4; i >= 0; i--) // unroll for speed...
        {
            if ((v & b[i])!=0)
            {
                v >>= S[i];
                c |= S[i];
            } 
        }
        return c;
    }

     */
}

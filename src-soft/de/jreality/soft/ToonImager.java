/*
 * Created on 09.06.2005
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

/**
 * 
 * @version 1.0
 * @author timh
 *
 */
public class ToonImager extends Imager {
//    private final int BLACK = 255<<24;
//  private final int WHITE = (255<<24)+(255<<16)+(255<<8)+(255);
//    private final int EDGE = 300000000;
//    private final int BLACK = (255<<24)+(255<<16)+(255<<8)+(255);
//  private final int WHITE = (255<<24)+(255<<16)+(255<<8)+(255);
    private final int BLACK = 255<<24;
    private final int WHITE = 255<<24;
    private final int EDGE = 300000;
    private final int COL = 600;
    public ToonImager() {
        super();
    }
    public void process(int[] pixels, int[] zBuf, int w, int h) {
        for(int i = 1;i< w-2;i++)
            for(int j = 1; j<h-2;j++) {
                int pos = i + w*j;
                if((zBuf[pos]-zBuf[pos+1]) > EDGE) {
                   // pixels[pos-1] = WHITE;
                    pixels[pos] = WHITE;
                    pixels[pos+1] = BLACK;
                    //pixels[pos+2] = BLACK;
                } else
                if(-(zBuf[pos]-zBuf[pos+1]) > EDGE) {
                    //pixels[pos-1] = BLACK;
                    pixels[pos] = BLACK;
                    pixels[pos+1] = WHITE;
                    //pixels[pos+2] = WHITE;
                }
                if((zBuf[pos]-zBuf[pos+w]) > EDGE) {
                    //pixels[pos-w] = WHITE;
                    pixels[pos] = WHITE;
                    pixels[pos+w] = BLACK;
                    //pixels[pos+2*w] = BLACK;
                }else
                if(-(zBuf[pos]-zBuf[pos+w]) > EDGE) {
                    //pixels[pos-w] = BLACK;
                    pixels[pos] = BLACK;
                    pixels[pos+w] = WHITE;
                    //pixels[pos+2*w] = WHITE;
                } else
                    if((value(zBuf[pos])-value(zBuf[pos+1])) > COL) {
                        // pixels[pos-1] = WHITE;
                         pixels[pos] = WHITE;
                         pixels[pos+1] = BLACK;
                         //pixels[pos+2] = BLACK;
                     } else
                     if(-(value(zBuf[pos])-value(zBuf[pos+1])) > COL) {
                         //pixels[pos-1] = BLACK;
                         pixels[pos] = BLACK;
                         pixels[pos+1] = WHITE;
                         //pixels[pos+2] = WHITE;
                     }
                     if((value(zBuf[pos])-value(zBuf[pos+w])) > COL) {
                         //pixels[pos-w] = WHITE;
                         pixels[pos] = WHITE;
                         pixels[pos+w] = BLACK;
                         //pixels[pos+2*w] = BLACK;
                     }else
                     if(-(value(zBuf[pos])-value(zBuf[pos+w])) > COL) {
                         //pixels[pos-w] = BLACK;
                         pixels[pos] = BLACK;
                         pixels[pos+w] = WHITE;
                         //pixels[pos+2*w] = WHITE;
                     } else {
                    
                int p = pixels[pos];
                int a = (p& 0xff000000);
                int r = 255 &(p>>16);
                int g = 255 &(p>>8);
                int b = 255 &(p);
                int d = (r+g+b);
                if(d>250) {
                    r = g = b = 255;
                } else 
                if(d>50) {
                    r = g = b = 150;
                } else 
                    r = g=b=0;
                    
                pixels[pos]= (a) |
                ((r) <<16) |
                ((g) << 8)|
                ((b)  );

                
                }
                
//                int value  = (((pixels[pos]>>16)&255) + ((pixels[pos]>>8)&255) + ((pixels[pos])&255))/3;
//
//                if(value>80) {
//                    pixels[pos] = WHITE;
//                    continue;
//                }
//                
//                int m = value >20? 4:(value>5? 2:1);
//                int iu = ((int) ((i+j)/4))%m;
//                //int iu = ((int) ((v/scaleV)+(u/scaleU)))%m;
//                if(iu ==0 ) {
//                    pixels[pos] = BLACK;
//                } else {
//                    pixels[pos] = WHITE;
//                }


            }

    }

    private static final int value(final int p) {
        return (( 255 &(p>>16)) + (255 &(p>>8)) + (255 &(p)))/3;
        
    }
    /*
    void disabled() {
        if (toonShading) {
          for (int i= 1; i < w - 1; i++)
            for (int j= 1; j < h - 1; j++) {
              int pos= i + w * j;
              int ppos= 3 * (pos--);
              int zm= zBuffer[pos++];
              int z= zBuffer[pos++];
              int zp= zBuffer[pos--];

              double dzm= Math.abs((double)z - zm);
              double dzp= Math.abs((double)zp - z);

              if (dzp > 1.5 * dzm || dzm > 1.5 * dzp) {
                makeBlack(pos);
              } else if (
                edgeDetect(3 * (pos), 3 * (pos + 1))
                  || edgeDetect(3 * (pos), 3 * (pos + w)))
                makeBlack(pos);
              else
                makeWhiter(pos);

            }
        }
        if (blur) {
          for (int i= 0; i < w - 1; i++)
            for (int j= 0; j < h - 1; j++) {
              //                                    int pos = i+w*j;
              //                                    if(edgeDetect(3*(pos),3*(pos+1)\)||edgeDetect(3*(pos),3*(pos+w)))
              blurPixel(i + w * j);
            }
        }

      }
    private final void blurPixel(final int p) {
        int pp= 3 * p;
        int ppr= pp + 3;
        int ppd= 3 * (p + w);
        int ppld= ppd + 3;

        int dr=
              (255 & pixelsR[pp++])
            + (255 & pixelsR[ppr++])
            + (255 & pixelsR[ppd++])
            + (255 & pixelsR[ppld++]);
        int dg=
              (255 & pixelsR[pp++])
            + (255 & pixelsR[ppr++])
            + (255 & pixelsR[ppd++])
            + (255 & pixelsR[ppld++]);
        int db=
              (255 & pixelsR[pp])
            + (255 & pixelsR[ppr])
            + (255 & pixelsR[ppd])
            + (255 & pixelsR[ppld]);

        dr /= 4;
        dg /= 4;
        db /= 4;
        pp= 3 * (p);

        pixelsR[pp++]= (byte) (dr);
        pixelsR[pp++]= (byte) (dg);
        pixelsR[pp]= (byte) (db);
      }

      private final void makeBlack(final int pos) {
        int ppos= 3 * pos;
        pixelsR[ppos++]= pixelsR[ppos++]= pixelsR[ppos]= 0;
      }
      private final void makeWhiter(final int pos) {
        int ppos= 3 * pos;
        pixelsR[ppos]= (byte) (((254 & pixelsR[ppos++]) >> 1) + 0x7f);
        pixelsR[ppos]= (byte) (((254 & pixelsR[ppos++]) >> 1) + 0x7f);
        pixelsR[ppos]= (byte) (((254 & pixelsR[ppos]) >> 1) + 0x7f);
      }
      private final boolean edgeDetect(int p, int q) {

          int dr= pixelsR[p++] - pixelsR[q++];
          int dg= pixelsR[p++] - pixelsR[q++];
          int db= pixelsR[p] - pixelsR[q];

          //          return (dr*dr + dg*dg+db*db) > 255;
          dr *= dr;
          dg *= dg;
          db *= db;
          int r= (dr > dg ? dr : dg);
          r= r > db ? r : db;
          return r > 255;

        }
*/
    
}

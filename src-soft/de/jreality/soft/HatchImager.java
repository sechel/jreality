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
public class HatchImager extends Imager {
    private final int BLACK = 255<<24;
    private final int WHITE = (255<<24)+(255<<16)+(255<<8)+(255);
    private final int EDGE = 300000000;
    public HatchImager() {
        super();
    }
    public void process(int[] pixels, int[] zBuf, int w, int h) {
        for(int i = 1;i< w-2;i++)
            for(int j = 1; j<h-2;j++) {
                int pos = i + w*j;
                if((zBuf[pos]-zBuf[pos+1]) > EDGE) {
                    pixels[pos-1] = WHITE;
                    pixels[pos] = WHITE;
                    pixels[pos+1] = BLACK;
                    pixels[pos+2] = BLACK;
                } else
                if(-(zBuf[pos-1]-zBuf[pos+1]) > EDGE) {
                    pixels[pos-1] = BLACK;
                    pixels[pos] = BLACK;
                    pixels[pos+1] = WHITE;
                    pixels[pos+2] = WHITE;
                }
                if((zBuf[pos]-zBuf[pos+w]) > EDGE) {
                    pixels[pos-w] = WHITE;
                    pixels[pos] = WHITE;
                    pixels[pos+w] = BLACK;
                    pixels[pos+2*w] = BLACK;
                }else
                if(-(zBuf[pos]-zBuf[pos+w]) > EDGE) {
                    pixels[pos-w] = BLACK;
                    pixels[pos] = BLACK;
                    pixels[pos+w] = WHITE;
                    pixels[pos+2*w] = WHITE;
                }
                
                int value  = (((pixels[pos]>>16)&255) + ((pixels[pos]>>8)&255) + ((pixels[pos])&255))/3;

                if(value>80) {
                    pixels[pos] = WHITE;
                    continue;
                }
                
                int m = value >20? 4:(value>5? 2:1);
                int iu = ((int) ((i+j)/4))%m;
                //int iu = ((int) ((v/scaleV)+(u/scaleU)))%m;
                if(iu ==0 ) {
                    pixels[pos] = BLACK;
                } else {
                    pixels[pos] = WHITE;
                }

                
            }

    }

}

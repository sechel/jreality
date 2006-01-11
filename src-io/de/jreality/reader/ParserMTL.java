/*
 * Created on 28-Feb-2005
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
package de.jreality.reader;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.*;


/**
 *
 * simple parser for mtl file format.
 * returns an appearance-array with attributes set from the given .mtl file.
 *
 * @author weissman
 *
 */
public class ParserMTL {
    
    public static Appearance defaultApp = createDefault();
    
    static Appearance createDefault() {
        Appearance ret= new Appearance();
        /*
        ret.setName("default");
        ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                +CommonAttributes.AMBIENT_COLOR,
                new Color(0.2f, 0.2f, 0.2f));
        ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                +CommonAttributes.DIFFUSE_COLOR,
                new Color(0.8f, 0.8f, 0.8f));
        ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                +CommonAttributes.SPECULAR_COLOR,
                Color.blue);
        ret.setAttribute(CommonAttributes.EDGE_DRAW, false);
        */
        return ret;
    }
    
    Input input;
    
    public ParserMTL(Input input) {
        this.input = input;
    }

    public static List readAppearences(Input mtlInput) throws IOException {
        return new ParserMTL(mtlInput).load(); 
    }
     
    public static Appearance getDefault() {
        return defaultApp;
    }

    public List load() {
        Reader r = input.getReader();
        SceneGraphComponent disk=new SceneGraphComponent();
        StreamTokenizer st = new StreamTokenizer(r);
        Vector ret = new Vector();
     try {
         while (st.ttype != StreamTokenizer.TT_EOF)
             ret.add(loadCurrent(st));
     } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
        return ret;
    }
    
    private StreamTokenizer globalSyntax(StreamTokenizer st) {
        st.resetSyntax();
        st.eolIsSignificant(true);
        st.wordChars('0', '9');
        st.wordChars('A', 'Z');
        st.wordChars('a', 'z');
        st.wordChars('_', '_');
        st.wordChars('.','.');
        st.wordChars('-','-');
        st.wordChars('+','+');
        st.wordChars('\u00A0', '\u00FF' );
        st.whitespaceChars('\u0000',  '\u0020');
        st.commentChar('#');
        st.ordinaryChar('/');
        st.parseNumbers();
        return st;
    }
    
    private Appearance loadCurrent(StreamTokenizer st) throws IOException {
        Appearance ret = createDefault();
        globalSyntax(st);
        boolean started = false;
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            if (st.ttype == StreamTokenizer.TT_WORD)  {
                String word = st.sval;
                if (word.equalsIgnoreCase("newmtl")) {
                    if (started) {
                        st.pushBack();
                        return ret;
                    }
                    setName(st, ret);
                    started = true;
                    continue;
                }
                if (word.equalsIgnoreCase("Ka")) { 
                    ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                            +CommonAttributes.AMBIENT_COLOR,
                            readColor(st));
                    continue;
                }
                if (word.equalsIgnoreCase("Kd")) { 
                    ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                            +CommonAttributes.DIFFUSE_COLOR,
                            readColor(st));
                    continue;
                }
                if (word.equalsIgnoreCase("Ks")) { 
                    ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                            +CommonAttributes.SPECULAR_COLOR,
                            readColor(st));
                    continue;
                }
                if (word.equalsIgnoreCase("d")) {
                    st.nextToken();
                    double val = st.nval;
                    ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                            +CommonAttributes.TRANSPARENCY, 1-val);
                    if (val < 1.) {
                        ret.setAttribute(CommonAttributes.POLYGON_SHADER+"."
                                +CommonAttributes.TRANSPARENCY_ENABLED, true);
                    }
                    while (st.nextToken() != StreamTokenizer.TT_EOL) ;
                    continue;
                }
                if (word.equalsIgnoreCase("Ns")) { 
                    ignoreToken(st);
                    continue;
                }
                if (word.equalsIgnoreCase("illum")) { // ascii dump of rgb values
                    ignoreToken(st);
                    continue;
                }
                if (word.equalsIgnoreCase("sharpness")) { // ascii dump of rgb values
                    ignoreToken(st);
                    continue;
                }
                if (word.equalsIgnoreCase("bump")) { // ascii dump of rgb values
                    ignoreToken(st);
                    continue;
                }
                if (word.equalsIgnoreCase("map_Ka")) { 
                    setTextureMap(st, ret);
                    continue;
                }
                if (word.equalsIgnoreCase("map_Kd")) { 
                    setTextureMap(st, ret);
                    continue;
                }
                if (word.equalsIgnoreCase("map_Ks")) { 
                    setTextureMap(st, ret);
                    continue;
                }
                if (word.equalsIgnoreCase("map_Ns")) { 
                    ignoreToken(st);
                    continue;
                }
                LoggingSystem.getLogger(this).fine("unknown tag: "+word);
                while (st.nextToken() != StreamTokenizer.TT_EOL) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) 
                    	LoggingSystem.getLogger(this).fine("["+st.nval+","+st.sval+","+st.ttype+"]");
                }
                LoggingSystem.getLogger(this).fine("unknown tag: "+word+" end");
            }
        }
        return ret;
    }

//    /**
//     * @param st
//     * @throws IOException
//     */
//    private void setShininess(StreamTokenizer st, Appearance ret) throws IOException {
//        System.out.println("MTLReader.setShininess()");
//        while (st.nextToken() != StreamTokenizer.TT_EOL) ;
//        
//    }

    private void setTextureMap(StreamTokenizer st, Appearance ret) throws IOException {
        String texFile = readString(st);
        try {
            Texture2D tex = TextureUtility.createTexture(ret, CommonAttributes.POLYGON_SHADER, input.resolveInput(texFile), false);
        } catch (FileNotFoundException e) {
        	LoggingSystem.getLogger(this).warning("couldn't find "+texFile);
        } catch (IOException e) {
            LoggingSystem.getLogger(this).warning("read error "+texFile);
        }
    }
 
    private void ignoreToken(StreamTokenizer st) throws IOException {
        while (st.nextToken() != StreamTokenizer.TT_EOL);
    }
    
    private void setName(StreamTokenizer st, Appearance ret) throws IOException {
        ret.setName(readString(st));
    }
        
    private String readString(StreamTokenizer st) throws IOException {
        String ret = "";
        while (st.nextToken() != StreamTokenizer.TT_EOL) {
            ret += st.sval;
        }
        return ret;
    }

    private Color readColor(StreamTokenizer st) throws IOException {
        double[] coords = new double[3];
        int ix = 0;
        while (st.nextToken() != StreamTokenizer.TT_EOL) {
            if (st.ttype != StreamTokenizer.TT_NUMBER) {
            	LoggingSystem.getLogger(this).fine("color ignoring token: "+st.sval);
                continue;
            }
            if (ix > 2) {
            	LoggingSystem.getLogger(this).fine("ignoring "+(ix+1)+"th color coord.");
                continue;
            }
            coords[ix++] = st.nval;
        }
        if (ix < 2) LoggingSystem.getLogger(this).fine("Warning: only "+ix+" color vals read.");
        Color ret = new Color((float)coords[0], (float)coords[1], (float)coords[2]);
        return ret;
    }

 }


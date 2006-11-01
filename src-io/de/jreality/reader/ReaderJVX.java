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


package de.jreality.reader;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;


/**
 *
 * Simple reader for JVX files (JavaView format).
 *
 * @author timh
 *
 */
public class ReaderJVX extends AbstractReader {

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    parserFactory.setValidating(false);
    try {
        SAXParser parser = parserFactory.newSAXParser();
        Handler handler = new Handler();
        parser.parse(input.getInputStream(), handler);
        root = handler.getRoot();
    } catch (ParserConfigurationException e) {
        IOException ie = new IOException(e.getMessage());
        ie.initCause(e);
        throw ie;
    } catch (SAXException e) {
      IOException ie = new IOException(e.getMessage());
      ie.initCause(e);
      throw ie;
    }
  }
  
  static class Handler extends DefaultHandler {
    SceneGraphComponent root = new SceneGraphComponent();
    Stack componentStack = new Stack();
    SceneGraphComponent currentComponent;
    DataListSet vertexAttributes;
    DataListSet edgeAttributes;
    DataListSet faceAttributes;
    double[][] currentPoints;
    double[] currentPoint;
    int currentPointNum;
    int[][] currentLines;
    int[] currentLine;
    int[][] currentFaces;
    private IndexedFaceSet currentIndexedFaceSet;
    private int pointLength;
    // TODO find out wether this is allways true:
    private int lineLength = 2;
    private int currentLineNum;
    private double[][] currentColors;
    private String[] currentPointLabels;
    private int currentColorNum;
    
    public Handler() {
        
    }
    public SceneGraphComponent getRoot() {
        return root;
    }
    
    public void endDocument() throws SAXException {
        if(componentStack.size()!= 0) throw new RuntimeException(" nonempty stack at end of jvx file.");
        super.endDocument();
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    	charactersImpl();
        LoggingSystem.getLogger(this).fine("end elem: qName "+qName);
        if(qName.equals("geometry")) {
            if(currentPoints != null) {
                currentIndexedFaceSet.setVertexCountAndAttributes(vertexAttributes);
                LoggingSystem.getLogger(this).fine("added coordinates");
            }
            if(currentLines != null)
                //currentIndexedFaceSet.setEdgeCountAndAttributes(edgeAttributes);
            
            currentIndexedFaceSet = null;
            currentComponent = (SceneGraphComponent) componentStack.pop();
            return;
        }
        //
        // a pointSet
        //
        if(qName.equals("pointSet")) {
            pointLength= -1;
            vertexAttributes = new DataListSet(currentPoints.length);
            //vertexAttributes.add(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(currentPoints));
            vertexAttributes.addWritable(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY_ARRAY,currentPoints);
            vertexAttributes.addWritable(Attribute.LABELS, StorageModel.STRING_ARRAY, currentPointLabels);
            if(currentColors!= null) {
                vertexAttributes.addWritable(Attribute.COLORS,StorageModel.DOUBLE3_ARRAY, currentColors);
                currentColors = null;
            }
            return;
        }
        //
        // points
        //
        if(qName.equals("points")) {
            //currentPoints = null;
            currentPointNum = -1;
            return;
        }
        //
        // p
        //
        if(qName.equals("p")) {
            currentPoint = null;
            currentPointNum++;
            return;
        }
        //
        // lineSet
        //
        if(qName.equals("lineSet")) {
            //pointLength= -1;
//            edgeAttributes = new DataListSet(currentPoints.length);
//            //edgeAttributes.add(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(currentLines));
//            edgeAttributes.addWritable(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY,currentLines);
//            if(currentColors!= null) {
//                //vertexAttributes.addWritable(Attribute.COLORS,StorageModel.primitive(Color.class),currentColors);
//                currentColors = null;
//            }
            //TODO the above does not work! probably some bug in the DataList stuff...
            currentIndexedFaceSet.setEdgeCountAndAttributes(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(currentLines));
            if(currentColors!= null) {
                LoggingSystem.getLogger(this).finest("pre  dl");
                DataList dl = StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(currentColors);
                LoggingSystem.getLogger(this).finest("dl done");
                currentIndexedFaceSet.setEdgeAttributes(Attribute.COLORS,dl);
                currentColors = null;
            }
            return;
        }
        //
        // lines
        //
        if(qName.equals("lines")) {
            //currentLines = null;
            currentLineNum = -1;
            return;
        }
        //
        // l
        //
        if(qName.equals("l")) {
            currentLine = null;
            currentLineNum++;
            return;
        }
        //
        // colors
        //
        if(qName.equals("colors")) {
            //currentLines = null;
            currentColorNum = -1;
            return;
        }
        //
        // c
        //
        if(qName.equals("c")) {
            currentColorNum++;
            return;
        }
        
        
        super.endElement(uri, localName, qName);
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        currentComponent = root;
        super.startDocument();
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        LoggingSystem.getLogger(this).fine("start elem: qName "+qName);
        //
        // a geometry
        //
        if(qName.equals("geometry")) {
            currentIndexedFaceSet = new IndexedFaceSet();
            currentIndexedFaceSet.setFaceCountAndAttributes(new DataListSet(0));
            SceneGraphComponent c = new SceneGraphComponent();
            Appearance ap = new Appearance();
            c.setAppearance(ap);
            if(currentComponent!= null) currentComponent.addChild(c);
            componentStack.push(currentComponent);
            currentComponent = c;
            c.setGeometry(currentIndexedFaceSet);
            return;
        }
        //
        // a pointSet
        //
        if(qName.equals("pointSet")) {
            String point = attributes.getValue("point");
            if(point!= null && point.equals("show")) 
                currentComponent.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, "true");
            else
                currentComponent.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, "false");
            pointLength= Integer.parseInt(attributes.getValue("dim"));
            return;
        }
        //
        // points
        //
        if(qName.equals("points")) {
            int pointNum= Integer.parseInt(attributes.getValue("num"));
            currentPoints = new double[pointNum][];
            currentPointLabels = new String[pointNum];
            currentPointNum = 0;
            return;
        }
        //
        // p
        //
        if(qName.equals("p")) {
        	if (currentPointNum == -1 || pointLength == -1) return;
            currentPointLabels[currentPointNum]=attributes.getValue("name");
            currentPoints[currentPointNum] = currentPoint = new double[pointLength];
            return;
        }
        //
        // a lineSet
        //
        if(qName.equals("lineSet")) {
            String point = attributes.getValue("line");
            if(point!= null && point.equals("show")) 
                currentComponent.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, "true");
            else 
                currentComponent.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, "false");
            return;
        }
        //
        // lines
        //
        if(qName.equals("lines")) {
            int lineNum= Integer.parseInt(attributes.getValue("num"));
            currentLines = new int[lineNum][];
            currentLineNum = 0;
            return;
        }
        //
        // l
        //
        if(qName.equals("l")) {
            String name = attributes.getValue("name");
            currentLines[currentLineNum] = currentLine = new int[lineLength];
            return;
        }
        //
        // colors
        //
        if(qName.equals("colors")) {
            int colorNum= Integer.parseInt(attributes.getValue("num"));
            currentColors = new double[colorNum][3];
            currentColorNum = 0;
            return;
        }
        //
        // c
        //
        if(qName.equals("c")) {
            
            return;
        }
        super.startElement(uri, localName, qName, attributes);
    }

    
    
    LinkedList<char[]> chars = new LinkedList<char[]>();
    public void characters(char[] ch, int start, int length) throws SAXException {
		// QUICK FIX: sax parser sometimes splits char content into two calls of characters(..)
    	if (currentColors != null || currentFaces != null || currentPoints != null) {
	    	char[] read = new char[length];
	    	System.arraycopy(ch, start, read, 0, length);
	    	chars.add(read);
    	} else super.characters(ch, start, length);
    }

    public void charactersImpl() {
    	if (chars.isEmpty()) return;
    	char[] ch;
    	if (chars.size() > 1) {
    		int len=0;
    		for (int i=0; i < chars.size(); i++) {
    			len+=chars.get(i).length;
    		}
    		ch=new char[len];
    		for (int i=0, s=0; i < chars.size(); i++) {
    			char[] cs = chars.get(i);
				System.arraycopy(cs, 0, ch, s, cs.length);
    			s+=cs.length;
    		}
    	} else {
    		ch=chars.get(0);
    	}
    	chars.clear();
    	int start=0, length=ch.length;
        if(currentPoint != null) {
            // read point coords...
            String s = String.valueOf(ch,start,length);
            String[] nums = s.split("\\s");
            for(int i = 0; i<nums.length;i++) {
                currentPoint[i] = Double.parseDouble(nums[i]);
            }
            return;
        }
        if(currentLine != null) {
            // read lineIndices...
            String s = String.valueOf(ch,start,length);
            String[] nums = s.split("\\s");
            for(int i = 0; i<nums.length;i++) {
                currentLine[i] = Integer.parseInt(nums[i]);
            }
            return;
        }
        if(currentColors != null) {
            // read Colors...
            String s = String.valueOf(ch,start,length);
            String[] nums = s.split("\\s");
            int r = Integer.parseInt(nums[0]);
            int g = Integer.parseInt(nums[1]);
            int b = Integer.parseInt(nums[2]);
            currentColors[currentColorNum][0] = r/255.;
            currentColors[currentColorNum][1] = g/255.;
            currentColors[currentColorNum][2] = b/255.;
            return;
        }
    }
}

}

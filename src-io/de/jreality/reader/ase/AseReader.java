/*
 * Copyright (c) 2000,2001 David Yazel, Teseract Software, LLP
 * Copyright (c) 2003-2004, Xith3D Project Group
 * All rights reserved.
 *
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the 'Xith3D Project Group' nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 *
 */
package de.jreality.reader.ase;



import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;


/**
 * Extends LineNumberReader to read and parse a single line into pieces for
 * easy reference by the parsing code.
 *
 * @author David Yazel
 *
 */
public class AseReader extends LineNumberReader {
    public String key;
    public String[] params;
    public String line;
    public int numParams;
    public boolean endBlock;
    public boolean startBlock;
    public AseFile file = null; //todo remove
    private int ch;
    private StringBuffer wordBuff;
    int lineNo = 0;
    boolean convertMeshCoordinates = true;

    
    public AseReader(Reader in) {
        this(in, null, true);
    }
    
    /**
     * 
     *
     * @see #AseReader(Reader)
     */
    public AseReader(Reader in, AseFile f) {
        this(in, f, true);
    }

    /**
     * Construct new ASE reader that will read ASE data from specified reader to specified ASE file
     * with optional converion of coordinate system from MAX to Xith3D
     *
     * @param in Reader to read ASE data from
     * @param f ASE file object to reconstruct data to
     * @param convertMeshCoordinates true if ASE coordinates should be converted to Xith3D format, false if they should be left unchanged
     */
    public AseReader(Reader in, AseFile f, boolean convertMeshCoordinates) {
        super(in);
        this.file = f;
        key = null;
        params = new String[20];
        wordBuff = new StringBuffer(100);
        this.convertMeshCoordinates = convertMeshCoordinates;
    }

    /**
     * Uses line and ch to collect an entire word.  Spaces are counted as separators or
     * within quotes.
     */
    private String parseWord() {
        wordBuff.setLength(0);

        int n = line.length();
        boolean inString = false;

        while (ch < n) {
            char c = line.charAt(ch++);

            // if its a space then either throw it away, add to string or
            // end the word
            if ((c == ' ') || (c == 9)) {
                if (inString) {
                    wordBuff.append(c);
                } else if (wordBuff.length() > 0) {
                    return wordBuff.toString();
                }
            } else if (c == '"') {
                if (wordBuff.length() > 0) {
                    return wordBuff.toString();
                } else {
                    inString = true;
                }
            } else {
                wordBuff.append(c);
            }
        }

        if (wordBuff.length() == 0) {
            return null;
        } else {
            return wordBuff.toString();
        }
    }

    /**
     * Parses the line into pieces for easy matching
     */
    private boolean parseLine() {
        // set up the line to be empty to start
        key = "";

        for (int i = 0; i < 20; i++)
            params[i] = null;

        numParams = 0;
        endBlock = false;
        startBlock = false;

        // start parsing at the first character
        ch = 0;

        if (line == null) {
            return false;
        }

        line = line.trim();

        String word;

        while ((word = parseWord()) != null) {
            if ((word.charAt(0) == '*') && (key.length() == 0)) {
                key = word;
            } else if (word.charAt(0) == '}') {
                endBlock = true;
            } else if (word.charAt(0) == '{') {
                startBlock = true;
            } else {
                params[numParams++] = word;
            }

            //         com.xith3d.utility.logs.Log.log.println(com.xith3d.utility.logs.LogType.DEBUG,"     word = |"+word+"|");
        }

        return true;
    }

    /**
     * Reads in a line and breaks it into peices.  If the line starts with a
     * star, then get the keyword out and then read the params.
     */
    public boolean readAseLine() {
        try {
            while (true) {
                line = super.readLine();
                lineNo++;

                if (line == null) {
                    return false;
                }

                if (line.length() > 0)
                    break;
            }

            return parseLine();
        } catch (IOException e) {
            return false;
        }
    }

}

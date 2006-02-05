/*
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
package de.jreality.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.math.Matrix;

/**
 * @author weissman
 *
 * This class is for managing configuration settings based on java.lang.Properties.
 * The config file used is given as a System Property, namely jreality.config.
 * Currently only the de.jreality.portal-Package uses this class
 * 
 * The Properties file is given via the System-property "jreality.config",
 * if not it is assumed to be "jreality.props" in the current directory 
 */
public class ConfigurationAttributes extends Properties {

    public static ConfigurationAttributes getDefaultConfiguration() {
        try {
            return new ConfigurationAttributes(
                Input.getInput(new File(System.getProperty("jreality.config", "jreality.props"))),
                null);
        } catch (IOException e) {
            LoggingSystem.getLogger(ConfigurationAttributes.class).log(Level.WARNING, "loading default Configuration", e);
        }
        return new ConfigurationAttributes();
    }
    
    private ConfigurationAttributes() {}

    public ConfigurationAttributes(Input input) {
      init(input, null);
    }

    public ConfigurationAttributes(Input input, ConfigurationAttributes parent) {
        init(input, parent);
    }

    public boolean getBool(String string) {
        return getProperty(string, "false").trim().equalsIgnoreCase("true");
    }

    public double getDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }
    public double[] getDoubleArray(String key) {
        StringTokenizer toki= new StringTokenizer(getProperty(key));
        double[] ret= new double[toki.countTokens()];
        for (int i= 0; i < ret.length; i++)
            ret[i]= Double.parseDouble(toki.nextToken());
        return ret;
    }
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }
    public String getProperty(String key) {
        return super.getProperty(key);
    }
    public String[] getStringArray(String key, String delimiters) {
        String str = getProperty(key);
        if (str == null) return null;
        StringTokenizer toki= new StringTokenizer(str, delimiters);
        String[] ret= new String[toki.countTokens()];
        for (int i= 0; i < ret.length; i++)
            ret[i]= toki.nextToken();
        return ret;
    }
    public Matrix getTransformation(String key) {
        double[] matrix= getDoubleArray(key);
        if (matrix.length != 16)
            throw new RuntimeException("wrong array length for transformation!");
        return new Matrix(matrix);
    }
    private void init(Input input, ConfigurationAttributes parentAttr) {
        try {
            InputStream in=input.getInputStream();
            try {
                load(in);
            } finally {
                in.close();
            }
            // load parent prop file
            super.defaults=(parentAttr!=null)?
              parentAttr: new ConfigurationAttributes();
            String parentPropFileName= getProperty("parent");
            if (parentPropFileName != null)
                super.defaults.load(input.getRelativeInput(parentPropFileName).getInputStream());
        } catch (FileNotFoundException e) {
            Logger.getLogger("de.jreality").log(Level.WARNING,
                "file {0} : {1} not found!", new Object[]{input, getProperty("parent")});
        } catch (IOException e) {
            Logger.getLogger("de.jreality").log(Level.WARNING, input.toString(), e);
        }
    }
}

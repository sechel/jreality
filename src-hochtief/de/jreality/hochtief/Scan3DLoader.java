package de.jreality.hochtief;

import java.io.IOException;
import java.io.LineNumberReader;

import de.jreality.reader.AbstractReader;
import de.jreality.util.Input;

public class Scan3DLoader extends AbstractReader{

	private double[][] depth;
	private int M, N;	
	private byte[][] colorR, colorG, colorB;
	private double phiOffset; 
	
	public Scan3DLoader(int M, int N){
		this.M=M;
		this.N=N;
	}
	
	public void setInput(Input input) throws IOException {
		super.setInput(input);
		load();
	}
	
	private void load() throws IOException{
		LineNumberReader r = new LineNumberReader(input.getReader());
		String l = null;
		while ((l = r.readLine().trim()).startsWith("#"))
			;
		
		depth = new double[M][N];

		colorR = new byte[M][N];
		colorG = new byte[M][N];
		colorB = new byte[M][N];
		
		int colCount = 0;
		int vertexCount = 0;
		
		double phi, theta, phi_=0;
		int n,m,last_m = 0;
		
		while ((l = r.readLine()) != null) {
			String[] split = l.split(" ");
			if (split.length != 7)
				continue;
			colCount++;

			double x = Double.parseDouble(split[0]);
			double y = Double.parseDouble(split[1]);
			double z = Double.parseDouble(split[2]);

			phi = Math.atan2(y, x);
			theta = Math.atan2(z, Math.sqrt(x * x + y * y));

			// should be divided by N and M (not -1) -> undersampling problems 
			n = (int) Math.round((phi + Math.PI) / 2 / Math.PI  * (N - 1));
			m = (int) Math.round((-theta + Math.PI / 2)
					/ (Math.PI - (Math.PI / 2 - 1.1306075316023216)) * (M - 1));

			if (depth[m][n] == 0) {
				vertexCount++;
				depth[m][n] = Math.sqrt(x * x + y * y + z * z) / 1000000;
				colorR[m][n] = (byte) Double.parseDouble(split[4]);
				colorG[m][n] = (byte) Double.parseDouble(split[5]);
				colorB[m][n] = (byte) Double.parseDouble(split[6]);
			}else{
				depth[m][n] = 0.5*(Math.sqrt(x * x + y * y + z * z) / 1000000 + depth[m][n]);			
			}
			if (last_m < m - 1) {
				System.out.println("skipping line " + (m - 1));
			}

			if (last_m != m) {
				colCount = 0;
			}

			last_m = m;

			phi_ = phi;	
		}
		
		phiOffset=phi_;
	}
	
	public byte[][] getColorR(){
		return colorR;
	}
	public byte[][] getColorG(){
		return colorG;
	}
	public byte[][] getColorB(){
		return colorB;
	}
	public double[][] getDepth(){
		return depth;
	}
	
	public double getPhiOffset(){
		return phiOffset;
	}
	
}

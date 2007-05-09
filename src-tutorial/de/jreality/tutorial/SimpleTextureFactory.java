package mathvisss07;

import de.jreality.shader.ImageData;

public class SimpleTextureFactory {
	public enum TextureType {
		WEAVE,
		GRAPH_PAPER,
		DISK,
		ANTI_DISK
	};
	TextureType type = TextureType.WEAVE;
	ImageData id;
	int size = 64;
	
	public SimpleTextureFactory() {
		super();
	}
	
	public void setType(TextureType foo)	{
		type = foo;
	}
	
	public ImageData getImageData()	{
		return id;
	}
	
	public void update()	{
		byte[] im = new byte[size*size* 4];
		byte[][] colors = {{(byte)0x0,(byte)0x0,(byte)0x0,(byte)0x0},
				{(byte)200,(byte)200,(byte)200,(byte)0xff},
				{(byte)255,(byte)255,(byte)255,(byte)255},
				{(byte)0,(byte)0, (byte) 0, (byte) 255}};

		switch(type)	{
		case ANTI_DISK:
		    for (int i = 0; i<size; ++i)	{
		        for (int j = 0; j< size; ++j)	{
					int I = 4*(i*size+j);
					int sq = (i-(size/2))*(i-(size/2)) + (j-(size/2))*(j-(size/2));
					sq = i*i + j*j;
					if (sq <= size*size)	
						{im[I] =  im[I+1] = im[I+2] = im[I+3] = (byte) 255; }
					else
						{im[I] =  im[I+1] = im[I+2] = im[I+3]  = 0;  }
			    }
			}
			break;
		case DISK:
		    for (int i = 0; i<size; ++i)	{
		        for (int j = 0; j< size; ++j)	{
					int I = 4*(i*size+j);
					int sq = (i-(size/2))*(i-(size/2)) + (j-(size/2))*(j-(size/2));
					sq = i*i + j*j;
					if (sq > size*size)	
						{im[I] =  im[I+1] = im[I+2] = im[I+3] = (byte) 255; }
					else
						{im[I] =  im[I+1] = im[I+2] = im[I+3]  = 0;  }
			    }
			}
			break;
		case WEAVE:
			int margin = size/16;
			int bandwidth = 16;
			int gapwidth =16;
			int shwd = 2;
			int onewidth = 32;
			int iband, jband, imod, jmod;
			int which = 0;
		    for (int i = 0; i<size; ++i)	{
		        iband = i/onewidth;
		        imod = i%onewidth;
		        for (int j = 0; j< size; ++j)	{
				int where = 4*(i*size+j);
					jband = j /onewidth;
					jmod = j%onewidth;
					int q = 2*(iband)+jband;
					if (imod > bandwidth && jmod > bandwidth) which = 0;
					else {
					    if (imod <= bandwidth && jmod <= bandwidth)	{
					        if (q == 0 || q == 3) which = 1;
					        else which = 2;
					    } else if (jmod > bandwidth) {
					        which = 1;
					        if ((q == 0 || q == 3)&& jmod > (onewidth - shwd)) which = 3;
					        if ((q == 1 || q == 2) && jmod < (bandwidth + shwd)) which = 3;
					    } else if (imod > bandwidth) {
				 	        which = 2;
					        if ((q == 1 || q == 2)&& imod > (onewidth - shwd)) which = 3;
					        if ((q == 0 || q ==3) && imod < (bandwidth + shwd)) which = 3;
					    }
					}
					System.arraycopy(colors[which],0,im,where,4);
				}
		    }
			break;
		case GRAPH_PAPER:
			int bands = 4;
			int[] widths = {4,2,2,2};

			onewidth = size/bands;
		    for (int i = 0; i<size; ++i)	{
		        iband = i/onewidth;
		        imod = i%onewidth;
		        for (int j = 0; j< size; ++j)	{
				int where = 4*(i*size+j);
				jband = j /onewidth;
				jmod = j%onewidth;
				which = 0;
				if (jmod <= widths[jband]) {
				    if (jband == 0) which = 2;
				    else which = 1;
				} 
				if (imod <=widths[iband]) {
				    if (iband == 0) which = 2;
				    else which = 1; }
					System.arraycopy(colors[which],0,im,where,4);
				}
		    }			
		    break;
		
		}
		id = new de.jreality.shader.ImageData(im, size, size);
	}
}

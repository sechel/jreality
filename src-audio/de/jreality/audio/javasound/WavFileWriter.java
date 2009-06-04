package de.jreality.audio.javasound;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

public class WavFileWriter {

    private long dataSize;
    private int channels;
    private int bps;
    private int sampleRate;
    
    private RandomAccessFile os;
	private boolean closed;

    public WavFileWriter(AudioFormat format, File outFile) throws IOException {
    	os = new RandomAccessFile(outFile, "rw");
    	channels = format.getChannels();
    	bps = format.getSampleSizeInBits();
    	sampleRate = (int) format.getSampleRate();
    	os.seek(44); // keep the first 44 bits for the file header...
    	
    	// register shutdown hook which writes the file header and
    	// closes the file
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		@Override
    		public void run() {
    			try {
					close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	});
    }
    
    void writeInt(int arg0) throws IOException {
        os.writeByte(arg0 & 0xff);
        os.writeByte((arg0 >> 8) & 0xff);
        os.writeByte((arg0 >> 16) & 0xff);
        os.writeByte((arg0 >> 24) & 0xff);
    }
    
    void writeShort(int arg0) throws IOException {
    	os.writeByte(arg0 & 0xff);
    	os.writeByte((arg0 >> 8) & 0xff);
    }
    
    public synchronized void write(byte[] buf, int offset, int len) throws IOException {
    	if (closed) return;
    	os.write(buf, offset, len);
    	dataSize+=len;
    }
    
    public synchronized void close() throws IOException {
    	// write file header
    	os.seek(0);
        os.write("RIFF".getBytes());
        writeInt((int) dataSize + 36);
        os.write("WAVEfmt ".getBytes());
        os.write(new byte[] {0x10, 0x00, 0x00, 0x00});
        os.write(new byte[] {0x01, 0x00});
        writeShort(channels);
        writeInt(sampleRate);
        writeInt(sampleRate * channels * ((bps + 7) / 8));
        writeShort(channels * ((bps + 7) / 8));
        writeShort(bps);
        os.write("data".getBytes());
        writeInt((int) dataSize);

        // close file
    	os.close();
    	closed=true;
    }
    
}

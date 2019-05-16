/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package importexport;

import importexport.stream.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ByteBufferReaderWriter 
{
	/**
	 * Returns the given input stream's contents as a byte array.
	 * If a length is specified (ie. if length != -1), only length bytes
	 * are returned. Otherwise all bytes in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static byte[] getFileAsByteArray(String filepath)
		throws IOException {
		final int DEFAULT_READING_SIZE = 8192;
		URL file = (new File(filepath)).toURL();
		InputStream stream = file.openStream();
		int length = -1;
		byte[] contents;
		if (length == -1) {
			contents = new byte[0];
			int contentsLength = 0;
			int amountRead = -1;
			do {
				int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);  // read at least 8K
				
				// resize contents if needed
				if (contentsLength + amountRequested > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new byte[contentsLength + amountRequested],
						0,
						contentsLength);
				}

				// read as many bytes as possible
				amountRead = stream.read(contents, contentsLength, amountRequested);

				if (amountRead > 0) {
					// remember length of contents
					contentsLength += amountRead;
				}
			} while (amountRead != -1); 

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new byte[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new byte[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = stream.read(contents, len, length - len);
			}
		}

		return contents;
	}

	final public static byte[] longToBytes(long[] values,int comprLen)
	{
		byte[] bvalues = new byte[comprLen*8];
		for(int vi=0;vi<comprLen;vi++)
		{
			ByteBufferReaderWriter.writelong(bvalues, vi*8, values[vi]);
		}
		return bvalues;
	}
	
	final public static long[] bytesToLong(byte[] compr,int off,int comprLen)
	{
		 int longs = comprLen/8;
		 int longs8 = longs;
		 if((comprLen%8)!=0)
			 longs++;
		 long[] tlong = new long[longs];
		 
		 for(int bi=0;bi<longs8;bi++)
		 {
			 long l = ByteBufferReaderWriter.readlong(compr, bi*8+off);
			 tlong[bi] = l;
		 }
		 
		 System.out.println(" ");

		 int remaining = comprLen%8;
		 if(remaining!=0)
		 {
			 int shift = 56;
			 int start = (comprLen-remaining);
			 int end = start+8;
			 long l = 0;
			 for(int bi=start;bi<end;bi++)
			 {
				 int i1 = 0;
				 if(bi<comprLen)
					 i1 = compr[bi+off];

				 l = l|((i1&0xFFL)<<shift);
//								return ((i1&0xFFL)<<56) | ((i2&0xFFL)<<48) | ((i3&0xFFL)<<40) | ((i4&0xFFL)<<32) |
//								((i5&0xFFL)<<24) | ((i6&0xFFL)<<16) | ((i7&0xFFL)<<8) | ((i8&0xFFL));
				 shift -= 8;
			 }						 
			 tlong[longs-1] = l;
		 }
		return tlong;
	}
	
	final public static byte[] longToBytes(long[][] values)
	{
		int total = 0;
		for(int i=0;i<values.length;i++)
			total += values[i].length;
		
		byte[] bvalues = new byte[total*8];

		int off = 0;
		for(int i=0;i<values.length;i++)
		{
			int comprLen = values[i].length;
			for(int vi=0;vi<comprLen;vi++)
			{
				ByteBufferReaderWriter.writelong(bvalues, vi*8+off, values[i][vi]);
			}
			off+=comprLen*8;
		}
		return bvalues;
	}
	
	final public static long[][] bytesToLongs(byte[] compr,int off,int comprLen,int maxlonglength)
	{
		long[] fulllongs = bytesToLong(compr, off, comprLen);
		int numLongArrays = fulllongs.length/maxlonglength;
		int numLongArraysf = numLongArrays;
		if((fulllongs.length%maxlonglength)!=0)
		{
			numLongArrays++;
		}
		long[][] longs = new long[numLongArrays][];

		for(int i=0;i<numLongArraysf;i++)
		{
			long[] longv = new long[maxlonglength];
			System.arraycopy(fulllongs, i*maxlonglength, longv, 0, maxlonglength);
			longs[i] = longv;
		}
		int remlong = fulllongs.length%maxlonglength; 
		if((remlong)!=0)
		{
			long[] longv = new long[remlong];
			System.arraycopy(fulllongs, numLongArraysf*maxlonglength, longv, 0, remlong);
			longs[numLongArraysf] = longv;
		}
		
		return longs;
	}

	final public static int writebytes(byte[] output,int offset,byte[] values)
	{
		for(int i=0;i<values.length;i++)
		{
			output[offset] = values[i];
			offset++;
		}
		return offset;
	}

	final public static int writeubyte(OutputStream output,int value) throws IOException
	{
		output.write((int)((value&0x000000FF)>>>0));
		return 1;
	}

	final public static int writeubyte(byte[] output,int offset,int value)
	{
		output[offset+0] = (byte)((value&0x000000FF)>>>0);
		return offset+1;
	}

	final public static int readubyte(InputStream input) throws IOException
	{		
		return input.read();
	}

	final public static int readubyte(byte[] input,int cur)
	{
		return (input[cur+0]&0xFF);
	}

	final public static int writechars(byte[] output,int offset,char[] values)
	{
		for(int i=0;i<values.length;i++)
		{
			output[offset] = (byte)values[i];
			offset++;
		}
		return offset;
	}

	final public static int writeushort(OutputStream output,int value) throws IOException
	{
		output.write((int)((value&0x0000FF00)>>>8));
		output.write((int)((value&0x000000FF)>>>0));
		return 2;
	}

	final public static int writeushort(byte[] output,int offset,int value)
	{
		output[offset+0] = (byte)((value&0x0000FF00)>>>8);
		output[offset+1] = (byte)((value&0x000000FF)>>>0);
		return offset+2;
	}

	final public static int readushort(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		return ((i1&0xFF)<<8) | ((i2&0xFF));
	}
	final public static int readushort2(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		return ((i2&0xFF)<<8) | ((i1&0xFF));
	}
	final public static short readshort(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		short val=(short)( ((i1&0xFF)<<8) | (i2&0xFF) );
		return val;
	}
	final public static short readshort2(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		short val=(short)( ((i2&0xFF)<<8) | (i1&0xFF) );
		return val;
	}
	final public static int readushort(byte[] input,int cur)
	{
		return ((input[cur+0]&0xFF)<<8) | ((input[cur+1]&0xFF));
	}
	
	final public static int writeint(OutputStream output,int value) throws IOException
	{
		output.write((int)((value&0xFF000000)>>>24));
		output.write((int)((value&0x00FF0000)>>>16));
		output.write((int)((value&0x0000FF00)>>>8));
		output.write((int)((value&0x000000FF)>>>0));
		return 8;
	}

	final public static int writedouble(OutputStream output,double dvalue) throws IOException
	{
		long value = Double.doubleToLongBits(dvalue);
		return writelong(output, value);
	}
	final public static int writefloat(OutputStream output,float fvalue) throws IOException
	{
		int value = Float.floatToIntBits(fvalue);
		output.write((int)((value&0xFF000000)>>>24));
		output.write((int)((value&0x00FF0000)>>>16));
		output.write((int)((value&0x0000FF00)>>>8));
		output.write((int)((value&0x000000FF)>>>0));
		return 8;
	}
	final public static int writefloat(byte[] output,int offset,float fvalue)
	{
		int value = Float.floatToIntBits(fvalue);
		output[offset+0] = (byte)((value&0xFF000000)>>>24);
		output[offset+1] = (byte)((value&0x00FF0000)>>>16);
		output[offset+2] = (byte)((value&0x0000FF00)>>>8);
		output[offset+3] = (byte)((value&0x000000FF)>>>0);
		return offset+4;
	}

	final public static int writeint(byte[] output,int offset,int value)
	{
		output[offset+0] = (byte)((value&0xFF000000)>>>24);
		output[offset+1] = (byte)((value&0x00FF0000)>>>16);
		output[offset+2] = (byte)((value&0x0000FF00)>>>8);
		output[offset+3] = (byte)((value&0x000000FF)>>>0);
		return offset+4;
	}

	final public static int readint(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		return ((i1&0xFF)<<24) | ((i2&0xFF)<<16) | ((i3&0xFF)<<8) | ((i4&0xFF));
	}
	final public static int readint2(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		return ((i4&0xFF)<<24) | ((i3&0xFF)<<16) | ((i2&0xFF)<<8) | ((i1&0xFF));
	}
	final public static int readint(BufferedReader input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		return ((i1&0xFF)<<24) | ((i2&0xFF)<<16) | ((i3&0xFF)<<8) | ((i4&0xFF));
	}

	final public static float readfloat(InputStream input) throws IOException
	{		
		int i1 = input.read();
		if(i1==-1)
			throw new IOException();
		int i2 = input.read();
		if(i2==-1)
			throw new IOException();
		int i3 = input.read();
		if(i3==-1)
			throw new IOException();
		int i4 = input.read();
		if(i4==-1)
			throw new IOException();

		int i = ((i1&0xFF)<<24) | ((i2&0xFF)<<16) | ((i3&0xFF)<<8) | ((i4&0xFF));
		return Float.intBitsToFloat(i);
	}
	final public static float readfloat2(InputStream input) throws IOException
	{		
		int i1 = input.read();
		if(i1==-1)
			throw new IOException();
		int i2 = input.read();
		if(i2==-1)
			throw new IOException();
		int i3 = input.read();
		if(i3==-1)
			throw new IOException();
		int i4 = input.read();
		if(i4==-1)
			throw new IOException();

		int i = ((i4&0xFF)<<24) | ((i3&0xFF)<<16) | ((i2&0xFF)<<8) | ((i1&0xFF));
		float f = Float.intBitsToFloat(i);
//		int int2 = ((i1&0xFF)<<24) | ((i2&0xFF)<<16) | ((i3&0xFF)<<8) | ((i4&0xFF));
//		float f2 = Float.intBitsToFloat(int2);
		return f;
	}
	final public static float readfloat(BufferedReader input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		int i = ((i1&0xFF)<<24) | ((i2&0xFF)<<16) | ((i3&0xFF)<<8) | ((i4&0xFF));
		return Float.intBitsToFloat(i);
	}
	final public static double readdouble(InputStream input) throws IOException
	{		
		long value = readlong(input);
		return Double.longBitsToDouble(value);
	}
	final public static double readdouble2(InputStream input) throws IOException
	{		
		long value = readlong2(input);
		return Double.longBitsToDouble(value);
	}
	final public static double readdouble(BufferedReader input) throws IOException
	{		
		long value = readlong(input);
		return Double.longBitsToDouble(value);
	}

	final public static float readfloat(byte[] input,int cur)
	{
		int v = ((input[cur]&0xFF)<<24) | ((input[cur+1]&0xFF)<<16) | ((input[cur+2]&0xFF)<<8) | ((input[cur+3]&0xFF));
		return Float.intBitsToFloat(v);
	}
	final public static int readint(byte[] input,int cur)
	{
		return ((input[cur]&0xFF)<<24) | ((input[cur+1]&0xFF)<<16) | ((input[cur+2]&0xFF)<<8) | ((input[cur+3]&0xFF));
	}
	final public static int readintrev(byte[] input,int cur)
	{
		return ((input[cur+3]&0xFF)<<24) | ((input[cur+2]&0xFF)<<16) | ((input[cur+1]&0xFF)<<8) | ((input[cur+0]&0xFF));
	}

	final public static int writelong(OutputStream output,long value) throws IOException
	{
		output.write((int)((value&0xFF00000000000000L)>>>56));
		output.write((int)((value&0x00FF000000000000L)>>>48));
		output.write((int)((value&0x0000FF0000000000L)>>>40));
		output.write((int)((value&0x000000FF00000000L)>>>32));
		output.write((int)((value&0xFF000000)>>>24));
		output.write((int)((value&0x00FF0000)>>>16));
		output.write((int)((value&0x0000FF00)>>>8));
		output.write((int)((value&0x000000FF)>>>0));
		return 8;
	}

	final public static int writelong(byte[] output,int offset,long value)
	{
		output[offset+0] = (byte)((value&0xFF00000000000000L)>>>56);
		output[offset+1] = (byte)((value&0x00FF000000000000L)>>>48);
		output[offset+2] = (byte)((value&0x0000FF0000000000L)>>>40);
		output[offset+3] = (byte)((value&0x000000FF00000000L)>>>32);
		output[offset+4] = (byte)((value&0xFF000000)>>>24);
		output[offset+5] = (byte)((value&0x00FF0000)>>>16);
		output[offset+6] = (byte)((value&0x0000FF00)>>>8);
		output[offset+7] = (byte)((value&0x000000FF)>>>0);
		return offset+8;
	}
	
	final public static long readlong(byte[] input,int cur)
	{
		return ((input[cur+0]&0xFFL)<<56) | ((input[cur+1]&0xFFL)<<48) | ((input[cur+2]&0xFFL)<<40) | ((input[cur+3]&0xFFL)<<32) |
				((input[cur+4]&0xFFL)<<24) | ((input[cur+5]&0xFFL)<<16) | ((input[cur+6]&0xFFL)<<8) | ((input[cur+7]&0xFFL));
	}
	
	final public static long readlong(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		int i5 = input.read();
		int i6 = input.read();
		int i7 = input.read();
		int i8 = input.read();
		return ((i1&0xFFL)<<56) | ((i2&0xFFL)<<48) | ((i3&0xFFL)<<40) | ((i4&0xFFL)<<32) |
		((i5&0xFFL)<<24) | ((i6&0xFFL)<<16) | ((i7&0xFFL)<<8) | ((i8&0xFFL));
	}
	final public static long readlong2(InputStream input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		int i5 = input.read();
		int i6 = input.read();
		int i7 = input.read();
		int i8 = input.read();
		return ((i8&0xFFL)<<56) | ((i7&0xFFL)<<48) | ((i6&0xFFL)<<40) | ((i5&0xFFL)<<32) |
		((i4&0xFFL)<<24) | ((i3&0xFFL)<<16) | ((i2&0xFFL)<<8) | ((i1&0xFFL));
	}

	final public static long readlong(BufferedReader input) throws IOException
	{		
		int i1 = input.read();
		int i2 = input.read();
		int i3 = input.read();
		int i4 = input.read();
		int i5 = input.read();
		int i6 = input.read();
		int i7 = input.read();
		int i8 = input.read();
		return ((i1&0xFFL)<<56) | ((i2&0xFFL)<<48) | ((i3&0xFFL)<<40) | ((i4&0xFFL)<<32) |
		((i5&0xFFL)<<24) | ((i6&0xFFL)<<16) | ((i7&0xFFL)<<8) | ((i8&0xFFL));
	}

}

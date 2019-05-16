/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class PropertyArray32 extends Property
{
	int type;
	public int[] array;

	public static final int T_INT = 0;
	public static final int T_FLOAT = 1;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyArray32);
	}
	
	public PropertyArray32() {}
	
	public PropertyArray32(int t)
	{
		type = t;
	}
	public PropertyArray32(int t,int size)
	{
		type = t;
		array = new int[size];
	}
	
	public void set(Property p)
	{
		PropertyArray32 pt = (PropertyArray32)p;
		type = pt.type;
		if(array.length!=pt.array.length)
		{
			array = new int[pt.array.length];
		}
		System.arraycopy(pt.array, 0, array, 0, pt.array.length);
	}
	public Property copy()
	{
		PropertyArray32 pt = new PropertyArray32(type,array.length);
		System.arraycopy(array, 0, pt.array, 0, pt.array.length);
		pt.id = id;
		return pt;
	}
	
	public Property createInstance()
	{
		return new PropertyArray32(PropertyArray32.T_INT);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("int[]"))
	    {
	    	return new PropertyArray32(PropertyArray32.T_INT);
	    }
	    else
	    if(type.equalsIgnoreCase("float[]"))
	    {
	    	return new PropertyArray32(PropertyArray32.T_FLOAT);
	    }
	    return null;
	}
	public String typeString()
	{
		switch(type)
		{
		case T_INT:
			return "int[]";
		case T_FLOAT:
			return "float[]";
		}
		
		return "Error";
	}

	public int Parse(String value) 
	{ 
		int remainingEntries = Integer.parseInt(value);
		array = new int[remainingEntries];
		return remainingEntries; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = array.length-remainingEntries;
		remainingEntries--; 
		switch(type)
		{
		case T_INT:
			array[i] = Integer.parseInt(value);
			break;
		case T_FLOAT:
			array[i] = Float.floatToIntBits(Float.parseFloat(value));
			break;
		}
		return remainingEntries; 
	}

	public void SetFloatArray(float[] farray,int num)
	{
		for(int i=0;i<num;i++)
		{
			array[i] = Float.floatToIntBits(farray[i]);
		}
	}
	public void GetFloatArray(float[] farray)
	{
		for(int i=0;i<array.length;i++)
		{
			farray[i] = Float.intBitsToFloat(array[i]);
		}
	}
	
	public void SaveVerbose(PrintStream pr)
	{
		switch(type)
		{
		case T_INT:
			pr.append("int[] "+idToString(id)+"="+array.length+"\n");
			break;
		case T_FLOAT:
			pr.append("float[] "+idToString(id)+"="+array.length+"\n");
			break;
		}
		
		for(int i=0;i<array.length;i++)
		{
			switch(type)
			{
			case T_INT:
				pr.append(""+array[i]+"\n");
				break;
			case T_FLOAT:
				pr.append(""+Float.intBitsToFloat(array[i])+"\n");
				break;
			}
		}
	}
	public void Parse(InputStream in) throws IOException
	{
		type = ByteBufferReaderWriter.readint(in);
		id = ByteBufferReaderWriter.readlong(in);
		int len = ByteBufferReaderWriter.readint(in);
		array = new int[len];
		for(int i=0;i<array.length;i++)
		{
			array[i] = ByteBufferReaderWriter.readint(in);
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyArray32);
		ByteBufferReaderWriter.writeint(o,type);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,array.length);
		for(int i=0;i<array.length;i++)
		{
			ByteBufferReaderWriter.writeint(o,array[i]);
		}		
	}
}

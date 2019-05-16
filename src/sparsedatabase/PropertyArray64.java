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

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixLong;

public class PropertyArray64 extends Property
{
	int type;
	public GeneralMatrixLong array;

	public static final int T_LONG = 0;
	public static final int T_DOUBLE = 1;
	public static final int T_ULONG = 2;
	public static final int T_ID = 3;
	public static final int T_SID = 4;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyArray64);
	}
	public String typeString()
	{
		switch(type)
		{
		case T_LONG:
			return "long[]";
		case T_DOUBLE:
			return "double[]";
		case T_ULONG:
			return "ulong[]";
		case T_ID:
			return "id[]";
		case T_SID:
			return "sid[]";
		}
		
		return "Error";
	}

	public PropertyArray64() {}
	
	public PropertyArray64(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		array = new GeneralMatrixLong(1);
		parent.AddProperty(this);
	}

	public PropertyArray64(int t)
	{
		type = t;
	}
	
	public void set(Property p)
	{
		PropertyArray64 pt = (PropertyArray64)p;
		type = pt.type;
		array.set(pt.array);
	}
	public Property copy()
	{
		PropertyArray64 pt = new PropertyArray64(type);
		pt.array = new GeneralMatrixLong(array);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyArray64(PropertyArray64.T_LONG);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("long[]"))
	    {
	    	return new PropertyArray64(PropertyArray64.T_LONG);
	    }
	    else
	    if(type.equalsIgnoreCase("double[]"))
	    {
	    	return new PropertyArray64(PropertyArray64.T_DOUBLE);
	    }
	    else
	    if(type.equalsIgnoreCase("ulong[]"))
	    {
	    	return new PropertyArray64(PropertyArray64.T_ULONG);
	    }
	    else
	    if(type.equalsIgnoreCase("id[]"))
	    {
	    	return new PropertyArray64(PropertyArray64.T_ID);
	    }	
	    else
	    if(type.equalsIgnoreCase("sid[]"))
	    {
	    	return new PropertyArray64(PropertyArray64.T_SID);
	    }	
	    return null;
	}
	public int Parse(String value) 
	{ 
		int remainingEntries = Integer.parseInt(value);
		array = new GeneralMatrixLong(1,remainingEntries);
		return remainingEntries; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = array.height-remainingEntries;
		remainingEntries--; 
		switch(type)
		{
		case T_LONG:
		case T_ID:
			array.value[i] = Long.parseLong(value);
			break;
		case T_DOUBLE:
			array.value[i] = Double.doubleToLongBits(Double.parseDouble(value));
			break;
		case T_ULONG:
			array.value[i] = Long.parseLong(value);
			break;
		case T_SID:
			array.value[i] = stringToID(value);
			break;
		}
		return remainingEntries; 
	}

	public void SaveVerbose(PrintStream pr)
	{
		switch(type)
		{
		case T_LONG:
			pr.append("long[] "+idToString(id)+"="+array.height+"\n");
			break;
		case T_DOUBLE:
			pr.append("double[] "+idToString(id)+"="+array.height+"\n");
			break;
		case T_ULONG:
			pr.append("ulong[] "+idToString(id)+"="+array.height+"\n");
			break;
		case T_ID:
			pr.append("id[] "+idToString(id)+"="+array.height+"\n");
			break;
		case T_SID:
			pr.append("sid[] "+idToString(id)+"="+array.height+"\n");
			break;
		}
		
		for(int i=0;i<array.height;i++)
		{
			switch(type)
			{
			case T_LONG:
				pr.append(""+array.value[i]+"\n");
				break;
			case T_DOUBLE:
				pr.append(""+Double.longBitsToDouble(array.value[i])+"\n");
				break;
			case T_ULONG:
				pr.append(""+array.value[i]+"\n");
				break;
			case T_ID:
				pr.append(""+array.value[i]+"\n");
				break;
			case T_SID:
				pr.append(""+array.value[i]+"\n");
				break;
			}
		}
	}
	
	public void Parse(InputStream in) throws IOException
	{
		type = ByteBufferReaderWriter.readint(in);
		id = ByteBufferReaderWriter.readlong(in);
		int len = ByteBufferReaderWriter.readint(in);
		array = new GeneralMatrixLong(1,len);
		for(int i=0;i<array.height;i++)
		{
			array.value[i] = ByteBufferReaderWriter.readlong(in);
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyArray64);
		ByteBufferReaderWriter.writeint(o,type);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,array.height);
		for(int i=0;i<array.height;i++)
		{
			ByteBufferReaderWriter.writelong(o,array.value[i]);
		}		
	}
	
}

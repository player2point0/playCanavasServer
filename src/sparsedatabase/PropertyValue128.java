/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyValue128 extends Property 
{
	public int type = 0;
	public int value[] = new int[4];

	public static final int T_FLOAT2 = 0;
	public static final int T_FLOAT3 = 1;
	public static final int T_FLOAT4 = 2;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyValue128);
	}
	
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("float2");
		n.push_back("float3");
		n.push_back("float4");
	}
	public String typeString()
	{
		switch(type)
		{
		case T_FLOAT2:
			return "float2";
		case T_FLOAT3:
			return "float3";
		case T_FLOAT4:
			return "float4";
		}
		
		return "Error";
	}

	public PropertyValue128()
	{
	}

	public PropertyValue128(int t)
	{
		type = t;
	}
	
	public void set(Property p)
	{
		PropertyValue128 pt = (PropertyValue128)p;
		type = pt.type;
		System.arraycopy(pt.value, 0, value, 0, 4);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyValue128))
			return false;
		PropertyValue128 o = (PropertyValue128)p;
		return o.value == (value);
	}

	public Property copy()
	{
		PropertyValue128 pt = new PropertyValue128();
		System.arraycopy(value, 0, pt.value, 0, 4);
		pt.type = type;
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyValue128(PropertyValue128.T_FLOAT4);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("float2"))
	    {
	    	return new PropertyValue128(PropertyValue128.T_FLOAT2);
	    }	
	    else
	    if(type.equalsIgnoreCase("float3"))
	    {
	    	return new PropertyValue128(PropertyValue128.T_FLOAT3);
	    }	
	    else
	    if(type.equalsIgnoreCase("float4"))
	    {
	    	return new PropertyValue128(PropertyValue128.T_FLOAT4);
	    }	
		return null;
	}

	public int Parse(String value)
	{
		switch(type)
		{
		case T_FLOAT2:
		{
			float v1,v2;
			SplitTuple(value);
			setFloat2(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]));
			break;
		}
		case T_FLOAT3:
		{
			float v1,v2,v3;
			SplitTuple(value);
			setFloat3(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
			break;
		}
		case T_FLOAT4:
		{
			float v1,v2,v3,v4;
			SplitTuple(value);
			setFloat4(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]),Float.parseFloat(vals[2]), Float.parseFloat(vals[3]));
			break;
		}
		}
		return 0;
	}

	public void SaveVerbose(GeneralMatrixString p)
	{
		switch(type)
		{
		case T_FLOAT2:
			p.push_back("float2 "+idToString(id)+"="+getFloat_0()+","+getFloat_1());
			break;
		case T_FLOAT3:
			p.push_back("float3 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2());
			break;
		case T_FLOAT4:
			p.push_back("float4 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2()+","+getFloat_3());
			break;
		}
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		switch(type)
		{
		case T_FLOAT2:
			p.push_back("float2 "+idToString(id)+"="+getFloat_0()+","+getFloat_1());
			break;
		case T_FLOAT3:
			p.push_back("float3 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2());
			break;
		case T_FLOAT4:
			p.push_back("float4 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2()+","+getFloat_3());
			break;
		}
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p)
	{
		switch(type)
		{
		case T_FLOAT2:
			p.append("float2 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+"\n");
			break;
		case T_FLOAT3:
			p.append("float3 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2()+"\n");
			break;
		case T_FLOAT4:
			p.append("float4 "+idToString(id)+"="+getFloat_0()+","+getFloat_1()+","+getFloat_2()+","+getFloat_3()+"\n");
			break;
		}
	}
	
	public void setFloat2(float v1,float v2)
	{
		value[0] = Float.floatToIntBits(v1);
		value[1] = Float.floatToIntBits(v2);
	}
	public void setFloat3(float v1,float v2,float v3)
	{
		value[0] = Float.floatToIntBits(v1);
		value[1] = Float.floatToIntBits(v2);
		value[2] = Float.floatToIntBits(v3);
	}
	public void setFloat4(float v1,float v2,float v3,float v4)
	{
		value[0] = Float.floatToIntBits(v1);
		value[1] = Float.floatToIntBits(v2);
		value[2] = Float.floatToIntBits(v3);
		value[3] = Float.floatToIntBits(v4);
	}
	
	public float getFloat_0()
	{
		return Float.intBitsToFloat(value[0]);
	}
	public float getFloat_1()
	{
		return Float.intBitsToFloat(value[1]);
	}
	public float getFloat_2()
	{
		return Float.intBitsToFloat(value[2]);
	}
	public float getFloat_3()
	{
		return Float.intBitsToFloat(value[3]);
	}
	
	public void Parse(InputStream in) throws IOException
	{
		type = ByteBufferReaderWriter.readint(in);
		id = ByteBufferReaderWriter.readlong(in);
		switch(type)
		{
		case T_FLOAT2:
			value[0] = ByteBufferReaderWriter.readint(in);
			value[1] = ByteBufferReaderWriter.readint(in);
			break;
		case T_FLOAT3:
			value[0] = ByteBufferReaderWriter.readint(in);
			value[1] = ByteBufferReaderWriter.readint(in);
			value[2] = ByteBufferReaderWriter.readint(in);
			break;
		case T_FLOAT4:
			value[0] = ByteBufferReaderWriter.readint(in);
			value[1] = ByteBufferReaderWriter.readint(in);
			value[2] = ByteBufferReaderWriter.readint(in);
			value[3] = ByteBufferReaderWriter.readint(in);
			break;
		}
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyValue128);
		ByteBufferReaderWriter.writeint(o,type);
		ByteBufferReaderWriter.writelong(o,id);
		
		switch(type)
		{
		case T_FLOAT2:
			ByteBufferReaderWriter.writeint(o,value[0]);
			ByteBufferReaderWriter.writeint(o,value[1]);
			break;
		case T_FLOAT3:
			ByteBufferReaderWriter.writeint(o,value[0]);
			ByteBufferReaderWriter.writeint(o,value[1]);
			ByteBufferReaderWriter.writeint(o,value[2]);
			break;
		case T_FLOAT4:
			ByteBufferReaderWriter.writeint(o,value[0]);
			ByteBufferReaderWriter.writeint(o,value[1]);
			ByteBufferReaderWriter.writeint(o,value[2]);
			ByteBufferReaderWriter.writeint(o,value[3]);
			break;
		}
	}
}

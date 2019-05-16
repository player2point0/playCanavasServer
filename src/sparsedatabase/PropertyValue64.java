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

public class PropertyValue64 extends Property 
{
	public static final int T_LONG = 0;
	public static final int T_DOUBLE = 1;
	public static final int T_ULONG = 2;
	public static final int T_ID = 3;
	public static final int T_SID = 4;
	//static final int T_FLOAT2 = 5;
	
	int type = 0;
	public long value;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyValue64);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("long");
		n.push_back("double");
		n.push_back("ulong");
		n.push_back("id");
		n.push_back("sid");
	}
	public String typeString()
	{
		switch(type)
		{
		case T_LONG:
			return "long";
		case T_DOUBLE:
			return "double";
		case T_ULONG:
			return "ulong";
		case T_ID:
			return "id";
		case T_SID:
			return "sid";
		}
		
		return "Error";
	}

	public PropertyValue64(PropertyHashtable parent,long v,String name)
	{
		id = Property.stringToID(name);	
		type = T_LONG;
		setLong(v);
		parent.AddProperty(this);
	}
	
	public PropertyValue64(PropertyHashtable parent,double v,String name)
	{
		id = Property.stringToID(name);	
		type = T_DOUBLE;
		setDouble(v);
		parent.AddProperty(this);
	}
	
	public PropertyValue64(int t)
	{
		type = t;
	}

	public PropertyValue64()
	{
	}
	
	public void set(Property p)
	{
		PropertyValue64 pt = (PropertyValue64)p;
		type = pt.type;
		value = pt.value;
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyValue64))
			return false;
		PropertyValue64 o = (PropertyValue64)p;
		return o.value == (value);
	}

	public Property copy()
	{
		PropertyValue64 pt = new PropertyValue64();
		pt.value = value;
		pt.type = type;
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyValue64(PropertyValue64.T_LONG);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("long"))
	    {
	    	return new PropertyValue64(PropertyValue64.T_LONG);
	    }
	    else
	    if(type.equalsIgnoreCase("double"))
	    {
	    	return new PropertyValue64(PropertyValue64.T_DOUBLE);
	    }
	    else
	    if(type.equalsIgnoreCase("ulong"))
	    {
	    	return new PropertyValue64(PropertyValue64.T_ULONG);
	    }
	    else
	    if(type.equalsIgnoreCase("id"))
	    {
	    	return new PropertyValue64(PropertyValue64.T_ID);
	    }	
	    else
	    if(type.equalsIgnoreCase("sid"))
	    {
	    	return new PropertyValue64(PropertyValue64.T_SID);
	    }	
		return null;
	}

	public int Parse(String value)
	{
		switch(type)
		{
		case T_LONG:
		case T_ID:
			setLong(Long.parseLong(value));
			break;
		case T_DOUBLE:
			setDouble(Double.parseDouble(value));
			break;
		case T_ULONG:
			setULong(Long.parseLong(value));
			break;
		case T_SID:
			setLong(stringToID(value));
			break;
		//case T_FLOAT2:
		//{
		//	float v1,v2;
		//	SplitTuple(value);
		//	setFloat2(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]));
		//	break;
		//}
		}
		return 0;
	}

	public void SaveVerbose(GeneralMatrixString p)
	{
		switch(type)
		{
		case T_LONG:
			p.push_back("long "+idToString(id)+"="+value);
			break;
		case T_DOUBLE:
			p.push_back("double "+idToString(id)+"="+getDouble());
			break;
		case T_ULONG:
			p.push_back("ulong "+idToString(id)+"="+value);
			break;
		case T_ID:
			p.push_back("id "+idToString(id)+"="+value);
			break;
		case T_SID:
			p.push_back("sid "+idToString(id)+"="+idToString(value));
			break;
		//case T_FLOAT2:
		//	p.append("float2 "+idToString(id)+"="+getFloat2_0()+","+getFloat2_1()+"\n");
		//	break;
		}
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		switch(type)
		{
		case T_LONG:
			p.push_back("long "+idToString(id)+"="+value);
			break;
		case T_DOUBLE:
			p.push_back("double "+idToString(id)+"="+getDouble());
			break;
		case T_ULONG:
			p.push_back("ulong "+idToString(id)+"="+value);
			break;
		case T_ID:
			p.push_back("id "+idToString(id)+"="+value);
			break;
		case T_SID:
			p.push_back("sid "+idToString(id)+"="+idToString(value));
			break;
		}
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p)
	{
		switch(type)
		{
		case T_LONG:
			p.append("long "+idToString(id)+"="+value+"\n");
			break;
		case T_DOUBLE:
			p.append("double "+idToString(id)+"="+getDouble()+"\n");
			break;
		case T_ULONG:
			p.append("ulong "+idToString(id)+"="+value+"\n");
			break;
		case T_ID:
			p.append("id "+idToString(id)+"="+value+"\n");
			break;
		case T_SID:
			p.append("sid "+idToString(id)+"="+idToString(value)+"\n");
			break;
		//case T_FLOAT2:
		//	p.append("float2 "+idToString(id)+"="+getFloat2_0()+","+getFloat2_1()+"\n");
		//	break;
		}
	}

	public void Parse(InputStream in) throws IOException
	{
		type = ByteBufferReaderWriter.readint(in);
		id = ByteBufferReaderWriter.readlong(in);
		value = ByteBufferReaderWriter.readlong(in);
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyValue64);
		ByteBufferReaderWriter.writeint(o,type);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writelong(o,value);
	}

	public void setDouble(double v)
	{
		value = Double.doubleToLongBits(v);
	}
	public void setLong(long v)
	{
		value = v;
	}
	public void setULong(long v)
	{
		value = v;
	}
	/*
	void setFloat2(float v1,float v2)
	{
		int val1 = Float.floatToIntBits(v1);
		int val2 = Float.floatToIntBits(v2);
		value = val1;
		value = value<<32;
		value = value|((long)val2);
	}
	*/
	
	public double getDouble()
	{
		return Double.longBitsToDouble(value);
	}
	public long getLong()
	{
		return value;
	}
	public long getULong()
	{
		return value;
	}
	/*
	public float getFloat2_0()
	{
		long lv1 = value;
		lv1 = lv1>>32;
		int val1 = (int)(lv1|0xFFFFFFFF);
		return Float.intBitsToFloat(val1);
	}
	public float getFloat2_1()
	{
		long lv1 = value;
		int val1 = (int)(lv1|0xFFFFFFFF);
		return Float.intBitsToFloat(val1);
	}
	*/
}

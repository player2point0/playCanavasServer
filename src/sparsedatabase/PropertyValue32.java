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

public class PropertyValue32 extends Property 
{
	public static final int T_INT = 0;
	public static final int T_FLOAT = 1;
	public static final int T_UINT = 2;
	public static final int T_BOOL = 3;
	int type = 0;
	public int value;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyValue32);
	}

	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("int");
		n.push_back("float");
		n.push_back("uint");
		n.push_back("bool");
	}
	public String typeString()
	{
		switch(type)
		{
		case T_INT:
			return "int";
		case T_FLOAT:
			return "float";
		case T_UINT:
			return "uint";
		case T_BOOL:
			return "bool";
		}
		
		return "Error";
	}

	public PropertyValue32(PropertyHashtable parent,float v,String name)
	{
		id = Property.stringToID(name);	
		type = T_FLOAT;
		setFloat(v);
		parent.AddProperty(this);
	}
	
	public PropertyValue32(PropertyHashtable parent,int v,String name)
	{
		id = Property.stringToID(name);	
		type = T_INT;
		setInt(v);
		parent.AddProperty(this);
	}
	
	public PropertyValue32() {}
	
	public PropertyValue32(int t)
	{
		type = t;
	}
	
	public void set(Property p)
	{
		PropertyValue32 pt = (PropertyValue32)p;
		type = pt.type;
		value = pt.value;
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyValue32))
			return false;
		PropertyValue32 o = (PropertyValue32)p;
		return o.value == (value);
	}

	public Property copy()
	{
		PropertyValue32 pt = new PropertyValue32();
		pt.value = value;
		pt.type = type;
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyValue32(PropertyValue32.T_INT);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("int"))
	    {
	    	return new PropertyValue32(PropertyValue32.T_INT);
	    }
	    else
	    if(type.equalsIgnoreCase("float"))
	    {
	    	return new PropertyValue32(PropertyValue32.T_FLOAT);
	    }
	    else
	    if(type.equalsIgnoreCase("uint"))
	    {
	    	return new PropertyValue32(PropertyValue32.T_UINT);
	    }
	    else
	    if(type.equalsIgnoreCase("bool"))
	    {
	    	return new PropertyValue32(PropertyValue32.T_BOOL);
	    }
		return null;
	}

	public int Parse(String value)
	{
		switch(type)
		{
		case T_INT:
			setInt(Integer.parseInt(value));
			break;
		case T_FLOAT:
			setFloat(Float.parseFloat(value));
			break;
		case T_UINT:
			setUInt(Long.parseLong(value));
			break;
		case T_BOOL:
			if(value.equalsIgnoreCase("true"))
			{
				this.value = 1;
			}
			else
			{
				this.value = 0;
			}
			break;
		}
		return 0;
	}

	public void SaveVerbose(GeneralMatrixString p)
	{
		switch(type)
		{
		case T_INT:
			p.push_back("int "+idToString(id)+"="+value);
			break;
		case T_FLOAT:
			p.push_back("float "+idToString(id)+"="+getFloat());
			break;
		case T_UINT:
			p.push_back("uint "+idToString(id)+"="+getUInt());
			break;
		case T_BOOL:
			if(this.value==0)
				p.push_back("bool "+idToString(id)+"=false");
			else
				p.push_back("bool "+idToString(id)+"=true");
			break;
		}
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		switch(type)
		{
		case T_INT:
			p.push_back("int "+idToString(id)+"="+value);
			break;
		case T_FLOAT:
			p.push_back("float "+idToString(id)+"="+getFloat());
			break;
		case T_UINT:
			p.push_back("uint "+idToString(id)+"="+getUInt());
			break;
		case T_BOOL:
			if(this.value==0)
				p.push_back("bool "+idToString(id)+"=false");
			else
				p.push_back("bool "+idToString(id)+"=true");
			break;
		}
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p)
	{
		switch(type)
		{
		case T_INT:
			p.append("int "+idToString(id)+"="+value+"\n");
			break;
		case T_FLOAT:
			p.append("float "+idToString(id)+"="+getFloat()+"\n");
			break;
		case T_UINT:
			p.append("uint "+idToString(id)+"="+getUInt()+"\n");
			break;
		case T_BOOL:
			if(this.value==0)
				p.append("bool "+idToString(id)+"=false\n");
			else
				p.append("bool "+idToString(id)+"=true\n");
			break;
		}
	}
	
	public void Parse(InputStream in) throws IOException
	{
		type = ByteBufferReaderWriter.readint(in);
		id = ByteBufferReaderWriter.readlong(in);
		value = ByteBufferReaderWriter.readint(in);
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyValue32);
		ByteBufferReaderWriter.writeint(o,type);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,value);
	}
	public void setFloat(float v)
	{
		value = Float.floatToIntBits(v);
	}
	public void setInt(int v)
	{
		value = v;
	}
	public void setUInt(long v)
	{
		value = (int)(v&0xFFFFFFFF);
	}
	
	public float getFloat()
	{
		return Float.intBitsToFloat(value);
	}
	public int getInt()
	{
		return value;
	}
	public long getUInt()
	{
		return value&0xFFFFFFFF;
	}
}

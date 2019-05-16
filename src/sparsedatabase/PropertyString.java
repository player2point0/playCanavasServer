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

public class PropertyString extends Property
{
	public String value;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyString);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("string");
	}
	public String typeString()
	{
		return "string";
	}

	public PropertyString()
	{
		
	}
	
	public PropertyString(PropertyHashtable parent,String contents,String name)
	{
		id = Property.stringToID(name);		
		value = contents;
		parent.AddProperty(this);
	}

	public PropertyString(PropertyList parent,String contents,String name)
	{
		id = Property.stringToID(name);		
		value = contents;
		parent.appendProperty(this);
	}

	public void set(Property p)
	{
		PropertyString pt = (PropertyString)p;
		value = pt.value;
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyString))
			return false;
		PropertyString o = (PropertyString)p;
		return o.value.contentEquals(value);
	}

	public Property copy()
	{
		PropertyString pt = new PropertyString();
		pt.value = value;
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyString();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("string"))
	    {
	    	return new PropertyString();
	    }	
		return null;
	}

	public int Parse(String v)
	{
		value = v;
		return 0;
	}
	public void SaveVerbose(GeneralMatrixString p) 
	{
		p.push_back("string "+idToString(id)+"="+value);		
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		p.push_back("string "+idToString(id)+"="+value);		
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p) 
	{
		p.append("string "+idToString(id)+"="+value+"\n");		
	}
	
	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int len = ByteBufferReaderWriter.readint(in);

			value = "";
			for(int j=0;j<len;j++)
			{
				char c = (char)ByteBufferReaderWriter.readushort(in);
				value += c;
			}
		}
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyString);
		ByteBufferReaderWriter.writelong(o,id);
		int len = value.length();
		ByteBufferReaderWriter.writeint(o,len);
		for(int j=0;j<len;j++)
		{
			ByteBufferReaderWriter.writeushort(o, value.charAt(j));
		}
	}	
}

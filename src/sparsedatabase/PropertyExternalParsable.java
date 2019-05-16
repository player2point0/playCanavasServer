package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyExternalParsable extends Property 
{
	public String value;
	public Property parsed = null;
	public boolean dirty = false;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyExternalImage);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("extern");
	}
	public String typeString()
	{
		return "extern";
	}
	
	public PropertyExternalParsable() {}
	public PropertyExternalParsable(PropertyHashtable parent,String filepath,String name) 
	{
		id = Property.stringToID(name);		
		parent.AddProperty(this);		
		value = filepath;
	}
	
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyExternalParsable))
			return false;
		PropertyExternalParsable o = (PropertyExternalParsable)p;
		return o.value.contentEquals(value);
	}

	public void set(Property p)
	{
		PropertyExternalParsable pt = (PropertyExternalParsable)p;
		value = pt.value;
		parsed = pt.parsed;
		dirty = pt.dirty;
	}
	public Property copy()
	{
		PropertyExternalParsable pt = new PropertyExternalParsable();
		pt.value = value;
		pt.parsed = parsed;
		pt.dirty = dirty;
		pt.id = id;
		return pt;
	}
	
	public Property createInstance()
	{
		return new PropertyExternalImage();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("extern"))
	    {
	    	return new PropertyExternalParsable();
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
		if(dirty)
		{
			System.out.println("Parsable is dirty and needs to be saved");
		}
		p.push_back("extern "+idToString(id)+"="+value);		
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		p.push_back("extern "+idToString(id)+"="+value);		
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p) 
	{
		if(dirty)
		{
			System.out.println("Parsable is dirty and needs to be saved");
		}
		p.append("extern "+idToString(id)+"="+value+"\n");		
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
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyExternalParsable);
		ByteBufferReaderWriter.writelong(o,id);
		{
			int len = value.length();
			ByteBufferReaderWriter.writeint(o,len);
			for(int j=0;j<len;j++)
			{
				ByteBufferReaderWriter.writeushort(o, value.charAt(j));
			}
		}		
	}
}

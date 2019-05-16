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

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixLong;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyArrayString extends Property
{
	public GeneralMatrixString value;
//	public String[] value;
//	public int height = 0;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyArrayString);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyArrayString))
			return false;
		PropertyArrayString o = (PropertyArrayString)p;
		return o.value.isequal(value);
	}

	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("string[]");
	}
	public String typeString()
	{
		return "string[]";
	}

	public PropertyArrayString()
	{
		value = new GeneralMatrixString(1);
	}
	public PropertyArrayString(int size)
	{
		value = new GeneralMatrixString(1,size);
	}
	public PropertyArrayString(int size,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,size);
	}
	public PropertyArrayString(PropertyHashtable parent,int size,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,size);
		parent.AddProperty(this);
	}
	public PropertyArrayString(PropertyList parent,int size,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,size);
		parent.appendProperty(this);
	}
	public PropertyArrayString(GeneralMatrixString v,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,v.width*v.height);
		System.arraycopy(v.value, 0, value.value, 0, value.height);
	}
	public PropertyArrayString(PropertyHashtable parent,String[] v,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,v.length);
		System.arraycopy(v, 0, value.value, 0, value.height);
		parent.AddProperty(this);
	}
	public PropertyArrayString(PropertyList parent,String[] v,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,v.length);
		System.arraycopy(v, 0, value.value, 0, value.height);
		parent.appendProperty(this);
	}
	public PropertyArrayString(PropertyHashtable parent,String v,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,1);
		value.value[0] = v;
		parent.AddProperty(this);
	}
	public PropertyArrayString(PropertyHashtable parent,String v1,String v2,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1,2);
		value.value[0] = v1;
		value.value[1] = v2;
		parent.AddProperty(this);
	}

	public void set(Property p)
	{
		PropertyArrayString pt = (PropertyArrayString)p;
		value.setDimensions(1, pt.value.height);
        System.arraycopy(pt.value.value,0,value.value,0,value.height);  
	}
	public Property copy()
	{
		PropertyArrayString pt = new PropertyArrayString(value.height);
        System.arraycopy(value.value,0,pt.value.value,0,value.height);  
		pt.id = id;
		return pt;
	}

	public int find(String v)
    {
    	for(int i=0;i<(value.height);i++)
    	{
    		if(value.value[i].equalsIgnoreCase(v))
    			return i;
    	}
    	return -1;
    }

    public PropertyArrayString(PropertyHashtable parent,GeneralMatrixString contents,String name)
	{
		id = Property.stringToID(name);		
		//value = new GeneralMatrixString(contents);
		value = contents;
		parent.AddProperty(this);
	}

    public PropertyArrayString(PropertyList parent,GeneralMatrixString contents,String name)
	{
		id = Property.stringToID(name);		
		//value = new GeneralMatrixString(contents);
		value = contents;
		parent.appendProperty(this);
	}
	
    public PropertyArrayString(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		value = new GeneralMatrixString(1);
		parent.AddProperty(this);
	}
	
//    public void push_back_rows(GeneralMatrixString ss)
//    {
//    	for(int si=0;si<ss.height;si++)
//    	{
//    		push_back(ss.value[si]);
//    	}
//    }

//    public void removeRow(int index)
//    {
//    	if(index>=height)
//    	{
//    		System.out.println("Row being removed larger than array");
//    	}
//    	for(int i=index;i<((height-1));i++)
//    	{
//    		value[i] = value[(i)];
//    	}
//    	height--;
//    }
    
	public Property createInstance()
	{
		return new PropertyArrayString();
	}
    public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("string[]"))
	    {
	    	return new PropertyArrayString();
	    }
	    return null;
	}

	public int Parse(String value) 
	{ 
		int remainingEntries = Integer.parseInt(value);
		this.value = new GeneralMatrixString(1,remainingEntries);
		return remainingEntries; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = this.value.height-remainingEntries;
		remainingEntries--; 
		if(value!="")			
			this.value.value[i] = value;
		return remainingEntries; 
	}
	
	public static String toString(String name,GeneralMatrixString matrix)
	{
		String val = "string[] "+name+"="+matrix.height;
		
		for(int i=0;i<matrix.height;i++)
		{
			if(matrix.value[i]==null)
				val += ("\n");
			else
				val += (""+matrix.value[i]);
		}
		return val;
	}
	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("string[] "+idToString(id)+"="+value.height);
		
		for(int i=0;i<value.height;i++)
		{
			if(value.value[i]==null)
				pr.push_back("\n");
			else
				pr.push_back(value.value[i]);
		}
	}
	
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		pr.push_back("string[] "+idToString(id)+"="+value.height);
		properties.push_back(this);
		element.push_back(-1);
		for(int i=0;i<value.height;i++)
		{
			if(value.value[i]==null)
			{
				pr.push_back("\n");
			}
			else
				pr.push_back(value.value[i]);
			properties.push_back(this);
			element.push_back(i);
		}
	}

	public void SaveVerbose(PrintStream pr)
	{
		pr.append("string[] "+idToString(id)+"="+value.height+"\n");
		
		for(int i=0;i<value.height;i++)
		{
			if(value.value[i]==null)
				pr.append("\n");
			else
				pr.append(value.value[i]+"\n");
		}
	}

	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		int height = ByteBufferReaderWriter.readint(in);
		value.setDimensions(1, height);
		for(int i=0;i<height;i++)
		{
			int len = ByteBufferReaderWriter.readint(in);
			value.value[i] = "";
			for(int j=0;j<len;j++)
			{
				char c = (char)ByteBufferReaderWriter.readushort(in);
				value.value[i] += c;
			}
		}
	}

	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyArrayString);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,value.height);
		for(int i=0;i<value.height;i++)
		{
			int len = value.value[i].length();
			ByteBufferReaderWriter.writeint(o,len);
			for(int j=0;j<len;j++)
			{
				ByteBufferReaderWriter.writeushort(o, value.value[i].charAt(j));
			}
		}		
	}
	
}

package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;
import mathematics.hash.HashMatrixFloat;

public class PropertyHashMatrixFloat extends Property
{
	public HashMatrixFloat hash;
	
	public static GeneralMatrixFloat loadLine = new GeneralMatrixFloat();

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixFloat);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("float{}{}");
	}
	public String typeString()
	{
		return "float{}{}";
	}

	public PropertyHashMatrixFloat(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		hash = new HashMatrixFloat(0);
		parent.AddProperty(this);
	}
	
	public PropertyHashMatrixFloat(PropertyHashtable parent,HashMatrixFloat contents,String name)
	{
		id = Property.stringToID(name);		
		hash = contents;
		parent.AddProperty(this);
	}

	public PropertyHashMatrixFloat(PropertyHashtable parent,HashMatrixFloat contents,long id)
	{
		this.id = id;		
		hash = contents;
		parent.AddProperty(this);
	}

	public PropertyHashMatrixFloat()
	{
	}
	
	public void set(Property p)
	{
		PropertyHashMatrixFloat pt = (PropertyHashMatrixFloat)p;
		hash.set(pt.hash);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyHashMatrixFloat))
			return false;
		PropertyHashMatrixFloat o = (PropertyHashMatrixFloat)p;
		return o.hash.isequal(hash);
	}

	public Property copy()
	{
		PropertyHashMatrixFloat pt = new PropertyHashMatrixFloat();
		pt.hash = new HashMatrixFloat(hash);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixFloat();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("float{}{}"))
	    {
	    	return new PropertyHashMatrixFloat();
	    }	
		return null;
	}

	public boolean isEmpty()
	{
		return hash.num_elements==0;
	}

	public int Parse(String value) 
	{ 
		SplitTuple(value);
		hash = new HashMatrixFloat(Integer.parseInt(vals[0]));
		return Integer.parseInt(vals[1]); 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		remainingEntries--; 
		SplitTuple(value);
		long key = Long.parseLong(vals[0]);
		loadLine.setDimensions(hash.width, 1);
		for(int mi=0;mi<hash.width;mi++)
		{
			loadLine.value[mi] = Float.parseFloat(vals[mi+1]);
		}
		hash.add(key, loadLine.value, 0);
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("float{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements);

		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			String line = "";
			line = (""+hash.table[j]);
			for(int i=0;i<hash.width;i++)
			{
				line += (","+hash.values[i+j*hash.width]);
			}
			pr.push_back(line);
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		properties.push_back(this);
		element.push_back(-1);
		pr.push_back("float{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements);

		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			String line = "";
			line = (""+hash.table[j]);
			for(int i=0;i<hash.width;i++)
			{
				line += (","+hash.values[i+j*hash.width]);
			}
			pr.push_back(line);
			properties.push_back(this);
			element.push_back(j);
		}
	}

	public void SaveVerbose(PrintStream pr)
	{
		pr.append("float{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements+"\n");

		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			pr.append(""+hash.table[j]);
			for(int i=0;i<hash.width;i++)
			{
				pr.append(","+hash.values[i+j*hash.width]);
			}
			pr.append("\n");
		}
	}
	
	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int width = ByteBufferReaderWriter.readint(in);
			int height = ByteBufferReaderWriter.readint(in);
			
			hash = new HashMatrixFloat(width);
			loadLine.setDimensions(hash.width, 1);

			for(int j=0;j<height;j++)
			{
				long key = ByteBufferReaderWriter.readlong(in);
				for(int i=0;i<width;i++)
				{
					loadLine.value[i] = ByteBufferReaderWriter.readfloat(in);
				}
				hash.add(key, loadLine.value, 0);
			}
		}
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyHashMatrixFloat);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,hash.width);
		ByteBufferReaderWriter.writeint(o,hash.num_elements);
		
		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			ByteBufferReaderWriter.writelong(o, hash.table[j]);
			for(int i=0;i<hash.width;i++)
			{
				ByteBufferReaderWriter.writefloat(o, hash.values[i+j*hash.width]);
			}
		}
	}

}

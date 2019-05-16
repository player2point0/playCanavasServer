package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixLong;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;
import mathematics.hash.HashMatrixFloat;
import mathematics.hash.HashMatrixLong;

public class PropertyHashMatrixLong extends Property
{
	public HashMatrixLong hash;
	
	public static GeneralMatrixLong loadLine = new GeneralMatrixLong();

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixLong);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("long{}{}");
	}
	public String typeString()
	{
		return "long{}{}";
	}

	public PropertyHashMatrixLong(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		hash = new HashMatrixLong(0);
		parent.AddProperty(this);
	}
	
	public PropertyHashMatrixLong(PropertyHashtable parent,HashMatrixLong contents,String name)
	{
		id = Property.stringToID(name);		
		hash = contents;
		parent.AddProperty(this);
	}

	public PropertyHashMatrixLong(PropertyHashtable parent,HashMatrixLong contents,long id)
	{
		this.id = id;		
		hash = contents;
		parent.AddProperty(this);
	}

	public PropertyHashMatrixLong()
	{
	}
	
	public void set(Property p)
	{
		PropertyHashMatrixLong pt = (PropertyHashMatrixLong)p;
		hash.set(pt.hash);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyHashMatrixLong))
			return false;
		PropertyHashMatrixLong o = (PropertyHashMatrixLong)p;
		return o.hash.isequal(hash);
	}
	public Property copy()
	{
		PropertyHashMatrixLong pt = new PropertyHashMatrixLong();
		pt.hash = new HashMatrixLong(hash);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixLong();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("long{}{}"))
	    {
	    	return new PropertyHashMatrixLong();
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
		hash = new HashMatrixLong(Integer.parseInt(vals[0]));
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
			loadLine.value[mi] = Long.parseLong(vals[mi+1]);
		}
		hash.add(key, loadLine.value, 0);
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("long{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements+"\n");

		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			String line = (""+hash.table[j]);

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
		pr.push_back("long{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements+"\n");

		for(int j=0;j<hash.num_buckets;j++)
		{
			if((hash.table[j]==hash.delval)||(hash.table[j]==hash.emptyval))
				continue;
			String line = (""+hash.table[j]);

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
		pr.append("long{}{} "+idToString(id)+"="+hash.width+","+hash.num_elements+"\n");

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
			
			hash = new HashMatrixLong(width);
			loadLine.setDimensions(hash.width, 1);

			for(int j=0;j<height;j++)
			{
				long key = ByteBufferReaderWriter.readlong(in);
				for(int i=0;i<width;i++)
				{
					loadLine.value[i] = ByteBufferReaderWriter.readlong(in);
				}
				hash.add(key, loadLine.value, 0);
			}
		}
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyHashMatrixLong);
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
				ByteBufferReaderWriter.writelong(o, hash.values[i+j*hash.width]);
			}
		}
	}

}

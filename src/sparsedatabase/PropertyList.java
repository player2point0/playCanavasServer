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
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;
import mathematics.hash.HashLongArrayObject;

public class PropertyList extends Property 
{
	public GeneralMatrixObject entries;
	//public Property[]	entries;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyList);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("list");
	}
	public String typeString()
	{
		return "list";
	}

	public PropertyList()
	{
		entries = new GeneralMatrixObject(1);
	}
	public PropertyList(String name)
	{
		id = Property.stringToID(name);		
		entries = new GeneralMatrixObject(1);
	}
	public PropertyList(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		entries = new GeneralMatrixObject(1);
		parent.AddProperty(this);
	}
	public PropertyList(PropertyList parent,String name)
	{
		id = Property.stringToID(name);		
		entries = new GeneralMatrixObject(1);
		parent.appendProperty(this);
	}
	public PropertyList(int numEntries,String name)
	{
		id = Property.stringToID(name);		
		entries = new GeneralMatrixObject(1);
	}
	public PropertyList(int numEntries)
	{
		entries = new GeneralMatrixObject(1);
	}
	public PropertyList(PropertyHashtable parent,int numEntries,String name)
	{
		id = Property.stringToID(name);		
		entries = new GeneralMatrixObject(1,numEntries);
		parent.AddProperty(this);
	}
	
	public void set(Property p)
	{
		PropertyList pt = (PropertyList)p;
		entries = new GeneralMatrixObject(1,pt.entries.height);
		for(int i=0;i<entries.height;i++)
		{
			entries.value[i] = ((Property)pt.entries.value[i]).copy();
		}
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyList))
			return false;
		PropertyList o = (PropertyList)p;
		if(!(entries.width==o.entries.width))
			return false;
		if(!(entries.height==o.entries.height))
			return false;
		for(int i=0;i<entries.width*entries.height;i++)
		{
			if(!((Property)entries.value[i]).contentEquals((Property)o.entries.value[i]))
				return false;
		}
		return true;
	}

	public Property copy()
	{
		PropertyList pt = new PropertyList();
		pt.entries = new GeneralMatrixObject(1,entries.height);
		pt.id = id;
		for(int i=0;i<entries.height;i++)
		{
			pt.entries.value[i] = ((Property)entries.value[i]).copy();
		}
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyList();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("list"))
	    {
	    	return new PropertyList();
	    }	
		return null;
	}

	public long getRefOfSubProperty(Property p)
	{
		for(int i=0;i<entries.height;i++)
		{
			if(p.equals((Property)entries.value[i]))
				return i;
		}
		System.out.println("Error");
		return -1;
	}
	public void insertPropertyAfter(int index,Property p)
	{
		int i = entries.appendRow();
		if(entries.height!=1)
			System.arraycopy(entries, (index+1), entries, (index+2), (entries.height-1-(index+1)));
    	entries.value[index+1] = p;
	}
	public void removePropertyAt(int index)
	{
		if(index!=(entries.height-1))
			System.arraycopy(entries, (index+1), entries, (index), (entries.height-(index+1)));
		entries.height--;
	}
	public void appendProperty(Property p)
	{
		entries.push_back(p);
	}
    
    public int find(Property p)
    {
    	for(int i=0;i<entries.height;i++)
    	{
    		if(entries.value[i]==p)
    		{
    			return i;
    		}
    	}
    	return -1;
    }
            
	public void SaveVerbose(GeneralMatrixString pr)
	{
		int count = 0;
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				count++;
			}
		}
		pr.push_back("list "+idToString(id)+"="+count);
		
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				p.SaveVerbose(pr);
			}
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		int count = 0;
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				count++;
			}
		}
		pr.push_back("list "+idToString(id)+"="+count);
		properties.push_back(this);
		element.push_back(-1);

		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				p.SaveVerbose(pr,properties,element);
			}
		}
	}
	
	public void SaveVerbose(PrintStream pr)
	{
		int count = 0;
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				count++;
			}
		}
		pr.append("list "+idToString(id)+"="+count+"\n");
		
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				p.SaveVerbose(pr);
			}
		}
	}

	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int height = ByteBufferReaderWriter.readint(in);
			
			entries = new GeneralMatrixObject(1,height);
			for(int j=0;j<height;j++)
			{
				((Property)entries.value[j]).Parse(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		int count = 0;
		for(int i=0;i<entries.height;i++)
		{
			Property p = (Property)entries.value[i];
			if(p!=null)
			{
				count++;
			}
		}
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyList);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,count);
		{
			for(int i=0;i<entries.height;i++)
			{
				Property p = (Property)entries.value[i];
				if(p!=null)
				{
					p.SaveBinary(o);
				}
			}
		}		
	}

	public int Parse(String value) 
	{ 
		int remainingEntries = Integer.parseInt(value); 
		entries = new GeneralMatrixObject(1,remainingEntries);
		return remainingEntries; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = entries.height-remainingEntries;
		remainingEntries--; 
		Property p = PropertyFactory.ParseProperty(value,in,indent+1);
		entries.value[i] = p;		
		p.parent = this;
		return remainingEntries; 
	}
}
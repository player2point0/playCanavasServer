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
import mathematics.hash.HashLongArrayObject;
import mathematics.hash.HashLongArrayObjectIterator;
import mathematics.hash.HashMatrixLong;

public class PropertyHashtable extends Property
{
	public HashLongArrayObject entries = new HashLongArrayObject();
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyHashtable);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("dict");
	}
	public String typeString()
	{
		return "dict";
	}

	public PropertyHashtable() 
	{
	}
	
	public PropertyHashtable(String name)
	{
		id = Property.stringToID(name);		
	}

	public PropertyHashtable(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		if(parent!=null)
			parent.AddProperty(this);
	}
	
	public PropertyHashtable(PropertyHashtable parent,long id)
	{
		this.id = id;		
		if(parent!=null)
			parent.AddProperty(this);
	}
	
	public void set(Property p)
	{
		PropertyHashtable pt = (PropertyHashtable)p;
		entries.set(pt.entries);
		for(int i=0;i<entries.num_buckets;i++)
		{
			if(
					(entries.table[i]==entries.emptyval)||
					(entries.table[i]==entries.delval)
					)
				continue;

			//is there anything in the slot
			Property pe = (Property)entries.values[i];
			entries.values[i] = pe.copy();
		}
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyHashtable))
			return false;
		PropertyHashtable o = (PropertyHashtable)p;
		if(o.entries.num_elements!=entries.num_elements)
			return false;
		for(int i=0;i<entries.num_buckets;i++)
		{
			if(
					(entries.table[i]==entries.emptyval)||
					(entries.table[i]==entries.delval)
					)
				continue;

			//is there anything in the slot
			Property pe = (Property)entries.values[i];
			Property ope = o.GetProperty(pe.id);
			if(ope==null)
				return false;
			if(!pe.contentEquals(ope))
				return false;
		}		
		return true;
	}

	public void get(GeneralMatrixObject pentries)
	{
		for(int i=0;i<entries.num_buckets;i++)
		{
			if(
					(entries.table[i]==entries.emptyval)||
					(entries.table[i]==entries.delval)
					)
				continue;

			//is there anything in the slot
			Property pe = (Property)entries.values[i];
			pentries.push_back(pe);
		}
	}
	public Property copy()
	{
		PropertyHashtable pt = new PropertyHashtable();
		pt.entries = new HashLongArrayObject(entries);
		pt.id = id;
		for(int i=0;i<pt.entries.num_buckets;i++)
		{
			if(
					(pt.entries.table[i]==entries.emptyval)||
					(pt.entries.table[i]==entries.delval)
					)
				continue;

			//is there anything in the slot
			Property pe = (Property)pt.entries.values[i];
			pt.entries.values[i] = pe.copy();
		}
		return pt;
	}
	public void clear()
	{
		entries.clearToEmpty();
	}
	
	public Property createInstance()
	{
		return new PropertyHashtable();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("dict"))
	    {
	    	return new PropertyHashtable();
	    }			
		return null;
	}
	public long getRefOfSubProperty(Property p)
	{
		return p.id;
	}
	public void AddProperty(Property p)
	{
		Property existing = GetProperty(p.id);
		if(existing!=null)
		{
			System.out.println("Removing "+Property.idToString(existing.id));
			RemoveProperty(existing);
		}
		
		p.parent = this;
		
		entries.set(p.id, p);
	}

	public void AddPropertyRenameIfNonUnique(Property p)
	{
		Property existing = GetProperty(p.id);
		if(existing!=null)
		{
			p.id = newUniqueId(idToString(p.id));
		}
		
		p.parent = this;
		
		entries.set(p.id, p);
	}

	public Property GetProperty(String id)
	{
		return GetProperty(Property.stringToID(id));
	}
	
	public Property GetProperty(long id)
	{
		Property p = (Property)entries.get(id);
		return p;
	}

	public void RemoveProperty(Property p)
	{
		if(p==null)
			return;
	
		entries.erase(p.id);	
	}
	public Property RemoveProperty(long id)
	{
		return (Property)entries.erase(id);	
	}
	public Property RemoveProperty(String sid)
	{
		long id = Property.stringToID(sid);
		return (Property)entries.erase(id);	
	}

	public long newUniqueId(String posName,GeneralMatrixLong keywords)
	{
		long pid = Property.stringToID(posName);
		if(entries.exists(pid)||(keywords.find(pid)!=-1))
		{
			//try with a 1 digit postfix
			for(int i=0;i<10;i++)
			{
				pid = Property.stringToID(posName, i, 1);
				if(!(entries.exists(pid)||(keywords.find(pid)!=-1)))
				{
					return pid;
				}
			}
			//try with a 2 digit postfix
			for(int i=0;i<100;i++)
			{
				pid = Property.stringToID(posName, i, 2);
				if(!(entries.exists(pid)||(keywords.find(pid)!=-1)))
				{
					return pid;
				}
			}
			//try with a 3 digit postfix
			for(int i=0;i<1000;i++)
			{
				pid = Property.stringToID(posName, i, 3);
				if(!(entries.exists(pid)||(keywords.find(pid)!=-1)))
				{
					return pid;
				}
			}
			System.out.println("Can't find a unique id for this string");
		}
		return pid;
	}

	public long newUniqueId(String posName)
	{
		long pid = Property.stringToID(posName);
		if(entries.exists(pid))
		{
			//try with a 1 digit postfix
			for(int i=0;i<10;i++)
			{
				pid = Property.stringToID(posName, i, 1);
				if(!entries.exists(pid))
				{
					return pid;
				}
			}
			//try with a 2 digit postfix
			for(int i=0;i<100;i++)
			{
				pid = Property.stringToID(posName, i, 2);
				if(!entries.exists(pid))
				{
					return pid;
				}
			}
			//try with a 3 digit postfix
			for(int i=0;i<1000;i++)
			{
				pid = Property.stringToID(posName, i, 3);
				if(!entries.exists(pid))
				{
					return pid;
				}
			}
			System.out.println("Can't find a unique id for this string");
		}
		return pid;
	}

	public static boolean exists(long pid,GeneralMatrixObject entries)
	{
		for(int i=0;i<entries.height;i++)
		{
			Property pc = (Property)entries.value[i];
			if(pc.id==pid)
			{
				return true;
			}
		}
		return false;
	}
	
	public static long newUniqueId(String posName,GeneralMatrixObject entries)
	{
		long pid = Property.stringToID(posName);
		if(exists(pid,entries))
		{
			//try with a 1 digit postfix
			for(int i=0;i<10;i++)
			{
				pid = Property.stringToID(posName, i, 1);
				if(!exists(pid,entries))
				{
					return pid;
				}
			}
			//try with a 2 digit postfix
			for(int i=0;i<100;i++)
			{
				pid = Property.stringToID(posName, i, 2);
				if(!exists(pid,entries))
				{
					return pid;
				}
			}
			//try with a 3 digit postfix
			for(int i=0;i<1000;i++)
			{
				pid = Property.stringToID(posName, i, 3);
				if(!exists(pid,entries))
				{
					return pid;
				}
			}
			System.out.println("Can't find a unique id for this string");
		}
		return pid;
	}
	
	public void SaveVerbose(GeneralMatrixString pr)
	{
		HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
		
		int numEntries = entries.size();
		
		pr.push_back("dict "+idToString(id)+"="+numEntries);
		
		while(itr.hasMore(entries))
		{
			Property p = (Property)itr.get(entries);
			p.SaveVerbose(pr);
			itr.nextValid(entries);
		}		
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
		
		int numEntries = entries.size();
		
		pr.push_back("dict "+idToString(id)+"="+numEntries);
		properties.push_back(this);
		element.push_back(-1);

		while(itr.hasMore(entries))
		{
			Property p = (Property)itr.get(entries);
			p.SaveVerbose(pr,properties,element);
			itr.nextValid(entries);
		}		
	}
	
	public void SaveVerbose(PrintStream pr)
	{
		HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
		
		int numEntries = entries.size();
		
		pr.append("dict "+idToString(id)+"="+numEntries+"\n");
		
		while(itr.hasMore(entries))
		{
			Property p = (Property)itr.get(entries);
			p.SaveVerbose(pr);
			itr.nextValid(entries);
		}		
	}
	
	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int len = ByteBufferReaderWriter.readint(in);
			
			for(int j=0;j<len;j++)
			{
				int type = in.read();
				if(type==-1)
				{
					System.out.println("Error parsing hashtable");
					return;
				}
				try
				{
					Property p = (Property)PropertyFactory.types[type].getClass().newInstance();
					p.Parse(in);
					AddProperty(p);
				}
				catch(Exception e)
				{
					
				}
			}
		}
	}
	
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyHashtable);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,entries.size());

		HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
		while(itr.hasMore(entries))
		{
			Property p = (Property)itr.get(entries);
			p.SaveBinary(o);
			itr.nextValid(entries);
		}		
	}
	
	public int Parse(String value) { int remainingEntries = Integer.parseInt(value); return remainingEntries; }
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		remainingEntries--; 
		Property p = PropertyFactory.ParseProperty(value,in,indent+1);
		AddProperty(p);		
		return remainingEntries; 
	}
}

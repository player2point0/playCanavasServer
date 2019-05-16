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
import java.io.StringReader;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixLong;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyReference extends Property
{
	public long[] array;
	Property reference;
	static GeneralMatrixInt depth = new GeneralMatrixInt(1,1);
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyReference);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("ref");
	}
	public String typeString()
	{
		return "ref";
	}

	public static PropertyReference createReference(Property p)
	{
		//Work out the depth of the property
		depth.value[0] = 0;
		Property root = p.getRootParent(depth);
		//If in the 
		//if(root.equals(sd))
		{
			Property subProperty = p;
			Property parent = p.parent;
			PropertyReference ref = new PropertyReference(depth.value[0]);
			ref.array[0] = -2;
			for(int i=(depth.value[0]-1);i>0;i--)
			{
				ref.array[i] = parent.getRefOfSubProperty(subProperty);
				subProperty = parent;
				parent = subProperty.parent;
			}
			return ref;
		}
		//return null;
	}
	public PropertyReference(int depth)
	{
		array = new long[depth];
	}
	public PropertyReference()
	{
	}
	
	public void set(Property p)
	{
		PropertyReference pt = (PropertyReference)p;
		array = new long[pt.array.length];
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyReference))
			return false;
		PropertyReference o = (PropertyReference)p;
		if(array.length!=o.array.length)
			return false;
		for(int i=0;i<array.length;i++)
		{
			if(array[i]!=o.array[i])
				return false;
		}
		return true;
	}

	public Property copy()
	{
		PropertyReference pt = new PropertyReference(array.length);
		System.arraycopy(array, 0, pt.array, 0, array.length);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyReference(4);
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("ref"))
	    {
	    	return new PropertyReference(4);
	    }	
		return null;
	}

	public Property updateContext(Property currentContext)
	{
		for(int i=0;i<array.length;i++)
		{
			if(array[i]==-2)
			{
				currentContext = currentContext.getRootParent(null);
			}
			else
			if(array[i]==-1)
			{
				currentContext = currentContext.parent;
			}
			else
			{
				if(currentContext.parent==null)
				{
					PropertyHashtable root = (PropertyHashtable)currentContext;
					//If necesary entry has been loaded if not load it
					currentContext = root.GetProperty(array[i]);
					if(currentContext==null)
					{
						System.out.println("Error");
						//currentContext = root.LoadEntry(array[i], ""+array[i]+".txt");
					}
				}
				else
				{
					if(currentContext.getClass().equals(PropertyHashtable.class))
					{
						PropertyHashtable table = (PropertyHashtable)currentContext;
						currentContext = table.GetProperty(array[i]);
					}
					else
					if(currentContext.getClass().equals(PropertyList.class))
					{
						PropertyList list = (PropertyList)currentContext;
						currentContext = (Property)list.entries.value[(int)array[i]];
					}
				}
			}
		}
		return currentContext;
	}
	
	public int Parse(String value) 
	{ 
		int remainingEntries = 0;
		//Find the number of entries
		try
		{
			int c;
		    StringReader sin = new StringReader(value);
		    remainingEntries=1; //Minimum of 1

			    while((c = sin.read()) != -1)
		        {
		        	if((c=='/')||(c=='\\'))
		        	{
		        		remainingEntries++;
		        	}
		        }
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
		}
		
		array = new long[remainingEntries];
		//Now parse 
		try
		{
			String val="";
			int c;
		    StringReader sin = new StringReader(value);
		    int i=0; //Minimum of 1
			    while((c = sin.read()) != -1)
		        {
		        	if((c=='/')||(c=='\\'))
		        	{
		        		if(val.contentEquals("."))
		        		{
		        			array[i] = -2;
		        		}
		        		else
			        	if(val.contentEquals(".."))
			        	{
			        		array[i] = -1;
			        	}
			        	else
		        		{
		        			array[i] = Property.stringToID(val);
		        		}
		        		i++;
		        		val="";
		        	}
		        	else
		        	{
		        		val += (char)c;
		        	}
		        }
        		if(val.contentEquals("."))
        		{
        			array[i] = -2;
        		}
        		else
            	if(val.contentEquals(".."))
            	{
            		array[i] = -1;
            	}
            	else
        		{
        			array[i] = Property.stringToID(val);
        		}
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
		}
		return 0; 
	}
	public String getName(long entry)
	{
		if(entry==-2)
			return ".";
		if(entry==-1)
			return "..";
		return Property.idToString(entry);
	}
	public void SaveVerbose(GeneralMatrixString pr)
	{
		String line = ("ref "+idToString(id)+"="+getName(array[0]));
		for(int i=1;i<array.length;i++)
		{
			line+=("/"+getName(array[i]));
		}
		pr.push_back(line);
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		String line = ("ref "+idToString(id)+"="+getName(array[0]));
		for(int i=1;i<array.length;i++)
		{
			line+=("/"+getName(array[i]));
		}
		pr.push_back(line);
		properties.push_back(this);
		element.push_back(-1);
	}
	public void SaveVerbose(PrintStream pr)
	{
		pr.append("ref "+idToString(id)+"="+getName(array[0]));
		for(int i=1;i<array.length;i++)
		{
			pr.append("/"+getName(array[i]));
		}
		pr.append("\n");
	}
	
	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int len = ByteBufferReaderWriter.readint(in);

			array = new long[len];
			
			for(int i=0;i<len;i++)
			{
				array[i] = ByteBufferReaderWriter.readlong(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyReference);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,array.length);
		
		for(int i=0;i<array.length;i++)
		{
			ByteBufferReaderWriter.writelong(o, array[i]);
		}
	}	
}

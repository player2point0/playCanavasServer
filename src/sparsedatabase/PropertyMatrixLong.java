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
import mathematics.GeneralMatrixLong;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyMatrixLong extends Property 
{
	//Set of edits that can be made to this object
	public static final int ADD_ROW=0;
	public static final int REMOVE_ROW=1;
	public static final int SET_ENTRY=2;
	
	public GeneralMatrixLong matrix;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixLong);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("long[][]");
	}
	public String typeString()
	{
		return "long[][]";
	}

	public PropertyMatrixLong(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixLong();
		parent.AddProperty(this);
	}
	
	public PropertyMatrixLong(PropertyHashtable parent,GeneralMatrixLong contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
		if(parent!=null)
			parent.AddProperty(this);
	}

	public PropertyMatrixLong(GeneralMatrixLong contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
	}

	public PropertyMatrixLong()
	{
	}
	
	public void set(Property p)
	{
		PropertyMatrixLong pt = (PropertyMatrixLong)p;
		matrix.set(pt.matrix);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyMatrixLong))
			return false;
		PropertyMatrixLong o = (PropertyMatrixLong)p;
		return o.matrix.isequal(matrix);
	}

	public Property copy()
	{
		PropertyMatrixLong pt = new PropertyMatrixLong();
		pt.matrix = new GeneralMatrixLong(matrix);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixLong();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("long[][]"))
	    {
	    	return new PropertyMatrixLong();
	    }	
		return null;
	}

	public boolean isEmpty()
	{
		return matrix.height==0;
	}

	public int Parse(String value) 
	{ 
		SplitTuple(value);
		matrix = new GeneralMatrixLong(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		return matrix.height; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = matrix.height-remainingEntries;
		remainingEntries--; 
		SplitTuple(value);
		for(int mi=0;mi<matrix.width;mi++)
		{
			matrix.set(mi, i, Long.parseLong(vals[mi]));
		}
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("long[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					line+=(",");
				line+=(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		properties.push_back(this);
		element.push_back(-1);
		pr.push_back("long[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					line+=(",");
				line+=(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
			properties.push_back(this);
			element.push_back(j);
		}
	}
	
	public void SaveVerbose(PrintStream pr)
	{
		pr.append("long[][] "+idToString(id)+"="+matrix.width+","+matrix.height+"\n");

		for(int j=0;j<matrix.height;j++)
		{
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					pr.append(",");
				pr.append(""+matrix.value[i+j*matrix.width]);
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
			
			matrix = new GeneralMatrixLong(width,height);
			
			int numEntries = width*height;

			for(int i=0;i<numEntries;i++)
			{
				matrix.value[i] = ByteBufferReaderWriter.readlong(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyMatrixInt);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,matrix.width);
		ByteBufferReaderWriter.writeint(o,matrix.height);
		
		int numEntries = matrix.width*matrix.height;
		{
			for(int i=0;i<numEntries;i++)
			{
				ByteBufferReaderWriter.writelong(o, matrix.value[i]);
			}
		}		
	}	
}

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
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyMatrixInt extends Property 
{
	//Set of edits that can be made to this object
	public static final int ADD_ROW=0;
	public static final int REMOVE_ROW=1;
	public static final int SET_ENTRY=2;
	
	public GeneralMatrixInt matrix;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixInt);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("int[][]");
	}
	public String typeString()
	{
		return "int[][]";
	}

	public PropertyMatrixInt(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixInt();
		parent.AddProperty(this);
	}
	
	public PropertyMatrixInt(PropertyHashtable parent,GeneralMatrixInt contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
		if(parent!=null)
			parent.AddProperty(this);
	}

	public PropertyMatrixInt(GeneralMatrixInt contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
	}

	public PropertyMatrixInt(GeneralMatrixInt contents)
	{
		matrix = contents;
	}

	public PropertyMatrixInt()
	{
	}
	
	public void set(Property p)
	{
		PropertyMatrixInt pt = (PropertyMatrixInt)p;
		matrix.set(pt.matrix);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyMatrixInt))
			return false;
		PropertyMatrixInt o = (PropertyMatrixInt)p;
		return o.matrix.isequal(matrix);
	}

	public Property copy()
	{
		PropertyMatrixInt pt = new PropertyMatrixInt();
		pt.matrix = new GeneralMatrixInt(matrix);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixInt();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("int[][]"))
	    {
	    	return new PropertyMatrixInt();
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
		matrix = new GeneralMatrixInt(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		return matrix.height; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = matrix.height-remainingEntries;
		remainingEntries--; 
		SplitTuple(value);
		for(int mi=0;mi<matrix.width;mi++)
		{
			matrix.set(mi, i, Integer.parseInt(vals[mi]));
		}
		return remainingEntries; 
	}

	public static String SaveVerbose(GeneralMatrixInt matrix)
	{
		String pr = "";
		pr+=("int[][] "+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					line+=(",");
				line+=(""+matrix.value[i+j*matrix.width]);
			}
			pr+="\n"+(line);
		}
		return pr;
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("int[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

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
		pr.push_back("int[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

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
		pr.append("int[][] "+idToString(id)+"="+matrix.width+","+matrix.height+"\n");

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
			
			matrix = new GeneralMatrixInt(width,height);
			
			int numEntries = width*height;

			for(int i=0;i<numEntries;i++)
			{
				matrix.value[i] = ByteBufferReaderWriter.readint(in);
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
				ByteBufferReaderWriter.writeint(o, matrix.value[i]);
			}
		}		
	}
	
}

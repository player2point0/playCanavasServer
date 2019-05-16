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

import mathematics.GeneralMatrixDouble;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyMatrixFloat extends Property 
{	
	public GeneralMatrixFloat matrix;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixFloat);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("float[][]");
	}
	public String typeString()
	{
		return "float[][]";
	}

	public PropertyMatrixFloat(String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixFloat();
	}
	
	public PropertyMatrixFloat(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixFloat();
		parent.AddProperty(this);
	}
	
	public PropertyMatrixFloat(PropertyHashtable parent,GeneralMatrixFloat contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
		parent.AddProperty(this);
	}

	public PropertyMatrixFloat(PropertyHashtable parent,GeneralMatrixFloat contents,long id)
	{
		this.id = id;		
		matrix = contents;
		parent.AddProperty(this);
	}

	public PropertyMatrixFloat(GeneralMatrixFloat contents)
	{
		matrix = contents;
	}

	public PropertyMatrixFloat(GeneralMatrixFloat contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
	}

	public PropertyMatrixFloat()
	{
	}
	
	public void set(Property p)
	{
		PropertyMatrixFloat pt = (PropertyMatrixFloat)p;
		matrix.set(pt.matrix);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyMatrixFloat))
			return false;
		PropertyMatrixFloat o = (PropertyMatrixFloat)p;
		return o.matrix.isequal(matrix);
	}

	public Property copy()
	{
		PropertyMatrixFloat pt = new PropertyMatrixFloat();
		pt.matrix = new GeneralMatrixFloat(matrix);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixFloat();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("float[][]"))
	    {
	    	return new PropertyMatrixFloat();
	    }	
		return null;
	}

	public boolean isEmpty()
	{
		return matrix.height==0;
	}

	public static void parse(String value,GeneralMatrixFloat matrix)
	{
		String[] lines = value.split("\n");
		int ind = lines[0].indexOf('=');
		SplitTuple(lines[0].substring(ind+1));
		matrix.setDimensions(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		for(int i=0;i<matrix.height;i++)
		{
			SplitTuple(lines[i+1]);
			for(int mi=0;mi<matrix.width;mi++)
			{
				matrix.set(mi, i, Float.parseFloat(vals[mi]));
			}			
		}
	}
	public int Parse(String value) 
	{ 
		SplitTuple(value);
		matrix = new GeneralMatrixFloat(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		return matrix.height; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = matrix.height-remainingEntries;
		remainingEntries--; 
		if(matrix.height*matrix.width>10000)
		{
			System.out.println(""+remainingEntries+"/"+matrix.height);
		}
		SplitTuple(value);
		for(int mi=0;mi<matrix.width;mi++)
		{
			matrix.set(mi, i, Float.parseFloat(vals[mi]));
		}
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("float[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					line += (",");
				line+=(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		properties.push_back(this);
		element.push_back(-1);
		pr.push_back("float[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					line += (",");
				line+=(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
			properties.push_back(this);
			element.push_back(j);
		}
	}
	public static String toString(String name,GeneralMatrixFloat matrix)
	{
		String val = "float[][] "+name+"="+matrix.width+","+matrix.height+"\n";
		for(int j=0;j<matrix.height;j++)
		{
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
					val += (",");
				val += (""+matrix.value[i+j*matrix.width]);
			}
			val += ("\n");
		}
		return val;
	}
	public void SaveVerbose(PrintStream pr)
	{
		pr.append("float[][] "+idToString(id)+"="+matrix.width+","+matrix.height+"\n");

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
			
			matrix = new GeneralMatrixFloat(width,height);
			
			int numEntries = width*height;

			for(int i=0;i<numEntries;i++)
			{
				matrix.value[i] = ByteBufferReaderWriter.readfloat(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyMatrixFloat);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,matrix.width);
		ByteBufferReaderWriter.writeint(o,matrix.height);
		
		int numEntries = matrix.width*matrix.height;
		{
			for(int i=0;i<numEntries;i++)
			{
				ByteBufferReaderWriter.writefloat(o, matrix.value[i]);
			}
		}		
	}
	
}

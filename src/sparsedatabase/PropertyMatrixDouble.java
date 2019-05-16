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
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyMatrixDouble extends Property 
{	
	public GeneralMatrixDouble matrix;

	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixDouble);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("double[][]");
	}
	public String typeString()
	{
		return "double[][]";
	}

	public PropertyMatrixDouble(GeneralMatrixDouble contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
	}

	public PropertyMatrixDouble(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixDouble();
		parent.AddProperty(this);
	}
	
	public PropertyMatrixDouble(PropertyHashtable parent,GeneralMatrixDouble contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
		parent.AddProperty(this);
	}

	public PropertyMatrixDouble(PropertyHashtable parent,GeneralMatrixDouble contents,long id)
	{
		this.id = id;		
		matrix = contents;
		parent.AddProperty(this);
	}

	public PropertyMatrixDouble()
	{
	}
	
	public void set(Property p)
	{
		PropertyMatrixDouble pt = (PropertyMatrixDouble)p;
		matrix.set(pt.matrix);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyMatrixDouble))
			return false;
		PropertyMatrixDouble o = (PropertyMatrixDouble)p;
		return o.matrix.isequal(matrix);
	}

	public Property copy()
	{
		PropertyMatrixDouble pt = new PropertyMatrixDouble();
		pt.matrix = new GeneralMatrixDouble(matrix);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixDouble();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("double[][]"))
	    {
	    	return new PropertyMatrixDouble();
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
		matrix = new GeneralMatrixDouble(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		return matrix.height; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = matrix.height-remainingEntries;
		remainingEntries--; 
		SplitTuple(value);
		for(int mi=0;mi<matrix.width;mi++)
		{
			matrix.set(mi, i, Double.parseDouble(vals[mi]));
		}
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("double[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
				{
					line += (",");
				}
				line += (""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		properties.push_back(this);
		element.push_back(-1);
		pr.push_back("double[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
				if(i!=0)
				{
					line += (",");
				}
				line += (""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
			properties.push_back(this);
			element.push_back(j);
		}
	}
	
	public void SaveVerbose(PrintStream pr)
	{
		pr.append("double[][] "+idToString(id)+"="+matrix.width+","+matrix.height+"\n");

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
			
			matrix = new GeneralMatrixDouble(width,height);
			
			int numEntries = width*height;

			for(int i=0;i<numEntries;i++)
			{
				matrix.value[i] = ByteBufferReaderWriter.readdouble(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyMatrixDouble);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,matrix.width);
		ByteBufferReaderWriter.writeint(o,matrix.height);
		
		int numEntries = matrix.width*matrix.height;
		{
			for(int i=0;i<numEntries;i++)
			{
				ByteBufferReaderWriter.writedouble(o, matrix.value[i]);
			}
		}		
	}
	
}

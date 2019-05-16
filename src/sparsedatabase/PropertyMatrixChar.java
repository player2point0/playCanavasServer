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

import mathematics.GeneralMatrixChar;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyMatrixChar extends Property 
{
	//Set of edits that can be made to this object
	public static final int ADD_ROW=0;
	public static final int REMOVE_ROW=1;
	public static final int SET_ENTRY=2;
	
	public GeneralMatrixChar matrix;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyMatrixChar);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("char[][]");
	}
	public String typeString()
	{
		return "char[][]";
	}

	public PropertyMatrixChar(PropertyHashtable parent,String name)
	{
		id = Property.stringToID(name);		
		matrix = new GeneralMatrixChar();
		parent.AddProperty(this);
	}
	
	public PropertyMatrixChar(PropertyHashtable parent,GeneralMatrixChar contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
		if(parent!=null)
			parent.AddProperty(this);
	}

	public PropertyMatrixChar(GeneralMatrixChar contents,String name)
	{
		id = Property.stringToID(name);		
		matrix = contents;
	}

	public PropertyMatrixChar(GeneralMatrixChar contents)
	{
		matrix = contents;
	}

	public PropertyMatrixChar()
	{
	}
	
	public void set(Property p)
	{
		PropertyMatrixChar pt = (PropertyMatrixChar)p;
		matrix.set(pt.matrix);
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyMatrixChar))
			return false;
		PropertyMatrixChar o = (PropertyMatrixChar)p;
		return o.matrix.isequal(matrix);
	}

	public Property copy()
	{
		PropertyMatrixChar pt = new PropertyMatrixChar();
		pt.matrix = new GeneralMatrixChar(matrix);
		pt.id = id;
		return pt;
	}
	public Property createInstance()
	{
		return new PropertyMatrixChar();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("char[][]"))
	    {
	    	return new PropertyMatrixChar();
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
		matrix = new GeneralMatrixChar(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
		return matrix.height; 
	}
	public int ParseMultiline(String value,BufferedReader in,int remainingEntries,int indent) 
	{ 
		int i = matrix.height-remainingEntries;
		remainingEntries--; 
		//SplitTuple(value);
		char[] cs = value.toCharArray();
		System.arraycopy(cs,0,matrix.value, i*matrix.width, matrix.width);
		return remainingEntries; 
	}

	public void SaveVerbose(GeneralMatrixString pr)
	{
		pr.push_back("char[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
//				if(i!=0)
//					pr.append(",");
				line += (""+matrix.value[i+j*matrix.width]);
				//pr.push_back(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
		}
	}
	public void SaveVerbose(GeneralMatrixString pr,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		properties.push_back(this);
		element.push_back(-1);
		pr.push_back("char[][] "+idToString(id)+"="+matrix.width+","+matrix.height);

		for(int j=0;j<matrix.height;j++)
		{
			String line = "";
			for(int i=0;i<matrix.width;i++)
			{
//				if(i!=0)
//					pr.append(",");
				line += (""+matrix.value[i+j*matrix.width]);
				//pr.push_back(""+matrix.value[i+j*matrix.width]);
			}
			pr.push_back(line);
			properties.push_back(this);
			element.push_back(j);
		}
	}
	public void SaveVerbose(PrintStream pr)
	{
		pr.append("char[][] "+idToString(id)+"="+matrix.width+","+matrix.height+"\n");

		for(int j=0;j<matrix.height;j++)
		{
			for(int i=0;i<matrix.width;i++)
			{
//				if(i!=0)
//					pr.append(",");
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
			
			matrix = new GeneralMatrixChar(width,height);
			
			int numEntries = width*height;

			for(int i=0;i<numEntries;i++)
			{
				matrix.value[i] = (char)ByteBufferReaderWriter.readushort(in);
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyMatrixChar);
		ByteBufferReaderWriter.writelong(o,id);
		ByteBufferReaderWriter.writeint(o,matrix.width);
		ByteBufferReaderWriter.writeint(o,matrix.height);
		
		int numEntries = matrix.width*matrix.height;
		{
			for(int i=0;i<numEntries;i++)
			{
				ByteBufferReaderWriter.writeushort(o, matrix.value[i]);
			}
		}		
	}
	
}

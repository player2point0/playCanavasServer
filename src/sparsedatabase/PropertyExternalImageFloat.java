/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import importexport.ByteBufferReaderWriter;
import importexport.image.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

import rendering.RenderBuffer;
import rendering.RenderBufferFloat;

public class PropertyExternalImageFloat extends Property
{
	public static RenderBuffer tempBuffer = new RenderBuffer();
	public static RenderBuffer tempBuffer2 = new RenderBuffer();
	
	public String value;
	public RenderBufferFloat image;
	public boolean dirty = false;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyExternalImageFloat);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("imagefloat");
	}
	public String typeString()
	{
		return "imagefloat";
	}

	public PropertyExternalImageFloat() {}
	
	public void set(Property p)
	{
		PropertyExternalImageFloat pt = (PropertyExternalImageFloat)p;
		value = pt.value;
		image = pt.image;
		dirty = pt.dirty;
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyExternalImageFloat))
			return false;
		PropertyExternalImageFloat o = (PropertyExternalImageFloat)p;
		return o.value.contentEquals(value);
	}

	public Property copy()
	{
		PropertyExternalImageFloat pt = new PropertyExternalImageFloat();
		pt.value = value;
		pt.image = image;
		pt.dirty = dirty;
		pt.id = id;
		return pt;
	}
	public RenderBufferFloat getImage()
	{
		if(image==null)
		{
			image = new RenderBufferFloat();
			ImageLoader.loadFloatBufferFrom2ImagesURL(value, image, tempBuffer, tempBuffer2, Float.MAX_VALUE);
		}
		return image;
	}
	
	public Property createInstance()
	{
		return new PropertyExternalImageFloat();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("imagefloat"))
	    {
	    	return new PropertyExternalImageFloat();
	    }	
		return null;
	}

	public int Parse(String v)
	{
		value = v;
		return 0;
	}
	public void SaveVerbose(GeneralMatrixString p) 
	{
		if(dirty)
		{
			int error = (image.width/3);
			error = error*3;
			error = image.width-error;
			tempBuffer.ensureCapacity(((image.width/3)+error)*image.height);
			tempBuffer.width = (image.width/3)+error;
			tempBuffer.height = image.height;
			ImageLoader.saveFloatBufferAs2Images(value, image, tempBuffer, Float.MAX_VALUE);
			dirty = false;
		}
		p.push_back("imagefloat "+idToString(id)+"="+value);		
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		p.push_back("imagefloat "+idToString(id)+"="+value);		
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p) 
	{
		if(dirty)
		{
			int error = (image.width/3);
			error = error*3;
			error = image.width-error;
			tempBuffer.ensureCapacity(((image.width/3)+error)*image.height);
			tempBuffer.width = (image.width/3)+error;
			tempBuffer.height = image.height;
			ImageLoader.saveFloatBufferAs2Images(value, image, tempBuffer, Float.MAX_VALUE);
			dirty = false;
		}
		p.append("imagefloat "+idToString(id)+"="+value+"\n");		
	}
	
	public void Parse(InputStream in) throws IOException
	{
		id = ByteBufferReaderWriter.readlong(in);
		{
			int len = ByteBufferReaderWriter.readint(in);
			value = "";
			for(int j=0;j<len;j++)
			{
				char c = (char)ByteBufferReaderWriter.readushort(in);
				value += c;
			}
		}
	}
	public void SaveBinary(OutputStream o) throws IOException
	{
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyExternalImageFloat);
		ByteBufferReaderWriter.writelong(o,id);
		{
			int len = value.length();
			ByteBufferReaderWriter.writeint(o,len);
			for(int j=0;j<len;j++)
			{
				ByteBufferReaderWriter.writeushort(o, value.charAt(j));
			}
		}		
	}
}

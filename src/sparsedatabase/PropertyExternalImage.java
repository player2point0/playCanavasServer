/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import imageprocessing.Debayer;
import importexport.ByteBufferReaderWriter;
import importexport.image.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

import rendering.RenderBuffer;

public class PropertyExternalImage extends Property
{
	public String value;
	public RenderBuffer image;
	public boolean dirty = false;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyExternalImage);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("image");
	}
	public String typeString()
	{
		return "image";
	}
	
	public PropertyExternalImage() {}
	public PropertyExternalImage(PropertyHashtable parent,String filepath,String name) 
	{
		id = Property.stringToID(name);		
		parent.AddProperty(this);		
		value = filepath;
	}
	
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyExternalImage))
			return false;
		PropertyExternalImage o = (PropertyExternalImage)p;
		return o.value.contentEquals(value);
	}

	public void set(Property p)
	{
		PropertyExternalImage pt = (PropertyExternalImage)p;
		value = pt.value;
		image = pt.image;
		dirty = pt.dirty;
	}
	public Property copy()
	{
		PropertyExternalImage pt = new PropertyExternalImage();
		pt.value = value;
		pt.image = image;
		pt.dirty = dirty;
		pt.id = id;
		return pt;
	}
	public RenderBuffer getImage()
	{
		if(image==null)
		{
			image = new RenderBuffer();
			if(value==null)
				return image;
			ImageLoader.createAndloadBufferFromURL(image, getRootPath()+value);
			
			//try again
			if(image.width==0)
			{
				ImageLoader.createAndLoadBuffer(image,getRootPath()+value);
			}
		}
		return image;
	}
	
	public Property createInstance()
	{
		return new PropertyExternalImage();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("image"))
	    {
	    	return new PropertyExternalImage();
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
			image.setAlpha(0xFF);
			ImageLoader.saveBufferURL(image, value);
			image.setAlpha(0x0);
			dirty = false;
		}
		p.push_back("image "+idToString(id)+"="+value);		
	}

	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		p.push_back("image "+idToString(id)+"="+value);		
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p) 
	{
		if(dirty)
		{
			image.setAlpha(0xFF);
			ImageLoader.saveBufferURL(image, value);
			image.setAlpha(0x0);
			dirty = false;
		}
		p.append("image "+idToString(id)+"="+value+"\n");		
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
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyExternalImage);
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

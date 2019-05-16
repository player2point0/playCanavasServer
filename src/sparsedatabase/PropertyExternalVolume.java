package sparsedatabase;

import importexport.ByteBufferReaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

import rendering.VolumeBuffer;

public class PropertyExternalVolume extends Property 
{
	public String value;
	public VolumeBuffer volume;
	public boolean dirty = false;
	
	public boolean sametype(Property p)
	{
		return (p instanceof PropertyExternalVolume);
	}
	public void getTypeNames(GeneralMatrixString n)
	{
		n.push_back("volume");
	}
	public String typeString()
	{
		return "volume";
	}
	
	public PropertyExternalVolume() {}
	public PropertyExternalVolume(PropertyHashtable parent,String path,VolumeBuffer vb,String name) 
	{
		id = Property.stringToID(name);
		this.value = path;
		this.volume = vb;
		parent.AddProperty(this);		
	}
	
	public void set(Property p)
	{
		PropertyExternalVolume pt = (PropertyExternalVolume)p;
		value = pt.value;
		volume = pt.volume;
		dirty = pt.dirty;
	}
	public boolean contentEquals(Property p)
	{
		if(!(p instanceof PropertyExternalVolume))
			return false;
		PropertyExternalVolume o = (PropertyExternalVolume)p;
		return o.value.contentEquals(value);
	}

	public Property copy()
	{
		PropertyExternalVolume pt = new PropertyExternalVolume();
		pt.value = value;
		pt.volume = volume;
		pt.dirty = dirty;
		pt.id = id;
		return pt;
	}
	public VolumeBuffer getVolume()
	{
		if(volume==null)
		{
			volume = new VolumeBuffer();
			if(value.endsWith("txt"))
			{
				PropertyHashtable ext = SparseDatabase.LoadEntryFromFileOutsideOfDatabaseURL(value);
				PropertyMatrixFloat pv = (PropertyMatrixFloat)ext.GetProperty("volumes");
				
				volume.setFromVolumes(pv.matrix, 0.01f, 0, 0xFFFFFF);
			}
				
		}
		return volume;
	}
	
	public Property createInstance()
	{
		return new PropertyExternalVolume();
	}
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase("volume"))
	    {
	    	return new PropertyExternalVolume();
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
			System.out.println("Not implemented yet");
			dirty = false;
		}
		p.push_back("volume "+idToString(id)+"="+value);		
	}
	public void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element)
	{
		p.push_back("volume "+idToString(id)+"="+value);		
		properties.push_back(this);
		element.push_back(-1);
	}

	public void SaveVerbose(PrintStream p) 
	{
		if(dirty)
		{
			System.out.println("Not implemented yet");
			dirty = false;
		}
		p.append("volume "+idToString(id)+"="+value+"\n");		
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
		ByteBufferReaderWriter.writeubyte(o,PropertyFactory.TYPE_PropertyExternalVolume);
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

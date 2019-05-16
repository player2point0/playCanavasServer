/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import java.io.BufferedReader;
import java.io.InputStream;

import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public class PropertyFactory 
{
	public static Property[] 	types;
	public static int[] 		typeIds;
	public static int numTypes = 0;
	
	public static final int TYPE_PropertyNULL = -1;
	public static final int TYPE_PropertyHashtable = 0;
	public static final int TYPE_PropertyList = 1;
	public static final int TYPE_PropertyMatrixChar = 2;
	public static final int TYPE_PropertyMatrixInt = 3;
	public static final int TYPE_PropertyMatrixLong = 4;
	public static final int TYPE_PropertyMatrixFloat = 5;
	public static final int TYPE_PropertyMatrixDouble = 6;
	public static final int TYPE_PropertyReference = 7;
	public static final int TYPE_PropertyString = 8;
	public static final int TYPE_PropertyValue128 = 9;
	public static final int TYPE_PropertyValue32 = 10;
	public static final int TYPE_PropertyValue64 = 11;
	public static final int TYPE_PropertyExternalImage = 12;
	public static final int TYPE_PropertyExternalImageFloat = 13;
	public static final int TYPE_PropertyArrayString = 14;
	public static final int TYPE_PropertyHashMatrixFloat = 15;
	public static final int TYPE_PropertyHashMatrixLong = 16;
	public static final int TYPE_PropertyExternalVolume = 17;
	public static final int TYPE_PropertyExternalParsable = 18;
	public static final int TYPE_NUM_DEFAULT = 19;
	
	public static void getTypeNames(GeneralMatrixString propertyNames,GeneralMatrixObject propertyExamples)
	{
		for(int i=0;i<numTypes;i++)
		{
			int prevnum = propertyNames.height;
			types[i].getTypeNames(propertyNames);
			int rem = propertyNames.height-prevnum;
			for(int pi=0;pi<rem;pi++)
				propertyExamples.push_back(types[i]);
		}
	}
	
	public static void initialise()
	{
		numTypes = TYPE_NUM_DEFAULT;
		types = new Property[numTypes];
		types[TYPE_PropertyHashtable] = new PropertyHashtable();
		types[TYPE_PropertyList] = new PropertyList();
		types[TYPE_PropertyMatrixChar] = new PropertyMatrixChar();
		types[TYPE_PropertyMatrixInt] = new PropertyMatrixInt();
		types[TYPE_PropertyMatrixLong] = new PropertyMatrixLong();
		types[TYPE_PropertyMatrixFloat] = new PropertyMatrixFloat();
		types[TYPE_PropertyMatrixDouble] = new PropertyMatrixDouble();
		types[TYPE_PropertyReference] = new PropertyReference(1);
		types[TYPE_PropertyString] = new PropertyString();
		types[TYPE_PropertyValue128] = new PropertyValue128(0);
		types[TYPE_PropertyValue32] = new PropertyValue32(0);
		types[TYPE_PropertyValue64] = new PropertyValue64(0);
		types[TYPE_PropertyExternalImage] = new PropertyExternalImage();
		types[TYPE_PropertyExternalImageFloat] = new PropertyExternalImageFloat();
		types[TYPE_PropertyArrayString] = new PropertyArrayString();
		types[TYPE_PropertyHashMatrixFloat] = new PropertyHashMatrixFloat();
		types[TYPE_PropertyHashMatrixLong] = new PropertyHashMatrixLong();
		types[TYPE_PropertyExternalVolume] = new PropertyExternalVolume();
		types[TYPE_PropertyExternalParsable] = new PropertyExternalParsable();
		
		typeIds = new int[numTypes];
		for(int i=0;i<numTypes;i++)
			typeIds[i] = i;
	}
	
	public static void appendProperty(Property p)
	{
		int newlen = numTypes+1;
		if(newlen>types.length)
		{
			Property[] newarray = new Property[newlen];
			System.arraycopy(types, 0, newarray, 0, types.length);
			types = newarray;
			
			int[] newtarray = new int[newlen];
			System.arraycopy(typeIds, 0, newtarray, 0, typeIds.length);
			typeIds = newtarray;
		}
		types[numTypes] = p;
		typeIds[numTypes] = numTypes;
		numTypes++;
	}
	
	public static int getTypeIndex(Property p)
	{
		for(int i=0;i<numTypes;i++)
		{
			if(types[i].sametype(p))
			{
				return typeIds[i];
			}
		}
		return -1;
	}
	
	public static String getTypeString(Property p)
	{
		return p.typeString();
	}
		
	public static Property createInstance(int type)
	{
		if(numTypes==0)
			initialise();
		
		if(type<numTypes)
		{
			Property p = types[type].createInstance();
			return p;
		}
	    return null;
	}

	static Property createInstance(String type)
	{
		if(numTypes==0)
			initialise();
		
		Property p = null;
		for(int i=0;i<numTypes;i++)
		{
			p = types[i].createInstance(type);
			if(p!=null)
				return p;
		}
	    return null;
	}
	
	public static Property ParseProperty(String s,BufferedReader in,int indent)
	{
		String sind = "";
		for(int i=0;i<indent;i++)
			sind += " ";
		System.out.println(sind+s);
		Property p = null;
		try
		{
		Property.SplitUpLine(s);
		//Ideally get these from pools
		p = PropertyFactory.createInstance(Property.type);

		if(Property.name.equalsIgnoreCase("-2"))
			Property.name = "thumb";
		
		p.id = p.stringToID(Property.name);
        //String validate = p.idToString(p.id);
        //System.out.println(Property.name+"->"+validate);

        int remainingEntries = p.Parse(Property.val);	
		if(remainingEntries>0)
		{
			while(remainingEntries>0)
			{
				s = in.readLine();
				if(s==null)
				{
					System.out.println("Error");
					return p;
				}
				remainingEntries = p.ParseMultiline(s,in,remainingEntries,indent);
			}
		}
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		return p;
	}
	
	public static Property ParseBinaryProperty(InputStream in) throws Exception
	{
		if(numTypes==0)
			initialise();

		int type = in.read();
		if(type==-1)
			return null;
		
		Property p = (Property)types[type].getClass().newInstance();
		p.Parse(in);
		return p;
	}
	
}

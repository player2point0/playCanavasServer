/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package sparsedatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;

public abstract class Property 
{
	//The unique id of property
	public long id = -1;

	//A means to access ui information etc. associated with this property
	public int metadata = -1;
	
	public Property parent = null;

	public abstract boolean sametype(Property p);
	public abstract boolean contentEquals(Property p);
		
	public String getRootPath()
	{
		if(parent==null)
			return "";
		return parent.getRootPath();
	}
	
	public static String type="";
	static String name="";
	public static String val="";
	public static String[] vals = new String[8];	
	public static boolean valsinvalid = false;
	
	public abstract void set(Property p);
	public abstract Property copy();
	public abstract Property createInstance();

	public abstract void getTypeNames(GeneralMatrixString n);
	//public abstract Property createInstance(String type);
	public Property createInstance(String type)
	{
	    if(type.equalsIgnoreCase(typeString()))
	    {
	    	return createInstance();
	    }
		return null;
	}
	public abstract String typeString();
	
	public boolean isEmpty()
	{
		return false;
	}
	
	public int getDepth()
	{
		if(parent==null)
			return 0;
		else
			return parent.getDepth()+1;
	}
	public boolean hasParent(Property p)
	{
		if(p==parent)
			return true;
		if(parent==null)
			return false;
		return parent.hasParent(p);
	}
	public Property getRootParent(GeneralMatrixInt depth) 
	{
		if(depth!=null)
			depth.value[0]++;
		if(parent==null)
			return this;
		else
			return parent.getRootParent(depth);
	}
	public long getRefOfSubProperty(Property p)
	{
		System.out.println("Error");
		return -1;
	}
	public int Parse(String value) { return 0; }
	public int ParseMultiline(String value,BufferedReader in,int remaining,int indent)
	{
		System.out.println("?");
		return 0; 
	}
	public void Parse(InputStream in) throws IOException {}
	public void SaveBinary(OutputStream o) throws IOException {}
	public void SaveVerbose(PrintStream p) {}
	public abstract void SaveVerbose(GeneralMatrixString p);
	public abstract void SaveVerbose(GeneralMatrixString p,GeneralMatrixObject properties,GeneralMatrixInt element);

	public String toString()
	{
		GeneralMatrixString temp = new GeneralMatrixString(1);
		SaveVerbose(temp);
		String result = "";
		for(int i=0;i<temp.height;i++)
			result += temp.value[i]+"\n";
		return result;
	}
	
	public static final long addIndToID(long id,int ind,int numDim,long pidIndexScale)
	{
		long pid = id;
		int d1 = ind%10;
		int d10 = ind/10;
		int d100 = ind/100;

		int ci;
		if(numDim>=3)
		{
			ci = d100+1;
			pid = pid+ci*pidIndexScale;
			pidIndexScale *= 38;
		}
		if(numDim>=2)
		{
			ci = d10+1;
			pid = pid+ci*pidIndexScale;
			pidIndexScale *= 38;
		}
//		if(numDim>=1)
		{
			ci = d1+1;
			pid = pid+ci*pidIndexScale;
			pidIndexScale *= 38;
		}
		return pid;
	}
	
	public static final long stringToID(String name,int ind,int numDim)
	{
		int maxp = name.length();
		int maxp2 = 5-numDim; 
		if(maxp2<maxp)
			maxp = maxp2;
		
		String prefix = name.substring(0, maxp);
		
		int d1 = ind%10;
		int d10 = ind/10;
		int d100 = ind/100;

		if(numDim==1)		
			return stringToID(prefix+d1);
		else
		if(numDim==2)		
			return stringToID(prefix+d10+""+d1);
		else
		//if(numDim==3)		
			return stringToID(prefix+d100+""+d10+""+d1);
		
		/*
		int numChars = 0;
		long pidIndexScale = 1;
		long pid = 0;
		for(int i=0;i<name.length();i++)
        {
			int c = name.charAt(i);
        	int ci = 0;
        	if((c>=48)&&(c<=57))
        	{
        		ci = c-48;
        	}
        	else
        	if((c>=65)&&(c<=90))
        	{
        		ci = 10+c-65;
        	}
        	else
        	if((c==95))
        	{
        		ci = 10+26;
        	}
        	else
        	if((c>=97)&&(c<=122))
        	{
        		ci = 10+c-97;
        	}
        	else
        	{
        		//System.out.println("Invalid character in id");
        		continue;
        	}
        	ci++;
        	pid = pid+ci*pidIndexScale;
    		pidIndexScale *= 38;
        	
        	numChars++;
        	if(numChars==3)
        	{
        		pid = addIndToID(pid, ind, numDim, pidIndexScale);
        		return pid;
        	}
        	//String validate = idToString(pid);
        	//System.out.println(validate);
        }
		pid = addIndToID(pid, ind, numDim, pidIndexScale);
    	//String validate = idToString(pid);
    	//System.out.println(validate);
		return pid;
		*/
	}
	public static final long stringToID(String name)
	{
		int numChars = 0;
		long pidIndexScale = 1;
		long pid = 0;
		for(int i=0;i<name.length();i++)
        {
			int c = name.charAt(i);
        	int ci = 0;
        	if((c>=48)&&(c<=57))
        	{
        		ci = c-48;
        	}
        	else
        	if((c>=65)&&(c<=90))
        	{
        		ci = 10+c-65;
        	}
        	else
        	if((c==95))
        	{
        		ci = 10+26;
        	}
        	else
        	if((c>=97)&&(c<=122))
        	{
        		ci = 10+c-97;
        	}
        	else
        	{
        		//System.out.println("Invalid character in id");
        		continue;
        	}
        	ci++;
        	pid = pid+ci*pidIndexScale;
        	
        	numChars++;
        	if(numChars==5)
        		break;
        	//String validate = idToString(pid);
        	//System.out.println(validate);
    		pidIndexScale *= 38;
        }
    	//String validate = idToString(pid);
    	//System.out.println(validate);
		return pid;
	}
	
	public static final long reverseId(long id,int[] tchars)
	{
		boolean validId = false;
		String s = "";
		int ind = 0;
		while(id!=0)
		{
			int c = (int)(id%38);
			id /= 38;
			c--;
			tchars[ind] = c;
			ind++;
		}
		
		if(ind==0)
			return 0;
		
		long pidIndexScale = 1;
		long pid = 0;
		for(int i=(ind-1);i>=0;i--)
		{
			int ci = tchars[i];
        	ci++;
        	pid = pid+ci*pidIndexScale;
        	pidIndexScale *= 38;
		}
		return pid;
	}
	
	public static final String idToString(long id)
	{	
		boolean validId = false;
		String s = "";
		while(id!=0)
		{
			int c = (int)(id%38);
			id /= 38;
			c--;
			if(c<10)
			{
				s += c;
			}
			else
			if(c==36)
			{
				s += '_';			
			}
			else
			if(c==37)
			{
				//All valid ids end in 37
				validId = true;
				break;
			}
			else
			if(c>=10)
			{
				char cc = 'a';
				cc += c-10;
				s += cc;
			}			
		}
		return s;
	}

	public static void SplitUpSimpleLine(String s)
	{
		try
		{
			type="";
			val="";
			int c;
		    StringReader sin = new StringReader(s);
	        while((c = sin.read()) != ' ')
	        {
	        	if(c==-1)
	        	{
	        		return;
	        	}
	        	type = type+(char)c;
	        }
	
	        while((c = sin.read()) != -1)
	        {
	        	val = val+(char)c;
	        }
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
		}
	}
	public static void SplitUpLine(String s)
	{
		try
		{
			type="";
			name="";
			val="";
			int c;
		    StringReader sin = new StringReader(s);
	        while(true)
	        {
	        	c = sin.read();
	        	if((type.length()>0)&&(c==' '))
	        		break;
	        	if((c=='\t')||(c==' '))
	        		continue;
	        	type = type+(char)c;
	        }
	
	        while((c = sin.read()) != '=')
	        {
	        	name += (char)c;
	        }
	        
	        while((c = sin.read()) != -1)
	        {
	        	val = val+(char)c;
	        }
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
		}
	}

	public static void SplitTuple(String s)
	{
		vals = SplitTuple(s,vals);
	}
	public static String[] SplitTuple(String s,String vals[])
	{
		try
		{
			int c;
		    StringReader sin = new StringReader(s);
		    int i=0;
		    while(true)
		    {
				vals[i]="";
			    while((c = sin.read()) != ',')
		        {
		        	if(c==-1)
		        	{
		    			valsinvalid = false;
		        		return vals;
		        	}
		        	vals[i] = vals[i]+(char)c;
		        }
			    i++;
			    if(i>=vals.length)
			    {
			    	String[] temp = vals;
			    	vals = new String[vals.length*2];
			    	System.arraycopy(temp, 0, vals, 0, temp.length);
			    }
		    }
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
			valsinvalid = true;
		    return vals;
		}		
	}
}

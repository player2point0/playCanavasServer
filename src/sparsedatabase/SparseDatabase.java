package sparsedatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;

import mathematics.GeneralMatrixLong;
import mathematics.GeneralMatrixObject;
import mathematics.GeneralMatrixString;
import mathematics.hash.HashLongArrayObjectIterator;

public class SparseDatabase //extends PropertyHashtable
{
	public static Object get(Property p)
	{
		if(p==null)
		{
			return null;
		}		
		else
		if(p instanceof PropertyString)
		{
			return ((PropertyString)p).value;
		}
		else
		if(p instanceof PropertyArrayString)
		{
			return ((PropertyArrayString)p).value;
		}
		else
		if(p instanceof PropertyList)
		{
			GeneralMatrixObject o = new GeneralMatrixObject(1,((PropertyList)p).entries.height);
			for(int li=0;li<o.height;li++)
			{
				o.value[li] = get((Property)((PropertyList)p).entries.value[li]);
			}
			return o;
		}
		return null;
	}
	
	public static void add(Property parent,Object e,String name)
	{
		PropertyHashtable pregion = null;
		PropertyList plistp = null;
		if(parent instanceof PropertyHashtable)
			pregion = (PropertyHashtable)parent;
		else
			plistp = (PropertyList)parent;
			
		if(e!=null)
		{
			if(e instanceof String)
			{
				if(plistp!=null)
				{
					PropertyString s = new PropertyString(plistp, (String)e, name); 
				}
				else
				{
					PropertyString s = new PropertyString(pregion, (String)e, name); 
				}
			}
			else
			if(e instanceof GeneralMatrixString)
			{
				if(plistp!=null)
				{
					PropertyArrayString as = new PropertyArrayString(plistp, (GeneralMatrixString)e, name);
				}
				else
				{
					PropertyArrayString as = new PropertyArrayString(pregion, (GeneralMatrixString)e, name);
				}
			}
			else
			if(e instanceof GeneralMatrixObject)
			{
				PropertyList pelems = null;
				if(plistp!=null)
				{
					pelems = new PropertyList(plistp,name);					
				}
				else
				{
					pelems = new PropertyList(pregion,name);					
				}
				GeneralMatrixObject o = (GeneralMatrixObject)e;

				for(int li=0;li<o.height;li++)
				{
					add(pelems, o.value[li], "v"+li);
				}
			}
		}
	}

//	int propertyHashSize = 8;
//	PropertyHashtable entryPool = null;
//	public URL documentBase; 
	
//	public SparseDatabase(int sizeOfEntryHash,int initSizeOfPropertyHash)
//	{
//		super(sizeOfEntryHash);
//		propertyHashSize = initSizeOfPropertyHash;
//		if(PropertyFactory.numTypes==0)
//		{
//			PropertyFactory.initialise();
//		}
//	}
//
//	public void setRootPath(String path)
//	{
//		try
//		{
//			documentBase = (new File(path)).toURL();
//		}
//		catch(Exception e)
//		{
//			System.out.println(e.toString());
//		}
//	}
//	
//	public String getRootPath()
//	{
//		return documentBase.getPath();
//	}
//		
//	public void FreeEntry(long id)
//	{
//		PropertyHashtable p = (PropertyHashtable)GetProperty(id);
//		RemoveProperty(p);
//		p.id = -1;		
//	}
//	
//	public boolean SaveEntryVerbose(long id,String entryFile)
//	{
//		PropertyHashtable p = (PropertyHashtable)GetProperty(id);
//		if(p==null)
//			return false;
//		
//		try
//		{
//			URL file = new URL(documentBase,entryFile);
//		    String path = URLDecoder.decode(file.getFile(), System.getProperty("file.encoding"));
//		    String driveLetter = ""+path.charAt(1);
//		    path = path.substring(4);
//		    path = driveLetter+":/"+path;
//			File fi;
//		      fi=new File(path);
//		      if(!fi.exists())
//		    	  fi.createNewFile();	
//		      FileOutputStream f = new FileOutputStream(path);
//			PrintStream pr = new PrintStream(f);
//			
//			//Go through all the properties and save out
//			for(int i=0;i<p.entries.length;i++)
//			{
//				Property e = p.entries.value[i];
//				while(e!=null)
//				{
//					if(!e.isEmpty())
//						e.SaveVerbose(pr);
//					e = e.hash_next;
//				}
//			}
//			
//			return true;
//		}
//		catch(Exception e)
//		{
//			System.out.println(e.toString());
//			return false;
//		}
//	}

	public static boolean SaveEntryVerboseURL(PropertyHashtable p,String fullpath)
	{
		if(p==null)
			return false;
		
		try
		{
			URL file = new URL(fullpath);
		    String path = URLDecoder.decode(file.getFile(), System.getProperty("file.encoding"));
//		    if(OsUtils.isWindows())
//		    {
//			    String driveLetter = ""+path.charAt(1);
//			    path = path.substring(4);
//			    path = driveLetter+":/"+path;
//		    }
			File fi;
		      fi=new File(path);
		      if(!fi.exists())
		    	  fi.createNewFile();	
		      FileOutputStream f = new FileOutputStream(path);
		      return SaveEntryVerboseStream(p,f);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;
		}
	}
	
	public static boolean SaveEntryVerboseStream(PropertyHashtable p,OutputStream o)
	{
		PrintStream pr = new PrintStream(o);
		
		//Go through all the properties and save out
		HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
		while(itr.hasMore(p.entries))
		{
			Property prop = (Property)itr.get(p.entries);
			prop.SaveVerbose(pr);
			itr.nextValid(p.entries);
		}		

		pr.flush();
		
		return true;
	}
	
	public static boolean SaveEntryVerbose(PropertyHashtable p,String fullpath)
	{
		if(p==null)
			return false;
		
		try
		{
			File f = new File(fullpath);
			System.out.println(f.getAbsolutePath());
			URL file = (new File(fullpath)).toURL();
			return SaveEntryVerboseURL(p,file.toExternalForm());
		}
		catch(Exception e)
		{
			File f = new File(fullpath);
			
			System.out.println(e.toString());
			return false;
		}
	}
	public static boolean SaveEntryBinary(PropertyHashtable p,String fullpath)
	{
		if(p==null)
			return false;
		
		try
		{
			URL file = (new File(fullpath)).toURL();
		    String path = URLDecoder.decode(file.getFile(), System.getProperty("file.encoding"));
		    String driveLetter = ""+path.charAt(1);
		    path = path.substring(4);
		    path = driveLetter+":/"+path;
			File fi;
		      fi=new File(path);
		      if(!fi.exists())
		    	  fi.createNewFile();	
		      FileOutputStream f = new FileOutputStream(path);
			//PrintStream pr = new PrintStream(f);
			
			//Go through all the properties and save out
			HashLongArrayObjectIterator itr = new HashLongArrayObjectIterator();
			while(itr.hasMore(p.entries))
			{
				Property prop = (Property)itr.get(p.entries);
				prop.SaveBinary(f);
				itr.nextValid(p.entries);
			}		

			f.flush();
			
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;
		}
	}

	public static PropertyHashtable LoadEntryFromString(final String stringpropertydata)
	{
		PropertyHashtable entry = new PropertyHashtable();
		try
		{
		StringReader sb = new StringReader(stringpropertydata);
		BufferedReader br = new BufferedReader(sb);
		String s;
		while((s = br.readLine())!= null)
		{
			Property p = PropertyFactory.ParseProperty(s,br,0);
			entry.AddProperty(p);
		}
		}
		catch(Exception e)
		{
			return null;
		}
		return entry;
	}
	public static PropertyHashtable LoadEntryFromFileOutsideOfDatabase(String entryFile)
	{
		PropertyHashtable entry = new PropertyHashtable();
		return LoadEntryFromFileOutsideOfDatabase(entryFile,entry);
	}
	public static PropertyHashtable LoadEntryFromFileOutsideOfDatabase(String entryFile, PropertyHashtable entry)
	{
		try
		{
			File f = new File(entryFile);
			if(!f.exists())
				return null;
			return LoadEntryFromFileOutsideOfDatabaseURL(f.toURL().toExternalForm(),entry);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
	}

	public static PropertyHashtable LoadEntryFromFileOutsideOfDatabaseURL(String entryFile)
	{
		PropertyHashtable entry = new PropertyHashtable();
		return LoadEntryFromFileOutsideOfDatabaseURL(entryFile,entry);
	}
	
	public static PropertyHashtable LoadEntryFromFileOutsideOfDatabaseURL(String entryFile, PropertyHashtable entry)
	{
		entry.id = -1;
		
		try
		{
			URL file = new URL(entryFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
			String s;
			while((s = in.readLine())!= null)
			{
				Property p = PropertyFactory.ParseProperty(s,in,0);
				entry.AddProperty(p);
			}
			in.close();
			return entry;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
	}

	public static PropertyHashtable LoadEntryFromStream(InputStream is)
	{
		PropertyHashtable entry = new PropertyHashtable();
		LoadEntryFromStream(is, entry);
		return entry;
	}

	public static PropertyHashtable LoadEntryFromStream(InputStream is,PropertyHashtable entry)
	{
		entry.id = -1;
		
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String s;
			while((s = in.readLine())!= null)
			{
				Property p = PropertyFactory.ParseProperty(s,in,0);
				entry.AddProperty(p);
			}
			return entry;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
	}
	
	public static PropertyHashtable LoadBinaryEntryFromFileOutsideOfDatabase(String entryFile)
	{
		PropertyHashtable entry = new PropertyHashtable();
		entry.id = -1;
		
		try
		{
			URL file = (new File(entryFile)).toURL();
			InputStream in = file.openStream();
			while(true)
			{
				Property p = PropertyFactory.ParseBinaryProperty(in);
				if(p==null)
					break;
				else
					entry.AddProperty(p);
			}
			return entry;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return null;
		}
	}
	
	public static boolean updateEntryFromFileOutsideOfDatabase(String entryFile,PropertyHashtable entry)
	{
		entry.clear();
		try
		{
			URL file = (new File(entryFile)).toURL();
			BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
			String s;
			while((s = in.readLine())!= null)
			{
				if(s.equalsIgnoreCase(""))
					continue;
				Property p = PropertyFactory.ParseProperty(s,in,0);
				entry.AddProperty(p);
			}
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;
		}
	}
	
//	public PropertyHashtable LoadEntry(long id,String entryFile)
//	{
//		PropertyHashtable entry = new PropertyHashtable();
//		entry.id = id;
//		
//		//Nice readable string version for now
//		try
//		{
//			URL file = new URL(documentBase,entryFile);
//			BufferedReader in = new BufferedReader(new InputStreamReader(file.openStream()));
//			String s;
//			while((s = in.readLine())!= null)
//			{
//				Property p = PropertyFactory.ParseProperty(s,in);
//				entry.AddProperty(p);
//			}
//			AddProperty(entry);
//			return entry;
//		}
//		catch(Exception e)
//		{
//			System.out.println(e.toString());
//			return null;
//		}
//	}
//
//	public PropertyHashtable FindEntryWithProperty(Property p,PropertyComparison c)
//	{
//		for(int i=0;i<entries.length;i++)
//		{
//			PropertyHashtable e = (PropertyHashtable)entries[i];
//			while(e!=null)
//			{
//				Property b = e.GetProperty(p.id);
//				if(c.compare(p, b))
//					return e;
//				e = (PropertyHashtable)e.hash_next;
//			}
//		}		
//		return null;
//	}
//	
//	public void FindEntriesWithProperty(Property p,PropertyComparison c,GeneralMatrixLong output)
//	{
//		for(int i=0;i<entries.length;i++)
//		{
//			PropertyHashtable e = (PropertyHashtable)entries[i];
//			while(e!=null)
//			{
//				Property b = e.GetProperty(p.id);
//				if(c.compare(p, b))
//				{
//					int in = output.appendRow();
//					output.set(0, in, e.id);
//				}
//				e = (PropertyHashtable)e.hash_next;
//			}
//		}		
//	}
}

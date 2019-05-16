package os;

import importexport.TextImportExport;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;

import mathematics.GeneralMatrixString;

public class FileSystem {

	static final String _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+_=";
	private static final int BUFFER = 8192;
	
	public static boolean setCurrentDirectory(String directory_name)
    {
        boolean result = false;  // Boolean indicating whether directory was set
        File    directory;       // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
        {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return result;
    }
	
	public static String toSafeFilename(String id)
	{
		String output = "";
		for (int i = 0; i < id.length(); i++) 
		{
			char c = id.charAt(i);
			if((c=='/')||(c=='\\'))
			{
				output += '_';
			}
			else
	        if(Character.isLetterOrDigit(c)||(c=='.')||(c=='-')) 
	        {
	        	output += c;
	        }
	        else
	        {
	    		String utftext = "";
	    		
	    		int chr1, chr2, chr3, enc1, enc2, enc3, enc4;
	    		boolean nochr2 = false;
	    		boolean nochr3 = false;
    			chr1 = chr2 = chr3 = 0;
    			int nochar = 0;
    			if (c < 128) 
    			{
    				chr1 += (char)(c);
    			}
    			else if((c > 127) && (c < 2048)) {
    				chr1 += (char)((c >> 6) | 192);
    				chr2 += (char)((c & 63) | 128);
    				nochr2 = true;
    			}
    			else {
    				chr1 += (char)((c >> 12) | 224);
    				chr2 += (char)(((c >> 6) & 63) | 128);
    				chr3 += (char)((c & 63) | 128);
    				nochr3 = true;
    			}

    			enc1 = chr1 >> 2;
    			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
    			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
    			enc4 = chr3 & 63;
    			
    			if (nochr2) {
    				output = output +
    				_keyStr.charAt(enc1) + _keyStr.charAt(enc2);
    			} else if (nochr3) {
    				output = output +
    				_keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
    				_keyStr.charAt(enc3);
    			} else {
    				output = output +
    				_keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
    				_keyStr.charAt(enc3) + _keyStr.charAt(enc4);
    			}
	        	
	        }
	    }		
		
		return output;
	}
	
	public static String getParentFolder(String file)
	{
		File f = new File(file);
		file = f.getAbsolutePath();
    	int ind = file.lastIndexOf("/");
    	return file.substring(0,ind);
	}
	
	public static void splitPath(File f,GeneralMatrixString path)
	{
		String test = f.getAbsolutePath();
		splitPath(test,path);
	}

	public static void splitPath(String test,GeneralMatrixString path)
	{
		if(test.contentEquals("/"))
		{
			path.width = 1;
			path.height = 0;
			return;
		}
		test = test.substring(1);
		path.value = test.split("/");
		path.width = 1;
		path.height = path.value.length;
//		File p = f.getParentFile();
//		if(p==null)
//			path.push_back(f.getName());
//		else
//		{
//			splitPath(p,path);
//			path.push_back(f.getName());
//		}
	}
	
	public static String newuniquefile(String in)
	{
		if(!FileSystem.exists(in))
			return in;
		int num = 0;
		while(true)
		{
			String tryf = in+num;
			if(!FileSystem.exists(tryf))
				return tryf;
			num++;
		}
	}
	public static String newuniquefile(String in,String filetype)
	{
		if(!FileSystem.exists(in+filetype))
			return in+filetype;
		int num = 0;
		while(true)
		{
			String tryf = in+num+filetype;
			if(!FileSystem.exists(tryf))
				return tryf;
			num++;
		}
	}
	
	public static void robustRestartableCopy(String in1,String in2,String progressfolder,String progressid)
	{
		GeneralMatrixString fromto = new GeneralMatrixString(2);
		copyOverwriteWithMoreRecentOrLarger(0,in1,in2,fromto);
		
		try
		{
			FileOutputStream f = new FileOutputStream(progressfolder+"/"+progressid+"_list.txt");
			PrintStream p = new PrintStream(f);
			for(int i=0;i<fromto.height;i++)
			{
				p.append(fromto.value[i*2+0]);
				p.append("\t");
				p.append(fromto.value[i*2+1]);
				p.append("\n");				
			}
			p.close();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		fromto = null;
		System.gc();
		TextImportExport.saveTextFile(progressfolder+"/"+progressid+"_ind.txt", "0");
		
		restartRobustCopy(progressfolder,progressid);
	}
	
	public static void restartRobustCopy(String progressfolder,String progressid)
	{
		GeneralMatrixString list = new GeneralMatrixString(2);
		TextImportExport.loadTextFile(progressfolder+"/"+progressid+"_list.txt", list);
		int ind = Integer.parseInt(new String(TextImportExport.loadTextFile(progressfolder+"/"+progressid+"_ind.txt")));
		for(;ind<list.height;ind++)
		{
			try
			{
				Thread.sleep(10);
			}
			catch(Exception e)
			{
				
			}

			System.out.println(""+ind+"/"+list.height);
			String[] copyfile = list.value[ind].split("\t");
			copyFile(copyfile[0], copyfile[1]);
			TextImportExport.saveTextFile(progressfolder+"/"+progressid+"_ind.txt", ""+(ind+1));
		}
	}
	
	public static boolean copyOverwriteWithMoreRecentOrLarger(int depth,String from,String to,GeneralMatrixString fromto)
	{
		boolean anycopies = false;
		try
		{
			Thread.sleep(10);
		}
		catch(Exception e)
		{
			
		}
		File fin1 = new File(from);
		File fin2 = new File(to);
		if(fin1.isDirectory())
		{
			//System.out.println(from);
			if(!fin2.isDirectory())
			{
				System.out.println("Name clash "+fin1.getAbsolutePath()+" "+fin2.getAbsolutePath());
			}
			File[] fin1s = fin1.listFiles();
			File[] fin2s = fin2.listFiles();
			String[] fin2str = fin2.list();
			for(int i=0;i<fin1s.length;i++)
			{
				boolean match = false;
				String name = fin1s[i].getName();
				for(int o=0;o<fin2s.length;o++)
				{
					if(fin2str[o]==null)
						continue;
					if(fin2str[o].contentEquals(name))
					{
						fin2str[o] = null;
						match = true;

						boolean eany = copyOverwriteWithMoreRecentOrLarger(depth+1,fin1s[i].getAbsolutePath(),fin2s[o].getAbsolutePath(),fromto);
						anycopies = anycopies || eany;
						break;
					}
				}
				if(!match)
				{
					anycopies = true;
					System.out.println(fin1s[i].getAbsolutePath()+"\t->\t"+to+"/"+fin1s[i].getName());
					copyInto(fin1s[i].getAbsolutePath(),to,fromto);
				}
			}
//			for(int o=0;o<fin2s.length;o++)
//			{
//				if(fin2str[o]!=null)
//				{
//					copyInto(in1,fin2s[o].getAbsolutePath(),fromto);
//				}
//			}
			if((depth<=3)&&(!anycopies))
				System.out.println("Folder copy complete:\t"+from);
		}
		else
		{
			long fdate1 = fin1.lastModified();
			long fdate2 = fin2.lastModified();
			
			long fsize1 = fin1.length();
			long fsize2 = fin2.length();
			
			if(
					(fdate1<fdate2)||
					(fsize1<fsize2)
			  )
			{
				System.out.println(from+"\t->\t"+to);
				fromto.push_back_row(from, to);
				anycopies = true;
			}
		}
		return anycopies;
	}
	
	public static boolean compare(String in1, String in2,GeneralMatrixString diffs) 
	{
		boolean isequal = true;
		File fin1 = new File(in1);
		File fin2 = new File(in2);
		if(fin1.isDirectory())
		{
			if(!fin2.isDirectory())
			{
				diffs.push_back_row(in1,in2);
				return false;
			}
			File[] fin1s = fin1.listFiles();
			File[] fin2s = fin2.listFiles();
			String[] fin2str = fin2.list();
			for(int i=0;i<fin1s.length;i++)
			{
				boolean match = false;
				String name = fin1s[i].getName();
				for(int o=0;o<fin2s.length;o++)
				{
					if(fin2str[o]==null)
						continue;
					if(fin2str[o].contentEquals(name))
					{
						fin2str[o] = null;
						match = true;
						isequal = isequal&&compare(fin1s[i].getAbsolutePath(),fin2s[o].getAbsolutePath(),diffs);
						break;
					}
				}
				if(!match)
				{
					diffs.push_back_row("missing",fin1s[i].getAbsolutePath(),null);
					isequal = false;
				}
			}
			for(int o=0;o<fin2s.length;o++)
			{
				if(fin2str[o]!=null)
				{
					diffs.push_back_row("missing",null,fin2s[o].getAbsolutePath());					
				}
			}
		}
		else
		{
			long fil1 = fin1.length();
			long fil2 = fin2.length();
			
			long fdate1 = fin1.lastModified();
			long fdate2 = fin2.lastModified();
			
			if(fil1!=fil2)
			{				
				if(fdate1!=fdate2)
				{				
					diffs.push_back_row("size+date",in1,in2);					
					return false;
				}
				else
				{
					diffs.push_back_row("size",in1,in2);					
					return false;					
				}
			}
			else
			{
				if(fdate1!=fdate2)
				{				
					diffs.push_back_row("date",in1,in2);					
					return false;
				}
				else
				{
					return true;
				}
			}
		}
		return isequal;
	}
	
	public static String findFile(String in, String name) 
	{
		File inf = new File(in);
		File[] cf = inf.listFiles();
		for(int ci=0;ci<cf.length;ci++)
		{
			if(cf[ci].getName().equalsIgnoreCase(name))
			{
				return cf[ci].getAbsolutePath();
			}
			else
			if(cf[ci].isDirectory())
			{
				String res = findFile(cf[ci].getAbsolutePath(), name);
				if(res!=null)
					return res;
			}
		}
		return null;
	}
	
	//just returns the route with the name not all the child nodes
	public static void findFilesContaining(String in, String name,GeneralMatrixString found) 
	{
		File inf = new File(in);
		File[] cf = inf.listFiles();
		for(int ci=0;ci<cf.length;ci++)
		{
			if(cf[ci].getName().toLowerCase().contains(name))
			{
				System.out.println(cf[ci].getAbsolutePath());
				found.push_back(cf[ci].getAbsolutePath());
				//return;
			}
			else
			if(cf[ci].isDirectory())
			{
				findFilesContaining(cf[ci].getAbsolutePath(), name, found);
//				if(found.height>0)
//					return;
			}
		}
	}
	
	public static void moveInto(String in, String out) 
	{		
		File inf = new File(in);
		if(inf.isDirectory())
		{
			String newdir = out;
			createDirectory(newdir);
			File[] infs = inf.listFiles();
			for(int i=0;i<infs.length;i++)
			{
				copyInto(in+"/"+infs[i].getName(),newdir);
			}
		}
		else
		{
			copyFile(in, out);
		}
		
		deleteDirectory(in);
	}

	public static void copyInto(String in, String out) 
	{		
		File inf = new File(in);
		if(inf.isDirectory())
		{
			String newdir = out+"/"+inf.getName();
			createDirectory(newdir);
			File[] infs = inf.listFiles();
			for(int i=0;i<infs.length;i++)
			{
				copyInto(in+"/"+infs[i].getName(),newdir);
			}
		}
		else
		{
			copyFile(in, out+"/"+inf.getName());
		}
	}

	public static void copyInto(String from, String to,GeneralMatrixString fromto) 
	{		
		File inf = new File(from);
		if(inf.isDirectory())
		{
			String newdir = to+"/"+inf.getName();
			fromto.push_back_row(inf.getAbsolutePath(), newdir);
			//createDirectory(newdir);
			File[] infs = inf.listFiles();
			for(int i=0;i<infs.length;i++)
			{
				copyInto(from+"/"+infs[i].getName(),newdir,fromto);
			}
		}
		else
		{
			fromto.push_back_row(from, to+"/"+inf.getName());
		}
	}

	public static boolean makeLink(String in, String out) 
	{
		RunExe.run(new String[] { "ln", "-s", in, out });
		return true;
	}
	
	public static boolean copyFile(String in, String out) 
	{
		File inf = new File(in);
		File outf = new File(out);
		return copyFile(inf, outf);
	}
	public static boolean swapFiles(String in, String out) 
	{
		File inf = new File(in);
		File outf = new File(out);
		File outft = new File(newuniquefile(out));
		inf.renameTo(outft);
		outf.renameTo(inf);
		outft.renameTo(outf);
		return true;
	}
	
	public static boolean moveFile(File file,File intodir)
	{
		boolean success = file.renameTo(new File(intodir, file.getName()));
		return success;
	}

	private static void close(Closeable closable) {
	    if (closable != null) {
	        try {
	            closable.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public static boolean copyFile(File source, File target) 
	{
		if(source.isDirectory())
		{
			createDirectory(target.getAbsolutePath());
			return true;
		}
		
        FileChannel in = null;
        FileChannel out = null;

        boolean success = true;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(target).getChannel();

            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER);
            while (in.read(buffer) != -1) {
                buffer.flip();

                while(buffer.hasRemaining()){
                    out.write(buffer);
                }

                buffer.clear();
            }
        } catch (IOException e) {
        	success = false;
            e.printStackTrace();
        } finally {
            close(in);
            close(out);
        }

	    return success;
	  }
	  
	public static void deleteFile(File in) 
	{
		in.delete();
	}
	public static void deleteFile(String in) 
	{
		new File(in).delete();
	}
	public static void rename(String from,String to) 
	{
		File f = new File(from);
		File fto = new File(to);
		if(fto.exists())
		{
			System.out.println(to+":exists");
		}
		else
			f.renameTo(fto);
	}
	public static void createAllParentDirectories(String dir)
	{
		String[] split = dir.split("/");
		String path = "";
		for(int i=1;i<(split.length-1);i++)
		{
			path += "/"+split[i];
			if(!exists(path))
				FileSystem.createDirectory(path);
		}
	}
	public static void createDirectory(String dir)
	{
		boolean success = (new File(dir)).mkdir();
	    if (success) {
	      System.out.println("Directory: " + dir + " created");
	    }    		
	    else
	    {
	    	System.out.println("!Directory: " + dir + " not created");
	    }
	}

	public static void createURL(String file,String urlpath)
	{
		String script = "[InternetShortcut]"
				+"\nURL="+urlpath
				+"\nWorkingDirectory=C:\\WINDOWS\\"
				+"\nShowCommand=7"
				+"\nIconIndex=1"
				+"\nIconFile=C:\\WINDOWS\\SYSTEM\\url.dll"
				+"\nModified=20F06BA06D07BD014D"
				+"\nHotKey=1601";
		TextImportExport.saveTextFile(file,script);
	}
	
	/*
	@echo off
	echo Set oWS = WScript.CreateObject("WScript.Shell") > CreateShortcut.vbs
	echo sLinkFile = "%HOMEDRIVE%%HOMEPATH%\Desktop\Hello.lnk" >> CreateShortcut.vbs
	echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs
	echo oLink.TargetPath = "C:\Windows\notepad.exe" >> CreateShortcut.vbs
	echo oLink.Save >> CreateShortcut.vbs
	cscript CreateShortcut.vbs
	del CreateShortcut.vbs
	*/

	public static void createLnk(String from,String to)
	{
		/*
		try
		{
		String script = "Set sh = CreateObject(\"WScript.Shell\")"
		        + "\nSet shortcut = sh.CreateShortcut(\""+from+"\")"
		        + "\nshortcut.TargetPath = \""+to+"\""
		        + "\nshortcut.Save";
		    
		    File file = new File("temp.vbs");
		    FileOutputStream fo = new FileOutputStream(file);
		    fo.write(script.getBytes());
		    fo.close();
		    Runtime.getRuntime().exec("wscript.exe " + file.getAbsolutePath() );
		}
		catch(Exception e)
		{
			
		}
		*/
		//*
		from = from.replace("/", "\\");
		to = to.replace("/", "\\");
		String createlnks = "@echo off\n";
		createlnks += "echo Set oWS = WScript.CreateObject(\"WScript.Shell\") > CreateShortcut.vbs\n";
		createlnks += "echo sLinkFile = \""+from+"\" >> CreateShortcut.vbs\n";
		createlnks += "echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs\n";
		createlnks += "echo oLink.TargetPath = \""+to+"\" >> CreateShortcut.vbs\n";
		createlnks += "echo oLink.Save >> CreateShortcut.vbs\n";
		createlnks += "cscript CreateShortcut.vbs\n";
		createlnks += "del CreateShortcut.vbs\n";	
		
		TextImportExport.saveTextFile("createlnk.bat", createlnks);
		RunExe.run("./createlnk.bat");	
		//*/	
	}
	
	public static void createLink(String from,String to)
	{
		RunExe.run(new String[] {"ln","-s",from,to});
	}
	
	public static void deleteDirectory(String dir)
	{
		File dirf = new File(dir);
		deleteDirectory(dirf);
	}

	public static boolean exists(String dir)
	{
		File dirf = new File(dir);
		return dirf.exists();
	}
	
	public static void deleteDirectory(File dirf)
	{
		if( dirf.exists() ) {
		      File[] files = dirf.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) 
		         {
		           deleteDirectory(files[i]);
		         }
		         else {
		           files[i].delete();
		         }
		      }
		      dirf.delete();
		}
	}

private boolean isDirectory;
private boolean isLocal;
private String real_file;

public boolean isDirectory() {
    return isDirectory;
}

public String getRealFilename() {
    return real_file;
}

private void parse(File f) throws IOException 
{
    // read the entire file into a byte buffer
    FileInputStream fin = new FileInputStream(f);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] buff = new byte[256];
    while (true) {
        int n = fin.read(buff);
        if (n == -1) {
            break;
        }
        bout.write(buff, 0, n);
    }
    fin.close();
    byte[] link = bout.toByteArray();

    parseLink(link);
}

private void parseLink(byte[] link) {
    // get the flags byte
    byte flags = link[0x14];

    // get the file attributes byte
    final int file_atts_offset = 0x18;
    byte file_atts = link[file_atts_offset];
    byte is_dir_mask = (byte)0x10;
    if ((file_atts & is_dir_mask) > 0) {
        isDirectory = true;
    } else {
        isDirectory = false;
    }

    // if the shell settings are present, skip them
    final int shell_offset = 0x4c;
    final byte has_shell_mask = (byte)0x01;
    int shell_len = 0;
    if ((flags & has_shell_mask) > 0) {
        // the plus 2 accounts for the length marker itself
        shell_len = bytes2short(link, shell_offset) + 2;
    }

    // get to the file settings
    int file_start = 0x4c + shell_len;

    final int file_location_info_flag_offset_offset = 0x08;
    int file_location_info_flag = link[file_start + file_location_info_flag_offset_offset];
    isLocal = (file_location_info_flag & 2) == 0;
    // get the local volume and local system values
    //final int localVolumeTable_offset_offset = 0x0C;
    final int basename_offset_offset = 0x10;
    final int networkVolumeTable_offset_offset = 0x14;
    final int finalname_offset_offset = 0x18;
    int finalname_offset = link[file_start + finalname_offset_offset] + file_start;
    String finalname = getNullDelimitedString(link, finalname_offset);
    if (isLocal) {
        int basename_offset = link[file_start + basename_offset_offset] + file_start;
        String basename = getNullDelimitedString(link, basename_offset);
        real_file = basename + finalname;
    } else {
        int networkVolumeTable_offset = link[file_start + networkVolumeTable_offset_offset] + file_start;
        int shareName_offset_offset = 0x08;
        int shareName_offset = link[networkVolumeTable_offset + shareName_offset_offset]
                + networkVolumeTable_offset;
        String shareName = getNullDelimitedString(link, shareName_offset);
        real_file = shareName + "\\" + finalname;
    }
}

private static String getNullDelimitedString(byte[] bytes, int off) {
    int len = 0;
    // count bytes until the null character (0)
    while (true) {
        if (bytes[off + len] == 0) {
            break;
        }
        len++;
    }
    return new String(bytes, off, len);
}

/*
 * convert two bytes into a short note, this is little endian because it's
 * for an Intel only OS.
 */
private static int bytes2short(byte[] bytes, int off) {
    return ((bytes[off + 1] & 0xff) << 8) | (bytes[off] & 0xff);
}

/**
 * Returns the value of the instance variable 'isLocal'.
 *
 * @return Returns the isLocal.
 */
public boolean isLocal() {
    return isLocal;
}
}
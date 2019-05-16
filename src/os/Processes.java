package os;

import java.io.StringReader;

import importexport.TextImportExport;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixString;

public class Processes 
{
	public static void getCurrentProcesses(GeneralMatrixString names,GeneralMatrixInt pids)
	{
		//ps -ef
		try
		{
			String[] params = {"ps","-ef"};
			String processes = RunExe.runNoWait(params);
			String[] lines = processes.split("\n");
			GeneralMatrixString word = new GeneralMatrixString(1);
			for(int i=1;i<lines.length;i++)
			{
		    	  StringReader sin = new StringReader(lines[i]);
		    	 
		    	  int firstc = sin.read();
		    	  for(int ei=0;ei<7;ei++)
		    	  {
		    		  firstc = TextImportExport.readWord(sin, true, firstc, word);
		    		  if(ei==1)
		    		  {
		    			  pids.push_back(Integer.parseInt(word.value[0]));
		    		  }
		    	  }
		    	  String cmdstring = "";
		    	  int c = sin.read();
		    	  while(c!=-1)
		    	  {
		    		  cmdstring += (char)c;
		    		  c = sin.read();
		    	  }
		    	  names.push_back(cmdstring);
				//names.p
			}
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
}

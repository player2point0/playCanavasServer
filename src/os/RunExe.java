package os;

import java.io.InputStream;
import java.io.OutputStream;

public class RunExe implements Runnable 
{
	String[] exeCommands = null;
	String exeCommand = null;
	
	public RunExe(String ec)
	{
		exeCommand = ec;
	}
	public RunExe(String[] ecs)
	{
		exeCommands = ecs;
	}
	
	public void run() 
	{
		if(exeCommand!=null)
		{
			run(exeCommand);
		}
		else
		if(exeCommands!=null)
		{
			run(exeCommands);			
		}
    }
	
	public static String run(String exeCommand)
	{
		System.out.println(exeCommand);
		try
		{
		 Runtime rt = Runtime.getRuntime() ;
		 Process p = rt.exec(exeCommand) ;
		 InputStream in = p.getInputStream() ;
		 OutputStream out = p.getOutputStream ();
		 InputStream err = p.getErrorStream() ;
		 
		 p.waitFor();
		 
		//do whatever you want
		 //some more code
		 
		 String ins = "";
		 int c;
		 while((c = in.read())!=-1)
		 {
			 ins += (char)c;			 
		 }
		 System.out.println(ins);
		 
		 String errs = "Err: ";
		 while((c = err.read())!=-1)
		 {
			 errs += (char)c;			 
		 }
		 System.out.println(errs);
		 
		 p.destroy() ;
		 return ins;
		}catch(Exception exc)
		{
			/*handle exception*/
			System.out.println(exc.toString());
		}		
		 return null;
	}

	
	public static String run(String[] exeCommand,String[] env)
	{
		try
		{
		 Runtime rt = Runtime.getRuntime() ;
		 Process p = rt.exec(exeCommand,env) ;
		 InputStream in = p.getInputStream() ;
		 OutputStream out = p.getOutputStream ();
		 InputStream err = p.getErrorStream() ;
		 
		//do whatever you want
		 //some more code
		 p.waitFor();
		 
		 String ins = "";
		 int c;
		 while((c = in.read())!=-1)
		 {
			 ins += (char)c;			 
		 }
		 System.out.println(ins);
		 
		 String errs = "";
		 while((c = err.read())!=-1)
		 {
			 errs += (char)c;			 
		 }
		 System.out.println(errs);
		 
		 p.destroy() ;
		 return ins;
		}catch(Exception exc)
		{
			/*handle exception*/
			System.out.println(exc.toString());
		}		
		 return null;
	}

	public static String run(String[] exeCommand)
	{
		try
		{
		 Runtime rt = Runtime.getRuntime() ;
		 Process p = rt.exec(exeCommand) ;
		 InputStream in = p.getInputStream() ;
		 OutputStream out = p.getOutputStream ();
		 InputStream err = p.getErrorStream() ;
		 
		//do whatever you want
		 //some more code
		 p.waitFor();
		 
		 String ins = "";
		 int c;
		 while((c = in.read())!=-1)
		 {
			 ins += (char)c;			 
		 }
		 System.out.println(ins);
		 
		 String errs = "";
		 while((c = err.read())!=-1)
		 {
			 errs += (char)c;			 
		 }
		 System.out.println(errs);
		 
		 p.destroy() ;
		 return ins;
		}catch(Exception exc)
		{
			/*handle exception*/
			System.out.println(exc.toString());
		}		
		 return null;
	}

	public static String runNoWait(String[] exeCommand)
	{
		try
		{
		 Runtime rt = Runtime.getRuntime() ;
		 Process p = rt.exec(exeCommand) ;
		 InputStream in = p.getInputStream() ;
		 OutputStream out = p.getOutputStream ();
		 InputStream err = p.getErrorStream() ;
		 		 
		 String ins = "";
		 int c;
		 while((c = in.read())!=-1)
		 {
			 ins += (char)c;			 
		 }
		 System.out.println(ins);
		 
		 String errs = "";
		 while((c = err.read())!=-1)
		 {
			 errs += (char)c;			 
		 }
		 System.out.println(errs);
		 
		 p.destroy() ;
		 return ins;
		}catch(Exception exc)
		{
			/*handle exception*/
			System.out.println(exc.toString());
		}		
		 return null;
	}
}

package os;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ProcessWrapper extends Thread
{
	String cmd = null;
	String[] cmds = null;
	Process process = null;
	
	InputStream in = null;
	BufferedWriter out = null;
	
	String inBuffer = "";
	boolean inBufferDirty = false;
	String outBuffer = "";
	public boolean outBufferDirty = false;
	public boolean terminated = false;
	
	public ProcessWrapper(String cmd)
	{
		this.cmd = cmd;
	}
	public ProcessWrapper(String[] cmds)
	{
		this.cmds = cmds;
	}
	
	public synchronized void appendToInput(String cmd)
	{
		inBuffer = inBuffer+cmd;
		inBufferDirty = true;
		try
		{
			out.write(inBuffer);
			out.flush();
			System.out.println(cmd);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		inBuffer = "";
		inBufferDirty = false;
	}
	public synchronized void appendToOutput(char c)
	{
		outBuffer = outBuffer+c;
		outBufferDirty = true;
	}
	public synchronized String getOutput()
	{
		String res = ""+outBuffer;
		outBuffer = "";
		outBufferDirty = false;
		return res;
	}
	
	public void start()
	{
		ProcessBuilder builder = null;
		if(cmd!=null)
			builder = new ProcessBuilder(cmd);
		else
			builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);
		try
		{
			process = builder.start();
			in = process.getInputStream() ;
			 OutputStream outs = process.getOutputStream();
			 out = new BufferedWriter(new OutputStreamWriter(outs));
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		
		
		super.start();
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				int c = in.read();
				if(c==-1)
				{
					terminated = true;
					break;
				}
				else
					appendToOutput((char)c);
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
	}
}

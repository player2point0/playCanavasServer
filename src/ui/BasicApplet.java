/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package ui;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rendering.RenderBuffer;
import ui.input.InputEvents;
import ui.input.WindowEvents;

public abstract class BasicApplet extends Applet implements Runnable, UpdateInterface
{
	public boolean isApplet = true;
	public BasicJFrame nonAppletFrame;

	public WebDisplay display;
	public RenderBuffer rb;
	public InputEvents input;
	public WindowEvents windowEvents;
	
	private Thread thread;
	
	public boolean loading = true;

	public void setCursor(RenderBuffer img,int x,int y,int w,int h)
	{
		BufferedImage bimg = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0,0,w,h,rb.pixel,x+y*img.width,img.width);			
		
		java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
		//Image image = toolkit.getImage("icons/handwriting.gif");
		java.awt.Cursor c = toolkit.createCustomCursor(bimg , new Point(this.getX(),
				this.getY()), "img");
		this.setCursor(c);
	}

	
	public static void runAsMain(BasicApplet example)
	{
		example.nonAppletFrame = new BasicJFrame();
		
		example.nonAppletFrame.updatee = example;
		example.nonAppletFrame.setVisible(true);     
		example.nonAppletFrame.init();               
		example.nonAppletFrame.start();
		
		example.rb = example.nonAppletFrame.rb;
		example.input = example.nonAppletFrame.input;
		example.display = example.nonAppletFrame.display;
		example.windowEvents = example.nonAppletFrame.windowEvents;

		example.init();
		example.nonAppletFrame.setSize(example.nonAppletFrame.windowEvents.width,example.nonAppletFrame.windowEvents.height);		
		example.loading = false;
	}
	
	public void init()
	{
		if(rb==null)
		{	
			rb = new RenderBuffer(getWidth(),getHeight());
			display = new WebDisplay(rb);
			input = new InputEvents();

			windowEvents = new WindowEvents();
			windowEvents.inComponent = this;
			addMouseListener(input); 
			addMouseWheelListener(input); 
			addMouseMotionListener(input); 
			addKeyListener(input);
			setFocusable(true);

			loading = false;
		}
	}
	
	public String getDocumentRoot(boolean isApplet)
	{
		String dataroot = "";
    	try
    	{
    	if(isApplet)
    	{
    		String docBase = getDocumentBase().toExternalForm();
    		if(docBase.endsWith("html")||docBase.endsWith("cgi"))
    		{
    			int rootIndex = docBase.lastIndexOf('/');
    			docBase = docBase.substring(0,rootIndex);
    		}
    		dataroot = docBase+"/../";
    	}
    	else
    	{
    		File root = new File(".");
    		dataroot = root.toURL().toExternalForm();
    	}
    	}
    	catch(Exception e)
    	{
    		System.out.println(e.toString());
    	}
    	
    	return dataroot;
	}
	
	public String callJavaScript(String jscmd)
	{
		String jsresult = null;
		boolean success = false;
		try {
		  Method getw = null, eval = null;
		  Object jswin = null;
		  Class c =
		    Class.forName("netscape.javascript.JSObject"); /* does it in IE too */
		  Method ms[] = c.getMethods();
		  for (int i = 0; i < ms.length; i++) 
		  {
		      if (ms[i].getName().compareTo("getWindow") == 0)
		         getw = ms[i];
		      else if (ms[i].getName().compareTo("eval") == 0)
		         eval = ms[i];
		  }
		  Object a[] = new Object[1];
		  a[0] = this;               /* this is the applet */
		  jswin = getw.invoke(c, a); /* this yields the JSObject */
		  a[0] = jscmd;
		  Object result = eval.invoke(jswin, a);
		  if (result instanceof String)
			  return (String) result;
		  else
			  return null;
		  }

		catch (InvocationTargetException ite) {
		  jsresult = "" + ite.getTargetException();
		  }
		catch (Exception e) {
		  jsresult = "" + e;
		  }
		return null;
	}
	
	public abstract void frameUpdate();
	
	public synchronized void update(Graphics g)
	{
		frameUpdate();
		if(display!=null)
			g.drawImage(display.getImage(),0,0,this);
	}

	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void stop()
	{
		if (thread != null)
		{
			thread.stop();
			thread = null;
		}
	}
	
	public void run()
	{
		while(true)
		{
			repaint();
			try
			{
				thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				//System.out.println("idx://interrupted");
			}
		}
	}
}

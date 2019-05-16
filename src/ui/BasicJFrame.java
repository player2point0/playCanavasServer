/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import rendering.RenderBuffer;
import ui.input.InputEvents;
import ui.input.WindowEvents;

public class BasicJFrame extends JFrame
{
	public WebDisplay display;
	public RenderBuffer rb;
	public InputEvents input;
	public WindowEvents windowEvents = new WindowEvents();
	public UpdateInterface updatee = null;
	BasicJPanel jp;
	
	public BasicJFrame()
	{
		setSize(windowEvents.width,windowEvents.height);
	}
	
	public void init()
	{
		if(rb==null)
		{	
			rb = new RenderBuffer(getWidth(),getHeight());
			display = new WebDisplay(rb);
			input = new InputEvents();
			
			addWindowListener(windowEvents);
	        addWindowFocusListener(windowEvents);
	        addWindowStateListener(windowEvents);
	        this.addComponentListener(windowEvents);
	        addKeyListener(input);
		}
	}
	
	public void start()
	{
		jp = new BasicJPanel(this);
		jp.removeAll();
		
		windowEvents.inComponent = jp;
		jp.addMouseListener(input); 
		jp.addMouseWheelListener(input); 
		jp.addMouseMotionListener(input); 
		jp.addKeyListener(input);

		setLayout(new BorderLayout());
		setContentPane(jp);
		setVisible(true);
		jp.setFocusable(true);
		jp.repaint();
	}
	
	public void stop()
	{
	}

	public void update(Graphics g)
	{
		frameUpdate();
		Graphics2D g2 = (Graphics2D)g;  
		g2.drawImage(display.getImage(), 0, 0, this); 
	}

	public void frameUpdate()
	{
		if(updatee!=null)
		{
			updatee.frameUpdate();
		}
	}	
}

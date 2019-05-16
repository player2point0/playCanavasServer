/*
Copyright (c) 2008-Present John Bustard  http://johndavidbustard.com

This code is release under the GPL http://www.gnu.org/licenses/gpl.html

To get the latest version of this code goto http://johndavidbustard.com/mmconst.html
*/
package ui;

import java.awt.Graphics;
import javax.swing.JPanel;

public class BasicJPanel extends JPanel
{
	BasicJFrame frame;
	
	public BasicJPanel(BasicJFrame j)
	{
		frame = j;
	}
	public void paintComponent(Graphics page)
	{		
	      super.paintComponent(page);
    	  frame.update(page);
	      
		try
		{
			Thread.sleep(20);
		}
		catch (InterruptedException e)
		{
		}	      
	      repaint();
	}
}

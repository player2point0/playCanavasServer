package os;

import importexport.image.ImageLoader;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import rendering.RenderBuffer;

public class OsUtils 
{
	   private static String OS = null;
	   public static String getOsName()
	   {
	      if(OS == null) { OS = System.getProperty("os.name"); }
	      return OS;
	   }

	   public static boolean isWindows()
	   {
	      return getOsName().startsWith("Windows");
	   }

	   public static void grabScreen(RenderBuffer rb)
	   {
		   try
		   {
			   Robot robot = new Robot();
			   
			   BufferedImage bi = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			   rb.resize(bi.getWidth(), bi.getHeight());
				int xoff = 0;
				int yoff = 0;
				PixelGrabber pg= new PixelGrabber(bi,xoff,yoff,rb.width,rb.height,rb.pixel,0,rb.width);
				pg.grabPixels();
		   }
		   catch(Exception e)
		   {
			   System.out.println(e.toString());
		   }
	   }
	   
	   //public static boolean isUnix() // and so on
}

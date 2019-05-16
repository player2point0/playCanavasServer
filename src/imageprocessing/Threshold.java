package imageprocessing;

import rendering.RenderBuffer;

public class Threshold 
{
	public static void threshold(RenderBuffer image,int min, int max) 
	{
		int numpixels = image.height*image.width;
	    for (int pi = 0; pi < numpixels; pi++) 
	    {
	            if (image.pixel[pi] < min)
	            	image.pixel[pi] = 0;
	            else if (image.pixel[pi] >= max)
	            	image.pixel[pi] = 255;
	    }
	}

	public static void threshold(RenderBuffer image,int t) 
	{
	    threshold(image, t, t+1);
	}

}

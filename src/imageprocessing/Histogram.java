package imageprocessing;

import mathematics.GeneralMatrixInt;
import rendering.RenderBuffer;

public class Histogram 
{
	public static void smooth(GeneralMatrixInt hist,GeneralMatrixInt smoothHistogram)
	{
		smoothHistogram.setDimensions(hist.width, hist.height);
		int numhistv = hist.width*hist.height;
	    smoothHistogram.value[0] = (hist.value[0] + hist.value[1]) / 2;
		for(int i=1;i<(numhistv-1);i++)
		{
	        smoothHistogram.value[i] = (hist.value[i-1] + hist.value[i] + hist.value[i+1]) / 3;
		}
	    smoothHistogram.value[255] = (hist.value[254] + hist.value[255]) / 2;
	}
	
	public static void build(RenderBuffer rb,int xi,int yi,int w,int height,GeneralMatrixInt hist)
	{
		hist.setDimensions(1, 256);
		hist.clear(0);

		int minx = xi;
		int maxx = xi+w;
		int miny = yi;
		int maxy = yi+height;
		
		minx=(minx>0)?((minx<rb.width)?minx:rb.width):0;
		maxx=(maxx>0)?((maxx<rb.width)?maxx:rb.width):0;
		miny=(miny>0)?((miny<rb.height)?miny:rb.height):0;
		maxy=(maxy>0)?((maxy<rb.height)?maxy:rb.height):0;
		
		for(int y=miny;y<maxy;y++)
		{
			for(int x=minx;x<maxx;x++)
			{
				int i = x+y*rb.width;
				int colour = rb.pixel[i]&0xFFFFFF;
				if(colour>=hist.height)
				{
					hist.setDimensionsAndClearNew(1, colour+1, 0);
				}
				hist.ensureCapacity(colour);
				hist.value[colour]++;
			}
		}
	}
}

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
package rendering;

public class RenderBuffer 
{
	public int pixel[];
	public int width;
	public int height;
	
	public RenderBuffer()
	{
		width=0;
		height=0;
		pixel = null;
	}

	public RenderBuffer(final int w, final int h)
	{
		width=w;
		height=h;
		pixel=new int[w*h];
	}

	public RenderBuffer(RenderBuffer rb)
	{
		width=rb.width;
		height=rb.height;
		pixel=new int[width*height];
		copy(rb);
	}

	public void convertGrayscaleImageToValues()
	{
		int numPixels = width*height;
		for(int i=0;i<numPixels;i++)
		{
            int p = pixel[i];
            int r = (p >> 16) & 0xFF;
    		pixel[i] = r;
		}
	}
	
	public void convertARGBtoRGBA()
	{
		int numPixels = width*height;
		for(int i=0;i<numPixels;i++)
		{
            int p = pixel[i];
            int a = (p >> 24) & 0xFF;  // get pixel bytes in ARGB order
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = (p >> 0) & 0xFF;
            
    		pixel[i] = (r<<24)|(g<<16)|(b<<8)|(a<<0);
		}
	}
	
	public void ensureCapacity(int size)
	{
		if((pixel==null)||(pixel.length<size))
		{
			pixel = new int[size];
		}
	}
	
	public void resize(int width,int height)
	{
		ensureCapacity(width*height);
		this.width = width;
		this.height = height;
	}

	public void clear(final int value)
	{
		clear(pixel,value);
	}
	
	public static void clear(int[] pixel,final int value)
	{
		if(pixel==null)
			return;
		int size=pixel.length-1;
		int cleared=1;
		int index=1;
		pixel[0]=value;

		while (cleared<size)
		{
			System.arraycopy(pixel,0,pixel,index,cleared);
			size-=cleared;
			index+=cleared;
			cleared<<=1;
		}
		System.arraycopy(pixel,0,pixel,index,size);
	}

	public void clearHidden(final int value)
	{
		int offset = width*height;
		int size=pixel.length-1-offset;
		int cleared=1;
		int index=1+offset;
		pixel[offset]=value;

		while (cleared<size)
		{
			System.arraycopy(pixel,offset,pixel,index,cleared);
			size-=cleared;
			index+=cleared;
			cleared<<=1;
		}
		System.arraycopy(pixel,offset,pixel,index,size);
	}

	public void clear(final int value,int xi,int yi,int w,int h)
	{
		int minx = xi;
		int maxx = xi+w;
		int miny = yi;
		int maxy = yi+h;
		
		minx=(minx>0)?((minx<width)?minx:width):0;
		maxx=(maxx>0)?((maxx<width)?maxx:width):0;
		miny=(miny>0)?((miny<height)?miny:height):0;
		maxy=(maxy>0)?((maxy<height)?maxy:height):0;
		
		for(int y=miny;y<maxy;y++)
		{
			for(int x=minx;x<maxx;x++)
			{
				pixel[x+y*width] = value;
			}
		}
	}

	public void clear(final int value,float x,float y,float w,float h)
	{
		int xi = (int)x;
		int yi = (int)y;
		int wi = (int)w;
		int hi = (int)h;

		int minx = xi;
		int maxx = xi+wi;
		int miny = yi;
		int maxy = yi+hi;
		
		minx=(minx>0)?((minx<width)?minx:width):0;
		maxx=(maxx>0)?((maxx<width)?maxx:width):0;
		miny=(miny>0)?((miny<height)?miny:height):0;
		maxy=(maxy>0)?((maxy<height)?maxy:height):0;
		
		for(int cy=miny;cy<maxy;cy++)
		{
			for(int cx=minx;cx<maxx;cx++)
			{
				pixel[cx+cy*width] = value;
			}
		}
	}
	
	public void flipy()
	{
		int pi = 0;
		int opi = width*(height-1);
		for(int y=0;y<(height/2);y++)
		{
			for(int x=0;x<width;x++)
			{
				int old = pixel[pi+x];
				pixel[pi+x] = pixel[opi+x];
				pixel[opi+x] = old;
			}
			pi += width;
			opi -= width;
		}
	}
	
	public void flipx()
	{
		int pi = 0;
		int opi = width-1;
		for(int y=0;y<(height);y++)
		{
			for(int x=0;x<(width/2);x++)
			{
				int old = pixel[pi+x];
				pixel[pi+x] = pixel[opi-x];
				pixel[opi-x] = old;
			}
			pi += width;
			opi += width;
		}
	}
	
	public void shiftx(final int value,int clearColour)
	{
		if(value<0)
		{
			int maxx = width+value;
			if(maxx<0)
			{
				clear(clearColour);
				return;
			}
			for(int y=0;y<height;y++)
			{
				for(int x=0;x<maxx;x++)
				{
					pixel[x+y*width] = pixel[x-value+y*width];
				}
				for(int x=maxx;x<width;x++)
				{
					pixel[x+y*width] = clearColour;
				}
			}
		}
	}
	public void shiftImageXY(RenderBuffer result,int x,int y)
	{
		result.clear(0);
		
		int rwidth = width;
		  int i,j, iLow, iHigh, jLow, jHigh;

		  if(x>=0) {iLow = x; iHigh = rwidth;}
		  else {iLow = 0; iHigh = rwidth + x;}

		  if(y>=0) {jLow = y; jHigh = height;}
		  else {jLow = 0; jHigh = height + y;}

		    for(j=jLow; j<jHigh;j++)
		      for(i=iLow; i<iHigh;i++)
		      {
		    	  result.pixel[i+j*width] = pixel[(i-x)+(j-y)*width];
		      }		
	}
	public void set(final RenderBuffer rb)
	{
		if((rb.width==0)||(rb.height==0)||(rb.pixel==null))
			return;
//			System.out.println("Eh");
		ensureCapacity(rb.width*rb.height);
		width = rb.width;
		height = rb.height;
		System.arraycopy(rb.pixel,0,pixel,0,rb.width*rb.height);
	}
	public void set(final int[] rb)
	{
//		if((rb.width==0)||(rb.height==0)||(rb.pixel==null))
//			System.out.println("Eh");
		System.arraycopy(rb,0,pixel,0,width*height);
	}
	public void copy(final RenderBuffer rb)
	{
		if(
				(rb.width==width)&&
				(rb.height==height)
		  )
		{
			System.arraycopy(rb.pixel,0,pixel,0,rb.width*rb.height);
		}
		else
		{
			final int minHeight = Math.min(rb.height, height);
			final int minWidth = Math.min(rb.width, width);
			for(int y=0;y<minHeight;y++)
			{
				try
				{
				System.arraycopy(rb.pixel,y*rb.width,pixel,y*width,minWidth);
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
				}
			}
		}
	}
	public void copy(final int[] rbpixel,int rbw,int rbh)
	{
		if(
				(rbw==width)&&
				(rbh==height)
		  )
		{
			System.arraycopy(rbpixel,0,pixel,0,rbw*rbh);
		}
		else
		{
			final int minHeight = Math.min(rbh, height);
			final int minWidth = Math.min(rbw, width);
			for(int y=0;y<minHeight;y++)
			{
				try
				{
				System.arraycopy(rbpixel,y*rbw,pixel,y*width,minWidth);
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
				}
			}
		}
	}
	
	public void copySubRect(final RenderBuffer rb,int xi,int yi,int w,int h)
	{
		for(int y=0;y<h;y++)
		{
			try
			{
			System.arraycopy(rb.pixel,xi+(y+yi)*rb.width,pixel,y*width,w);
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
	}
	
	public void copy(final RenderBuffer rb,int xi,int yi)
	{
		int maxy = yi+rb.height;
		if(maxy>height)
			maxy=height;
		
		int maxx = xi+rb.width;
		if(maxx>width)
			maxx = width;

		int minx = xi;
		int offx = 0;
		if(minx<0)
		{
			offx = -minx;
			minx = 0;
		}
		
		int miny = yi;
		int offy = 0;
		if(miny<0)
		{
			offy = -miny;
			miny = 0;
		}
		
		if(maxx<=0)
			return;
		
		if(maxy<=0)
			return;

		int w = maxx-minx;
		int h = maxy-miny;

		if(w<0)
			return;
		if(h<0)
			return;

//		int maxw = width-offx;
//		if(w>maxw)
//			w=maxw;

		offy = 0;
			for(int y=yi;y<maxy;y++)
			{
				if(y<0)
				{
					offy++;
					continue;
				}
				try
				{
					System.arraycopy(rb.pixel,offx+offy*rb.width,
									pixel,y*width+minx,
									w);
					offy++;
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
				}
			}
	}

	public void setAlpha(int a)
	{
		for(int i=0;i<height*width;i++)
		{
			pixel[i] = (a<<24) | (pixel[i]&0xFFFFFF);
		}
	}
	public void invertAlpha()
	{
		for(int i=0;i<height*width;i++)
		{
			int a = (pixel[i]&0xFF000000)>>>24;
			a = 0xFF-a;
			pixel[i] = (a<<24) | (pixel[i]&0xFFFFFF);
		}
	}
	public void setConditionalAlpha(int a,int notpixel)
	{
		for(int i=0;i<height*width;i++)
		{
			if(pixel[i]!=notpixel)
				pixel[i] = (a<<24) | (pixel[i]&0xFFFFFF);
		}
	}
	
}


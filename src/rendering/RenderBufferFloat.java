package rendering;

import mathematics.GeneralMatrixFloat;
import imageprocessing.colour.Colour;


public class RenderBufferFloat 
{
	public int width;
	public int height;
	public float[] pixel; //one array of r,g,b or l
	
	public RenderBufferFloat()
	{
	}
	
	public RenderBufferFloat(int w,int h)
	{
		width = w;
		height = h;
		pixel = new float[w*h];
	}

	public RenderBufferFloat(int w,int h,float[] vals)
	{
		width = w;
		height = h;
		pixel = vals;
	}

	public RenderBufferFloat(RenderBufferFloat a)
	{
		width = a.width;
		height = a.height;
		pixel = new float[width*height];
		copy(a);
	}
	
	public void ensureCapacity(int size)
	{
		if((pixel==null)||(pixel.length<size))
		{
			pixel = new float[size];
		}
	}
	
	public void resize(int width,int height)
	{
		ensureCapacity(width*height);
		this.width = width;
		this.height = height;
	}

	public void clear(final float value)
	{
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
	
	public void copy(final RenderBufferFloat rb)
	{
		if(
				(rb.width==width)&&
				(rb.height==height)
		  )
		{
			System.arraycopy(rb.pixel,0,pixel,0,pixel.length);
		}
		else
		{
			final int minHeight = Math.min(rb.height, height);
			final int minWidth = Math.min(rb.width, width);
			for(int y=0;y<minHeight;y++)
			{
				System.arraycopy(rb.pixel,y*rb.width,pixel,y*width,minWidth);				
			}
		}
	}
	public void shiftImageXY(int numChannels, RenderBufferFloat result,int x,int y)
	{
		result.clear(0.0f);
		
		int rwidth = width/numChannels;
		  int i,j,k, iLow, iHigh, jLow, jHigh;

		  if(x>=0) {iLow = x; iHigh = rwidth;}
		  else {iLow = 0; iHigh = rwidth + x;}

		  if(y>=0) {jLow = y; jHigh = height;}
		  else {jLow = 0; jHigh = height + y;}

		    for(j=jLow; j<jHigh;j++)
		      for(i=iLow; i<iHigh;i++)
		      {
		    	  for(k=0;k<numChannels;k++)
		    		  result.pixel[i*numChannels+j*width+k] = pixel[(i-x)*numChannels+(j-y)*width+k];
		      }		
	}
	public void clearMasked(RenderBuffer mask,float value)
	{
		for(int i=0;i<width*height;i++)
		{
			if(mask.pixel[i]!=0)
			{
				pixel[i] = value;
			}
		}
	}
	public void copyMasked(RenderBuffer mask,RenderBufferFloat value)
	{
		for(int i=0;i<width*height;i++)
		{
			if(mask.pixel[i]!=0)
			{
				pixel[i] = value.pixel[i];
			}
		}
	}

	public void setAsGreyscale(RenderBuffer rb)
	{
		for(int j=0;j<rb.height;j++)
		{
			for(int i=0;i<rb.width;i++)
			{
				int p = rb.pixel[i+j*rb.width];
				int r = (p&0xFF0000)>>16;
				int g = (p&0xFF00)>>8;
				int b = (p&0xFF);
				pixel[i+j*width] = (r+g+b)/(255.0f*3.0f);
			}
		}
	}

	public void getAsUnnormalisedGreyscale(RenderBuffer rb,float min,float max)
	{
		for(int i=0;i<width*height;i++)
		{
			float f = pixel[i];
			if(f==Float.MAX_VALUE)
			{
				rb.pixel[i] = 0xFF0000;
				continue;
			}
			int g = (int)(255*(f-min)/(max-min));
			if(g<0)
				g=0;
			if(g>255)
				g=255;
			rb.pixel[i] = g | g<<8 | g<<16;
		}
	}

	public static void getAsUnnormalisedGreyscale(float[] pixelf,int[] pixel,int width,int height,float min,float max)
	{
		for(int i=0;i<width*height;i++)
		{
			float f = pixelf[i];
			if(f==Float.MAX_VALUE)
			{
				pixel[i] = 0xFF0000;
				continue;
			}
			int g = (int)(255*(f-min)/(max-min));
			if(g<0)
				g=0;
			if(g>255)
				g=255;
			pixel[i] = g | g<<8 | g<<16;
		}
	}
	public static void getAsGreyscale(float[] pixelf,int[] pixel,int width,int height)
	{
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for(int i=0;i<width*height;i++)
		{
			float f = pixelf[i];
			if(f==Float.MAX_VALUE)
				continue;
			if(f<min)
				min = f;
			if(f>max)
				max = f;
		}
		for(int i=0;i<width*height;i++)
		{
			float f = pixelf[i];
			if(f==Float.MAX_VALUE)
			{
				pixel[i] = 0xFF0000;
				continue;
			}
			int g = (int)(255*(f-min)/(max-min));
			if(g<0)
				g=0;
			if(g>255)
				g=255;
			pixel[i] = g | g<<8 | g<<16;
		}
	}
	
	public void getAsGreyscale(RenderBuffer rb)
	{
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for(int i=0;i<width*height;i++)
		{
			float f = pixel[i];
			if(f==Float.MAX_VALUE)
				continue;
			if(f<min)
				min = f;
			if(f>max)
				max = f;
		}
		
		for(int i=0;i<width*height;i++)
		{
			float f = pixel[i];
			if(f==Float.MAX_VALUE)
			{
				rb.pixel[i] = 0xFF0000;
				continue;
			}
			int g = (int)(255*(f-min)/(max-min));
			rb.pixel[i] = g | g<<8 | g<<16;
		}
	}

	public float sqrDistance(RenderBufferFloat other)
	{
		float sum = 0.0f;
		for(int i=0;i<width*height;i++)
		{
			float diff = (other.pixel[i]-pixel[i]); 
			sum += diff*diff;
		}	
		sum /= width*height;
		return sum;
	}

	public void sub(RenderBufferFloat a,RenderBufferFloat b)
	{
		for(int i=0;i<width*height;i++)
		{
			pixel[i] = a.pixel[i]-b.pixel[i];
		}	
	}
	
	public void setAsColour(RenderBuffer rb)
	{
		for(int j=0;j<rb.height;j++)
		{
			for(int i=0;i<rb.width;i++)
			{
				int p = rb.pixel[i+j*rb.width];
				int r = (p&0xFF0000)>>16;
				int g = (p&0xFF00)>>8;
				int b = (p&0xFF);
				pixel[i*3+j*width+0] = r/255.0f;
				pixel[i*3+j*width+1] = g/255.0f;
				pixel[i*3+j*width+2] = b/255.0f;
			}
		}
	}
	
	public void getAsColour(RenderBuffer rb)
	{
		for(int i=0;i<(width/3)*height;i++)
		{
			float f = pixel[i*3];
			if(f==Float.MAX_VALUE)
				rb.pixel[i] = 0;
			else
			{
				if(f<0.0f)
					f = 0.0f;
				if(f>1.0f)
					f = 1.0f;
				int r = (int)(255*f);
				f = pixel[i*3+1];
				if(f<0.0f)
					f = 0.0f;
				if(f>1.0f)
					f = 1.0f;
				int g = (int)(255*f);
				f = pixel[i*3+2];
				if(f<0.0f)
					f = 0.0f;
				if(f>1.0f)
					f = 1.0f;
				int b = (int)(255*f);
				rb.pixel[i] = b | g<<8 | r<<16;
			}
		}
	}
	public void getAsColourscale(RenderBuffer rb)
	{
		float minr = Float.MAX_VALUE;
		float ming = Float.MAX_VALUE;
		float minb = Float.MAX_VALUE;
		float maxr = -Float.MAX_VALUE;
		float maxg = -Float.MAX_VALUE;
		float maxb = -Float.MAX_VALUE;
		for(int i=0;i<(width/3)*height;i++)
		{
			float f = pixel[i*3];
			if(f==Float.MAX_VALUE)
				rb.pixel[i] = 0;
			else
			{
				if(f<minr)
					minr = f;
				if(f>maxr)
					maxr = f;
				f = pixel[i*3+1];
				if(f<ming)
					ming = f;
				if(f>maxg)
					maxg = f;
				f = pixel[i*3+2];
				if(f<minb)
					minb = f;
				if(f>maxb)
					maxb = f;			
			}
		}
		
		for(int i=0;i<(width/3)*height;i++)
		{
			float f = pixel[i*3];
			if(f==Float.MAX_VALUE)
				rb.pixel[i] = 0;
			else
			{
				int r = (int)(255*(f-minr)/(maxr-minr));
				f = pixel[i*3+1];
				int g = (int)(255*(f-ming)/(maxg-ming));			
				f = pixel[i*3+2];
				int b = (int)(255*(f-minb)/(maxb-minb));			
				rb.pixel[i] = b | g<<8 | r<<16;
			}
		}
	}

	public void getHeightAsColourscale(RenderBuffer rb,float sat,float bright)
	{
		float minr = Float.MAX_VALUE;
		float maxr = -Float.MAX_VALUE;
		for(int i=0;i<(width)*height;i++)
		{
			float f = pixel[i];
			if(f==Float.MAX_VALUE)
				rb.pixel[i] = 0;
			else
			{
				if(f<minr)
					minr = f;
				if(f>maxr)
					maxr = f;
			}
		}
		
		for(int i=0;i<(width)*height;i++)
		{
			float f = pixel[i];
			if(f==Float.MAX_VALUE)
				rb.pixel[i] = 0;
			else
			{
				float normf = (f-minr)/(maxr-minr);
				rb.pixel[i] = Colour.getColourFromHSB(normf, sat, bright)&0xFFFFFF;
			}
		}
	}

	public void getAsRobustGreyscale(RenderBuffer rb,float outlierdist)
	{
		float mean = 0.0f;
		float meansqr = 0.0f;
		float sum = 0.0f;
		for(int i=0;i<width*height;i++)
		{
			float f = pixel[i];
			if(f!=outlierdist)
			{
				mean += f;
				meansqr += f*f;
				sum += 1.0f;
			}
		}
		mean /= sum;
		meansqr /= sum;
		float sd = (float)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int i=0;i<width*height;i++)
		{
			float f = pixel[i];
			if(f!=outlierdist)
			{
				int g = (int)(128*(f-mean)/(sd*3));
				g += 128;
				if(g>255)
					g = 255;
				if(g<0)
					g = 0;
				rb.pixel[i] = g | g<<8 | g<<16;
			}
			else
			{
				rb.pixel[i] = 0xFF0000;
			}
		}
	}

	public void getDimensionAsRobustGreyscale(int dim,int validdim,int pdim,RenderBuffer rb)
	{
		float mean = 0.0f;
		float meansqr = 0.0f;
		float sum = 0.0f;
		
		int numpixels = width*height/pdim;
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if(pixel[i*pdim+validdim]!=Float.MAX_VALUE)
			{
				mean += f;
				meansqr += f*f;
				sum += 1.0f;
			}
		}
		mean /= sum;
		meansqr /= sum;
		float sd = (float)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if(pixel[i*pdim+validdim]!=Float.MAX_VALUE)
			{
				int g = (int)(128*(f-mean)/(sd*3));
				g += 128;
				if(g>255)
					g = 255;
				if(g<0)
					g = 0;
				rb.pixel[i] = g | g<<8 | g<<16;
			}
			else
			{
				rb.pixel[i] = 0xFF0000;
			}
		}
	}

	public void getDimensionAsRobustColourscaleSubregion(int dim,int validdim,int pdim,RenderBuffer rb,float invalid,int x,int y,int rwidth, int rheight)
	{
		float mean = 0.0f;
		float meansqr = 0.0f;
		float sum = 0.0f;
		
		int numpixels = width*height/pdim;
		
		int minx = x;
		int maxx = x+rwidth;
		int miny = y;
		int maxy = y+rheight;
		
		if(minx<0)
			minx=0;
		if(miny<0)
			miny=0;
		if(maxx>=rb.width)
			maxx=rb.width-1;
		if(maxy>=rb.height)
			maxy=0;
		
		for(int j=miny;j<=maxy;j++)
		{
			for(int pi=minx;pi<=maxx;pi++)
			{
				int i=j*rb.width+pi;
				float f = pixel[i*pdim+dim];
				if(pixel[i*pdim+validdim]!=invalid)
				{
					mean += f;
					meansqr += f*f;
					sum += 1.0f;
				}
			}
		}

		if(sum==0.0f)
			return;
		
		mean /= sum;
		meansqr /= sum;
		float sd = (float)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int j=miny;j<=maxy;j++)
		{
			for(int pi=minx;pi<=maxx;pi++)
			{
				int i=j*rb.width+pi;
			float f = pixel[i*pdim+dim];
			if(pixel[i*pdim+validdim]!=invalid)
			{
				float h = ((0.5f)*(f-mean)/(sd*2));
				h += 0.5f;
				
				if(h<0.0f)
					h = 0.0f;
				if(h>1.0f)
					h = 1.0f;
						
				float b = h*0.75f+0.25f;
				b = 1.0f-b;
				rb.pixel[i] = Colour.getColourFromHSB(b, 1.0f, h)&0xFFFFFF;
			}
			else
			{
				rb.pixel[i] = 0xc0c0c0;
			}
		}
		}
	}
	
	public void getDimensionAsRobustColourscale(int dim,int validdim,int pdim,RenderBuffer rb,float invalid)
	{
		float mean = 0.0f;
		float meansqr = 0.0f;
		float sum = 0.0f;
		
		int numpixels = width*height/pdim;
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if(pixel[i*pdim+validdim]!=invalid)
			{
				mean += f;
				meansqr += f*f;
				sum += 1.0f;
			}
		}
		mean /= sum;
		meansqr /= sum;
		float sd = (float)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if(pixel[i*pdim+validdim]!=invalid)
			{
				float h = ((0.5f)*(f-mean)/(sd*3));
				h += 0.5f;
				
				if(h<0.0f)
					h = 0.0f;
				if(h>1.0f)
					h = 1.0f;
						
				float b = h*0.75f+0.25f;
				b = 1.0f-b;
				rb.pixel[i] = Colour.getColourFromHSB(b, 1.0f, h)&0xFFFFFF;
			}
			else
			{
				rb.pixel[i] = 0xc0c0c0;
			}
		}
	}

	public void getDimensionRangeAsRobustColourscale(int dim,int validdim,int pdim,RenderBuffer rb,float min,float max)
	{
		float mean = 0.0f;
		float meansqr = 0.0f;
		float sum = 0.0f;
		
		int numpixels = width*height/pdim;
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if((pixel[i*pdim+validdim]>=min)&&(pixel[i*pdim+validdim]<=max))
			{
				mean += f;
				meansqr += f*f;
				sum += 1.0f;
			}
		}
		mean /= sum;
		meansqr /= sum;
		float sd = (float)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i*pdim+dim];
			if((pixel[i*pdim+validdim]>=min)&&(pixel[i*pdim+validdim]<=max))
			{
				float h = ((0.5f)*(f-mean)/(sd*3));
				h += 0.5f;
				
				if(h<0.0f)
					h = 0.0f;
				if(h>1.0f)
					h = 1.0f;
						
				float b = h*0.75f+0.25f;
				b = 1.0f-b;
				rb.pixel[i] = Colour.getColourFromHSB(b, 1.0f, h)&0xFFFFFF;
			}
			else
			{
				rb.pixel[i] = 0xc0c0c0;
			}
		}
	}

	public void getPCAasRedGreen(RenderBuffer rb)
	{
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		int numpixels = width*height;
		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i];
			if(f<min)
				min = f;
			if(f>max)
				max = f;
		}
		
		float scale = Float.MAX_VALUE;
		if(max>0.0f)
		{
			float nscale = 255.0f/max;
			if(nscale<scale)
				scale = nscale;
		}
		if(min<0.0f)
		{
			float nscale = 255.0f/-min;
			if(nscale<scale)
				scale = nscale;
		}

		for(int i=0;i<numpixels;i++)
		{
			float f = pixel[i];
			if(f<0.0f)
			{
				int r = (int)(-f*scale);
				rb.pixel[i] = r<<16;
			}
			else
			{
				int g = (int)(f*scale);
				rb.pixel[i] = g<<8;
			}
		}
	}
	
	public void get2DAsRobustRedGreen(RenderBuffer rb)
	{
		float mean1 = 0.0f;
		float meansqr1 = 0.0f;
		float sum1 = 0.0f;

		float mean2 = 0.0f;
		float meansqr2 = 0.0f;
		float sum2 = 0.0f;
		
		int numpixels = width*height/2;
		
		for(int i=0;i<numpixels;i++)
		{
			float f1 = pixel[i*2+0];
			float f2 = pixel[i*2+1];
			if(f1!=Float.MAX_VALUE)
			{
				mean1 += f1;
				meansqr1 += f1*f1;
				sum1 += 1.0f;

			}
			if(f2!=Float.MAX_VALUE)
			{
				mean2 += f2;
				meansqr2 += f2*f2;
				sum2 += 1.0f;

			}
		}
		mean1 /= sum1;
		meansqr1 /= sum1;
		float sd1 = (float)Math.sqrt(Math.abs(meansqr1-mean1*mean1));
		mean2 /= sum2;
		meansqr2 /= sum2;
		float sd2 = (float)Math.sqrt(Math.abs(meansqr2-mean2*mean2));
		
		for(int i=0;i<numpixels;i++)
		{
			int r = 0;
			int g = 0;
			
			float f1 = pixel[i*2+0];
			float f2 = pixel[i*2+1];
			if(f1!=Float.MAX_VALUE)
			{
				r = (int)(128*(f1-mean1)/(sd1*3));
				if(r>255)
					r = 255;
				if(r<0)
					r = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			if(f2!=Float.MAX_VALUE)
			{
				g = (int)(128*(f2-mean2)/(sd2*3));
				if(g>255)
					g = 255;
				if(g<0)
					g = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			
			rb.pixel[i] = 0 | g<<8 | r<<16;
		}
	}

	public void get2DAsRedGreen(RenderBuffer rb)
	{
		int numpixels = width*height/2;
		
		for(int i=0;i<numpixels;i++)
		{
			int r = 0;
			int g = 0;
			
			float f1 = pixel[i*2+0];
			float f2 = pixel[i*2+1];
			if(f1!=Float.MAX_VALUE)
			{
				r = (int)(128+f1*128);
				if(r>255)
					r = 255;
				if(r<0)
					r = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			if(f2!=Float.MAX_VALUE)
			{
				g = (int)(128+f2*128);
				if(g>255)
					g = 255;
				if(g<0)
					g = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			
			rb.pixel[i] = 0 | g<<8 | r<<16;
		}
	}
	
	public void get3DAsRedGreen(RenderBuffer rb)
	{
		int numpixels = width*height/3;
		
		for(int i=0;i<numpixels;i++)
		{
			int r = 0;
			int g = 0;
			
			float f1 = pixel[i*3+0];
			float f2 = pixel[i*3+1];
			if(f1!=Float.MAX_VALUE)
			{
				r = (int)(128+f1*128);
				if(r>255)
					r = 255;
				if(r<0)
					r = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			if(f2!=Float.MAX_VALUE)
			{
				g = (int)(128+f2*128);
				if(g>255)
					g = 255;
				if(g<0)
					g = 0;
			}
			else
			{
				rb.pixel[i] = 0x0000FF;
				continue;
			}
			
			rb.pixel[i] = 0 | g<<8 | r<<16;
		}
	}
	
	public void zeroMeanUnitLength()
	{
		float mean = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			mean += pixel[i];
		}
		mean /= (width*height);
		float sqr = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			pixel[i] -= mean;
			sqr += pixel[i]*pixel[i];
		}
		float len = (float)Math.sqrt(sqr);
		for(int i=0;i<(width*height);i++)
		{
			pixel[i] /= len;
		}
	}
	public void normalise()
	{
		float sqr = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			sqr += pixel[i]*pixel[i];
		}
		float len = (float)Math.sqrt(sqr);
		for(int i=0;i<(width*height);i++)
		{
			pixel[i] /= len;
		}
	}
	
	public void robustMeanNormalise3D()
	{
		float meanx = 0.0f;
		float meany = 0.0f;
		float meanz = 0.0f;
		int numPixels = (width*height)/3;
		int numValidPixels = 0;
		for(int i=0;i<numPixels;i++)
		{
			if(pixel[i*3+0]!=Float.MAX_VALUE)
			{
				meanx += pixel[i*3+0];
				meany += pixel[i*3+1];
				meanz += pixel[i*3+2];
				numValidPixels++;
			}
		}
		meanx /= (numValidPixels);
		meany /= (numValidPixels);
		meanz /= (numValidPixels);
		for(int i=0;i<(numPixels);i++)
		{
			if(pixel[i*3+0]!=Float.MAX_VALUE)
			{
				pixel[i*3+0] -= meanx;
				pixel[i*3+1] -= meany;
				pixel[i*3+2] -= meanz;
			}
		}
	}
	
	public void robustMeanNormalise3DZ()
	{
		float meanz = 0.0f;
		int numPixels = (width*height)/3;
		int numValidPixels = 0;
		for(int i=0;i<numPixels;i++)
		{
			if(pixel[i*3+0]!=Float.MAX_VALUE)
			{
				meanz += pixel[i*3+2];
				numValidPixels++;
			}
		}
		meanz /= (numValidPixels);
		for(int i=0;i<(numPixels);i++)
		{
			if(pixel[i*3+0]!=Float.MAX_VALUE)
			{
				pixel[i*3+2] -= meanz;
			}
		}
	}

	public void robustClamp3DZ(float min,float max)
	{
		int numPixels = (width*height)/3;
		for(int i=0;i<numPixels;i++)
		{
			if(pixel[i*3+0]!=Float.MAX_VALUE)
			{
				float z = pixel[i*3+2];
				pixel[i*3+2] = Math.max(Math.min(min, z),max);
			}
		}
	}

	
	public float interpolateLinear(int x,int y,float fx,float fy)
	{
		int i = x+y*width;
		  float fll = pixel[i];
		  float fhl = pixel[(i+1)];
		  float flh = pixel[(i+width)];
		  float fhh = pixel[(i+1+width)];
		
		  float ifx = 1.0f-fx;
		  float ify = 1.0f-fy;
		  return (ifx*ify*fll+fx*ify*fhl+ifx*fy*flh+fx*fy*fhh);
	}
	
	public float convolvePoint(float x,float y,RenderBufferFloat mask)
	{
	    int i, j;
	    float mysum = 0;
	    float offsetx = x - mask.width/2.0f  - 0.5f;
	    float offsety = y - mask.height/2.0f - 0.5f;

	    int foffsetx = (int)Math.floor(offsetx);
	    int foffsety = (int)Math.floor(offsety);
	    float fx = offsetx-foffsetx;
	    float fy = offsety-foffsety;
	    if((Math.abs(fx)<0.01f)&&(Math.abs(fy)<0.01f))
	    {
	        mysum = 0;
	        for(j = 0; j < mask.height; j++){
	            for(i = 0; i < mask.width; i++){
	                mysum += pixel[i+foffsetx+(j+foffsety)*width] * mask.pixel[i+j*mask.width];
	            }
	        }
	    }
	    else{
	        mysum = 0;
	        for(j = 0; j < mask.height; j++){
	            for(i = 0; i < mask.width; i++){
	            	//mysum += interpolateLinear(i+foffsetx,j+foffsety,fx,fy) * mask.pixel[i+j*mask.width];
	        		int ind = (i+foffsetx)+(j+foffsety)*width;
		      		  float fll = pixel[ind];
		      		  float fhl = pixel[(ind+1)];
		      		  float flh = pixel[(ind+width)];
		      		  float fhh = pixel[(ind+1+width)];
		      		
		      		  float ifx = 1.0f-fx;
		      		  float ify = 1.0f-fy;
	      		  	mysum += (ifx*ify*fll+fx*ify*fhl+ifx*fy*flh+fx*fy*fhh)* mask.pixel[i+j*mask.width];
	            }
	        }
	    }
		return mysum;
	}

	//Performs 2D minimum filtering on the input image, i.e., every output pixel is set to the minimum value within a rectangular block
	public void minFilter2D(int filterHeight, int filterWidth, int numChannels,RenderBufferFloat out)
	{
		int rwidth = width/numChannels;
		
		int i,j,k;
		float v;
		float tv;
		int ik,jk,ii,ji;
		int ind;

		int jKernelMidpoint = filterHeight/2;
		int iKernelMidpoint = filterWidth/2;

		for(k=0;k<numChannels;k++)
		{
			for(j=0;j<rwidth;j++)
			{
				for(i=0;i<height;i++)
				{
					ind = j*numChannels+k+i*width;
					v = pixel[ind];

					for(jk=0; jk<filterHeight; jk++)
					{
						ji = j + (jk-jKernelMidpoint); 
						if((ji<0)||(ji>=rwidth)) continue;

						for(ik=0; ik<filterHeight; ik++)
						{
							ii = i + (ik-iKernelMidpoint); 
							if((ii<0)||(ii>=height)) continue;

							tv = pixel[ji*numChannels+k+ii*width];

							if(v>tv) v = tv;
						}
					}
					out.pixel[ind] = v;
				}				
			}
		}
	}
	//Performs 2D maximum filtering on the input image, i.e., every output pixel is set to the maximum value within a rectangular block
	public void maxFilter2D(int filterHeight, int filterWidth, int numChannels, RenderBufferFloat out)
	{
		int rwidth = width/numChannels;
		
		int i,j,k;
		float v;
		float tv;
		int ik,jk,ii,ji;
		int ind;

		int jKernelMidpoint = filterHeight/2;
		int iKernelMidpoint = filterWidth/2;

		for(k=0;k<numChannels;k++)
		{
			for(j=0;j<rwidth;j++)
			{
				for(i=0;i<height;i++)
				{
					ind = j*numChannels+k+i*width;
					v = pixel[ind];

					for(jk=0; jk<filterHeight; jk++)
					{
						ji = j + (jk-jKernelMidpoint); 
						if((ji<0)||(ji>=rwidth)) continue;

						for(ik=0; ik<filterHeight; ik++)
						{
							ii = i + (ik-iKernelMidpoint); 
							if((ii<0)||(ii>=height)) continue;

							tv = pixel[ji*numChannels+k+ii*width];

							if(v<tv) v = tv;
						}
					}
					out.pixel[ind] = v;
				}				
			}
		}
	}
	
	public void scale(float s)
	{
		int numPixels = width*height;
		for(int i=0;i<numPixels;i++)
		{
			if(pixel[i]!=Float.MAX_VALUE)
				pixel[i] *= s;
		}
	}
	
	public void scaledExpOfMeanAbs(double alpha, int numChannels)
	{
		int i,j,k;
		float v;
		int ind;
		int rwidth = width/numChannels;
		for(j=0;j<rwidth;j++)
		{
			for(i=0;i<height;i++)
			{
				ind = j*numChannels+i*width;
				v = 0.0f;
				
				for(k=0;k<numChannels;k++)
				{
					v += Math.abs(pixel[ind+k]);
				}
				v/=numChannels;
				v = (float)Math.exp(-alpha*v);
				for(k=0;k<numChannels;k++)
				{
					pixel[ind+k]=v;
				}
			}
		}
	}
	
	public void calcInterpolatedPosition(float xf,float yf,GeneralMatrixFloat val)
	{
		float x = 0.0f;
		float y = 0.0f;
		float z = 0.0f;

		if(xf<0.0f)
			xf = 0.0f;
		if(yf<0.0f)
			yf = 0.0f;
		if(xf>1.0f)
			xf = 1.0f;
		if(yf>1.0f)
			yf = 1.0f;
		
		int rwidth = width/3;
		float minlonf = (xf*(rwidth-1));
		float minlatf = (yf*(height-1));
		int minlon = (int)minlonf;
		int minlat = (int)minlatf;

		float remlon = minlonf-minlon;
		float iremlon = 1.0f-remlon;
		float remlat = minlatf-minlat;
		float iremlat = 1.0f-remlat;
		
		int maxlon = minlon+1;
		int maxlat = minlat+1;
		if(maxlon==rwidth)
		{
			if(maxlat==height)
			{
				int ind0 = rwidth-1+(height-1)*rwidth;
				int ind3 = ind0*3;
				x = pixel[ind3+0];
				y = pixel[ind3+1];
				z = pixel[ind3+2];
			}
			else
			{
				int ind0 = (rwidth-1)+minlat*rwidth;
				int ind1 = (rwidth-1)+maxlat*rwidth;
				int ind3 = ind0*3;
				int ind4 = ind1*3;
				x = pixel[ind3+0]*iremlat;
				y = pixel[ind3+1]*iremlat;
				z = pixel[ind3+2]*iremlat;				
				x += pixel[ind4+0]*remlat;
				y += pixel[ind4+1]*remlat;
				z += pixel[ind4+2]*remlat;				
			}
		}
		else
		{			
			if(maxlat==height)
			{
				int ind0 = minlon+(height-1)*rwidth;
				int ind1 = maxlon+(height-1)*rwidth;
				int ind3 = ind0*3;
				int ind4 = ind1*3;
				x = pixel[ind3+0]*iremlon;
				y = pixel[ind3+1]*iremlon;
				z = pixel[ind3+2]*iremlon;				
				x += pixel[ind4+0]*remlon;
				y += pixel[ind4+1]*remlon;
				z += pixel[ind4+2]*remlon;				
			}
			else
			{
				int ind0 = minlon+minlat*rwidth;
				int ind1 = maxlon+minlat*rwidth;
				int ind2 = minlon+maxlat*rwidth;
				int ind3 = maxlon+maxlat*rwidth;
				int ind4 = ind0*3;
				int ind5 = ind1*3;
				int ind6 = ind2*3;
				int ind7 = ind3*3;
				if(ind4>pixel.length)
					System.out.println("?");
				x = pixel[ind4+0]*iremlon*iremlat;
				y = pixel[ind4+1]*iremlon*iremlat;
				z = pixel[ind4+2]*iremlon*iremlat;				
				x += pixel[ind5+0]*remlon*iremlat;
				y += pixel[ind5+1]*remlon*iremlat;
				z += pixel[ind5+2]*remlon*iremlat;				
				x += pixel[ind6+0]*iremlon*remlat;
				y += pixel[ind6+1]*iremlon*remlat;
				z += pixel[ind6+2]*iremlon*remlat;				
				x += pixel[ind7+0]*remlon*remlat;
				y += pixel[ind7+1]*remlon*remlat;
				z += pixel[ind7+2]*remlon*remlat;								
			}
		}	
		val.value[0] = x;
		val.value[1] = y;
		val.value[2] = z;
	}
}

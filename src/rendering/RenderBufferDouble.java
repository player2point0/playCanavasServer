package rendering;

import mathematics.GeneralMatrixDouble;

public class RenderBufferDouble 
{
	public int numChannels = 1;
	public int width;
	public int height;
	public double[] pixel; //one array of r,g,b or l
	
	public RenderBufferDouble()
	{
	}
	
	public RenderBufferDouble(int w,int h)
	{
		width = w;
		height = h;
		pixel = new double[w*h];
	}
	public RenderBufferDouble(int w,int h,int c)
	{
		numChannels = c;
		width = w;
		height = h;
		pixel = new double[w*h];
	}

	public void ensureCapacity(int size)
	{
		if((pixel==null)||(pixel.length<size))
		{
			pixel = new double[size];
		}
	}
	public void resize(int width,int height)
	{
		ensureCapacity(width*height);
		this.width = width;
		this.height = height;
	}

	public void clear(final double value)
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
	
	public void copy(final RenderBufferDouble rb)
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
	public void shiftImageXY(RenderBufferDouble result,int x,int y)
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
	public void clearMasked(RenderBuffer mask,double value)
	{
		for(int i=0;i<width*height;i++)
		{
			if(mask.pixel[i]!=0)
			{
				pixel[i] = value;
			}
		}
	}
	public void copyMasked(RenderBuffer mask,RenderBufferDouble value)
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
				pixel[i+j*width+0] = (r+g+b)/(255.0f*3.0f);
			}
		}
	}
	
	public void getAsUnnormalisedGreyscale(RenderBuffer rb,double min,double max)
	{
		for(int i=0;i<width*height;i++)
		{
			double f = pixel[i];
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
	public static void getAsUnnormalisedGreyscale(double[] pixelf,int[] pixel,int width,int height,double min,double max)
	{
		for(int i=0;i<width*height;i++)
		{
			double f = pixelf[i];
			if(f==Double.MAX_VALUE)
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
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for(int i=0;i<width*height;i++)
		{
			double f = pixel[i];
			if(f<min)
				min = f;
			if(f>max)
				max = f;
		}
		
		for(int i=0;i<width*height;i++)
		{
			double f = pixel[i];
			int g = (int)(255*(f-min)/(max-min));
			rb.pixel[i] = g | g<<8 | g<<16;
		}
	}

	public double sqrDistance(RenderBufferDouble other)
	{
		double sum = 0.0f;
		for(int i=0;i<width*height;i++)
		{
			double diff = (other.pixel[i]-pixel[i]); 
			sum += diff*diff;
		}	
		sum /= width*height;
		return sum;
	}

	public void sub(RenderBufferDouble a,RenderBufferDouble b)
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
	
	public void getAsColourscale(RenderBuffer rb)
	{
		double minr = Double.MAX_VALUE;
		double ming = Double.MAX_VALUE;
		double minb = Double.MAX_VALUE;
		double maxr = -Double.MAX_VALUE;
		double maxg = -Double.MAX_VALUE;
		double maxb = -Double.MAX_VALUE;
		for(int i=0;i<(width/3)*height;i++)
		{
			double f = pixel[i*3];
			if(f==Double.MAX_VALUE)
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
			double f = pixel[i*3];
			if(f==Double.MAX_VALUE)
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
	
	public void getAsRobustGreyscale(RenderBuffer rb)
	{
		double mean = 0.0f;
		double meansqr = 0.0f;
		double sum = 0.0f;
		for(int i=0;i<width*height;i++)
		{
			double f = pixel[i];
			if(f!=Double.MAX_VALUE)
			{
				mean += f;
				meansqr += f*f;
				sum += 1.0f;
			}
		}
		mean /= sum;
		meansqr /= sum;
		double sd = (double)Math.sqrt(Math.abs(meansqr-mean*mean));
		
		for(int i=0;i<width*height;i++)
		{
			double f = pixel[i];
			if(f!=Double.MAX_VALUE)
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
				rb.pixel[i] = 0;
			}
		}
	}
	public void zeroMeanUnitLength()
	{
		double mean = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			mean += pixel[i];
		}
		mean /= (width*height);
		double sqr = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			pixel[i] -= mean;
			sqr += pixel[i]*pixel[i];
		}
		double len = (double)Math.sqrt(sqr);
		for(int i=0;i<(width*height);i++)
		{
			pixel[i] /= len;
		}
	}
	
	public void getMeanAndScale(GeneralMatrixDouble meanAndScale)
	{
		double mean = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			mean += pixel[i];
		}
		mean /= (width*height);
		meanAndScale.value[0] = mean;
		double sqr = 0.0f;
		for(int i=0;i<(width*height);i++)
		{
			double p = pixel[i];
			p -= mean;
			sqr += p*p;
		}
		double len = (double)Math.sqrt(sqr);
		meanAndScale.value[1] = 1.0/len;
	}
	
	public void offsetAndScale(double offset,double scale)
	{
		for(int i=0;i<(width*height);i++)
		{
			double p = pixel[i];
			pixel[i] = (p-offset)*scale;
		}		
	}
	
	public double interpolateLinear(int x,int y,double fx,double fy)
	{
		int i = x+y*width;
		  double fll = pixel[i];
		  double fhl = pixel[(i+1)];
		  double flh = pixel[(i+width)];
		  double fhh = pixel[(i+1+width)];
		
		  double ifx = 1.0f-fx;
		  double ify = 1.0f-fy;
		  return (ifx*ify*fll+fx*ify*fhl+ifx*fy*flh+fx*fy*fhh);
	}
	
	public double convolvePoint(double x,double y,RenderBufferDouble mask)
	{
	    int i, j;
	    double mysum = 0;
	    double offsetx = x - mask.width/2.0f  - 0.5f;
	    double offsety = y - mask.height/2.0f - 0.5f;

	    int foffsetx = (int)Math.floor(offsetx);
	    int foffsety = (int)Math.floor(offsety);
	    double fx = offsetx-foffsetx;
	    double fy = offsety-foffsety;
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
		      		  double fll = pixel[ind];
		      		  double fhl = pixel[(ind+1)];
		      		  double flh = pixel[(ind+width)];
		      		  double fhh = pixel[(ind+1+width)];
		      		
		      		  double ifx = 1.0f-fx;
		      		  double ify = 1.0f-fy;
	      		  	mysum += (ifx*ify*fll+fx*ify*fhl+ifx*fy*flh+fx*fy*fhh)* mask.pixel[i+j*mask.width];
	            }
	        }
	    }
		return mysum;
	}

	//Performs 2D minimum filtering on the input image, i.e., every output pixel is set to the minimum value within a rectangular block
	public void minFilter2D(int filterHeight, int filterWidth,RenderBufferDouble out)
	{
		int rwidth = width/numChannels;
		
		int i,j,k;
		double v;
		double tv;
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
	public void maxFilter2D(int filterHeight, int filterWidth,RenderBufferDouble out)
	{
		int rwidth = width/numChannels;
		
		int i,j,k;
		double v;
		double tv;
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
	
	public void scaledExpOfMeanAbs(double alpha)
	{
		int i,j,k;
		double v;
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
				v = (double)Math.exp(-alpha*v);
				for(k=0;k<numChannels;k++)
				{
					pixel[ind+k]=v;
				}
			}
		}
	}}

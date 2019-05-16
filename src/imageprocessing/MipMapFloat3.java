package imageprocessing;

import rendering.RenderBufferFloat;

public class MipMapFloat3 extends RenderBufferFloat 
{
	public RenderBufferFloat rb;
	public int[] levelstart;
	public int[] leveldims;
	public int levels;
	
	static float[] tx = new float[4];
	static float[] ty = new float[4];
	static float[] tz = new float[4];
	
	public MipMapFloat3(int width,int height)
	{
		pixel = new float[width*3*height];
		levels = (int)(Math.log(height)/Math.log(2));
		levelstart = new int[levels];		
		leveldims = new int[levels];		
	}
	
	public void init(RenderBufferFloat rb)
	{
		this.rb = rb;
		width = rb.width/6;
		height = (int)(rb.height*0.75f);
		
		levelstart[0] = 0;

		int lastWidth = width;
		int lastHeight = rb.height/2;
		int sourceWidth = rb.width/3;
		int sourceHeight = rb.height;
		leveldims[0] = lastWidth;
		//First scale
		float t00x,t10x,t11x,t01x;
		float t00y,t10y,t11y,t01y;
		float t00z,t10z,t11z,t01z;
		int source;
		int i,rx,ry;
		for (int y=0;y<lastHeight;y++)
		{
			for (int x=0;x<lastWidth;x++)
			{
				rx = ((x)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t00x = rb.pixel[i*3+0];
				t00y = rb.pixel[i*3+1];
				t00z = rb.pixel[i*3+2];
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t10x = rb.pixel[i*3+0];
				t10y = rb.pixel[i*3+1];
				t10z = rb.pixel[i*3+2];
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				//System.out.println("rx:"+rx);
				//System.out.println("ry:"+ry);
				i = rx+ry*sourceWidth;
				t11x = rb.pixel[i*3+0];
				t11y = rb.pixel[i*3+1];
				t11z = rb.pixel[i*3+2];
				rx = ((x)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t01x = rb.pixel[i*3+0];
				t01y = rb.pixel[i*3+1];
				t01z = rb.pixel[i*3+2];
				pixel[(x+y*width)*3+0] = (t00x+t10x+t11x+t01x)*0.25f; 
				pixel[(x+y*width)*3+1] = (t00y+t10y+t11y+t01y)*0.25f; 
				pixel[(x+y*width)*3+2] = (t00z+t10z+t11z+t01z)*0.25f; 
			}
		}
		//System.out.println("Tada");

		int lastx = 0;
		int lasty = 0;
		int destx = 0;
		int desty = 0;
		int destWidth = 0;
		int destHeight = 0;
		boolean nextInX = false;
		for(int l=1;l<levels;l++)
		{
			if(nextInX)
			{
				destx += lastWidth;
			}
			else
			{
				desty += lastHeight;
			}
			levelstart[l] = destx+desty*width;
			nextInX = !nextInX;
			//pixel[levelstart[l]] = 0xFF0000;
			destWidth = lastWidth>>1;
			destHeight = lastHeight>>1;

			leveldims[l] = destWidth;
			System.out.println("level:"+l);
			System.out.println(" lastx:"+lastx);
			System.out.println(" lasty:"+lasty);
			System.out.println(" lastWidth:"+lastWidth);
			System.out.println(" lastHeight:"+lastHeight);
			System.out.println(" destx:"+destx);
			System.out.println(" desty:"+desty);
			System.out.println(" destWidth:"+destWidth);
			System.out.println(" destHeight:"+destHeight);
			scaleDown(lastx,lasty,lastWidth,lastHeight,destx,desty,destWidth,destHeight);
			lastWidth = destWidth;
			lastHeight = destHeight;
			lastx = destx;
			lasty = desty;
		}
	}
	public void init(RenderBufferFloat rb,float occlusionColour)
	{
		this.rb = rb;
		width = rb.width/6;
		height = (int)(rb.height*0.75f);
		
		levelstart[0] = 0;

		int lastWidth = width;
		int lastHeight = rb.height/2;
		int sourceWidth = rb.width/3;
		int sourceHeight = rb.height;
		leveldims[0] = lastWidth;
		//First scale
		int source;
		int i,rx,ry;
		int dest, numvp;
		
		for (int y=0;y<lastHeight;y++)
		{
			for (int x=0;x<lastWidth;x++)
			{
				numvp = 0;
				rx = ((x)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				tx[numvp] = rb.pixel[i*3+0];
				ty[numvp] = rb.pixel[i*3+1];
				tz[numvp] = rb.pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				tx[numvp] = rb.pixel[i*3+0];
				ty[numvp] = rb.pixel[i*3+1];
				tz[numvp] = rb.pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				//System.out.println("rx:"+rx);
				//System.out.println("ry:"+ry);
				i = rx+ry*sourceWidth;
				tx[numvp] = rb.pixel[i*3+0];
				ty[numvp] = rb.pixel[i*3+1];
				tz[numvp] = rb.pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((x)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				tx[numvp] = rb.pixel[i*3+0];
				ty[numvp] = rb.pixel[i*3+1];
				tz[numvp] = rb.pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				//pixel[x+y*width] = ((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2);
				//pixel[x+y*width] = 0xFF;
				//pixel[x+y*width] = t00;
				dest = x+y*width;
				switch(numvp)
				{
				case 0:
					pixel[dest*3+0] = occlusionColour;
					pixel[dest*3+1] = occlusionColour;
					pixel[dest*3+2] = occlusionColour;
					break;
				case 1:
					pixel[dest*3+0] = tx[0];
					pixel[dest*3+1] = ty[0];
					pixel[dest*3+2] = tz[0];
					break;
				case 2:
				{
					pixel[dest*3+0] = (tx[0]+tx[1])*0.5f;
					pixel[dest*3+1] = (ty[0]+ty[1])*0.5f;
					pixel[dest*3+2] = (tz[0]+tz[1])*0.5f;
					break;
				}
				case 3:
				{
					pixel[dest*3+0] = (tx[0]+tx[1]+tx[2])/3.0f;
					pixel[dest*3+1] = (ty[0]+ty[1]+ty[2])/3.0f;
					pixel[dest*3+2] = (tz[0]+tz[1]+tz[2])/3.0f;
					break;
				}
				case 4:
					pixel[dest*3+0] = (tx[0]+tx[1]+tx[2]+tx[3])*0.25f;
					pixel[dest*3+1] = (ty[0]+ty[1]+ty[2]+ty[3])*0.25f;
					pixel[dest*3+2] = (tz[0]+tz[1]+tz[2]+tz[3])*0.25f;
					break;
				}
			}
		}
		//System.out.println("Tada");

		int lastx = 0;
		int lasty = 0;
		int destx = 0;
		int desty = 0;
		int destWidth = 0;
		int destHeight = 0;
		boolean nextInX = false;
		for(int l=1;l<levels;l++)
		{
			if(nextInX)
			{
				destx += lastWidth;
			}
			else
			{
				desty += lastHeight;
			}
			levelstart[l] = destx+desty*width;
			nextInX = !nextInX;
			//pixel[levelstart[l]] = 0xFF0000;
			destWidth = lastWidth>>1;
			destHeight = lastHeight>>1;
			leveldims[l] = destWidth;
			System.out.println("level:"+l);
			System.out.println(" lastx:"+lastx);
			System.out.println(" lasty:"+lasty);
			System.out.println(" lastWidth:"+lastWidth);
			System.out.println(" lastHeight:"+lastHeight);
			System.out.println(" destx:"+destx);
			System.out.println(" desty:"+desty);
			System.out.println(" destWidth:"+destWidth);
			System.out.println(" destHeight:"+destHeight);
			scaleDownWithOcclusion(lastx,lasty,lastWidth,lastHeight,destx,desty,destWidth,destHeight,occlusionColour);
			lastWidth = destWidth;
			lastHeight = destHeight;
			lastx = destx;
			lasty = desty;
		}

//		for(int l=0;l<levels;l++)
//		{
//			int x = levelstart[l]%width;
//			int y = levelstart[l]/width;
//			pixel[levelstart[l]] = 0xFF0000;
//			System.out.println("levels = x:"+x+" y:"+y);
//		}
	}
	
	public void scaleDown(int sourcex,int sourcey,int sourceWidth,int sourceHeight,int destx,int desty,int destWidth,int destHeight)
	{
		System.out.println("sourcex:"+sourcex);
		System.out.println("sourcey:"+sourcey);
		float t00x,t10x,t11x,t01x;
		float t00y,t10y,t11y,t01y;
		float t00z,t10z,t11z,t01z;
		int dest,rx,ry,i,x2,y2;
		for (int y=0;y<destHeight;y++)
		{
			for (int x=0;x<destWidth;x++)
			{
				dest = (destx+x)+((desty+y)*width);
				x2 = x<<1;
				y2 = y<<1;
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				t00x = rb.pixel[i*3+0];
				t00y = rb.pixel[i*3+1];
				t00z = rb.pixel[i*3+2];
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				t10x = rb.pixel[i*3+0];
				t10y = rb.pixel[i*3+1];
				t10z = rb.pixel[i*3+2];
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t11x = rb.pixel[i*3+0];
				t11y = rb.pixel[i*3+1];
				t11z = rb.pixel[i*3+2];
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t01x = rb.pixel[i*3+0];
				t01y = rb.pixel[i*3+1];
				t01z = rb.pixel[i*3+2];

				//pixel[dest] = t00;
				pixel[(dest)*3+0] = (t00x+t10x+t11x+t01x)*0.25f; 
				pixel[(dest)*3+1] = (t00y+t10y+t11y+t01y)*0.25f; 
				pixel[(dest)*3+2] = (t00z+t10z+t11z+t01z)*0.25f; 
			}
		}
	}

	public void scaleDownWithOcclusion(int sourcex,int sourcey,int sourceWidth,int sourceHeight,int destx,int desty,int destWidth,int destHeight,float occlusionColour)
	{
		System.out.println("sourcex:"+sourcex);
		System.out.println("sourcey:"+sourcey);
		int dest,rx,ry,i,x2,y2;
		int numvp;
		for (int y=0;y<destHeight;y++)
		{
			for (int x=0;x<destWidth;x++)
			{
				numvp = 0;
				dest = (destx+x)+((desty+y)*width);
				x2 = x<<1;
				y2 = y<<1;
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				tx[numvp] = pixel[i*3];
				ty[numvp] = pixel[i*3+1];
				tz[numvp] = pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				tx[numvp] = pixel[i*3];
				ty[numvp] = pixel[i*3+1];
				tz[numvp] = pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				tx[numvp] = pixel[i*3];
				ty[numvp] = pixel[i*3+1];
				tz[numvp] = pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				tx[numvp] = pixel[i*3];
				ty[numvp] = pixel[i*3+1];
				tz[numvp] = pixel[i*3+2];
				if((tx[numvp])!=occlusionColour)
					numvp++;

				//pixel[dest] = t00;
				switch(numvp)
				{
				case 0:
					pixel[dest*3+0] = occlusionColour;
					pixel[dest*3+1] = occlusionColour;
					pixel[dest*3+2] = occlusionColour;
					break;
				case 1:
					pixel[dest*3+0] = tx[0];
					pixel[dest*3+1] = ty[0];
					pixel[dest*3+2] = tz[0];
					break;
				case 2:
				{
					pixel[dest*3+0] = (tx[0]+tx[1])*0.5f;
					pixel[dest*3+1] = (ty[0]+ty[1])*0.5f;
					pixel[dest*3+2] = (tz[0]+tz[1])*0.5f;
					break;
				}
				case 3:
				{
					pixel[dest*3+0] = (tx[0]+tx[1]+tx[2])/3.0f;
					pixel[dest*3+1] = (ty[0]+ty[1]+ty[2])/3.0f;
					pixel[dest*3+2] = (tz[0]+tz[1]+tz[2])/3.0f;
					break;
				}
				case 4:
					pixel[dest*3+0] = (tx[0]+tx[1]+tx[2]+tx[3])*0.25f;
					pixel[dest*3+1] = (ty[0]+ty[1]+ty[2]+ty[3])*0.25f;
					pixel[dest*3+2] = (tz[0]+tz[1]+tz[2]+tz[3])*0.25f;
					break;
				}
			}
		}
	}
	
	public final float getPixel(int x,int y,int ind,int level)
	{
		if(level==0)
			return rb.pixel[(x*3+y*rb.width)+ind];
		int lx = x>>level;
		int ly = y>>level;
		
		return pixel[(levelstart[level-1]+lx+ly*width)*3+ind];
	}

	public boolean isInterpolatedPixelOccluded(int x,int y,int level,float occlusionColour)
	{
		if(level==0)
			return ((rb.pixel[x*3+y*rb.width])==occlusionColour);
		
		float fx = x/(float)(rb.width-1);
		float fy = y/(float)(rb.height-1);

		float flx = fx*(leveldims[level-1]-1);
		float fly = fy*(leveldims[level-1]-1);
		int lx = (int)flx;
		int ly = (int)fly;

		float xf = flx-lx;
		float yf = fly-ly;
//		int lx = x>>level;
//		int ly = y>>level;
//	
//		if(
//				(lx>=(leveldims[level-1]-1))||
//				(ly>=(leveldims[level-1]-1))
//		  )
//		{
//			System.out.println("Getting wrong pixel");
//		}
		
		int i = levelstart[level-1]+lx+ly*width;
		tx[0] = pixel[i*3];
		tx[1] = pixel[((i+1)*3)%pixel.length];
		tx[2] = pixel[((i+width)*3)%pixel.length];
		tx[3] = pixel[((i+1+width)*3)%pixel.length];
	
		if((tx[0]==occlusionColour)||(tx[1]==occlusionColour)||(tx[2]==occlusionColour)||(tx[3]==occlusionColour))
		{
			return true;
		}		
		return false;
	}
	public final float interpolatePixelNoOcclusion(int x,int y,int ind,int level,float occlusionColour)
	{		
		if(level==0)
			return rb.pixel[x*3+y*rb.width+ind];
		
		float fx = x/(float)((rb.width/3)-1);
		float fy = y/(float)(rb.height-1);

		float flx = fx*(leveldims[level-1]-1);
		float fly = fy*(leveldims[level-1]-1);
		int lx = (int)flx;
		int ly = (int)fly;

		float xf = flx-lx;
		float yf = fly-ly;
//		int lx = x>>level;
//		int ly = y>>level;
//	
//		if(
//				(lx>=(leveldims[level-1]-1))||
//				(ly>=(leveldims[level-1]-1))
//		  )
//		{
//			System.out.println("Getting wrong pixel");
//		}
		
		int i = levelstart[level-1]+lx+ly*width;
		tx[0] = pixel[i*3];
		tx[1] = pixel[((i+1)*3)%pixel.length];
		tx[2] = pixel[((i+width)*3)%pixel.length];
		tx[3] = pixel[((i+1+width)*3)%pixel.length];
	
		if((tx[0]==occlusionColour)||(tx[1]==occlusionColour)||(tx[2]==occlusionColour)||(tx[3]==occlusionColour))
		{
			return occlusionColour;
		}

		ty[0] = pixel[i*3+1];
		ty[1] = pixel[((i+1)*3)%pixel.length+1];
		ty[2] = pixel[((i+width)*3)%pixel.length+1];
		ty[3] = pixel[((i+1+width)*3)%pixel.length+1];
			
		tz[0] = pixel[i*3+2];
		tz[1] = pixel[((i+1)*3)%pixel.length+2];
		tz[2] = pixel[((i+width)*3)%pixel.length+2];
		tz[3] = pixel[((i+1+width)*3)%pixel.length+2];
			
		float ixf = 1.0f-xf;
		float iyf = 1.0f-yf;
		
		if(ind==0)
		{
			return (tx[0]*ixf+tx[1]*xf)*iyf+(tx[2]*ixf+tx[3]*xf)*yf;
		}
		if(ind==1)
		{
			return (ty[0]*ixf+ty[1]*xf)*iyf+(ty[2]*ixf+ty[3]*xf)*yf;
		}

			return (tz[0]*ixf+tz[1]*xf)*iyf+(tz[2]*ixf+tz[3]*xf)*yf;
	}

	public final float interpolatePixel(int x,int y,int ind,int level,float occlusionColour)
	{		
		if(level==0)
			return rb.pixel[x*3+y*rb.width+ind];
		
		float fx = x/(float)((rb.width/3)-1);
		float fy = y/(float)(rb.height-1);

		float flx = fx*(leveldims[level-1]-1);
		float fly = fy*(leveldims[level-1]-1);
		int lx = (int)flx;
		int ly = (int)fly;

		float xf = flx-lx;
		float yf = fly-ly;
//		int lx = x>>level;
//		int ly = y>>level;
//	
//		if(
//				(lx>=(leveldims[level-1]-1))||
//				(ly>=(leveldims[level-1]-1))
//		  )
//		{
//			System.out.println("Getting wrong pixel");
//		}
		
		int i = levelstart[level-1]+lx+ly*width;
		tx[0] = pixel[i*3];
		tx[1] = pixel[((i+1)*3)%pixel.length];
		tx[2] = pixel[((i+width)*3)%pixel.length];
		tx[3] = pixel[((i+1+width)*3)%pixel.length];
	
		ty[0] = pixel[i*3+1];
		ty[1] = pixel[((i+1)*3)%pixel.length+1];
		ty[2] = pixel[((i+width)*3)%pixel.length+1];
		ty[3] = pixel[((i+1+width)*3)%pixel.length+1];
			
		tz[0] = pixel[i*3+2];
		tz[1] = pixel[((i+1)*3)%pixel.length+2];
		tz[2] = pixel[((i+width)*3)%pixel.length+2];
		tz[3] = pixel[((i+1+width)*3)%pixel.length+2];
//		if((t[0]==occlusionColour)&&(t[1]==occlusionColour)&&(t[2]==occlusionColour)&&(t[3]==occlusionColour))
//		{
//			return occlusionColour;
//		}
			
//		float xf = x-(lx<<level);
//		float yf = y-(ly<<level);
//		xf /= (float)(1<<level);
//		yf /= (float)(1<<level);

		float ixf = 1.0f-xf;
		float iyf = 1.0f-yf;
		
		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;
		float sum = 0.0f;

		if(tx[0]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += tx[0]*factor; 
			g += ty[0]*factor; 
			b += tz[0]*factor;
			sum += factor;
		}
		if(tx[1]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += tx[1]*factor; 
			g += ty[1]*factor; 
			b += tz[1]*factor;
			sum += factor;
		}
		if(tx[2]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += tx[2]*factor; 
			g += ty[2]*factor; 
			b += tz[2]*factor;
			sum += factor;
		}
		if(tx[3]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += tx[3]*factor; 
			g += ty[3]*factor; 
			b += tz[3]*factor;
			sum += factor;
		}
		

		if(sum<0.00001f)
			return occlusionColour;
		
		if(ind==0)
		{
			return r/sum;
		}
		if(ind==1)
		{
			return g/sum;
		}

			return b/sum;
	}

}

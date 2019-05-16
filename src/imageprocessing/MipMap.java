package imageprocessing;

import rendering.RenderBuffer;

public class MipMap extends RenderBuffer
{
	public RenderBuffer rb;
	public int[] levelstart;
	public int[] leveldims;
	public int levels;

	static final int MASK7Bit=0xFEFEFF;  // mask for additive/subtractive shading
	static final int MASK6Bit=0xFCFCFC;  // mask for additive/subtractive shading
	static int[] t = new int[4];

	public MipMap(RenderBuffer rb)
	{
		pixel = new int[rb.width*rb.height];
		levels = (int)(Math.log(rb.height)/Math.log(2));
		levelstart = new int[levels];
		leveldims = new int[levels];

		init(rb);
	}

	public MipMap(int width,int height)
	{
		pixel = new int[width*height];
		levels = (int)(Math.log(height)/Math.log(2));
		levelstart = new int[levels];
		leveldims = new int[levels];
	}

	public static void scaleDown(RenderBuffer rb,RenderBuffer srb)
	{
		int rx,ry,i,t00,t10,t11,t01;
		srb.resize(rb.width/2, rb.height/2);
		for (int y=0;y<srb.height;y++)
		{
			for (int x=0;x<srb.width;x++)
			{
				rx = ((x)<<1)%rb.width;
				ry = (((y)<<1)%rb.height);
				i = rx+ry*rb.width;
				t00 = rb.pixel[i];
				rx = (((x)<<1)+1)%rb.width;
				ry = (((y)<<1)%rb.height);
				i = rx+ry*rb.width;
				t10 = rb.pixel[i];
				rx = (((x)<<1)+1)%rb.width;
				ry = ((((y)<<1)+1)%rb.height);

				i = rx+ry*rb.width;
				t11 = rb.pixel[i];
				rx = ((x)<<1)%rb.width;
				ry = ((((y)<<1)+1)%rb.height);
				i = rx+ry*rb.width;
				t01 = rb.pixel[i];
				int a00 = (t00&0xFF000000)>>>24;
				int a10 = (t10&0xFF000000)>>>24;
				int a11 = (t11&0xFF000000)>>>24;
				int a01 = (t01&0xFF000000)>>>24;
				int a = ((a00+a10+a11+a01)<<(24-2))&0xFF000000;
				srb.pixel[x+y*srb.width] = a|(((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2));
			}
		}
	}

	public void init(RenderBuffer rb)
	{
		this.rb = rb;
		width = rb.width/2;
		height = (int)(rb.height*0.75f);

		levelstart[0] = 0;

		int lastWidth = width;
		int lastHeight = rb.height/2;
		int sourceWidth = rb.width;
		int sourceHeight = rb.height;
		leveldims[0] = lastWidth;
		//First scale
		int t00,t10,t11,t01;
		int source;
		int i,rx,ry;
		for (int y=0;y<lastHeight;y++)
		{
			for (int x=0;x<lastWidth;x++)
			{
				rx = ((x)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t00 = rb.pixel[i];
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t10 = rb.pixel[i];
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				//System.out.println("rx:"+rx);
				//System.out.println("ry:"+ry);
				i = rx+ry*sourceWidth;
				t11 = rb.pixel[i];
				rx = ((x)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t01 = rb.pixel[i];
				int a00 = (t00&0xFF000000)>>>24;
				int a10 = (t10&0xFF000000)>>>24;
				int a11 = (t11&0xFF000000)>>>24;
				int a01 = (t01&0xFF000000)>>>24;
				int a = ((a00+a10+a11+a01)<<(24-2))&0xFF000000;
				pixel[x+y*width] = a|(((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2));
				//pixel[x+y*width] = 0xFF;
				//pixel[x+y*width] = t00;
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
//			System.out.println("level:"+l);
//			System.out.println(" lastx:"+lastx);
//			System.out.println(" lasty:"+lasty);
//			System.out.println(" lastWidth:"+lastWidth);
//			System.out.println(" lastHeight:"+lastHeight);
//			System.out.println(" destx:"+destx);
//			System.out.println(" desty:"+desty);
//			System.out.println(" destWidth:"+destWidth);
//			System.out.println(" destHeight:"+destHeight);
			scaleDown(lastx,lasty,lastWidth,lastHeight,destx,desty,destWidth,destHeight);
			lastWidth = destWidth;
			lastHeight = destHeight;
			lastx = destx;
			lasty = desty;
		}
	}
	public void init(RenderBuffer rb,int occlusionColour)
	{
		this.rb = rb;
		width = rb.width/2;
		height = (int)(rb.height*0.75f);

		levelstart[0] = 0;

		int lastWidth = width;
		int lastHeight = rb.height/2;
		int sourceWidth = rb.width;
		int sourceHeight = rb.height;
		leveldims[0] = lastWidth;
		//First scale
		int t00,t10,t11,t01;
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
				t[numvp] = rb.pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t[numvp] = rb.pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((x+1)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				//System.out.println("rx:"+rx);
				//System.out.println("ry:"+ry);
				i = rx+ry*sourceWidth;
				t[numvp] = rb.pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((x)<<1)%sourceWidth;
				ry = (((y+1)<<1)%sourceHeight);
				i = rx+ry*sourceWidth;
				t[numvp] = rb.pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				//pixel[x+y*width] = ((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2);
				//pixel[x+y*width] = 0xFF;
				//pixel[x+y*width] = t00;
				dest = x+y*width;
				switch(numvp)
				{
				case 0:
					pixel[dest] = occlusionColour;
					break;
				case 1:
					pixel[dest] = t[0];
					break;
				case 2:
				{
					int r0 = (t[0]&0xFF0000)>>16;
					int g0 = (t[0]&0xFF00)>>8;
					int b0 = (t[0]&0xFF);
					int r1 = (t[1]&0xFF0000)>>16;
					int g1 = (t[1]&0xFF00)>>8;
					int b1 = (t[1]&0xFF);
					int r = (r0+r1)/2;
					int g = (g0+g1)/2;
					int b = (b0+b1)/2;
					if((r0==0)&&(g0==0)&&(b0==0))
					{
						System.out.println("OOps");
					}
					if((r1==0)&&(g1==0)&&(b1==0))
					{
						System.out.println("OOps");
					}
					pixel[dest] = (r<<16)|(g<<8)|(b);
					//pixel[dest] = ((t[0]&MASK7Bit)>>1)+((t[1]&MASK7Bit)>>2);
					break;
				}
				case 3:
				{
					int r0 = (t[0]&0xFF0000)>>16;
					int g0 = (t[0]&0xFF00)>>8;
					int b0 = (t[0]&0xFF);
					int r1 = (t[1]&0xFF0000)>>16;
					int g1 = (t[1]&0xFF00)>>8;
					int b1 = (t[1]&0xFF);
					int r2 = (t[2]&0xFF0000)>>16;
					int g2 = (t[2]&0xFF00)>>8;
					int b2 = (t[2]&0xFF);
					int r = (r0+r1+r2)/3;
					int g = (g0+g1+g2)/3;
					int b = (b0+b1+b2)/3;
					if((r0==0)&&(g0==0)&&(b0==0))
					{
						System.out.println("OOps");
					}
					if((r1==0)&&(g1==0)&&(b1==0))
					{
						System.out.println("OOps");
					}
					if((r2==0)&&(g2==0)&&(b2==0))
					{
						System.out.println("OOps");
					}
					pixel[dest] = (r<<16)|(g<<8)|(b);
					break;
				}
				case 4:
					pixel[dest] = ((t[0]&MASK6Bit)>>2)+((t[1]&MASK6Bit)>>2)+((t[2]&MASK6Bit)>>2)+((t[3]&MASK6Bit)>>2);
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
//		System.out.println("sourcex:"+sourcex);
//		System.out.println("sourcey:"+sourcey);
		int t00,t10,t11,t01,dest,rx,ry,i,x2,y2;
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
				t00 = pixel[i];
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				t10 = pixel[i];
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t11 = pixel[i];
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t01 = pixel[i];

				//pixel[dest] = t00;
				int a00 = (t00&0xFF000000)>>>24;
				int a10 = (t10&0xFF000000)>>>24;
				int a11 = (t11&0xFF000000)>>>24;
				int a01 = (t01&0xFF000000)>>>24;
				int a = ((a00+a10+a11+a01)<<(24-2))&0xFF000000;
				pixel[dest] = a|(((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2));
				//pixel[dest] = ((t00&MASK6Bit)>>2)+((t10&MASK6Bit)>>2)+((t11&MASK6Bit)>>2)+((t01&MASK6Bit)>>2);
			}
		}
	}

	public void scaleDownWithOcclusion(int sourcex,int sourcey,int sourceWidth,int sourceHeight,int destx,int desty,int destWidth,int destHeight,int occlusionColour)
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
				t[numvp] = pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+y2%sourceHeight));
				i = rx+ry*width;
				t[numvp] = pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+(x2+1)%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t[numvp] = pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;
				rx = ((sourcex+x2%sourceWidth));
				ry = ((sourcey+(y2+1)%sourceHeight));
				i = rx+ry*width;
				t[numvp] = pixel[i]&0xFFFFFF;
				if((t[numvp])!=occlusionColour)
					numvp++;

				//pixel[dest] = t00;
				switch(numvp)
				{
				case 0:
					pixel[dest] = occlusionColour;
					break;
				case 1:
					pixel[dest] = t[0];
					break;
				case 2:
				{
					int r0 = (t[0]&0xFF0000)>>16;
					int g0 = (t[0]&0xFF00)>>8;
					int b0 = (t[0]&0xFF);
					int r1 = (t[1]&0xFF0000)>>16;
					int g1 = (t[1]&0xFF00)>>8;
					int b1 = (t[1]&0xFF);

					if((r0==0)&&(g0==0)&&(b0==0))
					{
						System.out.println("OOps");
					}
					if((r1==0)&&(g1==0)&&(b1==0))
					{
						System.out.println("OOps");
					}

					int r = (r0+r1)/2;
					int g = (g0+g1)/2;
					int b = (b0+b1)/2;
					pixel[dest] = (r<<16)|(g<<8)|(b);
					//pixel[dest] = ((t[0]&MASK7Bit)>>1)+((t[1]&MASK7Bit)>>2);
					break;
				}
				case 3:
					int r0 = (t[0]&0xFF0000)>>16;
					int g0 = (t[0]&0xFF00)>>8;
					int b0 = (t[0]&0xFF);
					int r1 = (t[1]&0xFF0000)>>16;
					int g1 = (t[1]&0xFF00)>>8;
					int b1 = (t[1]&0xFF);
					int r2 = (t[2]&0xFF0000)>>16;
					int g2 = (t[2]&0xFF00)>>8;
					int b2 = (t[2]&0xFF);

					if((r0==0)&&(g0==0)&&(b0==0))
					{
						System.out.println("OOps");
					}
					if((r1==0)&&(g1==0)&&(b1==0))
					{
						System.out.println("OOps");
					}
					if((r2==0)&&(g2==0)&&(b2==0))
					{
						System.out.println("OOps");
					}
					int r = (r0+r1+r2)/3;
					int g = (g0+g1+g2)/3;
					int b = (b0+b1+b2)/3;
					pixel[dest] = (r<<16)|(g<<8)|(b);
					break;
				case 4:
					pixel[dest] = ((t[0]&MASK6Bit)>>2)+((t[1]&MASK6Bit)>>2)+((t[2]&MASK6Bit)>>2)+((t[3]&MASK6Bit)>>2);
					break;
				}
			}
		}
	}

	public final int getPixel(int x,int y,int level)
	{
		if(level==0)
			return rb.pixel[x+y*rb.width];
		int lx = x>>level;
		int ly = y>>level;

		return pixel[levelstart[level-1]+lx+ly*width];
	}

	public final void getLevel(int level, RenderBuffer rb)
	{
		if(level==0)
		{
			rb.resize(this.rb.width, this.rb.height);
			rb.copy(this.rb);
			return;
		}
		int w = this.rb.width>>level;
		int h = this.rb.height>>level;
		rb.resize(w, h);

		int ind = 0;
		for(int y=0;y<h;y++)
		{
			for(int x=0;x<h;x++)
			{
				rb.pixel[ind] = pixel[levelstart[level-1]+x+y*width];
				ind++;
			}
		}
	}

	public int getSize(int level)
	{
		return leveldims[level-1];
	}

	public boolean isInterpolatedPixelOccluded(int x,int y,int level,int occlusionColour)
	{
		if(level==0)
			return ((rb.pixel[x+y*rb.width]&0xFFFFFF)==occlusionColour);

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
		t[0] = pixel[i]&0xFFFFFF;
		t[1] = pixel[(i+1)%pixel.length]&0xFFFFFF;
		t[2] = pixel[(i+width)%pixel.length]&0xFFFFFF;
		t[3] = pixel[(i+1+width)%pixel.length]&0xFFFFFF;

		if((t[0]==occlusionColour)||(t[1]==occlusionColour)||(t[2]==occlusionColour)||(t[3]==occlusionColour))
		{
			return true;
		}
		return false;
	}

	public final void interpolate(float x,float y,int level,float[] tex,int at)
	{
		float xf;
		float yf;

		if(level==0)
		{
			int lx = (int)x;
			int ly = (int)y;
			xf = x-lx;
			yf = y-ly;

			int i = lx+ly*rb.width;

			t[0] = pixel[i];
			t[1] = pixel[(i+1)%rb.pixel.length];
			t[2] = pixel[(i+rb.width)%rb.pixel.length];
			t[3] = pixel[(i+1+rb.width)%rb.pixel.length];
		}
		else
		{
			float fx = x/(float)(rb.width-1);
			float fy = y/(float)(rb.height-1);
			float flx = fx*(leveldims[level-1]-1);
			float fly = fy*(leveldims[level-1]-1);
			int lx = (int)flx;
			int ly = (int)fly;

			xf = flx-lx;
			yf = fly-ly;

			int i = levelstart[level-1]+lx+ly*width;
			t[0] = pixel[i];
			t[1] = pixel[(i+1)%pixel.length];
			t[2] = pixel[(i+width)%pixel.length];
			t[3] = pixel[(i+1+width)%pixel.length];

		}
		float ixf = 1.0f-xf;
		float iyf = 1.0f-yf;

		int r0 = (t[0]&0xFF0000)>>16;
		int g0 = (t[0]&0xFF00)>>8;
		int b0 = (t[0]&0xFF);
		int r1 = (t[1]&0xFF0000)>>16;
		int g1 = (t[1]&0xFF00)>>8;
		int b1 = (t[1]&0xFF);
		int r2 = (t[2]&0xFF0000)>>16;
		int g2 = (t[2]&0xFF00)>>8;
		int b2 = (t[2]&0xFF);
		int r3 = (t[3]&0xFF0000)>>16;
		int g3 = (t[3]&0xFF00)>>8;
		int b3 = (t[3]&0xFF);

		float r = ((r0*ixf+r1*xf)*iyf+(r2*ixf+r3*xf)*yf);
		float g = ((g0*ixf+g1*xf)*iyf+(g2*ixf+g3*xf)*yf);
		float b = ((b0*ixf+b1*xf)*iyf+(b2*ixf+b3*xf)*yf);

		tex[at] = r;
		tex[at+1] = g;
		tex[at+2] = b;
	}

	public final int interpolatePixelNoOcclusion(int x,int y,int level,int occlusionColour)
	{
		if(level==0)
			return rb.pixel[x+y*rb.width];

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
		t[0] = pixel[i];
		t[1] = pixel[(i+1)%pixel.length];
		t[2] = pixel[(i+width)%pixel.length];
		t[3] = pixel[(i+1+width)%pixel.length];

		if((t[0]==occlusionColour)||(t[1]==occlusionColour)||(t[2]==occlusionColour)||(t[3]==occlusionColour))
		{
			return occlusionColour;
		}

//		float xf = x-(lx<<level);
//		float yf = y-(ly<<level);
//		xf /= (float)(1<<level);
//		yf /= (float)(1<<level);

		float ixf = 1.0f-xf;
		float iyf = 1.0f-yf;

		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;

		if(t[0]!=occlusionColour)
		{

		}

		int r0 = (t[0]&0xFF0000)>>16;
		int g0 = (t[0]&0xFF00)>>8;
		int b0 = (t[0]&0xFF);
		int r1 = (t[1]&0xFF0000)>>16;
		int g1 = (t[1]&0xFF00)>>8;
		int b1 = (t[1]&0xFF);
		int r2 = (t[2]&0xFF0000)>>16;
		int g2 = (t[2]&0xFF00)>>8;
		int b2 = (t[2]&0xFF);
		int r3 = (t[3]&0xFF0000)>>16;
		int g3 = (t[3]&0xFF00)>>8;
		int b3 = (t[3]&0xFF);



		int ri = (int)((r0*ixf+r1*xf)*iyf+(r2*ixf+r3*xf)*yf);
		int gi = (int)((g0*ixf+g1*xf)*iyf+(g2*ixf+g3*xf)*yf);
		int bi = (int)((b0*ixf+b1*xf)*iyf+(b2*ixf+b3*xf)*yf);
		return (ri<<16)|(gi<<8)|(bi);
	}

	public final int interpolatePixel(int x,int y,int level,int occlusionColour)
	{
		if(level==0)
			return rb.pixel[x+y*rb.width];

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
		t[0] = pixel[i];
		t[1] = pixel[(i+1)%pixel.length];
		t[2] = pixel[(i+width)%pixel.length];
		t[3] = pixel[(i+1+width)%pixel.length];

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

		int r0 = (t[0]&0xFF0000)>>16;
		int g0 = (t[0]&0xFF00)>>8;
		int b0 = (t[0]&0xFF);
		int r1 = (t[1]&0xFF0000)>>16;
		int g1 = (t[1]&0xFF00)>>8;
		int b1 = (t[1]&0xFF);
		int r2 = (t[2]&0xFF0000)>>16;
		int g2 = (t[2]&0xFF00)>>8;
		int b2 = (t[2]&0xFF);
		int r3 = (t[3]&0xFF0000)>>16;
		int g3 = (t[3]&0xFF00)>>8;
		int b3 = (t[3]&0xFF);

		if(t[0]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += r0*factor;
			g += g0*factor;
			b += b0*factor;
			sum += factor;
		}
		if(t[1]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += r1*factor;
			g += g1*factor;
			b += b1*factor;
			sum += factor;
		}
		if(t[2]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += r2*factor;
			g += g2*factor;
			b += b2*factor;
			sum += factor;
		}
		if(t[3]!=occlusionColour)
		{
			float factor = ixf*iyf;
			r += r3*factor;
			g += g3*factor;
			b += b3*factor;
			sum += factor;
		}


		if(sum<0.00001f)
			return occlusionColour;

		int ri = (int)(r/sum);
		int gi = (int)(g/sum);
		int bi = (int)(b/sum);
		return (ri<<16)|(gi<<8)|(bi);
	}

}

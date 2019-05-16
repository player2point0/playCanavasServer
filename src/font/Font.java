package font;

import mathematics.GeneralMatrixFloat;
import rendering.RenderBuffer;


public class Font 
{
	static final int BINARY = 0;
	static final int BINARY_FIXED_WIDTH = 1;
	
	static final int GRAYSCALE = 2;
	static final int GRAYSCALE_FIXED_WIDTH = 3;
	
	static final int COLOUR = 4;
	static final int COLOUR_FIXED_WIDTH = 5;
	
	int type = GRAYSCALE_FIXED_WIDTH;
	int numChars = 256;
	int numBlocks = 1;
	int unicodes[]; //start and stop of runs of unicode chars
	int leftTopAdvance[]; //= new int[numChars*3];
	int pixelTopLeftDims[]; //= new int[numChars*3];
	int pixels[];
	
	public int totalAboveLine = 0;
	public int totalBelowLine = 0;
	
	public int getHeight()
	{
		return totalAboveLine+totalBelowLine;
	}
	
	public int getCursorOffset(int cursor,String chars,int offset,int length)
	{
		int cind = 0;
		int totalAdvance = 0;
		if(cursor>length)
			cursor = length;
		for(int ci=0;ci<cursor;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
	}
	
	public int Rasterise(RenderBuffer rb,int x,int y,int[] chars,int offset,int length)
	{
		return 0;	
	}
	public int Rasterise(RenderBuffer rb,int x,int y,String chars)
	{		
		return Rasterise(rb, x, y, chars, 0, chars.length());
	}
	public int Rasterise(RenderBuffer rb,int x,int y,String chars,int offset,int length)
	{		
		int cind = 0;
		int totalAdvance = 0;
		for(int ci=0;ci<length;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int px, py;
			int pi = pixelTopLeftDims[l*3+0];
			for(int j=0;j<pixelTopLeftDims[l*3+2];j++)
			{
				px = x+leftTopAdvance[l*3+0]+totalAdvance;
				py = y+j+leftTopAdvance[l*3+1];
				for(int i=0;i<pixelTopLeftDims[l*3+1];i++)
				{
					if((px>=0)&&(px<rb.width)&&(py>=0)&&(py<rb.height)
							&&((pixels[pi]&0xFF000000)!=0xFF000000))
					{
						int a = (pixels[pi]&0xFF000000)>>>24;
						int r = (pixels[pi]&0xFF0000)>>16;
						int g = (pixels[pi]&0xFF00)>>8;
						int b = (pixels[pi]&0xFF);
						int ia = 0x100-a;
			
						int ind = px+py*rb.width;
						int pr = (rb.pixel[ind]&0xFF0000)>>16;
						int pg = (rb.pixel[ind]&0xFF00)>>8;
						int pb = (rb.pixel[ind]&0xFF);
	
						r = (r*ia+pr*a)>>8;
						g = (g*ia+pg*a)>>8;
						b = (b*ia+pb*a)>>8;
						rb.pixel[ind] = (r<<16)|(g<<8)|b;
					}
					pi++;
					px++;
				}
			}
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
	}

	public void calcDimensionsCentered(GeneralMatrixFloat dims,float cx,float cy,String chars,int offset,int length)
	{
		int height = totalAboveLine+totalBelowLine;
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		int x = (int)(cx-width*0.5f);
		int y = (int)(cy-height*0.5f);
		dims.value[0] = x;
		dims.value[1] = y;
		dims.value[2] = width;
		dims.value[3] = height;
	}
	public void calcDimensions(GeneralMatrixFloat dims,float cx,float cy,String chars,int offset,int length)
	{
		int height = totalAboveLine+totalBelowLine;
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		int x = (int)(cx);
		int y = (int)(cy-totalAboveLine);
		dims.value[0] = x;
		dims.value[1] = y;
		dims.value[2] = width;
		dims.value[3] = height;
	}
	public float calcWidth(String chars)
	{
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		return width;
	}
	
	public float getCursorCenterX(int cursor, float cx, float cy, String chars)
	{
		int cind = 0;
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		int x = (int)(cx-width*0.5f);
		
		int offset = 0;
		
		if(cursor>chars.length())
			cursor = chars.length();
		
		int totalAdvance = 0;
		for(int ci=0;ci<cursor;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance+x;
	}
	public float getCursorForX(float mx,String chars)
	{
		int cind = 0;
		if(mx<0.0f)
		{
			return 0.0f;
		}
		
		float min = 0.0f;
		float max = 0.0f;
		for(int ci=0;ci<chars.length();ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int totalAdvance = leftTopAdvance[l*3+2]*scale;
			min = max;
			max += totalAdvance;
			if(mx<max)
			{
				float factor = (mx-min)/(max-min);
				return ci+factor;
			}
			cind += scale;
		}
		return chars.length()+0.1f;

	}
	public float getCursorX(int cursor, float cx, float cy, String chars)
	{
		int cind = 0;
		//int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		//width += totalBelowLine;
		int x = (int)(cx);
		
		int offset = 0;
		
		int totalAdvance = 0;
		if(cursor>chars.length())
			cursor = chars.length();
		
		for(int ci=0;ci<cursor;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance+x;
	}
	public int RasteriseCentered(RenderBuffer rb,int colour,float cx,float cy,String chars,int minx,int miny,int maxx,int maxy)
	{		
		int height = totalAboveLine+totalBelowLine;
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		int x = (int)(cx-width*0.5f);
		int y = (int)(cy-height*0.5f) + totalAboveLine;
		
		return RasteriseColoured(rb, x, y, chars, 0, chars.length(), colour, minx, miny, maxx, maxy);
	}	
	public int RasteriseCentered(RenderBuffer rb,float cx,float cy,String chars,int offset,int length)
	{		
		int cind = 0;
		int height = totalAboveLine+totalBelowLine;
		int width = getCursorOffset(chars.length(), chars, 0, chars.length());
		width += totalBelowLine;
		int x = (int)(cx-width*0.5f);
		int y = (int)(cy-height*0.5f) + totalAboveLine;
		
		int totalAdvance = 0;
		for(int ci=0;ci<length;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int px, py;
			int pi = pixelTopLeftDims[l*3+0];
			for(int j=0;j<pixelTopLeftDims[l*3+2];j++)
			{
				px = x+leftTopAdvance[l*3+0]+totalAdvance;
				py = y+j+leftTopAdvance[l*3+1];
				for(int i=0;i<pixelTopLeftDims[l*3+1];i++)
				{
					if((px>=0)&&(px<rb.width)&&(py>=0)&&(py<rb.height)
							&&((pixels[pi]&0xFF000000)!=0xFF000000))
					{
						int a = (pixels[pi]&0xFF000000)>>>24;
						int r = (pixels[pi]&0xFF0000)>>16;
						int g = (pixels[pi]&0xFF00)>>8;
						int b = (pixels[pi]&0xFF);
						int ia = 0x100-a;
			
						int ind = px+py*rb.width;
						int pr = (rb.pixel[ind]&0xFF0000)>>16;
						int pg = (rb.pixel[ind]&0xFF00)>>8;
						int pb = (rb.pixel[ind]&0xFF);
	
						r = (r*ia+pr*a)>>8;
						g = (g*ia+pg*a)>>8;
						b = (b*ia+pb*a)>>8;
						rb.pixel[ind] = (r<<16)|(g<<8)|b;
					}
					pi++;
					px++;
				}
			}
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
	}
	
	public int BlitColoured(RenderBuffer rb,int x,int y,String chars,int offset,int length,int colour)
	{		
		int cind = 0;
		int totalAdvance = 0;
		for(int ci=0;ci<length;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int px, py;
			int pi = pixelTopLeftDims[l*3+0];
			for(int j=0;j<pixelTopLeftDims[l*3+2];j++)
			{
				px = x+leftTopAdvance[l*3+0]+totalAdvance;
				py = y+j+leftTopAdvance[l*3+1];
				for(int i=0;i<pixelTopLeftDims[l*3+1];i++)
				{
					if((px>=0)&&(px<rb.width)&&(py>=0)&&(py<rb.height)
							&&((pixels[pi]&0xFF000000)!=0xFF000000))
					{
						int a = (pixels[pi]&0xFF000000)>>>24;
						int r = (colour&0xFF0000)>>16;
						int g = (colour&0xFF00)>>8;
						int b = (colour&0xFF);
						
						int ind = px+py*rb.width;

						rb.pixel[ind] = (a<<24)|(r<<16)|(g<<8)|b;
					}
					pi++;
					px++;
				}
			}
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
	}

	public int RasteriseTopLeftColoured(RenderBuffer rb,int colour,float fx,float fy,String chars,int minx,int miny,int maxx,int maxy)
	{	
		int cind = 0;
		int x = (int)fx;
		int y = (int)(fy+totalAboveLine);
		int offset = 0;
		int length = chars.length();
		
		int totalAdvance = 0;
		for(int ci=0;ci<length;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int px, py;
			int pi = pixelTopLeftDims[l*3+0];
			for(int j=0;j<pixelTopLeftDims[l*3+2];j++)
			{
				px = x+leftTopAdvance[l*3+0]+totalAdvance;
				py = y+j+leftTopAdvance[l*3+1];
				for(int i=0;i<pixelTopLeftDims[l*3+1];i++)
				{
					if((px>=minx)&&(px<maxx)&&(py>=miny)&&(py<maxy)
							&&((pixels[pi]&0xFF000000)!=0xFF000000))
					{
						int a = (pixels[pi]&0xFF000000)>>>24;
						int r = (colour&0xFF0000)>>16;
						int g = (colour&0xFF00)>>8;
						int b = (colour&0xFF);
						int ia = 0x100-a;
			
						int ind = px+py*rb.width;
						int pr = (rb.pixel[ind]&0xFF0000)>>16;
						int pg = (rb.pixel[ind]&0xFF00)>>8;
						int pb = (rb.pixel[ind]&0xFF);
	
						r = (r*ia+pr*a)>>8;
						g = (g*ia+pg*a)>>8;
						b = (b*ia+pb*a)>>8;
						rb.pixel[ind] = (r<<16)|(g<<8)|b;
					}
					pi++;
					px++;
				}
			}
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
		
	}
	public int RasteriseColoured(RenderBuffer rb,int x,int y,String chars,int offset,int length,int colour,int minx,int miny,int maxx,int maxy)
	{		
		int cind = 0;
		int totalAdvance = 0;
		for(int ci=0;ci<length;ci++)
		{
			boolean notPresent = true;
			int l = 0;
			int c = chars.charAt(offset+ci);
			int scale = 1;
			if(c=='\t')
			{
				c = ' ';
				int div = (cind%4);
				scale = 4-div;
			}
			for(int uib=0;uib<numBlocks;uib++)
			{
				int st = unicodes[uib*2+0];
				int sz = unicodes[uib*2+1];
				if(c<st)
				{
					break;
				}
				else
				if(c>=(st+sz))
				{
					l += sz;
				}
				else
				{
					l += c-st;
					notPresent = false;
					break;
				}
			}
			if(notPresent)
				continue;
			
			if(l>=numChars)
			{
				System.out.println("Error");
			}
			
			int px, py;
			int pi = pixelTopLeftDims[l*3+0];
			for(int j=0;j<pixelTopLeftDims[l*3+2];j++)
			{
				px = x+leftTopAdvance[l*3+0]+totalAdvance;
				py = y+j+leftTopAdvance[l*3+1];
				for(int i=0;i<pixelTopLeftDims[l*3+1];i++)
				{
					if((px>=minx)&&(px<maxx)&&(py>=miny)&&(py<maxy)
							&&((pixels[pi]&0xFF000000)!=0xFF000000))
					{
						int a = (pixels[pi]&0xFF000000)>>>24;
						int r = (colour&0xFF0000)>>16;
						int g = (colour&0xFF00)>>8;
						int b = (colour&0xFF);
						int ia = 0x100-a;
			
						int ind = px+py*rb.width;
						int pr = (rb.pixel[ind]&0xFF0000)>>16;
						int pg = (rb.pixel[ind]&0xFF00)>>8;
						int pb = (rb.pixel[ind]&0xFF);
	
						r = (r*ia+pr*a)>>8;
						g = (g*ia+pg*a)>>8;
						b = (b*ia+pb*a)>>8;
						rb.pixel[ind] = (r<<16)|(g<<8)|b;
					}
					pi++;
					px++;
				}
			}
			totalAdvance += leftTopAdvance[l*3+2]*scale;
			cind += scale;
		}
		return totalAdvance;
	}
	/*
	void loadTest()
	{
		byte[] compressedData = new byte[1024*1024];
		
		for(int c=minChar;c<=maxChar;c++)
		{
			int offset = 0;
			int sizeToRead = 0;
			try{
				File base = new File("c:/"+c+".gly");
				java.net.URL file = base.toURL();//new java.net.URL(filename);
				DataInputStream fis = new DataInputStream(file.openStream());
				try{
					fis.readFully(compressedData);
				}
				catch(EOFException e)
				{
					
				}
		        fis.close();	
		        
		        int ind = 0;
		        int width = ByteBufferReaderWriter.readintrev(compressedData,ind);
		        ind+=4;
		        int height = ByteBufferReaderWriter.readintrev(compressedData,ind);
		        ind+=4;
		        testChar[c-minChar] = new RenderBuffer(width,height);
		        int i=0;
		        for(int y=0;y<height;y++)
		        {
			        for(int x=0;x<width;x++)
			        {
			        	int v = compressedData[ind+i]&0xFF;
			        	testChar[c-minChar].pixel[i] = ((v)<<16)|((v)<<8)|((v));
			        	i++;
			        }
		        }
		        ind += i;
		        left[c-minChar] = ByteBufferReaderWriter.readintrev(compressedData,ind);
		        ind+=4;
		        top[c-minChar] = -ByteBufferReaderWriter.readintrev(compressedData,ind);
		        ind+=4;
		        advance[c-minChar] = ByteBufferReaderWriter.readintrev(compressedData,ind);
		        ind+=4;
	        	System.out.println("width:"+width+" height:"+height+" left:"+left[c-minChar]+" top:"+top[c-minChar]+" advance:"+advance[c-minChar]);
		}
		catch (Exception e){System.err.println(e+"");}	
		}
		
	}
	*/
}

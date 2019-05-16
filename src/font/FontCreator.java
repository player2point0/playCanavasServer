package font;

import importexport.TextImportExport;
import importexport.font.FontImportExport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import os.RunExe;
import rendering.RenderBuffer;
import sparsedatabase.SparseDatabase;

public class FontCreator 
{
	public static void main(String[] list) throws Exception 
	{
		//convertFonts();
		//convertFontsToVectorFont();
	}

	
	public static void convertFontsToVectorFont()
	{
		File fontdir = new File("/Users/johnbustard/Documents/workspace_old/Projects");
		File[] children = fontdir.listFiles();
		for(int i=0;i<children.length;i++)
		{
			String path = children[i].getName();
			if(path.endsWith(".ttf.svg"))
			{
				String fname = path.substring(0,path.length()-4)+".vf";
				File fe = new File(fname);
				if(children[i].length()>20*1024*1024)
					continue;
				if(!fe.exists())
				{
					try
					{
						VectorFont vf = new VectorFont();
						FontImportExport.parseSVGFont(children[i].getAbsolutePath(), vf);
						vf.toFile(fname);
					}
					catch(Exception e)
					{
						
					}
				}
								
				String fbasename = path.substring(0,path.length()-4)+".vf";
				fname = path.substring(0,path.length()-4)+".reduced.vf";
				fe = new File(fname);
				if(!fe.exists())
				{
					try
					{
						VectorFont vf = new VectorFont();
						vf.fromFile(fbasename);
						
						VectorFont vfr = new VectorFont();
						
					    char[] chars = new char[] {
				            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
				            '0','1','2','3','4','5','6','7','8','9',
				            '.','/',//'_',
				            //',','"','!','?',
				            //'(',')','{','}','<','>','[',']',
				            //':',';','%','£','&','@'
					    };
						
						if(fname.contains("dings"))
						{
			                chars = new char[] {
			                        0xd4,//arrow left
			                        0x30,//directory closed
			                        0x32,//file corner turned
			                        0x33,//file
			                        0x34,//files
			                        0x31,//directory open
			                };
						}
					    
						vfr.createSubset(vf, chars);
						
						vfr.toFile(fname);
					}
					catch(Exception e)
					{
						
					}
				}
			}
		}
	}
	
	public static void convertFonts()
	{
		File fontdir = new File("/Library/Fonts/");
		File[] children = fontdir.listFiles();
		for(int i=0;i<children.length;i++)
		{
			String path = children[i].getAbsolutePath();
			String command = "Open(\""+path+"\")\n";
			command += "Generate(\""+children[i].getName()+".svg\")";
			TextImportExport.saveTextFile("command.pe", command);
			String[] cmds = new String[]{"/usr/local/bin/fontforge", "-script", "command.pe"}; 
			RunExe.run(cmds);
		}
	}
	
	public static void createFont(Font font,InputStream is,int fontSize)
	{
		try
		{
			java.awt.Font theFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
			int[] unicodeBlocks = new int[2];
			unicodeBlocks[0] = 0;
			unicodeBlocks[1] = 0x100;
			createFont(font, theFont, fontSize, unicodeBlocks, 1);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	public static void createFont(Font font,String fontName,int fontSize)
	{
		int[] unicodeBlocks = new int[2];
		unicodeBlocks[0] = 0;
		unicodeBlocks[1] = 0x100;
		java.awt.Font theFont = new java.awt.Font(fontName,java.awt.Font.PLAIN,fontSize);
		createFont(font, theFont, fontSize, unicodeBlocks, 1);
	}

	public static void createFont(Font font,String fontName,int fontSize,int[] unicodeBlocks,int numBlocks)
	{
		java.awt.Font theFont = new java.awt.Font(fontName,java.awt.Font.PLAIN,fontSize);
		createFont(font, theFont, fontSize, unicodeBlocks, numBlocks);
	}
	
	public static void createFont(Font font,java.awt.Font theFont,int fontSize,int[] unicodeBlocks,int numBlocks)
	{
		font.totalAboveLine = (fontSize*7)/10;
		font.totalBelowLine = fontSize/4;
		
		String chars = "";
		for(int i=0;i<numBlocks;i++)
		{
			int st = unicodeBlocks[2*i+0];
			int sz = unicodeBlocks[2*i+1];
			for(int j=0;j<sz;j++)
			{
				chars += (char)(st+j);
			}			
		}
		BufferedImage fontImg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1, Transparency.OPAQUE);
		Graphics2D gfx = (Graphics2D)fontImg.getGraphics();

		gfx.setFont(theFont);
        int picXOffset = 5;
		int picYOffset = 3;
		int picWidth = gfx.getFontMetrics().stringWidth(chars)+picXOffset+(chars.length()*picXOffset);
		int picHeight = gfx.getFontMetrics().getHeight()+picYOffset*2;
		
		fontImg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(picWidth, picHeight, Transparency.BITMASK);
		gfx = (Graphics2D)fontImg.getGraphics();
        gfx.setFont(theFont);

        font.type = Font.COLOUR;
        
		gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		gfx.setColor(new Color(0xFFFFFF));

		gfx.fillRect(0,0,picWidth,picHeight);

		//BufferedImage theFontImage = renderBitmapFontSpecific(gfx,fontImg,renderSpecs,theCharacters);
		int previousWidth = picXOffset;

		float delta = 3.0f;
		
		font.numChars = chars.length();
		font.unicodes = new int[numBlocks*2];
		System.arraycopy(unicodeBlocks, 0, font.unicodes, 0, numBlocks*2);
		font.numBlocks  = numBlocks;
		font.leftTopAdvance = new int[font.numChars*3];
		font.pixelTopLeftDims = new int[font.numChars*3];
		int curPixel = 0;
		for(int index=0;index<chars.length();index++){
			Rectangle2D pos = gfx.getFontMetrics().getStringBounds(chars.substring(index,index+1),gfx);
			gfx.setColor(new Color(0x0));
			gfx.drawString(chars.substring(index,index+1),previousWidth-((int)(pos.getX()+0.5f)),picYOffset+((int)(pos.getY()+0.5f))*-1);

			previousWidth += (int)(pos.getWidth()+0.5f)+picXOffset;
			curPixel += ((int)(pos.getWidth()+0.5f))*((int)(pos.getHeight()+0.5f+delta));
		}
		font.pixels = new int[curPixel];
		
		int[] pixels = new int[picWidth*picHeight];
		PixelGrabber pG = new PixelGrabber(fontImg,0,0,picWidth,picHeight,pixels,0,picWidth);
		try{
			pG.grabPixels();
		}catch(Exception ex){System.out.println(ex);}

		RenderBuffer test = new RenderBuffer();
		test.pixel = pixels;
		test.width = picWidth;
		test.height = picHeight;
		
		//ImageLoader.saveBuffer(test, "c:/testfont.png");
		
		curPixel = 0;
		previousWidth = picXOffset;
		
//Extract the pixels
		for(int index=0;index<chars.length();index++){
			Rectangle2D pos = gfx.getFontMetrics().getStringBounds(chars.substring(index,index+1),gfx);
			font.leftTopAdvance[index*3+0] = ((int)(pos.getX()+0.5f));
			font.leftTopAdvance[index*3+1] = ((int)(pos.getY()+0.5f));
			font.leftTopAdvance[index*3+2] = gfx.getFontMetrics().charWidth(chars.charAt(index));
			font.pixelTopLeftDims[index*3+0] = curPixel;
			font.pixelTopLeftDims[index*3+1] = (int)(pos.getWidth()+0.5f);
			font.pixelTopLeftDims[index*3+2] = (int)(pos.getHeight()+0.5f+delta);


			for(int j=0;j<font.pixelTopLeftDims[index*3+2];j++)
			{
				for(int i=0;i<font.pixelTopLeftDims[index*3+1];i++)
				{
					int p = pixels[previousWidth+i+j*picWidth]&0xFFFFFF;
					if(p==0xFFFFFF)
						p=0xFF000000;
					else
						p=(p&0xFF)<<24;
					font.pixels[curPixel+i+j*font.pixelTopLeftDims[index*3+1]] = p;
				}					
			}
			previousWidth += (int)(pos.getWidth()+0.5f)+picXOffset;
			curPixel += font.pixelTopLeftDims[index*3+1]*font.pixelTopLeftDims[index*3+2];
		}

		gfx.dispose();
		return;

	}


}

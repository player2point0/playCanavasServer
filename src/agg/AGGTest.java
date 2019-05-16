package agg;

import mathematics.GeneralMatrixFloat;
import mathematics.curvefitting.Polynomial;
import rendering.RenderBuffer;
import importexport.font.FontImportExport;
import ui.BasicApplet;
import font.VectorFont;

public class AGGTest extends BasicApplet  
{
	long oldtime;
	
//	int startx = 10<<8;
//	int starty = 10<<8;
	
	RenderBuffer image = new RenderBuffer(256,256);
	
	int polyorder = 4;
	GeneralMatrixFloat thicknessPoly = new GeneralMatrixFloat(polyorder,2);
	GeneralMatrixFloat polynomial = new GeneralMatrixFloat(polyorder,2);
	GeneralMatrixFloat gradient = new GeneralMatrixFloat(polyorder-1,2);
	
	GeneralMatrixFloat thicknesses = new GeneralMatrixFloat(1,4);
	GeneralMatrixFloat points = new GeneralMatrixFloat(2,4);
	
	boolean dragging = false;
	int selectedpoint = 0;
	
	VectorFont font = new VectorFont();
	
	int ci = 0;
	
	boolean fixvolume = false;
	
	public static void main(String[] list) throws Exception 
    {
		AGGTest app = new AGGTest();
		BasicApplet.runAsMain(app);
    }	

    public void init()
    {
    	super.init();
    	
		loading = true;
				
		//FontImportExport.load("/media/Drobo_/GraphicDesign/Fonts/80 Fonts - World of Darkness Fonts/Ach4.ttf", font);
		//FontImportExport.load("/media/Drobo_/GraphicDesign/Fonts/80 Fonts - World of Darkness Fonts/abaddon.TTF", font);
		//rasteriser.setGamma(2.0f);
		rasteriser.setGamma(1.8f);
		//rasteriser.setGamma(1.5f);
		
		points.value[0*2+0] = 0.3f;			points.value[0*2+1] = 0.2f;
		points.value[1*2+0] = 0.45f;	points.value[1*2+1] = 0.8f;
		points.value[2*2+0] = 0.55f;	points.value[2*2+1] = 0.8f;
		points.value[3*2+0] = 0.7f;			points.value[3*2+1] = 0.2f;

		thicknesses.value[0] = 0.0f;
		thicknesses.value[1] = 0.01f;
		thicknesses.value[2] = 0.01f;
		thicknesses.value[3] = 0.05f;
		
		Polynomial.fit(thicknesses, thicknessPoly);
		Polynomial.fit(points, polynomial);
	
		Polynomial.differentiate(polynomial, gradient);
		
		oldtime = System.currentTimeMillis();
		loading = false;
    }
    
//    double m_x[3];
//    double m_y[3];
//    double m_dx;
//    double m_dy;
//    int    m_idx;
//    agg::rasterizer_scanline_aa<> m_ras;
//    agg::scanline_p8              m_sl_p8;
//    agg::scanline_bin             m_sl_bin;

    Clipper clipper = new Clipper();
    Rasteriser rasteriser = new Rasteriser();
    
    boolean test = false;
    
	public void frameUpdate()
	{
		if(loading)
			return;
		
		windowEvents.startOfFrame();
		input.startOfFrame();
		
		//The frame or applet can have its dimensions altered
		//In which case the buffer storing the appearance of the screen needs updating
		if(windowEvents.sizeChanged)
		{
			display.resize(windowEvents.width, windowEvents.height);
			windowEvents.sizeChanged = false;
		}		

		if(input.letterReleased('f'))
		{
			fixvolume = !fixvolume;
			System.out.println("fixvolume="+fixvolume);
		}
		
		long time = System.currentTimeMillis();
		float secs = (time-oldtime)/1000.0f;
		oldtime = time;
		
		//rasteriser.printout = true;
		rb.clear(0xFFFFFF);
		
		rasteriser.reset();
		
		clipper.m_clipping = true;
		clipper.maxx = rb.width<<8;
		clipper.maxy = rb.height<<8;
		
//		int mx = 1000;
//		int my = 1000;
//		int mx = input.mouseX;
//		int my = input.mouseY;
//		if(mx<10)
//			mx = 10;
//		if(mx>=(rb.width-10))
//			mx=rb.width-11;
//		if(my<10)
//			my = 10;
//		if(my>=(rb.height-10))
//			my=rb.height-11;

//		mx/=10;
//		my/=10;
//		mx = 100;
//		my = 100;
		
		/*
		if(input.letterReleased('t'))
		{
			test = !test;
			System.out.println("test = "+test);
		}
		if(input.letterReleased('c'))
		{
			ci= ci+1;
			if(ci>=(font.glyphs.length/2))
				ci = 0;
			System.out.println("ci = "+ci);
		}
		
		int cs = font.glyphs[ci*2+0];
		int ce = cs+font.glyphs[ci*2+1];
		
		for(int ci=cs;ci<ce;ci++)
		{
			int es = font.glyphContours[ci*2+0];
			int ee = es+font.glyphContours[ci*2+1];
			int ps = font.glyphEdges[es*2+0];
			clipper.move_to(font.points[ps*2+0]*mx+(10<<8), font.points[ps*2+1]*my+(10<<8));
			for(int ei=es;ei<ee;ei++)
			{
				ps = font.glyphEdges[ei*2+0];
				int pn = font.glyphEdges[ei*2+1];

				clipper.move_to(font.points[ps*2+0]*mx+(10<<8), font.points[ps*2+1]*my+(10<<8));
				//
				if((pn==3))
				{
					int x2 = font.points[(ps+1)*2+0];
					int y2 = font.points[(ps+1)*2+1];
					int x3 = font.points[(ps+2)*2+0];
					int y3 = font.points[(ps+2)*2+1];
					clipper.curve_to(rasteriser, x2*mx+(10<<8), y2*my+(10<<8), x3*mx+(10<<8), y3*my+(10<<8));
				}
				else
				if((pn==4))
				{
					//if()
					{
						int x2 = font.points[(ps+1)*2+0];
						int y2 = font.points[(ps+1)*2+1];
						int x3 = font.points[(ps+2)*2+0];
						int y3 = font.points[(ps+2)*2+1];
						int x4 = font.points[(ps+3)*2+0];
						int y4 = font.points[(ps+3)*2+1];

//						if(test)
//							clipper.curve_tod(rasteriser, x2*mx+(10<<8), y2*my+(10<<8), x3*mx+(10<<8), y3*my+(10<<8), x4*mx+(10<<8), y4*my+(10<<8));
//						else
							clipper.curve_to(rasteriser, x2*mx+(10<<8), y2*my+(10<<8), x3*mx+(10<<8), y3*my+(10<<8), x4*mx+(10<<8), y4*my+(10<<8));
					}
				}
				else
				{
					int pe = ps+pn;
					for(int pi=(ps+1);pi<pe;pi++)
					{
	//					int x0 = font.points[(pi-1)*2+0];
	//					int y0 = font.points[(pi-1)*2+1];
						int x2 = font.points[(pi)*2+0];
						int y2 = font.points[(pi)*2+1];
						
						clipper.line_to(rasteriser, x2*mx+(10<<8), y2*my+(10<<8));
						
						//rasteriser.line(x0*10+(10<<8), y0*10+(10<<8), x1*10+(10<<8), y1*10+(10<<8));
					}
				}
			}
		}
		*/
//		mx = 12;
//		my = 23;
		
//		System.out.println("start");
		
//		int[] m_x = new int[3];
//		int[] m_y = new int[3];
//	    m_x[0] = 100 + 120; m_y[0] = 60;
//	    //m_x[1] = 191; m_y[1] = 415;
//	    m_x[1] = mx; m_y[1] = my;
//	    //m_x[1] = 369 + 120; m_y[1] = 170;
//	    m_x[2] = 143 + 120; m_y[2] = 310;

	    //System.out.println("mx:"+mx+" my:"+my);
//		rasteriser.line(m_x[0]<<8, m_y[0]<<8, m_x[1]<<8, m_y[1]<<8);
//		rasteriser.line(m_x[1]<<8, m_y[1]<<8, m_x[2]<<8, m_y[2]<<8);
//		rasteriser.line(m_x[2]<<8, m_y[2]<<8, m_x[0]<<8, m_y[0]<<8);
//
//		rasteriser.line((m_x[0]+10)<<8, (m_y[0]+10)<<8, (m_x[2]-10)<<8, (m_y[2]-10)<<8);
//		rasteriser.line((m_x[2]-10)<<8, (m_y[2]-10)<<8, (m_x[1]+10)<<8, (m_y[1]-10)<<8);
//		rasteriser.line((m_x[1]+10)<<8, (m_y[1]-10)<<8, (m_x[0]+10)<<8, (m_y[0]+10)<<8);

	    //RenderBuffer image = rb;

		image.clear(0xFFFFFF);
//		int indent = image.width/10;
//		
//		int hw = (image.width)/2;
//		int hh = (image.height)/2;
//		
//		clipper.move_to((indent<<8), (hh<<8));
//		clipper.curve_to(rasteriser, (hw<<8), (indent<<8), ((image.width-indent)<<8), (hh<<8));
//		clipper.curve_to(rasteriser, (hw<<8), ((image.height-indent)<<8), ((indent)<<8), (hh<<8));
	    
		float thickness = 0.1f;
		
		float scale = image.width;
		float sx = polynomial.value[0];
		float sy = polynomial.value[0+polynomial.width];
		int numsegs = 100;
		
//		float pthickness = 0.0f;
//		float px=0.0f;
//		float py=0.0f;
//		float pfx=0.0f;
//		float pfy=0.0f;
		
		for(int i=0;i<numsegs;i++)
		{
			float f = i/(float)(numsegs-1);
			
			float fx = Polynomial.evalDim(polynomial, 0, f);
			float fy = Polynomial.evalDim(polynomial, 1, f);//+0.1f;
			
//			float dx = fx-pfx;
//			float dy = fy-pfy;
//			
//			float d = (float)Math.sqrt(dx*dx+dy*dy);
			
			float dfx = Polynomial.evalDim(gradient, 0, f);
			float dfy = Polynomial.evalDim(gradient, 1, f);
			float df = (float)Math.sqrt(dfx*dfx+dfy*dfy);
			
			thickness = Polynomial.evalDim(thicknessPoly, 0, f);
			
			if(i==0)
			{
//				pfx = fx;
//				pfy = fy;
				fx += thickness*-dfy/df;
				fy += thickness*dfx/df;
				clipper.move_to((fx*scale), (fy*scale));
				sx = fx;
				sy = fy;
//				px = sx;
//				py = sy;
			}
			else
			{
//				pfx = fx;
//				pfy = fy;
				fx += thickness*-dfy/df;
				fy += thickness*dfx/df;
				clipper.line_to(rasteriser, (fx*scale), (fy*scale));
				//min 
//				float kEdge0x = pfx-fx;
//				float kEdge0y = pfy-fy;
//				pfx = fx;
//				pfy = fy;
//				float kEdge1x = px-fx;
//				float kEdge1y = py-fy;
//
//				float bz1 = kEdge1x*kEdge0y-kEdge0x*kEdge1y;				
//				float asqr1 = (bz1*bz1)*0.25f;
//				float t1 = (float)Math.sqrt(asqr1);
//				
//				float nfx = fx+thickness*-dfy/df;
//				float nfy = fy+thickness*dfx/df;
//
//				kEdge1x = kEdge0x;
//				kEdge1y = kEdge0y;
//				
//				kEdge0x = nfx-fx;
//				kEdge0y = nfy-fy;
//
//				float bz2 = kEdge1x*kEdge0y-kEdge0x*kEdge1y;				
//				float asqr2 = (bz2*bz2)*0.25f;
//				float t2 = (float)Math.sqrt(asqr2);
//				
//				float totala = d*thickness;
//				float rema = totala-t1;
//				
//				float nscale = rema/t2;
//				
//				if(fixvolume)
//				{
//					nfx = fx+nscale*thickness*-dfy/df;
//					nfy = fy+nscale*thickness*dfx/df;
//				}
//				clipper.line_to(rasteriser, nfx*scale, nfy*scale);
//				px = nfx;
//				py = nfy;
			}
		}
		for(int i=numsegs-1;i>=0;i--)
		{
			float f = i/(float)(numsegs-1);
			
			float fx = Polynomial.evalDim(polynomial, 0, f);
			float fy = Polynomial.evalDim(polynomial, 1, f);
			
//			float dx = fx-sx;
//			float dy = fy-sy;
//			
//			float d = (float)Math.sqrt(dx*dx+dy*dy);

			float dfx = Polynomial.evalDim(gradient, 0, f);
			float dfy = Polynomial.evalDim(gradient, 1, f);
			float df = (float)Math.sqrt(dfx*dfx+dfy*dfy);
			
			thickness = Polynomial.evalDim(thicknessPoly, 0, f);

			if(i==numsegs)
			{
				fx -= thickness*-dfy/df;
				fy -= thickness*dfx/df;
//				px = fx;
//				py = fy;
				clipper.line_to(rasteriser, fx*scale, fy*scale);
			}
			else
			{
				fx -= thickness*-dfy/df;
				fy -= thickness*dfx/df;
				clipper.line_to(rasteriser, fx*scale, fy*scale);
			}
		}
		clipper.line_to(rasteriser, sx*scale, sy*scale);
		
		//rasteriser.line((startx+(10<<8)), starty, (mx+10)<<8, (my)<<8);
		rasteriser.sort_cells();
		rasteriser.scan2(image, 0x0);
		
		rb.copy(image);
	    //image.resize(ow, oh);
		/*
		cs = font.glyphs[ci*2+0];
		ce = cs+font.glyphs[ci*2+1];
		
		for(int ci=cs;ci<ce;ci++)
		{
			int es = font.glyphContours[ci*2+0];
			int ee = es+font.glyphContours[ci*2+1];
			int ps = font.glyphEdges[es*2+0];
			rb.clear(0xFF0000, (font.points[ps*2+0]*mx+(8<<8))>>8, (font.points[ps*2+1]*my+(8<<8))>>8, 4, 4);
			for(int ei=es;ei<ee;ei++)
			{
				ps = font.glyphEdges[ei*2+0];
				int pn = font.glyphEdges[ei*2+1];

				//clipper.move_to(font.points[ps*2+0]*mx+(10<<8), font.points[ps*2+1]*my+(10<<8));
				//
				if((pn==3))
				{
					int x2 = font.points[(ps+1)*2+0];
					int y2 = font.points[(ps+1)*2+1];
					int x3 = font.points[(ps+2)*2+0];
					int y3 = font.points[(ps+2)*2+1];
					rb.clear(0xFFFF00, (x2*mx+(8<<8))>>8, (y2*my+(8<<8))>>8, 4, 4);
					rb.clear(0xFF0000, (x3*mx+(8<<8))>>8, (y3*my+(8<<8))>>8, 4, 4);
				}
				else
				if((pn==4))
				{
					//if()
					{
						int x2 = font.points[(ps+1)*2+0];
						int y2 = font.points[(ps+1)*2+1];
						int x3 = font.points[(ps+2)*2+0];
						int y3 = font.points[(ps+2)*2+1];
						int x4 = font.points[(ps+3)*2+0];
						int y4 = font.points[(ps+3)*2+1];

						rb.clear(0xFFFF00, (x2*mx+(8<<8))>>8, (y2*my+(8<<8))>>8, 4, 4);
						rb.clear(0xFFFF00, (x3*mx+(8<<8))>>8, (y3*my+(8<<8))>>8, 4, 4);
						rb.clear(0xFF0000, (x4*mx+(8<<8))>>8, (y4*my+(8<<8))>>8, 4, 4);
					}
				}
				else
				{
					int pe = ps+pn;
					for(int pi=(ps+1);pi<pe;pi++)
					{
						int x2 = font.points[(pi)*2+0];
						int y2 = font.points[(pi)*2+1];
						
						rb.clear(0xFF0000, (x2*mx+(8<<8))>>8, (y2*my+(8<<8))>>8, 4, 4);
					}
				}
			}
		}
		*/
		
		//System.out.println("doh");
//		for(int y=(rb.height-1);y>=0;y--)
//		{
//			for(int x=(rb.width-1);x>=0;x--)
//			{
//				int sx = x/10;
//				int sy = y/10;
//				int sind = sy*rb.width+sx;
//				int dind = y*rb.width+x;
//				rb.pixel[dind] = rb.pixel[sind];
//			}
//		}
//	    typedef agg::renderer_base<pixfmt> renderer_base;
//	    typedef agg::renderer_scanline_aa_solid<renderer_base> renderer_aa;
//	    typedef agg::renderer_scanline_bin_solid<renderer_base> renderer_bin;

//        pixfmt pixf(rbuf_window());
//        renderer_base rb(pixf);
//        renderer_aa ren_aa(rb);
//
//        rb.clear(agg::rgba(1, 1, 1));

//        agg::path_storage path;
//
//        path.move_to(m_x[0], m_y[0]);
//        path.line_to(m_x[1], m_y[1]);
//        path.line_to(m_x[2], m_y[2]);
//        path.close_polygon();
//
//        ren_aa.color(agg::rgba(0.7, 0.5, 0.1, m_alpha.value()));
//
//        m_ras.gamma(agg::gamma_power(m_gamma.value() * 2.0));
//        m_ras.add_path(path);
//        agg::render_scanlines(m_ras, m_sl_p8, ren_aa);

		input.endOfFrame();		
		windowEvents.endOfFrame();
	}

}

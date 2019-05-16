package agg;

import rendering.RenderBuffer;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;

public class Rasteriser 
{
	public boolean noAlphaAgainstBackground = false;
	public static boolean printout = false;
	
    static final int base_shift = 8;
    static final int base_scale = 1 << base_shift;
    static final int base_mask  = base_scale - 1;

	static final int CELL_X = 0;
	static final int CELL_Y = 1;
	static final int CELL_COVER = 2;
	static final int CELL_AREA = 3;

	static final int aa_shift=(8);
	static final int aa_scale=(1 << aa_shift);
	static final int aa_mask=(aa_scale - 1);
	static final int aa_scale2=(aa_scale * 2);
	static final int aa_mask2=(aa_scale2 - 1);

//	static final int cell_block_pool = 256;
	static final int poly_subpixel_shift = 8;                      //----poly_subpixel_shift
	static final int poly_subpixel_scale = 1<<poly_subpixel_shift; //----poly_subpixel_scale 
	static final int poly_subpixel_mask  = poly_subpixel_scale-1;  //----poly_subpixel_mask 
	
	static final int dx_limit = 16384 << poly_subpixel_shift;
	
	private GeneralMatrixInt m_cells = new GeneralMatrixInt(4,1024);
	//private Cell[] m_cells;
	private int m_curr_cell_ptr;

	private GeneralMatrixInt m_cell_yhist = new GeneralMatrixInt(1);
	private GeneralMatrixInt m_cell_yind = new GeneralMatrixInt(1);
	private GeneralMatrixInt m_sorted_cells = new GeneralMatrixInt(1);

//	static final int max_width = 1024<<3;
//	int[] xs = new int[max_width<<2];
	
	static final int fill_non_zero = 0;
	static final int fill_even_odd = 1;
	
	public int m_filling_rule = fill_even_odd;//fill_non_zero;
	private int m_min_x;
	private int m_min_y;
	private int m_max_x;
	private int m_max_y;
	
	private int[] m_gamma = new int[aa_scale];
	
	private float[] tempypowers = new float[2048*32];
	
	private boolean m_sorted;
	
	public Rasteriser()
	{
		setGamma(2.0f);
		
		for(int y=0;y<2048;y++)
		{
	    	float fy = y*0.001f;
	
			tempypowers[y] = 1.0f;
			for(int i=1;i<16;i++)
			{
				tempypowers[y+i*2048] = tempypowers[y+(i-1)*2048]*fy; 
			}
		}
	}
	public Rasteriser(float gamma)
	{
		setGamma(gamma);
	}
	
	public void setGamma(float g)
	{
        for(int i = 0; i < aa_scale; i++)
        {
            m_gamma[i] = (int)(Math.pow(((double)i) / aa_mask,g) * aa_mask);
        }
	}
	
    public void reset()
    {
        m_cells.height = 0;
        m_cells.value[CELL_X] = 0;
        m_cells.value[CELL_Y] = 0;
        m_cells.value[CELL_COVER] = 0;
        m_cells.value[CELL_AREA] = 0;
        m_sorted = false;
        m_min_x =  0x7FFFFFFF;
        m_min_y =  0x7FFFFFFF;
        m_max_x = -0x7FFFFFFF;
        m_max_y = -0x7FFFFFFF;
    }

	public final int calculate_alpha(int area)
	{
		int cover = area >> (poly_subpixel_shift *2 + 1 - aa_shift);

		if(cover < 0)
			cover = -cover;
		if(m_filling_rule == fill_even_odd)
		{
			cover &= aa_mask2;
			if(cover > aa_scale)
			{
				cover = aa_scale2 - cover;
			}
		}
		if(cover > aa_mask)
			cover = aa_mask;
		return m_gamma[cover];
	}

	public final int calculate_alpha(int area,int colouralpha)
	{
		int cover = area >> (poly_subpixel_shift *2 + 1 - aa_shift);

		if(cover < 0)
			cover = -cover;
		if(m_filling_rule == fill_even_odd)
		{
			cover &= aa_mask2;
			if(cover > aa_scale)
			{
				cover = aa_scale2 - cover;
			}
		}
		
		cover = (cover*colouralpha)>>8;
		if(cover > aa_mask)
			cover = aa_mask;
		return m_gamma[cover];
	}

	int m_last_x;
	int m_cover_ptr;
	int m_cur_span;
	GeneralMatrixInt m_spans = new GeneralMatrixInt(4,1024);
	GeneralMatrixInt m_covers = new GeneralMatrixInt(1,1024);
	static final int SPAN_LEN = 0;
	static final int SPAN_COVERS = 1;
	static final int SPAN_X = 2;
	
    void reset_spans()
    {
        m_last_x    = 0x7FFFFFF0;
        m_cover_ptr = 0;
        m_cur_span  = 0;
        m_spans.value[m_cur_span<<2+SPAN_LEN] = 0;
    }

    void add_cell(int x, int cover)
    {
    	m_covers.value[m_cover_ptr] = cover;
        //if(x == m_last_x+1 && m_cur_span->len > 0)

    	if(
        	(x == (m_last_x+1)) && 
            (m_spans.value[(m_cur_span<<2)+SPAN_LEN] > 0)
        )        	
        {
            //m_cur_span->len++;
        	m_spans.value[(m_cur_span<<2)+SPAN_LEN]++;
        }
        else
        {
        	if(m_last_x!=0x7FFFFFF0)
        		m_cur_span++;
            m_spans.value[(m_cur_span<<2)+SPAN_COVERS] = m_cover_ptr;
            m_spans.value[(m_cur_span<<2)+SPAN_X]      = x;
            m_spans.value[(m_cur_span<<2)+SPAN_LEN]    = 1;
        }
        m_last_x = x;
        m_cover_ptr++;
    }

    void add_span(int x, int len, int cover)
    {
    	if(len==0)
    	{
    		System.out.println("???");
    		return;
    	}
        if(
        	(x == (m_last_x+1)) && 
            (m_spans.value[(m_cur_span<<2)+SPAN_LEN] < 0) && 
            (cover == m_covers.value[m_spans.value[(m_cur_span<<2)+SPAN_COVERS]])
          )
        {
        	m_spans.value[(m_cur_span<<2)+SPAN_LEN] -= len;
        }
        else
        {
        	m_covers.value[m_cover_ptr] = cover;
            //*m_cover_ptr = (cover_type)cover;
            m_cur_span++;
            m_spans.value[(m_cur_span<<2)+SPAN_COVERS] = m_cover_ptr++;
            m_spans.value[(m_cur_span<<2)+SPAN_X]      = x;
            m_spans.value[(m_cur_span<<2)+SPAN_LEN]    = -len;
        }
        m_last_x = x + len - 1;
    }

    void blend_hline(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            RenderBuffer rb)
    {
    	if(len<=0)
    		return;
    	
        //if (c.a)
        {
			int p = x+y*rb.width;

			 int alpha = (0xFF * (covers[ci] + 1)) >> 8;
			 //int alpha = ((covers[ci]));// + 1);// >> 8;
            if(alpha == base_mask)
            {
                do
                {
                	if(p>=0)
                		rb.pixel[p%rb.pixel.length] = c;
                    p++;
                }
                while((--len)!=0);
            }
            else
            {
                do
                {
                	if(p<0)
                	{
                		System.out.println("err");
                		break;
                	}
                	int pind = p%rb.pixel.length;
       		 		int pc = rb.pixel[pind];
       		    	int pcr = (pc&0xFF0000)>>16;
       		    	int pcg = (pc&0x00FF00)>>8;
       		    	int pcb = (pc&0x0000FF);
       		    	
					pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;
       		    	int pca = alpha+(pc&0xFF000000)>>24;
    				if(pca>0xFF)
    					pca = 0xFF;

					if(printout)
	       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + ","+ covers[ci] + ")");
       		 		rb.pixel[pind] = (pca<<24)|(pcr<<16)|(pcg<<8)|(pcb);
                    //m_blender.blend_pix(p, c.r, c.g, c.b, alpha, cover);
                    p++;
                }
                while((--len)!=0);
            }
        }
    }
    

    void blend_hline(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            RenderBuffer rb, int id, RenderBuffer idBuffer)
    {
    	if(len<=0)
    		return;
    	
        //if (c.a)
        {
			int p = x+y*rb.width;
			if((x>=rb.width)||(y>=rb.height)||(p<0))
				return;
			
			 int alpha = (0xFF * (covers[ci] + 1)) >> 8;
			 //int alpha = ((covers[ci]));// + 1);// >> 8;
            if(alpha == base_mask)
            {
                do
                {
                	rb.pixel[p] = c;
                	idBuffer.pixel[p] = id;
                    p++;
                }
                while((--len)!=0);
            }
            else
            {
                do
                {
       		 		int pc = rb.pixel[p];
       		    	int pcr = (pc&0xFF0000)>>16;
       		    	int pcg = (pc&0x00FF00)>>8;
       		    	int pcb = (pc&0x0000FF);
       		    	
					pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

					if(printout)
	       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + ","+ covers[ci] + ")");
       		 		rb.pixel[p] = (pcr<<16)|(pcg<<8)|(pcb);
                	idBuffer.pixel[p] = id;
                    //m_blender.blend_pix(p, c.r, c.g, c.b, alpha, cover);
                    p++;
                }
                while((--len)!=0);
            }
        }
    }
    
    void blend_hline(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            int rbw, int rboff, int[] rbp)
    {
    	if(len<=0)
    		return;
    	
        //if (c.a)
        {
			int p = rboff+x+y*rbw;

			 int alpha = (0xFF * (covers[ci] + 1)) >> 8;
			 //int alpha = ((covers[ci]));// + 1);// >> 8;
            if(alpha == base_mask)
            {
                do
                {
                	rbp[p] = c;
                    p++;
                }
                while((--len)!=0);
            }
            else
            {
                do
                {
       		 		int pc = rbp[p];
       		    	int pcr = (pc&0xFF0000)>>16;
       		    	int pcg = (pc&0x00FF00)>>8;
       		    	int pcb = (pc&0x0000FF);
       		    	
					pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

					if(printout)
	       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + ","+ covers[ci] + ")");
					rbp[p] = (pcr<<16)|(pcg<<8)|(pcb);
                    //m_blender.blend_pix(p, c.r, c.g, c.b, alpha, cover);
                    p++;
                }
                while((--len)!=0);
            }
        }
    }
    void blend_solid_hspan(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            RenderBuffer rb)
    {
		//if (c.a)
		{
			int p = x+y*rb.width;
			
			
			do 
			{
				if((p<0)||(p>=(rb.width*rb.height)))
					return;

				int alpha = (0xFF * (covers[ci] + 1)) >> 8;

				if(printout)
       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + "," + covers[ci] +")");
       		 		//int alpha = ((covers[ci]));// + 1);// >> 8;
				 if(alpha == base_mask)
				 {
					 rb.pixel[p] = c;
				 }
				 else
				 {
//			            p[Order::R] += (value_type)(((cr - p[Order::R]) * alpha) >> base_shift);
//			            p[Order::G] += (value_type)(((cg - p[Order::G]) * alpha) >> base_shift);
//			            p[Order::B] += (value_type)(((cb - p[Order::B]) * alpha) >> base_shift);
//				     	m_blender.blend_pix(p, c.r, c.g, c.b, alpha, *covers);
	       		 		int pc = rb.pixel[p];
	       		    	int pcr = (pc&0xFF0000)>>16;
	       		    	int pcg = (pc&0x00FF00)>>8;
	       		    	int pcb = (pc&0x0000FF);
	       		    	int pcra = (pc&0xFF000000)>>24;
	       		    	int pca = alpha+pcra;
        				if(pca>0xFF)
        					pca = 0xFF;
        				if((pcra==0)&&(noAlphaAgainstBackground))
        					alpha = 0x100;
						pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
						pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
						pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

	       		 		rb.pixel[p] = (pca<<24)|(pcr<<16)|(pcg<<8)|(pcb);
				 }
				 p++;
				 
					x++;
//				if((x==221)&&(y==237))
//				{
//					System.out.println("test");
//				}
				 
				 ci++;
			}
			while((--len)!=0);
		}
	}

    void blend_solid_hspan(int x, int y,
            int len,
            GeneralMatrixFloat[] colourpolynomial,
            int numdim,
            int[] tempc,
            int[] covers,int ci,
            RenderBuffer rb,
            int id,
            RenderBuffer idBuffer)
    {
    	if(len<=0)
    		return;
    	if((x<0)||(x>=rb.width)||(y<0)||(y>=rb.height))
    		return;
    	
    	int p = x+y*rb.width;

    	do 
		{
			int alpha = (0xFF * (covers[ci] + 1)) >> 8;
    	
    		int oldid = idBuffer.pixel[p];

			//for each colour
			for(int cind=0;cind<3;cind++)
				//for(int cind=0;cind<4;cind++)
			{
				float cv = 0.0f;
				int ind = 0;//cind*numpolynomialterms;
				for(int yp=0;yp<numdim;yp++)
				{
					for(int xp=0;xp<numdim;xp++)
					{
						cv += tempypowers[2048*xp+x]*tempypowers[2048*yp+y]*colourpolynomial[cind].value[ind];
						ind++;
					}
				}
				if(cv<0.0f)
					tempc[cind] = 0;
				else
				if(cv>1.0f)
					tempc[cind] = 0xFF;
				else
					tempc[cind] = (int)(255*cv);
			}

		     //alpha = (alpha*(0x100-tempc[3]))>>8;

			 if((alpha == base_mask)||(oldid==-1))
			 {
				 rb.pixel[p] = (tempc[3]<<24)|(tempc[0]<<16)|(tempc[1]<<8)|(tempc[2]);
				idBuffer.pixel[p] = id;
			 }
			 else
			 {
				 if(alpha>=128)
						idBuffer.pixel[p] = id;

//		            p[Order::R] += (value_type)(((cr - p[Order::R]) * alpha) >> base_shift);
//		            p[Order::G] += (value_type)(((cg - p[Order::G]) * alpha) >> base_shift);
//		            p[Order::B] += (value_type)(((cb - p[Order::B]) * alpha) >> base_shift);
//			     	m_blender.blend_pix(p, c.r, c.g, c.b, alpha, *covers);
      		 		int pc = rb.pixel[p];
      		    	int pcr = (pc&0xFF0000)>>16;
      		    	int pcg = (pc&0x00FF00)>>8;
      		    	int pcb = (pc&0x0000FF);
      		    	
					pcr = ((tempc[0]-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((tempc[1]-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((tempc[2]-pcb)*alpha+(pcb<<8))>>8;

      		 		rb.pixel[p] = (pcr<<16)|(pcg<<8)|(pcb);
			 }

			 x++;
			p++;
		}
		while((--len)!=0);
    }
    
    void blend_solid_hspan(int x, int y,
            int len,
            GeneralMatrixFloat[] colourpolynomial,
            int numdim,
            int[] tempc,
            int[] covers,int ci,
            RenderBuffer rb)
    {
    	if(len<=0)
    		return;

    	int p = x+y*rb.width;

    	do 
		{
			int alpha = (0xFF * (covers[ci] + 1)) >> 8;

			//for each colour
			for(int cind=0;cind<4;cind++)
			{
				float cv = 0.0f;
				int ind = 0;//cind*numpolynomialterms;
				for(int yp=0;yp<numdim;yp++)
				{
					for(int xp=0;xp<numdim;xp++)
					{
						cv += tempypowers[2048*xp+x]*tempypowers[2048*yp+y]*colourpolynomial[cind].value[ind];
						ind++;
					}
				}
				if(cv<0.0f)
					tempc[cind] = 0;
				else
				if(cv>1.0f)
					tempc[cind] = 0xFF;
				else
					tempc[cind] = (int)(255*cv);
			}

		     alpha = (alpha*(0x100-tempc[3]))>>8;

			 if(alpha == base_mask)
			 {
				 rb.pixel[p] = (tempc[3]<<24)|(tempc[0]<<16)|(tempc[1]<<8)|(tempc[2]);
			 }
			 else
			 {
//		            p[Order::R] += (value_type)(((cr - p[Order::R]) * alpha) >> base_shift);
//		            p[Order::G] += (value_type)(((cg - p[Order::G]) * alpha) >> base_shift);
//		            p[Order::B] += (value_type)(((cb - p[Order::B]) * alpha) >> base_shift);
//			     	m_blender.blend_pix(p, c.r, c.g, c.b, alpha, *covers);
      		 		int pc = rb.pixel[p];
      		    	int pcr = (pc&0xFF0000)>>16;
      		    	int pcg = (pc&0x00FF00)>>8;
      		    	int pcb = (pc&0x0000FF);
      		    	
					pcr = ((tempc[0]-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((tempc[1]-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((tempc[2]-pcb)*alpha+(pcb<<8))>>8;

      		 		rb.pixel[p] = (pcr<<16)|(pcg<<8)|(pcb);
			 }

			 x++;
			p++;
		}
		while((--len)!=0);
    }
    
    void blend_solid_hspan(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            RenderBuffer rb,
            int id,
            RenderBuffer idBuffer)
    {
		//if (c.a)
		{
			int p = x+y*rb.width;
			
			if((x>=rb.width)||(y>=rb.height)||(p<0))
				return;
			
			do 
			{
				 int alpha = (0xFF * (covers[ci] + 1)) >> 8;

				if(printout)
       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + "," + covers[ci] +")");
       		 		//int alpha = ((covers[ci]));// + 1);// >> 8;
				 idBuffer.pixel[p] = id;
				 if(alpha == base_mask)
				 {
					 rb.pixel[p] = c;
				 }
				 else
				 {
//			            p[Order::R] += (value_type)(((cr - p[Order::R]) * alpha) >> base_shift);
//			            p[Order::G] += (value_type)(((cg - p[Order::G]) * alpha) >> base_shift);
//			            p[Order::B] += (value_type)(((cb - p[Order::B]) * alpha) >> base_shift);
//				     	m_blender.blend_pix(p, c.r, c.g, c.b, alpha, *covers);
	       		 		int pc = rb.pixel[p];
	       		    	int pcr = (pc&0xFF0000)>>16;
	       		    	int pcg = (pc&0x00FF00)>>8;
	       		    	int pcb = (pc&0x0000FF);
	       		    	
						pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
						pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
						pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

	       		 		rb.pixel[p] = (pcr<<16)|(pcg<<8)|(pcb);
				 }
				 p++;
				 ci++;
			}
			while((--len)!=0);
		}
	}


    void blend_solid_hspan(int x, int y,
            int len, 
            int c, int cr,int cg, int cb,
            int[] covers,int ci,
            int rbw, int rboff, int[] rbp)
    {
		//if (c.a)
		{
			int p = rboff+x+y*rbw;
			
			do 
			{
				 int alpha = (0xFF * (covers[ci] + 1)) >> 8;

				if(printout)
       		 		System.out.println("pixel(" + x + "," + y + "," + alpha + "," + covers[ci] +")");
       		 		//int alpha = ((covers[ci]));// + 1);// >> 8;
				 if(alpha == base_mask)
				 {
					 rbp[p] = c;
				 }
				 else
				 {
//			            p[Order::R] += (value_type)(((cr - p[Order::R]) * alpha) >> base_shift);
//			            p[Order::G] += (value_type)(((cg - p[Order::G]) * alpha) >> base_shift);
//			            p[Order::B] += (value_type)(((cb - p[Order::B]) * alpha) >> base_shift);
//				     	m_blender.blend_pix(p, c.r, c.g, c.b, alpha, *covers);
	       		 		int pc = rbp[rboff+p];
	       		    	int pcr = (pc&0xFF0000)>>16;
	       		    	int pcg = (pc&0x00FF00)>>8;
	       		    	int pcb = (pc&0x0000FF);
	       		    	
						pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
						pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
						pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

						rbp[p] = (pcr<<16)|(pcg<<8)|(pcb);
				 }
				 p++;
				 ci++;
			}
			while((--len)!=0);
		}
	}    
	public void scan2(RenderBuffer rb,int c)
	{
    	int cr = (c&0xFF0000)>>16;
    	int cg = (c&0x00FF00)>>8;
    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                    blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN], 
	                    					  c,cr,cg,cb, 
	                    					  m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb);
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                    blend_hline(//x1, y, x2 - x1 + 1, 
	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
	                    		c,cr,cg,cb, 
	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}
	
	public void scan2withalpha(RenderBuffer rb,int c)
	{
		int ca = 0xFF-((c&0xFF000000)>>>24);
    	int cr = (c&0xFF0000)>>16;
    	int cg = (c&0x00FF00)>>8;
    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area,ca);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1),ca);
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                    blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN], 
	                    					  c,cr,cg,cb, 
	                    					  m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb);
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                    blend_hline(//x1, y, x2 - x1 + 1, 
	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
	                    		c,cr,cg,cb, 
	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}
	
	public void scan2withalphaandid(RenderBuffer rb,int c,RenderBuffer idBuffer,int id)
	{
		int ca = 0xFF-((c&0xFF000000)>>>24);
    	int cr = (c&0xFF0000)>>16;
    	int cg = (c&0x00FF00)>>8;
    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		if(sy>=m_cell_yind.height)
    		{
    			System.out.println("err");
    		}
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area,ca);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1),ca);
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                    blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN], 
	                    					  c,cr,cg,cb, 
	                    					  m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer);
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                    blend_hline(//x1, y, x2 - x1 + 1, 
	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
	                    		c,cr,cg,cb, 
	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}
	
	public void scan2withpolynomialandid(RenderBuffer rb,GeneralMatrixFloat[] colourpolynomial,int polyd,
			RenderBuffer idBuffer,int id)
	{
		int[] tempc = new int[4];
//		int ca = 0xFF-((c&0xFF000000)>>>24);
//    	int cr = (c&0xFF0000)>>16;
//    	int cg = (c&0x00FF00)>>8;
//    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		if(sy>=m_cell_yind.height)
    		{
    			System.out.println("err");
    		}
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                	blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN],
	                			colourpolynomial,polyd,tempc,
	                			m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer
	                					);	                			
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                	blend_solid_hspan(spanx, y,-m_spans.value[(span<<2)+SPAN_LEN],
	                			colourpolynomial,polyd,tempc,
	                			m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer
	                					);	                			
	                	
//	                    blend_hline(//x1, y, x2 - x1 + 1, 
//	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
//	                    		c,cr,cg,cb, 
//	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}
	
	public void scan2withpolynomial(RenderBuffer rb,GeneralMatrixFloat[] colourpolynomial,int polyd)
	{
		int[] tempc = new int[4];
//		int ca = 0xFF-((c&0xFF000000)>>>24);
//    	int cr = (c&0xFF0000)>>16;
//    	int cg = (c&0x00FF00)>>8;
//    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		if(sy>=m_cell_yind.height)
    		{
    			System.out.println("err");
    		}
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                	blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN],
	                			colourpolynomial,polyd,tempc,
	                			m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb
	                					);	                			
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                	blend_solid_hspan(spanx, y,-m_spans.value[(span<<2)+SPAN_LEN],
	                			colourpolynomial,polyd,tempc,
	                			m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb
	                					);	                			
	                	
//	                    blend_hline(//x1, y, x2 - x1 + 1, 
//	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
//	                    		c,cr,cg,cb, 
//	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rb,id,idBuffer);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}

	public void scan2(int rbw,int rboff,int[] rbp,int c)
	{
    	int cr = (c&0xFF0000)>>16;
    	int cg = (c&0x00FF00)>>8;
    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int cover = 0;
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rbw;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		reset_spans();
    		int yoff = m_cell_yind.value[sy];
    		int num_cells = m_cell_yhist.value[sy];
        	int sx = 0;
	        while(num_cells!=0)
	        {
	        	int si = m_sorted_cells.value[yoff+sx];

	            //const cell_aa* cur_cell = *cells;
	            int x    = m_cells.value[(si<<2)+CELL_X];
	            int area = m_cells.value[(si<<2)+CELL_AREA];
	            int alpha;
	
	            cover += m_cells.value[(si<<2)+CELL_COVER];
	
	            //accumulate all cells with the same X
	            while((--num_cells)!=0)
	            {
	            	sx++;
	            	si = m_sorted_cells.value[yoff+sx];
	                //cur_cell = *++cells;
	                if(m_cells.value[(si<<2)+CELL_X] != x) break;
	                area  += m_cells.value[(si<<2)+CELL_AREA];
	                cover += m_cells.value[(si<<2)+CELL_COVER];
	            }
	
	            if(area!=0)
	            {
	                alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
	                if(alpha!=0)
	                {
//	                	if((x==262)&&(cover==-256)&&(area==-119552))
//	                		System.out.println("test");
						if(printout)
							System.out.println("add_cell(" + x + "," + cover + "," + area + ")");
	                    add_cell(x, alpha);
	                }
	                x++;
	            }
	
	            if((num_cells!=0) && (m_cells.value[(si<<2)+CELL_X] > x))
	            {
	                alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
	                if(alpha!=0)
	                {
						if(printout)
							System.out.println("add_span(" + x + "," + m_cells.value[(si<<2)+CELL_X] + "," + cover + ")");
	                    add_span(x, m_cells.value[(si<<2)+CELL_X] - x, alpha);
	                }
	            }
	        }

	        if(m_last_x!=0x7FFFFFF0)
	        {
	            int y = m_min_y+sy;
	            int num_spans = m_cur_span+1;
	            //typename Scanline::const_iterator span = sl.begin();
	            int span = 0;
	            for(;;)
	            {
					if(printout)
						System.out.println("render_scanline_aa_solid(" + m_spans.value[(span<<2)+SPAN_X] + "," + y + "," + m_spans.value[(span<<2)+SPAN_LEN] + ")");
	            	//cout << "render_scanline_aa_solid(" << span->x << "," << y << "," << span->len << ")" << endl;
	            	int spanx = m_spans.value[(span<<2)+SPAN_X];
	                if(m_spans.value[(span<<2)+SPAN_LEN] > 0)
	                {
	                    blend_solid_hspan(spanx, y, m_spans.value[(span<<2)+SPAN_LEN], 
	                    					  c,cr,cg,cb, 
	                    					  m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rbw,rboff,rbp);
	                }
	                else
	                {
	//                	int x1 = spanx;
	//                	int x2 = (spanx - m_spans.value[span<<2+SPAN_LEN] - 1);
	//                    blend_hline(spanx, y, , 
	//                                    c,cr,cg,cb, 
	//                                    m_covers.value[m_spans.value[span<<2+SPAN_COVERS]]);
	
	                    blend_hline(//x1, y, x2 - x1 + 1, 
	                    		spanx,y,-m_spans.value[(span<<2)+SPAN_LEN],
	                    		c,cr,cg,cb, 
	                    		m_covers.value,m_spans.value[(span<<2)+SPAN_COVERS],rbw,rboff,rbp);
	                }
	                if(--num_spans == 0) break;
	                ++span;
	            }
	        }
    	}

	}	
    public void scan(RenderBuffer rb,int c)
    {
    	int cr = (c&0xFF0000)>>16;
    	int cg = (c&0x00FF00)>>8;
    	int cb = (c&0x0000FF);
    	//int numxs = 0;
    	
    	int yn = m_max_y-m_min_y;
    	int pind = m_min_y*rb.width;
    	for(int sy=0;sy<=yn;sy++)
    	{
    		int yoff = m_cell_yind.value[sy];
    		int xn = m_cell_yhist.value[sy];
    		int xnn = xn-1;
    		if(xn==0)
    		{
    			pind += rb.width;
    			continue;
    		}
    		
    		int si = m_sorted_cells.value[yoff]<<2;
    		int cx = m_cells.value[si];
    		int cover = 0;
    		int area = 0;
    		int alpha;
    		for(int sx=0;sx<xn;sx++)
    		{
    			si = m_sorted_cells.value[yoff+sx]<<2;
    			int tx = m_cells.value[si];
    			if(tx==cx)
    			{
    				area += m_cells.value[si+CELL_AREA];
    				cover += m_cells.value[si+CELL_COVER];
    			}
    			else
    			{
    				alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
//    				if((numxs>0)&&(tx==(cx+1))&&(alpha==xs[numxs-4+1]))
//    				{
//    					//just extend the previous span
//    					xs[numxs-4+2]++;
//    				}
//    				else
    				{
    					//add the alpha for the start of the span
//	    				xs[numxs] = cx;
//	    				xs[numxs+1] = alpha;
//	    				xs[numxs+2] = 1;
//	    				numxs += 4;
		       		 	if(alpha == base_mask)
		       		 	{
		       		 		rb.pixel[pind+cx] = c;
		       		 	}
		       		 	else
		       		 	{
		       		 		int pc = rb.pixel[pind+cx];
		       		    	int pcr = (pc&0xFF0000)>>16;
		       		    	int pcg = (pc&0x00FF00)>>8;
		       		    	int pcb = (pc&0x0000FF);
		       		    	
							pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
							pcg = ((cr-pcg)*alpha+(pcg<<8))>>8;
							pcb = ((cr-pcb)*alpha+(pcb<<8))>>8;

		       		 		rb.pixel[pind+cx] = (pcr<<16)|(pcg<<8)|(pcb);
		       		 	}
	    				
	    				//if not the last x, add a span to the next sample
	    				//if(sx<xnn)
	    				{
	    					//calculate the alpha for this run
		    				alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
		    				//then add a span
		    				if(alpha>0)
		    				{
		    					for(int spanx=cx+1;spanx<tx;spanx++)
		    					{
		    		       		 	if(alpha == base_mask)
		    		       		 	{
		    		       		 		rb.pixel[pind+spanx] = c;
		    		       		 	}
		    		       		 	else
		    		       		 	{
		    		       		 		int pc = rb.pixel[pind+spanx];
		    		       		    	int pcr = (pc&0xFF0000)>>16;
		    		       		    	int pcg = (pc&0x00FF00)>>8;
		    		       		    	int pcb = (pc&0x0000FF);
		    		       		    	
		    							pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
		    							pcg = ((cr-pcg)*alpha+(pcg<<8))>>8;
		    							pcb = ((cr-pcb)*alpha+(pcb<<8))>>8;

		    		       		 		rb.pixel[pind+cx] = (pcr<<16)|(pcg<<8)|(pcb);
		    		       		 	}
		    					}
//			    				xs[numxs] = cx+1;
//			    				xs[numxs+1] = alpha;
//			    				xs[numxs+2] = nextx-(cx+1);
//			    				cx = tx;
//			    				numxs += 4;
		    				}
	    				}	    				
    				}
    				area = m_cells.value[si+CELL_AREA];
    				cover = m_cells.value[si+CELL_COVER];
    				cx = tx;
    			}
    		}
    		
    		if(area!=0)
    		{
    			alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
    			
       		 	if(alpha == base_mask)
       		 	{
       		 		rb.pixel[pind+cx] = c;
       		 	}
       		 	else
       		 	{
       		 		int pc = rb.pixel[pind+cx];
       		    	int pcr = (pc&0xFF0000)>>16;
       		    	int pcg = (pc&0x00FF00)>>8;
       		    	int pcb = (pc&0x0000FF);
       		    	
					pcr = ((cr-pcr)*alpha+(pcr<<8))>>8;
					pcg = ((cg-pcg)*alpha+(pcg<<8))>>8;
					pcb = ((cb-pcb)*alpha+(pcb<<8))>>8;

       		 		rb.pixel[pind+cx] = (pcr<<16)|(pcg<<8)|(pcb);
       		 	}
//				xs[numxs] = cx;
//				xs[numxs+1] = alpha;
//				numxs += 2;
    		}
    		
    		
    		pind += rb.width;
    	}
    }
    
//    public boolean sweep_scanline(Scanline sl)
//    {
//        for(;;)
//        {
//            if(m_scan_y > m_outline.max_y()) return false;
//            sl.reset_spans();
//            unsigned num_cells = m_outline.scanline_num_cells(m_scan_y);
//            const cell_aa* const* cells = m_outline.scanline_cells(m_scan_y);
//            int cover = 0;
//
//            while(num_cells)
//            {
//                const cell_aa* cur_cell = *cells;
//                int x    = cur_cell->x;
//                int area = cur_cell->area;
//                unsigned alpha;
//
//                cover += cur_cell->cover;
//
//                //accumulate all cells with the same X
//                while(--num_cells)
//                {
//                    cur_cell = *++cells;
//                    if(cur_cell->x != x) break;
//                    area  += cur_cell->area;
//                    cover += cur_cell->cover;
//                }
//
//                if(area)
//                {
//                    alpha = calculate_alpha((cover << (poly_subpixel_shift + 1)) - area);
//                    if(alpha)
//                    {
//                        sl.add_cell(x, alpha);
//                    }
//                    x++;
//                }
//
//                if(num_cells && cur_cell->x > x)
//                {
//                    alpha = calculate_alpha(cover << (poly_subpixel_shift + 1));
//                    if(alpha)
//                    {
//                        sl.add_span(x, cur_cell->x - x, alpha);
//                    }
//                }
//            }
//    
//            if(sl.num_spans()) break;
//            ++m_scan_y;
//        }
//
//        sl.finalize(m_scan_y);
//        ++m_scan_y;
//        return true;
//    }
	
//	private void add_curr_cell()
//    {
//        if((m_cells.value[m_curr_cell_ptr+CELL_COVER] | m_cells.value[m_curr_cell_ptr+CELL_AREA])!=0)
//        {
//			m_curr_cell_ptr = m_cells.appendRow()<<2;
//        }
//    }

	private void set_curr_cell(int x, int y)
	{
//		if(printout)
//			System.out.println("set_curr_cell("+x+","+y+")");
		if(
				(m_cells.height==0)||
				(m_cells.value[m_curr_cell_ptr+CELL_X]!=x)||
				(m_cells.value[m_curr_cell_ptr+CELL_Y]!=y)
		  )
		{
//			if(y>m_max_y)
//				System.out.println("?");
//			if(y<m_min_y)
//				System.out.println("?");
			m_curr_cell_ptr = m_cells.appendRow()<<2;
			m_cells.value[m_curr_cell_ptr+CELL_X] = x;
			m_cells.value[m_curr_cell_ptr+CELL_Y] = y;
			m_cells.value[m_curr_cell_ptr+CELL_COVER] = 0;
			m_cells.value[m_curr_cell_ptr+CELL_AREA] = 0;
		}
	}

	private void validate()
	{
    	for(int i=0;i<m_cells.height;i++)
    	{
    		int y = m_cells.value[(i<<2)+CELL_Y];
    		int yind = y-m_min_y;
    		if(yind>=(1+m_max_y-m_min_y))
    			System.out.println("?");
			if(yind<0)
				System.out.println("?");
    	}
	}
	
	private void render_hline(int ey, int x1, int y1, int x2, int y2)
	{
		//validate();
//		if(printout)
//		{
//			System.out.println("render_hline("+ey+","+x1+","+y1+","+x2+","+y2+")");
//		}
		
    	int ex1 = x1 >> poly_subpixel_shift;
		int ex2 = x2 >> poly_subpixel_shift;
		int fx1 = x1 & poly_subpixel_mask;
		int fx2 = x2 & poly_subpixel_mask;

		int delta;
		int p;
		int first;
		int dx;
		int incr;
		int lift;
		int mod;
		int rem;

		//trivial case. Happens often
		if(y1 == y2)
		{
			set_curr_cell(ex2, ey);
			return;
		}

		//everything is located in a single cell.  That is easy!
		if(ex1 == ex2)
		{
			delta = y2 - y1;
			m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
			m_cells.value[m_curr_cell_ptr+CELL_AREA] += (fx1 + fx2) * delta;
			return;
		}

		//ok, we'll have to render a run of adjacent cells on the same
		//hline...
		p = (poly_subpixel_scale - fx1) * (y2 - y1);
		first = poly_subpixel_scale;
		incr = 1;

		dx = x2 - x1;

		if(dx < 0)
		{
			p = fx1 * (y2 - y1);
			first = 0;
			incr = -1;
			dx = -dx;
		}

		delta = p / dx;
		mod = p % dx;

		if(mod < 0)
		{
			delta--;
			mod += dx;
		}

		m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
		m_cells.value[m_curr_cell_ptr+CELL_AREA] += (fx1 + first) * delta;

		ex1 += incr;
		set_curr_cell(ex1, ey);
		y1 += delta;

		if(ex1 != ex2)
		{
			p = poly_subpixel_scale * (y2 - y1 + delta);
			lift = p / dx;
			rem = p % dx;

			if (rem < 0)
			{
				lift--;
				rem += dx;
			}

			mod -= dx;

			while (ex1 != ex2)
			{
				delta = lift;
				mod += rem;
				if(mod >= 0)
				{
					mod -= dx;
					delta++;
				}

				m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
				m_cells.value[m_curr_cell_ptr+CELL_AREA] += poly_subpixel_scale * delta;
				y1 += delta;
				ex1 += incr;
				set_curr_cell(ex1, ey);
			}
		}
		delta = y2 - y1;
		m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
		m_cells.value[m_curr_cell_ptr+CELL_AREA] += (fx2 + poly_subpixel_scale - first) * delta;

		//validate();
	}

    void line(int x1, int y1, int x2, int y2)
    {
        int dx = x2 - x1;

        if(dx >= dx_limit || dx <= -dx_limit)
        {
            int cx = (x1 + x2) >> 1;
            int cy = (y1 + y2) >> 1;
            line(x1, y1, cx, cy);
            line(cx, cy, x2, y2);
        }

        int dy = y2 - y1;
        int ex1 = x1 >> poly_subpixel_shift;
        int ex2 = x2 >> poly_subpixel_shift;
        int ey1 = y1 >> poly_subpixel_shift;
        int ey2 = y2 >> poly_subpixel_shift;
        int fy1 = y1 & poly_subpixel_mask;
        int fy2 = y2 & poly_subpixel_mask;

        int x_from, x_to;
        int p, rem, mod, lift, delta, first, incr;

        if(ex1 < m_min_x) m_min_x = ex1;
        if(ex1 > m_max_x) m_max_x = ex1;
        if(ey1 < m_min_y) m_min_y = ey1;
        if(ey1 > m_max_y) m_max_y = ey1;
        if(ex2 < m_min_x) m_min_x = ex2;
        if(ex2 > m_max_x) m_max_x = ex2;
        if(ey2 < m_min_y) m_min_y = ey2;
        if(ey2 > m_max_y) m_max_y = ey2;

        set_curr_cell(ex1, ey1);

        //everything is on a single hline
        if(ey1 == ey2)
        {
            render_hline(ey1, x1, fy1, x2, fy2);
            return;
        }

        //Vertical line - we have to calculate start and end cells,
        //and then - the common values of the area and coverage for
        //all cells of the line. We know exactly there's only one 
        //cell, so, we don't have to call render_hline().
        incr  = 1;
        if(dx == 0)
        {
            int ex = x1 >> poly_subpixel_shift;
            int two_fx = (x1 - (ex << poly_subpixel_shift)) << 1;
            int area;

            first = poly_subpixel_scale;
            if(dy < 0)
            {
                first = 0;
                incr  = -1;
            }

            x_from = x1;

            //render_hline(ey1, x_from, fy1, x_from, first);
            delta = first - fy1;
            m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
            m_cells.value[m_curr_cell_ptr+CELL_AREA]  += two_fx * delta;

            ey1 += incr;
            set_curr_cell(ex, ey1);

            delta = first + first - poly_subpixel_scale;
            area = two_fx * delta;
            while(ey1 != ey2)
            {
                //render_hline(ey1, x_from, poly_subpixel_scale - first, x_from, first);
            	m_cells.value[m_curr_cell_ptr+CELL_COVER] = delta;
            	m_cells.value[m_curr_cell_ptr+CELL_AREA]  = area;
                ey1 += incr;
                set_curr_cell(ex, ey1);
            }
            //render_hline(ey1, x_from, poly_subpixel_scale - first, x_from, fy2);
            delta = fy2 - poly_subpixel_scale + first;
            m_cells.value[m_curr_cell_ptr+CELL_COVER] += delta;
            m_cells.value[m_curr_cell_ptr+CELL_AREA]  += two_fx * delta;
            
    		//validate();

            return;
        }

        //ok, we have to render several hlines
        p     = (poly_subpixel_scale - fy1) * dx;
        first = poly_subpixel_scale;

        if(dy < 0)
        {
            p     = fy1 * dx;
            first = 0;
            incr  = -1;
            dy    = -dy;
        }

        delta = p / dy;
        mod   = p % dy;

        if(mod < 0)
        {
            delta--;
            mod += dy;
        }

        x_from = x1 + delta;
        render_hline(ey1, x1, fy1, x_from, first);

		//validate();

    	
    	ey1 += incr;

    	set_curr_cell(x_from >> poly_subpixel_shift, ey1);
        
		//validate();

        if(ey1 != ey2)
        {
            p     = poly_subpixel_scale * dx;
            lift  = p / dy;
            rem   = p % dy;

            if(rem < 0)
            {
                lift--;
                rem += dy;
            }
            mod -= dy;

            while(ey1 != ey2)
            {
                delta = lift;
                mod  += rem;
                if (mod >= 0)
                {
                    mod -= dy;
                    delta++;
                }

                x_to = x_from + delta;
                render_hline(ey1, x_from, poly_subpixel_scale - first, x_to, first);
                x_from = x_to;

                ey1 += incr;
                set_curr_cell(x_from >> poly_subpixel_shift, ey1);
        		//validate();
            }
        }
        render_hline(ey1, x_from, poly_subpixel_scale - first, x2, fy2);
    }

    private static void quicksort(int[] a,int in[], int left, int right) 
    {
        if (right <= left) return;
        int i = partition(a,in, left, right);
        quicksort(a,in, left, i-1);
        quicksort(a,in, i+1, right);
    }
    private static int partition(int[] a,int in[], int left, int right) 
    {
        int i = left - 1;
        int j = right;
        while (true) 
        {
            while (in[(a[++i])<<2]<in[a[right]<<2])      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (in[a[right]<<2]<in[a[--j]<<2])      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
        int t = a[i];
        a[i] = a[right];
        a[right] = t;
        //exch(a,in, i, right);                      // swap with partition element
        return i;
    }

    public void sort_cells()
    {
    	
    	//System.out.println("eh?");
    	
    	if(m_sorted)
    		return;
    	
    	m_cell_yhist.setDimensions(1, 1+m_max_y-m_min_y);
    	m_cell_yind.setDimensions(1, 1+m_max_y-m_min_y);
    	m_sorted_cells.setDimensions(1, m_cells.height);
    	m_cell_yhist.clear(0);

    	for(int i=0;i<m_cells.height;i++)
    	{
    		int y = m_cells.value[(i<<2)+CELL_Y];
    		int yind = y-m_min_y;
//    		if(yind>=(1+m_max_y-m_min_y))
//    			System.out.println("?");
//			if(yind<0)
//				System.out.println("?");
    		m_cell_yhist.value[yind]++;
    	}

    	//convert to offsets for each y row
    	int si = 0;
    	for(int i=0;i<m_cell_yhist.height;i++)
    	{
    		int v = m_cell_yhist.value[i];
    		m_cell_yind.value[i] = si;
    		m_cell_yhist.value[i] = 0;
    		si += v;
    	}
    	
    	for(int i=0;i<m_cells.height;i++)
    	{
    		int cy = m_cells.value[(i<<2)+CELL_Y]-m_min_y;
    		int cn = m_cell_yind.value[cy]+m_cell_yhist.value[cy];
    		m_sorted_cells.value[cn] = i;
    		m_cell_yhist.value[cy]++;
    	}

    	for(int i=0;i<m_cell_yhist.height;i++)
    	{
    		int is = m_cell_yind.value[i];
    		int ie = is+m_cell_yhist.value[i]-1;

    		quicksort(m_sorted_cells.value, m_cells.value, is, ie);
    		
    		//print out the first row data
//    		if(i==0)
//    		{
//    			for(int ci=is;ci<=ie;ci++)
//    			{
//    				int tsi = m_sorted_cells.value[ci];
//    				System.out.println("m_cell:"+m_cells.value[(tsi<<2)+CELL_X]+","+m_cells.value[(tsi<<2)+CELL_Y]
//    				                 +","+m_cells.value[(tsi<<2)+CELL_AREA]+","+m_cells.value[(tsi<<2)+CELL_COVER]);
//    			}
//    		}
    	}
    	
    	m_sorted = true;
    }
    

}

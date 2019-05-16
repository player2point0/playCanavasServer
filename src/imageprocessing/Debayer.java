package imageprocessing;

import rendering.RenderBuffer;

public class Debayer 
{
	//Out can be the same as rb
	public static void debayer_RG_GB(RenderBuffer rb,RenderBuffer out)
	{
		out.resize(rb.width, rb.height);
		//Super simple loss of resolution estimate for colour values
		for(int y=0;y<(rb.height/2);y++)
		{
			for(int x=0;x<(rb.width/2);x++)
			{
				int offset = y*2*rb.width+x*2; 
				int ul = rb.pixel[offset];
				int ur = rb.pixel[offset+1];
				int ll = rb.pixel[offset+rb.width];
				int lr = rb.pixel[offset+1+rb.width];

				//bggr
				int g = (ur+ll)>>2;
				int r = ul;//lr;
				int b = lr;//ul;
				
//				int g = (ul+lr)>>2;
//				int r = ur;
//				int b = ll;
				
				int c = (r<<16)|(g<<8)|(b<<0);
				int c1 = (r<<16)|(ur<<8)|(b<<0);
				int c2 = (r<<16)|(ll<<8)|(b<<0);
				out.pixel[offset] = c;
				out.pixel[offset+1] = c1;
				out.pixel[offset+rb.width] = c2;
				out.pixel[offset+1+rb.width] = c;
			}
		}
	}
	
	public static void debayer_cok_RG_GB(RenderBuffer rb,RenderBuffer out)
	{
		out.resize(rb.width*3, rb.height);
		
		int green_threshold = 20;
		
//	    int x_size = inData->dimensions[1];
//	    unsigned char * srcR1 = (unsigned char *) inData->data;
//	    unsigned char * srcR0 = &srcR1[-x_size];
//	    unsigned char * srcR2 = &srcR1[x_size];
//	    unsigned char * srcR3 = &srcR1[x_size*2];
//	    unsigned char * destR0;
//	    unsigned char * destR1 = (unsigned char *) outData->data;
//	    unsigned char * destR2 = &destR1[x_size*3];
//	    unsigned char * destR3;
//	    int hx = inData->dimensions[1] / 2;
//	    int hy = inData->dimensions[0] / 2;
//	    int x,y;

		int green_hgrad;
		int green_vgrad;
		
		int x_size = rb.width;
		
		int srcR0 = -rb.width;
		int srcR1 = 0;
		int srcR2 = rb.width;
		int srcR3 = rb.width*2;
		
		int destR0;
		int destR1 = 0;
		int destR2 = x_size*3;
		int destR3;
		
		int hx = rb.width/2;
		int hy = rb.height/2;
		int x,y;
		
	    // (0,0)
	    out.pixel[destR1++] = rb.pixel[srcR1]; // R
	    out.pixel[destR1++] = (((rb.pixel[srcR2]) + (rb.pixel[srcR1+1])) >> 1); // G
	    destR1++; // B
	    // (1,0)
	    destR2++; // R
	    out.pixel[destR2++] = rb.pixel[srcR2]; // G
	    destR2++; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    // (0,1)
	    destR1++; // R
	    out.pixel[destR1++] = rb.pixel[srcR1]; // G
	    destR1++; // B
	    // (1,1)
	    destR2++; // R
	    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1]) + (rb.pixel[srcR3]) + (rb.pixel[srcR1+1])) >> 2); // G
	    out.pixel[destR2++] = rb.pixel[srcR2]; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    for (x=1; x < hx-1; x++)
	    {
	        // R G R G R G
	        // G B G B G B
	        // R G R G R G
	        // G B G B G B
	        // R G R G R G
	        // G B G B G B
	        
	        // (0,0)
	        out.pixel[destR1++] = rb.pixel[srcR1]; // R
	        out.pixel[destR1++] = (((rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	        destR1++; // B
	        // (1,0)
	        destR2++; // R
	        out.pixel[destR2++] = rb.pixel[srcR2]; // G
	        destR2++; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        // (0,1)
	        destR1++; // R
	        out.pixel[destR1++] = rb.pixel[srcR1]; // G
	        destR1++; // B
	        // (1,1)
	        destR2++; // R
	        out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1]) + (rb.pixel[srcR3]) + (rb.pixel[srcR1+1])) >> 2); // G
	        out.pixel[destR2++] = rb.pixel[srcR2]; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	    }
	    // (0,0)
	    out.pixel[destR1++] = rb.pixel[srcR1]; // R
	    out.pixel[destR1++] = (((rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	    destR1++; // B
	    // (1,0)
	    destR2++; // R
	    out.pixel[destR2++] = rb.pixel[srcR2]; // G
	    destR2++; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    // (0,1)
	    destR1++; // R
	    out.pixel[destR1++] = rb.pixel[srcR1]; // G
	    destR1++; // B
	    // (1,1)
	    destR2++; // R
	    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR3])) >> 1); // G
	    out.pixel[destR2++] = rb.pixel[srcR2]; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    if ((x_size % 2)!=0)
	    {
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        destR1++; out.pixel[destR1++] = 0; destR1++;
	        destR2++; out.pixel[destR2++] = 0; destR2++;
	    }
	    destR1 += x_size*3; destR2 += x_size*3;
	    srcR0 += x_size; srcR1 += x_size; srcR2 += x_size; srcR3 += x_size;
	    
	    ///////////////////////////////
	    
	    for (y=1; y < hy-1; y++)
	    {
	        // (0,0)
	        out.pixel[destR1++] = rb.pixel[srcR1]; // R
	        out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR2])) >> 1); // G
	        destR1++; // B
	        // (1,0)
	        destR2++; // R
	        out.pixel[destR2++] = rb.pixel[srcR2]; // G
	        destR2++; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        // (0,1)
	        destR1++; // R
	        out.pixel[destR1++] = rb.pixel[srcR1]; // G
	        destR1++; // B
	        // (1,1)
	        destR2++; // R
	        out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1]) + (rb.pixel[srcR3]) + (rb.pixel[srcR1+1])) >> 2); // G
	        out.pixel[destR2++] = rb.pixel[srcR2]; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        for (x=1; x < hx-1; x++)
	        {
	            // R G R G R G
	            // G B G B G B
	            // R G R G R G
	            // G B G B G B
	            // R G R G R G
	            // G B G B G B
	            
	            // (0,0)
	            out.pixel[destR1++] = rb.pixel[srcR1]; // R
	            green_hgrad = rb.pixel[srcR1-1] - rb.pixel[srcR1+1];
	            green_vgrad = rb.pixel[srcR0] - rb.pixel[srcR2];
	            if ((green_hgrad < green_threshold) && (green_hgrad > -green_threshold)) {
	                if ((green_vgrad < green_threshold) && (green_vgrad > -green_threshold))
	                    out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1]) + (rb.pixel[srcR2])) >> 2); // G
	                else
	                    out.pixel[destR1++] = (((rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	            } else {
	                if ((green_vgrad < green_threshold) && (green_vgrad > -green_threshold))
	                    out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR2])) >> 1); // G
	                else
	                    out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1]) + (rb.pixel[srcR2])) >> 2); // G
	            }
	            destR1++; // B
	            // (1,0)
	            destR2++; // R
	            out.pixel[destR2++] = rb.pixel[srcR2]; // G
	            destR2++; // B
	            // END OF COLUMN
	            srcR0++; srcR1++; srcR2++; srcR3++;
	            // (0,1)
	            destR1++; // R
	            out.pixel[destR1++] = rb.pixel[srcR1]; // G
	            destR1++; // B
	            // (1,1)
	            destR2++; // R
	            
	            green_hgrad = rb.pixel[srcR2-1] - rb.pixel[srcR1+1];
	            green_vgrad = rb.pixel[srcR1] - rb.pixel[srcR3];
	            if ((green_hgrad < green_threshold) && (green_hgrad > -green_threshold)) {
	                if ((green_vgrad < green_threshold) && (green_vgrad > -green_threshold))
	                    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1]) + (rb.pixel[srcR3]) + (rb.pixel[srcR1+1])) >> 2); // G
	                else
	                    out.pixel[destR2++] = (((rb.pixel[srcR2-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	            } else {
	                if ((green_vgrad < green_threshold) && (green_vgrad > -green_threshold))
	                    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR3])) >> 1); // G
	                else
	                    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1]) + (rb.pixel[srcR3]) + (rb.pixel[srcR1+1])) >> 2) ; // G
	            }
	            out.pixel[destR2++] = rb.pixel[srcR2]; // B
	            // END OF COLUMN
	            srcR0++; srcR1++; srcR2++; srcR3++;
	        }
	        // (0,0)
	        out.pixel[destR1++] = rb.pixel[srcR1]; // R
	        out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1]) + (rb.pixel[srcR2])) >> 2); // G
	        destR1++; // B
	        // (1,0)
	        destR2++; // R
	        out.pixel[destR2++] = rb.pixel[srcR2]; // G
	        destR2++; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        // (0,1)
	        destR1++; // R
	        out.pixel[destR1++] = rb.pixel[srcR1]; // G
	        destR1++; // B
	        // (1,1)
	        destR2++; // R
	        out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR3])) >> 1); // G
	        out.pixel[destR2++] = rb.pixel[srcR2]; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        if((x_size % 2)!=0)
	        {
	            srcR0++; srcR1++; srcR2++; srcR3++;
	            destR1++; out.pixel[destR1++] = 0; destR1++;
	            destR2++; out.pixel[destR2++] = 0; destR2++;
	        }
	        destR1 += x_size*3; destR2 += x_size*3;
	        srcR0 += x_size; srcR1 += x_size; srcR2 += x_size; srcR3 += x_size;
	    }
	    ///////////////////////////////////////////
	    
	    // (0,0)
	    out.pixel[destR1++] = rb.pixel[srcR1]; // R
	    out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR2])) >> 1); // G
	    destR1++; // B
	    // (1,0)
	    destR2++; // R
	    out.pixel[destR2++] = rb.pixel[srcR2]; // G
	    destR2++; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    // (0,1)
	    destR1++; // R
	    out.pixel[destR1++] = rb.pixel[srcR1]; // G
	    destR1++; // B
	    // (1,1)
	    destR2++; // R
	    out.pixel[destR2++] = (((rb.pixel[srcR2-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	    out.pixel[destR2++] = rb.pixel[srcR2]; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    for (x=1; x < hx-1; x++)
	    {
	        // R G R G R G
	        // G B G B G B
	        // R G R G R G
	        // G B G B G B
	        // R G R G R G
	        // G B G B G B
	        
	        // (0,0)
	        out.pixel[destR1++] = rb.pixel[srcR1]; // R
	        out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1]) + (rb.pixel[srcR2])) >> 2); // G
	        destR1++; // B
	        // (1,0)
	        destR2++; // R
	        out.pixel[destR2++] = rb.pixel[srcR2]; // G
	        destR2++; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	        // (0,1)
	        destR1++; // R
	        out.pixel[destR1++] = rb.pixel[srcR1]; // G
	        destR1++; // B
	        // (1,1)
	        destR2++; // R
	        out.pixel[destR2++] = (((rb.pixel[srcR2-1]) + (rb.pixel[srcR1+1])) >> 1); // G
	        out.pixel[destR2++] = rb.pixel[srcR2]; // B
	        // END OF COLUMN
	        srcR0++; srcR1++; srcR2++; srcR3++;
	    }
	    // (0,0)
	    out.pixel[destR1++] = rb.pixel[srcR1]; // R
	    out.pixel[destR1++] = (((rb.pixel[srcR0]) + (rb.pixel[srcR1-1]) + (rb.pixel[srcR1+1]) + (rb.pixel[srcR2])) >> 2); // G
	    destR1++; // B
	    // (1,0)
	    destR2++; // R
	    out.pixel[destR2++] = rb.pixel[srcR2]; // G
	    destR2++; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    // (0,1)
	    destR1++; // R
	    out.pixel[destR1++] = rb.pixel[srcR1]; // G
	    destR1++; // B
	    // (1,1)
	    destR2++; // R
	    out.pixel[destR2++] = (((rb.pixel[srcR1]) + (rb.pixel[srcR2-1])) >> 1); // G
	    out.pixel[destR2++] = rb.pixel[srcR2]; // B
	    // END OF COLUMN
	    srcR0++; srcR1++; srcR2++; srcR3++;
	    
	    // RESET THE PIXEL DATA POINTERS
	    destR1 = 0;
	    destR2 = x_size*3;
	    destR3 = x_size*6;
	    destR0 = -x_size*3;
	    //~ destR0 += x_size * 6 + 6;
	    //~ destR1 += x_size * 6 + 6;
	    //~ destR2 += x_size * 6 + 6;
	    //~ destR3 += x_size * 6 + 6;
	    
	    
	    out.pixel[destR1+2] = out.pixel[destR2+5];
	    out.pixel[destR1+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	    out.pixel[destR1+5] = out.pixel[destR2+5];
	    out.pixel[destR2+0] = (((out.pixel[destR1]) + (out.pixel[destR3])) >> 1);
	    out.pixel[destR2+2] = out.pixel[destR2+5];
	    out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6]) + (out.pixel[destR3+0]) + (out.pixel[destR3+6])) >> 2);
	    destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	    for (x=1; x < hx-1; x++)
	    {
	        out.pixel[destR1+2] = (((out.pixel[destR2-1]) + (out.pixel[destR2+5])) >> 1);
	        out.pixel[destR1+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	        out.pixel[destR1+5] = out.pixel[destR2+5];
	        out.pixel[destR2+0] = (((out.pixel[destR1]) + (out.pixel[destR3])) >> 1);
	        out.pixel[destR2+2] = (((out.pixel[destR2+5]) + (out.pixel[destR2-1])) >> 1);
	        out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6]) + (out.pixel[destR3+0]) + (out.pixel[destR3+6])) >> 2);
	        destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	    }
	    out.pixel[destR1+2] = (((out.pixel[destR2-1]) + (out.pixel[destR2+5])) >> 1);
	    out.pixel[destR1+3] = out.pixel[destR1];
	    out.pixel[destR1+5] = out.pixel[destR2+5];
	    out.pixel[destR2+0] = (((out.pixel[destR1]) + (out.pixel[destR3])) >> 1);
	    out.pixel[destR2+2] = (((out.pixel[destR2+5]) + (out.pixel[destR2-1])) >> 1);
	    out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR3+0])) >> 1);
	    if ((x_size % 2)!=0)
	    {
	        destR0 += 9 + 3 * x_size;
	        destR1 += 9 + 3 * x_size;
	        destR2 += 9 + 3 * x_size;
	        destR3 += 9 + 3 * x_size;
	    }
	    else
	    {
	        destR0 += 6 + 3 * x_size;
	        destR1 += 6 + 3 * x_size;
	        destR2 += 6 + 3 * x_size;
	        destR3 += 6 + 3 * x_size;
	    }
	    
        float QF1_r0,QF1_r1,QF1_r2,QF1_r3;
        float QF2_r0,QF2_r1,QF2_r2,QF2_r3;
	    int QWi_r0, QWi_r1;
	    
	    for (y=1; y < hy-1; y++)
	    {
	        out.pixel[destR1+2] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	        out.pixel[destR1+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	        out.pixel[destR1+5] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	        out.pixel[destR2+0] = (((out.pixel[destR1]) + (out.pixel[destR3])) >> 1);
	        out.pixel[destR2+2] = out.pixel[destR2+5];
	        out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6]) + (out.pixel[destR3+0]) + (out.pixel[destR3+6])) >> 2);
	        destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	        for (x=1; x < hx-1; x++)
	        {
	        	//(B3 + B2, B1 + B0, A3 + A2, A1 + A0).
	        	
	            // interpolate the blue value for (0,0) - R
	            //QF1 = _mm_cvtpi16_ps(_mm_set_pi16(out.pixel[destR2+5], out.pixel[destR2-1], out.pixel[destR0+5], out.pixel[destR0-1]));
	            //QF2 = _mm_add_ps( _mm_cvtpi16_ps(_mm_set_pi16(destR2[4], destR2[-2], destR0[4], destR0[-2])), _mm_set1_ps(1) );
	            //QF1 = _mm_div_ps( QF1, QF2 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_mul_ss( QF1, _mm_set_ss( ((float) destR1[1]) * 0.25f ) );
	            //QF1 = _mm_min_ss( QF1, _mm_set_ss( 255.4f ) );
	            //out.pixel[destR1+2] = _mm_cvtss_si32( QF1 );
	            
	            QF1_r0 = out.pixel[destR2+5];   QF1_r1 = out.pixel[destR2-1];   QF1_r2 = out.pixel[destR0+5];   QF1_r3 = out.pixel[destR0-1];
	            QF2_r0 = out.pixel[destR2+4]+1; QF2_r1 = out.pixel[destR2-2]+1; QF2_r2 = out.pixel[destR0+4]+1; QF2_r3 = out.pixel[destR0-2]+1;
	            QF1_r0 = QF1_r0/QF2_r0;         QF1_r1 = QF1_r1/QF2_r1;         QF1_r2 = QF1_r2/QF2_r2;         QF1_r3 = QF1_r3/QF2_r3;   
	            
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            
	            QF1_r0 *= ((float) out.pixel[destR1+1]) * 0.25f;
	            QF1_r0 = (QF1_r0<255.4f)?QF1_r0:255.4f;
	            
	            out.pixel[destR1+2] = (int)QF1_r0;
	                      
	            // interpolate the red and blue values for (0,1) - G
	            //QF1 = _mm_cvtpi16_ps(_mm_set_pi16(out.pixel[destR2+5], out.pixel[destR0+5], out.pixel[destR1+6], out.pixel[destR1+0]));
	            //QF2 = _mm_add_ps( _mm_cvtpi16_ps(_mm_set_pi16(destR2[4], destR0[4], destR1[7], destR1[1])), _mm_set1_ps(1) );
	            //QF1 = _mm_div_ps( QF1, QF2 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_mul_ps( QF1, _mm_set1_ps( ((float) destR1[4]) * 0.5f ) );
	            //QF1 = _mm_min_ps( QF1, _mm_set1_ps( 255.4f ) );
	            //QW1.i64 = _mm_cvtsi64_si64x(_mm_cvtps_pi32( QF1 ));
	            //out.pixel[destR1+3] = QW1.i32[0];
	            //out.pixel[destR1+5] = QW1.i32[1];
	            
	            QF1_r0 = out.pixel[destR2+5];   QF1_r1 = out.pixel[destR0+5];   QF1_r2 = out.pixel[destR1+6];   QF1_r3 = out.pixel[destR1+0];
	            QF2_r0 = out.pixel[destR2+4]+1; QF2_r1 = out.pixel[destR0+4]+1; QF2_r2 = out.pixel[destR1+7]+1; QF2_r3 = out.pixel[destR1+1]+1;
	            QF1_r0 = QF1_r0/QF2_r0;         QF1_r1 = QF1_r1/QF2_r1;         QF1_r2 = QF1_r2/QF2_r2;         QF1_r3 = QF1_r3/QF2_r3;   
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            QF1_r0 *= ((float) out.pixel[destR1+4]) * 0.5f;
	            QF1_r0 = (QF1_r0<255.4f)?QF1_r0:255.4f;

	            QWi_r0 = (int)QF1_r0;           QWi_r1 = (int)QF1_r0;
	            out.pixel[destR1+3] = QWi_r0;
	            out.pixel[destR1+5] = QWi_r1;
	            
	            // interpolate the red and blue values for (1,0) - G
	            //QF1 = _mm_cvtpi16_ps(_mm_set_pi16(out.pixel[destR2+5], out.pixel[destR2-1], out.pixel[destR1+0], out.pixel[destR3+0]));
	            //QF2 = _mm_add_ps( _mm_cvtpi16_ps(_mm_set_pi16(destR2[4], destR2[-2], destR1[1], destR3[1])), _mm_set1_ps(1) );
	            //QF1 = _mm_div_ps( QF1, QF2 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_mul_ps( QF1, _mm_set1_ps( ((float) destR2[1]) * 0.5f ) );
	            //QF1 = _mm_min_ps( QF1, _mm_set1_ps( 255.4f ) );
	            //QW1.i64 = _mm_cvtsi64_si64x(_mm_cvtps_pi32( QF1 ));
	            //out.pixel[destR2+0] = QW1.i32[0];
	            //out.pixel[destR2+2] = QW1.i32[1];
	            
	            QF1_r0 = out.pixel[destR2+5];   QF1_r1 = out.pixel[destR2-1];   QF1_r2 = out.pixel[destR1+0];   QF1_r3 = out.pixel[destR3+0];
	            QF2_r0 = out.pixel[destR2+4]+1; QF2_r1 = out.pixel[destR2-2]+1; QF2_r2 = out.pixel[destR1+1]+1; QF2_r3 = out.pixel[destR3+1]+1;
	            QF1_r0 = QF1_r0/QF2_r0;         QF1_r1 = QF1_r1/QF2_r1;         QF1_r2 = QF1_r2/QF2_r2;         QF1_r3 = QF1_r3/QF2_r3;   
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            QF1_r0 *= ((float) out.pixel[destR2+1]) * 0.5f;
	            QF1_r0 = (QF1_r0<255.4f)?QF1_r0:255.4f;

	            QWi_r0 = (int)QF1_r0;           QWi_r1 = (int)QF1_r0;
	            out.pixel[destR2+0] = QWi_r0;
	            out.pixel[destR2+2] = QWi_r1;

	            // interpolate the red value for (1,1) - B
	            //QF1 = _mm_cvtpi16_ps(_mm_set_pi16(out.pixel[destR3+6], out.pixel[destR3+0], out.pixel[destR1+6], out.pixel[destR1+0]));
	            //QF2 = _mm_add_ps( _mm_cvtpi16_ps(_mm_set_pi16(destR3[7], destR3[1], destR1[7], destR1[1])), _mm_set1_ps(1) );
	            //QF1 = _mm_div_ps( QF1, QF2 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_hadd_ps( QF1, QF1 );
	            //QF1 = _mm_mul_ss( QF1, _mm_set_ss( ((float) destR2[4]) * 0.25f ) );
	            //QF1 = _mm_min_ss( QF1, _mm_set_ss( 255.4 ) );
	            //out.pixel[destR2+3] = _mm_cvtss_si32( QF1 );
	            
	            QF1_r0 = out.pixel[destR3+6];   QF1_r1 = out.pixel[destR3+0];   QF1_r2 = out.pixel[destR1+6];   QF1_r3 = out.pixel[destR1+0];
	            QF2_r0 = out.pixel[destR3+7]+1; QF2_r1 = out.pixel[destR3+1]+1; QF2_r2 = out.pixel[destR1+7]+1; QF2_r3 = out.pixel[destR1+1]+1;
	            QF1_r0 = QF1_r0/QF2_r0;         QF1_r1 = QF1_r1/QF2_r1;         QF1_r2 = QF1_r2/QF2_r2;         QF1_r3 = QF1_r3/QF2_r3;   
	            
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            QF1_r0 = QF1_r0+QF1_r1;         QF1_r1 = QF1_r2+QF1_r3;         QF1_r2 = QF1_r0;         		QF1_r3 = QF1_r1;         
	            
	            QF1_r0 *= ((float) out.pixel[destR2+4]) * 0.25f;
	            QF1_r0 = (QF1_r0<255.4f)?QF1_r0:255.4f;
	            
	            out.pixel[destR2+3] = (int)QF1_r0;

	            destR0 += 6;
	            destR1 += 6;
	            destR2 += 6;
	            destR3 += 6;
	        }
	        out.pixel[destR1+2] = (((out.pixel[destR2-1]) + (out.pixel[destR0-1]) + (out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 2);
	        out.pixel[destR1+3] = out.pixel[destR1+0];
	        out.pixel[destR1+5] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	        out.pixel[destR2+0] = (((out.pixel[destR1]) + (out.pixel[destR3])) >> 1);
	        out.pixel[destR2+2] = (((out.pixel[destR2+5]) + (out.pixel[destR2-1])) >> 1);
	        out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR3+0])) >> 1);
	        destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	        if ((x_size % 2)!=0)
	        {
	            destR0 += 3 + 3 * x_size;
	            destR1 += 3 + 3 * x_size;
	            destR2 += 3 + 3 * x_size;
	            destR3 += 3 + 3 * x_size;
	        }
	        else
	        {
	            destR0 += x_size * 3;
	            destR1 += x_size * 3;
	            destR2 += x_size * 3;
	            destR3 += x_size * 3;
	        }
	    }
	    out.pixel[destR1+2] = out.pixel[destR2+5];
	    out.pixel[destR1+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	    out.pixel[destR1+5] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	    out.pixel[destR2+0] = out.pixel[destR1];
	    out.pixel[destR2+2] = out.pixel[destR2+5];
	    out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	    destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	    for (x=1; x < hx-1; x++)
	    {
	        out.pixel[destR1+2] = (((out.pixel[destR2-1]) + (out.pixel[destR2+5])) >> 1);
	        out.pixel[destR1+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	        out.pixel[destR1+5] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	        out.pixel[destR2+0] = out.pixel[destR1];
	        out.pixel[destR2+2] = (((out.pixel[destR2+5]) + (out.pixel[destR2-1])) >> 1);
	        out.pixel[destR2+3] = (((out.pixel[destR1+0]) + (out.pixel[destR1+6])) >> 1);
	        destR0 += 6; destR1 += 6; destR2 += 6; destR3 += 6;
	    }
	    out.pixel[destR1+2] = (((out.pixel[destR2-1]) + (out.pixel[destR2+5])) >> 1);
	    out.pixel[destR1+3] = out.pixel[destR1];
	    out.pixel[destR1+5] = (((out.pixel[destR2+5]) + (out.pixel[destR0+5])) >> 1);
	    out.pixel[destR2+0] = out.pixel[destR1];
	    out.pixel[destR2+2] = (((out.pixel[destR2+5]) + (out.pixel[destR2-1])) >> 1);
	    out.pixel[destR2+3] = out.pixel[destR1+0];

	    int numPixels = rb.width*rb.height;
	    for(int pi=0;pi<numPixels;pi++)
	    {
	    	int r = out.pixel[pi*3+0];
	    	int g = out.pixel[pi*3+1];
	    	int b = out.pixel[pi*3+2];
	    	int c = (r<<16)|(g<<8)|(b);
	    	out.pixel[pi] = c;
	    }
	    out.width= rb.width;
	    out.height= rb.height;
	}
}
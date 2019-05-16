package agg;

import mathematics.MathematicsUtils;

public class Clipper 
{
	public int minx = 0;
	public int miny = 0;
	public int maxx = 800;
	public int maxy = 600;

	public static final int curve_recursion_limit = 32;
	public static final int curve_collinearity_epsilon = 0;
	public static final double curve_angle_tolerance_epsilon = 0.01;

    public boolean m_clipping = false;
    public int m_x1 = 0;
    public int m_y1 = 0;
    public int m_f1 = 0;

    public int m_distance_tolerance_square;
    public double m_distance_tolerance_square_d;
    public double m_angle_tolerance = 0.0;
    public double m_cusp_limit = 0.0;
	
	public Clipper()
	{
		float m_approximation_scale = 1.0f;
        float m_distance_tolerance_squaref = 0.5f / m_approximation_scale;
        m_distance_tolerance_squaref *= m_distance_tolerance_squaref;
        m_distance_tolerance_square_d = m_distance_tolerance_squaref;
        m_distance_tolerance_square = (int)(0x100*m_distance_tolerance_squaref);
	}
	
	final int clipping_flags_y(int y)
	{
		int f2 = 0;
		if(y > maxy) 
			f2 += 2;
		if(y < miny)
			f2 += 8;
		return f2;
	}

	static final int mul_div(int a,int b,int c)
	{
		return (a*b)/c;
	}

	static final double mul_div(double a,double b,double c)
	{
		return (a*b)/c;
	}

    static final int calc_sq_distance(int x1, int y1, int x2, int y2)
    {
        int dx = x2-x1;
        int dy = y2-y1;
        return dx * dx + dy * dy;
    }
	
    static final double calc_sq_distance(double x1, double y1, double x2, double y2)
    {
    	double dx = x2-x1;
    	double dy = y2-y1;
        return dx * dx + dy * dy;
    }

    public void move_to(float x1, float y1)
    {
    	move_to((int)(x1*256), (int)(y1*256));
    }
    
    public void move_to(int x1, int y1)
    {
        m_x1 = x1;
        m_y1 = y1;
        if(m_clipping)
        {
			int f2 = 0;
			if(x1 > maxx) 
				f2 = 1;
			if(y1 > maxy) 
				f2 += 2;
			if(x1 < minx)
				f2 += 4;
			if(y1 < miny)
				f2 += 8;
        	m_f1 = f2;
        }
    }

    public void curve_to(Rasteriser ras, int x2, int y2, int x3, int y3)
    {
    	//aa
    	recursive_bezier(ras, m_x1, m_y1, x2, y2, x3, y3, 0);
    	line_to(ras, x3, y3);
    	//
    }
	
    public void curve_to(Rasteriser ras, int x2, int y2, int x3, int y3, int x4, int y4)
    {
    	//aa
    	recursive_bezier(ras, m_x1, m_y1, x2, y2, x3, y3, x4, y4, 0);
    	//recursive_bezier(ras, m_x1, m_y1, x2, y2, x3, y3, x4, y4, 0);
    	line_to(ras, x4, y4);
    	//
    }

    public void curve_tod(Rasteriser ras, int x2, int y2, int x3, int y3, int x4, int y4)
    {
    	//aa
    	recursive_bezier(ras, (double)m_x1, (double)m_y1, (double)x2, (double)y2, (double)x3, (double)y3, (double)x4, (double)y4, 0);
    	//recursive_bezier(ras, m_x1, m_y1, x2, y2, x3, y3, x4, y4, 0);
    	line_to(ras, x4, y4);
    	//
    }


    void recursive_bezier(Rasteriser ras,int x1,int y1,int x2,int y2,int x3,int y3,int level)
    {
        if(level > curve_recursion_limit) 
        {
            return;
        }

        // Calculate all the mid-points of the line segments
        //----------------------
        int x12   = (x1 + x2) / 2;                
        int y12   = (y1 + y2) / 2;
        int x23   = (x2 + x3) / 2;
        int y23   = (y2 + y3) / 2;
        int x123  = (x12 + x23) / 2;
        int y123  = (y12 + y23) / 2;

        int dx = x3-x1;
        int dy = y3-y1;
        int d = Math.abs(((x2 - x3) * dy - (y2 - y3) * dx));
        //double da;

        if(d > curve_collinearity_epsilon)
        { 
            // Regular case
            //-----------------
        	int pd2 = MathematicsUtils.fastSqrt((m_distance_tolerance_square * (dx*dx + dy*dy))>>8);
            if(d<=pd2)
            //if(d * d <= (m_distance_tolerance_square * (dx*dx + dy*dy))>>8)
            {
                // If the curvature doesn't exceed the distance_tolerance value
                // we tend to finish subdivisions.
                //----------------------
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                    //m_points.add(point_d(x123, y123));
                    line_to(ras, x123, y123);
                    return;
                }

                // Angle & Cusp Condition
                //----------------------
                double da = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                if(da >= Math.PI) da = 2*Math.PI - da;

                if(da < m_angle_tolerance)
                {
                    // Finally we can stop the recursion
                    //----------------------
                    //m_points.add(point_d(x123, y123));
                    line_to(ras, x123, y123);
                    return;                 
                }
            }
        }
        else
        {
            // Collinear case
            //------------------
            int da = dx*dx + dy*dy;
            if(da == 0)
            {
                d = calc_sq_distance(x1, y1, x2, y2);
            }
            else
            {
                d = ((x2 - x1)*dx + (y2 - y1)*dy) / da;
                if(d > 0 && d < (1<<8))
                {
                    // Simple collinear case, 1---2---3
                    // We can leave just two endpoints
                    return;
                }
                     if(d <= 0) d = calc_sq_distance(x2, y2, x1, y1);
                else if(d >= 1) d = calc_sq_distance(x2, y2, x3, y3);
                else            d = calc_sq_distance(x2, y2, x1 + d*dx, y1 + d*dy);
            }
            if(d < m_distance_tolerance_square)
            {
            	line_to(ras, x2, y2);
                return;
            }
        }

        // Continue subdivision
        //----------------------
        recursive_bezier(ras, x1, y1, x12, y12, x123, y123, level + 1); 
        recursive_bezier(ras, x123, y123, x23, y23, x3, y3, level + 1); 
    }
    
    void recursive_bezier(Rasteriser ras,int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4,int level)
    {
        if(level > curve_recursion_limit) 
        {
            return;
        }

        // Calculate all the mid-points of the line segments
        //----------------------
        int x12   = (x1 + x2) / 2;
        int y12   = (y1 + y2) / 2;
        int x23   = (x2 + x3) / 2;
        int y23   = (y2 + y3) / 2;
        int x34   = (x3 + x4) / 2;
        int y34   = (y3 + y4) / 2;
        int x123  = (x12 + x23) / 2;
        int y123  = (y12 + y23) / 2;
        int x234  = (x23 + x34) / 2;
        int y234  = (y23 + y34) / 2;
        int x1234 = (x123 + x234) / 2;
        int y1234 = (y123 + y234) / 2;


        // Try to approximate the full cubic curve by a single straight line
        //------------------
        int dx = x4-x1;
        int dy = y4-y1;

        int d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
        int d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
        int da1, da2, k;
        double da1d,da2d,kd;

        int flags = 0;
        if((d2 > curve_collinearity_epsilon))
        	flags += 2; 
        if((d3 > curve_collinearity_epsilon))
        	flags++;
        switch(flags)
        {
        case 0:
            // All collinear OR p1==p4
            //----------------------
            k = dx*dx + dy*dy;
            k = k>>8;
            if(k == 0)
            {
                d2 = calc_sq_distance(x1, y1, x2, y2);
                d3 = calc_sq_distance(x4, y4, x3, y3);
            }
            else
            {
                da1 = x2 - x1;
                da2 = y2 - y1;
                d2  = (da1*dx + da2*dy)/k;
                da1 = x3 - x1;
                da2 = y3 - y1;
                d3  = (da1*dx + da2*dy)/k;
                if(d2 > 0 && d2 < (1<<8) && d3 > 0 && d3 < (1<<8))
                {
                    // Simple collinear case, 1---2---3---4
                    // We can leave just two endpoints
                    return;
                }
                     if(d2 <= 0) d2 = calc_sq_distance(x2, y2, x1, y1);
                else if(d2 >= (1<<8)) d2 = calc_sq_distance(x2, y2, x4, y4);
                else             d2 = calc_sq_distance(x2, y2, x1 + d2*dx, y1 + d2*dy);

                     if(d3 <= 0) d3 = calc_sq_distance(x3, y3, x1, y1);
                else if(d3 >= (1<<8)) d3 = calc_sq_distance(x3, y3, x4, y4);
                else             d3 = calc_sq_distance(x3, y3, x1 + d3*dx, y1 + d3*dy);
            }
            if(d2 > d3)
            {
                if(d2 < m_distance_tolerance_square)
                {
                	line_to(ras, x2, y2);
                    return;
                }
            }
            else
            {
                if(d3 < m_distance_tolerance_square)
                {
                	line_to(ras, x3, y3);
                    return;
                }
            }
            break;

        case 1:
            // p1,p2,p4 are collinear, p3 is significant
            //----------------------
            if(d3 * d3 <= (m_distance_tolerance_square * (dx*dx + dy*dy))>>8)
            {
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, x23, y23);
                    return;
                }

                // Angle Condition
                //----------------------
                da1d = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;

                if(da1d < m_angle_tolerance)
                {
                	line_to(ras, x2, y2);
                	line_to(ras, x3, y3);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, x3, y3);
                        return;
                    }
                }
            }
            break;

        case 2:
            // p1,p3,p4 are collinear, p2 is significant
            //----------------------
            if(d2 * d2 <= (m_distance_tolerance_square * (dx*dx + dy*dy))>>8)
            {
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, x23, y23);
                    return;
                }

                // Angle Condition
                //----------------------
                da1d = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;

                if(da1d < m_angle_tolerance)
                {
                	line_to(ras, x2, y2);
                	line_to(ras, x3, y3);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, x2, y2);
                        return;
                    }
                }
            }
            break;

        case 3: 
            // Regular case
            //-----------------
        	int pd = Math.abs(d2 + d3);
        	int pd2 = (int)MathematicsUtils.fastSqrt((m_distance_tolerance_square * (dx*dx + dy*dy))>>8);
            if(pd<=pd2)
            //if((d2 + d3)*(d2 + d3) <= m_distance_tolerance_square * (dx*dx + dy*dy))
            {
                // If the curvature doesn't exceed the distance_tolerance value
                // we tend to finish subdivisions.
                //----------------------
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, x23, y23);
                    return;
                }

                // Angle & Cusp Condition
                //----------------------
                kd   = Math.atan2(y3 - y2, x3 - x2);
                da1d = Math.abs(kd - Math.atan2(y2 - y1, x2 - x1));
                da2d = Math.abs(Math.atan2(y4 - y3, x4 - x3) - kd);
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;
                if(da2d >= Math.PI) da2d = 2*Math.PI - da2d;

                if(da1d + da2d < m_angle_tolerance)
                {
                    // Finally we can stop the recursion
                    //----------------------
                	line_to(ras, x23, y23);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, x2, y2);
                        return;
                    }

                    if(da2d > m_cusp_limit)
                    {
                    	line_to(ras, x3, y3);
                        return;
                    }
                }
            }
            break;
        }

        // Continue subdivision
        //----------------------
        recursive_bezier(ras, x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1); 
        recursive_bezier(ras, x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1); 
    }

    void recursive_bezier(Rasteriser ras,double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4,int level)
    {
        if(level > curve_recursion_limit) 
        {
            return;
        }

        // Calculate all the mid-points of the line segments
        //----------------------
        double x12   = (x1 + x2) / 2;
        double y12   = (y1 + y2) / 2;
        double x23   = (x2 + x3) / 2;
        double y23   = (y2 + y3) / 2;
        double x34   = (x3 + x4) / 2;
        double y34   = (y3 + y4) / 2;
        double x123  = (x12 + x23) / 2;
        double y123  = (y12 + y23) / 2;
        double x234  = (x23 + x34) / 2;
        double y234  = (y23 + y34) / 2;
        double x1234 = (x123 + x234) / 2;
        double y1234 = (y123 + y234) / 2;


        // Try to approximate the full cubic curve by a single straight line
        //------------------
        double dx = x4-x1;
        double dy = y4-y1;

        double d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
        double d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
        double da1, da2, k;
        double da1d,da2d,kd;

        int flags = 0;
        if((d2 > curve_collinearity_epsilon))
        	flags += 2; 
        if((d3 > curve_collinearity_epsilon))
        	flags++;
        switch(flags)
        {
        case 0:
            // All collinear OR p1==p4
            //----------------------
            k = dx*dx + dy*dy;
            if(k == 0)
            {
                d2 = calc_sq_distance(x1, y1, x2, y2);
                d3 = calc_sq_distance(x4, y4, x3, y3);
            }
            else
            {
                k = 1/k;
                da1 = x2 - x1;
                da2 = y2 - y1;
                d2  = (da1*dx + da2*dy)*k;
                da1 = x3 - x1;
                da2 = y3 - y1;
                d3  = (da1*dx + da2*dy)*k;
                if(d2 > 0 && d2 < (1<<8) && d3 > 0 && d3 < (1<<8))
                {
                    // Simple collinear case, 1---2---3---4
                    // We can leave just two endpoints
                    return;
                }
                     if(d2 <= 0) d2 = calc_sq_distance(x2, y2, x1, y1);
                else if(d2 >= (1<<8)) d2 = calc_sq_distance(x2, y2, x4, y4);
                else             d2 = calc_sq_distance(x2, y2, x1 + d2*dx, y1 + d2*dy);

                     if(d3 <= 0) d3 = calc_sq_distance(x3, y3, x1, y1);
                else if(d3 >= (1<<8)) d3 = calc_sq_distance(x3, y3, x4, y4);
                else             d3 = calc_sq_distance(x3, y3, x1 + d3*dx, y1 + d3*dy);
            }
            if(d2 > d3)
            {
                if(d2 < m_distance_tolerance_square)
                {
                	line_to(ras, (int)x2, (int)y2);
                    return;
                }
            }
            else
            {
                if(d3 < m_distance_tolerance_square)
                {
                	line_to(ras, (int)x3, (int)y3);
                    return;
                }
            }
            break;

        case 1:
            // p1,p2,p4 are collinear, p3 is significant
            //----------------------
            if(d3 * d3 <= m_distance_tolerance_square * (dx*dx + dy*dy))
            {
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, (int)x23, (int)y23);
                    return;
                }

                // Angle Condition
                //----------------------
                da1d = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;

                if(da1d < m_angle_tolerance)
                {
                	line_to(ras, (int)x2, (int)y2);
                	line_to(ras, (int)x3, (int)y3);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, (int)x3, (int)y3);
                        return;
                    }
                }
            }
            break;

        case 2:
            // p1,p3,p4 are collinear, p2 is significant
            //----------------------
            if(d2 * d2 <= m_distance_tolerance_square * (dx*dx + dy*dy))
            {
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, (int)x23, (int)y23);
                    return;
                }

                // Angle Condition
                //----------------------
                da1d = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;

                if(da1d < m_angle_tolerance)
                {
                	line_to(ras, (int)x2, (int)y2);
                	line_to(ras, (int)x3, (int)y3);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, (int)x2, (int)y2);
                        return;
                    }
                }
            }
            break;

        case 3: 
            // Regular case
            //-----------------
        	//int pd = (d2 + d3);
        	//int pd2 = (int)Math.sqrt(m_distance_tolerance_square * (dx*dx + dy*dy));
            //if(pd<=pd2)
            if((d2 + d3)*(d2 + d3) <= m_distance_tolerance_square_d * (dx*dx + dy*dy))
            {
                // If the curvature doesn't exceed the distance_tolerance value
                // we tend to finish subdivisions.
                //----------------------
                if(m_angle_tolerance < curve_angle_tolerance_epsilon)
                {
                	line_to(ras, (int)x23, (int)y23);
                    return;
                }

                // Angle & Cusp Condition
                //----------------------
                kd   = Math.atan2(y3 - y2, x3 - x2);
                da1d = Math.abs(kd - Math.atan2(y2 - y1, x2 - x1));
                da2d = Math.abs(Math.atan2(y4 - y3, x4 - x3) - kd);
                if(da1d >= Math.PI) da1d = 2*Math.PI - da1d;
                if(da2d >= Math.PI) da2d = 2*Math.PI - da2d;

                if(da1d + da2d < m_angle_tolerance)
                {
                    // Finally we can stop the recursion
                    //----------------------
                	line_to(ras, (int)x23, (int)y23);
                    return;
                }

                if(m_cusp_limit != 0.0)
                {
                    if(da1d > m_cusp_limit)
                    {
                    	line_to(ras, (int)x2, (int)y2);
                        return;
                    }

                    if(da2d > m_cusp_limit)
                    {
                    	line_to(ras, (int)x3, (int)y3);
                        return;
                    }
                }
            }
            break;
        }

        // Continue subdivision
        //----------------------
        recursive_bezier(ras, x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1); 
        recursive_bezier(ras, x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1); 
    }

	public void line_to(Rasteriser ras, float x2, float y2)
	{
		line_to(ras,(int)(x2*256),(int)(y2*256));
	}
    
	public void line_to(Rasteriser ras, int x2, int y2)
	{
		if(m_clipping)
		{
			int f2 = 0;
			if(x2 > maxx) 
				f2 = 1;
			if(y2 > maxy) 
				f2 += 2;
			if(x2 < minx)
				f2 += 4;
			if(y2 < miny)
				f2 += 8;

			if((m_f1 & 10) == (f2 & 10) && (m_f1 & 10) != 0)
			{
				// Invisible by Y
				m_x1 = x2;
				m_y1 = y2;
				m_f1 = f2;
				return;
			}

			int x1 = m_x1;
			int y1 = m_y1;
			int f1 = m_f1;
			int y3, y4;
			int f3, f4;

			switch(((f1 & 5) << 1) | (f2 & 5))
			{
			case 0: // Visible by X
				line_clip_y(ras, x1, y1, x2, y2, f1, f2);
				break;

			case 1: // x2 > clip.x2
				y3 = y1 + mul_div(maxx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				line_clip_y(ras, x1, y1, maxx, y3, f1, f3);
				line_clip_y(ras, maxx, y3, maxx, y2, f3, f2);
				break;

			case 2: // x1 > clip.x2
				y3 = y1 + mul_div(maxx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				line_clip_y(ras, maxx, y1, maxx, y3, f1, f3);
				line_clip_y(ras, maxx, y3, x2, y2, f3, f2);
				break;

			case 3: // x1 > clip.x2 && x2 > clip.x2
				line_clip_y(ras, maxx, y1, maxx, y2, f1, f2);
				break;

			case 4: // x2 < clip.x1
				y3 = y1 + mul_div(minx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				line_clip_y(ras, x1, y1, minx, y3, f1, f3);
				line_clip_y(ras, minx, y3, minx, y2, f3, f2);
				break;

			case 6: // x1 > clip.x2 && x2 < clip.x1
				y3 = y1 + mul_div(maxx-x1, y2-y1, x2-x1);
				y4 = y1 + mul_div(minx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				f4 = clipping_flags_y(y4);
				line_clip_y(ras, maxx, y1, maxx, y3, f1, f3);
				line_clip_y(ras, maxx, y3, minx, y4, f3, f4);
				line_clip_y(ras, minx, y4, minx, y2, f4, f2);
				break;

			case 8: // x1 < clip.x1
				y3 = y1 + mul_div(minx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				line_clip_y(ras, minx, y1, minx, y3, f1, f3);
				line_clip_y(ras, minx, y3, x2, y2, f3, f2);
				break;

			case 9:  // x1 < clip.x1 && x2 > clip.x2
				y3 = y1 + mul_div(minx-x1, y2-y1, x2-x1);
				y4 = y1 + mul_div(maxx-x1, y2-y1, x2-x1);
				f3 = clipping_flags_y(y3);
				f4 = clipping_flags_y(y4);
				line_clip_y(ras, minx, y1, minx, y3, f1, f3);
				line_clip_y(ras, minx, y3, maxx, y4, f3, f4);
				line_clip_y(ras, maxx, y4, maxx, y2, f4, f2);
				break;

			case 12: // x1 < clip.x1 && x2 < clip.x1
				line_clip_y(ras, minx, y1, minx, y2, f1, f2);
				break;
			}
			m_f1 = f2;
		}
		else
		{
			ras.line(m_x1, m_y1, 
					x2,   y2); 
		}
		m_x1 = x2;
		m_y1 = y2;
	}

	final void line_clip_y(Rasteriser ras,
			int x1, int y1, 
			int x2, int y2, 
			int f1, int f2)
	{
		f1 &= 10;
		f2 &= 10;
		if((f1 | f2) == 0)
		{
			// Fully visible
			ras.line(x1, y1, x2, y2); 
		}
		else
		{
			if(f1 == f2)
			{
				// Invisible by Y
				return;
			}

			int tx1 = x1;
			int ty1 = y1;
			int tx2 = x2;
			int ty2 = y2;

			if((f1 & 8)!=0) // y1 < clip.y1
			{
				tx1 = x1 + mul_div(miny-y1, x2-x1, y2-y1);
				ty1 = miny;
			}

			if((f1 & 2)!=0) // y1 > clip.y2
			{
				tx1 = x1 + mul_div(maxy-y1, x2-x1, y2-y1);
				ty1 = maxy;
			}

			if((f2 & 8)!=0) // y2 < clip.y1
			{
				tx2 = x1 + mul_div(miny-y1, x2-x1, y2-y1);
				ty2 = miny;
			}

			if((f2 & 2)!=0) // y2 > clip.y2
			{
				tx2 = x1 + mul_div(maxy-y1, x2-x1, y2-y1);
				ty2 = maxy;
			}
			ras.line((tx1), (ty1), 
					(tx2), (ty2)); 
		}
	}

}

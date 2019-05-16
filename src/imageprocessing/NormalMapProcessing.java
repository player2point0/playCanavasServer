package imageprocessing;

public class NormalMapProcessing 
{
	public static void GradMagnitude(int[] pixel,int width,int height,int[] pixelo)
	{
        double maxsqrt = Math.sqrt(256.0*256.0*2.0);
        int i = 0;
        int r,g,b;
        for(int yi=0;yi<width;yi++)
        {
            for(int xi=0;xi<height;xi++)
            {
				r = (pixel[i]&0xFF0000)>>16;
				g = (pixel[i]&0x00FF00)>>8;
				b = (pixel[i]&0x0000FF);
				
                int magx = g-128;
                int magy = b-128;
                int mag = magx*magx+magy*magy;
                mag = (int)(255.0*Math.sqrt((double)mag)/maxsqrt);
                mag=(mag>0)?((mag<0xFF)?mag:0xFF):0;

                pixelo[i] = 0xFF000000|(mag<<16)|(mag<<8)|mag;
				i++;
            }
        }
		
	}

	public static void CurvatureMag(int[] pixel,int width,int height,int[] pixelo,float scale)
	{
        double maxsqrt = Math.sqrt(256.0*256.0*2.0*scale);
		int nx = 0;
		int ny = 0;
		int i=0;
        int g10,g12,b01,b21;
		for (int y=1;y<height-1;y++)
		{
			for (int x=1;x<width-1;x++)
			{
				i = x+y*width; 
				b01 = (pixel[i-1]&0x0000FF);
				b21 = (pixel[i+1]&0x0000FF);
				g10 = (pixel[i-width]&0x0000FF);
				g12 = (pixel[i+width]&0x0000FF);

				nx=(b21-b01);
				ny=(g12-g10);
                int mag = nx*nx+ny*ny;
                mag = (int)(255.0*Math.sqrt((double)mag)/maxsqrt);
                mag=(mag>0)?((mag<0xFF)?mag:0xFF):0;
				
                pixelo[i] = 0xFF000000|(mag<<16)|(mag<<8)|mag;
			}
		}		
	}

	public static void CurvatureGrad(int[] pixel,int width,int height,int[] pixelo,float scale)
	{
        double maxsqrt = Math.sqrt(256.0*256.0*2.0*scale);
		int nx = 0;
		int ny = 0;
		int i=0;
        int g10,g12,b01,b21;
		for (int y=1;y<height-1;y++)
		{
			for (int x=1;x<width-1;x++)
			{
				i = x+y*width; 
				b01 = (pixel[i-1]&0x0000FF);
				b21 = (pixel[i+1]&0x0000FF);
				g10 = (pixel[i-width]&0x0000FF);
				g12 = (pixel[i+width]&0x0000FF);

				nx=(b21-b01);
				ny=(g12-g10);
                nx=(nx>0)?((nx<0xFF)?nx:0xFF):0;
                ny=(ny>0)?((ny<0xFF)?ny:0xFF):0;
                int mag = nx*nx+ny*ny;
                mag = (int)(255.0*Math.sqrt((double)mag)/maxsqrt);
                mag=(mag>0)?((mag<0xFF)?mag:0xFF):0;
				
                pixelo[i] = (mag<<16)|(ny<<8)|nx;
			}
		}		
	}
	
}

package registration;

import imageprocessing.ImageDifference;

import java.util.Random;

import camera.Camera;
import rendering.RenderBuffer;
import mathematics.GeneralMatrixDouble;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.linearalgebra.SVD;
import mathematics.linearalgebra.SVDDouble;
import mathematics.statistics.Outliers;

public class Homography 
{	
	public static void main(String[] list) throws Exception 
    {
		GeneralMatrixFloat testhomography = new GeneralMatrixFloat(3,3);
		testhomography.setIdentity();
		
		//testhomography.value[7] = 0.0f;

		testhomography.value[0]=0.29352474f;
		testhomography.value[1]=-0.198885f;
		testhomography.value[2]=169.15305f;
		testhomography.value[3]=-0.15583168f;
		testhomography.value[4]=0.060560532f;
		testhomography.value[5]=217.96257f;
		testhomography.value[6]=-1.5196392E-4f;
		testhomography.value[7]=-7.9124555E-4f;
		testhomography.value[8]=1.0f;
		
		float testPanelWidth = 0.5f;

		Camera c = new Camera();
		
		float zNear = 0.005f;
		float zFar = 100.0f;
    	c.zNear = zNear;
    	c.zFar = zFar;

    	c.width = 512;
    	c.height = 512;

    	Homography.calculateCamera_orig(testhomography, 512, 512, testPanelWidth, c);
    }
	
	//What position on the calibration image takes a point to the origin
	public static void calculateCalibrationPositionOfTransformedOrigin(GeneralMatrixFloat uTox,GeneralMatrixFloat u)
	{
//		x' = (h1x+h2y+h3)/(h7x+h8y+1.0)
//		y' = (h4x+h5y+h6)/(h7x+h8y+1.0)
//		0 = (h1x+h2y+h3)
//		0 = (h4x+h5y+h6)
//
//		-h3 = h1x+h2y
//		-h6 = h4x+h5y
//
//		-(h2y+h3)/h1 = x
//		-h6 = -(h2y+h3)*h4/h1+h5y
		//-1.0 = -(0+0)+
//		-h6 = -((h2*h4/h1)+h5)y-(h3*h4/h1)
//
//		y = (h6-(h3*h4/h1))/((h2*h4/h1)+h5)

		float h1 = uTox.value[0];
		float h2 = uTox.value[1];
		float h3 = uTox.value[2];
		float h4 = uTox.value[3];
		float h5 = uTox.value[4];
		float h6 = uTox.value[5];
		
		u.value[1] = h6*h1-(h3*h4);
		u.value[1] /= (h2*h4)+h5*h1;			
//		if(Math.abs(h1)<0.00001f)
//		{
//			u.value[1] = h6*h1-(h3*h4);
//			u.value[1] /= (h2*h4)+h5*h1;			
//		}
//		else
//		{
//			u.value[1] = h6-(h3*h4/h1);
//			u.value[1] /= (h2*h4/h1)+h5;
//		}
		
		float y = u.value[1]; 
		float x = -(h2*y+h3)/(h1);

		u.value[0] = x;

		float y2 = -y;
		float x2 = -(h2*y2+h3)/(h1);
		
		float vy = (uTox.value[3]*x+uTox.value[4]*y+uTox.value[5])/(uTox.value[6]*x+uTox.value[7]*y+1.0f);
		
		if(Math.abs(vy)>0.00000001f)
		{
			u.value[0] = x2;
			u.value[1] = y2;
		}
		vy = (uTox.value[3]*u.value[0]+uTox.value[4]*u.value[1]+uTox.value[5])/(uTox.value[6]*u.value[0]+uTox.value[7]*u.value[1]+1.0f);
		float vx = (uTox.value[0]*u.value[0]+uTox.value[1]*u.value[1]+uTox.value[2]);
		vx /= (uTox.value[6]*u.value[0]+uTox.value[7]*u.value[1]+1.0f);
		
//		System.out.println("vx="+vx+"\tvy="+vy);
	}
	
	public static void calculateCamera(GeneralMatrixFloat uTox,
			//int calibwidth,int calibheight,
			//float imagewidth,
			GeneralMatrixFloat camd,GeneralMatrixFloat camm,GeneralMatrixFloat camp)
	{
		float cam_width = camd.value[0];
		float cam_height = camd.value[1];
		//Then create the post translation that puts the points relative to the camera axis
		GeneralMatrixFloat xToCameraRel = new GeneralMatrixFloat(3,3);
		xToCameraRel.setIdentity();
		xToCameraRel.value[2] = -cam_width*0.5f;
		xToCameraRel.value[5] = -cam_height*0.5f;

		uTox.width = 3;
		uTox.height = 3;

		GeneralMatrixFloat normuTox = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat.mult(xToCameraRel, uTox, normuTox);

		//normuTox.setIdentity();
		//normuTox.value[6] = 1000.0f;

		//First calculate f (assuming t3 = -1) this is correct to a scale factor in f

		//x' = (h1x+h2y+h3)/(h7x+h8y+1.0)
		//y' = (h4x+h5y+h6)/(h7x+h8y+1.0)
		//
		//x' = (m1x+m4y+t1)*f/(-(m3x+m6y+t3))
		//y' = (m2x+m5y+t2)*f/(-(m3x+m6y+t3))
		//
		//m1 = h1/f
		//m2 = h4/f
		//m4 = h2/f
		//n5 = h5/f
		//
		//a)m1*m4+m2*m5+m3*m6=0.0
		//b)n1*n1+n2*n2+n3*n3=1.0
		//c)n4*n4+n5*n5+n6*n6=1.0
		//
		//b)(h1*h1+g4*g4)/f*f+n3*n3=1.0
		//(h1*h1+g4*g4)+n3*n3*f*f=f*f
		//n3 = (sqrt(f*f-(h1*h1+g4*g4)))/f
		//
		//c)(g2*g2+h5*h5)/f*f+n6*n6=1.0
		//(g2*g2+h5*h5)+n6*n6*f*f=f*f
		//n6 = (sqrt(f*f-(g2*g2+h5*h5)))/f
		//
		//a)h1*g2+g4*h5+(sqrt(f*f-(g2*g2+h5*h5)))*(sqrt(f*f-(h1*h1+g4*g4)))=0.0
		//(h1*g2+g4*h5)*(h1*g2+g4*h5) = (f*f-(g2*g2+h5*h5))*(f*f-(h1*h1+g4*g4))

		normuTox.scale(1.0f/normuTox.value[8]);

		double h1 = normuTox.value[0];
		double h2 = normuTox.value[1];
		double h4 = normuTox.value[3];
		double h5 = normuTox.value[4];

		double h3 = normuTox.value[2];
		double h6 = normuTox.value[5];

		double h7 = normuTox.value[6];
		double h8 = normuTox.value[7];



		//solve ax^2+bx+c
		//(-b +-sqrt(b^2-4ac))/2a;
		double a = 1.0f;
		double b = -(h2*h2+h5*h5+h1*h1+h4*h4); 
		double c = (h1*h1+h4*h4)*(h2*h2+h5*h5);
		double cmin = (h1*h2+h4*h5)*(h1*h2+h4*h5);
		c -= cmin;
		double minb = -b/2*a;
		double tosqrt = b*b-4*a*c;
		double plusminus = 0.0;
		if(tosqrt>0.0)
			plusminus = (double)Math.sqrt(tosqrt)/2*a;
		double f1 = Math.sqrt(minb+plusminus);
		double f2 = Math.sqrt(minb-plusminus);

		//double vala = h1*h2+h4*h5+(Math.sqrt(f1*f1-(h2*h2+h5*h5)))*(Math.sqrt(f1*f1-(h1*h1+h4*h4)));
		//double vala2 = h1*h2+h4*h5+(Math.sqrt(f2*f2-(h2*h2+h5*h5)))*(Math.sqrt(f2*f2-(h1*h1+h4*h4)));

		double f = (double)f1;
		double m1 = h1/f;
		double m2 = h4/f;
		double m3 = 0.0f;
		double m3sqr = 1.0f-m1*m1-m2*m2;
		if(m3sqr>0.0f)
			m3 = (double)Math.sqrt(m3sqr);

		if(h7>0.0f)
			m3 = -m3;

		double m4 = h2/f;
		double m5 = h5/f;
		double m6 = 0.0f;
		double m6sqr = 1.0f-m4*m4-m5*m5;
		if(m6sqr>0.0f)
			m6 = (double)Math.sqrt(m6sqr);

		if(h8>0.0f)
			m6 = -m6;

		double m12 = (double)(h1/f2);
		double m22 = (double)(h4/f2);
		double m32 = 0.0f;
		double m32sqr = 1.0f-m12*m12-m22*m22;
		if(m32sqr>0.0f)
			m32 = (double)Math.sqrt(m32sqr);

		if(h7>0.0f)
			m32 = -m32;

		double m42 = (double)(h2/f2);
		double m52 = (double)(h5/f2);
		double m62 = 0.0f;
		double m62sqr = 1.0f-m42*m42-m52*m52;
		if(m62sqr>0.0f)
			m62 = (double)Math.sqrt(m62sqr);

		if(h8>0.0f)
			m62 = -m62;

		double dotv = m1*m4+m2*m5+m3*m6;
		double dotr1 = m1*m1+m2*m2+m3*m3;
		double dotr2 = m4*m4+m5*m5+m6*m6;

		double dotv2 = m12*m42+m22*m52+m32*m62;
		double dotr12 = m12*m12+m22*m22+m32*m32;
		double dotr22 = m42*m42+m52*m52+m62*m62;

		double err1 = Math.abs(dotv)+Math.abs(dotr1-1.0f)+Math.abs(dotr2-1.0f);
		double err2 = Math.abs(dotv2)+Math.abs(dotr12-1.0f)+Math.abs(dotr22-1.0f);

		if(err2<err1)
		{
			f = (double)f2;
			m1 = m12;
			m2 = m22;
			m3 = m32;
			m4 = m42;
			m5 = m52;
			m6 = m62;
		}

		//xrow cross yrow produces zrow
		double m7 = m2*m6-m3*m5;
		double m8 = m3*m4-m1*m6;
		double m9 = m1*m5-m2*m4;

		//Now we can calculate the correct t1 and t2 from the transformed to origin values
		//GeneralMatrixdouble toxOrigin = new GeneralMatrixdouble(2,1);
		//calculateCalibrationPositionOfTransformedOrigin(normuTox, toxOrigin);

		//from these we know that the only way to get to the origin is if
		//m1x+m4y+t1 = 0
		//m2x+m5y+t2 = 0

		double t1 = h3/f;
		double t2 = h6/f;

		double zsqrscale = Math.sqrt(h7*h7+h8*h8);
		double czsqrscale = Math.sqrt(m3*m3+m6*m6);

		double t3 = -1.0f;
		if((zsqrscale>0.000001)&&(czsqrscale>0.000001))
		{
			t3 = -(double)(czsqrscale/zsqrscale);
		}

		//best f now maintains the scale of the transform for the image extremities
		//x' = (m1x+m4y+t1)*f/(-(m3x+m6y+t3))
		//y' = (m2x+m5y+t2)*f/(-(m3x+m6y+t3))
		double xcorner = 0.25f;//imagewidth*0.5f;
		double ycorner = 0.25f;//imagewidth*0.5f*calibheight/(double)calibheight;
		double tl = Math.abs((m1*-xcorner+m4*-ycorner+t1)*f/(-(m3*-xcorner+m6*-ycorner+t3)));
		double tr = Math.abs((m1*xcorner+m4*-ycorner+t1)*f/(-(m3*xcorner+m6*-ycorner+t3)));
		double bl = Math.abs((m1*-xcorner+m4*ycorner+t1)*f/(-(m3*-xcorner+m6*ycorner+t3)));
		double br = Math.abs((m1*xcorner+m4*ycorner+t1)*f/(-(m3*xcorner+m6*ycorner+t3)));

		double tly = Math.abs((m2*-xcorner+m5*-ycorner+t2)*f/(-(m3*-xcorner+m6*-ycorner+t3)));
		double tr_y = Math.abs((m2*xcorner+m5*-ycorner+t2)*f/(-(m3*xcorner+m6*-ycorner+t3)));
		double bly = Math.abs((m2*-xcorner+m5*ycorner+t2)*f/(-(m3*-xcorner+m6*ycorner+t3)));
		double bry = Math.abs((m2*xcorner+m5*ycorner+t2)*f/(-(m3*xcorner+m6*ycorner+t3)));

		//x' = (h1x+h2y+h3)/(h7x+h8y+1.0)
		//y' = (h4x+h5y+h6)/(h7x+h8y+1.0)
		double htl = Math.abs((h1*-xcorner+h2*-ycorner+h3)/((h7*-xcorner+h8*-ycorner+1.0f)));
		double htr = Math.abs((h1*xcorner+h2*-ycorner+h3)/((h7*xcorner+h8*-ycorner+1.0f)));
		double hbl = Math.abs((h1*-xcorner+h2*ycorner+h3)/((h7*-xcorner+h8*ycorner+1.0f)));
		double hbr = Math.abs((h1*xcorner+h2*ycorner+h3)/((h7*xcorner+h8*ycorner+1.0f)));

		double htly = Math.abs((h4*-xcorner+h5*-ycorner+h6)/((h7*-xcorner+h8*-ycorner+1.0f)));
		double htry = Math.abs((h4*xcorner+h5*-ycorner+h6)/((h7*xcorner+h8*-ycorner+1.0f)));
		double hbly = Math.abs((h4*-xcorner+h5*ycorner+h6)/((h7*-xcorner+h8*ycorner+1.0f)));
		double hbry = Math.abs((h4*xcorner+h5*ycorner+h6)/((h7*xcorner+h8*ycorner+1.0f)));

		int numvalid = 0;
		double avgscale = 0.0f;

		if(tl>0.0000001f)
		{
			avgscale += htl/tl;
			numvalid++;
		}
		if(tr>0.0000001f)
		{
			avgscale += htr/tr;
			numvalid++;
		}
		if(bl>0.0000001f)
		{
			avgscale += hbl/bl;
			numvalid++;
		}
		if(br>0.0000001f)
		{
			avgscale += hbr/br;
			numvalid++;
		}		
		if(tly>0.0000001f)
		{
			avgscale += htly/tly;
			numvalid++;
		}
		if(tr_y>0.0000001f)
		{
			avgscale += htry/tr_y;
			numvalid++;
		}
		if(bly>0.0000001f)
		{
			avgscale += hbly/bly;
			numvalid++;
		}
		if(bry>0.0000001f)
		{
			avgscale += hbry/bry;
			numvalid++;
		}		

		if(numvalid>0)
		{
			avgscale /= numvalid;
			f *= avgscale;
		}

		//Now we need to calculate the scaled f and the tz value
		//at x=1 y=0
		//(m1+t1)*f/(-(m3+t3)) = (h1+h3)/(h7+1.0)
		//at x=0 y=1
		//(m2+t2)*f/(-(m6+t3)) = (h5+h6)/(h8+1.0)

		//f = -(h1+h3)*(m3+t3)/((h7+1.0)*(m1+t1))
		//f = -(h5+h6)*(m6+t3)/((h8+1.0)*(m2+t2))

		//(h1+h3)*(m3+t3)/((h7+1.0)*(m1+t1)) = (h5+h6)*(m6+t3)/((h8+1.0)*(m2+t2))
		//(m3+t3)*(h1+h3)*((h8+1.0)*(m2+t2)) = (m6+t3)*(h5+h6)*((h7+1.0)*(m1+t1))
		//m3*(h1+h3)*((h8+1.0)*(m2+t2))-m6*(h5+h6)*((h7+1.0)*(m1+t1)) =
		//t3*((h5+h6)*((h7+1.0)*(m1+t1))-(h1+h3)*((h8+1.0)*(m2+t2)))
		//t3 = m3*(h1+h3)*((h8+1.0)*(m2+t2))-m6*(h5+h6)*((h7+1.0)*(m1+t1))/
		//((h5+h6)*((h7+1.0)*(m1+t1))-(h1+h3)*((h8+1.0)*(m2+t2)))
		//float t3 = m3*(h1+h3)*((h8+1.0f)*(m2+t2))-m6*(h5+h6)*((h7+1.0f)*(m1+t1));
		//if(Math.abs(t3)<0.0000001f)
		//{
		////Handle the ambiguous case (no tilt in the homography so can't tell what f should be)
		//t3 = 1.0f;
		//}
		//else
		//{
		//t3 /= ((h5+h6)*((h7+1.0f)*(m1+t1))-(h1+h3)*((h8+1.0f)*(m2+t2)));
		//}
		//f = -(h1+h3)*(m3+t3)/((h7+1.0f)*(m1+t1));

		camm.value[0] = (float)m1; 
		camm.value[1] = (float)m2;		
		camm.value[2] = (float)m3;
		camm.value[3] = (float)0.0f;

		camm.value[4] = (float)m4;
		camm.value[5] = (float)m5;
		camm.value[6] = (float)m6;
		camm.value[7] = (float)0.0f;

		camm.value[8] = (float)m7;
		camm.value[9] = (float)m8;
		camm.value[10] = (float)m9;
		camm.value[11] = (float)0.0f;

		camm.value[12] = (float)t1;
		camm.value[13] = (float)t2;
		camm.value[14] = (float)t3;
		camm.value[15] = (float)1.0f;

		double zFar = 100.0;
		double zNear = 0.001;

		double deltaZ = zFar - zNear;

		/* convert from world coordinates to camera coordinates */
		/* Note: zw is implicitly assumed to be zero for these (coplanar) calculations */
		camp.value[0] = (float)(f*2.0/(cam_width));
		camp.value[1] = (float)0.0f;
		camp.value[2] = (float)0.0f;
		camp.value[3] = (float)0.0f;

		camp.value[4] = (float)0.0f;
		camp.value[5] = (float)(f*2.0/(cam_height));
		camp.value[6] = (float)0.0f;
		camp.value[7] = (float)0.0f;

		camp.value[8] = (float)0.0f;
		camp.value[9] = (float)0.0f;
		camp.value[10] = (float)(-(zFar + zNear) / deltaZ);
		camp.value[11] = (float)-1.0f;

		camp.value[12] = (float)0.0f;
		camp.value[13] = (float)0.0f;
		camp.value[14] = (float)(-2.0 * zNear * zFar / deltaZ);
		camp.value[15] = (float)0.0f;

		//clear the centre pixel offset
		camd.value[2] = 0.0f;
		camd.value[3] = 0.0f;

		camd.value[4] = 1.0f;
		camd.value[5] = 0.0f;
		camd.value[6] = 0.0f;
		
		GeneralMatrixFloat comparepoint = new GeneralMatrixFloat(3,1);
		//GeneralMatrixFloat cameraProjectedPoint = new GeneralMatrixFloat(3,1);


		float x,y;
		float fix,fiy,fih;

	    double[] postProj = new double[4];
	    double[] postModel = new double[3];
		
		//Pixel position
		x = 0.0f;
		y = 0.0f;
		for(int i=0;i<4;i++)
		{
			switch(i)
			{
			case 0:
				x = 0.0f;
				y = 0.0f;
				break;
			case 1:
				//position in world coords
				x = 0.5f;
				y = 0.5f;
				break;
			case 2:
				x = 1.0f;
				y = 0.0f;
				break;
			case 3:
				x = 0.0f;
				y = 1.0f;
				break;
			}
			fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
			fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
			fih = (uTox.value[8]+(x)*(uTox.value[6])+(y)*(uTox.value[7]));
			fix/=fih;
			fiy/=fih;

			//Convert to world position
			comparepoint.value[0] = x;
			comparepoint.value[1] = y;
			comparepoint.value[2] = 0.0f;

			for (i=0; i<3; i++) 
			{
				postModel[i] = 
						comparepoint.value[0] * camm.value[0*4+i] +
				comparepoint.value[1] * camm.value[1*4+i] +
				comparepoint.value[2] * camm.value[2*4+i] +
				1.0f * camm.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * camp.value[0*4+i] +
				postModel[1] * camp.value[1*4+i] +
				postModel[2] * camp.value[2*4+i] +
				1.0f * camp.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    //Debug for 
		    if(
		    		(postProj[0]<-1.0f)||(postProj[0]>1.0f)||
		    		(postProj[1]<-1.0f)||(postProj[1]>1.0f)||
		    		(postProj[2]<-1.0f)||(postProj[2]>1.0f)
		      )
		    {
		    	continue;
		    }
		    	
		    double aspect = cam_width/(double)cam_height;
			double sensorscalex = postProj[0]*aspect;
			double sensorscaley = postProj[1];
		    
			double R2 = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
			double R4 = R2*R2;
		    
			double scale = 1.0;

//		    scale += k1*R2+k2*R4;
//		    
//		    postProj[0] *= scale*sx;
//		    postProj[1] *= scale;

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    //sp.value[2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    double cameraProjectedPointx = (postProj[0] * cam_width);//+cx;
		    double cameraProjectedPointy = (postProj[1] * cam_height);//+cy;

			float fix2 = (normuTox.value[2]+(x)*(normuTox.value[0])+(y)*(normuTox.value[1]));
			float fiy2 = (normuTox.value[5]+(x)*(normuTox.value[3])+(y)*(normuTox.value[4]));
			float fih2 = (normuTox.value[8]+(x)*(normuTox.value[6])+(y)*(normuTox.value[7]));
			fix2/=fih2;
			fiy2/=fih2;

			fix2 += cam_width*0.5f;
			fiy2 += cam_height*0.5f;

			double dx = fix-cameraProjectedPointx;
			double dy = fiy-cameraProjectedPointy;
			//System.out.println("dx="+dx+"\tdy="+dy);

			double dx2 = fix2-cameraProjectedPointx;
			double dy2 = fiy2-cameraProjectedPointy;
			//System.out.println("dx2="+dx2+"\tdy2="+dy2);
		}

		//for(int i=0;i<9;i++)
		//System.out.println("testhomography.value["+i+"]="+uTox.value[i]+"f;");
	}


	
	public static void calculateCamera_orig(GeneralMatrixFloat uTox,
										int calibwidth,int calibheight,
										float imagewidth,
										Camera camera)
	{
		//First create the full homography mapping (from world positions of the plane input to pixel values)
		float planescale = (calibwidth-1)/imagewidth;
		GeneralMatrixFloat planeToPixel = new GeneralMatrixFloat(3,3);
		planeToPixel.setIdentity();
		planeToPixel.value[0] *= planescale;
		planeToPixel.value[4] *= -planescale;
		planeToPixel.value[2] = +(calibwidth-1)*0.5f;
		planeToPixel.value[5] = +(calibheight-1)*0.5f;
		
		//Then create the post translation that puts the points relative to the camera axis
		GeneralMatrixFloat xToCameraRel = new GeneralMatrixFloat(3,3);
		xToCameraRel.setIdentity();
		xToCameraRel.value[2] = -camera.width*0.5f;
		xToCameraRel.value[5] = -camera.height*0.5f;

		uTox.width = 3;
		uTox.height = 3;
		
		GeneralMatrixFloat temp = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat normuTox = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat.mult(uTox, planeToPixel, temp);
		GeneralMatrixFloat.mult(xToCameraRel, temp, normuTox);
		
//		normuTox.setIdentity();
//		normuTox.value[6] = 1000.0f;
		
		//First calculate f (assuming t3 = -1) this is correct to a scale factor in f
		
//		x' = (h1x+h2y+h3)/(h7x+h8y+1.0)
//		y' = (h4x+h5y+h6)/(h7x+h8y+1.0)
//
//		x' = (m1x+m4y+t1)*f/(-(m3x+m6y+t3))
//		y' = (m2x+m5y+t2)*f/(-(m3x+m6y+t3))
//
//		m1 = h1/f
//		m2 = h4/f
//		m4 = h2/f
//		n5 = h5/f
//
//		a)m1*m4+m2*m5+m3*m6=0.0
//		b)n1*n1+n2*n2+n3*n3=1.0
//		c)n4*n4+n5*n5+n6*n6=1.0
//
//		b)(h1*h1+g4*g4)/f*f+n3*n3=1.0
//		(h1*h1+g4*g4)+n3*n3*f*f=f*f
//		n3 = (sqrt(f*f-(h1*h1+g4*g4)))/f
//
//		c)(g2*g2+h5*h5)/f*f+n6*n6=1.0
//		(g2*g2+h5*h5)+n6*n6*f*f=f*f
//		n6 = (sqrt(f*f-(g2*g2+h5*h5)))/f
//
//		a)h1*g2+g4*h5+(sqrt(f*f-(g2*g2+h5*h5)))*(sqrt(f*f-(h1*h1+g4*g4)))=0.0
//		(h1*g2+g4*h5)*(h1*g2+g4*h5) = (f*f-(g2*g2+h5*h5))*(f*f-(h1*h1+g4*g4))

		normuTox.scale(1.0f/normuTox.value[8]);
		
		double h1 = normuTox.value[0];
		double h2 = normuTox.value[1];
		double h4 = normuTox.value[3];
		double h5 = normuTox.value[4];

		double h3 = normuTox.value[2];
		double h6 = normuTox.value[5];
		
		double h7 = normuTox.value[6];
		double h8 = normuTox.value[7];

		
		
		//solve ax^2+bx+c
		//(-b +-sqrt(b^2-4ac))/2a;
		double a = 1.0f;
		double b = -(h2*h2+h5*h5+h1*h1+h4*h4); 
		double c = (h1*h1+h4*h4)*(h2*h2+h5*h5);
		double cmin = (h1*h2+h4*h5)*(h1*h2+h4*h5);
		c -= cmin;
		double minb = -b/2*a;
		double tosqrt = b*b-4*a*c;
		double plusminus = 0.0;
		if(tosqrt>0.0)
			plusminus = (double)Math.sqrt(tosqrt)/2*a;
		double f1 = Math.sqrt(minb+plusminus);
		double f2 = Math.sqrt(minb-plusminus);
				
//		double vala = h1*h2+h4*h5+(Math.sqrt(f1*f1-(h2*h2+h5*h5)))*(Math.sqrt(f1*f1-(h1*h1+h4*h4)));
//		double vala2 = h1*h2+h4*h5+(Math.sqrt(f2*f2-(h2*h2+h5*h5)))*(Math.sqrt(f2*f2-(h1*h1+h4*h4)));
		
		double f = (double)f1;
		double m1 = h1/f;
		double m2 = h4/f;
		double m3 = 0.0f;
		double m3sqr = 1.0f-m1*m1-m2*m2;
		if(m3sqr>0.0f)
			m3 = (double)Math.sqrt(m3sqr);

		if(h7>0.0f)
			m3 = -m3;
		
		double m4 = h2/f;
		double m5 = h5/f;
		double m6 = 0.0f;
		double m6sqr = 1.0f-m4*m4-m5*m5;
		if(m6sqr>0.0f)
			m6 = (double)Math.sqrt(m6sqr);

		if(h8>0.0f)
			m6 = -m6;
		
		double m12 = (double)(h1/f2);
		double m22 = (double)(h4/f2);
		double m32 = 0.0f;
		double m32sqr = 1.0f-m12*m12-m22*m22;
		if(m32sqr>0.0f)
			m32 = (double)Math.sqrt(m32sqr);

		if(h7>0.0f)
			m32 = -m32;
		
		double m42 = (double)(h2/f2);
		double m52 = (double)(h5/f2);
		double m62 = 0.0f;
		double m62sqr = 1.0f-m42*m42-m52*m52;
		if(m62sqr>0.0f)
			m62 = (double)Math.sqrt(m62sqr);

		if(h8>0.0f)
			m62 = -m62;
		
		double dotv = m1*m4+m2*m5+m3*m6;
		double dotr1 = m1*m1+m2*m2+m3*m3;
		double dotr2 = m4*m4+m5*m5+m6*m6;
		
		double dotv2 = m12*m42+m22*m52+m32*m62;
		double dotr12 = m12*m12+m22*m22+m32*m32;
		double dotr22 = m42*m42+m52*m52+m62*m62;
		
		double err1 = Math.abs(dotv)+Math.abs(dotr1-1.0f)+Math.abs(dotr2-1.0f);
		double err2 = Math.abs(dotv2)+Math.abs(dotr12-1.0f)+Math.abs(dotr22-1.0f);
		
		if(err2<err1)
		{
			f = (double)f2;
			m1 = m12;
			m2 = m22;
			m3 = m32;
			m4 = m42;
			m5 = m52;
			m6 = m62;
		}
		
		//xrow cross yrow produces zrow
		double m7 = m2*m6-m3*m5;
		double m8 = m3*m4-m1*m6;
		double m9 = m1*m5-m2*m4;
		
		//Now we can calculate the correct t1 and t2 from the transformed to origin values
//		GeneralMatrixdouble toxOrigin = new GeneralMatrixdouble(2,1);
//		calculateCalibrationPositionOfTransformedOrigin(normuTox, toxOrigin);
		
		//from these we know that the only way to get to the origin is if
		//m1x+m4y+t1 = 0
		//m2x+m5y+t2 = 0
		
		double t1 = h3/f;
		double t2 = h6/f;
		
		double zsqrscale = Math.sqrt(h7*h7+h8*h8);
		double czsqrscale = Math.sqrt(m3*m3+m6*m6);

		double t3 = -1.0f;
		if((zsqrscale>0.000001)&&(czsqrscale>0.000001))
		{
			t3 = -(double)(czsqrscale/zsqrscale);
		}

		//best f now maintains the scale of the transform for the image extremities
//		x' = (m1x+m4y+t1)*f/(-(m3x+m6y+t3))
//		y' = (m2x+m5y+t2)*f/(-(m3x+m6y+t3))
		double xcorner = imagewidth*0.5f;
		double ycorner = imagewidth*0.5f*calibheight/(double)calibheight;
		double tl = Math.abs((m1*-xcorner+m4*-ycorner+t1)*f/(-(m3*-xcorner+m6*-ycorner+t3)));
		double tr = Math.abs((m1*xcorner+m4*-ycorner+t1)*f/(-(m3*xcorner+m6*-ycorner+t3)));
		double bl = Math.abs((m1*-xcorner+m4*ycorner+t1)*f/(-(m3*-xcorner+m6*ycorner+t3)));
		double br = Math.abs((m1*xcorner+m4*ycorner+t1)*f/(-(m3*xcorner+m6*ycorner+t3)));

		double tly = Math.abs((m2*-xcorner+m5*-ycorner+t2)*f/(-(m3*-xcorner+m6*-ycorner+t3)));
		double tr_y = Math.abs((m2*xcorner+m5*-ycorner+t2)*f/(-(m3*xcorner+m6*-ycorner+t3)));
		double bly = Math.abs((m2*-xcorner+m5*ycorner+t2)*f/(-(m3*-xcorner+m6*ycorner+t3)));
		double bry = Math.abs((m2*xcorner+m5*ycorner+t2)*f/(-(m3*xcorner+m6*ycorner+t3)));
		
//		x' = (h1x+h2y+h3)/(h7x+h8y+1.0)
//		y' = (h4x+h5y+h6)/(h7x+h8y+1.0)
		double htl = Math.abs((h1*-xcorner+h2*-ycorner+h3)/((h7*-xcorner+h8*-ycorner+1.0f)));
		double htr = Math.abs((h1*xcorner+h2*-ycorner+h3)/((h7*xcorner+h8*-ycorner+1.0f)));
		double hbl = Math.abs((h1*-xcorner+h2*ycorner+h3)/((h7*-xcorner+h8*ycorner+1.0f)));
		double hbr = Math.abs((h1*xcorner+h2*ycorner+h3)/((h7*xcorner+h8*ycorner+1.0f)));

		double htly = Math.abs((h4*-xcorner+h5*-ycorner+h6)/((h7*-xcorner+h8*-ycorner+1.0f)));
		double htry = Math.abs((h4*xcorner+h5*-ycorner+h6)/((h7*xcorner+h8*-ycorner+1.0f)));
		double hbly = Math.abs((h4*-xcorner+h5*ycorner+h6)/((h7*-xcorner+h8*ycorner+1.0f)));
		double hbry = Math.abs((h4*xcorner+h5*ycorner+h6)/((h7*xcorner+h8*ycorner+1.0f)));
		
		int numvalid = 0;
		double avgscale = 0.0f;
		
		if(tl>0.0000001f)
		{
			avgscale += htl/tl;
			numvalid++;
		}
		if(tr>0.0000001f)
		{
			avgscale += htr/tr;
			numvalid++;
		}
		if(bl>0.0000001f)
		{
			avgscale += hbl/bl;
			numvalid++;
		}
		if(br>0.0000001f)
		{
			avgscale += hbr/br;
			numvalid++;
		}		
		if(tly>0.0000001f)
		{
			avgscale += htly/tly;
			numvalid++;
		}
		if(tr_y>0.0000001f)
		{
			avgscale += htry/tr_y;
			numvalid++;
		}
		if(bly>0.0000001f)
		{
			avgscale += hbly/bly;
			numvalid++;
		}
		if(bry>0.0000001f)
		{
			avgscale += hbry/bry;
			numvalid++;
		}		
		
		if(numvalid>0)
		{
			avgscale /= numvalid;
			f *= avgscale;
		}
		
		//Now we need to calculate the scaled f and the tz value
		//at x=1 y=0
		//(m1+t1)*f/(-(m3+t3)) = (h1+h3)/(h7+1.0)
		//at x=0 y=1
		//(m2+t2)*f/(-(m6+t3)) = (h5+h6)/(h8+1.0)

		//f = -(h1+h3)*(m3+t3)/((h7+1.0)*(m1+t1))
		//f = -(h5+h6)*(m6+t3)/((h8+1.0)*(m2+t2))
		
		//(h1+h3)*(m3+t3)/((h7+1.0)*(m1+t1)) = (h5+h6)*(m6+t3)/((h8+1.0)*(m2+t2))
		//(m3+t3)*(h1+h3)*((h8+1.0)*(m2+t2)) = (m6+t3)*(h5+h6)*((h7+1.0)*(m1+t1))
		//m3*(h1+h3)*((h8+1.0)*(m2+t2))-m6*(h5+h6)*((h7+1.0)*(m1+t1)) =
		//t3*((h5+h6)*((h7+1.0)*(m1+t1))-(h1+h3)*((h8+1.0)*(m2+t2)))
		//t3 = m3*(h1+h3)*((h8+1.0)*(m2+t2))-m6*(h5+h6)*((h7+1.0)*(m1+t1))/
			//((h5+h6)*((h7+1.0)*(m1+t1))-(h1+h3)*((h8+1.0)*(m2+t2)))
//		float t3 = m3*(h1+h3)*((h8+1.0f)*(m2+t2))-m6*(h5+h6)*((h7+1.0f)*(m1+t1));
//		if(Math.abs(t3)<0.0000001f)
//		{
//			//Handle the ambiguous case (no tilt in the homography so can't tell what f should be)
//			t3 = 1.0f;
//		}
//		else
//		{
//			t3 /= ((h5+h6)*((h7+1.0f)*(m1+t1))-(h1+h3)*((h8+1.0f)*(m2+t2)));
//		}
//		f = -(h1+h3)*(m3+t3)/((h7+1.0f)*(m1+t1));
		
		camera.modelMatrix.value[0] = (float)m1; 
		camera.modelMatrix.value[1] = (float)m2;		
		camera.modelMatrix.value[2] = (float)m3;
		camera.modelMatrix.value[3] = (float)0.0f;
		
		camera.modelMatrix.value[4] = (float)m4;
		camera.modelMatrix.value[5] = (float)m5;
		camera.modelMatrix.value[6] = (float)m6;
		camera.modelMatrix.value[7] = (float)0.0f;

		camera.modelMatrix.value[8] = (float)m7;
		camera.modelMatrix.value[9] = (float)m8;
		camera.modelMatrix.value[10] = (float)m9;
		camera.modelMatrix.value[11] = (float)0.0f;
		
		camera.modelMatrix.value[12] = (float)t1;
		camera.modelMatrix.value[13] = (float)t2;
		camera.modelMatrix.value[14] = (float)t3;
		camera.modelMatrix.value[15] = (float)1.0f;

		double deltaZ = camera.zFar - camera.zNear;

	    /* convert from world coordinates to camera coordinates */
		/* Note: zw is implicitly assumed to be zero for these (coplanar) calculations */
		camera.projectionMatrix.value[0] = (float)(f*2.0/(camera.width));
		camera.projectionMatrix.value[1] = (float)0.0f;
		camera.projectionMatrix.value[2] = (float)0.0f;
		camera.projectionMatrix.value[3] = (float)0.0f;
		
		camera.projectionMatrix.value[4] = (float)0.0f;
		camera.projectionMatrix.value[5] = (float)(f*2.0/(camera.height));
		camera.projectionMatrix.value[6] = (float)0.0f;
		camera.projectionMatrix.value[7] = (float)0.0f;

		camera.projectionMatrix.value[8] = (float)0.0f;
		camera.projectionMatrix.value[9] = (float)0.0f;
		camera.projectionMatrix.value[10] = (float)(-(camera.zFar + camera.zNear) / deltaZ);
		camera.projectionMatrix.value[11] = (float)-1.0f;
		
	    camera.projectionMatrix.value[12] = (float)0.0f;
		camera.projectionMatrix.value[13] = (float)0.0f;
		camera.projectionMatrix.value[14] = (float)(-2.0 * camera.zNear * camera.zFar / deltaZ);
		camera.projectionMatrix.value[15] = (float)0.0f;

		camera.centrePixelOffsetx = 0.0f;
		camera.centrePixelOffsety = 0.0f;
		
		GeneralMatrixFloat comparepoint = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat cameraProjectedPoint = new GeneralMatrixFloat(3,1);
		
		
		float x,y;
		float fix,fiy,fih;

		//Pixel position
		x = 0.0f;
		y = 0.0f;
		for(int i=0;i<4;i++)
		{
			switch(i)
			{
			case 0:
				x = 0.0f;
				y = 0.0f;
				break;
			case 1:
				x = calibwidth*0.5f;
				y = calibheight*0.5f;
				break;
			case 2:
				x = 1.0f;
				y = 0.0f;
				break;
			case 3:
				x = 0.0f;
				y = 1.0f;
				break;
			}
			fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
			fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
			fih = (uTox.value[8]+(x)*(uTox.value[6])+(y)*(uTox.value[7]));
			fix/=fih;
			fiy/=fih;
	
			//Convert to world position
			x -= (calibwidth*0.5f);
			x /= planescale;
			y -= (calibheight*0.5f);
			y /= -planescale;				
			comparepoint.value[0] = x;
			comparepoint.value[1] = y;
			comparepoint.value[2] = 0.0f;
			
			camera.project(comparepoint, cameraProjectedPoint);
			
			float fix2 = (normuTox.value[2]+(x)*(normuTox.value[0])+(y)*(normuTox.value[1]));
			float fiy2 = (normuTox.value[5]+(x)*(normuTox.value[3])+(y)*(normuTox.value[4]));
			float fih2 = (normuTox.value[8]+(x)*(normuTox.value[6])+(y)*(normuTox.value[7]));
			fix2/=fih2;
			fiy2/=fih2;

			fix2 += camera.width*0.5f;
			fiy2 += camera.height*0.5f;
			
			float dx = fix-cameraProjectedPoint.value[0];
			float dy = fiy-cameraProjectedPoint.value[1];
//			System.out.println("dx="+dx+"\tdy="+dy);

			float dx2 = fix2-cameraProjectedPoint.value[0];
			float dy2 = fiy2-cameraProjectedPoint.value[1];
//			System.out.println("dx2="+dx2+"\tdy2="+dy2);
		}

//		for(int i=0;i<9;i++)
//			System.out.println("testhomography.value["+i+"]="+uTox.value[i]+"f;");
	}
	
	//From http://cmp.felk.cvut.cz/cmp/courses/XE33PVR/WS20072008/Lectures/Supporting/constrained_lsq.pdf
	public static void calculateWeighted(GeneralMatrixFloat upoints,GeneralMatrixFloat xpoints,GeneralMatrixFloat weights, GeneralMatrixFloat uTox,
			GeneralMatrixFloat validate)
	{
		GeneralMatrixDouble A = new GeneralMatrixDouble(9,upoints.height*2);
		uTox.setDimensionsNoCopy(9, 1);
		A.clear(0.0f);
		
		for(int i=0;i<upoints.height;i++)
		{
			float f = weights.value[i];
			int ind = i*9*2;
			A.value[ind+0] = upoints.value[i*2+0]*f;
			A.value[ind+1] = upoints.value[i*2+1]*f;
			A.value[ind+2] = f;
			A.value[ind+6] = -xpoints.value[i*2+0]*upoints.value[i*2+0]*f;
			A.value[ind+7] = -xpoints.value[i*2+0]*upoints.value[i*2+1]*f;
			A.value[ind+8] = -xpoints.value[i*2+0]*f;

			A.value[ind+9+3] = upoints.value[i*2+0]*f;
			A.value[ind+9+4] = upoints.value[i*2+1]*f;
			A.value[ind+9+5] = f;
			A.value[ind+9+6] = -xpoints.value[i*2+1]*upoints.value[i*2+0]*f;
			A.value[ind+9+7] = -xpoints.value[i*2+1]*upoints.value[i*2+1]*f;
			A.value[ind+9+8] = -xpoints.value[i*2+1]*f;
		}			
		
		GeneralMatrixDouble At = new GeneralMatrixDouble(A.height,A.width);
		GeneralMatrixDouble AtA = new GeneralMatrixDouble(9,9);

		GeneralMatrixDouble.transpose(A, At);
		GeneralMatrixDouble.mult(At, A, AtA);
		
		SVDDouble svd = new SVDDouble(9,9);
		svd.init(AtA);

		GeneralMatrixDouble ut = new GeneralMatrixDouble(9,9);
		GeneralMatrixDouble.transpose(svd.u, ut);
		
		//Normalise each of the possible solutions
		for(int j=0;j<9;j++)
		{
			double f = ut.value[j*9+8];
			for(int i=0;i<9;i++)
			{
				ut.value[j*9+i]/=f;
			}
		}
		
		uTox.width = 9;
		uTox.height = 1;

		uTox.setFromSubset(ut, 8);

		uTox.width = 1;
		uTox.height = 9;

		if(validate!=null)
		{
			System.out.println("Validation requires code");
			//GeneralMatrixDouble.mult(A, uTox, validate);
		}		
		
		//System.out.println("Calculated Homography");
	}

	public static void ransacCalculateWeighted(long startseed, int numIterations,
			GeneralMatrixFloat upoints,GeneralMatrixFloat xpoints,GeneralMatrixFloat weights, GeneralMatrixFloat uTox,
			RenderBuffer calibrated, RenderBuffer input, 
			RenderBuffer tempBuffer, GeneralMatrixFloat tempweights,
			GeneralMatrixFloat bettercalibrated,GeneralMatrixFloat betterdetected)
	{
		float pointtolerance = 5.0f;
		float pointtolerancesqr = pointtolerance*pointtolerance;
		
		float totalWeight = 0.0f;
		
		int homographySubset = 4;
		
		for(int i=0;i<weights.height;i++)
		{
			totalWeight += weights.value[i];
		}
		
		Random r = new Random(startseed);
		GeneralMatrixFloat usubset = new GeneralMatrixFloat(2,homographySubset);
		GeneralMatrixFloat xsubset = new GeneralMatrixFloat(2,homographySubset);
		GeneralMatrixInt subset = new GeneralMatrixInt(1,homographySubset);
		
		GeneralMatrixFloat iuTox = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat tuTox = new GeneralMatrixFloat(3,3);
		GeneralMatrixFloat bestuTox = new GeneralMatrixFloat(1,9);
		Homography.robustlyCalculateWeighted(upoints, xpoints, weights, bestuTox, 2.0f, 2);
		render(input,bestuTox,tempBuffer,0,0,tempBuffer.width,tempBuffer.height);		
		//And use to measure the match quality
		float bestD = Float.MAX_VALUE;//ImageDifference.difference(calibrated, tempBuffer);

		//float bestD = Float.MAX_VALUE;

		GeneralMatrixDouble A = new GeneralMatrixDouble(9,homographySubset*2);
		GeneralMatrixDouble At = new GeneralMatrixDouble(A.height,A.width);
		GeneralMatrixDouble AtA = new GeneralMatrixDouble(9,9);

		SVDDouble svd = new SVDDouble(9,9);

		GeneralMatrixDouble ut = new GeneralMatrixDouble(9,9);

		uTox.setDimensionsNoCopy(9, 1);
				
		GeneralMatrixFloat tbettercalibrated = new GeneralMatrixFloat(2);
		GeneralMatrixFloat tbetterdetected = new GeneralMatrixFloat(2);
		
		for(int i=0;i<numIterations;i++)
		{		
			float runningTotal = totalWeight;
			tempweights.set(weights);
			
			//System.out.println("itr="+i);
			for(int hi=0;hi<homographySubset;hi++)
			{
				float f = r.nextFloat()*runningTotal;
				for(int pi=0;pi<upoints.height;pi++)
				{
					f -= tempweights.value[pi];
					if(f<=0.0f)
					{
						//System.out.println(""+pi);
						subset.value[hi] = pi;
						usubset.value[2*hi+0] = upoints.value[2*pi+0];
						usubset.value[2*hi+1] = upoints.value[2*pi+1];
						xsubset.value[2*hi+0] = xpoints.value[2*pi+0];
						xsubset.value[2*hi+1] = xpoints.value[2*pi+1];
						runningTotal -= tempweights.value[pi];
						tempweights.value[pi] = 0.0f;
						break;
					}
				}
			}
			
			A.height = homographySubset*2;
			At.width = A.height;

			A.clear(0.0f);
			
			for(int pi=0;pi<homographySubset;pi++)
			{
				int ind = pi*9*2;
				A.value[ind+0] = usubset.value[pi*2+0];
				A.value[ind+1] = usubset.value[pi*2+1];
				A.value[ind+2] = 1.0;
				A.value[ind+6] = -xsubset.value[pi*2+0]*usubset.value[pi*2+0];
				A.value[ind+7] = -xsubset.value[pi*2+0]*usubset.value[pi*2+1];
				A.value[ind+8] = -xsubset.value[pi*2+0];

				A.value[ind+9+3] = usubset.value[pi*2+0];
				A.value[ind+9+4] = usubset.value[pi*2+1];
				A.value[ind+9+5] = 1.0;
				A.value[ind+9+6] = -xsubset.value[pi*2+1]*usubset.value[pi*2+0];
				A.value[ind+9+7] = -xsubset.value[pi*2+1]*usubset.value[pi*2+1];
				A.value[ind+9+8] = -xsubset.value[pi*2+1];
			}			
			
			GeneralMatrixDouble.transpose(A, At);
			GeneralMatrixDouble.mult(At, A, AtA);
			
			svd.init(AtA);

			GeneralMatrixDouble.transpose(svd.u, ut);
			
			//Normalise each of the possible solutions
			for(int j=0;j<9;j++)
			{
				double f = ut.value[j*9+8];
				for(int k=0;k<9;k++)
				{
					ut.value[j*9+k]/=f;
				}
			}
			
			uTox.width = 9;
			uTox.height = 1;

			uTox.setFromSubset(ut, 8);

			uTox.width = 3;
			uTox.height = 3;

			GeneralMatrixFloat.transpose(uTox, tuTox);
			GeneralMatrixFloat.invert(tuTox, iuTox);
			
			{
				double f = iuTox.value[8];
				for(int k=0;k<9;k++)
				{
					iuTox.value[k]/=f;
				}
			}
			
			uTox.width = 1;
			uTox.height = 9;
			
			tbettercalibrated.height = 0;
			tbetterdetected.height = 0;
			//
			for(int pi=0;pi<upoints.height;pi++)
			{
				//*
				{
					float x = xpoints.value[2*pi+0];
					float y = xpoints.value[2*pi+1];
					//if((y==1)&&(x==result.width))
					//	System.out.println("Debug");
					float fix = (iuTox.value[6]+(x)*(iuTox.value[0])+(y)*(iuTox.value[3]));
					float fiy = (iuTox.value[7]+(x)*(iuTox.value[1])+(y)*(iuTox.value[4]));
					float fih = (iuTox.value[8]+(x)*(iuTox.value[2])+(y)*(iuTox.value[5]));
					fix/=fih;
					fiy/=fih;
					
					float dx = upoints.value[2*pi+0]-fix;
					float dy = upoints.value[2*pi+1]-fiy;
					
//					boolean isHomographyPoint = false;
//					for(int hpi=0;hpi<homographySubset;hpi++)
//					{
//						if(subset.value[hpi]==pi)
//							isHomographyPoint = true;
//					}

					float d = dx*dx+dy*dy;
					if(d<pointtolerancesqr)
					{
						tbettercalibrated.push_back_row(upoints.value[2*pi+0], upoints.value[2*pi+1]);
						tbetterdetected.push_back_row(xpoints.value[2*pi+0], xpoints.value[2*pi+1]);
					}
				}
				//*/
				/*
				{
					float x = upoints.value[2*pi+0];
					float y = upoints.value[2*pi+1];
					float fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
					float fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
					float fih = (uTox.value[8]+(x)*(uTox.value[6])+(y)*(uTox.value[7]));
					fix/=fih;
					fiy/=fih;

					float dx = xpoints.value[2*pi+0]-fix;
					float dy = xpoints.value[2*pi+1]-fiy;
					
//					boolean isHomographyPoint = false;
//					for(int hpi=0;hpi<homographySubset;hpi++)
//					{
//						if(subset.value[hpi]==pi)
//							isHomographyPoint = true;
//					}
					
					float d = dx*dx+dy*dy;
					if(d<pointtolerancesqr)
					{
						tbettercalibrated.push_back_row(upoints.value[2*pi+0], upoints.value[2*pi+1]);
						tbetterdetected.push_back_row(xpoints.value[2*pi+0], xpoints.value[2*pi+1]);
					}
				}
				*/
			}
			
			if(tbettercalibrated.height<4)
			{
				//System.out.println("Error");
				continue;
			}

			A.ensureCapacityNoCopy(9*tbettercalibrated.height*2);
			At.ensureCapacityNoCopy(9*tbettercalibrated.height*2);
			A.height = tbettercalibrated.height*2;
			At.width = A.height;
			
			A.clear(0.0f);
			
			for(int pi=0;pi<tbettercalibrated.height;pi++)
			{
				int ind = pi*9*2;
				A.value[ind+0] = tbettercalibrated.value[pi*2+0];
				A.value[ind+1] = tbettercalibrated.value[pi*2+1];
				A.value[ind+2] = 1.0;
				A.value[ind+6] = -tbetterdetected.value[pi*2+0]*tbettercalibrated.value[pi*2+0];
				A.value[ind+7] = -tbetterdetected.value[pi*2+0]*tbettercalibrated.value[pi*2+1];
				A.value[ind+8] = -tbetterdetected.value[pi*2+0];

				A.value[ind+9+3] = tbettercalibrated.value[pi*2+0];
				A.value[ind+9+4] = tbettercalibrated.value[pi*2+1];
				A.value[ind+9+5] = 1.0;
				A.value[ind+9+6] = -tbetterdetected.value[pi*2+1]*tbettercalibrated.value[pi*2+0];
				A.value[ind+9+7] = -tbetterdetected.value[pi*2+1]*tbettercalibrated.value[pi*2+1];
				A.value[ind+9+8] = -tbetterdetected.value[pi*2+1];
			}			
			
			GeneralMatrixDouble.transpose(A, At);
			GeneralMatrixDouble.mult(At, A, AtA);
			
			svd.init(AtA);

			GeneralMatrixDouble.transpose(svd.u, ut);
			
			//Normalise each of the possible solutions
			for(int j=0;j<9;j++)
			{
				double f = ut.value[j*9+8];
				for(int k=0;k<9;k++)
				{
					ut.value[j*9+k]/=f;
				}
			}
			
			uTox.width = 9;
			uTox.height = 1;

			uTox.setFromSubset(ut, 8);

			uTox.width = 1;
			uTox.height = 9;
			
			//Now reverse render the homography
			render(input,uTox,tempBuffer,0,0,tempBuffer.width,tempBuffer.height);
			
			//And use to measure the match quality
			float diff = ImageDifference.difference(calibrated, tempBuffer);
			if(diff<bestD)
			{
				System.out.println("Found bettermatch "+i+" nump="+tbettercalibrated.height);
				bestD = diff;
				bestuTox.set(uTox);
				bettercalibrated.ensureCapacityNoCopy(tbettercalibrated.height*2);
				betterdetected.ensureCapacityNoCopy(tbetterdetected.height*2);
				bettercalibrated.height = tbettercalibrated.height;
				betterdetected.height = tbetterdetected.height;
				bettercalibrated.set(tbettercalibrated);
				betterdetected.set(tbetterdetected);
				
				if(diff==0.0f)
					return;
			}
		}
		
		uTox.set(bestuTox);
	}
	
	public static void robustlyCalculateWeighted(GeneralMatrixFloat upoints,GeneralMatrixFloat xpoints,GeneralMatrixFloat weights, GeneralMatrixFloat uTox, float sdError,int numIter)
	{
		GeneralMatrixFloat validate = new GeneralMatrixFloat(1,upoints.height*2);
		
		calculateWeighted(upoints, xpoints, weights, uTox, validate);
		
		GeneralMatrixFloat robustWeight = weights;//new GeneralMatrixFloat(1,weights.height);
		//robustWeight.set(weights);

		GeneralMatrixFloat posValidate = new GeneralMatrixFloat(1,upoints.height);
		
		for(int iter=0;iter<numIter;iter++)
		{
			for(int i=0;i<(posValidate.height);i++)
			{
				float dx = validate.value[i*2+0];
				float dy = validate.value[i*2+1];
				posValidate.value[i] = dx*dx+dy*dy;
			}
			Outliers.detectWeightedOutliers(posValidate, robustWeight, sdError);
			//Outliers.detectOutliers(validate.value, validate.height, 3.0f, Float.MAX_VALUE);
			for(int i=0;i<posValidate.height;i++)
			{
				if(validate.value[i]==Float.MAX_VALUE)
				{
					robustWeight.value[i] = 0.0f;
				}
				else
				if(robustWeight.value[i]!=0.0f)
				{
//					if(iter==(numIter-1))
//						robustWeight.value[i] = 1.0f;
				}
			}

			calculateWeighted(upoints, xpoints, weights, uTox, validate);
		}
		
		for(int i=0;i<(posValidate.height);i++)
		{
			float x = upoints.value[i*2+0];
			float y = upoints.value[i*2+1];
			float fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
			float fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
			float fih = (uTox.value[8]+(x)*(uTox.value[6])+(y)*(uTox.value[7]));
			fix/=fih;
			fiy/=fih;
			
			float dx = fix-xpoints.value[i*2+0];
			float dy = fiy-xpoints.value[i*2+1];
			posValidate.value[i] = dx*dx+dy*dy;
		}
		System.out.println("Built and tested homography");
	}
	
	public static void render(RenderBuffer tb,GeneralMatrixFloat uTox,RenderBuffer rb,int xoff,int yoff,int w,int h)
	{
		for(int y=0;y<h;y++)
		{
			for(int x=0;x<w;x++)
			{
				//if((y==1)&&(x==result.width))
				//	System.out.println("Debug");
				float fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
				float fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
				float fih = (uTox.value[8]+(x)*(uTox.value[6])+(y)*(uTox.value[7]));
				fix/=fih;
				fiy/=fih;
				int lx = (int)(fix);
				int ly = (int)(fiy);
				float fdx = fix-lx;
				float fdy = fiy-ly;
				float ifdx = 1.0f-fdx;
				float ifdy = 1.0f-fdy;
				
				if((lx<0)||(lx>=(tb.width-1))||(ly<0)||(ly>=(tb.height-1)))
					continue;
				
				int ind = lx+ly*tb.width;
				int ll = tb.pixel[ind];
				int hl = tb.pixel[ind+1];
				int lh = tb.pixel[ind+tb.width];
				int hh = tb.pixel[ind+1+tb.width];
				
				int rll = (ll&0xFF0000)>>16;
				int rhl = (hl&0xFF0000)>>16;
				int rlh = (lh&0xFF0000)>>16;
				int rhh = (hh&0xFF0000)>>16;
				
				int r = (int)((rll*ifdx+rhl*fdx)*ifdy+(rlh*ifdx+rhh*fdx)*fdy);
				
				if(r>0xFF)
					r=0xFF;
				
				int gll = (ll&0xFF00)>>8;
				int ghl = (hl&0xFF00)>>8;
				int glh = (lh&0xFF00)>>8;
				int ghh = (hh&0xFF00)>>8;

				int g = (int)((gll*ifdx+ghl*fdx)*ifdy+(glh*ifdx+ghh*fdx)*fdy);
				
				if(g>0xFF)
					g=0xFF;
				
				int bll = (ll&0xFF);
				int bhl = (hl&0xFF);
				int blh = (lh&0xFF);
				int bhh = (hh&0xFF);

				int b = (int)((bll*ifdx+bhl*fdx)*ifdy+(blh*ifdx+bhh*fdx)*fdy);
				
				if(b>0xFF)
					b=0xFF;
				
				int rbx = xoff+x;
				int rby = yoff+y;
				if((rbx<0)||(rbx>=rb.width)||(rby<0)||(rby>=rb.height))
					continue;
				ind = rbx+(rby)*rb.width;
				rb.pixel[ind] = (r<<16)|(g<<8)|(b); 
			}
		}		
	}

	/*
	public static void renderRigid(RenderBuffer tb,GeneralMatrixFloat uTox,RenderBuffer rb,int xoff,int yoff,int w,int h)
	{
		for(int y=0;y<h;y++)
		{
			for(int x=0;x<w;x++)
			{
				//if((y==1)&&(x==result.width))
				//	System.out.println("Debug");
				float fix = (uTox.value[2]+(x)*(uTox.value[0])+(y)*(uTox.value[1]));
				float fiy = (uTox.value[5]+(x)*(uTox.value[3])+(y)*(uTox.value[4]));
				int lx = (int)(fix);
				int ly = (int)(fiy);
				float fdx = fix-lx;
				float fdy = fiy-ly;
				float ifdx = 1.0f-fdx;
				float ifdy = 1.0f-fdy;
				
				if((lx<0)||(lx>=(tb.width-1))||(ly<0)||(ly>=(tb.height-1)))
					continue;
				
				int ind = lx+ly*tb.width;
				int ll = tb.pixel[ind];
				int hl = tb.pixel[ind+1];
				int lh = tb.pixel[ind+tb.width];
				int hh = tb.pixel[ind+1+tb.width];
				
				int rll = (ll&0xFF0000)>>16;
				int rhl = (hl&0xFF0000)>>16;
				int rlh = (lh&0xFF0000)>>16;
				int rhh = (hh&0xFF0000)>>16;
				
				int r = (int)((rll*ifdx+rhl*fdx)*ifdy+(rlh*ifdx+rhh*fdx)*fdy);
				
				if(r!=0)
				{
					boolean test = true;
				}
				
				if(r>0xFF)
					r=0xFF;
				
				int gll = (ll&0xFF00)>>8;
				int ghl = (hl&0xFF00)>>8;
				int glh = (lh&0xFF00)>>8;
				int ghh = (hh&0xFF00)>>8;

				int g = (int)((gll*ifdx+ghl*fdx)*ifdy+(glh*ifdx+ghh*fdx)*fdy);
				
				if(g>0xFF)
					g=0xFF;
				
				int bll = (ll&0xFF);
				int bhl = (hl&0xFF);
				int blh = (lh&0xFF);
				int bhh = (hh&0xFF);

				int b = (int)((bll*ifdx+bhl*fdx)*ifdy+(blh*ifdx+bhh*fdx)*fdy);
				
				if(b>0xFF)
					b=0xFF;
				
				int rbx = xoff+x;
				int rby = yoff+y;
				if((rbx<0)||(rbx>=rb.width)||(rby<0)||(rby>=rb.height))
					continue;
				ind = rbx+(rby)*rb.width;
				rb.pixel[ind] = (r<<16)|(g<<8)|(b); 
			}
		}		
	}
	*/
}

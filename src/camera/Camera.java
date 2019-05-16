package camera;

import rendering.RenderBuffer;
import mathematics.GeneralMatrixDouble;
import mathematics.GeneralMatrixFloat;
import mathematics.distance.Line;
import mathematics.linearalgebra.SVD;


//Full camera model
public class Camera 
{
	//perspective scale factor
	double f;
	//CCD pixels
	public int width;
	public int height;
	//For calculating zBuffer values
	public double zNear = 0.01f;
	public double zFar = 1000.0f;

	public float centrePixelOffsetx = 0.0f;
	public float centrePixelOffsety = 0.0f;

	//The physical width of ccd elements in the camera (only used when setting from focal length)
	public float dpx = 5.6e-4f;
	public float dpy = 5.6e-4f;
	
	public float kappa1 = 0.0f;
	public float sx = 1.0f;

	public float k1 = 0.0f;
	public float k2 = 0.0f;

	/*
	//Distortion of the camera shape (currently unused)
	double xu;
	double yu;
	*/
	
	//As if it was an object in space (this is inverted to get the model matrix
	public GeneralMatrixFloat cameraTransformMatrix = 	new GeneralMatrixFloat(4,4);
	//Calculated values
	public GeneralMatrixFloat projectionMatrix = 	new GeneralMatrixFloat(4,4);
	//Currently unused
	public GeneralMatrixFloat modelMatrix = 		new GeneralMatrixFloat(4,4);
	
	//Intermediate values (so no new in update)
	public static GeneralMatrixFloat objToCamera = new GeneralMatrixFloat(4,4);
	public static float[] postTrans = new float[3];
	public static float[] postModel = new float[3];
	public static float[] postProj = new float[4];

	public GeneralMatrixFloat clipPlanes = new GeneralMatrixFloat(4,6);
	
	public static void calcWorldPosition(Camera c1,Camera c2,GeneralMatrixFloat p1,
			GeneralMatrixFloat p2,
			GeneralMatrixFloat X)
	{
		//Get the ray for the points p1 and p2
	}

	public void getiP(GeneralMatrixFloat iP)
	{
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat.mult(modelMatrix,projectionMatrix,temp);
		GeneralMatrixFloat.invert(temp,iP);		
	}
	
	static void getRay(GeneralMatrixFloat ip1,
			GeneralMatrixFloat corign1,
			GeneralMatrixFloat p1,
			GeneralMatrixFloat sp,
			GeneralMatrixFloat l1)
	{
		sp.value[0] = p1.value[0];
		sp.value[1] = p1.value[1];
		sp.value[2] = 0.0f;
		sp.value[3] = 1.0f;
		
		l1.width = 4;
		GeneralMatrixFloat.mult(sp,ip1,l1);
		
		l1.value[0] = l1.value[0] / l1.value[3];
		l1.value[1] = l1.value[1] / l1.value[3];
		l1.value[2] = l1.value[2] / l1.value[3];

		l1.value[0] -= corign1.value[0];
		l1.value[1] -= corign1.value[1];
		l1.value[2] -= corign1.value[2];
		l1.width = 3;
		l1.normalise();
		l1.width = 6;
		l1.value[3] = corign1.value[0];
		l1.value[4] = corign1.value[1];
		l1.value[5] = corign1.value[2];
	}
	
	//Given points in 
	public static void calcWorldPosition(GeneralMatrixFloat ip1,
			GeneralMatrixFloat corign1,
			GeneralMatrixFloat p1,
			GeneralMatrixFloat ip2,
			GeneralMatrixFloat corign2,
			GeneralMatrixFloat p2,
			GeneralMatrixFloat X)
	{
		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat l1 = new GeneralMatrixFloat(6,1);
		GeneralMatrixFloat l2 = new GeneralMatrixFloat(6,1);

		GeneralMatrixFloat closestRayPoint = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat closestRayPoint2 = new GeneralMatrixFloat(3,1);
		
		getRay(ip1, corign1, p1, sp, l1);
		getRay(ip2, corign2, p2, sp, l2);
		
		Line.GetRayToRaySquared(l1.value[3], l1.value[4], l1.value[5],
				l1.value[0], l1.value[1], l1.value[2], 
				l2.value[3], l2.value[4], l2.value[5],
				l2.value[0], l2.value[1], l2.value[2], 
				closestRayPoint, 
				closestRayPoint2);
		
		X.value[0] = (closestRayPoint.value[0]+closestRayPoint2.value[0])*0.5f;
		X.value[1] = (closestRayPoint.value[1]+closestRayPoint2.value[1])*0.5f;
		X.value[2] = (closestRayPoint.value[2]+closestRayPoint2.value[2])*0.5f;
	}
	
	//A matrix that given an image point on 'from', creates a vector 
	//that defines the line of possible points it matches in 'to'
	//in the format ax+by+c = 0
	public static void calcF(Camera from,Camera to,GeneralMatrixFloat F,
			GeneralMatrixFloat epipole,
			GeneralMatrixFloat simpP1toP2)
	{
		F.setDimensionsNoCopy(3, 3);
		
//		float nx = (x-centrePixelOffsetx)/((float)width); 
//		float ny = (y-centrePixelOffsety)/((float)height); 
//		float nz = z/((float)0x7FFFFFFF);
//		
//		nx = nx*2.0f - 1.0f;
//		ny = ny*2.0f - 1.0f;
//		nz = nz*2.0f - 1.0f;
//		
//		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
//		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
//		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
//		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
//		
//		if(objToWorld==null)
//			temp.set(modelMatrix);
//		else
//			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
//		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
//		GeneralMatrixFloat.invert(temp2,temp);
		
		//Build the camera matrices for each
		GeneralMatrixFloat P1 = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat.mult(from.modelMatrix,from.projectionMatrix,P1);
		GeneralMatrixFloat P2 = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat.mult(to.modelMatrix,to.projectionMatrix,P2);

		GeneralMatrixFloat iP1 = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat.invert(P1,iP1);

		GeneralMatrixFloat P1toP2 = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat.mult(iP1,P2,P1toP2);
		
		//Find the camera center of P1 in P2
		GeneralMatrixFloat centre1 = new GeneralMatrixFloat(4,1);
		centre1.setFromSubset(from.cameraTransformMatrix,3);
		
		int i;
		for (i=0; i<3; i++) 
		{
			postModel[i] = 
				centre1.value[0] * to.modelMatrix.value[0*4+i] +
				centre1.value[1] * to.modelMatrix.value[1*4+i] +
				centre1.value[2] * to.modelMatrix.value[2*4+i] +
			1.0f * to.modelMatrix.value[3*4+i];
		}
		for (i=0; i<4; i++) 
		{
			postProj[i] = 
			postModel[0] * to.projectionMatrix.value[0*4+i] +
			postModel[1] * to.projectionMatrix.value[1*4+i] +
			postModel[2] * to.projectionMatrix.value[2*4+i] +
			1.0f * to.projectionMatrix.value[3*4+i];
		}

	    if ((postProj[3] == 0.0f)||(postModel[2]>0.0f))
	    {
	    	//The center of the camera is not visible in the second camera
	    	
	    }
	    postProj[0] /= postProj[3];
	    postProj[1] /= postProj[3];
	    postProj[2] /= postProj[3];
		
	    //This creates a point that all of the points in P1 pass through in P2
	    epipole.value[0] = postProj[0];
	    epipole.value[1] = postProj[1];
	    epipole.value[2] = postProj[2];
	    
	    //Simplify the the P1toP2 matrix assuming that the point is at 
	    //x,y,0.0f in camera space
	    simpP1toP2.setDimensionsNoCopy(3, 3);
//	    GeneralMatrixFloat simpP1toP2 = new GeneralMatrixFloat(3,3);
	    simpP1toP2.value[0*3+0] = P1toP2.value[0*4+0];
	    simpP1toP2.value[0*3+1] = P1toP2.value[0*4+1];
	    simpP1toP2.value[0*3+2] = P1toP2.value[0*4+3];

	    simpP1toP2.value[1*3+0] = P1toP2.value[1*4+0];
	    simpP1toP2.value[1*3+1] = P1toP2.value[1*4+1];
	    simpP1toP2.value[1*3+2] = P1toP2.value[1*4+3];

	    simpP1toP2.value[2*3+0] = P1toP2.value[3*4+0];
	    simpP1toP2.value[2*3+1] = P1toP2.value[3*4+1];
	    simpP1toP2.value[2*3+2] = P1toP2.value[3*4+3];

	    //now can create a matrix that calcs an equation of the line 
	    //from the projected center to the projected x,y,0.0 point
	    
	    postProj[2] = 1.0f;
	    
	    GeneralMatrixFloat crossC = new GeneralMatrixFloat(3,3);
	    crossC.value[0*3+0] = 0.0f;
	    crossC.value[0*3+1] = -postProj[2];
	    crossC.value[0*3+2] = postProj[1];

	    crossC.value[1*3+0] = postProj[2];
	    crossC.value[1*3+1] = 0.0f;
	    crossC.value[1*3+2] = -postProj[0];

	    crossC.value[2*3+0] = -postProj[1];
	    crossC.value[2*3+1] = postProj[0];
	    crossC.value[2*3+2] = 0.0f;
	    
	    GeneralMatrixFloat.mult(simpP1toP2, crossC, F);
//		C1 = null(P1);  % Camera centre 1 is the null space of P1
//		  e2 = P2*C1;     % epipole in camera 2
//
//		  e2x = [  0   -e2(3) e2(2)    % Skew symmetric matrix from e2
//		         e2(3)    0  -e2(1)
//		        -e2(2)  e2(1)   0  ];
//
//		  F = e2x*P2*pinv(P1);		  
	}
	
	public static float computeEPD(GeneralMatrixFloat F,
			GeneralMatrixFloat p0,
			GeneralMatrixFloat p1)
	{
		//assuming p0 and p1 are normalised
		GeneralMatrixFloat line = new GeneralMatrixFloat(3,1);
		GeneralMatrixFloat.mult(F, p1, line);
		float mag = line.norm();
		if(mag==0.0f)
			return 0.0f;
		line.scale(1.0f/mag);
		float dist = GeneralMatrixFloat.dotProduct3(line,p0);
		return Math.abs(dist);
	}
	
	public void setFrom(Camera c)
	{
		centrePixelOffsetx = c.centrePixelOffsetx;
		centrePixelOffsety = c.centrePixelOffsety;
		width = c.width;
		height = c.height;
		zNear = c.zNear;
		zFar = c.zFar;
		cameraTransformMatrix.set(c.cameraTransformMatrix);
		projectionMatrix.set(c.projectionMatrix);
		modelMatrix.set(c.modelMatrix);
	}

	//Calculate a world ray given a pixel position
	public void getRay(float x,float y,GeneralMatrixFloat op)
	{
		float z = 0x7FFFFFFF/2;
		op.clear(0.0f);
		unproject(x,y,z,null,op);
		op.value[0] -= cameraTransformMatrix.value[12+0];
		op.value[1] -= cameraTransformMatrix.value[12+1];
		op.value[2] -= cameraTransformMatrix.value[12+2];
		op.normalise();
		op.value[3] = op.value[0];
		op.value[4] = op.value[1];
		op.value[5] = op.value[2];
		op.value[0] = cameraTransformMatrix.value[12+0];
		op.value[1] = cameraTransformMatrix.value[12+1];
		op.value[2] = cameraTransformMatrix.value[12+2];
	}
	
	public void getRayDistorted(float x,float y,GeneralMatrixFloat op)
	{
		float z = 0x7FFFFFFF/2;
		op.clear(0.0f);
		unprojectDistorted(x,y,z,null,op);
		op.value[0] -= cameraTransformMatrix.value[12+0];
		op.value[1] -= cameraTransformMatrix.value[12+1];
		op.value[2] -= cameraTransformMatrix.value[12+2];
		op.normalise();
		op.value[3] = op.value[0];
		op.value[4] = op.value[1];
		op.value[5] = op.value[2];
		op.value[0] = cameraTransformMatrix.value[12+0];
		op.value[1] = cameraTransformMatrix.value[12+1];
		op.value[2] = cameraTransformMatrix.value[12+2];
	}

	public static void getRayDistorted(float x,float y,
			GeneralMatrixFloat modelMatrix,GeneralMatrixFloat projectionMatrix,
			float width,float height,float centrePixelOffsetx,float centrePixelOffsety,float sx,double k1,double k2,
			GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float z = 0x7FFFFFFF/2;
		op.clear(0.0f);
		unprojectDistorted(x,y,z,modelMatrix,projectionMatrix,
				width,height,centrePixelOffsetx,centrePixelOffsety,sx,k1,k2,
				null,op);

		float tx = modelMatrix.value[3*4+0];
		float ty = modelMatrix.value[3*4+1];
		float tz = modelMatrix.value[3*4+2];		
		float ctx = -(modelMatrix.value[0*4+0]*tx+modelMatrix.value[0*4+1]*ty+modelMatrix.value[0*4+2]*tz);
		float cty = -(modelMatrix.value[1*4+0]*tx+modelMatrix.value[1*4+1]*ty+modelMatrix.value[1*4+2]*tz);
		float ctz = -(modelMatrix.value[2*4+0]*tx+modelMatrix.value[2*4+1]*ty+modelMatrix.value[2*4+2]*tz);
		op.value[0] -= ctx;
		op.value[1] -= cty;
		op.value[2] -= ctz;
		op.normalise();
		op.value[3] = op.value[0];
		op.value[4] = op.value[1];
		op.value[5] = op.value[2];
		op.value[0] = ctx;
		op.value[1] = cty;
		op.value[2] = ctz;
	}
	
	//Calculate the world position for an object that has a screen position and screen radius, given its world radius
	public void getPositionforScale(float worldradius,float screenradius,float x,float y,GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float nx = (x-centrePixelOffsetx)/((float)width); 
		float ny = (y-centrePixelOffsety)/((float)height); 
		screenradius = screenradius/(height/2);
		
		nx = nx*2.0f - 1.0f;
		ny = ny*2.0f - 1.0f;

		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
		
		temp.set(modelMatrix);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		GeneralMatrixFloat.invert(temp2,temp);
		//GeneralMatrixFloat.invert(projectionMatrix,temp);
		
		//Infer nz
		float nz = (-temp.value[15]+(temp.value[5]*screenradius/worldradius))/temp.value[11];
		
		sp.value[0] = nx;
		sp.value[1] = -ny;
		sp.value[2] = nz;
		sp.value[3] = 1.0f;
		
		if(objToWorld==null)
			temp.set(modelMatrix);
		else
			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		GeneralMatrixFloat.invert(temp2,temp);
		GeneralMatrixFloat.mult(sp,temp,hop);		
		
		op.value[0] = hop.value[0] / hop.value[3];
		op.value[1] = hop.value[1] / hop.value[3];
		op.value[2] = hop.value[2] / hop.value[3];
	}
	
	public void setFromRotateAroundPoint(float tx,float ty,float tz,float lat,float lon,float roll,float r)
	{
		final float dx = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.sin(lon *Math.PI /180.0)));
		final float dy = (float)(r *Math.sin(lat *Math.PI /180.0));
		final float dz = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.cos(lon *Math.PI /180.0)));
		
		setModelFromLookat(tx+dx,ty+dy,tz+dz,tx,ty,tz,roll);
	}
	
	public static void calcTransformFromRotateAroundPoint(float tx,float ty,float tz,float lat,float lon,float roll,float r, GeneralMatrixFloat cameraTransform)
	{
		final float dx = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.sin(lon *Math.PI /180.0)));
		final float dy = (float)(r *Math.sin(lat *Math.PI /180.0));
		final float dz = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.cos(lon *Math.PI /180.0)));

		calcTransformFromLookat(tx+dx, ty+dy, tz+dz, tx, ty, tz, roll,cameraTransform);
	}
	
	public static void calculateRotateAroundPointFromLookat(float sx,float sy,float sz,
			   float tx,float ty,float tz, float fov,
			   GeneralMatrixFloat params,int paramoffset)
	{
		float ndx = sx-tx;
		float ndy = sy-ty;
		float ndz = sz-tz;
		float newd = (float)Math.sqrt(ndx*ndx+ndy*ndy+ndz*ndz);
		
		float nlt = ndy/newd;
		nlt = (float)Math.asin(nlt);
		nlt = (float)(nlt*180.0/Math.PI);
		
		float nln = (float)(newd *(Math.cos(nlt *Math.PI /180.0)));
		nln = ndx/nln;
		if(nln<-1.0f)
			nln = -1.0f;
		if(nln>1.0f)
			nln = 1.0f;
		nln = (float)Math.asin(nln);
		nln = (float)(nln*180.0/Math.PI);

		float nln2 = (float)(newd *(Math.cos(nlt *Math.PI /180.0)));
		nln2 = ndz/nln2;						
		nln2 = (float)Math.acos(nln2);
		nln2 = (float)(nln2*180.0/Math.PI);
		
		if(nln<0.0f)
			nln2 = -nln2;
		
		if(Math.abs(nln2-nln)>0.01f)
		{
			//Get the other sin value for a given input
			if(nlt>0.0f)
			{
				if(nlt>90.0f)
				{
					nlt = 180-nlt;
				}
				else
				{
					nlt = 180-nlt;
				}
			}
			else
			{
				if(nlt<-90.0f)
				{
					nlt = -180-nlt;
				}
				else
				{
					nlt = -180-nlt;
				}
			}
			
			nln = (float)(newd *(Math.cos(nlt *Math.PI /180.0)));
			nln = ndx/nln;
			nln = (float)Math.asin(nln);
			nln = (float)(nln*180.0/Math.PI);

			nln2 = (float)(newd *(Math.cos(nlt *Math.PI /180.0)));
			nln2 = ndz/nln2;						
			nln2 = (float)Math.acos(nln2);
			nln2 = (float)(nln2*180.0/Math.PI);	
			
			if(nln<0.0f)
				nln2 = -nln2;
		}
		
		params.value[paramoffset*7+0] = nlt;
		params.value[paramoffset*7+1] = nln;
		params.value[paramoffset*7+2] = newd;
		
		params.value[paramoffset*7+3] = tx;
		params.value[paramoffset*7+4] = ty;
		params.value[paramoffset*7+5] = tz;
		params.value[paramoffset*7+6] = fov;
	}
	public static void calcTransformFromLookat(float x,float y,float z,
								   float tx,float ty,float tz,
								   float roll,
								   GeneralMatrixFloat c)
	{
		float fx = tx-x;
		float fy = ty-y;
		float fz = tz-z;
		
		if((Math.abs(fx)<=GeneralMatrixFloat.EPSILON)&&(Math.abs(fz)<=GeneralMatrixFloat.EPSILON))
		{
			if(Math.abs(fz)>0.0)
				fz += GeneralMatrixFloat.EPSILON;
			else
				fz -= GeneralMatrixFloat.EPSILON;
		}
		//Normalise
		final float fl = 1.0f/(float)Math.sqrt(fx*fx+fy*fy+fz*fz);
		fx *= fl;
		fy *= fl;
		fz *= fl;
		
		//Cross product with y=1
		float rx = fz;
		float ry = 0.0f;
		float rz = -fx;
		
		//Normalise
		float rl = 1.0f/(float)Math.sqrt(rx*rx+ry*ry+rz*rz);
		rx *= rl;
		ry *= rl;
		rz *= rl;

		//unrolled x axis
		
		//forward cross right
		float ux = fy*rz-ry*fz;
		float uy = fz*rx-rz*fx;
		float uz = fx*ry-rx*fy;
		
		//Normalise
		float ul = 1.0f/(float)Math.sqrt(ux*ux+uy*uy+uz*uz);
		ux *= ul;
		uy *= ul;
		uz *= ul;

		//unrolled y axis

		double rrad = roll*Math.PI/180.0;
		float cosr = (float)Math.cos(rrad);
		float sinr = (float)Math.sin(rrad);
		
		float rrx = cosr*rx-sinr*ux;
		float rry = cosr*ry-sinr*uy;
		float rrz = cosr*rz-sinr*uz;

		float rux = sinr*rx+cosr*ux;
		float ruy = sinr*ry+cosr*uy;
		float ruz = sinr*rz+cosr*uz;
		
		//Model matrix is transpose of this mat
		c.value[0*4+0] = rrx;
		c.value[0*4+1] = rry;
		c.value[0*4+2] = rrz;

		c.value[1*4+0] = rux;
		c.value[1*4+1] = ruy;
		c.value[1*4+2] = ruz;

		c.value[2*4+0] = fx;
		c.value[2*4+1] = fy;
		c.value[2*4+2] = fz;

		c.value[3*4+0] = x;
		c.value[3*4+1] = y;
		c.value[3*4+2] = z;
	}

	public void setModelFromLookat(float x,float y,float z,
			   float tx,float ty,float tz,
			   float roll)
	{
		calcTransformFromLookat(x, y, z, tx, ty, tz, roll, cameraTransformMatrix);
		buildModelMatrix(cameraTransformMatrix,modelMatrix);
	}
	
	public static void setModelFromRotateAroundPoint(float tx,float ty,float tz,float lat,float lon,float roll,float r,GeneralMatrixFloat model)
	{
		final float dx = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.sin(lon *Math.PI /180.0)));
		final float dy = (float)(r *Math.sin(lat *Math.PI /180.0));
		final float dz = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.cos(lon *Math.PI /180.0)));
		
		setModelFromLookat(tx+dx,ty+dy,tz+dz,tx,ty,tz,roll,model);
	}
	public static void setTransformFromRotateAroundPoint(float tx,float ty,float tz,float lat,float lon,float roll,float r,GeneralMatrixFloat model)
	{
		final float dx = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.sin(lon *Math.PI /180.0)));
		final float dy = (float)(r *Math.sin(lat *Math.PI /180.0));
		final float dz = (float)(r *(Math.cos(lat *Math.PI /180.0)*Math.cos(lon *Math.PI /180.0)));
		
		calcTransformFromLookat(tx+dx,ty+dy,tz+dz,tx,ty,tz,roll,model);
	}
	public static void setModelFromLookat(float x,float y,float z,
			   float tx,float ty,float tz,
			   float roll,GeneralMatrixFloat model)
	{
		GeneralMatrixFloat camtranmat = new GeneralMatrixFloat(4,4);
		calcTransformFromLookat(x, y, z, tx, ty, tz, roll, camtranmat);
		buildModelMatrix(camtranmat,model);
	}

	public static void buildCameraTransformMatrixFromModelMatrix(GeneralMatrixFloat cameraTransformMatrix,final GeneralMatrixFloat modelMatrix)
	{
		//needs to be rotated like the rest of the points in space
		float tx = modelMatrix.value[3*4+0];
		float ty = modelMatrix.value[3*4+1];
		float tz = modelMatrix.value[3*4+2];		
		float x = modelMatrix.value[0*4+0]*tx+modelMatrix.value[0*4+1]*ty+modelMatrix.value[0*4+2]*tz;
		float y = modelMatrix.value[1*4+0]*tx+modelMatrix.value[1*4+1]*ty+modelMatrix.value[1*4+2]*tz;
		float z = modelMatrix.value[2*4+0]*tx+modelMatrix.value[2*4+1]*ty+modelMatrix.value[2*4+2]*tz;
//		float x = modelMatrix.value[0*4+0]*tx+modelMatrix.value[1*4+0]*ty+modelMatrix.value[2*4+0]*tz;
//		float y = modelMatrix.value[0*4+1]*tx+modelMatrix.value[1*4+1]*ty+modelMatrix.value[2*4+1]*tz;
//		float z = modelMatrix.value[0*4+2]*tx+modelMatrix.value[1*4+2]*ty+modelMatrix.value[2*4+2]*tz;

		cameraTransformMatrix.value[3*4+0] = -x;
		cameraTransformMatrix.value[3*4+1] = -y;
		cameraTransformMatrix.value[3*4+2] = -z;

		cameraTransformMatrix.value[0*4+0] = modelMatrix.value[0*4+0];
		cameraTransformMatrix.value[0*4+1] = modelMatrix.value[1*4+0];
		cameraTransformMatrix.value[0*4+2] = modelMatrix.value[2*4+0];

		cameraTransformMatrix.value[1*4+0] = -modelMatrix.value[0*4+1];
		cameraTransformMatrix.value[1*4+1] = -modelMatrix.value[1*4+1];
		cameraTransformMatrix.value[1*4+2] = -modelMatrix.value[2*4+1];

		cameraTransformMatrix.value[2*4+0] = -modelMatrix.value[0*4+2];
		cameraTransformMatrix.value[2*4+1] = -modelMatrix.value[1*4+2];
		cameraTransformMatrix.value[2*4+2] = -modelMatrix.value[2*4+2];
	}
	
	public static void buildDazCameraTransformMatrixFromModelMatrix(GeneralMatrixFloat cameraTransformMatrix,final GeneralMatrixFloat modelMatrix)
	{
		//needs to be rotated like the rest of the points in space
		float tx = modelMatrix.value[3*4+0];
		float ty = modelMatrix.value[3*4+1];
		float tz = modelMatrix.value[3*4+2];		
		float x = modelMatrix.value[0*4+0]*tx-modelMatrix.value[0*4+1]*ty-modelMatrix.value[0*4+2]*tz;
		float y = modelMatrix.value[1*4+0]*tx-modelMatrix.value[1*4+1]*ty-modelMatrix.value[1*4+2]*tz;
		float z = modelMatrix.value[2*4+0]*tx-modelMatrix.value[2*4+1]*ty-modelMatrix.value[2*4+2]*tz;
//		float x = modelMatrix.value[0*4+0]*tx+modelMatrix.value[1*4+0]*ty+modelMatrix.value[2*4+0]*tz;
//		float y = modelMatrix.value[0*4+1]*tx+modelMatrix.value[1*4+1]*ty+modelMatrix.value[2*4+1]*tz;
//		float z = modelMatrix.value[0*4+2]*tx+modelMatrix.value[1*4+2]*ty+modelMatrix.value[2*4+2]*tz;

		cameraTransformMatrix.value[3*4+0] = x;
		cameraTransformMatrix.value[3*4+1] = -y;
		cameraTransformMatrix.value[3*4+2] = z;

		cameraTransformMatrix.value[0*4+0] = modelMatrix.value[0*4+0];
		cameraTransformMatrix.value[0*4+1] = modelMatrix.value[1*4+0];
		cameraTransformMatrix.value[0*4+2] = modelMatrix.value[2*4+0];

		cameraTransformMatrix.value[1*4+0] = modelMatrix.value[0*4+1];
		cameraTransformMatrix.value[1*4+1] = modelMatrix.value[1*4+1];
		cameraTransformMatrix.value[1*4+2] = modelMatrix.value[2*4+1];

		cameraTransformMatrix.value[2*4+0] = modelMatrix.value[0*4+2];
		cameraTransformMatrix.value[2*4+1] = modelMatrix.value[1*4+2];
		cameraTransformMatrix.value[2*4+2] = modelMatrix.value[2*4+2];
	}
	
	public static void buildModelMatrix(GeneralMatrixFloat cameraTransformMatrix,GeneralMatrixFloat modelMatrix)
	{
		//GeneralMatrixFloat.invert(cameraTransformMatrix,modelMatrix);
		//*
		modelMatrix.value[0*4+0] = cameraTransformMatrix.value[0*4+0];
		modelMatrix.value[1*4+0] = cameraTransformMatrix.value[0*4+1];
		modelMatrix.value[2*4+0] = cameraTransformMatrix.value[0*4+2];

		modelMatrix.value[0*4+1] = -cameraTransformMatrix.value[1*4+0];
		modelMatrix.value[1*4+1] = -cameraTransformMatrix.value[1*4+1];
		modelMatrix.value[2*4+1] = -cameraTransformMatrix.value[1*4+2];

		modelMatrix.value[0*4+2] = -cameraTransformMatrix.value[2*4+0];
		modelMatrix.value[1*4+2] = -cameraTransformMatrix.value[2*4+1];
		modelMatrix.value[2*4+2] = -cameraTransformMatrix.value[2*4+2];

		float x = -cameraTransformMatrix.value[3*4+0];
		float y = -cameraTransformMatrix.value[3*4+1];
		float z = -cameraTransformMatrix.value[3*4+2];
		
		//needs to be rotated like the rest of the points in space
		float tx = modelMatrix.value[0*4+0]*x+modelMatrix.value[1*4+0]*y+modelMatrix.value[2*4+0]*z;
		float ty = modelMatrix.value[0*4+1]*x+modelMatrix.value[1*4+1]*y+modelMatrix.value[2*4+1]*z;
		float tz = modelMatrix.value[0*4+2]*x+modelMatrix.value[1*4+2]*y+modelMatrix.value[2*4+2]*z;
		modelMatrix.value[3*4+0] = tx;
		modelMatrix.value[3*4+1] = ty;
		modelMatrix.value[3*4+2] = tz;		
		//*/
	}

	public static void buildModelMatrix2(GeneralMatrixFloat cameraTransformMatrix,GeneralMatrixFloat modelMatrix)
	{
		//GeneralMatrixFloat.invert(cameraTransformMatrix,modelMatrix);
		//*
		modelMatrix.value[0*4+0] = cameraTransformMatrix.value[0*4+0];
		modelMatrix.value[1*4+0] = cameraTransformMatrix.value[0*4+1];
		modelMatrix.value[2*4+0] = cameraTransformMatrix.value[0*4+2];

		modelMatrix.value[0*4+1] = cameraTransformMatrix.value[1*4+0];
		modelMatrix.value[1*4+1] = cameraTransformMatrix.value[1*4+1];
		modelMatrix.value[2*4+1] = cameraTransformMatrix.value[1*4+2];

		modelMatrix.value[0*4+2] = -cameraTransformMatrix.value[2*4+0];
		modelMatrix.value[1*4+2] = -cameraTransformMatrix.value[2*4+1];
		modelMatrix.value[2*4+2] = -cameraTransformMatrix.value[2*4+2];

		float x = -cameraTransformMatrix.value[3*4+0];
		float y = -cameraTransformMatrix.value[3*4+1];
		float z = -cameraTransformMatrix.value[3*4+2];
		
		//needs to be rotated like the rest of the points in space
		float tx = modelMatrix.value[0*4+0]*x+modelMatrix.value[1*4+0]*y+modelMatrix.value[2*4+0]*z;
		float ty = modelMatrix.value[0*4+1]*x+modelMatrix.value[1*4+1]*y+modelMatrix.value[2*4+1]*z;
		float tz = modelMatrix.value[0*4+2]*x+modelMatrix.value[1*4+2]*y+modelMatrix.value[2*4+2]*z;
		modelMatrix.value[3*4+0] = tx;
		modelMatrix.value[3*4+1] = ty;
		modelMatrix.value[3*4+2] = tz;		
		//*/
	}
	
	public static void buildModelMatrix(GeneralMatrixDouble cameraTransformMatrix,GeneralMatrixDouble modelMatrix)
	{
		//GeneralMatrixFloat.invert(cameraTransformMatrix,modelMatrix);
		//*
		modelMatrix.value[0*4+0] = cameraTransformMatrix.value[0*4+0];
		modelMatrix.value[1*4+0] = cameraTransformMatrix.value[0*4+1];
		modelMatrix.value[2*4+0] = cameraTransformMatrix.value[0*4+2];

		modelMatrix.value[0*4+1] = -cameraTransformMatrix.value[1*4+0];
		modelMatrix.value[1*4+1] = -cameraTransformMatrix.value[1*4+1];
		modelMatrix.value[2*4+1] = -cameraTransformMatrix.value[1*4+2];

		modelMatrix.value[0*4+2] = -cameraTransformMatrix.value[2*4+0];
		modelMatrix.value[1*4+2] = -cameraTransformMatrix.value[2*4+1];
		modelMatrix.value[2*4+2] = -cameraTransformMatrix.value[2*4+2];

		double x = -cameraTransformMatrix.value[3*4+0];
		double y = -cameraTransformMatrix.value[3*4+1];
		double z = -cameraTransformMatrix.value[3*4+2];
		
		//needs to be rotated like the rest of the points in space
		double tx = modelMatrix.value[0*4+0]*x+modelMatrix.value[1*4+0]*y+modelMatrix.value[2*4+0]*z;
		double ty = modelMatrix.value[0*4+1]*x+modelMatrix.value[1*4+1]*y+modelMatrix.value[2*4+1]*z;
		double tz = modelMatrix.value[0*4+2]*x+modelMatrix.value[1*4+2]*y+modelMatrix.value[2*4+2]*z;
		modelMatrix.value[3*4+0] = tx;
		modelMatrix.value[3*4+1] = ty;
		modelMatrix.value[3*4+2] = tz;		
		//*/
	}
	
	public static float calculateFOVY(float focallength,double pixelpitchy,int textureHeight)
	{
		double y = pixelpitchy*textureHeight;
		double f = (2.0*focallength)/y;
		float fovy = (float)(2.0 * Math.atan(1.0/f) * 180.0 / Math.PI);
		return fovy;
	}
	public float calculateFOVYFromFocalLength()
	{
		float fovy = (float)(2.0 * Math.atan(1.0/f) * 180.0 / Math.PI);
		return fovy;
	}
	public float calculateFOVYFromProjectionMatrix()
	{
		f = projectionMatrix.value[1*4+1];
		return calculateFOVYFromFocalLength();
	}
	public static float calculateFOVYFromProjectionMatrix(GeneralMatrixFloat projectionMatrix)
	{
		float f = projectionMatrix.value[1*4+1];
		float fovy = (float)(2.0 * Math.atan(1.0/f) * 180.0 / Math.PI);
		return fovy;
	}
	public void setFromFocalLengthAndPixelPitch(double focalLength,float pixelpitchy,double zNear,double zFar,int width,int height)
	{
		this.centrePixelOffsetx = 0.0f;
		this.centrePixelOffsetx = 0.0f;
		this.width = width;
		this.height = height;
		this.zNear = zNear;
		this.zFar = zFar;
		this.dpx = pixelpitchy;
		this.dpy = pixelpitchy;
	    double y = pixelpitchy*height;
	    this.f = (2.0*focalLength)/y;
	    
	    buildMatrixes();
	}
	public void setFOVY(double fovy)
	{
	    double sine;
	    double radians = (fovy / 2) * Math.PI / 180;
	    sine = Math.sin(radians);
	    this.f = Math.cos(radians) / sine;
		double deltaZ = zFar - zNear;
		double aspect = ((double)width)/((double)height);
		projectionMatrix.setIdentity();
	    projectionMatrix.value[0*4+0] = (float)(f / aspect);
	    projectionMatrix.value[1*4+1] = (float)(f);
	    projectionMatrix.value[2*4+2] = (float)(-(zFar + zNear) / deltaZ);
	    projectionMatrix.value[2*4+3] = -1.0f;
	    projectionMatrix.value[3*4+2] = (float)(-2.0 * zNear * zFar / deltaZ);
	    projectionMatrix.value[3*4+3] = 0.0f;
	}
	public void setFromFOVY(double fovy,double zNear,double zFar,int width,int height)
	{
		this.centrePixelOffsetx = 0.0f;
		this.centrePixelOffsetx = 0.0f;
		this.width = width;
		this.height = height;
		this.zNear = zNear;
		this.zFar = zFar;
	    double sine;
	    double radians = (fovy / 2) * Math.PI / 180;
	    sine = Math.sin(radians);
	    this.f = Math.cos(radians) / sine;
	    buildMatrixes();
	}
	public void setScreenSpace(double zNear,double zFar,int width,int height)
	{
		this.centrePixelOffsetx = 0.0f;
		this.centrePixelOffsety = 0.0f;
		this.width = width;
		this.height = height;
		this.zNear = zNear;
		this.zFar = zFar;
		modelMatrix.setIdentity();
		projectionMatrix.setIdentity();
		double aspect = ((double)width)/((double)height);
	    projectionMatrix.value[0*4+0] = (float)(1.0 / aspect);
		double deltaZ = zFar - zNear;
	    projectionMatrix.value[2*4+2] = (float)(1.0f / deltaZ);
	    projectionMatrix.value[3*4+2] = (float)-(zNear/ deltaZ);
	}
	void buildMatrixes()
	{
		modelMatrix.setIdentity();
		modelMatrix.value[0*4+0] = -1.0f;
		modelMatrix.value[1*4+1] = -1.0f;
		cameraTransformMatrix.setIdentity();
		double deltaZ = zFar - zNear;
		double aspect = ((double)width)/((double)height);
		projectionMatrix.setIdentity();
	    projectionMatrix.value[0*4+0] = (float)(f / aspect);
	    projectionMatrix.value[1*4+1] = (float)(f);
	    projectionMatrix.value[2*4+2] = (float)(-(zFar + zNear) / deltaZ);
	    projectionMatrix.value[2*4+3] = -1.0f;
	    projectionMatrix.value[3*4+2] = (float)(-2.0 * zNear * zFar / deltaZ);
	    projectionMatrix.value[3*4+3] = 0.0f;
	}
	public void setFromOrthogonalScale(float scale,float zn,float zf)
	{
		zNear = zn;
		zFar = zf;
		projectionMatrix.setIdentity();
		float deltaZ = (float)(zFar - zNear);
		double aspect = ((double)width)/((double)height);
	    projectionMatrix.value[0*4+0] = (float)(scale / aspect);
	    projectionMatrix.value[1*4+1] = (float)(scale);
	    projectionMatrix.value[2*4+2] = 1.0f / deltaZ;
	    projectionMatrix.value[3*4+2] = -((float)zNear)/deltaZ;
	}

	public void project3DAndCalcScale(GeneralMatrixFloat objToWorld,GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		if(objToWorld!=null)
		{
			GeneralMatrixFloat.mult(objToWorld, modelMatrix, objToCamera);			
		}
		else
		{
			objToCamera.set(modelMatrix);
		}
		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * objToCamera.value[0*4+i] +
				wp.value[wpi+1] * objToCamera.value[1*4+i] +
				wp.value[wpi+2] * objToCamera.value[2*4+i] +
				1.0f * objToCamera.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if ((postProj[3] == 0.0f)||(postModel[2]>0.0f))
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;
		    postProj[2] /= postProj[3];
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    //Calc z scale for this point (i.e. how far a x or y world point movement translates to image movement for a given z
		    postModel[0] = 1.0f;
		    postModel[1] = 0.0f;
		    postModel[2] = postModel[2];
		    i = 0;
			postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
		    i = 3;
			postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];

			postProj[0] /= postProj[3];
		    postProj[0] = postProj[0] * 0.5f;
		    sp.value[spi+3] = postProj[0] * width;
		    
		    
		    wpi += wp.width;
		    spi += sp.width;
		}
	}
	
	public void projectAndCalcScale(GeneralMatrixFloat objToWorld,GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		if(objToWorld!=null)
		{
			GeneralMatrixFloat.mult(objToWorld, modelMatrix, objToCamera);			
		}
		else
		{
			objToCamera.set(modelMatrix);
		}
		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * objToCamera.value[0*4+i] +
				wp.value[wpi+1] * objToCamera.value[1*4+i] +
				wp.value[wpi+2] * objToCamera.value[2*4+i] +
				1.0f * objToCamera.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if ((postProj[3] == 0.0f)||(postModel[2]>0.0f))
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    //Calc z scale for this point (i.e. how far a x or y world point movement translates to image movement for a given z
		    postModel[0] = 1.0f;
		    postModel[1] = 0.0f;
		    postModel[2] = postModel[2];
		    i = 0;
			postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
		    i = 3;
			postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];

			postProj[0] /= postProj[3];
		    postProj[0] = postProj[0] * 0.5f;
		    sp.value[spi+2] = postProj[0] * width;
		    
		    
		    wpi += wp.width;
		    spi += sp.width;
		}
	}

	//Project a buffer of 3D points
	//*
	public void projectNoZFlip(GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * modelMatrix.value[0*4+i] +
				wp.value[wpi+1] * modelMatrix.value[1*4+i] +
				wp.value[wpi+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    // Map x, y and z to range 0-1 
		    float z = -postProj[2] * 0.5f + 0.5f;
		    z *= (float)(zFar-zNear);
		    z+=zNear;
		    if(z<0.0f)
		    {
		    	postProj[0] = -postProj[0];
		    	postProj[1] = -postProj[1];
		    }
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    
		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += wp.width;
		    spi += sp.width;
		}
	}

	public void project(GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * modelMatrix.value[0*4+i] +
				wp.value[wpi+1] * modelMatrix.value[1*4+i] +
				wp.value[wpi+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += wp.width;
		    spi += sp.width;
		}
	}

	public void projectDistortedSimplified(GeneralMatrixFloat wp,GeneralMatrixFloat sp, float scale)
	{
		final float SQRT3 = (float)Math.sqrt(3.0);

	    //float s = 1.0f+r2*0.173266f+r2*r2*0.020083f;
	    
	    //kappa1 = 0.173266f;
	    kappa1 = 0.020083f;
	    //kappa1 = 1.0f;

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				(wp.value[pi*3+0]) * modelMatrix.value[0*4+i] * scale +
				(wp.value[pi*3+2]-0.4f) * modelMatrix.value[1*4+i] * scale +
				(wp.value[pi*3+1]) * modelMatrix.value[2*4+i] * scale +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    sp.value[spi+0] = (postProj[0]);
		    sp.value[spi+1] = 480-(postProj[1]);

		    float dx = sp.value[spi+0]-320.0f;
		    float dy = sp.value[spi+0]-240.0f;
		    dx /= 320.0f;
		    dy /= 320.0f;
//		    dx /= 240.0f;
//		    dy /= 240.0f;
		    		    
		    float s = 1.0f;
		    if(Math.abs(kappa1)>0.00000000000001f)
		    {
		    float Ru = (float)Math.hypot(dx, dy);
		    float cc = 1.0f/kappa1;
		    float d = -cc*Ru;
		    float Q = cc/3.0f;
		    float R = -d/2.0f;
		    float D = Q*Q*Q+R*R;
		    
		    float Rd;
		    if(D>=0)
		    {
		    	D = (float)Math.sqrt(D);
		    	float S = CBRT(R+D);
		    	float T = CBRT(R-D);
		    	Rd = S+T;
		    	
		    	if (Rd < 0) {
		    	    Rd = (float)Math.sqrt (-1 / (3 * kappa1));
		    	}
		    }
		    else 
		    {			/* three real roots */
		    	D = (float)Math.sqrt(-D);
		    	float S = CBRT ((float)Math.hypot (R, D));
		    	float T = (float)(Math.atan2 (D, R) / 3.0);
		    	float sinT = (float)Math.sin(T);
		    	float cosT = (float)Math.cos(T);

		    	/* the larger positive root is    2*S*cos(T)                   */
		    	/* the smaller positive root is   -S*cos(T) + SQRT(3)*S*sin(T) */
		    	/* the negative root is           -S*cos(T) - SQRT(3)*S*sin(T) */

		    	Rd = -S * cosT + SQRT3 * S * sinT;	/* use the smaller positive root */
		    }

		    	s = Rd/Ru;
		    }

		    float r2 = dx*dx+dy*dy;
		    s = 1.0f+kappa1*r2-kappa1*r2*r2;
		    s = 1.0f;
		    
		    sp.value[spi+0] = (sp.value[spi+0]-320.0f)*s+320.0f;
		    sp.value[spi+1] = (sp.value[spi+1]-240.0f)*s+240.0f;
		    
		    wpi += 3;
		    spi += 3;
		}
	}
	
	public void projectDistorted(GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		float aspect = width/(float)height;
		//float aspect = height/(float)width;

		final float SQRT3 = (float)Math.sqrt(3.0);

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[pi*3+0] * modelMatrix.value[0*4+i] +
				wp.value[pi*3+1] * modelMatrix.value[1*4+i] +
				wp.value[pi*3+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
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
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    	
		    float sensorscalex = postProj[0]*aspect;
		    float sensorscaley = postProj[1];
		    
		    float scale = 1.0f;
		    
		    if(Math.abs(kappa1)>0.00000000000001f)
		    {
		    float Ru = (float)Math.hypot(sensorscalex, sensorscaley);
		    float cc = 1.0f/kappa1;
		    float d = -cc*Ru;
		    float Q = cc/3.0f;
		    float R = -d/2.0f;
		    float D = Q*Q*Q+R*R;
		    
		    float Rd;
		    if(D>=0)
		    {
		    	D = (float)Math.sqrt(D);
		    	float S = CBRT(R+D);
		    	float T = CBRT(R-D);
		    	Rd = S+T;
		    	
		    	if (Rd < 0) {
		    	    Rd = (float)Math.sqrt (-1 / (3 * kappa1));
		    	}
		    }
		    else 
		    {			/* three real roots */
		    	D = (float)Math.sqrt(-D);
		    	float S = CBRT ((float)Math.hypot (R, D));
		    	float T = (float)(Math.atan2 (D, R) / 3.0);
		    	float sinT = (float)Math.sin(T);
		    	float cosT = (float)Math.cos(T);

		    	/* the larger positive root is    2*S*cos(T)                   */
		    	/* the smaller positive root is   -S*cos(T) + SQRT(3)*S*sin(T) */
		    	/* the negative root is           -S*cos(T) - SQRT(3)*S*sin(T) */

		    	Rd = -S * cosT + SQRT3 * S * sinT;	/* use the smaller positive root */
		    }

		    	scale = Rd/Ru;
		    }
//		    float undistsqr = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
//		    float innerdivide = 1.0f-c.kappa1*undistsqr;
//		    float scale = 1.0f-c.kappa1*(undistsqr/innerdivide);
		    
		    postProj[0] *= scale;
		    postProj[1] *= scale;

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)*sx+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += 3;
		    spi += 3;
		}
	}

	public void projectDistorted_2(GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		float aspect = width/(float)height;
		//float aspect = height/(float)width;

		final float SQRT3 = (float)Math.sqrt(3.0);

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[pi*3+0] * modelMatrix.value[0*4+i] +
				wp.value[pi*3+1] * modelMatrix.value[1*4+i] +
				wp.value[pi*3+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
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
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    	
			float sensorscalex = postProj[0]*aspect;
		    float sensorscaley = postProj[1];
		    
		    float R2 = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
		    float R4 = R2*R2;
		    
		    float scale = 1.0f;

		    scale += k1*R2+k2*R4;
		    
		    postProj[0] *= scale*sx;
		    postProj[1] *= scale;

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += 3;
		    spi += 3;
		}
	}	//*/
	
	public static void projectDistorted_2(
			GeneralMatrixFloat modelMatrix,GeneralMatrixFloat projectionMatrix,
			float width,float height,float centrePixelOffsetx,float centrePixelOffsety,float sx,double k1,double k2,
			GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		float aspect = width/(float)height;
		//float aspect = height/(float)width;

		//final float SQRT3 = (float)Math.sqrt(3.0);

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * modelMatrix.value[0*4+i] +
				wp.value[wpi+1] * modelMatrix.value[1*4+i] +
				wp.value[wpi+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += 3;
			    spi += 3;
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
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    	
			float sensorscalex = postProj[0]*aspect;
		    float sensorscaley = postProj[1];
		    
		    float R2 = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
		    float R4 = R2*R2;
		    
		    float scale = 1.0f;

		    scale += k1*R2+k2*R4;
		    
		    postProj[0] *= scale*sx;
		    postProj[1] *= scale;

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += 3;
		    spi += 3;
		}
	}	//*/

	public static void projectDistorted_2(
			GeneralMatrixFloat transMatrix,GeneralMatrixFloat modelMatrix,GeneralMatrixFloat projectionMatrix,
			float width,float height,float centrePixelOffsetx,float centrePixelOffsety,float sx,double k1,double k2,
			GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		float aspect = width/(float)height;
		//float aspect = height/(float)width;

		//final float SQRT3 = (float)Math.sqrt(3.0);

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postTrans[i] = 
				wp.value[wpi+0] * transMatrix.value[0*4+i] +
				wp.value[wpi+1] * transMatrix.value[1*4+i] +
				wp.value[wpi+2] * transMatrix.value[2*4+i] +
				1.0f * transMatrix.value[3*4+i];
			}
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				postTrans[0] * modelMatrix.value[0*4+i] +
				postTrans[1] * modelMatrix.value[1*4+i] +
				postTrans[2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += 3;
			    spi += 3;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    //Debug for 
		    if(
		    		//(postProj[0]<-1.0f)||(postProj[0]>1.0f)||
		    		//(postProj[1]<-1.0f)||(postProj[1]>1.0f)||
		    		(postProj[2]<-1.0f)||(postProj[2]>1.0f)
		      )
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    	
			float sensorscalex = postProj[0]*aspect;
		    float sensorscaley = postProj[1];
		    
		    float R2 = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
		    float R4 = R2*R2;
		    
		    float scale = 1.0f;

		    scale += k1*R2+k2*R4;
		    
		    postProj[0] *= scale*sx;
		    postProj[1] *= scale;

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += 3;
		    spi += 3;
		}
	}	//*/

	public void project(GeneralMatrixFloat objToWorld,GeneralMatrixFloat wp,GeneralMatrixFloat sp)
	{
		GeneralMatrixFloat.mult(objToWorld, modelMatrix, objToCamera);

		int i;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<wp.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				wp.value[wpi+0] * objToCamera.value[0*4+i] +
				wp.value[wpi+1] * objToCamera.value[1*4+i] +
				wp.value[wpi+2] * objToCamera.value[2*4+i] +
				1.0f * objToCamera.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += wp.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    // Map x, y and z to range 0-1 
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    sp.value[spi+2] = postProj[2] * 0.5f + 0.5f;

		    // Map x,y to viewport 
		    sp.value[spi+0] = (postProj[0] * width)+centrePixelOffsetx;
		    sp.value[spi+1] = (postProj[1] * height)+centrePixelOffsety;

		    wpi += wp.width;
		    spi += sp.width;
		}
	}
	
	public void projectAndClip(GeneralMatrixFloat objToWorld,GeneralMatrixFloat op,GeneralMatrixFloat sp,int[] clipcode)
	{
		//compose the objToWorld and worldToCamera
		GeneralMatrixFloat.mult(objToWorld, modelMatrix, objToCamera);
		//GeneralMatrixFloat.mult(modelMatrix, objToWorld, objToCamera);
		//objToCamera.set(modelMatrix);
		
		int ccode;
		int i;
		//float[] postModel = new float[3];
		//float[] postProj = new float[4];
		
		float x,y,z;
		
		int wpi = 0;
		int spi = 0;
		for (int pi=0;pi<op.height;pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
				op.value[wpi+0] * objToCamera.value[0*4+i] +
				op.value[wpi+1] * objToCamera.value[1*4+i] +
				op.value[wpi+2] * objToCamera.value[2*4+i] +
				1.0f * objToCamera.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	sp.value[spi+0] = Float.MAX_VALUE;
		    	sp.value[spi+1] = Float.MAX_VALUE;
		    	sp.value[spi+2] = Float.MAX_VALUE;
			    wpi += op.width;
			    spi += sp.width;
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    /* Map x, y and z to range 0-1 */
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    z = postProj[2] * 0.5f + 0.5f;

			/* Map x,y to viewport */
		    x = (postProj[0] * width);
		    y = (postProj[1] * height);

		    ccode=0;
			if (x<0.0f) ccode|=1;
			if (x>=(width-0.5f)) ccode|=2;
			if (y<0.0f) ccode|=4;
			if (y>=(height-0.5f)) ccode|=8;
			if (z<0.0f) ccode|=16;
			if (z>1.0f) ccode|=32;
			clipcode[pi] = ccode;
			
			x += centrePixelOffsetx;
			y += centrePixelOffsety;
			
		    sp.value[spi+0] = x;
		    sp.value[spi+1] = y;
		    sp.value[spi+2] = z;
			
			wpi += op.width;
		    spi += sp.width;
		}
	
	}

	public void inverseRender(float[] worldpixelpositions,RenderBuffer cameraview,RenderBuffer worldview)
	{
		int i;
		int wpi = 0;
		float fx,fy,fz;
		for(int pi=0;pi<(worldview.width*worldview.height);pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
					worldpixelpositions[wpi+0] * modelMatrix.value[0*4+i] +
					worldpixelpositions[wpi+1] * modelMatrix.value[1*4+i] +
					worldpixelpositions[wpi+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    /* Map x, y and z to range 0-1 */
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    fz = postProj[2] * 0.5f + 0.5f;

			/* Map x,y to viewport */
		    fx = (postProj[0] * width);
		    fy = (postProj[1] * height);

			fx += centrePixelOffsetx;
			fy += centrePixelOffsety;
			
			int lx = (int)(fx);
			int ly = (int)(fy);
			float fdx = fx-lx;
			float fdy = fy-ly;
			float ifdx = 1.0f-fdx;
			float ifdy = 1.0f-fdy;
			
			if((lx<0)||(lx>=(cameraview.width-1))||(ly<0)||(ly>=(cameraview.height-1)))
			{
				wpi+=3;
				continue;
			}
			
			int ind = lx+ly*cameraview.width;
			int ll = cameraview.pixel[ind];
			int hl = cameraview.pixel[ind+1];
			int lh = cameraview.pixel[ind+cameraview.width];
			int hh = cameraview.pixel[ind+1+cameraview.width];
			
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
			
			worldview.pixel[pi] = (r<<16)|(g<<8)|(b); 

			wpi += 3;
		}
	}

	float CBRT (float x)
	{
	    if (x == 0)
		return (0);
	    else if (x > 0)
		return (float)(Math.pow (x, 1.0 / 3.0));
	    else
		return (float)(-Math.pow (-x, 1.0 / 3.0));
	} 

	public void inverseRenderDistorted(float[] worldpixelpositions,RenderBuffer cameraview,RenderBuffer worldview)
	{
		int i;
		int wpi = 0;
		float fx,fy,fz;
		
		float aspect = cameraview.height/(float)cameraview.width;
		float cx = 0.5f*centrePixelOffsetx/cameraview.width;
		float cy = 0.5f*centrePixelOffsety/cameraview.height;
		final float SQRT3 = (float)Math.sqrt(3.0);
		for(int pi=0;pi<(worldview.width*worldview.height);pi++)
		{
			for (i=0; i<3; i++) 
			{
				postModel[i] = 
					worldpixelpositions[wpi+0] * modelMatrix.value[0*4+i] +
					worldpixelpositions[wpi+1] * modelMatrix.value[1*4+i] +
					worldpixelpositions[wpi+2] * modelMatrix.value[2*4+i] +
				1.0f * modelMatrix.value[3*4+i];
			}
			for (i=0; i<4; i++) 
			{
				postProj[i] = 
				postModel[0] * projectionMatrix.value[0*4+i] +
				postModel[1] * projectionMatrix.value[1*4+i] +
				postModel[2] * projectionMatrix.value[2*4+i] +
				1.0f * projectionMatrix.value[3*4+i];
			}

		    if (postProj[3] == 0.0)
		    {
		    	continue;
		    }
		    postProj[0] /= postProj[3];
		    postProj[1] /= postProj[3];
		    postProj[2] /= postProj[3];

		    float sensorscalex = postProj[0]-cx;
		    float sensorscaley = postProj[1]-cy;
		    
		    float Ru = (float)Math.hypot(sensorscalex, sensorscaley);
		    float c = 1.0f/kappa1;
		    float d = -c*Ru;
		    float Q = c/3.0f;
		    float R = -d/2.0f;
		    float D = Q*Q*Q+R*R;
		    
		    float Rd;
		    if(D>=0)
		    {
		    	D = (float)Math.sqrt(D);
		    	float S = CBRT(R+D);
		    	float T = CBRT(R-D);
		    	Rd = S+T;
		    	
		    	if (Rd < 0) {
		    	    Rd = (float)Math.sqrt (-1 / (3 * kappa1));
//		    	    fprintf (stderr, "\nWarning: undistorted image point to distorted image point mapping limited by\n");
//		    	    fprintf (stderr, "         maximum barrel distortion radius of %lf\n", Rd);
//		    	    fprintf (stderr, "         (Xu = %lf, Yu = %lf) -> (Xd = %lf, Yd = %lf)\n\n",
//		    		     Xu, Yu, Xu * Rd / Ru, Yu * Rd / Ru);
		    	}
		    }
		    else 
		    {			/* three real roots */
		    	D = (float)Math.sqrt(-D);
		    	float S = CBRT ((float)Math.hypot (R, D));
		    	float T = (float)(Math.atan2 (D, R) / 3.0);
		    	float sinT = (float)Math.sin(T);
		    	float cosT = (float)Math.cos(T);

		    	/* the larger positive root is    2*S*cos(T)                   */
		    	/* the smaller positive root is   -S*cos(T) + SQRT(3)*S*sin(T) */
		    	/* the negative root is           -S*cos(T) - SQRT(3)*S*sin(T) */

		    	Rd = -S * cosT + SQRT3 * S * sinT;	/* use the smaller positive root */
		    }

		    float scale = Rd/Ru;
		    
//		    float undistsqr = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
//		    float innerdivide = 1.0f-kappa1*undistsqr;
//		    float scale = 1.0f-kappa1*(undistsqr/innerdivide);
//		    
		    postProj[0] *= scale;
		    postProj[1] *= scale;
		    
		    /* Map x, y and z to range 0-1 */
		    postProj[0] = postProj[0] * 0.5f + 0.5f;
		    postProj[1] = postProj[1] * 0.5f + 0.5f;
		    fz = postProj[2] * 0.5f + 0.5f;

			/* Map x,y to viewport */
		    fx = (postProj[0] * width);
		    fy = (postProj[1] * height);

			fx += centrePixelOffsetx;
			fy += centrePixelOffsety;
			
			int lx = (int)(fx);
			int ly = (int)(fy);
			float fdx = fx-lx;
			float fdy = fy-ly;
			float ifdx = 1.0f-fdx;
			float ifdy = 1.0f-fdy;
			
			if((lx<0)||(lx>=(cameraview.width-1))||(ly<0)||(ly>=(cameraview.height-1)))
			{
				wpi+=3;
				continue;
			}
			
			int ind = lx+ly*cameraview.width;
			int ll = cameraview.pixel[ind];
			int hl = cameraview.pixel[ind+1];
			int lh = cameraview.pixel[ind+cameraview.width];
			int hh = cameraview.pixel[ind+1+cameraview.width];
			
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
			
			worldview.pixel[pi] = (r<<16)|(g<<8)|(b); 

			wpi += 3;
		}
	}
	
	public void unprojectplane(float x,float y,GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float nx = (x-centrePixelOffsetx)/((float)width); 
		float ny = (y-centrePixelOffsety)/((float)height); 
		
		nx = nx*2.0f - 1.0f;
		ny = ny*2.0f - 1.0f;

		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
		
		if(objToWorld==null)
			temp.set(modelMatrix);
		else
			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		GeneralMatrixFloat.invert(temp2,temp);
		
		//Assuming that the original z is 0 work out the oriented z
		float nz = -(temp.value[14]+nx*temp.value[2]+ny*temp.value[6])/temp.value[10];
		
		sp.value[0] = nx;
		sp.value[1] = ny;
		sp.value[2] = nz;
		//sp.value[2] = nz;
		sp.value[3] = 1.0f;
		
		GeneralMatrixFloat.mult(sp,temp,hop);
		
		op.value[0] = hop.value[0] / hop.value[3];
		op.value[1] = hop.value[1] / hop.value[3];
		op.value[2] = hop.value[2] / hop.value[3];		
//		
//		for (int i=0; i<3; i++) 
//		{
//			postModel[i] = 
//				op.value[0] * modelMatrix.value[0*4+i] +
//				op.value[1] * modelMatrix.value[1*4+i] +
//				op.value[2] * modelMatrix.value[2*4+i] +
//			1.0f * modelMatrix.value[3*4+i];
//		}
//		for (int i=0; i<4; i++) 
//		{
//			postProj[i] = 
//			postModel[0] * projectionMatrix.value[0*4+i] +
//			postModel[1] * projectionMatrix.value[1*4+i] +
//			postModel[2] * projectionMatrix.value[2*4+i] +
//			1.0f * projectionMatrix.value[3*4+i];
//		}
//
//	    if (postProj[3] == 0.0)
//	    {
//	    	System.out.println("Error");
//	    	return;
//	    }
//
//	    postProj[0] /= postProj[3];
//	    postProj[1] /= postProj[3];
//	    postProj[2] /= postProj[3];
//
//	    float errorx = postProj[0]-nx;
//	    float errory = postProj[1]-ny;
//	    System.out.println("Errorx="+errorx);
//	    System.out.println("Errory="+errory);
	}
	public void unproject(float x,float y,float z,GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float nx = (x-centrePixelOffsetx)/((float)width); 
		float ny = (y-centrePixelOffsety)/((float)height); 
		float nz = z/((float)0x7FFFFFFF);
		
		nx = nx*2.0f - 1.0f;
		ny = ny*2.0f - 1.0f;
		nz = nz*2.0f - 1.0f;
		
		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
		
		if(objToWorld==null)
			temp.set(modelMatrix);
		else
			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		GeneralMatrixFloat.invert(temp2,temp);
		
		sp.value[0] = nx;
		sp.value[1] = ny;
		sp.value[2] = nz;
		sp.value[3] = 1.0f;
		
		GeneralMatrixFloat.mult(sp,temp,hop);
		
		op.value[0] = hop.value[0] / hop.value[3];
		op.value[1] = hop.value[1] / hop.value[3];
		op.value[2] = hop.value[2] / hop.value[3];
	}

	public void unprojectDistorted(float x,float y,float z,GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float nx = (x-centrePixelOffsetx)/((float)width*sx); 
		float ny = (y-centrePixelOffsety)/((float)height); 
		float nz = z/((float)0x7FFFFFFF);
		
		nx = nx*2.0f - 1.0f;
		ny = ny*2.0f - 1.0f;
		nz = nz*2.0f - 1.0f;
		
		double dnx = (x-centrePixelOffsetx)/((float)height);
		dnx = dnx*2.0 - 1.0;
		double distortion_factor = 1 + kappa1 * (dnx*dnx + ny*ny);
		nx = (float)(nx * distortion_factor);
		ny = (float)(ny * distortion_factor);

		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
		
		if(objToWorld==null)
			temp.set(modelMatrix);
		else
			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		GeneralMatrixFloat.invert(temp2,temp);
		
		sp.value[0] = nx;
		sp.value[1] = ny;
		sp.value[2] = nz;
		sp.value[3] = 1.0f;
		
		GeneralMatrixFloat.mult(sp,temp,hop);
		
		op.value[0] = hop.value[0] / hop.value[3];
		op.value[1] = hop.value[1] / hop.value[3];
		op.value[2] = hop.value[2] / hop.value[3];
	}
	
	public static void unprojectDistorted(float x,float y,float z,
			GeneralMatrixFloat modelMatrix,GeneralMatrixFloat projectionMatrix,
			float width,float height,float centrePixelOffsetx,float centrePixelOffsety,float sx,double k1,double k2,
			GeneralMatrixFloat objToWorld,GeneralMatrixFloat op)
	{
		float nx = (x-centrePixelOffsetx)/((float)width*sx); 
		float ny = (y-centrePixelOffsety)/((float)height); 
		float nz = z/((float)0x7FFFFFFF);
		
		nx = nx*2.0f - 1.0f;
		ny = ny*2.0f - 1.0f;
		nz = nz*2.0f - 1.0f;
		
		double dnx = (x-centrePixelOffsetx)/((float)height);
		dnx = dnx*2.0 - 1.0;

		double distsensorscalesqr = dnx*dnx+ny*ny;
		
		//distsensorscalesqr = origsensorscalesqr*(1+k1*origsensorscalesqr+k2*origsensorscalesqr*origsensorscalesqr)
		
		//this is a cubic equation
		double d = -distsensorscalesqr;
		double c = 1;
		double b = k1;
		double a = k2;
		
		//by simplifying the general solution from http://en.wikipedia.org/wiki/Cubic_function we can solve to find the result
		
		//we require a single root solution which means that this determinant must be negative
		double distortion_factor = 1.0;
		
		double determinant = 18*a*b*c*d-4*b*b*b*d+b*b*c*c-4*a*c*c*c-27*a*a*d*d;
		
		if(determinant<0.0)
		{
			double deter0 = b*b-3*a*c;
			double deter1 = 2*b*b*b-9*a*b*c+27*a*a*d;
			double C0 = Math.sqrt(-27*a*a*determinant);
			double C = Math.pow((deter1+C0)*0.5, 1.0/3.0);
			double origsensorscalesqr = -(b+C+deter0/C)/(3.0*a);
			
	//	    float R2 = sensorscalex*sensorscalex+sensorscaley*sensorscaley;
	//	    float R4 = R2*R2;
	//	    
	//	    float scale = 1.0f;
	//	    scale += k1*R2+k2*R4;
	
			double distsensord = Math.sqrt(distsensorscalesqr);
			double origsensord = Math.sqrt(origsensorscalesqr);
			
		    //double distortion_factor = 1 + kappa1 * (dnx*dnx + ny*ny);
			distortion_factor = origsensord/distsensord;
		}
		nx = (float)(nx * distortion_factor);
		ny = (float)(ny * distortion_factor);

		GeneralMatrixFloat sp = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat hop = new GeneralMatrixFloat(4,1);
		GeneralMatrixFloat temp = new GeneralMatrixFloat(4,4);
		GeneralMatrixFloat temp2 = new GeneralMatrixFloat(4,4);
		
		if(objToWorld==null)
			temp.set(modelMatrix);
		else
			GeneralMatrixFloat.mult(objToWorld,modelMatrix,temp);
		GeneralMatrixFloat.mult(temp,projectionMatrix,temp2);
		boolean success = GeneralMatrixFloat.invert(temp2,temp);
		
		sp.value[0] = nx;
		sp.value[1] = ny;
		sp.value[2] = nz;
		sp.value[3] = 1.0f;

		if(success==false)
		{
			SVD svd = new SVD(4, 4);
			svd.init(temp2);
			svd.solve(sp, hop, 0.000001f);
			op.value[0] = hop.value[0];
			op.value[1] = hop.value[1];
			op.value[2] = hop.value[2];
		}
		else
		{			
			GeneralMatrixFloat.mult(sp,temp,hop);			
			op.value[0] = hop.value[0] / hop.value[3];
			op.value[1] = hop.value[1] / hop.value[3];
			op.value[2] = hop.value[2] / hop.value[3];
		}
	}
	
	//Clipping
	public boolean isCulled(float x,float y,float z,float r)
	{
		for(int clip_i=0;clip_i<6;clip_i++)
		{
			int clip_ind = clip_i*4;
			float clip_da = clipPlanes.value[0+clip_ind]*x+clipPlanes.value[1+clip_ind]*y+clipPlanes.value[2+clip_ind]*z;
			clip_da -= clipPlanes.value[3+clip_ind];
			
			if((clip_da<-r))
				return true;
		}
		return false;
	}
	//Calculate how far from the camera a point is (for culling purposes)
	public float squareDistanceTo(GeneralMatrixFloat worldPoint)
	{
		float cdx = cameraTransformMatrix.value[0*4+4]-worldPoint.value[0];
		float cdy = cameraTransformMatrix.value[1*4+4]-worldPoint.value[1];
		float cdz = cameraTransformMatrix.value[2*4+4]-worldPoint.value[2];
		return cdx*cdx+cdy*cdy+cdz*cdz;
	}

	public void buildClipPlanes()
	{
		double fov = calculateFOVYFromFocalLength()*(Math.PI/360.0);
		double sin = Math.sin(fov);
		//double cos = Math.cos(fov);
		double l = zFar;
		double farHeight = l*sin;
		double farWidth = farHeight*width/height;
		//double ln = zNear/cos;
		//double nearHeight = ln*sin;
		//double nearWidth = nearHeight*width/height;
		
		float x,y,z;
		float dist;
		int ind = 0;
		double xlen = Math.sqrt(farWidth*farWidth+zFar*zFar);
		//Left plane
		z = (float)(farWidth/xlen);
		y = 0.0f;
		x = (float)(zFar/xlen);
		clipPlanes.value[4*ind+0] = cameraTransformMatrix.value[0]*x+cameraTransformMatrix.value[8+0]*z;
		clipPlanes.value[4*ind+1] = cameraTransformMatrix.value[1]*x+cameraTransformMatrix.value[8+1]*z;
		clipPlanes.value[4*ind+2] = cameraTransformMatrix.value[2]*x+cameraTransformMatrix.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += cameraTransformMatrix.value[12+0]*clipPlanes.value[4*ind+0];
		dist += cameraTransformMatrix.value[12+1]*clipPlanes.value[4*ind+1];
		dist += cameraTransformMatrix.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
		
		//Right plane
		z = (float)(farWidth/xlen);
		y = 0.0f;
		x = -(float)(zFar/xlen);
		clipPlanes.value[4*ind+0] = cameraTransformMatrix.value[0]*x+cameraTransformMatrix.value[8+0]*z;
		clipPlanes.value[4*ind+1] = cameraTransformMatrix.value[1]*x+cameraTransformMatrix.value[8+1]*z;
		clipPlanes.value[4*ind+2] = cameraTransformMatrix.value[2]*x+cameraTransformMatrix.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += cameraTransformMatrix.value[12+0]*clipPlanes.value[4*ind+0];
		dist += cameraTransformMatrix.value[12+1]*clipPlanes.value[4*ind+1];
		dist += cameraTransformMatrix.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		double ylen = Math.sqrt(farHeight*farHeight+zFar*zFar);
		//Bottom plane
		x = 0.0f;
		z = (float)(farHeight/ylen);
		y = -(float)(zFar/ylen);
		clipPlanes.value[4*ind+0] = cameraTransformMatrix.value[4+0]*y+cameraTransformMatrix.value[8+0]*z;
		clipPlanes.value[4*ind+1] = cameraTransformMatrix.value[4+1]*y+cameraTransformMatrix.value[8+1]*z;
		clipPlanes.value[4*ind+2] = cameraTransformMatrix.value[4+2]*y+cameraTransformMatrix.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += cameraTransformMatrix.value[12+0]*clipPlanes.value[4*ind+0];
		dist += cameraTransformMatrix.value[12+1]*clipPlanes.value[4*ind+1];
		dist += cameraTransformMatrix.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		//Top plane
		x = 0.0f;
		z = (float)(farHeight/ylen);
		y = (float)(zFar/ylen);
		clipPlanes.value[4*ind+0] = cameraTransformMatrix.value[4+0]*y+cameraTransformMatrix.value[8+0]*z;
		clipPlanes.value[4*ind+1] = cameraTransformMatrix.value[4+1]*y+cameraTransformMatrix.value[8+1]*z;
		clipPlanes.value[4*ind+2] = cameraTransformMatrix.value[4+2]*y+cameraTransformMatrix.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += cameraTransformMatrix.value[12+0]*clipPlanes.value[4*ind+0];
		dist += cameraTransformMatrix.value[12+1]*clipPlanes.value[4*ind+1];
		dist += cameraTransformMatrix.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		
		//Near plane
		clipPlanes.value[4*ind+0] = cameraTransformMatrix.value[8+0];
		clipPlanes.value[4*ind+1] = cameraTransformMatrix.value[8+1];
		clipPlanes.value[4*ind+2] = cameraTransformMatrix.value[8+2];
		x = cameraTransformMatrix.value[12+0]+cameraTransformMatrix.value[8+0]*(float)zNear;
		y = cameraTransformMatrix.value[12+1]+cameraTransformMatrix.value[8+1]*(float)zNear;
		z = cameraTransformMatrix.value[12+2]+cameraTransformMatrix.value[8+2]*(float)zNear;
		dist = 0.0f;
		dist += x*clipPlanes.value[4*ind+0];
		dist += y*clipPlanes.value[4*ind+1];
		dist += z*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
		                 
		//Far plane
		clipPlanes.value[4*ind+0] = -cameraTransformMatrix.value[8+0];
		clipPlanes.value[4*ind+1] = -cameraTransformMatrix.value[8+1];
		clipPlanes.value[4*ind+2] = -cameraTransformMatrix.value[8+2];
		x = cameraTransformMatrix.value[12+0]+cameraTransformMatrix.value[8+0]*(float)zFar;
		y = cameraTransformMatrix.value[12+1]+cameraTransformMatrix.value[8+1]*(float)zFar;
		z = cameraTransformMatrix.value[12+2]+cameraTransformMatrix.value[8+2]*(float)zFar;
		dist = 0.0f;
		dist += x*clipPlanes.value[4*ind+0];
		dist += y*clipPlanes.value[4*ind+1];
		dist += z*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
	}
	
	public static void buildClipPlanes(GeneralMatrixFloat clipPlanes,
			float zNear,float zFar,
			double fov,double width,double height,
			float px,float py,float pz,float tx,float ty,float tz, float roll)
	{
		calcTransformFromLookat(px, py, pz, tx, ty, tz, roll, objToCamera);
		//double fov = calculateFOVY()*0.25f;
		fov *= (Math.PI/360.0);
		double sin = Math.sin(fov);
		//double cos = Math.cos(fov);
		//double l = farPlane/cos;
		double farHeight = zFar*sin;
		double farWidth = farHeight*width/height;

		float x,y,z;
		float dist;
		int ind = 0;
		double xlen = Math.sqrt(farWidth*farWidth+zFar*zFar);
		//Left plane
		z = (float)(farWidth/xlen);
		y = 0.0f;
		x = (float)(zFar/xlen);
		clipPlanes.value[4*ind+0] = objToCamera.value[0]*x+objToCamera.value[8+0]*z;
		clipPlanes.value[4*ind+1] = objToCamera.value[1]*x+objToCamera.value[8+1]*z;
		clipPlanes.value[4*ind+2] = objToCamera.value[2]*x+objToCamera.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += objToCamera.value[12+0]*clipPlanes.value[4*ind+0];
		dist += objToCamera.value[12+1]*clipPlanes.value[4*ind+1];
		dist += objToCamera.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
		
		//Right plane
		z = (float)(farWidth/xlen);
		y = 0.0f;
		x = -(float)(zFar/xlen);
		clipPlanes.value[4*ind+0] = objToCamera.value[0]*x+objToCamera.value[8+0]*z;
		clipPlanes.value[4*ind+1] = objToCamera.value[1]*x+objToCamera.value[8+1]*z;
		clipPlanes.value[4*ind+2] = objToCamera.value[2]*x+objToCamera.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += objToCamera.value[12+0]*clipPlanes.value[4*ind+0];
		dist += objToCamera.value[12+1]*clipPlanes.value[4*ind+1];
		dist += objToCamera.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		double ylen = Math.sqrt(farHeight*farHeight+zFar*zFar);
		//Bottom plane
		x = 0.0f;
		z = (float)(farHeight/ylen);
		y = -(float)(zFar/ylen);
		clipPlanes.value[4*ind+0] = objToCamera.value[4+0]*y+objToCamera.value[8+0]*z;
		clipPlanes.value[4*ind+1] = objToCamera.value[4+1]*y+objToCamera.value[8+1]*z;
		clipPlanes.value[4*ind+2] = objToCamera.value[4+2]*y+objToCamera.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += objToCamera.value[12+0]*clipPlanes.value[4*ind+0];
		dist += objToCamera.value[12+1]*clipPlanes.value[4*ind+1];
		dist += objToCamera.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		//Top plane
		x = 0.0f;
		z = (float)(farHeight/ylen);
		y = (float)(zFar/ylen);
		clipPlanes.value[4*ind+0] = objToCamera.value[4+0]*y+objToCamera.value[8+0]*z;
		clipPlanes.value[4*ind+1] = objToCamera.value[4+1]*y+objToCamera.value[8+1]*z;
		clipPlanes.value[4*ind+2] = objToCamera.value[4+2]*y+objToCamera.value[8+2]*z;
		//Transform the clip planes with the camera transform
		dist = 0.0f;
		dist += objToCamera.value[12+0]*clipPlanes.value[4*ind+0];
		dist += objToCamera.value[12+1]*clipPlanes.value[4*ind+1];
		dist += objToCamera.value[12+2]*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;

		
		//Near plane
		clipPlanes.value[4*ind+0] = objToCamera.value[8+0];
		clipPlanes.value[4*ind+1] = objToCamera.value[8+1];
		clipPlanes.value[4*ind+2] = objToCamera.value[8+2];
		x = objToCamera.value[12+0]+objToCamera.value[8+0]*(float)zNear;
		y = objToCamera.value[12+1]+objToCamera.value[8+1]*(float)zNear;
		z = objToCamera.value[12+2]+objToCamera.value[8+2]*(float)zNear;
		dist = 0.0f;
		dist += x*clipPlanes.value[4*ind+0];
		dist += y*clipPlanes.value[4*ind+1];
		dist += z*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
		                 
		//Far plane
		clipPlanes.value[4*ind+0] = -objToCamera.value[8+0];
		clipPlanes.value[4*ind+1] = -objToCamera.value[8+1];
		clipPlanes.value[4*ind+2] = -objToCamera.value[8+2];
		x = objToCamera.value[12+0]+objToCamera.value[8+0]*(float)zFar;
		y = objToCamera.value[12+1]+objToCamera.value[8+1]*(float)zFar;
		z = objToCamera.value[12+2]+objToCamera.value[8+2]*(float)zFar;
		dist = 0.0f;
		dist += x*clipPlanes.value[4*ind+0];
		dist += y*clipPlanes.value[4*ind+1];
		dist += z*clipPlanes.value[4*ind+2];
		clipPlanes.value[4*ind+3] = dist;
		ind++;
	}
	
	
	public void buildFrustumVerts(GeneralMatrixFloat verts)
	{
		double fov = calculateFOVYFromFocalLength()*(Math.PI/360.0);
		double sin = Math.sin(fov);
		//double cos = Math.cos(fov);
		double l = zFar;
		double farHeight = l*sin;
		double farWidth = farHeight*width/height;
		
		int ind = 0;
		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		
		double nearScale = zNear/zFar;
		verts.value[3*ind+0] = (float)(nearScale*-farWidth);
		verts.value[3*ind+1] = (float)(nearScale*-farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*farWidth);
		verts.value[3*ind+1] = (float)(nearScale*-farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*-farWidth);
		verts.value[3*ind+1] = (float)(nearScale*farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*farWidth);
		verts.value[3*ind+1] = (float)(nearScale*farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);

		GeneralMatrixFloat.rowtransform(verts, cameraTransformMatrix, verts);
	}

	public static void buildFrustumVerts(GeneralMatrixFloat verts,float farPlane,
			double fov,double width,double height,
			
			float x,float y,float z,float tx,float ty,float tz, float roll)
{
		calcTransformFromLookat(x, y, z, tx, ty, tz, roll, objToCamera);
		buildFrustumVerts(verts, farPlane, fov, width, height, objToCamera);
}
	
	public static void buildFrustumVerts(GeneralMatrixFloat verts,float nearPlane,float farPlane,
			GeneralMatrixFloat projectionMatrix,
			GeneralMatrixFloat objToCamera)
{
//double fov = calculateFOVY()*0.25f;
		float f = projectionMatrix.value[1*4+1];
		double fov = (Math.atan(1.0/f));
		double sin = Math.sin(fov);
//double cos = Math.cos(fov);
//double l = farPlane/cos;
		double aspect = f/projectionMatrix.value[0*4+0];
	    double farHeight = farPlane*sin;
	    double farWidth = farHeight*aspect;

int ind = 0;

verts.value[3*ind+0] = 0.0f;
verts.value[3*ind+1] = 0.0f;
verts.value[3*ind+2] = 0.0f;
ind++;

verts.value[3*ind+0] = (float)-farWidth;
verts.value[3*ind+1] = (float)-farHeight;
verts.value[3*ind+2] = (float)farPlane;
ind++;
verts.value[3*ind+0] = (float)farWidth;
verts.value[3*ind+1] = (float)-farHeight;
verts.value[3*ind+2] = (float)farPlane;
ind++;
verts.value[3*ind+0] = (float)-farWidth;
verts.value[3*ind+1] = (float)farHeight;
verts.value[3*ind+2] = (float)farPlane;
ind++;
verts.value[3*ind+0] = (float)farWidth;
verts.value[3*ind+1] = (float)farHeight;
verts.value[3*ind+2] = (float)farPlane;
ind++;

double nearScale = nearPlane/farPlane;
verts.value[3*ind+0] = (float)(nearScale*-farWidth);
verts.value[3*ind+1] = (float)(nearScale*-farHeight);
verts.value[3*ind+2] = (float)(nearScale*farPlane);
ind++;
verts.value[3*ind+0] = (float)(nearScale*farWidth);
verts.value[3*ind+1] = (float)(nearScale*-farHeight);
verts.value[3*ind+2] = (float)(nearScale*farPlane);
ind++;
verts.value[3*ind+0] = (float)(nearScale*-farWidth);
verts.value[3*ind+1] = (float)(nearScale*farHeight);
verts.value[3*ind+2] = (float)(nearScale*farPlane);
ind++;
verts.value[3*ind+0] = (float)(nearScale*farWidth);
verts.value[3*ind+1] = (float)(nearScale*farHeight);
verts.value[3*ind+2] = (float)(nearScale*farPlane);

GeneralMatrixFloat.rowtransform(verts, objToCamera, verts);
}

	public static void buildFrustumVerts(GeneralMatrixFloat verts,float farPlane,
									double fov,double width,double height,
									GeneralMatrixFloat objToCamera)
	{
		//double fov = calculateFOVY()*0.25f;
		fov *= (Math.PI/360.0);
		double sin = Math.sin(fov);
		//double cos = Math.cos(fov);
		//double l = farPlane/cos;
		double farHeight = farPlane*sin;
		double farWidth = farHeight*width/height;
		
		int ind = 0;
		
		verts.value[3*ind+0] = 0.0f;
		verts.value[3*ind+1] = 0.0f;
		verts.value[3*ind+2] = 0.0f;
		ind++;

		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)farPlane;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)farPlane;
		ind++;
		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)farPlane;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)farPlane;
		ind++;

		GeneralMatrixFloat.rowtransform(verts, objToCamera, verts);
	}
	
	public static void buildFrustumVerts(GeneralMatrixFloat verts,float zNear,float zFar,
			double fov,double width,double height,
			float x,float y,float z,float tx,float ty,float tz,float roll)
	{
		calcTransformFromLookat(x, y, z, tx, ty, tz, roll, objToCamera);
		//double fov = calculateFOVY()*0.25f;
		fov *= (Math.PI/360.0);
		double sin = Math.sin(fov);
		//double cos = Math.cos(fov);
		//double l = farPlane/cos;
		double l = zFar;
		double farHeight = l*sin;
		double farWidth = farHeight*width/height;
		
		int ind = 0;
		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)-farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)-farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		verts.value[3*ind+0] = (float)farWidth;
		verts.value[3*ind+1] = (float)farHeight;
		verts.value[3*ind+2] = (float)zFar;
		ind++;
		
		double nearScale = zNear/zFar;
		verts.value[3*ind+0] = (float)(nearScale*-farWidth);
		verts.value[3*ind+1] = (float)(nearScale*-farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*farWidth);
		verts.value[3*ind+1] = (float)(nearScale*-farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*-farWidth);
		verts.value[3*ind+1] = (float)(nearScale*farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		ind++;
		verts.value[3*ind+0] = (float)(nearScale*farWidth);
		verts.value[3*ind+1] = (float)(nearScale*farHeight);
		verts.value[3*ind+2] = (float)(nearScale*zFar);
		
		GeneralMatrixFloat.rowtransform(verts, objToCamera, verts);
	}
}

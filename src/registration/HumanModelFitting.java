package registration;

import procedural.human.Human;
import mathematics.GeneralMatrixFloat;
import mathematics.GeneralMatrixInt;
import mathematics.GeneralMatrixString;

public class HumanModelFitting 
{
	public Human human;
	
	//INFORMATION FOR CALCULATING THE SKELETON FROM OTHER DATA
	//Estimating bones from verts
	//Includes a head pivot which is inaccurate as it ignores tilting
	public GeneralMatrixInt bonepivots = new GeneralMatrixInt(2,7);
	public GeneralMatrixInt bodybones = new GeneralMatrixInt(4,5);

	//Probability distributions and constraints on the skeleton
	//public GeneralMatrixInt boneSymmetries = new GeneralMatrixInt(1,15);
	//public GeneralMatrixFloat vertpositionpriors = new GeneralMatrixFloat(15,15);
	//public GeneralMatrixFloat bonepriors = new GeneralMatrixFloat(6,14);
	
	//Relative distance of verts
	//public GeneralMatrixFloat boneSymmetryPriors = new GeneralMatrixFloat(1,15);
	//What is the mean and sd distance from the bone (if it is greater than this then it is excluded)
	//public GeneralMatrixFloat bonewidthpriors = new GeneralMatrixFloat(2,14);
	

	//Estimating bones from volumes
	public GeneralMatrixString volumeNames = new GeneralMatrixString(1,6);
	public GeneralMatrixString volumeNames_detailed = new GeneralMatrixString(1,10);
	public GeneralMatrixString volumeNames_detailed2 = new GeneralMatrixString(1,12);
	//Estimates of regions where different body parts can be found
	//one each for the legs head and body with 3 for the arms (front, side and back)
	public GeneralMatrixFloat volumePriors = new GeneralMatrixFloat(8,4+3+3);
	//Information to estimate verts from volumes
	public GeneralMatrixFloat volumeVertexMappings = new GeneralMatrixFloat(12,17);
	public GeneralMatrixFloat VolumeVertexMappings_detailed = new GeneralMatrixFloat(12,17);
	public GeneralMatrixFloat VolumeVertexMappings_detailed2 = new GeneralMatrixFloat(12,17);
	//To map volumes to bone widths
	public GeneralMatrixFloat volumeWidthMappings = new GeneralMatrixFloat(3,16);
	public GeneralMatrixFloat volumeWidthMappings_detailed = new GeneralMatrixFloat(3,16);
	public GeneralMatrixFloat volumeWidthMappings_detailed2 = new GeneralMatrixFloat(3,16);

	//To map volumes to bones so that they can be reconstructed from them
	//volume and then 1,2 or 3 bones
	public GeneralMatrixInt volumeToBoneMappings = new GeneralMatrixInt(4,6);
	public GeneralMatrixInt volumeToBoneMappings_detailed = new GeneralMatrixInt(4,10);
	public GeneralMatrixInt volumeToBoneMappings_detailed2 = new GeneralMatrixInt(4,12);

	public HumanModelFitting(float width, float height, float depth)
	{
		human = new Human(width,height,depth);
		initAlignmentInformation();
		//initPriors(width, height, depth);
		initVolumePriors(width, height, depth);
		initVolumeVertexMappings();
		initVolumeVertexMappings_detailed();
		initVolumeVertexMappings_detailed2();
		initVolumeWidthMappings();
		initVolumeWidthMappings_detailed();
		initVolumeWidthMappings_detailed2();
		
		initvolumeToBoneMappings();
		initvolumeToBoneMappingsDetailed();		
		initvolumeToBoneMappingsDetailed2();		
	}
	
	public void initvolumeToBoneMappings()
	{
//		bones.value[0*2+0] = 0; bones.value[0*2+1] = 1;	boneNames.value[0] = "skull";
//		bones.value[1*2+0] = 1; bones.value[1*2+1] = 2; boneNames.value[1] = "lshoulder";
//		bones.value[2*2+0] = 1; bones.value[2*2+1] = 5; boneNames.value[2] = "rshoulder";
//		bones.value[3*2+0] = 2; bones.value[3*2+1] = 3; boneNames.value[3] = "lhumerous";
//		bones.value[4*2+0] = 3; bones.value[4*2+1] = 4; boneNames.value[4] = "lulna";
//		bones.value[5*2+0] = 5; bones.value[5*2+1] = 6; boneNames.value[5] = "rhumerous";
//		bones.value[6*2+0] = 6; bones.value[6*2+1] = 7; boneNames.value[6] = "rulna";
//		bones.value[7*2+0] = 1; bones.value[7*2+1] = 8; boneNames.value[7] = "spine";
//		bones.value[8*2+0] = 8; bones.value[8*2+1] = 9; boneNames.value[8] = "lpelvis";
//		bones.value[9*2+0] = 9; bones.value[9*2+1] = 10; boneNames.value[9] = "lfemur";
//		bones.value[10*2+0] = 10; bones.value[10*2+1] = 11; boneNames.value[10] = "ltibia";
//		bones.value[11*2+0] = 8; bones.value[11*2+1] = 12; boneNames.value[11] = "rpelvis";
//		bones.value[12*2+0] = 12; bones.value[12*2+1] = 13; boneNames.value[12] = "rfemur";
//		bones.value[13*2+0] = 13; bones.value[13*2+1] = 14; boneNames.value[13] = "rtibia";
//		s.bones.value[14*2+0] = 11; s.bones.value[14*2+1] = 15; s.boneNames.value[14] = "lfoot";
//		s.bones.value[15*2+1] = 14; s.bones.value[15*2+1] = 16; s.boneNames.value[15] = "rfoot";

//		volumeNames.value[0] = "Head";
//		volumeNames.value[1] = "Torso";
//		volumeNames.value[2] = "LLeg";
//		volumeNames.value[3] = "RLeg";
//		volumeNames.value[4] = "LArm";
//		volumeNames.value[5] = "RArm";
		
		volumeToBoneMappings.clear(-1);
		
		int i=0;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 0; 	//head volume
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 0; 	//head bone
		i++;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 1; 	//torso volume
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 7; 	//spine bone

		i++;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 2; 	//LLeg
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 9; 	//lfemur
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+2] = 10; 	//ltibia
		//volumeToBoneMappings.value[i*volumeToBoneMappings.width+3] = 15; 	//lfoot

		i++;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 3; 	//RLeg
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 12; 	//rfemur
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+2] = 13; 	//rtibia
		//volumeToBoneMappings.value[i*volumeToBoneMappings.width+3] = 16; 	//rfoot

		i++;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 4; 	//LArm
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 3; 	//humerous
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+2] = 4; 	//ulna
		i++;
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+0] = 5; 	//RArm
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+1] = 5; 	//humerous
		volumeToBoneMappings.value[i*volumeToBoneMappings.width+2] = 6; 	//ulna
	}

	public void initvolumeToBoneMappingsDetailed()
	{
//		volumeNames_detailed.value[0] = "Head";
//		volumeNames_detailed.value[1] = "Torso";
//		volumeNames_detailed.value[2] = "LLegU";
//		volumeNames_detailed.value[3] = "RLegU";
//		volumeNames_detailed.value[4] = "LArmU";
//		volumeNames_detailed.value[5] = "RArmU";
//		volumeNames_detailed.value[6] = "LLegL";
//		volumeNames_detailed.value[7] = "RLegL";
//		volumeNames_detailed.value[8] = "LArmL";
//		volumeNames_detailed.value[9] = "RArmL";
		
		volumeToBoneMappings_detailed.clear(-1);

		int i=0;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 0; 	//head volume
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 0; 	//head bone
		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 1; 	//torso volume
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 7; 	//spine bone

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 2; 	//LLegU
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 9; 	//lfemur

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 3; 	//RLegU
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 12; 	//rfemur

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 4; 	//LArmU
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 3; 	//humerous
		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 5; 	//RArmU
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 5; 	//humerous

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 6; 	//LLegL
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 10; 	//lfemur

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 7; 	//RLeg
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 13; 	//rfemur

		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 8; 	//LArm
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 4; 	//humerous
		i++;
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+0] = 9; 	//RArm
		volumeToBoneMappings_detailed.value[i*volumeToBoneMappings_detailed.width+1] = 6; 	//humerous
	}
	
	public void initvolumeToBoneMappingsDetailed2()
	{
//		volumeNames_detailed2.value[0] = "Head";
//		volumeNames_detailed2.value[1] = "Torso";
//		volumeNames_detailed2.value[2] = "LLegU";
//		volumeNames_detailed2.value[3] = "RLegU";
//		volumeNames_detailed2.value[4] = "LArmU";
//		volumeNames_detailed2.value[5] = "RArmU";
//		volumeNames_detailed2.value[6] = "LLegL";
//		volumeNames_detailed2.value[7] = "RLegL";
//		volumeNames_detailed2.value[8] = "LArmL";
//		volumeNames_detailed2.value[9] = "RArmL";
//		volumeNames_detailed2.value[10] = "LFoot";
//		volumeNames_detailed2.value[11] = "RFoot";
		
		volumeToBoneMappings_detailed2.clear(-1);

		int i=0;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 0; 	//head volume
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 0; 	//head bone
		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 1; 	//torso volume
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 7; 	//spine bone

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 2; 	//LLegU
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 9; 	//lfemur

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 3; 	//RLegU
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 12; 	//rfemur

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 4; 	//LArmU
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 3; 	//humerous
		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 5; 	//RArmU
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 5; 	//humerous

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 6; 	//LLegL
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 10; 	//lfemur

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 7; 	//RLeg
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 13; 	//rfemur

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 8; 	//LArm
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 4; 	//humerous
		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 9; 	//RArm
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 6; 	//humerous

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 10; 	//LFoot
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 14; 	//lfoot

		i++;
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+0] = 11; 	//RFoot
		volumeToBoneMappings_detailed2.value[i*volumeToBoneMappings_detailed2.width+1] = 15; 	//rfoot
	}
	
	public void initVolumeWidthMappings_detailed()
	{
		volumeWidthMappings_detailed.clear(0.0f);
		//Have a flag indicating that both widths should be the smaller of the two and the same (i.e. for shoulder and pelvis bones)
		
//		bones.value[0*2+0] = 0; bones.value[0*2+1] = 1;	boneNames.value[0] = "skull";
//		bones.value[1*2+0] = 1; bones.value[1*2+1] = 2; boneNames.value[1] = "lshoulder";
//		bones.value[2*2+0] = 1; bones.value[2*2+1] = 5; boneNames.value[2] = "rshoulder";
//		bones.value[3*2+0] = 2; bones.value[3*2+1] = 3; boneNames.value[3] = "lhumerous";
//		bones.value[4*2+0] = 3; bones.value[4*2+1] = 4; boneNames.value[4] = "lulna";
//		bones.value[5*2+0] = 5; bones.value[5*2+1] = 6; boneNames.value[5] = "rhumerous";
//		bones.value[6*2+0] = 6; bones.value[6*2+1] = 7; boneNames.value[6] = "rulna";
//		bones.value[7*2+0] = 1; bones.value[7*2+1] = 8; boneNames.value[7] = "spine";
//		bones.value[8*2+0] = 8; bones.value[8*2+1] = 9; boneNames.value[8] = "lpelvis";
//		bones.value[9*2+0] = 9; bones.value[9*2+1] = 10; boneNames.value[9] = "lfemur";
//		bones.value[10*2+0] = 10; bones.value[10*2+1] = 11; boneNames.value[10] = "ltibia";
//		bones.value[11*2+0] = 8; bones.value[11*2+1] = 12; boneNames.value[11] = "rpelvis";
//		bones.value[12*2+0] = 12; bones.value[12*2+1] = 13; boneNames.value[12] = "rfemur";
//		bones.value[13*2+0] = 13; bones.value[13*2+1] = 14; boneNames.value[13] = "rtibia";
		
		int i=0;		
		volumeWidthMappings_detailed.value[i+0] = 0;
		volumeWidthMappings_detailed.value[i+1] = 0;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 1;
		volumeWidthMappings_detailed.value[i+1] = 1;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 2;
		volumeWidthMappings_detailed.value[i+1] = 1;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 3;
		volumeWidthMappings_detailed.value[i+1] = 4;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 4;
		volumeWidthMappings_detailed.value[i+1] = 8;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 5;
		volumeWidthMappings_detailed.value[i+1] = 5;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 6;
		volumeWidthMappings_detailed.value[i+1] = 9;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 7;
		volumeWidthMappings_detailed.value[i+1] = 1;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 8;
		volumeWidthMappings_detailed.value[i+1] = 1;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 9;
		volumeWidthMappings_detailed.value[i+1] = 2;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 10;
		volumeWidthMappings_detailed.value[i+1] = 6;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 11;
		volumeWidthMappings_detailed.value[i+1] = 1;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 12;
		volumeWidthMappings_detailed.value[i+1] = 3;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
		volumeWidthMappings_detailed.value[i+0] = 13;
		volumeWidthMappings_detailed.value[i+1] = 7;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;

		volumeWidthMappings_detailed.value[i+0] = 14;
		volumeWidthMappings_detailed.value[i+1] = 6;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;

		volumeWidthMappings_detailed.value[i+0] = 15;
		volumeWidthMappings_detailed.value[i+1] = 7;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed.width;
	}
	
	public void initVolumeWidthMappings_detailed2()
	{
		volumeWidthMappings_detailed2.clear(0.0f);
		//Have a flag indicating that both widths should be the smaller of the two and the same (i.e. for shoulder and pelvis bones)
		
//		bones.value[0*2+0] = 0; bones.value[0*2+1] = 1;	boneNames.value[0] = "skull";
//		bones.value[1*2+0] = 1; bones.value[1*2+1] = 2; boneNames.value[1] = "lshoulder";
//		bones.value[2*2+0] = 1; bones.value[2*2+1] = 5; boneNames.value[2] = "rshoulder";
//		bones.value[3*2+0] = 2; bones.value[3*2+1] = 3; boneNames.value[3] = "lhumerous";
//		bones.value[4*2+0] = 3; bones.value[4*2+1] = 4; boneNames.value[4] = "lulna";
//		bones.value[5*2+0] = 5; bones.value[5*2+1] = 6; boneNames.value[5] = "rhumerous";
//		bones.value[6*2+0] = 6; bones.value[6*2+1] = 7; boneNames.value[6] = "rulna";
//		bones.value[7*2+0] = 1; bones.value[7*2+1] = 8; boneNames.value[7] = "spine";
//		bones.value[8*2+0] = 8; bones.value[8*2+1] = 9; boneNames.value[8] = "lpelvis";
//		bones.value[9*2+0] = 9; bones.value[9*2+1] = 10; boneNames.value[9] = "lfemur";
//		bones.value[10*2+0] = 10; bones.value[10*2+1] = 11; boneNames.value[10] = "ltibia";
//		bones.value[11*2+0] = 8; bones.value[11*2+1] = 12; boneNames.value[11] = "rpelvis";
//		bones.value[12*2+0] = 12; bones.value[12*2+1] = 13; boneNames.value[12] = "rfemur";
//		bones.value[13*2+0] = 13; bones.value[13*2+1] = 14; boneNames.value[13] = "rtibia";
		
		int i=0;		
		volumeWidthMappings_detailed2.value[i+0] = 0;
		volumeWidthMappings_detailed2.value[i+1] = 0;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 1;
		volumeWidthMappings_detailed2.value[i+1] = 1;
		volumeWidthMappings_detailed2.value[i+2] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 2;
		volumeWidthMappings_detailed2.value[i+1] = 1;
		volumeWidthMappings_detailed2.value[i+2] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 3;
		volumeWidthMappings_detailed2.value[i+1] = 4;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 4;
		volumeWidthMappings_detailed2.value[i+1] = 8;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 5;
		volumeWidthMappings_detailed2.value[i+1] = 5;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 6;
		volumeWidthMappings_detailed2.value[i+1] = 9;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 7;
		volumeWidthMappings_detailed2.value[i+1] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 8;
		volumeWidthMappings_detailed2.value[i+1] = 1;
		volumeWidthMappings_detailed2.value[i+2] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 9;
		volumeWidthMappings_detailed2.value[i+1] = 2;
		volumeWidthMappings_detailed.value[i+2] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 10;
		volumeWidthMappings_detailed2.value[i+1] = 6;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 11;
		volumeWidthMappings_detailed2.value[i+1] = 1;
		volumeWidthMappings_detailed2.value[i+2] = 1;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 12;
		volumeWidthMappings_detailed2.value[i+1] = 3;
		i+=volumeWidthMappings_detailed2.width;
		volumeWidthMappings_detailed2.value[i+0] = 13;
		volumeWidthMappings_detailed2.value[i+1] = 7;
		i+=volumeWidthMappings_detailed2.width;

		volumeWidthMappings_detailed2.value[i+0] = 14;
		volumeWidthMappings_detailed2.value[i+1] = 10;
		i+=volumeWidthMappings_detailed2.width;

		volumeWidthMappings_detailed2.value[i+0] = 15;
		volumeWidthMappings_detailed2.value[i+1] = 11;
		i+=volumeWidthMappings_detailed2.width;
	}

	public void initVolumeWidthMappings()
	{
		volumeWidthMappings.clear(0.0f);
		//Have a flag indicating that both widths should be the smaller of the two and the same (i.e. for shoulder and pelvis bones)
		
		int i=0;		
		volumeWidthMappings.value[i+0] = 0;
		volumeWidthMappings.value[i+1] = 0;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 1;
		volumeWidthMappings.value[i+1] = 1;
		volumeWidthMappings.value[i+2] = 1;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 2;
		volumeWidthMappings.value[i+1] = 1;
		volumeWidthMappings.value[i+2] = 1;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 3;
		volumeWidthMappings.value[i+1] = 4;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 4;
		volumeWidthMappings.value[i+1] = 4;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 5;
		volumeWidthMappings.value[i+1] = 5;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 6;
		volumeWidthMappings.value[i+1] = 5;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 7;
		volumeWidthMappings.value[i+1] = 1;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 8;
		volumeWidthMappings.value[i+1] = 1;
		volumeWidthMappings.value[i+2] = 1;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 9;
		volumeWidthMappings.value[i+1] = 2;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 10;
		volumeWidthMappings.value[i+1] = 2;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 11;
		volumeWidthMappings.value[i+1] = 1;
		volumeWidthMappings.value[i+2] = 1;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 12;
		volumeWidthMappings.value[i+1] = 3;
		i+=volumeWidthMappings.width;
		volumeWidthMappings.value[i+0] = 13;
		volumeWidthMappings.value[i+1] = 3;
		i+=volumeWidthMappings.width;
	}
	
	public void initVolumePriors(float width,float height,float depth)
	{
		volumeNames.value[0] = "Head";
		volumeNames.value[1] = "Torso";
		volumeNames.value[2] = "LLeg";
		volumeNames.value[3] = "RLeg";
		volumeNames.value[4] = "LArm";
		volumeNames.value[5] = "RArm";

		volumeNames_detailed.value[0] = "Head";
		volumeNames_detailed.value[1] = "Torso";
		volumeNames_detailed.value[2] = "LLegU";
		volumeNames_detailed.value[3] = "RLegU";
		volumeNames_detailed.value[4] = "LArmU";
		volumeNames_detailed.value[5] = "RArmU";
		volumeNames_detailed.value[6] = "LLegL";
		volumeNames_detailed.value[7] = "RLegL";
		volumeNames_detailed.value[8] = "LArmL";
		volumeNames_detailed.value[9] = "RArmL";
		
		volumeNames_detailed2.value[0] = "Head";
		volumeNames_detailed2.value[1] = "Torso";
		volumeNames_detailed2.value[2] = "LLegU";
		volumeNames_detailed2.value[3] = "RLegU";
		volumeNames_detailed2.value[4] = "LArmU";
		volumeNames_detailed2.value[5] = "RArmU";
		volumeNames_detailed2.value[6] = "LLegL";
		volumeNames_detailed2.value[7] = "RLegL";
		volumeNames_detailed2.value[8] = "LArmL";
		volumeNames_detailed2.value[9] = "RArmL";
		volumeNames_detailed2.value[10] = "LFoot";
		volumeNames_detailed2.value[11] = "RFoot";
		
		int i=0;
		
		//Head prior
		//Minx,y,z
		volumePriors.value[volumePriors.width*i+0] = 0;
		volumePriors.value[volumePriors.width*i+1] = width*0.25f;
		volumePriors.value[volumePriors.width*i+2] = height*0.87f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.25f;
		volumePriors.value[volumePriors.width*i+4] = width*0.75f;
		volumePriors.value[volumePriors.width*i+5] = height;
		volumePriors.value[volumePriors.width*i+6] = depth*0.75f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		//Torso
		volumePriors.value[volumePriors.width*i+0] = 1;
		volumePriors.value[volumePriors.width*i+1] = width*0.25f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.25f;
		volumePriors.value[volumePriors.width*i+4] = width*0.75f;
		volumePriors.value[volumePriors.width*i+5] = height*0.87f;
		volumePriors.value[volumePriors.width*i+6] = depth*0.75f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		//LLeg
		volumePriors.value[volumePriors.width*i+0] = 2;
		volumePriors.value[volumePriors.width*i+1] = width*0.0f;
		volumePriors.value[volumePriors.width*i+2] = height*0.0f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*0.5f;
		volumePriors.value[volumePriors.width*i+5] = height*0.535f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		//RLeg
		volumePriors.value[volumePriors.width*i+0] = 3;
		volumePriors.value[volumePriors.width*i+1] = width*0.5f;
		volumePriors.value[volumePriors.width*i+2] = height*0.0f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*1.0f;
		volumePriors.value[volumePriors.width*i+5] = height*0.535f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		
		//LArm
		volumePriors.value[volumePriors.width*i+0] = 4;
		volumePriors.value[volumePriors.width*i+1] = width*0.0f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*0.25f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		volumePriors.value[volumePriors.width*i+0] = 4;
		volumePriors.value[volumePriors.width*i+1] = width*0.0f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.75f;
		volumePriors.value[volumePriors.width*i+4] = width*0.5f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		volumePriors.value[volumePriors.width*i+0] = 4;
		volumePriors.value[volumePriors.width*i+1] = width*0.0f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*0.5f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*0.25f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;

		//RArm
		volumePriors.value[volumePriors.width*i+0] = 5;
		volumePriors.value[volumePriors.width*i+1] = width*0.75f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*1.0f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		volumePriors.value[volumePriors.width*i+0] = 5;
		volumePriors.value[volumePriors.width*i+1] = width*0.5f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.75f;
		volumePriors.value[volumePriors.width*i+4] = width*1.0f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*1.0f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
		volumePriors.value[volumePriors.width*i+0] = 5;
		volumePriors.value[volumePriors.width*i+1] = width*0.5f;
		volumePriors.value[volumePriors.width*i+2] = height*0.535f;
		volumePriors.value[volumePriors.width*i+3] = depth*0.0f;
		volumePriors.value[volumePriors.width*i+4] = width*1.0f;
		volumePriors.value[volumePriors.width*i+5] = height*1.0f;
		volumePriors.value[volumePriors.width*i+6] = depth*0.25f;
		volumePriors.value[volumePriors.width*i+7] = 1.0f;
		i++;
	}
	
	public void initVolumeVertexMappings_detailed()
	{
//		vetexNames.value[0] = "topofhead";
//		vetexNames.value[1] = "neck";
//		vetexNames.value[2] = "lshoulder";
//		vetexNames.value[3] = "lelbow";
//		vetexNames.value[4] = "lwrist";
//		vetexNames.value[5] = "rshoulder";
//		vetexNames.value[6] = "relbow";
//		vetexNames.value[7] = "rwrist";
//		vetexNames.value[8] = "pelvis";
//		vetexNames.value[9] = "lhip";
//		vetexNames.value[10] = "lknee";
//		vetexNames.value[11] = "lankle";
//		vetexNames.value[12] = "rhip";
//		vetexNames.value[13] = "rknee";
//		vetexNames.value[14] = "rankle";

//		volumeNames_detailed.value[0] = "Head";
//		volumeNames_detailed.value[1] = "Torso";
//		volumeNames_detailed.value[2] = "LLegU";
//		volumeNames_detailed.value[3] = "RLegU";
//		volumeNames_detailed.value[4] = "LArmU";
//		volumeNames_detailed.value[5] = "RArmU";
//		volumeNames_detailed.value[6] = "LLegL";
//		volumeNames_detailed.value[7] = "RLegL";
//		volumeNames_detailed.value[8] = "LArmL";
//		volumeNames_detailed.value[9] = "RArmL";
		int i = 0;
		//Top of head
		VolumeVertexMappings_detailed.value[i+0] = 0;
		VolumeVertexMappings_detailed.value[i+1] = 0; //head
		VolumeVertexMappings_detailed.value[i+2] = 0; //head
		VolumeVertexMappings_detailed.value[i+3] = 5; //top face
		VolumeVertexMappings_detailed.value[i+4] = 5; //top face
		VolumeVertexMappings_detailed.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed.width;
		//Neck
		VolumeVertexMappings_detailed.value[i+0] = 1;
		VolumeVertexMappings_detailed.value[i+1] = 0; //head
		VolumeVertexMappings_detailed.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed.value[i+3] = -5; //Bottom face
		VolumeVertexMappings_detailed.value[i+4] = 5; //top face
		VolumeVertexMappings_detailed.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed.width;

		//Assumes the arm is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//LShoulder
		VolumeVertexMappings_detailed.value[i+0] = 2;
		VolumeVertexMappings_detailed.value[i+1] = 4; //larm
		VolumeVertexMappings_detailed.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //top face
		VolumeVertexMappings_detailed.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed.width;
		//LElbow
		VolumeVertexMappings_detailed.value[i+0] = 3;
		VolumeVertexMappings_detailed.value[i+1] = 4; //larm
		VolumeVertexMappings_detailed.value[i+2] = 8; //larml
		VolumeVertexMappings_detailed.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = 2; //top face
		VolumeVertexMappings_detailed.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed.width;
		//LWrist
		VolumeVertexMappings_detailed.value[i+0] = 4;
		VolumeVertexMappings_detailed.value[i+1] = 8; //larm
		VolumeVertexMappings_detailed.value[i+2] = 8; //torso
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.9f;
		i+=VolumeVertexMappings_detailed.width;

		//RShoulder
		VolumeVertexMappings_detailed.value[i+0] = 5;
		VolumeVertexMappings_detailed.value[i+1] = 5; //rarm
		VolumeVertexMappings_detailed.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed.width;
		//RElbow
		VolumeVertexMappings_detailed.value[i+0] = 6;
		VolumeVertexMappings_detailed.value[i+1] = 5; //rarm
		VolumeVertexMappings_detailed.value[i+2] = 9; //rarm
		VolumeVertexMappings_detailed.value[i+3] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+4] = 2; //top face
		VolumeVertexMappings_detailed.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed.width;
		//RWrist
		VolumeVertexMappings_detailed.value[i+0] = 7;
		VolumeVertexMappings_detailed.value[i+1] = 9; //rarm
		VolumeVertexMappings_detailed.value[i+2] = 9; //rarm
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.9f;
		i+=VolumeVertexMappings_detailed.width;

		//Pelvis
		VolumeVertexMappings_detailed.value[i+0] = 8;
		VolumeVertexMappings_detailed.value[i+1] = 1; //torso
		VolumeVertexMappings_detailed.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed.value[i+3] = 5; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -5; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.7f;
		i+=VolumeVertexMappings_detailed.width;

		//Assumes the leg is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//Lpelvis
		VolumeVertexMappings_detailed.value[i+0] = 9;
		VolumeVertexMappings_detailed.value[i+1] = 2; //lleg
		VolumeVertexMappings_detailed.value[i+2] = 2; //lleg//1; //torso
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = -0.2f;
		i+=VolumeVertexMappings_detailed.width;
		//LKnee
		VolumeVertexMappings_detailed.value[i+0] = 10;
		VolumeVertexMappings_detailed.value[i+1] = 2; //lleg
		VolumeVertexMappings_detailed.value[i+2] = 6; //lleg
		VolumeVertexMappings_detailed.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = 2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed.width;
		//LAnkle
		VolumeVertexMappings_detailed.value[i+0] = 11;
		VolumeVertexMappings_detailed.value[i+1] = 6; //lleg
		VolumeVertexMappings_detailed.value[i+2] = 6; //lleg
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.7f;
		i+=VolumeVertexMappings_detailed.width;

		//Lpelvis
		VolumeVertexMappings_detailed.value[i+0] = 12;
		VolumeVertexMappings_detailed.value[i+1] = 3; //rleg
		VolumeVertexMappings_detailed.value[i+2] = 3; //rleg//1; //torso
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = -0.2f;
		i+=VolumeVertexMappings_detailed.width;
		//LKnee
		VolumeVertexMappings_detailed.value[i+0] = 13;
		VolumeVertexMappings_detailed.value[i+1] = 3; //rleg
		VolumeVertexMappings_detailed.value[i+2] = 7; //rleg
		VolumeVertexMappings_detailed.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = 2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed.width;
		//LAnkle
		VolumeVertexMappings_detailed.value[i+0] = 14;
		VolumeVertexMappings_detailed.value[i+1] = 7; //rleg
		VolumeVertexMappings_detailed.value[i+2] = 7; //rleg
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.7f;
		i+=VolumeVertexMappings_detailed.width;

		//LFoot
		VolumeVertexMappings_detailed.value[i+0] = 15;
		VolumeVertexMappings_detailed.value[i+1] = 6; //lleg
		VolumeVertexMappings_detailed.value[i+2] = 6; //lleg
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.9f;
		VolumeVertexMappings_detailed.value[i+6] = 0.0f; //bone scaled relative offset from first point
		VolumeVertexMappings_detailed.value[i+7] = 0.0f;
		VolumeVertexMappings_detailed.value[i+8] = 1.0f;
		VolumeVertexMappings_detailed.value[i+9] = 0.0f; //bone scaled relative offset from second point
		VolumeVertexMappings_detailed.value[i+10] = 0.0f;
		VolumeVertexMappings_detailed.value[i+11] = 1.0f;
		i+=VolumeVertexMappings_detailed.width;

		//RFoot
		VolumeVertexMappings_detailed.value[i+0] = 16;
		VolumeVertexMappings_detailed.value[i+1] = 7; //rleg
		VolumeVertexMappings_detailed.value[i+2] = 7; //rleg
		VolumeVertexMappings_detailed.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed.value[i+5] = 0.9f;
		VolumeVertexMappings_detailed.value[i+6] = 0.0f; //bone scaled relative offset from first point
		VolumeVertexMappings_detailed.value[i+7] = 0.0f;
		VolumeVertexMappings_detailed.value[i+8] = 1.0f;
		VolumeVertexMappings_detailed.value[i+9] = 0.0f; //bone scaled relative offset from second point
		VolumeVertexMappings_detailed.value[i+10] = 0.0f;
		VolumeVertexMappings_detailed.value[i+11] = 1.0f;
		i+=VolumeVertexMappings_detailed.width;
	}

	public void initVolumeVertexMappings_detailed2()
	{
//		vetexNames.value[0] = "topofhead";
//		vetexNames.value[1] = "neck";
//		vetexNames.value[2] = "lshoulder";
//		vetexNames.value[3] = "lelbow";
//		vetexNames.value[4] = "lwrist";
//		vetexNames.value[5] = "rshoulder";
//		vetexNames.value[6] = "relbow";
//		vetexNames.value[7] = "rwrist";
//		vetexNames.value[8] = "pelvis";
//		vetexNames.value[9] = "lhip";
//		vetexNames.value[10] = "lknee";
//		vetexNames.value[11] = "lankle";
//		vetexNames.value[12] = "rhip";
//		vetexNames.value[13] = "rknee";
//		vetexNames.value[14] = "rankle";

//		volumeNames_detailed.value[0] = "Head";
//		volumeNames_detailed.value[1] = "Torso";
//		volumeNames_detailed.value[2] = "LLegU";
//		volumeNames_detailed.value[3] = "RLegU";
//		volumeNames_detailed.value[4] = "LArmU";
//		volumeNames_detailed.value[5] = "RArmU";
//		volumeNames_detailed.value[6] = "LLegL";
//		volumeNames_detailed.value[7] = "RLegL";
//		volumeNames_detailed.value[8] = "LArmL";
//		volumeNames_detailed.value[9] = "RArmL";
		int i = 0;
		//Top of head
		VolumeVertexMappings_detailed2.value[i+0] = 0;
		VolumeVertexMappings_detailed2.value[i+1] = 0; //head
		VolumeVertexMappings_detailed2.value[i+2] = 0; //head
		VolumeVertexMappings_detailed2.value[i+3] = 5; //top face
		VolumeVertexMappings_detailed2.value[i+4] = 5; //top face
		VolumeVertexMappings_detailed2.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed2.width;
		//Neck
		VolumeVertexMappings_detailed2.value[i+0] = 1;
		VolumeVertexMappings_detailed2.value[i+1] = 0; //head
		VolumeVertexMappings_detailed2.value[i+2] = 0; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 5; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -5; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 1.3f;
		i+=VolumeVertexMappings_detailed2.width;

		//Assumes the arm is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//LShoulder
		VolumeVertexMappings_detailed2.value[i+0] = 2;
		VolumeVertexMappings_detailed2.value[i+1] = 4; //larm
		VolumeVertexMappings_detailed2.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //top face
		VolumeVertexMappings_detailed2.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed2.width;
		//LElbow
		VolumeVertexMappings_detailed2.value[i+0] = 3;
		VolumeVertexMappings_detailed2.value[i+1] = 4; //larm
		VolumeVertexMappings_detailed2.value[i+2] = 8; //larml
		VolumeVertexMappings_detailed2.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 2; //top face
		VolumeVertexMappings_detailed2.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed2.width;
		//LWrist
		VolumeVertexMappings_detailed2.value[i+0] = 4;
		VolumeVertexMappings_detailed2.value[i+1] = 8; //larm
		VolumeVertexMappings_detailed2.value[i+2] = 8; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.6f;
		i+=VolumeVertexMappings_detailed2.width;

		//RShoulder
		VolumeVertexMappings_detailed2.value[i+0] = 5;
		VolumeVertexMappings_detailed2.value[i+1] = 5; //rarm
		VolumeVertexMappings_detailed2.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.0f;
		i+=VolumeVertexMappings_detailed2.width;
		//RElbow
		VolumeVertexMappings_detailed2.value[i+0] = 6;
		VolumeVertexMappings_detailed2.value[i+1] = 5; //rarm
		VolumeVertexMappings_detailed2.value[i+2] = 9; //rarm
		VolumeVertexMappings_detailed2.value[i+3] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+4] = 2; //top face
		VolumeVertexMappings_detailed2.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed2.width;
		//RWrist
		VolumeVertexMappings_detailed2.value[i+0] = 7;
		VolumeVertexMappings_detailed2.value[i+1] = 9; //rarm
		VolumeVertexMappings_detailed2.value[i+2] = 9; //rarm
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.6f;
		i+=VolumeVertexMappings_detailed2.width;

		//Pelvis
		VolumeVertexMappings_detailed2.value[i+0] = 8;
		VolumeVertexMappings_detailed2.value[i+1] = 1; //torso
		VolumeVertexMappings_detailed2.value[i+2] = 1; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 5; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -5; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.7f;
		i+=VolumeVertexMappings_detailed2.width;

		//Assumes the leg is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//Lpelvis
		VolumeVertexMappings_detailed2.value[i+0] = 9;
		VolumeVertexMappings_detailed2.value[i+1] = 2; //lleg
		VolumeVertexMappings_detailed2.value[i+2] = 2; //lleg//1; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = -0.2f;
		i+=VolumeVertexMappings_detailed2.width;
		//LKnee
		VolumeVertexMappings_detailed2.value[i+0] = 10;
		VolumeVertexMappings_detailed2.value[i+1] = 2; //lleg
		VolumeVertexMappings_detailed2.value[i+2] = 6; //lleg
		VolumeVertexMappings_detailed2.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed2.width;
		//LAnkle
		VolumeVertexMappings_detailed2.value[i+0] = 11;
		VolumeVertexMappings_detailed2.value[i+1] = 10; //lleg
		VolumeVertexMappings_detailed2.value[i+2] = 10; //lleg
//		VolumeVertexMappings_detailed2.value[i+1] = 6; //lleg
//		VolumeVertexMappings_detailed2.value[i+2] = 6; //lleg
		VolumeVertexMappings_detailed2.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 2; //bottom face
//		VolumeVertexMappings_detailed2.value[i+5] = 0.8f;
		VolumeVertexMappings_detailed2.value[i+5] = -0.1f;
//		VolumeVertexMappings_detailed2.value[i+6] = 0.0f;
//		VolumeVertexMappings_detailed2.value[i+7] = 0.0f;
//		VolumeVertexMappings_detailed2.value[i+8] = -0.2f;
//		VolumeVertexMappings_detailed2.value[i+9] = 0.0f;
//		VolumeVertexMappings_detailed2.value[i+10] = 0.0f;
//		VolumeVertexMappings_detailed2.value[i+11] = -0.2f;
		i+=VolumeVertexMappings_detailed2.width;

		//Lpelvis
		VolumeVertexMappings_detailed2.value[i+0] = 12;
		VolumeVertexMappings_detailed2.value[i+1] = 3; //rleg
		VolumeVertexMappings_detailed2.value[i+2] = 3; //rleg//1; //torso
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = -0.2f;
		i+=VolumeVertexMappings_detailed2.width;
		//RKnee
		VolumeVertexMappings_detailed2.value[i+0] = 13;
		VolumeVertexMappings_detailed2.value[i+1] = 3; //rleg
		VolumeVertexMappings_detailed2.value[i+2] = 7; //rleg
		VolumeVertexMappings_detailed2.value[i+3] = -2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 0.5f;
		i+=VolumeVertexMappings_detailed2.width;
		//RAnkle
		VolumeVertexMappings_detailed2.value[i+0] = 14;
		VolumeVertexMappings_detailed2.value[i+1] = 11; //rfoot
		VolumeVertexMappings_detailed2.value[i+2] = 11; //rfoot
		VolumeVertexMappings_detailed2.value[i+3] = 2; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = -2; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = -0.1f;
		VolumeVertexMappings_detailed2.value[i+6] = 0.0f;
		VolumeVertexMappings_detailed2.value[i+7] = 0.0f;
		VolumeVertexMappings_detailed2.value[i+8] = 0.0f;
		VolumeVertexMappings_detailed2.value[i+9] = 0.0f;
		VolumeVertexMappings_detailed2.value[i+10] = 0.0f;
		VolumeVertexMappings_detailed2.value[i+11] = 0.0f;
		i+=VolumeVertexMappings_detailed2.width;

		//LFoot
		VolumeVertexMappings_detailed2.value[i+0] = 15;
		VolumeVertexMappings_detailed2.value[i+1] = 10; //lleg
		VolumeVertexMappings_detailed2.value[i+2] = 10; //lleg
		VolumeVertexMappings_detailed2.value[i+3] = -3; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 3; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 1.0f;
		i+=VolumeVertexMappings_detailed2.width;

		//RFoot
		VolumeVertexMappings_detailed2.value[i+0] = 16;
		VolumeVertexMappings_detailed2.value[i+1] = 11; //rleg
		VolumeVertexMappings_detailed2.value[i+2] = 11; //rleg
		VolumeVertexMappings_detailed2.value[i+3] = -3; //Top face
		VolumeVertexMappings_detailed2.value[i+4] = 3; //bottom face
		VolumeVertexMappings_detailed2.value[i+5] = 1.0f;
		i+=VolumeVertexMappings_detailed2.width;
	}
	
	public void initVolumeVertexMappings()
	{
//		vetexNames.value[0] = "topofhead";
//		vetexNames.value[1] = "neck";
//		vetexNames.value[2] = "lshoulder";
//		vetexNames.value[3] = "lelbow";
//		vetexNames.value[4] = "lwrist";
//		vetexNames.value[5] = "rshoulder";
//		vetexNames.value[6] = "relbow";
//		vetexNames.value[7] = "rwrist";
//		vetexNames.value[8] = "pelvis";
//		vetexNames.value[9] = "lhip";
//		vetexNames.value[10] = "lknee";
//		vetexNames.value[11] = "lankle";
//		vetexNames.value[12] = "rhip";
//		vetexNames.value[13] = "rknee";
//		vetexNames.value[14] = "rankle";
		int i = 0;
		//Top of head
		volumeVertexMappings.value[i+0] = 0;
		volumeVertexMappings.value[i+1] = 0; //head
		volumeVertexMappings.value[i+2] = 0; //head
		volumeVertexMappings.value[i+3] = 5; //top face
		volumeVertexMappings.value[i+4] = 5; //top face
		volumeVertexMappings.value[i+5] = 0.0f;
		i+=volumeVertexMappings.width;
		//Neck
		volumeVertexMappings.value[i+0] = 1;
		volumeVertexMappings.value[i+1] = 0; //head
		volumeVertexMappings.value[i+2] = 1; //torso
		volumeVertexMappings.value[i+3] = -5; //Bottom face
		volumeVertexMappings.value[i+4] = 5; //top face
		volumeVertexMappings.value[i+5] = 0.5f;
		i+=volumeVertexMappings.width;

		//Assumes the arm is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//LShoulder
		volumeVertexMappings.value[i+0] = 2;
		volumeVertexMappings.value[i+1] = 4; //larm
		volumeVertexMappings.value[i+2] = 1; //torso
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //top face
		volumeVertexMappings.value[i+5] = 0.1f;
		i+=volumeVertexMappings.width;
		//LElbow
		volumeVertexMappings.value[i+0] = 3;
		volumeVertexMappings.value[i+1] = 4; //larm
		volumeVertexMappings.value[i+2] = 4; //torso
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 0.5f;
		i+=volumeVertexMappings.width;
		//LWrist
		volumeVertexMappings.value[i+0] = 4;
		volumeVertexMappings.value[i+1] = 4; //larm
		volumeVertexMappings.value[i+2] = 4; //torso
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		i+=volumeVertexMappings.width;

		//RShoulder
		volumeVertexMappings.value[i+0] = 5;
		volumeVertexMappings.value[i+1] = 5; //rarm
		volumeVertexMappings.value[i+2] = 1; //torso
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 0.1f;
		i+=volumeVertexMappings.width;
		//RElbow
		volumeVertexMappings.value[i+0] = 6;
		volumeVertexMappings.value[i+1] = 5; //rarm
		volumeVertexMappings.value[i+2] = 5; //rarm
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 0.5f;
		i+=volumeVertexMappings.width;
		//RWrist
		volumeVertexMappings.value[i+0] = 7;
		volumeVertexMappings.value[i+1] = 5; //rarm
		volumeVertexMappings.value[i+2] = 5; //rarm
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		i+=volumeVertexMappings.width;

		//Pelvis
		volumeVertexMappings.value[i+0] = 8;
		volumeVertexMappings.value[i+1] = 1; //torso
		volumeVertexMappings.value[i+2] = 1; //torso
		volumeVertexMappings.value[i+3] = 5; //Top face
		volumeVertexMappings.value[i+4] = -5; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		i+=volumeVertexMappings.width;

		//Assumes the leg is pointing down (this may need changing, i.e. face closest to a point as an alternative definition)
		//Lpelvis
		volumeVertexMappings.value[i+0] = 9;
		volumeVertexMappings.value[i+1] = 2; //lleg
		volumeVertexMappings.value[i+2] = 2; //lleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //top face
		volumeVertexMappings.value[i+5] = 0.25f;
		i+=volumeVertexMappings.width;
		//LKnee
		volumeVertexMappings.value[i+0] = 10;
		volumeVertexMappings.value[i+1] = 2; //lleg
		volumeVertexMappings.value[i+2] = 2; //lleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 0.25f+0.75f*0.5f;
		i+=volumeVertexMappings.width;
		//LAnkle
		volumeVertexMappings.value[i+0] = 11;
		volumeVertexMappings.value[i+1] = 2; //lleg
		volumeVertexMappings.value[i+2] = 2; //lleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		i+=volumeVertexMappings.width;

		//Lpelvis
		volumeVertexMappings.value[i+0] = 12;
		volumeVertexMappings.value[i+1] = 3; //rleg
		volumeVertexMappings.value[i+2] = 3; //torso
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //top face
		volumeVertexMappings.value[i+5] = 0.25f;
		i+=volumeVertexMappings.width;
		//LKnee
		volumeVertexMappings.value[i+0] = 13;
		volumeVertexMappings.value[i+1] = 3; //rleg
		volumeVertexMappings.value[i+2] = 3; //rleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 0.25f+0.75f*0.5f;
		i+=volumeVertexMappings.width;
		//LAnkle
		volumeVertexMappings.value[i+0] = 14;
		volumeVertexMappings.value[i+1] = 3; //rleg
		volumeVertexMappings.value[i+2] = 3; //rleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		i+=volumeVertexMappings.width;

		//LFoot
		volumeVertexMappings.value[i+0] = 15;
		volumeVertexMappings.value[i+1] = 2; //lleg
		volumeVertexMappings.value[i+2] = 2; //lleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		volumeVertexMappings.value[i+6] = 0.0f; //bone scaled relative offset from first point
		volumeVertexMappings.value[i+7] = 0.0f;
		volumeVertexMappings.value[i+8] = 2.0f;
		volumeVertexMappings.value[i+9] = 0.0f; //bone scaled relative offset from second point
		volumeVertexMappings.value[i+10] = 0.0f;
		volumeVertexMappings.value[i+11] = 1.0f;
		i+=volumeVertexMappings.width;

		//RFoot
		volumeVertexMappings.value[i+0] = 16;
		volumeVertexMappings.value[i+1] = 3; //rleg
		volumeVertexMappings.value[i+2] = 3; //rleg
		volumeVertexMappings.value[i+3] = 2; //Top face
		volumeVertexMappings.value[i+4] = -2; //bottom face
		volumeVertexMappings.value[i+5] = 1.0f;
		volumeVertexMappings.value[i+6] = 0.0f; //bone scaled relative offset from first point
		volumeVertexMappings.value[i+7] = 0.0f;
		volumeVertexMappings.value[i+8] = 2.0f;
		volumeVertexMappings.value[i+9] = 0.0f; //bone scaled relative offset from second point
		volumeVertexMappings.value[i+10] = 0.0f;
		volumeVertexMappings.value[i+11] = 1.0f;
		i+=VolumeVertexMappings_detailed.width;
		
	}

	/*
	public void initPriors(float width,float height,float depth)
	{
		buildVertexPriors(0, width*0.5f, height, depth*0.63f, 10.0f, 10.0f, 30.0f);
		buildVertexPriors(1, width*0.5f, height*0.87f, depth*0.5f, 5.0f, 5.0f, 20.0f);
		buildVertexPriors(2, width*0.25f, height*0.85f, depth*0.5f, 5.0f, 10.0f, 40.0f);
		buildVertexPriors(3, width*0.25f, height*0.5f, depth*0.5f, (0.2f*height)/3.0f, (0.2f*height)/3.0f, (0.2f*height)/3.0f);
		buildVertexPriors(4, width*0.25f, height*0.4f, depth*0.5f, (0.4f*height)/3.0f, (0.4f*height)/3.0f, (0.4f*height)/3.0f);
		buildVertexPriors(5, width*0.75f, height*0.85f, depth*0.5f, 5.0f, 10.0f, 40.0f);
		buildVertexPriors(6, width*0.75f, height*0.5f, depth*0.5f, (0.2f*height)/3.0f, (0.2f*height)/3.0f, (0.2f*height)/3.0f);
		buildVertexPriors(7, width*0.75f, height*0.4f, depth*0.5f, (0.4f*height)/3.0f, (0.4f*height)/3.0f, (0.4f*height)/3.0f);
		buildVertexPriors(8, width*0.5f, height*0.535f, depth*0.5f, 5.0f, 10.0f, 5.0f);
		buildVertexPriors(9, width*0.25f, height*0.5f, depth*0.5f, 5.0f, 10.0f, 10.0f);
		buildVertexPriors(10, width*0.25f, height*0.285f, depth*0.5f, 10.0f, 20.0f, 30.0f);
		buildVertexPriors(11, width*0.25f, height*0.05f, depth*0.5f, 20.0f, 20.0f, 40.0f);
		buildVertexPriors(12, width*0.75f, height*0.5f, depth*0.5f, 5.0f, 10.0f, 10.0f);
		buildVertexPriors(13, width*0.75f, height*0.285f, depth*0.5f, 10.0f, 20.0f, 30.0f);
		buildVertexPriors(14, width*0.75f, height*0.05f, depth*0.5f, 20.0f, 20.0f, 40.0f);

		boneSymmetries.value[0] = -1;
		buildBonePriors(0, 5f, 0.5f);
		boneSymmetries.value[1] = 2;
		buildBonePriors(1, 10f, 0.5f);
		boneSymmetries.value[2] = 1;
		buildBonePriors(2, 10f, 0.5f);
		boneSymmetries.value[3] = 5;
		buildBonePriors(3, 10f, 0.5f);
		boneSymmetries.value[4] = 6;
		buildBonePriors(4, 10f, 0.5f);
		boneSymmetries.value[5] = 3;
		buildBonePriors(5, 10f, 0.5f);
		boneSymmetries.value[6] = 4;
		buildBonePriors(6, 10f, 0.5f);
		boneSymmetries.value[7] = -1;
		buildBonePriors(7, 10f, 0.5f);
		boneSymmetries.value[8] = 11;
		buildBonePriors(8, 10f, 0.5f);
		boneSymmetries.value[9] = 12;
		buildBonePriors(9, 10f, 0.5f);
		boneSymmetries.value[10] = 13;
		buildBonePriors(10, 10f, 0.5f);
		boneSymmetries.value[11] = 8;
		buildBonePriors(11, 10f, 0.5f);
		boneSymmetries.value[12] = 9;
		buildBonePriors(12, 10f, 0.5f);
		boneSymmetries.value[13] = 10;
		buildBonePriors(13, 10f, 0.5f);
	}
	*/
	
	public void initAlignmentInformation()
	{
		//head and spine
		bonepivots.value[2*0+0] = 0;
		bonepivots.value[2*0+1] = 7;
		//larm
		bonepivots.value[2*1+0] = 3;
		bonepivots.value[2*1+1] = 4;
		//rarm
		bonepivots.value[2*2+0] = 5;
		bonepivots.value[2*2+1] = 6;
		//lleg
		bonepivots.value[2*3+0] = 9;
		bonepivots.value[2*3+1] = 10;
		//rleg
		bonepivots.value[2*4+0] = 12;
		bonepivots.value[2*4+1] = 13;

		//lfoot
		bonepivots.value[2*5+0] = 10;
		bonepivots.value[2*5+1] = 14;
		//rfoot
		bonepivots.value[2*6+0] = 13;
		bonepivots.value[2*6+1] = 15;
		
		//spine
		bodybones.value[4*0+0] = 7;
		bodybones.value[4*0+1] = 2;
		bodybones.value[4*0+2] = 5;
		bodybones.value[4*0+3] = 8;
		//lshoulder
		bodybones.value[4*1+0] = 1;
		bodybones.value[4*1+1] = 2;
		bodybones.value[4*1+2] = 1;
		bodybones.value[4*1+3] = 8;
		//rshoulder
		bodybones.value[4*2+0] = 2;
		bodybones.value[4*2+1] = 1;
		bodybones.value[4*2+2] = 5;
		bodybones.value[4*2+3] = 8;
		//lpelvis
		bodybones.value[4*3+0] = 8;
		bodybones.value[4*3+1] = 9;
		bodybones.value[4*3+2] = 8;
		bodybones.value[4*3+3] = 12;
		//rpelvis
		bodybones.value[4*4+0] = 11;
		bodybones.value[4*4+1] = 9;
		bodybones.value[4*4+2] = 8;
		bodybones.value[4*4+3] = 12;
	}
	
	/*
	public void buildBoneWidthPiors(int b,float sd,VolumeBuffer distancemap,Skeleton skeleton,Pose pose)
	{
		//Walk the line between the bone verts
		//calc the distances
		
		int v0 = skeleton.bones.value[2*b+0];
		int v1 = skeleton.bones.value[2*b+1];
		
		GeneralMatrixInt distances = new GeneralMatrixInt(1);
		LineRasteriser.readline(pose.vertexPositions.value[3*v0], pose.vertexPositions.value[3*v0+1], pose.vertexPositions.value[3*v0+2], 
				pose.vertexPositions.value[3*v1], pose.vertexPositions.value[3*v1+1], pose.vertexPositions.value[3*v1+2], 
								distances, distancemap, 
								0, 0, 0, 
								distancemap.width, distancemap.height, distancemap.depth);
		
		//Now lets use this distance map to estimate the actual cm distance for this path
		//Need to do this robustly
		//So lets sort and exclude the lowest n samples
		QuickSort.sort(distances.value, distances.height);
		int v = (int)(distances.height*0.5f);
		
		bonewidthpriors.value[bonewidthpriors.width*b+0] = distances.value[v]/((float)DistanceMap.ONE);
		bonewidthpriors.value[bonewidthpriors.width*b+1] = sd;
	}
	*/
	
	/*
	public void buildBonePriors(int v,float meanl,float sdl,float asd)
	{
		int v0 = human.skeleton.bones.value[v*2+0];
		int v1 = human.skeleton.bones.value[v*2+1];
		float ax = vertpositionpriors.value[v1*vertpositionpriors.width+0]-vertpositionpriors.value[v0*vertpositionpriors.width+0];
		float ay = vertpositionpriors.value[v1*vertpositionpriors.width+1]-vertpositionpriors.value[v0*vertpositionpriors.width+1];
		float az = vertpositionpriors.value[v1*vertpositionpriors.width+2]-vertpositionpriors.value[v0*vertpositionpriors.width+2];
		float d = (float)Math.sqrt(ax*ax+ay*ay+az*az);
		ax /= d;
		ay /= d;
		az /= d;
		
		bonepriors.value[v*bonepriors.width+0] = meanl;
		bonepriors.value[v*bonepriors.width+1] = sdl;
		bonepriors.value[v*bonepriors.width+2] = ax;
		bonepriors.value[v*bonepriors.width+3] = ay;
		bonepriors.value[v*bonepriors.width+4] = az;
		bonepriors.value[v*bonepriors.width+5] = asd;
	}
	
	public void buildBonePriors(int v,float sdl,float asd)
	{
		int v0 = human.skeleton.bones.value[v*2+0];
		int v1 = human.skeleton.bones.value[v*2+1];
		float ax = vertpositionpriors.value[v1*vertpositionpriors.width+0]-vertpositionpriors.value[v0*vertpositionpriors.width+0];
		float ay = vertpositionpriors.value[v1*vertpositionpriors.width+1]-vertpositionpriors.value[v0*vertpositionpriors.width+1];
		float az = vertpositionpriors.value[v1*vertpositionpriors.width+2]-vertpositionpriors.value[v0*vertpositionpriors.width+2];
		float d = (float)Math.sqrt(ax*ax+ay*ay+az*az);
		ax /= d;
		ay /= d;
		az /= d;
		
		bonepriors.value[v*bonepriors.width+0] = d;
		bonepriors.value[v*bonepriors.width+1] = sdl;
		bonepriors.value[v*bonepriors.width+2] = ax;
		bonepriors.value[v*bonepriors.width+3] = ay;
		bonepriors.value[v*bonepriors.width+4] = az;
		bonepriors.value[v*bonepriors.width+5] = asd;
	}
	
	public void buildVertexPriors(int v,float cx,float cy,float cz,float sx,float sy,float sz)
	{
		vertpositionpriors.value[v*vertpositionpriors.width+0] = cx;
		vertpositionpriors.value[v*vertpositionpriors.width+1] = cy;
		vertpositionpriors.value[v*vertpositionpriors.width+2] = cz;
		
		for(int i=0;i<3;i++)
		{
			vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+0] = 0.0f;
			vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+1] = 0.0f;
			vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+2] = 0.0f;
			
			switch(i)
			{
			case 0:
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+0] = 1.0f;
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+3] = 1.0f/(sx*sx);
				break;
			case 1:
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+1] = 1.0f;
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+3] = 1.0f/(sy*sy);
				break;
			case 2:
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+2] = 1.0f;
				vertpositionpriors.value[v*vertpositionpriors.width+3+i*4+3] = 1.0f/(sz*sz);
				break;
			}
		}
	}
	 */
}

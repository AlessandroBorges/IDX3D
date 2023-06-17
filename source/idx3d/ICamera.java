// | -----------------------------------------------------------------
// | idx3d III is (c)1999/2000 by Peter Walser
// | -----------------------------------------------------------------
// | idx3d is a 3d engine written in 100% pure Java (1.1 compatible)
// | and provides a fast and flexible API for software 3d rendering
// | on the Java platform.
// |
// | Feel free to use the idx3d API / classes / source code for
// | non-commercial purposes (of course on your own risk).
// | If you intend to use idx3d for commercial purposes, please
// | contact me with an e-mail [proxima@active.ch].
// |
// | Thanx & greetinx go to:
// | * Wilfred L. Guerin, 	for testing, bug report, and tons
// |			of brilliant suggestions
// | * Sandy McArthur,	for reverse loops
// | * Dr. Douglas Lyons,	for mentioning idx3d1 in his book
// | * Hugo Elias,		for maintaining his great page
// | * the comp.graphics.algorithms people,
// | 			for scientific concerns
// | * Tobias Hill,		for inspiration and awakening my
// |			interest in java gfx coding
// | * Kai Krause,		for inspiration and hope
// | * Incarom & Parisienne,	for keeping me awake during the
// |			long coding nights
// | * Doris Langhard,	for being the sweetest girl on earth
// | * Etnica, Infinity Project, X-Dream and "Space Night"@BR3
// | 			for great sound while coding
// | and all coderz & scenerz out there (keep up the good work, ppl :)
// |
// | Peter Walser
// | proxima@active.ch
// | http://www2.active.ch/~proxima
// | "On the eigth day, God started debugging"
// | -----------------------------------------------------------------

package idx3d;

public class ICamera
{
	// F I E L D S

		public IMatrix matrix=new IMatrix();
		public IMatrix normalmatrix=new IMatrix();

		boolean needsRebuild=true;   // Flag indicating changes on matrix

		// Camera settings
		public IVector pos=new IVector(0f,0f,0f);
		public IVector lookat=new IVector(0f,0f,0f);
		public float roll=0f;

		float fovfact;             // Field of View factor
		int screenwidth;
		int screenheight;
		int screenscale;

	// C O N S T R U C T O R S

		public ICamera()
		{
			setFov(90f);
		}

		public ICamera(float fov)
		{
			setFov(fov);
		}

	// P U B L I C   M E T H O D S

		IMatrix getMatrix()
		{
			rebuildMatrices();
			return matrix;
		}

		IMatrix getNormalMatrix()
		{
			rebuildMatrices();
			return normalmatrix;
		}

		void rebuildMatrices()
		{
			if (!needsRebuild) return;
			needsRebuild=false;

			IVector forward,up,right;

			forward=IVector.sub(lookat,pos);
			up=new IVector(0f,1f,0f);
			right=IVector.getNormal(up,forward);
			up=IVector.getNormal(forward,right);

			forward.normalize();
			up.normalize();
			right.normalize();

			normalmatrix=new IMatrix(right,up,forward);
			normalmatrix.rotate(0,0,roll);
			//matrix=normalmatrix.getClone();
    if (matrix == null)
    {
      matrix = new  IMatrix();
    }
      normalmatrix.copyTo(matrix);
			matrix.shift(pos.x,pos.y,pos.z);

		//	normalmatrix=normalmatrix.inverse();
      normalmatrix.inverse(normalmatrix);
			//matrix=matrix.inverse();
      matrix.inverse(matrix);
		}

		public void setFov(float fov)
		{
			fovfact=(float)Math.tan(IMath.deg2rad(fov)/2);
		}

		public void roll(float angle)
		{
			roll+=angle;
			needsRebuild=true;
		}

		public void setPos(float px, float py, float pz)
		{
			pos=new IVector(px,py,pz);
			needsRebuild=true;
		}

		public void setPos(IVector p)
		{
			pos=p;
			needsRebuild=true;
		}

		public void lookAt(float px, float py, float pz)
		{
			lookat=new IVector(px,py,pz);
			needsRebuild=true;
		}

		public void lookAt(IVector p)
		{
			lookat=p;
			needsRebuild=true;
		}

		public void setScreensize(int w, int h)
		{
			screenwidth=w;
			screenheight=h;
			screenscale=(w<h)?w:h;
		}

	// MATRIX MODIFIERS

		public final void shift(float dx, float dy, float dz)
		{
			//pos = pos.transform(IMatrix.shiftMatrix(dx,dy,dz));
			pos.transform(IMatrix.shiftMatrix(dx,dy,dz),pos);

			//lookat=lookat.transform(IMatrix.shiftMatrix(dx,dy,dz));
			lookat.transform(IMatrix.shiftMatrix(dx,dy,dz),lookat);
			needsRebuild=true;

		}

		public final void shift(IVector v)
		{
			shift(v.x,v.y,v.z);

		}

		public final void rotate(float dx, float dy, float dz)
		{
			//pos=pos.transform(IMatrix.rotateMatrix(dx,dy,dz));
			pos.transform(IMatrix.rotateMatrix(dx,dy,dz),pos);
			needsRebuild=true;
		}

		public final void rotate(IVector v)
		{
			rotate(v.x,v.y,v.z);
		}

		public static ICamera FRONT()
		{
			ICamera cam=new ICamera();
			cam.setPos(0,0,-2f);
			return cam;
		}

		public static ICamera LEFT()
		{
			ICamera cam=new ICamera();
			cam.setPos(2f,0,0);
			return cam;
		}

		public static ICamera RIGHT()
		{
			ICamera cam=new ICamera();
			cam.setPos(-2f,0,0);
			return cam;
		}

		public static ICamera TOP()
		{
			ICamera cam=new ICamera();
			cam.setPos(0,-2f,0);
			return cam;
		}


}
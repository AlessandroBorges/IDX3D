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

public class IVector
{
	// F I E L D S
	
		public float x=0;      //Cartesian (default)
		public float y=0;      //Cartesian (default)
		public float z=0;      //Cartesian (default),Cylindric
		public float r=0;      //Cylindric
		public float theta=0;  //Cylindric
        
        private static final float MIN_LENGTH = 1E-8f;


	// C O N S T R U C T O R S

		public IVector ()
		{
		}

		public IVector (float xpos, float ypos, float zpos)
		{
			x=xpos;
			y=ypos;
			z=zpos;
		}

	// P U B L I C   M E T H O D S

		public IVector normalize()
		// Normalizes the vector
		{
			float dist=length();
			if (dist < MIN_LENGTH) return this;
			float invdist=1/dist;
			x*=invdist;
			y*=invdist;
			z*=invdist;
			return this;
		}
		
		public IVector reverse()
		// Reverses the vector
		{	
			x=-x;
			y=-y;
			z=-z;
			return this;
		}
		
		public final float length()
		// Lenght of this vector
		{	
     float v = x*x+y*y+z*z;
         if (v < MIN_LENGTH) return 0.0f;
    // System.out.println("Length " + v);
         return (float)Math.sqrt(v);
     
		}

    public IVector transform(IMatrix m)
    // Modifies the vector by matrix m
    {
        float newx = x * m.m00 + y * m.m01 + z * m.m02 + m.m03;
        float newy = x * m.m10 + y * m.m11 + z * m.m12 + m.m13;
        float newz = x * m.m20 + y * m.m21 + z * m.m22 + m.m23;
        return new IVector(newx, newy, newz);
    }

    public void transform(IMatrix m, IVector vec)
    // Modifies the vector by matrix m
    {
        float dx = x * m.m00 + y * m.m01 + z * m.m02 + m.m03;
        float dy = x * m.m10 + y * m.m11 + z * m.m12 + m.m13;
        float dz = x * m.m20 + y * m.m21 + z * m.m22 + m.m23;
        vec.x = dx;
        vec.y = dy;
        vec.z = dz;
    }

		public void buildCylindric()
		// Builds the cylindric coordinates out of the given cartesian coordinates
		{
			r=(float)Math.sqrt(x*x+y*y);
			theta=(float)Math.atan2(x,y);
		}

		public void buildCartesian()
		// Builds the cartesian coordinates out of the given cylindric coordinates
		{
			x=r*IMath.cos(theta);
			y=r*IMath.sin(theta);
		}

		public static IVector getNormal(IVector a, IVector b)
		// returns the normal vector of the plane defined by the two vectors
		{
			return vectorProduct(a,b).normalize();
		}
		
		public static IVector getNormal(IVector a, IVector b, IVector c)
		// returns the normal vector of the plane defined by the two vectors
		{
			return vectorProduct(a,b,c).normalize();
		}
		
		public static IVector vectorProduct(IVector a, IVector b)
		// returns a x b
		{
			return new IVector(a.y*b.z-b.y*a.z,a.z*b.x-b.z*a.x,a.x*b.y-b.x*a.y);
		}
		
		public static IVector vectorProduct(IVector a, IVector b, IVector c)
		// returns (b-a) x (c-a)
		{
			return vectorProduct(sub(b,a),sub(c,a));
		}

		public static float angle(IVector a, IVector b)
		// returns the angle between 2 vectors
		{
			a.normalize();
			b.normalize();
			return (a.x*b.x+a.y*b.y+a.z*b.z);
		}
		
		public static IVector add(IVector a, IVector b)
		// adds 2 vectors
		{
			return new IVector(a.x+b.x,a.y+b.y,a.z+b.z);
		}
		
		public static IVector sub(IVector a, IVector b)
		// substracts 2 vectors
		{
			return new IVector(a.x-b.x,a.y-b.y,a.z-b.z);
		}
		
		public static IVector scale(float f, IVector a)
		// substracts 2 vectors
		{
			return new IVector(f*a.x,f*a.y,f*a.z);
		}
		
		public static float len(IVector a)
		// length of vector
		{
			return (float)Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
		}
		
		public static IVector random(float fact)
		// returns a random vector
		{
			return new IVector(fact*IMath.random(),fact*IMath.random(),fact*IMath.random());
		}
		
		public String toString()
		{
			return new String ("<vector x="+x+" y="+y+" z="+z+">\r\n");
		}

		public IVector getClone()
		{
			return new IVector(x,y,z);
		}
    
    /**
   * Fast SQRT using Taylor
   * author Alex W. Rogoyski. Email: <rogoyski@cats.ucsc.edu>
   * Java port: Alessandro Borges
   * @param dUserVal 
   */
    private static final float iFact1 =(1f/2.0f),
                              iFact2 = (1f/4f)/(1f*2f),
                              iFact3 = (3f/8.0f) /(1f*2f*3f),
                              iFact4 = (15f/16.0f)/(1f*2f*3f*4f); 

    @Deprecated
    private static final float fastSQRT(float dUserVal)
    {
     
           /* reduce value */
         /* here we are dividing the user value by
            k * k until it's less than two and then
            we will multiply the taylor series
            value by some fixup amount k to the nth
            power where n is the number times in
            the loop */
         float  dFixup = 1.0f;
         float dKReduce = 2.0f;
         while(dUserVal > 2f)
         {
            // dUserVal /= dKReduce * dKReduce;
             dUserVal *= 0.25f;
             dFixup *= dKReduce;
         }

         /* compute taylor series value */

       float  x = dUserVal; /* switch to x because dUserVal is
                          too long */
        /*
         iFact1 = 1;
         iFact2 = 1*2;
         iFact3 = 1*2*3;
         iFact4 = 1*2*3*4;

         float  dTaylorVal =
             1.0
             +  (1/2.0)  * (x-1) / iFact1
             -  (1/4.0)  * (x-1) * (x-1) / iFact2
             +  (3/8.0)  * (x-1) * (x-1) * (x-1) / iFact3
             - (15/16.0) * (x-1) * (x-1) * (x-1) * (x-1) / iFact4;
      */
      float x1 = x - 1.0f;
      float x2 = x1*x1;
      float  dTaylorVal =             
             1.0f
             +  x1 * iFact1
             -  x2 * iFact2
             +  x2*x1 * iFact3
             -  x2*x2* iFact4;
         /* do the fixup to correct the reduction */
         dTaylorVal *= dFixup;
         
         return dTaylorVal;
   
    }

}
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

/**
 * Hold Triangle data
 * @author Livia
 */
public class ITriangle 
  implements Comparable<ITriangle>
// defines a 3d triangle
{
	// F I E L D S
	
		public IObject parent;  // the object which obtains this triangle
		public boolean visible=true;  //visibility tag for clipping
		public boolean outOfFrustrum=false;  //visibility tag for frustrum clipping

		public IVertex p1;  // first  vertex
		public IVertex p2;  // second vertex 
		public IVertex p3;  // third  vertex 

		public IVector n = new IVector() ;  // Normal vector of flat triangle
		public IVector n2 = new IVector(); // Projected Normal vector

		private int minx,maxx,miny,maxy; // for clipping
		private IVector triangleCenter=new IVector();
		float dist=0;
		
		public int id=0;

	// C O N S T R U C T O R S

		public ITriangle(IVertex a, IVertex b, IVertex c)
		{
			p1=a;
			p2=b;
			p3=c;
		}


	// P U B L I C   M E T H O D S


		public void clipFrustrum(int w, int h)
		// Backface culling and frustrum clipping
		{
			if (parent.material==null) {visible=false; return; }
			outOfFrustrum=(p1.clipcode&p2.clipcode&p3.clipcode)!=0;
			if (outOfFrustrum) {visible=false; return; }
			if (n2.z>0.5) {visible=true; return; }
			
			triangleCenter.x=(p1.pos2.x+p2.pos2.x+p3.pos2.x);
			triangleCenter.y=(p1.pos2.y+p2.pos2.y+p3.pos2.y);
			triangleCenter.z=(p1.pos2.z+p2.pos2.z+p3.pos2.z);
			visible=IVector.angle(triangleCenter,n2)>0;

		}

		public void project(IMatrix normalProjection)
		{
			//n2=n.transform(normalProjection);
			n.transform(normalProjection,n2);
			dist=getDist();
		}
		
		public void regenerateNormal()
		{
			n=IVector.getNormal(p1.pos,p2.pos,p3.pos);
		}
		
		public IVector getWeightedNormal()
		{
			return IVector.vectorProduct(p1.pos,p2.pos,p3.pos);
		}
		
		public IVertex getMedium()
		{
			float cx=(p1.pos.x+p2.pos.x+p3.pos.x)/3;
			float cy=(p1.pos.y+p2.pos.y+p3.pos.y)/3;
			float cz=(p1.pos.z+p2.pos.z+p3.pos.z)/3;
			float cu=(p1.u+p2.u+p3.u)/3;
			float cv=(p1.v+p2.v+p3.v)/3;
			return new IVertex(cx,cy,cz,cu,cv);
		}
		
		public IVector getCenter()
		{
			float cx=(p1.pos.x+p2.pos.x+p3.pos.x)/3;
			float cy=(p1.pos.y+p2.pos.y+p3.pos.y)/3;
			float cz=(p1.pos.z+p2.pos.z+p3.pos.z)/3;
			return new IVector(cx,cy,cz);
		}
		
		public float getDist()
		{
			return p1.z+p2.z+p3.z;
		}
		
				
		public boolean degenerated()
		{
			return p1.equals(p2)||p2.equals(p3)||p3.equals(p1);
		}
		
		public ITriangle getClone()
		{
			return new ITriangle(p1,p2,p3);
		}
		


	// P R I V A T E   M E T H O D S

    @Override
    public int compareTo(ITriangle o) {
       if(this.dist < o.dist) return -1;
       if(this.dist > o.dist) return  1;
       else 
           return 0;
      // return  (int)((this.dist - o.dist)*1000f);
    }


}
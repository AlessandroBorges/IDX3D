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

public class ILight extends ICoreObject
// defines a light in the scene
{
	// F I E L D S

		public IVector v;               //Light direction
		public IVector v2;             //projected Light direction
		public int diffuse=0;
		public int specular=0;
		public int highlightSheen=0;
		public int highlightSpread=0;
	
		IMatrix matrix2;


	// C O N S T R U C T O R S
	
		private ILight()
		// Default constructor not accessible
		{
		}		

		public ILight(IVector direction)
		{
			v=direction.getClone();
			v.normalize();
		}

		public ILight(IVector direction,int diffuse)
		{
			v=direction.getClone();
			v.normalize();
			this.diffuse=diffuse;
		}
		
		public ILight(IVector direction, int color, int highlightSheen, int highlightSpread)
		{
			v=direction.getClone();
			v.normalize();
			this.diffuse=color;
			this.specular=color;
			this.highlightSheen=highlightSheen;
			this.highlightSpread=highlightSpread;
		}
		
		public ILight(IVector direction, int diffuse, int specular, int highlightSheen, int highlightSpread)
		{
			v=direction.getClone();
			v.normalize();
			this.diffuse=diffuse;
			this.specular=specular;
			this.highlightSheen=highlightSheen;
			this.highlightSpread=highlightSpread;
		}


	// P U B L I C   M E T H O D S

    public void project(IMatrix m) {        
        // matrix2=matrix.getClone();
        // v2=v.transform(matrix2);
        if (matrix2 == null) 
            matrix2 = new IMatrix();
       
        matrix.copyTo(matrix2);
        matrix2.transform(m);        
        v.transform(matrix2, v2);
    }
}
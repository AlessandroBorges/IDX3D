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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashMap;
import java.awt.Image;

public final class IScene extends ICoreObject
{
	//Release Information
	
		public final static String version="3.5.001";
		public final static String release="31.05.2023";
		
	// F I E L D S		

		public IRenderPipeline renderPipeline;
		public int width,height;

		public IEnvironment environment=new IEnvironment();
		public ICamera defaultCamera=ICamera.FRONT();
		
		public IObject object[];
		public ILight light[];
		public int objects=0;
		public int lights=0;

		private boolean objectsNeedRebuild=true;
		private boolean lightsNeedRebuild=true;
		
		protected boolean preparedForRendering=false;
		
		public IVector normalizedOffset=new IVector(0f,0f,0f);
		public float normalizedScale=1f;
		private static boolean instancesRunning=false;
		
	// D A T A   S T R U C T U R E S
		
		public Map<String, IObject> objectData=new HashMap<String, IObject>();
		public Hashtable lightData=new Hashtable();
		public Hashtable materialData=new Hashtable();
		public Hashtable cameraData=new Hashtable();


	// C O N S T R U C T O R S
	
		private IScene()
		{
		}
	
		public IScene(int w, int h)
		{
			showInfo(); width=w; height=h;
			renderPipeline= new IRenderPipeline(this,w,h);
		}

		
		public void showInfo()
		{
			if (instancesRunning) return;
			System.out.println();
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			System.out.println(" idx3d Kernel "+version+" [Build "+release+"]");
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			System.out.println(" (c)1999 by Peter Walser, all rights reserved.");
			System.out.println(" http://www2.active.ch/~proxima/idx3d");
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			instancesRunning=true;
		}
		

	// D A T A   M A N A G E M E N T
	
		public void removeAllObjects()
		{
		        objectData.clear();
		        //objectData=new Hashtable();
			objectsNeedRebuild=true;
			rebuild();
		}

		public void rebuild()
		{
			if (objectsNeedRebuild)
			{
				objectsNeedRebuild=false;
				objects=objectData.size();
				object=new IObject[objects];
				//Enumeration en=objectData.elements();
				Iterator<IObject> en = objectData.values().iterator(); 
				for (int i=objects-1;i>=0;i--)
				{
					object[i]=(IObject)en.next();
					object[i].id=i;
					object[i].rebuild();
				}
				
			}
			
			if (lightsNeedRebuild)
			{
				lightsNeedRebuild=false;
				lights=lightData.size();
				light=new ILight[lights];				
				Enumeration en=lightData.elements();
				for (int i=lights-1;i>=0;i--) light[i]=(ILight)en.nextElement();

			}
		}

	// A C C E S S O R S

		public IObject object(String key)   { return (IObject)  objectData.get(key);}
		public ILight light(String key)     { return (ILight)   lightData.get(key);}
		public IMaterial material(String key) { return (IMaterial) materialData.get(key);}
		public ICamera camera(String key) { return (ICamera) cameraData.get(key);}
		
	// O B J E C T   M A N A G E M E N T

		public void addObject(String key, IObject obj){ obj.name=key; objectData.put(key,obj); obj.parent=this; objectsNeedRebuild=true;}
		public void removeObject(String key) { objectData.remove(key); objectsNeedRebuild=true; preparedForRendering=false;}

		public void addLight(String key, ILight l) { lightData.put(key,l); lightsNeedRebuild=true;}
		public void removeLight(String key) { lightData.remove(key); lightsNeedRebuild=true; preparedForRendering=false;}

		public void addMaterial(String key, IMaterial m) {materialData.put(key,m);}
		public void removeMaterial(String key) { materialData.remove(key); }
		
		public void addCamera(String key, ICamera c) {cameraData.put(key,c);}
		public void removeCamera(String key) { cameraData.remove(key); }


	// R E N D E R I N G
	
		void prepareForRendering()
		{
			if (preparedForRendering) return;
			preparedForRendering=true;

			System.out.println(">> Preparing structures for realtime rendering ...   ");
			rebuild();
			renderPipeline.buildLightMap();
			printSceneInfo();
		}
		
		public void printSceneInfo()
		{
			System.out.println(">> | Objects   : "+objects);
			System.out.println(">> | Vertices  : "+countVertices());
			System.out.println(">> | Triangles : "+countTriangles());
		}
				
	
    public final void render(ICamera cam) {
        renderPipeline.render(cam);
    }

    public final void render() {
        renderPipeline.render(this.defaultCamera);
    }

    public final Image getImage() {
        return renderPipeline.screen.getImage();
    }

    public final void setAntialias(boolean antialias) {
        renderPipeline.setAntialias(antialias);
    }

    public final boolean antialias() {
        return renderPipeline.screen.antialias;
    }

    public float getFPS() {
        return renderPipeline.getFPS();
    }

    public void useIdBuffer(boolean useIdBuffer)
    // Enables / Disables idBuffering
    {
        renderPipeline.useIdBuffer(useIdBuffer);
    }
		
		public ITriangle identifyTriangleAt(int xpos, int ypos)
		{
			if (!renderPipeline.useIdBuffer) return null;
			if (xpos<0 || xpos>=width) return null;
			if (ypos<0 || ypos>=height) return null;

			int pos=xpos+renderPipeline.screen.w*ypos;
			if(renderPipeline.screen.antialias) pos*=2;
			int idCode=renderPipeline.idBuffer[pos];
			if (idCode<0) return null;
			return object[idCode>>16].triangle[idCode&0xFFFF];
		}
		
		public IObject identifyObjectAt(int xpos, int ypos)
		{
			ITriangle tri=identifyTriangleAt(xpos,ypos);
			if (tri==null) return null;
			return tri.parent;
		}

	// P U B L I C   M E T H O D S
	
	public java.awt.Dimension size() {
		return new java.awt.Dimension(width, height);
	}
	
	public void resize(int w, int h) {
		if ((width == w) && (height == h))
			return;
		width = w;
		height = h;
		renderPipeline.resize(w, h);
	}

	public void setBackgroundColor(int bgcolor) {
		environment.bgcolor = bgcolor;
	}

	public void setBackground(ITexture t) {
		environment.setBackground(t);
	}

	public void setAmbient(int ambientcolor) {
		environment.ambient = ambientcolor;
	}

	public int countVertices() {
		int counter = 0;
		for (int i = 0; i < objects; i++)
			counter += object[i].vertices;
		return counter;
	}
		
		public int countTriangles()
		{
			int counter=0;
			for (int i=0;i<objects;i++) counter+=object[i].triangles;
			return counter;
		}
				
		public String toString()
		{
			StringBuffer buffer=new StringBuffer();
			buffer.append("<scene>\r\n");
			for (int i=0;i<objects;i++) buffer.append(object[i].toString());
			return buffer.toString();
		}
		
		public void normalize()
		// useful if you can't find your objects on the screen ;)
		{
			objectsNeedRebuild=true;				
			rebuild();
			
			IVector min, max, tempmax, tempmin;
			if (objects==0) return;
			
			matrix=new IMatrix();
			normalmatrix=new IMatrix();
			
			max=object[0].max();
			min=object[0].min();

			for (int i=0; i<objects; i++)
			{
				tempmax=object[i].max();
				tempmin=object[i].min();
				if (tempmax.x>max.x) max.x=tempmax.x;
				if (tempmax.y>max.y) max.y=tempmax.y;
				if (tempmax.z>max.z) max.z=tempmax.z;
				if (tempmin.x<min.x) min.x=tempmin.x;
				if (tempmin.y<min.y) min.y=tempmin.y;
				if (tempmin.z<min.z) min.z=tempmin.z;
			}
			float xdist=max.x-min.x;
			float ydist=max.y-min.y;
			float zdist=max.z-min.z;
			float xmed=(max.x+min.x)/2;
			float ymed=(max.y+min.y)/2;
			float zmed=(max.z+min.z)/2;

			float diameter=(xdist>ydist)?xdist:ydist;
			diameter=(zdist>diameter)?zdist:diameter;
			
			normalizedOffset=new IVector(xmed,ymed,zmed);
			normalizedScale=2/diameter;

			shift(normalizedOffset.reverse());
			scale(normalizedScale);
			
		}
		
		
		

	// P R I V A T E   M E T H O D S
}
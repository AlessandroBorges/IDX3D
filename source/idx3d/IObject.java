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

import java.util.Vector;
import java.util.Enumeration;

public class IObject extends ICoreObject
{
	// F I E L D S

		public Object userData=null;	// Can be freely used
		public String user=null; 	// Can be freely used

		public Vector vertexData=new Vector();
		public Vector triangleData=new Vector();

		public int id;  // This object's index
		public String name="";  // This object's name
		public boolean visible=true; // Visibility tag

		public IScene parent=null;
		private boolean dirty=true;  // Flag for dirty handling

		IVertex[] vertex;
		ITriangle[] triangle;

		public int vertices=0;
		public int triangles=0;

		public IMaterial material=null;

	// C O N S T R U C T O R S

		public IObject()
		{
		}

	// D A T A  S T R U C T U R E S

		public IVertex vertex(int id)
		{
			return (IVertex) vertexData.elementAt(id);
		}

		public ITriangle triangle(int id)
		{
			return (ITriangle) triangleData.elementAt(id);
		}

		public void addVertex(IVertex newVertex)
		{
			newVertex.parent=this;
			vertexData.addElement(newVertex);
			dirty=true;
		}

		public void addTriangle(ITriangle newTriangle)
		{
			newTriangle.parent=this;
			triangleData.addElement(newTriangle);
			dirty=true;
		}

		public void addTriangle(int v1, int v2, int v3)
		{
			addTriangle(vertex(v1),vertex(v2),vertex(v3));
		}

		public void removeVertex(IVertex v)
		{
			vertexData.removeElement(v);
		}

		public void removeTriangle(ITriangle t)
		{
			triangleData.removeElement(t);
		}

		public void removeVertexAt(int pos)
		{
			vertexData.removeElementAt(pos);
		}

		public void removeTriangleAt(int pos)
		{
			triangleData.removeElementAt(pos);
		}


		public void setMaterial(IMaterial m)
		{
			material=m;
		}

		public void rebuild()
		{
			if (!dirty) return;
			dirty=false;
			Enumeration enume;

			// Generate faster structure for vertices
			vertices=vertexData.size();
			vertex=new IVertex[vertices];
			enume=vertexData.elements();
			for (int i=vertices-1;i>=0;i--) 
      vertex[i]=(IVertex)enume.nextElement();

			// Generate faster structure for triangles
			triangles=triangleData.size();
			triangle=new ITriangle[triangles];
			enume=triangleData.elements();
			for (int i=triangles-1;i>=0;i--)
			{
				triangle[i]=(ITriangle)enume.nextElement();
				triangle[i].id=i;
			}

			for (int i=vertices-1;i>=0;i--)
			{
				vertex[i].id=i;
				vertex[i].resetNeighbors();
			}

			ITriangle tri;
			for (int i=triangles-1;i>=0;i--)
			{
				tri=triangle[i];
				tri.p1.registerNeighbor(tri);
				tri.p2.registerNeighbor(tri);
				tri.p3.registerNeighbor(tri);
			}

			regenerate();
		}

		public void addVertex(float x, float y, float z)
		{
			addVertex(new IVertex(x,y,z));
		}


		public void addVertex(float x, float y, float z, float u, float v)
		{
			IVertex vert=new IVertex(x,y,z);
			vert.setUV(u,v);
			addVertex(vert);
		}

		public void addTriangle(IVertex a, IVertex b, IVertex c)
		{
			addTriangle(new ITriangle(a,b,c));
		}

		public void regenerate()
		// Regenerates the vertex normals
		{
			for (int i=0;i<triangles;i++) triangle[i].regenerateNormal();
			for (int i=0;i<vertices;i++) vertex[i].regenerateNormal();
		}

		public String toString()
		{
			StringBuffer buffer=new StringBuffer();
			buffer.append("<object id="+name+">\r\n");
			for (int i=0;i<vertices;i++) buffer.append(vertex[i].toString());
			return buffer.toString();
		}

		public void scaleTextureCoordinates(float fu, float fv)
		{
			rebuild();
			for (int i=0;i<vertices;i++) vertex[i].scaleTextureCoordinates(fu,fv);
		}

		public void tilt(float fact)
		{
			rebuild();
			for (int i=0;i<vertices;i++)
				vertex[i].pos=IVector.add(vertex[i].pos,IVector.random(fact));
			regenerate();
		}

		public IVector min()
		{
			if (vertices==0) return new IVector(0f,0f,0f);
			float minX=vertex[0].pos.x;
			float minY=vertex[0].pos.y;
			float minZ=vertex[0].pos.z;
			for (int i=1; i<vertices; i++)
			{
				if(vertex[i].pos.x<minX) minX=vertex[i].pos.x;
				if(vertex[i].pos.y<minY) minY=vertex[i].pos.y;
				if(vertex[i].pos.z<minZ) minZ=vertex[i].pos.z;
			}
			return new IVector(minX,minY,minZ);
		}

		public IVector max()
		{
			if (vertices==0) return new IVector(0f,0f,0f);
			float maxX=vertex[0].pos.x;
			float maxY=vertex[0].pos.y;
			float maxZ=vertex[0].pos.z;
			for (int i=1; i<vertices; i++)
			{
				if(vertex[i].pos.x>maxX) maxX=vertex[i].pos.x;
				if(vertex[i].pos.y>maxY) maxY=vertex[i].pos.y;
				if(vertex[i].pos.z>maxZ) maxZ=vertex[i].pos.z;
			}
			return new IVector(maxX,maxY,maxZ);
		}


		public void detach()
		// Centers the object in its coordinate system
		// The offset from origin to object center will be transfered to the matrix,
		// so your object actually does not move.
		// Usefull if you want prepare objects for self rotation.
		{
			IVector center=getCenter();

			for (int i=0;i<vertices;i++)
			{
				vertex[i].pos.x-=center.x;
				vertex[i].pos.y-=center.y;
				vertex[i].pos.z-=center.z;
			}
			shift(center);
		}

		public IVector getCenter()
		// Returns the center of this object
		{
			IVector max=max();
			IVector min=min();
			return new IVector((max.x+min.x)/2,(max.y+min.y)/2,(max.z+min.z)/2);
		}

		public IVector getDimension()
		// Returns the x,y,z - Dimension of this object
		{
			IVector max=max();
			IVector min=min();
			return new IVector(max.x-min.x,max.y-min.y,max.z-min.z);
		}

		public void matrixMeltdown()
		// Applies the transformations in the matrix to all vertices
		// and resets the matrix to untransformed.
		{
			rebuild();
			for (int i=vertices-1;i>=0;i--)
			{
				//vertex[i].pos= vertex[i].pos.transform(matrix);

				vertex[i].pos.transform(matrix,vertex[i].pos);
			}
			regenerate();
			matrix.reset();
			normalmatrix.reset();
		}

		public IObject getClone()
		{
			IObject obj=new IObject();
			rebuild();
			for(int i=0;i<vertices;i++) obj.addVertex(vertex[i].getClone());
			for(int i=0;i<triangles;i++) obj.addTriangle(triangle[i].getClone());
			obj.name=name+" [cloned]";
			obj.material=material;
			obj.matrix=matrix.getClone();
			obj.normalmatrix=normalmatrix.getClone();
			obj.rebuild();
			return obj;
		}

		public void removeDuplicateVertices()
		{
			rebuild();
			Vector edgesToCollapse=new Vector();
			for (int i=0;i<vertices;i++)
				for (int j=i+1;j<vertices;j++)
					if (vertex[i].equals(vertex[j],0.0001f))
						edgesToCollapse.addElement(new IEdge(vertex[i],vertex[j]));
			Enumeration en=edgesToCollapse.elements();
			while(en.hasMoreElements()) edgeCollapse((IEdge)en.nextElement());

			removeDegeneratedTriangles();
		}

		public void removeDegeneratedTriangles()
		{
			rebuild();
			for (int i=0;i<triangles;i++)
				if (triangle[i].degenerated()) removeTriangleAt(i);

			dirty=true;
			rebuild();
		}

		public void meshSmooth()
		{
			rebuild();
			ITriangle tri;
			float u,v;
			IVertex a,b,c,d,e,f,temp;
			IVector ab,bc,ca,nab,nbc,nca,center;
			float sab,sbc,sca,rab,rbc,rca;
			float uab,vab,ubc,vbc,uca,vca;
			float sqrt3=(float)Math.sqrt(3f);

			for (int i=0;i<triangles;i++)
			{
				tri=triangle(i);
				a=tri.p1;
				b=tri.p2;
				c=tri.p3;
				ab=IVector.scale(0.5f,IVector.add(b.pos,a.pos));
				bc=IVector.scale(0.5f,IVector.add(c.pos,b.pos));
				ca=IVector.scale(0.5f,IVector.add(a.pos,c.pos));
				rab=IVector.sub(ab,a.pos).length();
				rbc=IVector.sub(bc,b.pos).length();
				rca=IVector.sub(ca,c.pos).length();

				nab=IVector.scale(0.5f,IVector.add(a.n,b.n));
				nbc=IVector.scale(0.5f,IVector.add(b.n,c.n));
				nca=IVector.scale(0.5f,IVector.add(c.n,a.n));
				uab=0.5f*(a.u+b.u);
				vab=0.5f*(a.v+b.v);
				ubc=0.5f*(b.u+c.u);
				vbc=0.5f*(b.v+c.v);
				uca=0.5f*(c.u+a.u);
				vca=0.5f*(c.v+a.v);
				sab=1f-nab.length();
				sbc=1f-nbc.length();
				sca=1f-nca.length();
				nab.normalize();
				nbc.normalize();
				nca.normalize();

				d=new IVertex(IVector.sub(ab,IVector.scale(rab*sab,nab)),uab,vab);
				e=new IVertex(IVector.sub(bc,IVector.scale(rbc*sbc,nbc)),ubc,vbc);
				f=new IVertex(IVector.sub(ca,IVector.scale(rca*sca,nca)),uca,vca);

				addVertex(d);
				addVertex(e);
				addVertex(f);
				tri.p2=d;
				tri.p3=f;
				addTriangle(b,e,d);
				addTriangle(c,f,e);
				addTriangle(d,e,f);
			}
			removeDuplicateVertices();
		}


	// P R I V A T E   M E T H O D S

		private void edgeCollapse(IEdge edge)
		// Collapses the edge [u,v] by replacing v by u
		{
			IVertex u=edge.start();
			IVertex v=edge.end();
			if (!vertexData.contains(u)) return;
			if (!vertexData.contains(v)) return;
			rebuild();
			ITriangle tri;
			for (int i=0; i<triangles; i++)
			{
				tri=triangle(i);
				if (tri.p1==v) tri.p1=u;
				if (tri.p2==v) tri.p2=u;
				if (tri.p3==v) tri.p3=u;
			}
			vertexData.removeElement(v);
		}
}
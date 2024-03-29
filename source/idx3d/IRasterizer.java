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

import static idx3d.IColor.ALPHA;
import static idx3d.IColor.MASK7Bit;

public final class IRasterizer
{
	private boolean materialLoaded=false;
	private boolean lightmapLoaded=false;
	public boolean ready=false;
	
	// Current material settings
		private int color=0;
		private int currentColor=0;
		private int transparency=0;
		private int reflectivity=0;
		private int refraction=0;
		private ITexture texture=null;
		private int[] envmap=null;
		private int[] diffuse=null;
		private int[] specular=null;
		private short[] refractionMap=null;
		private int tw=0;
		private int th=0;
		private int tbitW=0;
		private int tbitH=0;
		
	// Rasterizer hints
	
		private int mode=0;
		private static final int F=0;   	// FLAT
		private static final int W=1;	// WIREFRAME
		private static final int P=2;  	// PHONG
		private static final int E=4;  	// ENVMAP
		private static final int T=8; 	// TEXTURED
		private static final int SHADED=P|E|T;
		
	//  R E G I S T E R S

		IVertex p1,p2,p3,tempVertex;
		private int
			bkgrd,c,s,lutID,r,   //lutID is position in LUT (diffuse,envmap,specular)
		
			x1,x2,x3,x4,y1,y2,y3,z1,z2,z3,z4,
			x,y,z,k,dx,dy,dz,offset,pos,temp,
			xL,xR,xBase,zBase,xMax,yMax,dxL,dxR,dzBase,
			
			nx1,nx2,nx3,nx4,ny1,ny2,ny3,ny4,
			nxBase,nyBase,
			dnx4,dny4,
			dnx,dny,nx,ny,
			dnxBase,dnyBase,
			
			tx1,tx2,tx3,tx4,ty1,ty2,ty3,ty4,
			txBase,tyBase,
			dtx4,dty4,
			dtx,dty,tx,ty,
			dtxBase,dtyBase;
			
		IScreen screen;
		int[] zBuffer;
		int[] idBuffer;
		int width,height;
		boolean useIdBuffer;
		final int zFar=0xFFFFFFF;
		int currentId=0;
	
		
	// Constructor
	
		public IRasterizer(IRenderPipeline pipeline)
		{
			rebuildReferences(pipeline);			
			loadLightmap(pipeline.lightmap);
		}
		
	// References
	
		void rebuildReferences(IRenderPipeline pipeline)
		{
			screen=pipeline.screen;
			zBuffer=pipeline.zBuffer;
			idBuffer=pipeline.idBuffer;
			width=screen.w;
			height=screen.h;
			useIdBuffer=pipeline.useIdBuffer;
		}
		
	// Lightmap loader
	
		public void loadLightmap(ILightmap lm)
		{
			if (lm==null) return;
			diffuse=lm.diffuse;
			specular=lm.specular;
			lightmapLoaded=true;
			ready=lightmapLoaded&&materialLoaded;
		}
		
	// Material loader
		
		public void loadMaterial(IMaterial material)
		{
			color=material.color;
			transparency=material.transparency;
			reflectivity=material.reflectivity;
			texture=material.texture;
			if (material.envmap!=null) envmap=material.envmap.pixel;
			else envmap=null;
			
			if (texture!=null)
			{
				tw=texture.width-1;
				th=texture.height-1;
				tbitW=texture.bitWidth;
				tbitH=texture.bitHeight;
			}
					
			mode=0;
			if (!material.flat) mode|=P;
			if (envmap!=null) mode|=E;
			if (texture!=null) mode|=T;
			if (material.wireframe) mode|=W;
			materialLoaded=true;
			ready=lightmapLoaded&&materialLoaded;
		}
		
		public void render(ITriangle tri)
		{
			if (!ready) return;
			if (tri.parent==null) return;
			if ((mode&W)!=0)
			{
				drawWireframe(tri,color);
				if ((mode&W)==0) return;
				
			}
						
			p1=tri.p1;
			p2=tri.p2;
			p3=tri.p3;
				
			if (p1.y>p2.y) { tempVertex=p1; p1=p2; p2=tempVertex; }
			if (p2.y>p3.y) { tempVertex=p2; p2=p3; p3=tempVertex; }
			if (p1.y>p2.y) { tempVertex=p1; p1=p2; p2=tempVertex; }
				
			if (p1.y>=height) return;
			if (p3.y<0) return;
			if (p1.y==p3.y) return;
			
			if (mode==F)
			{
				lutID=(int)(tri.n2.x*127+127)+((int)(tri.n2.y*127+127)<<8);
				c=IColor.multiply(color,diffuse[lutID]);
				s=IColor.scale(specular[lutID],reflectivity);
				currentColor=IColor.add(c,s);
			}
			
			currentId=(tri.parent.id<<16)|tri.id;
			
			x1=p1.x<<8;
			x2=p2.x<<8;
			x3=p3.x<<8;
			y1=p1.y;
			y2=p2.y;
			y3=p3.y;
			
			x4=x1+(x3-x1)*(y2-y1)/(y3-y1);
			x1<<=8; x2<<=8; x3<<=8; x4<<=8;

			z1=p1.z;
			z2=p2.z;
			z3=p3.z;
			nx1=p1.nx<<16;
			nx2=p2.nx<<16;
			nx3=p3.nx<<16;
			ny1=p1.ny<<16;
			ny2=p2.ny<<16;
			ny3=p3.ny<<16;
			tx1=p1.tx<<16;
			tx2=p2.tx<<16;
			tx3=p3.tx<<16;
			ty1=p1.ty<<16;
			ty2=p2.ty<<16;
			ty3=p3.ty<<16;
			
			dx=(x4-x2)>>16;
			if (dx==0) return;
			
			temp=256*(y2-y1)/(y3-y1);
			
			z4=z1+((z3-z1)>>8)*temp;
			nx4=nx1+((nx3-nx1)>>8)*temp;
			ny4=ny1+((ny3-ny1)>>8)*temp;
			tx4=tx1+((tx3-tx1)>>8)*temp;
			ty4=ty1+((ty3-ty1)>>8)*temp;

			dz=(z4-z2)/dx;
			dnx=(nx4-nx2)/dx;
			dny=(ny4-ny2)/dx;
			dtx=(tx4-tx2)/dx;
			dty=(ty4-ty2)/dx;

			
			if (dx<0)
			{ 
				temp=x2; x2=x4; x4=temp; 
				z2=z4;
				tx2=tx4;
				ty2=ty4;
				nx2=nx4;
				ny2=ny4;
			}
			if (y2>=0)
			{
				dy=y2-y1;
				if (dy!=0)
				{
					dxL=(x2-x1)/dy;
					dxR=(x4-x1)/dy;
					dzBase=(z2-z1)/dy;
					dnxBase=(nx2-nx1)/dy;
					dnyBase=(ny2-ny1)/dy;
					dtxBase=(tx2-tx1)/dy;
					dtyBase=(ty2-ty1)/dy;
				}
							
				xBase=x1;
				xMax=x1;
				zBase=z1;
				nxBase=nx1;
				nyBase=ny1;
				txBase=tx1;
				tyBase=ty1;
				
				if (y1<0)
				{
					xBase-=y1*dxL;
					xMax-=y1*dxR;
					zBase-=y1*dzBase;
					nxBase-=y1*dnxBase;
					nyBase-=y1*dnyBase;
					txBase-=y1*dtxBase;
					tyBase-=y1*dtyBase;
					y1=0;
				}
				
				y2=(y2<height)?y2:height;
				offset=y1*width;
				for (y=y1;y<y2;y++) renderLine();
			}
			
			if (y2<height)
			{
				dy=y3-y2;
				if (dy!=0)
				{
					dxL=(x3-x2)/dy;
					dxR=(x3-x4)/dy;
					dzBase=(z3-z2)/dy;
					dnxBase=(nx3-nx2)/dy;
					dnyBase=(ny3-ny2)/dy;
					dtxBase=(tx3-tx2)/dy;
					dtyBase=(ty3-ty2)/dy;
				}
								
				xBase=x2;
				xMax=x4;
				zBase=z2;
				nxBase=nx2;
				nyBase=ny2;
				txBase=tx2;
				tyBase=ty2;
				
				if (y2<0)
				{
					xBase-=y2*dxL;
					xMax-=y2*dxR;
					zBase-=y2*dzBase;
					nxBase-=y2*dnxBase;
					nyBase-=y2*dnyBase;
					txBase-=y2*dtxBase;
					tyBase-=y2*dtyBase;
					y2=0;
				}
				
				y3=(y3<height)?y3:height;
				offset=y2*width;
				
				for (y=y2;y<y3;y++) renderLine();
			}
		}
		
		private void renderLine() {
			xL = xBase >> 16;
			xR = xMax >> 16;
			z = zBase;
			nx = nxBase;
			ny = nyBase;
			tx = txBase;
			ty = tyBase;

			if (xL < 0) {
				z -= xL * dz;
				nx -= xL * dnx;
				ny -= xL * dny;
				tx -= xL * dtx;
				ty -= xL * dty;
				xL = 0;
			}
			xR = (xR < width) ? xR : width;
			
//			if (mode==F) renderLineF();                        
//			else if ((mode&SHADED)==P) renderLineP();
//			else if ((mode&SHADED)==E) renderLineE();
//			else if ((mode&SHADED)==T) renderLineT();
//			else if ((mode&SHADED)==(P|E)) renderLinePE();
//			else if ((mode&SHADED)==(P|T)) renderLinePT();
//			else if ((mode&SHADED)==(P|E|T)) renderLinePET();
			
                        int modeShade = mode&SHADED;
                        switch(modeShade){
                            case F:renderLineF();break;
                            case P: renderLineP();break;
                            case E: renderLineE();break;
                            case T: renderLineT();break;
                            case (P|E): renderLinePE();break;    
                            case (P|T): renderLinePT();break; 
                            case (P|E|T):renderLinePET();break;    
                            default: break;
                        }
                        
						offset += width;
						xBase += dxL;
						xMax += dxR;
						zBase += dzBase;
						nxBase += dnxBase;
						nyBase += dnyBase;
						txBase += dtxBase;
						tyBase += dtyBase;
		}
		
	// Fast scanline rendering
	
		private void renderLineF()
		{
			for (x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					bkgrd=screen.p[pos];
					c=IColor.transparency(bkgrd,currentColor,transparency);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
			}
			
		}
		
		private void renderLineP()
		{
			for (int x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					lutID=((nx>>16)&255)+(((ny>>16)&255)<<8);
					bkgrd=screen.p[pos];
					c=IColor.multiply(color,diffuse[lutID]);
					s=specular[lutID];
					s=IColor.scale(s,reflectivity);
					c=IColor.transparency(bkgrd,c,transparency);
					c=IColor.add(c,s);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
				nx+=dnx;
				ny+=dny;
			}
			
		}
		
		private void renderLineE() {
			for (x = xL; x < xR; x++) {
				pos = x + offset;
				if (z < zBuffer[pos]) {
					lutID = ((nx >> 16) & 255) + (((ny >> 16) & 255) << 8);
					bkgrd = screen.p[pos];
					s = IColor.add(specular[lutID], envmap[lutID]);
					s = IColor.scale(s, reflectivity);
					c = IColor.transparency(bkgrd, s, transparency);

					screen.p[pos] = 0xFF000000 | c;
					zBuffer[pos] = z;
					if (useIdBuffer)
						idBuffer[pos] = currentId;
				}
				z += dz;
				nx += dnx;
				ny += dny;
			}

		}
		
		private void renderLineT()
		{
			for (x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					bkgrd=screen.p[pos];
					c=texture.pixel[((tx>>16)&tw)+(((ty>>16)&th)<<tbitW)];
					c=IColor.transparency(bkgrd,c,transparency);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
				tx+=dtx;
				ty+=dty;
			}
			
		}
		
		private void renderLinePE()
		{
			for (x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					lutID=((nx>>16)&255)+(((ny>>16)&255)<<8);
					bkgrd=screen.p[pos];
					c=IColor.multiply(color,diffuse[lutID]);
					s=IColor.add(specular[lutID],envmap[lutID]);
					s=IColor.scale(s,reflectivity);
					c=IColor.transparency(bkgrd,c,transparency);
					c=IColor.add(c,s);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
				nx+=dnx;
				ny+=dny;
			}
		}
		
		private void renderLinePT()
		{
			for (x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					lutID=((nx>>16)&255)+(((ny>>16)&255)<<8);
					bkgrd=screen.p[pos];
					c=texture.pixel[((tx>>16)&tw)+(((ty>>16)&th)<<tbitW)];
					c=IColor.multiply(c,diffuse[lutID]);
					s=specular[lutID];
					s=IColor.scale(s,reflectivity);
					c=IColor.transparency(bkgrd,c,transparency);
					c=IColor.add(c,s);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
				nx+=dnx;
				ny+=dny;
				tx+=dtx;
				ty+=dty;
			}
		}
		
		private void renderLinePET()
		{		
			for (x=xL;x<xR;x++)
			{
				pos=x+offset;
				if (z<zBuffer[pos])
				{
					lutID=((nx>>16)&255)+(((ny>>16)&255)<<8);
					bkgrd=screen.p[pos];
					c=texture.pixel[((tx>>16)&tw)+(((ty>>16)&th)<<tbitW)];
					c=IColor.multiply(c,diffuse[lutID]);
					s=IColor.add(specular[lutID],envmap[lutID]);
					s=IColor.scale(s,reflectivity);
					c=IColor.transparency(bkgrd,c,transparency);
					c=IColor.add(c,s);
										
					screen.p[pos]=0xFF000000|c;
					zBuffer[pos]=z;
					if (useIdBuffer) idBuffer[pos]=currentId;
				}
				z+=dz;
				nx+=dnx;
				ny+=dny;
				tx+=dtx;
				ty+=dty;
			}
		}
		
		private void drawWireframe(ITriangle tri, int defaultcolor)
		{
			drawLine(tri.p1,tri.p2,defaultcolor);
			drawLine(tri.p2,tri.p3,defaultcolor);
			drawLine(tri.p3,tri.p1,defaultcolor);
		}

	
		private void drawLine(IVertex a, IVertex b ,int color)
		{
			IVertex temp;
			if ((a.clipcode&b.clipcode)!=0) return;

			dx=(int)Math.abs(a.x-b.x);
			dy=(int)Math.abs(a.y-b.y);
			dz=0;

			if (dx>dy)
			{
				if (a.x>b.x){temp=a; a=b; b=temp;}
				if (dx>0)
				{
					dz=(b.z-a.z)/dx;
					dy=((b.y-a.y)<<16)/dx;
				}
				z=a.z;
				y=a.y<<16;
				for(x=a.x;x<=b.x;x++)
				{
					y2=y>>16;
					if (IMath.inrange(x,0,width-1)&&IMath.inrange(y2,0,height-1))
					{
						offset=y2*width;
						if (z<zBuffer[x+offset])
						{
							if (!screen.antialias) 
							{
								screen.p[x+offset]=color;
								zBuffer[x+offset]=z;
							}							
							else
							{
								screen.p[x+offset]=color;
								screen.p[x+offset+1]=color;
								screen.p[x+offset+width]=color;
								screen.p[x+offset+width+1]=color;
								zBuffer[x+offset]=z;
							}	
						}
						if (useIdBuffer) idBuffer[x+offset]=currentId;
					}
					z+=dz; y+=dy;	
				}
			}
			else
			{
				if (a.y>b.y){ temp=a; a=b; b=temp; }
				if (dy>0)
				{
					dz=(b.z-a.z)/dy;
					dx=((b.x-a.x)<<16)/dy;
				}
				z=a.z;
				x=a.x<<16;
				for(y=a.y;y<=b.y;y++)
				{
					x2=x>>16;
					if (IMath.inrange(x2,0,width-1)&&IMath.inrange(y,0,height-1))
					{
						offset=y*width;
						if (z<zBuffer[x2+offset])
						{
							if (!screen.antialias) 
							{
								screen.p[x2+offset]=color;
								zBuffer[x2+offset]=z;
							}							
							else
							{
								screen.p[x2+offset]=color;
								screen.p[x2+offset+1]=color;
								screen.p[x2+offset+width]=color;
								screen.p[x2+offset+width+1]=color;
								zBuffer[x2+offset]=z;
							}
						}
						if (useIdBuffer) idBuffer[x2+offset]=currentId;
					}
					z+=dz; x+=dx;
				}
			}
		}
                
                
                private final static int add(int color1, int color2)
		// Adds color1 and color2
		{
			int pixel=(color1&MASK7Bit)+(color2&MASK7Bit);
			int overflow=pixel&0x1010100;
			overflow=overflow-(overflow>>8);
			return ALPHA|overflow|pixel;			
		}
                
		
}
	
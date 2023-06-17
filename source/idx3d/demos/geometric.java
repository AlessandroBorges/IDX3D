package idx3d.demos;
import idx3d.*;
import java.awt.*;
import java.applet.*;

public final class geometric extends Applet implements Runnable
{
	private Thread idx_Thread;
	IScene scene;

	int oldx=0;
	int oldy=0;
	boolean autorotation=true;
	boolean antialias=false;
	float speed=1f;

	public void init()
	{
		setNormalCursor();
		
		// BUILD SCENE
		
			scene=new IScene(this.size().width,this.size().height);
		
			scene.addMaterial("default",new IMaterial(new ITexture(getDocumentBase(),"texture.jpg")));
			scene.material("default").setEnvmap(new ITexture(getDocumentBase(),"skymap.jpg"));
			scene.material("default").setWireframe(true);
			scene.material("default").setColor(0x0066FF);
			//scene.material("default").setFlat(true);
			//scene.addLight("Light1",new ILight(new IVector(0.2f,0.2f,1f),0xFFFFFF,80,200));			
			//scene.addLight("Light2",new ILight(new IVector(-1f,-1f,0.5f),0xFFDDAA,120,80));
			
			scene.addLight("Light1",new ILight(new IVector(0f,0f,1f),0xFFFFFF,200,80));
			
			try{
				new I3ds_Importer().importFromURL(new java.net.URL(getDocumentBase(),"demon.3ds"),scene);
				scene.normalize();
				
				scene.object("Skydome").setMaterial(scene.material("default"));
				
				scene.object("Demon").setMaterial(scene.material("default"));
				ITextureProjector.projectTop(scene.object("Demon"));
				
				scene.object("Demon").matrixMeltdown();
				scene.defaultCamera.lookAt(scene.object("Demon").getCenter().transform(scene.matrix));
				scene.rotate(3.84159265f/2,0f,0f);
				scene.scale(3f);
				scene.shift(0,-1f,0);
			}
			catch(Exception e){System.out.println(e+"");}
			
	}

	public synchronized void paint(Graphics g)
	{
	}

	public void start()
	{
		if (idx_Thread == null)
		{
			idx_Thread = new Thread(this);
			idx_Thread.start();
		}
	}
	
	public void stop()
	{
		if (idx_Thread != null)
		{
			idx_Thread.stop();
			idx_Thread = null;
		}
	}

	public void run()
	{
		while(true)
		{
			repaint();
			try
			{
				idx_Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				System.out.println("idx://interrupted");
			}
		}
	}

	public synchronized void update(Graphics g)
	{
		scene.render();
		g.drawImage(scene.getImage(),0,0,this);
	}		

	public boolean imageUpdate(Image image, int a, int b, int c, int d, int e)
   	{
   	     return true;
   	}

	public boolean mouseDown(Event evt,int x,int y)
	{
		oldx=x;
		oldy=y;
		setMovingCursor();
		return true;
	}

	public boolean keyDown(Event evt,int key)
	{
		if (key==32) { System.out.println(scene.getFPS()+""); return true; }
		if (key==Event.PGUP) {scene.shift(0f,0f,0.2f); return true; }
		if (key==Event.PGDN) {scene.shift(0f,0f,-0.2f); return true; }
		if (key==Event.UP) {scene.shift(0f,0.2f,0f); return true; }
		if (key==Event.DOWN) {scene.shift(0f,-0.2f,0f); return true; }
		if (key==Event.LEFT) {scene.shift(-0.2f,0f,0f); return true; }
		if (key==Event.RIGHT) {scene.shift(0.2f,0f,0f); return true; }
		if ((char)key=='+') {scene.scaleSelf(1.2f); return true; }
		if ((char)key=='-') {scene.scaleSelf(0.8f); return true; }
		if ((char)key=='.') {speed*=1.2f;  return true; }
		if ((char)key==',') {speed*=0.8f; return true; }
		if ((char)key=='r') {scene.resize(200,200); return true; }		
		if ((char)key=='a') {antialias=!antialias; scene.setAntialias(antialias); return true; }
		
		if ((char)key=='f') {for (int i=0; i<scene.objects;i++) ITextureProjector.projectFrontal(scene.object[i]); return true; }
		if ((char)key=='t') {for (int i=0; i<scene.objects;i++) ITextureProjector.projectTop(scene.object[i]); return true; }
		if ((char)key=='c') {for (int i=0; i<scene.objects;i++) ITextureProjector.projectCylindric(scene.object[i]); return true; }
		if ((char)key=='d') {for (int i=0;i<scene.objects;i++) scene.object[i].removeDuplicateVertices(); scene.printSceneInfo(); return true; }
		if ((char)key=='m') {for (int i=0;i<scene.objects;i++) scene.object[i].meshSmooth(); return true; }
		return true;
	}


	
	public boolean mouseDrag(Event evt,int x,int y)
	{
		autorotation=false;
		float dx=(float)(y-oldy)/50;
		float dy=(float)(oldx-x)/50;
		scene.rotate(dx,dy,0);
		oldx=x;
		oldy=y;
		return true;
	}

	public boolean mouseUp(Event evt,int x,int y)
	{
		autorotation=true;
		setNormalCursor();
		return true;
	}
	
	private void setMovingCursor()
	{
		if (getFrame()==null) return;
		getFrame().setCursor(Frame.MOVE_CURSOR);
	}
	
	private void setNormalCursor()
	{
		if (getFrame()==null) return;
		getFrame().setCursor(Frame.HAND_CURSOR);		
	}
	
	private Frame getFrame()
	{
		Component comp=this;
		while ((comp=comp.getParent())!=null) if(comp instanceof Frame) return (Frame)comp;
		return null;
	}
}

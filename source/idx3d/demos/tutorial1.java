package idx3d.demos;
import idx3d.*;
import java.awt.*;
import java.applet.*;

public class tutorial1 extends Applet
{
	IScene scene;
	int oldx=0;
	int oldy=0;
	
	public void init()
	{		
		// Prepare Scene
		
		scene=new IScene(this.size().width,this.size().height);
		scene.setAntialias(true);
		
		// Import material (generated with idx3dMaterialLab)
			
			IMaterial marble=new IMaterial(getDocumentBase(),"marble.material");
			scene.addMaterial("Marble",marble);
		
		// Add light sources
		
			scene.addLight("Light1",new ILight(new IVector(0.2f,0.2f,1f),0xFFFFFF,320,120));			
			scene.addLight("Light2",new ILight(new IVector(-1f,-1f,1f),0xFFCC99,160,200));
		
		// Create Trefoil with Object Factory
		
			IObject trefoil=IObjectFactory.TORUSKNOT(2f,3f,0.4f,1.2f,0.48f,1.2f,120,8);
			scene.addObject("Trefoil",trefoil);
			
		// Assign material to object
		
			scene.object("Trefoil").setMaterial(scene.material("Marble"));
			ITextureProjector.projectTop(scene.object("Trefoil"));
			
			scene.object("Trefoil").rotate(0.2f,3.5f,-0.5f);
			scene.object("Trefoil").scale(0.4f);								
	}

	public void paint(Graphics g)
	{
		repaint();
	}

	public void update(Graphics g)
	{
		// Render scene into buffer
			scene.render();
			
		// Aquire rendered image and display it
			g.drawImage(scene.getImage(),0,0,this);
	}

	public boolean mouseDown(Event evt,int x,int y)
	{
		scene.setAntialias(false);
		oldx=x;
		oldy=y;
		return true;
	}
	
	public boolean mouseUp(Event evt, int x, int y)
	{
		scene.setAntialias(true);
		repaint();
		return true;
	}
	
	public boolean mouseDrag(Event evt,int x,int y)
	{
		float dx=(float)(y-oldy)/50;
		float dy=(float)(oldx-x)/50;
		scene.rotate(dx,dy,0);
		oldx=x;
		oldy=y;
		repaint();
		return true;
	}
}

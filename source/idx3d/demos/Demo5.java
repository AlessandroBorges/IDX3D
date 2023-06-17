package idx3d.demos;

import idx3d.*;
import java.awt.*;
import java.applet.*;

public final class Demo5 extends RunnableApplet{ // Applet implements Runnable {
    
	
	static {
		 System.setProperty("sun.java2d.d3d", "false");
		 System.setProperty("sun.java2d.noddraw", "true");
	}
	
    private boolean useAWTThread = false;
    
    private Image renderImage;

    public void init() {
        setNormalCursor();

        // BUILD SCENE

        scene = new IScene(this.size().width, this.size().height);
        scene.defaultCamera.setFov(100f);

        // Material setup

        scene.addMaterial("Glass", new IMaterial(getCodeBase(), "glass.material"));
        scene.addMaterial("Chrome", new IMaterial(getCodeBase(), "chrome.material"));
        scene.addMaterial("Blue", new IMaterial(getCodeBase(), "blue.material"));
        scene.addMaterial("Orange", new IMaterial(0xFF6600));

        IMaterial flatGreen = new IMaterial(0x00FF00);
        flatGreen.setFlat(true);
        scene.addMaterial("Green", flatGreen);

        scene.addMaterial("Yellow", new IMaterial(0xFFCC00));

        // Light setup

        scene.addLight("Light1", new ILight(new IVector(0.2f, 0.2f, 1f), 0xFFFFFF, 320, 80));
        scene.addLight("Light2", new ILight(new IVector(-1f, -1f, 1f), 0xFFCC99, 100, 40));
        scene.addLight("Light3", new ILight(new IVector(0f, -1f, 0.5f), 0x666666, 320, 80));

        // Object setup

        try {
            new I3ds_Importer().importFromURL(new java.net.URL(getCodeBase(), "demo5.3ds"), scene);
        } catch (Exception e) {
            System.out.println(e + "");
        }

        scene.rebuild();
        for (int i = 0; i < scene.objects; i++)
            scene.object[i].detach();
        scene.normalize();
        scene.scale(0.64f);

        scene.object("Blob1").setMaterial(scene.material("Glass"));
        scene.object("Blob2").setMaterial(scene.material("Blue"));
        scene.object("Text1").setMaterial(scene.material("Chrome"));
        scene.object("Text2").setMaterial(scene.material("Chrome"));
        scene.object("Cube").setMaterial(scene.material("Green"));
        scene.object("Cone").setMaterial(scene.material("Yellow"));
        scene.object("Sphere").setMaterial(scene.material("Orange"));
        ITextureProjector.projectFrontal(scene.object("Blob1"));

        ITexture bkgrd = ITexture.blendTopDown(
                ITextureFactory.CHECKERBOARD(this.size().width, this.size().height, 4, 0x000000, 0x999999),
                new ITexture(getCodeBase(), "idxbkgrd.jpg"));
        scene.environment.setBackground(bkgrd);

    }

    public synchronized void paint(Graphics g) {
    }

    public void start() {
        if (idx_Thread == null) {
            idx_Thread = new Thread(this, "IDX3D Thread");
            idx_Thread.setPriority(Thread.MAX_PRIORITY);
            System.out.println("IDX Thread MAX");
            idx_Thread.start();
        }
    }

    public void stop() {
        if (idx_Thread != null) {
            idx_Thread.interrupt();//stop();
            idx_Thread = null;
        }
    }

   private int countSkip = 0; 
   
	public void run() {
		while (true) {
			if (!useAWTThread) {
				if (!drawMode) {
					render();
				} else {
					System.err.println("Skip rendering " + ++countSkip);
				}
			}
			repaint();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}

    /**
     * Render the Scene
     */
    public void render(){
        if (autorotation) {
            float speed = 0.2f;
            float dx = (float) Math.sin((float) System.currentTimeMillis() / 1000) / 20;
            float dy = (float) Math.cos((float) System.currentTimeMillis() / 1000) / 20;
            scene.rotate(-speed * dx / 4, speed * dy, speed * 0.04f);

        }
        scene.object("Blob2").rotateSelf(0.02f, 0.05f, -0.03f);
        scene.object("Cube").rotateSelf(-0.08f, 0.02f, 0.07f);
        scene.object("Cone").rotateSelf(0.03f, -0.06f, 0.04f);
        scene.object("Text2").rotateSelf(0.03f, 0, 0.04f);

        scene.render();
        renderImage = scene.getImage();
    }
    
    private boolean drawMode = false;
    
    public synchronized void update(Graphics g) {
       if(useAWTThread){
           render();
       }
       drawMode = true;
        g.drawImage(renderImage, 0, 0, this);        
         if ((count++ % 140) == 0) {
            long t2 =  System.currentTimeMillis() - time;
            System.err.println("FPS: " + (1000*140/(float)t2) +"\t\t Frames:" + count);
            time =  System.currentTimeMillis() ;
        }
       drawMode = false;
    }
    
    private long time = System.currentTimeMillis();

    private int count = 0;
    
    
    public boolean imageUpdate(Image image, int a, int b, int c, int d, int e) {
        return true;
    }

    public boolean mouseDown(Event evt, int x, int y) {
        oldx = x;
        oldy = y;
        setMovingCursor();
        return true;
    }

    public boolean mouseMove(Event evt, int x, int y) {
        oldx = x;
        oldy = y;
        return true;
    }

    public boolean keyDown(Event evt, int key) {
        if (key == 32) {
            System.out.println(scene.getFPS() + "");
            return true;
        }
        if (key == Event.PGUP) {
            scene.defaultCamera.shift(0f, 0f, 0.2f);
            return true;
        }
        if (key == Event.PGDN) {
            scene.defaultCamera.shift(0f, 0f, -0.2f);
            return true;
        }
        if (key == Event.UP) {
            scene.defaultCamera.shift(0f, -0.2f, 0f);
            return true;
        }
        if (key == Event.DOWN) {
            scene.defaultCamera.shift(0f, 0.2f, 0f);
            return true;
        }
        if (key == Event.LEFT) {
            scene.defaultCamera.shift(0.2f, 0f, 0f);
            return true;
        }
        if (key == Event.RIGHT) {
            scene.defaultCamera.shift(-0.2f, 0f, 0f);
            return true;
        }

        if ((char) key == 'a') {
            antialias = !antialias;
            scene.setAntialias(antialias);
            return true;
        }
        if ((char) key == '+') {
            scene.scale(1.2f);
            return true;
        }
        if ((char) key == '-') {
            scene.scale(0.8f);
            return true;
        }
        return true;
    }

    public boolean mouseDrag(Event evt, int x, int y) {
        autorotation = false;
        float dx = (float) (y - oldy) / 50;
        float dy = (float) (oldx - x) / 50;
        scene.rotate(dx, dy, 0);
        oldx = x;
        oldy = y;
        return true;
    }

    public boolean mouseUp(Event evt, int x, int y) {
        autorotation = true;
        setNormalCursor();
        return true;
    }

    public static void main(String[] args) {
        Demo5 demo = new Demo5();
        demo.launch(1024,1024);
    }
   
}

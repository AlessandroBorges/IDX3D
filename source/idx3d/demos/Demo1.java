package idx3d.demos;

import idx3d.*;
import java.awt.*;
import java.applet.*;
import java.awt.image.VolatileImage;

@SuppressWarnings("serial")
public final class Demo1 extends RunnableApplet{ //Applet implements Runnable {
 
	
	
	public Demo1() {
		super();
	}
    /*
    private Thread idx_Thread;
    IScene scene;

    int oldx = 0;
    int oldy = 0;
    boolean autorotation = true;
    boolean antialias = false;
*/
    private VolatileImage volImage = null;
    private Image origImage = null;

    public void init() {
        setNormalCursor();

		// BUILD SCENE
        this.scene = new IScene(this.getWidth(), this.getHeight());
        IScene scene = this.scene; 
        IMaterial crystal = new IMaterial(getCodeBase(), "glass.material");
        crystal.setReflectivity(255);
        scene.addMaterial("Crystal", crystal);

        IMaterial plastic = new IMaterial(new ITexture(getCodeBase(), "texture.jpg"));
        scene.addMaterial("Plastic", plastic);

        scene.environment.setBackground(ITextureFactory.CHECKERBOARD(160, 120, 2, 0x000000, 0x999999));

        scene.addLight("Light1", new ILight(new IVector(0.2f, 0.2f, 1f), 0xFFFFFF, 320, 80));
        scene.addLight("Light2", new ILight(new IVector(-1f, -1f, 1f), 0xFFCC99, 100, 40));

			// Create Torus as a Lattice Object from a circle textureName
        IVector[] path = new IVector[9];

        path[0] = new IVector(0.4f, 0.0f, 0);
        path[1] = new IVector(0.6f, 0.3f, 0);
        path[2] = new IVector(0.8f, 0.4f, 0);
        path[3] = new IVector(0.9f, 0.3f, 0);
        path[4] = new IVector(1.0f, 0.0f, 0);
        path[5] = new IVector(0.9f, -0.3f, 0);
        path[6] = new IVector(0.8f, -0.4f, 0);
        path[7] = new IVector(0.6f, -0.3f, 0);
        path[8] = new IVector(0.4f, 0.0f, 0);

        scene.addObject("Torus", IObjectFactory.ROTATIONOBJECT(path, 16));
        scene.object("Torus").rotate(4.2f, 0.2f, -0.5f);
        scene.object("Torus").shift(-0.5f, 0f, 0f);
        scene.object("Torus").scale(0.72f);
        scene.object("Torus").setMaterial(scene.material("Plastic"));

			// Create Wineglass as a Lattice Object
        path = new IVector[15];
        path[0] = new IVector(0.0f, 0.2f, 0);
        path[1] = new IVector(0.13f, 0.25f, 0);
        path[2] = new IVector(0.33f, 0.3f, 0);
        path[3] = new IVector(0.43f, 0.6f, 0);
        path[4] = new IVector(0.48f, 0.9f, 0);
        path[5] = new IVector(0.5f, 0.9f, 0);
        path[6] = new IVector(0.45f, 0.6f, 0);
        path[7] = new IVector(0.35f, 0.3f, 0);
        path[8] = new IVector(0.25f, 0.2f, 0);
        path[9] = new IVector(0.1f, 0.15f, 0);
        path[10] = new IVector(0.1f, 0.0f, 0);
        path[11] = new IVector(0.1f, -0.5f, 0);
        path[12] = new IVector(0.35f, -0.55f, 0);
        path[13] = new IVector(0.4f, -0.6f, 0);
        path[14] = new IVector(0.0f, -0.6f, 0);

        scene.addObject("Wineglass", IObjectFactory.ROTATIONOBJECT(path, 16));
        scene.object("Wineglass").rotate(0.5f, 0f, 0f);
        scene.object("Wineglass").setMaterial(scene.material("Crystal"));
    }

    public synchronized void paint(Graphics g) {
        // Draw accelerated image
        int x = 0;
        int y = 0;
        //  volImage = drawVolatileImage((Graphics2D)g, volImage, x, y, origImage);
        System.out.println("paint");
    }

    public void start() {
        if (idx_Thread == null) {
            idx_Thread = new Thread(this,"idxThread");
            idx_Thread.setPriority(Thread.MAX_PRIORITY);
            idx_Thread.start();
        }
    }

    public void stop() {
        if (idx_Thread != null) {
            idx_Thread.stop();
            idx_Thread = null;
        }
    }

    public void run() {
    	long t = 0;
        while (true) { 
        	t = System.currentTimeMillis();
            repaint();
            long dt = System.currentTimeMillis();
            dt = (dt - t);
            try {
            	long s = 15 - dt;            	
            	if(s>0) {
            		idx_Thread.sleep(s);
            	}
            } catch (Exception e) {
                System.out.println("idx://interrupted");
            }
        }
    }

    private int i = 0;
    boolean useVolatile = false;
    
    public synchronized void update(Graphics g) {
        if (autorotation) {
            scene.object("Torus").rotate(0f, 0.05f, 0.03f);
            scene.object("Wineglass").rotate(0f, -0.08f, -0.1f);
        }
        scene.render();
        
        if(useVolatile){
           volImage = drawVolatileImage((Graphics2D)g, null, 0, 0, scene.getImage()); 
        }else{
           g.drawImage(scene.getImage(), 0, 0, this); 
        }
        // 
        if ((i++ % 60) == 0) {
        	showStatus("FPS " + scene.getFPS());           
        }
    }

    public boolean imageUpdate(Image image, int a, int b, int c, int d, int e) {
        return true;
    }

    public boolean mouseDown(Event evt, int x, int y) {
        oldx = x;
        oldy = y;
        setMovingCursor();
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
        if ((char) key == '+') {
            scene.scale(1.2f);
            return true;
        }
        if ((char) key == '-') {
            scene.scale(0.8f);
            return true;
        }
        if ((char) key == 'a') {
            antialias = !antialias;
            scene.setAntialias(antialias);
            return true;
        }
        if ((char) key == 'm') {
            for (int i = 0; i < scene.objects; i++) {
                scene.object[i].meshSmooth();
            }
            return true;
        }

        if ((char) key == 'i') {
            idx3d.debug.Inspector.inspect(scene);
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


    /**
     * This method draws a volatile image and returns it or possibly a newly
     * created volatile image object. Subsequent calls to this method should
     * always use the returned volatile image. If the contents of the image is
     * lost, it is recreated using orig. img may be null, in which case a new
     * volatile image is created.
     */
    public VolatileImage drawVolatileImage(Graphics2D g2, VolatileImage img,
            int x, int y, Image orig) {
        final int MAX_TRIES = 5;
        for (int i = 0; i < MAX_TRIES; i++) {
            if (img != null) {
                // Draw the volatile image
                g2.drawImage(img, x, y, null);

                // Check if it is still valid
                if (!img.contentsLost()) {
                    return img;
                }
            } else {
                // Create the volatile image
                img = g2.getDeviceConfiguration().
                        createCompatibleVolatileImage(orig.getWidth(null), orig.getHeight(null));
            }

            // Determine how to fix the volatile image
            switch (img.validate(g2.getDeviceConfiguration())) {
                case VolatileImage.IMAGE_OK:
                    // This should not happen
                    break;
                case VolatileImage.IMAGE_INCOMPATIBLE:
                // Create a new volatile image object;
                    // this could happen if the component was moved to another device
                    img.flush();
                    img = g2.getDeviceConfiguration().createCompatibleVolatileImage(orig.getWidth(null), orig.getHeight(null));

                case VolatileImage.IMAGE_RESTORED:
                    // Copy the original image to accelerated image memory
                    Graphics2D gc = (Graphics2D) img.createGraphics();
                    gc.drawImage(orig, 0, 0, null);
                    gc.dispose();
                    break;
            }
        }

        // The image failed to be drawn after MAX_TRIES;
        // draw with the non-accelerated image
        g2.drawImage(orig, x, y, null);
        return img;
    }
    
    public static void main(String... args){
        Demo1 demo = new Demo1();
        demo.launch();
    }
    
}

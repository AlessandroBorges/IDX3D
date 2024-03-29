package idx3d.demos;
import java.applet.Applet;
import java.awt.Component;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;

//import com.sun.j3d.utils.applet.MainFrame;

import idx3d.I3ds_Importer;
import idx3d.IMaterial;
import idx3d.IMatrix;
import idx3d.IObjectFactory;
import idx3d.IScene;
import idx3d.IVector;

@SuppressWarnings("serial")
public final class Demo8 extends Applet implements Runnable {
    private Thread idx_Thread;
    IScene    scene;

    int            oldx         = 0;
    int            oldy         = 0;
    boolean        autorotation = true;
    boolean        antialias    = false;

    public void init() {
        setNormalCursor();

        // BUILD SCENE

        scene = new IScene(getWidth(), getHeight());

        scene.addMaterial("Chrome", new IMaterial(getDocumentBase(), "chrome.material"));

        try {
            new I3ds_Importer()
                .importFromStream(new java.net.URL(getDocumentBase(), "mech.3ds").openStream(),   scene);
            
            scene.rotateSelf(20, 15, 20);
            
            
            
            new I3ds_Importer()
            .importFromStream(new java.net.URL(getDocumentBase(), "mech.3ds").openStream(),   scene);
             scene.rebuild();
            
            for (int i = 0; i < scene.objects; i++)
                scene.object[i].setMaterial(scene.material("Chrome"));
           
            scene.normalize();
           
            scene.rotate(3.14159265f / 2, 3.14159265f / 2, 0f);
        } catch (Exception e) {
            System.out.println(e + "");
        }
    }

    public synchronized void paint(Graphics g) {
    }

    public void start() {
        if (idx_Thread == null) {
            idx_Thread = new Thread(this);
            idx_Thread.start();
        }
    }

    public void stop() {
        if (idx_Thread != null) {
            idx_Thread.interrupt();// .stop();
            idx_Thread = null;
        }
    }

    public void run() {
        int count = 0;
        while (true) {
            repaint();
            try {
                count++;
                idx_Thread.sleep(16);
                if (count > 128) {
                    System.out.println("FPS: " + scene.getFPS());
                    count = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public synchronized void update(Graphics g) {
        if (autorotation) scene.rotate(0f, 0.06f, 0f);
        scene.render();        
        g.drawImage(scene.getImage(), 0, 0, this);
    }

    public boolean imageUpdate(Image image, int a, int b, int c, int d, int e) {
        return true;
    }

    public boolean mouseDown(Event evt, int x, int y) {
        oldx = x;
        oldy = y;
        antialias = true;
        scene.setAntialias(antialias);
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
        antialias = false;
        scene.setAntialias(antialias);
        setNormalCursor();
        return true;
    }

    private void setMovingCursor() {
        if (getFrame() == null) return;
        getFrame().setCursor(Frame.MOVE_CURSOR);
    }

    private void setNormalCursor() {
        if (getFrame() == null) return;
        getFrame().setCursor(Frame.HAND_CURSOR);
    }

    private Frame getFrame() {
        Component comp = this;
        while ((comp = comp.getParent()) != null)
            if (comp instanceof Frame) return (Frame) comp;
        return null;
    }
    
    public static void main(String... args){
        Demo8 demo = new Demo8();        
        MainFrame mainFrame = new MainFrame(demo, 800,600);
        
    }

}

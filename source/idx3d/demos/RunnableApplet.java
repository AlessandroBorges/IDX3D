package idx3d.demos;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

//import com.sun.j3d.utils.applet.MainFrame;

import idx3d.IScene;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JApplet;
import javax.swing.JFrame;

/**
 * Class to auto launch IDX3D Demos
 * @author Alessndro
 *
 */
@SuppressWarnings("serial")
public abstract class RunnableApplet extends Applet implements Runnable {

    public  Thread idx_Thread;
    public IScene scene;
    public int oldx=0;
    public int oldy=0;
    public boolean antialias=false;
    public boolean autorotation=true;
    public boolean handCursor=false;
    Queue<Image> fifo = new LinkedList<Image>();
    
    protected MainFrame main = null;
    
    public RunnableApplet(){
        
    }

    public void launch(){
        launch(this);
    }
    
    public void launch(int width, int height){
        launch(this,width, height);
    }
    
    protected void launch(Applet app){      
		this.main = new MainFrame(app, 600, 600);
    } 
    
    protected void launch(Applet app, int w, int h){
        this.main = new MainFrame(app, w, h);
    } 

    @Override
    public URL getCodeBase() {
    	URL url = main.getCodeBase();
    	if(url==null) {
    		url = getClass().getResource(".");
    	}    	
    	return url;
    }
    
    @Override
    public URL getDocumentBase() {
    	return main.getDocumentBase();
    }
    
    @Override
    public boolean isActive(){
   	return true;
   	}
    
   
	public void showDocument(URL url) {
		Desktop d = Desktop.getDesktop();
		try {
			d.browse(url.toURI());
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (URISyntaxException e) {		
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * 
	 */
	public void showStatus( String status ) {
		main.showStatus(status);
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

    protected void setMovingCursor() {
        if (getFrame() == null) return;
        Cursor c = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        getFrame().setCursor(c);
    }

    protected void setNormalCursor() {
        if (getFrame() == null) return;
        getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    protected JFrame getFrame() {
        Component comp = this;
        while ((comp = comp.getParent()) != null)
            if (comp instanceof Frame) return (JFrame) comp;
        return null;
    }
   
}

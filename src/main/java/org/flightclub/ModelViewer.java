/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Font;
import org.flightclub.compat.Graphics;

import java.awt.BorderLayout;
import java.awt.Panel;

public class ModelViewer extends Panel implements ClockObserver {

    protected static final int FRAME_RATE = 25;

    public static final float TIME_PER_FRAME_DEFAULT = (float) (1.0 / FRAME_RATE) / 2;
    public static final float TIME_PER_FRAME_FAST = TIME_PER_FRAME_DEFAULT * 5;

    ModelCanvas modelCanvas = null;
    Obj3dManager obj3dManager = null;
    EventManager eventManager = null;
    CameraMan cameraMan = null;
    Landscape landscape = null;    //hack
    Sky sky = null;
    Interface envInterface;
    String textMessage = null;
    Compass compass = null;
    DataSlider slider = null;
    Clock clock = null;
    boolean pendingStart = false;
    //how much model time elapses during each tick, say 1/25 of a model time unit (a minute)
    protected float timePerFrame = TIME_PER_FRAME_DEFAULT;

    ModelViewer() {
    }

    void init(Interface envInterface) {
        this.envInterface = envInterface;
        createClock();
        createModelCanvas();
        obj3dManager = new Obj3dManager(this);
        eventManager = new EventManager();
        cameraMan = new CameraMan(this);
    }

    public void start() {
        if (clock != null) clock.start();
        else pendingStart = true;
    }

    public void stop() {
        if (clock != null) clock.stop();
    }

    protected void createClock() {
        clock = new Clock(1000 / FRAME_RATE);
        clock.addObserver(this);
        if (pendingStart) start();
    }

    @Override
    public void tick(Clock c) {
        eventManager.tick();
        modelCanvas.tick();
        cameraMan.tick();
        modelCanvas.repaint(); //TODO
    }

    protected void createModelCanvas() {
        setLayout(new BorderLayout());
        add("Center", modelCanvas = new ModelCanvas(this));

        /*
          If running as an applet we must set the size
          of this panel. Works fine without this call when
          running as a stand alone app. In the later case
          the following call makes the canvas too tall so
          that the score etc disappeear off the bottom off
          the screen.
        */
        try {
            AppletInterface a = (AppletInterface) envInterface;
            setSize(a.getSize().width, a.getSize().height);
        } catch (ClassCastException ignored) {
        }

        doLayout();
        modelCanvas.init();
    }

    int getFrameRate() {
        return FRAME_RATE;
    }

    public void draw(Graphics g, int width, int height) {
        //TODO optimize - build vector of objs in FOV, need only draw these
        cameraMan.setMatrix();

        obj3dManager.sortObjects();
        for (int layer = 0; layer < obj3dManager.MAX_LAYERS; layer++) {
            for (int i = 0; i < obj3dManager.os.get(layer).size(); i++) {
                Object3d o = obj3dManager.os.get(layer).elementAt(i);
                o.film(cameraMan);
                o.draw(g);
            }
        }

        //Text
        if (textMessage != null) {
            Font font = new Font("SansSerif", Font.PLAIN, 10);
            g.setFont(font);
            g.setColor(Color.LIGHT_GRAY);

            String s;
            if (!clock.paused) {
                s = textMessage;
            } else {
                s = textMessage + " [ paused ]";
            }
            g.drawString(s, 15, height - 15);
        }

        if (compass != null)
            compass.draw(g);

        if (slider != null)
            slider.draw(g);
    }
}

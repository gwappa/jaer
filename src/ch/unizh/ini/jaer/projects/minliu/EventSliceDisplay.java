/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.jaer.projects.minliu;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLException;
import java.nio.FloatBuffer;
import net.sf.jaer.graphics.ImageDisplay;

/**
 * The event slice is displayed with RGBA format in jaer. However, the ImageDisplay class
 * just support the RGB format. This subclass of ImageDisplay is used to address this problem.
 * @author minliu
 */
public class EventSliceDisplay extends ImageDisplay{
    private int GLTextID;
    private int[] GLTextBuffers;

    /**
     * Creates a new EventSliceDisplay, given some Open GL capabilities.
     *
     * @param caps the capabilities desired. See factory method.
     * @see #createOpenGLCanvas() for factory method with predefined
     * capabilities.
     */
    
    public EventSliceDisplay(GLCapabilitiesImmutable caps) {
        super(caps);
    }

    public static EventSliceDisplay createOpenGLCanvas() {
        // design capabilities of opengl canvas
        GLCapabilities caps = new GLCapabilities(null);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        caps.setAlphaBits(8);
        caps.setRedBits(8);
        caps.setGreenBits(8);
        caps.setBlueBits(8);
        EventSliceDisplay trackDisplay = new EventSliceDisplay(caps);
        return trackDisplay;
    }
    
        /**
     * Subclasses should call checkPixmapAllocation to make sure the pixmap
     * FloatBuffer is allocated before accessing it.
     *
     */
    @Override
    public void checkPixmapAllocation() {
        final int n = 4 * getSizeX() * getSizeY();
        if (n == 0) {
            log.warning("tried to set pixmap with 0 pixels in it; ignoring");
            return;
        }
        if ((pixmap == null) || (pixmap.capacity() != n)) {
            if (pixmap != null) {
                pixmap = null;
            }
            System.gc();
            if (n > 0) {
//                log.info("allocating " + n + " floats for pixmap");
                pixmap = FloatBuffer.allocate(n); // Buffers.newDirectFloatBuffer(n);
                pixmap.rewind();
                pixmap.limit(n);
            }
        }

    }  

    @Override
    public synchronized void display(GLAutoDrawable drawable) {
        GL2 gl = getGL().getGL2();
//        gl.getContext().makeCurrent();
        checkGLError(gl, "before display in ID");
        if (reshapePending) {
            reshapePending = false;
            reshape(drawable, 0, 0, getWidth(), getHeight());
        }
        try {
            gl.glClearColor(0.5f, 0.5f, 0.5f, 0f);            
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            displayPixmap(drawable);
            drawText(gl);
            gl.glFlush();
        } catch (GLException e) {
            log.warning(e.toString());
        }
        checkGLError(gl, "after setDefaultProjection in ID");
    }
    
    @Override    
    synchronized public void displayPixmap(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (gl == null) {
            return;
        }

        checkGLError(gl, "before pixmap");
        final int wi = drawable.getSurfaceWidth(), hi = drawable.getSurfaceHeight();
        float scale = 1;
        if (fillsVertically) {// tall chip, use chip height
            scale = (hi - (2 * borderPixels)) / getSizeY();
        } else if (fillsHorizontally) {
            scale = (wi - (2 * borderPixels)) / getSizeX();
        }

        gl.glPixelZoom(scale, scale);
        //        gl.glRasterPos2f(-.5f, -.5f); // to LL corner of chip, but must be inside viewport or else it is ignored, breaks on zoom     if (zoom.isZoomEnabled() == false) {
        gl.glRasterPos2f(0, 0); // to LL corner of chip, but must be inside viewport or else it is ignored, breaks on zoom     if (zoom.isZoomEnabled() == false) {

        checkPixmapAllocation();
        {
            try {
                pixmap.rewind();                
                if(GLTextBuffers == null) {
                    GLTextBuffers = new int[1];   
                    gl.glGenTextures(1, GLTextBuffers, 0);
                    GLTextID = GLTextBuffers[0];                    
                }
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_BLEND);
                
                final int nearestFilter = GL.GL_NEAREST;

                gl.glBindTexture(GL.GL_TEXTURE_2D, GLTextID);
                gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, nearestFilter);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, nearestFilter);
                gl.glTexEnvf(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
//                gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, getSizeX(), getSizeY(), 0, GL.GL_RGBA, GL.GL_FLOAT, pixmap);
                gl.glDrawPixels(getSizeX(), getSizeY(), GL.GL_RGBA, GL.GL_FLOAT, pixmap);

                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glBindTexture(GL.GL_TEXTURE_2D, GLTextID);
                drawPolygon(gl, getSizeX(), getSizeY());
                gl.glDisable(GL.GL_TEXTURE_2D);    
		gl.glDisable(GL.GL_BLEND);
                
                
            } catch (IndexOutOfBoundsException e) {
                log.warning(e.toString());
            }
        }
        //        FloatBuffer minMax=FloatBuffer.allocate(6);
        //        gl.glGetMinmax(GL.GL_MINMAX, true, GL.GL_RGB, GL.GL_FLOAT, minMax);
        //        gl.glDisable(GL.GL_MINMAX);
        checkGLError(gl, "after rendering image");

        // outline frame
        gl.glColor4f(0, 0, 1f, 0f);
        gl.glLineWidth(2f);
        {
            gl.glBegin(GL.GL_LINE_LOOP);
            final float o = 0;
            final float w = getSizeX();
            final float h = getSizeY();
            gl.glVertex2f(-o, -o);
            gl.glVertex2f(w + o, -o);
            gl.glVertex2f(w + o, h + o);
            gl.glVertex2f(-o, h + o);
            gl.glEnd();
        }
        checkGLError(gl, "after rendering frame");        
    }

    private void drawPolygon(GL2 gl, int width, int height) {
        final double xRatio = (double) getSizeX() / (double) width;
        final double yRatio = (double) getSizeY() / (double) height;
        gl.glBegin(GL2.GL_POLYGON);

        gl.glTexCoord2d(0, 0);
        gl.glVertex2d(0, 0);
        gl.glTexCoord2d(xRatio, 0);
        gl.glVertex2d(xRatio * width, 0);
        gl.glTexCoord2d(xRatio, yRatio);
        gl.glVertex2d(xRatio * width, yRatio * height);
        gl.glTexCoord2d(0, yRatio);
        gl.glVertex2d(0, yRatio * height);

        gl.glEnd();
    }
    
    /**
     * Returns an int that can be used to index to a particular pixel's RGB
     * start location in the pixmap. The successive 3 entries are the float
     * (0-1) RGB values.
     *
     * @param x pixel x, 0 is left side.
     * @param y pixel y, 0 is bottom.
     * @return index into pixmap.
     * @see #getPixmapArray()
     */
    @Override      
    public int getPixMapIndex(int x, int y) {
        return 4 * (x + (y * getSizeX()));
    }    
}

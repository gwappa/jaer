/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.jaer.projects.minliu;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.opencv.core.Core; 
import org.opencv.core.CvType; 
import org.opencv.core.Mat; 
import org.opencv.core.MatOfByte; 
import org.opencv.core.MatOfFloat; 
import org.opencv.core.MatOfPoint; 
import org.opencv.core.TermCriteria;
import org.opencv.core.MatOfPoint2f; 
import org.opencv.core.Point; 
import org.opencv.core.Size; 
import org.opencv.video.Video;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ch.unizh.ini.jaer.projects.davis.frames.ApsFrameExtractor;
import ch.unizh.ini.jaer.projects.rbodo.opticalflow.AbstractMotionFlow;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.chip.Chip2D;

import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import static net.sf.jaer.eventprocessing.EventFilter.log;
import net.sf.jaer.eventprocessing.FilterChain;
import net.sf.jaer.graphics.AEFrameChipRenderer;
import org.apache.commons.lang3.ArrayUtils;


/**
 *
 * @author minliu
 */
@Description("Optical Flow methods based on OpenCV")
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
public class OpenCVFlow extends AbstractMotionFlow 
                        implements PropertyChangeListener, Observer /* Observer needed to get change events on chip construction */ {

    static { 
    String jvmVersion = System.getProperty("sun.arch.data.model");
        
    try {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    // System.loadLibrary("opencv_ffmpeg320_" + jvmVersion);   // Notice, cannot put the file type extension (.dll) here, it will add it automatically. 
    } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library failed to load.\n" + e);
        // System.exit(1);
        }
    }
    
    private final EventSliceDisplay tminusdSliceResult,tminus2dSliceResult;    
    private JFrame tminusdSlice = null, tminus2dSlice = null;
    protected boolean showEventSlicesDisplay = getBoolean("showEventSlicesDisplay", true);
    private int[][] color = new int[100][3];
    private float[] oldBuffer = null, newBuffer = null;
    private PatchMatchFlow patchFlow;
    private boolean isSavedAsImage = getBoolean("isSavedAsImage", false);
    private int colorScale = getInt("colorScale", 2);
    
    
    public OpenCVFlow(AEChip chip) {
        super(chip);
        System.out.println("Welcome to OpenCV " + Core.VERSION);

        tminusdSliceResult = EventSliceDisplay.createOpenGLCanvas();   
        tminus2dSliceResult = EventSliceDisplay.createOpenGLCanvas();   
        
        tminusdSlice = new JFrame("The t-d event slice frame");
        tminusdSlice.setPreferredSize(new Dimension(800, 800));
        tminusdSlice.getContentPane().add(tminusdSliceResult, BorderLayout.CENTER);
        
        tminusdSlice.pack();
        tminusdSlice.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                setShowEventSlicesDisplay(false);
            }
        });         
        tminusdSlice.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                imagePanelMouseWheelMoved(evt);
            }
        });

        
        tminus2dSlice = new JFrame("The t-2d event slice frame");
        tminus2dSlice.setPreferredSize(new Dimension(800, 800));
        tminus2dSlice.getContentPane().add(tminus2dSliceResult, BorderLayout.CENTER);
        tminus2dSlice.pack();
        tminus2dSlice.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                setShowEventSlicesDisplay(false);
            }
        });         
        tminus2dSlice.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                imagePanelMouseWheelMoved(evt);
            }
        });
        
        FilterChain chain = new FilterChain(chip);        
        try {
            patchFlow = new PatchMatchFlow(chip);
            patchFlow.setFilterEnabled(true);
            chain.add(patchFlow);
        } catch (Exception e) {
            log.warning("could not setup PatchMatchFlow fiter.");
        }        
        setEnclosedFilterChain(chain);
        
        // apsFrameExtractor.getSupport().addPropertyChangeListener(ApsFrameExtractor.EVENT_NEW_FRAME, this);   
        patchFlow.getSupport().addPropertyChangeListener(PatchMatchFlow.EVENT_NEW_SLICES,this);
        chip.addObserver(this); // to allocate memory once chip size is known
    }

    @Override
    public EventPacket filterPacket(EventPacket in) {   
        if(!isFilterEnabled()) {
                return in;
        }
        setupFilter(in);
        
        if (showEventSlicesDisplay && !tminusdSlice.isVisible()) {
            tminusdSlice.setVisible(true);
        }
        if (showEventSlicesDisplay && !tminus2dSlice.isVisible()) {
            tminus2dSlice.setVisible(true);            
        }        

        tminusdSliceResult.checkPixmapAllocation();     
        tminus2dSliceResult.checkPixmapAllocation();     

        if(isShowEventSlicesDisplay()) {
            tminusdSliceResult.repaint(); 
            tminus2dSliceResult.repaint();                      
        }
        return in;     
    }

    @Override
    public synchronized void resetFilter() {
        super.resetFilter();

        if(patchFlow != null) {
            patchFlow.resetFilter();            
        }
        
        tminusdSliceResult.setImageSize(chip.getSizeX(), chip.getSizeY());
        tminus2dSliceResult.setImageSize(chip.getSizeX(), chip.getSizeY());            
    }

    @Override
    public void initFilter() {
        resetFilter();
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((o != null) && (arg != null)) {
            if ((o instanceof AEChip) && (arg.equals(Chip2D.EVENT_SIZEX) || arg.equals(Chip2D.EVENT_SIZEY))) {
                initFilter();
            }
        }    
    }
    
    @Override
    public synchronized void setFilterEnabled(final boolean yes) {
        super.setFilterEnabled(yes); // To change body of generated methods, choose Tools | Templates.
        if (!isFilterEnabled()) {
            if (tminusdSlice != null) {
                tminusdSlice.setVisible(false);
            }
            if (tminus2dSlice != null) {
                tminus2dSlice.setVisible(false);
            }            
        }
    } 
    
    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {       
        if (evt.getPropertyName().equals(PatchMatchFlow.EVENT_NEW_SLICES)) {
            byte[][][] tMinus2dSlice = (byte[][][]) evt.getOldValue();
            byte[][][] tMinusdSlice = (byte[][][]) evt.getNewValue();
            Mat newFrame = new Mat(chip.getSizeY(), chip.getSizeX(), CvType.CV_8U);
            Mat oldFrame = new Mat(chip.getSizeY(), chip.getSizeX(), CvType.CV_8U);
            
//            /* An example to flatten the nested array to 1D array */
//            double[][][] vals = {{{1.1, 2.1}, {3.2, 4.1}}, {{5.2, 6.1}, {7.1, 8.3}}};
//
//            double[] test = Arrays.stream(vals)
//                    .flatMap(Arrays::stream)
//                    .flatMapToDouble(Arrays::stream)
//                    .toArray();
//
//            System.out.println(Arrays.toString(test));
     

            // Flatten the two arrays to 1D array
            byte[] old1DArray = new byte[chip.getSizeY() * chip.getSizeX()], 
                    new1DArray = new byte[chip.getSizeY() * chip.getSizeX()];
            for (int i = 0; i < chip.getSizeY(); i++) {
                for (int j = 0; j < chip.getSizeX(); j++) {
                    old1DArray[chip.getSizeX()*i + j] = (byte)(tMinus2dSlice[0][j][i] * 20);  // Multiple the intensity so the feature can be extracted
                    new1DArray[chip.getSizeX()*i + j] = (byte)(tMinusdSlice[0][j][i] * 20);         
                }
            }

            List oldList = Arrays.asList(ArrayUtils.toObject(old1DArray));
            float oldGrayScale = Collections.max((List<Byte>) oldList);     // Set the maximum of tha array as the scale value.
            List newList = Arrays.asList(ArrayUtils.toObject(new1DArray));
            float newGrayScale = Collections.max((List<Byte>) newList);     // Set the maximum of tha array as the scale value.        
           
            newFrame.put(0, 0, new1DArray);
            oldFrame.put(0, 0, old1DArray);
            
            // params for ShiTomasi corner detection            
            FeatureParams feature_params  = new FeatureParams(100, 0.3, 7, 7);
            
            // Feature extraction
            MatOfPoint p0 = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(oldFrame, p0, feature_params.maxCorners, feature_params.qualityLevel, feature_params.minDistance);       

            MatOfPoint2f prevPts = new MatOfPoint2f(p0.toArray());
            MatOfPoint2f nextPts = new MatOfPoint2f();
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();

            int featureNum = prevPts.checkVector(2, CvType.CV_32F, true);
            System.out.println("The number of feature detected is : " + featureNum);     

            try {
                Video.calcOpticalFlowPyrLK(oldFrame, newFrame, prevPts, nextPts, status, err);            
            } catch (Exception e) {
                System.err.println(e);                   
                return;
            } finally {
                // showResult(newFrame);

                
                float[] new_slice_buff = new float[(int) (newFrame.total() * 
                                                newFrame.channels()) * 4];

                Arrays.fill(new_slice_buff, 0);
                Random r = new Random();
                for (int i = 0; i < chip.getSizeY(); i++) {
                    for (int j = 0; j < chip.getSizeX(); j++) {
                        if(new1DArray[chip.getSizeX()*i + j] > 0) {                            
                            new_slice_buff[(chip.getSizeX()*i + j) * 4] = new1DArray[chip.getSizeX()*i + j]/newGrayScale;       
                            new_slice_buff[(chip.getSizeX()*i + j) * 4 + 1] = new1DArray[chip.getSizeX()*i + j]/newGrayScale;         
                            new_slice_buff[(chip.getSizeX()*i + j) * 4 + 2] = new1DArray[chip.getSizeX()*i + j]/newGrayScale;   
                            new_slice_buff[(chip.getSizeX()*i + j) * 4 + 3] = 1.0f/colorScale;  
                        }            

                    }
                }      
                
                
                float[] old_slice_buff = new float[(int) (oldFrame.total() * 
                                                oldFrame.channels()) * 4];

                Arrays.fill(old_slice_buff, 0);
                for (int i = 0; i < chip.getSizeY(); i++) {
                    for (int j = 0; j < chip.getSizeX(); j++) {
                        if(old1DArray[chip.getSizeX()*i + j] > 0) {                            
                            old_slice_buff[(chip.getSizeX()*i + j) * 4] = old1DArray[chip.getSizeX()*i + j]/oldGrayScale;       
                            old_slice_buff[(chip.getSizeX()*i + j) * 4 + 1] = old1DArray[chip.getSizeX()*i + j]/oldGrayScale;         
                            old_slice_buff[(chip.getSizeX()*i + j) * 4 + 2] = old1DArray[chip.getSizeX()*i + j]/oldGrayScale;   
                            old_slice_buff[(chip.getSizeX()*i + j) * 4 + 3] = 1.0f/colorScale;  
                        }            

                    }
                }  
                
                AEFrameChipRenderer render = (AEFrameChipRenderer)(chip.getRenderer());
                tminusdSliceResult.setPixmapArray(new_slice_buff);  
                tminus2dSliceResult.setPixmapArray(old_slice_buff);  
                
                if(isSavedAsImage) {
                    saveImage();
                }
            }            
            
            // draw the tracks
            Point[] prevPoints = prevPts.toArray();
            Point[] nextPoints = nextPts.toArray();
            byte[] st = status.toArray();
            float[] er = err.toArray();

            // Select good points  and copy them for output
            int index = 0;
            for(byte stTmp: st) {
                if(stTmp == 1) {
                    e = new PolarityEvent();
                    x = (short)(prevPoints[index].x);
                    y = (short)prevPoints[index].y;
                    e.x = (short)x;
                    e.y = (short)y;    // e, x and y all of them are used in processGoodEvent();
                    vx = (float)(nextPoints[index].x - prevPoints[index].x) * 1000000 / -patchFlow.getSliceDeltaT();
                    vy = (float)(nextPoints[index].y - prevPoints[index].y) * 1000000 / -patchFlow.getSliceDeltaT();
                    v = (float) Math.sqrt(vx * vx + vy * vy);
                    processGoodEvent();
                    index++;
                }
            }
           
            Mat mask = new Mat(newFrame.rows(), newFrame.cols(), CvType.CV_32F);
            for (int i = 0; i < prevPoints.length; i++) {
                // Imgproc.line(displayFrame, prevPoints[i], nextPoints[i], new Scalar(color[i][0],color[i][1],color[i][2]), 2);  
                // Imgproc.circle(newFrame,prevPoints[i], 5, new Scalar(255,255,255),-1);
            }  
           
        }
        
    }

    private void saveImage() {
        final Date d = new Date();
        final String fn = "EventSlices/" + "EventSlice-" + System.nanoTime() + ".png";
        final BufferedImage theImage = new BufferedImage(chip.getSizeX(), chip.getSizeY(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < chip.getSizeY(); y++) {
            for (int x = 0; x < chip.getSizeX(); x++) {
                final int idx = tminusdSliceResult.getPixMapIndex(x, chip.getSizeY() - y - 1);
                final int value = ((int) (256 * tminusdSliceResult.getPixmapArray()[idx]) << 16)
                        | ((int) (256 * tminusdSliceResult.getPixmapArray()[idx + 1]) << 8) | (int) (256 * tminusdSliceResult.getPixmapArray()[idx + 2]);
                theImage.setRGB(x, y, value);
            }
        }
        final File outputfile = new File(fn);
        try {
            ImageIO.write(theImage, "png", outputfile);
        } catch (final IOException ex) {
            Logger.getLogger(ApsFrameExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class FeatureParams {
        
        int maxCorners;
        double qualityLevel;
        double minDistance;
        int blockSize;

        public FeatureParams(int maxCorners, double qualityLevel, int minDistance, int blockSize) {
            this.maxCorners = maxCorners;
            this.qualityLevel = qualityLevel;
            this.minDistance = minDistance;
            this.blockSize = blockSize;
        }
    }  

    public class LKParams {
        int winSizeX;
        int winSizeY;
        int maxLevel;
        TermCriteria criteria = new TermCriteria();

        public LKParams(int winSizeX, int winSizeY, int maxLevel, TermCriteria criteria) {
            this.winSizeX = winSizeX;
            this.winSizeY = winSizeY;
            this.maxLevel = maxLevel;
            this.criteria = criteria;
        }
    }

    public void showResult(Mat img) {
        Imgproc.resize(img, img, new Size(640, 480));
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        } catch (IOException | HeadlessException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage Mat2BufferedImage(Mat m){
        // source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
        // Fastest code
        // The output can be assigned either to a BufferedImage or to an Image

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;

    }

    public void displayImage(Image img2) {   
        //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
        ImageIcon icon=new ImageIcon(img2);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());        
        frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }    
    
    /**
     * @return the showEventSlicesDisplay
     */
    public boolean isShowEventSlicesDisplay() {
        return showEventSlicesDisplay;
    }

    /**
     * @param showEventSlicesDisplay the showEventSlicesDisplay to set
     */
    public void setShowEventSlicesDisplay(final boolean showEventSlicesDisplay) {
        this.showEventSlicesDisplay = showEventSlicesDisplay;
        putBoolean("showEventSlicesDisplay", showEventSlicesDisplay);
        if (tminusdSlice != null) {
            tminusdSlice.setVisible(showEventSlicesDisplay);
        }
        if (tminus2dSlice != null) {
            tminus2dSlice.setVisible(showEventSlicesDisplay);
        }        
        getSupport().firePropertyChange("showEventSlicesDisplay", null, showEventSlicesDisplay);
    }  

    public boolean isIsSavedAsImage() {
        return isSavedAsImage;
    }

    public void setIsSavedAsImage(boolean isSavedAsImage) {
        this.isSavedAsImage = isSavedAsImage;
        getSupport().firePropertyChange("isSavedAsImage", null, isSavedAsImage);        
    }

    public int getColorScale() {
        return colorScale;
    }

    public void setColorScale(int colorScale) {
        if(colorScale < 0) {
            colorScale = 0;
        }
        this.colorScale = colorScale;
        getSupport().firePropertyChange("colorScale", null, colorScale);            
    }
    
    private void imagePanelMouseWheelMoved(MouseWheelEvent evt) {
        int rotation = evt.getWheelRotation();
        setColorScale(colorScale + rotation);
    }       
}



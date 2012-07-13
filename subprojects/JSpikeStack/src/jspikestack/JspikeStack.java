/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jspikestack;
//import org.ejml.simple.SimpleMatrix;
//import org.ejml.data.DenseMatrix64F;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.plot.CombinedXYPlot;

/**
 * Here's a collection of Demos that make use of the Spiking Network 
 * package.  To run, just uncomment the appropriate line in the main function.
 * 
 * HAVE FUN.
 * 
 * @author oconnorp
 */
public class JspikeStack {

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
//        simpleDemo();
        
//         learningDemo();

//        readNet();
        
//        liquidDemo();
        
        demoMenu();
        
    }
    
    /** Read in a network from XML, do stuff with it */
    public static void readNet()
    {   
//        SpikeStack net=new SpikeStack();
        //
        STPStack<STPStack,STPStack.Layer> net = new STPStack();
        net.read.readFromXML(net);    
       
        net.tau=100f;
        net.delay=10f;
        net.tref=5;
        
        net.plot.timeScale=1f;
        
        // Set up connections
        float[] sigf={0, 1, 0, 1};
        net.setForwardStrength(sigf);
        float[] sigb={1, 1, 0, 1};
        net.setBackwardStrength(sigb);
                
        // Create input events
        int nEvents=500;
        LinkedList evts=new LinkedList();
        
        // Up the threshold
        for (int i=0; i<net.layers.size(); i++)
            for (SpikeStack.Layer.Unit u:net.lay(i).units)
                u.thresh*=400;
        
        net.fastWeightTC=2;
        
        net.lay(1).enableFastSTDP=true;
        net.lay(3).enableFastSTDP=true;
        
        
        net.fastSTDP.plusStrength=-.001f;
        net.fastSTDP.minusStrength=-.001f;   
        net.fastSTDP.stdpTCminus=10;
        net.fastSTDP.stdpTCplus=10;
        
        for (int i=0; i<nEvents; i++)
        {   int number=i<nEvents/2?8:2;
            net.addToQueue(new Spike(number,(double)5000*(i/(float)nEvents),3));
        }
        
        net.plot.timeScale=1f;
        
//        STPStack<STPStack,STPStack.Layer> net2=net.read.copy();
        
        net.plot.followState();
        
        net.eatEvents(10000);
        
//        STPStack net2=net.read.copy();
        
        
    }
    
    /** Read an AEFile and do STDP learning beginning from a random net */
    public static void learningDemo()
    {
        
        // Read Events
        AERFile aef=new AERFile();
        aef.read();
        int nLayers=2;
        
        
        // Plot Events
        
        // Initialize Network
        STDPStack.Initializer ini=new STDPStack.Initializer(nLayers);
        
        ini.tau             = 20f;
        ini.thresh          = 1;
        ini.tref            = 5;        
        ini.stdp.plusStrength    = .001f;
        ini.stdp.minusStrength    = -.001f;
        ini.stdp.stdpTCplus      = 10f;
        ini.stdp.stdpTCminus     = 10f;
        
        ini.stdpWin         = 30;
        
        ini.lay(0).nUnits   = 64;
        ini.lay(0).targ     = 1;
        ini.lay(0).name     = "Input";
        ini.lay(0).WoutMean = .5f;
        ini.lay(0).WoutStd  = 0.3f;
        ini.lay(0).enableSTDP=true;
        
        ini.lay(0).WlatMean = -0f;
        ini.lay(0).WlatStd  = 0f;
        
        ini.lay(1).nUnits   = 20;
        ini.lay(1).name     = "A1";
        ini.lay(1).WlatMean = -2f;
        ini.lay(1).WlatStd  = .0f;
        
        
        STDPStack<STDPStack,STDPStack.STDPLayer> es=new STDPStack(ini);
                
        es.lay(1).latSend=1;
        es.lay(0).latSend=0;
        
        es.inputCurrents=false;
        es.inputCurrentStrength=0.5f;
        
        
        int nEpochs     = 10;
                        
        ArrayList<Spike> events=aevents2events(aef.events);
        
        es.lay(0).enableSTDP=false;
        es.feedEventsAndGo(events);
        es.plot.raster("Random Initializations");
        
        es.plot.timeScale=0;
        
        es.lay(0).enableSTDP=true;
        // Feed Events to Network
        for (int i=0; i<nEpochs; i++)
        {   System.out.println("Pass "+i);
            es.reset();
            es.feedEventsAndGo(events);
        }
        
        
        es.plot.raster("After "+nEpochs+" training cycles");
        
    }
    
    /** Experiment with liquid-state machines */
    public static void liquidDemo()
    {
        
        // Read Events
        AERFile aef=new AERFile();
        aef.read();
        int nLayers=2;
        
        
        // Plot Events
        
        // Initialize Network
        STDPStack.Initializer ini=new STDPStack.Initializer(nLayers);
        
        ini.tau             = 500f;
        ini.thresh          = 1;
        ini.tref            = 0.005f;        
        ini.stdp.plusStrength    = .0001f;
        ini.stdp.stdpTCminus     = 10f;
        ini.stdp.stdpTCplus      = 10;
        ini.stdpWin         = 30;
        
        ini.lay(0).nUnits   = 64;
        ini.lay(0).targ     = 1;
        ini.lay(0).name     = "Input";
        ini.lay(0).WoutMean = .06f;
        ini.lay(0).WoutStd  = 0.06f;
        ini.lay(0).enableSTDP=true;
        
        ini.lay(0).WlatMean = -0.5f;
        ini.lay(0).WlatStd  = 1f;
        
        ini.lay(1).nUnits   = 20;
        ini.lay(1).name     = "A1";
        ini.lay(1).WlatMean = -.5f;
        ini.lay(1).WlatStd  = 0f;
        
        
        STDPStack<STDPStack,STDPStack.STDPLayer> es=new STDPStack(ini);
        
        
        es.lay(1).latSend=1;
        es.lay(0).latSend=1;
        
        es.inputCurrents=true;
        es.inputCurrentStrength=0.8f;
        
        
        int nEpochs     = 5;
        
        
        ArrayList<Spike> events=aevents2events(aef.events);
        
        
        es.lay(0).enableSTDP=false;
        es.feedEvents(events);
        es.plot.raster();
        
        es.plot.timeScale=0;
        
        es.lay(0).enableSTDP=true;
        // Feed Events to Network
        for (int i=0; i<nEpochs; i++)
        {   System.out.println("Pass "+i);
            es.reset();
            es.feedEvents(events);
        }
        
        
        es.eatEvents();
        es.plot.raster();
        
        
        
    }
    
    /** Simple Demo, no Learning */
    public static void simpleDemo()
    {
        
        // Read Events
        AERFile aef=new AERFile();
        aef.read();
        ArrayList<Spike> events=aevents2events(aef.events);
        
        // Plot Events
        
        // Initialize Network
        int nLayers=2;
        SpikeStack.Initializer ini=new SpikeStack.Initializer(nLayers);
        ini.tau=20;
        ini.thresh=1;
        ini.lay(0).nUnits=64;
        ini.lay(0).targ=1;
        ini.lay(0).name="Input";
        ini.lay(0).WoutMean=.01f;
        ini.lay(0).WoutStd=0.005f;
        
        ini.lay(1).nUnits=10;
        ini.lay(1).name="A1";
        ini.lay(1).WlatMean=-1;
        ini.lay(1).WlatStd=-1;
        
        SpikeStack es=new SpikeStack(ini);
        
        // Feed Events to Network
        es.feedEventsAndGo(events);
        
        es.plot.raster();
        
    }
    
    /** Best working settings for voice separation */
    public static void voiceSepDemo()
    {
        // Read Events
        AERFile aef=new AERFile();
        aef.read();
        int nLayers=2;
        
        
        // Plot Events
        
        // Initialize Network
        STDPStack.Initializer ini=new STDPStack.Initializer(nLayers);
        
        ini.tau             = 5f;
        ini.thresh          = 1;
        ini.tref            = 0;        
        ini.stdp.plusStrength    = .0001f;
        ini.stdp.stdpTCminus     = 10f;
        ini.stdp.stdpTCplus      = 10;
        ini.stdpWin         = 30;
        
        ini.lay(0).nUnits   = 64;
        ini.lay(0).targ     = 1;
        ini.lay(0).name     = "Input";
        ini.lay(0).WoutMean = .05f;
        ini.lay(0).WoutStd  = 0.0005f;
        ini.lay(0).enableSTDP=true;
        
        ini.lay(1).nUnits   = 10;
        ini.lay(1).name     = "A1";
        ini.lay(1).WlatMean = -1;
        ini.lay(1).WlatStd  = 0f;
        
        
        STDPStack es=new STDPStack(ini);
        
        
        es.lay(1).latSend=1;
        
        
        // Feed Events to Network
        for (int i=0; i<10; i++)
        {   es.reset();
            ArrayList<Spike> events=aevents2events(aef.events);
            es.feedEvents(events);
        }
        es.plot.raster();
        
    }
    
    /** Convert AEViewer file events to JspikeStack Events */
    public static ArrayList<Spike> aevents2events(ArrayList<AERFile.Event> events)
    {   
        ArrayList<Spike> evts =new ArrayList<Spike>();
        
        for (int i=0; i<events.size(); i++)
        {   AERFile.Event ev=events.get(i);
        
            if (ev.addr>255) continue;
        
            Spike enew=new Spike(ev.addr/4, (double)(ev.timestamp-events.get(0).timestamp)/1000., 0);
            
            evts.add(enew);
        }
        return evts;
    }
    
    /** Plot a list of events read from a file */
    public static void plotEvents(Queue<Spike> evts)
    {   
        Iterator<Spike> itr=evts.iterator();
        XYSeries data=new XYSeries("Size");
        for (int i=0; i<evts.size(); i++)
        {   Spike ev=itr.next();
            data.add((float)ev.time,ev.addr);
        }
        XYDataset raster = new XYSeriesCollection(data);
        
        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setDotWidth(2);
        renderer.setDotHeight(2);

        XYPlot subplot1 = new XYPlot(raster, null, new NumberAxis("address"), renderer);
        XYPlot subplot2 = new XYPlot(raster, null, new NumberAxis("address"), renderer);

        CombinedXYPlot plot = new CombinedXYPlot(new NumberAxis("Time"),new NumberAxis("Address"));
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);
        
        JFreeChart chart= new JFreeChart("Raster",JFreeChart.DEFAULT_TITLE_FONT, plot,true);

        JFrame fr=new JFrame();
        fr.getContentPane().add(new ChartPanel(chart));
        fr.setSize(1200,1000);
        fr.setVisible(true);

    }
    
    /** Enumeration of the list of available demos */
    public static enum Demos {GENERATE,LEARN};
    
    /** Start a menu that allows the user to launch a number of demos for the 
     * JSpikeStack package.  To add a new demo to the menu:
     * 1) Add the appropriate element to the "Demos" enumerator;
     * 2) Add the button in demoMenu
     * 3) Connect the enumerator element to the appropriate function in DemoLauncher
     */
    public static void demoMenu()
    {
                
        JFrame frm=new JFrame();
        frm.setTitle("JSpikeStack demos");
        Container pane=frm.getContentPane();
        JButton button;
        
        pane.setLayout(new GridBagLayout());
        
        addDemoButton("Network Generation Demo","Read a network From XML and let it generate",Demos.GENERATE,pane);
        addDemoButton("Learning Demo","Read an AER file, initialize a random net, and run STDP learning",Demos.LEARN,pane);
        
        
        frm.setPreferredSize(new Dimension(500,300));
        frm.pack();
        frm.setVisible(true);
        
    }
    
    static class DemoLauncher extends Thread{
            Demos demoNumber;
            public DemoLauncher(Demos demoNum)
            {   demoNumber=demoNum;
            }
            
            @Override
            public void run()
            {   switch (demoNumber)
                {   case GENERATE:
                        readNet();       
                        break;
                    case LEARN:
                        learningDemo();
                        break;
                }
            }
        }        
    
    /** Add a demo button */
    public static void addDemoButton(String demoName,String description,final Demos demoNumber,Container pane)
    {        
        JButton button=new JButton(demoName);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DemoLauncher(demoNumber).start();
            }
        });
        
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill=GridBagConstraints.BOTH;
        c.gridx=0;
        c.gridy=demoNumber.ordinal();
        c.weightx=1;
        c.weighty=1;
        pane.add(button,c);
        
        JTextArea jt=new JTextArea(description);
        jt.setLineWrap(true);
        jt.setWrapStyleWord(true);
        
        c.gridx=1;
        //c.anchor=GridBagConstraints.CENTER;
        c.fill=GridBagConstraints.CENTER;
        
        pane.add(jt,c);
        
    }
    
    
    
}
   
/*
 * Copyright (C) 2017-2018 Viktor Bahr, Keisuke Sehara
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package de.cco.jaer.eval;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.InetAddress;

/**
 * a proxy class to FastEventServer.
 *
 * <h3>The interface</h3>
 * <p>
 * There are only three interface methods that can be used from the other classes:
 * <ul>
 * <li>setSync(boolean) -- used to flag whether or not logging is on.</li>
 * <li>setEvent(boolean) -- used to flag whether or not event is on (as evaluated/called by EvaluationTarget).</li>
 * <li>shutdown() -- called when jAER is terminating.</li>
 * </ul>
 * In most cases, these methods are called by FastEventManager (setEvent() will be by EvaluationTarget).
 * You will not call them directly.
 * </p>
 *
 * <h3>Internal structure</h3>
 * <p>
 * The EventTriggerManager class is comprised of the producer-consumer pattern,
 * where the Manager updates the 'internal' state (whether or not to trigger event/sync),
 * and its nested IOLoop thread works to send the given internal state to the FastEventServer as fast as possible.
 * </p>
 * <p>
 * Because of the algorithm used, the IOLoop thread can in theory drop some state updates
 * as the cost of the fast responses (although we see these drops quite rarely).
 * </p>
 *
 * <h3>The "internal state"</h3>
 * <p>
 * The EventTriggerManager holds the current "supposed-to-be" output state all the time,
 * in the form of EventTriggerManager.State instance.
 * </p>
 * <p>
 * Reflecting the three main interface methods, this class holds the state of:
 * <ul>
 * <li>Whether or not the logging is active</li>
 * <li>Whether or not the event is on</li>
 * <li>Whether or not the process must be shut down</li>
 * </ul>
 * </p>
 * <p>
 * Note that there are two copies of this State instance: a 'request' instance that EventTriggerManager holds,
 * and the other 'server state' instance that the IOLoop thread holds.
 * The former reflects the up-to-date state of evaluation, whereas the latter reflects the current state on the
 * server side.
 * The IOLoop thread may refer to the 'request' instance as frequent as it can, but it does not send any output to
 * FastEventServer unless it differs from the 'server state' instance.
 * </p>
 *
 * <h3>The life cycle of the IOLoop thread</h3>
 * <p>
 * The IOLoop thread starts as soon as EventTriggerManager is loaded, through the `startIOThread()` static method.
 * You may need to be careful when the main thread fails accidentally, as the IOLoop thread is likely to be still running.
 * The shutdown of the IOLoop thread occurs when any class (most likely to be FastEventManager) calls `EventTriggerManager.shutdown()`.
 * This results in passing the shutdown flag to the IOLoop thread, and lets it terminate.
 * </p>
 *
 * <h3>Profiling vs performance</h3>
 * <p>
 * By switching the `LOGGING_ENABLED` static constant to `true`, you can let the IOLoop to output all the timings
 * of its transactions in the form of CSV.
 * On the other hand, this logging feature often results in a significant reduction in performance (as it accompanies
 * output to a file).
 * Therefore, unless you really want to examine the distribution of latency, we strongly recommend to leave the 
 * `LOGGING_ENABLED` flag being `false`.
 * </p>
 * <p>
 * Instead, we also have the `PROFILE_LATENCY` flag, which is `true` by default.
 * This enables one to review the min/max/average latency during the whole jAER session, when jAER is about to shutdown,
 * through the standard output (you may need to run jAER from NetBeans or from the terminal emulator).
 * Although this should not be a lot of duty by itself, you can turn it to `false` if you think this may be 
 * causing the latency problem.
 * </p>
 *
 * @author gwappa
 * @see FastEventManager
 * @see EvaluationTarget
 */
public class EventTriggerManager
{
    private static final boolean PROFILE_LATENCY    = true;
    private static final boolean LOGGING_ENABLED    = false;

    /*
     * command bytes
     */
    private static final byte SYNC_ON   = (byte)'1';
    private static final byte SYNC_OFF  = (byte)'2';
    private static final byte EVENT_ON  = (byte)'A';
    private static final byte EVENT_OFF = (byte)'D';
    private static final byte SHUTDOWN  = (byte)'X';

    static {
        startIOThread();
    }

    /**
     * expected to be performed during startup.
     */
    private static void startIOThread() {
        ioThread_    = new Thread(new EventTriggerManager.IOLoop());
        ioThread_.setPriority(Thread.MAX_PRIORITY);
        ioThread_.start();
    }

    /**
     * the class that represents the internal state of the triggers.
     * instances are used in both EventTriggerManager and EventTriggerManager.Thread classes.
     */
    private static class State {
        public boolean  event;
        public boolean  sync;
        public boolean  shutdown;
        public long     index;

        public State(long index, boolean event, boolean sync, boolean shutdown){
            this.event      = event;
            this.sync       = sync;
            this.shutdown   = shutdown;
            this.index      = index;
        }

        public State copy(){
            return new State(this.index, this.event, this.sync, this.shutdown);
        }

        public boolean equiv(State other) {
            if (this.event != other.event) {
                return false;
            } else if (this.sync != other.sync) {
                return false;
            } else {
                return true;
            }
        }
        
        @Override
        public String toString() {
            return String.format("State(index=%d,event=%b,sync=%b,shutdown=%b)",index,event,sync,shutdown);
        }
    }

    /**
     * the Runnable class that handles I/O to the server.
     */
    private static class IOLoop 
        implements Runnable 
    {
        EventTriggerManager.State    stateMonitor_   = new EventTriggerManager.State(0, false, false, false);
        DatagramSocket          connection_;
        DatagramPacket          packet_;

        /** the byte buffer to be set to `packet_` */
        byte[]                  buffer_     = new byte[1];

        /*
         * placeholders for logging latency
         * to hold microsecond values
         *
         * used only when PROFILE_LATENCY is true.
         */
        private double  sum_latency = 0.0;
        private int     nrequests   = 0;
        private long    min_latency = Long.MAX_VALUE;
        private long    max_latency = 0;
        private long    start, end;

        /*
         * file output for logging output
         */
        private FastEventWriter logger;

        /**
         * the infinite loop that handles I/O to the server.
         * it keeps running until `shutdown` flag is set for EventTriggerManager.stateUpdate_ state.
         */
        @Override
        public void run() {
            setup();

            EventTriggerManager.State newState;

            // loop
            while (true) {
                // obtain the up-to-date state
                synchronized (io_) {
                    // if it is exactly the same as the thread's own internal state,
                    while (!stateUpdate_.shutdown) {
                        // System.out.println(String.format("monitor=%s; update=%s", stateMonitor_.toString(), stateUpdate_.toString()));
                        if (!stateMonitor_.equiv(stateUpdate_)){
                            break;
                        }
                        
                        // wait until some change is made.
                        try {
                            io_.wait();
                        } catch (InterruptedException e) {
                            System.err.println("***EventTriggerManagerThread detected an interrupt while waiting.");

                            // end the event loop
                            teardown();
                            return;
                        }
                    }
                    // download the up-to-date state
                    newState = stateUpdate_.copy();
                }

                // now update has a nice up-to-date copy.
                // prepare datagram
                if (!updatePacket(newState)) {
                    continue;
                }
                // update the internal state
                stateMonitor_ = newState;

                try {
                    // System.out.println(String.format("transact: %s", newState.toString()));
                    transact(); 
                    if (LOGGING_ENABLED == true) {
                        updateLogger();
                    }
                    if (stateMonitor_.shutdown) {
                        return;
                    }
                } catch (IOException e) {
                    System.err.println("***failed to communicate with FastEventServer.");
                    teardown();
                }
           }
        }

        private void setup() {
            openConnection();
            if (LOGGING_ENABLED == true) {
                setupLogger();
            }
        }

        private void teardown() {
            closeConnection();
            if (LOGGING_ENABLED == true) {
                finalizeLogger();
            }
        }

        /**
         * sub-routine for transaction.
         */
        private void transact()
            throws IOException
        {
            // for logging: note the beginning time
            if (PROFILE_LATENCY) {
                start = System.nanoTime();
            }

            connection_.send(packet_);
            // if update suggests shutdown, then there will be no more processing.
            if (stateMonitor_.shutdown) {
                teardown();
                return;
            }
            
            // receive the response otherwise
            connection_.receive(packet_);
            
            // TODO: check the state from the response packet

            // for logging: update latency stats
            if (PROFILE_LATENCY) {
                nrequests++;
                end      = System.nanoTime();
                long latency = (end - start)/1000;
                sum_latency += (double)latency;
                if (min_latency > latency) min_latency = latency;
                if (max_latency < latency) max_latency = latency;
            }
        }

        /**
         * updates packet_ member based on the difference between
         * its internal state (stateMonitor_) and the up-to-date state (update)
         *
         * @param update    the up-to-date state
         * @return hasContents whether the updated packet has a content
         */
        private boolean updatePacket(EventTriggerManager.State update) {
            if (update.shutdown == true) {
                buffer_[0] = SHUTDOWN;
                packet_.setData(buffer_, 0, 1);
                return true;
            }
            // otherwise, add byte-by-byte
            buffer_[0] = (byte)0x00;
            if (update.event != stateMonitor_.event) {
                buffer_[0] |= (update.event)? EVENT_ON: EVENT_OFF;
            }
            if (update.sync != stateMonitor_.sync) {
                buffer_[0] |= (update.sync)? SYNC_ON: SYNC_OFF;
            }
            packet_.setData(buffer_, 0, 1);
            return (buffer_[0] != (byte)0x00);
        } 

        /**
         * opens a connection to the host.
         *
         * if it fails to receive the "hello" message from the host,
         * the `connection_` property will be set null.
         */
        private void openConnection() {
            String hostName = FastEventSettings.getServiceHost();
            try {
                // prepare UDP socket
                connection_ = new DatagramSocket();
                connection_.connect(InetAddress.getByName(hostName), FastEventSettings.getServicePort());
                packet_ = new DatagramPacket(new byte [] { 0x00 }, 1,
                                            connection_.getInetAddress(),
                                            connection_.getPort());
            } catch (UnknownHostException e) {
                System.err.println("***failed to resolve host: "+hostName);
                connection_ = null;
            } catch (IOException e) {
                System.err.println("***failed connecting to the host: "+hostName);
                connection_ = null;
            }
        }

        /**
         * closes a connection to the host.
         *
         * if the connection is not open, it does nothing.
         */
        private void closeConnection() {
            // do not try to "close" the connection,
            // if it is not opened.
            if (connection_ == null) {
                return;
            }

            connection_.close();

            // log latency
            if (PROFILE_LATENCY) {
                if (nrequests > 0){
                    System.out.println("------------------------------");
                    System.out.println(String.format("min. latency = %6d usec", min_latency));
                    System.out.println(String.format("max. latency = %6d usec", max_latency));
                    System.out.println(String.format("avg. latency = %6.1f usec", sum_latency/nrequests));
                    System.out.println("------------------------------");
                } else {
                    System.out.println("***seems to have had no event output for FastEventServer.");
                }
            }
 
        }

        /**
         * sets up `logger` buffered writer instance.
         */
        private void setupLogger() {
            logger = FastEventWriter.fromBaseName("EventTriggerManager",
                                                null,
                                                "index,start,end,sync,event");
        }

        private void updateLogger() {
            logger.write(String.format("%d,%d,%d,%b,%b",
                        stateMonitor_.index,
                        start,
                        end,
                        stateMonitor_.sync,
                        stateMonitor_.event));
        }

        /**
         * finalizes `logger` buffered writer instance.
         */
        private void finalizeLogger() {
            logger.close();
        }
    }

    private static State                stateUpdate_ = new State(0, false, false, false);
    private static final Object         io_ = new Object();
    private static java.lang.Thread     ioThread_;

    /**
     * updates the EVENT trigger flag.
     */
    public static void setEvent(boolean value) {
        synchronized (io_) {
            if (value != stateUpdate_.event) {
                stateUpdate_.event = value;
                stateUpdate_.index++;
                // System.out.println(String.format("new state=%s", stateUpdate_.toString()));
                io_.notify();
            }
        }
    }

    /**
     * updates the SYNC trigger flag.
     */
    public static void setSync(boolean value) {
        synchronized (io_) {
            if (value != stateUpdate_.sync) {
                stateUpdate_.sync = value;
                stateUpdate_.index++;
                System.out.println(String.format("new state=%s", stateUpdate_.toString()));
                io_.notify();
            }
        }
    }

    /**
     * shuts down the server.
     */
    public static void shutdown() {
        synchronized (io_) {
            stateUpdate_.shutdown = true;
            stateUpdate_.index++;
            // System.out.println(String.format("new state=%s", stateUpdate_.toString()));
            io_.notify();
        }
        try {
            ioThread_.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

}


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

/**
 * a `packet` class to be sent from a TrackingDelegate to EvaluationTarget.
 *
 * <h3>What TrackingDelegate must initialize</h3>
 * Most probably during its initialization.
 * <ul>
 * <li>type -- the type of what is tracked (position or angle)</li>
 * <li>trackerHeaders -- the tracker-specifi header used for logging</li>
 * </ul>
 *
 * <h3>What TrackingDelegate must update</h3>
 * Supposed to be during the `filterPacket` call.
 * <ul>
 * <li>nevents -- the number of events in the packet</li>
 * <li>firstts, lastts -- the first and the last timestamps of the events in the packet</li>
 * <li>coords -- the array of 2 double numbers that represents the tracked position</li>
 * <li>confidence -- the array of 2 double numbers that represents the confidence intervals
 * of the tracked positions</li>
 * </ul>
 *
 * <h3>What EvaluationTarget must update</h3>
 * <ul>
 * <li>eval -- whether or not the tracked position satisfies the target's criteria.
 * must be false unless the evaluation mode is 'evaluating'.</li>
 * </ul>
 *
 * @author gwappa
 * @see TrackingDelegate
 * @see EvaluationTarget
 */
public final class TrackingParameter
{
    public static final int NDIM = 2;
    private static final String     DELIM           = ",";
    private static final String[]   DEFAULT_HEADERS = new String[] {"nevents",
                                                                    "firstts",
                                                                    "lastts",
                                                                    "eval"};

    public final    CoordinateType  type;
    public int                      nevents         = 0;
    public long                     firstts         = 0;
    public long                     lastts          = 0;
    public double []                coords          = new double[NDIM];
    public double []                confidence      = new double[NDIM];
    public boolean                  eval            = false; 
    public String []                trackerHeaders  = new String[] {};

    public TrackingParameter(CoordinateType type){
        this.type = type;
    }


    /**
     * generate header array string based on given array.
     * Since I did not want to force using Java 8, it does not utilize String.join()
     * as the initial implementation.
     *
     * @param trackerSpecificHeaders the `NDIM`-sized String header array to be used
     * in place of `coords`
     */
    public final String generateHeaders(String[] trackerSpecificHeaders) {
        StringBuilder builder = new StringBuilder();

        int len_default = DEFAULT_HEADERS.length;
        for(int i=0; i<len_default; i++){
            builder.append(DEFAULT_HEADERS[i]);
            builder.append(DELIM);
        }

        // NOTE: for the moment, confidence intervals are not written.
        for(int i=0; i<NDIM; i++){
            builder.append(trackerSpecificHeaders[i]);
            if (i < (NDIM-1)) {
                builder.append(DELIM);
            }
        }

        trackerHeaders = trackerSpecificHeaders;

        return builder.toString();
    }

    /**
     * generate a record of this TrackingParameter instance
     * in a form of CSV.
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(nevents);
        builder.append(DELIM);
        builder.append(firstts);
        builder.append(DELIM);
        builder.append(lastts);
        builder.append(DELIM);
        builder.append(eval);
        builder.append(DELIM);

        // NOTE: for the moment, confidence intervals are not written.
        for(int i=0; i<NDIM; i++){
            builder.append(coords[i]);
            if (i < (NDIM-1)) {
                builder.append(DELIM);
            }
        }

        return builder.toString();
    }

    /**
     * deprecated: only retained for compatibility.
     */
    public final String[] getHeadersAsString() {
        String [] ret = new String[1 + NDIM];
        ret[0] = DEFAULT_HEADERS[0];
        ret[1] = trackerHeaders[0];
        ret[2] = trackerHeaders[1];
        return ret;
    }

    /**
     * deprecated: only retained for compatibility.
     */
    public final String[] getDataAsString() {
        String[] ret = new String[DEFAULT_HEADERS.length + NDIM];
        ret[0] = String.valueOf(nevents);
        ret[1] = String.valueOf(coords[0]);
        ret[2] = String.valueOf(coords[1]);
        return ret;
    }

}


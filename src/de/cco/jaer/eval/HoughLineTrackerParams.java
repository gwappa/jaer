/*
 * Copyright (C) 2017 Viktor Bahr
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
 * Class that acts as a sinc for jAER HoughLineTracker filter data, Extends
 * TrackerParamBase template class
 *
 * @author viktor
 */
public class HoughLineTrackerParams extends TrackerParamsBase {

    /**
     * HoughLineTracker parameters
     *
     * line_rho, line_theta Line rho and theta coordinates in Hough Space
     * prev_rho, prev_theta Rho and Theta of previous package rho_res, theta_res
     * Resolution of theta and rho in Hough space
     */
    private float line_rho, line_theta;
    private float prev_rho, prev_theta;
    private float rho_res, theta_res;

    /**
     * Class constructor, set default hough space resolution
     */
    public HoughLineTrackerParams() {
        setName("HoughLineTracker");
        setRhoRes(6);
        setThetaRes(10);
    }

    /**
     * Update class parameters with current filter values, called from
     * HoughLineTracker
     *
     * @param n Number of events in filtered package
     * @param first_ts First timestamp in event package
     * @param last_ts Last timestamp in event package
     * @param rho Current line rho
     * @param theta Current line theta
     */
    public void update(int n, int first_ts, int last_ts, float rho, float theta) {
        setNumEvents(n);
        setFirstTS(first_ts);
        setLastTS(last_ts);
        prev_rho = line_rho;
        prev_theta = line_theta;
        line_rho = rho;
        line_theta = theta;
    }

    /**
     * Setter for Hough space rho resolution
     *
     * @param res Resolution of rho parameter
     */
    public void setRhoRes(float res) {
        rho_res = res;
    }

    /**
     * Setter for Hough space theta resolution
     *
     * @param res Resolution of theta parameter
     */
    public void setThetaRes(float res) {
        theta_res = res;
    }

    /**
     * Get euclidian distance between last and current line
     *
     * @return Euclidian distance between last and current line
     */
    public double getDist() {
        double dRho = Math.abs((line_rho - prev_rho) / rho_res);
        double dTheta = Math.abs((line_theta - prev_theta) / theta_res);
        return Math.sqrt(dRho * dRho + dTheta * dTheta);
    }

    /**
     * Print class parameters as comma-seperated string
     *
     * @return Tracker data in CSV row style
     */
    @Override
    public String print() {
        return getEventRate() + "," + getFirstTS() + "," + getLastTS() + "," + line_rho + "," + line_theta + "," + getDist();
    }

    /**
     * Print class parameter description as command-seperated string
     *
     * @return CSV file header
     */
    @Override
    public String printHeader() {
        return "eventrate,first_ts,last_ts,rho,theta,distance,eval";
    }

    /**
     * Evaluate threshold using data of current package
     *
     * @param thresh Instance of EvaluatorThreshold
     * @return True, if threshold is exceeded, otherwise false
     */
    @Override
    public Boolean eval(EvaluatorThreshold thresh) {
        // TODO: Implement more thresholds for theta and rho?
        switch (thresh.getTarget()) {
            case EVENTRATE:
                return (getEventRate() > (double) thresh.getValue());
            case POSITION:
                // TODO: true, when any part of the line crosses this point
                return false;
            case SPEED:
                // TODO: Implement a measure for line speed
                return false;
            case DISTANCE:
                return (getDist() > (double) thresh.getValue());
        }
        return false;
    }
}

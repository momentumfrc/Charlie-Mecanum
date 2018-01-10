package org.usfirst.frc.team4999.robot;

import java.util.HashMap;
import java.util.HashSet;


import edu.wpi.first.wpilibj.GenericHID;

public class ControllerWrapper {
	private HashSet<Integer> held;
	private HashMap<Integer, Double> deadzones;
	private HashMap<Integer, Double> curves;
	
	public GenericHID controller;
	
	public ControllerWrapper(GenericHID wrap) {
		controller = wrap;
		held = new HashSet<Integer>();
		deadzones = new HashMap<Integer, Double>();
		curves = new HashMap<Integer, Double>();
	}
	
	/**
	 * Returns true if this is the first push of a button on the controller. When called from an iterative class, it returns true for the first loop and then false until the button is released and pushed again.
	 * @param button The number of the button to check
	 * @return True if this is the first push of the button. False otherwise
	 */
	public boolean isFirstPush(int button) {
		if(controller.getRawButton(button)) {
			if(held.contains(button)) {
				return false;
			} else {
				held.add(button);
				return true;
			}
		} else {
			held.remove(button);
			return false;
		}
	}
	
	/**
	 * Returns the value of the axis, between -1 and 1. Will adjust for any curves and deadzones set
	 * @param axis Which axis to return
	 * @return The value of the axis. A number between -1 and 1
	 */
	public double getRawAxis(int axis) {
		double value = controller.getRawAxis(axis);
		if(deadzones.containsKey(axis))
			value = deadzone(value, deadzones.get(axis));
		if(curves.containsKey(axis))
			value = expCurve(value, curves.get(axis));
		return value;
	}
	
	/**
	 * Sets a deadzone on the controller
	 * @param axis The axis to set the deadzone on
	 * @param value The deadzone to set
	 */
	public void setDeadzone(int axis, double value) {
		deadzones.put(axis, value);
	}
	
	/**
	 * Sets a curve on the controller
	 * The curve functions by raising the value of the axis (which is between -1 and 1) to the power specified
	 * @param axis The axis to set the curve on
	 * @param value The value of the curve.
	 */
	public void setCurve(int axis, double value) {
		curves.put(axis,  value);
	}
	
	/**
	 * Applies an exponential curve to the input
	 * @param input The value to be processed
	 * @param pow The curve to be set
	 * @return The processed value
	 */
	private double expCurve(double input, double pow) {
		if(input == 0)
			return input;
		double powed = Math.pow(Math.abs(input), pow);
		if(input * powed > 0)
			return powed;
		else
			return -powed;
	}
	
	private double deadzone(double input, double deadzone) {
		if(Math.abs(input) < deadzone) {
			return 0;
		} else if(input < 0) {
			return map(input, -1, -deadzone, -1, 0);
		} else if(input > 0) {
			return map(input, deadzone, 1, 0, 1);
		}
		return 0;
	}
	
	private double map(double input, double minIn, double maxIn, double minOut, double maxOut) {
		return minOut + (maxOut - minOut) * ((input - minIn) / (maxIn - minIn));
	}

}

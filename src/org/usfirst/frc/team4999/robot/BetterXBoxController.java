package org.usfirst.frc.team4999.robot;

import java.util.HashSet;
import java.util.HashMap;

import edu.wpi.first.wpilibj.XboxController;
/**
 * Stores the hand requested, and the value of the rumble
 * @author jordan
 *
 */
class RumbleValue {
	public XboxController.RumbleType hand;
	public double value;
	public RumbleValue(XboxController.RumbleType hand, double value) {
		this.hand = hand;
		this.value = value;
	}
}

/**
 * Extended xbox controller. Improves system for deadzones, button pushes, rumbles, and response curves.
 * @author jordan
 *
 */
public class BetterXBoxController extends XboxController {
	// HashSets and HashMaps to hold data concerning buttons, rumbles, deadzones, and curves
	HashSet<Integer> held;
	HashMap<String, RumbleValue> rumbles;
	HashMap<Integer, Double> deadzones;
	HashMap<Integer, Double> curves;
	
	public BetterXBoxController(int port) {
		super(port);
		held = new HashSet<Integer>();
		rumbles = new HashMap<String, RumbleValue>();
		deadzones = new HashMap<Integer, Double>();
		curves = new HashMap<Integer, Double>();
	}
	
	/**
	 * Returns true if this is the first push of a button on the controller. When called from an iterative class, it returns true for the first loop and then false until the button is released and pushed again.
	 * @param button The number of the button to check
	 * @return True if this is the first push of the button. False otherwise
	 */
	public boolean isFirstPush(int button) {
		if(this.getRawButton(button)) {
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
	
	public boolean isFirstPushA() {
		return isFirstPush(1);
	}
	public boolean isFirstPushB() {
		return isFirstPush(2);
	}
	public boolean isFirstPushX() {
		return isFirstPush(3);
	}
	public boolean isFirstPushY() {
		return isFirstPush(4);
	}
	public boolean isFirstPushStick(Hand hand) {
		if (hand.equals(Hand.kLeft)) {
	      return isFirstPush(9);
	    } else {
	      return isFirstPush(10);
	    }
	}
	public boolean isFirstPushBack() {
		return isFirstPush(7);
	}
	public boolean isFirstPushStart() {
		return isFirstPush(8);
	}
	
	/**
	 * Returns the value of the axis, between -1 and 1. Will adjust for any curves and deadzones set
	 * @param axis Which axis to return
	 * @return The value of the axis. A number between -1 and 1
	 */
	@Override
	public double getRawAxis(int axis) {
		double value = super.getRawAxis(axis);
		if(deadzones.containsKey(axis) && Math.abs(value) < deadzones.get(axis))
			value = 0;
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
	 * Sets a deadzone on the x axis of a stick on the controller
	 * @param hand Which stick to set, left or right
	 * @param value The the deadzone to set
	 */
	public void setDeadzoneX(Hand hand, double value) {
		switch(hand) {
		case kRight:
			deadzones.put(4, value);
			break;
		case kLeft:
			deadzones.put(0, value);
			break;
		}
	}
	/**
	 * Sets a deadzone on the y axis of a stick on the controller
	 * @param hand Whick stick to set, left or right
	 * @param value The deadzone to set
	 */
	public void setDeadzoneY(Hand hand, double value) {
		switch(hand) {
		case kRight:
			deadzones.put(5, value);
			break;
		case kLeft:
			deadzones.put(1, value);
			break;
		}
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
	 * Set a curve on the x axis of a stick on the controller
	 * The curve functions by raising the value of the axis (which is between -1 and 1) to the power specified
	 * @param hand Which stick to set, left or right
	 * @param value The curve to set
	 */
	public void setCurveX(Hand hand, double value) {
		switch(hand) {
		case kRight:
			curves.put(4, value);
			break;
		case kLeft:
			curves.put(0, value);
			break;
		}
	}
	/**
	 * Set a curve on the y axis of a stick on the controller
	 * The curve functions by raising the value of the axis (which is between -1 and 1) to the power specified
	 * @param hand Which stick to set, left or right
	 * @param value The curve to set
	 */
	public void setCurveY(Hand hand, double value) {
		switch(hand) {
		case kRight:
			curves.put(5, value);
			break;
		case kLeft:
			curves.put(1, value);
			break;
		}
	}
	
	/**
	 * Loops through the rumbles set and applies them
	 */
	private void refreshRumbles() {
		double lHandRumble = 0;
		double rHandRumble = 0;
		for(String key: rumbles.keySet()) {
			RumbleValue val = rumbles.get(key);
			switch(val.hand) {
			case kRightRumble:
				lHandRumble = (val.value > lHandRumble)? val.value: lHandRumble;
				break;
			case kLeftRumble:
				rHandRumble = (val.value > rHandRumble)? val.value: rHandRumble;
				break;
			}
		}
		setRumble(RumbleType.kLeftRumble, lHandRumble);
		setRumble(RumbleType.kRightRumble, rHandRumble);
	}
	/**
	 * Adds a rumble to the controller
	 * @param name The name to register the rumble as
	 * @param hand The hand to add the rumble to
	 * @param value The amount (between 0 and 1) to rumble
	 */
	public void addRumble(String name, RumbleType hand, double value) {
		rumbles.put(name, new RumbleValue(hand, value));
		refreshRumbles();
	}
	/**
	 * Remove the specified rumble
	 * @param name Which rumble to remove
	 */
	public void removeRumble(String name) {
		rumbles.remove(name);
		refreshRumbles();
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

}

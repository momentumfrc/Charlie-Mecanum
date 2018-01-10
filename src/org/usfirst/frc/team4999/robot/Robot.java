package org.usfirst.frc.team4999.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


enum drivemode {XBOX,STICK,F310};

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	ADXRS450_Gyro gyro;
	MecanumDrive drive;
	
	Victor frontLeft, frontRight, backLeft, backRight;
	
	Joystick stick;
	BetterXBoxController xbox;
	ControllerWrapper f310;
	
	SendableChooser<drivemode> mode;
	
	double DEADZONE = 0.2;
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		gyro = new ADXRS450_Gyro();
		gyro.calibrate();
		
		frontLeft = new Victor(0);
		frontRight = new Victor(1);
		frontRight.setInverted(true);
		backLeft = new Victor(2);
		backRight = new Victor(3);
		backRight.setInverted(true);

		
		drive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);
		drive.setDeadband(0);
		
		stick = new Joystick(1);
		xbox = new BetterXBoxController(0);
		f310 = new ControllerWrapper(new Joystick(2));
		
		mode = new SendableChooser<drivemode>();
		mode.addObject("Flight Stick", drivemode.STICK);
		mode.addObject("Logitech F310", drivemode.F310);
		mode.addDefault("XBox Controller", drivemode.XBOX);
		
		SmartDashboard.putData("Drive Mode", mode);
		
		if(!SmartDashboard.containsKey("Deadzone")) 
			SmartDashboard.putNumber("Deadzone", DEADZONE);
		SmartDashboard.putNumber("Throttle", 1);
		SmartDashboard.getEntry("Deadzone").setPersistent();
		SmartDashboard.getEntry("Throttle").setPersistent();
	}

	@Override
	public void autonomousInit() {
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
	}
	
	@Override
	public void teleopInit(){
	}
	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		double x = 0, y = 0, twist = 0, speedLimit = 1;
		switch(mode.getSelected()) {
		case STICK:
			x = stick.getX();
			y = stick.getY();
			twist = stick.getZ();
			
			speedLimit = (-stick.getThrottle() + 1) / 2;
			SmartDashboard.putNumber("Throttle", speedLimit);
			
			break;
		case XBOX:
			x = -xbox.getX(Hand.kLeft);
			y = xbox.getY(Hand.kLeft); 	
			twist = xbox.getX(Hand.kRight);
			speedLimit = SmartDashboard.getNumber("Throttle",speedLimit);
			break;
		case F310:
			x = -f310.getRawAxis(0);
			y = f310.getRawAxis(1);
			twist = f310.getRawAxis(4);
			speedLimit = SmartDashboard.getNumber("Throttle",speedLimit);
			
		}
		
		
		x = deadzone(x, SmartDashboard.getNumber("Deadzone",DEADZONE));
		y = deadzone(y, SmartDashboard.getNumber("Deadzone",DEADZONE));
		x = expcurve(x, 2.5);
		y = expcurve(y, 2.5);
		twist = deadzone(twist, SmartDashboard.getNumber("Deadzone",DEADZONE));
		
		x = x * speedLimit;
		y = y * speedLimit;
		twist = twist * speedLimit;
		
		if(xbox.isFirstPushY() || f310.isFirstPush(4)) {
			drive.stopMotor();
			gyro.calibrate();
		}
		double gyro_a = gyro.getAngle();
		// System.out.format("X:%.2f, Y:%.2f, twist:%.2f, gyro:%.2f\n", x, y, twist, gyro_a);
		drive.driveCartesian(x, y, twist, gyro_a);
		
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
	
	private double deadzone(double value, double zone) {
		if(Math.abs(value) < zone) {
			return 0;
		} else if(value < 0) {
			return map(value, -1, -zone, -1, 0);
		} else if(value > 0) {
			return map(value, zone, 1, 0, 1);
		}
		return 0;
	}
	
	private double expcurve(double value, double curve) {
		double ret = Math.pow(Math.abs(value), curve);
		return (ret * value > 0) ? ret: -ret;
	}
	
	private double map(double input, double minIn, double maxIn, double minOut, double maxOut) {
		return minOut + (maxOut - minOut) * ((input - minIn) / (maxIn - minIn));
	}
	
}


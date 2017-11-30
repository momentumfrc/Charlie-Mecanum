package org.usfirst.frc.team4999.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


enum drivemode {xbox,stick};

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	ADXRS450_Gyro gyro;
	RobotDrive drive;
	
	Victor frontLeft, frontRight, backLeft, backRight;
	
	Joystick stick;
	XboxController xbox;
	
	SendableChooser<drivemode> mode;
	
	static double DEADZONE = 0.2;
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		gyro = new ADXRS450_Gyro();
		
		frontLeft = new Victor(0);
		frontRight = new Victor(1);
		backLeft = new Victor(2);
		backRight = new Victor(3);
		
		drive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		
		stick = new Joystick(1);
		xbox = new XboxController(0);
		
		mode = new SendableChooser<drivemode>();
		mode.addObject("Flight Stick", drivemode.stick);
		mode.addDefault("XBox Controller", drivemode.xbox);
		
		SmartDashboard.putData("Drive Mode", mode);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
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
		gyro.calibrate();
	}
	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		double x = 0, y = 0, twist = 0, speedLimit = 1;
		switch(mode.getSelected()) {
		case stick:
			x = stick.getX();
			y = stick.getY();
			twist = stick.getZ();
			
			speedLimit = (-stick.getThrottle() + 1) / 2;
			
			break;
		case xbox:
			x = xbox.getX(Hand.kLeft);
			y = xbox.getY(Hand.kLeft); 	
			twist = xbox.getX(Hand.kRight);
			break;
		}
		
		
		x = deadzone(x, DEADZONE);
		y = deadzone(y, DEADZONE);
		x = expcurve(x, 2.5);
		y = expcurve(y, 2.5);
		twist = deadzone(twist, DEADZONE);
		
		x = x * speedLimit;
		y = y * speedLimit;
		twist = twist * speedLimit;
		
		double gyro_a = gyro.getAngle();
		// System.out.format("X:%.2f, Y:%.2f, twist:%.2f, gyro:%.2f\n", x, y, twist, gyro_a);
		drive.mecanumDrive_Cartesian(x, y, twist, gyro_a);
		
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


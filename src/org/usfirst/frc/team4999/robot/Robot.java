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
		mode.addDefault("Flight Stick", drivemode.stick);
		mode.addObject("XBox Controller", drivemode.xbox);
		
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

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		double x = 0, y = 0, twist = 0;
		switch(mode.getSelected()) {
		case stick:
			x = stick.getX();
			y = stick.getY();
			twist = stick.getZ();
			break;
		case xbox:
			x = stick.getX(Hand.kLeft);
			y = stick.getY(Hand.kLeft);
			twist = stick.getX(Hand.kRight);
			break;
		}
		
		
		x = deadzone(x, 0.1);
		y = deadzone(y, 0.1);
		twist = deadzone(twist, 0.1);
		
		double gyro_a = gyro.getAngle();
		
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
		} else {
			return value;
		}
	}
	
}


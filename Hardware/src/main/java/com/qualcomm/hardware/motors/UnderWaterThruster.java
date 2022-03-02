
package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.ModernRoboticsMotorControllerParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;

import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@MotorType(ticksPerRev=1478.4, gearing=52.8, maxRPM=190, orientation= Rotation.CW)
@DeviceProperties(xmlTag = "UnderWaterThruster", name = "Under Water Thruster Motor", builtIn = true)
@ModernRoboticsMotorControllerParams(P=192, I=44, D=136, ratio=16)
@DistributorInfo(distributor="Aqua Robotics", model="UNDERWATER-217", url="http://www.modernroboticsinc.com/12v-6mm-motor-kit-2")
public interface UnderWaterThruster
{

}
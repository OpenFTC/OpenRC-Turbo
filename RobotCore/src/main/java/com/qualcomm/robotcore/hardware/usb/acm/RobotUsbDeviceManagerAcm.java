package com.qualcomm.robotcore.hardware.usb.acm;

import android.hardware.usb.UsbDevice;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.SerialNumber;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.usb.LynxModuleSerialNumber;

import java.util.ArrayList;
import java.util.List;

public class RobotUsbDeviceManagerAcm implements RobotUsbManager
{
    PicoSerialManager manager = PicoSerialManager.getInstance(AppUtil.getDefContext());

    @Override
    public List<SerialNumber> scanForDevices() throws RobotCoreException
    {
        ArrayList<UsbDevice> devices = manager.scanForPicos();
        ArrayList<SerialNumber> ret = new ArrayList<>();

        for(UsbDevice dev : devices)
        {
            ret.add(SerialNumber.fromString(dev.getSerialNumber()));
        }

        return ret;
    }

    @Override
    public RobotUsbDevice openBySerialNumber(SerialNumber serialNumber) throws RobotCoreException
    {
        System.out.println("Acm manager opening opening " + serialNumber.getString());

        ArrayList<UsbDevice> ports = manager.scanForPicos();

        for(UsbDevice dev : ports)
        {
            if(dev.getSerialNumber().equals(serialNumber.getString()))
            {
                PicoSerialPort picoSerialPort = manager.connectDevice(dev);

                if(picoSerialPort == null)
                {
                    return null;
                }

                RobotUsbDeviceAcm acm = new RobotUsbDeviceAcm(picoSerialPort, serialNumber);
                return acm;
            }
        }

        return null;
    }
}

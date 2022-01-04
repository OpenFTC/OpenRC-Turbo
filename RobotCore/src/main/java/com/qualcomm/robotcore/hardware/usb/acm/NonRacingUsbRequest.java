package com.qualcomm.robotcore.hardware.usb.acm;

import android.hardware.usb.UsbRequest;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * https://issuetracker.google.com/issues/37060141?pli=1
 */
public class NonRacingUsbRequest extends UsbRequest
{
    static final String usbRqBufferField = "mBuffer";
    static final String usbRqLengthField = "mLength";

    Field usbRequestBuffer;
    Field usbRequestLength;

    public NonRacingUsbRequest()
    {
        try
        {
            usbRequestBuffer = UsbRequest.class.getDeclaredField(usbRqBufferField);
            usbRequestLength = UsbRequest.class.getDeclaredField(usbRqLengthField);
            usbRequestBuffer.setAccessible(true);
            usbRequestLength.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean queue(ByteBuffer buffer, int length)
    {
        try
        {
            usbRequestBuffer.set(this, buffer);
            usbRequestLength.set(this, length);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return super.queue(buffer, length);
    }
}
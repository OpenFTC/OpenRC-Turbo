package com.qualcomm.robotcore.hardware.usb.acm;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PicoSerialPort
{
    public enum OpenResultCode
    {
        SUCCESS,
        FAILURE_PERMISSION_DENIED,
        FAILURE_TO_CLAIM_INTERFACE
    }

    private String TAG = "PicoSerialPort(id=%d)";

    private static final int SERIAL_DATA_INTERFACE_NUMBER = 1;
    private static final int DATA_OUT_ENDPOINT_NUM = 0;
    private static final int DATA_IN_ENDPOINT_NUM = 1;

    private UsbDevice usbDevice;
    private UsbManager usbManager;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface serialDataInterface;
    private UsbEndpoint dataOutEndpoint;
    private UsbEndpoint dataInEndpoint;
    private UsbRequest dataOutUrb;
    private UsbRequest dataInUrb;

    private IncomingURB_Poller incomingUrbPoller;
    private final ReentrantLock urbLock = new ReentrantLock();
    private final Condition conditionOutUrbDone = urbLock.newCondition();

    private volatile CountDownLatch pollerStopSynchronizer;

    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int USB_RT_ACM = UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;
    private static final int SET_CONTROL_LINE_STATE = 0x22;

    boolean mRts = false;
    boolean mDtr = true;

    private static final int FIFO_CAPACITY = 256;
    private static final int URB_BUF_SIZE = 64;

    private ByteFIFO rxFifo;
    private ByteFIFO txFifo;

    private byte[] rxBuf;
    private ByteBuffer rxByteBuf;

    private byte[] txBuf;
    private ByteBuffer txByteBuf;

    private boolean dataOutUrbInFlight = false;

    private TxDataSender txThread;
    private volatile CountDownLatch txThreadStopLatch = new CountDownLatch(1);

    public PicoSerialPort(UsbDevice usbDevice, UsbManager usbManager)
    {
        this.usbDevice = usbDevice;
        this.usbManager = usbManager;

        TAG = String.format(TAG, usbDevice.getDeviceId());
    }

    public static boolean isPico(UsbDevice dev)
    {
        return dev.getVendorId()==0x2e8a && dev.getProductId()==0x000a;
    }

    public synchronized OpenResultCode openAssumingPermission()
    {
        Log.d(TAG, "openAssumingPermission()");

        if(!usbManager.hasPermission(usbDevice))
        {
            Log.d(TAG, "Failure opening device, permission denied");
            Log.d(TAG, "...openAssumingPermission()");
            return OpenResultCode.FAILURE_PERMISSION_DENIED;
        }

        usbDeviceConnection = usbManager.openDevice(usbDevice);

        if(usbDeviceConnection != null)
        {
            Log.d(TAG, "Successfully opened device handle");

            // Try to steal control of the gamepad away from Android's input driver
            serialDataInterface = usbDevice.getInterface(SERIAL_DATA_INTERFACE_NUMBER);
            boolean claimed = usbDeviceConnection.claimInterface(serialDataInterface, true);

            if(claimed)
            {
                Log.d(TAG, "Successfully claimed target interface");

                // Find the endpoints we want to talk to
                Log.d(TAG, "Selecting endpoints");
                dataOutEndpoint = serialDataInterface.getEndpoint(DATA_OUT_ENDPOINT_NUM);
                dataInEndpoint = serialDataInterface.getEndpoint(DATA_IN_ENDPOINT_NUM);

                Log.d(TAG, "Initializing URBs");

                // Set up buffers and FIFOs
                txBuf = new byte[URB_BUF_SIZE];
                rxBuf = new byte[URB_BUF_SIZE];
                rxByteBuf = ByteBuffer.wrap(rxBuf);
                txByteBuf = ByteBuffer.wrap(txBuf);
                rxFifo = new ByteFIFO(FIFO_CAPACITY);
                txFifo = new ByteFIFO(FIFO_CAPACITY);

                // Create our URBs
                if(Build.VERSION.SDK_INT >= 26)
                {
                    dataOutUrb = new UsbRequest();
                    dataInUrb = new UsbRequest();
                }
                else
                {
                    dataOutUrb = new NonRacingUsbRequest();
                    dataInUrb = new NonRacingUsbRequest();
                }

                // Initialize our URBs
                dataOutUrb.initialize(usbDeviceConnection, dataOutEndpoint);
                dataInUrb.initialize(usbDeviceConnection, dataInEndpoint);

                Log.d(TAG, "Starting URB worker thread");

                // At this point, we are all set up and ready to start talking to gamepad!
                // First, we start the incoming URB poller.
                pollerStopSynchronizer = new CountDownLatch(1);
                incomingUrbPoller = new IncomingURB_Poller();
                incomingUrbPoller.start();

                setDtrRts();

                // Then, we submit the control surface receive URB. Once the response comes
                // in, we'll automatically resubmit it again and again. But we have to manually
                // kick off that process with the first send here.
                //submitControlSurfaceReceiveURB();
                submitDataInUrb();
                txThread = new TxDataSender();
                txThread.start();

                Log.d(TAG, "Hooking complete");
                Log.d(TAG, "...openAssumingPermission()");
                return OpenResultCode.SUCCESS;
            }
            else
            {
                Log.d(TAG, "Failed to claim target interface; closing device handle");
                usbDeviceConnection.close();
                usbDeviceConnection = null;
                Log.d(TAG, "...openAssumingPermission()");
                return OpenResultCode.FAILURE_TO_CLAIM_INTERFACE;
            }
        }
        else
        {
            Log.d(TAG, "Failure opening device, permission denied");
            Log.d(TAG, "...openAssumingPermission()");
            return OpenResultCode.FAILURE_PERMISSION_DENIED;
        }
    }

    public synchronized void close()
    {
        Log.d(TAG, "close()");

        Log.d(TAG, "Interrupt reader");
        requestReadInterrupt(true);

        Log.d(TAG, "Terminating TX sender thread");
        txThread.interrupt();
        acquireUninterruptibly(txThreadStopLatch);
        txThreadStopLatch = null;
        txThread = null;

        Log.d(TAG, "Canceling in-flight URBs");
        dataOutUrb.cancel();
        dataInUrb.cancel();

        Log.d(TAG, "Terminating incoming URB poller");
        incomingUrbPoller.interrupt();
        acquireUninterruptibly(pollerStopSynchronizer);
        pollerStopSynchronizer = null;
        incomingUrbPoller = null;

        Log.d(TAG, "Closing URBs");
        dataOutUrb.close();
        dataInUrb.close();
        dataOutUrb = null;
        dataInUrb = null;

        Log.d(TAG, "Releasing interface and closing device handle");
        usbDeviceConnection.releaseInterface(serialDataInterface);
        usbDeviceConnection.close();
        usbDeviceConnection = null;

        serialDataInterface = null;
        dataInEndpoint = null;
        dataOutEndpoint = null;

        Log.d(TAG, "...close()");
    }

    public synchronized boolean isOpen()
    {
        return usbDeviceConnection != null;
    }

    private class IncomingURB_Poller extends Thread
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("IncomingURB_Poller");
            Log.d(TAG, "URB worker thread ENTER");
            while(!Thread.currentThread().isInterrupted())
            {
                UsbRequest incomingURB = usbDeviceConnection.requestWait();
                if(incomingURB != null)
                {
                    handleIncomingURB(incomingURB);
                }
            }
            Log.d(TAG, "URB worker thread EXIT");
            pollerStopSynchronizer.countDown();
        }
    }

    private class TxDataSender extends Thread
    {
        byte[] scratch = new byte[256];

        @Override
        public void run()
        {
            System.out.println("TxDatSender START");

            while (!Thread.currentThread().isInterrupted())
            {
                urbLock.lock();
                if (dataOutUrbInFlight)
                {
                    // Wait till it comes back before we send more data
                    try
                    {
                        conditionOutUrbDone.await();
                    }
                    catch (InterruptedException e)
                    {
                        urbLock.unlock();
                        txThreadStopLatch.countDown();
                        return;
                    }
                }

                // We're good to send the data now!
                int len = txFifo.usedSpace();

                if(len > 0)
                {
                    try
                    {
                        txFifo.pull(scratch, len, 0);
                        txByteBuf.put(scratch, 0, len);
                        submitDataOutUrb();
                        urbLock.unlock();
                        continue;
                    }
                    catch (InterruptedException e)
                    {
                        urbLock.unlock();
                        txThreadStopLatch.countDown();
                        return;
                    }
                }

                urbLock.unlock();

                try
                {
                    int count = 1;
                    txFifo.pull(scratch, 1, 0);
                    int more = txFifo.usedSpace();

                    if(more > 0)
                    {
                        txFifo.pull(scratch, more, 1);
                        count += more;

                        if(count > txByteBuf.capacity())
                        {
                            count = txByteBuf.capacity();
                        }
                    }

                    urbLock.lock();

                    txByteBuf.clear();
                    txByteBuf.put(scratch, 0, count);
                    submitDataOutUrb();
                    urbLock.unlock();
                }
                catch (InterruptedException e)
                {
                    txThreadStopLatch.countDown();
                    return;
                }
            }

            txThreadStopLatch.countDown();
        }
    }

    private void acquireUninterruptibly(CountDownLatch latch)
    {
        boolean interrupted = false;

        while (true)
        {
            try
            {
                latch.await();
                break;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                interrupted = true;
            }
        }

        if(interrupted)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void handleIncomingURB(UsbRequest request)
    {
        urbLock.lock();

        if(request == dataOutUrb)
        {
            dataOutUrbInFlight = false;
            conditionOutUrbDone.signal();
        }
        else if(request == dataInUrb)
        {
            rxFifo.pushOverwriting(rxBuf, rxByteBuf.position(), 0);
            submitDataInUrb();
        }

        urbLock.unlock();
    }

    private void submitDataOutUrb()
    {
        urbLock.lock();
        int numToTx = txByteBuf.position();
        txByteBuf.clear();
        dataOutUrb.queue(txByteBuf, numToTx);
        dataOutUrbInFlight = true;
        urbLock.unlock();
    }

    private void submitDataInUrb()
    {
        urbLock.lock();
        rxByteBuf.clear();
        dataInUrb.queue(rxByteBuf, 64);
        urbLock.unlock();
    }

    private void setDtrRts()
    {
        int value = (mRts ? 0x2 : 0) | (mDtr ? 0x1 : 0);
        sendAcmControlMessage(SET_CONTROL_LINE_STATE, value, null);
    }

    private int sendAcmControlMessage(int request, int value, byte[] buf)
    {
        int len = usbDeviceConnection.controlTransfer(USB_RT_ACM, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
        if(len < 0)
        {
            throw new RuntimeException();
        }
        return len;
    }

    public static final int RC_DEVICE_CLOSED            = -1;
    public static final int RC_ILLEGAL_ARGUMENT         = -2;

    private volatile Thread readThread = null;

    public void requestReadInterrupt(boolean req)
    {
        Thread thread = readThread;
        if(thread != null)
        {
            System.out.println("port: request read interrupt " + thread.getName());
            thread.interrupt();
        }
    }

    public int read(byte[] data, int ibFirst, int cbToRead, long msTimeout, @Nullable TimeWindow timeWindow) throws InterruptedException
    {
        synchronized (this)
        {
            if(!isOpen())
            {
                return RC_DEVICE_CLOSED;
            }
        }

        readThread = Thread.currentThread();

        try
        {
            rxFifo.pull(data, cbToRead, ibFirst);
            return cbToRead;
        }
        catch (InterruptedException e)
        {
            synchronized (this)
            {
                if(!isOpen())
                {
                    return RC_DEVICE_CLOSED;
                }
            }

            throw e;
        }
        finally
        {
            readThread = null;
        }
    }

    public void write(byte[] data)
    {
        txFifo.pushOverwriting(data);
    }

    public String getSerialNumber()
    {
        return usbDevice.getSerialNumber();
    }

    public String getManufacturer()
    {
        return usbDevice.getManufacturerName();
    }

    public String getProduct()
    {
        return usbDevice.getProductName();
    }

    public int getVid()
    {
        return usbDevice.getVendorId();
    }

    public int getPid()
    {
        return usbDevice.getProductId();
    }

    public int getId()
    {
        return usbDevice.getDeviceId();
    }

    public UsbDevice getUsbDevice()
    {
        return usbDevice;
    }
}
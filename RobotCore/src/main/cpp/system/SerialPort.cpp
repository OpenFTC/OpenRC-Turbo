/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// Native method support for SerialPort class
// Inspired by https://github.com/cepr/android-serialport-api/tree/master/android-serialport-api/project

#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <jni.h>
#include "android/log.h"
#include <stdlib.h>
#include <stdio.h>
#include "ftc.h"

#undef TAG
static LPCSTR TAG = "SerialPortNative";

static speed_t getBaudrate(jint baudrate)
    {
    if (baudrate == 4000000) return B4000000;
    if (baudrate == 3500000) return B3500000;
    if (baudrate == 3000000) return B3000000;
    if (baudrate == 2500000) return B2500000;
    if (baudrate == 2000000) return B2000000;
    if (baudrate == 1500000) return B1500000;
    if (baudrate == 1152000) return B1152000;
    if (baudrate == 1000000) return B1000000;
    if (baudrate == 921600) return B921600;
    if (baudrate == 576000) return B576000;
    if (baudrate == 500000) return B500000;
    if (baudrate == 460800) return B460800;
    if (baudrate == 230400) return B230400;
    if (baudrate == 115200) return B115200;
    if (baudrate == 57600) return B57600;
    if (baudrate == 38400) return B38400;
    if (baudrate == 19200) return B19200;
    if (baudrate == 9600) return B9600;
    if (baudrate == 4800) return B4800;
    if (baudrate == 2400) return B2400;
    if (baudrate == 1800) return B1800;
    if (baudrate == 1200) return B1200;
    if (baudrate == 600) return B600;
    if (baudrate == 300) return B300;
    if (baudrate == 200) return B200;
    if (baudrate == 150) return B150;
    if (baudrate == 134) return B134;
    if (baudrate == 110) return B110;
    if (baudrate == 75) return B75;
    if (baudrate == 50) return B50;
    return 0;
    }

/*
 * internal function to enable high speed UART.
 */
int enableUART()
    {
    const char filepath[] = "/sys/devices/soc.0/78af000.uart/clock";
    FILE *tgtFile;
    int retVal;

    tgtFile = fopen(filepath, "w");
    if (tgtFile == NULL)
        {
        LOGE("enableUART - Failed to open %s, errno = %d.", filepath, errno);
        retVal = 1;
        }
    else
        {
        if (fputc('1', tgtFile) == EOF)
            {
            LOGE("enableUART - Failed to write '1' to %s.", filepath);
            retVal = 2;
            }
        else
            {
            retVal = 0;
            }
        fclose(tgtFile);
        }

    return retVal;
    }

/*
 * Class:     com_qualcomm_robotcore_hardware_usb_serial_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_qualcomm_robotcore_hardware_usb_serial_SerialPort_open(
        JNIEnv *penv,
        jclass  serialPortClass,
        jstring path,
        jint    baudrate,
        jboolean isDragonboard)
    {
    jobject result = NULL;

    // Validate params
    speed_t speed = getBaudrate(baudrate);
    if (speed > 0)
        {
        // Get native-version of string
        LPCSTR utf8Path = penv->GetStringUTFChars(path, NULL);
        if (utf8Path != NULL)
            {
            // Open that puppy!
            //
            //  O_RDWR:     Open read-write
            //  O_SYNC:     Write operations on the file will complete according to the requirements of synchronized I/O file integrity completion
            //  O_NOCTTY:   If the named file is a terminal device, don't make it the controlling terminal for the process
            //
            // Of those, we only know for *certain* that we need O_RDWR.
            //
            int fd = open(utf8Path, O_RDWR | O_SYNC | O_NOCTTY);
            if (fd != -1)
                {
                LOGV("opened(%s): fd=%d", utf8Path, fd);

                // Fetch current configuration
                struct termios cfg;
                if (tcgetattr(fd, &cfg) == 0)
                    {
                    // Adjust the configuration to our desired profile
                    cfmakeraw(&cfg);            // do raw I/O
                    cfsetispeed(&cfg, speed);   // set the input baud rate
                    cfsetospeed(&cfg, speed);   // set the output baud rate

                    // Configure so that we won't infinitely block when doing a read. This is subtle.
                    //
                    // http://www.cmrr.umn.edu/~strupp/serial.html
                    //
                    // From http://unixwiz.net/techtips/termios-vmin-vtime.html;
                    //      VMIN = 0 and VTIME > 0
                    //      This is a pure timed read. If data are available in the input queue, it's
                    //      transferred to the caller's buffer up to a maximum of nbytes, and returned
                    //      immediately to the caller. Otherwise the driver blocks until data arrives, or
                    //      when VTIME tenths expire from the start of the call. If the timer expires without
                    //      data, zero is returned. A single byte is sufficient to satisfy this read call,
                    //      but if more is available in the input queue, it's returned to the caller.
                    //      Note that this is an overall timer, not an intercharacter one.
                    //
                    // From http://tldp.org/HOWTO/Serial-Programming-HOWTO/x115.html:
                    //      If MIN = 0 and TIME > 0, TIME serves as a timeout value. The read will be satisfied
                    //      if a single character is read, or TIME is exceeded (t = TIME *0.1 s). If TIME
                    //      is exceeded, no character will be returned.
                    //
                    // From http://stackoverflow.com/questions/20154157/termios-vmin-vtime-and-blocking-non-blocking-read-operations:
                    //      In non-blocking mode, VMIN/VTIME have no effect (FNDELAY / O_NDELAY seem to
                    //      be linux variants of O_NONBLOCK, the portable, POSIX flag).
                    //
                    cfg.c_cc[VMIN]  = 0;
                    cfg.c_cc[VTIME] = 1;

                    // Update the configuration to be what we want
                    if (tcsetattr(fd, TCSANOW, &cfg) == 0)
                        {
                        /* Create a corresponding file descriptor */
                        jclass    fileDescriptorClass = penv->FindClass("java/io/FileDescriptor");
                        jmethodID fileDescriptorCtor  = penv->GetMethodID(fileDescriptorClass, "<init>", "()V");
                        jfieldID  fileDescriptorFieldDescriptor = penv->GetFieldID(fileDescriptorClass, "descriptor", "I");

                        result = penv->NewObject(fileDescriptorClass, fileDescriptorCtor);
                        penv->SetIntField(result, fileDescriptorFieldDescriptor, (jint) fd);

                        // attempt to turn on UART clock if this is a Dragonboard
                        if (isDragonboard)
                            {
                                if (enableUART() == 0)
                                {
                                    LOGV("Enabled high speed UART clock.");
                                }
                                else
                                {
                                    LOGE("Failed trying to enable UART clock");
                                }
                            }

                        fd = -1;  // avoid close on the way out
                        }
                    else
                        LOGE("tcsetattr() failed: err=%d", errno);
                    }
                else
                    {
                    LOGE("tcgetattr() failed: err=%d", errno);
                    }

                }
            else
                LOGE("error opening device: err=%d path=%s", errno, utf8Path);

            // Clean up on the way out
            if (fd != -1) close(fd);
            penv->ReleaseStringUTFChars(path, utf8Path);
            }
        else
            LOGE("penv->GetStringUTFChars failed");
        }
    else
        LOGE("invalid baudrate: %d", baudrate);
    //
    return result;
    }

/*
 * Class:     com_qualcomm_robotcore_hardware_usb_serial_SerialPort
 * Method:    close
 * Signature: (Ljava/io/FileDescriptor;)V
 */
JNIEXPORT void JNICALL Java_com_qualcomm_robotcore_hardware_usb_serial_SerialPort_close(
        JNIEnv *penv, jclass serialPortClass, jobject fd)
    {
    // Find access to the integer 'descriptor' member of FileDescriptor
    jclass fileDescriptorClass = penv->FindClass("java/io/FileDescriptor");
    jfieldID fileDescriptorFieldDescriptor = penv->GetFieldID(fileDescriptorClass, "descriptor", "I");

    // Dig out the os file descriptor
    jint osfd = penv->GetIntField(fd, fileDescriptorFieldDescriptor);

    // Close it!
    LOGV("close(osfd = %d)", osfd);
    close(osfd);
    }

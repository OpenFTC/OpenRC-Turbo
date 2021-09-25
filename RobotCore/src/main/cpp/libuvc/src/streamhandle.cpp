//
// streamhandle.cpp
//

#include "libuvc.h"
#include "libuvc/libuvc_internal.h"
#include "errno.h"

uvc_stream_handle::uvc_stream_handle(uvc_device_handle* devh, uvc_streaming_interface* stream_if) :
        userCallbackFrameAvailable(userCallbackLock),
        streamingTransferProcessed(streamingLock)
    {
    this->devh = devh;
    this->stream_if = stream_if;
    this->msUserCallbackThreadExitWait = 5000;  // generous to user code, but not forever
    this->msTransfersTimeout = 1000;            // originally 5000, but smaller helps make 'stop streaming' more responsive. no user code involved
    this->cIsoPacketMax = 32;                   // pretty arbitrary, I think; empirically determined
    this->pUserCallbackThreadInterlock = new ThreadInterlock();
    this->userCallbackThreadStarted = false;
    this->frameNumberPolledMax = frameNumberInvalid;
    allocTransfers(LIBUVC_NUM_TRANSFER_BUFS);

    pFrame = allocateFrame();
    if (pFrame)
        {
        pFrame->resetAppend();
        }
    DL_APPEND(devh->openStreamsList, this);
    }

uvc_frame* uvc_stream_handle::allocateFrame()
    {
    /* We allocate bigger than cbFrame (here,+1) so overflows/overruns are manifest as 'too much data'
     * and so are distinguishable from properly-sized frames */
    return uvc_allocate_frame(getContext(), cbFrameExpected==0 ? LIBUVC_DEFAULT_FRAME_SIZE : cbFrameExpected+1, cbFrameExpected);
    };

uvc_context* uvc_stream_handle::getContext()
    {
    return devh->getContext();
    }

uvc_stream_handle::~uvc_stream_handle()
    {
    UVC_ENTER();
    releaseInterface();
    DL_REMOVE(devh->openStreamsList, this);
    delete pFrame;
    delete pFrameUser;
    delete pFrameUserReturn;
    freeTransfers();
    releaseRef(pUserCallbackThreadInterlock);
    UVC_EXIT_VOID();
    }

uvc_error uvc_stream_handle::claimInterface()
    {
    UVC_ENTER();
    uvc_error_t rc = devh->claimInterface(stream_if->bInterfaceNumber);
    interfaceClaimed = (rc==UVC_SUCCESS);
    UVC_RETURN(rc);
    }

void uvc_stream_handle::releaseInterface()
    {
    UVC_ENTER();
    if (interfaceClaimed)
        {
        interfaceClaimed = false;
        devh->releaseInterface(stream_if->bInterfaceNumber);
        }
    UVC_EXIT_VOID();
    }
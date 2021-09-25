/*********************************************************************
* Software License Agreement (BSD License)
*
*  Copyright (C) 2010-2012 Ken Tossell
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*   * Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*   * Redistributions in binary form must reproduce the above
*     copyright notice, this list of conditions and the following
*     disclaimer in the documentation and/or other materials provided
*     with the distribution.
*   * Neither the name of the author nor other contributors may be
*     used to endorse or promote products derived from this software
*     without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
*  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
*  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
*  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
*  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
*  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
*  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
*  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
*  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
*  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
*  POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/
/**
 * @defgroup device Device handling and enumeration
 * @brief Support for finding, inspecting and opening UVC devices
 */

#include <unistd.h>
#include <libusbi.h>
#include "libuvc.h"
#include "libuvc/libuvc_internal.h"

bool uvc_already_open(uvc_context_t *ctx, struct libusb_device *usb_dev);
void uvc_free_devh(uvc_device_handle_t *devh);

uvc_error_t uvc_scan_control(uvc_device_t *dev, uvc_device_info_t *info);
uvc_error_t uvc_parse_vc(uvc_device_t *dev,
			 uvc_device_info_t *info,
			 const unsigned char *block, size_t block_size);
uvc_error_t uvc_parse_vc_selector_unit(uvc_device_t *dev,
					uvc_device_info_t *info,
					const unsigned char *block, size_t block_size);
uvc_error_t uvc_parse_vc_extension_unit(uvc_device_t *dev,
					uvc_device_info_t *info,
					const unsigned char *block,
					size_t block_size);
uvc_error_t uvc_parse_vc_header(uvc_device_t *dev,
				uvc_device_info_t *info,
				const unsigned char *block, size_t block_size);
uvc_error_t uvc_parse_vc_input_terminal(uvc_device_t *dev,
					uvc_device_info_t *info,
					const unsigned char *block,
					size_t block_size);
uvc_error_t uvc_parse_vc_processing_unit(uvc_device_t *dev,
					 uvc_device_info_t *info,
					 const unsigned char *block,
					 size_t block_size);

uvc_error_t uvc_scan_streaming(uvc_device_t *dev,
			       uvc_device_info_t *info,
			       int interface_idx);
uvc_error_t uvc_parse_vs(uvc_device_t *dev,
			 uvc_device_info_t *info,
			 uvc_streaming_interface_t *stream_if,
			 const unsigned char *block, size_t block_size);
uvc_error_t uvc_parse_vs_format_uncompressed(uvc_streaming_interface_t *stream_if,
					     const unsigned char *block,
					     size_t block_size);
uvc_error_t uvc_parse_vs_format_mjpeg(uvc_streaming_interface_t *stream_if,
					     const unsigned char *block,
					     size_t block_size);
uvc_error_t uvc_parse_vs_frame_uncompressed(uvc_streaming_interface_t *stream_if,
					    const unsigned char *block,
					    size_t block_size);
uvc_error_t uvc_parse_vs_frame_format(uvc_streaming_interface_t *stream_if,
					    const unsigned char *block,
					    size_t block_size);
uvc_error_t uvc_parse_vs_frame_frame(uvc_streaming_interface_t *stream_if,
					    const unsigned char *block,
					    size_t block_size);
uvc_error_t uvc_parse_vs_input_header(uvc_streaming_interface_t *stream_if,
				      const unsigned char *block,
				      size_t block_size);

void LIBUSB_CALL _uvc_status_callback(struct libusb_transfer *transfer);

/** @internal
 * @brief Test whether the specified USB device has been opened as a UVC device
 * @ingroup device
 *
 * @param ctx Context in which to search for the UVC device
 * @param usb_dev USB device to find
 * @return true if the device is open in this context
 */
bool uvc_already_open(uvc_context_t *ctx, struct libusb_device *usb_dev) {
  uvc_device_handle_t *devh;

  DL_FOREACH(ctx->openDevicesList, devh) {
    if (usb_dev == devh->dev->usb_dev)
      return true;
  }

  return false;
}

/** @brief Finds a camera identified by vendor, product and/or serial number
 * @ingroup device
 *
 * @param[in] ctx UVC context in which to search for the camera
 * @param[out] dev Reference to the camera, or NULL if not found
 * @param[in] vid Vendor ID number, optional
 * @param[in] pid Product ID number, optional
 * @param[in] sn Serial number or NULL
 * @return Error finding device or UVC_SUCCESS
 */
uvc_error_t uvc_find_device(
    uvc_context_t *ctx, uvc_device_t **dev,
    int vid, int pid, const char *sn) {
  uvc_error_t ret = UVC_SUCCESS;

  uvc_device_t **list;
  uvc_device_t *test_dev;
  int dev_idx;
  int found_dev;

  UVC_ENTER();

  ret = uvc_get_device_listKitKat(ctx, &list);

  if (ret != UVC_SUCCESS) {
    UVC_EXIT(ret);
    return ret;
  }

  dev_idx = 0;
  found_dev = 0;

  while (!found_dev && (test_dev = list[dev_idx++]) != NULL) {
    uvc_device_descriptor_t *desc;

    if (uvc_get_device_descriptor(test_dev, &desc) != UVC_SUCCESS)
      continue;

    if ((!vid || desc->idVendor == vid)
        && (!pid || desc->idProduct == pid)
        && (!sn || (desc->serialNumber && !strcmp(desc->serialNumber, sn))))
      found_dev = 1;

    uvc_free_device_descriptor(desc);
  }

  if (found_dev)
    uvc_ref_device(test_dev);

  uvc_free_device_list(list, 1);

  if (found_dev) {
    *dev = test_dev;
    UVC_EXIT(UVC_SUCCESS);
    return UVC_SUCCESS;
  } else {
    UVC_EXIT(UVC_ERROR_NO_DEVICE);
    return UVC_ERROR_NO_DEVICE;
  }
}

/** @brief Finds all cameras identified by vendor, product and/or serial number
 * @ingroup device
 *
 * @param[in] ctx UVC context in which to search for the camera
 * @param[out] devs List of matching cameras
 * @param[in] vid Vendor ID number, optional
 * @param[in] pid Product ID number, optional
 * @param[in] sn Serial number or NULL
 * @return Error finding device or UVC_SUCCESS
 */
uvc_error_t uvc_find_devices(
    uvc_context_t *ctx, uvc_device_t ***devs,
    int vid, int pid, const char *sn) {
  uvc_error_t ret = UVC_SUCCESS;

  uvc_device_t **list;
  uvc_device_t *test_dev;
  int dev_idx;
  int found_dev;

  uvc_device_t **list_internal;
  int num_uvc_devices;

  UVC_ENTER();

  ret = uvc_get_device_listKitKat(ctx, &list);

  if (ret != UVC_SUCCESS) {
    UVC_EXIT(ret);
    return ret;
  }

  num_uvc_devices = 0;
  dev_idx = 0;
  found_dev = 0;

  list_internal = (uvc_device_t**)malloc(sizeof(*list_internal));
  *list_internal = NULL;

  while ((test_dev = list[dev_idx++]) != NULL) {
    uvc_device_descriptor_t *desc;

    if (uvc_get_device_descriptor(test_dev, &desc) != UVC_SUCCESS)
      continue;

    if ((!vid || desc->idVendor == vid)
        && (!pid || desc->idProduct == pid)
        && (!sn || (desc->serialNumber && !strcmp(desc->serialNumber, sn)))) {
      found_dev = 1;
      uvc_ref_device(test_dev);

      num_uvc_devices++;
      list_internal = (uvc_device_t**) realloc(list_internal, (num_uvc_devices + 1) * sizeof(*list_internal));

      list_internal[num_uvc_devices - 1] = test_dev;
      list_internal[num_uvc_devices] = NULL;
    }

    uvc_free_device_descriptor(desc);
  }

  uvc_free_device_list(list, 1);

  if (found_dev) {
    *devs = list_internal;
    UVC_EXIT(UVC_SUCCESS);
    return UVC_SUCCESS;
  } else {
    UVC_EXIT(UVC_ERROR_NO_DEVICE);
    return UVC_ERROR_NO_DEVICE;
  }
}

/** @brief Get the number of the bus to which the device is attached
 * @ingroup device
 */
uint8_t uvc_get_bus_number(uvc_device_t *dev) {
  return libusb_get_bus_number(dev->usb_dev);
}

/** @brief Get the number assigned to the device within its bus
 * @ingroup device
 */
uint8_t uvc_get_device_address(uvc_device_t *dev) {
  return libusb_get_device_address(dev->usb_dev);
}

/** @brief Open a UVC device
 * @ingroup device
 *
 * @param dev Device to open
 * @param[out] devh Handle on opened device
 * @return Error opening device or SUCCESS
 */
uvc_error uvc_open(uvc_device *dev, /*optional*/ UsbInterfaceManager* pUsbInterfaceManager, uvc_device_handle **devh)
    {
    uvc_error ret;
    struct libusb_device_handle *usb_devh = NULL;
    uvc_device_handle *internal_devh;
    struct libusb_device_descriptor desc;

    UVC_ENTER();

    if (isValidFd(dev->fdJava))
        {
        // Callee will dup the handle and copy the path
        ret = dev->open(&usb_devh);
        if (!ret)
            {
            // For good measure, reset the device to help correct for previous errors. May not be
            // necessary (we did *just* open), but would seem harmless.
            if (dev->hasFeature(CamCompatFeature::AvoidLibUsbResetDevice))
                {
                UVC_DEBUG("libusb_reset_device() not called");
                }
            else
                {
                UVC_DEBUG("calling libusb_reset_device()...");
                ret = uvc_error(libusb_reset_device(usb_devh));
                if (!ret)
                    {
                    // all is well
                    }
                else
                    UVC_ERROR("libusb_reset_device() failed: rc=%d(%s)", ret, uvcErrorName(ret));
                UVC_DEBUG("...libusb_reset_device() called");
                }
            }
        }
    else
        {
        ret = uvc_error(LIBUSB_ERROR_INVALID_PARAM);
        }
    UVC_DEBUG("libusb_open() = %d", ret);

    if (ret != UVC_SUCCESS)
        {
        UVC_EXIT(ret);
        return ret;
        }

    internal_devh = new uvc_device_handle(dev, pUsbInterfaceManager, usb_devh);

    ret = uvc_get_device_info(dev, &(internal_devh->info));

    if (ret != UVC_SUCCESS)
        goto fail2;

    UVC_DEBUG("claiming control interface %d", internal_devh->info->ctrl_if.bInterfaceNumber);
    ret = internal_devh->claimInterface(internal_devh->info->ctrl_if.bInterfaceNumber);
    if (ret != UVC_SUCCESS)
        goto fail;

    libusb_get_device_descriptor(dev->usb_dev, &desc);

    if (internal_devh->info->ctrl_if.bEndpointAddress)
        {
        internal_devh->status_xfer = libusb_alloc_transfer(0);
        if (!internal_devh->status_xfer)
            {
            ret = UVC_ERROR_NO_MEM;
            goto fail;
            }

        libusb_fill_interrupt_transfer(internal_devh->status_xfer,
                usb_devh,
                internal_devh->info->ctrl_if.bEndpointAddress,
                internal_devh->status_buf,
                sizeof(internal_devh->status_buf),
                _uvc_status_callback, internal_devh,
                0);
        ret = uvc_error_t(libusb_submit_transfer(internal_devh->status_xfer));
        if (ret)
            {
            UVC_ERROR("uvc: device has a status interrupt endpoint, but unable to read from it");
            goto fail;
            }
        }

    DL_APPEND(dev->ctx->openDevicesList, internal_devh);
    internal_devh->onOpenDevicesList = true;
    *devh = internal_devh;

    UVC_EXIT(ret);
    return ret;

fail:
fail2:

    ::releaseRef(internal_devh);
    UVC_EXIT(ret);
    return ret;
    }

/**
 * @internal
 * @brief Parses the complete device descriptor for a device
 * @ingroup device
 * @note Free *info with uvc_free_device_info when you're done
 *
 * @param dev Device to parse descriptor for
 * @param info Where to store a pointer to the new info struct
 */
uvc_error uvc_get_device_info(uvc_device_t *dev, uvc_device_info_t **info)
    {
    uvc_error_t ret;
    uvc_device_info_t *internal_info;

    UVC_ENTER();

    internal_info = (uvc_device_info_t *)calloc(1, sizeof(*internal_info));
    if (!internal_info)
        {
        UVC_EXIT(UVC_ERROR_NO_MEM);
        return UVC_ERROR_NO_MEM;
        }

    if (libusb_get_config_descriptor(dev->usb_dev, 0, &(internal_info->config)) != 0)
        {
        free(internal_info);
        UVC_EXIT(UVC_ERROR_IO);
        return UVC_ERROR_IO;
        }

    ret = uvc_scan_control(dev, internal_info);
    if (ret != UVC_SUCCESS)
        {
        uvc_free_device_info(internal_info);
        UVC_EXIT(ret);
        return ret;
        }

    *info = internal_info;

    UVC_EXIT(ret);
    return ret;
    }

/**
 * @internal
 * @brief Frees the device descriptor for a device
 * @ingroup device
 *
 * @param info Which device info block to free
 */
void uvc_free_device_info(uvc_device_info_t *info) {
  uvc_input_terminal_t *input_term, *input_term_tmp;
  uvc_output_terminal_t *output_term, *output_term_tmp;
  uvc_processing_unit_t *proc_unit, *proc_unit_tmp;
  uvc_extension_unit_t *ext_unit, *ext_unit_tmp;

  uvc_streaming_interface_t *stream_if, *stream_if_tmp;
  uvc_format_desc_t *format, *format_tmp;
  uvc_frame_desc_t *frame, *frame_tmp;

  UVC_ENTER();

  DL_FOREACH_SAFE(info->ctrl_if.input_term_descs, input_term, input_term_tmp) {
    DL_REMOVE(info->ctrl_if.input_term_descs, input_term);
    free(input_term);
  }

  DL_FOREACH_SAFE(info->ctrl_if.output_term_descs, output_term, output_term_tmp) {
    DL_REMOVE(info->ctrl_if.output_term_descs, output_term);
    free(output_term);
  }

  DL_FOREACH_SAFE(info->ctrl_if.processing_unit_descs, proc_unit, proc_unit_tmp) {
    DL_REMOVE(info->ctrl_if.processing_unit_descs, proc_unit);
    free(proc_unit);
  }

  DL_FOREACH_SAFE(info->ctrl_if.extension_unit_descs, ext_unit, ext_unit_tmp) {
    DL_REMOVE(info->ctrl_if.extension_unit_descs, ext_unit);
    free(ext_unit);
  }

  DL_FOREACH_SAFE(info->stream_ifs, stream_if, stream_if_tmp) {
    DL_FOREACH_SAFE(stream_if->format_descs, format, format_tmp) {
      DL_FOREACH_SAFE(format->frame_descs, frame, frame_tmp) {
        if (frame->rgIntervals)
          free(frame->rgIntervals);

        DL_REMOVE(format->frame_descs, frame);
        free(frame);
      }

      DL_REMOVE(stream_if->format_descs, format);
      free(format);
    }

    DL_REMOVE(info->stream_ifs, stream_if);
    free(stream_if->bmaControls);	// XXX
    free(stream_if);
  }

  if (info->config)
    libusb_free_config_descriptor(info->config);

  free(info);

  UVC_EXIT_VOID();
}

/**
 * @brief Get a descriptor that contains the general information about
 * a device
 * @ingroup device
 *
 * Free *desc with uvc_free_device_descriptor when you're done.
 *
 * @param dev Device to fetch information about
 * @param[out] desc Descriptor structure
 * @return Error if unable to fetch information, else SUCCESS
 */
uvc_error_t uvc_get_device_descriptor(
    uvc_device_t *dev,
    uvc_device_descriptor_t **desc) {
  uvc_device_descriptor_t *desc_internal;
  struct libusb_device_descriptor usb_desc;
  struct libusb_device_handle *usb_devh = NULL;
  uvc_error_t ret;

  UVC_ENTER();

  ret = uvc_error_t(libusb_get_device_descriptor(dev->usb_dev, &usb_desc));

  if (ret != UVC_SUCCESS) {
    UVC_EXIT(ret);
    return ret;
  }

  desc_internal = (uvc_device_descriptor_t *)calloc(1, sizeof(*desc_internal));
  desc_internal->idVendor = usb_desc.idVendor;
  desc_internal->idProduct = usb_desc.idProduct;

  // Callee will dup the handle and copy the path
  if (libusb_open(dev->usb_dev, &usb_devh, dev->fdJava, dev->szUsbPath) == 0) {
    unsigned char buf[64];

    int bytes = libusb_get_string_descriptor_ascii(usb_devh, usb_desc.iSerialNumber, buf, sizeof(buf));
    if (bytes > 0)
      desc_internal->serialNumber = strdup((const char*) buf);

    bytes = libusb_get_string_descriptor_ascii(usb_devh, usb_desc.iManufacturer, buf, sizeof(buf));
    if (bytes > 0)
      desc_internal->manufacturer = strdup((const char*) buf);

    bytes = libusb_get_string_descriptor_ascii(usb_devh, usb_desc.iProduct, buf, sizeof(buf));
    if (bytes > 0)
      desc_internal->product = strdup((const char*) buf);

    libusb_close(usb_devh);
  } else {
    UVC_DEBUG("can't open device %04x:%04x, not fetching serial etc.", usb_desc.idVendor, usb_desc.idProduct);
  }

  *desc = desc_internal;

  UVC_EXIT(ret);
  return ret;
}

/**
 * @brief Frees a device descriptor created with uvc_get_device_descriptor
 * @ingroup device
 *
 * @param desc Descriptor to free
 */
void uvc_free_device_descriptor(
    uvc_device_descriptor_t *desc) {
  UVC_ENTER();

  if (desc->serialNumber)
    free((void*) desc->serialNumber);

  if (desc->manufacturer)
    free((void*) desc->manufacturer);

  if (desc->product)
    free((void*) desc->product);

  free(desc);

  UVC_EXIT_VOID();
}

uvc_error uvc_is_usb_device_compatible(uvc_context_t *ctx, libusb_device *pLibUsbDevice, /*OUT*/ bool *pIsCompatible)
    {
    UVC_ENTER();
    *pIsCompatible = false;

    bool got_interface = false;
    libusb_config_descriptor *config;
    uvc_error rc = (uvc_error)libusb_get_config_descriptor(pLibUsbDevice, 0, &config);
    if (!rc)
        {
        libusb_device_descriptor desc;
        rc = (uvc_error)libusb_get_device_descriptor(pLibUsbDevice, &desc);
        if (!rc)
            {
            for (int iIntf = 0; !got_interface && iIntf < config->bNumInterfaces; ++iIntf)
                {
                const libusb_interface* pIntf = &config->interface[iIntf];
                for (int iAltSetting = 0; !got_interface && iAltSetting < pIntf->num_altsetting; ++iAltSetting)
                    {
                    const libusb_interface_descriptor* pIntfDesc = &pIntf->altsetting[iAltSetting];

                    /* Video, Streaming */
                    if (pIntfDesc->bInterfaceClass == LIBUSB_CLASS_VIDEO && pIntfDesc->bInterfaceSubClass == 2)
                        {
                        got_interface = true;
                        }
                    }
                }
            }
        else
            {
            LOGE("failed: libusb_get_device_descriptor()=%d", rc);
            }

        libusb_free_config_descriptor(config);
        }
    else
        {
        LOGE("failed: libusb_get_config_descriptor()=%d", rc);
        }

    *pIsCompatible = got_interface;
    UVC_EXIT(rc);
    return rc;
    }

uvc_error uvc_create_uvc_device(uvc_context_t *ctx, /*optional*/ libusb_device *pUsbDevice, /*OUT*/ uvc_device **ppUvcDevice)
    {
    UVC_ENTER();
    *ppUvcDevice = nullptr;

    uvc_error rc = UVC_SUCCESS;
    uvc_device* puvcDevice = new uvc_device(ctx, pUsbDevice);
    if (puvcDevice)
        {
        *ppUvcDevice = puvcDevice;
        }
    else
        rc = (uvc_error)outOfMemory();

    UVC_EXIT(rc);
    return (uvc_error)rc;
    }

uvc_error uvc_device_from_libusb_device(uvc_context_t* ctx, libusb_device *pUsbDevice, /*OUT*/ uvc_device** ppUvcDevice) // a classic API
    {
    UVC_ENTER();
    *ppUvcDevice = nullptr;

    int busnum = pUsbDevice->bus_number;
    int devnum = (int)(pUsbDevice->session_data & 0xFF);
    LOGD("uvc_device_from_libusb_device: bus=%d dev=%d", busnum, devnum);

    bool isCompatible = false;
    uvc_error_t rc = uvc_is_usb_device_compatible(ctx, pUsbDevice, &isCompatible);
    if (isCompatible)
        {
        LOGI("found UVC USB device: bus=%d dev=%d", busnum, devnum);
        UVC_DEBUG("    UVC: bus=%d dev=%d", busnum, devnum);

        rc = uvc_create_uvc_device(ctx, pUsbDevice, ppUvcDevice);
        }
    else
        {
        LOGI("found non-UVC USB device: bus=%d dev=%d", busnum, devnum);
        UVC_DEBUG("non-UVC: bus=%d dev=%d", busnum, devnum);
        if (!rc)
            {
            rc = UVC_ERROR_INVALID_DEVICE;
            }
        }

    UVC_EXIT(rc);
    return rc;
    }

/**
 * @brief Get a list of the UVC devices attached to the system
 * @ingroup device
 *
 * @note Free the list with uvc_free_device_list when you're done.
 *
 * @param ctx UVC context in which to list devices
 * @param prgpUvcDeviceResult List of uvc_device pointers (NULL terminated)
 * @return Error if unable to list devices, else SUCCESS
 */
uvc_error_t uvc_get_device_listKitKat(uvc_context_t* ctx, uvc_device_t * /*array*/* /*out*/* prgpUvcDeviceResult)
    {
    UVC_ENTER();
    *prgpUvcDeviceResult = NULL;

    libusb_device** rgpUsbDevice;
    int cUsbDevice = libusb_get_device_list_kitkat(ctx->pLibUsbContext, &rgpUsbDevice); //

    if (cUsbDevice < 0)
        {
        UVC_RETURN(uvc_originate_err(UVC_ERROR_IO));
        }
    else if (cUsbDevice == 0)
        {
        LOGI("no usb devices attached");
        }

    uvc_device** rgpUvcDevice = typedMalloc<uvc_device*>();
    if (NULL == rgpUvcDevice) outOfMemory();
    *rgpUvcDevice = NULL;

    int cUvcDevice = 0;
    int iUsbDevice = -1;
    libusb_device *pUsbDevice;
    while ((pUsbDevice = rgpUsbDevice[++iUsbDevice]) != NULL)
        {
        uvc_device* puvcDevice = nullptr;
        uvc_device_from_libusb_device(ctx, pUsbDevice, &puvcDevice);
        if (puvcDevice)
            {
            cUvcDevice++;
            rgpUvcDevice = typedRealloc(rgpUvcDevice, (cUvcDevice + 1) * sizeof(*rgpUvcDevice));
            rgpUvcDevice[cUvcDevice - 1] = puvcDevice;
            rgpUvcDevice[cUvcDevice] = NULL;
            }
        }

    libusb_free_device_list(rgpUsbDevice, 1);

    *prgpUvcDeviceResult = rgpUvcDevice;

    UVC_EXIT(UVC_SUCCESS);
    return UVC_SUCCESS;
    }

/**
 * @brief Frees a list of device structures created with uvc_get_device_list.
 * @ingroup device
 *
 * @param list Device list to free
 * @param unref_devices Decrement the reference counter for each device
 * in the list, and destroy any entries that end up with zero references
 */
void uvc_free_device_list(uvc_device_t **list, uint8_t unref_devices) {
  uvc_device_t *dev;
  int dev_idx = 0;

  UVC_ENTER();

  if (unref_devices) {
    while ((dev = list[dev_idx++]) != NULL) {
      uvc_unref_device(dev);
    }
  }

  free(list);

  UVC_EXIT_VOID();
}

/**
 * @brief Get the uvc_device_t corresponding to an open device
 * @ingroup device
 *
 * @note Unref the uvc_device_t when you're done with it
 *
 * @param devh Device handle to an open UVC device
 */
uvc_device_t *uvc_get_device(uvc_device_handle_t *devh) {
  uvc_ref_device(devh->dev);
  return devh->dev;
}

/**
 * @brief Get the underlying libusb device handle for an open device
 * @ingroup device
 *
 * This can be used to access other interfaces on the same device, e.g.
 * a webcam microphone.
 *
 * @note The libusb device handle is only valid while the UVC device is open;
 * it will be invalidated upon calling uvc_close.
 *
 * @param devh UVC device handle to an open device
 */
libusb_device_handle *uvc_get_libusb_handle(uvc_device_handle_t *devh) {
  return devh->usb_devh;
}

/**
 * @brief Get camera terminal descriptor for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list, but iterating through
 * it will make it no longer the camera terminal
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_input_terminal_t *uvc_get_camera_terminal(uvc_device_handle_t *devh) {
  const uvc_input_terminal_t *term = uvc_get_input_terminals(devh);
  while(term != NULL) {
    if (term->wTerminalType == UVC_ITT_CAMERA) {
      break;
    }
    else {
      term = term->next;
    }
  }
  return term;
}


/**
 * @brief Get input terminal descriptors for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list. Iterate through
 *       it by using the 'next' pointers.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_input_terminal_t *uvc_get_input_terminals(uvc_device_handle_t *devh) {
  return devh->info->ctrl_if.input_term_descs;
}

/**
 * @brief Get output terminal descriptors for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list. Iterate through
 *       it by using the 'next' pointers.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_output_terminal_t *uvc_get_output_terminals(uvc_device_handle_t *devh) {
	return devh->info->ctrl_if.output_term_descs;
}

/**
 * @brief Get selector unit descriptors for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list. Iterate through
 *       it by using the 'next' pointers.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_selector_unit_t *uvc_get_selector_units(uvc_device_handle_t *devh) {
  return devh->info->ctrl_if.selector_unit_descs;
}

/**
 * @brief Get processing unit descriptors for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list. Iterate through
 *       it by using the 'next' pointers.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_processing_unit_t *uvc_get_processing_units(uvc_device_handle_t *devh) {
  return devh->info->ctrl_if.processing_unit_descs;
}

/**
 * @brief Get extension unit descriptors for the open device.
 *
 * @note Do not modify the returned structure.
 * @note The returned structure is part of a linked list. Iterate through
 *       it by using the 'next' pointers.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_extension_unit_t *uvc_get_extension_units(uvc_device_handle_t *devh) {
  return devh->info->ctrl_if.extension_unit_descs;
}

/**
 * @brief Increment the reference count for a device
 * @ingroup device
 *
 * @param dev Device to reference
 */
void uvc_ref_device(uvc_device_t *dev) {
  UVC_ENTER();
  if (dev)
      {
      dev->addRef();
      }
  UVC_EXIT_VOID();
}

/**
 * @brief Decrement the reference count for a device
 * @ingropu device
 * @note If the count reaches zero, the device will be discarded
 *
 * @param dev Device to unreference
 */
void uvc_unref_device(uvc_device_t *dev)
    {
    UVC_ENTER();
    ::releaseRef(dev);
    UVC_EXIT_VOID();
    }

/** @internal
 * Claim a UVC interface, detaching the kernel driver if necessary.
 * @ingroup device
 *
 * @param devh UVC device handle
 * @param idx UVC interface index
 */
uvc_error uvc_device_handle::uvc_claim_if(uvc_device_handle_t *devh, int idx)
    {
    UVC_ENTER("uvc_claim_if(idx=%d)", idx);
    uvc_error ret = UVC_SUCCESS;

    if (useAutoDetach)
        {
        ret = (uvc_error)libusb_claim_interface(devh->usb_devh, idx);
        }
    else
        {
        if (!manuallyDetached)
            {
            /* Manual detach. Tell libusb to detach any active kernel drivers. libusb will keep track of whether
             * it found a kernel driver for this interface. */
            manuallyDetached = true;
            ret = (uvc_error)libusb_detach_kernel_driver(devh->usb_devh, idx);
            if (ret == UVC_SUCCESS || ret == (uvc_error)LIBUSB_ERROR_NOT_FOUND || ret == (uvc_error)LIBUSB_ERROR_NOT_SUPPORTED)
                {
                ret = UVC_SUCCESS;
                }
            else
                UVC_ERROR("not claiming interface %d: unable to detach kernel driver (%s)", idx, uvc_strerror(ret));
            }
        if (ret == UVC_SUCCESS)
            {
            ret = (uvc_error)libusb_claim_interface(devh->usb_devh, idx); // libusb_claim_interface is idempotent
            }
        }

    UVC_EXIT(ret);
    return ret;
    }

/** @internal
 * Release a UVC interface.
 * @ingroup device
 *
 * @param devh UVC device handle
 * @param idx UVC interface index
 */
uvc_error uvc_device_handle::uvc_release_if(uvc_device_handle *devh, int idx)
    {
    UVC_ENTER("idx=%d", idx);
	uvc_error ret = (uvc_error)libusb_release_interface(devh->usb_devh, idx);
    if (UVC_SUCCESS == ret)
        {
        if (useAutoDetach)
            {
            // libusb automatically attach/detach kernel driver on supported platforms and nothing to do here
            }
        else
            {
            if (manuallyDetached)
                {
                /* Reattach any kernel drivers that were disabled when we claimed this interface. Question: are there
                 * certain libusb_release_interface() errors after which we should try this anyway? */
                manuallyDetached = false;
                ret = (uvc_error)libusb_attach_kernel_driver(devh->usb_devh, idx);
                if (ret == UVC_SUCCESS)
                    {
                    UVC_DEBUG("reattached kernel driver to interface %d", idx);
                    }
                else if (ret == (uvc_error)LIBUSB_ERROR_NOT_FOUND || ret == (uvc_error)LIBUSB_ERROR_NOT_SUPPORTED)
                    {
                    ret = UVC_SUCCESS;  /* NOT_FOUND and NOT_SUPPORTED are OK: nothing to do */
                    }
                else
                    LOGE("error reattaching kernel driver to interface %d: %s", idx, uvcErrorName(ret));
                }
            }
        }
    else
        {
        LOGE("libusb_release_interface() failed: %d: %s", ret, uvcErrorName(ret));
        }

    UVC_EXIT(ret);
    return uvc_error(ret);
    }

/** @internal
 * Find a device's VideoControl interface and process its descriptor
 * @ingroup device
 */
uvc_error_t uvc_scan_control(uvc_device_t *dev, uvc_device_info_t *info) {
  const struct libusb_interface_descriptor *if_desc;
  uvc_error_t parse_ret, ret;
  int interface_idx;
  const unsigned char *buffer;
  size_t buffer_left, block_size;

  UVC_ENTER();

  ret = UVC_SUCCESS;
  if_desc = NULL;

  for (interface_idx = 0; interface_idx < info->config->bNumInterfaces; ++interface_idx) {
    if_desc = &info->config->interface[interface_idx].altsetting[0];

	// select first found Video control
    if (if_desc->bInterfaceClass == 14 && if_desc->bInterfaceSubClass == 1) // Video, Control
      break;

    if_desc = NULL;
  }

  if (if_desc == NULL) {
    UVC_EXIT(UVC_ERROR_INVALID_DEVICE);
    return UVC_ERROR_INVALID_DEVICE;
  }

  info->ctrl_if.bInterfaceNumber = (uint8_t)interface_idx;
  if (if_desc->bNumEndpoints != 0) {
    info->ctrl_if.bEndpointAddress = if_desc->endpoint[0].bEndpointAddress;
  }

  buffer = if_desc->extra;
  buffer_left = if_desc->extra_length;

  while (buffer_left >= 3) { // parseX needs to see buf[0,2] = length,type
    block_size = buffer[0];
    parse_ret = uvc_parse_vc(dev, info, buffer, block_size);

    if (parse_ret != UVC_SUCCESS) {
      ret = parse_ret;
      break;
    }

    buffer_left -= block_size;
    buffer += block_size;
  }

  UVC_EXIT(ret);
  return ret;
}

/** @internal
 * @brief Parse a VideoControl header.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_header(uvc_device_t *dev,
				uvc_device_info_t *info,
				const unsigned char *block, size_t block_size) {
  size_t i;
  uvc_error_t scan_ret, ret = UVC_SUCCESS;

  UVC_ENTER();

  /*
  int uvc_version;
  uvc_version = (block[4] >> 4) * 1000 + (block[4] & 0x0f) * 100
    + (block[3] >> 4) * 10 + (block[3] & 0x0f);
  */

  info->ctrl_if.bcdUVC = SW_TO_SHORT(&block[3]);

  switch (info->ctrl_if.bcdUVC) {
  case 0x0100:
  case 0x010a:
    info->ctrl_if.dwClockFrequency = DW_TO_INT(block + 7);
    break;
  case 0x0110:
  case 0x0150: // UVC 1.5
    break;
  default:
    UVC_EXIT(UVC_ERROR_NOT_SUPPORTED);
    return UVC_ERROR_NOT_SUPPORTED;
  }

  for (i = 12; i < block_size; ++i) {
    scan_ret = uvc_scan_streaming(dev, info, block[i]);
    if (scan_ret != UVC_SUCCESS) {
      ret = scan_ret;
      break;
    }
  }

  UVC_EXIT(ret);
  return ret;
}

/** @internal
 * @brief Parse a VideoControl input terminal.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_input_terminal(uvc_device_t *dev,
					uvc_device_info_t *info,
					const unsigned char *block, size_t block_size) {
    UVC_ENTER();
    // UVC Spec, Table 3-6 Camera Terminal Descriptor

	/* only supporting camera-type input terminals */
	if (SW_TO_SHORT(&block[4]) != UVC_ITT_CAMERA) {
		UVC_EXIT(UVC_SUCCESS);
		return UVC_SUCCESS;
	}

	uvc_input_terminal_t *term = (uvc_input_terminal_t *) calloc(1, sizeof(*term));

	term->bTerminalID = block[3];
	term->wTerminalType = (uvc_it_type)SW_TO_SHORT(&block[4]);
	term->wObjectiveFocalLengthMin = SW_TO_SHORT(&block[8]);
	term->wObjectiveFocalLengthMax = SW_TO_SHORT(&block[10]);
	term->wOcularFocalLength = SW_TO_SHORT(&block[12]);
	term->request = (term->bTerminalID << 8) | info->ctrl_if.bInterfaceNumber;

    int cbBmControls = block[14];
	for (int i = 14 + cbBmControls; i >= 15; --i)
        {
        term->bmControls = block[i] + (term->bmControls << 8);
        }

	DL_APPEND(info->ctrl_if.input_term_descs, term);

	UVC_EXIT(UVC_SUCCESS);
	return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a output terminal.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_output_terminal(uvc_device_t *dev,
		uvc_device_info_t *info, const unsigned char *block, size_t block_size) {
	uvc_output_terminal_t *term;
	size_t i;

	UVC_ENTER();

	/* only supporting display-type input terminals */
	if (SW_TO_SHORT(&block[4]) != UVC_OTT_DISPLAY) {
		UVC_EXIT(UVC_SUCCESS);
		return UVC_SUCCESS;
	}

	term = (uvc_output_terminal_t*)calloc(1, sizeof(*term));

	term->bTerminalID = block[3];
	term->wTerminalType = (uvc_ot_type)SW_TO_SHORT(&block[4]);
	term->bAssocTerminal = block[6];
	term->bSourceID = block[7];
	term->iTerminal = block[8];
	term->request = (term->bTerminalID << 8) | info->ctrl_if.bInterfaceNumber;
	// TODO depending on the wTerminalType

	DL_APPEND(info->ctrl_if.output_term_descs, term);

	UVC_EXIT(UVC_SUCCESS);
	return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoControl processing unit.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_processing_unit(uvc_device_t *dev,
					 uvc_device_info_t *info,
					 const unsigned char *block, size_t block_size) {
	uvc_processing_unit_t *unit;
	size_t i;

	UVC_ENTER();

	unit = (uvc_processing_unit_t *)calloc(1, sizeof(*unit));
	unit->bUnitID = block[3];
	unit->bSourceID = block[4];
	unit->request = (unit->bUnitID << 8) | info->ctrl_if.bInterfaceNumber;

	for (i = 7 + block[7]; i >= 8; --i)
		unit->bmControls = block[i] + (unit->bmControls << 8);

	DL_APPEND(info->ctrl_if.processing_unit_descs, unit);

	UVC_EXIT(UVC_SUCCESS);
	return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoControl selector unit.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_selector_unit(uvc_device_t *dev,
					 uvc_device_info_t *info,
					 const unsigned char *block, size_t block_size) {
  uvc_selector_unit_t *unit;
  size_t i;

  UVC_ENTER();

  unit = (uvc_selector_unit_t *)calloc(1, sizeof(*unit));
  unit->bUnitID = block[3];

  DL_APPEND(info->ctrl_if.selector_unit_descs, unit);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoControl extension unit.
 * @ingroup device
 */
uvc_error_t uvc_parse_vc_extension_unit(uvc_device_t *dev,
					uvc_device_info_t *info,
					const unsigned char *block, size_t block_size) {
  uvc_extension_unit_t *unit = (uvc_extension_unit_t *)calloc(1, sizeof(*unit));
  const uint8_t *start_of_controls;
  int size_of_controls, num_in_pins;
  int i;

  UVC_ENTER_VERBOSE();

  unit->bUnitID = block[3];
  memcpy(unit->guidExtensionCode, &block[4], 16);

  num_in_pins = block[21];
  size_of_controls = block[22 + num_in_pins];
  start_of_controls = &block[23 + num_in_pins];

  unit->bmControls = 0;	// XXX paranoia
  for (i = size_of_controls - 1; i >= 0; --i)
    unit->bmControls = start_of_controls[i] + (unit->bmControls << 8);

  DL_APPEND(info->ctrl_if.extension_unit_descs, unit);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * Process a single VideoControl descriptor block
 * @ingroup device
 */
uvc_error_t uvc_parse_vc(
    uvc_device_t *dev,
    uvc_device_info_t *info,
    const unsigned char *block, size_t block_size) {
  int descriptor_subtype;
  uvc_error_t ret = UVC_SUCCESS;

  UVC_ENTER_VERBOSE();

  if (block[1] != LIBUSB_DT_CS_INTERFACE) { // not a CS_INTERFACE descriptor??
    UVC_EXIT(UVC_SUCCESS);
    return UVC_SUCCESS; // UVC_ERROR_INVALID_DEVICE;
  }

  descriptor_subtype = block[2];

  switch (descriptor_subtype) {
  case UVC_VC_HEADER:
    ret = uvc_parse_vc_header(dev, info, block, block_size);
    break;
  case UVC_VC_INPUT_TERMINAL:
    ret = uvc_parse_vc_input_terminal(dev, info, block, block_size);
    break;
  case UVC_VC_OUTPUT_TERMINAL:
    break;
  case UVC_VC_SELECTOR_UNIT:
    ret = uvc_parse_vc_selector_unit(dev, info, block, block_size);
    break;
  case UVC_VC_PROCESSING_UNIT:
    ret = uvc_parse_vc_processing_unit(dev, info, block, block_size);
    break;
  case UVC_VC_EXTENSION_UNIT:
    ret = uvc_parse_vc_extension_unit(dev, info, block, block_size);
    break;
  default:
    ret = uvc_originate_err(UVC_ERROR_INVALID_DEVICE);
  }

  UVC_EXIT(ret);
  return ret;
}

/** @internal
 * Process a VideoStreaming interface
 * @ingroup device
 */
uvc_error_t uvc_scan_streaming(uvc_device_t *dev,
                               uvc_device_info_t *info,
                               int interface_idx)
    {
    const struct libusb_interface_descriptor *if_desc;
    const unsigned char *buffer;
    size_t buffer_left, block_size;
    uvc_error_t ret, parse_ret;
    uvc_streaming_interface_t *stream_if;

    UVC_ENTER();
    ret = UVC_SUCCESS;

    if_desc = &(info->config->interface[interface_idx].altsetting[0]);
    buffer = if_desc->extra;
    buffer_left = (size_t)if_desc->extra_length;

    // XXX some devices have their format descriptions after the endpoint descriptor
    if (UNLIKELY(!buffer || !buffer_left))
        {
        if (if_desc->bNumEndpoints && if_desc->endpoint)
            {
            // try to use extra data in endpoint[0]
            buffer = if_desc->endpoint[0].extra;
            buffer_left = (size_t)if_desc->endpoint[0].extra_length;
            }
        }

    stream_if = (uvc_streaming_interface_t *) calloc(1, sizeof(*stream_if));
    stream_if->parent = info;
    stream_if->bInterfaceNumber = if_desc->bInterfaceNumber;
    DL_APPEND(info->stream_ifs, stream_if);

    if (LIKELY(buffer_left >= 3))
        {
        while (buffer_left >= 3)
            {
            block_size = buffer[0];
            parse_ret = uvc_parse_vs(dev, info, stream_if, buffer, block_size);

            if (parse_ret != UVC_SUCCESS)
                {
                ret = parse_ret;
                break;
                }

            buffer_left -= block_size;
            buffer += block_size;
            }
        }
    else
        UVC_DEBUG("This VideoStreaming interface has no extra data");

    UVC_EXIT(ret);
    return ret;
    }

/** @internal
 * @brief Parse a VideoStreaming header block.
 * @ingroup device
 */
uvc_error_t uvc_parse_vs_input_header(uvc_streaming_interface_t *stream_if,
                                      const unsigned char *block,
                                      size_t block_size)
    {
    uvc_error rc = UVC_SUCCESS;
    UVC_ENTER();

    stream_if->bEndpointAddress = uint8_t(block[6] & 0x8f);
    stream_if->bTerminalLink = block[8];
    stream_if->bmInfo = block[7];    // XXX
    stream_if->bStillCaptureMethod = block[9];    // XXX
    stream_if->bTriggerSupport = block[10];    // XXX
    stream_if->bTriggerUsage = block[11];    // XXX
    free(stream_if->bmaControls);	// XXX // make idempotent
    stream_if->bmaControls = NULL; // XXX
    const uint8_t n = block[12];
    if (LIKELY(n))
        {
        const size_t p = (block_size - 13) / n;
        if (LIKELY(p))
            {
            uint64_t *bmaControls = (uint64_t *) calloc(p, sizeof(uint64_t));
            if (bmaControls)
                {
                stream_if->bmaControls = bmaControls;
                const uint8_t *bma;
                int pp, nn;
                for (pp = 1; pp <= p; pp++)
                    {
                    bma = &block[12 + pp * n];
                    for (nn = n - 1; nn >= 0; --nn)
                        {
                        *bmaControls = *bma-- + (*bmaControls << 8);
                        }
                    bmaControls++;
                    }
                }
            else
                rc = outOfMemory();
            }
        }
    UVC_EXIT(rc);
    return rc;
    }

/** @internal
 * @brief Parse a VideoStreaming uncompressed format block.
 * @ingroup device
 *
 * See "USB Device Class Definition for Video Devices: Uncompressed Payload", Table 3-1
 */
uvc_error_t uvc_parse_vs_format_uncompressed(uvc_streaming_interface_t *stream_if,
					     const unsigned char *block,
					     size_t block_size) {
  UVC_ENTER();

  uvc_format_desc_t *format = (uvc_format_desc_t *)calloc(1, sizeof(*format));

  format->pStreamingInterface = stream_if;
  format->bDescriptorSubtype = (uvc_vs_desc_subtype)block[2];
  format->bFormatIndex = block[3];
  //format->bmCapabilities = block[4];
  //format->bmFlags = block[5];
  memcpy(format->guidFormat, &block[5], 16);    // little endian!
  format->bBitsPerPixel = block[21];
  format->bDefaultFrameIndex = block[22];
  format->bAspectRatioX = block[23];
  format->bAspectRatioY = block[24];
  format->bmInterlaceFlags = block[25];
  format->bCopyProtect = block[26];

  DL_APPEND(stream_if->format_descs, format);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoStreaming frame format block.
 * @ingroup device
 */
uvc_error_t uvc_parse_vs_frame_format(uvc_streaming_interface_t *stream_if,
					     const unsigned char *block,
					     size_t block_size) {
  UVC_ENTER();

  uvc_format_desc_t *format = (uvc_format_desc_t *)calloc(1, sizeof(*format));

  format->pStreamingInterface = stream_if;
  format->bDescriptorSubtype = (uvc_vs_desc_subtype)block[2];
  format->bFormatIndex = block[3];
  format->bNumFrameDescriptors = block[4];
  memcpy(format->guidFormat, &block[5], 16);
  format->bBitsPerPixel = block[21];
  format->bDefaultFrameIndex = block[22];
  format->bAspectRatioX = block[23];
  format->bAspectRatioY = block[24];
  format->bmInterlaceFlags = block[25];
  format->bCopyProtect = block[26];
  format->bVariableSize = block[27];

  DL_APPEND(stream_if->format_descs, format);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoStreaming MJPEG format block.
 * @ingroup device
 */
uvc_error_t uvc_parse_vs_format_mjpeg(uvc_streaming_interface_t *stream_if,
					     const unsigned char *block,
					     size_t block_size) {
  UVC_ENTER();

  uvc_format_desc_t *format = (uvc_format_desc_t *)calloc(1, sizeof(*format));

  format->pStreamingInterface = stream_if;
  format->bDescriptorSubtype = (uvc_vs_desc_subtype)block[2];
  format->bFormatIndex = block[3];
  memcpy(format->fourccFormat, "MJPG", 4);
  format->bmFlags = block[5];
  format->bBitsPerPixel = 0;
  format->bDefaultFrameIndex = block[6];
  format->bAspectRatioX = block[7];
  format->bAspectRatioY = block[8];
  format->bmInterlaceFlags = block[9];
  format->bCopyProtect = block[10];

  DL_APPEND(stream_if->format_descs, format);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoStreaming uncompressed frame block.
 * @ingroup device
 *
 * Table 3-2 Frame Based Payload Video Frame Descriptors
 * USB Device Class Definition for Video Devices: Frame Based Payload
 */
uvc_error_t uvc_parse_vs_frame_frame(uvc_streaming_interface_t *stream_if,
					    const unsigned char *block,
					    size_t block_size) {
  uvc_format_desc_t *format;
  uvc_frame_desc_t *frame;

  const unsigned char *p;
  int i;

  UVC_ENTER_VERBOSE();

  format = stream_if->format_descs->prev;
  frame = (uvc_frame_desc_t *)calloc(1, sizeof(*frame));

  frame->parent = format;

  frame->bDescriptorSubtype = (uvc_vs_desc_subtype)block[2];
  frame->bFrameIndex = block[3];
  frame->bmCapabilities = block[4];
  frame->wWidth = block[5] + (block[6] << 8);
  frame->wHeight = block[7] + (block[8] << 8);
  frame->dwMinBitRate = DW_TO_INT(&block[9]);
  frame->dwMaxBitRate = DW_TO_INT(&block[13]);
  frame->dwDefaultFrameInterval = DW_TO_INT(&block[17]);
  frame->bFrameIntervalType = block[21];
  frame->dwBytesPerLine = DW_TO_INT(&block[22]);

  if (block[21] == 0) {
    frame->dwMinFrameInterval = DW_TO_INT(&block[26]);
    frame->dwMaxFrameInterval = DW_TO_INT(&block[30]);
    frame->dwFrameIntervalStep = DW_TO_INT(&block[34]);
  } else {
    frame->rgIntervals = (uint32_t *)calloc(block[21] + 1, sizeof(frame->rgIntervals[0]));
    p = &block[26];

    for (i = 0; i < block[21]; ++i) {
      frame->rgIntervals[i] = DW_TO_INT(p);
      p += 4;
    }
    frame->rgIntervals[block[21]] = 0;
  }

  DL_APPEND(format->frame_descs, frame);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * @brief Parse a VideoStreaming uncompressed frame block.
 * @ingroup device
 *
 * Table 3-2 Uncompressed Video Frame Descriptors
 * USB Device Class Definition for Video Devices: Uncompressed Payload
 */
uvc_error_t uvc_parse_vs_frame_uncompressed(uvc_streaming_interface_t *stream_if,
					    const unsigned char *block,
					    size_t block_size) {
  uvc_format_desc_t *format;
  uvc_frame_desc_t *frame;

  const unsigned char *p;
  int i;

  UVC_ENTER_VERBOSE();

  format = stream_if->format_descs->prev;
  frame = (uvc_frame_desc_t *)calloc(1, sizeof(*frame));

  frame->parent = format;

  int frameType = frame->bDescriptorSubtype = (uvc_vs_desc_subtype)block[2];
  frame->bFrameIndex = block[3];
  frame->bmCapabilities = block[4];
  frame->wWidth = block[5] + (block[6] << 8);
  frame->wHeight = block[7] + (block[8] << 8);
  frame->dwMinBitRate = DW_TO_INT(&block[9]);
  frame->dwMaxBitRate = DW_TO_INT(&block[13]);
  frame->dwMaxVideoFrameBufferSize = DW_TO_INT(&block[17]);
  frame->dwDefaultFrameInterval = DW_TO_INT(&block[21]);
  int n = frame->bFrameIntervalType = block[25];

  if (!n) {
    frame->dwMinFrameInterval = DW_TO_INT(&block[26]);
    frame->dwMaxFrameInterval = DW_TO_INT(&block[30]);
    frame->dwFrameIntervalStep = DW_TO_INT(&block[34]);
  } else {
    frame->rgIntervals = (uint32_t *)calloc(block[25] + 1, sizeof(frame->rgIntervals[0]));
    p = &block[26];

    for (i = 0; i < n; ++i) {
      uint32_t interval = DW_TO_INT(p);
      if (!interval) LOGW("interval is 0; treating as 1");
      frame->rgIntervals[i] = interval ? interval : 1; // avoid zero intervals. but why?
      p += 4;
    }
    frame->rgIntervals[n] = 0;
    frame->dwDefaultFrameInterval = MIN(frame->rgIntervals[n-1], MAX(frame->rgIntervals[0], frame->dwDefaultFrameInterval));
  }

  if (frameType == UVC_VS_FRAME_UNCOMPRESSED) {
    frame->dwMaxVideoFrameBufferSize = uint32_t(format->bBitsPerPixel * frame->wWidth * frame->wHeight / 8);
  }

  DL_APPEND(format->frame_descs, frame);

  UVC_EXIT(UVC_SUCCESS);
  return UVC_SUCCESS;
}

/** @internal
 * Process a single VideoStreaming descriptor block
 * Table 3-14 of "USB Device Class Definition for Video Devices"
 * @ingroup device
 */
uvc_error_t uvc_parse_vs(
    uvc_device_t *dev,
    uvc_device_info_t *info,
    uvc_streaming_interface_t *stream_if,
    const unsigned char *block, size_t block_size) {
  uvc_error_t ret;
  int descriptor_subtype;

  UVC_ENTER_VERBOSE();

  ret = UVC_SUCCESS;
  descriptor_subtype = block[2];

  switch (descriptor_subtype) {
  case UVC_VS_INPUT_HEADER:
    ret = uvc_parse_vs_input_header(stream_if, block, block_size);
    break;
  case UVC_VS_FORMAT_UNCOMPRESSED:
    ret = uvc_parse_vs_format_uncompressed(stream_if, block, block_size);
    break;
  case UVC_VS_FORMAT_MJPEG:
    ret = uvc_parse_vs_format_mjpeg(stream_if, block, block_size);
    break;
  case UVC_VS_FRAME_UNCOMPRESSED:
  case UVC_VS_FRAME_MJPEG:
    ret = uvc_parse_vs_frame_uncompressed(stream_if, block, block_size);
    break;
  case UVC_VS_FORMAT_FRAME_BASED:
    ret = uvc_parse_vs_frame_format ( stream_if, block, block_size );
    break;
  case UVC_VS_FRAME_FRAME_BASED:
    ret = uvc_parse_vs_frame_frame ( stream_if, block, block_size );
    break;
  case UVC_VS_COLORFORMAT:
  case UVC_VS_STILL_IMAGE_FRAME:
  default:
    /** @todo handle JPEG and maybe still frames or even DV... */
    LOGV("unsupported descriptor subtype: %d %s", descriptor_subtype, vsSubtypeName((uvc_vs_desc_subtype)descriptor_subtype));
    break;
  }

  UVC_EXIT(ret);
  return ret;
}

void uvc_release_ref(uvc_device_handle*& devh)
    {
    ::releaseRef(devh);
    }

/** @internal
 * @brief Get number of open devices
 */
size_t uvc_num_devices(uvc_context_t *ctx) {
  size_t count = 0;

  uvc_device_handle_t *devh;

  UVC_ENTER();

  DL_FOREACH(ctx->openDevicesList, devh) {
    count++;
  }

  UVC_EXIT((int) count);
  return count;
}

void uvc_process_control_status(uvc_device_handle_t *devh, unsigned char *data, int len)
    {
    enum uvc_status_class status_class;
    enum uvc_status_attribute attribute = UVC_STATUS_ATTRIBUTE_UNKNOWN;
    void *content = NULL;
    size_t content_len = 0;
    int found_entity = 0;
    struct uvc_input_terminal *input_terminal;
    struct uvc_processing_unit *processing_unit;

    UVC_ENTER_VERBOSE();

    if (len < sizeof(uvc_control_status_packet))
        {
        UVC_DEBUG("Short read of VideoControl status update (%d bytes)", len);
        UVC_EXIT_VOID();
        return;
        }

    uvc_control_status_packet* pControlStatus = reinterpret_cast<uvc_control_status_packet*>(data);
    uint8_t originator = pControlStatus->bOriginator;
    uint8_t event = pControlStatus->bEvent;
    uint8_t selector = pControlStatus->bSelector;

    if (originator == 0)
        {
        UVC_DEBUG("Unhandled update from VC interface");
        UVC_EXIT_VOID();
        return;  /* @todo VideoControl virtual entity interface updates */
        }

    if (event != 0)
        {
        UVC_DEBUG("Unhandled VC event %d", (int) event);
        UVC_EXIT_VOID();
        return;
        }

    /* printf("bSelector: %d\n", selector); */

    DL_FOREACH(devh->info->ctrl_if.input_term_descs, input_terminal)
        {
        if (input_terminal->bTerminalID == originator)
            {
            status_class = UVC_STATUS_CLASS_CONTROL_CAMERA;
            found_entity = 1;
            break;
            }
        }

    if (!found_entity)
        {
        DL_FOREACH(devh->info->ctrl_if.processing_unit_descs, processing_unit)
            {
            if (processing_unit->bUnitID == originator)
                {
                status_class = UVC_STATUS_CLASS_CONTROL_PROCESSING;
                found_entity = 1;
                break;
                }
            }
        }

    if (!found_entity)
        {
        UVC_DEBUG("Got status update for unknown VideoControl entity %d",
                  (int) originator);
        UVC_EXIT_VOID();
        return;
        }

    attribute = (uvc_status_attribute) data[4];
    content = data + 5;
    content_len = len - 5;

    UVC_DEBUG_VERBOSE("Event: class=%d, event=%d, selector=%d, attribute=%d, content_len=%zd",
              status_class, event, selector, attribute, content_len);

    if (devh->status_cb)
        {
        ScopedLock scopedLock(devh->callbackLock);
        UVC_DEBUG_VERBOSE("Running user-supplied status callback");
        devh->status_cb(status_class,
                        event,
                        selector,
                        attribute,
                        content, content_len,
                        devh->status_user_ptr);
        }

    UVC_EXIT_VOID();
    }

void uvc_process_streaming_status(uvc_device_handle_t *devh, unsigned char *data, int len)
    {
    UVC_ENTER();

    uvc_streaming_status_packet* pStreamingPacket = reinterpret_cast<uvc_streaming_status_packet*>(data);

    if (len < offsetmax(uvc_streaming_status_packet, bEvent))
        {
        UVC_DEBUG("Invalid streaming status event received.\n");
        UVC_EXIT_VOID();
        return;
        }

    if (pStreamingPacket->bEvent == uvc_streaming_status_packet::bEventButtonPress)
        {
        if (len < offsetmax(uvc_streaming_status_packet, bValue))
            {
            UVC_DEBUG("Short read of status update (%d bytes)", len);
            UVC_EXIT_VOID();
            return;
            }
        UVC_DEBUG("Button (intf %u) %s len %d\n", pStreamingPacket->bOriginator, pStreamingPacket->bValue ? "pressed" : "released", len);

        if (devh->button_cb)
            {
            ScopedLock scopedLock(devh->callbackLock);
            UVC_DEBUG("Running user-supplied button callback");
            devh->button_cb(pStreamingPacket->bOriginator,
                            pStreamingPacket->bValue,
                            devh->button_user_ptr);
            }
        }
    else
        {
        UVC_DEBUG("strm status org=%u error event=0x%02x value=0x%02x len=%d.", pStreamingPacket->bOriginator, pStreamingPacket->bEvent, pStreamingPacket->bValue, len);
        }

    UVC_EXIT_VOID();
    }

void uvc_process_status_xfer(uvc_device_handle_t *devh, struct libusb_transfer *transfer) {
  
  UVC_ENTER_VERBOSE();

  /* printf("Got transfer of aLen = %d\n", transfer->actual_length); */

  if (transfer->actual_length > 0) {
    uvc_status_packet* pStatusPacket = reinterpret_cast<uvc_status_packet*>(transfer->buffer);

    // USB Device Class Definition for Video Devices, Table 2-1 Status Packet Format
    switch (pStatusPacket->statusType()) {
    case 1: /* VideoControl interface */
      uvc_process_control_status(devh, transfer->buffer, transfer->actual_length);
      break;
    case 2:  /* VideoStreaming interface */
      uvc_process_streaming_status(devh, transfer->buffer, transfer->actual_length);
      break;
    }
  }

  UVC_EXIT_VOID();
}

/** @internal
 * @brief Process asynchronous status updates from the device.
 */
void LIBUSB_CALL _uvc_status_callback(struct libusb_transfer *transfer) {
  UVC_ENTER_VERBOSE();

  uvc_device_handle_t *devh = (uvc_device_handle_t *) transfer->user_data;

  switch (transfer->status) {
  case LIBUSB_TRANSFER_ERROR:
  case LIBUSB_TRANSFER_CANCELLED:
  case LIBUSB_TRANSFER_NO_DEVICE:
    UVC_DEBUG("transfer: not processing/resubmitting, status=%d(%s)", transfer->status, libusb_error_name(transfer->status));
    UVC_EXIT_VOID();
    return;
  case LIBUSB_TRANSFER_COMPLETED:
    uvc_process_status_xfer(devh, transfer);
    break;
  case LIBUSB_TRANSFER_STALL:
    // clear_halt(transfer); // seemed like a good idea, but cancels needed first, so, nada -rga
    // fall through
  case LIBUSB_TRANSFER_TIMED_OUT:
  case LIBUSB_TRANSFER_OVERFLOW:
    UVC_DEBUG("retrying transfer, status = %d", transfer->status);
    break;
  }

  uvc_error_t ret = uvc_error_t(libusb_submit_transfer(transfer));
  UVC_DEBUG_VERBOSE("libusb_submit_transfer() = %d", ret);

  UVC_EXIT_VOID();
}

/** @brief Set a callback function to receive status updates
 *
 * @ingroup device
 */
void uvc_set_status_callback(uvc_device_handle_t *devh,
                             uvc_status_callback_t cb,
                             void *user_ptr)
    {
    UVC_ENTER();

    ScopedLock scopedLock(devh->callbackLock);
    devh->status_cb = cb;
    devh->status_user_ptr = user_ptr;

    UVC_EXIT_VOID();
    }

/** @brief Set a callback function to receive button events
 *
 * @ingroup device
 */
void uvc_set_button_callback(uvc_device_handle_t *devh,
                             uvc_button_callback_t cb,
                             void *user_ptr)
    {
    UVC_ENTER();

    ScopedLock scopedLock(devh->callbackLock);
    devh->button_cb = cb;
    devh->button_user_ptr = user_ptr;

    UVC_EXIT_VOID();
    }

/**
 * @brief Get format descriptions for the open device.
 *
 * Note: this API seems bogus, as it only looks at the FIRST entry in the devh->info->stream_ifs
 * list when in fact there may be several such entries. How can it ever usefully work? I 
 * must be confused somehow. -rga
 *
 * @note Do not modify the returned structure.
 *
 * @param devh Device handle to an open UVC device
 */
const uvc_format_desc_t *uvc_get_format_descs(uvc_device_handle_t *devh) {
  return devh->info->stream_ifs->format_descs;
}


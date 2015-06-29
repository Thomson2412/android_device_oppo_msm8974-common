# inherit from the proprietary version
-include vendor/oppo/find7a/BoardConfigVendor.mk

COMMON_PATH := device/oppo/msm8974-common

TARGET_NO_BOOTLOADER := true

# Platform
TARGET_BOARD_PLATFORM := msm8974
TARGET_BOARD_PLATFORM_GPU := qcom-adreno330

# Architecture
TARGET_CPU_VARIANT := krait
TARGET_CPU_ABI := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_SMP := true

# Krait optimizations
TARGET_USE_KRAIT_BIONIC_OPTIMIZATION := true

BOARD_KERNEL_SEPARATED_DT := true
BOARD_KERNEL_CMDLINE := console=ttyHSL0,115200,n8 androidboot.hardware=qcom androidboot.selinux=enforcing user_debug=31 msm_rtb.filter=0x37 ehci-hcd.park=3
BOARD_KERNEL_BASE := 0x00000000
BOARD_KERNEL_PAGESIZE := 2048
BOARD_MKBOOTIMG_ARGS := --ramdisk_offset 0x02000000 --tags_offset 0x01e00000
BOARD_CUSTOM_BOOTIMG_MK := $(COMMON_PATH)/mkbootimg.mk

BOARD_BOOTIMAGE_PARTITION_SIZE := 0x00F00000
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 0x00F00000
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 1388314624
BOARD_USERDATAIMAGE_PARTITION_SIZE := 3221225472
BOARD_FLASH_BLOCK_SIZE := 131072

# global
TARGET_SPECIFIC_HEADER_PATH := $(COMMON_PATH)/include
BOARD_USES_QCOM_HARDWARE := true
TARGET_USES_QCOM_BSP := true
COMMON_GLOBAL_CFLAGS += -DQCOM_HARDWARE -DQCOM_BSP -DQCOM_MEDIA_DISABLE_BUFFER_SIZE_CHECK

# Display
TARGET_QCOM_DISPLAY_VARIANT := caf-bfam
USE_OPENGL_RENDERER := true
TARGET_USES_ION := true
TARGET_USES_C2D_COMPOSITION := true
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so

# Time Daemon
BOARD_USES_QC_TIME_SERVICES := true

# Audio/media
TARGET_QCOM_AUDIO_VARIANT := caf-bfam
TARGET_QCOM_MEDIA_VARIANT := caf-bfam
TARGET_ENABLE_QC_AV_ENHANCEMENTS := true

# audio
BOARD_USES_ALSA_AUDIO:= true
AUDIO_FEATURE_ENABLED_COMPRESS_VOIP := true
AUDIO_FEATURE_ENABLED_EXTN_FORMATS := true
AUDIO_FEATURE_ENABLED_EXTN_POST_PROC := true
AUDIO_FEATURE_ENABLED_FLUENCE := true
AUDIO_FEATURE_ENABLED_HFP := true
AUDIO_FEATURE_ENABLED_PCM_OFFLOAD := true
AUDIO_FEATURE_ENABLED_FLAC_OFFLOAD := true
AUDIO_FEATURE_ENABLED_PROXY_DEVICE := true
AUDIO_FEATURE_ENABLED_USBAUDIO := true
AUDIO_FEATURE_ENABLED_COMPRESS_CAPTURE := true
AUDIO_FEATURE_ENABLED_INCALL_MUSIC := true

# Camera
COMMON_GLOBAL_CFLAGS += -DNEEDS_VECTORIMPL_SYMBOLS -DOPPO_CAMERA_HARDWARE
USE_DEVICE_SPECIFIC_CAMERA := true

# Disable secure discard because it's SLOW
BOARD_SUPPRESS_SECURE_ERASE := true

# Bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_QCOM := true
BLUETOOTH_HCI_USE_MCT := true

# Wifi
BOARD_HAS_QCOM_WLAN              := true
BOARD_WLAN_DEVICE                := qcwcn
WPA_SUPPLICANT_VERSION           := VER_0_8_X
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
WIFI_DRIVER_FW_PATH_STA          := "sta"
WIFI_DRIVER_FW_PATH_AP           := "ap"
WIFI_DRIVER_MODULE_PATH          := "/system/lib/modules/wlan.ko"
WIFI_DRIVER_MODULE_NAME          := "wlan"

BOARD_EGL_CFG := $(COMMON_PATH)/configs/egl.cfg

# charger
BOARD_CHARGER_DISABLE_INIT_BLANK := true
HEALTHD_FORCE_BACKLIGHT_CONTROL := true
HEALTHD_BACKLIGHT_ON_LEVEL := 125

# power hal
TARGET_PROVIDES_POWERHAL := true

# ril - must be relative to frameworks/opt/telephony
BOARD_RIL_CLASS := ../../../$(COMMON_PATH)/ril

# selinux
include device/qcom/sepolicy/sepolicy.mk

BOARD_SEPOLICY_DIRS += \
    $(COMMON_PATH)/sepolicy

BOARD_SEPOLICY_UNION += \
    device.te \
    file_contexts \
    file.te \
    init_shell.te \
    mediaserver.te \
    mm-qcamerad.te \
    qseecomd.te \
    rmt_storage.te \
    sensors.te \
    system_app.te \
    system_server.te \
    time_daemon.te \
    thermal-engine.te \
    vold.te \
    property_contexts \
    property.te \
    mac-update.te

# Recovery:Start

# Use this flag if the board has a ext4 partition larger than 2gb
BOARD_HAS_LARGE_FILESYSTEM := true
TARGET_USERIMAGES_USE_EXT4 := true

# TWRP specific build flags
TW_THEME := portrait_hdpi
RECOVERY_GRAPHICS_USE_LINELENGTH := true
TARGET_RECOVERY_PIXEL_FORMAT := "RGBX_8888"
TW_NO_USB_STORAGE := true
TW_INCLUDE_JB_CRYPTO := false
TW_NO_SCREEN_BLANK := true
TW_EXCLUDE_ENCRYPTED_BACKUPS := true
TW_INCLUDE_L_CRYPTO := true
BOARD_USE_CUSTOM_RECOVERY_FONT := \"roboto_23x41.h\"
TW_BRIGHTNESS_PATH := /sys/class/leds/lcd-backlight/brightness

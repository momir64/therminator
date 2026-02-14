# Therminator a.k.a. Sleep Terminator
<p float="left">
    <img src="https://github.com/user-attachments/assets/75602776-a598-447b-8c4f-67aad60d0d7f" height="220">
    <img src="https://github.com/user-attachments/assets/ae228a15-4fd9-476a-8d0d-3f21e02323fe" height="220">
</p>

# Short system description
The idea of the project is to create a smart home alarm clock system. The system’s main focus is ensuring that the user actually wakes up. In order to achieve this, the system will have a high level of redundancy. Another goal of the system is adaptability to both the environment and the user’s preferences. To prevent the user from falling back asleep after turning off the alarm, the system will use a thermal camera to detect whether the user is still in bed.
<br/>

<img src="https://ftp.moma.rs/therminator/clock.png" width="640"/>

# System components
To allow proper positioning of each system element within a room, the system is divided into modules. It consists of three modules: the clock, the camera, and the speaker. The modules are wirelessly connected, and system coordination is handled by the clock module.

![](https://ftp.moma.rs/therminator/nacrt.png)

## Camera module
It is important to note that the resolution of the thermal camera is only 32×24 pixels. In addition, the subject will be located at a considerable distance from the camera (around 2 meters). As a consequence, the expected size of the subject will be only a few pixels, so the detection algorithm will be relatively simple and based on a limited number of parameters (e.g. a threshold depending on room temperature).

![](https://ftp.moma.rs/therminator/demo.png)

### Components
- [x] MLX90640BAB thermal camera (BAB – variant with a smaller FOV lens)
- [x] ESP32 S3 Nano
- [x] 5V power supply

## Speaker module
To prevent forceful silencing of the alarm using nearby objects (e.g. a pillow), the speaker is implemented as a separate wireless module. This enables placement of the speaker in harder-to-reach locations. Another benefit is the possibility of using more powerful speakers with higher power consumption without affecting the battery life of the central clock module.

### Components
- [x] Bluetooth audio receiver
- [x] PAM8610 2×15W amplifier
- [x] 10W 8Ω speakers
- [x] 12V power supply
- [x] 5V power supply

## Clock module
The central coordinator of the system is the clock module. This is where signal processing from the camera module takes place, as well as control of the speaker module. Alarm configurations and alarm sounds are stored locally within the clock module. The core elements of this module are a Raspberry Pi 3, a button for turning off the alarm, and a display for showing information such as the current time, time until the next alarm, weather forecast, etc. To increase system redundancy, the module also includes a UPS in case of power loss. It will also have its own speaker system in case the speaker module is unavailable.

### Components
- [x] Raspberry Pi 3 Model A+
- [x] Wisecoco 6.2 Inch 360×960 IPS Display SPI RGB Interface
- [x] QTSS HDMI-RGB21-V06 LCD Board for 40-pin display
- [x] Self-resetting push button
- [x] PCM5102 I2S DAC
- [x] CJMCU-8406 amplifier
- [x] 3W 4Ω speakers
- [x] Waveshare UPS Module 3S
- [x] 3× Li-ion 18650 batteries
- [x] 12V power supply
- [x] Cables and connectors

# System control
The clock module is accessed via a home server with a leased domain (Raspberry Pi 4). The home server acts as a reverse proxy between the clock module and the client application. It also hosts the client web application.

# Functional requirements
- Alarm scheduling
  - An alarm represents a sequence of alarms starting at a specific time
  - For each alarm, it is possible to define a time offset relative to the start of the sequence
  - The alarm sound can be defined or randomly selected from user-defined playlists
  - It is possible to set the time after which the system checks whether the user has returned to bed
  - It is possible to set the alarm volume
- Camera configuration
  - Camera preview to simplify positioning and calibration
  - Configuration of detection parameters (thresholds)
- Display configuration
  - Configuration of display brightness, font, and text color
  - Configuration of the display active time
  - Configuration of useful information such as the weather forecast
- Battery configuration
  - Viewing the current battery charge level
  - Configuration of battery level warnings

# Non-functional requirements
The system is primarily controlled via a phone, but it is necessary to retain the option of control from other devices as well. For this reason, the client application is developed using Kotlin Compose Multiplatform to enable cross-platform compatibility. The backend server is implemented using Sanic, a Python web server framework.

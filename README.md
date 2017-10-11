# ST_WiLight
ST_WiLight is a SmartApp and Device Handles that works with your WiLights devices to create SmartThings devices.

A project by Leonardo Figueiro (leoagfig@gmail.com)

# Introduction

This project intents to connect WiLights devices (www.wilight.com.br) to SmartTings.
WiLights are WIFI enabled devices that communicate with the SmartThings hub over the local LAN.

This project uses SmartThings Composite Device Handler (DH). This functionality allows one Parent DH to create many Child Devices (using Child DHs). This allows more than one of each SmartThings capability per WiLight real device.
The WiLight Parent DH has been written to automagically create Child Devices that exist in WiLight real device.
There is a SmartApp - WiLight (Conect) - that will discover WiLight real devices and add them to SmartThings as a Parent Device. Its name will be WiLight XXXXXX, where XXXXXX are the last six digits of WiLight's Serial Number.
Once WiLight Parent Device is created in SmartThings, it will create as many Child Devices as there are in the WiLight real device.

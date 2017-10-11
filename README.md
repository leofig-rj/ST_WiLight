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

# SmartThings IDE Setup Instructions

Create an account and/or log into the SmartThings Developers Web IDE.
Click on "My Device Handlers" from the navigation menu.
Click on "Settings" from the menu and add my GitHub Repository to your account
Owner: leofig-rj
Name: ST_WiLight
Branch: master
Click on "Update From Repo" from the menu
Select "ST_WiLight (master)" from the list
Select all of the Parent and Child Device Handlers
Check the "Publish" check box and click "Execute Update"
You should now have all of the necessary Device Handlers added to your account
Note: If desired, you can still create all of the Device Handlers manually by copying and pasting code from the GitHub repository files into your ST IDE. Trust me, the Github integration in SmartThings is so much easier! And, you will know when new versions of the DHs are available based on the color of each DH in your list of Device Handlers in the IDE.


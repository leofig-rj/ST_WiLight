/**
 *  Child WiLght Window Shade
 *
 *  Copyright 2017 Leonardo Figueiro
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Child WiLght Window Shade", namespace: "leofig-rj", author: "Leonardo Figueiro") {
		capability "Window Shade"
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
        capability "Sensor"

		attribute "stop", "string"

        attribute "stopStr", "string"
        
//       attribute "close/stop", "string"
        
        attribute "lastUpdated", "String"
        
        command "generateEvent", ["string", "string"]

        command "stop"
	}


	simulator {
		// TODO: define status and reply messages here
	}

    tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState("closed",  label:'${name}', action:"open", icon:"st.doors.garage.garage-closed", backgroundColor:"#bbbbdd", nextState: "opening")
                attributeState("open",    label:'${name}', action:"close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffcc33", nextState: "closing")
                attributeState("partially open", label:'preset', action:"close", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
                attributeState("closing", label:'${name}', action:"stop", icon:"st.doors.garage.garage-closing", backgroundColor:"#bbbbdd")
                attributeState("opening", label:'${name}', action:"stop", icon:"st.doors.garage.garage-opening", backgroundColor:"#ffcc33")
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState("level", action:"switch level.setLevel")
            }
        }

        standardTile("switchmain", "device.windowShade") {
            state("closed",  label:'${name}', action:"open", icon:"st.doors.garage.garage-closed", backgroundColor:"#bbbbdd", nextState: "opening")
            state("open",    label:'${name}', action:"close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffcc33", nextState: "closing")
            state("partially open", label:'preset', action:"close", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
            state("closing", label:'${name}', action:"stop", icon:"st.doors.garage.garage-closing", backgroundColor:"#bbbbdd")
            state("opening", label:'${name}', action:"stop", icon:"st.doors.garage.garage-opening", backgroundColor:"#ffcc33")
        }

        standardTile("on", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("on", label:'open', action:"open", icon:"st.doors.garage.garage-opening")
        }
        standardTile("off", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("off", label:'close', action:"close", icon:"st.doors.garage.garage-closing")
        }
        standardTile("stop", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("stop", label:'stop', action:"stop", icon:"st.Transportation.transportation13")
        }
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
        }
        valueTile("lastUpdated", "device.lastUpdated", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
            state "default", label:'Last Updated ${currentValue}', backgroundColor:"#ffffff"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
            state("level", action:"switch level.setLevel")
        }
        
        main(["switchmain"])
        details(["shade", "on", "off", "stop", "level", "lastUpdated"])
    }
}

// handle commands
def on() {
    log.debug "On '${device.deviceNetworkId}'"
    int level = 100
//    setLevel(level) 
    open()
}

def off() {
    log.debug "Off '${device.deviceNetworkId}'"
    int level = 0
//    setLevel(level)
    close()
}

def open() {
    log.debug "Open '${device.deviceNetworkId}'"
//    on()
    parent.childOpen(device.deviceNetworkId)
}

def close() {
    log.debug "Close '${device.deviceNetworkId}'"
//    off()
    parent.childClose(device.deviceNetworkId)
}

def stop() {
    log.debug "Stop '${device.deviceNetworkId}'"
    parent.childStop(device.deviceNetworkId)
}

def setLevel(value) {
    log.debug "setLevel >> value: $value"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 100), 0)
    if (level > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    sendEvent(name: "level", value: level, unit: "%")
    
    parent.childSetLevel(device.deviceNetworkId, level)
}

def generateEvent(String name, String value) {
    // Update device
    // The name coming in from WiLight will be "windowShade", but we want to the ST standard attribute for compatibility with normal SmartApps
    // The value coming in from WiLight will be on/off,level, so we will create two events one for switch and other to level
    log.debug "Parsing: $value"
    def parts = value.split(" ")
    def state = parts.length>0?parts[0].trim():null
    def level = parts.length>1?parts[1].trim():null
    if (state == "partially_open") {
        sendEvent(name: "windowShade", value: "partially open")
    } else {
        sendEvent(name: "windowShade", value: state)
    }
    sendEvent(name: "level", value: level, unit: "%")
    if (level > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
  	// Update lastUpdated date and time
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}

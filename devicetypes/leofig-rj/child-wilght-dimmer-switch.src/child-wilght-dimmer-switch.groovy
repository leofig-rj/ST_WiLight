/**
 *  Child WiLght Dimmer Switch
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
    definition (name: "Child WiLght Dimmer Switch", namespace: "leofig-rj", author: "Leonardo Figueiro") {
        capability "Switch Level"
        capability "Switch"
        capability "Relay Switch"
        capability "Actuator"
        capability "Sensor"
        
        attribute "lastUpdated", "String"
        
        command "generateEvent", ["string", "string"]
    }
    
    
    simulator {
        // TODO: define status and reply messages here
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.Lighting.light13", backgroundColor: "#00A0DC", nextState:"turningOff"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
            }
                tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }
        
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
        }
        valueTile("lastUpdated", "device.lastUpdated", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
            state "default", label:'Last Updated ${currentValue}', backgroundColor:"#ffffff"
        }
        
        main(["switch"])
        details(["switch", "level", "lastUpdated"])       
    }
}

// handle commands
def on() {
    log.debug "On '${device.deviceNetworkId}'"
    parent.childOn(device.deviceNetworkId)
}

def off() {
    log.debug "Off '${device.deviceNetworkId}'"
    parent.childOff(device.deviceNetworkId)
}

def setLevel(value) {
    log.debug "setLevel >> value: $value"
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)
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
    // The name coming in from WiLight will be "dimmerSwitch", but we want to the ST standard attribute for compatibility with normal SmartApps
    // The value coming in from WiLight will be on/off,level, so we will create two events one for switch and other to level
    log.debug "Parsing: $value"
    def parts = value.split(" ")
    def state = parts.length>0?parts[0].trim():null
    def level = parts.length>1?parts[1].trim():null
    sendEvent(name: "switch", value: state)
    sendEvent(name: "level", value: level, unit: "%")
   	// Update lastUpdated date and time
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}
/**
 *  Child WiLght Switch
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
    definition (name: "Child WiLght Switch", namespace: "leofig-rj", author: "Leonardo Figueiro") {
        capability "Switch"
        capability "Relay Switch"
        capability "Actuator"
        capability "Sensor"
        
        attribute "lastUpdated", "String"
        
        command "continueParse", ["string", "string"]
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
            tileAttribute("device.lastUpdated", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'    Last updated ${currentValue}',icon: "st.Health & Wellness.health9")
            }
        }
    }
}

def on() {
    log.debug "On '${device.deviceNetworkId}'"
    parent.childOn(device.deviceNetworkId)
}

def off() {
    log.debug "Off '${device.deviceNetworkId}'"
    parent.childOff(device.deviceNetworkId)
}

def continueParse(String name, String value) {
    // Update device
    sendEvent(name: name, value: value)
   	// Update lastUpdated date and time
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}
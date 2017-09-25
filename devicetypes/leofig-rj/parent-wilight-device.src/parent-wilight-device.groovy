/**
 *  Parent WiLight Device
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

import groovy.json.JsonSlurper

metadata {
	definition (name: "Parent WiLight Device", namespace: "leofig-rj", author: "Leonardo Figueiro") {
		capability "Configuration"
		capability "Refresh"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    def currentVal
    def isDisplayed = true
    def isPhysical = true
    def name
    def action
    //log.debug "descMap: ${descMap}"

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) sendEvent(name: 'ip', value: getDataValue("ip"))
    
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    
    	if(body.startsWith("{") || body.startsWith("[")) {
   
   			def slurper = new JsonSlurper()
    		def jsonResult = slurper.parseText(body)
			           
            if (jsonResult.containsKey("lampada1")) {
            	def value = jsonResult.lampada1
    			if (value == "on") {
    				//send an event if there is a state change
        			if (device?.currentValue("lampada1") != "on") {
                    	log.debug "parsing lampada1 on"
        				sendEvent (name:"lampada1", value:"on", displayed: true, isStateChange: true, isPhysical: true)
                    }
    			}
                if (value == "off") {
    				//send an event if there is a state change
        			if (device?.currentValue("lampada1") != "off") {
                    	log.debug "parsing lampada1 off"
        				sendEvent (name:"lampada1", value:"off", displayed: true, isStateChange: true, isPhysical: true)
                    }
    			}
			}
			if (jsonResult.containsKey("version")) {
            	//log.debug "firmware version: $jsonResult.version"
                if (device?.currentValue("firmware") != jsonResult.version) {
                	//log.debug "updating firmware version"
       				sendEvent(name:"firmware", value: jsonResult.version, displayed: false)
                }
    		}
    	} else {
        	//log.debug "Response is not JSON: $body"
    	}
  	}          
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}
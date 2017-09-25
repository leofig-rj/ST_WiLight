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
			           
            if (jsonResult.containsKey("switch1")) {
            	def value = jsonResult.switch1
                parseChild("switch1", value)
			}
            if (jsonResult.containsKey("switch2")) {
            	def value = jsonResult.switch2
                parseChild("switch2", value)
			}
            if (jsonResult.containsKey("switch3")) {
            	def value = jsonResult.switch3
                parseChild("switch3", value)
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

def parseChild(String nome, String value) {

		def nameparts = name.split("\\d+", 2)
		def namebase = nameparts.length>0?nameparts[0].trim():null
        def namenum = name.substring(namebase.length()).trim()
		
        def results = []
        
        def isChild = containsDigit(name)
   		//log.debug "Name = ${name}, isChild = ${isChild}, namebase = ${namebase}, namenum = ${namenum}"      
        //log.debug "parse() childDevices.size() =  ${childDevices.size()}"
		
		def childDevice = null
		
		try {
		
            childDevices.each {
				try{
            		//log.debug "Looking for child with deviceNetworkID = ${device.deviceNetworkId}-${name} against ${it.deviceNetworkId}"
                	if (it.deviceNetworkId == "${device.deviceNetworkId}-${name}") {
                	childDevice = it
                    //log.debug "Found a match!!!"
                	}
            	}
            	catch (e) {
            	//log.debug e
            	}
        	}
            
            //If a child should exist, but doesn't yet, automatically add it!            
        	if (isChild && childDevice == null) {
        		log.debug "isChild = true, but no child found - Auto Add it!"
            	//log.debug "    Need a ${namebase} with id = ${namenum}"
            
            	createChildDevice(namebase, namenum)
            	//find child again, since it should now exist!
            	childDevices.each {
					try{
            			//log.debug "Looking for child with deviceNetworkID = ${device.deviceNetworkId}-${name} against ${it.deviceNetworkId}"
                		if (it.deviceNetworkId == "${device.deviceNetworkId}-${name}") {
                			childDevice = it
                    		//log.debug "Found a match!!!"
                		}
            		}
            		catch (e) {
            			//log.debug e
            		}
        		}
        	}
            
            if (childDevice != null) {
                //log.debug "parse() found child device ${childDevice.deviceNetworkId}"
                
                childDevice.generateEvent(namebase, value)
				log.debug "${childDevice.deviceNetworkId} - name: ${namebase}, value: ${value}"
                
            }
            else  //must not be a child, perform normal update
            {
                results = createEvent(name: name, value: value)
                log.debug results
                return results
            }
		}
        catch (e) {
        	log.error "Error in parse() routine, error = ${e}"
        }
    
}

void childOn(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childOn($dni), name = ${name}"
    getAction("/command?${name}=on")
}

void childOff(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childOff($dni), name = ${name}"
    getAction("/command?${name}=off")
}

void childSetLevel(String dni, value) {
    def name = dni.split("-")[-1]
    log.debug "childSetLevel($dni), name = ${name}, level = ${value}"
    getAction("/command?${name}=${value}")
}

// handle commands
def configure() {
	log.debug "Executing 'configure()'"
    updateDeviceNetworkID()
//	sendEvent(name: "numberOfButtons", value: numButtons)
}

def refresh() {
	log.debug "Executing 'refresh()'"
	sendEthernet("refresh")
//	sendEvent(name: "numberOfButtons", value: numButtons)
}

def installed() {
	log.debug "Executing 'installed()'"
    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/)
    {
    }
    else
    {
        state.alertMessage = "WiLight Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        runIn(2, "sendAlert")
    }
}

private void createChildDevice(String deviceName, String deviceNumber) {

    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/)
    {
		log.trace "createChildDevice:  Creating Child Device '${device.displayName} (${deviceName}${deviceNumber})'"

		try 
        {
        	def deviceHandlerName = ""
        	switch (deviceName) {
//         		case "contact": 
//                	deviceHandlerName = "Child Contact Sensor" 
//                	break
         		case "switch": 
                	deviceHandlerName = "Child WiLght Switch" 
                	break
//         		case "dimmerSwitch": 
//                	deviceHandlerName = "Child Dimmer Switch" 
//                	break
//         		case "relaySwitch": 
//                	deviceHandlerName = "Child Relay Switch" 
//                	break
//				case "temperature": 
//                	deviceHandlerName = "Child Temperature Sensor" 
//                	break
//         		case "humidity": 
//                	deviceHandlerName = "Child Humidity Sensor" 
//                	break
//         		case "motion": 
//                	deviceHandlerName = "Child Motion Sensor" 
//                	break
//         		case "water": 
//                	deviceHandlerName = "Child Water Sensor" 
//                	break
//         		case "illuminance": 
//                	deviceHandlerName = "Child Illuminance Sensor" 
//                	break
//         		case "illuminancergb": 
//                	deviceHandlerName = "Child IlluminanceRGB Sensor" 
//                	break
//				case "voltage": 
//                	deviceHandlerName = "Child Voltage Sensor" 
//                	break
//				case "smoke": 
//                	deviceHandlerName = "Child Smoke Detector" 
//                	break    
//				case "carbonMonoxide": 
//                	deviceHandlerName = "Child Carbon Monoxide Detector" 
//                	break    
//         		case "alarm": 
//                	deviceHandlerName = "Child Alarm" 
//                	break    
//         		case "doorControl": 
//                	deviceHandlerName = "Child Door Control" 
//                	break
				default: 
                	log.error "No Child Device Handler case for ${deviceName}"
      		}
            if (deviceHandlerName != "") {
				addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${deviceName}${deviceNumber}", null,
		      		[completedSetup: true, label: "${device.displayName} (${deviceName}${deviceNumber})", 
                	isComponent: false, componentName: "${deviceName}${deviceNumber}", componentLabel: "${deviceName} ${deviceNumber}"])
        	}   
    	} catch (e) {
        	log.error "Child device creation failed with error = ${e}"
        	state.alertMessage = "Child device creation failed. Please make sure that the '${deviceHandlerName}' is installed and published."
	    	runIn(2, "sendAlert")
    	}
	} else 
    {
        state.alertMessage = "WiLight Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        runIn(2, "sendAlert")
    }
}

private getAction(uri){ 
  updateDNI()
  def userpass
  log.debug uri
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)

  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
  
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  return hubAction    
}

private sendAlert() {
   sendEvent(
      descriptionText: state.alertMessage,
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}

private boolean containsDigit(String s) {
    boolean containsDigit = false;

    if (s != null && !s.isEmpty()) {
//		log.debug "containsDigit .matches = ${s.matches(".*\\d+.*")}"
		containsDigit = s.matches(".*\\d+.*")
    }
    return containsDigit
}
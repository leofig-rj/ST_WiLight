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
		capability "Health Check"
        
		command "reboot"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "default", label:'Refresh', action: "refresh", icon: "st.secondary.refresh-icon"
			state "default", label:'Refresh', action: "refresh", icon: "https://github.com/leofig-rj/ST_WiLight/raw/master/icons/wilight_bw@2x.png"
		}
//		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "configure", label:'Configure', action:"configuration.configure", icon:"st.secondary.tools"
//		}
//    	standardTile("reboot", "device.reboot", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
//            state "default", label:"Reboot", action:"reboot", icon:"st.secondary.tools", backgroundColor:"#ffffff"
//      }
        valueTile("ip", "ip", width: 2, height: 2) {
    		state "ip", label:'IP Address\r\n${currentValue}'
		}
        valueTile("firmware", "firmware", width: 2, height: 2) {
    		state "firmware", label:'Firmware ${currentValue}'
		}
        
        childDeviceTiles("all")
	}
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing"
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

	log.debug "Body: ${body}"
    
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
            if (jsonResult.containsKey("dimmerSwitch1")) {
            	def value = jsonResult.dimmerSwitch1
                parseChild("dimmerSwitch1", value)
            }
            if (jsonResult.containsKey("dimmerSwitch2")) {
            	def value = jsonResult.dimmerSwitch2
                parseChild("dimmerSwitch2", value)
            }
            if (jsonResult.containsKey("dimmerSwitch3")) {
            	def value = jsonResult.dimmerSwitch3
                parseChild("dimmerSwitch3", value)
            }
            if (jsonResult.containsKey("windowShade1")) {
            	def value = jsonResult.windowShade1
                parseChild("windowShade1", value)
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

def parseChild(String name, String value) {

	log.debug "Name: ${name}"
	log.debug "Value: ${value}"
        def nameparts = name.split("\\d+", 2)
        def namebase = nameparts.length>0?nameparts[0].trim():null
        def namenum = name.substring(namebase.length()).trim()
        
        def results = []
        
        def isChild = containsDigit(name)
        
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
                //log.debug "Need a ${namebase} with id = ${namenum}"
                    
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
                //found child device - parse to child devise
                childDevice.continueParse(namebase, value)
                log.debug "${childDevice.deviceNetworkId} - name: ${namebase}, value: ${value}"
                
            }
//            else  //must not be a child, perform normal update
//            {
//                results = createEvent(name: name, value: value)
//                log.debug results
//                return results
//            }
        }
        catch (e) {
        	log.error "Error in parse() routine, error = ${e}"
        }
    
}

def childOn(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childOn($dni), name = ${name}"
//    getAction("/command?${name}=on")
    getActionChild("/command?${name}=on")
}

def childOff(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childOff($dni), name = ${name}"
//    getAction("/command?${name}=off")
    getActionChild("/command?${name}=off")
}

def childOpen(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childOpen($dni), name = ${name}"
//    getAction("/command?${name}=open")
    getActionChild("/command?${name}=open")
}

def childClose(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childClose($dni), name = ${name}"
//    getAction("/command?${name}=close")
    getActionChild("/command?${name}=close")
}

def childStop(String dni) {
    def name = dni.split("-")[-1]
    log.debug "childStop($dni), name = ${name}"
//    getAction("/command?${name}=stop")
    getActionChild("/command?${name}=stop")
}

def childSetLevel(String dni, value) {
    def name = dni.split("-")[-1]
    log.debug "childSetLevel($dni), name = ${name}, level = ${value}"
//    getAction("/command?${name}=${value}")
    getActionChild("/command?${name}=${value}")
}

private getHostAddressChild() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")    
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

def getActionChild(message) {
    log.debug "${device.deviceNetworkId}${message}"
	if (getDataValue("ip") != null && getDataValue("port") != null) {
        sendHubCommand(new physicalgraph.device.HubAction(
            method: "GET",
            path: "${message}",
            headers: [ HOST: "${getHostAddressChild()}" ]
        ))
    }
    else {
        state.alertMessage = "WiLight Parent Device has not yet been fully configured. Click the 'Gear' icon, enter data for all fields, and click 'Done'"
        runIn(2, "sendAlert")   
    }
}

private void createChildDevice(String deviceName, String deviceNumber) {

    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/) {
        log.trace "createChildDevice:  Creating Child Device '${device.displayName} (${deviceName}${deviceNumber})'"

        try {
        	def deviceHandlerName = ""
            switch (deviceName) {
//                case "contact": 
//                    deviceHandlerName = "Child WiLght Contact Sensor" 
//                    break
                case "switch": 
                    deviceHandlerName = "Child WiLght Switch" 
                    break
                case "dimmerSwitch": 
                    deviceHandlerName = "Child WiLght Dimmer Switch" 
                    break
//                case "relaySwitch": 
//                    deviceHandlerName = "Child WiLght Relay Switch" 
//                    break
//                case "temperature": 
//                    deviceHandlerName = "Child WiLght Temperature Sensor" 
//                    break
//                case "humidity": 
//                    deviceHandlerName = "Child WiLght Humidity Sensor" 
//                    break
//                case "motion": 
//                    deviceHandlerName = "Child WiLght Motion Sensor" 
//                    break
//                case "water": 
//                    deviceHandlerName = "Child WiLght Water Sensor" 
//                    break
//                case "illuminance": 
//                    deviceHandlerName = "Child WiLght Illuminance Sensor" 
//                    break
//                case "illuminancergb": 
//                    deviceHandlerName = "Child WiLght IlluminanceRGB Sensor" 
//                    break
//                case "voltage": 
//                    deviceHandlerName = "Child WiLght Voltage Sensor" 
//                    break
//                case "smoke": 
//                    deviceHandlerName = "Child WiLght Smoke Detector" 
//                    break    
//                case "carbonMonoxide": 
//                    deviceHandlerName = "Child WiLght Carbon Monoxide Detector" 
//                    break    
//                case "alarm": 
//                    deviceHandlerName = "Child WiLght Alarm" 
//                    break    
//                case "doorControl": 
//                    deviceHandlerName = "Child WiLght Door Control" 
//                    break
                case "windowShade": 
                    deviceHandlerName = "Child WiLght Window Shade" 
                    break
                default: 
                    log.error "No Child Device Handler case for ${deviceName}"
      	    }
            if (deviceHandlerName != "") {
                addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${deviceName}${deviceNumber}", null,
//		      	    [completedSetup: true, label: "${device.displayName} (${deviceName}${deviceNumber})", 
		      	    [completedSetup: true, label: "${device.displayName}-${deviceName}${deviceNumber}", 
                    isComponent: false, componentName: "${deviceName}${deviceNumber}", componentLabel: "${deviceName}${deviceNumber}"])
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
//	    log.debug "containsDigit .matches = ${s.matches(".*\\d+.*")}"
	    containsDigit = s.matches(".*\\d+.*")
    }
    return containsDigit
}

//Start of added functions

def reset() {
	log.debug "reset()"
}

def refresh() {
    log.debug "refresh(${device.deviceNetworkId})"
//    getAction("/status")
    getActionChild("/status")
}

def ping() {
    log.debug "ping()"
    refresh()
}

def reboot() {
    log.debug "reboot(${device.deviceNetworkId})"
//    getAction("/reboot")
    getActionChild("/reboot")
}

def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
}

private encodeCredentials(username, password){
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private getAction(uri){ 
  updateDNI()
  def userpass
  log.debug uri
  log.debug "${device.deviceNetworkId}${uri}"
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
  log.debug uri
  
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

private setDeviceNetworkId(ip, port = null){
    def myDNI
    if (port == null) {
        myDNI = ip
    } else {
        def iphex = convertIPtoHex(ip)
        def porthex = convertPortToHex(port)
        
        myDNI = "$iphex:$porthex"
    }
    return myDNI
}

private updateDNI() { 
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
       device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
        return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

def parseDescriptionAsMap(description) {
    description.split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

def toAscii(s){
    StringBuilder sb = new StringBuilder();
    String ascString = null;
    long asciiInt;
    for (int i = 0; i < s.length(); i++){
        sb.append((int)s.charAt(i));
        sb.append("|");
        char c = s.charAt(i);
    }
    ascString = sb.toString();
    asciiInt = Long.parseLong(ascString);
    return asciiInt;
}

def hex2int(value){
   return Integer.parseInt(value, 10)
}

def update_needed_settings()
{
    def cmds = []
    
    def isUpdateNeeded = "NO"
    
    cmds << getAction("/config?hubIp=${device.hub.getDataValue("localIP")}&hubPort=${device.hub.getDataValue("localSrvPortTCP")}")
        
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def installed() {
    log.debug "installed()"
    configure()
}

def configure() {
    log.debug "configure()"
    def cmds = [] 
    cmds = update_needed_settings()
    if (cmds != []) response(cmds)
}

def updated() {
    log.debug "updated()"
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    if (cmds != []) response(cmds)
}

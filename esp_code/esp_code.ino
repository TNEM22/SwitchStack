#define debugMode 1

#include <Arduino.h>
#include <WiFiMulti.h>
#include <WebSocketsClient.h>

#define blePin 4
// #define eled 2
#define S1 5
#define S2 18
#define S3 19
#define S4 21

#define BTSerial Serial2

WiFiMulti WiFiMulti;
WebSocketsClient webSocket;

const String userId = "/ws/65b6302baec9245b10d990ca";
String ssid = "";
String password = "";

char c = ' ';
char s = ' ';

bool bleStatus = false;

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length);

void setup() {
	if (debugMode) Serial.begin(115200);
	if (debugMode) Serial.setDebugOutput(true);
  BTSerial.begin(9600, SERIAL_8N1, 15, 2); // Initialize Serial2 with RX on GPIO 15 and TX on GPIO 2

  pinMode(blePin, OUTPUT);
  // pinMode(eled, OUTPUT);
  pinMode(S1, OUTPUT);
  pinMode(S2, OUTPUT);
  pinMode(S3, OUTPUT);
  pinMode(S4, OUTPUT);

  digitalWrite(blePin, LOW);
  // digitalWrite(eled, LOW);
  digitalWrite(S1, HIGH);
  digitalWrite(S2, HIGH);
  digitalWrite(S3, HIGH);
  digitalWrite(S4, HIGH);
}

void loop() {
  if (ssid.isEmpty() == 0 || password.isEmpty() == 0) {
    if (bleStatus == true) {
      digitalWrite(blePin, LOW);
      bleStatus = false;
      wifiMode();
    }
    if(WiFiMulti.run() == WL_CONNECTED) {
      webSocket.loop();
    }
  } else {
    bleMode();
  }
}

void bleMode() {
  if (bleStatus == false) {
    digitalWrite(blePin, HIGH);
    bleStatus = true;
    delay(500);
  }
  String msg = "";
  if (BTSerial.available()) {
    msg = BTSerial.readString();
    Serial.print(msg);
    String cmd = "", str = "";
    bool isCmd = true;
    for (int i = 0; i < msg.length(); i++) {
      if (isCmd) {
        if (msg.charAt(i) == ':') {
          isCmd = false;
          continue;
        } else {
          cmd += msg.charAt(i);
        }
      } else {
        if (msg.charAt(i) == ';') {
          Serial.print(cmd);
          Serial.print(" , ");
          Serial.println(str);
          if (cmd.equals("ssid")) {
            ssid = str;
            Serial.println("SSID set!!");
          } else if (cmd.equals("password")) {
            password = str;
            Serial.println("Password set!!");
          }
          cmd = "";
          str = "";
          isCmd = true;
        } else {
          str += msg.charAt(i);
        }
      }
    }
    BTSerial.write("Msg Received!\n");
  }
  delay(10);
}

void wifiMode() {
  WiFiMulti.addAP(ssid.c_str(), password.c_str());

	while(WiFiMulti.run() != WL_CONNECTED) {
    if (debugMode) Serial.print(".");
    delay(10);
	}
  if (debugMode) Serial.println();

	// server address, port and URL
  webSocket.beginSSL("smartswwsrailway-production.up.railway.app", 443, userId.c_str(), NULL, "");
  // change the above url with the one you have on connected network of esp or just use the hosted url
  webSocket.setExtraHeaders("");

	// event handler
	webSocket.onEvent(webSocketEvent);

	// try every 5000 sec again if connection has failed
	webSocket.setReconnectInterval(5000);
  
  // start heartbeat (optional)
  // ping server every 15000 ms
  // expect pong from server within 3000 ms
  // consider connection disconnected if pong is not received 2 times
  webSocket.enableHeartbeat(15000, 3000, 2);
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
	switch(type) {
		case WStype_DISCONNECTED:
			if (debugMode) Serial.printf("[WSc] Disconnected!\n");
      // analogWrite(eled, 15);
			break;
		case WStype_CONNECTED:
			if (debugMode) Serial.printf("[WSc] Connected to url: %s\n", payload);
      // analogWrite(eled, 0);
			// webSocket.sendTXT("Connected");
			break;
		case WStype_TEXT:
			if (debugMode) Serial.printf("[WSc] get text: %s\n", payload);
      for (int i = 0; i < length; i++) {
        if (payload[i] == '?') break;
        s = payload[i];
      }
      c = payload[length - 1];
      if (s == '0') {
        if(c == '1') digitalWrite(S1, LOW);
        else if(c == '0') digitalWrite(S1, HIGH);
      }
      else if (s == '1') {
        if(c == '1') digitalWrite(S2, LOW);
        else if(c == '0') digitalWrite(S2, HIGH);
      }
      else if (s == '2') {
        if(c == '1') digitalWrite(S3, LOW);
        else if(c == '0') digitalWrite(S3, HIGH);
      }
      else if (s == '3') {
        if(c == '1') digitalWrite(S4, LOW);
        else if(c == '0') digitalWrite(S4, HIGH);
      }
			// send message to server
			// webSocket.sendTXT("message here");
			break;
		case WStype_BIN:
			if (debugMode) Serial.printf("[WSc] get binary length: %u\n", length);
			// hexdump(payload, length);
			// send data to server
			// webSocket.sendBIN(payload, length);
			break;
    case WStype_PING:
      // pong will be send automatically
      if (debugMode) Serial.printf("[WSc] get ping\n");
      break;
    case WStype_PONG:
      // answer to a ping we send
      if (debugMode) Serial.printf("[WSc] get pong\n");
      break;
  }
}
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_PWMServoDriver.h"
#include <Bridge.h>
#include <Console.h>
#include <Wire.h>
#include <YunServer.h>
#include <YunClient.h>

/**
* Globals
*/
Adafruit_MotorShield AFMS = Adafruit_MotorShield();
// Select which 'port' M1, M2, M3 or M4. In this case, M1
Adafruit_DCMotor *frontWheels = AFMS.getMotor(3);
// You can also make another motor on port M2
Adafruit_DCMotor *rearWheels = AFMS.getMotor(4);

const int ledPin = 13; 

// Socket port 5555 to communicate.
YunServer server(5555); 

// Internal settings - Can be changed dynamically from an external application using the "settings" action. 
const char _settingHonkPin = 8;// Cannot be changed by external application.
int settingHonkNote = 440;
int settingHonkDuration = 250;

/**
* Entry point of the program.
* Initialize everything. Called when the Arduino is powered.
*/
void setup() {
  // Bridge startup
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Bridge.begin();

  AFMS.begin();  // create with the default frequency 1.6KHz

  // Set the speed to start, from 70 to 255 (max speed). 0 (off) 
  frontWheels->setSpeed(0); 
  frontWheels->run(FORWARD);
  frontWheels->run(RELEASE);

  //'turning' MOTOR
  rearWheels->setSpeed(0); 
  rearWheels->run(FORWARD);
  rearWheels->run(RELEASE);

  // Listen the entire network (socket communication)
  server.noListenOnLocalhost();
  server.begin();

  Serial.begin(9600);
}

/**
* Has to act as an -infinite- loop.
* Contains the program logic.
* Wait for a client then process it when he's found.
*/
void loop() {
  // Get clients coming from server
  YunClient client = server.accept();

  // There is a new client
  if (client) {
    // Change the predifined timeout from 2000 to 5. Avoid impressive timeout.
    client.setTimeout(5);
    Serial.println("Client connected!");

    // When we get a client, go in the loop and exit only when the client disconnect. This will happens when the android application is killed (the socket must be closed by the app). This will automatically happens from the website for each http request.
    while(client.connected()){	
      // Process request
      process(client);
    }
    // Close connection and free resources.
    client.stop();
  }
  else {
    Serial.println("no client connected, retrying");
  }
  // Delay for the battery, for the debug too. Doesn't affect the response time of the Arduino. (Check if there is another client each second)
  delay(1000);
}

/**
* Will get the command request from the socket/http/etc. entry point.
* Will parse the request and execute it.
*/
void process(YunClient client) {
  // Format: COMMAND/SPEED
  String command = client.readStringUntil('/');// Get the first element of the command.
  
  // Avoid interferences when there is no request. (Because we are in an infinite loop!)
  if(command.length() > 0){
    
    //Serial.println("Query:"+client.readString()); 
    //return;// DEBUG
    
    // Parse the speed.
    int speed = client.parseInt();// Get the second element of the command.
    Serial.println((String) speed);
    if (command == "forward") {
      client.print(F("forward"));
      Serial.println("forward");  
      rearWheels->run(RELEASE);// Stop turn to avoid infinite turn. (Application bug)
      frontWheels->setSpeed(speed);
      frontWheels->run(FORWARD);
    }
    else if (command == "backward") {
      client.print(F("backward"));
      Serial.println("backward"); 
      rearWheels->run(RELEASE);// Stop turn to avoid infinite turn. (Application bug)
      frontWheels->setSpeed(speed);
      frontWheels->run(BACKWARD);
    }
    else if (command == "left") {
      client.print(F("left"));
      Serial.println("left"); 
      rearWheels->setSpeed(speed);// If use speed, doesn't works. (Bad parsing)
      rearWheels->run(BACKWARD);
    }
    else if(command == "right"){
      client.print(F("right"));
      Serial.println("right"); 
      rearWheels->setSpeed(speed);// If use speed, doesn't works. (Bad parsing)
      rearWheels->run(FORWARD);
    }
    else if(command == "stop"){
      client.print(F("stop"));
      Serial.println("stop"); 
      rearWheels->run(RELEASE);
      frontWheels->run(RELEASE);
    }
    else if(command == "stopTurn"){
      client.print(F("stopTurn"));
      Serial.println("stopTurn"); 
      rearWheels->run(RELEASE);// Stop turn but don't do anything more.
    }
    else if(command == "photo"){
      client.print(F("photo"));
      Serial.println("photo"); 
      // TODO Take a photo
    }
    else if(command == "honk"){
      client.print(F("honk"));
      Serial.println("honk"); 
      tone(_settingHonkPin, settingHonkNote, settingHonkDuration);  //(PinNumber, Note, duration)
    }
    else if(command == "settings"){
      client.print(F("settings"));
      Serial.println("settings"); 
	  
	  // Load the custom tone.
      settingHonkNote = client.parseInt();// Get the third element of the command.
    }
  }
}

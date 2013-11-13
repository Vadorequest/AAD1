#include <Adafruit_MotorShield.h>

#include "utility/Adafruit_PWMServoDriver.h"
#include <Bridge.h>
#include <Console.h>
#include <Wire.h>
#include <YunServer.h>
#include <YunClient.h>


Adafruit_MotorShield AFMS = Adafruit_MotorShield();
// Select which 'port' M1, M2, M3 or M4. In this case, M1
Adafruit_DCMotor *frontWheels = AFMS.getMotor(2);
// You can also make another motor on port M2
Adafruit_DCMotor *rearWheels = AFMS.getMotor(1);

const int ledPin = 13; // the pin that the LED is attached to

// We open the socket 5555 to communicate.
YunServer server(5555);

void setup() {
  // Bridge startup
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Bridge.begin();

  AFMS.begin();  // create with the default frequency 1.6KHz

  // Set the speed to start, from 0 (off) to 255 (max speed)
  frontWheels->setSpeed(0);
  frontWheels->run(FORWARD);
  // turn on motor
  frontWheels->run(RELEASE);

  //OTHER MOTOR
  rearWheels->setSpeed(0);
  rearWheels->run(FORWARD);
  // turn on motor
  rearWheels->run(RELEASE);

  server.noListenOnLocalhost();
  server.begin();

  Serial.begin(9600);
  while(!Serial); // wait for a serial connection 
}

void loop() {
  // Get clients coming from server
  YunClient client = server.accept();

  // There is a new client?
  if (client) {
    Serial.println("Client connected!");
  
    while(client.connected()){	
      // Process request
      process(client);
    }

    // Close connection and free resources.
    client.stop();
  }else {
    Serial.println("no client connected, retrying");
  }

  delay(1000);
}

void process(YunClient client) {
  // Format: COMMAND/SPEED
  String command = client.readStringUntil('/');    
  int speed = client.read() == '/' ? client.parseInt() : 0;
  
  if(command.length() > 0){
  Serial.println("Command: "+command);
  Serial.println(client.readString());
    }
  
  
  if (command == "forward") {
    client.print(F("forward"));
	rearWheels->run(RELEASE);
	rearWheels->setSpeed(255);
	rearWheels->run(FORWARD);
  }
  else if (command == "backward") {
    client.print(F("backward"));
	rearWheels->run(RELEASE);
	rearWheels->setSpeed(255);
	rearWheels->run(BACKWARD);
  }
  else if (command == "left") {
    client.print(F("left"));
	frontWheels->run(RELEASE);
	frontWheels->setSpeed(150);
	frontWheels->run(FORWARD);
  }
  else if(command == "right"){
    client.print(F("right"));
	frontWheels->run(RELEASE);
	frontWheels->setSpeed(150);
	frontWheels->run(BACKWARD);
  }
  else if(command == "stop"){
    client.print(F("stop"));
	rearWheels->run(RELEASE);
	frontWheels->run(RELEASE);
  }
  else if(command == "photo"){
    client.print(F("photo"));
	// TODO Take a photo
  }
  else if(command == "honk"){
    client.print(F("honk"));
	// TODO Play a sound
  }
  else if(command == "settings"){
    client.print(F("settings"));
	// TODO Load settings
  }
}


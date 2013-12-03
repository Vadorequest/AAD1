package com.iha.wcc.job.car;

import android.util.Log;

/**
 * Class that represents the current controlled car.
 * Contains all values about it, manage the direction and the speed of the car for each user request.
 */
public class Car {

    /*
     ******************************************* CONSTANTS - Linino hotspot *****************************************
     */
    public final static String DEFAULT_NETWORK_IP = "192.168.240.1";
    public final static int DEFAULT_NETWORK_PORT = 5555;

    /*
     ******************************************* CONSTANTS - Motor rules *****************************************
     */

    /**
     * The minimal motor's speed.
     */
    public final static int MIN_ADAFRUIT_MOTORSHIELD_SPEED = 0;

    /**
     * The maximal motor's speed.
     */
    public final static int MAX_ADAFRUIT_MOTORSHIELD_SPEED = 255;

    /**
     * The minimal acceleration supported by the motor in one time.
     */
    public final static int MIN_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION = MIN_ADAFRUIT_MOTORSHIELD_SPEED;

    /**
     * The maximal acceleration supported by the motor in one time.
     */
    public final static int MAX_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION = MAX_ADAFRUIT_MOTORSHIELD_SPEED;

    /**
     * The minimal deceleration supported by the motor in one time.
     */
    public final static int MIN_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION = MIN_ADAFRUIT_MOTORSHIELD_SPEED;

    /**
     * The maximal deceleration supported by the motor in one time.
     */
    public final static int MAX_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION = MAX_ADAFRUIT_MOTORSHIELD_SPEED;

    /*
     ******************************************* PRIVATE DEFAULT SETTINGS *****************************************
     */

    /**
     * Each time forward is called the speed is increased if the car is going forward.
     */
    private static int speedAccelerationForward = 1;

    /**
     * Each time backward is called the speed is increased if the car is going backward.
     */
    private static int speedAccelerationBackward = 1;

    /**
     * Each time backward is called the speed is decreased if the car is going forward.
     */
    private static int speedDecelerationForward = 2;

    /**
     * Each time forward is called the speed is decreased if the car is going backward.
     */
    private static int speedDecelerationBackward = 2;

    /**
     * Speed is decremented when we turn going forward.
     */
    private static int speedDecTurnForward = 0;

    /**
     * Speed is decremented when we turn going backward.
     */
    private static int speedDecTurnBackward = 0;

    /**
     * Minimal speed available for forward direction.
     */
    private static int minSpeedForward = 25;

    /**
     * Maximal speed available for forward direction.
     */
    private static int maxSpeedForward = 250;

    /**
     * Minimal speed available for backward direction.
     */
    private static int minSpeedBackward = 25;

    /**
     * Maximal speed available for backward direction.
     */
    private static int maxSpeedBackward = 250;

    /**
     * Speed to use when you turn, will change the degree of the forwards wheels.
     */
    private static int speedTurnMotor = 100;

    /*
     ******************************************* VARIABLES *****************************************
     */

    /**
     * Speed of the car.
     */
    public static int speed = minSpeedForward;

    /**
     * Last direction used by the car. Stopped by default.
     */
    public static Direction lastDirection = Direction.STOP;

    /**
     * Last sens where the car was going. Useful to avoid change of sens after a LEFT/RIGHT action.
     */
    public static Direction lastSens = Direction.STOP;

    /**
     * List of available directions.
     */
    public static enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        STOP
    };

    /**
     * Will update the direction automatically once the speed will be calculated.
     * DEFAULT VALUE MUST BE TRUE, for each call to calculateSpeed().
     */
    private static boolean autoUpdateDirection = true;

    /*
     ******************************************* METHODS *****************************************
     */

    /**
     * Change the settings of the car. Check each setting before set to protect the motor engine.
     * If not set, the application will use the default values.
     * @param speedAccelerationForward  Each time forward is called the speed is increased if the car is going forward.
     * @param speedAccelerationBackward Each time backward is called the speed is increased if the car is going backward.
     * @param speedDecelerationForward  Each time backward is called the speed is decreased if the car is going forward.
     * @param speedDecelerationBackward Each time forward is called the speed is decreased if the car is going backward.
     * @param speedDecTurnForward       Speed is decremented when we turn going forward.
     * @param speedDecTurnBackward      Speed is decremented when we turn going backward.
     * @param minSpeedForward           Minimal speed available for forward direction.
     * @param maxSpeedForward           Maximal speed available for forward direction.
     * @param minSpeedBackward          Minimal speed available for backward direction.
     * @param maxSpeedBackward          Maximal speed available for backward direction.
     * @param speedTurnMotor            Speed to use when you turn, will change the degree of the forwards wheels.
     */
    public static void setSettings(int speedAccelerationForward,
                                   int speedAccelerationBackward,
                                   int speedDecelerationForward,
                                   int speedDecelerationBackward,
                                   int speedDecTurnForward,
                                   int speedDecTurnBackward,
                                   int minSpeedForward,
                                   int maxSpeedForward,
                                   int minSpeedBackward,
                                   int maxSpeedBackward,
                                   int speedTurnMotor){

        // Check settings to respects motor rules. The purpose is to protect the motor engine.
        Car.speedAccelerationForward = (speedAccelerationForward < MIN_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION ? MIN_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION : ((speedAccelerationForward > MAX_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION) ? MAX_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION : speedAccelerationForward));
        Car.speedAccelerationBackward = (speedAccelerationBackward < MIN_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION ? MIN_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION : ((speedAccelerationBackward > MAX_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION) ? MAX_ADAFRUIT_MOTORSHIELD_SPEED_ACCELERATION : speedAccelerationBackward));
        Car.speedDecelerationForward = (speedDecelerationForward < MIN_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION ? MIN_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION : ((speedDecelerationForward > MAX_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION) ? MAX_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION : speedDecelerationForward));
        Car.speedDecelerationBackward = (speedDecelerationBackward < MIN_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION ? MIN_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION : ((speedDecelerationBackward > MAX_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION) ? MAX_ADAFRUIT_MOTORSHIELD_SPEED_DECELERATION : speedDecelerationBackward));
        Car.speedDecTurnForward = speedDecTurnForward;
        Car.speedDecTurnBackward = speedDecTurnBackward;
        Car.minSpeedForward = minSpeedForward > MIN_ADAFRUIT_MOTORSHIELD_SPEED ? minSpeedForward : MIN_ADAFRUIT_MOTORSHIELD_SPEED;
        Car.maxSpeedForward = maxSpeedForward <= MAX_ADAFRUIT_MOTORSHIELD_SPEED ? maxSpeedForward : MAX_ADAFRUIT_MOTORSHIELD_SPEED;
        Car.minSpeedBackward = minSpeedBackward > MIN_ADAFRUIT_MOTORSHIELD_SPEED ? minSpeedBackward : MIN_ADAFRUIT_MOTORSHIELD_SPEED;
        Car.maxSpeedBackward = maxSpeedBackward <= MAX_ADAFRUIT_MOTORSHIELD_SPEED ? maxSpeedBackward : MAX_ADAFRUIT_MOTORSHIELD_SPEED;
        Car.speedTurnMotor = speedTurnMotor < MAX_ADAFRUIT_MOTORSHIELD_SPEED ? (speedTurnMotor > MIN_ADAFRUIT_MOTORSHIELD_SPEED ? speedTurnMotor : MIN_ADAFRUIT_MOTORSHIELD_SPEED) : MAX_ADAFRUIT_MOTORSHIELD_SPEED;
    }

    /**
     * Calculate the new speed.
     * @param direction The new direction of the car.
     *                  Not really the direction, but the button pressed, for instance, press the FORWARD button car be done in the BACKWARD sens, in this case, it will just slow down the car, not change the sens.
     * @return String   The last direction calculated.
     */
    public static String calculateSpeed(Direction direction) {
        // If we ask to stop, just stop.
        if(direction == Direction.STOP){
            _stop();
            return _formatLastDirection();
        }

        // Else it's a little more funny. (Wrote at 2 a.m)
        if(direction == lastDirection){
            // We keep the same direction.
            switch (direction){
                case FORWARD :
                    _accelerate(speedAccelerationForward, minSpeedForward, maxSpeedForward);
                    break;
                case BACKWARD :
                    _accelerate(speedAccelerationBackward, minSpeedBackward, maxSpeedBackward);
                    break;
                case LEFT :
                case RIGHT :
                    // Speed still the same.
                    break;
            }
        }else{
            // Depending on the last direction used.
            switch (lastDirection){
                case FORWARD :
                    if(direction == Direction.BACKWARD){
                        // Deceleration going forward.
                        _decelerate(speedDecelerationForward, minSpeedForward, minSpeedBackward);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turn(speedDecTurnForward, minSpeedForward);
                    }
                    break;
                case BACKWARD :
                    if(direction == Direction.FORWARD){
                        // Deceleration going backward.
                        _decelerate(speedDecelerationBackward, minSpeedBackward, minSpeedForward);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turn(speedDecTurnBackward, minSpeedBackward);
                    }
                    break;
                case LEFT :
                case RIGHT :
                    // Increase the speed depending of the sens used before TURN.
                    switch (direction){
                        case FORWARD :
                            // We are going forward.
                            if(lastSens == Direction.FORWARD){
                                // We accelerate in the same sens (going forward), after turned.
                                _accelerate(speedAccelerationForward, minSpeedForward, maxSpeedForward);
                            }else if(lastSens == Direction.BACKWARD){
                                // We decelerate in the same sens (going forward), after turned. (but could change the sens)
                                _decelerate(speedDecelerationForward, minSpeedForward, minSpeedBackward, lastSens);
                            }
                            break;
                        case BACKWARD :
                            // We are going backward.
                            if(lastSens == Direction.BACKWARD){
                                // We accelerate in the same sens (going backward), after turned.
                                _accelerate(speedAccelerationBackward, minSpeedBackward, maxSpeedBackward);
                            }else if(lastSens == Direction.FORWARD){
                                // We decelerate in the same sens (going backward), after turned. (but could change the sens)
                                _decelerate(speedDecelerationBackward, minSpeedBackward, minSpeedForward, lastSens);
                            }
                            break;
                    }
                    break;
                case STOP :
                    // Increase the speed depending of the direction if we are going forward or backward.
                    switch (direction){
                        case FORWARD :
                            _accelerate(speedAccelerationForward, minSpeedForward, maxSpeedForward);
                            break;
                        case BACKWARD :
                            _accelerate(speedAccelerationBackward, minSpeedBackward, maxSpeedBackward);
                            break;
                    }
                    break;
            }
        }

        // Update the last direction used.
        if(autoUpdateDirection){
            _saveNewDirection(direction);
        }else{
            // Re init to true for the next time. (Default TRUE)
            autoUpdateDirection = true;
        }
        /*Log.d("Car", "-----------------");
        Log.d("Car", "Sens:"+lastSens);
        Log.d("Car", "Direction:"+lastDirection);
        Log.d("Car", "Speed:"+speed);*/
        // Return the new speed to use.
        return _formatLastDirection();
    }

    /**
     * Increase the speed depending on the sens of the car.
     * @param speedAcceleration Value of the speed acceleration.
     * @param maxSpeed Minimal speed value.
     * @param maxSpeed Maximal speed value.
     */
    private static void _accelerate(int speedAcceleration, int minSpeed, int maxSpeed) {
        if(speed + speedAcceleration >= maxSpeed){
            // Don't exceed the max value.
            speed = maxSpeed;
        }else if(speed + speedAcceleration < minSpeed){
            // Don't exceed the min value.
            speed = minSpeed;
        }else if(speed + speedAcceleration < maxSpeed){
            // Increase the speed.
            speed += speedAcceleration;
        }
    }

    /**
     * Decrease the speed depending on the sens of the car. Can also change the sens of the car.
     * @param speedDeceleration Value of the speed deceleration.
     * @param minSpeed Minimal speed to keep.
     * @param minSpeedOppositeSens Minimal speed to use if we change the sens of the car.
     * @param newDirection New direction to use. [lastDirection]
     */
    private static void _decelerate(int speedDeceleration, int minSpeed, int minSpeedOppositeSens, Direction newDirection) {
        if(speed - speedDeceleration < minSpeed){
            // If we want to change the sens of the car.
            speed = minSpeedOppositeSens;

        }else if(speed - speedDeceleration >= minSpeed){
            // We decelerate going backward.
            speed -= speedDeceleration;

            // Don't change the sens of the car, we just decelerate.
            _saveNewDirection(newDirection);

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            autoUpdateDirection = false;
        }
    }

    /**
     * Decrease the speed depending on the sens of the car. Can also change the sens of the car.
     * @param speedDeceleration Value of the speed deceleration.
     * @param minSpeed Minimal speed to keep.
     * @param minSpeedOppositeSens Minimal speed to use if we change the sens of the car.
     */
    private static void _decelerate(int speedDeceleration, int minSpeed, int minSpeedOppositeSens) {
        _decelerate(speedDeceleration, minSpeed, minSpeedOppositeSens, lastDirection);
    }

    /**
     * Update the speed when turning depending on the sens of the car.
     * @param speedDecTurn The value of the speed to decrement before turn.
     * @param minSpeed Minimal speed to keep.
     */
    private static void _turn(int speedDecTurn, int minSpeed) {
        if(speed - speedDecTurn > minSpeed){
            speed -= speedDecTurn;
        }else{
            speed = minSpeed;
        }
    }

    /**
     * Stop the car.
     * @return int The actual speed of the car.
     */
    private static int _stop(){
        // Stop the car.
        speed = 0;

        // Save the new direction.
        _saveNewDirection(Direction.STOP);

        // Return the actual speed. (calculateSpeed method compatibility return type)
        return speed;
    }

    /**
     * Update the lastDirection for the next action.
     * @param direction The new direction of the car.
     */
    private static void _saveNewDirection(Direction direction){
        lastDirection = direction;
        _saveNewSens();
    }

    /**
     * Update the lastSens when the lastDirection is a sens. (Not a simple direction/action)
     */
    private static void _saveNewSens() {
        // If the last direction was a sens, refresh it.
        if(lastDirection == Direction.BACKWARD || lastDirection == Direction.FORWARD){
            lastSens = lastDirection;
        }
    }

    /**
     * Format the lastDirection calculated by the program.
     * @return The last direction converted in string and toLowerCase.
     */
    private static String _formatLastDirection(){
        return lastDirection.toString().toLowerCase();
    }

    /*
     ******************************************* GETTERS *****************************************
     */


    /**
     * Each time forward is called the speed is increased if the car is going forward.
     */
    public static int getSpeedAccelerationForward() {
        return speedAccelerationForward;
    }

    /**
     * Each time backward is called the speed is increased if the car is going backward.
     */
    public static int getSpeedAccelerationBackward() {
        return speedAccelerationBackward;
    }

    /**
     * Each time backward is called the speed is decreased if the car is going forward.
     */
    public static int getSpeedDecelerationForward() {
        return speedDecelerationForward;
    }

    /**
     * Each time forward is called the speed is decreased if the car is going backward.
     */
    public static int getSpeedDecelerationBackward() {
        return speedDecelerationBackward;
    }

    /**
     * Speed is decremented when we turn going forward.
     */
    public static int getSpeedDecTurnForward() {
        return speedDecTurnForward;
    }

    /**
     * Speed is decremented when we turn going backward.
     */
    public static int getSpeedDecTurnBackward() {
        return speedDecTurnBackward;
    }

    /**
     * Minimal speed available for forward direction.
     */
    public static int getMinSpeedForward() {
        return minSpeedForward;
    }

    /**
     * Maximal speed available for forward direction.
     */
    public static int getMaxSpeedForward() {
        return maxSpeedForward;
    }

    /**
     * Minimal speed available for backward direction.
     */
    public static int getMinSpeedBackward() {
        return minSpeedBackward;
    }

    /**
     * Maximal speed available for backward direction.
     */
    public static int getMaxSpeedBackward() {
        return maxSpeedBackward;
    }

    /**
     * Speed to use when you turn, will change the degree of the forwards wheels.
     */
    public static int getSpeedTurnMotor() {
        return speedTurnMotor;
    }
}

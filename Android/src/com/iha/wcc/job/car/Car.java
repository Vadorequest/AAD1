package com.iha.wcc.job.car;

/**
 * Class that represents the current controlled car.
 * Contains all values about it, manage the direction and the speed of the car for each user request.
 */
public class Car {

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
     ******************************************* PRIVATE SETTINGS *****************************************
     */

    /**
     * Each time forward is called the speed is increased if the car is going forward.
     */
    private static int speedAccelerationForward = 2;

    /**
     * Each time backward is called the speed is increased if the car is going backward.
     */
    private static int speedAccelerationBackward = 2;

    /**
     * Each time backward is called the speed is decreased if the car is going forward.
     */
    private static int speedDecelerationForward = 4;

    /**
     * Each time forward is called the speed is decreased if the car is going backward.
     */
    private static int speedDecelerationBackward = 4;

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
    private static int minSpeedForward = 70;

    /**
     * Maximal speed available for forward direction.
     */
    private static int maxSpeedForward = 255;

    /**
     * Minimal speed available for backward direction.
     */
    private static int minSpeedBackward = 100;

    /**
     * Maximal speed available for backward direction.
     */
    private static int maxSpeedBackward = 150;

    /*
     ******************************************* VARIABLES *****************************************
     */

    /**
     * Speed of the car.
     */
    public static int speed = 100;

    /**
     * Last direction used by the car. Stopped by default.
     */
    public static Direction lastDirection = Direction.STOP;

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

    /*
     ******************************************* METHODS *****************************************
     */

    /**
     * Change the settings of the car. Check each setting before set to protect the motor engine.
     * @param speedAccelerationForward
     * @param speedAccelerationBackward
     * @param speedDecelerationForward
     * @param speedDecelerationBackward
     * @param speedDecTurnForward
     * @param speedDecTurnBackward
     * @param minSpeedForward
     * @param maxSpeedForward
     * @param minSpeedBackward
     * @param maxSpeedBackward
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
                                   int maxSpeedBackward){

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
    }

    /**
     * Calculate the new speed.
     * @param direction The new direction of the car.
     *                  Not really the direction, but the button pressed, for instance, press the FORWARD button car be done in the BACKWARD sens, in this case, it will just slow down the car, not change the sens.
     * @return String   The last direction calculated.
     */
    public static String calculateSpeed(Direction direction) {
        // Will update the direction automatically once the speed will be calculated.
        boolean autoUpdateDirection = true;

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
                    _accelerate(speedAccelerationForward, maxSpeedForward);
                    break;
                case BACKWARD :
                    _accelerate(speedAccelerationBackward, maxSpeedBackward);
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
                        autoUpdateDirection = _decelerate(speedDecelerationForward, minSpeedForward, minSpeedBackward, lastDirection);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turn(speedDecTurnForward, minSpeedForward);
                    }
                    break;
                case BACKWARD :
                    if(direction == Direction.FORWARD){
                        // Deceleration going backward.
                        autoUpdateDirection = _decelerate(speedDecelerationBackward, minSpeedBackward, minSpeedForward, lastDirection);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turn(speedDecTurnBackward, minSpeedBackward);
                    }
                    break;
                case LEFT :
                case RIGHT :
                case STOP :
                    // Increase the speed depending of the direction if we are going forward or backward.
                    switch (direction){
                        case FORWARD :
                            _accelerate(speedAccelerationForward, maxSpeedForward);
                            break;
                        case BACKWARD :
                            _accelerate(speedAccelerationBackward, maxSpeedBackward);
                            break;
                    }
                    break;
            }
        }

        // Update the last direction used.
        if(autoUpdateDirection){
            _saveNewDirection(direction);
        }

        // Return the new speed to use.
        return _formatLastDirection();
    }

    /**
     * Increase the speed depending on the sens of the car.
     * @param speedAcceleration Value of the speed acceleration.
     * @param maxSpeed Maximal speed value.
     */
    private static void _accelerate(int speedAcceleration, int maxSpeed) {
        if(speed + speedAcceleration < maxSpeed){
            speed += speedAcceleration;
        }else{
            speed = maxSpeed;
        }
    }

    /**
     * Decrease the speed depending on the sens of the car.
     * @param speedDeceleration Value of the speed deceleration.
     * @param minSpeed Minimal speed to keep.
     * @param minSpeedOppositeSens Minimal speed to use if we change the sens of the car.
     * @param lastDirection Direction used during the last time.
     * @return boolean If true that means the script didn't force the direction and it has to be auto updated.
     */
    private static boolean _decelerate(int speedDeceleration, int minSpeed, int minSpeedOppositeSens, Direction lastDirection) {
        if(speed - speedDeceleration < minSpeed){
            // If we want to change the sens of the car.
            speed = minSpeedOppositeSens;
            return true;

        }else if(speed - speedDeceleration >= minSpeed){
            // We decelerate going backward.
            speed -= speedDeceleration;

            // Don't change the sens of the car, we just decelerate.
            _saveNewDirection(lastDirection);

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            return false;

        }else{
            return true;
        }
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
}

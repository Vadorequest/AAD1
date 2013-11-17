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
    private static int speedAccelerationForward = 5;

    /**
     * Each time backward is called the speed is increased if the car is going backward.
     */
    private static int speedAccelerationBackward = 2;

    /**
     * Each time backward is called the speed is decreased if the car is going forward.
     */
    private static int speedDecelerationForward = 10;

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
    private static int minSpeedForward = 5;

    /**
     * Maximal speed available for forward direction.
     */
    private static int maxSpeedForward = 255;

    /**
     * Minimal speed available for backward direction.
     */
    private static int minSpeedBackward = 2;

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
    public static int speed = 0;

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
     * @param direction The direction of the car.
     */
    public static int calculateSpeed(Direction direction) {
        // Will update the direction automatically once the speed will be calculated.
        boolean autoUpdateDirection = true;

        // If we ask to stop, just stop.
        if(direction == Direction.STOP){
            return _stop();
        }

        // Else it's a little more funny. (Wrote at 2 a.m)
        if(direction == lastDirection){
            // We keep the same direction.
            switch (direction){
                case FORWARD :
                    _accelerateForward();
                    break;
                case BACKWARD :
                    _accelerateBackward();
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
                        autoUpdateDirection = _decelerateForward(lastDirection, direction);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turnForward();
                    }
                    break;
                case BACKWARD :
                    if(direction == Direction.FORWARD){
                        // Deceleration going backward.
                        autoUpdateDirection = _decelerateBackward(lastDirection, direction);
                    } else if (direction == Direction.LEFT || direction == Direction.RIGHT){
                        // If we turn going forward.
                        _turnBackward();
                    }
                    break;
                case LEFT :
                case RIGHT :
                case STOP :
                    // Increase the speed depending of the direction if we are going forward or backward.
                    switch (direction){
                        case FORWARD :
                            _accelerateForward();
                            break;
                        case BACKWARD :
                            _accelerateBackward();
                            break;
                    }
                    break;
            }
        }

        // Update the last direction used.
        if(autoUpdateDirection){
            saveNewDirection(direction);
        }

        // Return the new speed to use.
        return speed;
    }

    /**
     * Increase the speed going forward.
     */
    private static void _accelerateForward(){
        if(speed + speedAccelerationForward < maxSpeedForward){
            speed += speedAccelerationForward;
        }else{
            speed = maxSpeedForward;
        }
    }

    /**
     * Increase the speed going backward.
     */
    private static void _accelerateBackward() {
        if(speed + speedAccelerationBackward < maxSpeedBackward){
            speed += speedAccelerationBackward;
        }else{
            speed = maxSpeedBackward;
        }
    }

    /**
     * Decrease the speed going forward.
     */
    private static boolean _decelerateForward(Direction lastDirection, Direction newDirection) {
        if(speed - speedDecelerationForward < 0){
            // If we want to change the sens of the car.
            speed = minSpeedBackward;
            return true;

        }else if(speed - speedDecelerationForward > 0){
            // We decelerate going forward.
            speed -= speedDecelerationForward;

            // Don't change the sens of the car, we just decelerate.
            saveNewDirection(lastDirection);

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            return false;

        }else{
            // We stop the car.
            _stop();

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            return false;
        }
    }

    /**
     * Decrease the speed going backward.
     */
    private static boolean _decelerateBackward(Direction lastDirection, Direction newDirection) {
        if(speed - speedDecelerationBackward < 0){
            // If we want to change the sens of the car.
            speed = minSpeedBackward;
            return true;

        }else if(speed - speedDecelerationBackward > 0){
            // We decelerate going backward.
            speed -= speedDecelerationBackward;

            // Don't change the sens of the car, we just decelerate.
            saveNewDirection(lastDirection);

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            return false;

        }else{
            // We stop the car.
            _stop();

            // We changed manually the direction, don't auto update the direction. (It would be wrong)
            return false;
        }
    }

    /**
     * Update the speed when turning forward.
     */
    private static void _turnForward() {
        if(speed - speedDecTurnForward > minSpeedForward){
            speed -= speedDecTurnForward;
        }else{
            speed = minSpeedForward;
        }
    }

    /**
     * Update the speed when turning backward.
     */
    private static void _turnBackward() {
        if(speed - speedDecTurnBackward > minSpeedBackward){
            speed -= speedDecTurnBackward;
        }else{
            speed = minSpeedBackward;
        }
    }

    /**
     * Stop the car.
     * @return speed The actual speed of the car.
     */
    private static int _stop(){
        // Stop the car.
        speed = 0;

        // Save the new direction.
        saveNewDirection(Direction.STOP);

        // Return the actual speed. (calculateSpeed method compatibility return type)
        return speed;
    }

    /**
     * Update the lastDirection for the next action.
     * @param direction The new direction of the car.
     */
    private static void saveNewDirection(Direction direction){
        lastDirection = direction;
    }

    /*
     ******************************************* GETTERS *****************************************
     */

    public static int getMinSpeedBackward() {
        return minSpeedBackward;
    }

    public static int getSpeedAccelerationForward() {
        return speedAccelerationForward;
    }

    public static int getSpeedAccelerationBackward() {
        return speedAccelerationBackward;
    }

    public static int getSpeedDecelerationForward() {
        return speedDecelerationForward;
    }

    public static int getSpeedDecelerationBackward() {
        return speedDecelerationBackward;
    }

    public static int getSpeedDecTurnForward() {
        return speedDecTurnForward;
    }

    public static int getSpeedDecTurnBackward() {
        return speedDecTurnBackward;
    }

    public static int getMaxSpeedForward() {
        return maxSpeedForward;
    }

    public static int getMinSpeedForward() {
        return minSpeedForward;
    }

    public static int getMaxSpeedBackward() {
        return maxSpeedBackward;
    }
}

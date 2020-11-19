/**
 * Diffraction Calculator Class
 * <br>
 * @author Nick Vilimek
 * @version 1.0.0
 * <br>
 * This class stores all the data variable required for a light diffraction
 * experiment including wavelength, the width of the slits, distance from the slit to the screen it
 * is being projected on, whether there are one or two slits, and what the separation between two
 * slits would be. It allows for accessing and mutating all of these fields.
 * <br>
 * The class also calculates the relative intensity pattern based on the values of these
 * diffraction input fields based on the formulas of Fraunhofer Diffraction. It also maps the
 * intensity values to the desired height to make a 2 dimensional graph and maps r,g,b values
 * which help dictate bright and dark spots in an intensity color map.
 */

public class DiffractionCalculator {

    private double slitWidth;
    private double distanceFromScreen;
    private double wavelength;
    private double numberSlits;
    private double distanceBetweenSlits;

    private double[] inputValues;
    private double[] outputValues;
    private final int inputLength = 1501;

    /**
     * The Constructor for this custom class which initializes the slitWidth,
     * distance to the screen, wavelength, the amount of slits and the separation
     * between slits if there are more than one
     * <br>
     * This function also initializes an array which holds 1501 x-coordinate values
     * ranging from -1.501 to 1.501 and increment by .001
     * @param _sWidth - width of the slit or slits
     * @param _distanceToScreen - distance from the slit to the screen
     * @param _wl - wavelength of the light
     * @param _slitNum - defines whether there are one or two slits in the diffraction
     * @param _slitsDistance - defines the distance between the slits if two slits exist
     */

    public DiffractionCalculator(double _sWidth, double _distanceToScreen, double _wl, double _slitNum, double _slitsDistance){
        slitWidth = _sWidth;
        distanceFromScreen = _distanceToScreen;
        wavelength = _wl;
        numberSlits = _slitNum;
        distanceBetweenSlits = _slitsDistance;
        inputValues = new double[inputLength];
        outputValues = new double[inputLength];

        for(int i=0;i<inputLength/2;i++){
            inputValues[i] = (((-inputLength+(2*(double)i))/1000));
            inputValues[inputValues.length-i-1]=(((inputLength+(2*(double)-i)))/1000);
        }
        inputValues[inputValues.length/2] = 0;
    }

    /**
     * Takes the private field outputValues and initializes its values to the result
     * of the calculation preformed in the follow function with the the corresponding
     * input as the variable of the function.
     */
    public void CalculateOutput(){
        for(int i=0;i<inputValues.length;i++){
            outputValues[i]=CalculateIntensity(inputValues[i]);
        }

    }

    /**
     * Takes an x-value as input for this mathematical trigonometric function and computes
     * the corresponding intensity using the data fields necessary for the diffraction
     * experiment.
     * @param xVal - x coordinate which is the variable in the function.
     * @return the computed intensity value for the given input x-value
     */
    private double CalculateIntensity(double xVal){
        double betaVal = (Math.PI*xVal*slitWidth)/(wavelength*distanceFromScreen);
        double val = (Math.sin(betaVal))/betaVal; //Can't divide by 0: handled in next line
        val = (Double.isNaN(val))?1:val*val;
        if(this.numberSlits==2){
            double twoSlitVal = Math.cos((Math.PI*distanceBetweenSlits*xVal)/(wavelength*distanceFromScreen));
            val*= twoSlitVal *twoSlitVal;
        }
        return  val;
    }

    /**
     * Takes the calculators computed intensity output functions and converts (maps)
     * them to values which are represented by different ranges in order that the
     * output values can be represented by charts and graphs.
     * <br>
     * This is accomplished by having a double array of doubles variable called
     * mappedValues in which the first and second array (mappedValues[0] and mappedValues[1])
     * stores the x and y values mapped to range of 0 to the width parameter and 0 to the
     * height parameter respectively. The third array in the mappedValues array
     * (mappedValues[2]) contains the intensity values mapped to a number between 0-255 which
     * represent either a red, green, or blue value.
     * <br>
     * The x coordinate is computed using the formula
     * (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min; see {@linktourl https://www.arduino.cc/en/Reference/Map}
     * <br>
     * The y coordinate is simply computed by finding taking the max height and multiplying it by the intensity value.
     * However, since the y - coordinate start with 0 at the top, the mapped value must be subtracted by the total height
     * so that the graph won't be upside down.
     * <br>
     * The r,g,b value is computed by the product of the intensity and the total 255 max value.
     *
     * @param width - maximum width of the area the graph will be displayed in
     * @param height - maximum height of the area the graph will be displayed in
     * @return double array of doubles with mapped x, y, and r,g,b values.
     */

    public double[][] MapValues(double width,double height){
        double[][] mappedValues = new double[3][inputLength];
        double xMax = inputLength/1000.0;
        double xMin = -xMax;

        for(int i=0;i<inputLength;i++){
            mappedValues[0][i] = ((inputValues[i]-xMin)* width)/(xMax-xMin);
            mappedValues[1][i] = height-(outputValues[i] * height);
            mappedValues[2][i] = outputValues[i] * 255;
        }
        return  mappedValues;
    }

    /**
     * Calculates the distance between the first and second peaks in the diffraction
     * using formulas contingent on the number of slits
     *
     * @return - the value of the 1st to 2nd order peak difference
     */

    public double getFirstDiffractionDistance(){
        if (numberSlits == 1) {
            return (distanceFromScreen * wavelength) / slitWidth;
        } else {
            return (0.5 * distanceFromScreen * wavelength) / distanceBetweenSlits;
        }
    }
    /**
     * Returns the calculators slit width
     *
     * @return slit width
     */
    public double getSlitWidth() {
        return slitWidth;
    }

    /**
     * Set the width of the slit
     *
     * @param slitWidth - value for the width to be set too
     */
    public void setSlitWidth(double slitWidth) {
        this.slitWidth = slitWidth;
    }

    /**
     * Returns the distance from the slit to the screen
     *
     * @return double value in millimeters of slit to screen
     */
    public double getDistanceFromScreen() {
        return distanceFromScreen;
    }

    /**
     * sets distance from slit to screen
     *
     * @param distanceFromScreen - distance value in mm
     */
    public void setDistanceFromScreen(double distanceFromScreen) {
        this.distanceFromScreen = distanceFromScreen;
    }

    /**
     * returns wavelength in mm
     * @return - wavelength value
     */
    public double getWavelength() {
        return wavelength;
    }

    /**
     * Sets the wavelength value
     * @param wavelength - wavelength value
     */
    public void setWavelength(double wavelength) {
        this.wavelength = wavelength;
    }

    /**
     * returns the number of slits
     * @return - slit number value
     */
    public double getNumberSlits() {
        return numberSlits;
    }

    /**
     * Sets the amount of slits
     *
     * @param numberSlits - either a 1 or 2 for the number of slits being used
     */
    public void setNumberSlits(double numberSlits) {
        this.numberSlits = numberSlits;
    }

    /**
     * Returns the slit separation distance
     * @return separation vale
     */
    public double getDistanceBetweenSlits() {
        return distanceBetweenSlits;
    }

    /**
     * sets the separation between slits
     *
     * @param distanceBetweenSlits - separation value
     */
    public void setDistanceBetweenSlits(double distanceBetweenSlits) {
        this.distanceBetweenSlits = distanceBetweenSlits;
    }

    /**
     * Returns raw intensity output values
     *
     * @return array of intensity values
     */
    public double[] getOutputValues() {
        return outputValues;
    }

    /**
     * Returns the length of the input/output arrays
     *
     * @return integer representing the array length
     */
    public int getInputLength() {
        return inputLength;
    }
}

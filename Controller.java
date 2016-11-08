import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

/**
 * Controller of the fxml form objects and handles the model and view
 * interaction
 *
 */
public class Controller implements Initializable{

    DiffractionCalculator calculator;

    public Pane graph,intensityMap,apertureGraph;
    public TextField wavelengthTextArea, separationTextArea, widthTextArea, distanceTextArea;
    public Slider wavelengthSlider, separationSlider, widthSlider, distanceSlider;
    public Button separationBtn;
    public RadioButton singleBtn, doubleBtn;
    public Text diffractionDifferenceText;
    public Color c = Color.BLUE;
    public ImageView img;
    public Image i;


    /**
     * Override function that allows the controller to be initialized
     * and have logic preformed on the setup of the GUI
     * <br>
     * This function handles saving and printing of the graphs,
     * adds listeners to the sliders, and sets up the custom class
     * DiffractionCalculator which handles the user input
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) { //Initializes interface

        //Sets up the image right click menu's
        ContextMenu contextMenu = new ContextMenu();
        MenuItem print = new MenuItem("Print");
        MenuItem save = new MenuItem("Save");
        contextMenu.getItems().addAll(print, save);
        //Sets up the file chooser and printer settings for the save and print functionality
        save.setOnAction((event)->{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"),
                    new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp"),
                    new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg"));
            File file = fileChooser.showSaveDialog(graph.getScene().getWindow());
            if (file != null) {
                WritableImage image = contextMenu.getOwnerNode().snapshot(new SnapshotParameters(), null);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        print.setOnAction((event)->{
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if(printerJob.showPrintDialog(graph.getScene().getWindow()) && printerJob.printPage(contextMenu.getOwnerNode()))
                printerJob.endJob();
        });
        //Listeners on each graph for right clicks
        graph.setOnMouseClicked((event) -> { //Allows for right click to save and print graphs
            if(event.getButton()== MouseButton.SECONDARY || event.isControlDown())
                contextMenu.show(graph,event.getScreenX(),event.getScreenY());
        });
        intensityMap.setOnMouseClicked((event) -> {
            if(event.getButton()== MouseButton.SECONDARY || event.isControlDown())
                contextMenu.show(intensityMap,event.getScreenX(),event.getScreenY());
        });
        apertureGraph.setOnMouseClicked((event) -> {
            if(event.getButton()== MouseButton.SECONDARY || event.isControlDown())
                contextMenu.show(apertureGraph,event.getScreenX(),event.getScreenY());
        });
        //Listeners to update values as slider moves
        wavelengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {OnWavelengthSliderChanged();});
        separationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {OnSeparationSliderChanged();});
        widthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {OnSlitWidthChanged();});
        distanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {OnDistanceSliderChanged();});

        //Custom class instance that holds and calculates the data
        calculator = new DiffractionCalculator(widthSlider.getValue(),distanceSlider.getValue(),wavelengthSlider.getValue()/1000000,1,separationSlider.getValue());
    }
    //Draws all graphs including diffraction pattern, intensity map, aperture, and visualization

    /**
     * Draws all the graphs in their respective panes, sets the overhead image
     * of the simulation, and sets the text of the difference between the peaks
     * of the wave.
     */
    public void drawGraphs(){
        //Clears old graphs before drawing graphs
        graph.getChildren().clear();
        intensityMap.getChildren().clear();
        apertureGraph.getChildren().clear();
        //Calculates outputs for graphs based on slider values
        calculator.CalculateOutput();
        //Array of mapped values - mappedValues[0] = x coordinates, mappedValues[1] = y coordinates, and mappedValues[2]=r,g,b values
        double[][] mappedValues = calculator.MapValues(graph.widthProperty().get(),graph.heightProperty().get());

        double lineWidth = intensityMap.widthProperty().get()/mappedValues[0].length;
        double xPos = 0;

        Line intensityLine;
        Line graphLine;
        //Generates both the intensity map and the graph at through the same for loop
        for(int i=0;i<mappedValues[0].length-1;i++){
            intensityLine= new Line(xPos,intensityMap.getHeight(),xPos+lineWidth,0);
            graphLine = new Line(mappedValues[0][i],mappedValues[1][i],mappedValues[0][i+1],mappedValues[1][i+1]);
            graphLine.setStrokeWidth(1);

            if(c == Color.BLUE) {
                intensityLine.setStroke(Color.rgb(0,0,((int) (mappedValues[2][i]))));
                graphLine.setStroke(Color.BLUE);
            } else if(c == Color.GREEN){
                intensityLine.setStroke(Color.rgb(0,((int) (mappedValues[2][i])),0));
                graphLine.setStroke(Color.GREEN);
            }else {
                intensityLine.setStroke(Color.rgb(((int) (mappedValues[2][i])), 0, 0));
                graphLine.setStroke(Color.RED);
            }
            xPos+=lineWidth;
            intensityMap.getChildren().add(intensityLine);

            graph.getChildren().add(graphLine);
        }
        //Sets up the aperture representation
        double _middle = (apertureGraph.widthProperty().get()/2.0); //Puts the line the in the middle of the screen
        double _width = apertureGraph.widthProperty().get()/30.0; //Arbitrary proportion of the pane
        double _distance = apertureGraph.widthProperty().get()/20.0;

        if(calculator.getNumberSlits()==1){ //Generates aperture visualization
            Line line = new Line(_middle,apertureGraph.heightProperty().get(),_middle,0);
            line.setStroke(c);
            line.setStrokeWidth(_width*calculator.getSlitWidth());
            apertureGraph.getChildren().add(line);
        }else{
            double distanceFromMiddle = _distance*calculator.getDistanceBetweenSlits()/2;
            Line line1 = new Line(_middle-distanceFromMiddle,apertureGraph.heightProperty().get(),_middle-distanceFromMiddle,0);
            line1.setStroke(c);
            line1.setStrokeWidth(_width*calculator.getSlitWidth());
            Line line2 = new Line(_middle+distanceFromMiddle,apertureGraph.heightProperty().get(),_middle+distanceFromMiddle,0);
            line2.setStroke(c);
            line2.setStrokeWidth(_width*calculator.getSlitWidth());
            apertureGraph.getChildren().addAll(line1,line2);
        }
        //Set the Diffraction overhead image based on wavelength and slit amount
        if(c == Color.BLUE){
            if(singleBtn.isSelected())
                i = new Image("res/BlueSingle.jpg");
            else
                i = new Image("res/BlueDouble.jpg");
        }
        else if(c == Color.RED) {
            if(singleBtn.isSelected())
                i = new Image("res/RedSingle.jpg");
            else
                i = new Image("res/RedDouble.jpg");
        }
        else{
            if(singleBtn.isSelected())
                i = new Image("res/GreenSingle.jpg");
            else
                i = new Image("res/GreenDouble.jpg");
        }
        img.setImage(i);
        //Sets the distance between peaks to a label
        NumberFormat formatter = new DecimalFormat("#.000000");
        diffractionDifferenceText.setText("Diffraction Peak Distance: "+ formatter.format(calculator.getFirstDiffractionDistance()));
    }

    /**
     * Sets the color of the graphs based on the wavelength.
     */
    public void setColor() {
        if(wavelengthSlider.getValue() >= 400 && wavelengthSlider.getValue() <= 500) {
            c = Color.BLUE;
        }
        else if (wavelengthSlider.getValue() > 500 && wavelengthSlider.getValue() <= 600){
            c = Color.GREEN;
        }
        else{
            c = Color.RED;
        }
    }
    //Event Handlers

    public void OnWavelengthButtonClicked(){ //Enter button for wavelength is clicked
        try { //Catches values greater than or less than accepted values, including NaNs, sets slider value to text value
            double txtVal = Double.parseDouble(wavelengthTextArea.getText());
            if(txtVal > 700) {
                wavelengthSlider.setValue(700);
                wavelengthTextArea.setText("700");
            }
            else if (txtVal < 400) {
                wavelengthSlider.setValue(400);
                wavelengthTextArea.setText("400");
            }
            else {
                wavelengthSlider.setValue(txtVal);
            }
        }
        catch (NumberFormatException e) {
            wavelengthSlider.setValue(400);
            wavelengthTextArea.setText("400");
        }
        calculator.setWavelength(wavelengthSlider.getValue()/1000000.0); //These 3 calls set changes to graphs based on wavelength changes
        drawGraphs();
        setColor();
    }
    public void OnWavelengthSliderChanged(){ //When slider for wavelength is dragged
        double slideVal = wavelengthSlider.getValue();
        String slideValTxt = Double.toString(slideVal);
        wavelengthTextArea.setText(slideValTxt); //Updates text field based on slider
        calculator.setWavelength(slideVal/1000000); //Change graphs based on wavelength change
        drawGraphs();
        setColor();
    }

    public void onSingleBtn() {//Single slit button selected, disables slit separation (not needed when single) and changes graphs
        separationSlider.setDisable(true);
        separationTextArea.setDisable(true);
        separationBtn.setDisable(true);
        calculator.setNumberSlits(1);
        drawGraphs();
    }

    public void onDoubleBtn() {//Double slit button selected, enables slit separation and changes graphs
        separationSlider.setDisable(false);
        separationTextArea.setDisable(false);
        separationBtn.setDisable(false);
        calculator.setNumberSlits(2);
        drawGraphs();
    }
    public void OnSeparationSliderChanged(){//When slider for slit separation changed
        double slideVal = separationSlider.getValue();
        String slideValTxt = Double.toString(slideVal);
        separationTextArea.setText(slideValTxt); //Update text field based on slider, change graphs
        calculator.setDistanceBetweenSlits(slideVal);
        drawGraphs();
    }

    public void OnSeparationButtonClicked(){//When enter button for separation is clicked
        try {//Error checking and update slider based on text field
            double txtVal = Double.parseDouble(separationTextArea.getText());
            if (txtVal > 10) {
                separationSlider.setValue(10);
                separationTextArea.setText("10");
            } else if (txtVal < 0) {
                separationSlider.setValue(0);
                separationTextArea.setText("0");
            } else {
                separationSlider.setValue(txtVal);
            }
        }
        catch (NumberFormatException e) {
            separationSlider.setValue(0);
            separationTextArea.setText("0");
        }
        calculator.setDistanceBetweenSlits(separationSlider.getValue());//Update graphs
        drawGraphs();
    }
    public void OnSlitWidthChanged(){//When slit width slider is dragged
        DecimalFormat df = new DecimalFormat("#.#");//Format to one decimal point (more precise than others)
        Double slideVal = widthSlider.getValue();
        String slideValTxt = df.format(slideVal);
        widthTextArea.setText(slideValTxt);//Update text area based on slider, change graphs
        calculator.setSlitWidth(slideVal);
        drawGraphs();
    }

    public void onWidthButtonClicked(){//When slid with 'Enter' button clicked
        try {//Error checking, update slider based on text area
            double txtVal = Double.parseDouble(widthTextArea.getText());
            if (txtVal > 3) {
                widthSlider.setValue(3);
                widthTextArea.setText("3");
            } else if (txtVal < 0.5) {
                widthSlider.setValue(0.5);
                widthTextArea.setText("0.5");
            } else {
                widthSlider.setValue(txtVal);
            }
        }
        catch (NumberFormatException e) {
            widthSlider.setValue(0.5);
            widthTextArea.setText("0.5");
        }
        calculator.setSlitWidth(widthSlider.getValue());//Update graphs
        drawGraphs();
    }
    public void OnDistanceSliderChanged(){//When Distance to Screen slider dragged
        double slideVal = distanceSlider.getValue();
        String slideValTxt = Double.toString(slideVal);
        distanceTextArea.setText(slideValTxt);//update text area based on slider, update graphs
        calculator.setDistanceFromScreen(slideVal);
        drawGraphs();
    }

    public void onDistanceButtonClicked(){//Enter button for Distance to Screen clicked
        try {//Error checking, update slider based on text area
            double txtVal = Double.parseDouble(distanceTextArea.getText());
            if (txtVal > 1000) {
                distanceSlider.setValue(1000);
                distanceTextArea.setText("1000");
            } else if (txtVal < 500) {
                distanceSlider.setValue(500);
                distanceTextArea.setText("500");
            } else {
                distanceSlider.setValue(txtVal);
            }
        }
        catch (NumberFormatException e) {
            distanceSlider.setValue(500);
            distanceTextArea.setText("500");
        }
        calculator.setDistanceFromScreen(distanceSlider.getValue());//Update graphs
        drawGraphs();
    }

    /**
     * Exports the diffraction variable inputs to a text file
     * at the user chosen location.
     */
    public void OnExportButtonClicked(){//When export button clicked, prompts for file save location, saves as TXT file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("TextFile");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Txt files (*.txt)", "*.text");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(graph.getScene().getWindow());
        if (file != null) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write("Wavelength: " + calculator.getWavelength() + "\n" +
                                "Slit Width: " + calculator.getSlitWidth() + "\n"+
                                "Distance to Screen: " + calculator.getDistanceFromScreen() + "\n"+
                                "Number of Slits: " + calculator.getNumberSlits() + "\n" +
                                "Slit Separation: " + calculator.getDistanceBetweenSlits());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }
}

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ArduinoMLX90614 extends Application implements Initializable/*, SerialPortDataListener */ {
    public ChoiceBox<String> choiceBoxPort;
    public Button buttonStart, buttonExport;
    public ArrayList<Object[]> dataToExport = new ArrayList<>(); // Arreglo necesaria para exportar datos a Excel
    public XYChart.Series<String, Number> series = new XYChart.Series<>();
    public LineChart<String, Number> lineChart;
    public CategoryAxis categoryAxis;
    public NumberAxis numberAxis;

    SerialPort port = null;
    public int time = 1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialize");   updatePorts();
    }

    public void updatePorts(){
        choiceBoxPort.getItems().clear();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            choiceBoxPort.getItems().add(port.getSystemPortName());
        }
        choiceBoxPort.getSelectionModel().selectFirst();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ArduinoMLX90614.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("ArduinoMLX90614");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("Main");
        launch(args);
    }

    public void onActionStart() {
        if(choiceBoxPort.getValue() == null){
            System.out.println("No port found");
            return;
        }

        if(buttonStart.getText().equals("Start")) {
            port = SerialPort.getCommPort(choiceBoxPort.getValue());
            port.setComPortParameters(9600, 8, 1, 0);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            if (port.openPort()) {
                System.out.println("Port opened successfully.");
                // port.addDataListener(this);
            } else {
                System.out.println("Unable to open the port. This may be because another application is using the port." +
                        " Please close the other application and try again.");
                return;
            }

            // https://www.youtube.com/watch?v=Q8beQ6xW0s0&ab_channel=BranislavStanojkovic
            new Thread(()->{
                while (!buttonStart.getText().equals("Start")) {

                    byte[] readBuffer = new byte[port.bytesAvailable()]; // Obtiene el numero de bytes y crea un array de bytes
                    int numRead = port.readBytes(readBuffer, readBuffer.length); // Lee una cantidad de bytes
                    System.out.println("Read " + numRead + " bytes.");
                    String message = new String(readBuffer);
                    if (numRead > 0) {
                        Platform.runLater(() -> {
                            int temp = ThreadLocalRandom.current().nextInt(0, 100);

                            System.out.println("Time: " + time + " Message: " + temp);
                            lineChart.getData().clear();    // Evita el error de series duplicadas
                            series.getData().add(new XYChart.Data<>(String.valueOf(time), temp));
                            lineChart.getData().add(series);
                            dataToExport.add(new Object[]{temp, time});

                            time += 1;
                        });
                    }
                    int sleepTime = 1000;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
            /*LinkedList<XYChart.Series<String, Number>> seriesList = new LinkedList<>(lineChart.getData());
                            seriesList.add(series);
                            for (XYChart.Series<String, Number> serie : seriesList) {
                                serie.getData().forEach(data -> {
                                    System.out.println("xData: " + data.getXValue() + " yData" + data.getYValue());
                                });
                                System.out.println("Series: " + serie.getData());
            }*/

            buttonStart.setText("Stop");    buttonExport.setDisable(true);
        } else {
            buttonStart.setText("Start");   buttonExport.setDisable(false);  port.closePort();
        }
    }

    public void exportToExcel(){
        XSSFWorkbook workbook = new XSSFWorkbook(); // Crea un libro de trabajo de Excel
        Sheet sheet = workbook.createSheet("Hoja1");    // Crea una hoja de trabajo de Excel
        // Lista estatica (no permite agregar datos en tiempo de ejecucion)
        // Object[][] datos = { {"Numero", "Nombre"}, {1, "Juan"}, {2, "Maria"}, {3, "Pedro"} };
        dataToExport.add(0, new Object[]{"Friction Moment (Nxm)", "Time (s)"}); // Agrega el encabezado
        int rowNum = 0;
        for (Object[] row : dataToExport) {
            Row excelRow = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object cellData : row) {
                Cell cell = excelRow.createCell(colNum++);
                if (cellData instanceof String) {
                    cell.setCellValue((String) cellData);
                } else if (cellData instanceof Integer) {
                    cell.setCellValue((Integer) cellData);
                }
            }
        }

        // Escribir los datos en un archivo Excel
        try (FileOutputStream outputStream = new FileOutputStream("datos.xlsx")) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onActionClear() {
        buttonExport.setDisable(true);  lineChart.getData().clear();    series.getData().clear();
        dataToExport.clear();   time = 1;
    }

    public void onActionExport() {
        exportToExcel();
    }

    public void onActionUpdatePorts() {
        updatePorts();
    }
}

package theranos_counter;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import theranos_counter.center_data.CenterData;
import theranos_counter.center_data.State;
import theranos_counter.main.Controller_main;
import theranos_counter.preloader.Preloader;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    public static Main inst;

    public Controller_main controller_main;
    public CenterData centerData;
    public CenterData oldCenterData;

    public Stage primaryStage;

    String fileName = "savedCount";

    @Override
    public void start(Stage primaryStage) throws Exception {
        inst = this;

        shutHtmlUnitUp();

        this.primaryStage = primaryStage;
        primaryStage.close();
        primaryStage.setTitle("Theranos Center Counter");

        final Task<CenterData> task = new Task<CenterData>() {
            @Override
            protected CenterData call() throws Exception {
                System.out.println("Getting site...");
                CenterData data = WebHandler.getCenters();
                return data;
            }
        };

        Preloader.show(() -> {
            try {
                showMain(task.valueProperty());
            } catch (Exception e) {
                System.out.println("Can't get CenterData");
            }
        }, task);

        new Thread(task).start();
    }

    public void showMain(ReadOnlyObjectProperty<CenterData> d) throws Exception
    {
        Preloader.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("main/fx_main.fxml").openStream());
        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(500);
        primaryStage.show();

        centerData = d.get();
        controller_main = fxmlLoader.getController();
        controller_main.refresh(centerData);

        saveCenterCount();
    }

    public CenterData loadCenterCount()
    {
        CenterData data = new CenterData();

        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();

            while (line != null)
            {
                
            }

            return centerData;

        } catch (IOException e) {
            System.out.println("Unable to open count file");
        }

        return null;
    }

    public void saveCenterCount(CenterData data)
    {
        try {
            FileWriter fw = new FileWriter(fileName, false);
            BufferedWriter bw = new BufferedWriter(fw);

            for (State state : centerData.states) {
                System.out.println(centerData.getTotalFrom(state));
                bw.write(String.valueOf(centerData.getTotalFrom(state)));
                bw.newLine();
            }
            bw.close();

        } catch (IOException e)
        {
            System.out.println("Error writing counts to file");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void shutHtmlUnitUp()
    {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.OFF);
    }
}

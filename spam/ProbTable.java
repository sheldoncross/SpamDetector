package spam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ProbTable extends Application {

    private TableView<spam.TestFile> result;

    public static File mainDirectory;

    public static void main(String[] args) { Application.launch(args);}


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Spam Probabilities");

        BorderPane bp = new BorderPane();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        mainDirectory = directoryChooser.showDialog(primaryStage);

        spam.SpamDetector.start(mainDirectory);

        TableColumn<spam.TestFile, String> filename = new TableColumn<>("File Name");
        filename.setPrefWidth(300);
        filename.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<spam.TestFile, String> actualClass = new TableColumn<>("Actual Class");
        actualClass.setPrefWidth(300);
        actualClass.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

        TableColumn<spam.TestFile, Double> spamProb = new TableColumn<>("Spam Probability");
        spamProb.setPrefWidth(280);
        spamProb.setCellValueFactory(new PropertyValueFactory<>("spamProbability"));

        ObservableList<spam.TestFile> items = spam.SpamDetector.getResult();
        this.result = new TableView<>(items);
        this.result.getColumns().setAll(filename, actualClass, spamProb);

        bp.setCenter(result);
        GridPane gp = new GridPane();
        gp.setPrefSize(900, 100);
        Text acc = new Text("Accuracy: " + Float.toString(spam.SpamDetector.getAccuracy()) + "\n");
        Text prec = new Text("Precision: " + Float.toString(spam.SpamDetector.getPrecision()) + "\n");
        Text alerts = new Text("Spammiest words:\n" + spam.SpamDetector.getSpammiest());
        gp.add(acc, 0, 0);
        gp.add(prec, 0, 1);
        gp.add(alerts, 0, 3);
        bp.setBottom(gp);

        result.setRowFactory(view -> {
            TableRow<spam.TestFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && row.isEmpty() == false){
                    spam.TestFile rowData = row.getItem();
                    Desktop desk = Desktop.getDesktop();
                    try {
                        try{
                            primaryStage.close();
                            desk.open(new File(mainDirectory + "/test/" + rowData.getActualClass() + "/" + rowData.getFilename()));
                        }catch(IllegalArgumentException e){
                            primaryStage.close();
                            desk.open(new File(mainDirectory + "/train/" + rowData.getActualClass() + "/" + rowData.getFilename()));
                        }
                    }catch(IOException e){
                        System.err.println(e);
                    }
                }
            });
            return row ;
        });

        primaryStage.setScene(new Scene(bp, 900, 600));

        primaryStage.show();

    }
}

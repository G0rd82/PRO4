package cz.spsmb.ctvrtak.c_spring.o_groovy_test.main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Gui extends Application {
    public static final int N_FREE_TRIES = 3;

    static{
        try {
            MainTest.createGroovyTemplateFile(MainTest.initGroovyCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
    private MainTest test = context.getBean("test", MainTest.class);


    private int freeTries = Gui.N_FREE_TRIES;
    private int[] markLimits={210, 140, 70 };
    private int seconds = 300;

    private Timeline timeline = new Timeline();
    private TextArea taCode = new TextArea(MainTest.initGroovyCode);
    private Label lTimer = new Label(Integer.toString(seconds));
    private Label lMark = new Label("1");
    private Button bSubmit = new Button("submit");
    private Label lFreeTries = new Label();
    private TextFlow tfwEntry = new TextFlow(
            new Text(test.getEntry()),
            new Text("\nin: "), new Text(test.getIn()),
            new Text("\npředp. výstup: "), new Text(test.getOut())
    );
    private Label lOutput = new Label();
    private VBox rightVBox = new VBox(lMark, lTimer, bSubmit, new Label("Zbývá volných "), lFreeTries);
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();
        root.setTop(this.tfwEntry);
        root.setCenter(this.taCode);
        root.setBottom(this.lOutput);
        root.setRight(this.rightVBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        this.init_app();
        this.addHandlers();
        stage.show();
    }
    private void init_app(){
        lMark.setStyle("-fx-font-size:40");
        tfwEntry.setMinSize(500,150);
        taCode.setMinSize(500, 400);
        lOutput.setMinSize(500,150);
        this.refreshFreeTries();

    }
    private void addHandlers(){
        this.bSubmit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Gui.this.lOutput.setText("");
                //Gui.this.timeline.stop();
                if(Gui.this.freeTries-- < 0){
                    Gui.this.seconds-=10;
                }
                Gui.this.refreshFreeTries();
                try {
                    MainTest.createGroovyTemplateFile(taCode.getText());
                    //context = new ClassPathXmlApplicationContext("context.xml");
                    test = context.getBean("test", MainTest.class);
                    Gui.this.lOutput.setText(test.check());
                    if(Gui.this.test.isValid()){
                        Gui.this.stopTest();
                    }
                } catch (Exception e) {
                    Gui.this.lOutput.setText(e.getMessage());
                }
                //Gui.this.timeline.play();

            }
        });
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(Gui.this.seconds-- < 0){
                    Gui.this.lMark.setText("5");
                    Gui.this.stopTest();
                }
                Gui.this.lTimer.setText(Integer.toString(Gui.this.seconds));
                Gui.this.lMark.setText(Integer.toString(Gui.this.getMark()));
            }
        }));
        timeline.setCycleCount(Gui.this.seconds);
        timeline.play();
    }
    private void stopTest() {
        Gui.this.lMark.setStyle("-fx-font-size:120;");
        Gui.this.lMark.setTextFill(Color.RED);
        Gui.this.timeline.stop();
        Gui.this.bSubmit.setOnAction(null);
    }
    private int getMark(){
        for(int i=0; i < this.markLimits.length; i++){
            if(this.markLimits[i] < this.seconds){
                return i+1;
            }
        }
        return this.seconds>0 ? 4 : 5;
    }
    private void refreshFreeTries(){
        this.lFreeTries.setText(String.format("pokusú: %d", freeTries));
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

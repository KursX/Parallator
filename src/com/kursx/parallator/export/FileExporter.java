package com.kursx.parallator.export;

import javafx.stage.Stage;
import com.kursx.parallator.controller.MainController;

import java.io.File;

public interface FileExporter {

    File process(MainController controller, Stage rootStage) throws Exception;

}

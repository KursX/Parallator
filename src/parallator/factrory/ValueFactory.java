package parallator.factrory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.Map;

public class ValueFactory implements Callback<TableColumn.CellDataFeatures<Map<String, String>, String>, ObservableValue<String>> {

    private String key;

    public ValueFactory(String key) {
        this.key = key;
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, String>, String> param) {
        return new SimpleStringProperty(param.getValue().get(key));
    }
}

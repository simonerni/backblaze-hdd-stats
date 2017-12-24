package ch.backblazehdd;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ModelMapper {


    private static Map<String, Model> knownModels = new HashMap<>();

    public ModelMapper() {


    }

    public static void initKnownModels(File file) throws Exception {

        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);
        List<CsvRow> rows = csvReader.read(file, StandardCharsets.UTF_8).getRows();

        for(CsvRow row : rows) {
            knownModels.put(row.getField(0), new Model(row.getField(1), row.getField(2)));
        }

    }

    public static void addKnownModel(String id, Model model) {

        knownModels.put(id, model);

    }

    public static Model getModelAndManufacturerFromModel(String model) {

        return knownModels.getOrDefault(model, new Model(model));

    }

}

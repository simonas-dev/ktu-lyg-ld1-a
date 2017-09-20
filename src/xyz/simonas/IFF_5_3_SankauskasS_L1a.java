package xyz.simonas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class IFF_5_3_SankauskasS_L1a {
    private static final String DATA_FILE_PATH = "./IFF-5-3_SankauskasS_L1a_dat.txt";
    private static final String RESULT_FILE_PATH = "./IFF-5-3_SankauskasS_L1a_rez.txt";

    private class DataModel {
        String vardas;
        int kursas;
        float vidurkis;

        @Override
        public String toString() {
            return vardas + " " + kursas + " " + vidurkis;
        }
    }

    private class WorkModel {
        String threadName;
        int index;
        DataModel dataModel;

        @Override
        public String toString() {
            return threadName + " " + index + " " + dataModel;
        }
    }

    public void execute() throws IOException, InterruptedException {
        Map<String, List<DataModel>> dataMap = readData(Paths.get(DATA_FILE_PATH));
        final CountDownLatch latch = new CountDownLatch(dataMap.size());
        final AtomicInteger lastWorkResultIndex = new AtomicInteger(0);
        final WorkModel[] workResultArray = new WorkModel[999];
        for (String dataKey : dataMap.keySet()) {
            String threadName = "thead-" + dataKey.toLowerCase();
            new Thread(() -> {
                parseData(dataMap.get(dataKey), workResultArray, lastWorkResultIndex);
                latch.countDown();
            }, threadName).start();
        }

        latch.await();

        printData(Paths.get(RESULT_FILE_PATH), workResultArray);
    }

    private void parseData(List<DataModel> data, WorkModel[] workResultArray, AtomicInteger index) {
        for (int i = 0; i < data.size(); i++) {
            WorkModel workModel = new WorkModel();
            workModel.index = i;
            workModel.dataModel = data.get(i);
            workModel.threadName = Thread.currentThread().getName();
            workResultArray[index.getAndAdd(1)] = workModel;
            try {
                Thread.sleep((long)(10 * Math.random())); // simulate work
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printData(Path filePath, WorkModel[] workModels) throws IOException {
        List<String> lines = new ArrayList<>(workModels.length);
        for (WorkModel model : workModels) {
            if (model == null) break;
            System.out.println(model.toString());
            lines.add(model.toString());
        }
        Files.write(filePath, lines, StandardOpenOption.CREATE_NEW);
    }

    private Map<String, List<DataModel>> readData(Path filePath) throws IOException {
        Map<String, List<DataModel>> dataList = new HashMap<>();
        List<String> lineList = Files.readAllLines(filePath);
        int lineIndex = 0;
        while (lineIndex < lineList.size()) {
            String[] headerSplit = lineList.get(lineIndex).split(" ");
            String module = headerSplit[0];
            int studentCount = Integer.valueOf(headerSplit[1]);
            List<DataModel> moduleStudentList = new ArrayList<>(studentCount);

            for (int i = 0; i < studentCount; i++) {
                lineIndex++;
                String[] studentSplit = lineList.get(lineIndex).split(" ");
                DataModel model = new DataModel();
                model.vardas = studentSplit[0];
                model.kursas = Integer.valueOf(studentSplit[1]);
                model.vidurkis = Float.valueOf(studentSplit[2]);
                moduleStudentList.add(model);
            }
            dataList.put(module, moduleStudentList);
            lineIndex++;
        }
        return dataList;
    }

}

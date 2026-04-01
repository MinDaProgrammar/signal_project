package data_management;


import com.data_management.DataReader;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.concurrent.ExecutionException;

class DataStorageTest {

    void testAddAndGetRecords() {
        // TODO Perhaps you can implement a mock data reader to mock the test data?
        //DataReader reader
        DataStorage reader = new DataStorage();
        DataStorage storage = new DataStorage(reader);
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }

    private void assertEquals(int i, int size) {
        if(i!=size){
            throw new AssertionError("The number of records are not matched!");
        }
    }

    private void assertEquals(double i, double size) {
        if(i!=size){
            throw new AssertionError("The value is not equal to the data in the record!");
        }
    }
}

package co.edu.icesi.zoo.controller;

import co.edu.icesi.zoo.constant.AnimalErrorCode;
import co.edu.icesi.zoo.constant.AnimalErrorMsgs;
import co.edu.icesi.zoo.constant.BurmesePython;
import co.edu.icesi.zoo.constant.UtilConstants;
import co.edu.icesi.zoo.dto.AnimalDTO;
import co.edu.icesi.zoo.error.exception.AnimalException;
import co.edu.icesi.zoo.mapper.AnimalMapper;
import co.edu.icesi.zoo.mapper.AnimalMapperImpl;
import co.edu.icesi.zoo.service.AnimalService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AnimalControllerTest {

    private static final String ANIMAL_TEST_UUID = "5b666754-e217-4775-9c95-352ebb0673cb";
    private static final String ANIMAL_TEST_NAME = "Asmodeus";
    private static final String ANIMAL_TEST_ARRIVAL_DATE = "2020-09-24T23:37:42";
    private AnimalDTO dummyAnimal;

    private AnimalController animalController;
    private AnimalMapper animalMapper;
    private AnimalService animalService;

    public void setupScenary1() {
        //A random python with valid average characteristics and no parents
        dummyAnimal = new AnimalDTO(ANIMAL_TEST_UUID, ANIMAL_TEST_NAME, BurmesePython.MALE, BurmesePython.MAX_WEIGHT / 2, BurmesePython.MAX_AGE / 2, BurmesePython.MAX_HEIGHT / 2, ANIMAL_TEST_ARRIVAL_DATE, null, null);
    }


    @BeforeEach
    public void init() {

        animalMapper = new AnimalMapperImpl();
        animalService = mock(AnimalService.class);
        animalController = new AnimalController(animalService, animalMapper);

    }

    @Test
    public void wrongNameLengthsTest() {

        int[] nameLengths = {0, 121};

        for (int nameLength : nameLengths) {

            setupScenary1();

            dummyAnimal.setName(generateRandomOnlyLettersString(nameLength));
            verifyAnimalException(AnimalErrorMsgs.WRONG_NAME_FORMAT_MSG, AnimalErrorCode.CODE_01);

        }

    }

    @Test
    public void invalidNameCharactersTest() {

        //Testing invalid characters
        String[] invalidNames = {"-Burmese", "_Burmese", ".Burmese"};

        for (String invalidName : invalidNames) {

            setupScenary1();

            dummyAnimal.setName(generateRandomOnlyLettersString(20) + invalidName);
            verifyAnimalException(AnimalErrorMsgs.WRONG_NAME_FORMAT_MSG, AnimalErrorCode.CODE_01);

        }

    }

    @Test
    public void futureArrivalDateTest() {

        //No animals can be registered after the current date
        //------------- Second, minute, hour, day, month and year in ms to get the future time
        long[] times = {1000L, 60000L, 3600000L, 86400000L, 2628000000L, 31540000000L};
        long currentTime = System.currentTimeMillis();
        ZoneId zoneId = ZoneId.systemDefault();

        for (long time : times) {

            setupScenary1();
            long futureArrivalTime = currentTime + time;
            LocalDateTime arrivalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(futureArrivalTime), zoneId);

            dummyAnimal.setArrivalDate(arrivalDate.toString());
            verifyAnimalException(AnimalErrorMsgs.IMPOSSIBLE_DATE_MSG, AnimalErrorCode.CODE_02);

        }
    }

    @Test
    public void validatePythonWrongHeightTest(){

        double[] invalidHeights = {-1, -0.1, 0, 8.01, 9};
        validatePythonCharacteristic(invalidHeights, BurmesePython.HEIGHT);

    }

    @Test
    public void validatePythonWrongWeightTest(){

        double[] invalidWeights = {-1, -0.1, 0, 180.01, 182};
        validatePythonCharacteristic(invalidWeights, BurmesePython.WEIGHT);

    }

    @Test
    public void validatePythonWrongAgeTest(){

        double[] invalidAges = {-1, -0.1, 0, 30.01, 31};
        validatePythonCharacteristic(invalidAges, BurmesePython.AGE);

    }

    @Test
    public void validateNotParseableUUIDTest(){

        String[] invalidsUUIDs = {"","1143876087", "a00347293", "5b666754-e217-4775-9c95-352ebb0673cx"};
        for(String invalidUUID : invalidsUUIDs){

            // We only need to check the parents UUIDs as these are the only ones manually set by the user
            setupScenary1();
            dummyAnimal.setMotherId(invalidUUID);
            verifyAnimalException(AnimalErrorMsgs.INVALID_ID, AnimalErrorCode.CODE_06);
            setupScenary1();
            dummyAnimal.setFatherId(invalidUUID);
            verifyAnimalException(AnimalErrorMsgs.INVALID_ID, AnimalErrorCode.CODE_06);
            

        }
    }

    /*
     * Utils
     */
    public void validatePythonCharacteristic(double[] invalidCharacteristics, char characteristic) {

        for (double invalidChacateristic : invalidCharacteristics) {

            setupScenary1();

            if (characteristic == BurmesePython.AGE) dummyAnimal.setAge(invalidChacateristic);
            if (characteristic == BurmesePython.WEIGHT) dummyAnimal.setWeight(invalidChacateristic);
            if (characteristic == BurmesePython.HEIGHT) dummyAnimal.setHeight(invalidChacateristic);


            verifyAnimalException(AnimalErrorMsgs.WRONG_PYTHON_CHARACTERISTICS_MSG, AnimalErrorCode.CODE_03);


        }

    }

    public void verifyAnimalException(String correctMSG, AnimalErrorCode correctCode) {

        // Check if the corresponding exception is thrown when we are trying to create an animal
        // containing an invalid attribute
        try {
            animalController.createAnimal(dummyAnimal);
            fail();
        } catch (AnimalException exception) {

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertNotNull(exception.getError());
            assertEquals(correctMSG, exception.getError().getMessage());
            assertEquals(correctCode, exception.getError().getCode());

        }

    }

    public String generateRandomOnlyLettersString(int length) {

        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);

    }


}

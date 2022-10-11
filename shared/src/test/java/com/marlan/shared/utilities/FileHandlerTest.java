package com.marlan.shared.utilities;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileHandlerTest {

    @BeforeAll
    static void setup() {
        File file = new File("test.txt");
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write("TestContents");
        } catch (IOException ioe) {
            fail("IOException was thrown creating test file.");
        }
    }

    @AfterAll
    static void teardown() {
        String[] testFiles = {"test.txt", "test.json"};
        for (String testFile : testFiles) {
            Path filePath = Path.of(testFile);
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException ioe) {
                fail("IOException was thrown deleting: " + testFile);
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("File should match test contents")
    void fileReadShouldEqualTestContents() {
        String readResult = FileHandler.readFile("", "test.txt");
        assertEquals("TestContents", readResult);
    }

    @Test
    @Order(2)
    @DisplayName("File should be overwritten with new contents")
    void fileOverwriteShouldEqualNewContents() {
        FileHandler.overwriteFile("", "test.txt", "NewContents");
        String readResult = FileHandler.readFile("", "test.txt");
        assertEquals("NewContents", readResult);
    }

    @Test
    @Order(3)
    @DisplayName("File should not exist when deleted")
    void fileDeleteShouldNotExist() {
        FileHandler.deleteFile("", "test.txt");
        Path filePath = Path.of("test.txt");
        assertFalse(Files.exists(filePath));
    }

    @Test
    @DisplayName("Not existent file should return empty string")
    void fileReadShouldReturnEmptyString() {
        String readResult = FileHandler.readFile("", "nonexistent.txt");
        assertEquals("", readResult);
    }

    @Test
    @DisplayName("Deleting non existent file throws no exception")
    void fileDeleteShouldNotDelete() {
        assertDoesNotThrow(() -> FileHandler.deleteFile("", "nonexistent.txt"));
    }

    @Test
    @DisplayName("Writing JSON matches expected output")
    void writeJSONShouldMatchExpectedOutput() {
        String expectedOutput =
                """
                {
                  "test_key": "testValue"
                }""";
        FileHandler.writeJSON("", "test.json", new TestObject());
        String readResult = FileHandler.readFile("", "test.json");
        assertEquals(expectedOutput, readResult);
    }
    private static class TestObject {
        @SuppressWarnings("unused")
        private final String testKey = "testValue";
    }

}
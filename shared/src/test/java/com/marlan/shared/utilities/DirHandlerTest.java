package com.marlan.shared.utilities;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirHandlerTest {

    @Test
    @DisplayName("Empty Args should return user.dir")
    @Order(1)
    void EmptyArgsShouldEqualSharedDir() {
        String[] args = {};
        String workingDir = DirHandler.getWorkingDir(args);
        assertTrue(workingDir.endsWith("shared\\"));
    }

    @Test
    @DisplayName("Null Args should return user.dir")
    @Order(2)
    void NullArgsShouldEqualSharedDir() {
        String workingDir = DirHandler.getWorkingDir(null);
        assertTrue(workingDir.endsWith("shared\\"));
    }

    @Test
    @DisplayName("Nonexistent Args Directory should throw IllegalArgumentException")
    @Order(2)
    void NonexistentArgsDirShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DirHandler.getWorkingDir(new String[]{"nonexistentDirectory"}));
    }

    @Test
    @DisplayName("Valid Args should return args[0]")
    void ValidArgsShouldEqualArgsDir() {
        // Setup
        String testDirName = "testDir";
        Path testDir = Path.of(testDirName);
        try {
            Files.createDirectories(testDir);
        } catch (IOException ioe) {
            fail("Exception thrown: " + ioe.getMessage());
        }

        // Test Block
        String[] args = {testDirName};
        String workingDir = DirHandler.getWorkingDir(args);
        assertTrue(workingDir.endsWith("testDir\\"));

        // Teardown
        try {
            Files.delete(testDir);
        } catch (IOException ioe) {
            fail("Exception thrown: " + ioe.getMessage());
        }
    }

}
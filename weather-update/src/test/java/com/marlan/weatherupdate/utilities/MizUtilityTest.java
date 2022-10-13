package com.marlan.weatherupdate.utilities;

import com.marlan.shared.model.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MizUtilityTest {
    private static final Config config = new Config();

    @Test
    @DisplayName("Empty custom config path should return default 7zip path")
    void emptyCustomConfigPathShouldReturnDefaultSevenzipPath() {
        config.setCustomSevenZipPath("");
        String expectedPath = System.getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
        assertEquals(expectedPath, new MizUtility(config).getSevenZipPath());
    }

    @Test
    @DisplayName("Custom config path should return custom 7zip path")
    void customConfigPathShouldReturnCustomSevenzipPath() {
        String expectedPath = "C:\\Test1\\Test2\\7z.exe";
        config.setCustomSevenZipPath(expectedPath);
        assertEquals(expectedPath, new MizUtility(config).getSevenZipPath());
    }

    @Test
    @DisplayName("Mission file should be extracted from .miz")
    void missionFileShouldBeExtractedFromMiz() {
        fail("Not implemented");
    }

    @Test
    @DisplayName("Mission file should be updated in .miz")
    void missionFileShouldBeUpdatedInMiz() {
        fail("Not implemented");
    }

}
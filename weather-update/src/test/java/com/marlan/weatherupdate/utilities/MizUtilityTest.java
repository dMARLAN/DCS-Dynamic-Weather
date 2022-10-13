package com.marlan.weatherupdate.utilities;

import com.marlan.shared.model.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class MizUtilityTest {
    private static final String sevenZipPathVarName = "sevenZipPath";

    @Test
    @DisplayName("Empty custom config path should return default 7zip path")
    void emptyCustomConfigPathShouldReturnDefaultSevenzipPath() {
        Config config = new Config();
        config.setCustomSevenZipPath("");
        MizUtility mizUtility = new MizUtility(config);
        String expectedPath = System.getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
        String actual = null;

        try {
            Field sevenZipPathField = mizUtility.getClass().getDeclaredField(sevenZipPathVarName);
            sevenZipPathField.setAccessible(true);
            actual = sevenZipPathField.get(mizUtility).toString();
        } catch (NoSuchFieldException nsfe){
            fail(sevenZipPathVarName + " field not found");
        } catch (IllegalAccessException iae){
            fail(sevenZipPathVarName + " field not accessible");
        }

        assertEquals(expectedPath, actual);
    }

    @Test
    @DisplayName("Custom config path should return custom 7zip path")
    void customConfigPathShouldReturnCustomSevenzipPath() {
        Config config = new Config();
        String expectedPath = "C:\\Test1\\Test2\\7z.exe";
        config.setCustomSevenZipPath(expectedPath);
        MizUtility mizUtility = new MizUtility(config);
        String actualPath = null;

        try {
            Field sevenZipPathField = mizUtility.getClass().getDeclaredField(sevenZipPathVarName);
            sevenZipPathField.setAccessible(true);
            actualPath = sevenZipPathField.get(mizUtility).toString();
        } catch (NoSuchFieldException nsfe){
            fail(sevenZipPathVarName + " field not found");
        } catch (IllegalAccessException iae){
            fail(sevenZipPathVarName + " field not accessible");
        }

        assertEquals(expectedPath, actualPath);
    }

}
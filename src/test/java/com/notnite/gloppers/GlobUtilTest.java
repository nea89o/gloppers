package com.notnite.gloppers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobUtilTest {
    @Test
    public void testGlobBeginning() {
        assertTrue(GlobUtil.matchGlob("test_id", "*_id"));
        assertTrue(GlobUtil.matchGlob("test__id", "*_id"));
        assertFalse(GlobUtil.matchGlob("testid", "*_id"));
    }

    @Test
    public void testRepeatedWildcards() {
        assertTrue(GlobUtil.matchGlob("test_id", "*_*"));
        assertTrue(GlobUtil.matchGlob("test_id", "**_*"));
        assertTrue(GlobUtil.matchGlob("test_id", "*?_*"));
        assertFalse(GlobUtil.matchGlob("testid", "*?_*"));
    }

    @Test
    public void testGlobEnd() {
        assertTrue(GlobUtil.matchGlob("test_id", "test_*"));
        assertTrue(GlobUtil.matchGlob("test_id", "test_i?"));
        assertFalse(GlobUtil.matchGlob("test_id", "test_?"));
    }

    @Test
    public void testSinglePlaceholder() {
        assertTrue(GlobUtil.matchGlob("test_id", "tes?_i?"));
    }
}

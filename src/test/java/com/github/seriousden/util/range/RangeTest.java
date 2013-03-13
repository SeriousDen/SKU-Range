package com.github.seriousden.util.range;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Denis.B.Demidov
 */
public class RangeTest {

    Range myRange;

    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {
    }

    @Before
    public void setUp() {
        myRange = new Range();
    }

    @After
    public void tearDown() {
        myRange = null;
    }

    @Test
    public void testContains1() {
        myRange.setRangesString("1; 2-3; 5-5; 2-AF;");
//        myRange.setRangesString("1; 3-2; 5-5; AF-2;"); // The order of bounds is never mind yet
        assertTrue(myRange.contains("1"));
        assertTrue(myRange.contains("2"));
        assertTrue(myRange.contains("3"));
        assertTrue(myRange.contains("5"));
        assertTrue(myRange.contains("A0"));
        assertTrue(myRange.contains("a0"));
        myRange.setCaseSense(true);
        assertFalse(myRange.contains("a0"));
    }

    @Test
    public void testContains2() {
        myRange.setRangesString("  1,    2 : 3,    5 :5,   2: AF  ");
        assertTrue(myRange.contains("1"));
        assertTrue(myRange.contains("2"));
        assertTrue(myRange.contains("3"));
        assertTrue(myRange.contains("5"));
        assertTrue(myRange.contains("A0"));
        assertTrue(myRange.contains("a0"));
        myRange.setCaseSense(true);
        assertFalse(myRange.contains("a0"));
    }

    @Test
    public void testTokenRestriction() {
        myRange.setCaseSense(true);
        myRange.setBoundRegEx("\\w{1,3}");
        try {
            myRange.setRangesString("123A2-C2, 12A-12E"); // 123A2 is wrong
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        }
        myRange.setRangesString("A2-C2, 12A-12E");
        assertTrue(myRange.contains("B2"));
        assertTrue(myRange.contains("12B"));
        assertFalse(myRange.contains("12312B"));
    }
}

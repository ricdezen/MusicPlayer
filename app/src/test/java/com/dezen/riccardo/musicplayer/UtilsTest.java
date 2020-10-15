package com.dezen.riccardo.musicplayer;

import com.dezen.riccardo.musicplayer.utils.Utils;

import junit.framework.TestCase;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void testSimpleOr() {
        TestCase.assertEquals(
                1L | 2L,
                Utils.bitOR(1L, 2L)
        );
    }

    @Test
    public void testSimpleOrArray() {
        long[] params = new long[]{1234, 5678};
        TestCase.assertEquals(
                params[0] | params[1],
                Utils.bitOR(params)
        );
    }

    @Test
    public void testLongerArray() {
        TestCase.assertEquals(
                15,
                Utils.bitOR(
                        1,  //0001
                        1,  //0001
                        4,  //0100
                        4,  //0100
                        8,  //1000
                        8,  //1000
                        10, //1010
                        9,  //1001
                        10, //1010
                        9   //1001
                )
                // Expecting 1111 = 15
        );
    }
}

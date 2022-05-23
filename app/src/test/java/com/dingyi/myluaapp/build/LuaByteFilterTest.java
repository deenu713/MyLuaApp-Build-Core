package com.dingyi.myluaapp.build;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LuaByteFilterTest {


    //read a file to byte array
    public static byte[] readFileToByteArray(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) fileSize];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytes); //no need to close it
        fis.close();
        return bytes;
    }

    //write byte array to file
    public static void writeByteArrayToFile(byte[] bytes, String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream fis = new FileOutputStream(file);
        fis.write(bytes); //no need to close it
        fis.close();
    }

    public static List<Integer[]> matchRuleRange(byte[] data, int[] intRule) {
        var rule = intArray2ByteArray(intRule);
        System.out.println(Arrays.toString(rule));
        //1.first,find all match range
        List<Integer[]> matchRange = new ArrayList<>();
        byte[] sliceArray = new byte[intRule.length];

        for (int index = 0; index < data.length; index++) {
            if (index + intRule.length >= data.length) {
                break;
            }

            System.arraycopy(data, index, sliceArray, 0, intRule.length);
            /* System.out.println(Arrays.toString(sliceArray));*/
            boolean isMatch = true;
            for (int sliceIndex = 0; sliceIndex < sliceArray.length; sliceIndex++) {
                if (sliceArray[sliceIndex] != rule[sliceIndex] && rule[sliceIndex] != -1) {
                    isMatch = false;
                    break;
                }
            }

            if (isMatch) {
                matchRange.add(new Integer[]{index, index + intRule.length});
                index += intRule.length - 1;
            }
        }

        return matchRange;

    }


    public static byte[] filterDataWithRange(byte[] data, List<Integer[]> rangeList, int rangeSize) {
        byte[] result = new byte[data.length - rangeList.size() * rangeSize];

        Integer[] nowRange = rangeList.get(0);
        int popIndex = 0;
        for (int index = 0; index < data.length; index++) {
            if (nowRange == null) {
                result[++popIndex] = data[index];
                continue;
            }
            if (index < nowRange[0]) {
                result[++popIndex] = data[index];
            } else {
                rangeList.remove(0);
                nowRange = null;
                if (rangeList.size() > 0) {
                    nowRange = rangeList.get(0);
                }
                index += rangeSize;
            }
        }
        return result;
    }

    //convent int array to byte array
    public static byte[] intArray2ByteArray(int[] array) {
        var result = new byte[array.length];
        for (int index = 0; index < array.length; index++) {
            result[index] = (byte) array[index];
        }
        return result;
    }

    @Test
    public void main() throws IOException {

        var data = readFileToByteArray("app\\src\\test\\resources\\main.luac");
       /* var data = new byte[]{
                1, 2, 3, 4,
                2, 4, 5, 6,
                3, 4, 5, 6,
                4, 5, 6, 1
        };*/
        var ranges = matchRuleRange(data, new int[]{
                /* 8, 128, 0, 0, 23, 240, 159, 154,
                 165, 180, 91, 64, 8, 128, 0, 0,
                 23, 240, 159, 140, 186, 178, 218, 64*/
                8, 128, 0, 0,
                -1, -1, -1, -1,
                -1, -1, -1, -1,
                -1, -1, -1, -1,
                -1, -1, -1, -1,
                186, 178, 218, 64
        });



       /* var ranges = matchRuleRange(data, new int[]{
                4, -1, 6
        });*/

        var rangeSize = ranges.get(0)[1] - ranges.get(0)[0];
        var filterData = filterDataWithRange(data, ranges, rangeSize);
        System.out.println(filterData.length + " " + data.length);
        writeByteArrayToFile(filterData, "app\\src\\test\\resources\\main_out.luac");
    }
}

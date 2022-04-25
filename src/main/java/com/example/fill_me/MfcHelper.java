package com.example.fill_me;

import android.nfc.FormatException;

public class MfcHelper {
    public static boolean checkIfValid(String hexString){
        short firstValue, firstValueReversed, secondValue;
        firstValue = (short)Integer.parseInt(HexTool.littleEndian2BigEndian(hexString.substring(0,4)), 16);
        firstValueReversed = (short)Integer.parseInt(HexTool.littleEndian2BigEndian(hexString.substring(8, 12)), 16);
        secondValue = (short)Integer.parseInt(HexTool.littleEndian2BigEndian(hexString.substring(16,20)), 16);
        if(firstValue == ~firstValueReversed && ~firstValueReversed == secondValue)
            return hexString.startsWith("04FB04FB", 24);

        return false;
    }

    public static String makeValidHexString(short value){
        return (HexTool.bigEndian2LittleEndian(Integer.toHexString(value))
                + "0100" +
                HexTool.bigEndian2LittleEndian(Integer.toHexString(~value)).substring(0,4)
                + "FEFF" +
                HexTool.bigEndian2LittleEndian(Integer.toHexString(value))
                + "0100" + "04FB04FB").toUpperCase();
    }

    public static short readShortFromHexString(String hexString) throws FormatException {
        if(checkIfValid(hexString)) {
            return (short) Integer.parseInt(HexTool.littleEndian2BigEndian(hexString.substring(0, 4)), 16);
        }else{
            throw new FormatException("Invalid format");
        }
    }
}

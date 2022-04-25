package com.example.fill_me;

public class HexTool {
    static String littleEndian2BigEndian(String littleEndianHexString){
        String temp = littleEndianHexString;
        if(littleEndianHexString.length() % 2 != 0){
            temp += "0";
        }
        return reverseHex(temp);
    }

    static String bigEndian2LittleEndian(String bigEndianHexString){
        String temp = bigEndianHexString;
        if(bigEndianHexString.length() % 2 != 0){
            temp = "0" + bigEndianHexString;
        }

        return reverseHex(temp);
    }

    private static String reverseHex(String hexString) {
        StringBuilder temp = new StringBuilder();
        if(hexString.length() % 2 == 0){
            for(int i = 0; i < hexString.length(); i += 2){
                temp.insert(0, hexString.substring(i, i + 2));
            }
        }
        return temp.toString();
    }
}

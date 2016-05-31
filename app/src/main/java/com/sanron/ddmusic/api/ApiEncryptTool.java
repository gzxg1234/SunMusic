package com.sanron.ddmusic.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class ApiEncryptTool {

    private static final String INPUT = "2012171402992850";
    private static final String IV = "2012061402992850";
    private static final char[] CHARS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

    public static String encrypt(String paramString) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(INPUT.getBytes());

            byte[] stringBytes = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder(stringBytes.length * 2);
            for (int i = 0; i < stringBytes.length; i++) {
                stringBuilder.append(CHARS[((stringBytes[i] & 0xF0) >>> 4)]);
                stringBuilder.append(CHARS[(stringBytes[i] & 0xF)]);
            }
            String str = stringBuilder.toString();
            SecretKeySpec keySpec = new SecretKeySpec(
                    str.substring(str.length() / 2)
                            .getBytes(), "AES");
            Cipher cipher;
            try {
                cipher = Cipher
                        .getInstance("AES/CBC/PKCS5Padding");
                cipher.init(1, keySpec,
                        new IvParameterSpec(IV.getBytes()));
                return URLEncoder.encode(
                        new String(BytesHandler.getChars(cipher
                                .doFinal(paramString.getBytes()))),
                        "utf-8");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class BytesHandler {
        private static char[] chars;
        private static byte[] bytes;

        static {
            chars = new char[64];
            int j = 0;
            for (int i = 65; i <= 90; i++, j++) {
                chars[j] = (char) i;
            }
            for (int i = 97; i <= 122; i++, j++) {
                chars[j] = (char) i;
            }
            for (int i = 48; i <= 57; i++, j++) {
                chars[j] = (char) i;
            }
            chars[j] = 43;
            j++;
            chars[j] = 47;

            bytes = new byte[128];

            for (int i = 0; i < 128; i++) {
                bytes[i] = -1;
            }
            for (int i = 0; i < 64; i++) {
                bytes[chars[i]] = (byte) i;
            }

        }

        public static char[] getChars(byte[] bytes) {
            return getChars(bytes, 0, bytes.length);
        }

        public static char[] getChars(byte[] bytes, int start, int length) {

            int num0 = (length * 4 + 2) / 3;
            final char CHAR = 61;
            char[] result = new char[(length + 2) / 3 * 4];
            int max = start + length;
            int bytesIndex = start;
            int resultIndex = 0;

            for (; bytesIndex < max; ) {

                int n0 = bytes[bytesIndex++] & 0xFF;

                int n1 = 0;
                if (bytesIndex < max) {
                    n1 = bytes[bytesIndex++] & 0xFF;
                }

                int n2 = 0;
                if (bytesIndex < max) {
                    n2 = bytes[bytesIndex++] & 0xFF;
                }

                int i1 = n0 >>> 2;
                int i2 = ((n0 & 0x3) << 4) | (n1 >>> 4);
                int i3 = ((n1 & 0xF) << 2) | (n2 >>> 6);
                int i4 = n2 & 0x3F;

                result[resultIndex++] = chars[i1];
                result[resultIndex++] = chars[i2];

                char c;
                c = resultIndex < num0 ? chars[i3] : CHAR;
                result[resultIndex++] = c;

                c = resultIndex < num0 ? chars[i4] : CHAR;
                result[resultIndex++] = c;
            }
            return result;
        }
    }
}
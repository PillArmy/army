/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.util;

public abstract class BinaryUtils {

    private BinaryUtils() {
    }

    public static int readIntLe(byte[] bytes, int offset) {
        return (bytes[offset++] & 0xFF) |
                ((bytes[offset++] & 0xFF) << 8) |
                ((bytes[offset++] & 0xFF) << 16) |
                ((bytes[offset] & 0xFF) << 24);
    }

    public static float readFloatLe(byte[] bytes, int offset) {
        return Float.intBitsToFloat(readIntLe(bytes, offset));
    }


    public static int writeIntLe(int value, byte[] bytes, int offset) {
        bytes[offset++] = (byte) (value & 0xFF);
        bytes[offset++] = (byte) ((value >> 8) & 0xFF);
        bytes[offset++] = (byte) ((value >> 16) & 0xFF);
        bytes[offset++] = (byte) ((value >> 24) & 0xFF);
        return offset;
    }

    public static int writeFloatLe(float value, byte[] bytes, int offset) {
        return writeIntLe(Float.floatToRawIntBits(value), bytes, offset);
    }


}

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

package io.army.generator.snowflake;


import java.net.*;
import java.util.Enumeration;

public abstract class Workers {

    private Workers() {
        throw new UnsupportedOperationException();
    }

    static {
        updateWorkerId();
    }

    private static volatile int workerId = -1;


    public static int currentWorkerId() {
        return workerId;
    }


    static void updateWorkerId() {
        int address;
        address = localAddress();
        if (address != -1) {
            doUpdateWorkerId(address);
        } else if ((address = localNetMacAddress()) != -1) {
            doUpdateWorkerId(address);
        } else if ((address = localNetAddress()) != -1) {
            doUpdateWorkerId(address);
        }
    }

    private static synchronized void doUpdateWorkerId(int address) {
        Workers.workerId = address & 0xFF;
    }


    private static int localAddress() {
        int suffix = -1;
        try {
            InetAddress address;
            address = InetAddress.getLocalHost();
            if (address instanceof InetAddress && address.isSiteLocalAddress()) {
                suffix = obtainSuffix((Inet4Address) address);
            }
        } catch (UnknownHostException e) {
            suffix = -1;
        }
        return suffix;
    }

    private static int localNetAddress() {
        int suffix = -1;
        try {
            final Enumeration<NetworkInterface> iterator;
            iterator = NetworkInterface.getNetworkInterfaces();
            Enumeration<InetAddress> addressIterator;

            topLoop:
            for (NetworkInterface ni; iterator.hasMoreElements(); ) {
                ni = iterator.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                addressIterator = ni.getInetAddresses();

                for (InetAddress address; addressIterator.hasMoreElements(); ) {
                    address = addressIterator.nextElement();
                    if (address.isSiteLocalAddress() && address instanceof Inet4Address) {
                        suffix = obtainSuffix((Inet4Address) address);
                        break topLoop;
                    }
                } // loop

                break;
            }
        } catch (SocketException e) {
            suffix = -1;
        }
        return suffix;
    }

    private static int localNetMacAddress() {
        int suffix = -1;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface ni; interfaces.hasMoreElements(); ) {
                ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual() || !isLocalNetworkInterface(ni)) {
                    continue;
                }

                final byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0) continue;

                // 将 MAC 地址的 6 个字节合成一个 long 值
                long macValue = 0;
                for (byte b : mac) {
                    macValue = (macValue << 8) | (b & 0xff);
                }
                // 取后 8 位 (0~255)
                suffix = (int) (macValue & 0xFF);
                break;
            }
        } catch (SocketException e) {
            suffix = -1;
        }
        return suffix;
    }


    private static boolean isLocalNetworkInterface(NetworkInterface in) {
        final Enumeration<InetAddress> addressIterator = in.getInetAddresses();
        boolean isLocal = false;
        for (InetAddress address; addressIterator.hasMoreElements(); ) {
            address = addressIterator.nextElement();
            if (address.isSiteLocalAddress()) {
                isLocal = true;
                break;
            }
        } // loop

        return isLocal;
    }


    private static int obtainSuffix(Inet4Address address) {
        final byte[] addressBytes = address.getAddress();
        return addressBytes[addressBytes.length - 1] & 0xFF;
    }


}

// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2020 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---

package org.dogtagpki.systemd;

import java.lang.System;

public class SDNotify {

    private static native int sd_booted();
    private static native int sd_notify(int unset_environment, String state);

    private static boolean booted() {
        return sd_booted() > 0;
    }

    private static boolean has_notify_socket() {
        return System.getenv("NOTIFY_SOCKET") != null;
    }

    public static boolean is_notify_service() {
         return (has_notify_socket() && booted());
    }

    private static boolean notify(String state) {
        return notify(false, state);
    }

    private static boolean notify(boolean unset_env, String state) {
        if (!is_notify_service()) {
            return false;
        }
        return sd_notify(unset_env ? 1 : 0, state) > 0;
    }

    public static boolean notifyStatus(String status) {
        return notify(String.format("STATUS=%s", status));
    }

    public static boolean notifyReady(String status) {
        return notify(String.format("READY=1\nSTATUS=%s", status));
    }

    public static boolean notifyStopping(String status) {
        return notify(String.format("STOPPING=1\nSTATUS=%s", status));
    }
}


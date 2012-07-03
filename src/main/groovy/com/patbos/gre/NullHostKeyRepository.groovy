package com.patbos.gre

import com.jcraft.jsch.HostKeyRepository
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.UserInfo

class NullHostKeyRepository implements HostKeyRepository {

    int check(String host, byte[] key) {
        return 0
    }

    void add(HostKey hostkey, UserInfo ui) {
    }

    void remove(String host, String type) {
    }

    void remove(String host, String type, byte[] key) {
    }

    String getKnownHostsRepositoryID() {
        return null
    }

    HostKey[] getHostKey() {
        return new HostKey[0]
    }

    HostKey[] getHostKey(String host, String type) {
        return new HostKey[0]
    }
}

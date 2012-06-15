package com.patbos.gre

import com.jcraft.jsch.HostKeyRepository
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.UserInfo

class NullHostKeyRepository implements HostKeyRepository {

    int check(String host, byte[] key) {
        return 0  //To change body of implemented methods use File | Settings | File Templates.
    }

    void add(HostKey hostkey, UserInfo ui) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    void remove(String host, String type) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    void remove(String host, String type, byte[] key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    String getKnownHostsRepositoryID() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    HostKey[] getHostKey() {
        return new HostKey[0]  //To change body of implemented methods use File | Settings | File Templates.
    }

    HostKey[] getHostKey(String host, String type) {
        return new HostKey[0]  //To change body of implemented methods use File | Settings | File Templates.
    }
}

package com.clj.blesample.listener;

public interface AdBtnCallback {
    void positive(String hexStr, byte[] broadcastData);

    void negative();
}

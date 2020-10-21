package com.clj.blesample.bean;

public class ReadingBean {
    public static final int STATUS_TIMEOUT = -1;
    public static final int STATUS_STANDBY = 0;
    public static final int STATUS_RECEIVE = 1;
    public static final int STATUS_SUCCESS = 2;

    private String meterAddress;//表编号
    private int status;//状态：-1超时异常；0未下发指令；1下发指令；2成功抄表；
    private int flow;//流量

    public ReadingBean(String meterAddress, int status, int flow) {
        this.meterAddress = meterAddress;
        this.status = status;
        this.flow = flow;
    }

    public String getMeterAddress() {
        return meterAddress;
    }

    public void setMeterAddress(String meterAddress) {
        this.meterAddress = meterAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }
}

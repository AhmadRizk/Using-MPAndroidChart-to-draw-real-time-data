package com.example.models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataModel {

    @SerializedName("RSRP")
    @Expose
    private Integer rSRP;
    @SerializedName("RSRQ")
    @Expose
    private Integer rSRQ;
    @SerializedName("SINR")
    @Expose
    private Integer sINR;

    public Integer getRSRP() {
        return rSRP;
    }

    public void setRSRP(Integer rSRP) {
        this.rSRP = rSRP;
    }

    public Integer getRSRQ() {
        return rSRQ;
    }

    public void setRSRQ(Integer rSRQ) {
        this.rSRQ = rSRQ;
    }

    public Integer getSINR() {
        return sINR;
    }

    public void setSINR(Integer sINR) {
        this.sINR = sINR;
    }

}
public class Mainframe {
    private String timestamp;
    private String identificaoMainframe;
    private Integer usoCpuTotal;
    private Integer usoRamTotal;
    private Integer swapRateMbs;
    private Integer tempoCpuOciosa;
    private Integer cpuIoWait;
    private Integer usoDiscoTotal;
    private Integer discoIopsTotal;
    private Integer discoThroughputMbs;
    private Integer discoReadCount;
    private Integer discoWriteCount;
    private Double discoLatenciaMs;

    @Override
    public String toString() {
        return "Mainframe{" +
                "timestamp='" + timestamp + '\'' +
                ", identificaoMainframe='" + identificaoMainframe + '\'' +
                ", usoCpuTotal=" + usoCpuTotal +
                ", usoRamTotal=" + usoRamTotal +
                ", swapRateMbs=" + swapRateMbs +
                ", tempoCpuOciosa=" + tempoCpuOciosa +
                ", cpuIoWait=" + cpuIoWait +
                ", usoDiscoTotal=" + usoDiscoTotal +
                ", discoIopsTotal=" + discoIopsTotal +
                ", discoThroughputMbs=" + discoThroughputMbs +
                ", discoReadCount=" + discoReadCount +
                ", discoWriteCount=" + discoWriteCount +
                ", discoLatenciaMs=" + discoLatenciaMs +
                '}';
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getDiscoLatenciaMs() {
        return discoLatenciaMs;
    }

    public void setDiscoLatenciaMs(Double discoLatenciaMs) {
        this.discoLatenciaMs = discoLatenciaMs;
    }

    public Integer getDiscoWriteCount() {
        return discoWriteCount;
    }

    public void setDiscoWriteCount(Integer discoWriteCount) {
        this.discoWriteCount = discoWriteCount;
    }

    public Integer getDiscoReadCount() {
        return discoReadCount;
    }

    public void setDiscoReadCount(Integer discoReadCount) {
        this.discoReadCount = discoReadCount;
    }

    public Integer getDiscoThroughputMbs() {
        return discoThroughputMbs;
    }

    public void setDiscoThroughputMbs(Integer discoThroughputMbs) {
        this.discoThroughputMbs = discoThroughputMbs;
    }

    public Integer getDiscoIopsTotal() {
        return discoIopsTotal;
    }

    public void setDiscoIopsTotal(Integer discoIopsTotal) {
        this.discoIopsTotal = discoIopsTotal;
    }

    public Integer getUsoDiscoTotal() {
        return usoDiscoTotal;
    }

    public void setUsoDiscoTotal(Integer usoDiscoTotal) {
        this.usoDiscoTotal = usoDiscoTotal;
    }

    public Integer getCpuIoWait() {
        return cpuIoWait;
    }

    public void setCpuIoWait(Integer cpuIoWait) {
        this.cpuIoWait = cpuIoWait;
    }

    public Integer getTempoCpuOciosa() {
        return tempoCpuOciosa;
    }

    public void setTempoCpuOciosa(Integer tempoCpuOciosa) {
        this.tempoCpuOciosa = tempoCpuOciosa;
    }

    public Integer getSwapRateMbs() {
        return swapRateMbs;
    }

    public void setSwapRateMbs(Integer swapRateMbs) {
        this.swapRateMbs = swapRateMbs;
    }

    public Integer getUsoRamTotal() {
        return usoRamTotal;
    }

    public void setUsoRamTotal(Integer usoRamTotal) {
        this.usoRamTotal = usoRamTotal;
    }

    public Integer getUsoCpuTotal() {
        return usoCpuTotal;
    }

    public void setUsoCpuTotal(Integer usoCpuTotal) {
        this.usoCpuTotal = usoCpuTotal;
    }

    public String getIdentificaoMainframe() {
        return identificaoMainframe;
    }

    public void setIdentificaoMainframe(String identificaoMainframe) {
        this.identificaoMainframe = identificaoMainframe;
    }
}

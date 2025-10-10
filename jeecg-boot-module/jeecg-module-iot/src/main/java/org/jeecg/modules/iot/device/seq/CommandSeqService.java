package org.jeecg.modules.iot.device.seq;

public interface CommandSeqService {
    /**
     * Get the next incremental command id for a device.
     * Must be monotonic per device, resilient to Redis flush.
     */
    long nextSeq(String sn);

    /**
     * Force rebuilding the Redis counter from DB max(command_code) for this device.
     */
    void rebuildFromDb(String sn);
}

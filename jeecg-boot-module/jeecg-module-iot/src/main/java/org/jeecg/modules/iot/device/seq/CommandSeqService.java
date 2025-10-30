package org.jeecg.modules.iot.device.seq;

public interface CommandSeqService {
    /**
     * Get the next incremental command id for a device.
     * Must be monotonic per device, resilient to Redis flush.
     */
    long nextSeq(String sn);

    /**
     * Atomically reserve a contiguous range of command ids for a device.
     * Returns the starting command id of the reserved range.
     * The reserved ids are: [start, start + count - 1].
     */
    long nextSeqRange(String sn, int count);

    /**
     * Force rebuilding the Redis counter from DB max(command_code) for this device.
     */
    void rebuildFromDb(String sn);
}

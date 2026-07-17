package com.smd.gctcore.common.integration.mekanism;

/**
 * Runtime access to Digital Miner harvest-level data injected by mixin.
 * Must NOT live in a mixin package — Mixin forbids non-mixin classes there.
 */
public interface DigitalMinerHarvestAccess {

    int gct$getMiningLevel();

    void gct$setMiningLevel(int level);
}

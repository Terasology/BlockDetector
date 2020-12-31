/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.blockdetector.utilities;

import org.terasology.math.Region3i;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;

import java.util.Set;

/**
 * A class containing the information about a specific detector-detectable binding.
 * <p>
 * Binds a single <b>detector</b> item Uri to multiple <b>detectable</b> block Uris.
 */
public abstract class DetectorData {
    /**
     * The Uri of the detector item.
     */
    private String detectorUri;

    /**
     * A set of detectable item Uris represented as strings.
     */
    private Set<String> detectableUris;

    /**
     * The range of the detector.
     */
    private BlockRegion range;

    /**
     * If this variable is not null, all blocks within the specified range
     * must not be AIR or UNLOADED.
     */
    private BlockRegion nonAerialRange;

    protected DetectorData(String detectorUri, Set<String> detectableUris, BlockRegion range) {
        this.detectorUri = detectorUri;
        this.detectableUris = detectableUris;
        this.range = range;
    }

    public String getDetectorUri() {
        return detectorUri;
    }

    public Set<String> getDetectableUris() {
        return detectableUris;
    }

    public BlockRegionc getRange() {
        return range;
    }

    public BlockRegionc getNonAerialRange() {
        return nonAerialRange;
    }

    public void setNonAerialRange(BlockRegionc nonAerialRange) {
        this.nonAerialRange.set(nonAerialRange);
    }

    /**
     * Get the period of the run() function being called.
     *
     * @param minDistance the closest distance to a detectable block within the range of the detector
     * @return the period of the signal
     */
    public abstract int getPeriod(int minDistance);


    /**
     * The function that will be called with a period of getPeriod().
     * <p>
     * Should produce a signal to the user.
     */
    public abstract void run();
}

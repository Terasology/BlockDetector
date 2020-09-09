// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.blockdetector.utilities;

import org.terasology.engine.math.Region3i;

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
    private final String detectorUri;

    /**
     * A set of detectable item Uris represented as strings.
     */
    private final Set<String> detectableUris;

    /**
     * The range of the detector.
     */
    private final Region3i range;

    /**
     * If this variable is not null, all blocks within the specified range must not be AIR or UNLOADED.
     */
    private Region3i nonAerialRange;

    protected DetectorData(String detectorUri, Set<String> detectableUris, Region3i range) {
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

    public Region3i getRange() {
        return range;
    }

    public Region3i getNonAerialRange() {
        return nonAerialRange;
    }

    public void setNonAerialRange(Region3i nonAerialRange) {
        this.nonAerialRange = nonAerialRange;
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

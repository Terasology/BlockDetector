// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.blockdetector.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.utilities.Assets;

import java.util.Set;

/**
 * A DetectorData implementation containing information about an audio asset.
 * <p>
 * Plays the asset with a frequency scaling depending on the distance to the closest block, ranging from frequencyLow to
 * frequencyHigh with four possible values.
 */
public class LinearAudioDetectorImpl extends DetectorData {
    private static final Logger logger = LoggerFactory.getLogger(LinearAudioDetectorImpl.class);

    /**
     * The audio manager. Should be injected in the custom system implementation.
     */
    private final AudioManager audioManager;

    /**
     * The audio asset Uri, represented as a string.
     */
    private final String audioUri;

    private final int frequencyLow;
    private final int frequencyHigh;

    private final int scaleCount = 4;

    public LinearAudioDetectorImpl(String detectorUri, Set<String> detectableUris, Region3i range,
                                   AudioManager audioManager, String audioUri, int frequencyLow, int frequencyHigh) {
        super(detectorUri, detectableUris, range);
        this.audioManager = audioManager;
        this.audioUri = audioUri;
        this.frequencyLow = frequencyLow;
        this.frequencyHigh = frequencyHigh;
    }

    @Override
    public int getPeriod(int minDistance) {
        int scale = (int) Math.floor(scaleCount * minDistance / getRange().size().length());
        return frequencyLow + (frequencyHigh - frequencyLow) * scale / scaleCount;
    }

    @Override
    public void run() {
        audioManager.playSound(Assets.getSound(audioUri).get());
    }
}

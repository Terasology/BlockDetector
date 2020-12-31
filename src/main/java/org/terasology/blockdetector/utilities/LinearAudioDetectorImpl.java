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

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.AudioManager;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockRegion;

import java.util.Set;

/**
 * A DetectorData implementation containing information about an audio asset.
 * <p>
 * Plays the asset with a frequency scaling depending on the distance to the closest block,
 * ranging from frequencyLow to frequencyHigh with four possible values.
 */
public class LinearAudioDetectorImpl extends DetectorData {
    private static final Logger logger = LoggerFactory.getLogger(LinearAudioDetectorImpl.class);

    /**
     * The audio manager. Should be injected in the custom system implementation.
     */
    private AudioManager audioManager;

    /**
     * The audio asset Uri, represented as a string.
     */
    private String audioUri;

    private int frequencyLow;
    private int frequencyHigh;

    private int scaleCount = 4;

    public LinearAudioDetectorImpl(String detectorUri, Set<String> detectableUris, BlockRegion range, AudioManager audioManager, String audioUri, int frequencyLow, int frequencyHigh) {
        super(detectorUri, detectableUris, range);
        this.audioManager = audioManager;
        this.audioUri = audioUri;
        this.frequencyLow = frequencyLow;
        this.frequencyHigh = frequencyHigh;
    }

    @Override
    public int getPeriod(int minDistance) {
        int scale = (int) Math.floor(scaleCount * minDistance / getRange().getSize(new Vector3i()).length());
        return frequencyLow + (frequencyHigh - frequencyLow) * scale / scaleCount;
    }

    @Override
    public void run() {
        audioManager.playSound(Assets.getSound(audioUri).get());
    }
}

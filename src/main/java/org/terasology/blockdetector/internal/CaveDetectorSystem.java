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
package org.terasology.blockdetector.internal;

import com.google.common.collect.Sets;
import org.terasology.audio.AudioManager;
import org.terasology.blockdetector.systems.BlockDetectorSystem;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.world.block.BlockRegion;

/**
 * A detector for caves, i.e. air located below the player.
 */
@RegisterSystem
public class CaveDetectorSystem extends BaseComponentSystem {
    @In
    private AudioManager audioManager;

    @In
    private BlockDetectorSystem blockDetectorSystem;

    private DetectorData data;

    @Override
    public void initialise() {
        BlockRegion range = new BlockRegion(-1, -55, -1, 1, -5, 1);
        data = new LinearAudioDetectorImpl("BlockDetector:caveDetector", Sets.newHashSet("engine:air"), range, audioManager, "BlockDetector:ScannerBeep", 250, 1000);

        BlockRegion nonAerialRange = new BlockRegion(-3, -3, -3, 3, 3, 3);
        data.setNonAerialRange(nonAerialRange);

        blockDetectorSystem.addDetector(data);
    }

    // Step 4: Gracefully clean up the bindings on shutdown.
    @Override
    public void shutdown() {
        blockDetectorSystem.removeDetector(data.getDetectorUri());
    }
}

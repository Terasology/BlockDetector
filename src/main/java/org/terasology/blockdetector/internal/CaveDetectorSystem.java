// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.blockdetector.internal;

import com.google.common.collect.Sets;
import org.terasology.blockdetector.systems.BlockDetectorSystem;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.In;
import org.terasology.math.geom.Vector3i;

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
        Region3i range = Region3i.createFromMinMax(new Vector3i(-1, -55, -1), new Vector3i(1, -5, 1));
        data = new LinearAudioDetectorImpl("BlockDetector:caveDetector", Sets.newHashSet("engine:air"), range,
                audioManager, "BlockDetector:ScannerBeep", 250, 1000);

        Region3i nonAerialRange = Region3i.createFromMinMax(new Vector3i(-3, -3, -3), new Vector3i(3, 3, 3));
        data.setNonAerialRange(nonAerialRange);

        blockDetectorSystem.addDetector(data);
    }

    // Step 4: Gracefully clean up the bindings on shutdown.
    @Override
    public void shutdown() {
        blockDetectorSystem.removeDetector(data.getDetectorUri());
    }
}

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
package org.terasology.blockdetector.systems;

import com.google.common.collect.Sets;
import org.terasology.audio.AudioManager;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;

@RegisterSystem
public class ExampleDetectorSystem extends BaseComponentSystem {
    @In
    private AudioManager audioManager;

    @In
    private BlockDetectorSystem blockDetectorSystem;

    private DetectorData data;

    @Override
    public void initialise() {
        data = new LinearAudioDetectorImpl("Core:shovel", Sets.newHashSet("core:Iris"), 32, audioManager, "BlockDetector:ScannerBeep", 200, 2000);
        blockDetectorSystem.addDetector(data);
    }

    @Override
    public void shutdown() {
        blockDetectorSystem.removeDetector(data.getDetectorUri());
    }
}

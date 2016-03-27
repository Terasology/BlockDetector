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

import org.terasology.blockdetector.utilities.DetectorData;

/**
 * The block detector system interface.
 * <p>
 * Should be injected via @In in custom detector implementations.
 */
public interface BlockDetectorSystem {
    void addDetector(DetectorData data);

    void removeDetector(String detectorUri);
}

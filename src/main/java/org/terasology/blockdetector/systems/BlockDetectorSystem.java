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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.AudioManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The main system containing all block detector logic.
 */
@RegisterSystem
public class BlockDetectorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static Logger logger = LoggerFactory.getLogger(BlockDetectorSystem.class);

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @In
    private AudioManager audioManager;

    /**
     * The detector's range.
     * <p>
     * Blocks will be detected in a cube centered at the player with an edge length of 2*detectorRange + 1.
     */
    private int detectorRange;

    private float timeSinceLastUpdate;
    private float updateFrequency;

    /**
     * The block types that will be detected.
     */
    private Set<String> detectedUriTypes;

    /**
     * The items that act as detectors when held.
     */
    private Set<String> detectorItemNames;

    @Override
    public void initialise() {
        super.initialise();

        detectorRange = 32;
        updateFrequency = 1.0f;

        // Temporarily populated with easy-to-test items
        detectedUriTypes = Sets.newHashSet("core:Iris");
        detectorItemNames = Sets.newHashSet("Core:shovel");
    }

    @Override
    public void update(float delta) {
        timeSinceLastUpdate += delta;
        if (timeSinceLastUpdate >= updateFrequency) {
            detectBlocks();
            timeSinceLastUpdate -= updateFrequency;
        }
    }

    /**
     * Main block detection method.
     */
    private void detectBlocks() {
        // Get the name of the item held by the player
        String name = null;

        EntityRef player = localPlayer.getCharacterEntity();
        EntityRef item = inventoryManager.getItemInSlot(player, player.getComponent(SelectedInventorySlotComponent.class).slot);

        if (item != EntityRef.NULL) {
            name = item.getParentPrefab().getName();
        }

        // Check if it is a detector
        if (name == null || !detectorItemNames.contains(name)) {
            return;
        }

        // Get the current block position, rounded down
        Vector3i blockPosition = new Vector3i(localPlayer.getPosition(), RoundingMode.FLOOR);

        Map<BlockUri, Integer> detectedBlocks = new HashMap<>();

        // Iterate through all the blocks within the detector's range
        for (int x = blockPosition.x - detectorRange; x <= blockPosition.x + detectorRange; x++) {
            for (int y = blockPosition.y - detectorRange; y <= blockPosition.y + detectorRange; y++) {
                for (int z = blockPosition.z - detectorRange; z <= blockPosition.z + detectorRange; z++) {
                    // Get the current block
                    Block block = worldProvider.getBlock(new Vector3i(x, y, z));

                    // Get the block's type via URI
                    BlockUri uri = block.getURI();

                    if (detectedUriTypes.contains(uri.toString())) {
                        // If there are no blocks of the given type, add it to the map
                        // Otherwise increment the amount of blocks of the given type
                        detectedBlocks.put(uri, detectedBlocks.containsKey(uri) ? detectedBlocks.get(uri) + 1 : 1);
                    }
                }
            }
        }

        // Get the total amount of detected blocks
        int detectedCount = 0;
        for (BlockUri uri : detectedBlocks.keySet()) {
            detectedCount += detectedBlocks.get(uri);
        }

        if (detectedCount > 0) {
            logger.info("Found {} block(s): {}", detectedCount, String.valueOf(detectedBlocks));

            // Play sound to indicate block in range
            // TODO: loop this; frequency should change depending on block range
            audioManager.playSound(Assets.getSound("BlockDetector:ScannerBeep").get());
        } else {
            logger.info("No blocks found :(");
        }
    }
}

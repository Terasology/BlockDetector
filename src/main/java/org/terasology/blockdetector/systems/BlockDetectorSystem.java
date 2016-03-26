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
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The main system containing all block detector logic
 */
@RegisterSystem
public class BlockDetectorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @In
    private AudioManager audioManager;

    /**
     * The detector's range
     * <p>
     * Blocks will be detected in a cube centered at the player with an edge length of 2*detectorRange + 1
     */
    private int detectorRange;

    private float timeSinceLastUpdate;
    private float updateInterval;

    /**
     * The block types that will be detected
     */
    private Set<String> detectedUriTypes;

    /**
     * The items that act as detectors when held
     */
    private Set<String> detectorItemNames;

    private TimerTask playSound;
    private Timer timer;

    @Override
    public void initialise() {
        super.initialise();

        detectorRange = 16;
        updateInterval = 1.0f;

        // Temporarily populated with easy-to-test items
        detectedUriTypes = Sets.newHashSet("core:Iris");
        detectorItemNames = Sets.newHashSet("Core:shovel");

        initTimer();
    }

    /**
     * Runs the main detectBlocks() function at an interval roughly equal to updateInterval.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        timeSinceLastUpdate += delta;
        if (timeSinceLastUpdate >= updateInterval) {
            detectBlocks();
            timeSinceLastUpdate -= updateInterval;
        }
    }

    @Override
    public void shutdown() {
        shutdownTimer();
    }

    /**
     * Initialises the TimerTask and Timer
     */
    private void initTimer() {
        playSound = new TimerTask() {
            @Override
            public void run() {
                audioManager.playSound(Assets.getSound("BlockDetector:ScannerBeep").get());
            }
        };
        timer = new Timer();
    }

    /**
     * Gracefully shuts down the TimerTask and Timer
     */
    private void shutdownTimer() {
        if (playSound != null) {
            playSound.cancel();
            playSound = null;
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * Main block detection method
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
            shutdownTimer();
            return;
        }

        // Get the current block position, rounded down
        Vector3i playerPosition = new Vector3i(localPlayer.getPosition(), RoundingMode.FLOOR);

        Set<Vector3i> detectedBlocks = new HashSet<>();

        // Iterate through all the blocks within the detector's range
        for (int x = playerPosition.x - detectorRange; x <= playerPosition.x + detectorRange; x++) {
            for (int y = playerPosition.y - detectorRange; y <= playerPosition.y + detectorRange; y++) {
                for (int z = playerPosition.z - detectorRange; z <= playerPosition.z + detectorRange; z++) {
                    // Get the current block
                    Vector3i blockPosition = new Vector3i(x, y, z);
                    Block block = worldProvider.getBlock(blockPosition);

                    // Get the block's type via URI
                    BlockUri uri = block.getURI();

                    if (detectedUriTypes.contains(uri.toString())) {
                        // If there are no blocks of the given type, add it to the map
                        // Otherwise increment the amount of blocks of the given type
                        detectedBlocks.add(blockPosition);
                    }
                }
            }
        }

        if (detectedBlocks.size() > 0) {
            // Get the minimum distance to a block

            int minDistance = Integer.MAX_VALUE;
            for (Vector3i block : detectedBlocks) {
                int distance = (int) Math.sqrt(
                        Math.pow(block.getX() - playerPosition.getX(), 2)
                                + Math.pow(block.getY() - playerPosition.getY(), 2)
                                + Math.pow(block.getZ() - playerPosition.getZ(), 2));
                minDistance = Math.min(minDistance, distance);
            }

            // Get the signal frequency depending on the distance
            int frequencyLow = 100;
            int frequencyHigh = 2000;

            int frequency = frequencyLow + (frequencyHigh - frequencyLow) * minDistance / detectorRange;

            // Reset and recreate the sound timer with the calculated frequency
            shutdownTimer();
            initTimer();
            timer.scheduleAtFixedRate(playSound, 0, frequency);
        } else {
            shutdownTimer();
        }
    }
}

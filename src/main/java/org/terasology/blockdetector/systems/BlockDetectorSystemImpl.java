// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.blockdetector.systems;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.module.inventory.components.SelectedInventorySlotComponent;
import org.terasology.module.inventory.systems.InventoryManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The main system containing all block detector logic.
 */
@RegisterSystem
@Share(BlockDetectorSystem.class)
public class BlockDetectorSystemImpl extends BaseComponentSystem implements UpdateSubscriberSystem, BlockDetectorSystem {
    private static final Logger logger = LoggerFactory.getLogger(BlockDetectorSystemImpl.class);

    /**
     * Used to get the block located at the specified position.
     */
    @In
    private WorldProvider worldProvider;

    /**
     * Used to get the player's current position.
     */
    @In
    private LocalPlayer localPlayer;

    /**
     * Used to get the player's current selected item.
     */
    @In
    private InventoryManager inventoryManager;

    /**
     * The map of detector-detectable bindings.
     */
    private final Map<String, DetectorData> detectors = new HashMap<>();

    private float timeSinceLastUpdate;

    /**
     * The period at which the detectBlocks() function should be called.
     */
    float updatePeriod = 1.0f;

    /**
     * The current timer task associated with the timer.
     */
    private TimerTask timerTask;

    /**
     * The timer object calling timerTask periodically.
     */
    private Timer timer;

    /**
     * The current task period, in ms.
     */
    private Integer taskPeriod=null;

    private final Set<Vector3i> detectedBlocks = new HashSet<>();

    /**
    * Sets the local player for the test
    * The dummy local player will make the system work and does not throw a null exception
    * @param set A local player whose location component is used
    */
    public void setLocalPlayer(LocalPlayer set) {
        this.localPlayer = set;
    }

    public float getTimeSinceLastUpdate() {
        return this.timeSinceLastUpdate;
    }

    /**
    * This sets the timeSinceLastUpdate value for the tests
    */
    public void setTimeSinceLastUpdate(float value) {
        this.timeSinceLastUpdate = value;
    }

    /**
    * This provides the DetectorData for the tests
    * @return a Map of DetectorData
    */
    public Map<String, DetectorData> getDetectors() {
        return this.detectors;
    }

    /**
     * Runs the main detectBlocks() function with a period roughly equal to updatePeriod.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        timeSinceLastUpdate += delta;
        if (timeSinceLastUpdate >= updatePeriod) {
            detectBlocks();
            timeSinceLastUpdate -= updatePeriod;
        }
    }

    @Override
    public void shutdown() {
        shutdownTimer();
    }

    /**
     * Initialises the current task and timer.
     *
     * @param data a specified DetectorData binding.
     */
    private void initTimer(DetectorData data) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                data.run();
            }
        };
        timer = new Timer();
    }

    /**
     * Gracefully shuts down the TimerTask and Timer.
     * <p>
     * Does nothing if the task or timer are already shut down.
     */
    private void shutdownTimer() {
        taskPeriod = null;

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * Adds a detector data object to the map.
     *
     * @param data the DetectorData object.
     */
    @Override
    public void addDetector(DetectorData data) {
        logger.info("Adding detector with item Uri {}", data.getDetectorUri());
        detectors.put(data.getDetectorUri(), data);
    }

    /**
     * Removes a detector with a specified Uri from the map.
     *
     * @param detectorUri the Uri of the detector to be removed.
     */
    @Override
    public void removeDetector(String detectorUri) {
        logger.info("Removing detector with item Uri {}", detectorUri);
        detectors.remove(detectorUri);
    }

    /**
     * Gets a detector with a specified Uri from the map, or null if it doesn't exist.
     *
     * @param detectorUri the detector's Uri.
     * @return the detector, or null if it doesn't exist.
     */
    private DetectorData getDetectorData(String detectorUri) {
        return detectors.getOrDefault(detectorUri, null);
    }

    public Set<Vector3i> getDetectedBlocks() {
        return detectedBlocks;
    }

    /**
     * The main block detection method.
     */
    public void detectBlocks() {
        String itemUri = null;

        // Get the current player's selected inventory item.
        EntityRef player = localPlayer.getCharacterEntity();

        if (player == null) {
            shutdownTimer();
            return;
        }

        SelectedInventorySlotComponent selectedSlot = player.getComponent(SelectedInventorySlotComponent.class);

        if (selectedSlot == null) {
            shutdownTimer();
            return;
        }

        EntityRef item = inventoryManager.getItemInSlot(player, selectedSlot.slot);

        if (item != EntityRef.NULL) {
            itemUri = item.getParentPrefab().getName();
        }

        // Get the item's associated DetectorData, if it exists.
        DetectorData data = getDetectorData(itemUri);

        if (data == null) {
            shutdownTimer();
            return;
        }

        // Get the current block position rounded down.
        Vector3i playerPosition = new Vector3i(localPlayer.getPosition(new Vector3f()), RoundingMode.FLOOR);

        if (data.getNonAerialRange() != null) {
            boolean nonAerialBlocksPresent = false;

            for (int x = playerPosition.x + data.getNonAerialRange().minX(); x <= playerPosition.x + data.getNonAerialRange().maxX(); x++) {
                for (int y = playerPosition.y + data.getNonAerialRange().minY(); y <= playerPosition.y + data.getNonAerialRange().maxY(); y++) {
                    for (int z = playerPosition.z + data.getNonAerialRange().minZ(); z <= playerPosition.z + data.getNonAerialRange().maxZ(); z++) {
                        // Get the current block.
                        Vector3i blockPosition = new Vector3i(x, y, z);
                        Block block = worldProvider.getBlock(blockPosition);

                        // Get the block's Uri.
                        BlockUri uri = block.getURI();

                        if (!uri.equals(BlockManager.AIR_ID) && !uri.equals(BlockManager.UNLOADED_ID)) {
                            nonAerialBlocksPresent = true;
                        }
                    }
                }
            }

            if (!nonAerialBlocksPresent) {
                shutdownTimer();
                return;
            }
        }


        // Iterate through all the blocks within the detector's range.
        for (int x = playerPosition.x + data.getRange().minX(); x <= playerPosition.x + data.getRange().maxX(); x++) {
            for (int y = playerPosition.y + data.getRange().minY(); y <= playerPosition.y + data.getRange().maxY(); y++) {
                for (int z = playerPosition.z + data.getRange().minZ(); z <= playerPosition.z + data.getRange().maxZ(); z++) {
                    // Get the current block.
                    Vector3i blockPosition = new Vector3i(x, y, z);
                    Block block = worldProvider.getBlock(blockPosition);

                    // Get the block's Uri.
                    BlockUri uri = block.getURI();

                    // If the current detector detects this block...
                    if (data.getDetectableUris().contains(uri.toString())) {
                        // ...add it to the set.
                        detectedBlocks.add(blockPosition);
                    }
                }
            }
        }

        if (detectedBlocks.size() > 0) {
            // Get the distance to the closest detectable block, MAX_VALUE if detectedBlocks is empty
            int minDistance = detectedBlocks.stream()
                    .map(block -> block.distanceSquared(playerPosition))
                    .min(Comparator.comparing(Long::valueOf))
                    .map(min -> (int) Math.sqrt(min))
                    .orElse(Integer.MAX_VALUE);

            // If taskPeriod is changed, reschedule the timer.
            int newPeriod = data.getPeriod(minDistance);

            if (taskPeriod == null || taskPeriod != newPeriod) {
                logger.info("Detector {} rescheduling task at taskPeriod {} (minimal block distance: {})", data.getDetectorUri(), newPeriod, minDistance);

                // Reset the timer.
                shutdownTimer();

                // Recreate the timer with the current detector's function.
                initTimer(data);

                taskPeriod = newPeriod;

                timer.scheduleAtFixedRate(timerTask, 0, newPeriod);
            }
        } else {
            shutdownTimer();
        }
    }
}

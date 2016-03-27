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

import com.google.api.client.util.Maps;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The main system containing all block detector logic.
 */
@RegisterSystem
public class BlockDetectorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
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
    private Map<String, DetectorData> detectors;

    /**
     * The time since the last update call.
     */
    private float timeSinceLastUpdate;

    /**
     * The period at which the detectBlocks() function should be called.
     */
    private float updatePeriod;

    /**
     * The current timer task associated with the timer.
     */
    private TimerTask timerTask;

    /**
     * The timer object calling timerTask periodically.
     */
    private Timer timer;

    @Override
    public void initialise() {
        super.initialise();

        updatePeriod = 1.0f;
        detectors = Maps.newHashMap();
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
    public void addDetector(DetectorData data) {
        detectors.put(data.getDetectorUri(), data);
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

    /**
     * Removes a detector with a specified Uri from the map.
     *
     * @param detectorUri the Uri of the detector to be removed.
     */
    public void removeDetector(String detectorUri) {
        detectors.remove(detectorUri);
    }

    /**
     * The main block detection method.
     */
    private void detectBlocks() {
        String itemUri = null;

        // Get the current player's selected inventory item.
        EntityRef player = localPlayer.getCharacterEntity();
        EntityRef item = inventoryManager.getItemInSlot(player, player.getComponent(SelectedInventorySlotComponent.class).slot);

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
        Vector3i playerPosition = new Vector3i(localPlayer.getPosition(), RoundingMode.FLOOR);

        Set<Vector3i> detectedBlocks = new HashSet<>();

        // Iterate through all the blocks within the detector's range.
        for (int x = playerPosition.x - data.getRange(); x <= playerPosition.x + data.getRange(); x++) {
            for (int y = playerPosition.y - data.getRange(); y <= playerPosition.y + data.getRange(); y++) {
                for (int z = playerPosition.z - data.getRange(); z <= playerPosition.z + data.getRange(); z++) {
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
            // Get the distance to the closest detectable block.

            int minDistance = Integer.MAX_VALUE;
            for (Vector3i block : detectedBlocks) {
                int distance = (int) Math.sqrt(
                        Math.pow(block.getX() - playerPosition.getX(), 2)
                                + Math.pow(block.getY() - playerPosition.getY(), 2)
                                + Math.pow(block.getZ() - playerPosition.getZ(), 2));
                minDistance = Math.min(minDistance, distance);
            }

            // Reset the timer.
            shutdownTimer();

            // Recreate the timer with the current detector's function.
            initTimer(data);

            // Reschedule the timer.
            timer.scheduleAtFixedRate(timerTask, 0, data.getPeriod(minDistance));
        } else {
            shutdownTimer();
        }
    }
}

// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.blockdetector.systems;

import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.integrationenvironment.MainLoop;
import org.terasology.engine.integrationenvironment.jupiter.Dependencies;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.module.inventory.components.InventoryItem;
import org.terasology.module.inventory.components.SelectedInventorySlotComponent;
import org.terasology.module.inventory.events.RequestInventoryEvent;
import org.terasology.module.inventory.systems.InventoryManager;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MTEExtension.class)
@Dependencies("BlockDetector")
@Tag("MteTest")
@IntegrationEnvironment(networkMode = NetworkMode.NONE)
public class BlockDetectorSystemTest {

    BlockDetectorSystemImpl blockDetectorSystem;

    @In
    private AudioManager audioManager;

    @BeforeEach
    public void getSystem(BlockDetectorSystem bdsInterface) {
        blockDetectorSystem = (BlockDetectorSystemImpl) bdsInterface;
    }

    /**
     * This updateTest is created to test if the timeSinceLastUpdate is increased by delta.
     */
    @Test
    public void updateTest() {
        blockDetectorSystem.updatePeriod = 9999;  // big enough to not run in to during the test
        blockDetectorSystem.setTimeSinceLastUpdate(5);
        blockDetectorSystem.update(3);
        assertThat(blockDetectorSystem.getTimeSinceLastUpdate()).isEqualTo(5 + 3);
    }

    @Test
    public void detectorTest() {
        BlockRegion range = new BlockRegion(-1, -55, -1, 1, -5, 1);
        DetectorData data = new LinearAudioDetectorImpl(
                        "BlockDetector:caveDetector", Set.of("engine:air"), range, audioManager,
                        "BlockDetector:ScannerBeep", 250, 1000);
        BlockRegion nonAerialRange = new BlockRegion(-3, -3, -3, 3, 3, 3);
        data.setNonAerialRange(nonAerialRange);

        blockDetectorSystem.addDetector(data);

        assertThat(blockDetectorSystem.getDetectors())
                .containsEntry(data.getDetectorUri(), data);
    }

    @Nested
    class PlayerDetectorTest {
        public static final String BLOCK_URI = "CoreAssets:Snow";

        @BeforeEach
        void giveDetectorToPlayer(LocalPlayer player) {
            var entity = player.getCharacterEntity();

            var items = blockDetectorSystem.getDetectors().keySet().stream()
                    .map(uri -> {
                        var item = new InventoryItem();
                        item.uri = uri;
                        return item;
                    })
                    .collect(Collectors.toList());
            entity.send(new RequestInventoryEvent(items));
        }

        @Test
        void detectedBlockTest(LocalPlayer player, InventoryManager inventory,
                             BlockManager blockManager, WorldProvider worldProvider,
                             MainLoop main) {
            var character = player.getCharacterEntity();

            // The detector only works while the player has it selected,
            // so we need to select it.
            for (int slot = 0; slot < inventory.getNumSlots(character); slot++) {
                var item = inventory.getItemInSlot(character, slot);
                var prefab = item.getParentPrefab();
                if (prefab == null || !item.exists()) {
                    continue;
                }
                // FIXME: there's no way to know if this detector goes with a given BLOCK_URI
                if (prefab.getName().startsWith("BlockDetector")) {
                    final var selectedSlot = slot;
                    character.updateComponent(SelectedInventorySlotComponent.class, selected -> {
                        selected.slot = selectedSlot;
                        return selected;
                    });
                    break;
                }
            }

            //place a block and check if it is detected
            Vector3i pos = new Vector3i(1, 1, 1);
            main.runUntil(main.makeBlocksRelevant(new BlockRegion(pos)));
            worldProvider.setBlock(pos, blockManager.getBlock(BLOCK_URI));
            blockDetectorSystem.detectBlocks();

            assertThat(blockDetectorSystem.getDetectedBlocks()).contains(pos);
        }
    }
}

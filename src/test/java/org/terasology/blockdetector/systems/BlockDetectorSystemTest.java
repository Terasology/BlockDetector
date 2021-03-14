/*
 * Copyright 2017 MovingBlocks
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
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.context.Context;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.event.ResetCameraEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.extension.Dependencies;
import org.terasology.moduletestingenvironment.extension.UseWorldGenerator;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MTEExtension.class)
@UseWorldGenerator("ModuleTestingEnvironment:empty")
@Dependencies({"BlockDetector"})
public class BlockDetectorSystemTest {

    @In
    private AudioManager audioManager;
    @In
    private ModuleTestingHelper helper;

    private DetectorData data;

    BlockDetectorSystemImpl blockDetectorSystem = new BlockDetectorSystemImpl();

    /* This updateTest is created to test if the timeSinceLastUpdate is increased by delta.
    To make the system runs, we create a dummy local player so that the localPlayer data is not null and does not throw a null exception
    */
    @Test
    public void updateTest() throws IOException {
        //create a dummy local player
        Context clientContext = helper.createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        blockDetectorSystem.setLocalPlayer(clientContext.get(LocalPlayer.class));

        blockDetectorSystem.setTimeSinceLastUpdate(5);
        blockDetectorSystem.update(3);
        assertEquals(8, blockDetectorSystem.getTimeSinceLastUpdate(), 3);
    }

    // This method should return the Detectors in not null condition. It's just running the method and check if Detectors is not null
    @Test
    public void initialiseTest() {
        blockDetectorSystem.initialise();
        assertNotNull(blockDetectorSystem.getDetectors());
    }

    /* These test method is to test whether the method addDetector is functioning or not.
    We use the same method CaveDetectorSystem to add.
    Then we want to assert if the map contains the same data we want to be stored there.
    When we try to get the values, it is started and ended with '[' and ']'.
    Therefore, we use string data type so we can use substring to remove them.
    Then we assert that both values (expected which is the data and actual which is the map value substring) are equal
    */
    @Test
    public void detectedBlockTest() throws IOException {
        WorldProvider worldProvider = helper.getHostContext().get(WorldProvider.class);
        BlockManager blockManager = helper.getHostContext().get(BlockManager.class);

        //create a dummy local player
        Context clientContext = helper.createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        blockDetectorSystem.setLocalPlayer(clientContext.get(LocalPlayer.class));

        //place a block and check if it is detected
        Vector3i pos = new Vector3i(1, 1, 1);
        helper.forceAndWaitForGeneration(pos);
        worldProvider.setBlock(pos, blockManager.getBlock("engine:stone"));
        blockDetectorSystem.detectBlocks();

        //at first, the detectedBlocks is null so it won't be null anymore if a new block is added
        assertNotNull(blockDetectorSystem.getDetectedBlocks());
    }

    /* Inside this method, the detectedBlocks set will be added with a block if a block is detected.
    First, we create a dummy local player to make the system runs and does not throw null exception.
    Next is we place a block with worldprovider.setBlock -- and then we run the method and check if the detectedBlocks set is not null (filled with our block)
    */
    @Test
    public void detectorTest() {
        BlockRegion range = new BlockRegion(-1, -55, -1, 1, -5, 1);
        data = new LinearAudioDetectorImpl("BlockDetector:caveDetector", Sets.newHashSet("engine:air"), range, audioManager, "BlockDetector:ScannerBeep", 250, 1000);
        BlockRegion nonAerialRange = new BlockRegion(-3, -3, -3, 3, 3, 3);
        data.setNonAerialRange(nonAerialRange);

        blockDetectorSystem.addDetector(data);

        //we use string because we want to modify the data (there are [ and ] that we don't want
        String value = blockDetectorSystem.getDetectors().values().toString();
        //to compare them, they have to be in the same data type
        String dataToCompare = this.data.toString();

        //we use substring because we don't want the '[' and ']' to be asserted
        assertEquals(dataToCompare, value.substring(1, value.length() - 1));
    }
}

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
import org.junit.Assert;
import org.junit.Test;
import org.terasology.audio.AudioManager;
import org.terasology.blockdetector.utilities.DetectorData;
import org.terasology.blockdetector.utilities.LinearAudioDetectorImpl;
import org.terasology.context.Context;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.ResetCameraEvent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

public class BlockDetectorSystemTest extends ModuleTestingEnvironment {

    @In
    private AudioManager audioManager;

    private DetectorData data;

    BlockDetectorSystemImpl blockDetectorSystem = new BlockDetectorSystemImpl();

    /* This updateTest is created to test if the timeSinceLastUpdate is increased by delta.
    To make the system runs, we create a dummy local player so that the localPlayer data is not null and does not throw a null exception
    */
    @Test
    public void updateTest() {
        //create a dummy local player
        Context clientContext = createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        blockDetectorSystem.setLocalPlayer(clientContext.get(LocalPlayer.class));

        blockDetectorSystem.setTimeSinceLastUpdate(5);
        blockDetectorSystem.update(3);
        Assert.assertEquals(8, blockDetectorSystem.getTimeSinceLastUpdate(), 3);
    }

    // This method should return the Detectors in not null condition. It's just running the method and check if Detectors is not null
    @Test
    public void initialiseTest() {
        blockDetectorSystem.initialise();
        Assert.assertNotNull(blockDetectorSystem.getDetectors());
    }

   /* These test method is to test whether the method addDetector is functioning or not.
   We use the same method CaveDetectorSystem to add.
   Then we want to assert if the map contains the same data we want to be stored there.
   When we try to get the values, it is started and ended with '[' and ']'.
   Therefore, we use string data type so we can use substring to remove them.
   Then we assert that both values (expected which is the data and actual which is the map value substring) are equal
   */
    @Test
    public void detectedBlockTest(){
        WorldProvider worldProvider = getHostContext().get(WorldProvider.class);
        BlockManager blockManager = getHostContext().get(BlockManager.class);

        //create a dummy local player
        Context clientContext = createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        blockDetectorSystem.setLocalPlayer(clientContext.get(LocalPlayer.class));

        //place a block and check if it is detected
        Vector3i pos = new Vector3i(1, 1, 1);
        forceAndWaitForGeneration(pos);
        worldProvider.setBlock(pos, blockManager.getBlock("engine:stone"));
        blockDetectorSystem.detectBlocks();

        //at first, the detectedBlocks is null so it won't be null anymore if a new block is added
        Assert.assertNotNull(blockDetectorSystem.getDetectedBlocks());
    }

    /* Inside this method, the detectedBlocks set will be added with a block if a block is detected.
    First, we create a dummy local player to make the system runs and does not throw null exception.
    Next is we place a block with worldprovider.setBlock -- and then we run the method and check if the detectedBlocks set is not null (filled with our block)
    */
    @Test
    public void detectorTest(){
        Region3i range = Region3i.createFromMinMax(new Vector3i(-1, -55, -1), new Vector3i(1, -5, 1));
        data = new LinearAudioDetectorImpl("BlockDetector:caveDetector", Sets.newHashSet("engine:air"), range, audioManager, "BlockDetector:ScannerBeep", 250, 1000);
        Region3i nonAerialRange = Region3i.createFromMinMax(new Vector3i(-3, -3, -3), new Vector3i(3, 3, 3));
        data.setNonAerialRange(nonAerialRange);

        blockDetectorSystem.addDetector(data);

        //we use string because we want to modify the data (there are [ and ] that we don't want
        String value = blockDetectorSystem.getDetectors().values().toString();
        //to compare them, they have to be in the same data type
        String dataToCompare = this.data.toString();

        //we use substring because we don't want the '[' and ']' to be asserted
        Assert.assertEquals(dataToCompare, value.substring(1, value.length()-1));
    }
}

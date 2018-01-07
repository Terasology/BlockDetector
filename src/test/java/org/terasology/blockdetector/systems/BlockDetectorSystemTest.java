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

    BlockDetectorSystemImpl obj = new BlockDetectorSystemImpl();

    @Test
    public void updateTest() {
        //create a dummy local player
        Context clientContext = createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        obj.setLocalPlayer(clientContext.get(LocalPlayer.class));

        obj.setTimeSinceLastUpdate(5);
        obj.update(3);
        Assert.assertEquals(8, obj.getTimeSinceLastUpdate(), 3);
    }

    @Test
    public void initialiseTest() {
        obj.initialise();
        Assert.assertNotNull(obj.getDetectors());
    }

    @Test
    public void detectedBlockTest(){
        WorldProvider worldProvider = getHostContext().get(WorldProvider.class);
        BlockManager blockManager = getHostContext().get(BlockManager.class);

        //create a dummy local player
        Context clientContext = createClient();
        clientContext.get(LocalPlayer.class).getClientEntity().send(new ResetCameraEvent());
        obj.setLocalPlayer(clientContext.get(LocalPlayer.class));

        //place a block and check if it is detected
        Vector3i pos = new Vector3i(1,1,1);
        forceAndWaitForGeneration(pos);
        worldProvider.setBlock(pos, blockManager.getBlock("engine:stone"));
        obj.detectBlocks();

        //at first, the detectedBlocks is null so it won't be null anymore if a new block is added
        Assert.assertNotNull(obj.getDetectedBlocks());
    }

    @Test
    public void detectorTest(){

        Region3i range = Region3i.createFromMinMax(new Vector3i(-1, -55, -1), new Vector3i(1, -5, 1));
        data = new LinearAudioDetectorImpl("BlockDetector:caveDetector", Sets.newHashSet("engine:air"), range, audioManager, "BlockDetector:ScannerBeep", 250, 1000);
        Region3i nonAerialRange = Region3i.createFromMinMax(new Vector3i(-3, -3, -3), new Vector3i(3, 3, 3));
        data.setNonAerialRange(nonAerialRange);

        //Run the addDetector method
        obj.addDetector(data);

        //we use string because we want to modify the data (there are [ and ] that we don't want
        String value = obj.getDetectors().values().toString();
        //to compare them, they have to be in the same data type
        String dataToCompare = this.data.toString();

        //we use substring because we don't want the '[' and ']' to be asserted
        Assert.assertEquals(dataToCompare, value.substring(1, value.length()-1));
    }
}

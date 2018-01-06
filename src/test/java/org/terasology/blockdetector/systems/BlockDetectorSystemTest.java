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

import org.junit.Assert;
import org.junit.Test;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

import static org.junit.Assert.*;

public class BlockDetectorSystemTest extends ModuleTestingEnvironment {

    BlockDetectorSystemImpl obj = new BlockDetectorSystemImpl();
    @Test
    public void updateTest() {
        obj.setTimeSinceLastUpdate(5);
        obj.update(3);
        Assert.assertEquals(8, obj.getTimeSinceLastUpdate());
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
        worldProvider.setBlock(obj.getPlayerPosition(), blockManager.getBlock("engine:stone"));
        obj.detectBlocks();
        Assert.assertNotNull(obj.getDetectedBlocks());
    }
}
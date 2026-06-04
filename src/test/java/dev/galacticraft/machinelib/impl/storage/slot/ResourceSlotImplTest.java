/*
 * Copyright (c) 2021-2026 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.util.Utils;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class ResourceSlotImplTest<Resource, Slot extends ResourceSlot<Resource>> implements MinecraftTest {
    protected static final long CAPACITY = 64;

    final DataComponentPatch dcp = Utils.generateComponents();

    final Resource resource0;
    final Resource resource1;

    Slot slot;

    ResourceSlotImplTest(Resource resource0, Resource resource1) {
        this.resource0 = resource0;
        this.resource1 = resource1;
    }

    abstract Slot createSlot();

    @BeforeEach
    void setup() {
        slot = createSlot();
    }

    @AfterEach
    void verify() {
        assertTrue(((ResourceSlotImpl<?>) this.slot).isSane());
    }

    @Test
    void verifyInitialState() {
        assertEquals(CAPACITY, slot.getCapacity());
    }

    @Test
    void set() {
        slot.set(resource0, dcp, 1);
        assertEquals(resource0, slot.getResource());
        assertEquals(dcp, slot.getComponents());
        assertEquals(1, slot.getAmount());
    }

    @Test
    void serializeEmpty() {
        slot.createTag();
    }

    @Test
    void deserializeEmpty() {
        slot.readTag(new CompoundTag());
        assertTrue(slot.isEmpty());
    }

    @Test
    void serializeEmptyPacket() {
        RegistryFriendlyByteBuf buf = Utils.createBuf();
        slot.writePacket(buf);
        buf.release();
    }

    @AfterEach
    void deserialize() {
        Slot slot1 = createSlot();
        slot1.readTag(slot.createTag());

        assertEquals(slot.getResource(), slot1.getResource());
        assertEquals(slot.getComponents(), slot1.getComponents());
        assertEquals(slot.getAmount(), slot1.getAmount());
    }

    @AfterEach
    void deserializePacket() {
        RegistryFriendlyByteBuf buf = Utils.createBuf();
        slot.writePacket(buf);
        Slot slot1 = createSlot();
        slot1.readPacket(buf);
        buf.release();

        assertEquals(slot.getResource(), slot1.getResource());
        assertEquals(slot.getComponents(), slot1.getComponents());
        assertEquals(slot.getAmount(), slot1.getAmount());
    }

    @Nested
    class Empty {
        @Test
        void isEmpty() {
            assertTrue(slot.isEmpty());
        }

        @Test
        void isNotFull() {
            assertFalse(slot.isFull());
        }

        @Test
        void canInsertOne() {
            assertTrue(slot.canInsert(resource0));
        }

        @Test
        void canInsertCapacity() {
            assertTrue(slot.canInsert(resource0, CAPACITY));
        }

        @Test
        void cannotInsertOverflow() {
            assertFalse(slot.canInsert(resource0, CAPACITY + 1));
        }

        @Test
        void tryInsertOne() {
            assertEquals(1, slot.tryInsert(resource0, 1));
            assertTrue(slot.isEmpty());
        }

        @Test
        void tryInsertCapacity() {
            assertEquals(CAPACITY, slot.tryInsert(resource0, CAPACITY));
            assertTrue(slot.isEmpty());
        }

        @Test
        void tryInsertOverflow() {
            assertEquals(CAPACITY, slot.tryInsert(resource0, CAPACITY + 1));
            assertTrue(slot.isEmpty());
        }

        @Test
        void insertOne() {
            assertEquals(1, slot.insert(resource0, 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(1, slot.getAmount());
        }

        @Test
        void insertCapacity() {
            assertEquals(CAPACITY, slot.insert(resource0, CAPACITY));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void insertOverflow() {
            assertEquals(CAPACITY, slot.insert(resource0, CAPACITY + 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void containsNothing() {
            assertFalse(slot.contains(resource0));
            assertEquals(0, slot.getAmount());
            assertTrue(slot.getComponents().isEmpty());
        }

        @Test
        void cannotExtract() {
            assertFalse(slot.canExtract(1));
            assertEquals(0, slot.tryExtract(1));
            assertEquals(0, slot.extract(1));
        }

        @Test
        void cannotExtractOne() {
            assertNull(slot.extractOne());
        }
    }

    @Nested
    class HalfFull {
        static final long HALF_CAPACITY = CAPACITY / 2;

        @BeforeEach
        void setup() {
            slot.set(resource0, CAPACITY / 2);
        }

        @Test
        void isNotEmpty() {
            assertFalse(slot.isEmpty());
        }

        @Test
        void isNotFull() {
            assertFalse(slot.isFull());
        }

        @Test
        void canInsertOne() {
            assertTrue(slot.canInsert(resource0));
        }

        @Test
        void cannotInsertDifferent() {
            assertFalse(slot.canInsert(resource1));
        }

        @Test
        void cannotInsertOverflow() {
            assertFalse(slot.canInsert(resource0, CAPACITY));
        }

        @Test
        void cannotInsertDifferentComponents() {
            assertFalse(slot.canInsert(resource0, dcp));
        }

        @Test
        void tryInsertOne() {
            assertEquals(1, slot.tryInsert(resource0, 1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryInsertOverflow() {
            assertEquals(CAPACITY / 2, slot.tryInsert(resource0, CAPACITY));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryInsertDifferent() {
            assertEquals(0, slot.tryInsert(resource1, 1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryInsertDifferentComponents() {
            assertEquals(0, slot.tryInsert(resource0, dcp, 1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void insertOne() {
            assertEquals(1, slot.insert(resource0, 1));

            assertEquals(resource0, slot.getResource());
            assertEquals(HALF_CAPACITY + 1, slot.getAmount());
        }

        @Test
        void insertOverflow() {
            assertEquals(HALF_CAPACITY, slot.insert(resource0, CAPACITY));

            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void insertDifferent() {
            assertEquals(0, slot.insert(resource1, 1));

            assertEquals(resource0, slot.getResource());
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void insertDifferentComponents() {
            assertEquals(0, slot.insert(resource0, dcp, 1));

            assertEquals(resource0, slot.getResource());
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void contains() {
            assertTrue(slot.contains(resource0));
            assertTrue(slot.contains(resource0, DataComponentPatch.EMPTY));
            assertEquals(HALF_CAPACITY, slot.getAmount());
            assertTrue(slot.getComponents().isEmpty());
        }

        @Test
        void canExtract() {
            assertTrue(slot.canExtract(1));
            assertTrue(slot.canExtract(resource0, 1));
            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, 1));
        }

        @Test
        void canExtractFully() {
            assertTrue(slot.canExtract(HALF_CAPACITY));
            assertTrue(slot.canExtract(resource0, HALF_CAPACITY));
            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, HALF_CAPACITY));
        }

        @Test
        void cannotExtractOverflow() {
            assertFalse(slot.canExtract(HALF_CAPACITY + 1));
            assertFalse(slot.canExtract(resource0, HALF_CAPACITY + 1));
            assertFalse(slot.canExtract(resource0, DataComponentPatch.EMPTY, HALF_CAPACITY + 1));
        }

        @Test
        void cannotExtractDifferent() {
            assertFalse(slot.canExtract(resource1, 1));
            assertFalse(slot.canExtract(resource1, DataComponentPatch.EMPTY, 1));
        }

        @Test
        void tryExtract() {
            assertEquals(1, slot.tryExtract(1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractFully() {
            assertEquals(HALF_CAPACITY, slot.tryExtract(HALF_CAPACITY));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractOverflow() {
            assertEquals(HALF_CAPACITY, slot.tryExtract(HALF_CAPACITY + 1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractDifferent() {
            assertEquals(0, slot.tryExtract(resource1, 1));
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void extract() {
            assertEquals(1, slot.extract(1));
            assertEquals(resource0, slot.getResource());
            assertEquals(HALF_CAPACITY - 1, slot.getAmount());
        }

        @Test
        void extractFully() {
            assertEquals(HALF_CAPACITY, slot.extract(HALF_CAPACITY));
            assertNull(slot.getResource());
            assertEquals(0, slot.getAmount());
        }

        @Test
        void extractOverflow() {
            assertEquals(HALF_CAPACITY, slot.extract(HALF_CAPACITY + 1));
            assertNull(slot.getResource());
            assertEquals(0, slot.getAmount());
        }

        @Test
        void extractDifferent() {
            assertEquals(0, slot.extract(resource1, 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(HALF_CAPACITY, slot.getAmount());
        }

        @Test
        void extractOne() {
            assertEquals(resource0, slot.extractOne());
            assertEquals(HALF_CAPACITY - 1, slot.getAmount());

            assertTrue(slot.extractOne(resource0));
            assertEquals(HALF_CAPACITY - 2, slot.getAmount());
        }

        @Test
        void extractOneDifferent() {
            assertFalse(slot.extractOne(resource1));
        }

        @Test
        void extractOneDifferentComponents() {
            assertFalse(slot.extractOne(resource0, dcp));
        }

        @Test
        void abortedExtraction() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(1, slot.extract(resource0, DataComponentPatch.EMPTY, 1, transaction));
                assertEquals(HALF_CAPACITY - 1, slot.getAmount());
                assertEquals(modifications + 1, slot.getModifications());
            }

            assertEquals(HALF_CAPACITY, slot.getAmount());
            assertEquals(modifications, slot.getModifications());
        }

        @Test
        void abortedInsertion() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(1, slot.insert(resource0, DataComponentPatch.EMPTY, 1, transaction));
                assertEquals(HALF_CAPACITY + 1, slot.getAmount());
                assertEquals(modifications + 1, slot.getModifications());
            }

            assertEquals(HALF_CAPACITY, slot.getAmount());
            assertEquals(modifications, slot.getModifications());
        }

        @Test
        void abortedExchange() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(4, slot.insert(resource0, DataComponentPatch.EMPTY, 4, transaction));
                assertEquals(HALF_CAPACITY + 4, slot.getAmount());
                assertEquals(1, slot.extract(resource0, DataComponentPatch.EMPTY, 1, transaction));
                assertEquals(HALF_CAPACITY + 3, slot.getAmount());
                assertEquals(modifications + 2, slot.getModifications());
            }

            assertEquals(HALF_CAPACITY, slot.getAmount());
            assertEquals(modifications, slot.getModifications());
        }

        @Test
        void committedExtraction() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(1, slot.extract(resource0, DataComponentPatch.EMPTY, 1, transaction));
                transaction.commit();
            }

            assertEquals(HALF_CAPACITY - 1, slot.getAmount());
            assertEquals(modifications + 1, slot.getModifications());
        }

        @Test
        void committedInsertion() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(1, slot.insert(resource0, DataComponentPatch.EMPTY, 1, transaction));
                transaction.commit();
            }

            assertEquals(HALF_CAPACITY + 1, slot.getAmount());
            assertEquals(modifications + 1, slot.getModifications());
        }

        @Test
        void committedExchange() {
            long modifications = slot.getModifications();
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(4, slot.insert(resource0, DataComponentPatch.EMPTY, 4, transaction));
                assertEquals(HALF_CAPACITY + 4, slot.getAmount());
                assertEquals(1, slot.extract(resource0, DataComponentPatch.EMPTY, 1, transaction));
                transaction.commit();
            }

            assertEquals(HALF_CAPACITY + 3, slot.getAmount());
            assertEquals(modifications + 2, slot.getModifications());
        }
    }

    @Nested
    class Full {
        @BeforeEach
        void setup() {
            slot.set(resource0, CAPACITY);
        }

        @Test
        void isNotEmpty() {
            assertFalse(slot.isEmpty());
        }

        @Test
        void isFull() {
            assertTrue(slot.isFull());
        }

        @Test
        void cannotInsert() {
            assertFalse(slot.canInsert(resource0));
            assertFalse(slot.canInsert(resource0, 1));
            assertFalse(slot.canInsert(resource0, dcp));
        }

        @Test
        void tryInsert() {
            assertEquals(0, slot.tryInsert(resource0, 1));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void tryInsertDifferent() {
            assertEquals(0, slot.tryInsert(resource1, 1));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void insert() {
            assertEquals(0, slot.insert(resource0, 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void insertDifferent() {
            assertEquals(0, slot.insert(resource1, 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void contains() {
            assertTrue(slot.contains(resource0));
            assertTrue(slot.contains(resource0, DataComponentPatch.EMPTY));
            assertEquals(CAPACITY, slot.getAmount());
            assertTrue(slot.getComponents().isEmpty());
        }

        @Test
        void canExtract() {
            assertTrue(slot.canExtract(1));
            assertTrue(slot.canExtract(resource0, 1));
            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, 1));
        }

        @Test
        void canExtractFully() {
            assertTrue(slot.canExtract(CAPACITY));
            assertTrue(slot.canExtract(resource0, CAPACITY));
            assertTrue(slot.canExtract(resource0, DataComponentPatch.EMPTY, CAPACITY));
        }

        @Test
        void cannotExtractOverflow() {
            assertFalse(slot.canExtract(CAPACITY + 1));
            assertFalse(slot.canExtract(resource0, CAPACITY + 1));
            assertFalse(slot.canExtract(resource0, DataComponentPatch.EMPTY, CAPACITY + 1));
        }

        @Test
        void cannotExtractDifferent() {
            assertFalse(slot.canExtract(resource1, 1));
            assertFalse(slot.canExtract(resource1, DataComponentPatch.EMPTY, 1));
        }

        @Test
        void tryExtract() {
            assertEquals(1, slot.tryExtract(1));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractFully() {
            assertEquals(CAPACITY, slot.tryExtract(CAPACITY));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractOverflow() {
            assertEquals(CAPACITY, slot.tryExtract(CAPACITY + 1));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void tryExtractDifferent() {
            assertEquals(0, slot.tryExtract(resource1, 1));
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void extract() {
            assertEquals(1, slot.extract(1));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY - 1, slot.getAmount());
        }

        @Test
        void extractFully() {
            assertEquals(CAPACITY, slot.extract(CAPACITY));
            assertNull(slot.getResource());
            assertEquals(0, slot.getAmount());
        }

        @Test
        void extractOverflow() {
            assertEquals(CAPACITY, slot.extract(CAPACITY + 1));
            assertNull(slot.getResource());
            assertEquals(0, slot.getAmount());
        }

        @Test
        void extractDifferent() {
            assertEquals(0, slot.extract(resource1, 1));
            assertEquals(resource0, slot.getResource());
            assertEquals(CAPACITY, slot.getAmount());
        }

        @Test
        void extractOne() {
            assertEquals(resource0, slot.extractOne());
            assertEquals(CAPACITY - 1, slot.getAmount());

            assertTrue(slot.extractOne(resource0));
            assertEquals(CAPACITY - 2, slot.getAmount());
        }

        @Test
        void extractOneDifferent() {
            assertFalse(slot.extractOne(resource1));
        }

        @Test
        void extractOneDifferentComponents() {
            assertFalse(slot.extractOne(resource0, dcp));
        }
    }
}

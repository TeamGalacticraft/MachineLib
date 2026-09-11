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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.test.MinecraftTest;
import dev.galacticraft.machinelib.test.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.LongTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MachineEnergyStorageImplTest implements MinecraftTest {
    private static final long CAPACITY = 2000;
    private MachineEnergyStorage storage;

    @BeforeEach
    void setup() {
        this.storage = new MachineEnergyStorageImpl(CAPACITY, 0, 0);
    }

    @Test
    void set() {
        storage.setEnergy(1000);
        assertEquals(1000, storage.getAmount());
    }

    @Test
    void getCapacity() {
        assertEquals(CAPACITY, this.storage.getCapacity());
    }

    @AfterEach
    void nbtSerialization() {
        long amount = storage.getAmount();
        LongTag tag = storage.createTag();
        storage.setEnergy(Utils.random(0, amount, CAPACITY));
        storage.readTag(tag);

        assertEquals(amount, storage.getAmount());
    }

    @AfterEach
    void packetSerialization() {
        long amount = storage.getAmount();
        ByteBuf buf = Unpooled.buffer();

        storage.writePacket(buf);
        storage.setEnergy(Utils.random(0, amount, CAPACITY));
        storage.readPacket(buf);

        assertEquals(amount, storage.getAmount());
    }

    @Nested
    class Empty {
        @Test
        void isEmpty() {
            assertTrue(storage.isEmpty());
        }

        @Test
        void isNotFull() {
            assertFalse(storage.isFull());
        }

        @Test
        void cannotExtract() {
            assertFalse(storage.canExtract(1));
        }

        @Test
        void tryExtract() {
            assertEquals(0, storage.tryExtract(1));
        }

        @Test
        void extract() {
            assertEquals(0, storage.extract(1));
        }

        @Test
        void canInsert() {
            assertTrue(storage.canInsert(1));
        }

        @Test
        void canInsertCapacity() {
            assertTrue(storage.canInsert(CAPACITY));
        }

        @Test
        void canInsertOverCapacity() {
            assertFalse(storage.canInsert(CAPACITY + 1));
        }

        @Test
        void tryInsert() {
            assertEquals(1, storage.tryInsert(1));
            assertEquals(0, storage.getAmount());
        }

        @Test
        void tryInsertCapacity() {
            assertEquals(CAPACITY, storage.tryInsert(CAPACITY));
            assertEquals(0, storage.getAmount());
        }

        @Test
        void tryInsertOverCapacity() {
            assertEquals(CAPACITY, storage.tryInsert(CAPACITY + 1));
            assertEquals(0, storage.getAmount());
        }

        @Test
        void insert() {
            assertEquals(1, storage.insert(1));
            assertEquals(1, storage.getAmount());
        }

        @Test
        void insertCapacity() {
            assertEquals(CAPACITY, storage.insert(CAPACITY));
            assertEquals(CAPACITY, storage.getAmount());
        }

        @Test
        void insertOverCapacity() {
            assertEquals(CAPACITY, storage.insert(CAPACITY + 1));
            assertEquals(CAPACITY, storage.getAmount());
        }

        @Test
        void insertExactOverCapacity() {
            assertFalse(storage.insertExact(CAPACITY + 1));
            assertEquals(0, storage.getAmount());
        }
    }

    @Nested
    class HalfFull {
        public static final long HALF_CAPACITY = CAPACITY / 2;

        @BeforeEach
        void setup() {
            storage.setEnergy(HALF_CAPACITY);
        }

        @Test
        void isNotEmpty() {
            assertFalse(storage.isEmpty());
        }

        @Test
        void isNotFull() {
            assertFalse(storage.isFull());
        }

        @Test
        void canExtract() {
            assertTrue(storage.canExtract(HALF_CAPACITY));
        }

        @Test
        void cannotExtractOverCapacity() {
            assertFalse(storage.canExtract(HALF_CAPACITY + 1));
        }

        @Test
        void tryExtract() {
            assertEquals(HALF_CAPACITY, storage.tryExtract(HALF_CAPACITY));
            assertEquals(HALF_CAPACITY, storage.getAmount());
        }

        @Test
        void extract() {
            assertEquals(HALF_CAPACITY, storage.extract(HALF_CAPACITY));
            assertEquals(0, storage.getAmount());
        }

        @Test
        void canInsert() {
            assertTrue(storage.canInsert(HALF_CAPACITY));
        }

        @Test
        void cannotInsertCapacity() {
            assertFalse(storage.canInsert(CAPACITY));
        }

        @Test
        void canInsertOverCapacity() {
            assertFalse(storage.canInsert(HALF_CAPACITY + 1));
        }

        @Test
        void tryInsert() {
            assertEquals(HALF_CAPACITY, storage.tryInsert(HALF_CAPACITY));
            assertEquals(HALF_CAPACITY, storage.getAmount());
        }

        @Test
        void insert() {
            assertEquals(HALF_CAPACITY, storage.insert(HALF_CAPACITY));
            assertEquals(CAPACITY, storage.getAmount());
        }

        @Test
        void insertExact() {
            assertTrue(storage.insertExact(HALF_CAPACITY));
            assertEquals(CAPACITY, storage.getAmount());
        }

        @Test
        void insertExactOverCapacity() {
            assertFalse(storage.insertExact(HALF_CAPACITY + 1));
            assertEquals(HALF_CAPACITY, storage.getAmount());
        }

        @Test
        void abortInsertion() {
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(HALF_CAPACITY, storage.insert(HALF_CAPACITY + 1, transaction));
                assertEquals(CAPACITY, storage.getAmount());
            }

            assertEquals(HALF_CAPACITY, storage.getAmount());
        }

        @Test
        void abortExtraction() {
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(HALF_CAPACITY, storage.extract(HALF_CAPACITY + 1, transaction));
                assertEquals(0, storage.getAmount());
            }

            assertEquals(HALF_CAPACITY, storage.getAmount());
        }

        @Test
        void commitInsertion() {
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(HALF_CAPACITY, storage.insert(HALF_CAPACITY, transaction));
                transaction.commit();
            }

            assertEquals(CAPACITY, storage.getAmount());
        }

        @Test
        void commitExtraction() {
            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(HALF_CAPACITY, storage.extract(HALF_CAPACITY, transaction));
                transaction.commit();
            }

            assertEquals(0, storage.getAmount());
        }
    }

    @Nested
    class Full {
        @BeforeEach
        void setup() {
            storage.setEnergy(CAPACITY);
        }

        @Test
        void isNotEmpty() {
            assertFalse(storage.isEmpty());
        }

        @Test
        void isFull() {
            assertTrue(storage.isFull());
        }

        @Test
        void canExtract() {
            assertTrue(storage.canExtract(1));
        }

        @Test
        void canExtractCapacity() {
            assertTrue(storage.canExtract(CAPACITY));
        }

        @Test
        void tryExtract() {
            assertEquals(1, storage.tryExtract(1));
        }

        @Test
        void tryExtractCapacity() {
            assertEquals(CAPACITY, storage.tryExtract(CAPACITY));
        }

        @Test
        void extract() {
            assertEquals(1, storage.extract(1));
        }

        @Test
        void extractCapacity() {
            assertEquals(CAPACITY, storage.extract(CAPACITY));
        }

        @Test
        void cannotInsert() {
            assertFalse(storage.canInsert(1));
        }

        @Test
        void tryInsert() {
            assertEquals(0, storage.tryInsert(1));
        }

        @Test
        void insert() {
            assertEquals(0, storage.insert(1));
        }

        @Test
        void insertExact() {
            assertFalse(storage.insertExact(1));
        }
    }
}

/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.transfer;

import org.jetbrains.annotations.Nullable;

public enum TransferType {
    INPUT(0x009001, true, false, true, true), // external: insertion only, players: insertion and extraction allowed
    OUTPUT(0xa7071e, false, true, false, true), // external: extraction only, players: extraction only
    STORAGE(0x008d90, true, true, true, true), // external: insertion and extraction allowed, players: insertion and extraction allowed
    TRANSFER(0x908400, false, false, true, true), // external: immutable, players: insertion and extraction allowed - e.g. battery slots
    PROCESSING(0x908400, true, true, true, true), // external: insertion and extraction allowed, players: insertion and extraction allowed - e.g. bucket slots
    STRICT_INPUT(0x004700, true, false, false, false), // external: insertion only, players: immutable
    STRICT_OUTPUT(0x54030f, false, true, false, false); // external: extraction only, players: immutable

    private final int color;
    private final boolean externalInsert;
    private final boolean externalExtract;
    private final boolean playerInsert;
    private final boolean playerExtract;

    TransferType(int color, boolean externalInsert, boolean externalExtract, boolean playerInsert, boolean playerExtract) {
        this.color = color;
        this.externalInsert = externalInsert;
        this.externalExtract = externalExtract;
        this.playerInsert = playerInsert;
        this.playerExtract = playerExtract;
    }

    public boolean externalExtraction() {
        return this.externalExtract;
    }

    public boolean externalInsertion() {
        return this.externalInsert;
    }

    public @Nullable ResourceFlow getExternalFlow() {
        return this.externalExtract ? this.externalInsert ? ResourceFlow.BOTH : ResourceFlow.OUTPUT : this.externalInsert ? ResourceFlow.INPUT : null;
    }

    public boolean playerInsertion() {
        return this.playerInsert;
    }

    public boolean playerExtraction() {
        return this.playerExtract;
    }

    public boolean isInput() {
        return this == INPUT;
    }

    public boolean isOutput() {
        return this == OUTPUT;
    }

    public int color() {
        return this.color;
    }
}

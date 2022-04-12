/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.api.gas;

import dev.galacticraft.impl.machine.Constant;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Defines some common gases for convenience.
 */
public final class Gases {
    public static final Identifier HYDROGEN_ID = new Identifier(Constant.MOD_ID, "hydrogen");
    /**
     * Hydrogen gas.
     */
    public static final Gas HYDROGEN = Gas.create(
            new TranslatableText("ui.machinelib.gases.hydrogen"),
            null,
            "H2"
    );
    public static final Identifier NITROGEN_ID = new Identifier(Constant.MOD_ID, "nitrogen");
    /**
     * Nitrogen gas.
     */
    public static final Gas NITROGEN = Gas.create(
            new TranslatableText("ui.machinelib.gases.nitrogen"),
            null,
            "N2"
    );
    public static final Identifier OXYGEN_ID = new Identifier(Constant.MOD_ID, "oxygen");
    /**
     * Oxygen gas.
     */
    public static final Gas OXYGEN = Gas.create(
            new TranslatableText("ui.machinelib.gases.oxygen"),
            null,
            "O2"
    );
    public static final Identifier CARBON_DIOXIDE_ID = new Identifier(Constant.MOD_ID, "carbon_dioxide");
    /**
     * Carbon dioxide.
     */
    public static final Gas CARBON_DIOXIDE = Gas.create(
            new TranslatableText("ui.machinelib.gases.carbon_dioxide"),
            null,
            "CO2"
    );
    public static final Identifier WATER_VAPOR_ID = new Identifier(Constant.MOD_ID, "water_vapor");
    /**
     * Water vapor.
     */
    public static final Gas WATER_VAPOR = Gas.create(
            new TranslatableText("ui.machinelib.gases.water_vapor"),
            null,
            "H2O"
    );
    public static final Identifier METHANE_ID = new Identifier(Constant.MOD_ID, "methane");
    /**
     * Methane.
     */
    public static final Gas METHANE = Gas.create(
            new TranslatableText("ui.machinelib.gases.methane"),
            null,
            "CH4"
    );
    public static final Identifier HELIUM_ID = new Identifier(Constant.MOD_ID, "helium");
    /**
     * Helium.
     */
    public static final Gas HELIUM = Gas.create(
            new TranslatableText("ui.machinelib.gases.helium"),
            null,
            "He"
    );
    public static final Identifier ARGON_ID = new Identifier(Constant.MOD_ID, "argon");
    /**
     * Argon.
     */
    public static final Gas ARGON = Gas.create(
            new TranslatableText("ui.machinelib.gases.argon"),
            null,
            "Ar"
    );
    public static final Identifier NEON_ID = new Identifier(Constant.MOD_ID, "neon");
    /**
     * Neon.
     */
    public static final Gas NEON = Gas.create(
            new TranslatableText("ui.machinelib.gases.neon"),
            null,
            "Ne"
    );
    public static final Identifier KRYPTON_ID = new Identifier(Constant.MOD_ID, "krypton");
    /**
     * Krypton.
     */
    public static final Gas KRYPTON = Gas.create(
            new TranslatableText("ui.machinelib.gases.krypton"),
            null,
            "Kr"
    );
    public static final Identifier NITROUS_OXIDE_ID = new Identifier(Constant.MOD_ID, "nitrous_oxide");
    /**
     * Nitrous oxide.
     */
    public static final Gas NITROUS_OXIDE = Gas.create(
            new TranslatableText("ui.machinelib.gases.nitrous_oxide"),
            null,
            "N2O"
    );
    public static final Identifier CARBON_MONOXIDE_ID = new Identifier(Constant.MOD_ID, "carbon_monoxide");
    /**
     * Carbon monoxide.
     */
    public static final Gas CARBON_MONOXIDE = Gas.create(
            new TranslatableText("ui.machinelib.gases.carbon_monoxide"),
            null,
            "CO"
    );
    public static final Identifier XENON_ID = new Identifier(Constant.MOD_ID, "xenon");
    /**
     * Xenon.
     */
    public static final Gas XENON = Gas.create(
            new TranslatableText("ui.machinelib.gases.xenon"),
            null,
            "Xe"
    );
    public static final Identifier OZONE_ID = new Identifier(Constant.MOD_ID, "ozone");
    /**
     * Ozone.
     */
    public static final Gas OZONE = Gas.create(
            new TranslatableText("ui.machinelib.gases.ozone"),
            null,
            "O3"
    );
    public static final Identifier NITROUS_DIOXIDE_ID = new Identifier(Constant.MOD_ID, "nitrous_dioxide");
    /**
     * Nitrous dioxide.
     */
    public static final Gas NITROUS_DIOXIDE = Gas.create(
            new TranslatableText("ui.machinelib.gases.nitrous_dioxide"),
            null,
            "NO2"
    );
    public static final Identifier IODINE_ID = new Identifier(Constant.MOD_ID, "iodine");
    /**
     * Iodine.
     */
    public static final Gas IODINE = Gas.create(
            new TranslatableText("ui.machinelib.gases.iodine"),
            null,
            "I2"
    );

    public static void init() {}

    static {
        Registry.register(Gas.REGISTRY, HYDROGEN_ID, HYDROGEN);
        Registry.register(Gas.REGISTRY, NITROGEN_ID, NITROGEN);
        Registry.register(Gas.REGISTRY, OXYGEN_ID, OXYGEN);
        Registry.register(Gas.REGISTRY, CARBON_DIOXIDE_ID, CARBON_DIOXIDE);
        Registry.register(Gas.REGISTRY, WATER_VAPOR_ID, WATER_VAPOR);
        Registry.register(Gas.REGISTRY, METHANE_ID, METHANE);
        Registry.register(Gas.REGISTRY, HELIUM_ID, HELIUM);
        Registry.register(Gas.REGISTRY, ARGON_ID, ARGON);
        Registry.register(Gas.REGISTRY, NEON_ID, NEON);
        Registry.register(Gas.REGISTRY, KRYPTON_ID, KRYPTON);
        Registry.register(Gas.REGISTRY, NITROUS_OXIDE_ID, NITROUS_OXIDE);
        Registry.register(Gas.REGISTRY, CARBON_MONOXIDE_ID, CARBON_MONOXIDE);
        Registry.register(Gas.REGISTRY, XENON_ID, XENON);
        Registry.register(Gas.REGISTRY, OZONE_ID, OZONE);
        Registry.register(Gas.REGISTRY, NITROUS_DIOXIDE_ID, NITROUS_DIOXIDE);
        Registry.register(Gas.REGISTRY, IODINE_ID, IODINE);
    }
}

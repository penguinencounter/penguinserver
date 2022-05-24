package org.penguinencounter.penguinserver.fplib;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;

public class EntityTrackerMapping {
    public static final byte CAPE = 0x01;
    public static final byte JACKET = 0x02;
    public static final byte L_SLEEVE = 0x04;
    public static final byte R_SLEEVE = 0x08;
    public static final byte L_PANTS = 0x10;
    public static final byte R_PANTS = 0x20;
    public static final byte HAT = 0x40;

    /*
     * notes:
     * 17, BYTE, skin layers as bitfield
     *      Bit 0 (0x01): Cape enabled
     *      Bit 1 (0x02): Jacket enabled
     *      Bit 2 (0x04): Left Sleeve enabled
     *      Bit 3 (0x08): Right Sleeve enabled
     *      Bit 4 (0x10): Left Pants Leg enabled
     *      Bit 5 (0x20): Right Pants Leg enabled
     *      Bit 6 (0x40): Hat enabled
     */
    public static byte generateSkinBitfield(
            boolean cape,
            boolean jacket,
            boolean lSleeve,
            boolean rSleeve,
            boolean lPants,
            boolean rPants,
            boolean hat
    ) {
        byte data = 0;
        if (cape) data |= CAPE;
        if (jacket) data |= JACKET;
        if (lSleeve) data |= L_SLEEVE;
        if (rSleeve) data |= R_SLEEVE;
        if (lPants) data |= L_PANTS;
        if (rPants) data |= R_PANTS;
        if (hat) data |= HAT;
        return data;
    }
    public static DataTracker.Entry<Byte> buildSkin(
            boolean cape,
            boolean jacket,
            boolean lSleeve,
            boolean rSleeve,
            boolean lPants,
            boolean rPants,
            boolean hat
    ) {
        byte data = generateSkinBitfield(
                cape,
                jacket,
                lSleeve,
                rSleeve,
                lPants,
                rPants,
                hat
        );
        return new DataTracker.Entry<>(new TrackedData<>(17, TrackedDataHandlerRegistry.BYTE), data);
    }
    public static DataTracker.Entry<Byte> buildSkin(SkinLayers layers) {
        return buildSkin(
                layers.cape,
                layers.jacket,
                layers.lSleeve,
                layers.rSleeve,
                layers.lPants,
                layers.rPants,
                layers.hat
        );
    }
}

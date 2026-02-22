/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package io.github.ferrinember.simpleblockphysics.utils;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PosLevelKey {
    private final BlockPos x;
    private final Level y;

    public PosLevelKey(BlockPos x, Level y) {
        this.x = x;
        this.y = y;
    }

    public BlockPos getPos() {
        return this.x;
    }

    public Level getLevel() {
        return this.y;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PosLevelKey)) {
            return false;
        }
        PosLevelKey posLevelKey = (PosLevelKey)o;
        return this.x == posLevelKey.x && this.y == posLevelKey.y;
    }

    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}

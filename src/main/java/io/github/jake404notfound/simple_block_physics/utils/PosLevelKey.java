package io.github.jake404notfound.simple_block_physics.utils;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PosLevelKey)) {
            return false;
        }
        PosLevelKey posLevelKey = (PosLevelKey) o;
        return Objects.equals(this.x, posLevelKey.x) && Objects.equals(this.y, posLevelKey.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}

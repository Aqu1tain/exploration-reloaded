package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationCustomData;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {
    @Redirect(method = "findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean useSelectedBiome(Set<Holder<Biome>> entries, Object value, @Local Holder<Biome> biome) {
        if (entries.size() > 50) {
            return biome.is(ExplorationCustomData.biomeSearch);
        }
        return entries.contains(biome);
    }
}

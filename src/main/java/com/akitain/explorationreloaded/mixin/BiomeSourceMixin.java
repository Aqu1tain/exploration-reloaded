package com.akitain.explorationreloaded.mixin;

import com.akitain.explorationreloaded.ExplorationCustomData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {
    @Redirect(method = "locateBiome(Lnet/minecraft/util/math/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;Lnet/minecraft/world/WorldView;)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean useSelectedBiome(Set<RegistryEntry<Biome>> entries, Object value, @Local RegistryEntry<Biome> biome) {
        if (entries.size() > 50) {
            return biome.matchesKey(ExplorationCustomData.biomeSearch);
        }
        return entries.contains(biome);
    }
}

package net.findneareststructure;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2))
                .executes(FindNearestStructureCommand::findNearestStructure)));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context) {
        Pair<BlockPos, RegistryEntry<Structure>> pair = locateNearestStructure(context, context.getSource().getWorld(), 50);
        if (pair == null) {
            return 0;
        }
        return 1;
    }

    private static Pair<BlockPos, RegistryEntry<Structure>> locateNearestStructure(CommandContext<ServerCommandSource> context, World world, int radius) {
        double d = Double.MAX_VALUE;
        ChunkPos startChunkPos = context.getSource().getEntity().getChunkPos();
        ChunkPos currentChunkPos;
        BlockPos pos = null;
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        for (int i = 0; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    currentChunkPos = new ChunkPos(startChunkPos.x + j, startChunkPos.z + k);
                    if (chunkHasStructure(currentChunkPos, world)) {
                        double f = startChunkPos.getSquaredDistance(currentChunkPos);
                        if (!(f < d)) {
                            continue;
                        }
                        d = f;
                        pos = world.getChunk(currentChunkPos.x, currentChunkPos.z).getPos().getStartPos();
                    }
                }
            }
        }
        stopwatch.stop();
        if (pos != null) {
            int x = pos.getX();
            int z = pos.getZ();
            context.getSource().sendFeedback(() -> Text.translatable("Found structure at [%s %s]", x,z), false);
            context.getSource().sendFeedback(() -> Text.translatable("Took %s ms", stopwatch.elapsed()), false);
        } else {
            context.getSource().sendError(Text.of("No"));
        }
        return pair;
    }

    private static boolean chunkHasStructure(ChunkPos chunkpos, World world) {
        Chunk chunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
        Map<Structure, StructureStart> structure = chunk.getStructureStarts();
        for (Map.Entry<Structure, StructureStart> entry : structure.entrySet()) {
            StructureStart structureStart = entry.getValue();
            if (structureStart != null && structureStart.hasChildren()) {
                return true;
            }
        }
        return false;
    }
}

package net.findneareststructure;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
        if(chunkHasStructure(context)) {
            context.getSource().sendFeedback(() -> Text.of("Y"), false);
        }
        return 1;
    }

    private static Pair<BlockPos, RegistryEntry<Structure>> locateNearestStructure(ServerWorld world, BlockPos center, int radius) {
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;

        return pair;
    }

    private static boolean chunkHasStructure(CommandContext<ServerCommandSource> context) {
        if (context == null) {
            return false;
        }
        ChunkPos chunkpos = context.getSource().getEntity().getChunkPos();
        Chunk chunk = context.getSource().getWorld().getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
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

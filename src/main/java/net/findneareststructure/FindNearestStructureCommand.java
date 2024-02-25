package net.findneareststructure;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> findNearestStructure(context, 100))
                .then(argument("radius", IntegerArgumentType.integer(0))
                        .executes(context -> findNearestStructure(context, IntegerArgumentType.getInteger(context, "radius"))))));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context, int radius) {
        Pair<Identifier, BlockPos> pair = locateNearestStructure(context, context.getSource().getWorld(), radius);
        if (pair == null) {
            context.getSource().sendError(Text.of("Could not find a structure in the given radius"));
            return 0;
        }
        BlockPos startPos = Objects.requireNonNull(context.getSource().getEntity()).getBlockPos();
        BlockPos pos = pair.getRight();
        MutableText coordinates = Texts.bracketed(Text.translatable("%s, ~, %s", pos.getX(), pos.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + startPos.getY() + " " + pos.getZ())));
        context.getSource().sendFeedback(() -> Text.translatable("The nearest %s is at %s (%s blocks away)", pair.getLeft(), coordinates, distanceBetween(startPos, new BlockPos(pos.getX(), startPos.getY(), pos.getZ()))), false);
        return 1;
    }

    private static Pair<Identifier, BlockPos> locateNearestStructure(CommandContext<ServerCommandSource> context, World world, int radius) {
        BlockPos startPos = context.getSource().getEntity().getBlockPos();
        int startX = context.getSource().getEntity().getChunkPos().x;
        int startZ = context.getSource().getEntity().getChunkPos().z;
        Registry<Structure> registry = context.getSource().getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
        Set<ChunkPos> visited = new HashSet<>();
        int ring = 0;
        int rindcount = 0;

        List<Pair<Structure, BlockPos>> foundStructures = new ArrayList<>();
        while (ring <= radius) {
            for (int x = -ring; x <= ring; x++) {
                for (int z = -ring; z <= ring; z++) {
                    if (Math.abs(x) == ring || Math.abs(z) == ring) {
                        ChunkPos checkChunkPos = new ChunkPos(startX + x, startZ + z);
                        if (!visited.contains(checkChunkPos)) {
                            visited.add(checkChunkPos);
                            Pair<Structure, Boolean> structurePair = chunkHasStructure(checkChunkPos, world);
                            if (structurePair.getRight()) {
                                foundStructures.add(new Pair<>(structurePair.getLeft(), world.getChunk(checkChunkPos.x, checkChunkPos.z).getPos().getStartPos()));
                            }
                        }
                    }
                }
            }
            if (!foundStructures.isEmpty()) {
                rindcount++;
            }
            if (rindcount == 2) {
                break;
            }
            ring++;
        }

        Pair<Structure, BlockPos> closestStructure = null;
        double closestDistance = Double.MAX_VALUE;

        for (Pair<Structure, BlockPos> structure : foundStructures) {
            double distance = distanceBetween(structure.getRight(), startPos);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestStructure = structure;
            }
        }

        if (closestStructure != null) {
            return new Pair<>(registry.getId(closestStructure.getLeft()), closestStructure.getRight());
        }
        return null;
    }

    private static Pair<Structure, Boolean> chunkHasStructure(ChunkPos chunkpos, World world) {
        Map<Structure, StructureStart> structure = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS).getStructureStarts();
        for (Map.Entry<Structure, StructureStart> entry : structure.entrySet()) {
            StructureStart structureStart = entry.getValue();
            if (structureStart != null && structureStart.hasChildren()) {
                return new Pair<>(entry.getKey(), true);
            }
        }
        return new Pair<>(null, false);
    }

    private static int distanceBetween(BlockPos pos1, BlockPos pos2) {
        int x = Math.abs(pos1.getX() - pos2.getX());
        int y = Math.abs(pos1.getY() - pos2.getY());
        int z = Math.abs(pos1.getZ() - pos2.getZ());
        return x + y + z;
    }
}
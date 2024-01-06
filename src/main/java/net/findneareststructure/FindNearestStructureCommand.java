package net.findneareststructure;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FindNearestStructureCommand {
    private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType((id) -> {
        return Text.translatable("commands.locate.structure.invalid", id);
    });
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("findneareststructure").requires(source -> source.hasPermissionLevel(2)).then(argument("structure", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.STRUCTURE))
                .executes(context -> findNearestStructure(context, context.getSource().getEntity().getBlockPos(), RegistryPredicateArgumentType.getPredicate(context, "structure", RegistryKeys.STRUCTURE, STRUCTURE_INVALID_EXCEPTION))))));
    }

    private static int findNearestStructure(CommandContext<ServerCommandSource> context, BlockPos pos, RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate) throws CommandSyntaxException {
        Registry<Structure> registry = context.getSource().getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
        RegistryEntryList<Structure> registryEntryList = getStructureListForPredicate(predicate, registry).orElseThrow(() -> {
            return STRUCTURE_INVALID_EXCEPTION.create(predicate.asString());
        });
        BlockPos blockPos = BlockPos.ofFloored(context.getSource().getPosition());
        ServerWorld serverWorld = context.getSource().getWorld();
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        Pair<BlockPos, RegistryEntry<Structure>> pair = serverWorld.getChunkManager().getChunkGenerator().locateStructure(serverWorld, registryEntryList, blockPos, 100, false);
        stopwatch.stop();
        return LocateCommand.sendCoordinates(context.getSource(), predicate, blockPos, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
    }

    private static Optional<? extends RegistryEntryList.ListBacked<Structure>> getStructureListForPredicate(RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate, Registry<Structure> structureRegistry) {
        return predicate.getKey().map(key -> structureRegistry.getEntry(key).map(entry -> RegistryEntryList.of(entry)), structureRegistry::getEntryList);
    }
}

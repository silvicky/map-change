package io.silvicky.map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapIndex;

import static net.minecraft.core.component.DataComponents.MAP_ID;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MapChange {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                literal("mapchange")
                        .then(argument("ID", IntegerArgumentType.integer())
                                .executes(context -> mapChange(context.getSource(),IntegerArgumentType.getInteger(context,"ID")))));
    }
    public static int mapChange(CommandSourceStack source, int id)
    {
        MapIndex idCountsState= source.getServer().getDataStorage().computeIfAbsent(MapIndex.TYPE);
        int maxId=idCountsState.lastMapId;
        if(id<0||id>maxId)
        {
            source.sendSuccess(()-> Component.literal("ERR:Invalid ID!"),false);
            return Command.SINGLE_SUCCESS;
        }
        Inventory inventory=source.getPlayer().getInventory();
        if(inventory==null)
        {
            source.sendSuccess(()-> Component.literal("ERR:Empty inventory!"),false);
            return Command.SINGLE_SUCCESS;
        }
        ItemStack itemStack=inventory.getItem(inventory.getSelectedSlot());
        if(itemStack!=null)
        {
            if(itemStack.getCount()>1)
            {
                source.sendSuccess(()-> Component.literal("ERR:Please hold only one!"),false);
                return Command.SINGLE_SUCCESS;
            }
            String curItem=itemStack.getItem().getDescriptionId();
            if(curItem.equals("item.minecraft.filled_map"))
            {
                itemStack.applyComponentsAndValidate(DataComponentPatch.builder().remove(MAP_ID).set(TypedDataComponent.createUnchecked(MAP_ID,new MapId(id))).build());
                source.getPlayer().getInventory().setItem(inventory.getSelectedSlot(),itemStack);
            }
            else if(curItem.equals("item.minecraft.map"))
            {

                ItemStack itemStack1=new ItemStack(Items.FILLED_MAP);
                itemStack1.applyComponentsAndValidate(DataComponentPatch.builder().set(TypedDataComponent.createUnchecked(MAP_ID,new MapId(id))).build());
                source.getPlayer().getInventory().setItem(inventory.getSelectedSlot(),itemStack1);
            }
            else
            {
                source.sendSuccess(()-> Component.literal("ERR:Only maps are allowed!"),false);
                return Command.SINGLE_SUCCESS;
            }
        }
        else
        {
            source.sendSuccess(()-> Component.literal("ERR:Only maps are allowed!"),false);
            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }
}

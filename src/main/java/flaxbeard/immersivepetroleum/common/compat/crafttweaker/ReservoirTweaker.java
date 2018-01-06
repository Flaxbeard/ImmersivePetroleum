package flaxbeard.immersivepetroleum.common.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivepetroleum.Reservoir")
@ZenRegister
public class ReservoirTweaker {
    @ZenMethod
    public static void registerReservoir(String name, ILiquidStack fluid, int minSize, int maxSize, int replenishRate, int weight){

        if (name.isEmpty()){
            CraftTweakerAPI.logError("Name can not be Empty String");
        } else if (minSize <= 0){
            CraftTweakerAPI.logError("minSize has to be atleast 1mb!");
        } else if (maxSize < minSize){
            CraftTweakerAPI.logError("maxSize can not be smaller than minSize!");
        } else if (weight <= 1){
            CraftTweakerAPI.logError("Weight has to be greater than or equal to 1!");
        }

        String rFluid = fluid.getName();

        PumpjackHandler.addReservoir(name, rFluid, minSize, maxSize, replenishRate, weight);
    }
}

package flaxbeard.immersivepetroleum.common.crafting;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Serializers{
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ImmersivePetroleum.MODID);
	
	public static final RegistryObject<SpecialRecipeSerializer<SchematicCraftingHandler>> SERIALIZER = RECIPE_SERIALIZERS.register("schematic_crafting", () -> new SpecialRecipeSerializer<>(SchematicCraftingHandler::new));
}

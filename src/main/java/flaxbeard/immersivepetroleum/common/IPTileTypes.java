package flaxbeard.immersivepetroleum.common;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.CokerUnitTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.GasGeneratorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IPTileTypes{
	public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ImmersivePetroleum.MODID);
	
	// Multiblocks
	public static final RegistryObject<TileEntityType<PumpjackTileEntity>> PUMP = register("pumpjack", PumpjackTileEntity::new, IPContent.Multiblock.pumpjack);
	public static final RegistryObject<TileEntityType<DistillationTowerTileEntity>> TOWER = register("distillationtower", DistillationTowerTileEntity::new, IPContent.Multiblock.distillationtower);
	public static final RegistryObject<TileEntityType<CokerUnitTileEntity>> COKER = register("cokerunit", CokerUnitTileEntity::new, IPContent.Multiblock.cokerunit);
	public static final RegistryObject<TileEntityType<HydrotreaterTileEntity>> TREATER = register("hydrotreater", HydrotreaterTileEntity::new, IPContent.Multiblock.hydrotreater);
	
	// Normal Blocks
	public static final RegistryObject<TileEntityType<GasGeneratorTileEntity>> GENERATOR = register("gasgenerator", GasGeneratorTileEntity::new, IPContent.Blocks.gas_generator);
	public static final RegistryObject<TileEntityType<AutoLubricatorTileEntity>> AUTOLUBE = register("autolubricator", AutoLubricatorTileEntity::new, IPContent.Blocks.auto_lubricator);
	public static final RegistryObject<TileEntityType<FlarestackTileEntity>> FLARE = register("flarestack", FlarestackTileEntity::new, IPContent.Blocks.flarestack);
	
	private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factory, Block... valid){
		return REGISTER.register(name, () -> new TileEntityType<>(factory, ImmutableSet.copyOf(valid), null));
	}
}

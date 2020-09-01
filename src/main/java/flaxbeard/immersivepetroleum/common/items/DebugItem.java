package flaxbeard.immersivepetroleum.common.items;

import java.util.Locale;
import java.util.Set;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DebugItem extends IPItemBase{
	public DebugItem(){
		super("debug");
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		boolean skip=false;
		if(!worldIn.isRemote && !skip){
			if(playerIn.isSneaking()){ // TODO DEBUG: Remove later.
				
				int contentSize = PumpjackHandler.oilCache.size();
				
				PumpjackHandler.oilCache.clear();
				PumpjackHandler.recalculateChances(true);
				
				IPSaveData.setDirty();
				
				playerIn.sendStatusMessage(new StringTextComponent("Cleared Oil Cache. (Removed " + contentSize + ")"), true);
			}else{
				
				BlockPos pos=playerIn.getPosition();
				
				DimensionChunkCoords coords=new DimensionChunkCoords(worldIn.dimension.getType(), (pos.getX() >> 4), (pos.getZ() >> 4));
				
				int last=PumpjackHandler.oilCache.size();
				OilWorldInfo info=PumpjackHandler.getOilWorldInfo(worldIn, coords.x, coords.z);
				boolean isNew=PumpjackHandler.oilCache.size()!=last;
				
				if(info != null){
					int cap=info.capacity;
					int cur=info.current;
					ReservoirType type=info.getType();
					
					if(type!=null){
						String out = String.format(Locale.ENGLISH,
								"%s %s: %.3f/%.3f Buckets of %s%s%s",
								coords.x,
								coords.z,
								cur/1000D,
								cap/1000D,
								type.name,
								(info.overrideType!=null?" [OVERRIDDEN]":""),
								(isNew?" [NEW]":"")
						);
						
						playerIn.sendStatusMessage(new StringTextComponent(out), true);
						return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
					}
				}
				playerIn.sendStatusMessage(new StringTextComponent(String.format(Locale.ENGLISH, "%s %s: Nothing.", coords.x, coords.z)), true);
			}
			
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
		}
		
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		PlayerEntity player=context.getPlayer();
		if(player==null) return ActionResultType.PASS;
		
		if(player.isSneaking()){
			TileEntity te=context.getWorld().getTileEntity(context.getPos());
			
			if(te instanceof AutoLubricatorTileEntity){
				AutoLubricatorTileEntity lube=(AutoLubricatorTileEntity)te;
				
				ITextComponent out=new StringTextComponent(context.getWorld().isRemote?"CLIENT: ":"SERVER: ");
				out.appendText(lube.facing+", ");
				out.appendText((lube.isActive?"Active":"Inactive")+", ");
				out.appendText((lube.isSlave?"Slave":"Master")+", ");
				out.appendText((lube.predictablyDraining?"Predictably Draining, ":""));
				if(!lube.tank.isEmpty()){
					out.appendSibling(lube.tank.getFluid().getDisplayName()).appendText(" "+lube.tank.getFluidAmount()+"/"+lube.tank.getCapacity()+"mB");
				}else{
					out.appendText("Empty");
				}
				
				player.sendMessage(out);
				
				return ActionResultType.PASS;
			}
			
			if(te instanceof GasGeneratorTileEntity){
				GasGeneratorTileEntity gas=(GasGeneratorTileEntity)te;
				
				ITextComponent out=new StringTextComponent(context.getWorld().isRemote?"CLIENT: ":"SERVER: ");
				out.appendText(gas.getFacing()+", ");
				out.appendText(gas.getEnergyStored(null)+", ");
				out.appendText(gas.getMaxEnergyStored(null)+", ");
				
				player.sendMessage(out);
				
				return ActionResultType.PASS;
			}
			
			if(te instanceof PoweredMultiblockTileEntity){ // Generic
				PoweredMultiblockTileEntity<?,?> poweredMultiblock=(PoweredMultiblockTileEntity<?,?>)te;
				
				Vec3i loc=poweredMultiblock.posInMultiblock;
				Set<BlockPos> energyInputs=poweredMultiblock.getEnergyPos();
				Set<BlockPos> redstoneInputs=poweredMultiblock.getRedstonePos();
				
				ITextComponent out=new StringTextComponent("["+loc.getX()+" "+loc.getY()+" "+loc.getZ()+"]: ");
				
				for(BlockPos pos:energyInputs){
					if(pos.equals(loc)){
						out.appendText("Energy Port.");
					}
				}
				
				for(BlockPos pos:redstoneInputs){
					if(pos.equals(loc)){
						out.appendText("Redstone Port.");
					}
				}
				
				if(poweredMultiblock.offsetToMaster.equals(BlockPos.ZERO)){
					out.appendText("Master.");
				}
				
				out.appendText(" (Facing: "+poweredMultiblock.getFacing()+", Block-Face: "+context.getFace()+")");
				
				player.sendStatusMessage(out, true);
				return ActionResultType.PASS;
			}
			
		}
		
		return ActionResultType.PASS;
	}
	
	public void onSpeedboatClick(SpeedboatEntity speedboatEntity, PlayerEntity player){
		ITextComponent textOut=new StringTextComponent("-- Speedboat --\n");
		
		FluidStack fluid=speedboatEntity.getContainedFluid();
		if(fluid==FluidStack.EMPTY){
			textOut.appendText("Tank: Empty");
		}else{
			textOut.appendText("Tank: "+fluid.getAmount()+"/"+speedboatEntity.getMaxFuel()+"mB of ").appendSibling(fluid.getDisplayName());
		}
		
		ITextComponent upgradesText=new StringTextComponent("\n");
		NonNullList<ItemStack> upgrades=speedboatEntity.getUpgrades();
		int i=0;
		for(ItemStack upgrade:upgrades){
			if(upgrade==null || upgrade==ItemStack.EMPTY){
				upgradesText.appendText("Upgrade "+(++i)+": Empty\n");
			}else{
				upgradesText.appendText("Upgrade "+(i++)+": ").appendSibling(upgrade.getDisplayName()).appendText("\n");
			}
		}
		textOut.appendSibling(upgradesText);
		
		if(!speedboatEntity.world.isRemote)
			player.sendMessage(textOut);
	}

	@SuppressWarnings("unused")
	private void analyze(ItemUseContext context, BlockState state, PumpjackTileEntity te){
		boolean isSlave=state.get(IEProperties.MULTIBLOCKSLAVE);
		boolean isMirrored=state.get(IEProperties.MIRRORED);
		Direction facing=state.get(IEProperties.FACING_HORIZONTAL);
		FluxStorageAdvanced fluxStorage=te.energyStorage;
		
		ITextComponent out=new StringTextComponent("Pump Info\n").applyTextStyle(TextFormatting.YELLOW);
		
		out.appendSibling(new StringTextComponent((isSlave?"\nSlave":"\nMaster")+"\n").applyTextStyle(isSlave?TextFormatting.GOLD:TextFormatting.AQUA));
		out.appendSibling(new StringTextComponent((isMirrored?"Mirrored":"Normal")+"\n").applyTextStyle(isMirrored?TextFormatting.GOLD:TextFormatting.AQUA));
		out.appendText("Facing: "+facing.getName()+"\n");
		out.appendText("Storage: "+fluxStorage.getEnergyStored()+"/"+fluxStorage.getMaxEnergyStored()+"\n");
		
		FluidTank tank=te.fakeTank;
		String str="[";
		for(int j=0;j<tank.getTanks();j++){
			FluidStack fluidStack=tank.getFluidInTank(j);
			str+=fluidStack.getDisplayName().getFormattedText()+" "+fluidStack.getAmount()+"mB, ";
		}
		out.appendText(str+"]\n");
		
		context.getPlayer().sendMessage(out);
		
	}
}

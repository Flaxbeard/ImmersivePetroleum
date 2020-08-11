package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity.DistillationTowerParentTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public class DebugItem extends IPItemBase{
	public DebugItem(){
		super("debug");
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		if(!context.getWorld().isRemote){
			TileEntity te=context.getWorld().getTileEntity(context.getPos());
			if((te instanceof DistillationTowerTileEntity) || (te instanceof DistillationTowerParentTileEntity)){
				BlockState state=context.getWorld().getBlockState(context.getPos());
				analyze(context, state, (DistillationTowerTileEntity)te);
			}
		}
		
		
		return ActionResultType.SUCCESS;
	}

	private void analyze(ItemUseContext context, BlockState state, DistillationTowerTileEntity te){
		boolean isSlave=state.get(IEProperties.MULTIBLOCKSLAVE);
		boolean isMirrored=state.get(IEProperties.MIRRORED);
		Direction facing=state.get(IEProperties.FACING_HORIZONTAL);
		FluxStorageAdvanced fluxStorage=te.energyStorage;
		
		ITextComponent out=te.getDisplayName();
		
		out.appendSibling(new StringTextComponent("\n"+(isSlave?"\nSlave":"\nMaster")+"\n").applyTextStyle(isSlave?TextFormatting.GOLD:TextFormatting.AQUA));
		out.appendSibling(new StringTextComponent((isMirrored?"Mirrored":"Normal")+"\n").applyTextStyle(isMirrored?TextFormatting.GOLD:TextFormatting.AQUA));
		out.appendText("Facing: "+facing.getName()+"\n");
		out.appendText("Storage: "+fluxStorage.getEnergyStored()+"/"+fluxStorage.getMaxEnergyStored()+"\n");
		
		for(int i=0;i<te.tanks.length;i++){
			MultiFluidTank tank=te.tanks[i];
			String str=i+"[";
			for(int j=0;j<tank.getTanks();j++){
				FluidStack fluidStack=tank.getFluidInTank(j);
				str+=fluidStack.getDisplayName().getFormattedText()+" "+fluidStack.getAmount()+"mB, ";
			}
			out.appendText(str+"]\n");
		}
		
		context.getPlayer().sendMessage(out);
		
	}
}

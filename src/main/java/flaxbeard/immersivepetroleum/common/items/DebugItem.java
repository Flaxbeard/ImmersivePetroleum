package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity.DistillationTowerParentTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity.PumpjackParentTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DebugItem extends IPItemBase{
	public DebugItem(){
		super("debug");
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		if(!context.getWorld().isRemote){
			TileEntity te=context.getWorld().getTileEntity(context.getPos());
			if(te instanceof DistillationTowerTileEntity || te instanceof DistillationTowerParentTileEntity){
				DistillationTowerTileEntity master=((DistillationTowerTileEntity)te);
				
				Vec3i loc=master.offsetToMaster;
				
				String strOut="At: "+loc.getX()+" "+loc.getY()+" "+loc.getZ()+" "+context.getFace();
				
				if(DistillationTowerTileEntity.Energy_IN.contains(loc)){
					strOut="Matching DistillationTowerTileEntity.Energy_IN";
				}
				
				context.getPlayer().sendStatusMessage(new StringTextComponent(strOut), true);
				
				return ActionResultType.PASS;
			}
			
			if(te instanceof PumpjackTileEntity || te instanceof PumpjackParentTileEntity){
				PumpjackTileEntity master=(PumpjackTileEntity)te;
				
				Vec3i loc=master.offsetToMaster;
				
				String strOut="At: "+loc.getX()+" "+loc.getY()+" "+loc.getZ()+" "+context.getFace();
				
				context.getPlayer().sendStatusMessage(new StringTextComponent(strOut), true);
				
				return ActionResultType.PASS;
			}
		}
		
		return ActionResultType.PASS;
	}

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

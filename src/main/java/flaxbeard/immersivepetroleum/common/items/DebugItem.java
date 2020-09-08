package flaxbeard.immersivepetroleum.common.items;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.OilWorldInfo;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler.ReservoirType;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.blocks.metal.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.DistillationTowerTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.metal.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.entity.SpeedboatEntity;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageDebugSync;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

public class DebugItem extends IPItemBase{
	public DebugItem(){
		super("debug");
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack){
		return new StringTextComponent("IP Debugging Tool").applyTextStyle(TextFormatting.LIGHT_PURPLE);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new StringTextComponent("[Shift + Scroll-UP/DOWN] Change mode.").applyTextStyle(TextFormatting.GRAY));
		Modes mode=getMode(stack);
		if(mode==Modes.DISABLED){
			tooltip.add(new StringTextComponent("  Disabled.").applyTextStyle(TextFormatting.DARK_GRAY));
		}else{
			tooltip.add(new StringTextComponent("  "+mode.display).applyTextStyle(TextFormatting.DARK_GRAY));
		}
		
		tooltip.add(new StringTextComponent("You're not supposed to have this.").applyTextStyle(TextFormatting.DARK_RED));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items){}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		if(!worldIn.isRemote){
			Modes mode=DebugItem.getMode(playerIn.getHeldItem(handIn));
			switch(mode){
				case CLEAR_RESERVOIR_CACHE:{
					int contentSize = PumpjackHandler.reservoirsCache.size();
					
					PumpjackHandler.reservoirsCache.clear();
					PumpjackHandler.recalculateChances(true);
					
					IPSaveData.setDirty();
					
					playerIn.sendStatusMessage(new StringTextComponent("Cleared Oil Cache. (Removed " + contentSize + ")"), true);
					
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
				}
				case RESERVOIR:{
					BlockPos pos=playerIn.getPosition();
					DimensionChunkCoords coords=new DimensionChunkCoords(worldIn.dimension.getType(), (pos.getX() >> 4), (pos.getZ() >> 4));
					
					int last=PumpjackHandler.reservoirsCache.size();
					OilWorldInfo info=PumpjackHandler.getOrCreateOilWorldInfo(worldIn, coords, false);
					boolean isNew=PumpjackHandler.reservoirsCache.size()!=last;
					
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
					
					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
				}
				default:break;
			}
			return new ActionResult<ItemStack>(ActionResultType.PASS, playerIn.getHeldItem(handIn));
		}
		
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		PlayerEntity player=context.getPlayer();
		if(player==null){
			return ActionResultType.PASS;
		}
		
		ItemStack held=player.getHeldItem(context.getHand());
		Modes mode=DebugItem.getMode(held);
		
		TileEntity te=context.getWorld().getTileEntity(context.getPos());
		switch(mode){
			case INFO_TE_DISTILLATION_TOWER:{
				if(te instanceof DistillationTowerTileEntity && !context.getWorld().isRemote){
					DistillationTowerTileEntity tower=(DistillationTowerTileEntity)te;
					if(!tower.offsetToMaster.equals(BlockPos.ZERO)){
						tower=tower.master();
					}
					
					ITextComponent tankInText=new StringTextComponent("\nInputFluids: ");
					{
						MultiFluidTank tank=tower.tanks[DistillationTowerTileEntity.TANK_OUTPUT];
						for(int i=0;i<tank.fluids.size();i++){
							FluidStack fstack=tank.fluids.get(i);
							tankInText.appendText(" ").appendSibling(fstack.getDisplayName()).appendText(" "+fstack.getAmount()+"mB,");
						}
					}
					
					ITextComponent tankOutText=new StringTextComponent("\nOutputFluids: ");
					{
						MultiFluidTank tank=tower.tanks[DistillationTowerTileEntity.TANK_INPUT];
						for(int i=0;i<tank.fluids.size();i++){
							FluidStack fstack=tank.fluids.get(i);
							tankOutText.appendText(" ").appendSibling(fstack.getDisplayName()).appendText(" "+fstack.getAmount()+"mB,");
						}
					}
					
					player.sendMessage(new StringTextComponent("DistillationTower:\n").appendSibling(tankInText).appendSibling(tankOutText));
				}
				return ActionResultType.PASS;
			}
			case INFO_TE_DISTILLATION_TOWER_STEP:{
				if(te instanceof DistillationTowerTileEntity && !context.getWorld().isRemote){
					DistillationTowerTileEntity tower=(DistillationTowerTileEntity)te;
					if(!tower.offsetToMaster.equals(BlockPos.ZERO)){
						tower=tower.master();
					}
					
					if(!tower.enableStepping){
						tower.enableStepping=true;
						player.sendMessage(new StringTextComponent("Enabled Stepping."));
					}else{
						tower.step++;
						player.sendMessage(new StringTextComponent("Ticked."));
					}
				}
				return ActionResultType.PASS;
			}
			case INFO_TE_MULTIBLOCK:{
				if(te instanceof PoweredMultiblockTileEntity && !context.getWorld().isRemote){ // Generic
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
					return ActionResultType.SUCCESS;
				}
				break;
			}
			case INFO_TE_AUTOLUBE:{
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
					
					return ActionResultType.SUCCESS;
				}
				break;
			}
			case INFO_TE_GASGEN:{
				if(te instanceof GasGeneratorTileEntity){
					GasGeneratorTileEntity gas=(GasGeneratorTileEntity)te;
					
					ITextComponent out=new StringTextComponent(context.getWorld().isRemote?"CLIENT: ":"SERVER: ");
					out.appendText(gas.getFacing()+", ");
					out.appendText(gas.getEnergyStored(null)+", ");
					out.appendText(gas.getMaxEnergyStored(null)+", ");
					
					player.sendMessage(out);
					
					return ActionResultType.SUCCESS;
				}
				break;
			}
			default:break;
		}
		
		return ActionResultType.PASS;
	}
	
	public void onSpeedboatClick(SpeedboatEntity speedboatEntity, PlayerEntity player, ItemStack debugStack){
		if(speedboatEntity.world.isRemote || DebugItem.getMode(debugStack)!=Modes.INFO_SPEEDBOAT){
			return;
		}
		
		ITextComponent textOut = new StringTextComponent("-- Speedboat --\n");
		
		FluidStack fluid = speedboatEntity.getContainedFluid();
		if(fluid == FluidStack.EMPTY){
			textOut.appendText("Tank: Empty");
		}else{
			textOut.appendText("Tank: " + fluid.getAmount() + "/" + speedboatEntity.getMaxFuel() + "mB of ").appendSibling(fluid.getDisplayName());
		}
		
		ITextComponent upgradesText = new StringTextComponent("\n");
		NonNullList<ItemStack> upgrades = speedboatEntity.getUpgrades();
		int i = 0;
		for(ItemStack upgrade:upgrades){
			if(upgrade == null || upgrade == ItemStack.EMPTY){
				upgradesText.appendText("Upgrade " + (++i) + ": Empty\n");
			}else{
				upgradesText.appendText("Upgrade " + (i++) + ": ").appendSibling(upgrade.getDisplayName()).appendText("\n");
			}
		}
		textOut.appendSibling(upgradesText);
		
		player.sendMessage(textOut);
	}

	@SuppressWarnings("unused")
	private void analyze(ItemUseContext context, BlockState state, PumpjackTileEntity te){}
	
	public static void setModeServer(ItemStack stack, Modes mode){
		CompoundNBT nbt=getSettings(stack);
		nbt.putInt("mode", mode.ordinal());
	}
	
	public static void setModeClient(ItemStack stack, Modes mode){
		CompoundNBT nbt=getSettings(stack);
		nbt.putInt("mode", mode.ordinal());
		IPPacketHandler.sendToServer(new MessageDebugSync(nbt));
	}
	
	public static Modes getMode(ItemStack stack){
		CompoundNBT nbt=getSettings(stack);
		if(nbt.contains("mode")){
			int mode=nbt.getInt("mode");
			
			if(mode<0 || mode>=Modes.values().length)
				mode=0;
			
			return Modes.values()[mode];
		}
		return Modes.DISABLED;
	}
	
	public static CompoundNBT getSettings(ItemStack stack){
		return stack.getOrCreateChildTag("settings");
	}
	
	
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientInputHandler{
		static boolean shiftHeld = false;
		
		@SubscribeEvent
		public static void handleScroll(InputEvent.MouseScrollEvent event){
			double delta = event.getScrollDelta();
			
			if(shiftHeld && delta != 0.0){
				PlayerEntity player = ClientUtils.mc().player;
				ItemStack mainItem = player.getHeldItemMainhand();
				ItemStack secondItem = player.getHeldItemOffhand();
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.debugItem;
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.debugItem;
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					Modes mode = DebugItem.getMode(target);
					int id = mode.ordinal() + (int) delta;
					if(id < 0){
						id = Modes.values().length - 1;
					}
					if(id >= Modes.values().length){
						id = 0;
					}
					mode = Modes.values()[id];
					
					DebugItem.setModeClient(target, mode);
					player.sendStatusMessage(new StringTextComponent(mode.display), true);
					event.setCanceled(true);
				}
			}
		}
		
		@SubscribeEvent
		public static void handleKey(InputEvent.KeyInputEvent event){
			if(event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT || event.getKey() == GLFW.GLFW_KEY_LEFT_SHIFT){
				switch(event.getAction()){
					case GLFW.GLFW_PRESS:{
						shiftHeld = true;
						return;
					}
					case GLFW.GLFW_RELEASE:{
						shiftHeld = false;
						return;
					}
				}
			}
		}
	}
	
	protected static enum Modes{
		DISABLED("Disabled"),
		INFO_SPEEDBOAT("Info: Speedboat."),
		INFO_TE_AUTOLUBE("Info: AutoLubricator."),
		INFO_TE_GASGEN("Info: Portable Generator."),
		INFO_TE_MULTIBLOCK("Info: Powered Multiblock."),
		INFO_TE_DISTILLATION_TOWER("Info: Distillation Tower."),
		INFO_TE_DISTILLATION_TOWER_STEP("Info: Manual DT Ticking."),
		RESERVOIR("Create/Get Reservoir"),
		CLEAR_RESERVOIR_CACHE("Clear Reservoir Cache"),
		;
		
		public final String display;
		private Modes(String display){
			this.display=display;
		}
	}
}

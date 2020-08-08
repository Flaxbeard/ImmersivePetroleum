package flaxbeard.immersivepetroleum.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class CommonProxy implements IGuiHandler{
	@SuppressWarnings("unused")
	private static final Logger log=LogManager.getLogger(ImmersivePetroleum.MODID+"/CommonProxy");
	
	/** Fired during instantiation of {@link ImmersivePetroleum} */
	public void construct(){
	}
	
	/** Fired at {@link FMLCommonSetupEvent} */
	public void setup(){
	}
	
	/** Fired at {@link FMLLoadCompleteEvent} */
	public void completed(){
	}
	
	public void preInit(){
	}
	
	public void preInitEnd(){
	}
	
	public void init(){
	}
	
	public void postInit(){
	}
	
	/*
	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile){
		player.openGui(ImmersivePetroleum.INSTANCE, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}
	*/
	
	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z){
		/*
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te instanceof IGuiTile){
			Object gui = null;
			if(ID == 0 && te instanceof DistillationTowerTileEntity){
				gui = new ContainerDistillationTower(player.inventory, (DistillationTowerTileEntity) te);
			}
			
			if(gui != null) ((IGuiTile) te).onGuiOpened(player, false);
			return gui;
		}
		*/
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z){
		/*
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te instanceof IGuiTile){
			Object gui = null;
			if(ID == 0 && te instanceof DistillationTowerTileEntity){
				gui = new GuiDistillationTower(player.inventory, (DistillationTowerTileEntity) te);
			}
			
			return gui;
		}
		*/
		return null;
	}
	
	
	public void renderTile(TileEntity te){}
	
	public void handleEntitySound(SoundEvent soundEvent, Entity e, boolean active, float volume, float pitch){}
	
	public void drawUpperHalfSlab(ItemStack stack){}
	
	public World getClientWorld(){
		return null;
	}
	
	public PlayerEntity getClientPlayer(){
		return null;
	}
	
	protected static ResourceLocation modLoc(String str){
		return new ResourceLocation(ImmersivePetroleum.MODID, str);
	}
}

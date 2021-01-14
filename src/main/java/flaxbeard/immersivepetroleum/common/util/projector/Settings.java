package flaxbeard.immersivepetroleum.common.util.projector;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import flaxbeard.immersivepetroleum.common.network.MessageProjectorSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class Settings{
	public static final String KEY_SELF = "settings";
	public static final String KEY_BLOCKS = "blocks";
	public static final String KEY_MODE = "mode";
	public static final String KEY_MULTIBLOCK = "multiblock";
	public static final String KEY_MIRROR = "mirror";
	public static final String KEY_PLACED = "placed";
	public static final String KEY_ROTATION = "rotation";
	public static final String KEY_POSITION = "pos";
	
	private Mode mode;
	private Rotation rotation;
	private BlockPos pos = null;
	private IMultiblock multiblock = null;
	private boolean mirror;
	private boolean isPlaced;
	
	public Settings(){
		this(new CompoundNBT());
	}
	
	public Settings(@Nullable final ItemStack stack){
		this(((Supplier<CompoundNBT>) () -> {
			CompoundNBT nbt = null;
			if(stack != null && (nbt = stack.getChildTag(KEY_SELF)) == null){
				// Fail-Safe, checks if what it got is null and if that's
				// the case just gives it an empty compound
				nbt = new CompoundNBT();
			}
			return nbt;
		}).get());
	}
	
	public Settings(CompoundNBT settingsNbt){
		if(settingsNbt == null || settingsNbt.isEmpty()){
			this.mode = Mode.MULTIBLOCK_SELECTION;
			this.rotation = Rotation.NONE;
			this.mirror = false;
			this.isPlaced = false;
		}else{
			this.mode = Mode.values()[MathHelper.clamp(settingsNbt.getInt(KEY_MODE), 0, Mode.values().length - 1)];
			this.rotation = Rotation.values()[settingsNbt.contains(KEY_ROTATION) ? settingsNbt.getInt(KEY_ROTATION) : 0];
			this.mirror = settingsNbt.getBoolean(KEY_MIRROR);
			this.isPlaced = settingsNbt.getBoolean(KEY_PLACED);
			
			if(settingsNbt.contains(KEY_MULTIBLOCK, NBT.TAG_STRING)){
				String str = settingsNbt.getString("multiblock");
				this.multiblock = MultiblockHandler.getByUniqueName(new ResourceLocation(str));
			}
			
			if(settingsNbt.contains(KEY_POSITION, NBT.TAG_COMPOUND)){
				CompoundNBT pos = settingsNbt.getCompound("pos");
				int x = pos.getInt("x");
				int y = pos.getInt("y");
				int z = pos.getInt("z");
				this.pos = new BlockPos(x, y, z);
			}
		}
	}
	
	/** Rotate by 90° Clockwise */
	public void rotateCW(){
		this.rotation = this.rotation.add(Rotation.CLOCKWISE_90);
	}
	
	/** Rotate by 90° Counter-Clockwise */
	public void rotateCCW(){
		this.rotation = this.rotation.add(Rotation.COUNTERCLOCKWISE_90);
	}
	
	public void flip(){
		this.mirror = !this.mirror;
	}
	
	public void switchMode(){
		int id = this.mode.ordinal() + 1;
		this.mode = Mode.values()[id % Mode.values().length];
	}
	
	public void sendPacketToServer(Hand hand){
		MessageProjectorSync.sendToServer(this, hand);
	}
	
	public void sendPacketToClient(PlayerEntity player, Hand hand){
		MessageProjectorSync.sendToClient(player, this, hand);
	}
	
	public void setRotation(Rotation rotation){
		this.rotation = rotation;
	}
	
	public void setMode(Mode mode){
		this.mode = mode;
	}
	
	public void setMultiblock(@Nullable IMultiblock multiblock){
		this.multiblock = multiblock;
	}
	
	public void setMirror(boolean mirror){
		this.mirror = mirror;
	}
	
	public void setPlaced(boolean isPlaced){
		this.isPlaced = isPlaced;
	}
	
	public void setPos(@Nullable BlockPos pos){
		this.pos = pos;
	}
	
	public Rotation getRotation(){
		return this.rotation;
	}
	
	public boolean isMirrored(){
		return this.mirror;
	}
	
	public boolean isPlaced(){
		return this.isPlaced;
	}
	
	public Settings.Mode getMode(){
		return this.mode;
	}
	
	/**
	 * May return null to indicate that the projection has not been placed yet
	 */
	@Nullable
	public BlockPos getPos(){
		return this.pos;
	}
	
	/** May return null to indicate no multiblock has been selected yet */
	@Nullable
	public IMultiblock getMultiblock(){
		return this.multiblock;
	}
	
	public CompoundNBT toNbt(){
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(KEY_MODE, this.mode.ordinal());
		nbt.putInt(KEY_ROTATION, this.rotation.ordinal());
		nbt.putBoolean(KEY_MIRROR, this.mirror);
		nbt.putBoolean(KEY_PLACED, this.isPlaced);
		
		if(this.multiblock != null){
			nbt.putString(KEY_MULTIBLOCK, this.multiblock.getUniqueName().toString());
		}
		
		if(this.pos != null){
			CompoundNBT pos = new CompoundNBT();
			pos.putInt("x", this.pos.getX());
			pos.putInt("y", this.pos.getY());
			pos.putInt("z", this.pos.getZ());
			nbt.put(KEY_POSITION, pos);
		}
		
		return nbt;
	}
	
	public ItemStack applyTo(ItemStack stack){
		stack.getOrCreateChildTag("settings");
		stack.getTag().put("settings", this.toNbt());
		return stack;
	}
	
	@Override
	public String toString(){
		return "\"Settings\":[" + toNbt().toString() + "]";
	}
	
	public static enum Mode{
		MULTIBLOCK_SELECTION, PROJECTION;
		
		final String translation;
		private Mode(){
			this.translation = "desc.immersivepetroleum.info.projector.mode_" + ordinal();
		}
		
		public ITextComponent getTranslated(){
			return new TranslationTextComponent(this.translation);
		}
	}
}

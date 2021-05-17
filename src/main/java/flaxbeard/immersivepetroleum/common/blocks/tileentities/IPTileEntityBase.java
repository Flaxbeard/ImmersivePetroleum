package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;

public abstract class IPTileEntityBase extends TileEntity{
	public IPTileEntityBase(TileEntityType<?> tileEntityTypeIn){
		super(tileEntityTypeIn);
	}
	
	@Nonnull
	public World getWorldNonnull(){
		return Objects.requireNonNull(super.getWorld());
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket(){
		return new SUpdateTileEntityPacket(this.pos, 3, getUpdateTag());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag){
		read(state, tag);
	}
	
	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = new CompoundNBT();
		write(nbt);
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
		read(getBlockState(), pkt.getNbtCompound());
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound){
		super.write(compound);
		writeCustom(compound);
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound){
		super.read(state, compound);
		readCustom(state, compound);
	}
	
	protected abstract void writeCustom(CompoundNBT compound);
	
	protected abstract void readCustom(BlockState state, CompoundNBT compound);
}

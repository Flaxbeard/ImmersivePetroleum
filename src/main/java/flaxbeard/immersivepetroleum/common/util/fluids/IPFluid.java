package flaxbeard.immersivepetroleum.common.util.fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.StateHolder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class IPFluid extends FlowingFluid{
	public static final List<IPFluid> LIST=new ArrayList<>();

	protected final String fluidName;
	protected final ResourceLocation stillTexture;
	protected final ResourceLocation flowingTexture;
	protected IPFluid source;
	protected IPFluid flowing;
	public Block block;
	protected Item bucket;
	@Nullable
	protected final Consumer<FluidAttributes.Builder> buildAttributes;
	
	public IPFluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture){
		this(name, stillTexture, flowingTexture, null, true);
	}
	
	public IPFluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable Consumer<FluidAttributes.Builder> buildAttributes){
		this(name, stillTexture, flowingTexture, buildAttributes, true);
	}
	
	public IPFluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable Consumer<FluidAttributes.Builder> buildAttributes, boolean isSource){
		this.fluidName=name;
		this.stillTexture=stillTexture;
		this.flowingTexture=flowingTexture;
		this.buildAttributes=buildAttributes;
		IPContent.registeredIPFluids.add(this);
		if(!isSource){
			flowing=this;
			setRegistryName(ImmersivePetroleum.MODID, fluidName+"_flowing");
		}else{
			source=this;
			this.block=createBlock();
			this.block.setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, fluidName+"_fluid_block"));
			IPContent.registeredIPBlocks.add(this.block);
			
			this.bucket=createBucket();
			this.bucket.setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, fluidName+"_bucket"));
			IPContent.registeredIPItems.add(this.bucket);
			
			this.flowing=createFlowing();
			this.flowing.source=this;
			this.flowing.bucket=this.bucket;
			this.flowing.block=this.block;
			this.flowing.setDefaultState(this.flowing.getStateContainer().getBaseState().with(LEVEL_1_8, 7));
			
			setRegistryName(new ResourceLocation(ImmersivePetroleum.MODID, fluidName));
			LIST.add(this);
		}
	}
	
	protected BucketItem createBucket(){
		BucketItem bucket=new BucketItem(()->this.source, new Item.Properties().maxStackSize(1).group(ImmersivePetroleum.creativeTab)){
			@Override
			public ItemStack getContainerItem(ItemStack itemStack){
				return new ItemStack(Items.BUCKET);
			}
			
			@Override
			public boolean hasContainerItem(ItemStack stack){
				return true;
			}
			
			@Override
			public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt){
				return new FluidBucketWrapper(stack);
			}
		};
		return bucket;
	}
	
	protected IPFluid createFlowing(){
		IPFluid flowing=new IPFluid(this.fluidName, this.stillTexture, this.flowingTexture, this.buildAttributes, false){
			@Override
			protected void fillStateContainer(Builder<Fluid, FluidState> builder){
				super.fillStateContainer(builder);
				builder.add(LEVEL_1_8);
			}
		};
		return flowing;
	}
	
	protected FlowingFluidBlock createBlock(){
		FlowingFluidBlock block=new FlowingFluidBlock(()->this.source, Block.Properties.create(Material.WATER)){
			@Override
			protected void fillStateContainer(Builder<Block, BlockState> builder){
				super.fillStateContainer(builder);
				builder.add(IPFluid.this.getStateContainer().getProperties().toArray(new Property[0]));
			}
			
			@Override
			public FluidState getFluidState(BlockState state){
				FluidState baseState=super.getFluidState(state);
				for(Property<?> prop: IPFluid.this.getStateContainer().getProperties())
					if(prop!=FlowingFluidBlock.LEVEL)
						baseState = withCopiedValue(prop, baseState, state);
				return baseState;
			}
			
			private <T extends StateHolder<?, T>, S extends Comparable<S>> T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom){
				return oldState.with(prop, copyFrom.get(prop));
			}
		};
		return block;
	}
	
	@Override
	protected void beforeReplacingBlock(IWorld arg0, BlockPos arg1, BlockState arg2){
	}
	
	@Override
	protected boolean canSourcesMultiply(){
		return false;
	}
	
	@Override
	public Fluid getFlowingFluid(){
		return this.flowing;
	}
	
	@Override
	public Fluid getStillFluid(){
		return this.source;
	}
	
	@Override
	public Item getFilledBucket(){
		return this.bucket;
	}
	
	@Override
	protected int getLevelDecreasePerBlock(IWorldReader arg0){
		return 1;
	}
	
	@Override
	protected int getSlopeFindDistance(IWorldReader arg0){
		return 4;
	}
	
	// TODO Block Render Layer
	/*
	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.TRANSLUCENT;
	}
	*/
	
	@Override
	protected boolean canDisplace(FluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_){
		return p_215665_5_ == Direction.DOWN && !isEquivalentTo(p_215665_4_);
	}
	
	@Override
	public int getTickRate(IWorldReader p_205569_1_){
		return 5;
	}
	
	@Override
	protected float getExplosionResistance(){
		return 100;
	}
	
	@Override
	protected BlockState getBlockState(FluidState state){
		return block.getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(state));
	}
	
	@Override
	public boolean isSource(FluidState state){
		return state.getFluid() == source;
	}
	
	@Override
	public int getLevel(FluidState state){
		if(isSource(state))
			return 8;
		else
			return state.get(LEVEL_1_8);
	}
	
	@Override
	public boolean isEquivalentTo(Fluid fluidIn){
		return fluidIn == this.source || fluidIn == this.flowing;
	}
	
	public static Consumer<FluidAttributes.Builder> createBuilder(int density, int viscosity){
		return builder -> builder.viscosity(viscosity).density(density);
	}
	
	@Override
	protected FluidAttributes createAttributes(){
		FluidAttributes.Builder builder=FluidAttributes.builder(this.stillTexture, this.flowingTexture);
		if(this.buildAttributes!=null)
			this.buildAttributes.accept(builder);
		return builder.build(this);
	}
}

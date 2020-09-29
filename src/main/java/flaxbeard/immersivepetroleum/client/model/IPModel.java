package flaxbeard.immersivepetroleum.client.model;

import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public abstract class IPModel extends Model{
	private TextureAtlasSprite sprite;
	public IPModel(Function<ResourceLocation, RenderType> renderTypeIn){
		super(renderTypeIn);
	}
	
	public final void spriteInit(AtlasTexture atlas){
		this.sprite=atlas.getSprite(textureLocation());
		
		this.textureWidth = (int) getAtlasX();
		this.textureHeight = (int) getAtlasY();
	}
	
	/** Unique name-id for this model */
	public abstract String id();
	
	/** Return the texture location */
	public abstract ResourceLocation textureLocation();
	
	/** This is where the model parts should be created, with {@link IPModel#createRenderer(Model, int, int)} */
	public abstract void init();
	
	protected final ModelRenderer createRenderer(Model model, int texOffX, int texOffY){
		int uStart = (int) (this.sprite.getMinU() * model.textureWidth);
		int vStart = (int) (this.sprite.getMinV() * model.textureHeight);
		
		return new ModelRenderer(model, uStart + texOffX, vStart + texOffY);
	}
	
	private float getAtlasX(){
		return this.sprite.getWidth() / (this.sprite.getMaxU() - this.sprite.getMinU());
	}
	
	private float getAtlasY(){
		return this.sprite.getHeight() / (this.sprite.getMaxV() - this.sprite.getMinV());
	}
}

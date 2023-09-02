package com.teamderpy.shouldersurfing.client;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.teamderpy.shouldersurfing.api.callback.IAdaptiveItemCallback;
import com.teamderpy.shouldersurfing.config.Config;
import com.teamderpy.shouldersurfing.mixins.ActiveRenderInfoAccessor;
import com.teamderpy.shouldersurfing.plugin.ShoulderSurfingRegistrar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;

public class ShoulderHelper
{
	public static final float DEG_TO_RAD = ((float)Math.PI / 180F);
	private static final Predicate<Entity> ENTITY_IS_PICKABLE = entity -> !entity.isSpectator() && entity.isPickable();
	private static final ResourceLocation PULL_PROPERTY = new ResourceLocation("pull");
	private static final ResourceLocation THROWING_PROPERTY = new ResourceLocation("throwing");
	private static final ResourceLocation CHARGED_PROPERTY = new ResourceLocation("charged");
	
	public static ShoulderLook shoulderSurfingLook(ActiveRenderInfo camera, Entity entity, float partialTicks, double distanceSq)
	{
		Vector3d cameraOffset = ShoulderHelper.calcCameraOffset(camera, ShoulderRenderer.getInstance().getCameraDistance());
		Vector3d headOffset = ShoulderHelper.calcRayTraceHeadOffset(camera, cameraOffset);
		Vector3d cameraPos = entity.getEyePosition(partialTicks).add(cameraOffset);
		Vector3d viewVector = entity.getViewVector(partialTicks);
		
		if(Config.CLIENT.limitPlayerReach() && headOffset.lengthSqr() < distanceSq)
		{
			distanceSq -= headOffset.lengthSqr();
		}
		
		double distance = Math.sqrt(distanceSq) + cameraOffset.distanceTo(headOffset);
		Vector3d traceEnd = cameraPos.add(viewVector.scale(distance));
		return new ShoulderLook(cameraPos, traceEnd, headOffset);
	}
	
	public static Vector3d calcCameraOffset(@Nonnull ActiveRenderInfo camera, double distance)
	{
		ActiveRenderInfoAccessor accessor = (ActiveRenderInfoAccessor) camera;
		double dX = camera.getUpVector().x() * ShoulderInstance.getInstance().getOffsetY() + accessor.getLeft().x() * ShoulderInstance.getInstance().getOffsetX() + camera.getLookVector().x() * -ShoulderInstance.getInstance().getOffsetZ();
		double dY = camera.getUpVector().y() * ShoulderInstance.getInstance().getOffsetY() + accessor.getLeft().y() * ShoulderInstance.getInstance().getOffsetX() + camera.getLookVector().y() * -ShoulderInstance.getInstance().getOffsetZ();
		double dZ = camera.getUpVector().z() * ShoulderInstance.getInstance().getOffsetY() + accessor.getLeft().z() * ShoulderInstance.getInstance().getOffsetX() + camera.getLookVector().z() * -ShoulderInstance.getInstance().getOffsetZ();
		return new Vector3d(dX, dY, dZ).normalize().scale(distance);
	}
	
	public static Vector3d calcRayTraceHeadOffset(@Nonnull ActiveRenderInfo camera, Vector3d cameraOffset)
	{
		Vector3d lookVector = new Vector3d(camera.getLookVector());
		return ShoulderHelper.calcPlaneWithLineIntersection(Vector3d.ZERO, lookVector, cameraOffset, lookVector);
	}
	
	public static Vector3d calcPlaneWithLineIntersection(Vector3d planePoint, Vector3d planeNormal, Vector3d linePoint, Vector3d lineNormal)
	{
		double distance = (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(lineNormal);
		return linePoint.add(lineNormal.scale(distance));
	}
	
	public static RayTraceResult traceBlocksAndEntities(ActiveRenderInfo camera, PlayerController gameMode, double playerReachOverride, RayTraceContext.FluidMode fluidContext, float partialTick, boolean traceEntities, boolean shoulderSurfing)
	{
		Entity entity = camera.getEntity();
		double playerReach = Math.max(gameMode.getPickRange(), playerReachOverride);
		RayTraceResult blockHit = traceBlocks(camera, entity, fluidContext, playerReach, partialTick, shoulderSurfing);
		
		if(!traceEntities)
		{
			return blockHit;
		}
		
		Vector3d eyePosition = entity.getEyePosition(partialTick);
		
		if(gameMode.hasFarPickRange())
		{
			playerReach = Math.max(playerReach, gameMode.getPlayerMode().isCreative() ? 6.0D : 3.0D);
		}
		
		if(blockHit != null)
		{
			playerReach = blockHit.getLocation().distanceTo(eyePosition);
		}
		
		EntityRayTraceResult entityHit = traceEntities(camera, entity, playerReach, partialTick, shoulderSurfing);
		
		if(entityHit != null)
		{
			double distance = eyePosition.distanceTo(entityHit.getLocation());

			if(distance < playerReach || blockHit == null)
			{
				return entityHit;
			}
		}
		
		return blockHit;
	}
	
	public static EntityRayTraceResult traceEntities(ActiveRenderInfo camera, Entity entity, double playerReach, float partialTick, boolean shoulderSurfing)
	{
		double playerReachSq = playerReach * playerReach;
		Vector3d viewVector = entity.getViewVector(1.0F)
			.scale(playerReach);
		Vector3d eyePosition = entity.getEyePosition(partialTick);
		AxisAlignedBB aabb = entity.getBoundingBox()
			.expandTowards(viewVector)
			.inflate(1.0D, 1.0D, 1.0D);
		
		if(shoulderSurfing)
		{
			ShoulderLook look = ShoulderHelper.shoulderSurfingLook(camera, entity, partialTick, playerReachSq);
			Vector3d from = eyePosition.add(look.headOffset());
			Vector3d to = look.traceEndPos();
			aabb = aabb.move(look.headOffset());
			return ProjectileHelper.getEntityHitResult(entity, from, to, aabb, ENTITY_IS_PICKABLE, from.distanceToSqr(to));
		}
		else
		{
			Vector3d from = eyePosition;
			Vector3d to = from.add(viewVector);
			return ProjectileHelper.getEntityHitResult(entity, from, to, aabb, ENTITY_IS_PICKABLE, playerReachSq);
		}
	}
	
	public static BlockRayTraceResult traceBlocks(ActiveRenderInfo camera, Entity entity, RayTraceContext.FluidMode fluidContext, double distance, float partialTick, boolean shoulderSurfing)
	{
		Vector3d eyePosition = entity.getEyePosition(partialTick);
		
		if(shoulderSurfing)
		{
			ShoulderLook look = ShoulderHelper.shoulderSurfingLook(camera, entity, partialTick, distance * distance);
			Vector3d from = eyePosition.add(look.headOffset());
			Vector3d to = look.traceEndPos();
			return entity.level.clip(new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, fluidContext, entity));
		}
		else
		{
			Vector3d from = eyePosition;
			Vector3d view = entity.getViewVector(partialTick);
			Vector3d to = from.add(view.scale(distance));
			return entity.level.clip(new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, fluidContext, entity));
		}
	}
	
	public static boolean isHoldingAdaptiveItem()
	{
		Minecraft minecraft = Minecraft.getInstance();
		
		if(minecraft.cameraEntity instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity) minecraft.cameraEntity;
			boolean result = isHoldingAdaptiveItemInternal(minecraft, entity);
			
			for(IAdaptiveItemCallback adaptiveItemCallback : ShoulderSurfingRegistrar.getInstance().getAdaptiveItemCallbacks())
			{
				result |= adaptiveItemCallback.isHoldingAdaptiveItem(minecraft, entity);
			}
			
			return result;
		}
		
		return false;
	}
	
	private static boolean isHoldingAdaptiveItemInternal(Minecraft minecraft, LivingEntity entity)
	{
		Item useItem = entity.getUseItem().getItem();
		List<? extends String> overrides = Config.CLIENT.getAdaptiveCrosshairItems();
		
		if(ItemModelsProperties.getProperty(useItem, PULL_PROPERTY) != null || ItemModelsProperties.getProperty(useItem, THROWING_PROPERTY) != null)
		{
			return true;
		}
		else if(overrides.contains(Registry.ITEM.getKey(useItem).toString()))
		{
			return true;
		}
		
		for(ItemStack handStack : entity.getHandSlots())
		{
			Item handItem = handStack.getItem();
			
			if(ItemModelsProperties.getProperty(handItem, CHARGED_PROPERTY) != null)
			{
				return true;
			}
			else if(overrides.contains(Registry.ITEM.getKey(handItem).toString()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static double angle(Vector3f a, Vector3f b)
	{
		return Math.acos(a.dot(b) / (length(a) * length(b)));
	}
	
	public static double length(Vector3f vec)
	{
		return MathHelper.sqrt(vec.x() * vec.x() + vec.y() * vec.y() + vec.z() * vec.z());
	}
	
	public static class ShoulderLook
	{
		private final Vector3d cameraPos;
		private final Vector3d traceEndPos;
		private final Vector3d headOffset;
		
		public ShoulderLook(Vector3d cameraPos, Vector3d traceEndPos, Vector3d headOffset)
		{
			this.cameraPos = cameraPos;
			this.traceEndPos = traceEndPos;
			this.headOffset = headOffset;
		}
		
		public Vector3d cameraPos()
		{
			return this.cameraPos;
		}
		
		public Vector3d traceEndPos()
		{
			return this.traceEndPos;
		}
		
		public Vector3d headOffset()
		{
			return this.headOffset;
		}
	}
}

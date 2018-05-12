package com.cout970.magneticraft.tilerenderer.multiblock

import com.cout970.magneticraft.block.Multiblocks
import com.cout970.magneticraft.misc.tileentity.RegisterRenderer
import com.cout970.magneticraft.tileentity.multiblock.TileSolarMirror
import com.cout970.magneticraft.tileentity.multiblock.TileSolarTower
import com.cout970.magneticraft.tilerenderer.core.ModelCache
import com.cout970.magneticraft.tilerenderer.core.Utilities
import com.cout970.magneticraft.tilerenderer.core.modelOf
import com.cout970.magneticraft.util.toRads
import com.cout970.magneticraft.util.vector.*
import com.cout970.vector.extensions.rotateZ
import net.minecraft.util.math.BlockPos

@RegisterRenderer(TileSolarTower::class)
object TileRendererSolarTower : TileRendererMultiblock<TileSolarTower>(
        modelLocation = modelOf(Multiblocks.solarTower)
) {

    override fun renderModels(models: List<ModelCache>, te: TileSolarTower) {
        Utilities.rotateFromCenter(te.facing, 0f)
        translate(0, 0, -1)
        models[0].renderTextured()
    }
}

@RegisterRenderer(TileSolarMirror::class)
object TileRendererSolarMirror : TileRendererMultiblock<TileSolarMirror>(
        modelLocation = modelOf(Multiblocks.solarMirror),
        filters = listOf<(String) -> Boolean>(
                { !it.contains("mirror") },
                { it.contains("mirror") }
        )
) {

    fun getAngles(from: BlockPos, to: BlockPos): Pair<Float, Float> {

        val diff = to - from
        val dirToTower = diff.toVec3d().normalize()

        val worldTime = world.worldTime
        val time = (worldTime % 24000L).toInt()
        val sunAngle = (time / 12000f) * 180f - 90f

        if (sunAngle > 100 && sunAngle < 258) {
            return Pair(0f, 0f)
        }

        val dirToSun = vec3Of(0, 1, 0).rotateZ(sunAngle.toRads()).normalize()

        val normal = (dirToSun + dirToTower).normalize()

        val plane = vec3Of(normal.x, 0, normal.z)
        val normPlane = plane.normalize()
        val angleY = Math.atan2(normPlane.x, normPlane.z) + 90.0.toRads()
        val realAngle = Math.atan2(plane.length, normal.y)
        val angleX = when {
            realAngle > 90.toRads() -> 0.0
            else -> realAngle
        }

//        Debug print of the vectors
//        stackMatrix {
//            translate(8 / 16f, 1 + 13 / 16f, 8 / 16f)
//            Utilities.renderLine(vec3Of(0), dirToSun * 5, vec3Of(1f, 1f, 0f))
//            Utilities.renderLine(vec3Of(0), dirToTower * 5, vec3Of(0f, 1f, 0f))
//            Utilities.renderLine(vec3Of(0), normal * 2.5, vec3Of(1f, 1f, 1f))
//
//
//            val vecA = vec3Of(0, 0, 1).rotateY(angleY)
//            val vecB = vec3Of(0, 1, 0).rotateZ(angleX)
//            Utilities.renderLine(vec3Of(0), vecA * 3.0, vec3Of(1f, 0f, 0f))
//            Utilities.renderLine(vec3Of(0), vecB * 3.0, vec3Of(0f, 0f, 1f))
//        }

        return Pair(
                Math.toDegrees(angleY).toFloat(),
                Math.toDegrees(angleX).toFloat()
        )
    }

    fun diff(src: Float, dst: Float): Float {
        return Math.toDegrees(Math.atan2(Math.sin(src.toRads() - dst.toRads()), Math.cos(src.toRads() - dst.toRads()))).toFloat()
    }

    override fun renderModels(models: List<ModelCache>, te: TileSolarMirror) {
        stackMatrix {
            Utilities.rotateFromCenter(te.facing, 180f)
            translate(0, 0, 1)
            models[0].renderTextured()
        }
        val rot = te.facing.toBlockPos().toVec3d()
        translate(rot)

        val dst = te.solarMirrorModule.solarTowerPos
        val angleX: Float
        val angleY: Float

        if (dst != null) {
            val mod = te.solarMirrorModule
            val oldTime = mod.deltaTime
            mod.deltaTime = System.currentTimeMillis()

            val delta = (Math.min(mod.deltaTime - oldTime, 1000)) / 1000f
            val src = te.solarMirrorModule.mirrorPos
            val (y, x) = getAngles(src, dst)

            mod.angleX += diff(x, mod.angleX) * delta
            mod.angleY += diff(y, mod.angleY) * delta

            angleX = mod.angleX
            angleY = mod.angleY
        } else {
            angleX = 0f
            angleY = 0f
        }

        stackMatrix {
            rotationCenter(8 / 16f, 1 + 13 / 16f, 8 / 16f) {
                rotate(angleY, 0, 1, 0)
                rotate(angleX, 0f, 0f, 1f)
            }
            models[1].renderTextured()
        }
    }
}